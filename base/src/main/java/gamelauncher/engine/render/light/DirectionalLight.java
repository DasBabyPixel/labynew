package gamelauncher.engine.render.light;

import gamelauncher.engine.render.shader.ProgramObject;
import gamelauncher.engine.render.shader.ShaderProgram;
import org.joml.Vector3f;

public class DirectionalLight implements ProgramObject {

    public final Vector3f color;
    public final Vector3f direction;
    public final float intensity;

    public DirectionalLight(Vector3f color, Vector3f direction, float intensity) {
        this.color = color;
        this.direction = direction;
        this.intensity = intensity;
    }

    public DirectionalLight(DirectionalLight other) {
        this(new Vector3f(other.color), new Vector3f(other.direction), other.intensity);
    }

    @Override public void upload(ShaderProgram program, String name) {
        program.uniformMap.get(name + ".color").set(color).upload();
        program.uniformMap.get(name + ".direction").set(direction).upload();
        program.uniformMap.get(name + ".intensity").set(intensity).upload();
    }
}
