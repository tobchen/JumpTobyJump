package de.tobchen.jumptobi.game.state;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import de.tobchen.android.game.manager.GameState;
import de.tobchen.android.game.manager.GfxUtil;
import de.tobchen.jumptobi.game.model.Model;
import de.tobchen.jumptobi.game.model.ModelListener;
import de.tobchen.jumptobi.game.util.InputOutput;
import de.tobchen.jumptobi.game.view.Gamepad;
import de.tobchen.jumptobi.game.view.Menu;
import de.tobchen.jumptobi.game.view.View;
import de.tobchen.jumptobi.model.Char;
import de.tobchen.jumptobi.model.Gamemap;

// TODO Maybe remove listener
public class IngameState extends GameState implements ModelListener {

	private final static String TAG = "Ingame";

	private String mapname;

	private boolean initializingTodo;

	private Model model;
	private View view;
	private Gamepad gamepad;

	private boolean jumpDown;
	private boolean leftDown;
	private boolean rightDown;

	private static final int MENU_MAIN = 0;
	private Menu menu;

	public IngameState(String tag) {
		super(tag);
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(Bundle data) {
		// Read arguments
		if (data != null) {
			mapname = data.getString("mapname");
		}
		int coins = manager.bundle.getInt("coins", 0);
		int lives = manager.bundle.getInt("lives", 4);
		boolean guardian = manager.bundle.getBoolean("guardian", false);
		Log.d(TAG, "Map: " + mapname + ", " + coins + " coins, " + lives
				+ " lives, guardian: " + guardian);

		// Load map
		Gamemap map = null;
		try {
			map = InputOutput.loadGameMap(mapname, manager.getContext()
					.getAssets());
		} catch (IOException e) {
			e.printStackTrace();
			map = new Gamemap();
			// TODO Check if correct
			Bundle output = new Bundle();
			output.putString("mapname", mapname);
			output.putBoolean("wasVictory", false);
			manager.changeState("minimap", output);
		}
		model = new Model(map, coins, lives, guardian);
		model.addListener(this);

		// Set up view
		view = new View();
		model.addListener(view);

		// Set up gamepad
		gamepad = new Gamepad(manager.width, manager.height,
				manager.getContext());

		// Request initializing
		initializingTodo = true;

		// Reset keys
		jumpDown = false;
		leftDown = false;
		rightDown = false;

		// Menu
		menu = new Menu();
		// menu.hasBlack = false;
		menu.addMenu("gfx/menu_ingame.png", MENU_MAIN, new int[] { 1, 1 },
				manager.width, manager.height);

		// Totally needs to call the GC
		System.gc();
	}

	@Override
	public void stop() {
		// Delete elements
		model = null;
		view = null;
		gamepad = null;
	}

	@Override
	public void update() {
		if (!menu.isActive) {
			model.update(leftDown, rightDown, jumpDown);
		}

		menu.update(manager);
	}

	@Override
	public void draw(GL10 gl) {
		// Initialize
		if (initializingTodo) {
			manager.freeTextures(gl);
			try {
				view.initialize(gl, manager, model.map.background);
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

		view.draw(gl, model, manager, menu.isActive);

		// Deconfigure screen
		gamepad.deconfigureScreen(gl);

		// Gamepad
		if (!menu.isActive) {
			gamepad.draw(gl);
		}

		// Menu
		menu.draw(gl, manager);
	}

	@Override
	public void onTouchEvent(MotionEvent event) {
		if (gamepad == null || menu == null) {
			return;
		}

		if (!menu.isActive) {
			int result = gamepad.onTouchEvent(event);

			if (result == Gamepad.LEFT_DOWN) {
				leftDown = true;
			} else if (result == Gamepad.LEFT_UP) {
				leftDown = false;
			} else if (result == Gamepad.RIGHT_DOWN) {
				rightDown = true;
			} else if (result == Gamepad.RIGHT_UP) {
				rightDown = false;
			} else if (result == Gamepad.ACTION_DOWN) {
				jumpDown = true;
			} else if (result == Gamepad.ACTION_UP) {
				jumpDown = false;
			} else if (result == Gamepad.MENU_UP) {
				menu.switchToMenu(MENU_MAIN);
				menu.setVisible(MENU_MAIN, 1,
						model.player.state != Char.STATE_DEAD
								&& model.player.onFloor);
			}
		} else {
			int value = menu.onTouchEvent(event);
			if (value == 0) {
				menu.switchOff();
			} else if (value == 1) {
				escapeToMinimap();
			}
		}
	}

	@Override
	public void onKeyDown(int keycode) {
		if (!menu.isActive) {
			if (keycode == KeyEvent.KEYCODE_DPAD_LEFT) {
				jumpDown = true;
			} else if (keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				rightDown = true;
			} else if (keycode == KeyEvent.KEYCODE_DPAD_UP) {
				jumpDown = true;
			}
		}
	}

	@Override
	public void onKeyUp(int keycode) {
		if (!menu.isActive) {
			if (keycode == KeyEvent.KEYCODE_DPAD_LEFT) {
				leftDown = false;
			} else if (keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				rightDown = false;
			} else if (keycode == KeyEvent.KEYCODE_DPAD_UP) {
				jumpDown = false;
			} else if (keycode == KeyEvent.KEYCODE_BACK) {
				escapeToMinimap();
			}
		}

		if (keycode == KeyEvent.KEYCODE_MENU) {
			if (!menu.isActive) {
				menu.switchToMenu(MENU_MAIN);
				menu.setVisible(MENU_MAIN, 1,
						model.player.state != Char.STATE_DEAD
								&& model.player.onFloor);
			} else {
				menu.switchOff();
			}
		}
	}

	private void escapeToMinimap() {
		if (model.player.state != Char.STATE_DEAD && model.player.onFloor) {
			manager.bundle.putInt("coins", model.coins);
			manager.bundle.putInt("lives", model.lives);
			manager.bundle.putBoolean("guardian", false);
			Bundle output = new Bundle();
			output.putString("mapname", mapname);
			output.putBoolean("wasVictory", false);
			menu.switchToState("minimap", output);
		}
	}

	@Override
	public void coinChanged(Model model) {

	}

	@Override
	public void lifeChanged(Model model) {

	}

	@Override
	public void boxDestroyed(Model model, int x, int y) {

	}

	@Override
	public void coinCollected(Model model, int x, int y) {

	}

	@Override
	public void enemyKilled(Model model, int x, int y) {

	}

	@Override
	public void playerKilled(Model model) {
		Log.d(TAG, "Player killed! Lives left: " + model.lives);
		if (model.lives < 0) {
			Bundle output = new Bundle();
			output.putInt("nextMenu", MenuState.MENU_GAMEOVER);
			menu.switchToState("menu", output);
		}
	}

	@Override
	public void levelWon(Model model) {
		manager.bundle.putInt("coins", model.coins);
		manager.bundle.putInt("lives", model.lives);
		manager.bundle.putBoolean("guardian", model.player.hasGuardian);
		Bundle output = new Bundle();
		output.putString("mapname", mapname);
		output.putBoolean("wasVictory", true);
		// manager.changeState("minimap", output);
		menu.switchToState("minimap", output);
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