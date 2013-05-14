/*
 * RMGUInterface.java
 * 
 * v1.0b
 * 
 * @Author Rohan Pathare
 * 
 * This software and documentation (the "Software") were developed at the Food and Drug Administration (FDA) 
 * by employees of the Federal Government in the course of their official duties. Pursuant to Title 17, Section 
 * 105 of the United States Code, this work is not subject to copyright protection and is in the public domain. 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of the Software, to deal in the 
 * Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, or sell copies of the Software or derivatives, and to permit persons to whom the 
 * Software is furnished to do so. FDA assumes no responsibility whatsoever for use by other parties of the 
 * Software, its source code, documentation or compiled executables, and makes no guarantees, expressed or 
 * implied, about its quality, reliability, or any other characteristic.   Further, use of this code in no way 
 * implies endorsement by the FDA or confers any advantage in regulatory decisions.  Although this software 
 * can be redistributed and/or modified freely, we ask that any derivative works bear some notice that they 
 * are derived from it, and any modified versions bear some notice that they have been modified.
 * 	
 * 
 * 		This class describes the interface for iRoeMetz application. It contains a panel for
 * 		inputting means, components of variance, and experiment size. The next panel performs
 * 		multiple simulation experiments based on the input. The last panel estimates the components
 * 		of variance for the given input.
 */

package roemetz.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import mrmc.core.matrix;

import roemetz.core.CofVGenRoeMetz;
import roemetz.core.RoeMetz;
import roemetz.core.SimRoeMetz;

public class RMGUInterface {

	private JTextField vR00;
	private JTextField vC00;
	private JTextField vRC00;
	private JTextField vR10;
	private JTextField vC10;
	private JTextField vRC10;
	private JTextField vR01;
	private JTextField vC01;
	private JTextField vRC01;
	private JTextField vR11;
	private JTextField vC11;
	private JTextField vRC11;
	private JTextField vR0;
	private JTextField vC0;
	private JTextField vRC0;
	private JTextField vR1;
	private JTextField vC1;
	private JTextField vRC1;
	private JTextField mu0;
	private JTextField mu1;
	private JTextField n0;
	private JTextField n1;
	private JTextField nr;
	private JTextField numExp;
	private JTextField numSamples;
	private JTextField seed;
	private JDialog progDialog;
	private JRadioButton mod1SimButton;
	private JRadioButton mod2SimButton;
	private JRadioButton modDSimButton;
	private int simSelectedMod = 0;
	private JCheckBox useBias = new JCheckBox("Use Bias");
	public int useBiasM;
	private JRadioButton mod1EstButton;
	private JRadioButton mod2EstButton;
	private JRadioButton modDEstButton;
	public int estSelectedMod = 0;
	private static JProgressBar simProgress;
	private static RoeMetz appl;

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

		JLabel vR00Label = new JLabel("vR00: ");
		JLabel vC00Label = new JLabel("vC00: ");
		JLabel vRC00Label = new JLabel("vRC00: ");
		JLabel vR10Label = new JLabel("vR10: ");
		JLabel vC10Label = new JLabel("vC10: ");
		JLabel vRC10Label = new JLabel("vRC10: ");

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

		/*
		 * Panel within cofvInputPanel with fields to input variances (row 2)
		 */
		JPanel varianceFields2 = new JPanel();
		varianceFields2.setLayout(new FlowLayout());

		vR01 = new JTextField(4);
		vR01.setMaximumSize(vR01.getPreferredSize());
		vC01 = new JTextField(4);
		vC01.setMaximumSize(vC01.getPreferredSize());
		vRC01 = new JTextField(4);
		vRC01.setMaximumSize(vRC01.getPreferredSize());
		vR11 = new JTextField(4);
		vR11.setMaximumSize(vR11.getPreferredSize());
		vC11 = new JTextField(4);
		vC11.setMaximumSize(vC11.getPreferredSize());
		vRC11 = new JTextField(4);
		vRC11.setMaximumSize(vRC11.getPreferredSize());

		JLabel vR01Label = new JLabel("vR01: ");
		JLabel vC01Label = new JLabel("vC01: ");
		JLabel vRC01Label = new JLabel("vRC01: ");
		JLabel vR11Label = new JLabel("vR11: ");
		JLabel vC11Label = new JLabel("vC11: ");
		JLabel vRC11Label = new JLabel("vRC11: ");

		varianceFields2.add(vR01Label);
		varianceFields2.add(vR01);
		varianceFields2.add(vC01Label);
		varianceFields2.add(vC01);
		varianceFields2.add(vRC01Label);
		varianceFields2.add(vRC01);
		varianceFields2.add(vR11Label);
		varianceFields2.add(vR11);
		varianceFields2.add(vC11Label);
		varianceFields2.add(vC11);
		varianceFields2.add(vRC11Label);
		varianceFields2.add(vRC11);

		/*
		 * Panel within cofvInputPanel with fields to input variances (row 3)
		 */
		JPanel varianceFields3 = new JPanel();
		varianceFields3.setLayout(new FlowLayout());

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

		JLabel vR0Label = new JLabel("vR0: ");
		JLabel vC0Label = new JLabel("vC0: ");
		JLabel vRC0Label = new JLabel("vRC0: ");
		JLabel vR1Label = new JLabel("vR1: ");
		JLabel vC1Label = new JLabel("vC1: ");
		JLabel vRC1Label = new JLabel("vRC1: ");

		varianceFields3.add(vR0Label);
		varianceFields3.add(vR0);
		varianceFields3.add(vC0Label);
		varianceFields3.add(vC0);
		varianceFields3.add(vRC0Label);
		varianceFields3.add(vRC0);
		varianceFields3.add(vR1Label);
		varianceFields3.add(vR1);
		varianceFields3.add(vC1Label);
		varianceFields3.add(vC1);
		varianceFields3.add(vRC1Label);
		varianceFields3.add(vRC1);

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

		JButton populateButton = new JButton("Populate CofV (Default)");
		populateButton.addActionListener(new populateBtnListner());

		JButton popFromFile = new JButton("Populate CofV from File");
		popFromFile.addActionListener(new popFromFileListener());
		populateFields.add(populateButton);
		populateFields.add(popFromFile);

		/*
		 * Add sub-panels to cofvInputPanel
		 */
		cofvInputPanel.add(inputLabels);
		cofvInputPanel.add(varianceFields1);
		cofvInputPanel.add(varianceFields2);
		cofvInputPanel.add(varianceFields3);
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

		// create modality select buttons
		String str1 = "Modality 1";
		mod1SimButton = new JRadioButton(str1);
		mod1SimButton.setActionCommand(str1);
		mod1SimButton.setSelected(true);
		String str2 = "Modality 2";
		mod2SimButton = new JRadioButton(str2);
		mod2SimButton.setActionCommand(str2);
		String strD = "Difference";
		modDSimButton = new JRadioButton(strD);
		modDSimButton.setActionCommand(strD);
		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(mod1SimButton);
		group.add(mod2SimButton);
		group.add(modDSimButton);

		// Register a listener for the radio buttons.
		ModSimListner gListener = new ModSimListner();
		mod1SimButton.addActionListener(gListener);
		mod2SimButton.addActionListener(gListener);
		modDSimButton.addActionListener(gListener);

		useBias.addItemListener(new useBiasListner());

		simulationExperiment.add(seedLabel);
		simulationExperiment.add(seed);
		simulationExperiment.add(numExpLabel);
		simulationExperiment.add(numExp);
		simulationExperiment.add(mod1SimButton);
		simulationExperiment.add(mod2SimButton);
		simulationExperiment.add(modDSimButton);
		simulationExperiment.add(useBias);
		simulationExperiment.add(doSimExp);

		simExpPanel.add(simExpDesc);
		simExpPanel.add(simulationExperiment);

		/*
		 * Panel to estimate moments/components of variance
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

		// create modality select buttons
		mod1EstButton = new JRadioButton(str1);
		mod1EstButton.setActionCommand(str1);
		mod1EstButton.setSelected(true);
		mod2EstButton = new JRadioButton(str2);
		mod2EstButton.setActionCommand(str2);
		modDEstButton = new JRadioButton(strD);
		modDEstButton.setActionCommand(strD);
		// Group the radio buttons.
		ButtonGroup groupEst = new ButtonGroup();
		groupEst.add(mod1EstButton);
		groupEst.add(mod2EstButton);
		groupEst.add(modDEstButton);

		// Register a listener for the radio buttons.
		ModEstListner gListenerEst = new ModEstListner();
		mod1SimButton.addActionListener(gListenerEst);
		mod2SimButton.addActionListener(gListenerEst);
		modDSimButton.addActionListener(gListenerEst);

		JButton doGenRoeMetz = new JButton("Estimate Components of Variance");
		doGenRoeMetz.addActionListener(new doGenRoeMetzBtnListner());

		cofvResults.add(numSamplesLabel);
		cofvResults.add(numSamples);
		cofvResults.add(mod1EstButton);
		cofvResults.add(mod2EstButton);
		// TODO figure out how to difference estimate
		// cofvResults.add(modDEstButton);
		cofvResults.add(doGenRoeMetz);

		cofvResultsPanel.add(cofvResultsDesc);
		cofvResultsPanel.add(cofvResults);

		cp.add(cofvInputPanel);
		cp.add(new JSeparator());
		cp.add(simExpPanel);
		cp.add(new JSeparator());
		cp.add(cofvResultsPanel);
	}

	private void clearInputs() {
		vR00.setText("");
		vC00.setText("");
		vRC00.setText("");
		vR10.setText("");
		vC10.setText("");
		vRC10.setText("");
		vR01.setText("");
		vC01.setText("");
		vRC01.setText("");
		vR11.setText("");
		vC11.setText("");
		vRC11.setText("");
		vR0.setText("");
		vC0.setText("");
		vRC0.setText("");
		vR1.setText("");
		vC1.setText("");
		vRC1.setText("");
		mu0.setText("");
		mu1.setText("");
		n0.setText("");
		n1.setText("");
		nr.setText("");
	}

	private ArrayList<String> readFile(InputStreamReader isr) {
		BufferedReader br = new BufferedReader(isr);
		ArrayList<String> content = new ArrayList<String>();
		String strtemp;
		try {
			while ((strtemp = br.readLine()) != null) {
				content.add(strtemp);
			}
		} catch (Exception e) {
			System.err.println("read file Error in RMGUInterface.java: "
					+ e.getMessage());
		}
		return content;
	}

	private void parseCofVfile(File f) {
		ArrayList<String> fileContent = new ArrayList<String>();

		if (f != null) {
			String filename = f.getPath();
			try {
				InputStreamReader isr;
				DataInputStream din;
				FileInputStream fstream = new FileInputStream(filename);
				din = new DataInputStream(fstream);
				isr = new InputStreamReader(din);
				fileContent = readFile(isr);
				din.close();
			} catch (Exception e) {
				System.err.println("Error reading file" + filename
						+ e.getMessage());
			}
		} else {
			return;
		}

		clearInputs();
		int totalLine = fileContent.size();
		int counter = 0;
		while (counter < totalLine) {
			String tempstr = fileContent.get(counter).toUpperCase();
			int loc = tempstr.indexOf("R00:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vR00.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("C00:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vC00.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("RC00:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vRC00.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("R10:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vR10.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("C10:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vC10.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("RC10:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vRC10.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("R01:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vR01.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("C01:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vC01.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("RC01:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vRC01.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("R11:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vR11.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("C11:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vC11.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("RC11:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vRC11.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("R0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vR0.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("C0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vC0.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("RC0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vRC0.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("R1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vR1.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("C1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vC1.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("RC1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				vRC1.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("U0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				mu0.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("U1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				mu1.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("N0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				n0.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("N1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				n1.setText(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("NR:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				nr.setText(tempstr.substring(tmploc + 1).trim());
			}
			counter++;
		}
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

	class popFromFileListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"iRoeMetz CofV Input (.irm)", "irm");
			fc.setFileFilter(filter);
			int fcReturn = fc.showOpenDialog((Component) e.getSource());
			File f = fc.getSelectedFile();
			parseCofVfile(f);
		}
	}

	/*
	 * radio buttons to select the type of modality when performing simulation
	 * experiments
	 */
	class ModSimListner implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String str;
			str = e.getActionCommand();
			System.out.println(str + "radiobutton selected");
			if (str == "Modality 1") {
				simSelectedMod = 0;
			}
			if (str == "Modality 2") {
				simSelectedMod = 1;
			}
			if (str == "Difference") {
				simSelectedMod = 3;
			}
		}
	}

	/*
	 * radio buttons to select the type of modality when performing simulation
	 * experiments
	 */
	class ModEstListner implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String str;
			str = e.getActionCommand();
			System.out.println(str + "radiobutton selected");
			if (str == "Modality 1") {
				estSelectedMod = 0;
			}
			if (str == "Modality 2") {
				estSelectedMod = 1;
			}
			if (str == "Difference") {
				estSelectedMod = 3;
			}
		}
	}

	class useBiasListner implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (useBias.isSelected()) {
				useBiasM = 1;
			} else {
				useBiasM = 0;
			}
		}
	}

	private class SimExperiments extends SwingWorker<double[][][], Integer> {
		double[] u;
		double[] var_t;
		int[] n;
		int numTimes;
		Random rand;
		AtomicInteger val;

		public SimExperiments(double[] u, double[] var_t, int[] n, Random rand,
				int numTimes, AtomicInteger val) {
			this.u = u;
			this.var_t = var_t;
			this.n = n;
			this.rand = rand;
			this.numTimes = numTimes;
			this.val = val;
		}

		public double[][][] doInBackground() {
			double[][] avgBDGdata = new double[3][8];
			double[][] avgBCKdata = new double[3][7];
			double[][] avgDBMdata = new double[3][6];
			double[][] avgORdata = new double[3][6];
			double[][] avgMSdata = new double[3][6];
			for (int i = 0; i < numTimes; i++) {
				SimRoeMetz.doSim(u, var_t, n, rand, simSelectedMod, useBiasM);
				avgBDGdata = matrix.matrixAdd(avgBDGdata,
						SimRoeMetz.getBDGdata());
				avgBCKdata = matrix.matrixAdd(avgBCKdata,
						SimRoeMetz.getBCKdata());
				avgDBMdata = matrix.matrixAdd(avgDBMdata,
						SimRoeMetz.getDBMdata());
				avgORdata = matrix.matrixAdd(avgORdata, SimRoeMetz.getORdata());
				avgMSdata = matrix.matrixAdd(avgMSdata, SimRoeMetz.getMSdata());

				publish(val.getAndIncrement());
				setProgress(100 * i / numTimes);
			}
			double scaleFactor = 1.0 / (double) numTimes;
			avgBDGdata = matrix.scaleMatrix(avgBDGdata, scaleFactor);
			avgBCKdata = matrix.scaleMatrix(avgBCKdata, scaleFactor);
			avgDBMdata = matrix.scaleMatrix(avgDBMdata, scaleFactor);
			avgORdata = matrix.scaleMatrix(avgORdata, scaleFactor);
			avgMSdata = matrix.scaleMatrix(avgMSdata, scaleFactor);

			return new double[][][] { avgBDGdata, avgBCKdata, avgDBMdata,
					avgORdata, avgMSdata };
		}

		protected void process(List<Integer> chunks) {
			for (int num : chunks)
				simProgress.setValue(num);
		}

		protected void done() {
			firePropertyChange("done", 0, 1);
		}
	}

	class doSimBtnListner implements ActionListener {
		int doneTasks = 0;
		final int numCores = Runtime.getRuntime().availableProcessors();
		double[][][][] results = new double[numCores][][][];

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				double[] u = { Double.valueOf(mu0.getText()),
						Double.valueOf(mu1.getText()) };
				double[] var_t = { Double.valueOf(vR00.getText()),
						Double.valueOf(vC00.getText()),
						Double.valueOf(vRC00.getText()),
						Double.valueOf(vR10.getText()),
						Double.valueOf(vC10.getText()),
						Double.valueOf(vRC10.getText()),
						Double.valueOf(vR01.getText()),
						Double.valueOf(vC01.getText()),
						Double.valueOf(vRC01.getText()),
						Double.valueOf(vR11.getText()),
						Double.valueOf(vC11.getText()),
						Double.valueOf(vRC11.getText()),
						Double.valueOf(vR0.getText()),
						Double.valueOf(vC0.getText()),
						Double.valueOf(vRC0.getText()),
						Double.valueOf(vR1.getText()),
						Double.valueOf(vC1.getText()),
						Double.valueOf(vRC1.getText()) };
				int[] n = { Integer.valueOf(n0.getText()),
						Integer.valueOf(n1.getText()),
						Integer.valueOf(nr.getText()) };
				long seedVar = Long.parseLong(seed.getText());
				final int numTimes = Integer.valueOf(numExp.getText());

				Random rand = new Random(seedVar);
				final AtomicInteger val = new AtomicInteger(0);

				simProgress = new JProgressBar(0, numTimes);
				simProgress.setValue(val.get());

				progDialog = new JDialog(appl.getFrame(), "Simulation Progress");
				JPanel pane = new JPanel(new FlowLayout());
				pane.add(simProgress);
				progDialog.setContentPane(pane);
				progDialog.pack();
				progDialog.setVisible(true);

				final SimExperiments[] allTasks = new SimExperiments[numCores];
				for (int i = 0; i < numCores; i++) {
					final int taskNum = i;
					allTasks[i] = new SimExperiments(u, var_t, n, rand,
							numTimes / numCores, val);

					allTasks[i]
							.addPropertyChangeListener(new PropertyChangeListener() {
								public void propertyChange(
										PropertyChangeEvent evt) {
									if (evt.getPropertyName().equals("done")) {
										try {
											results[taskNum] = allTasks[taskNum]
													.get();
											doneTasks++;
											if (doneTasks == numCores) {
												doneTasks = 0;
												processResults();
											}
										} catch (InterruptedException e) {
											e.printStackTrace();
										} catch (ExecutionException e) {
											e.printStackTrace();
										}
									}
								}
							});
				}
				for (int i = 0; i < numCores; i++) {
					allTasks[i].execute();
				}

			} catch (NumberFormatException e1) {
				System.out.print(e1.toString());
				JOptionPane.showMessageDialog(appl.getFrame(),
						"Incorrect / Incomplete Input", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		}

		public void processResults() {
			progDialog.setVisible(false);

			double[][] allBDG = results[0][0];
			double[][] allBCK = results[0][1];
			double[][] allDBM = results[0][2];
			double[][] allOR = results[0][3];
			double[][] allMS = results[0][4];

			for (int i = 1; i < numCores; i++) {
				allBDG = matrix.matrixAdd(allBDG, results[i][0]);
				allBCK = matrix.matrixAdd(allBCK, results[i][1]);
				allDBM = matrix.matrixAdd(allDBM, results[i][2]);
				allOR = matrix.matrixAdd(allOR, results[i][3]);
				allMS = matrix.matrixAdd(allMS, results[i][4]);
			}

			allBDG = matrix.scaleMatrix(allBDG, 1.0 / (double) numCores);
			allBCK = matrix.scaleMatrix(allBCK, 1.0 / (double) numCores);
			allDBM = matrix.scaleMatrix(allDBM, 1.0 / (double) numCores);
			allOR = matrix.scaleMatrix(allOR, 1.0 / (double) numCores);
			allMS = matrix.scaleMatrix(allMS, 1.0 / (double) numCores);

			System.out.println("BDG across Experiments\t");
			matrix.printMatrix(allBDG);
			System.out.println("\n");
			System.out.println("BCK across Experiments\t");
			matrix.printMatrix(allBCK);
			System.out.println("\n");
			System.out.println("DBM across Experiments\t");
			matrix.printMatrix(allDBM);
			System.out.println("\n");
			System.out.println("OR across Experiments\t");
			matrix.printMatrix(allOR);
			System.out.println("\n");
			System.out.println("MS across Experiments\t");
			matrix.printMatrix(allMS);
			System.out.println("\n");
		}
	}

	private class EstimateCofV implements Runnable {
		double[] u;
		double[] var_t;
		int n;

		public void run() {
			CofVGenRoeMetz.genRoeMetz(u, n, var_t, estSelectedMod);
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
				double[] u = { Double.valueOf(mu0.getText()),
						Double.valueOf(mu1.getText()) };
				double[] var_t = { Double.valueOf(vR00.getText()),
						Double.valueOf(vC00.getText()),
						Double.valueOf(vRC00.getText()),
						Double.valueOf(vR10.getText()),
						Double.valueOf(vC10.getText()),
						Double.valueOf(vRC10.getText()),
						Double.valueOf(vR01.getText()),
						Double.valueOf(vC01.getText()),
						Double.valueOf(vRC01.getText()),
						Double.valueOf(vR11.getText()),
						Double.valueOf(vC11.getText()),
						Double.valueOf(vRC11.getText()),
						Double.valueOf(vR0.getText()),
						Double.valueOf(vC0.getText()),
						Double.valueOf(vRC0.getText()),
						Double.valueOf(vR1.getText()),
						Double.valueOf(vC1.getText()),
						Double.valueOf(vRC1.getText()) };

				int n = Integer.valueOf(numSamples.getText());

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
