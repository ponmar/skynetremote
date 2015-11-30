package se.markstrom.skynet.skynetremote.model;

public class Camera {

	public final String name;
	public final int index;
	public final int width;
	public final int height;
	
	public Camera(String name, int index, int width, int height) {
		this.name = name;
		this.index = index;
		this.width = width;
		this.height = height;
	}
}
