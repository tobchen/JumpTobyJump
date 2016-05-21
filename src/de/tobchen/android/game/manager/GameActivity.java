package de.tobchen.android.game.manager;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

// TODO onSaveInstanceState
public class GameActivity extends Activity {
	// Manager
	protected GameManager manager;

	// Tag
	private static final String TAG = "GameActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "onCreate()");

		// Create manager and set content view
		manager = new GameManager(getApplicationContext());
		setContentView(manager);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		Log.d(TAG, "onRestoreInstanceState()");
		manager.restore(state);
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
		Log.d(TAG, "onSaveInstanceState()");
		manager.save(state);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.d(TAG, "onDestroy()");
	}

	@Override
	protected void onPause() {
		super.onPause();

		Log.d(TAG, "onPause()");

		// Notify manager
		manager.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.d(TAG, "onResume()");

		// Notify manager
		manager.onResume();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onKeyUp(int keycode, KeyEvent event) {
		manager.onKeyUp(keycode);
		return true;
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent event) {
		manager.onKeyDown(keycode);
		return true;
	}
}
