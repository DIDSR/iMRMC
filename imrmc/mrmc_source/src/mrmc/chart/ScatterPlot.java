package mrmc.chart;

import java.awt.BorderLayout;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.TreeMap;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class ScatterPlot extends JFrame {

	private static final long serialVersionUID = 1L;
	private XYLineAndShapeRenderer renderer;

	public ScatterPlot(final String title, String xaxis, String yaxis,
			double[][][] data) {
		super(title);

		final JFreeChart chart = ChartFactory.createScatterPlot("\n", xaxis,
				yaxis, createDataset(data), PlotOrientation.VERTICAL, true,
				true, false);
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
		for (int i = 1; i <= data.length; i++) {
			JCheckBox aBox = new JCheckBox("" + i);
			aBox.setSelected(true);
			aBox.addItemListener(new readerSelectListner());
			chartPanel.add(aBox);
		}
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 500));

		setContentPane(chartPanel);

	}

	private static XYDataset createDataset(double[][][] data) {
		XYSeriesCollection result = new XYSeriesCollection();
		for (int r = 0; r < data.length; r++) {
			XYSeries series = new XYSeries("" + (r + 1));
			for (int i = 0; i < data[r].length; i++) {
				series.add(data[r][i][0], data[r][i][1]);
			}
			result.addSeries(series);
		}
		return result;
	}

	class readerSelectListner implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				renderer.setSeriesLinesVisible(
						(Integer.parseInt(((JCheckBox) e.getItem()).getText()) - 1),
						false);
				renderer.setSeriesShapesVisible(
						(Integer.parseInt(((JCheckBox) e.getItem()).getText()) - 1),
						false);
			} else if (e.getStateChange() == ItemEvent.SELECTED) {
				renderer.setSeriesLinesVisible(
						(Integer.parseInt(((JCheckBox) e.getItem()).getText()) - 1),
						true);
				renderer.setSeriesShapesVisible(
						(Integer.parseInt(((JCheckBox) e.getItem()).getText()) - 1),
						true);
			}
		}
	}
}
