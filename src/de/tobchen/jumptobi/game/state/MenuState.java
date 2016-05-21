package de.tobchen.jumptobi.game.state;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import de.tobchen.android.game.manager.GameState;
import de.tobchen.android.game.manager.GfxUtil;
import de.tobchen.android.game.manager.TextureEntry;
import de.tobchen.jumptobi.game.VersionInfo;
import de.tobchen.jumptobi.game.view.Menu;
import de.tobchen.util.MiscUtil;

public class MenuState extends GameState {

	// States (IN this state, awesome)
	public static final int MENU_MAIN = 0;
	public static final int MENU_LOAD = 1;
	public static final int MENU_GAMEOVER = 2;

	// Textures
	private boolean initializingTodo;
	private TextureEntry background;
	private TextureEntry title;

	// Menu
	private Menu menu;

	public MenuState(String tag) {
		super(tag);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(Bundle data) {
		// Request initizalizing
		initializingTodo = true;

		// Menu
		menu = new Menu();
		menu.addMenu("gfx/menu_main.png", MENU_MAIN, new int[] { 1, 1, 1 },
				manager.width, manager.height);
		menu.addMenu("gfx/menu_loadsave.png", MENU_LOAD, new int[] { 3, 1 },
				manager.width, manager.height);
		menu.addMenu("gfx/menu_gameover.png", MENU_GAMEOVER,
				new int[] { 1, 1 }, manager.width, manager.height);
		menu.hasBackground = false;

		// Make menu switch
		int nextMenu = MENU_MAIN;
		if (data != null) {
			nextMenu = data.getInt("nextMenu", MENU_MAIN);
		}
		menu.switchToMenu(nextMenu);

		// Prepare menus
		prepareLoadMenu();

		// Call GC
		System.gc();
	}

	@Override
	public void stop() {
		// TODO Delete textures
		menu = null;
	}

	@Override
	public void update() {
		menu.update(manager);
	}

	@Override
	public void draw(GL10 gl) {
		// Initialize
		if (initializingTodo) {
			manager.freeTextures(gl);
			try {
				background = manager.loadTexture(gl, "gfx/menu_bg.png",
						GL10.GL_LINEAR);
				title = manager
						.loadTexture(gl, "gfx/title.png", GL10.GL_LINEAR);
			} catch (IOException e) {
				// Do nothing except for maybe cry
				e.printStackTrace();
			}
			initializingTodo = false;
		}

		// The whole game is 2D
		GfxUtil.turn2D(gl, manager.width, manager.height);

		// Draw background
		GfxUtil.drawImage(gl, 0, 0, manager.width, manager.height, background);

		// Draw title
		int size = manager.width / 2 - 40;
		GfxUtil.drawImage(gl, manager.width / 2 + 20,
				(manager.height - size) / 2, size, size, title);

		// Draw menu
		menu.draw(gl, manager);
	}

	@Override
	public void onTouchEvent(MotionEvent event) {
		int value = menu.onTouchEvent(event);

		if (menu.currentMenu == MENU_MAIN) {
			if (value == 0) {
				manager.bundle.clear();
				menu.switchToState("minimap", null);
			} else if (value == 1) {
				menu.switchToMenu(MENU_LOAD);
			} else if (value == 2) {
				menu.switchToState("config", null);
			}
		} else if (menu.currentMenu == MENU_LOAD) {
			if (value >= 0 && value <= 2) {
				int profile = value + 1;
				// TODO New method for loading
				SharedPreferences preferences = PreferenceManager
						.getDefaultSharedPreferences(manager.getContext());
				if (preferences.getBoolean("profile" + profile, false)) {
					manager.bundle.putInt("highestLevel",
							preferences.getInt("highestLevel" + profile, 0));
					manager.bundle.putInt("coins",
							preferences.getInt("coins" + profile, 0));
					manager.bundle.putInt("lives",
							preferences.getInt("lives" + profile, 4));
					manager.bundle
							.putBoolean("guardian", preferences.getBoolean(
									"guardian" + profile, false));
				}
				menu.switchToState("minimap", null);
			}
			if (value == 3) {
				menu.switchToMenu(MENU_MAIN);
			}
		} else if (menu.currentMenu == MENU_GAMEOVER) {
			if (value == 1) {
				menu.switchToMenu(MENU_MAIN);
			}
		}
	}

	@Override
	public void onKeyDown(int keycode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onKeyUp(int keycode) {
		// TODO Auto-generated method stub

	}

	private void prepareLoadMenu() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(manager.getContext());
		for (int i = 1; i <= 3; i++) {
			menu.setVisible(
					MENU_LOAD,
					i - 1,
					preferences.getBoolean("profile" + i, false)
							&& MiscUtil.arrayContains(
									VersionInfo.supportedSaveVersions,
									preferences.getInt("version" + i, 0)));
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