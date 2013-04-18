/*
 * StudyDesignPlot.java
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
 *     Creates chart displaying presence of score data for a particular case/reader 
 *     combination at a given modality
 */

package mrmc.chart;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Rectangle;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

public class StudyDesignPlot extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StudyDesignPlot(final String title, String xaxis, String yaxis,
			boolean[][] data) {
		super(title);
		DefaultXYDataset dataset = createDataset(data);
		JFreeChart chart = createChart(dataset, title, xaxis, yaxis);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}

	private DefaultXYDataset createDataset(boolean[][] data) {
		int t = 0;
		int f = 0;
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				if (data[i][j]) {
					t++;
				} else {
					f++;
				}
			}
		}
		double[][] trueVals = new double[2][t];
		double[][] falseVals = new double[2][f];
		final DefaultXYDataset dataset = new DefaultXYDataset();
		int tCount = 0;
		int fCount = 0;
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				if (data[i][j]) {
					trueVals[0][tCount] = j;
					trueVals[1][tCount] = i + 1;
					tCount++;
				} else {
					falseVals[0][fCount] = j;
					falseVals[1][fCount] = i + 1;
					fCount++;
				}
			}
		}
		dataset.addSeries("Missing", falseVals);
		dataset.addSeries("Present", trueVals);
		return dataset;
	}

	private JFreeChart createChart(final DefaultXYDataset dataset,
			String title, String xaxis, String yaxis) {
		final JFreeChart chart = ChartFactory.createScatterPlot(title, xaxis,
				yaxis, dataset, PlotOrientation.VERTICAL, true, true, false);
		XYPlot xyplot = (XYPlot) chart.getPlot();
		NumberAxis range = (NumberAxis) xyplot.getRangeAxis();
		range.setTickUnit(new NumberTickUnit(1));
		XYItemRenderer renderer = xyplot.getRenderer();
		Rectangle square = new Rectangle(5,5);
		renderer.setSeriesShape(0, square);
		renderer.setSeriesShape(1, square);
		renderer.setSeriesPaint(0, Color.white);
		renderer.setSeriesPaint(1,Color.black);
		return chart;

	}
}
