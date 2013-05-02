package roemetz.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.text.DocumentFilter;

import roemetz.core.RoeMetz;

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

	public RMGUInterface(RoeMetz lsttemp, Container cp) {
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

		/*
		 * Panel to handle CofV inputs
		 */

		JPanel cofvInputPanel = new JPanel();
		cofvInputPanel
				.setLayout(new BoxLayout(cofvInputPanel, BoxLayout.Y_AXIS));
		cofvInputPanel.setPreferredSize(new Dimension(1000, 300));

		/*
		 * Panel within cofvInputPanel with description of input, type
		 */
		JPanel inputLabels = new JPanel(new FlowLayout());

		JLabel inputDesc = new JLabel("Input 18 Components of Variance: ");
		inputLabels.add(inputDesc);

		/*
		 * Panel within cofvInputPanel with fields to input CofV
		 */
		JPanel cofvFields = new JPanel(new FlowLayout());

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
		mu0 = new JTextField(4);
		mu1 = new JTextField(4);
		n0 = new JTextField(4);
		n1 = new JTextField(4);
		nr = new JTextField(4);

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
		JLabel mu0Label = new JLabel("\u00B50: ");
		JLabel mu1Label = new JLabel("\u00B51: ");
		JLabel n0Label = new JLabel("n0: ");
		JLabel n1Label = new JLabel("n1: ");
		JLabel nrLabel = new JLabel("nr: ");

		cofvFields.add(vR00Label);
		cofvFields.add(vR00);
		cofvFields.add(vC00Label);
		cofvFields.add(vC00);
		cofvFields.add(vRC00Label);
		cofvFields.add(vRC00);
		cofvFields.add(vR10Label);
		cofvFields.add(vR10);
		cofvFields.add(vC10Label);
		cofvFields.add(vC10);
		cofvFields.add(vRC10Label);
		cofvFields.add(vRC10);
		cofvFields.add(vR01Label);
		cofvFields.add(vR01);
		cofvFields.add(vC01Label);
		cofvFields.add(vC01);
		cofvFields.add(vRC01Label);
		cofvFields.add(vRC01);
		cofvFields.add(vR11Label);
		cofvFields.add(vR11);
		cofvFields.add(vC11Label);
		cofvFields.add(vC11);
		cofvFields.add(vRC11Label);
		cofvFields.add(vRC11);
		cofvFields.add(vR0Label);
		cofvFields.add(vR0);
		cofvFields.add(vC0Label);
		cofvFields.add(vC0);
		cofvFields.add(vRC0Label);
		cofvFields.add(vRC0);
		cofvFields.add(vR1Label);
		cofvFields.add(vR1);
		cofvFields.add(vC1Label);
		cofvFields.add(vC1);
		cofvFields.add(vRC1Label);
		cofvFields.add(vRC1);
		cofvFields.add(mu0Label);
		cofvFields.add(mu0);
		cofvFields.add(mu1Label);
		cofvFields.add(mu1);
		cofvFields.add(n0Label);
		cofvFields.add(n0);
		cofvFields.add(n1Label);
		cofvFields.add(n1);
		cofvFields.add(nrLabel);
		cofvFields.add(nr);

		cofvInputPanel.add(inputLabels);
		cofvInputPanel.add(cofvFields);

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

		cofvResultsPanel.add(cofvResultsDesc);
		cofvResultsPanel.add(cofvResults);

		cp.add(cofvInputPanel);
		cp.add(new JSeparator());
		cp.add(simExpPanel);
		cp.add(new JSeparator());
		cp.add(cofvResultsPanel);
	}
}
