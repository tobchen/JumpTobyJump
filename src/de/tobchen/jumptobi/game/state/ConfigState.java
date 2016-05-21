package de.tobchen.jumptobi.game.state;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import de.tobchen.android.game.manager.GameState;
import de.tobchen.android.game.manager.GfxUtil;
import de.tobchen.android.game.manager.TextureEntry;
import de.tobchen.jumptobi.game.view.Gamepad;
import de.tobchen.util.GeoUtil;
import de.tobchen.util.Rect;

public class ConfigState extends GameState {

	private static final String TAG = "ConfigState";

	private Gamepad gamepad;

	private float gamepadX;
	private float gamepadY;
	private float gamepadWidth;
	private float gamepadHeight;

	private static int ACTION_NONE = -1;
	private static int ACTION_SCALE = 0;
	private static int ACTION_MOVE = 1;
	private int currentAction;

	private int oldX;
	private int oldY;

	private int origX;
	private int origY;

	private ArrayList<Rect> elements;
	private Rect currentElement;

	private boolean initializingTodo;
	private TextureEntry screenTex;
	private TextureEntry configTex;

	public ConfigState(String tag) {
		super(tag);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(Bundle data) {
		gamepad = new Gamepad(manager.width, manager.height,
				manager.getContext());

		gamepadWidth = manager.width - 100;
		gamepadHeight = gamepadWidth * manager.height / manager.width;
		if (gamepadHeight > manager.height - 100) {
			gamepadHeight = manager.height - 100;
			gamepadWidth = gamepadHeight * manager.width / manager.height;
		}
		gamepadX = manager.width - gamepadWidth;
		gamepadY = 0;

		initializingTodo = true;

		// Reset values
		currentAction = ACTION_NONE;
		currentElement = null;

		// Create rects
		elements = new ArrayList<Rect>();
		elements.add(gamepad.action);
		elements.add(gamepad.move);
		elements.add(gamepad.menu);
		elements.add(gamepad.screen);

		System.gc();
	}

	@Override
	public void stop() {
		gamepad = null;
		elements = null;
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(GL10 gl) {
		if (initializingTodo) {
			manager.freeTextures(gl);
			try {
				gamepad.initialize(gl, manager);
				screenTex = manager.loadTexture(gl, "gfx/screen.png",
						GL10.GL_LINEAR);
				configTex = manager.loadTexture(gl, "gfx/config.png",
						GL10.GL_LINEAR);
			} catch (IOException e) {
				// Do nothing
			}
			initializingTodo = false;
		}

		// 2D
		GfxUtil.turn2D(gl, manager.width, manager.height);

		// Matrix to emulate the whole screen
		gl.glPushMatrix();
		gl.glTranslatef(gamepadX, gamepadY, 0);
		gl.glScalef(gamepadWidth / manager.width, gamepadHeight
				/ manager.height, 1);

		// Screen border
		gl.glColor4f(0.3f, 0.3f, 0.3f, 1f);
		GfxUtil.drawRect(gl, 0, 0, manager.width, manager.height);
		gl.glColor4f(1f, 1f, 1f, 1f);

		// Screen element
		GfxUtil.drawImage(gl, gamepad.screen.x, gamepad.screen.y,
				gamepad.screen.width, gamepad.screen.height, screenTex);

		// Other gamepad elements (buttons)
		gamepad.draw(gl);

		// Highlight current element
		if (currentElement != null) {
			gl.glColor4f(0f, 1f, 0f, 0.5f);
			GfxUtil.drawRect(gl, currentElement.x, currentElement.y,
					currentElement.width, currentElement.height);
			gl.glColor4f(1f, 1f, 1f, 1f);
		}

		// Get rid of screen emulating matrix
		gl.glPopMatrix();

		// Display config buttons
		int screenHeightThird = manager.height / 3;
		int offY = (screenHeightThird - 100) / 2;
		GfxUtil.drawImageRect(gl, 0, offY, 100, 100, configTex, 0f, 0f, 0.5f,
				0.5f);
		GfxUtil.drawImageRect(gl, 0, screenHeightThird + offY, 100, 100,
				configTex, 0.5f, 0f, 0.5f, 0.5f);
		GfxUtil.drawImageRect(gl, 0, screenHeightThird * 2 + offY, 100, 100,
				configTex, 0f, 0.5f, 0.5f, 0.5f);
		if (currentElement != null) {
			GfxUtil.drawImageRect(gl, manager.width - 170, manager.height - 90,
					160, 80, configTex, 0.5f, 0.75f, 0.5f, 0.25f);
		}
	}

	@Override
	public void onTouchEvent(MotionEvent event) {
		int action = event.getAction();

		if (action == MotionEvent.ACTION_DOWN) {
			int x = (int) ((event.getX() - gamepadX) * (manager.width / gamepadWidth));
			int y = (int) ((event.getY() - gamepadY) * (manager.height / gamepadHeight));
			if (GeoUtil
					.isPointInRect(x, y, 0, 0, manager.width, manager.height)) {
				for (Rect element : elements) {
					if (GeoUtil.isPointInRect(x, y, element.x, element.y,
							element.width, element.width)) {
						currentElement = element;
						origX = currentElement.x;
						origY = currentElement.y;
						currentAction = ACTION_MOVE;
						oldX = x;
						oldY = y;
						break;
					}
				}
			}
		} else if (action == MotionEvent.ACTION_UP) {
			if (currentAction == ACTION_NONE) {
				int x = (int) event.getX();
				int y = (int) event.getY();

				// Config buttons
				int screenHeightThird = manager.height / 3;
				int offY = (screenHeightThird - 100) / 2;
				if (GeoUtil.isPointInRect(x, y, 0, offY, 100, 100)) {
					// OK
					gamepad.saveUI(manager.getContext());
					manager.changeState("menu", null);
				} else if (GeoUtil.isPointInRect(x, y, 0, screenHeightThird
						+ offY, 100, 100)) {
					// Discard
					manager.changeState("menu", null);
				} else if (GeoUtil.isPointInRect(x, y, 0, screenHeightThird * 2
						+ offY, 100, 100)) {
					// Reset
					gamepad.loadUI(manager.width, manager.height, null);
				}

				// change size
				if (currentElement != null) {
					if (GeoUtil.isPointInRect(x, y, manager.width - 170,
							manager.height - 90, 80, 80)) {
						currentElement.width *= 0.95;
						currentElement.height *= 0.95;
					} else if (GeoUtil.isPointInRect(x, y, manager.width - 90,
							manager.height - 90, 80, 80)) {
						currentElement.width *= 1.05;
						currentElement.height *= 1.05;
					}
				}
			}
			currentAction = ACTION_NONE;
		} else if (action == MotionEvent.ACTION_MOVE) {
			int x = (int) ((event.getX() - gamepadX) * (manager.width / gamepadWidth));
			int y = (int) ((event.getY() - gamepadY) * (manager.height / gamepadHeight));
			if (currentAction == ACTION_MOVE) {
				int dX = x - oldX;
				int dY = y - oldY;
				oldX = x;
				oldY = y;
				origX += dX;
				origY += dY;
				currentElement.x = origX;
				currentElement.y = origY;
			}

			if (currentElement != null) {
				if (currentElement.x < 0) {
					currentElement.x = 0;
				}
				if (currentElement.x + currentElement.width > manager.width) {
					currentElement.x = manager.width - currentElement.width;
				}
				if (currentElement.y < 0) {
					currentElement.y = 0;
				}
				if (currentElement.y + currentElement.height > manager.height) {
					currentElement.y = manager.height - currentElement.height;
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
		if (keycode == KeyEvent.KEYCODE_BACK) {
			manager.changeState("menu", null);
		}
	}

	@Override
	public Bundle save() {
		// TODO Save Gamepad data
		return new Bundle();
	}

	@Override
	public void restore(Bundle bundle) {
		// TODO Take Gamepad data
	}
}
