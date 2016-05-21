package de.tobchen.android.game.manager;

public class TextureEntry {
	public int[] texture;
	public String path;
	public int magFilter;

	public TextureEntry(String path, int magFilter) {
		this.magFilter = magFilter;
		this.path = path;
		this.texture = new int[1];
	}
}