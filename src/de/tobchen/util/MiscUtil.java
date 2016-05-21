package de.tobchen.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class MiscUtil {
	public static FloatBuffer bufferFromArray(float[] array) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer buffer = byteBuffer.asFloatBuffer();
		buffer.put(array);
		buffer.position(0);
		return buffer;
	}

	public static ShortBuffer bufferFromArray(short[] array) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length * 2);
		byteBuffer.order(ByteOrder.nativeOrder());
		ShortBuffer buffer = byteBuffer.asShortBuffer();
		buffer.put(array);
		buffer.position(0);
		return buffer;
	}

	public static boolean arrayContains(int[] array, int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) {
				return true;
			}
		}

		return false;
	}
}
