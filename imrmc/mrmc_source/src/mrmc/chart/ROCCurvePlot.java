/*
 * ROCCurvePlot.java
 * 
 * v1.0
 * 
 * @Author Xin He, Phd, Brandon D. Gallas, PhD, Rohan Pathare
 * 
 * Copyright 2013 Food & Drug Administration, Division of Image Analysis & Mathematics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Creates a chart displaying ROC curves for each reader of a particular modality.
 *     Reader curves are then averaged in direction of sensitivity (vertically), 
 *     specificity (horizontally), and sensitivity + specificity (diagonally). 
 *     Additionally all reader scores are placed in one set and a curve is determined, 
 *     creating a "pooled average"
 */

package mrmc.chart;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.PopupMenu;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ROCCurvePlot extends JFrame {

	private static final long serialVersionUID = 1L;
	private XYLineAndShapeRenderer renderer;
	private XYSeriesCollection seriesCollection;
	private ArrayList<InterpolatedLine> allLines;
	private ArrayList<String> readerSeriesTitles;
	private ArrayList<JCheckBox> readerSeriesBoxes;
	private JCheckBox vert;
	private JCheckBox horiz;
	private JCheckBox diag;
	private JCheckBox pooled;

	public ROCCurvePlot(final String title, String xaxis, String yaxis,
			TreeMap<Integer, TreeSet<XYPair>> data) {
		super(title);

		createDataset(data);
		final JFreeChart chart = ChartFactory.createScatterPlot(title, xaxis,
				yaxis, seriesCollection, PlotOrientation.VERTICAL, true, true,
				false);
		XYPlot xyplot = (XYPlot) chart.getPlot();
		xyplot.setDomainCrosshairVisible(true);
		xyplot.setRangeCrosshairVisible(true);
		NumberAxis domain = (NumberAxis) xyplot.getDomainAxis();
		domain.setRange(0.00, 1.00);
		domain.setTickUnit(new NumberTickUnit(0.1));
		NumberAxis range = (NumberAxis) xyplot.getRangeAxis();
		range.setRange(0.00, 1.00);
		range.setTickUnit(new NumberTickUnit(0.1));
		renderer = new XYLineAndShapeRenderer();
		chart.getXYPlot().setRenderer(renderer);
		ChartPanel chartPanel = new ChartPanel(chart);

		JPanel readerSelect = new JPanel(new WrapLayout());
		readerSeriesBoxes = new ArrayList<JCheckBox>();

		for (Integer r : data.keySet()) {
			JCheckBox aBox = new JCheckBox("" + r);
			aBox.setSelected(false);
			aBox.addItemListener(new SeriesSelectListner());
			hideSeries("" + r, true);
			readerSeriesBoxes.add(aBox);
			readerSelect.add(aBox);
		}

		renderer.setSeriesShapesVisible(
				seriesCollection.getSeriesIndex("Vertical Average"), false);
		renderer.setSeriesStroke(
				seriesCollection.getSeriesIndex("Vertical Average"),
				new java.awt.BasicStroke(3f));
		renderer.setSeriesShapesVisible(
				seriesCollection.getSeriesIndex("Horizontal Average"), false);
		renderer.setSeriesStroke(
				seriesCollection.getSeriesIndex("Horizontal Average"),
				new java.awt.BasicStroke(3f));
		renderer.setSeriesShapesVisible(
				seriesCollection.getSeriesIndex("Diagonal Average"), false);
		renderer.setSeriesStroke(
				seriesCollection.getSeriesIndex("Diagonal Average"),
				new java.awt.BasicStroke(3f));
		renderer.setSeriesStroke(
				seriesCollection.getSeriesIndex("Pooled Average"),
				new java.awt.BasicStroke(3f));

		vert = new JCheckBox("Vertical Average");
		vert.setSelected(true);
		vert.addItemListener(new SeriesSelectListner());
		readerSelect.add(vert);
		horiz = new JCheckBox("Horizontal Average");
		horiz.setSelected(true);
		horiz.addItemListener(new SeriesSelectListner());
		readerSelect.add(horiz);
		diag = new JCheckBox("Diagonal Average");
		diag.setSelected(true);
		diag.addItemListener(new SeriesSelectListner());
		readerSelect.add(diag);
		pooled = new JCheckBox("Pooled Average");
		pooled.setSelected(true);
		pooled.addItemListener(new SeriesSelectListner());
		readerSelect.add(pooled);

		JCheckBox allReaders = new JCheckBox("Show Readers");
		allReaders.setSelected(false);
		allReaders.addItemListener(new ReadersSelectListner());
		readerSelect.add(allReaders);
		JCheckBox allAverages = new JCheckBox("Show Averages");
		allAverages.setSelected(true);
		allAverages.addItemListener(new AverageSelectListner());
		readerSelect.add(allAverages);

		chartPanel.setPreferredSize(new java.awt.Dimension(700, 700));
		this.add(chartPanel);
		this.add(readerSelect, BorderLayout.PAGE_END);

	}

	private void createDataset(TreeMap<Integer, TreeSet<XYPair>> data) {
		seriesCollection = new XYSeriesCollection();
		readerSeriesTitles = new ArrayList<String>();

		for (Integer r : data.keySet()) {
			XYSeries series = new XYSeries("" + r, false);
			readerSeriesTitles.add("" + r);
			for (XYPair point : data.get(r)) {
				series.add(point.x, point.y);
			}
			seriesCollection.addSeries(series);
		}

		allLines = new ArrayList<InterpolatedLine>();
		for (Integer r : data.keySet()) {
			allLines.add(new InterpolatedLine(data.get(r)));
		}

		XYSeries vertAvg = generateVerticalROC();
		seriesCollection.addSeries(vertAvg);
		XYSeries horizAvg = generateHorizontalROC();
		seriesCollection.addSeries(horizAvg);
		XYSeries diagAvg = generateDiagonalROC(data);
		seriesCollection.addSeries(diagAvg);
		XYSeries pooledAvg = new XYSeries("Pooled Average", false);
		seriesCollection.addSeries(pooledAvg);

	}

	public void addData(TreeSet<XYPair> newData, String type) {
		for (XYPair point : newData) {
			seriesCollection.getSeries(type).add(point.x, point.y);
		}
	}

	// TODO verify that this generates correct curve
	private XYSeries generateDiagonalROC(TreeMap<Integer, TreeSet<XYPair>> data) {
		XYSeries diagAvg = new XYSeries("Diagonal Average", false);
		TreeMap<Integer, TreeSet<XYPair>> rotatedData = new TreeMap<Integer, TreeSet<XYPair>>();

		// rotate all points in data 45 degrees clockwise about origin
		for (Integer r : data.keySet()) {
			rotatedData.put(r, new TreeSet<XYPair>());
			for (XYPair point : data.get(r)) {
				double x2 = (point.x + point.y) / Math.sqrt(2.0);
				double y2 = (point.y - point.x) / Math.sqrt(2.0);
				rotatedData.get(r).add(new XYPair(x2, y2));
			}
		}

		// generate linear interpolation with new points
		ArrayList<InterpolatedLine> rotatedLines = new ArrayList<InterpolatedLine>();
		for (Integer r : rotatedData.keySet()) {
			rotatedLines.add(new InterpolatedLine(rotatedData.get(r)));
		}

		// take vertical sample averages from x = 0 to x = 1
		for (double i = 0; i <= Math.sqrt(2); i += 0.01) {
			double avg = 0;
			int counter = 0;
			for (InterpolatedLine line : rotatedLines) {
				avg += line.getYatDiag(i);
				counter++;
			}

			// rotate points back 45 degrees counterclockwise
			double x1 = i;
			double y1 = (avg / (double) counter);
			double x2 = (x1 * Math.cos(Math.toRadians(45)))
					- (y1 * Math.sin(Math.toRadians(45)));
			double y2 = (x1 * Math.sin(Math.toRadians(45)))
					+ (y1 * Math.cos(Math.toRadians(45)));
			diagAvg.add(x2, y2);
		}

		diagAvg.add(1, 1);
		return diagAvg;
	}

	private XYSeries generateHorizontalROC() {
		XYSeries horizAvg = new XYSeries("Horizontal Average", false);
		for (double i = 0; i <= 1.01; i += 0.01) {
			double avg = 0;
			int counter = 0;
			for (InterpolatedLine line : allLines) {
				avg += line.getXat(i);
				counter++;
			}
			horizAvg.add(avg / (double) counter, i);
		}
		return horizAvg;
	}

	private XYSeries generateVerticalROC() {
		XYSeries vertAvg = new XYSeries("Vertical Average", false);
		for (double i = 0; i <= 1.01; i += 0.01) {
			double avg = 0;
			int counter = 0;
			for (InterpolatedLine line : allLines) {
				avg += line.getYat(i);
				counter++;
			}
			vertAvg.add(i, avg / (double) counter);
		}
		return vertAvg;
	}

	private void hideSeries(String series, boolean shapes) {
		renderer.setSeriesLinesVisible(
				(seriesCollection.getSeriesIndex(series)), false);
		renderer.setSeriesShapesVisible(
				(seriesCollection.getSeriesIndex(series)), false);
		renderer.setSeriesVisibleInLegend(
				(seriesCollection.getSeriesIndex(series)), false);
	}

	private void showSeries(String series, boolean shapes) {
		renderer.setSeriesLinesVisible(
				(seriesCollection.getSeriesIndex(series)), true);
		if (shapes) {
			renderer.setSeriesShapesVisible(
					(seriesCollection.getSeriesIndex(series)), true);
		}
		renderer.setSeriesVisibleInLegend(
				(seriesCollection.getSeriesIndex(series)), true);
	}

	class ReadersSelectListner implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				for (JCheckBox readerBox : readerSeriesBoxes) {
					readerBox.setSelected(false);
				}
				for (String title : readerSeriesTitles) {
					hideSeries(title, true);
				}
			} else if (e.getStateChange() == ItemEvent.SELECTED) {
				for (JCheckBox readerBox : readerSeriesBoxes) {
					readerBox.setSelected(true);
				}
				for (String title : readerSeriesTitles) {
					showSeries(title, true);
				}
			}
		}
	}

	class AverageSelectListner implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				vert.setSelected(false);
				horiz.setSelected(false);
				diag.setSelected(false);
				pooled.setSelected(false);
				hideSeries("Vertical Average", false);
				hideSeries("Horizontal Average", false);
				hideSeries("Diagonal Average", false);
				hideSeries("Pooled Average", true);
			} else if (e.getStateChange() == ItemEvent.SELECTED) {
				vert.setSelected(true);
				horiz.setSelected(true);
				diag.setSelected(true);
				pooled.setSelected(true);
				showSeries("Vertical Average", false);
				showSeries("Horizontal Average", false);
				showSeries("Diagonal Average", false);
				showSeries("Pooled Average", true);
			}

		}
	}

	class SeriesSelectListner implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				if (((JCheckBox) e.getItem()).getText().equals(
						"Vertical Average")
						|| ((JCheckBox) e.getItem()).getText().equals(
								"Horizontal Average")
						|| ((JCheckBox) e.getItem()).getText().equals(
								"Diagonal Average")) {
					hideSeries(((JCheckBox) e.getItem()).getText(), false);
				} else {
					hideSeries(((JCheckBox) e.getItem()).getText(), true);
				}
			} else if (e.getStateChange() == ItemEvent.SELECTED) {
				if (((JCheckBox) e.getItem()).getText().equals(
						"Vertical Average")
						|| ((JCheckBox) e.getItem()).getText().equals(
								"Horizontal Average")
						|| ((JCheckBox) e.getItem()).getText().equals(
								"Diagonal Average")) {
					showSeries(((JCheckBox) e.getItem()).getText(), false);
				} else {
					showSeries(((JCheckBox) e.getItem()).getText(), true);
				}
			}
		}
	}
}
