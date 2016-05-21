package de.tobchen.jumptobi.game.view;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import de.tobchen.android.game.manager.GameManager;
import de.tobchen.android.game.manager.GfxUtil;
import de.tobchen.android.game.manager.TextureEntry;
import de.tobchen.util.GeoUtil;
import de.tobchen.util.Rect;

public class Gamepad {
	public static final int ACTION_NONE = 0;
	public static final int ACTION_DOWN = 1;
	public static final int ACTION_UP = 2;
	public static final int LEFT_DOWN = 3;
	public static final int LEFT_UP = 4;
	public static final int RIGHT_DOWN = 5;
	public static final int RIGHT_UP = 6;
	public static final int MENU_DOWN = 7;
	public static final int MENU_UP = 8;

	public Rect move;
	public Rect action;
	public Rect menu;

	public Rect screen;

	private int keyRightId;
	private int keyLeftId;
	private int keyActionId;
	private int keyMenuId;

	private TextureEntry gamepad;

	public Gamepad(int width, int height, Context context) {
		move = new Rect(0, 0, 0, 0);
		action = new Rect(0, 0, 0, 0);
		menu = new Rect(0, 0, 0, 0);
		screen = new Rect(0, 0, 0, 0);
		loadUI(width, height, context);

		keyLeftId = -1;
		keyRightId = -1;
		keyActionId = -1;
		keyMenuId = -1;
	}

	public void initialize(GL10 gl, GameManager manager) throws IOException {
		gamepad = manager.loadTexture(gl, "gfx/gamepad.png", GL10.GL_LINEAR);
	}

	public void configurScreen(GL10 gl, int width, int height) {
		gl.glScissor(screen.x, height - screen.height - screen.y, screen.width,
				screen.height);
		gl.glEnable(GL10.GL_SCISSOR_TEST);
		gl.glPushMatrix();
		gl.glTranslatef(screen.x, screen.y, 0);
		gl.glScalef(screen.width / 256.0f, screen.height / 224.0f, 1);
	}

	public void deconfigureScreen(GL10 gl) {
		gl.glPopMatrix();
		gl.glDisable(GL10.GL_SCISSOR_TEST);
	}

	public void draw(GL10 gl) {
		GfxUtil.drawImageRect(gl, move.x, move.y, move.width, move.height,
				gamepad, 0f, 0f, 1f, 0.5f);
		GfxUtil.drawImageRect(gl, action.x, action.y, action.width,
				action.height, gamepad, 0.0f, 0.5f, 0.5f, 0.5f);
		GfxUtil.drawImageRect(gl, menu.x, menu.y, menu.width, menu.height,
				gamepad, 0.5f, 0.5f, 0.5f, 0.5f);
	}

	public void loadUI(int width, int height, Context context) {
		// Calculate ui
		move.width = (int) (width * 0.375f);
		move.height = move.width / 2;
		move.x = 0;
		move.y = height - move.height;

		action.width = move.height;
		action.height = move.height;
		action.x = width - action.width;
		action.y = height - action.height;

		menu.width = action.width / 2;
		menu.height = action.height / 2;
		menu.x = width - menu.width;
		menu.y = 0;

		screen.height = height - move.height;
		screen.width = (int) (screen.height * (256.0 / 224.0));
		screen.y = 0;
		screen.x = (width - screen.width) / 2;

		// Load ui preferences
		if (context != null) {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(context);

			move.width = preferences.getInt("movePadWidth", move.width);
			move.height = preferences.getInt("movePadHeight", move.height);
			move.x = preferences.getInt("movePadX", move.x);
			move.y = preferences.getInt("movePadY", move.y);

			action.width = preferences.getInt("actionPadWidth", action.width);
			action.height = preferences
					.getInt("actionPadHeight", action.height);
			action.x = preferences.getInt("actionPadX", action.x);
			action.y = preferences.getInt("actionPadY", action.y);

			menu.width = preferences.getInt("menuPadWidth", menu.width);
			menu.height = preferences.getInt("menuPadHeight", menu.height);
			menu.x = preferences.getInt("menuPadX", menu.x);
			menu.y = preferences.getInt("menuPadY", menu.y);

			screen.width = preferences.getInt("screenWidth", screen.width);
			screen.height = preferences.getInt("screenHeight", screen.height);
			screen.x = preferences.getInt("screenX", screen.x);
			screen.y = preferences.getInt("screenY", screen.y);
		}
	}

	public int onTouchEvent(MotionEvent event) {
		int result = 0;

		int eventAction = event.getAction();
		int actionCode = eventAction & MotionEvent.ACTION_MASK;

		if (actionCode == MotionEvent.ACTION_DOWN
				|| actionCode == MotionEvent.ACTION_POINTER_DOWN) {
			int id;
			if (actionCode == MotionEvent.ACTION_DOWN)
				id = event.getPointerId(0);
			else
				id = eventAction >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			for (int i = 0; i < event.getPointerCount(); i++) {
				if (id == event.getPointerId(i)) {
					float x = event.getX(i);
					float y = event.getY(i);
					if (keyLeftId < 0
							&& GeoUtil.isPointInRect(x, y, move.x, move.y,
									move.width / 2, move.height)) {
						keyLeftId = id;
						result = LEFT_DOWN;
					}
					if (keyRightId < 0
							&& GeoUtil.isPointInRect(x, y, move.x
									+ (move.width / 2), move.y, move.width / 2,
									move.height)) {
						keyRightId = id;
						result = RIGHT_DOWN;
					}
					if (keyActionId < 0
							&& GeoUtil.isPointInRect(x, y, action.x, action.y,
									action.width, action.height)) {
						keyActionId = id;
						result = ACTION_DOWN;
					}
					if (keyMenuId < 0
							&& GeoUtil.isPointInRect(x, y, menu.x, menu.y,
									menu.width, menu.height)) {
						keyMenuId = id;
						result = MENU_DOWN;
					}
				}

			}
		} else if (actionCode == MotionEvent.ACTION_UP
				|| actionCode == MotionEvent.ACTION_POINTER_UP) {
			int id;
			if (actionCode == MotionEvent.ACTION_UP)
				id = event.getPointerId(0);
			else
				id = eventAction >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			if (id == keyLeftId) {
				keyLeftId = -1;
				result = LEFT_UP;
			} else if (id == keyRightId) {
				keyRightId = -1;
				result = RIGHT_UP;
			} else if (id == keyActionId) {
				keyActionId = -1;
				result = ACTION_UP;
			} else if (id == keyMenuId) {
				keyMenuId = -1;
				result = MENU_UP;
			}
		}

		return result;
	}

	public void saveUI(Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = preferences.edit();

		editor.putInt("movePadWidth", move.width);
		editor.putInt("movePadHeight", move.height);
		editor.putInt("movePadX", move.x);
		editor.putInt("movePadY", move.y);

		editor.putInt("actionPadWidth", action.width);
		editor.putInt("actionPadHeight", action.height);
		editor.putInt("actionPadX", action.x);
		editor.putInt("actionPadY", action.y);

		editor.putInt("menuPadWidth", menu.width);
		editor.putInt("menuPadHeight", menu.height);
		editor.putInt("menuPadX", menu.x);
		editor.putInt("menuPadY", menu.y);

		editor.putInt("screenWidth", screen.width);
		editor.putInt("screenHeight", screen.height);
		editor.putInt("screenX", screen.x);
		editor.putInt("screenY", screen.y);

		editor.commit();
	}
}