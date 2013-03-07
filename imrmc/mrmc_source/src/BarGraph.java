import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class BarGraph extends JFrame {

	public BarGraph(final String title, HashMap<Integer, Integer> data) {
		super(title);
		CategoryDataset dataset = createDataset(data);
		JFreeChart chart = createChart(dataset, title);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}

	private CategoryDataset createDataset(HashMap<Integer, Integer> data) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Entry<Integer, Integer> e : data.entrySet()) {
			int key = e.getKey();
			int value = e.getValue();
			dataset.addValue(value, key + "", "Readers");
		}

		return dataset;
	}

	private JFreeChart createChart(final CategoryDataset dataset, String title) {

		final JFreeChart chart = ChartFactory.createBarChart("Bar Chart",
				"Category", "Value", dataset, PlotOrientation.VERTICAL, true,
				true, false);
		return chart;

	}
}
