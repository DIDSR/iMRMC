/**
 * InterpolatedLine.java
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

import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.HashMap;

/**
 * Given a set of XY coordinates, creates a set of linear equations describing
 * lines between said coordinates. Allows one to determine a linearly
 * interpolated point for a given x or y position.
 * 
 * @author Rohan Pathare
 */
public class InterpolatedLine {
	private HashMap<XYPair, double[]> allLineEqs;
	private TreeSet<XYPair> actualPoints;

	/**
	 * Sole constructor. Given set of XY coordinates, creates a set of linear
	 * equations describing lines between said coordinates
	 * 
	 * @param series Set of XY coordinates describing a curve
	 */
	public InterpolatedLine(TreeSet<XYPair> series) {
		TreeMap<Double, TreeSet<Double>> allDistinctXs = new TreeMap<Double, TreeSet<Double>>();
		for (XYPair point : series) {
			if (!allDistinctXs.containsKey(point.x)) {
				TreeSet<Double> ts = new TreeSet<Double>();
				ts.add(point.y);
				allDistinctXs.put(point.x, ts);
			} else {
				TreeSet<Double> ts = allDistinctXs.get(point.x);
				ts.add(point.y);
				allDistinctXs.put(point.x, ts);
			}
		}

		actualPoints = new TreeSet<XYPair>();

		// average together any points with the same x value
		for (Double x : allDistinctXs.keySet()) {
			double allY = 0;
			int denom = 0;
			for (Double y : allDistinctXs.get(x)) {
				allY += y;
				denom++;
			}
			actualPoints.add(new XYPair(x, (allY / (double) denom)));
		}

		allLineEqs = new HashMap<XYPair, double[]>();

		Iterator<XYPair> iter = actualPoints.iterator();
		XYPair prev = iter.next();
		while (iter.hasNext()) {
			XYPair curr = iter.next();
			double[] eq = new double[2];
			eq[0] = (prev.y - curr.y) / (prev.x - curr.x); // m
			eq[1] = prev.y - (prev.x * eq[0]);
			allLineEqs.put(curr, eq);
			prev = curr;
		}

	}

	/**
	 * Gets the Y coordinate for a given X along the set of interpolated lines
	 * 
	 * @param x The X position
	 * @return The Y position
	 */
	public double getYat(double x) {
		if (x > 1) {
			return 1;
		} else if (x <= 0) {
			return 0;
		} else {
			XYPair currCeiling = actualPoints.ceiling(new XYPair(x, 0));
			double m = allLineEqs.get(currCeiling)[0];
			double b = allLineEqs.get(currCeiling)[1];
			return (m * x) + b;
		}
	}

	/**
	 * Gets the X coordinate for given Y along the set of interpolated lines.
	 * 
	 * @param y The Y position
	 * @return The X position
	 */
	public double getXat(double y) {
		if (y > 1) {
			return 1;
		} else if (y <= 0) {
			return 0;
		} else {
			Iterator<XYPair> iter = actualPoints.iterator();
			XYPair currCeiling = null;
			XYPair prev = iter.next();
			while (iter.hasNext()) {
				XYPair curr = iter.next();
				if (y > prev.y && y < curr.y) {
					currCeiling = curr;
				}
				prev = curr;
			}
			if (currCeiling == null) {
				currCeiling = actualPoints.higher(actualPoints.first());
			}
			double m = allLineEqs.get(currCeiling)[0];
			double b = allLineEqs.get(currCeiling)[1];
			double returnX = (y - b) / m;
			if (returnX > 1) {
				return 1;
			}
			if (returnX < 0) {
				return 0;
			}
			return returnX;

		}
	}

	/**
	 * Gets the Y coordinate for a given X along the set of interpolated lines.
	 * When determining a diagonally averaged ROC curve, the points are roated
	 * such taht they lie along the x axis. Since they will be rotated back to
	 * the diagonal (x=y) direction, x values go up to sqrt(2) instead of 1.
	 * 
	 * @param x The X position
	 * @return The Y position
	 */
	public double getYatDiag(double x) {
		if (x > Math.sqrt(2)) {
			return 0;
		} else if (x <= 0) {
			return 0;
		} else {
			XYPair currCeiling = actualPoints.ceiling(new XYPair(x, 0));
			double m = allLineEqs.get(currCeiling)[0];
			double b = allLineEqs.get(currCeiling)[1];
			return (m * x) + b;
		}
	}
}
