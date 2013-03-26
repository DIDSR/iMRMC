package mrmc.chart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.HashMap;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

public class JointedLine {
	HashMap<Double, double[]> allLines;
	TreeSet<XYDataItem> actualPoints;

	public JointedLine(XYSeries series) {
		ArrayList<XYDataItem> allPoints = new ArrayList<XYDataItem>(
				series.getItems());
		actualPoints = new TreeSet<XYDataItem>();
		for (XYDataItem point : allPoints) {
			actualPoints.add(point);
		}
		allLines = new HashMap<Double, double[]>();

		Iterator<XYDataItem> iter = actualPoints.descendingIterator();
		XYDataItem prev = iter.next();
		while (iter.hasNext()) {
			XYDataItem curr = iter.next();
			double[] eq = new double[2];
			eq[0] = (prev.getYValue() - curr.getYValue())
					/ (prev.getXValue() - curr.getXValue()); // m
			eq[1] = prev.getYValue() - (prev.getXValue() * eq[0]); // b
			allLines.put(curr.getXValue(), eq);
			prev = curr;
		}

	}

	public double getYat(double x) {
		Double currFloor = actualPoints.floor(new XYDataItem(x, 0)).getXValue();
		double m = allLines.get(currFloor)[0];
		double b = allLines.get(currFloor)[1];
		return (m * x) + b;
	}
	
	public double getXat(double y){
		return y;
	}
}
