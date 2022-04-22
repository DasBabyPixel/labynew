package gamelauncher.lwjgl.render;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicReference;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import gamelauncher.engine.GameException;
import gamelauncher.engine.render.Camera;
import gamelauncher.engine.render.DrawContext;
import gamelauncher.engine.render.Model;
import gamelauncher.engine.render.Transformations;
import gamelauncher.lwjgl.render.GameItem.GameItemModel;
import gamelauncher.lwjgl.render.Mesh.MeshModel;

public class LWJGLDrawContext implements DrawContext {

	private static final Vector3f X_AXIS = new Vector3f(1, 0, 0);
	private static final Vector3f Y_AXIS = new Vector3f(0, 1, 0);
	private static final Vector3f Z_AXIS = new Vector3f(0, 0, 1);

	private final LWJGLWindow window;
	private final double tx, ty, tz;
	private final double sx, sy, sz;
	private final Matrix4f projectionMatrix;
	private final Matrix4f modelViewMatrix = new Matrix4f();
	private final Matrix4f viewMatrix;
	private final Matrix4f tempMatrix = new Matrix4f();
	private final AtomicReference<ShaderProgram> shaderProgram;

	public LWJGLDrawContext(LWJGLWindow window) {
		this(window, 0, 0, 0, 1, 1, 1);
	}

	private LWJGLDrawContext(LWJGLWindow window, double tx, double ty, double tz, double sx, double sy, double sz) {
		this(window, tx, ty, tz, sx, sy, sz, new AtomicReference<>(), new Matrix4f(), new Matrix4f());
	}

	private LWJGLDrawContext(LWJGLWindow window, double tx, double ty, double tz, double sx, double sy, double sz,
			AtomicReference<ShaderProgram> shaderProgram, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
		this.window = window;
		this.tx = tx;
		this.ty = ty;
		this.tz = tz;
		this.sx = sx;
		this.sy = sy;
		this.sz = sz;
		this.shaderProgram = shaderProgram;
		this.projectionMatrix = projectionMatrix;
		this.viewMatrix = viewMatrix;
	}

	@Override
	public void setProjectionMatrix(Transformations.Projection projection) throws GameException {
		if (projection instanceof Transformations.Projection.Projection3D) {
			Transformations.Projection.Projection3D p3d = (Transformations.Projection.Projection3D) projection;
			float aspectRatio = (float) window.framebufferWidth.get() / (float) window.framebufferHeight.get();
			projectionMatrix.setPerspective(p3d.fov, aspectRatio, p3d.zNear, p3d.zFar);
		}
	}

	@Override
	public void drawRect(double x, double y, double w, double h, Color color) {

	}

	@Override
	public void drawTriangle(double x1, double y1, double x2, double y2, double x3, double y3, Color color) {

	}

	@Override
	public void drawModel(Model model, double x, double y, double z, double rx, double ry, double rz)
			throws GameException {
		drawModel(model, x, y, z, rx, ry, rz, 1, 1, 1);
	}

	@Override
	public void drawModel(Model model, double x, double y, double z, double rx, double ry, double rz, double sx,
			double sy, double sz) throws GameException {
		modelViewMatrix.identity();
		pDrawModel(model, x, y, z, rx, ry, rz, sx, sy, sz);
	}

	private void pDrawModel(Model model, double x, double y, double z, double rx, double ry, double rz, double sx,
			double sy, double sz) throws GameException {
		Mesh mesh = null;
		if (model instanceof MeshModel) {
			mesh = ((MeshModel) model).mesh;
		} else if (model instanceof GameItemModel) {
			GameItem item = ((GameItemModel) model).gameItem;
			item.applyToTransformationMatrix(modelViewMatrix);
			pDrawModel(item.getModel(), x, y, z, rx, ry, rz, sx, sy, sz);
			return;
		}
		modelViewMatrix.translate((float) (x + this.tx), (float) (y + this.ty), (float) (z + this.tz));
		modelViewMatrix.rotateXYZ((float) Math.toRadians(-rx), (float) Math.toRadians(-ry),
				(float) Math.toRadians(-rz));
		modelViewMatrix.scale((float) sx, (float) sy, (float) sz);
		drawMesh(mesh);
	}

	@Override
	public DrawContext translate(double x, double y, double z) {
		return new LWJGLDrawContext(window, tx + x, ty + y, tz + z, sx, sy, sz, shaderProgram, projectionMatrix,
				viewMatrix);
	}

	@Override
	public DrawContext scale(double x, double y, double z) {
		return new LWJGLDrawContext(window, tx, ty, tz, sx * x, sy * y, sz * z, shaderProgram, projectionMatrix,
				viewMatrix);
	}

	@Override
	public void drawModel(Model model, double x, double y, double z) throws GameException {
		drawModel(model, x, y, z, 0, 0, 0);
	}

	@Override
	public void update(Camera camera) throws GameException {
		loadViewMatrix(camera);
		ShaderProgram shaderProgram = this.shaderProgram.get();
		shaderProgram.bind();
		shaderProgram.setUniform("texture_sampler", 0);
		shaderProgram.setUniform("projectionMatrix", projectionMatrix);
	}

	public void loadViewMatrix(Camera camera) {
		viewMatrix.identity();
		viewMatrix.rotate((float) Math.toRadians(camera.getRotX()), X_AXIS)
				.rotate((float) Math.toRadians(camera.getRotY()), Y_AXIS)
				.rotate((float) Math.toRadians(camera.getRotZ()), Z_AXIS);
		viewMatrix.translate(-camera.getX(), -camera.getY(), -camera.getZ());
	}

	private void drawMesh(Mesh mesh) throws GameException {
		tempMatrix.set(viewMatrix);
		tempMatrix.mul(modelViewMatrix);

		ShaderProgram shaderProgram = this.shaderProgram.get();
		shaderProgram.bind();
		shaderProgram.setUniform("modelViewMatrix", tempMatrix);
		shaderProgram.setUniform("color", mesh.getColor());
		shaderProgram.setUniform("useColor", mesh.hasTexture() ? 0 : 1);
		mesh.render();
		shaderProgram.unbind();
	}

	public void setProgram(ShaderProgram program) {
		this.shaderProgram.set(program);
	}

	public ShaderProgram getProgram() {
		return shaderProgram.get();
	}
}