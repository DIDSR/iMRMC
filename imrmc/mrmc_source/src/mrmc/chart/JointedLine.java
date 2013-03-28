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
	HashMap<Double, double[]> allLineEqs;
	TreeSet<XYDataItem> actualPoints;
	TreeSet<XYDataItem> reversedPoints;

	public JointedLine(XYSeries series) {
		ArrayList<XYDataItem> allPoints = new ArrayList<XYDataItem>(
				series.getItems());
		// TODO: this treeSet is removing items with same x values (i.e.
		// vertical ROC), change structure, or use better pair than
		// XYDataItem (which only sorts on x)

		TreeMap<Double, TreeSet<Double>> pointsOnX = new TreeMap<Double, TreeSet<Double>>();
		for (XYDataItem point : allPoints) {
			if (!pointsOnX.containsKey(point.getXValue())) {
				TreeSet<Double> ts = new TreeSet<Double>();
				ts.add(point.getYValue());
				pointsOnX.put(point.getXValue(), ts);
			} else {
				TreeSet<Double> ts = pointsOnX.get(point.getXValue());
				ts.add(point.getYValue());
				pointsOnX.put(point.getXValue(), ts);
			}
		}

		actualPoints = new TreeSet<XYDataItem>();

		for (Double x : pointsOnX.keySet()) {
			double avgY = 0;
			int denom = 0;
			for (Double y : pointsOnX.get(x)) {
				avgY += y;
				denom++;
			}
			actualPoints.add(new XYDataItem(x.doubleValue(), avgY
					/ (double) denom));
		}

		allLineEqs = new HashMap<Double, double[]>();

		Iterator<XYDataItem> iter = actualPoints.descendingIterator();
		XYDataItem prev = iter.next();
		while (iter.hasNext()) {
			XYDataItem curr = iter.next();
			double[] eq = new double[2];
			eq[0] = (prev.getYValue() - curr.getYValue())
					/ (prev.getXValue() - curr.getXValue()); // m
			eq[1] = prev.getYValue() - (prev.getXValue() * eq[0]); // b
			allLineEqs.put(curr.getXValue(), eq);
			prev = curr;
		}

	}

	public double getYat(double x) {
		if (x > 1) {
			return 1;
		} else if (x < 0) {
			return 0;
		} else {
			Double currFloor = actualPoints.floor(new XYDataItem(x, 0))
					.getXValue();
			double m = allLineEqs.get(currFloor)[0];
			double b = allLineEqs.get(currFloor)[1];
			return (m * x) + b;
		}
	}

	public double getXat(double y) {
		Double currFloor = reversedPoints.floor(new XYDataItem(y, 0))
				.getYValue();
		double m;
		double b;
		if (allLineEqs.get(currFloor) != null) {
			m = allLineEqs.get(currFloor)[0];
			b = allLineEqs.get(currFloor)[1];
		} else {
			// TODO this method may not be correct (finding last equation and
			// continuing it)
			m = allLineEqs.get(reversedPoints.first().getYValue())[0];
			b = allLineEqs.get(reversedPoints.first().getYValue())[1];
		}
		System.out.println(y);
		return (y - b) / m;
	}
}
