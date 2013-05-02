package roemetz.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

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
	JTextField numSamples;

	public RMGUInterface(RoeMetz lsttemp, Container cp) {
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
		 * Panel within cofvInputPanel with fields to input variances
		 */
		JPanel varianceFields = new JPanel(new GridLayout(0, 18, 2, 5));

		vR00 = new JTextField(4);
		vC00 = new JTextField(4);
		vRC00 = new JTextField(4);
		vR10 = new JTextField(4);
		vC10 = new JTextField(4);
		vRC10 = new JTextField(4);
		vR01 = new JTextField(4);
		vC01 = new JTextField(4);
		vRC01 = new JTextField(4);
		vR11 = new JTextField(4);
		vC11 = new JTextField(4);
		vRC11 = new JTextField(4);
		vR0 = new JTextField(4);
		vC0 = new JTextField(4);
		vRC0 = new JTextField(4);
		vR1 = new JTextField(4);
		vC1 = new JTextField(4);
		vRC1 = new JTextField(4);

		JLabel vR00Label = new JLabel("vR00: ");
		JLabel vC00Label = new JLabel("vC00: ");
		JLabel vRC00Label = new JLabel("vRC00: ");
		JLabel vR10Label = new JLabel("vR10: ");
		JLabel vC10Label = new JLabel("vC10: ");
		JLabel vRC10Label = new JLabel("vRC10: ");
		JLabel vR01Label = new JLabel("vR01: ");
		JLabel vC01Label = new JLabel("vC01: ");
		JLabel vRC01Label = new JLabel("vRC01: ");
		JLabel vR11Label = new JLabel("vR11: ");
		JLabel vC11Label = new JLabel("vC11: ");
		JLabel vRC11Label = new JLabel("vRC11: ");
		JLabel vR0Label = new JLabel("vR0: ");
		JLabel vC0Label = new JLabel("vC0: ");
		JLabel vRC0Label = new JLabel("vRC0: ");
		JLabel vR1Label = new JLabel("vR1: ");
		JLabel vC1Label = new JLabel("vC1: ");
		JLabel vRC1Label = new JLabel("vRC1: ");

		varianceFields.add(vR00Label);
		varianceFields.add(vR00);
		varianceFields.add(vC00Label);
		varianceFields.add(vC00);
		varianceFields.add(vRC00Label);
		varianceFields.add(vRC00);
		varianceFields.add(vR10Label);
		varianceFields.add(vR10);
		varianceFields.add(vC10Label);
		varianceFields.add(vC10);
		varianceFields.add(vRC10Label);
		varianceFields.add(vRC10);
		varianceFields.add(vR01Label);
		varianceFields.add(vR01);
		varianceFields.add(vC01Label);
		varianceFields.add(vC01);
		varianceFields.add(vRC01Label);
		varianceFields.add(vRC01);
		varianceFields.add(vR11Label);
		varianceFields.add(vR11);
		varianceFields.add(vC11Label);
		varianceFields.add(vC11);
		varianceFields.add(vRC11Label);
		varianceFields.add(vRC11);
		varianceFields.add(vR0Label);
		varianceFields.add(vR0);
		varianceFields.add(vC0Label);
		varianceFields.add(vC0);
		varianceFields.add(vRC0Label);
		varianceFields.add(vRC0);
		varianceFields.add(vR1Label);
		varianceFields.add(vR1);
		varianceFields.add(vC1Label);
		varianceFields.add(vC1);
		varianceFields.add(vRC1Label);
		varianceFields.add(vRC1);

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

		cofvInputPanel.add(inputLabels);
		cofvInputPanel.add(varianceFields);
		cofvInputPanel.add(meansFields);
		cofvInputPanel.add(sizeFields);

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

		JButton doSimExp = new JButton("Perform Simulation Experiment");
		doSimExp.addActionListener(new doSimBtnListner());

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

	class doSimBtnListner implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
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
			SimRoeMetz.doSim(u, var_t, n);
			SimRoeMetz.printResults();
		}
	}

	class doGenRoeMetzBtnListner implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
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
			CofVGenRoeMetz.genRoeMetz(u, n, var_t);
			CofVGenRoeMetz.printResults();
		}
	}
}
