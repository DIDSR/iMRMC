/**
 * RMGUInterface.java
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
 */

package roemetz.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.uncommons.maths.random.MersenneTwisterRNG;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import mrmc.core.DBRecord;
import mrmc.core.Matrix;
import mrmc.core.StatTest;
import roemetz.core.CalcGenRoeMetz;
import roemetz.core.RoeMetz;
import roemetz.core.SimRoeMetz;

/**
 * This class describes the interface for iRoeMetz application. It contains a
 * panel for inputting means, components of variance, and experiment size. The
 * next panel performs multiple simulation experiments based on the input. The
 * last panel estimates the components of variance for the given input.
 * 
 * @author Rohan Pathare
 */
public class RMGUInterface {
	private final int USE_MLE = 1;
	private final int NO_MLE = 0;
	private static RoeMetz appl;

	private JTextField mu0, mu1;
	private JTextField  v_R0,  v_C0,  v_RC0,  v_R1,  v_C1,  v_RC1;
	private JTextField v_AR0, v_AC0, v_ARC0, v_AR1, v_AC1, v_ARC1;
	private JTextField v_BR0, v_BC0, v_BRC0, v_BR1, v_BC1, v_BRC1;
	private JTextField n0, n1, nr;
	
	private JLabel mu0Label, mu1Label;
	private JLabel n0Label, n1Label, nrLabel;
	private JLabel  v_R0Label,  v_C0Label,  v_RC0Label,  v_R1Label,  v_C1Label,  v_RC1Label;
	private JLabel v_AR0Label, v_AC0Label, v_ARC0Label, v_AR1Label, v_AC1Label, v_ARC1Label;
	private JLabel v_BR0Label, v_BC0Label, v_BRC0Label, v_BR1Label, v_BC1Label, v_BRC1Label;
	
	private JTextField numExp;
	private JTextField seed;
	
	private JDialog progDialog;
	private JCheckBox useMLEbox = new JCheckBox("Use MLE?");
	private int useMLE = NO_MLE;
	private String simSaveDirectory;
	private static JProgressBar simProgress;
	public String calcSaveDirectory;
	private DecimalFormat threeDecOpt = new DecimalFormat("0.###");
	private DecimalFormat threeDecOptE = new DecimalFormat("0.###E0");
	private DecimalFormat threeDec = new DecimalFormat("0.000");
	private DecimalFormat threeDecE = new DecimalFormat("0.000E0");

	/**
	 * Gets the experiment means parameters from the text fields
	 * 
	 * @return Array of experiment means parameters
	 */
	public double[] getMeans() {
		return new double[] { Double.valueOf(mu0.getText()),
				Double.valueOf(mu1.getText()) };
	}

	/**
	 * Gets the components of variance from the text fields
	 * 
	 * @return Array of components of variance
	 */
	public double[] getVariances() {
		return new double[] { 
				Double.valueOf(v_AR0.getText()),
				Double.valueOf(v_AC0.getText()),
				Double.valueOf(v_ARC0.getText()),
				Double.valueOf(v_AR1.getText()), 
				Double.valueOf(v_AC1.getText()),
				Double.valueOf(v_ARC1.getText()),
				Double.valueOf(v_BR0.getText()), 
				Double.valueOf(v_BC0.getText()),
				Double.valueOf(v_BRC0.getText()),
				Double.valueOf(v_BR1.getText()), 
				Double.valueOf(v_BC1.getText()),
				Double.valueOf(v_BRC1.getText()), 
				Double.valueOf(v_R0.getText()),
				Double.valueOf(v_C0.getText()), 
				Double.valueOf(v_RC0.getText()),
				Double.valueOf(v_R1.getText()), 
				Double.valueOf(v_C1.getText()),
				Double.valueOf(v_RC1.getText()) };
	}

	/**
	 * Gets the experiment size parameters from the text fields
	 * 
	 * @return Array of experiment size parameters
	 */
	public long[] getSizes() {
		return new long[] { Long.valueOf(n0.getText()),
				Long.valueOf(n1.getText()), Long.valueOf(nr.getText()) };
	}

	/**
	 * Sole constructor for GUI, only invoked by RoeMetz driver class
	 * 
	 * @param lsttemp Application driver class
	 * @param cp Content pane of the application
	 */
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
		JPanel inputHeaderMeans = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JLabel inputDescMeans = new JLabel(
				"Input Means: ");
		inputHeaderMeans.add(inputDescMeans);
//		inputHeaderMeans.setAlignmentX(Component.LEFT_ALIGNMENT);

		/*
		 * Panel within cofvInputPanel with description of input, type
		 */
		JPanel inputHeaderVarsO = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JLabel inputDescVars = new JLabel(
				"Input Variances Invariant to Modality: ");
		inputHeaderVarsO.add(inputDescVars);

		/*
		 * Panel within cofvInputPanel with description of input, type
		 */
		JPanel inputHeaderVarsA = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JLabel inputDescVarsA = new JLabel(
				"Input Variances Specific to Modality A: ");
		inputHeaderVarsA.add(inputDescVarsA);

		/*
		 * Panel within cofvInputPanel with description of input, type
		 */
		JPanel inputHeaderVarsB = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JLabel inputDescVarsB= new JLabel(
				"Input Variances Specific to Modality B: ");
		inputHeaderVarsB.add(inputDescVarsB);

		/*
		 * Panel within cofvInputPanel with description of input, type
		 */
		JPanel inputHeaderSize = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JLabel inputDescSize= new JLabel(
				"Input Experiment Size: ");
		inputHeaderSize.add(inputDescSize);

		initializeInputLabels();
		initalizeInputFields();

		/*
		 * Panel to input means
		 */
		JPanel meansFields = new JPanel(new FlowLayout(FlowLayout.LEFT));
		meansFields.add(Box.createHorizontalStrut(20));
		meansFields.add(mu0Label);
		meansFields.add(mu0);
		meansFields.add(mu1Label);
		meansFields.add(mu1);

		/*
		 * Panel within cofvInputPanel with fields to input variances (row 3)
		 */
		JPanel varianceFieldsO = new JPanel(new FlowLayout(FlowLayout.LEFT));
		varianceFieldsO.add(Box.createHorizontalStrut(20));
		varianceFieldsO.add(v_R0Label);
		varianceFieldsO.add(v_R0);
		varianceFieldsO.add(v_C0Label);
		varianceFieldsO.add(v_C0);
		varianceFieldsO.add(v_RC0Label);
		varianceFieldsO.add(v_RC0);
		varianceFieldsO.add(v_R1Label);
		varianceFieldsO.add(v_R1);
		varianceFieldsO.add(v_C1Label);
		varianceFieldsO.add(v_C1);
		varianceFieldsO.add(v_RC1Label);
		varianceFieldsO.add(v_RC1);

		/*
		 * Panel within cofvInputPanel with fields to input variances (row 1)
		 */
		JPanel varianceFieldsA = new JPanel(new FlowLayout(FlowLayout.LEFT));
		varianceFieldsA.add(Box.createHorizontalStrut(20));
		varianceFieldsA.add(v_AR0Label);
		varianceFieldsA.add(v_AR0);
		varianceFieldsA.add(v_AC0Label);
		varianceFieldsA.add(v_AC0);
		varianceFieldsA.add(v_ARC0Label);
		varianceFieldsA.add(v_ARC0);
		varianceFieldsA.add(v_AR1Label);
		varianceFieldsA.add(v_AR1);
		varianceFieldsA.add(v_AC1Label);
		varianceFieldsA.add(v_AC1);
		varianceFieldsA.add(v_ARC1Label);
		varianceFieldsA.add(v_ARC1);

		/*
		 * Panel within cofvInputPanel with fields to input variances (row 2)
		 */
		JPanel varianceFieldsB = new JPanel(new FlowLayout(FlowLayout.LEFT));
		varianceFieldsB.add(Box.createHorizontalStrut(20));
		varianceFieldsB.add(v_BR0Label);
		varianceFieldsB.add(v_BR0);
		varianceFieldsB.add(v_BC0Label);
		varianceFieldsB.add(v_BC0);
		varianceFieldsB.add(v_BRC0Label);
		varianceFieldsB.add(v_BRC0);
		varianceFieldsB.add(v_BR1Label);
		varianceFieldsB.add(v_BR1);
		varianceFieldsB.add(v_BC1Label);
		varianceFieldsB.add(v_BC1);
		varianceFieldsB.add(v_BRC1Label);
		varianceFieldsB.add(v_BRC1);

		/*
		 * Panel to input experiment size
		 */
		JPanel sizeFields = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sizeFields.add(Box.createHorizontalStrut(20));
		sizeFields.add(n0Label);
		sizeFields.add(n0);
		sizeFields.add(n1Label);
		sizeFields.add(n1);
		sizeFields.add(nrLabel);
		sizeFields.add(nr);

		/*
		 * Panel of buttons controlling input fields
		 */
		JPanel populateFields = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JButton clearButton = new JButton("Clear Fields");
		clearButton.addActionListener(new ClearBtnListener());
		JButton popFromFile = new JButton("Populate Components from File");
		popFromFile.addActionListener(new PopFromFileListener());
		JButton saveFields = new JButton("Save Components to File");
		saveFields.addActionListener(new SaveFieldsListener());

		populateFields.add(Box.createHorizontalStrut(20));
		populateFields.add(clearButton);
		populateFields.add(popFromFile);
		populateFields.add(saveFields);

		/*
		 * Add sub-panels to cofvInputPanel
		 */
		cofvInputPanel.add(inputHeaderMeans);
		cofvInputPanel.add(meansFields);
		cofvInputPanel.add(inputHeaderVarsO);
		cofvInputPanel.add(varianceFieldsO);
		cofvInputPanel.add(inputHeaderVarsA);
		cofvInputPanel.add(varianceFieldsA);
		cofvInputPanel.add(inputHeaderVarsB);
		cofvInputPanel.add(varianceFieldsB);
		cofvInputPanel.add(inputHeaderSize);
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
		JPanel simExpDesc = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JLabel expLabel = new JLabel("Simulation Experiments:");
		simExpDesc.add(expLabel);

		/*
		 * Panel within simExpPanel to show simulation experiment results
		 */
		JPanel simulationExperiment = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JLabel seedLabel = new JLabel("Seed for RNG");
		seed = new JTextField(9);
		seed.setText(Long.toString(System.currentTimeMillis()));

		JLabel numExpLabel = new JLabel("# of Experiments");
		numExp = new JTextField(4);
		
		useMLEbox.addItemListener(new UseMLEListener());

		simulationExperiment.add(Box.createHorizontalStrut(20));
		simulationExperiment.add(seedLabel);
		simulationExperiment.add(seed);
		simulationExperiment.add(numExpLabel);
		simulationExperiment.add(numExp);
		simulationExperiment.add(useMLEbox);

		/*
		 * Panel within simExpPanel to show simulation experiment results
		 */
		JPanel simulationButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JButton doSimExp = new JButton("Perform Simulation Experiments");
		doSimExp.addActionListener(new DoSimBtnListener());

		JButton saveLoc = new JButton("Output Location");
		saveLoc.addActionListener(new SaveSimulationListener());

		simulationButtonsPanel.add(Box.createHorizontalStrut(20));
		simulationButtonsPanel.add(saveLoc);
		simulationButtonsPanel.add(doSimExp);

		simExpPanel.add(simExpDesc);
		simExpPanel.add(simulationExperiment);
		simExpPanel.add(simulationButtonsPanel);

		/*
		 * Panel to calculate moments/components of variance
		 */
		JPanel calculatePanel = new JPanel();
		calculatePanel
				.setLayout(new BoxLayout(calculatePanel, BoxLayout.Y_AXIS));

		/*
		 * Panel within calculatePanel to describe function
		 */
		JPanel cofvResultsDesc = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel cofvLabel = new JLabel("Calculate Components of Variance:");
		cofvResultsDesc.add(cofvLabel);

		/*
		 * Panel within calculatePanel to display calc Results
		 */
		JPanel cofvResults = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JButton doGenRoeMetz = new JButton("Perform Calculation");
		doGenRoeMetz.addActionListener(new DoGenRoeMetzBtnListener());
		JButton saveCalcResults = new JButton("Output Location");
		saveCalcResults.addActionListener(new saveCalcResultsListener());

		cofvResults.add(Box.createHorizontalStrut(20));
		cofvResults.add(saveCalcResults);
		cofvResults.add(doGenRoeMetz);

		calculatePanel.add(cofvResultsDesc);
		calculatePanel.add(cofvResults);

		cp.add(cofvInputPanel);
		cp.add(new JSeparator());
		cp.add(simExpPanel);
		cp.add(new JSeparator());
		cp.add(calculatePanel);
	}



	/**
	 * Initialize all text input fields with default placeholder values and
	 * designate size within GUI
	 */
	private void initalizeInputFields() {
		v_AR0 = new JTextField("0.166", 4);
		v_AR0.setMaximumSize(v_AR0.getPreferredSize());
		v_AC0 = new JTextField("0.166", 4);
		v_AC0.setMaximumSize(v_AC0.getPreferredSize());
		v_ARC0 = new JTextField("0.166", 4);
		v_ARC0.setMaximumSize(v_ARC0.getPreferredSize());
		v_AR1 = new JTextField("0.166", 4);
		v_AR1.setMaximumSize(v_AR1.getPreferredSize());
		v_AC1 = new JTextField("0.166", 4);
		v_AC1.setMaximumSize(v_AC1.getPreferredSize());
		v_ARC1 = new JTextField("0.166", 4);
		v_ARC1.setMaximumSize(v_ARC1.getPreferredSize());
		v_BR0 = new JTextField("0.166", 4);
		v_BR0.setMaximumSize(v_BR0.getPreferredSize());
		v_BC0 = new JTextField("0.166", 4);
		v_BC0.setMaximumSize(v_BC0.getPreferredSize());
		v_BRC0 = new JTextField("0.166", 4);
		v_BRC0.setMaximumSize(v_BRC0.getPreferredSize());
		v_BR1 = new JTextField("0.166", 4);
		v_BR1.setMaximumSize(v_BR1.getPreferredSize());
		v_BC1 = new JTextField("0.166", 4);
		v_BC1.setMaximumSize(v_BC1.getPreferredSize());
		v_BRC1 = new JTextField("0.166", 4);
		v_BRC1.setMaximumSize(v_BRC1.getPreferredSize());
		v_R0 = new JTextField("0.166", 4);
		v_R0.setMaximumSize(v_R0.getPreferredSize());
		v_C0 = new JTextField("0.166", 4);
		v_C0.setMaximumSize(v_C0.getPreferredSize());
		v_RC0 = new JTextField("0.166", 4);
		v_RC0.setMaximumSize(v_RC0.getPreferredSize());
		v_R1 = new JTextField("0.166", 4);
		v_R1.setMaximumSize(v_R1.getPreferredSize());
		v_C1 = new JTextField("0.166", 4);
		v_C1.setMaximumSize(v_C1.getPreferredSize());
		v_RC1 = new JTextField("0.166", 4);
		v_RC1.setMaximumSize(v_RC1.getPreferredSize());
		mu0 = new JTextField("1.5", 4);
		mu1 = new JTextField("1.0", 4);
		n0 = new JTextField("20", 4);
		n1 = new JTextField("20", 4);
		nr = new JTextField("4", 4);
	}

	/**
	 * Set text for all labels next to text input fields
	 */
	private void initializeInputLabels() {
		v_AR0Label = new JLabel("v_AR0: ");
		v_AC0Label = new JLabel("v_AC0: ");
		v_ARC0Label = new JLabel("v_ARC0: ");
		v_AR1Label = new JLabel("v_AR1: ");
		v_AC1Label = new JLabel("v_AC1: ");
		v_ARC1Label = new JLabel("v_ARC1: ");
		v_BR0Label = new JLabel("v_BR0: ");
		v_BC0Label = new JLabel("v_BC0: ");
		v_BRC0Label = new JLabel("v_BRC0: ");
		v_BR1Label = new JLabel("v_BR1: ");
		v_BC1Label = new JLabel("v_BC1: ");
		v_BRC1Label = new JLabel("v_BRC1: ");
		v_R0Label = new JLabel("  v_R0: ");
		v_C0Label = new JLabel("  v_C0: ");
		v_RC0Label = new JLabel("  v_RC0: ");
		v_R1Label = new JLabel("  v_R1: ");
		v_C1Label = new JLabel("  v_C1: ");
		v_RC1Label = new JLabel("  v_RC1: ");
		mu0Label = new JLabel("\u00B5_A: ");
		mu1Label = new JLabel("\u00B5_B: ");
		n0Label = new JLabel("n0: ");
		n1Label = new JLabel("n1: ");
		nrLabel = new JLabel("nr: ");
	}

	/**
	 * Empty out all text input fields
	 */
	private void clearInputs() {
		v_AR0.setText("");
		v_AC0.setText("");
		v_ARC0.setText("");
		v_AR1.setText("");
		v_AC1.setText("");
		v_ARC1.setText("");
		v_BR0.setText("");
		v_BC0.setText("");
		v_BRC0.setText("");
		v_BR1.setText("");
		v_BC1.setText("");
		v_BRC1.setText("");
		v_R0.setText("");
		v_C0.setText("");
		v_RC0.setText("");
		v_R1.setText("");
		v_C1.setText("");
		v_RC1.setText("");
		mu0.setText("");
		mu1.setText("");
		n0.setText("");
		n1.setText("");
		nr.setText("");
	}

	/**
	 * Reads in list of experiment parameters from a file and sets text input
	 * fields with said parameters. Input file format is described in iRoeMetz
	 * User Guide
	 * 
	 * @param f The file object from which to read values
	 */
	private void parseCofVfile(File f) {
		ArrayList<String> fileContent = new ArrayList<String>();

		if (f != null) {
			fileContent = readFile(f.getPath());
		} else {
			return;
		}

		clearInputs();
		setInputsFromFile(fileContent);
	}

	/**
	 * Parses experiment parameter information from ArrayList of parameters in
	 * string form. Sets input text fields with corresponding values.
	 * 
	 * @param fileContent ArrayList where each element is a single line
	 *            containing experiment parameter information
	 */
	private void setInputsFromFile(ArrayList<String> fileContent) {
		int totalLine = fileContent.size();
		int counter = 0;
		while (counter < totalLine) {
			String tempstr = fileContent.get(counter).toUpperCase();
			counter++;
			int loc;
			
			loc = tempstr.indexOf("ARC0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_ARC0.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("ARC1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_ARC1.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("BRC0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_BRC0.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("BRC1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_BRC1.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("RC0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_RC0.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("RC1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_RC1.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("AR0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_AR0.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("AC0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_AC0.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("AR1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_AR1.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("AC1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_AC1.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("BR0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_BR0.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("BC0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_BC0.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("BR1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_BR1.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("BC1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_BC1.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("R0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_R0.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("C0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_C0.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("R1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_R1.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("C1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				v_C1.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("UA:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				mu0.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("UB:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				mu1.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("N0:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				n0.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("N1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				n1.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("NR:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				nr.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
		}
	}

	/**
	 * Takes a text file and makes each line an individual String object which
	 * is placed in an ArrayList
	 * 
	 * @param filename Path to input file
	 * @return ArrayList where each element is a single line from the input file
	 *         specified
	 */
	private ArrayList<String> readFile(String filename) {
		ArrayList<String> content = new ArrayList<String>();
		try {
			InputStreamReader isr;
			DataInputStream din;
			FileInputStream fstream;
			fstream = new FileInputStream(filename);
			din = new DataInputStream(fstream);
			isr = new InputStreamReader(din);
			BufferedReader br = new BufferedReader(isr);
			String strtemp;
			while ((strtemp = br.readLine()) != null) {
				content.add(strtemp);
			}
			br.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			System.err.println("read file Error in RMGUInterface.java: "
					+ e.getMessage());
		}
		return content;
	}

	/**
	 * Handler for "Clear Fields" button
	 */
	class ClearBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			clearInputs();
		}
	}

	/**
	 * Handler for "Populate Components from File" button. Displays a file
	 * browser and processes selected file.
	 */
	class PopFromFileListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"iRoeMetz CofV Input (.irm)", "irm");
			fc.setFileFilter(filter);
			@SuppressWarnings("unused")
			int fcReturn = fc.showOpenDialog((Component) e.getSource());
			File f = fc.getSelectedFile();
			parseCofVfile(f);
		}
	}

	/**
	 * Handler for "Save Components to File" button. Displays a file browser and
	 * writes information to selected file.
	 */
	class SaveFieldsListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				JFileChooser fc = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"iRoeMetz CofV Input (.irm)", "irm");
				fc.setFileFilter(filter);
				int fcReturn = fc.showOpenDialog((Component) e.getSource());
				if (fcReturn == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					String fPath = f.getPath();
					if (!fPath.toLowerCase().endsWith(".irm")) {
						f = new File(fPath + ".irm");
					}
					if (!f.exists()) {
						f.createNewFile();
					}
					FileWriter fw = new FileWriter(f.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write("AR0: " + v_AR0.getText() + "\n");
					bw.write("AC0: " + v_AC0.getText() + "\n");
					bw.write("ARC0: " + v_ARC0.getText() + "\n");
					bw.write("AR1: " + v_AR1.getText() + "\n");
					bw.write("AC1: " + v_AC1.getText() + "\n");
					bw.write("ARC1: " + v_ARC1.getText() + "\n");
					bw.write("BR0: " + v_BR0.getText() + "\n");
					bw.write("BC0: " + v_BC0.getText() + "\n");
					bw.write("BRC0: " + v_BRC0.getText() + "\n");
					bw.write("BR1: " + v_BR1.getText() + "\n");
					bw.write("BC1: " + v_BC1.getText() + "\n");
					bw.write("BRC1: " + v_BRC1.getText() + "\n");
					bw.write("R0: " + v_R0.getText() + "\n");
					bw.write("C0: " + v_C0.getText() + "\n");
					bw.write("RC0: " + v_RC0.getText() + "\n");
					bw.write("R1: " + v_R1.getText() + "\n");
					bw.write("C1: " + v_C1.getText() + "\n");
					bw.write("RC1: " + v_RC1.getText() + "\n");
					bw.write("uA: " + mu0.getText() + "\n");
					bw.write("uB: " + mu1.getText() + "\n");
					bw.write("n0: " + n0.getText() + "\n");
					bw.write("n1: " + n1.getText() + "\n");
					bw.write("nr: " + nr.getText() + "\n");
					bw.close();
				}
			} catch (HeadlessException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Handler for "Use MLE" check box.
	 * 
	 */
	class UseMLEListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (useMLEbox.isSelected()) {
				useMLE = USE_MLE;
			} else {
				useMLE = NO_MLE;
			}
		}
	}

	/**
	 * Handler for "Output Location" button. Displays a file browser and
	 * designates selected directory as path to save simulation output.
	 */
	class SaveSimulationListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int fcReturn = fc.showSaveDialog((JComponent) arg0.getSource());
			if (fcReturn == JFileChooser.APPROVE_OPTION) {
				simSaveDirectory = fc.getSelectedFile().toString();
				System.out.println(simSaveDirectory);
			} else {
				System.out.println("No save directory selected");
				simSaveDirectory = null;
			}
		}
	}

	/**
	 * Handler for "Output Location" button in calculation panel. Displays a
	 * file browser and designates selected directory as path to save
	 * calculation output
	 */
	class saveCalcResultsListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int fcReturn = fc.showSaveDialog((JComponent) e.getSource());
			if (fcReturn == JFileChooser.APPROVE_OPTION) {
				calcSaveDirectory = fc.getSelectedFile().toString();
				System.out.println(calcSaveDirectory);
			} else {
				System.out.println("No save directory selected");
				calcSaveDirectory = null;
			}
		}
	}

	private class SimExperiments {

		/**
		 * Constructor for group of simulation experiments.
		 * 
		 * @param u Contains experiment means
		 * @param var_t Contains components of variance
		 * @param n Contains experiment size
		 * @param rand Random number generator, no guarantee on current position
		 *            in sequence
		 * @param numTimes Number of simulation experiments to perform.
		 * @param progVal Shared counter of number of experiments performed
		 *            across all threads
		 * @param filenameTime Timestamp when this set of experiments was
		 *            started, to categorize output files
		 * @throws IOException 
		 */
		public double [][][] do_SimExperiments(double[] u, double[] var_t, long[] n, Random rand,
				long numTimes, AtomicInteger progVal, String filenameTime) throws IOException {

			double[][] avgBDGdata = new double[3][8];
			double[][] avgBCKdata = new double[3][7];
			double[][] avgDBMdata = new double[4][6];
			double[][] avgORdata = new double[4][6];
			double[][] avgMSdata = new double[4][6];
			double[] avgAUC = new double[3];

			SimRoeMetz currSim = new SimRoeMetz(u, var_t, n, rand, useMLE);
			for (long i = 0; i < numTimes; i++) {

				currSim.doSim(var_t, rand);
				currSim.processSimExperiment();
				
				if (simSaveDirectory != null && !simSaveDirectory.equals("")) {
					writeMRMCFile(currSim.gettA0(), currSim.gettB0(),
							currSim.gettA1(), currSim.gettB1(),
							currSim.getAUC(), filenameTime, i);
					writeComponentsFile(currSim.getBDGdata(), n,
							currSim.getAUC(), useMLE, filenameTime, i);
				}

				avgBDGdata = Matrix.matrixAdd(avgBDGdata, currSim.getBDGdata());
				avgBCKdata = Matrix.matrixAdd(avgBCKdata, currSim.getBCKdata());
				avgDBMdata = Matrix.matrixAdd(avgDBMdata, currSim.getDBMdata());
				avgORdata = Matrix.matrixAdd(avgORdata, currSim.getORdata());
				avgMSdata = Matrix.matrixAdd(avgMSdata, currSim.getMSdata());
				avgAUC = Matrix.matrixAdd(avgAUC, currSim.getAUC());

			}

			double scaleFactor = 1.0 / (double) numTimes;
			avgBDGdata = Matrix.scaleMatrix(avgBDGdata, scaleFactor);
			avgBCKdata = Matrix.scaleMatrix(avgBCKdata, scaleFactor);
			avgDBMdata = Matrix.scaleMatrix(avgDBMdata, scaleFactor);
			avgORdata = Matrix.scaleMatrix(avgORdata, scaleFactor);
			avgMSdata = Matrix.scaleMatrix(avgMSdata, scaleFactor);
			avgAUC = Matrix.scaleVector(avgAUC, scaleFactor);

			return new double[][][] { avgBDGdata, avgBCKdata, avgDBMdata,
					avgORdata, avgMSdata, { avgAUC } };
		}

	}
	
	/**
	 * Performs multiple consecutive simulation experiments in a separate
	 * thread. Enables multi-threading to take advantage of multi-core systems
	 * and keep GUI responsive during heavy calculations.
	 * 
	 */
	private class SimExperiments_thread extends SwingWorker<double[][][], Integer> {
		double[] u; // experiment means
		double[] var_t; // components of variance
		long[] n; // experiment sizes
		long numTimes;
		Random rand;
		AtomicInteger progVal;
		String filenameTime;
		int whichTask;

		/**
		 * Constructor for group of simulation experiments.
		 * 
		 * @param u Contains experiment means
		 * @param var_t Contains components of variance
		 * @param n Contains experiment size
		 * @param rand Random number generator, no guarantee on current position
		 *            in sequence
		 * @param numTimes Number of simulation experiments to perform.
		 * @param progVal Shared counter of number of experiments performed
		 *            across all threads
		 * @param filenameTime Timestamp when this set of experiments was
		 *            started, to categorize output files
		 * @param whichTask Indentifier for this group of simulation experiments
		 *            (this thread)
		 */
		public SimExperiments_thread(double[] u, double[] var_t, long[] n, Random rand,
				long numTimes, AtomicInteger progVal, String filenameTime,
				int whichTask) {
			this.u = u;
			this.var_t = var_t;
			this.n = n;
			this.rand = rand;
			this.numTimes = numTimes;
			this.progVal = progVal;
			this.filenameTime = filenameTime;
			this.whichTask = whichTask;
		}

		/**
		 * Actual task that is performed
		 * 
		 * @return Averaged components of variance decompositions and AUCs for
		 *         this group of simulation experiments
		 * @throws IOException 
		 */
		public double[][][] doInBackground() throws IOException {
			double[][] avgBDGdata = new double[3][8];
			double[][] avgBCKdata = new double[3][7];
			double[][] avgDBMdata = new double[4][6];
			double[][] avgORdata = new double[4][6];
			double[][] avgMSdata = new double[4][6];
			double[] avgAUC = new double[3];

			SimRoeMetz currSim = new SimRoeMetz(u, var_t, n, rand, useMLE);
			for (int i = 0; i < numTimes; i++) {

				currSim.doSim(var_t, rand);
				currSim.processSimExperiment();
				
				if (simSaveDirectory != null && !simSaveDirectory.equals("")) {
					writeMRMCFile(currSim.gettA0(), currSim.gettB0(),
							currSim.gettA1(), currSim.gettB1(),
							currSim.getAUC(), filenameTime,
							((whichTask * numTimes) + i));
					writeComponentsFile(currSim.getBDGdata(), n,
							currSim.getAUC(), useMLE, filenameTime,
							((whichTask * numTimes) + i));
				}

				avgBDGdata = Matrix.matrixAdd(avgBDGdata, currSim.getBDGdata());
				avgBCKdata = Matrix.matrixAdd(avgBCKdata, currSim.getBCKdata());
				avgDBMdata = Matrix.matrixAdd(avgDBMdata, currSim.getDBMdata());
				avgORdata = Matrix.matrixAdd(avgORdata, currSim.getORdata());
				avgMSdata = Matrix.matrixAdd(avgMSdata, currSim.getMSdata());
				avgAUC = Matrix.matrixAdd(avgAUC, currSim.getAUC());

				publish(progVal.getAndIncrement());
				setProgress((int) (100 * i / numTimes));
			}

			double scaleFactor = 1.0 / (double) numTimes;
			avgBDGdata = Matrix.scaleMatrix(avgBDGdata, scaleFactor);
			avgBCKdata = Matrix.scaleMatrix(avgBCKdata, scaleFactor);
			avgDBMdata = Matrix.scaleMatrix(avgDBMdata, scaleFactor);
			avgORdata = Matrix.scaleMatrix(avgORdata, scaleFactor);
			avgMSdata = Matrix.scaleMatrix(avgMSdata, scaleFactor);
			avgAUC = Matrix.scaleVector(avgAUC, scaleFactor);

			return new double[][][] { avgBDGdata, avgBCKdata, avgDBMdata,
					avgORdata, avgMSdata, { avgAUC } };
		}

		protected void process(List<Integer> chunks) {
			for (int num : chunks)
				simProgress.setValue(num);
		}

		protected void done() {
			firePropertyChange("done", 0, 1);
		}
	}

	/**
	 * Handler for "Perform Simulation Experiments" button. Sets up
	 * multi-threaded, multi-core spread of experiment tasks if possible.
	 * 
	 */
	class DoSimBtnListener implements ActionListener {
		int finishedTasks = 0;
		final int numCores = Runtime.getRuntime().availableProcessors();
		// final int numCores = 1;
		int numCoresToUse;
		double[][][][] results = new double[numCores][][][];
		long[] n;

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				double[] u = getMeans();
				double[] var_t = getVariances();
				n = getSizes();
				long seedVar = Long.parseLong(seed.getText());
				final long numOfExper = Integer.valueOf(numExp.getText());

				if (simSaveDirectory == null || simSaveDirectory.equals("")) {
					JOptionPane
							.showMessageDialog(
									appl.getFrame(),
									"Save directory not specified.\nExperiment output files will not be written.",
									"Warning", JOptionPane.WARNING_MESSAGE);
				}

				// create string representation of current time to use in
				// filename
				DateFormat dateForm = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
				Date currDate = new Date();
				final String filenameTime = dateForm.format(currDate);

				// create global RNG which is used across all experiments
				byte[] byteSeed = ByteBuffer.allocate(16).putLong(seedVar)
						.array();
				Random rand = new MersenneTwisterRNG(byteSeed);

				final AtomicInteger progVal = new AtomicInteger(0);

				createProgressBar((int) numOfExper, progVal.get());

				// divide simulations into separate tasks
				if (numOfExper < numCores) {
					numCoresToUse = (int) numOfExper;
				} else {
					numCoresToUse = numCores;
				}

				if ( numCoresToUse == 1) {
					final SimExperiments oneCore = new SimExperiments();
					try {
						results[0] = oneCore.do_SimExperiments(u, var_t, n, rand, numOfExper, progVal, filenameTime);
						processResults( simSaveDirectory, filenameTime);

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				
				} else {
					final SimExperiments_thread[] allTasks = new SimExperiments_thread[numCoresToUse];
					for (int i = 0; i < numCoresToUse; i++) {
						final int taskNum = i;
						allTasks[i] = new SimExperiments_thread(u, var_t, n, rand,
								numOfExper / numCoresToUse, progVal, filenameTime, i);
						// Check to see when each task finishes and get its results
						allTasks[i]
								.addPropertyChangeListener(new PropertyChangeListener() {
									public void propertyChange(
											PropertyChangeEvent evt) {
										if (evt.getPropertyName().equals("done")) {
											try {
												results[taskNum] = allTasks[taskNum]
														.get();
												finishedTasks++;
												if (finishedTasks == numCoresToUse) {
													finishedTasks = 0;
													processResults(
															simSaveDirectory,
															filenameTime);
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
					// run each task in its own thread, to spread across cores
					for (int i = 0; i < numCoresToUse; i++) {
						allTasks[i].execute();
					}
				}
			} catch (NumberFormatException e1) {
				System.out.println(e1.toString());
				JOptionPane.showMessageDialog(appl.getFrame(),
						"Incorrect / Incomplete Input", "Error",
						JOptionPane.ERROR_MESSAGE);

			}
			
		}

		/**
		 * Makes a bar indicating the amount of progress over all simulation
		 * experiments
		 * 
		 * @param numTimes Total number of experiments to be performed, maximum
		 *            value of progress bar
		 * @param initProgress Initial value of progress bar
		 */
		private void createProgressBar(int numTimes, int initProgress) {
			simProgress = new JProgressBar(0, numTimes);
			simProgress.setValue(initProgress);
			progDialog = new JDialog(appl.getFrame(), "Simulation Progress");
			JPanel pane = new JPanel(new FlowLayout());
			pane.add(simProgress);
			progDialog.setContentPane(pane);
			progDialog.pack();
			progDialog.setVisible(true);
		}

		/**
		 * Called after all groups of simulation experiments are completed.
		 * Averages together their results and calls method to display them.
		 */
		public void processResults(String simSaveDirectory, String filenameTime) {
			progDialog.setVisible(false);

			double[][] avgdBDG = new double[3][8];
			double[][] avgdBCK = new double[3][7];
			double[][] avgdDBM = new double[4][6];
			double[][] avgdOR = new double[4][6];
			double[][] avgdMS = new double[4][6];
			double[] avgdAUC = new double[3];

			for (int i = 0; i < numCoresToUse; i++) {
				avgdBDG = Matrix.matrixAdd(avgdBDG, results[i][0]);
				avgdBCK = Matrix.matrixAdd(avgdBCK, results[i][1]);
				avgdDBM = Matrix.matrixAdd(avgdDBM, results[i][2]);
				avgdOR = Matrix.matrixAdd(avgdOR, results[i][3]);
				avgdMS = Matrix.matrixAdd(avgdMS, results[i][4]);
				avgdAUC = Matrix.matrixAdd(avgdAUC, results[i][5][0]);
			}

			avgdBDG = Matrix.scaleMatrix(avgdBDG, 1.0 / (double) numCoresToUse);
			avgdBCK = Matrix.scaleMatrix(avgdBCK, 1.0 / (double) numCoresToUse);
			avgdDBM = Matrix.scaleMatrix(avgdDBM, 1.0 / (double) numCoresToUse);
			avgdOR = Matrix.scaleMatrix(avgdOR, 1.0 / (double) numCoresToUse);
			avgdMS = Matrix.scaleMatrix(avgdMS, 1.0 / (double) numCoresToUse);
			avgdAUC = Matrix.scaleVector(avgdAUC, 1.0 / (double) numCoresToUse);

			double[][] BDGcoeff = DBRecord.genBDGCoeff(n[2], n[0], n[1]);
			double[][] BCKcoeff = DBRecord.genBCKCoeff(n[2], n[0], n[1]);
			double[][] DBMcoeff = DBRecord.genDBMCoeff(n[2], n[0], n[1]);
			double[][] MScoeff = DBRecord.genMSCoeff(n[2], n[0], n[1]);
			double[][] ORcoeff = DBRecord.genORCoeff(n[2], n[0], n[1]);

			double[][][] allCoeffs = new double[][][] { BDGcoeff, BCKcoeff,
					DBMcoeff, MScoeff, ORcoeff };
			double[][][] allDecomps = new double[][][] { avgdBDG, avgdBCK,
					avgdDBM, avgdOR, avgdMS };

			showSimOutput(allDecomps, allCoeffs, avgdAUC);

			writeSummaryFile(simSaveDirectory, "Summary of Simulation Results",
					"results-simulation-" + filenameTime, allDecomps,
					allCoeffs, avgdAUC);

		}

		/**
		 * Creates a pop-up table to display results of the simulations.
		 * 
		 * @param allDecomps Contains all of the decompositions of the
		 *            components of variance
		 * @param allCoeffs Contains all of the coefficients for each
		 *            decomposition
		 * @param avgdAUC Contains the average AUCs for all simulated
		 *            experiments
		 */
		private void showSimOutput(double[][][] allDecomps,
				double[][][] allCoeffs, double[] avgdAUC) {

			JDialog simOutput = new JDialog(appl.getFrame(),
					"Simulation Results");
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

			JPanel tablePanel = new JPanel();
			tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.X_AXIS));

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

			JTabbedPane tabTables = makeTableTabs(allDecomps, allCoeffs);

			// Display AUCs
			JLabel AUCs = new JLabel("AUC1: " + threeDecOpt.format(avgdAUC[0])
					+ "   AUC2: " + threeDecOpt.format(avgdAUC[1])
					+ "   AUC1-AUC2: " + threeDecOpt.format(avgdAUC[2]) + "   ");

			// create modality select buttons
			String str1 = "Modality 1";
			JRadioButton mod1SimButton = new JRadioButton(str1);
			mod1SimButton.setActionCommand(str1);
			mod1SimButton.setSelected(true);
			String str2 = "Modality 2";
			JRadioButton mod2SimButton = new JRadioButton(str2);
			mod2SimButton.setActionCommand(str2);
			String strD = "Difference";
			JRadioButton modDSimButton = new JRadioButton(strD);
			modDSimButton.setActionCommand(strD);
			// Group the radio buttons.
			ButtonGroup group = new ButtonGroup();
			group.add(mod1SimButton);
			group.add(mod2SimButton);
			group.add(modDSimButton);
			// Register a listener for the radio buttons.
			ModSimListener gListener = new ModSimListener(tabTables,
					allDecomps, allCoeffs);
			mod1SimButton.addActionListener(gListener);
			mod2SimButton.addActionListener(gListener);
			modDSimButton.addActionListener(gListener);

			tablePanel.add(tabTables);
			buttonPanel.add(AUCs);
			buttonPanel.add(mod1SimButton);
			buttonPanel.add(mod2SimButton);
			buttonPanel.add(modDSimButton);
			panel.add(tablePanel);
			panel.add(buttonPanel);
			simOutput.add(panel);
			simOutput.pack();
			simOutput.setVisible(true);
		}

		/**
		 * Handler for "Modality 1", "Modality 2", and "Difference" radio
		 * buttons in pop-up table of simulation results.
		 * 
		 */
		class ModSimListener implements ActionListener {
			JTabbedPane tabTables;
			private double[][] allBDG;
			private double[][] allBCK;
			private double[][] allDBM;
			private double[][] allOR;
			private double[][] allMS;
			private double[][] BDGcoeff;
			private double[][] BCKcoeff;
			private double[][] DBMcoeff;
			private double[][] MScoeff;
			private double[][] ORcoeff;

			public ModSimListener(JTabbedPane tabTables,
					double[][][] allDecomps, double[][][] allCoeffs) {
				this.tabTables = tabTables;
				this.allBDG = allDecomps[0];
				this.allBCK = allDecomps[1];
				this.allDBM = allDecomps[2];
				this.allOR = allDecomps[3];
				this.allMS = allDecomps[4];
				this.BDGcoeff = allCoeffs[0];
				this.BCKcoeff = allCoeffs[1];
				this.DBMcoeff = allCoeffs[2];
				this.MScoeff = allCoeffs[3];
				this.ORcoeff = allCoeffs[4];
			}

			public void actionPerformed(ActionEvent e) {
				String str;
				str = e.getActionCommand();
				if (str == "Modality 1") {
					updatePanes(0);
				}
				if (str == "Modality 2") {
					updatePanes(1);
				}
				if (str == "Difference") {
					updatePanes(3);
				}
			}

			/**
			 * Changes values of pop-up table according to which modality has
			 * been selected
			 * 
			 * @param mod Selected modality
			 */
			private void updatePanes(int mod) {
				updateBDGpane((JComponent) tabTables.getComponent(0), mod,
						allBDG, BDGcoeff);
				updateBCKpane((JComponent) tabTables.getComponent(1), mod,
						allBCK, BCKcoeff);
				updateDBMpane((JComponent) tabTables.getComponent(2), mod,
						allDBM, DBMcoeff);
				updateORpane((JComponent) tabTables.getComponent(3), mod,
						allOR, ORcoeff);
				updateMSpane((JComponent) tabTables.getComponent(4), mod,
						allMS, MScoeff);
			}
		}
	}

	/**
	 * Class to enable calculation of components of variance in a separate
	 * thread to keep GUI responsive during heavy calculation.
	 * 
	 */
	private class CalculateCofV extends SwingWorker<double[][][], Integer> {
		double[] u; // experiment means
		double[] var_t; // components of variance
		long[] n; // experiment sizes

		public CalculateCofV(double[] u, double[] var_t, long[] n2) {
			this.u = u;
			this.var_t = var_t;
			this.n = n2;
		}

		/**
		 * Performs calculation of components of variance and returns their
		 * decompositions
		 */
		public double[][][] doInBackground() {
			CalcGenRoeMetz.genRoeMetz(u, var_t, n);
			double[][][] results_temp = new double[][][] { CalcGenRoeMetz.getBDGdata(),
					CalcGenRoeMetz.getBCKdata(), CalcGenRoeMetz.getDBMdata(),
					CalcGenRoeMetz.getORdata(), CalcGenRoeMetz.getMSdata() };
			
			return results_temp;
		}

		protected void done() {
			firePropertyChange("done", 0, 1);
		}
	}

	/**
	 * Handler for "Perform Calculation" button. Starts calculation in its own
	 * thread and displays results in a pop-up table.
	 * 
	 */
	class DoGenRoeMetzBtnListener implements ActionListener {
		double[][][] results; // averaged decompositions of cofv
		long[] n; // experiment size

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				double[] u = getMeans();
				double[] var_t = getVariances();
				n = getSizes();

				final CalculateCofV calcTask = new CalculateCofV(u, var_t, n);
				calcTask.addPropertyChangeListener(new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("done")) {
							try {
								results = calcTask.get();
								processResults();
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							}
						}
					}
				});

				calcTask.execute();
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(appl.getFrame(),
						"Incorrect / Incomplete Input", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

		/**
		 * Gets calculation results, 
		 * calculates decomposition coefficients and
		 * calls method to display them in pop-up table
		 */
		public void processResults() {
			double[][] BDG = results[0];
			double[][] BCK = results[1];
			double[][] DBM = results[2];
			double[][] OR = results[3];
			double[][] MS = results[4];
			double[][] BDGcoeff = DBRecord.genBDGCoeff(n[2], n[0], n[1]);
			double[][] BCKcoeff = DBRecord.genBCKCoeff(n[2], n[0], n[1]);
			double[][] DBMcoeff = DBRecord.genDBMCoeff(n[2], n[0], n[1]);
			double[][] MScoeff = DBRecord.genMSCoeff(n[2], n[0], n[1]);
			double[][] ORcoeff = DBRecord.genORCoeff(n[2], n[0], n[1]);

			double[][][] allCoeffs = new double[][][] { BDGcoeff, BCKcoeff,
					DBMcoeff, MScoeff, ORcoeff };
			double[][][] allDecomps = new double[][][] { BDG, BCK, DBM, OR, MS };

			showCalcOutput(allDecomps, allCoeffs);

			double[] AUCs = { allDecomps[0][0][0], allDecomps[0][1][0],
					allDecomps[0][0][0] - allDecomps[0][1][0] };
			DateFormat dateForm = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
			Date currDate = new Date();
			final String filenameTime = dateForm.format(currDate);

			writeSummaryFile(calcSaveDirectory,
					"Summary of Calculation Results", "calc-results-"
							+ filenameTime, allDecomps, allCoeffs, AUCs);
		}

		/**
		 * Displays the pop-up table with results of calculation.
		 * 
		 * @param allDecomps Contains all individual decompositions of
		 *            components of variance
		 * @param allCoeffs Contains all individual coefficients for each
		 *            decomposition
		 */
		private void showCalcOutput(double[][][] allDecomps,
				double[][][] allCoeffs) {
			JDialog estOutput = new JDialog(appl.getFrame(),
					"Calculation Results");
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

			JPanel tablePanel = new JPanel();
			tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

			JTabbedPane tabTables = makeTableTabs(allDecomps, allCoeffs);

			// create AUCs label
			JLabel AUCs = new JLabel("AUC1: "
					+ threeDecOpt.format(allDecomps[0][0][0])
					+ "   AUC2: "
					+ threeDecOpt.format(allDecomps[0][1][0])
					+ "   AUC1-AUC2: "
					+ threeDecOpt.format(allDecomps[0][0][0]
							- allDecomps[0][1][0]) + "    ");

			// create modality select buttons
			String str1 = "Modality 1";
			JRadioButton mod1EstButton = new JRadioButton(str1);
			mod1EstButton.setActionCommand(str1);
			mod1EstButton.setSelected(true);
			String str2 = "Modality 2";
			JRadioButton mod2EstButton = new JRadioButton(str2);
			mod2EstButton.setActionCommand(str2);
			String strD = "Difference";
			JRadioButton modDEstButton = new JRadioButton(strD);
			modDEstButton.setActionCommand(strD);
			// Group the radio buttons.
			ButtonGroup groupEst = new ButtonGroup();
			groupEst.add(mod1EstButton);
			groupEst.add(mod2EstButton);
			groupEst.add(modDEstButton);

			// Register a listener for the radio buttons.
			ModCalcListener gListenerEst = new ModCalcListener(tabTables,
					allDecomps, allCoeffs);
			mod1EstButton.addActionListener(gListenerEst);
			mod2EstButton.addActionListener(gListenerEst);
			modDEstButton.addActionListener(gListenerEst);

			tablePanel.add(tabTables);
			buttonPanel.add(AUCs);
			buttonPanel.add(mod1EstButton);
			buttonPanel.add(mod2EstButton);
			buttonPanel.add(modDEstButton);
			panel.add(tablePanel);
			panel.add(buttonPanel);
			estOutput.add(panel);
			estOutput.pack();
			estOutput.setVisible(true);
		}

		/**
		 * Handler for "Modality 1", "Modality 2", and "Difference" radio
		 * buttons in pop-up table of calculation results.
		 */
		class ModCalcListener implements ActionListener {
			JTabbedPane tabTables;
			double[][] BDG;
			double[][] BCK;
			double[][] DBM;
			double[][] OR;
			double[][] MS;
			double[][] BDGcoeff;
			double[][] BCKcoeff;
			double[][] DBMcoeff;
			double[][] ORcoeff;
			double[][] MScoeff;

			public ModCalcListener(JTabbedPane tabTables,
					double[][][] allDecomps, double[][][] allCoeffs) {
				this.tabTables = tabTables;
				this.BDG = allDecomps[0];
				this.BCK = allDecomps[1];
				this.DBM = allDecomps[2];
				this.OR = allDecomps[3];
				this.MS = allDecomps[4];
				this.BDGcoeff = allCoeffs[0];
				this.BCKcoeff = allCoeffs[1];
				this.DBMcoeff = allCoeffs[2];
				this.MScoeff = allCoeffs[3];
				this.ORcoeff = allCoeffs[4];
			}

			/**
			 * Performed when any of the radio buttons are used
			 */
			public void actionPerformed(ActionEvent e) {
				String str;
				str = e.getActionCommand();
				if (str == "Modality 1") {
					updatePanes(0);
				}
				if (str == "Modality 2") {
					updatePanes(1);
				}
				if (str == "Difference") {
					updatePanes(3);
				}
			}

			/**
			 * Changes values of pop-up table according to which modality has
			 * been selected
			 * 
			 * @param mod Selected modality
			 */
			private void updatePanes(int mod) {
				updateBDGpane((JComponent) tabTables.getComponent(0), mod,
						results[0], BDGcoeff);
				updateBCKpane((JComponent) tabTables.getComponent(1), mod,
						results[1], BCKcoeff);
				updateDBMpane((JComponent) tabTables.getComponent(2), mod,
						results[2], DBMcoeff);
				updateORpane((JComponent) tabTables.getComponent(3), mod,
						results[3], ORcoeff);
				updateMSpane((JComponent) tabTables.getComponent(4), mod,
						results[4], MScoeff);
			}
		}
	}

	/**
	 * Writes the results of a simulation experiment as ROC scores to a text
	 * file, formatted to be read by iMRMC or equivalent software
	 * 
	 * @param t00 Matrix for normal cases, modality 0
	 * @param t01 Matrix for normal cases, modality 1
	 * @param t10 Matrix for disease cases, modality 0
	 * @param t11 Matrix for disease cases, modality 1
	 * @param auc AUCs of experiment1
	 * @param filename Filename prefix (is a timestamp) for a particular batch
	 *            of output files
	 * @param l The number of this particular experiment in the batch
	 */
	public void writeMRMCFile(double[][] t00, double[][] t01, double[][] t10,
			double[][] t11, double[] auc, String filename, long l) {
		try {
			File file = new File(simSaveDirectory + "/" + "sim-" + filename
					+ "-" + String.format("%05d", l) + ".imrmc");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("Simulated iMRMC input from " + filename + "\n");
			bw.write("\n");
			bw.write("NR: " + t00.length + "\n");
			bw.write("N0: " + t00[0].length + "\n");
			bw.write("N1: " + t10[0].length + "\n");
			bw.write("NM: 2\n");
			bw.write("\n");
			bw.write("AUC1: " + threeDecOpt.format(auc[0]) + "\n");
			bw.write("AUC2: " + threeDecOpt.format(auc[1]) + "\n");
			bw.write("DAUC: " + threeDecOpt.format(auc[2]) + "\n");
			bw.write("\n");
			bw.write("BEGIN DATA:\n");

			int caseNum = 1;
			for (int j = 0; j < t00[0].length; j++) {
				bw.write("-1," + caseNum + ",0,0\n");
				caseNum++;
			}
			for (int k = 0; k < t10[0].length; k++) {
				bw.write("-1," + caseNum + ",0,1\n");
				caseNum++;
			}

			for (int i = 0; i < t00.length; i++) {
				caseNum = 1;
				for (int m = 0; m < t00[i].length; m++) {
					bw.write((i + 1) + "," + caseNum + ",1," + t00[i][m] + "\n");
					bw.write((i + 1) + "," + caseNum + ",2," + t01[i][m] + "\n");
					caseNum++;
				}
				for (int n = 0; n < t10[i].length; n++) {
					bw.write((i + 1) + "," + caseNum + ",1," + t10[i][n] + "\n");
					bw.write((i + 1) + "," + caseNum + ",2," + t11[i][n] + "\n");
					caseNum++;
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes component info, p-values, and confidence interval of a single study to file
	 * @param BDGdata BDG components of study
	 * @param n Experiment size
	 * @param auc AUC of experiment
	 * @param useMLE whether or not to use MLE
	 * @param filename Timestamp based filename to identify group of experiments
	 * @param l Individual experiment number
	 */
	public void writeComponentsFile(double[][] BDGdata, long[] n, double[] auc,
			int useMLE, String filename, long l) {
		DBRecord tempRecord = new DBRecord(BDGdata, 0, n[2], n[0], n[1],
				new double[] { auc[0], auc[1] });
		StatTest tempStat = new StatTest(tempRecord, 3, useMLE, 0.05,
				Math.abs(auc[0] - auc[1]));
		System.out.println("p-val = " + tempStat.getpValF());
		System.out.println("CI = " + tempStat.getCI()[0] + ", "
				+ tempStat.getCI()[1]);

		try {
			File file = new File(simSaveDirectory + "/" + "sim-comp-"
					+ filename + "-" + String.format("%05d", l) + ".txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("BDG components from simulated study " + filename + "-"
					+ l + "\n");

			int col = BDGdata[0].length;
			int row = BDGdata.length;
			DecimalFormat df = new DecimalFormat("0.###E0");
			for (int i = 0; i < row; i++) {
				String temp = "";
				for (int j = 0; j < col; j++) {
					int totalWidth = 14;
					int numWidth = df.format(BDGdata[i][j]).length();
					int numSpaces = totalWidth - numWidth;
					temp = temp + df.format(BDGdata[i][j]);
					for (int x = 0; x < numSpaces; x++) {
						temp = temp + " ";
					}
				}
				temp = temp + "\n";
				bw.write(temp);
			}

			bw.write("p-value: " + tempStat.getpValF() + "\n");
			bw.write("Confidence Interval: (" + tempStat.getCI()[0] + ", "
					+ tempStat.getCI()[1] + ")\n");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Writes the results of a simulation (averaged results) or calculation to
	 * file, consisting of components and AUCs.
	 * 
	 * @param summaryTitle Header of summary file
	 * @param filename identifier file name
	 * @param allDecomps All decompositions of analysis
	 * @param allCoeffs All coefficients for components
	 * @param AUCs AUCs for both modalities and difference
	 */
	public void writeSummaryFile(String saveDirectory, String summaryTitle,
			String filename, double[][][] allDecomps, double[][][] allCoeffs,
			double[] AUCs) {
		try {
			if (saveDirectory == null || saveDirectory.equals("")) {
				System.out.println("No save directory specified.");
			} else {
				System.out.println("filename: " + filename);
				File file = new File(saveDirectory + "/" + filename + ".txt");
				if (!file.exists()) {
					file.createNewFile();
				}
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write(summaryTitle + "\n\n");
				bw.write("AUC1: " + AUCs[0] + " AUC2: " + AUCs[1]
						+ " AUC1-AUC2: " + AUCs[2] + "\n\n");

				double[][] BDGTab = DBRecord.getBDGTab(3, allDecomps[0],
						allCoeffs[0]);
				double[][] BCKTab = DBRecord.getBCKTab(3, allDecomps[1],
						allCoeffs[1]);
				double[][] DBMTab0 = DBRecord.getDBMTab(0, allDecomps[2],
						allCoeffs[2]);
				double[][] DBMTab1 = DBRecord.getDBMTab(0, allDecomps[2],
						allCoeffs[2]);
				double[][] DBMTabDiff = DBRecord.getDBMTab(0, allDecomps[2],
						allCoeffs[2]);
				double[][] ORTab0 = DBRecord.getORTab(0, allDecomps[2],
						allCoeffs[2]);
				double[][] ORTab1 = DBRecord.getORTab(0, allDecomps[2],
						allCoeffs[2]);
				double[][] ORTabDiff = DBRecord.getORTab(0, allDecomps[2],
						allCoeffs[2]);
				double[][] MSTab0 = DBRecord.getMSTab(0, allDecomps[2],
						allCoeffs[2]);
				double[][] MSTab1 = DBRecord.getMSTab(0, allDecomps[2],
						allCoeffs[2]);
				double[][] MSTabDiff = DBRecord.getMSTab(0, allDecomps[2],
						allCoeffs[2]);

				bw.write("BDG:\n");

				bw.write("components M0: \t\t");
				for (int i = 0; i < 8; i++) {
					bw.write(threeDecE.format(BDGTab[0][i]) + "\t");
				}
				bw.write("\n");
				bw.write("coefficients M0: \t");
				for (int i = 0; i < 8; i++) {
					bw.write(threeDecE.format(BDGTab[1][i]) + "\t");
				}
				bw.write("\n");
				bw.write("components M1: \t\t");
				for (int i = 0; i < 8; i++) {
					bw.write(threeDecE.format(BDGTab[2][i]) + "\t");
				}
				bw.write("\n");
				bw.write("coefficients M1: \t");
				for (int i = 0; i < 8; i++) {
					bw.write(threeDecE.format(BDGTab[3][i]) + "\t");
				}
				bw.write("\n");
				bw.write("product M0,M1: \t\t");
				for (int i = 0; i < 8; i++) {
					bw.write(threeDecE.format(BDGTab[4][i]) + "\t");
				}
				bw.write("\n");
				bw.write("2*coeff M0-M1: \t\t");
				for (int i = 0; i < 8; i++) {
					bw.write(threeDecE.format(BDGTab[5][i]) + "\t");
				}
				bw.write("\n");
				bw.write("total: \t\t\t");
				for (int i = 0; i < 8; i++) {
					bw.write(threeDecE.format(BDGTab[6][i]) + "\t");
				}
				bw.write("\n\n");

				bw.write("BCK:\n");
				bw.write("components M0: \t\t");
				for (int i = 0; i < 7; i++) {
					bw.write(threeDecE.format(BCKTab[0][i]) + "\t");
				}
				bw.write("\n");
				bw.write("coefficients M0: \t");
				for (int i = 0; i < 7; i++) {
					bw.write(threeDecE.format(BCKTab[1][i]) + "\t");
				}
				bw.write("\n");
				bw.write("components M1: \t\t");
				for (int i = 0; i < 7; i++) {
					bw.write(threeDecE.format(BCKTab[2][i]) + "\t");
				}
				bw.write("\n");
				bw.write("coefficients M1: \t");
				for (int i = 0; i < 7; i++) {
					bw.write(threeDecE.format(BCKTab[3][i]) + "\t");
				}
				bw.write("\n");
				bw.write("product M0,M1: \t\t");
				for (int i = 0; i < 7; i++) {
					bw.write(threeDecE.format(BCKTab[4][i]) + "\t");
				}
				bw.write("\n");
				bw.write("2*coeff M0-M1: \t\t");
				for (int i = 0; i < 7; i++) {
					bw.write(threeDecE.format(BCKTab[5][i]) + "\t");
				}
				bw.write("\n");
				bw.write("total: \t\t\t");
				for (int i = 0; i < 7; i++) {
					bw.write(threeDecE.format(BCKTab[6][i]) + "\t");
				}
				bw.write("\n\n");

				bw.write("DBM Modality 0: \n");
				bw.write("components: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(DBMTab0[0][i]) + "\t");
				}
				bw.write("\n");
				bw.write("coefficients: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(DBMTab0[1][i]) + "\t");
				}
				bw.write("\n");
				bw.write("total: \t\t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(DBMTab0[2][i]) + "\t");
				}
				bw.write("\n\n");

				bw.write("DBM Modality 1: \n");
				bw.write("components: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(DBMTab1[0][i]) + "\t");
				}
				bw.write("\n");
				bw.write("coefficients: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(DBMTab1[1][i]) + "\t");
				}
				bw.write("\n");
				bw.write("total: \t\t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(DBMTab1[2][i]) + "\t");
				}
				bw.write("\n\n");

				bw.write("DBM Difference: \n");
				bw.write("components: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(DBMTabDiff[0][i]) + "\t");
				}
				bw.write("\n");
				bw.write("coefficients: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(DBMTabDiff[1][i]) + "\t");
				}
				bw.write("\n");
				bw.write("total: \t\t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(DBMTabDiff[2][i]) + "\t");
				}
				bw.write("\n\n");

				bw.write("OR Modality 0: \n");
				bw.write("components: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(ORTab0[0][i]) + "\t");
				}
				bw.write("\n");
				bw.write("coefficients: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(ORTab0[1][i]) + "\t");
				}
				bw.write("\n");
				bw.write("total: \t\t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(ORTab0[2][i]) + "\t");
				}
				bw.write("\n\n");

				bw.write("OR Modality 1: \n");
				bw.write("components: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(ORTab1[0][i]) + "\t");
				}
				bw.write("\n");
				bw.write("coefficients: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(ORTab1[1][i]) + "\t");
				}
				bw.write("\n");
				bw.write("total: \t\t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(ORTab1[2][i]) + "\t");
				}
				bw.write("\n\n");

				bw.write("OR Difference: \n");
				bw.write("components: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(ORTabDiff[0][i]) + "\t");
				}
				bw.write("\n");
				bw.write("coefficients: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(ORTabDiff[1][i]) + "\t");
				}
				bw.write("\n");
				bw.write("total: \t\t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(ORTabDiff[2][i]) + "\t");
				}
				bw.write("\n\n");

				bw.write("MS Modality 0: \n");
				bw.write("components: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(MSTab0[0][i]) + "\t");
				}
				bw.write("\n");
				bw.write("coefficients: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(MSTab0[1][i]) + "\t");
				}
				bw.write("\n");
				bw.write("total: \t\t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(MSTab0[2][i]) + "\t");
				}
				bw.write("\n\n");

				bw.write("MS Modality 1: \n");
				bw.write("components: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(MSTab1[0][i]) + "\t");
				}
				bw.write("\n");
				bw.write("coefficients: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(MSTab1[1][i]) + "\t");
				}
				bw.write("\n");
				bw.write("total: \t\t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(MSTab1[2][i]) + "\t");
				}
				bw.write("\n\n");

				bw.write("MS Difference: \n");
				bw.write("components: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(MSTabDiff[0][i]) + "\t");
				}
				bw.write("\n");
				bw.write("coefficients: \t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(MSTabDiff[1][i]) + "\t");
				}
				bw.write("\n");
				bw.write("total: \t\t");
				for (int i = 0; i < 6; i++) {
					bw.write(threeDecE.format(MSTabDiff[2][i]) + "\t");
				}
				bw.write("\n\n");

				bw.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a "Tabbed Pane" table consisting of multiple tabs for each
	 * decomposition. Populates the tables with their values.
	 * 
	 * @param allDecomps Contains all the individual decompositions of the
	 *            components of variance
	 * @param allCoeffs Contains the coefficients for the decompositions
	 * @return Tabbed Pane filled in with data
	 */
	public JTabbedPane makeTableTabs(double[][][] allDecomps,
			double[][][] allCoeffs) {
		JTabbedPane tabTables = new JTabbedPane();
		JComponent BDGpane = makeTablePane(new String[] { "", "M1", "M2", "M3",
				"M4", "M5", "M6", "M7", "M8" });
		JComponent BCKpane = makeTablePane(new String[] { "", "N", "D", "N~D",
				"R", "N~R", "D~R", "R~N~D" });
		JComponent DBMpane = makeTablePane(new String[] { "", "R", "C", "R~C",
				"T~R", "T~C", "T~R~C" });
		JComponent ORpane = makeTablePane(new String[] { "", "R", "TR", "COV1",
				"COV2", "COV3", "ERROR" });
		JComponent MSpane = makeTablePane(new String[] { "", "R", "C", "RC",
				"MR", "MC", "MRC" });
		tabTables.addTab("BDG", BDGpane);
		tabTables.addTab("BCK", BCKpane);
		tabTables.addTab("DBM", DBMpane);
		tabTables.addTab("OR", ORpane);
		tabTables.addTab("MS", MSpane);
		updateBDGpane(BDGpane, 0, allDecomps[0], allCoeffs[0]);
		updateBCKpane(BCKpane, 0, allDecomps[1], allCoeffs[1]);
// TODO
//		updateDBMpane(DBMpane, 0, allDecomps[2], allCoeffs[2]);
//		updateORpane(ORpane, 0, allDecomps[3], allCoeffs[3]);
//		updateMSpane(MSpane, 0, allDecomps[4], allCoeffs[4]);

		return tabTables;
	}

	/**
	 * Make an individual pane for a tabbed table
	 * 
	 * @param colNames Names for the columns of the table (Moment names)
	 * @return Component containing all the individual components for a pane
	 */
	private JComponent makeTablePane(String[] colNames) {
		JPanel thePane = new JPanel(false);
		Object[][] theData = new Object[3][colNames.length];
		JTable table = new JTable(theData, colNames);
		table.setValueAt("components", 0, 0);
		table.setValueAt("coeff", 1, 0);
		table.setValueAt("total", 2, 0);
		thePane.setLayout(new BorderLayout());
		thePane.add(table.getTableHeader(), BorderLayout.PAGE_START);
		thePane.add(table);
		thePane.add(new JLabel("sqrt(Var) = "), BorderLayout.EAST);
		return thePane;
	}

	/**
	 * Change the values in the BDG pane of a tabbed table
	 * 
	 * @param BDGpane Swing component containing the BDG pane
	 * @param mod Modality being used
	 * @param BDG BDG decomposition
	 * @param BDGcoeff Coefficient for BDG decomposition
	 */
	private void updateBDGpane(JComponent BDGpane, int mod, double[][] BDG,
			double[][] BDGcoeff) {
		
		JTable table = (JTable) BDGpane.getComponent(1);
		JLabel varLabel = (JLabel) BDGpane.getComponent(2);
		double[][] BDGdata = new double[7][8];
		double[][] tempBDGTab = DBRecord.getBDGTab(mod, BDG, BDGcoeff);
		String output1 = "sqrt(Var) = ";
		
		if (mod == 0) {
			BDGdata[0] = tempBDGTab[0];
			BDGdata[1] = tempBDGTab[1];
		} else if (mod == 1) {
			BDGdata[0] = tempBDGTab[2];
			BDGdata[1] = tempBDGTab[3];
		} else if (mod == 3) {
			BDGdata[0] = tempBDGTab[4];
			BDGdata[1] = Matrix.scaleVector(tempBDGTab[5], 0.5);
			output1 = "sqrt(Cov) = ";
		}

		double currVar = 0.0;
		for (int j = 0; j < 8; j++) {
			BDGdata[2][j] = BDGdata[0][j]*BDGdata[1][j];
			currVar = currVar + BDGdata[2][j];
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 8; j++) {
				table.setValueAt(threeDecOptE.format(BDGdata[i][j]), i, j + 1);
			}
		}
		String output = threeDec.format(Math.sqrt(currVar));
		varLabel.setText(output1 + output);
	}

	/**
	 * Change the values in the BCK pane of a tabbed table
	 * 
	 * @param BCKpane Swing component containing the BCK pane
	 * @param mod Modality being used
	 * @param BCK BCK decomposition
	 * @param BCKcoeff Coefficient for BCK decomposition
	 */
	private void updateBCKpane(JComponent BCKpane, int mod, double[][] BCK,
			double[][] BCKcoeff) {
		JTable table = (JTable) BCKpane.getComponent(1);
		JLabel varLabel = (JLabel) BCKpane.getComponent(2);
		double[][] BCKdata = new double[3][7];
		double[][] tempBCKTab = DBRecord.getBCKTab(mod, BCK, BCKcoeff);
		String output1 = "sqrt(Var) = ";
		
		if (mod == 0) {
			BCKdata[0] = tempBCKTab[0];
			BCKdata[1] = tempBCKTab[1];
		} else if (mod == 1) {
			BCKdata[0] = tempBCKTab[2];
			BCKdata[1] = tempBCKTab[3];
		} else if (mod == 3) {
			BCKdata[0] = tempBCKTab[4];
			BCKdata[1] = Matrix.scaleVector(tempBCKTab[5], 0.5);
			output1 = "sqrt(Cov) = ";
		}

		double currVar = 0.0;
		for (int j = 0; j < 7; j++) {
			BCKdata[2][j] = BCKdata[0][j]*BCKdata[1][j];
			currVar = currVar + BCKdata[2][j];
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 7; j++) {
				table.setValueAt(threeDecOptE.format(BCKdata[i][j]), i, j + 1);
			}
		}
		String output = threeDec.format(Math.sqrt(currVar));
		varLabel.setText(output1 + output);
		
//		BCKdata[2] = tempBCKTab[6];
//		double currVar = Matrix.total(tempBCKTab[6]);
//		for (int i = 0; i < 3; i++) {
//			for (int j = 0; j < 7; j++) {
//				table.setValueAt(threeDecOptE.format(BCKdata[i][j]), i, j + 1);
//			}
//		}
//		String output = threeDec.format(Math.sqrt(currVar));
//		varLabel.setText(output1 + output);
	}

	/**
	 * Change the values in the DBM pane of a tabbed table
	 * 
	 * @param DBMpane Swing component containing the DBM pane
	 * @param mod Modality being used
	 * @param DBM DBM decomposition
	 * @param DBMcoeff Coefficient for DBM decomposition
	 */
	private void updateDBMpane(JComponent DBMpane, int mod, double[][] DBM,
			double[][] DBMcoeff) {
		JTable table = (JTable) DBMpane.getComponent(1);
		JLabel varLabel = (JLabel) DBMpane.getComponent(2);
		double[][] DBMdata = DBRecord.getDBMTab(mod, DBM, DBMcoeff);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 6; j++) {
				table.setValueAt(threeDecOptE.format(DBMdata[i][j]), i, j + 1);
			}
		}
		double currVar = Matrix.total(DBMdata[2]);
		String output = threeDec.format(Math.sqrt(currVar));
		varLabel.setText("sqrt(Var) = " + output);
	}

	/**
	 * Change the values in the OR pane of a tabbed table
	 * 
	 * @param ORpane Swing component containing the BDG pane
	 * @param mod Modality being used
	 * @param OR OR decomposition
	 * @param ORcoeff Coefficient for OR decomposition
	 */
	private void updateORpane(JComponent ORpane, int mod, double[][] OR,
			double[][] ORcoeff) {
		JTable table = (JTable) ORpane.getComponent(1);
		JLabel varLabel = (JLabel) ORpane.getComponent(2);
		double[][] ORdata = DBRecord.getORTab(mod, OR, ORcoeff);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 6; j++) {
				table.setValueAt(threeDecOptE.format(ORdata[i][j]), i, j + 1);
			}
		}
		double currVar = Matrix.total(ORdata[2]);
		String output = threeDec.format(Math.sqrt(currVar));
		varLabel.setText("sqrt(Var) = " + output);
	}

	/**
	 * Change the values in the MS pane of a tabbed table
	 * 
	 * @param MSpane Swing component containing the MS pane
	 * @param mod Modality being used
	 * @param MS MS decomposition
	 * @param MScoeff Coefficient for MS decomposition
	 */
	private void updateMSpane(JComponent MSpane, int mod, double[][] MS,
			double[][] MScoeff) {
		JTable table = (JTable) MSpane.getComponent(1);
		JLabel varLabel = (JLabel) MSpane.getComponent(2);
		double[][] MSdata = DBRecord.getMSTab(mod, MS, MScoeff);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 6; j++) {
				table.setValueAt(threeDecOptE.format(MSdata[i][j]), i, j + 1);
			}
		}
		double currVar = Matrix.total(MSdata[2]);
		String output = threeDec.format(Math.sqrt(currVar));
		varLabel.setText("sqrt(Var) = " + output);
	}
}
