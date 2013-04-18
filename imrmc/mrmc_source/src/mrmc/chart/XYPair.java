/*
 * XYPair.java
 * 
 * v2.0b
 * 
 * @Author Brandon D. Gallas, PhD, Rohan Pathare
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
 *     
 *     Helper class to contain XY coordinate data within a single object. Coordinates are
 *     comparable such that one coordinate is greater than another if it's x value is greater 
 *     than the other. If the x coordinates are equal, comparison is determined by the y value. 
 */

package mrmc.chart;

public class XYPair implements Comparable<XYPair> {
	public double x;
	public double y;

	public XYPair(double theX, double theY) {
		x = theX;
		y = theY;
	}

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

	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
