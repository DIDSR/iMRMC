/**
 * StudyDesignPlot.java
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

import javax.swing.JFrame;

import java.awt.Color;
import java.awt.Rectangle;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 * Creates chart displaying presence of score data for a particular case/reader
 * combination at a given modality
 * 
 * @author Rohan Pathare
 */
public class StudyDesignPlot extends JFrame {

	private static final long serialVersionUID = 1L;

	/**
	 * Sole constructor. Creates a plot to show study design
	 * 
	 * @param title Title of chart
	 * @param xaxis Label for x-axis
	 * @param yaxis Label for y-axis
	 * @param data Mapping of x-y data
	 */
	public StudyDesignPlot(final String title, String xaxis, String yaxis,
			String[][] data) {
		super(title);
		String[] label = new String[data.length];
		XYDataset dataset = createDataset(data);
		for (int i = 0; i < data.length; i++){
			label[i]=data[i][0];
		}
		JFreeChart chart = createChart(dataset, title, xaxis, yaxis,label);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}

	/**
	 * Converts data mapping into format for used by chart
	 * 
	 * @param data Mapping of x-y data
	 * @return Chart data in XYDataset format
	 */
	private XYDataset createDataset(String[][] data) {

		int nBlack = 0;
		int nWhite = 0;
		// Find how many points are black and white
		for (int i = 0; i < data.length; i++) {
			for (int j = 1; j < data[i].length; j++) {
				if (data[i][j]=="true") {
					nBlack++;
				} else {
					nWhite++;
				}
			}
		}

		double[][] trueVals = new double[2][nBlack];
		double[][] falseVals = new double[2][nWhite];
		final DefaultXYDataset dataset = new DefaultXYDataset();

		int tCount = 0;
		int fCount = 0;
		for (int i = 0; i < data.length; i++) {
			for (int j = 1; j < data[i].length; j++) {
				if (data[i][j]=="true") {
					trueVals[0][tCount] = j;     // x-axis
					trueVals[1][tCount] = i; // y-axis
					tCount++;
				} else {
					falseVals[0][fCount] = j;     // x-axis
					falseVals[1][fCount] = i; // y-axis
					fCount++;
				}
			}
		}
		dataset.addSeries("Missing", falseVals);
		dataset.addSeries("Present", trueVals);
		
		return dataset;
	}

	/**
	 * Constructs the chart
	 * 
	 * @param dataset Representation of x-y data
	 * @param title Title of chart
	 * @param xaxis Label for x-axis
	 * @param yaxis Label for y-axis
	 * @return The chart
	 */
	private JFreeChart createChart(final XYDataset dataset,
			String title, String xaxis, String yaxis,String[] label) {
		final JFreeChart chart = ChartFactory.createScatterPlot(title, xaxis,
				yaxis, dataset, PlotOrientation.VERTICAL, true, true, false);
		XYPlot xyplot = (XYPlot) chart.getPlot();


       
	    SymbolAxis rangeAxis1 = new SymbolAxis(yaxis, label);
		rangeAxis1.setTickUnit(new NumberTickUnit(1));
		rangeAxis1.setRange(-0.5,label.length-0.5);
	    xyplot.setRangeAxis(rangeAxis1);

		
		XYItemRenderer renderer = xyplot.getRenderer();
		Rectangle square = new Rectangle(5, 5);
		renderer.setSeriesShape(0, square);
		renderer.setSeriesShape(1, square);
		renderer.setSeriesPaint(0, Color.white);
		renderer.setSeriesPaint(1, Color.black);
		return chart;

	}
}
