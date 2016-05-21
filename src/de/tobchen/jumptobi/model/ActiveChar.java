package de.tobchen.jumptobi.model;

public class ActiveChar extends Char {

	public boolean active;
	public int initX;
	public int initY;

	public ActiveChar(int x, int y, int width, int height) {
		super(x, y, width, height);
		active = false;
		initX = x;
		initY = y;
	}

	@Override
	public void reset() {
		super.reset();
		active = false;
		x = initX;
		y = initY;
	}
}
