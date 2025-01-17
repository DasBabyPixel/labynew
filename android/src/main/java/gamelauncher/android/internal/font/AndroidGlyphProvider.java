/*
 * Copyright (C) 2023 Lorenz Wrobel. - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package gamelauncher.android.internal.font;

import android.graphics.*;
import de.dasbabypixel.api.property.BooleanValue;
import de.dasbabypixel.api.property.NumberInvalidationListener;
import de.dasbabypixel.api.property.NumberValue;
import gamelauncher.android.AndroidGameLauncher;
import gamelauncher.engine.data.DataUtil;
import gamelauncher.engine.render.Frame;
import gamelauncher.engine.render.GameItem;
import gamelauncher.engine.render.font.GlyphProvider;
import gamelauncher.engine.render.model.CombinedModelsModel;
import gamelauncher.engine.render.model.GlyphStaticModel;
import gamelauncher.engine.render.model.Model;
import gamelauncher.engine.render.shader.ShaderProgram;
import gamelauncher.engine.resource.AbstractGameResource;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.Key;
import gamelauncher.engine.util.concurrent.ExecutorThreadService;
import gamelauncher.engine.util.function.GameRunnable;
import gamelauncher.engine.util.logging.Logger;
import gamelauncher.engine.util.text.Component;
import gamelauncher.engine.util.text.serializer.PlainTextComponentSerializer;
import gamelauncher.gles.GLES;
import gamelauncher.gles.font.bitmap.*;
import gamelauncher.gles.model.GLESCombinedModelsModel;
import gamelauncher.gles.model.Texture2DModel;
import gamelauncher.gles.texture.GLESTexture;
import gamelauncher.gles.util.MemoryManagement;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.Consumer;
import org.joml.Math;
import org.joml.Vector4i;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AndroidGlyphProvider extends AbstractGameResource implements GlyphProvider {
    private static final Logger logger = Logger.logger();
    private final GLES gles;
    private final AndroidGameLauncher launcher;
    private final ExecutorThreadService service;
    private Frame frame;
    private DynamicSizeTextureAtlas textureAtlas;

    public AndroidGlyphProvider(GLES gles) throws GameException {
        this.gles = gles;
        this.launcher = (AndroidGameLauncher) gles.launcher();
        this.service = launcher.threads().newWorkStealingPool();
    }

    @Override public GlyphStaticModel loadStaticModel(Component text, int pixelHeight) throws GameException {
        if (frame == null) {
            this.frame = launcher.frame().newFrame();
            this.textureAtlas = new DynamicSizeTextureAtlas(gles, launcher, this.frame.renderThread());
        }
        Key fkey = text.style().font();
        if (fkey == null) fkey = new Key("fonts/calibri.ttf");
        Typeface tyf = Typeface.createFromAsset(launcher.activity().getAssets(), fkey.namespace() + "/" + fkey.key());
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(pixelHeight);
        paint.setColor(0Xffffffff);
        paint.setTypeface(tyf);

        Paint.FontMetrics fm = paint.getFontMetrics();

        @SuppressWarnings("UnnecessaryLocalVariable") float scale = pixelHeight;
        float descent = -fm.descent;
        float ascent = fm.ascent;
        char[] ar = PlainTextComponentSerializer.serialize(text).toCharArray();
        Collection<CompletableFuture<AtlasEntry>> futures = new ArrayList<>();
        Queue<Consumer<Void>> tasks = new ConcurrentLinkedQueue<>();
        Map<GLESTexture, List<AtlasEntry>> entries = new HashMap<>();
        for (char ch : ar) {
            GlyphKey key = new GlyphKey(scale, ch);
            CompletableFuture<AtlasEntry> fentry = this.requireGlyphKey(tasks, key, paint, ch);
            futures.add(fentry);
        }

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            service.submit(new FontLoadRunnable(tasks));
        }

//        this.textureAtlas.byTexture().keySet().forEach(t -> t.write());
        GlyphModelWrapper wrapper = new GlyphModelWrapper(null, 0, 0, ascent, descent);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            for (CompletableFuture<AtlasEntry> f : futures) {
                List<AtlasEntry> e = entries.computeIfAbsent(f.getNow(null).texture, k -> new ArrayList<>());
                e.add(f.getNow(null));
            }
            Collection<Model> meshes = new ArrayList<>();
            float mwidth = 0;
            float mheight = 0;
            float xpos = 0;
            float z = 0;
            for (Map.Entry<GLESTexture, List<AtlasEntry>> entry : entries.entrySet()) {
                for (int i = 0; i < entry.getValue().size(); i++) {
                    AtlasEntry e = entry.getValue().get(i);
                    if (i == 0) xpos = -e.entry.data.bearingX;
                    Vector4i bd = e.bounds;

                    NumberValue tw = e.texture.width();
                    NumberValue th = e.texture.height();
                    NumberValue tl = NumberValue.constant(bd.x + 0.5F).divide(tw);
                    NumberValue tb = NumberValue.constant(bd.y + 0.5F).divide(th);
                    NumberValue tr = NumberValue.constant(bd.x + 0.5F).add(bd.z + 0.5F).divide(tw);
                    NumberValue tt = NumberValue.constant(bd.y + 0.5F).add(bd.w + 0.5F).divide(th);

                    GlyphData data = e.entry.data;
                    float pb = -data.bearingY - data.height;
                    float pt = pb + data.height;
                    float pl = xpos + data.bearingX;
                    float pr = pl + data.width;
                    float width = pr - pl;
                    float height = pt - pb;
                    float x = pl + width / 2;
                    float y = pt + height / 2;

                    mheight = Math.max(mheight, height);
                    mwidth = Math.max(mwidth, pl + width);
                    DynamicModel m = new DynamicModel(e, tl, tr, tt, tb);

                    GameItem gi = new GameItem(m);
                    gi.position(x, y, z);
                    gi.scale(width, height, 1);
                    meshes.add(gi.createModel());
                    xpos += e.entry.data.advance;

                }
            }

            CombinedModelsModel cmodel = new GLESCombinedModelsModel(meshes.toArray(new Model[0]));
            GameItem gi = new GameItem(cmodel);
            gi.addColor(1, 1, 1, 0);
            GameItem.GameItemModel gim = gi.createModel();
            try {
                wrapper.handle(gim);
            } catch (GameException e) {
                throw new RuntimeException(e);
            }
            wrapper.width(mwidth);
            wrapper.height(mheight);
            frame.launcher().guiManager().redrawAll();
        });
        return wrapper;
    }

    public CompletableFuture<Void> releaseGlyphKey(GlyphKey key) {
        if (key.required.decrementAndGet() == 0) {
            return this.textureAtlas.removeGlyph(this.getId(key));
        }
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<AtlasEntry> requireGlyphKey(Queue<Consumer<Void>> tasks, GlyphKey key, Paint paint, char ch) throws GameException {
//        return this.frame.launcher().threads().cached.submit(() -> {
        int id = this.getId(key);
        DynamicSizeTextureAtlas.AddFuture af = textureAtlas.addGlyph(tasks, id, data -> {

            char[] ca1 = new char[]{ch};
            Rect bounds = new Rect();
            float w = paint.measureText(new String(ca1));
            paint.getTextBounds(ca1, 0, 1, bounds);

            MemoryManagement memoryManagement = gles.memoryManagement();
            GlyphData gdata = new GlyphData();
            gdata.advance = w;
            gdata.bearingX = bounds.left;
            gdata.bearingY = bounds.bottom;
            gdata.width = bounds.width();
            gdata.height = bounds.height();

            Bitmap bitmap = Bitmap.createBitmap(2 + gdata.width, 2 + gdata.height, Bitmap.Config.ALPHA_8);
            bitmap.eraseColor(0x00000000);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawText(ca1, 0, 1, -gdata.bearingX, gdata.height - gdata.bearingY, paint);

            ByteBuffer abuf = memoryManagement.allocDirect((2 + gdata.width) * (2 + gdata.height));
            bitmap.copyPixelsToBuffer(abuf);

            // Setup for usage by GLESTexture
            ByteBuffer buf = memoryManagement.calloc(DataUtil.BYTES_INT * abuf.capacity() + DataUtil.BYTES_INT * 2 + GLESTexture.SIGNATURE_RAW.length);
            buf.put(GLESTexture.SIGNATURE_RAW);
            buf.putInt(gdata.width + 2);
            buf.putInt(gdata.height + 2);
            //			buf.put(apxls);
            for (int i = 0; i < abuf.capacity(); i += 1) {
                buf.position(buf.position() + 3);
                buf.put(abuf.get(i));
//                if (b != 0) System.out.println(b);
            }
            buf.flip();
            memoryManagement.free(abuf);
            byte[] pixels = new byte[buf.capacity()];
            buf.get(pixels);
            memoryManagement.free(buf);

            return new GlyphEntry(gdata, key, pixels);
        });
        af.future().exceptionally(ex -> {
            logger.error(ex);
            return null;
        });
        if (!af.suc()) {
            throw new GameException("Could not add glyph to texture atlas");
        }
        return af.future();
//        });
    }

    @Override public CompletableFuture<Void> cleanup0() throws GameException {
        return CompletableFuture.allOf(textureAtlas.cleanup(), frame.cleanup());
    }

    public int getId(GlyphKey key) {
        return key.hashCode();
    }

    class FontLoadRunnable implements GameRunnable {
        private final Queue<Consumer<Void>> tasks;

        public FontLoadRunnable(Queue<Consumer<Void>> tasks) {
            this.tasks = tasks;
        }

        @Override public void run() {
            Consumer<Void> task = tasks.poll();
            if (task == null) return;
            task.accept(null);
            while ((task = tasks.poll()) != null) {
                task.accept(null);
            }
        }
    }

    private class DynamicModel extends AbstractGameResource implements Model {

        private final AtlasEntry e;
        private final NumberValue tl;
        private final NumberValue tr;
        private final NumberValue tt;
        private final NumberValue tb;
        private final BooleanValue invalid = BooleanValue.trueValue();

        private Texture2DModel texture2DModel;

        public DynamicModel(AtlasEntry e, NumberValue tl, NumberValue tr, NumberValue tt, NumberValue tb) {
            this.e = e;
            this.tl = tl;
            this.tr = tr;
            this.tt = tt;
            this.tb = tb;
            NumberInvalidationListener invalidationListener = property -> invalid.value(true);
            tl.addListener(invalidationListener);
            tr.addListener(invalidationListener);
            tt.addListener(invalidationListener);
            tb.addListener(invalidationListener);
        }

        @Override public void render(ShaderProgram program) throws GameException {
            if (invalid.booleanValue()) {
                invalid.value(false);
                if (texture2DModel != null) {
                    texture2DModel.cleanup();
                }
                texture2DModel = new Texture2DModel(e.texture, tl.floatValue(), 1 - tb.floatValue(), tr.floatValue(), 1 - tt.floatValue());
            }
            texture2DModel.render(program);
        }

        @Override protected CompletableFuture<Void> cleanup0() throws GameException {
            CompletableFuture<Void> f1 = releaseGlyphKey(e.entry.key);
            CompletableFuture<Void> f2 = null;
            if (texture2DModel != null) f2 = texture2DModel.cleanup();
            return f2 == null ? f1 : CompletableFuture.allOf(f1, f2);
        }
    }
}
