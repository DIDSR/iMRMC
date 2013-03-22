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

public class BarGraph extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BarGraph(final String title, String xaxis, String yaxis,
			TreeMap<Integer, Double> data) {
		super(title);
		CategoryDataset dataset = createDataset(data, xaxis);
		JFreeChart chart = createChart(dataset, title, yaxis, xaxis);
		LegendTitle legend = chart.getLegend();
		legend.setVisible(false);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}

	private CategoryDataset createDataset(TreeMap<Integer, Double> data,
			String xaxis) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Entry<Integer, Double> e : data.entrySet()) {
			int key = e.getKey();
			double value = e.getValue();
			dataset.addValue(value, key + "", xaxis);
		}

		return dataset;
	}

	private JFreeChart createChart(final CategoryDataset dataset, String title,
			String yaxis, String xaxis) {

		final JFreeChart chart = ChartFactory.createBarChart(title, xaxis,
				yaxis, dataset, PlotOrientation.VERTICAL, true, true, false);
		return chart;

	}
}
