package mrmc.chart;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.PopupMenu;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

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
	private ArrayList<JointedLine> allLines;

	public ROCCurvePlot(final String title, String xaxis, String yaxis,
			double[][][] data) {
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

		JPanel readerSelect = new JPanel(new FlowLayout());
		for (int i = 1; i <= data.length; i++) {
			JCheckBox aBox = new JCheckBox("" + i);
			aBox.setSelected(true);
			aBox.addItemListener(new SeriesSelectListner());
			readerSelect.add(aBox);
		}

		JCheckBox vert = new JCheckBox("Vertical Average");
		vert.setSelected(true);
		vert.addItemListener(new SeriesSelectListner());
		readerSelect.add(vert);

		JCheckBox horiz = new JCheckBox("Horizontal Average");
		horiz.setSelected(true);
		horiz.addItemListener(new SeriesSelectListner());
		readerSelect.add(horiz);

		JCheckBox diag = new JCheckBox("Diagonal Average");
		diag.setSelected(true);
		diag.addItemListener(new SeriesSelectListner());
		readerSelect.add(diag);

		JCheckBox pooled = new JCheckBox("Pooled Average");
		pooled.setSelected(true);
		pooled.addItemListener(new SeriesSelectListner());
		readerSelect.add(pooled);

		chartPanel.setPreferredSize(new java.awt.Dimension(500, 500));

		this.add(chartPanel);
		this.add(readerSelect, BorderLayout.PAGE_END);

	}

	private void createDataset(double[][][] data) {
		seriesCollection = new XYSeriesCollection();

		for (int r = 0; r < data.length; r++) {
			XYSeries series = new XYSeries("" + (r + 1));
			for (int i = 0; i < data[r].length; i++) {
				series.add(data[r][i][0], data[r][i][1]);
			}
			seriesCollection.addSeries(series);
		}

		ArrayList<XYSeries> allSeries = new ArrayList<XYSeries>(
				seriesCollection.getSeries());
		allLines = new ArrayList<JointedLine>();
		for (XYSeries series : allSeries) {
			allLines.add(new JointedLine(series));
		}

		XYSeries vertAvg = generateVerticalROC();
		seriesCollection.addSeries(vertAvg);
		XYSeries horizAvg = generateHorizontalROC();
		seriesCollection.addSeries(horizAvg);
		XYSeries diagAvg = new XYSeries("Diagonal Average");
		seriesCollection.addSeries(diagAvg);
		XYSeries pooledAvg = new XYSeries("Pooled Average");
		seriesCollection.addSeries(pooledAvg);
	}

	public void addData(double[][] newData, String type) {
		for (int i = 0; i < newData.length; i++) {
			seriesCollection.getSeries(type).add(newData[i][0], newData[i][1]);
		}
	}

	private XYSeries generateHorizontalROC() {
		XYSeries horizAvg = new XYSeries("Horizontal Average");
		for (double i = 0; i <= 1; i += 0.01) {
			double avg = 0;
			int counter = 0;
			for (JointedLine line : allLines) {
				avg += line.getXat(i);
				counter++;
			}
			horizAvg.add(avg / counter, i);
		}
		horizAvg.add(0, 0);
		return horizAvg;
	}

	private XYSeries generateVerticalROC() {
		XYSeries vertAvg = new XYSeries("Vertical Average");
		for (double i = 0; i <= 1; i += 0.01) {
			double avg = 0;
			for (JointedLine line : allLines) {
				avg += line.getYat(i);
			}
			vertAvg.add(i, avg / allLines.size());
		}
		vertAvg.add(1, 1);
		return vertAvg;
	}

	class SeriesSelectListner implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				renderer.setSeriesLinesVisible((seriesCollection
						.getSeriesIndex(((JCheckBox) e.getItem()).getText())),
						false);
				renderer.setSeriesShapesVisible((seriesCollection
						.getSeriesIndex(((JCheckBox) e.getItem()).getText())),
						false);
				renderer.setSeriesVisibleInLegend((seriesCollection
						.getSeriesIndex(((JCheckBox) e.getItem()).getText())),
						false);
			} else if (e.getStateChange() == ItemEvent.SELECTED) {
				renderer.setSeriesLinesVisible((seriesCollection
						.getSeriesIndex(((JCheckBox) e.getItem()).getText())),
						true);
				renderer.setSeriesShapesVisible((seriesCollection
						.getSeriesIndex(((JCheckBox) e.getItem()).getText())),
						true);
				renderer.setSeriesVisibleInLegend((seriesCollection
						.getSeriesIndex(((JCheckBox) e.getItem()).getText())),
						true);
			}
		}
	}
}
