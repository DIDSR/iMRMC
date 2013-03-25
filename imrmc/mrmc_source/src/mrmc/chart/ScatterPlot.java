package mrmc.chart;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.PopupMenu;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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

public class ScatterPlot extends JFrame {

	private static final long serialVersionUID = 1L;
	private XYLineAndShapeRenderer renderer;
	private XYSeriesCollection seriesCollection;

	private static final int POOLED = -1;
	private static final int VERTICAL = -2;
	private static final int HORIZONTAL = -3;
	private static final int DIAGONAL = -4;

	public ScatterPlot(final String title, String xaxis, String yaxis,
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
			aBox.addItemListener(new readerSelectListner());
			readerSelect.add(aBox);
		}

		JCheckBox pooled = new JCheckBox("Pooled Average");
		pooled.setSelected(true);
		pooled.addItemListener(new avgSelectListner());
		readerSelect.add(pooled);

		JCheckBox vert = new JCheckBox("Vertical Average");
		vert.setSelected(true);
		vert.addItemListener(new avgSelectListner());
		readerSelect.add(vert);

		JCheckBox horiz = new JCheckBox("Horizontal Average");
		horiz.setSelected(true);
		horiz.addItemListener(new avgSelectListner());
		readerSelect.add(horiz);

		JCheckBox diag = new JCheckBox("Diagonal Average");
		diag.setSelected(true);
		diag.addItemListener(new avgSelectListner());
		readerSelect.add(diag);

		chartPanel.setPreferredSize(new java.awt.Dimension(500, 500));

		this.add(chartPanel);
		this.add(readerSelect, BorderLayout.PAGE_END);

	}

	private XYDataset createDataset(double[][][] data) {
		seriesCollection = new XYSeriesCollection();

		for (int r = 0; r < data.length; r++) {
			XYSeries series = new XYSeries("" + (r + 1));
			for (int i = 0; i < data[r].length; i++) {
				series.add(data[r][i][0], data[r][i][1]);
			}
			seriesCollection.addSeries(series);
		}

		XYSeries vertAvg = new XYSeries("Vertical Average");
		seriesCollection.addSeries(vertAvg);
		XYSeries horizAvg = new XYSeries("Horizontal Average");
		seriesCollection.addSeries(horizAvg);
		XYSeries diagAvg = new XYSeries("Diagonal Average");
		seriesCollection.addSeries(diagAvg);
		XYSeries pooledAvg = new XYSeries("Pooled Average");
		seriesCollection.addSeries(pooledAvg);

		return seriesCollection;
	}

	public void addData(double[][] newData, String type) {
		for (int i = 0; i < newData.length; i++) {
			seriesCollection.getSeries(type).add(newData[i][0], newData[i][1]);
		}
	}

	class readerSelectListner implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				renderer.setSeriesLinesVisible((seriesCollection
						.getSeriesIndex(((JCheckBox) e.getItem()).getText())),
						false);
				renderer.setSeriesShapesVisible((seriesCollection
						.getSeriesIndex(((JCheckBox) e.getItem()).getText())),
						false);
			} else if (e.getStateChange() == ItemEvent.SELECTED) {
				renderer.setSeriesLinesVisible((seriesCollection
						.getSeriesIndex(((JCheckBox) e.getItem()).getText())),
						true);
				renderer.setSeriesShapesVisible((seriesCollection
						.getSeriesIndex(((JCheckBox) e.getItem()).getText())),
						true);
			}
		}
	}

	class avgSelectListner implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				switch (((JCheckBox) e.getItem()).getText()) {
				case "Pooled Average":
					renderer.setSeriesLinesVisible(
							seriesCollection.getSeriesIndex("Pooled Average"),
							false);
					renderer.setSeriesShapesVisible(
							seriesCollection.getSeriesIndex("Pooled Average"),
							false);
					break;
				case "Vertical Average":
					renderer.setSeriesLinesVisible(
							seriesCollection.getSeriesIndex("Vertical Average"),
							false);
					renderer.setSeriesShapesVisible(
							seriesCollection.getSeriesIndex("Vertical Average"),
							false);
					break;
				case "Horizontal Average":
					renderer.setSeriesLinesVisible(seriesCollection
							.getSeriesIndex("Horizontal Average"), false);
					renderer.setSeriesShapesVisible(seriesCollection
							.getSeriesIndex("Horizontal Average"), false);
					break;
				case "Diagonal Average":
					renderer.setSeriesLinesVisible(
							seriesCollection.getSeriesIndex("Diagonal Average"),
							false);
					renderer.setSeriesShapesVisible(
							seriesCollection.getSeriesIndex("Diagonal Average"),
							false);
					break;
				}
			} else if (e.getStateChange() == ItemEvent.SELECTED) {
				switch (((JCheckBox) e.getItem()).getText()) {
				case "Pooled Average":
					renderer.setSeriesLinesVisible(
							seriesCollection.getSeriesIndex("Pooled Average"),
							true);
					renderer.setSeriesShapesVisible(
							seriesCollection.getSeriesIndex("Pooled Average"),
							true);
					break;
				case "Vertical Average":
					renderer.setSeriesLinesVisible(
							seriesCollection.getSeriesIndex("Vertical Average"),
							true);
					renderer.setSeriesShapesVisible(
							seriesCollection.getSeriesIndex("Vertical Average"),
							true);
					break;
				case "Horizontal Average":
					renderer.setSeriesLinesVisible(seriesCollection
							.getSeriesIndex("Horizontal Average"), true);
					renderer.setSeriesShapesVisible(seriesCollection
							.getSeriesIndex("Horizontal Average"), true);
					break;
				case "Diagonal Average":
					renderer.setSeriesLinesVisible(
							seriesCollection.getSeriesIndex("Diagonal Average"),
							true);
					renderer.setSeriesShapesVisible(
							seriesCollection.getSeriesIndex("Diagonal Average"),
							true);
					break;
				}
			}
		}
	}
}
