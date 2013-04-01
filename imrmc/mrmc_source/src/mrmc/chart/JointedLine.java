package mrmc.chart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.HashMap;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

public class JointedLine {
	HashMap<XYPair, double[]> allLineEqs;
	TreeSet<XYPair> actualPoints;

	public JointedLine(TreeSet<XYPair> series) {
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

	public double getYatDiag(double x){
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
			if (returnX < 0){
				return 0;
			}
			return returnX;

		}
	}
}
