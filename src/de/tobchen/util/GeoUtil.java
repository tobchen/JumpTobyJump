package de.tobchen.util;

public class GeoUtil {
	public static boolean doRectsOverlap(float x0, float y0, float w0,
			float h0, float x1, float y1, float w1, float h1) {
		if (x0 >= (x1 + w1) || (x0 + w0) <= x1)
			return false;
		if (y0 >= (y1 + h1) || (y0 + h0) <= y1)
			return false;
		return true;
	}

	public static boolean isPointInRect(float x0, float y0, float x1, float y1,
			float w1, float h1) {
		if (x0 < x1 || (x1 + w1) < x0)
			return false;
		if (y0 < y1 || (y1 + h1) < y0)
			return false;
		return true;
	}

	public static boolean doesLineIntersectRectangle(double rectangleMinX,
			double rectangleMinY, double rectangleMaxX, double rectangleMaxY,
			double p1x, double p1y, double p2x, double p2y) {
		// Find min and max X for the segment
		double minX = p1x;
		double maxX = p2x;
		if (p1x > p2x) {
			minX = p2x;
			maxX = p1x;
		}

		// Find the intersection of the segment's and rectangle's x-projections
		if (maxX > rectangleMaxX) {
			maxX = rectangleMaxX;
		}
		if (minX < rectangleMinX) {
			minX = rectangleMinX;
		}

		// If their projections do not intersect return false
		if (minX > maxX) {
			return false;
		}

		// Find corresponding min and max Y for min and max X we found before
		double minY = p1y;
		double maxY = p2y;

		double dx = p2x - p1x;

		if (Math.abs(dx) > 0.0000001) {
			double a = (p2y - p1y) / dx;
			double b = p1y - a * p1x;
			minY = a * minX + b;
			maxY = a * maxX + b;
		}

		if (minY > maxY) {
			double tmp = maxY;
			maxY = minY;
			minY = tmp;
		}

		// Find the intersection of the segment's and rectangle's y-projections
		if (maxY > rectangleMaxY) {
			maxY = rectangleMaxY;
		}
		if (minY < rectangleMinY) {
			minY = rectangleMinY;
		}

		// If Y-projections do not intersect return false
		if (minY > maxY) {
			return false;
		}

		return true;
	}
}
