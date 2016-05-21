package de.tobchen.jumptobi.game.model;

import java.util.ArrayList;

import de.tobchen.jumptobi.model.ActiveChar;
import de.tobchen.jumptobi.model.Char;
import de.tobchen.jumptobi.model.Gamemap;
import de.tobchen.jumptobi.model.JumpChar;
import de.tobchen.jumptobi.model.Player;
import de.tobchen.util.GeoUtil;

public class Model {
	public Gamemap map;
	public int frameCount;

	public int scrollX;

	public Player player;

	public int coins;
	public int lives;

	public int width16;

	private ArrayList<ModelListener> listener;

	public Model(Gamemap map, int coins, int lives, boolean guardian) {
		this.map = map;
		width16 = map.width * 16;

		this.coins = coins;
		this.lives = lives;

		player = new Player(map.startX, map.startY, guardian);

		scrollX = 0;

		frameCount = 0;

		listener = new ArrayList<ModelListener>();
	}

	public void update(boolean leftKeyDown, boolean rightKeyDown,
			boolean jumpDown) {
		// Increment frame count
		frameCount++;

		// Maybe reanimate
		if (player.state == Char.STATE_DEAD
				&& player.reanimateTime < frameCount && lives >= 0) {
			// Player and Scrolling
			player.reset();
			if (map.type == Gamemap.TYPE_STANDARD && player.x < scrollX) {
				scrollX = player.x;
			} else if (map.type == Gamemap.TYPE_CHASE) {
				scrollX = 0;
			}
			// Boxes
			for (int x = 0; x < map.width; x++) {
				for (int y = 0; y < 14; y++) {
					if (map.tiles[x][y] == 120) {
						map.tiles[x][y] = 15;
					} else if (map.tiles[x][y] == 121) {
						map.tiles[x][y] = 13;
					}
				}
			}
			// Enemies
			for (int i = 0; i < map.horizontalEnemy.length; i++) {
				map.horizontalEnemy[i].reset();
			}
			for (int i = 0; i < map.horizontalSpikedEnemy.length; i++) {
				map.horizontalSpikedEnemy[i].reset();
			}
			for (int i = 0; i < map.spikedEnemy.length; i++) {
				map.spikedEnemy[i].reset();
			}
			for (int i = 0; i < map.verticalEnemy.length; i++) {
				map.verticalEnemy[i].reset();
			}
			for (int i = 0; i < map.verticalHorizontalEnemy.length; i++) {
				map.verticalHorizontalEnemy[i].reset();
			}
			for (int i = 0; i < map.fallingEnemy.length; i++) {
				map.fallingEnemy[i].reset();
			}
			for (int i = 0; i < map.shootingEnemy.length; i++) {
				map.shootingEnemy[i].reset();
			}
		}

		// Speed up
		if (map.type == Gamemap.TYPE_CHASE) {
			scrollX += 2;
		}

		// Update player
		updatePlayer(leftKeyDown, rightKeyDown, jumpDown);

		// Update enemies
		updateEnemies();

		// Update scrolling
		int oldScrollX = scrollX;
		scrollX = player.x - 121;
		if (scrollX < oldScrollX) {
			scrollX = oldScrollX;
		}
		if (scrollX > width16 - 256) {
			scrollX = width16 - 256;
		}

		// Finally end
		if (player.x >= width16) {
			notifyOfLevelWon();
		}
	}

	private void updateEnemies() {
		int activateLimit = scrollX + 256;
		int killLimit = scrollX - 32;

		// Horizontal moving
		for (int i = 0; i < map.horizontalEnemy.length; i++) {
			JumpChar enemy = map.horizontalEnemy[i];
			if (enemy.state != Char.STATE_DEAD) {
				activateOrKillChar(enemy, killLimit, activateLimit);
				if (enemy.state == Char.STATE_ACTIVE) {
					moveHorizontalChar(enemy);
					checkKillableChar(enemy);
				}
			}
		}

		// Horizontal spiked moving
		for (int i = 0; i < map.horizontalSpikedEnemy.length; i++) {
			JumpChar enemy = map.horizontalSpikedEnemy[i];
			if (enemy.state != Char.STATE_DEAD) {
				activateOrKillChar(enemy, killLimit, activateLimit);
				if (enemy.state == Char.STATE_ACTIVE) {
					moveHorizontalChar(enemy);
					checkUnkillableChar(enemy);
				}
			}
		}

		// Spiked
		for (int i = 0; i < map.spikedEnemy.length; i++) {
			Char enemy = map.spikedEnemy[i];
			if (enemy.state != Char.STATE_DEAD) {
				activateOrKillChar(enemy, killLimit, activateLimit);
				if (enemy.state == Char.STATE_ACTIVE) {
					checkUnkillableChar(enemy);
				}
			}
		}

		// Jumping on spot
		for (int i = 0; i < map.verticalEnemy.length; i++) {
			JumpChar enemy = map.verticalEnemy[i];
			if (enemy.state != Char.STATE_DEAD) {
				activateOrKillChar(enemy, killLimit, activateLimit);
				if (enemy.state == Char.STATE_ACTIVE) {
					if (enemy.onFloor) {
						enemy.jump(7.0f, false);
					}
					enemy.moveVertical(map, 0.0f);
					checkKillableChar(enemy);
				}
			}
		}

		// Jumping horizontal
		for (int i = 0; i < map.verticalHorizontalEnemy.length; i++) {
			JumpChar enemy = map.verticalHorizontalEnemy[i];
			if (enemy.state != Char.STATE_DEAD) {
				activateOrKillChar(enemy, killLimit, activateLimit);
				if (enemy.state == Char.STATE_ACTIVE) {
					if (enemy.onFloor) {
						enemy.jump(8.0f, false);
					}
					moveHorizontalChar(enemy);
					checkKillableChar(enemy);
				}
			}
		}

		// Falling
		int fallingLimit = player.x + 48;
		for (int i = 0; i < map.fallingEnemy.length; i++) {
			ActiveChar enemy = map.fallingEnemy[i];
			if (enemy.state != Char.STATE_DEAD) {
				activateOrKillChar(enemy, killLimit, activateLimit);
				if (enemy.state == Char.STATE_ACTIVE) {
					if (enemy.active) {
						enemy.y += 4;
						checkUnkillableChar(enemy);
					} else if (enemy.x < fallingLimit) {
						enemy.active = true;
					}
				}
			}
		}

		// Shooting
		for (int i = 0; i < map.shootingEnemy.length; i++) {
			Char enemy = map.shootingEnemy[i];
			if (enemy.state != Char.STATE_DEAD) {
				activateOrKillChar(enemy, killLimit, activateLimit);
				if (enemy.state == Char.STATE_ACTIVE) {
					enemy.x -= 3;
					checkUnkillableChar(enemy);
				}
			}
		}
	}

	private void activateOrKillChar(Char ch, int killLimit, int activateLimit) {
		if (ch.x < killLimit) {
			ch.state = Char.STATE_DEAD;
		} else if (ch.x < activateLimit) {
			ch.state = Char.STATE_ACTIVE;
		}
	}

	private void moveHorizontalChar(JumpChar ch) {
		ch.moveVertical(map, 0.0f);
		int speed = 1;
		if (!ch.looksRight) {
			speed = -1;
		}
		if (ch.moveHorizontal(map, 0, width16, speed)) {
			ch.looksRight = !ch.looksRight;
		}
	}

	private void checkUnkillableChar(Char ch) {
		if (GeoUtil.doRectsOverlap(player.x, player.y, player.width,
				player.height, ch.x, ch.y, ch.width, ch.height)) {
			killPlayer(true);
		}
	}

	private void checkKillableChar(Char ch) {
		if (GeoUtil.doRectsOverlap(player.x, player.y, player.width,
				player.height, ch.x, ch.y, ch.width, ch.height)) {
			int oldY = ch.y;
			if (ch instanceof JumpChar) {
				oldY = ((JumpChar) ch).oldY;
			}
			if (player.oldY + player.height <= oldY) {
				ch.state = Char.STATE_DEAD;
				player.smallJumpToDo = true;
				notifyOfEnemyKilled(ch.x, ch.y);
			} else {
				killPlayer(true);
			}
		}
	}

	private void updatePlayer(boolean leftKeyDown, boolean rightKeyDown,
			boolean jumpDown) {

		if (player.state == Char.STATE_DEAD) {
			return;
		}

		// STRICT order:
		// - move horizontal
		// - maybe jump
		// - move vertical
		// - check boxes

		// Horizontal
		int speed = 0;
		if (leftKeyDown) {
			speed = -3;
		} else if (rightKeyDown) {
			speed = +3;
		}
		int limit = scrollX;
		if (map.type == Gamemap.TYPE_CHASE) {
			limit = 0;
		}
		player.moveHorizontal(map, limit, width16 + 16, speed);

		// Vertical
		float boost = 0f;
		if (jumpDown) {
			boost = 0.3f;
			player.jump(5f, false);
		}
		if (player.smallJumpToDo) {
			player.jump(3.5f, true);
		}
		player.moveVertical(map, boost);

		// Check boxes
		player.smallJumpToDo = false;
		if (player.oldY != player.y) {
			int top = (player.y - 1) / 16;
			int bottom = (player.y + player.height) / 16;
			boolean out = false;
			for (int x1 = player.x; !out; x1 += 16) {
				if (x1 >= (player.x + player.width)) {
					x1 = player.x + player.width - 1;
					out = true;
				}
				int x2 = x1 / 16;
				// Top
				boolean tileTop = checkBox(x2, top);
				if (tileTop) {
					break;
				}
				// Bottom
				boolean tileBottom = checkBox(x2, bottom);
				if (tileBottom) {
					player.smallJumpToDo = true;
					break;
				}
			}
		}

		// Check coins
		if (player.oldX != player.x || player.oldY != player.y) {
			int maxX = player.x + 12;
			int maxY = player.y + 20;
			for (int x = player.x + 2; x <= maxX; x += 10) {
				for (int y = player.y + 4; y <= maxY; y += 16) {
					int tileX = x / 16;
					int tileY = y / 16;
					if (map.getTile(tileX, tileY) == 16) {
						map.tiles[tileX][tileY] = 0;
						incrementCoins();
						notifyOfCoinCollected(tileX, tileY);
					}
				}
			}
		}

		// Kill if under map
		if (player.y >= 224) {
			killPlayer(false);
		}

		// Kill if scrolled over
		if (player.x < scrollX) {
			killPlayer(false);
		}
	}

	private boolean checkBox(int x, int y) {
		boolean breakNecessary = false;

		int tile = map.getTile(x, y);
		if (tile == 1 || tile == 2) {
			map.tiles[x][y] = 120;
			incrementCoins();
		} else if (tile >= 3 && tile <= 11) {
			map.tiles[x][y]--;
			incrementCoins();
		} else if (tile == 12) {
			map.tiles[x][y] = 120;
			incrementLives();
		} else if (tile == 13) {
			map.tiles[x][y] = 121;
			player.hasGuardian = true;
		} else if (tile == 14) {
			map.tiles[x][y] = 0;
			player.initX = (x * 16) + 1;
			player.initY = (y * 16) - 8;
		} else if (tile == 15) {
			map.tiles[x][y] = 120;
		}
		if (tile >= 1 && tile <= 15) {
			breakNecessary = true;
			if (tile < 3 || tile > 11) {
				notifyOfBoxDestroyed(x, y);
			}
		}

		return breakNecessary;
	}

	private void incrementCoins() {
		coins++;
		if (coins >= 100) {
			coins = 0;
			incrementLives();
		}
		notifyOfCoinChanged();
	}

	private void incrementLives() {
		lives++;
		if (lives > 99) {
			lives = 99;
		}
		notifyOfLifeChange();
	}

	private void killPlayer(boolean canBeSaved) {
		if (canBeSaved
				&& (player.hasGuardian || player.invulnerableTime > frameCount)) {
			player.jump(3.5f, true);
			if (player.invulnerableTime <= frameCount) {
				player.hasGuardian = false;
				player.invulnerableTime = frameCount + 60;
			}
		} else if (player.state != Char.STATE_DEAD) {
			player.state = Char.STATE_DEAD;
			player.reanimateTime = frameCount + 60;
			lives--;
			notifyOfLifeChange();
			notifyOfPlayerKilled();
		}
	}

	private void notifyOfCoinChanged() {
		for (ModelListener listener : this.listener) {
			listener.coinChanged(this);
		}
	}

	private void notifyOfLifeChange() {
		for (ModelListener listener : this.listener) {
			listener.lifeChanged(this);
		}
	}

	private void notifyOfBoxDestroyed(int x, int y) {
		for (ModelListener listener : this.listener) {
			listener.boxDestroyed(this, x, y);
		}
	}

	private void notifyOfCoinCollected(int x, int y) {
		for (ModelListener listener : this.listener) {
			listener.coinCollected(this, x, y);
		}
	}

	private void notifyOfEnemyKilled(int x, int y) {
		for (ModelListener listener : this.listener) {
			listener.enemyKilled(this, x, y);
		}
	}

	private void notifyOfPlayerKilled() {
		for (ModelListener listener : this.listener) {
			listener.playerKilled(this);
		}
	}

	private void notifyOfLevelWon() {
		for (ModelListener listener : this.listener) {
			listener.levelWon(this);
		}
	}

	public void addListener(ModelListener listener) {
		this.listener.add(listener);
	}
}
