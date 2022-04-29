package gamelauncher.lwjgl.render.light;

import org.joml.Vector3f;

public class PointLight {

	public Vector3f color;
	public Vector3f position;
	public float intensity;
	public Attenuation att;

	public PointLight(Vector3f color, Vector3f position, float intensity) {
		att = new Attenuation(1, 0, 0);
		this.color = color;
		this.position = position;
		this.intensity = intensity;
	}

	public PointLight(Vector3f color, Vector3f position, float intensity, Attenuation attenuation) {
		this(color, position, intensity);
		this.att = attenuation;
	}

	public PointLight(PointLight pointLight) {
		this(new Vector3f(pointLight.color), new Vector3f(pointLight.position), pointLight.intensity, pointLight.att);
	}

	public static class Attenuation {
		public float constant;
		public float linear;
		public float exponent;

		public Attenuation() {
		}

		public Attenuation(float constant, float linear, float exponent) {
			this.constant = constant;
			this.linear = linear;
			this.exponent = exponent;
		}

	}
}