/**
 * BarGraph.java
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
 */

package mrmc.chart;

import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Creates a chart displaying a bar graph for a particular dataset. Currently
 * used to display charts for readers per case and cases per reader.
 * 
 * @author Rohan Pathare.
 */
public class BarGraph extends JFrame {

	private static final long serialVersionUID = 1L;

	/**
	 * Sole constructor. Creates a bar-graph style chart
	 * 
	 * @param title Title of chart
	 * @param xaxis Label for x-axis
	 * @param yaxis Label for y-axis
	 * @param treeMap Mapping of x-y data
	 */
	public BarGraph(final String title, String xaxis, String yaxis,
			TreeMap<String, Double> treeMap) {
		super(title);
		CategoryDataset dataset = createDataset(treeMap, xaxis);
		JFreeChart chart = createChart(dataset, title, xaxis, yaxis);
		LegendTitle legend = chart.getLegend();
		legend.setVisible(false);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}

	/**
	 * Converts data mapping into format for used by chart
	 * 
	 * @param treeMap Mapping of x-y data
	 * @param xaxis Label for x-axis
	 * @return Chart data in CategoryDataset format
	 */
	private CategoryDataset createDataset(TreeMap<String, Double> treeMap,
			String xaxis) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Entry<String, Double> e : treeMap.entrySet()) {
			String key = e.getKey();
			double value = e.getValue();
			dataset.addValue(value, key + "", xaxis);
		}

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
	private JFreeChart createChart(final CategoryDataset dataset, String title,
			String xaxis, String yaxis) {

		final JFreeChart chart = ChartFactory.createBarChart(title, xaxis,
				yaxis, dataset, PlotOrientation.VERTICAL, true, true, false);
		return chart;

	}
}
