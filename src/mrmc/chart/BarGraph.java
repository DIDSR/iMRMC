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
