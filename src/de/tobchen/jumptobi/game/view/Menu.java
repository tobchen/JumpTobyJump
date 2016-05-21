package de.tobchen.jumptobi.game.view;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import de.tobchen.android.game.manager.GameManager;
import de.tobchen.android.game.manager.GfxUtil;
import de.tobchen.android.game.manager.TextureEntry;
import de.tobchen.util.GeoUtil;

public class Menu {
	private static final String TAG = "Menu";

	public int currentMenu;
	private MenuLayout currentMenuLayout;

	private int nextMenu;
	private String nextState;
	private Bundle nextStateArguments;
	private boolean switchOff;

	private int menuCounter;

	private Map<Integer, MenuLayout> layouts;

	public final static int NO_MENU = Integer.MIN_VALUE;
	private final static String NO_STATE = "";

	private boolean down;

	public boolean isActive;

	private boolean initializingTodo;

	public boolean hasBackground;
	private int bgCounter;

	public boolean hasBlack;
	private int blackCounter;

	public Menu() {
		layouts = new HashMap<Integer, MenuLayout>();

		currentMenu = NO_MENU;
		currentMenuLayout = null;

		nextMenu = NO_MENU;
		nextState = NO_STATE;
		nextStateArguments = null;
		switchOff = false;
		isActive = false;

		menuCounter = 10;

		hasBackground = true;
		bgCounter = 0;

		hasBlack = true;
		blackCounter = 10;

		initializingTodo = false;
	}

	public void addMenu(String texturePath, int id, int[] layout,
			int screenWidth, int screenHeight) {
		layouts.put(new Integer(id), new MenuLayout(texturePath, layout,
				screenWidth, screenHeight));
		initializingTodo = true;
	}

	public void switchToMenu(int id) {
		nextMenu = id;
		nextState = NO_STATE;
		nextStateArguments = null;
		switchOff = false;
		isActive = true;
	}

	public void switchToState(String state, Bundle arguments) {
		nextState = state;
		nextStateArguments = arguments;
		nextMenu = NO_MENU;
		switchOff = false;
		isActive = true;
	}

	public void switchOff() {
		switchOff = true;
		nextMenu = NO_MENU;
		nextState = NO_STATE;
		nextStateArguments = null;
		isActive = true;
	}

	public void setVisible(int id, int place, boolean visible) {
		MenuLayout layout = layouts.get(new Integer(id));
		outer: for (int y = 0; y < layout.visual.length; y++) {
			for (int x = 0; x < layout.visual[y].length; x++) {
				if (place == 0) {
					layout.visual[y][x] = visible;
					break outer;
				}
				place--;
			}
		}
	}

	public void update(GameManager manager) {
		if (!isActive) {
			menuCounter = 10;
			return;
		}

		if (nextMenu != NO_MENU || nextState != NO_STATE || switchOff
				|| currentMenuLayout == null) {
			menuCounter++;
			if (menuCounter > 10) {
				menuCounter = 10;
				if (nextMenu != NO_MENU) {
					Log.d(TAG, "Switch to menu " + nextMenu);
					currentMenu = nextMenu;
					currentMenuLayout = layouts.get(new Integer(currentMenu));
					nextMenu = NO_MENU;
				} else if (nextState != NO_STATE) {
					Log.d(TAG, "Switch to state " + nextState);
					manager.changeState(nextState, nextStateArguments);
				} else if (switchOff) {
					Log.d(TAG, "Switch off");
					isActive = false;
				}
			}
		} else if (currentMenuLayout != null) {
			menuCounter--;
			if (menuCounter < 0) {
				menuCounter = 0;
			}
		}
		if (currentMenuLayout != null) {
			currentMenuLayout
					.update(menuCounter, manager.width, manager.height);
		}
	}

	public void draw(GL10 gl, GameManager manager) {
		// Draw black
		if (hasBlack) {
			if (nextState == NO_STATE || !isActive) {
				blackCounter--;
				if (blackCounter < 0) {
					blackCounter = 0;
				}
			} else {
				blackCounter++;
				if (blackCounter > 10) {
					blackCounter = 10;
				}
			}
			if (blackCounter != 0) {
				gl.glColor4f(0f, 0f, 0f, blackCounter / 10f);
				GfxUtil.drawRect(gl, 0, 0, manager.width, manager.height);
				gl.glColor4f(1f, 1f, 1f, 1f);
			}
		}

		if (!isActive) {
			return;
		}

		if (initializingTodo) {
			for (Map.Entry<Integer, MenuLayout> entry : layouts.entrySet()) {
				MenuLayout layout = entry.getValue();
				if (layout.texture == null) {
					try {
						layout.texture = manager.loadTexture(gl,
								layout.texturePath, GL10.GL_LINEAR);
						Log.d(TAG, "Load texture for menu " + entry.getKey()
								+ ": " + layout.texturePath);
					} catch (IOException e) {
						// Do nothing, everything's lost
					}
				}
			}
			initializingTodo = false;
		}

		// Draw background
		if (hasBackground) {
			if (!switchOff && isActive) {
				bgCounter++;
				if (bgCounter > 10) {
					bgCounter = 10;
				}
			} else {
				bgCounter--;
				if (bgCounter < 0) {
					bgCounter = 0;
				}
			}
			if (bgCounter != 0) {
				gl.glColor4f(0f, 0f, 0f, bgCounter / 20f);
				GfxUtil.drawRect(gl, 0, 0, manager.width, manager.height);
				gl.glColor4f(1f, 1f, 1f, 1f);
			}
		}

		// Draw current menu
		if (currentMenuLayout != null) {
			currentMenuLayout.draw(gl);
		}
	}

	public int onTouchEvent(MotionEvent event) {
		if (!isActive) {
			return NO_MENU;
		}

		int value = NO_MENU;

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			down = true;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (down) {
				int x = (int) event.getX();
				int y = (int) event.getY();
				if (currentMenuLayout != null) {
					value = currentMenuLayout.whichMenuHit(x, y);
				}
			}

			down = false;
		}

		return value;
	}

	private class MenuLayout {
		public TextureEntry texture;
		public String texturePath;
		public boolean[][] visual;
		public MenuRect[][] rects;

		public MenuLayout(String texturePath, int[] layout, int width,
				int height) {
			visual = new boolean[layout.length][];
			rects = new MenuRect[layout.length][];
			for (int y = 0; y < layout.length; y++) {
				visual[y] = new boolean[layout[y]];
				rects[y] = new MenuRect[layout[y]];
				for (int x = 0; x < layout[y]; x++) {
					visual[y][x] = true;
					rects[y][x] = new MenuRect();
				}
			}

			this.texturePath = texturePath;
			texture = null;

			update(0, width, height);
		}

		public void update(int factor, int width, int height) {
			// TODO Make sure fractions aren't too big
			int managerHeightFraction = height / rects.length;
			float fac = factor / 5f;
			for (int y = 0; y < rects.length; y++) {
				int managerWidthFraction = width / 2 / rects[y].length;
				int menuWidth = managerWidthFraction - 20;
				int menuHeight = menuWidth / (4 - rects[y].length);
				int menuHeightAlt = managerHeightFraction - 20;
				if (menuHeight > menuHeightAlt) {
					menuHeight = menuHeightAlt;
					menuWidth = menuHeight * (4 - rects[y].length);
				}
				int offX = (managerWidthFraction - menuWidth) / 2;
				int offY = (managerHeightFraction - menuHeight) / 2;
				if (factor > 0) {
					menuWidth = (int) (menuWidth * (1 + fac));
					menuHeight = (int) (menuHeight * (1 + fac));
				}
				for (int x = 0; x < rects[y].length; x++) {
					int posX = managerWidthFraction * x + offX;
					int posY = managerHeightFraction * y + offY;

					if (factor > 0) {
						posX = (int) (posX * (1 + fac) - (width * fac));
						posY = (int) ((posY * (1 + fac)) - (height / 2 * fac));
					}

					MenuRect rect = rects[y][x];
					rect.x = posX;
					rect.y = posY;
					rect.width = menuWidth;
					rect.height = menuHeight;
				}
			}
		}

		public void draw(GL10 gl) {
			for (int y = 0; y < rects.length; y++) {
				for (int x = 0; x < rects[y].length; x++) {
					if (visual[y][x] && texture != null) {
						MenuRect rect = rects[y][x];
						GfxUtil.drawImageRect(gl, rect.x, rect.y, rect.width,
								rect.height, texture, 1f / rects[y].length * x,
								0.3333f * y, 1f / rects[y].length, 0.3333f);
					}
				}
			}
		}

		public int whichMenuHit(int clickX, int clickY) {
			int value = NO_MENU;

			int counter = 0;
			outer: for (int y = 0; y < rects.length; y++) {
				for (int x = 0; x < rects[y].length; x++) {
					if (visual[y][x] && texture != null) {
						MenuRect rect = rects[y][x];
						if (GeoUtil.isPointInRect(clickX, clickY, rect.x,
								rect.y, rect.width, rect.height)) {
							value = counter;
							break outer;
						}
					}
					counter++;
				}
			}

			return value;
		}

		private class MenuRect {
			public int x;
			public int y;
			public int width;
			public int height;
		}
	}
}