package de.tobchen.jumptobi.model;

public class Gamemap {
	public int width;
	public byte[][] tiles;

	public int startX;
	public int startY;

	public static final int BG_DAY = 0;
	public static final int BG_SUNDOWN = 1;
	public static final int BG_NIGHT = 2;
	public static final int BG_SUNRISE = 3;
	public int background;

	public static final int WEATHER_SKY = 0;
	public static final int WEATHER_RAIN = 1;
	public static final int WEATHER_SNOW = 2;
	public int weather;

	public static final int TYPE_STANDARD = 0;
	public static final int TYPE_CHASE = 1; // Auto walking
	public int type;

	public JumpChar[] horizontalEnemy;
	public JumpChar[] horizontalSpikedEnemy;
	public Char[] spikedEnemy;
	public JumpChar[] verticalEnemy;
	public JumpChar[] verticalHorizontalEnemy;
	public ActiveChar[] fallingEnemy;
	public ActiveChar[] shootingEnemy;

	public int destinationX;
	public int destinationY;

	public Gamemap() {
		this(16);
	}

	public Gamemap(Gamemap map, int width) {
		this(width);

		for (int x = 0; x < width && x < map.width; x++) {
			for (int y = 0; y < 14; y++) {
				tiles[x][y] = map.tiles[x][y];
			}
		}
	}

	public Gamemap(int width) {
		this.width = width;
		this.tiles = new byte[this.width][14];
		this.background = BG_DAY;
		this.weather = WEATHER_SKY;
		this.type = TYPE_STANDARD;

		horizontalEnemy = new JumpChar[0];
		horizontalSpikedEnemy = new JumpChar[0];
		spikedEnemy = new Char[0];
		verticalEnemy = new JumpChar[0];
		verticalHorizontalEnemy = new JumpChar[0];
		fallingEnemy = new ActiveChar[0];
		shootingEnemy = new ActiveChar[0];
	}

	public boolean doesCollide(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= 14) {
			return false;
		}
		int tile = tiles[x][y];
		if ((tile >= 1 && tile <= 15) || (tile >= 50 && tile <= 113)) {
			return true;
		}
		return false;
	}

	public int getTile(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= 14) {
			return 0;
		}
		return tiles[x][y];
	}

	public void setDestinationSign() {
		destinationX = (width - 2) * 16;
		int x = width - 2;
		for (int y = 13; y >= 0; y--) {
			if (tiles[x][y] == 0) {
				destinationY = y * 16;
				break;
			}
		}
	}
}
