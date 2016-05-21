package de.tobchen.jumptobi.model;

public class MoveChar extends Char {

	public int oldX;
	public int initX;
	public boolean looksRight;

	public MoveChar(int x, int y, int width, int height) {
		super(x, y, width, height);
		initX = x;
	}

	@Override
	public void reset() {
		super.reset();
		x = initX;
		looksRight = false;
	}

	public boolean moveHorizontal(Gamemap map, int limitLeft, int limitRight,
			int speed) {
		boolean collision = false;

		oldX = x;

		if (speed > 0) {
			int farest = x + speed;
			if (farest + width >= limitRight) {
				farest = limitRight - width;
				collision = true;
			}
			boolean out = false;
			for (int y1 = y; !out; y1 += 16) {
				if (y1 >= (y + height)) {
					y1 = y + height - 1;
					out = true;
				}
				for (int x1 = x + width; x1 < farest + width + 16; x1 += 16) {
					int x2 = x1 / 16;
					int y2 = y1 / 16;
					if (map.doesCollide(x2, y2)) {
						int test = (x2 * 16) - width;
						if (farest > test) {
							farest = test;
							collision = true;
						}
						break;
					}
				}
			}
			x = farest;
			looksRight = true;
		} else if (speed < 0) {
			int farest = x + speed;
			if (farest < limitLeft) {
				farest = limitLeft;
				collision = true;
			}
			boolean out = false;
			for (int y1 = y; !out; y1 += 16) {
				if (y1 >= (y + height)) {
					y1 = y + height - 1;
					out = true;
				}
				for (int x1 = x - 1; x1 > farest - 1 - 16; x1 -= 16) {
					int x2 = x1 / 16;
					int y2 = y1 / 16;
					if (map.doesCollide(x2, y2)) {
						int test = (x2 * 16) + 16;
						if (farest < test) {
							farest = test;
							collision = true;
						}
						break;
					}
				}
			}
			x = farest;
			looksRight = false;
		}

		return collision;
	}
}
