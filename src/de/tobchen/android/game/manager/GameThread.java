package de.tobchen.android.game.manager;

import android.util.Log;

public class GameThread extends Thread {

	private static final String TAG = "GameThread";

	protected boolean running;
	private GameManager manager;

	private long lastSecond;
	private int frameCount;

	public GameThread(GameManager manager) {
		super();

		this.manager = manager;
		this.running = false;
	}

	@Override
	public void run() {
		long startMS, endMS, sleepMS;

		while (running) {
			startMS = System.currentTimeMillis();
			manager.requestRender();
			countFPS();
			endMS = System.currentTimeMillis();

			sleepMS = 33 - endMS + startMS;
			if (sleepMS < 0) {
				sleepMS = 0;
			}
			try {
				sleep(sleepMS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void countFPS() {
		frameCount++;
		if (System.currentTimeMillis() >= lastSecond + 1000) {
			if (frameCount < 28) {
				Log.d(TAG, "FPS far less than 30!! FPS: " + frameCount);
			}
			frameCount = 0;
			lastSecond = System.currentTimeMillis();
		}
	}
}
