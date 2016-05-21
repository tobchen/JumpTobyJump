package de.tobchen.jumptobi.model;

public class Char {
	public int x;
	public int y;
	public int width;
	public int height;
	public int state;

	public static final int STATE_WAITING = 0;
	public static final int STATE_ACTIVE = 1;
	public static final int STATE_DEAD = 2;

	public Char(int x, int y, int width, int height) {
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		this.state = STATE_WAITING;
	}

	public void reset() {
		state = STATE_WAITING;
	}
}
