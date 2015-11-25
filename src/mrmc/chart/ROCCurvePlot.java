/**
 * ROCCurvePlot.java
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Creates a chart displaying ROC curves for each reader of a particular
 * modality. Reader curves are then averaged in direction of sensitivity
 * (vertically), specificity (horizontally), and sensitivity + specificity
 * (diagonally). Additionally all reader scores are placed in one set and a
 * curve is determined, creating a "pooled average"
 * 
 * @author Rohan Pathare
 */
public class ROCCurvePlot extends JFrame {

	private static final long serialVersionUID = 1L;
	private XYLineAndShapeRenderer renderer;
	private XYSeriesCollection seriesCollection;          //ROC curve data
	private ArrayList<InterpolatedLine> allLines;
	private ArrayList<String> readerSeriesTitles;         //readers ID
	private ArrayList<JCheckBox> readerSeriesBoxes;
	private JCheckBox vert;
	private JCheckBox horiz;
	private JCheckBox diag;
	private JCheckBox pooled;
	private String inputfilepath ;
	private Set<String> modalityID;

	/**
	 * Sole constructor. Creates a line plot display ROC curves
	 * 
	 * @param title Title of the chart
	 * @param xaxis x-axis label
	 * @param yaxis y-axis label
	 * @param treeMap Mapping of readers to a set of points defining an ROC curve
	 */
	public ROCCurvePlot(final String title, String xaxis, String yaxis,
			TreeMap<String, TreeMap<String, TreeSet<XYPair>>> fulltreeMap,String filepath) {
		super(title);
		inputfilepath = filepath;
	    String inputfiletitle = inputfilepath.substring(inputfilepath.lastIndexOf("\\") + 1);
		TextTitle subtitle = new TextTitle(inputfiletitle);
		seriesCollection = new XYSeriesCollection();
		readerSeriesTitles = new ArrayList<String>();       
		modalityID =fulltreeMap.keySet();
		List<String> readerarray = new ArrayList<String>();
		for (String mod : modalityID){
		    TreeMap<String, TreeSet<XYPair>> treeMap = fulltreeMap.get(mod);
			createDataset(mod,treeMap);     										//extract data from fulltreeMap and save them into seriesCollection and readerSeriesTitles
			for (String r : treeMap.keySet()) {
				if(!readerarray.contains(r)){
				readerarray.add(r);
				}
			}
		}
		Collections.sort(readerarray.subList(0, readerarray.size()));
		final JFreeChart chart = ChartFactory.createScatterPlot(title, xaxis,
				yaxis, seriesCollection, PlotOrientation.VERTICAL, true, true,
				false);
		chart.addSubtitle(subtitle);
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

		for (String r : readerarray) {
			JCheckBox aBox = new JCheckBox("" + r);
			aBox.setSelected(false);
			aBox.addItemListener(new SeriesSelectListener());
			hideSeries(""+r);
			readerSeriesBoxes.add(aBox);
			readerSelect.add(aBox);
			}
		hideSeries("Vertical Average");
		hideSeries("Horizontal Average");
		hideSeries("Pooled Average");
		hideSeries("Diagonal Average");
		showSeries("Diagonal Average",false);
		for (String mod : modalityID){
			renderer.setSeriesStroke(
					seriesCollection.getSeriesIndex(mod+": Vertical Average"),
					new java.awt.BasicStroke(3f));
			renderer.setSeriesStroke(
					seriesCollection.getSeriesIndex(mod+": Horizontal Average"),
					new java.awt.BasicStroke(3f));
			renderer.setSeriesStroke(
					seriesCollection.getSeriesIndex(mod+": Diagonal Average"),
					new java.awt.BasicStroke(3f));
			renderer.setSeriesStroke(
					seriesCollection.getSeriesIndex(mod+ ": Pooled Average"),
					new java.awt.BasicStroke(3f));
		}

		vert = new JCheckBox("Vertical Average");
		vert.setSelected(false);
		vert.addItemListener(new SeriesSelectListener());
		readerSelect.add(vert);
		horiz = new JCheckBox("Horizontal Average");
		horiz.setSelected(false);
		horiz.addItemListener(new SeriesSelectListener());
		readerSelect.add(horiz);
		diag = new JCheckBox("Diagonal Average");
		diag.setSelected(true);
		diag.addItemListener(new SeriesSelectListener());
		readerSelect.add(diag);
		pooled = new JCheckBox("Pooled Average");
		pooled.setSelected(false);
		pooled.addItemListener(new SeriesSelectListener());
		readerSelect.add(pooled);

		JCheckBox allReaders = new JCheckBox("Show Readers");
		allReaders.setSelected(false);
		allReaders.addItemListener(new ReadersSelectListener());
		readerSelect.add(allReaders);
		JCheckBox allAverages = new JCheckBox("Show Averages");
		allAverages.setSelected(false);
		allAverages.addItemListener(new AverageSelectListener());
		readerSelect.add(allAverages);
         
		JButton exportresult = new JButton("Export");
		exportresult.addActionListener(new exportROCresult());
		readerSelect.add(exportresult);

		chartPanel.setPreferredSize(new java.awt.Dimension(700, 700));
		this.add(chartPanel);
		this.add(readerSelect, BorderLayout.PAGE_END);

	}

	/**
	 * Converts the mapping of readers to curve points into a collection of
	 * separate XY data.
	 * 
	 * @param treeMap Mapping of readers to points defining a curve
	 */
	private void createDataset(String mod, TreeMap<String, TreeSet<XYPair>> treeMap) {
		for (String r : treeMap.keySet()) {
			XYSeries series = new XYSeries(mod +": "+ r, false);
			readerSeriesTitles.add("" + r);
			for (XYPair point : treeMap.get(r)) {
				series.add(point.x, point.y);
			}
			seriesCollection.addSeries(series);
		}

		allLines = new ArrayList<InterpolatedLine>();
		for (String r : treeMap.keySet()) {

			allLines.add(new InterpolatedLine(treeMap.get(r)));
		}
		XYSeries vertAvg = generateVerticalROC(mod);
		seriesCollection.addSeries(vertAvg);
		XYSeries horizAvg = generateHorizontalROC(mod);
		seriesCollection.addSeries(horizAvg);
		XYSeries diagAvg = generateDiagonalROC(mod, treeMap);
		seriesCollection.addSeries(diagAvg);
		XYSeries pooledAvg = new XYSeries(mod + ": Pooled Average", false);
		seriesCollection.addSeries(pooledAvg);

	}

	/**
	 * Adds a set of XY points to the collection of ROC curves
	 * 
	 * @param newData Set of XY coordinates
	 * @param type Name for this set of points
	 */
	public void addData(TreeMap<String, TreeSet<XYPair>> fullnewData, String type) {
		for (String mod : modalityID){
			TreeSet<XYPair> newData = fullnewData.get(mod);
			for (XYPair point : newData) {
				seriesCollection.getSeries(mod +": " + type).add(point.x, point.y);
			}
		}
	}

	/**
	 * Creates an ROC curve that averages together the scores for all readers in
	 * the diagonal direction
	 * 
	 * @param treeMap Mapping of readers to points defining a curve
	 * @return Series containing the ROC curve points
	 */
	private XYSeries generateDiagonalROC(String mod, TreeMap<String, TreeSet<XYPair>> treeMap) {
		XYSeries diagAvg = new XYSeries(mod + ": Diagonal Average", false);
		TreeMap<String, TreeSet<XYPair>> rotatedData = new TreeMap<String, TreeSet<XYPair>>();

		// rotate all points in data 45 degrees clockwise about origin
		for (String r : treeMap.keySet()) {
			rotatedData.put(r, new TreeSet<XYPair>());
			for (XYPair point : treeMap.get(r)) {
				double x2 = (point.x + point.y) / Math.sqrt(2.0);
				double y2 = (point.y - point.x) / Math.sqrt(2.0);
				rotatedData.get(r).add(new XYPair(x2, y2));
			}
		}

		// generate linear interpolation with new points
		ArrayList<InterpolatedLine> rotatedLines = new ArrayList<InterpolatedLine>();
		for (String r : rotatedData.keySet()) {
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

	/**
	 * Creates an ROC curve that averages together the scores for all readers in
	 * the horizontal direction
	 * 
	 * @return Series containing the ROC curve points
	 */
	private XYSeries generateHorizontalROC(String mod) {
		XYSeries horizAvg = new XYSeries(mod+": Horizontal Average", false);
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

	/**
	 * Creates an ROC curve that averages together the scores for all readers in
	 * the vertical direction
	 * 
	 * @return Series containing the ROC curve points
	 */
	private XYSeries generateVerticalROC(String mod) {
		XYSeries vertAvg = new XYSeries(mod + ": Vertical Average", false);
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

	/**
	 * Hides the specified series from the chart
	 * 
	 * @param series Which series to hide
	 */
	private void hideSeries(String series) {
		for (String mod : modalityID){
			if (seriesCollection.getSeriesIndex(mod+": "+series)!=-1){
				renderer.setSeriesLinesVisible(
						(seriesCollection.getSeriesIndex(mod+": "+series)), false);
				renderer.setSeriesShapesVisible(
						(seriesCollection.getSeriesIndex(mod+": "+series)), false);
				renderer.setSeriesVisibleInLegend(
						(seriesCollection.getSeriesIndex(mod+": "+series)), false);
			}
		}
	}

	/**
	 * Displays the specified series on the chart
	 * 
	 * @param series Which series to display
	 * @param shapes Whether or not to display shapes indicating individual data
	 *            points
	 */
	private void showSeries(String series, boolean shapes) {
		for(String mod : modalityID){
			if (seriesCollection.getSeriesIndex(mod+": "+series)!=-1){
				renderer.setSeriesLinesVisible(
						(seriesCollection.getSeriesIndex(mod+": "+series)), true);
				if (shapes) {
					renderer.setSeriesShapesVisible(
							(seriesCollection.getSeriesIndex(mod+": "+series)), true);
				}
				renderer.setSeriesVisibleInLegend(
						(seriesCollection.getSeriesIndex(mod+": "+series)), true);
			}
		}
	}

	/**
	 * Handler for checkbox to show or hide all readers from chart
	 */
	class ReadersSelectListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				for (JCheckBox readerBox : readerSeriesBoxes) {
					readerBox.setSelected(false);
				}
				for (String title : readerSeriesTitles) {
					hideSeries(title);
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

	/**
	 * Handler for checkbox to show or hide all averages from chart
	 */
	class AverageSelectListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				vert.setSelected(false);
				horiz.setSelected(false);
				diag.setSelected(false);
				pooled.setSelected(false);
				hideSeries("Vertical Average");
				hideSeries("Horizontal Average");
				hideSeries("Diagonal Average");
				hideSeries("Pooled Average");
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

	/**
	 * Handler for checkboxes to show/hide a specific series
	 */
	class SeriesSelectListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				if (((JCheckBox) e.getItem()).getText().equals(
						"Vertical Average")
						|| ((JCheckBox) e.getItem()).getText().equals(
								"Horizontal Average")
						|| ((JCheckBox) e.getItem()).getText().equals(
								"Diagonal Average")) {
					hideSeries(((JCheckBox) e.getItem()).getText());
				} else {
					hideSeries(((JCheckBox) e.getItem()).getText());
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
	/**
	 * Button for export ROC data to file
	 */
	class exportROCresult implements ActionListener{
		public void actionPerformed(ActionEvent e) {
            DateFormat dateForm = new SimpleDateFormat("yyyyMMddHHmm");
			Date currDate = new Date();
			final String fileTime = dateForm.format(currDate);
			String FileName=inputfilepath;
			FileName= FileName.substring(0,FileName.lastIndexOf("."));
            String sFileName = FileName+"ROCcurve"+fileTime+".csv";
            String sFileNameonly = sFileName.substring(FileName.lastIndexOf("\\")+1);
			try {
				 //generate whatever data you want	    		
				FileWriter writer = new FileWriter(sFileName);	   		
	            writer.append("ModalityID:ReaderID");
	            writer.append(',');
	            writer.append("Number of points");
	            writer.append(',');
	            writer.append("Axises");
	            writer.append('\n');
	            for (int j=0;j<seriesCollection.getSeriesCount();j++){
		            String serisekey =(String) seriesCollection.getSeriesKey(j); 
		            XYSeries seriesget = seriesCollection.getSeries(serisekey);             
		    	    writer.append(serisekey);
		    	    writer.append(','); 
		    	    writer.append(Integer.toString(seriesget.getItemCount()));
		    	    writer.append(','); 
		    	    writer.append("FPF"); 
		    	    writer.append(','); 
		    	    for (int i=0; i<seriesget.getItemCount(); i++){
		    	    	String tempx=String.valueOf(seriesget.getX(i));
		    	    writer.append(tempx);
		    	    writer.append(',');	
		    	    }
		    	    writer.append('\n');
		    	    writer.append(',');
		    	    writer.append(',');
		    	    writer.append("TPF"); 
		    	    writer.append(','); 
		    	    for (int i=0; i<seriesget.getItemCount(); i++){
		    	    	String tempx=String.valueOf(seriesget.getY(i));
		    	    writer.append(tempx);
		    	    writer.append(',');	
		    	    }
		    	    writer.append('\n');
	            } 			
	    	    writer.flush();
	    	    writer.close();
	    	    JFrame frame = new JFrame();
				JOptionPane.showMessageDialog(
						frame,"ROC data has been succeed export to input file directory!"+"\n"+"Filename="+sFileNameonly, 
						"Exported", JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}
}
