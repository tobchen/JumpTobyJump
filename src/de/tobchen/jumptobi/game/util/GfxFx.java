package de.tobchen.jumptobi.game.util;

import javax.microedition.khronos.opengles.GL10;

import de.tobchen.android.game.manager.GfxUtil;
import de.tobchen.android.game.manager.TextureEntry;

public class GfxFx {
	public static void drawNumber(GL10 gl, int number, int x, int y,
			TextureEntry font) {
		if (number < 0) {
			return;
		}
		for (boolean out = false; number > 0 || !out; number /= 10) {
			int frame = number % 10;
			int frameX = frame % 4;
			int frameY = frame / 4;
			GfxUtil.drawImageRect(gl, x, y, 8, 8, font, frameX * 0.25f,
					frameY * 0.25f, 0.25f, 0.25f);
			x = x - 8;
			out = true;
		}
	}
}
