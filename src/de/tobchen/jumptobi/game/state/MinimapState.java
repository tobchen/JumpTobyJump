package de.tobchen.jumptobi.game.state;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import de.tobchen.android.game.manager.GameState;
import de.tobchen.android.game.manager.GfxUtil;
import de.tobchen.android.game.manager.TextureEntry;
import de.tobchen.jumptobi.game.VersionInfo;
import de.tobchen.jumptobi.game.model.MinimapLevel;
import de.tobchen.jumptobi.game.util.InputOutput;
import de.tobchen.jumptobi.game.view.Gamepad;
import de.tobchen.jumptobi.game.view.Menu;

public class MinimapState extends GameState {

	private boolean initializingTodo;
	private TextureEntry minimap;
	private TextureEntry elements;
	private TextureEntry minitobi;

	private MinimapLevel[] levels;

	private Gamepad gamepad;

	private int tobiX;
	private int tobiY;

	private int highestLevel;
	private int currentLevel;
	private int nextLevelToGoTo;

	private boolean buttonNextHit;
	private boolean buttonPrevHit;
	private boolean buttonOkHit;

	private Menu menu;

	private final static int MENU_MAIN = 0;
	private final static int MENU_SAVE = 1;

	public MinimapState(String tag) {
		super(tag);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(Bundle data) {
		// Get data
		highestLevel = manager.bundle.getInt("highestLevel", 0);

		// Get level list
		try {
			levels = InputOutput
					.loadLevelList(manager.getContext().getAssets());
		} catch (IOException e) {
			// TODO Move back to main menu
			levels = new MinimapLevel[] {};
		}

		// Get to last played level
		int currentLevel = highestLevel;
		if (data != null) {
			String lastLevel = data.getString("mapname");
			for (int i = 0; i < levels.length; i++) {
				MinimapLevel level = levels[i];
				if (lastLevel.equals(level.mapfile)) {
					currentLevel = i;
				}
			}

			// Maybe increase highest level (if last level won)
			if (data != null && data.getBoolean("wasVictory", false)) {
				if (currentLevel == highestLevel) {
					highestLevel++;
				}
				if (highestLevel >= levels.length) {
					highestLevel = levels.length - 1;
				}
				manager.bundle.putInt("highestLevel", highestLevel);
			}
		}
		nextLevelToGoTo = currentLevel;
		if (currentLevel == highestLevel - 1) {
			nextLevelToGoTo = highestLevel;
		}

		// Take Position of last played level
		MinimapLevel level = levels[currentLevel];
		tobiX = level.x;
		tobiY = level.y;

		// Set up gamepad
		gamepad = new Gamepad(manager.width, manager.height,
				manager.getContext());

		// Request loading
		initializingTodo = true;

		// Reset values
		buttonPrevHit = false;
		buttonNextHit = false;
		buttonOkHit = false;

		// Menu
		menu = new Menu();
		menu.addMenu("gfx/menu_minimap.png", MENU_MAIN, new int[] { 1, 1, 1 },
				manager.width, manager.height);
		menu.addMenu("gfx/menu_loadsave.png", MENU_SAVE, new int[] { 3, 1 },
				manager.width, manager.height);

		// Call GC to clean
		System.gc();
	}

	@Override
	public void stop() {
		// Free stuff
		levels = null;
		menu = null;
		gamepad = null;
		// TODO Get rid of TextureEntries
	}

	@Override
	public void update() {
		// Update menu
		menu.update(manager);

		// If not in ingame menu
		if (!menu.isActive) {
			// If Tobi is standing on the level he is supposed to go to
			if (nextLevelToGoTo == currentLevel) {
				if (buttonPrevHit) {
					nextLevelToGoTo = currentLevel - 1;
					if (nextLevelToGoTo < 0) {
						nextLevelToGoTo = 0;
					}
				} else if (buttonNextHit) {
					nextLevelToGoTo = currentLevel + 1;
					if (nextLevelToGoTo > highestLevel) {
						nextLevelToGoTo = highestLevel;
					}
				} else if (buttonOkHit) {
					Bundle output = new Bundle();
					output.putString("mapname", levels[currentLevel].mapfile);
					manager.changeState("ingame", output);
				}
			}

			// Move Tobi
			if (nextLevelToGoTo != currentLevel) {
				MinimapLevel next = levels[nextLevelToGoTo];

				if (Math.abs(next.x - tobiX) <= 2
						&& Math.abs(next.y - tobiY) <= 2) {
					tobiX = next.x;
					tobiY = next.y;
					currentLevel = nextLevelToGoTo;
				} else if (next.x == tobiX) {
					if (next.y < tobiY) {
						tobiY -= 4;
					} else {
						tobiY += 4;
					}
				} else if (next.y == tobiY) {
					if (next.x < tobiX) {
						tobiX -= 4;
					} else {
						tobiX += 4;
					}
				}
			}
		}

		// Reset buttons
		buttonPrevHit = false;
		buttonNextHit = false;
		buttonOkHit = false;
	}

	@Override
	public void draw(GL10 gl) {
		// Initialize
		if (initializingTodo) {
			manager.freeTextures(gl);
			try {
				minimap = manager.loadTexture(gl, "gfx/minimap.png",
						GL10.GL_NEAREST);
				elements = manager.loadTexture(gl, "gfx/minimap_elements.png",
						GL10.GL_NEAREST);
				minitobi = manager.loadTexture(gl, "gfx/minitobi.png",
						GL10.GL_NEAREST);
				gamepad.initialize(gl, manager);
			} catch (IOException e) {
				// Do nothing except for maybe cry
				e.printStackTrace();
			}
			initializingTodo = false;
		}

		// The whole game is 2D
		GfxUtil.turn2D(gl, manager.width, manager.height);

		// Configure screen
		gamepad.configurScreen(gl, manager.width, manager.height);

		// Draw minimap background
		GfxUtil.drawImage(gl, 0, 0, 256, 256, minimap);

		// Draw levels on minimap
		MinimapLevel prev = null;
		for (int i = 0; i < levels.length; i++) {
			MinimapLevel level = levels[i];
			if (i <= highestLevel) {
				GfxUtil.drawImageRect(gl, level.x, level.y, 8, 8, elements, 0f,
						0f, 0.5f, 0.5f);
			} else {
				GfxUtil.drawImageRect(gl, level.x, level.y, 8, 8, elements,
						0.5f, 0f, 0.5f, 0.5f);
			}

			// Draw paths on minimap
			if (prev != null) {
				if (level.x == prev.x) {
					int y = level.y + 8;
					int height = prev.y - level.y - 8;
					if (prev.y < level.y) {
						y = prev.y + 8;
						height = level.y - prev.y - 8;
					}
					GfxUtil.drawImageRect(gl, level.x, y, 8, height, elements,
							0f, 0.5f, 0.5f, 0.5f);
				} else {
					int x = level.x + 8;
					int width = prev.x - level.x - 8;
					if (prev.x < level.x) {
						x = prev.x + 8;
						width = level.x - prev.x - 8;
					}
					GfxUtil.drawImageRect(gl, x, level.y, width, 8, elements,
							0.5f, 0.5f, 0.5f, 0.5f);
				}
			}

			prev = level;
		}

		// Draw Tobi
		GfxUtil.drawImage(gl, tobiX, tobiY, 8, 8, minitobi);

		// Deconfigure screen
		gamepad.deconfigureScreen(gl);

		// Gamepad
		if (!menu.isActive) {
			gamepad.draw(gl);
		}

		// Draw menu
		menu.draw(gl, manager);
	}

	@Override
	public void onTouchEvent(MotionEvent event) {
		if (gamepad == null || menu == null) {
			return;
		}

		if (!menu.isActive) {
			int result = gamepad.onTouchEvent(event);

			if (result == Gamepad.LEFT_UP) {
				buttonPrevHit = true;
			} else if (result == Gamepad.RIGHT_UP) {
				buttonNextHit = true;
			} else if (result == Gamepad.ACTION_UP) {
				buttonOkHit = true;
			} else if (result == Gamepad.MENU_UP) {
				menu.switchToMenu(MENU_MAIN);
			}
		} else {
			int value = menu.onTouchEvent(event);
			if (menu.currentMenu == MENU_MAIN) {
				if (value == 0) {
					menu.switchOff();
				} else if (value == 1) {
					menu.switchToMenu(MENU_SAVE);
				} else if (value == 2) {
					menu.switchToState("menu", null);
				}
			} else if (menu.currentMenu == MENU_SAVE) {
				if (value >= 0 && value <= 2) {
					int profile = value + 1;
					SharedPreferences preferences = PreferenceManager
							.getDefaultSharedPreferences(manager.getContext());
					Editor editor = preferences.edit();
					editor.putBoolean("profile" + profile, true);
					editor.putInt("version" + profile,
							VersionInfo.currentSaveVersion);
					editor.putInt("highestLevel" + profile,
							manager.bundle.getInt("highestLevel"));
					editor.putInt("coins" + profile,
							manager.bundle.getInt("coins"));
					editor.putInt("lives" + profile,
							manager.bundle.getInt("lives"));
					editor.putBoolean("guardian" + profile,
							manager.bundle.getBoolean("guardian"));
					editor.commit();
					menu.switchToMenu(MENU_MAIN);
				} else if (value == 3) {
					menu.switchToMenu(MENU_MAIN);
				}
			}
		}
	}

	@Override
	public void onKeyDown(int keycode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onKeyUp(int keycode) {
		if (!menu.isActive) {
			if (keycode == KeyEvent.KEYCODE_DPAD_LEFT) {
				buttonPrevHit = true;
			} else if (keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				buttonNextHit = true;
			} else if (keycode == KeyEvent.KEYCODE_ENTER
					|| keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
				buttonOkHit = true;
			}
		}

		if (keycode == KeyEvent.KEYCODE_MENU) {
			if (!menu.isActive) {
				menu.switchToMenu(MENU_MAIN);
			} else {
				menu.switchOff();
			}
		}
	}

	@Override
	public Bundle save() {
		// Do nothing
		return new Bundle();
	}

	@Override
	public void restore(Bundle bundle) {
		// Do nothing
	}
}
