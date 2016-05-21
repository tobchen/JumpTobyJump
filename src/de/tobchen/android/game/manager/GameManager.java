package de.tobchen.android.game.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class GameManager extends GLSurfaceView implements
		GLSurfaceView.Renderer {

	private static final String TAG = "GameManager";

	private GameThread thread;

	private GameState currentState;
	private Map<String, GameState> states;

	public Bundle bundle;

	public int width;
	public int height;

	private boolean changeStateWanted;
	private String changeStateTag;
	private Bundle changeStateData;

	private boolean restoreWanted;
	private Bundle restoreData;

	private ArrayList<TextureEntry> textures;
	private TextureEntry loadingTex;

	public GameManager(Context context) {
		super(context);

		setRenderer(this);
		setRenderMode(RENDERMODE_WHEN_DIRTY);

		this.thread = new GameThread(this);

		this.states = new HashMap<String, GameState>();

		this.textures = new ArrayList<TextureEntry>();

		this.changeStateWanted = false;

		this.bundle = new Bundle();

		setFocusable(true);
	}

	public void addState(GameState state) {
		Log.d(TAG, "Add state");

		state.putManagerInfo(this);
		states.put(state.tag, state);
	}

	public void setState(String tag) {
		Log.d(TAG, "Set state: " + tag);

		changeState(tag, null);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		super.surfaceCreated(holder);

		Log.d(TAG, "surfaceCreated!");

		if (thread.getState() == Thread.State.TERMINATED) {
			Log.d(TAG, "Old GameThread terminated, creating new one");
			thread = new GameThread(this);
		}
		thread.running = true;
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		super.surfaceDestroyed(holder);

		Log.d(TAG, "surfaceDestroyed!");

		Log.d(TAG, "Attempting stopping GameThread");
		thread.running = false;
		boolean retry = true;
		while (retry) {
			try {
				thread.join();
				retry = false;
				Log.d(TAG, "Stopping GameThread succeeded");
			} catch (InterruptedException e) {
				// try again
				Log.d(TAG, "Failed to stop GameThread, try again...");
			}
		}
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		super.onTouchEvent(event);

		if (currentState != null) {
			currentState.onTouchEvent(event);
		}
		return true;
	}

	public void onKeyDown(int keycode) {
		if (currentState != null) {
			currentState.onKeyDown(keycode);
		}
	}

	public void onKeyUp(int keycode) {
		if (currentState != null) {
			currentState.onKeyUp(keycode);
		}
	}

	public void changeState(String tag, Bundle data) {
		Log.d(TAG, "Change state initialized");

		changeStateWanted = true;
		changeStateTag = tag;
		changeStateData = data;
	}

	protected void restore(Bundle bundle) {
		String nextState = bundle.getString("MANAGER_currentState");
		Bundle nextData = bundle.getBundle("MANAGER_data");
		this.bundle = bundle.getBundle("MANAGER_bundle");
		restoreData = bundle.getBundle("MANAGER_stateData");
		restoreWanted = true;
		changeState(nextState, nextData);
	}

	protected void save(Bundle bundle) {
		bundle.putString("MANAGER_currentState", currentState.tag);
		bundle.putBundle("MANAGER_bundle", this.bundle);
		bundle.putBundle("MANAGER_data", changeStateData);
		bundle.putBundle("MANAGER_stateData", currentState.save());
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		if (currentState != null) {
			currentState.update();
			currentState.draw(gl);
		}

		if (changeStateWanted) {
			GfxUtil.turn2D(gl, width, height);
			GfxUtil.drawImage(gl, 5, height - 69, 64, 64, loadingTex);

			Log.d(TAG, "Change state to " + changeStateTag);
			if (currentState != null) {
				currentState.stop();
			}
			currentState = states.get(changeStateTag);
			currentState.start(changeStateData);

			changeStateWanted = false;

			if (restoreWanted) {
				Log.d(TAG, "Restore state");
				currentState.restore(restoreData);
				restoreWanted = false;
			}

			Log.d(TAG,
					"Call garbage collection after state change (and restore)");
			System.gc();
		}
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.d(TAG, "onSurfaceCreated!");

		gl.glEnable(GL10.GL_TEXTURE_2D);

		gl.glEnable(GL10.GL_CULL_FACE);

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

		gl.glClearColor(0f, 0f, 0f, 0.5f);
		gl.glClearDepthf(1.0f);

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.d(TAG, "onSurfaceChanged!");

		this.width = width;
		this.height = height;

		// TODO Maybe notify states

		// TODO Why not in onSurfaceCreated?
		// Reload textures
		Log.d(TAG, "Reload textures...");
		for (TextureEntry texture : textures) {
			try {
				Log.d(TAG, "Reload '" + texture.path + "'");
				loadTextureHelper(gl, texture);
			} catch (IOException e) {
				Log.d(TAG, "Failed to load texture: " + texture.path);
				texture.texture = new int[] { 0 };
			}
		}
		// Load "Loading" texture
		if (loadingTex != null) {
			gl.glDeleteTextures(1, loadingTex.texture, 0);
		}
		try {
			loadingTex = new TextureEntry("manager/loading.png", GL10.GL_LINEAR);
			loadTextureHelper(gl, loadingTex);
		} catch (IOException e) {
			loadingTex = new TextureEntry("manager/loading.png", GL10.GL_LINEAR);
			loadingTex.texture = new int[] { 0 };
		}
	}

	public TextureEntry loadTexture(GL10 gl, String path) throws IOException {
		return this.loadTexture(gl, path, GL10.GL_NEAREST);
	}

	public TextureEntry loadTexture(GL10 gl, String path, int magFilter)
			throws IOException {
		TextureEntry entry = new TextureEntry(path, magFilter);
		loadTextureHelper(gl, entry);
		this.textures.add(entry);
		return entry;
	}

	private TextureEntry loadTextureHelper(GL10 gl, TextureEntry entry)
			throws IOException {
		Bitmap bitmap = null;
		gl.glDeleteTextures(1, entry.texture, 0);
		gl.glGenTextures(1, entry.texture, 0);

		InputStream is = getContext().getAssets().open(entry.path);

		try {
			bitmap = BitmapFactory.decodeStream(is);
		} finally {
			is.close();
			is = null;
		}

		gl.glBindTexture(GL10.GL_TEXTURE_2D, entry.texture[0]);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				entry.magFilter);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_REPEAT);

		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

		bitmap.recycle();

		return entry;
	}

	public void freeTexture(GL10 gl, int[] texture) {
		int size = textures.size();
		for (int i = 0; i < size; i++) {
			TextureEntry entry = textures.get(i);
			if (texture == entry.texture) {
				gl.glDeleteTextures(1, entry.texture, 0);
				textures.remove(i);
				break;
			}
		}
	}

	public void freeTextures(GL10 gl) {
		int size = textures.size();
		for (int i = 0; i < size; i++) {
			TextureEntry entry = textures.get(i);
			gl.glDeleteTextures(1, entry.texture, 0);
		}
		textures.clear();
	}
}