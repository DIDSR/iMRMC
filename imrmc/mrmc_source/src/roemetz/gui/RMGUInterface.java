package roemetz.gui;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import mrmc.core.matrix;

import roemetz.core.CofVGenRoeMetz;
import roemetz.core.RoeMetz;
import roemetz.core.SimRoeMetz;

public class RMGUInterface {

	JTextField vR00;
	JTextField vC00;
	JTextField vRC00;
	JTextField vR10;
	JTextField vC10;
	JTextField vRC10;
	JTextField vR01;
	JTextField vC01;
	JTextField vRC01;
	JTextField vR11;
	JTextField vC11;
	JTextField vRC11;
	JTextField vR0;
	JTextField vC0;
	JTextField vRC0;
	JTextField vR1;
	JTextField vC1;
	JTextField vRC1;
	JTextField mu0;
	JTextField mu1;
	JTextField n0;
	JTextField n1;
	JTextField nr;
	JTextField numExp;
	JTextField numSamples;
	JTextField seed;
	JDialog progDialog;
	static JProgressBar simProgress;
	static JProgressBar estProgress;
	static RoeMetz appl;

	public RMGUInterface(RoeMetz lsttemp, Container cp) {
		appl = lsttemp;
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

		/*
		 * Panel to handle CofV inputs
		 */

		JPanel cofvInputPanel = new JPanel();
		cofvInputPanel
				.setLayout(new BoxLayout(cofvInputPanel, BoxLayout.Y_AXIS));

		/*
		 * Panel within cofvInputPanel with description of input, type
		 */
		JPanel inputLabels = new JPanel(new FlowLayout());

		JLabel inputDesc = new JLabel(
				"Input Means, Variances, and Experiment Size: ");
		inputLabels.add(inputDesc);

		/*
		 * Panel within cofvInputPanel with fields to input variances (row 1)
		 */
		JPanel varianceFields1 = new JPanel();
		varianceFields1.setLayout(new FlowLayout());

		vR00 = new JTextField(4);
		vR00.setMaximumSize(vR00.getPreferredSize());
		vC00 = new JTextField(4);
		vC00.setMaximumSize(vC00.getPreferredSize());
		vRC00 = new JTextField(4);
		vRC00.setMaximumSize(vRC00.getPreferredSize());
		vR10 = new JTextField(4);
		vR10.setMaximumSize(vR10.getPreferredSize());
		vC10 = new JTextField(4);
		vC10.setMaximumSize(vC10.getPreferredSize());
		vRC10 = new JTextField(4);
		vRC10.setMaximumSize(vRC10.getPreferredSize());
		vR01 = new JTextField(4);
		vR01.setMaximumSize(vR01.getPreferredSize());
		vC01 = new JTextField(4);
		vC01.setMaximumSize(vC01.getPreferredSize());
		vRC01 = new JTextField(4);
		vRC01.setMaximumSize(vRC01.getPreferredSize());

		JLabel vR00Label = new JLabel("vR00: ");
		JLabel vC00Label = new JLabel("vC00: ");
		JLabel vRC00Label = new JLabel("vRC00: ");
		JLabel vR10Label = new JLabel("vR10: ");
		JLabel vC10Label = new JLabel("vC10: ");
		JLabel vRC10Label = new JLabel("vRC10: ");
		JLabel vR01Label = new JLabel("vR01: ");
		JLabel vC01Label = new JLabel("vC01: ");
		JLabel vRC01Label = new JLabel("vRC01: ");

		varianceFields1.add(vR00Label);
		varianceFields1.add(vR00);
		varianceFields1.add(vC00Label);
		varianceFields1.add(vC00);
		varianceFields1.add(vRC00Label);
		varianceFields1.add(vRC00);
		varianceFields1.add(vR10Label);
		varianceFields1.add(vR10);
		varianceFields1.add(vC10Label);
		varianceFields1.add(vC10);
		varianceFields1.add(vRC10Label);
		varianceFields1.add(vRC10);
		varianceFields1.add(vR01Label);
		varianceFields1.add(vR01);
		varianceFields1.add(vC01Label);
		varianceFields1.add(vC01);
		varianceFields1.add(vRC01Label);
		varianceFields1.add(vRC01);

		/*
		 * Panel within cofvInputPanel with fields to input variances (row 2)
		 */
		JPanel varianceFields2 = new JPanel();
		varianceFields2.setLayout(new FlowLayout());

		vR11 = new JTextField(4);
		vR11.setMaximumSize(vR11.getPreferredSize());
		vC11 = new JTextField(4);
		vC11.setMaximumSize(vC11.getPreferredSize());
		vRC11 = new JTextField(4);
		vRC11.setMaximumSize(vRC11.getPreferredSize());
		vR0 = new JTextField(4);
		vR0.setMaximumSize(vR0.getPreferredSize());
		vC0 = new JTextField(4);
		vC0.setMaximumSize(vC0.getPreferredSize());
		vRC0 = new JTextField(4);
		vRC0.setMaximumSize(vRC0.getPreferredSize());
		vR1 = new JTextField(4);
		vR1.setMaximumSize(vR1.getPreferredSize());
		vC1 = new JTextField(4);
		vC1.setMaximumSize(vC1.getPreferredSize());
		vRC1 = new JTextField(4);
		vRC1.setMaximumSize(vRC1.getPreferredSize());

		JLabel vR11Label = new JLabel("vR11: ");
		JLabel vC11Label = new JLabel("vC11: ");
		JLabel vRC11Label = new JLabel("vRC11: ");
		JLabel vR0Label = new JLabel("vR0: ");
		JLabel vC0Label = new JLabel("vC0: ");
		JLabel vRC0Label = new JLabel("vRC0: ");
		JLabel vR1Label = new JLabel("vR1: ");
		JLabel vC1Label = new JLabel("vC1: ");
		JLabel vRC1Label = new JLabel("vRC1: ");

		varianceFields2.add(vR11Label);
		varianceFields2.add(vR11);
		varianceFields2.add(vC11Label);
		varianceFields2.add(vC11);
		varianceFields2.add(vRC11Label);
		varianceFields2.add(vRC11);
		varianceFields2.add(vR0Label);
		varianceFields2.add(vR0);
		varianceFields2.add(vC0Label);
		varianceFields2.add(vC0);
		varianceFields2.add(vRC0Label);
		varianceFields2.add(vRC0);
		varianceFields2.add(vR1Label);
		varianceFields2.add(vR1);
		varianceFields2.add(vC1Label);
		varianceFields2.add(vC1);
		varianceFields2.add(vRC1Label);
		varianceFields2.add(vRC1);

		/*
		 * Panel to input means
		 */
		JPanel meansFields = new JPanel(new FlowLayout());

		mu0 = new JTextField(4);
		mu1 = new JTextField(4);
		JLabel mu0Label = new JLabel("\u00B50: ");
		JLabel mu1Label = new JLabel("\u00B51: ");

		meansFields.add(mu0Label);
		meansFields.add(mu0);
		meansFields.add(mu1Label);
		meansFields.add(mu1);

		/*
		 * Panel to input experiment size
		 */
		JPanel sizeFields = new JPanel(new FlowLayout());

		n0 = new JTextField(4);
		n1 = new JTextField(4);
		nr = new JTextField(4);
		JLabel n0Label = new JLabel("n0: ");
		JLabel n1Label = new JLabel("n1: ");
		JLabel nrLabel = new JLabel("nr: ");

		sizeFields.add(n0Label);
		sizeFields.add(n0);
		sizeFields.add(n1Label);
		sizeFields.add(n1);
		sizeFields.add(nrLabel);
		sizeFields.add(nr);

		/*
		 * Panel to populate fields with default values
		 */
		JPanel populateFields = new JPanel(new FlowLayout());

		JButton populateButton = new JButton("Populate Components of Variance");
		populateButton.addActionListener(new populateBtnListner());
		populateFields.add(populateButton);

		/*
		 * Add sub-panels to cofvInputPanel
		 */
		cofvInputPanel.add(inputLabels);
		cofvInputPanel.add(varianceFields1);
		cofvInputPanel.add(varianceFields2);
		cofvInputPanel.add(meansFields);
		cofvInputPanel.add(sizeFields);
		cofvInputPanel.add(populateFields);

		/*
		 * Panel to perform simulation experiment
		 */
		JPanel simExpPanel = new JPanel();
		simExpPanel.setLayout(new BoxLayout(simExpPanel, BoxLayout.Y_AXIS));

		/*
		 * Panel within simExpPanel to describe function
		 */
		JPanel simExpDesc = new JPanel(new FlowLayout());

		JLabel expLabel = new JLabel("Simulation Experiment:");
		simExpDesc.add(expLabel);

		/*
		 * Panel within simExpPanel to show simulation experiment results
		 */
		JPanel simulationExperiment = new JPanel(new FlowLayout());

		numExp = new JTextField(4);
		JLabel numExpLabel = new JLabel("# of Experiments");
		JButton doSimExp = new JButton("Perform Simulation Experiment");
		doSimExp.addActionListener(new doSimBtnListner());
		JLabel seedLabel = new JLabel("Seed for RNG");
		seed = new JTextField(4);

		simulationExperiment.add(seedLabel);
		simulationExperiment.add(seed);
		simulationExperiment.add(numExpLabel);
		simulationExperiment.add(numExp);
		simulationExperiment.add(doSimExp);

		simExpPanel.add(simExpDesc);
		simExpPanel.add(simulationExperiment);

		/*
		 * Panel to calculate moments/components of variance?
		 */
		JPanel cofvResultsPanel = new JPanel();
		cofvResultsPanel.setLayout(new BoxLayout(cofvResultsPanel,
				BoxLayout.Y_AXIS));

		/*
		 * Panel within cofvResultsPanel to describe function
		 */
		JPanel cofvResultsDesc = new JPanel(new FlowLayout());
		JLabel cofvLabel = new JLabel("Components of Variance:");
		cofvResultsDesc.add(cofvLabel);

		/*
		 * Panel within cofvResultsPanel to display cofv Results
		 */
		JPanel cofvResults = new JPanel(new FlowLayout());

		numSamples = new JTextField(4);
		numSamples.setText("256");
		JLabel numSamplesLabel = new JLabel("# Samples: ");
		JButton doGenRoeMetz = new JButton("Estimate Components of Variance");
		doGenRoeMetz.addActionListener(new doGenRoeMetzBtnListner());

		cofvResults.add(numSamplesLabel);
		cofvResults.add(numSamples);
		cofvResults.add(doGenRoeMetz);

		cofvResultsPanel.add(cofvResultsDesc);
		cofvResultsPanel.add(cofvResults);

		cp.add(cofvInputPanel);
		cp.add(new JSeparator());
		cp.add(simExpPanel);
		cp.add(new JSeparator());
		cp.add(cofvResultsPanel);
	}

	class populateBtnListner implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			vR00.setText("0.166");
			vC00.setText("0.166");
			vRC00.setText("0.166");
			vR10.setText("0.166");
			vC10.setText("0.166");
			vRC10.setText("0.166");
			vR01.setText("0.166");
			vC01.setText("0.166");
			vRC01.setText("0.166");
			vR11.setText("0.166");
			vC11.setText("0.166");
			vRC11.setText("0.166");
			vR0.setText("0.166");
			vC0.setText("0.166");
			vRC0.setText("0.166");
			vR1.setText("0.166");
			vC1.setText("0.166");
			vRC1.setText("0.166");
			mu0.setText("1.5");
			mu1.setText("1.0");
			n0.setText("20");
			n1.setText("20");
			nr.setText("4");
		}
	}

	private class SimExperiments implements Runnable {
		double[] u;
		double[] var_t;
		int[] n;
		long seed;
		int numTimes;

		public void run() {
			simProgress.setMaximum(numTimes);

			double[][] avgBDGdata = new double[3][8];
			double[][] avgBCKdata = new double[3][7];
			double[][] avgDBMdata = new double[3][6];
			double[][] avgORdata = new double[3][6];
			double[][] avgMSdata = new double[3][6];
			for (int i = 0; i < numTimes; i++) {
				SimRoeMetz.doSim(u, var_t, n, seed);
				avgBDGdata = matrix.matrixAdd(avgBDGdata,
						SimRoeMetz.getBDGdata());
				avgBCKdata = matrix.matrixAdd(avgBCKdata,
						SimRoeMetz.getBCKdata());
				avgDBMdata = matrix.matrixAdd(avgDBMdata,
						SimRoeMetz.getDBMdata());
				avgORdata = matrix.matrixAdd(avgORdata, SimRoeMetz.getORdata());
				avgMSdata = matrix.matrixAdd(avgMSdata, SimRoeMetz.getMSdata());

				simProgress.setValue(i);
			}
			simProgress.setValue(numTimes);
			double scaleFactor = 1.0 / (double) numTimes;
			avgBDGdata = matrix.scaleMatrix(avgBDGdata, scaleFactor);
			avgBCKdata = matrix.scaleMatrix(avgBCKdata, scaleFactor);
			avgDBMdata = matrix.scaleMatrix(avgDBMdata, scaleFactor);
			avgORdata = matrix.scaleMatrix(avgORdata, scaleFactor);
			avgMSdata = matrix.scaleMatrix(avgMSdata, scaleFactor);

			System.out.println("BDG across Experiments\t");
			matrix.printMatrix(avgBDGdata);
			System.out.println("\n");
			System.out.println("BCK across Experiments\t");
			matrix.printMatrix(avgBCKdata);
			System.out.println("\n");
			System.out.println("DBM across Experiments\t");
			matrix.printMatrix(avgDBMdata);
			System.out.println("\n");
			System.out.println("OR across Experiments\t");
			matrix.printMatrix(avgORdata);
			System.out.println("\n");
			System.out.println("MS across Experiments\t");
			matrix.printMatrix(avgMSdata);
			System.out.println("\n");
			progDialog.setVisible(false);
		}

		public SimExperiments(double[] u, double[] var_t, int[] n, long seed,
				int numTimes) {
			this.u = u;
			this.var_t = var_t;
			this.n = n;
			this.seed = seed;
			this.numTimes = numTimes;
		}
	}

	class doSimBtnListner implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				double[] u = { Double.parseDouble(mu0.getText()),
						Double.parseDouble(mu1.getText()) };
				double[] var_t = { Double.parseDouble(vR00.getText()),
						Double.parseDouble(vC00.getText()),
						Double.parseDouble(vRC00.getText()),
						Double.parseDouble(vR10.getText()),
						Double.parseDouble(vC10.getText()),
						Double.parseDouble(vRC10.getText()),
						Double.parseDouble(vR01.getText()),
						Double.parseDouble(vC01.getText()),
						Double.parseDouble(vRC01.getText()),
						Double.parseDouble(vR11.getText()),
						Double.parseDouble(vC11.getText()),
						Double.parseDouble(vRC11.getText()),
						Double.parseDouble(vR0.getText()),
						Double.parseDouble(vC0.getText()),
						Double.parseDouble(vRC0.getText()),
						Double.parseDouble(vR1.getText()),
						Double.parseDouble(vC1.getText()),
						Double.parseDouble(vRC1.getText()) };
				int[] n = { Integer.parseInt(n0.getText()),
						Integer.parseInt(n1.getText()),
						Integer.parseInt(nr.getText()) };
				long seedVar = Long.parseLong(seed.getText());

				int numTimes = Integer.parseInt(numExp.getText());
				simProgress = new JProgressBar(0, 100);
				simProgress.setValue(0);

				// Perform simulations in background thread
				Thread simThread = new Thread(new SimExperiments(u, var_t, n,
						seedVar, numTimes));
				simThread.start();

				progDialog = new JDialog(appl.getFrame(), "Simulation Progress");
				JPanel pane = new JPanel(new FlowLayout());
				pane.add(simProgress);
				progDialog.setContentPane(pane);
				progDialog.pack();
				progDialog.setVisible(true);

				if (!simThread.isAlive()) {
					progDialog.setVisible(false);
				}

			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(appl.getFrame(),
						"Incorrect / Incomplete Input", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private class EstimateCofV implements Runnable {
		double[] u;
		double[] var_t;
		int n;

		public void run() {
			CofVGenRoeMetz.genRoeMetz(u, n, var_t);
			CofVGenRoeMetz.printResults();
		}

		public EstimateCofV(double[] u, double[] var_t, int n) {
			this.u = u;
			this.var_t = var_t;
			this.n = n;
		}
	}

	class doGenRoeMetzBtnListner implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				double[] u = { Double.parseDouble(mu0.getText()),
						Double.parseDouble(mu1.getText()) };
				double[] var_t = { Double.parseDouble(vR00.getText()),
						Double.parseDouble(vC00.getText()),
						Double.parseDouble(vRC00.getText()),
						Double.parseDouble(vR10.getText()),
						Double.parseDouble(vC10.getText()),
						Double.parseDouble(vRC10.getText()),
						Double.parseDouble(vR01.getText()),
						Double.parseDouble(vC01.getText()),
						Double.parseDouble(vRC01.getText()),
						Double.parseDouble(vR11.getText()),
						Double.parseDouble(vC11.getText()),
						Double.parseDouble(vRC11.getText()),
						Double.parseDouble(vR0.getText()),
						Double.parseDouble(vC0.getText()),
						Double.parseDouble(vRC0.getText()),
						Double.parseDouble(vR1.getText()),
						Double.parseDouble(vC1.getText()),
						Double.parseDouble(vRC1.getText()) };

				int n = Integer.parseInt(numSamples.getText());

				Thread estThread = new Thread(new EstimateCofV(u, var_t, n));
				estThread.run();
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(appl.getFrame(),
						"Incorrect / Incomplete Input", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}
}
