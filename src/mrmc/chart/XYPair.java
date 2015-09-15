/**
 * XYPair.java
 * 
 * This software and documentation (the "Software") were developed at the Food and Drug Administration (FDA) 
 * by employees of the Federal Government in the course of their official duties. Pursuant to Title 17, Section 
 * 105 of the United States Code, this work is not subject to copyright protection and is in the public domain. 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of the Software, to deal in the 
 * Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, or sell copies of the Software or derivatives, and to permit persons to whom the 
 * Software is furnished to do so. FDA assumes no responsibility whatsoever for use by other parties of the 
 * Software, its source code, documentation or compiled executables, and makes no guarantees, expressed or 
 * implied, about its quality, reliability, or any other characteristic.   Further, use of this code in no way 
 * implies endorsement by the FDA or confers any advantage in regulatory decisions.  Although this software 
 * can be redistributed and/or modified freely, we ask that any derivative works bear some notice that they 
 * are derived from it, and any modified versions bear some notice that they have been modified.
 */

package mrmc.chart;

/**
 * Helper class to contain XY coordinate data within a single object.
 * Coordinates are comparable such that one coordinate is greater than another
 * if it's x value is greater than the other. If the x coordinates are equal,
 * comparison is determined by the y value.
 * 
 * @author Rohan Pathare
 */
public class XYPair implements Comparable<XYPair> {
	public double x;
	public double y;

	/**
	 * Sole constructor. Creates a new XYPair with the specified position
	 * 
	 * @param theX The X coordinate
	 * @param theY The Y coordinate
	 */
	public XYPair(double theX, double theY) {
		x = theX;
		y = theY;
	}

	/**
	 * Determines whether this point is in the same position as another point
	 * 
	 * @param other The other point
	 * @return true if the points are in the same position, false otherwise
	 */
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (other.getClass() != getClass()) {
			return false;
		}

		XYPair compared = (XYPair) other;
		return ((Double.compare(this.x, compared.x) == 0) && (Double.compare(
				this.y, compared.y) == 0));
	}

	/**
	 * Compares the position of this point to another point
	 * 
	 * @param other The point being compared
	 * @return 0 If the points are in the same position, -1 if this point is to
	 *         the left or directly below the other point, and 1 if this point
	 *         is to the right or directly above the other point
	 */
	@Override
	public int compareTo(XYPair other) {
		if (other == null) {
			NullPointerException e = new NullPointerException();
			throw e;
		}
		if (other == this) {
			return 0;
		}

		if (this.equals(other)) {
			return 0;
		}

		if (Double.compare(this.x, other.x) < 0) {
			return -1;
		} else if (Double.compare(this.x, other.x) == 0) {
			if (Double.compare(this.y, other.y) < 0) {
				return -1;
			} else {
				return 1;
			}
		} else {
			return 1;
		}
	}

	/**
	 * Returns a string representation of this point
	 * 
	 * @return String representation of this point
	 */
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
