package de.tobchen.jumptobi.game.view;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import de.tobchen.android.game.manager.GameManager;
import de.tobchen.android.game.manager.GfxUtil;
import de.tobchen.android.game.manager.TextureEntry;
import de.tobchen.jumptobi.game.model.Model;
import de.tobchen.jumptobi.game.model.ModelListener;
import de.tobchen.jumptobi.game.util.GfxFx;
import de.tobchen.jumptobi.model.Char;
import de.tobchen.jumptobi.model.MoveChar;

public class View implements ModelListener {
	// Textures
	private TextureEntry tileset;
	private TextureEntry special;
	private TextureEntry background;
	private TextureEntry tobi;
	private TextureEntry numbers;
	private TextureEntry enemies;

	// Particles
	private int currentBox;
	private Anim[] boxAnims;
	private static final int MAX_BOX_ANIMS = 2;
	private int currentCoin;
	private Anim[] coinAnims;
	private static final int MAX_COIN_ANIMS = 3;
	private int currentEnemy;
	private Anim[] enemyAnims;
	private static final int MAX_ENEMY_ANIMS = 2;

	// Walking animation
	private int walkFrameHelper;
	private int walkFrame;

	// HUD stuff
	private int guardianY;
	private int collectionsY;

	// Timer
	private int lastReceived;

	// Background file names
	// TODO Optimize
	private String[] bgNames = { "gfx/day.png", "gfx/down.png",
			"gfx/night.png", "gfx/rise.png" };

	public View() {
		// Reset some values
		lastReceived = 90;
		boxAnims = new Anim[MAX_BOX_ANIMS];
		for (int i = 0; i < MAX_BOX_ANIMS; i++) {
			boxAnims[i] = new Anim();
		}
		coinAnims = new Anim[MAX_COIN_ANIMS];
		for (int i = 0; i < MAX_COIN_ANIMS; i++) {
			coinAnims[i] = new Anim();
		}
		enemyAnims = new Anim[MAX_ENEMY_ANIMS];
		for (int i = 0; i < MAX_ENEMY_ANIMS; i++) {
			enemyAnims[i] = new Anim();
		}
		guardianY = -32;
		collectionsY = 0;
	}

	public void initialize(GL10 gl, GameManager manager, int backgroundID)
			throws IOException {
		tileset = manager.loadTexture(gl, "gfx/tileset.png", GL10.GL_NEAREST);
		special = manager.loadTexture(gl, "gfx/special.png", GL10.GL_NEAREST);
		background = manager.loadTexture(gl, bgNames[backgroundID],
				GL10.GL_NEAREST);
		tobi = manager.loadTexture(gl, "gfx/tobi.png", GL10.GL_NEAREST);
		numbers = manager.loadTexture(gl, "gfx/numbers.png", GL10.GL_NEAREST);
		enemies = manager.loadTexture(gl, "gfx/enemies.png", GL10.GL_NEAREST);
	}

	public void draw(GL10 gl, Model model, GameManager manager,
			boolean showCoins) {
		// Walking
		if (model.frameCount % 4 == 0) {
			walkFrameHelper++;
			if (walkFrameHelper > 3) {
				walkFrameHelper = 0;
			}
			if (walkFrameHelper == 0 || walkFrameHelper == 2) {
				walkFrame = 0;
			} else if (walkFrameHelper == 1) {
				walkFrame = 1;
			} else {
				walkFrame = 2;
			}
		}

		// Sky
		GfxUtil.drawImage(gl, 0, 0, 256, 256, background);
		float color = 1f;
		if (model.map.background == 2) {
			color = 0.5f;
		} else if (model.map.background == 3 || model.map.background == 1) {
			color = 0.75f;
		}
		gl.glColor4f(color, color, color, 1f);

		// Draw destination sign
		GfxUtil.drawImageRect(gl, model.map.destinationX - model.scrollX,
				model.map.destinationY, 16, 16, enemies, 0.75f, 0.5f, 0.25f,
				0.25f);

		// Draw map
		int maxX = (model.scrollX / 16) + 16;
		for (int x = (model.scrollX / 16); x <= maxX && x < model.map.width; x++) {
			for (int y = 0; y < 14; y++) {
				int tile = model.map.tiles[x][y];
				if (tile >= 1 && tile <= 16) {
					int frame = 0;
					if (tile == 1) {
						frame = 0;
					} else if (tile >= 2 && tile <= 11) {
						frame = 1;
					} else if (tile >= 12 && tile <= 16) {
						frame = tile - 10;
					}
					float frameX = (frame % 4) * 0.25f;
					float frameY = (frame / 4) * 0.25f;
					GfxUtil.drawImageRect(gl, (x * 16) - model.scrollX, y * 16,
							16, 16, special, frameX, frameY, 0.25f, 0.25f);
				} else if (tile >= 50 && tile <= 113) {
					tile -= 50;
					float frameX = (tile % 8) * 0.125f;
					float frameY = (tile / 8) * 0.125f;
					GfxUtil.drawImageRect(gl, (x * 16) - model.scrollX, y * 16,
							16, 16, tileset, frameX, frameY, 0.125f, 0.125f);
				}
			}
		}

		// Draw destroyed boxes
		for (int i = 0; i < MAX_BOX_ANIMS; i++) {
			if (boxAnims[i].frame < 3) {
				GfxUtil.drawImageRect(gl, boxAnims[i].x - model.scrollX,
						boxAnims[i].y, 16, 16, special,
						boxAnims[i].frame * 0.25f, 0.75f, 0.25f, 0.25f);
				boxAnims[i].frame++;
			}
		}

		// Draw collected coins
		for (int i = 0; i < MAX_COIN_ANIMS; i++) {
			if (coinAnims[i].frame < 3) {
				GfxUtil.drawImageRect(gl, coinAnims[i].x - model.scrollX,
						coinAnims[i].y, 16, 16, special,
						coinAnims[i].frame * 0.25f, 0.5f, 0.25f, 0.25f);
				coinAnims[i].frame++;
			}
		}

		// Draw killed enemies
		gl.glColor4f(1f, 1f, 1f, 1f);
		for (int i = 0; i < MAX_ENEMY_ANIMS; i++) {
			if (enemyAnims[i].frame < 4) {
				GfxUtil.drawImageRect(gl, enemyAnims[i].x - model.scrollX,
						enemyAnims[i].y, 16, 16, enemies,
						enemyAnims[i].frame * 0.25f, 0.75f, 0.25f, 0.25f);
				enemyAnims[i].frame++;
			}
		}
		gl.glColor4f(color, color, color, 1f);

		// Enemies
		drawAnimatedEnemies(gl, model.map.horizontalEnemy, model.scrollX, 0f);
		drawAnimatedEnemies(gl, model.map.horizontalSpikedEnemy, model.scrollX,
				0.25f);
		drawEnemies(gl, model.map.spikedEnemy, model.scrollX, 0.75f, 0f);
		drawEnemies(gl, model.map.verticalEnemy, model.scrollX, 0.75f, 0.25f);
		drawEnemies(gl, model.map.verticalHorizontalEnemy, model.scrollX, 0f,
				0.5f);
		drawEnemies(gl, model.map.fallingEnemy, model.scrollX, 0.25f, 0.5f);
		drawEnemies(gl, model.map.shootingEnemy, model.scrollX, 0.5f, 0.5f);

		// Player/Tobi
		int frame = 0;
		if (model.player.state == Char.STATE_DEAD) {
			frame = 3;
		} else if (!model.player.onFloor) {
			frame = 2;
		} else if (model.player.x != model.player.oldX) {
			frame = walkFrame;
		}
		if (model.player.invulnerableTime > model.frameCount) {
			gl.glColor4f(color, color, color, 0.5f);
		}
		if (model.player.looksRight) {
			GfxUtil.drawImageRect(gl, model.player.x - 2 - model.scrollX,
					model.player.y, 16, 32, tobi, frame * 0.25f, 0f, 0.25f,
					0.5f);
		} else {
			GfxUtil.drawImageRect(gl, model.player.x - model.scrollX,
					model.player.y, 16, 32, tobi, frame * 0.25f + 0.25f, 0f,
					-0.25f, 0.5f);
		}

		// Restore color
		gl.glColor4f(1f, 1f, 1f, 1f);

		// Guardian angel
		if (model.player.hasGuardian) {
			guardianY += 2;
			if (guardianY > 0) {
				guardianY = 0;
			}
		} else {
			guardianY -= 2;
			if (guardianY < -32) {
				guardianY = -32;
			}
		}
		if (guardianY > -32) {
			if (model.player.looksRight) {
				GfxUtil.drawImageRect(gl,
						model.player.oldX - model.scrollX - 2, guardianY, 16,
						32, special, 0.75f, 0.25f, 0.25f, 0.5f);
			} else {
				GfxUtil.drawImageRect(gl,
						model.player.oldX - model.scrollX - 2, guardianY, 16,
						32, special, 0f, 0.25f, -0.25f, 0.5f);
			}
		}

		// HUD
		if (lastReceived > model.frameCount || showCoins) {
			collectionsY += 2;
			if (collectionsY > 0) {
				collectionsY = 0;
			}
		} else {
			collectionsY--;
			if (collectionsY < -32) {
				collectionsY = -32;
			}
		}
		if (collectionsY > -32) {
			GfxUtil.drawImageRect(gl, 0, collectionsY, 16, 16, special, 0.5f,
					0.25f, 0.25f, 0.25f);
			GfxFx.drawNumber(gl, model.coins, 24, 4 + collectionsY, numbers);
			GfxUtil.drawImageRect(gl, 215, collectionsY, 16, 16, special,
					0.75f, 0.75f, 0.25f, 0.25f);
			GfxFx.drawNumber(gl, model.lives, 240, 4 + collectionsY, numbers);
		}
	}

	private void drawAnimatedEnemies(GL10 gl, MoveChar[] enemyList,
			int scrollX, float offY) {
		for (int i = 0; i < enemyList.length; i++) {
			MoveChar enemy = enemyList[i];
			if (enemy.state == Char.STATE_ACTIVE) {
				if (!enemy.looksRight) {
					GfxUtil.drawImageRect(gl, enemy.x - scrollX, enemy.y, 16,
							16, enemies, walkFrame * 0.25f, offY, 0.25f, 0.25f);
				} else {
					GfxUtil.drawImageRect(gl, enemy.x - scrollX, enemy.y, 16,
							16, enemies, walkFrame * 0.25f + 0.25f, offY,
							-0.25f, 0.25f);
				}
			}
		}
	}

	private void drawEnemies(GL10 gl, Char[] enemyList, int scrollX,
			float offX, float offY) {
		for (int i = 0; i < enemyList.length; i++) {
			Char enemy = enemyList[i];
			if (enemy.state == Char.STATE_ACTIVE) {
				GfxUtil.drawImageRect(gl, enemy.x - scrollX, enemy.y, 16, 16,
						enemies, offX, offY, 0.25f, 0.25f);
			}
		}
	}

	// Particle class
	private class Anim {
		public int x;
		public int y;
		public int frame;

		public Anim() {
			frame = 4;
		}
	}

	@Override
	public void coinChanged(Model model) {
		lastReceived = model.frameCount + 90;
	}

	@Override
	public void lifeChanged(Model model) {
		lastReceived = model.frameCount + 90;
	}

	@Override
	public void boxDestroyed(Model model, int x, int y) {
		boxAnims[currentBox].x = x * 16;
		boxAnims[currentBox].y = y * 16;
		boxAnims[currentBox].frame = 0;
		currentBox++;
		if (currentBox >= MAX_BOX_ANIMS) {
			currentBox = 0;
		}
	}

	@Override
	public void coinCollected(Model model, int x, int y) {
		coinAnims[currentCoin].x = x * 16;
		coinAnims[currentCoin].y = y * 16;
		coinAnims[currentCoin].frame = 0;
		currentCoin++;
		if (currentCoin >= MAX_COIN_ANIMS) {
			currentCoin = 0;
		}
	}

	@Override
	public void enemyKilled(Model model, int x, int y) {
		enemyAnims[currentEnemy].x = x;
		enemyAnims[currentEnemy].y = y;
		enemyAnims[currentEnemy].frame = 0;
		currentEnemy++;
		if (currentEnemy >= MAX_ENEMY_ANIMS) {
			currentEnemy = 0;
		}
	}

	@Override
	public void playerKilled(Model model) {
		// TODO Auto-generated method stub

	}

	@Override
	public void levelWon(Model model) {
		// TODO Auto-generated method stub

	}
}
