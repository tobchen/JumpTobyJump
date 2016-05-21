package de.tobchen.android.game.manager;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLU;
import de.tobchen.util.MiscUtil;

public class GfxUtil {
	public static void turn2D(final GL10 gl, final int width, final int height) {
		preTurn(gl, width, height);

		gl.glDisable(GL10.GL_DEPTH_TEST);
		GLU.gluOrtho2D(gl, 0, width, height, 0);

		postTurn(gl);
	}

	public static void turn3D(final GL10 gl, final int width, final int height,
			final float fovy) {
		turn3D(gl, width, height, fovy, 1.0f, 100.0f);
	}

	public static void turn3D(final GL10 gl, final int width, final int height,
			final float fovy, final float near, final float far) {
		preTurn(gl, width, height);

		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);

		GLU.gluPerspective(gl, fovy, (float) width / (float) height, near, far);
		// gl.glShadeModel(GL10.GL_SMOOTH);

		postTurn(gl);
	}

	private static void preTurn(final GL10 gl, final int width, final int height) {
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
	}

	private static void postTurn(final GL10 gl) {
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public static void drawBuffers(GL10 gl, FloatBuffer vertices,
			FloatBuffer uv, ShortBuffer indices, TextureEntry texture) {
		drawBuffers(gl, vertices, uv, indices, indices.capacity(), texture);
	}

	public static void drawBuffers(GL10 gl, FloatBuffer vertices,
			FloatBuffer uv, ShortBuffer indices, int indexLength,
			TextureEntry texture) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture.texture[0]);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glFrontFace(GL10.GL_CCW);

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, uv);

		gl.glDrawElements(GL10.GL_TRIANGLES, indexLength,
				GL10.GL_UNSIGNED_SHORT, indices);

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}

	public static void drawImage(GL10 gl, int x, int y, int width, int height,
			TextureEntry texture) {
		drawImage(gl, x, y, width, height, texture, 0, 0, 0);
	}

	public static void drawImage(GL10 gl, int x, int y, int width, int height,
			TextureEntry texture, int rotation, int transX, int transY) {
		// TODO Maybe change to rect if texture is nonexistant

		// Initialize image (if nessecary)
		if (imageVertices == null || imageUV == null || imageIndices == null) {
			initializeImage();
		}

		// Matrix stuff
		gl.glPushMatrix();
		gl.glTranslatef(x, y, 0); // translate to x, y
		if (rotation != 0) { // if rotation is wanted
			gl.glRotatef(rotation, 0, 0, 1); // rotate
			gl.glTranslatef(-transX, -transY, 0); // translate to "center"
		}
		gl.glScalef(width, height, 0); // scale to image size

		drawBuffers(gl, imageVertices, imageUV, imageIndices, 6, texture);

		// Reverse matrix stuff
		gl.glPopMatrix();
	}

	public static void drawImageRect(GL10 gl, int x, int y, int width,
			int height, TextureEntry texture, float offX, float offY,
			float scaleX, float scaleY) {
		drawImageRect(gl, x, y, width, height, texture, offX, offY, scaleX,
				scaleY, 0, 0, 0);
	}

	public static void drawImageRect(GL10 gl, int x, int y, int width,
			int height, TextureEntry texture, float offX, float offY,
			float scaleX, float scaleY, int rotation, int transX, int transY) {
		gl.glMatrixMode(GL10.GL_TEXTURE);
		gl.glTranslatef(offX, offY, 0);
		gl.glScalef(scaleX, scaleY, 1f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);

		drawImage(gl, x, y, width, height, texture, rotation, transX, transY);

		gl.glMatrixMode(GL10.GL_TEXTURE);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
	}

	private static TextureEntry rectTexture;

	public static void drawRect(GL10 gl, int x, int y, int width, int height) {
		if (rectTexture == null) {
			rectTexture = new TextureEntry(null, 0);
			rectTexture.texture = new int[] { 0 };
		}
		drawImage(gl, x, y, width, height, rectTexture);
	}

	private static FloatBuffer imageVertices = null;
	private static FloatBuffer imageUV = null;
	private static ShortBuffer imageIndices = null;

	private static void initializeImage() {
		float[] vertices = new float[12];
		vertices[0] = 0f; // First
		vertices[1] = 1f;
		vertices[2] = 0f;
		vertices[3] = 0f; // Second
		vertices[4] = 0f;
		vertices[5] = 0f;
		vertices[6] = 1f; // Third
		vertices[7] = 0f;
		vertices[8] = 0f;
		vertices[9] = 1f; // Fourth
		vertices[10] = 1f;
		vertices[11] = 0f;

		float[] uv = new float[8];
		uv[0] = 0f; // First
		uv[1] = 1f;
		uv[2] = 0f; // Second
		uv[3] = 0f;
		uv[4] = 1f; // Third
		uv[5] = 0f;
		uv[6] = 1f; // Fourth
		uv[7] = 1f;

		short[] indices = new short[6];
		indices[0] = 2; // First
		indices[1] = 1;
		indices[2] = 0;
		indices[3] = 3; // Second
		indices[4] = 2;
		indices[5] = 0;

		imageVertices = MiscUtil.bufferFromArray(vertices);
		imageUV = MiscUtil.bufferFromArray(uv);
		imageIndices = MiscUtil.bufferFromArray(indices);
	}
}
