package de.tobchen.android.game.manager;

import javax.microedition.khronos.opengles.GL10;

import android.os.Bundle;
import android.view.MotionEvent;

public abstract class GameState {

	protected String tag;
	protected GameManager manager;

	public GameState(String tag) {
		this.tag = tag;
	}

	protected void putManagerInfo(GameManager manager) {
		this.manager = manager;

		initialize();
	}

	public abstract void initialize();

	public abstract void start(Bundle data);

	public abstract void stop();

	public abstract void update();

	public abstract void draw(GL10 gl);

	public abstract void onTouchEvent(MotionEvent event);

	public abstract void onKeyDown(int keycode);

	public abstract void onKeyUp(int keycode);

	public abstract Bundle save();

	public abstract void restore(Bundle bundle);
}
