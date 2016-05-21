package de.tobchen.jumptobi.model;

public class JumpChar extends MoveChar {
	public int oldY;
	public int initY;
	public float gravity;
	public boolean onFloor;

	public JumpChar(int x, int y, int width, int height) {
		super(x, y, width, height);
		initY = y;
	}

	@Override
	public void reset() {
		super.reset();
		y = initY;
		gravity = 0f;
		onFloor = false;
	}

	public void jump(float speed, boolean ignoreFloor) {
		speed = -speed;
		if (speed < gravity) {
			if (ignoreFloor) {
				gravity = speed;
			} else if (onFloor) {
				gravity = speed;
			}
		}
	}

	public void moveVertical(Gamemap map, float boost) {
		oldY = y;

		gravity += 0.6f;
		if (gravity < 0f) {
			gravity -= boost;
		} else if (gravity > 10f) {
			gravity = 10f;
		}

		onFloor = false;

		if (gravity > 0f) {
			int farest = (int) (y + gravity + 0.5f);
			boolean out = false;
			for (int x1 = x; !out; x1 += 16) {
				if (x1 >= (x + width)) {
					x1 = x + width - 1;
					out = true;
				}
				for (int y1 = y + height; y1 < farest + height + 16; y1 += 16) {
					int x2 = x1 / 16;
					int y2 = y1 / 16;
					if (map.doesCollide(x2, y2)) {
						int test = (y2 * 16) - height;
						if (farest > test) {
							farest = test;
							gravity = 0;
							onFloor = true;
						}
						break;
					}
				}
			}
			y = farest;
		} else if (gravity < 0f) {
			int farest = (int) (y + gravity - 0.5f);
			boolean out = false;
			for (int x1 = x; !out; x1 += 16) {
				if (x1 >= (x + width)) {
					x1 = x + width - 1;
					out = true;
				}
				for (int y1 = y - 1; y1 > farest - 1 - 16; y1 -= 16) {
					int x2 = x1 / 16;
					int y2 = y1 / 16;
					if (map.doesCollide(x2, y2)) {
						int test = (y2 * 16) + 16;
						if (farest < test) {
							farest = test;
							gravity = 0;
						}
						break;
					}
				}
			}
			y = farest;
		}
	}
}
