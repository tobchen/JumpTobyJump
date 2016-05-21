package de.tobchen.util;

public class MathUtil {
	public static double getDistance(float x1, float y1, float x2, float y2) {
		float x = x1 - x2;
		float y = y1 - y2;
		return Math.sqrt((x * x) + (y * y));
	}
}
