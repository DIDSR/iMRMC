package mrmc.chart;

import java.util.TreeMap;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class ScatterPlot extends JFrame {
	
	private static final long serialVersionUID = 1L;

	public ScatterPlot(final String title, String xaxis, String yaxis, float[][] data) {
		super(title);
		
		final NumberAxis domainAxis = new NumberAxis(xaxis);
		domainAxis.setRange(0, 1);
		final NumberAxis rangeAxis = new NumberAxis(yaxis);
		rangeAxis.setRange(0, 1);
		final FastScatterPlot plot = new FastScatterPlot(data, domainAxis, rangeAxis);
		final JFreeChart chart = createChart(plot, title);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		setContentPane(chartPanel);
	}

	
	private JFreeChart createChart(final FastScatterPlot plot, String title) {

		final JFreeChart chart = new JFreeChart(title, plot);
		return chart;

	}
}
