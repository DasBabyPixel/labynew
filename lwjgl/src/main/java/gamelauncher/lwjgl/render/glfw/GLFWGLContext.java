package gamelauncher.lwjgl.render.glfw;

import java.util.Collection;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengles.GLES32;

import gamelauncher.engine.resource.AbstractGameResource;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.concurrent.ExecutorThread;
import gamelauncher.engine.util.concurrent.Threads;
import gamelauncher.engine.util.logging.LogLevel;
import gamelauncher.engine.util.logging.Logger;
import gamelauncher.lwjgl.render.states.GlStates;
import gamelauncher.lwjgl.render.states.StateRegistry;

@SuppressWarnings("javadoc")
public class GLFWGLContext extends AbstractGameResource {

	private static final Logger logger = Logger.getLogger();

	private static final LogLevel level = new LogLevel("GL", 10);

	private volatile long glfwId;

	ExecutorThread owner = null;

	boolean owned = false;

	GLFWFrame parent;

	final Collection<GLFWGLContext> sharedContexts;

	public long getGLFWId() {
		return this.glfwId;
	}

	GLFWGLContext(Collection<GLFWGLContext> sharedContexts) {
		super();
		this.sharedContexts = sharedContexts;
		this.sharedContexts.add(this);
	}

	synchronized GLFWFrame.Creator create(GLFWFrame frame) {
		GLFWFrame.Creator creator = new GLFWFrame.Creator(frame);
		creator.run();
		this.glfwId = creator.glfwId;
		StateRegistry.addWindow(this.glfwId);
		return creator;
	}

	@Override
	protected void cleanup0() throws GameException {
		if (this.owned) {
			Threads.waitFor(this.owner.submit(() -> {
				StateRegistry.removeContext(this.glfwId);
				this.destroyCurrent();
			}));
			this.owned = false;
			this.owner = null;
		}
		GLFW.glfwDestroyWindow(this.glfwId);
		StateRegistry.removeWindow(this.glfwId);
	}

	synchronized void beginCreationShared() throws GameException {
		if (this.owned) {
			Threads.waitFor(this.owner.submit(() -> {
				StateRegistry.setContextHoldingThread(this.glfwId, null);
			}));
		}
	}

	synchronized void endCreationShared() throws GameException {
		if (this.owned) {
			Threads.waitFor(this.owner.submit(() -> {
				StateRegistry.setContextHoldingThread(this.glfwId, Thread.currentThread());
			}));
		}
	}

	synchronized void destroyCurrent() {
		StateRegistry.setContextHoldingThread(this.glfwId, null);
		this.owned = false;
		this.owner = null;
	}

	synchronized void makeCurrent() {
		this.owned = true;
		this.owner = (ExecutorThread) Thread.currentThread();
		StateRegistry.setContextHoldingThread(this.glfwId, Thread.currentThread());
		GlStates.current().enable(GLES32.GL_DEBUG_OUTPUT);
		GLUtil.setupDebugMessageCallback(GLFWGLContext.logger.createPrintStream(GLFWGLContext.level));
	}

}