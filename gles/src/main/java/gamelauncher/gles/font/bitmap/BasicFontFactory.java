/*
 * Copyright (C) 2023 Lorenz Wrobel. - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package gamelauncher.gles.font.bitmap;

import gamelauncher.engine.GameLauncher;
import gamelauncher.engine.render.font.Font;
import gamelauncher.engine.render.font.FontFactory;
import gamelauncher.engine.resource.ResourceStream;
import gamelauncher.engine.util.GameException;
import gamelauncher.gles.GLES;

public class BasicFontFactory implements FontFactory {

    private final GameLauncher launcher;
    private final GLES gles;

    public BasicFontFactory(GLES gles, GameLauncher launcher) {
        this.gles = gles;
        this.launcher = launcher;
    }

    @Override public Font createFont(ResourceStream stream, boolean close) throws GameException {
        return new BasicFont(stream);
    }
}
