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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import mrmc.core.DBRecord;
import mrmc.core.InputFile;
import mrmc.core.MRMC;
import mrmc.core.Matrix;
import mrmc.core.StatTest;
import mrmc.gui.GUInterface;
import mrmc.gui.SizePanel;
import mrmc.gui.StatPanel;
import roemetz.core.CalcGenRoeMetz;
import roemetz.core.RoeMetz;
import roemetz.core.SimRoeMetz;
import umontreal.iro.lecuyer.rng.RandomStream;
import umontreal.iro.lecuyer.rng.WELL1024;

/**
 * This class describes the interface for iRoeMetz application. It contains a
 * panel for inputting means, components of variance, and experiment size. The
 * next panel performs multiple simulation experiments based on the input. The
 * last panel estimates the components of variance for the given input.
 * 
 * Note: In order to debug properly, you do not want to be running in multi-thread mode.
 * Change the numCores in class DoSimBtnListener to 1
 * 
 * @author Rohan Pathare
 */
public class RMGUInterface {


	private final int USE_MLE = 1;
	private final int NO_MLE = 0;
	private static RoeMetz RoeMetz1;
	private File outputDirectory = null;
	SizePanel SizePanelRoeMetz;
	JPanel studyDesignJPanel;

	/**
	 * Input means
	 */
	JTextField 
		mu0 = new JTextField("1.0", 6),
		mu1 = new JTextField("1.0", 6);
	/**
	 * Input variances invariant to modality
	 */
	JTextField 
		v_R0 = new JTextField("0.166", 6),
		v_C0 = new JTextField("0.166", 6),
		v_RC0 = new JTextField("0.166", 6),
		v_R1 = new JTextField("0.166", 6),
		v_C1 = new JTextField("0.166", 6),
		v_RC1 = new JTextField("0.166", 6);
	/** 
	 * Input variances specific to modality A
	 */
	JTextField 
		v_AR0 = new JTextField("0.166", 6),
		v_AC0 = new JTextField("0.166", 6),
		v_ARC0 = new JTextField("0.166", 6),
		v_AR1 = new JTextField("0.166", 6),
		v_AC1 = new JTextField("0.166", 6),
		v_ARC1 = new JTextField("0.166", 6);
	/**
	 * Input variances specific to modality B
	 */
	JTextField
		v_BR0 = new JTextField("0.166", 6),
		v_BC0 = new JTextField("0.166", 6),
		v_BRC0 = new JTextField("0.166", 6),
		v_BR1 = new JTextField("0.166", 6),
		v_BC1 = new JTextField("0.166", 6),
		v_BRC1 = new JTextField("0.166", 6);
	/**
	 * Input experiment size
	 */
	JTextField
		NnormalJTextField = new JTextField("20", 6),
		NdiseaseJTextField = new JTextField("20", 6),
		NreaderJTextField = new JTextField("4", 6);

	/**
	 * Number of experiments
	 */
	JTextField JTextField_Nexp = new JTextField("10",7);
	/**
	 * Initial seed for RNG
	 */
	JTextField JTextField_seed;
	
	private JDialog progDialog;
	private JCheckBox useMLEbox = new JCheckBox("Use MLE?");
	@SuppressWarnings("unused")
	private int useMLE = NO_MLE;
	private String simSaveDirectory;
	private static JProgressBar simProgress;
	public String calcSaveDirectory;
	public DecimalFormat threeDecOpt = new DecimalFormat("0.###");
	public DecimalFormat threeDecOptE = new DecimalFormat("0.###E0");
	public DecimalFormat threeDec = new DecimalFormat("0.000");
	public DecimalFormat threeDecE = new DecimalFormat("0.000E0");
	public DecimalFormat fiveDecOpt = new DecimalFormat( "0.#####");
	public DecimalFormat fiveDecOptE = new DecimalFormat("0.#####E0");
	public DecimalFormat fiveDec = new DecimalFormat( "0.00000");
	public DecimalFormat fiveDecE = new DecimalFormat("0.00000E0");

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
	 * Sole constructor for GUI, only invoked by RoeMetz driver class
	 * 
	 * @param tempRoeMetz Application driver class
	 * @param cp Content pane of the application
	 */
	public RMGUInterface(RoeMetz tempRoeMetz, Container cp) {
		RoeMetz1 = tempRoeMetz;
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

		/*
		 * Panel(top-bottom) to handle CofV inputs
		 */
		JPanel cofvInputPanel = new JPanel();
		cofvInputPanel.setLayout(new BoxLayout(cofvInputPanel, BoxLayout.Y_AXIS));

		/*
		 * cofvInputPanel rows 1&2 panel(left-right) for means
		 */
		JPanel meansJPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		meansJPanel.add(new JLabel("Input Means: "));
		//
		JPanel meansFields = new JPanel(new FlowLayout(FlowLayout.LEFT));
		meansFields.add(Box.createHorizontalStrut(20));
		meansFields.add(new JLabel("\u00B5_A: "));
		meansFields.add(mu0);
		meansFields.add(new JLabel("\u00B5_B: "));
		meansFields.add(mu1);

		/*
		 * cofvInputPanel rows 3&4 panel(left-right) for variances invariant to modality
		 */
		JPanel varJPanel0 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		varJPanel0.add(new JLabel("Input Variances Invariant to Modality: "));
		//
		JPanel varFieldsO = new JPanel(new FlowLayout(FlowLayout.LEFT));
		varFieldsO.add(Box.createHorizontalStrut(20));
		varFieldsO.add(new JLabel("  v_R0: "));
		varFieldsO.add(v_R0);
		varFieldsO.add(new JLabel("  v_C0: "));
		varFieldsO.add(v_C0);
		varFieldsO.add(new JLabel("  v_RC0: "));
		varFieldsO.add(v_RC0);
		varFieldsO.add(new JLabel("  v_R1: "));
		varFieldsO.add(v_R1);
		varFieldsO.add(new JLabel("  v_C1: "));
		varFieldsO.add(v_C1);
		varFieldsO.add(new JLabel("  v_RC1: "));
		varFieldsO.add(v_RC1);

		/*
		 * cofvInputPanel rows 5&6 panel(left-right) for variances specific to modality A
		 */
		JPanel varJPanelA = new JPanel(new FlowLayout(FlowLayout.LEFT));
		varJPanelA.add(new JLabel("Input Variances Specific to Modality A: "));
		//
		JPanel varFieldsA = new JPanel(new FlowLayout(FlowLayout.LEFT));
		varFieldsA.add(Box.createHorizontalStrut(20));
		varFieldsA.add(new JLabel("v_AR0: "));
		varFieldsA.add(v_AR0);
		varFieldsA.add(new JLabel("v_AC0: "));
		varFieldsA.add(v_AC0);
		varFieldsA.add(new JLabel("v_ARC0: "));
		varFieldsA.add(v_ARC0);
		varFieldsA.add(new JLabel("v_AR1: "));
		varFieldsA.add(v_AR1);
		varFieldsA.add(new JLabel("v_AC1: "));
		varFieldsA.add(v_AC1);
		varFieldsA.add(new JLabel("v_ARC1: "));
		varFieldsA.add(v_ARC1);

		/*
		 * cofvInputPanel rows 7&8 panel(left-right) for variances specific to modality B
		 */
		JPanel varJPanelB = new JPanel(new FlowLayout(FlowLayout.LEFT));
		varJPanelB.add(new JLabel("Input Variances Specific to Modality B: "));
		//
		JPanel varFieldsB = new JPanel(new FlowLayout(FlowLayout.LEFT));
		varFieldsB.add(Box.createHorizontalStrut(20));
		varFieldsB.add(new JLabel("v_BR0: "));
		varFieldsB.add(v_BR0);
		varFieldsB.add(new JLabel("v_BC0: "));
		varFieldsB.add(v_BC0);
		varFieldsB.add(new JLabel("v_BRC0: "));
		varFieldsB.add(v_BRC0);
		varFieldsB.add(new JLabel("v_BR1: "));
		varFieldsB.add(v_BR1);
		varFieldsB.add(new JLabel("v_BC1: "));
		varFieldsB.add(v_BC1);
		varFieldsB.add(new JLabel("v_BRC1: "));
		varFieldsB.add(v_BRC1);

		/*
		 * cofvInputPanel rows 9&10 panel(left-right) for experiment size
		 */
		JPanel sizeJPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sizeJPanel.add(new JLabel("Input Experiment Size: "));
		//
		JPanel sizeFields = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sizeFields.add(Box.createHorizontalStrut(20));
		sizeFields.add(new JLabel("N0: "));
		sizeFields.add(NnormalJTextField);
		sizeFields.add(new JLabel("N1: "));
		sizeFields.add(NdiseaseJTextField);
		sizeFields.add(new JLabel("Nr: "));
		sizeFields.add(NreaderJTextField);

		/*
		 * cofvInputPanel row 11 panel(left-right) of study design inputs
		 */
		SizePanelRoeMetz = new SizePanel();
		studyDesignJPanel = SizePanelRoeMetz.setStudyDesign();
		
		/*
		 * cofvInputPanel row 12 panel(left-right) of buttons controlling input fields
		 */
		JPanel populateFields = new JPanel(new FlowLayout(FlowLayout.LEFT));
		//
		JButton clearButton = new JButton("Clear Fields");
		clearButton.addActionListener(new ClearBtnListener());
		JButton popFromFile = new JButton("Populate Components from File");
		popFromFile.addActionListener(new PopFromFileListener());
		JButton saveFields = new JButton("Save Components to File");
		saveFields.addActionListener(new SaveFieldsListener());
		//
		populateFields.add(Box.createHorizontalStrut(20));
		populateFields.add(clearButton);
		populateFields.add(popFromFile);
		populateFields.add(saveFields);

		/*
		 * Add rows to cofvInputPanel
		 */
		cofvInputPanel.add(meansJPanel);
		cofvInputPanel.add(meansFields);
		cofvInputPanel.add(varJPanel0);
		cofvInputPanel.add(varFieldsO);
		cofvInputPanel.add(varJPanelA);
		cofvInputPanel.add(varFieldsA);
		cofvInputPanel.add(varJPanelB);
		cofvInputPanel.add(varFieldsB);
		cofvInputPanel.add(sizeJPanel);
		cofvInputPanel.add(sizeFields);
		cofvInputPanel.add(studyDesignJPanel);
		cofvInputPanel.add(populateFields);

		/*
		 * Panel to perform simulation experiment
		 */
		JPanel simExpPanel = new JPanel();
		simExpPanel.setLayout(new BoxLayout(simExpPanel, BoxLayout.Y_AXIS));

		/*
		 * simExpPanel row 1 panel within simExpPanel to describe simExpPanel
		 */
		JPanel simExpDesc = new JPanel(new FlowLayout(FlowLayout.LEFT));
		simExpDesc.add(new JLabel("Simulation Experiments:"));

		/*
		 * simExpPanel row 2 panel: Seed for RNG, Number of experiments, MLE box
		 */
		JPanel simExp = new JPanel(new FlowLayout(FlowLayout.LEFT));
		simExp.add(Box.createHorizontalStrut(20));

		// Seed for RNG
		JLabel seedLabel = new JLabel("Seed for RNG");
		simExp.add(seedLabel);
		//
		JTextField_seed = new JTextField(10);
		JTextField_seed.addFocusListener(new SeedInputListener());
		simExp.add(JTextField_seed);
		// initialize seed: 9 digits or less, so that it can be converted to an integer
		String String_seed = Long.toString(System.currentTimeMillis());
		if(String_seed.length() > 9){
			String_seed = String_seed.substring(String_seed.length()-9, String_seed.length());
		}
		JTextField_seed.setText(String_seed);

		// Number of experiments
		JLabel NexpLabel = new JLabel("# of Experiments");
		//
		simExp.add(NexpLabel);
		//
		simExp.add(JTextField_Nexp);

		// MLE box
		useMLEbox.addItemListener(new UseMLEListener());
		//
		simExp.add(useMLEbox);

		/*
		 *  simExpPanel row 3: Output location button, Perform simulation button
		 */
		JPanel simExpButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		simExpButtons.add(Box.createHorizontalStrut(20));

		// Output location button
		JButton saveLoc = new JButton("Output Location");
		saveLoc.addActionListener(new SaveSimulationListener());
		simExpButtons.add(saveLoc);

		// Perform simulation button
		JButton doSimExp = new JButton("Perform Simulation Experiments");
		doSimExp.addActionListener(new DoSimBtnListener());
		simExpButtons.add(doSimExp);

		/*
		 *  Add rows to simExpPanel
		 */
		simExpPanel.add(simExpDesc);
		simExpPanel.add(simExp);
		simExpPanel.add(simExpButtons);

		/*
		 * Panel to calculate moments/components of variance
		 */
		JPanel calculatePanel = new JPanel();
		calculatePanel.setLayout(new BoxLayout(calculatePanel, BoxLayout.Y_AXIS));

		/*
		 * Panel within calculatePanel to describe function
		 */
		JPanel cofvResultsDesc = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel cofvLabel = new JLabel("Calculate Components of Variance by Numerical Integration:");
		cofvResultsDesc.add(cofvLabel);

		/*
		 * Panel within calculatePanel to display calc Results
		 */
		JPanel cofvResults = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JButton saveCalcResults = new JButton("Output Location");
		saveCalcResults.addActionListener(new saveCalcResultsListener());
		JButton doGenRoeMetz = new JButton("Do Numerical Integration");
		doGenRoeMetz.addActionListener(new DoNumericalIntegrationBtnListener());

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
		NnormalJTextField.setText("");
		NdiseaseJTextField.setText("");
		NreaderJTextField.setText("");
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
				NnormalJTextField.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("N1:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				NdiseaseJTextField.setText(tempstr.substring(tmploc + 1).trim());
				continue;
			}
			loc = tempstr.indexOf("NR:");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				NreaderJTextField.setText(tempstr.substring(tmploc + 1).trim());
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
					bw.write("n0: " + NnormalJTextField.getText() + "\n");
					bw.write("n1: " + NdiseaseJTextField.getText() + "\n");
					bw.write("nr: " + NreaderJTextField.getText() + "\n");
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


	
	/**
	 * extends SwingWorker, Performs multiple consecutive simulation experiments in a separate
	 * thread. Enables multi-threading to take advantage of multi-core systems
	 * and keep GUI responsive during heavy calculations.
	 * 
	 */
	private class SimExperiments_thread extends SwingWorker<DBRecord[], Integer> {

		double[] u; // experiment means
		double[] var_t; // components of variance
		SizePanel sizePanel1;
		
		/**
		 * number of experiments per thead
		 */
		long Nexp, NexpStart, NexpEnd, NexpThisCore;
		/**
		 * progress indicating number of completed experiments,
		 *  updated by all threads via "atomic"
		 */
		AtomicInteger NexpCompleted_atomic;
		String filenameTime;
		@SuppressWarnings("unused")
		int whichTask;
		private RandomStream RandomStreamI;
		
		/**
		 * Constructor for group of simulation experiments.
		 * 
		 * @param u Contains experiment means
		 * @param var_t Contains components of variance
		 * @param Nexp Number of simulation experiments
		 * @param NexpStart The starting index of the simulation experiment for this core
		 * @param NexpEnd The ending index of the simulation experiment for this core 
		 * @param NexpCompleted_atomic Shared counter of number of experiments performed
		 *            across all threads
		 * @param filenameTime Timestamp when this set of experiments was
		 *            started, to categorize output files
		 * @param whichTask Indentifier for this group of simulation experiments
		 *            (this thread)
		 * @param RandomStreamI Random numbergenerator at its current state
		 * @param sizePanelTemp Contains parameters for the mrmc experiment to be simulated
		 */
		public SimExperiments_thread(double[] u, double[] var_t,
				long Nexp, long NexpStart, long NexpEnd, AtomicInteger NexpCompleted_atomic, String filenameTime,
				int whichTask, RandomStream RandomStreamI, SizePanel sizePanelTemp) {
			
			sizePanel1 = sizePanelTemp;
			
			this.u = u;
			this.var_t = var_t;
			this.Nexp = Nexp;
			this.NexpStart = NexpStart;
			this.NexpEnd = NexpEnd;
			this.NexpThisCore = NexpEnd-NexpStart;
			this.NexpCompleted_atomic = NexpCompleted_atomic;
			this.filenameTime = filenameTime;
			this.whichTask = whichTask;
			this.RandomStreamI = RandomStreamI;
			
			System.out.print("ThreadName:"+Thread.currentThread().getName()+":");
			//each thread prints 5 random numbers for testing proper threading and reproducibility
	        for(int j = 0 ; j < 5; j++) {
	            int nextInt = RandomStreamI.nextInt(1, 100);
	            System.out.print(nextInt + ",");
	        }
	        System.out.println();

		}

		/**
		 * Actual task that is performed
		 * 
		 * @return Averaged components of variance decompositions and AUCs for
		 *         this group of simulation experiments
		 * @throws IOException 
		 */
		public DBRecord[] doInBackground() throws IOException {

			DBRecord DBRecordStat = new DBRecord();
			DBRecord squareDBRecordStat = new DBRecord();
			DBRecord sumDBRecordStat = new DBRecord();
			DBRecord sumSquareDBRecordStat = new DBRecord();
			DBRecordStat.verbose = false;
			sumDBRecordStat.verbose = false;
			squareDBRecordStat.verbose = false;
			sumSquareDBRecordStat.verbose = false;
			
			long flagTotalVarIsNegative = 0;

			SimRoeMetz currSimRoeMetz = new SimRoeMetz(u, var_t, RandomStreamI, sizePanel1);
			
			// initialize DBRecords
			currSimRoeMetz.doSim(squareDBRecordStat);
			currSimRoeMetz.doSim(sumDBRecordStat);
			currSimRoeMetz.doSim(sumSquareDBRecordStat);

			// for i=NexpStart
			currSimRoeMetz.doSim(DBRecordStat);
			// Accumulate DBRecord
			DBRecord.copy(DBRecordStat, sumDBRecordStat);
			// Accumulate squareDBRecord
			DBRecord.copy(DBRecordStat, squareDBRecordStat);			
			DBRecord.square(squareDBRecordStat);
			DBRecord.copy(squareDBRecordStat, sumSquareDBRecordStat);
			// write to disk
			if (simSaveDirectory != null && !simSaveDirectory.equals("")) {
				writeInputFile(sumDBRecordStat, filenameTime, NexpStart);
				writeOutputFile(sumDBRecordStat, filenameTime, NexpStart);
			}
			
			// continue the loop, add the simulation results to avgDBRecordStat
			for (long i = NexpStart+1; i < NexpEnd; i++) {

				// Sends data chunks to the "process" method.
				// Below, the process method updates the progress bar.
				publish(NexpCompleted_atomic.incrementAndGet());

				// SwingWorker supports bound properties, 
				// which are useful for communicating with other threads.
				// Two bound properties are predefined: progress and state.
				// As with all bound properties, progress and state can be used 
				// to trigger event-handling tasks on the event dispatch thread.
				// The progress bound variable is an int value that can range from 0 to 100.
				// It has a predefined setter method (the protected SwingWorker.setProgress)
				// and a predefined getter method (the public SwingWorker.getProgress).
				setProgress((int) (100 * (i-NexpStart) / NexpThisCore));
				
				currSimRoeMetz.doSim(DBRecordStat);
				
				// Check if DBRecordStat.totalVar < 0
				// Keep track of how often this happens
				// Replace current simulation with a new simulation
				if(DBRecordStat.totalVar < 0) {
					flagTotalVarIsNegative++;
					continue;
				}
				
				// Accumulate DBRecord
				DBRecord.add(DBRecordStat, sumDBRecordStat);
				// Accumulate squareDBRecord
				DBRecord.copy(DBRecordStat, squareDBRecordStat);
				DBRecord.square(squareDBRecordStat);
				DBRecord.add(squareDBRecordStat, sumSquareDBRecordStat);
				// write to disk
				if (simSaveDirectory != null && !simSaveDirectory.equals("")) {
					writeInputFile(DBRecordStat, filenameTime, i);
					writeOutputFile(DBRecordStat, filenameTime, i);
				}
				if(DBRecordStat.verbose) {
					System.out.print("ThreadName:"+Thread.currentThread().getName()+":");
					System.out.print(i+1 + " of " + Nexp + " completed\n");
				}

			}

			sumDBRecordStat.flagTotalVarIsNegative = flagTotalVarIsNegative;
			
			/**
			 * Return array of DBRecords! 
			 */
			DBRecord[] currDBRecord = new DBRecord[4];
			currDBRecord[0] = DBRecordStat;
			currDBRecord[1] = sumDBRecordStat;
			currDBRecord[2] = squareDBRecordStat;
			currDBRecord[3] = sumSquareDBRecordStat;
			return currDBRecord;
			
		}

		/**
		 * Move the progress bar along.
		 */
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
//		final int numCores = Runtime.getRuntime().availableProcessors();
		final int numCores = 1;
		int numCoresToUse;
		DBRecord[][] results = new DBRecord[numCores][2];

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				
				SizePanelRoeMetz.NreaderJTextField = NreaderJTextField;
				SizePanelRoeMetz.NnormalJTextField = NnormalJTextField;
				SizePanelRoeMetz.NdiseaseJTextField = NdiseaseJTextField;

				String String_seed = JTextField_seed.getText();
				if(String_seed.length() > 9) {
					return;
				}
				
				double[] u = getMeans();
				double[] var_t = getVariances();

				// Get number of experiments
				long Nexp = Integer.valueOf(JTextField_Nexp.getText());
				// Determine number of cores
				if (Nexp < numCores) {
					numCoresToUse = (int) Nexp;
				} else {
					numCoresToUse = numCores;
				}
				// Determine the number of experiments per core
				long NexpPerCore = Nexp/numCoresToUse;
				// Declare the start and end of experiment index to be assigned to each core 
				long NexpStart, NexpEnd;

				// Check if saving results
				if (simSaveDirectory == null || simSaveDirectory.equals("")) {
					JOptionPane.showMessageDialog(
							RoeMetz1.getFrame(),
						"Save directory not specified.\nExperiment output files will not be written.",
						"Warning", JOptionPane.WARNING_MESSAGE);
				}

				// Create string representation of current time to use in filename
				DateFormat dateForm = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
				Date currDate = new Date();
				final String filenameTime = dateForm.format(currDate);

				/*
				 * SSJ: Simulation Stochastique in Java
				 * http://simul.iro.umontreal.ca/ssj
				 * The actual random number generators (RNGs) are provided in
				 * classes that implement this RandomStream interface. Each
				 * stream of random numbers is an object of the class that 
				 * implements this interface, and can be viewed as a virtual
				 * random number generator.
				 * ...
				 * Each time a new RandomStream is created, its starting point
				 * (initial seed) is computed automatically, Z steps ahead of
				 * the starting point of the previously created stream of the
				 * same type, and its current state is set equal to this starting point.
				 */
				int[] seedIntArr32 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 
					     10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 
					     20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31} ;
				String seedString = JTextField_seed.getText();
				seedIntArr32[0] = Integer.parseInt(seedString);
				WELL1024.setPackageSeed(seedIntArr32);
				// set seed to 234567890 to yield a sequence for 1 core where DF_BDG < 1
				// set seed to 725722555 to yield a sequence for 1 core where totalVar < 0, iter=6547

				// Create a progress bar
				final AtomicInteger NexpCompleted_atomic = new AtomicInteger(0);
				createProgressBar((int) Nexp, NexpCompleted_atomic.get());

				final SimExperiments_thread[] allTasks = new SimExperiments_thread[numCoresToUse];
		        System.out.println("******** TEST serial RNG BEG ********");
				for (int i = 0; i < numCoresToUse; i++) {
					final int taskNum = i;
					WELL1024 RandomStreamI = new WELL1024();
					
					NexpStart = NexpPerCore * i;
					NexpEnd = NexpStart + NexpPerCore;
					// Last core must do more experiments 
					//   if the number of experiments is not a factor of the number of cores
					if( i==numCoresToUse-1) {
						NexpEnd = Nexp;
					}
					
					// Create the simulation objects on available cores
					allTasks[i] = new SimExperiments_thread(
						u, var_t, Nexp, NexpStart, NexpEnd, NexpCompleted_atomic, 
						filenameTime, i, RandomStreamI, SizePanelRoeMetz);
					
					// Check to see when each task finishes and get its results
					allTasks[i].addPropertyChangeListener(
					new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("done")) {
							try {
								results[taskNum] = allTasks[taskNum].get();
								finishedTasks++;
								if (finishedTasks == numCoresToUse) {
									finishedTasks = 0;
									processResults(simSaveDirectory,filenameTime);
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e) {
								e.printStackTrace();
							}
						}
					}});
				}
				
		        System.out.println("******** TEST serial RNG END ********");
				// Run each simulation object on its own core
				for (int i = 0; i < numCoresToUse; i++) {
					allTasks[i].execute();
				}
	
			} catch (NumberFormatException e1) {
				System.out.println(e1.toString());
				JOptionPane.showMessageDialog(
						RoeMetz1.getFrame(),
					"Incorrect / Incomplete Input", "Error",
					JOptionPane.ERROR_MESSAGE);
			}
			
		}

		/**
		 * Makes a bar indicating the amount of progress over all simulation
		 * experiments
		 * 
		 * @param Nexp number of experiments
		 * @param initProgress Initial value of progress bar
		 */
		private void createProgressBar(int Nexp, int initProgress) {
			simProgress = new JProgressBar(0, Nexp);
			simProgress.setValue(initProgress);
			progDialog = new JDialog(RoeMetz1.getFrame(), "Simulation Progress");
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

			DBRecord DBRecordStat = new DBRecord();
			DBRecord avgDBRecordStat = new DBRecord();
			DBRecord squareDBRecordStat = new DBRecord();
			DBRecord avgSquareDBRecordStat = new DBRecord();
			
			// for i=0, put results in avgDBRecordStat
			DBRecordStat = results[0][0];
			avgDBRecordStat = results[0][1];
			squareDBRecordStat = results[0][2];
			avgSquareDBRecordStat = results[0][3];
			// continue the loop, add the simulation results to avgDBRecordStat
			for (int i = 1; i < numCoresToUse; i++) {
				DBRecordStat = results[i][1];
				DBRecord.add(DBRecordStat, avgDBRecordStat);
				DBRecordStat = results[i][0];
				
				squareDBRecordStat = results[i][3];
				DBRecord.add(squareDBRecordStat, avgSquareDBRecordStat);
				squareDBRecordStat = results[i][2];
			}

			if(avgDBRecordStat.flagTotalVarIsNegative > 0) {
	 			JFrame frame = new JFrame();
	 			JOptionPane.showMessageDialog(frame,
						"There were " + avgDBRecordStat.flagTotalVarIsNegative + 
						" iterations where the totalVar estimate was negative.\n" +
						"These iterations were replaced new iterations.\n" +
						"Negative estimates of totalVar are generally expected to be very rare.\n" +
						"The likelihood increases as N0, N1, and NR get small.\n" +
						"Negative estimates of totalVar are not possible when you use MLE.", "Warning",
						JOptionPane.ERROR_MESSAGE);
			}
			
 			final double Nexp = Integer.valueOf(JTextField_Nexp.getText());
			DBRecord.scale(avgDBRecordStat, 1.0/Nexp);
			DBRecord.scale(avgSquareDBRecordStat, 1.0/Nexp);

			// Calculate second order moments
			DBRecord.copy(avgSquareDBRecordStat, squareDBRecordStat);
			DBRecord.scale(squareDBRecordStat, Nexp/(Nexp-1));
			// Calculate squared means
			DBRecord.copy(avgDBRecordStat, DBRecordStat);
			DBRecord.square(DBRecordStat);
			DBRecord.scale(DBRecordStat, -Nexp/(Nexp-1));
			// Calculate variances: N/(N-1) * (avg(x^2) - avg(x)^2)
			DBRecord.add(DBRecordStat, squareDBRecordStat);
			// Rename result
			DBRecord varDBRecordStat = squareDBRecordStat;
			squareDBRecordStat = null;
			// Reset DBRecordStat: Access one MC trial
			DBRecordStat = results[0][0];
			
			avgDBRecordStat.Decompositions();
			String simulationFileName = "SimulationOutputBy" + JTextField_Nexp.getText() + "Experiments";
			StatPanel StatPanel1 = new StatPanel(RoeMetz1.getFrame(), avgDBRecordStat);
			StatPanel1.setStatPanel();
			StatPanel1.setTable1();
			StatPanel1.setMCresults(avgDBRecordStat, varDBRecordStat);
			
			JDialog simOutput = new JDialog(RoeMetz1.getFrame(), "Simulation Results");
			simOutput.add(StatPanel1.JPanelStat);
			JButton simulationExport= new JButton("Export Analysis Result");
			simulationExport.addActionListener(new analysisExportListener(avgDBRecordStat,simulationFileName));			
			simOutput.add(simulationExport, BorderLayout.PAGE_END);
			simOutput.pack();
			simOutput.setVisible(true);

//			writeSummaryFile(simSaveDirectory, "Summary of Simulation Results",
//					"results-simulation-" + filenameTime, allDecomps,
//					allCoeffs, avgdAUC);

		}
        

		/**
		 * Creates a pop-up table to display results of the simulations.
		 * 
		 * @param DBRecordStat Contains the results from the simulations 
		 */
		private void showSimOutput(DBRecord DBRecordStat) {

			JDialog simOutput = new JDialog(RoeMetz1.getFrame(),
					"Simulation Results");
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

			JPanel tablePanel = new JPanel();
			tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.X_AXIS));

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

//			JTabbedPane tabTables = makeTableTabs(DBRecordStat);

			// Display AUCs
			JLabel AUCs = new JLabel(
					"AUC1: " + threeDecOpt.format(DBRecordStat.AUCsReaderAvg[0]) + "   " + 
					"AUC2: " + threeDecOpt.format(DBRecordStat.AUCsReaderAvg[1]) + "   " +
					"AUC1-AUC2: " + threeDecOpt.format(DBRecordStat.AUCsReaderAvg[0] 
														- DBRecordStat.AUCsReaderAvg[1]) + "   ");

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
//			ModSimListener gListener = new ModSimListener(tabTables,
//					allDecomps, allCoeffs);
//			mod1SimButton.addActionListener(gListener);
//			mod2SimButton.addActionListener(gListener);
//			modDSimButton.addActionListener(gListener);

//			tablePanel.add(tabTables);
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
	private class CalculateCofV extends SwingWorker<DBRecord, Integer> {
		double[] u; // experiment means
		double[] var_t; // components of variance
		SizePanel SizePanelRoeMetz;

		public CalculateCofV(double[] u, double[] var_t, SizePanel SizePanelRoeMetz) {
			this.u = u;
			this.var_t = var_t;
			this.SizePanelRoeMetz = SizePanelRoeMetz;
		}

		/**
		 * Performs calculation of components of variance and returns their
		 * decompositions
		 */
		public DBRecord doInBackground() {
			CalcGenRoeMetz.genRoeMetz(u, var_t, SizePanelRoeMetz);
			return CalcGenRoeMetz.DBRecordNumerical;
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
	class DoNumericalIntegrationBtnListener implements ActionListener {
		DBRecord DBRecordNumerical; // averaged decompositions of cofv
		
		@Override
		public void actionPerformed(ActionEvent e) {

			SizePanelRoeMetz.NreaderJTextField = NreaderJTextField;
			SizePanelRoeMetz.NnormalJTextField = NnormalJTextField;
			SizePanelRoeMetz.NdiseaseJTextField = NdiseaseJTextField;

			System.out.println(NreaderJTextField.getText());

			try {
				double[] u = getMeans();
				double[] var_t = getVariances();
				
				final CalculateCofV calcTask = new CalculateCofV(u, var_t, SizePanelRoeMetz);
				calcTask.addPropertyChangeListener(new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("done")) {
							try {
								DBRecordNumerical = calcTask.get();
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
				JOptionPane.showMessageDialog(RoeMetz1.getFrame(),
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

			DBRecordNumerical.Decompositions();
			DBRecordNumerical.InputFile1 = new InputFile();
			for (int i=1;i<DBRecordNumerical.Nreader+1;i++)
			DBRecordNumerical.InputFile1.readerIDs.put(Integer.toString(i), i);
			StatPanel StatPanelNumerical = new StatPanel(RoeMetz1.getFrame(), DBRecordNumerical);
			StatPanelNumerical.setStatPanel();
			StatPanelNumerical.setTable1();

			JDialog numericalOutput = new JDialog(RoeMetz1.getFrame(), "Numerical Integration Results");
//			numericalOutput.add(studyDesignJPanel);
			numericalOutput.add(StatPanelNumerical.JPanelStat);
			JButton numericalOutputExport= new JButton("Export Analysis Result");
			numericalOutputExport.addActionListener(new analysisExportListener(DBRecordNumerical,"NumericalOutput"));			
			numericalOutput.add(numericalOutputExport, BorderLayout.PAGE_END);
			numericalOutput.pack();
			numericalOutput.setVisible(true);

			DateFormat dateForm = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
			Date currDate = new Date();
			final String filenameTime = dateForm.format(currDate);

//			writeSummaryFile(calcSaveDirectory,
//					"Summary of Calculation Results", "calc-results-"
//							+ filenameTime, allDecomps, allCoeffs, AUCs);
		}

	}
	
	/**
	 * check seed when focusLost on JTextField_seed
	 * @author BDG
	 *
	 */
	public class SeedInputListener implements FocusListener {

		/* (non-Javadoc)
		 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
		 */
		@Override
		public void focusGained(FocusEvent e) {

		}

		/* (non-Javadoc)
		 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
		 */
		@Override
		public void focusLost(FocusEvent e) {

			String String_seed, String_desc;
			
			// Check seed has 9 digits or less
			String_seed = JTextField_seed.getText();
			if(String_seed.length() > 9) {
				String_seed = String_seed.substring(String_seed.length()-9, String_seed.length());
				JTextField_seed.setText(String_seed);

				String_desc = "Seed must be 9 digit integer or smaller. \n" +
						"Input seed will be truncated.";
				System.out.println(String_desc);
				JOptionPane.showMessageDialog(
						RoeMetz1.getFrame(), String_desc, "Error", JOptionPane.ERROR_MESSAGE);

			}

			// Check seed is only digits
			String regex = "[0-9]+"; 
			if(!String_seed.matches(regex)) {
				
				/*
				 * Create a seed that is 9 digits or less, so that it can be converted to an integer
				 */
				String_seed = Long.toString(System.currentTimeMillis());
				if(String_seed.length() > 9){
					String_seed = String_seed.substring(String_seed.length()-9, String_seed.length());
				}
				JTextField_seed.setText(String_seed);

				String_desc = "Seed must be digits only. \n" +
						"Random seed being used.";
				System.out.println(String_desc);
				JOptionPane.showMessageDialog(
						RoeMetz1.getFrame(), String_desc, "Error", JOptionPane.ERROR_MESSAGE);

			}
			
		}

	}

	/**
	 * Writes the results of a simulation experiment as ROC scores to a text
	 * file, formatted to be read by iMRMC or equivalent software
	 * 
	 * @param filename Filename prefix (is a timestamp) for a particular batch
	 *            of output files
	 * @param l The number of this particular experiment in the batch
	 */
	public void writeInputFile(DBRecord currDBRecord, String filename, long l) {
		
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
			bw.write("NR: " + currDBRecord.Nreader + "\n");
			bw.write("N0: " + currDBRecord.Nnormal + "\n");
			bw.write("N1: " + currDBRecord.Ndisease + "\n");
			bw.write("NM: 2\n");
			bw.write("\n");
			bw.write("AUC1: " + threeDecOpt.format(
					currDBRecord.AUCsReaderAvg[0]) + "\n");
			bw.write("AUC2: " + threeDecOpt.format(
					currDBRecord.AUCsReaderAvg[1]) + "\n");
			bw.write("DAUC: " + threeDecOpt.format(
					currDBRecord.AUCsReaderAvg[0]-currDBRecord.AUCsReaderAvg[1]) + "\n");
			bw.write("\n");
			bw.write("BEGIN DATA:\n");

			int nrows = currDBRecord.InputFile1.observerData.length;
			String[][] observerDataTemp = currDBRecord.InputFile1.observerData;
			for(int i=0; i<nrows; i++) {
				bw.write(observerDataTemp[i][0] + ", " + 
						observerDataTemp[i][1] + ", " +
						observerDataTemp[i][2] + ", " +
						observerDataTemp[i][3] + "\n");
				
			}

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	/**
	 * Writes component info, p-values, and confidence interval of a single study to file
	 * @param filename Timestamp based filename to identify group of experiments
	 * @param l Individual experiment number
	 */
	public void writeOutputFile(DBRecord currDBRecord, String filename, long l) {

		StatTest tempStat = new StatTest(currDBRecord);
		System.out.println("p-val = " + tempStat.pValNormal);
		System.out.println("CI = " + tempStat.ciBotNormal + ", "
				+ tempStat.ciTopNormal);

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

			int col = currDBRecord.BDG[0].length;
			int row = currDBRecord.BDG.length;
			DecimalFormat df = new DecimalFormat("0.###E0");
			for (int i = 0; i < row; i++) {
				String temp = "";
				for (int j = 0; j < col; j++) {
					int totalWidth = 14;
					int numWidth = df.format(currDBRecord.BDG[i][j]).length();
					int numSpaces = totalWidth - numWidth;
					temp = temp + df.format(currDBRecord.BDG[i][j]);
					for (int x = 0; x < numSpaces; x++) {
						temp = temp + " ";
					}
				}
				temp = temp + "\n";
				bw.write(temp);
			}

			bw.write("p-value: " + tempStat.pValNormal + "\n");
			bw.write("Confidence Interval: (" + tempStat.ciBotNormal + ", "
					+ tempStat.ciTopNormal + ")\n");
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
			BDGdata[1] = Matrix.scale(tempBDGTab[5], 0.5);
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
			BCKdata[1] = Matrix.scale(tempBCKTab[5], 0.5);
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
	class analysisExportListener implements ActionListener {
		private DBRecord DB1;
		private String subFileName;
		@Override
     	public void actionPerformed(ActionEvent e) {
			try {
				JFileChooser fc = new JFileChooser();
	            DateFormat dateForm = new SimpleDateFormat("yyyyMMddHHmm");
				Date currDate = new Date();
				String fileTime = dateForm.format(currDate);
				String exportFileName = subFileName+fileTime+".omrmc";
				fc.setSelectedFile(new File(outputDirectory+"//"+exportFileName));
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"iMRMC Summary Files (.omrmc or csv)", "csv","omrmc");
				fc.setFileFilter(filter);	
				int fcReturn = fc.showSaveDialog((Component) e.getSource());
				if (fcReturn == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					if (!f.exists()) {
						f.createNewFile();
					}
					FileWriter fw = new FileWriter(f.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					outputDirectory = fc.getCurrentDirectory();			
					String savedFileName = fc.getSelectedFile().getName();
					String report = "";
					report = genReport(DB1);					
					bw.write(report);
					bw.close();
					JOptionPane.showMessageDialog(
							RoeMetz1.getFrame(), subFileName+" has been succeed export to " + outputDirectory + " !\n"+ "Filename = " +savedFileName, 
							"Exported", JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (HeadlessException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} 
				
		 }
		public analysisExportListener(DBRecord DBtemp, String tempSubFileName){
			DB1 = DBtemp;
			subFileName = tempSubFileName;
		}
	
	}
	
	public String genReport(DBRecord processDBRecordStat) {
//		double[][] BDGcoeff = DBRecordSize.BDGcoeff;
//		double[][] BCKcoeff = DBRecordSize.BCKcoeff;
//		double[][] DBMcoeff = DBRecordSize.DBMcoeff;
//		double[][] ORcoeff = DBRecordSize.ORcoeff;
//		double[][] MScoeff = DBRecordSize.MScoeff;
		String SEPA = ",";
		int NreaderSize = Integer.parseInt(NreaderJTextField.getText());
		int NnormalSize = Integer.parseInt(NnormalJTextField.getText());
		int NdiseaseSize = Integer.parseInt(NdiseaseJTextField.getText());

		

		String str = "";
		str = str + "MRMC summary statistics from " +MRMC.versionname + "\r\n";
		str = str + "Summary statistics written to file named:" + "\r\n";
		str = str + GUInterface.summaryfilename + "\r\n";

		str = str + "\r\n*****************************************************************\r\n";
		str = str + "Reader=" + NreaderJTextField.getText() + "\r\n"
				+ "Normal=" + NnormalJTextField.getText() + "\r\n"
				+ "Disease=" +NdiseaseJTextField.getText()+"\r\n";
		if (useMLE == 1)
			str = str + "this report uses MLE estimate of components.\r\n";
		str = str
				+ "\r\n*****************************************************************\r\n";
		
		
		str = str + "BEGIN SUMMARY\r\n";
		str = str + "NReader=  " + NreaderSize + "\r\n";
		str = str + "Nnormal=  " + NnormalSize + "\r\n";
		str = str + "NDisease= " + NdiseaseSize + "\r\n" + "\r\n";
		str = str + "Modality A = " + processDBRecordStat.modalityA + "\r\n";
		str = str + "Modality B = " + processDBRecordStat.modalityB + "\r\n" + "\r\n";
		str = str + "Reader-Averaged AUCs" + "\r\n";
		str = str +  "AUC_A =" +processDBRecordStat.AUCsReaderAvg[0] + "\r\n";
		str = str +  "AUC_B =" +processDBRecordStat.AUCsReaderAvg[1] + "\r\n";
		str = str +  "AUC_A - AUC_B =" + Double.toString(Double.valueOf(processDBRecordStat.AUCsReaderAvg[0])-Double.valueOf(processDBRecordStat.AUCsReaderAvg[1])) + "\r\n";
		str = str +  "Reader Specific AUCs" +"\r\n";
		int k=1;
		int IDlength = 0;
		for(String desc_temp : processDBRecordStat.InputFile1.readerIDs.keySet() ) {
			IDlength = Math.max(IDlength,desc_temp.length());
		}
		if (IDlength>9){
			for (int i=0; i<IDlength-9; i++){
				str = str + " ";
			}
			str = str + "Reader ID";
		    str = str+SEPA + "       AUC_A" + SEPA +  "      AUCs_B" + SEPA +  "   AUC_A - AUCs_B";
		} else{
			str = str + "Reader ID" +SEPA + "       AUC_A" + SEPA +  "      AUCs_B" + SEPA +  "   AUC_A - AUCs_B";
		}
		
		k=1;
		for(String desc_temp : processDBRecordStat.InputFile1.readerIDs.keySet() ) {
			str = str + "\r\n";
			for (int i=0; i<Math.max(IDlength,9) - desc_temp.length(); i++){
				str = str + " ";
			}
			str = str + desc_temp;
			str = str+ SEPA + "  " +
					fiveDecE.format(processDBRecordStat.AUCs[k-1][0]) + SEPA + "  " +
					fiveDecE.format(processDBRecordStat.AUCs[k-1][1]) + SEPA;
			        double AUC_dif = processDBRecordStat.AUCs[k-1][0]-processDBRecordStat.AUCs[k-1][1];
					if(AUC_dif<0)
						str = str + "      " + fiveDecE.format(AUC_dif);
					else if (AUC_dif>0)
						str = str + "       " + fiveDecE.format(AUC_dif);
					else
						str = str + "        " + fiveDecE.format(AUC_dif);
			k=k+1;
		}
		str = str + "\r\n**********************BDG Moments***************************\r\n";
		str = str + "         Moments" + SEPA + "         M1" + SEPA + "         M2" + SEPA + "         M3" + SEPA
				+ "         M4" + SEPA + "         M5" + SEPA + "         M6" + SEPA + "         M7" + SEPA + "         M8"
				+ "\r\n";
		str = str + "Modality1(AUC_A)" + SEPA;
		for (int i = 0; i < 8; i++){
			if(processDBRecordStat.BDG[0][i]>0)
				str = str + " " + fiveDecE.format(processDBRecordStat.BDG[0][i])+SEPA;
			else
				str = str + "  " + fiveDecE.format(processDBRecordStat.BDG[0][i])+SEPA;
		}
		str = str + "\r\n" + "Modality2(AUC_B)" + SEPA;
		for (int i = 0; i < 8; i++){
			if(processDBRecordStat.BDG[1][i]>0)
				str = str + " " + fiveDecE.format(processDBRecordStat.BDG[1][i])+SEPA;
			else
				str = str + "  " + fiveDecE.format(processDBRecordStat.BDG[1][i])+SEPA;
		}
		str = str + "\r\n" + "    comp product" + SEPA;
		for (int i = 0; i < 8; i++){
			if(processDBRecordStat.BDG[2][i]>0)
				str = str + " " + fiveDecE.format(processDBRecordStat.BDG[2][i])+SEPA;
			else
				str = str + "  " + fiveDecE.format(processDBRecordStat.BDG[2][i])+SEPA;
		}
		str = str +"\r\n"; 
		str = str +"END SUMMARY \r\n"; 
		
		

		str = str
				+ "\r\n**********************BDG output Results***************************\r\n";
		str = str + "Moments" + SEPA + "M1" + SEPA + "M2" + SEPA + "M3" + SEPA
				+ "M4" + SEPA + "M5" + SEPA + "M6" + SEPA + "M7" + SEPA + "M8";
		/*
		 * added for saving the results
		 */
		str = str + "\r\n" + "comp MA" + SEPA;
		for(int i = 0; i<8; i++)
			str = str + fiveDecE.format(DBRecord.BDGPanelresult[0][i]) + SEPA;
		str = str + "\r\n" + "coeff MA" + SEPA;
		for(int i = 0; i<8; i++)
			str = str + fiveDecE.format(DBRecord.BDGPanelresult[1][i]) + SEPA;
		str = str + "\r\n" + "comp MB" + SEPA;
		for(int i = 0; i<8; i++)
			str = str + fiveDecE.format(DBRecord.BDGPanelresult[2][i]) + SEPA;
		str = str + "\r\n" + "coeff MB" + SEPA;
		for(int i = 0; i<8; i++)
			str = str + fiveDecE.format(DBRecord.BDGPanelresult[3][i]) + SEPA;
		str = str + "\r\n" + "comp product" + SEPA;
		for(int i = 0; i<8; i++)
			str = str + fiveDecE.format(DBRecord.BDGPanelresult[4][i]) + SEPA;
		str = str + "\r\n" + "-coeff product" + SEPA;
		for(int i = 0; i<8; i++)
			str = str + fiveDecE.format(DBRecord.BDGPanelresult[5][i]) + SEPA;
		str = str + "\r\n" + "total" + SEPA;
		for(int i = 0; i<8; i++)
			str = str + fiveDecE.format(DBRecord.BDGPanelresult[6][i]) + SEPA;
		str = str +"\r\n"; 
		str = str
				+ "\r\n**********************BCK output Results***************************";
		str = str + "\r\nMoments" + SEPA + "N" + SEPA + "D" + SEPA + "N~D" + SEPA
				+ "R" + SEPA + "N~R" + SEPA + "D~R" + SEPA + "R~N~D";
		str = str + "\r\n" + "comp MA" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(DBRecord.BCKPanelresult[0][i]) + SEPA;
		str = str + "\r\n" + "coeff MA" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(DBRecord.BCKPanelresult[1][i]) + SEPA;
		str = str + "\r\n" + "comp MB" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(DBRecord.BCKPanelresult[2][i]) + SEPA;
		str = str + "\r\n" + "coeff MB" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(DBRecord.BCKPanelresult[3][i]) + SEPA;
		str = str + "\r\n" + "comp product" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(DBRecord.BCKPanelresult[4][i]) + SEPA;
		str = str + "\r\n" + "-coeff product" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(DBRecord.BCKPanelresult[5][i]) + SEPA;
		str = str + "\r\n" + "total" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(DBRecord.BCKPanelresult[6][i]) + SEPA;
		str = str +"\r\n"; 
		str = str
				+ "\r\n**********************DBM output Results***************************";
		str = str + "\r\nComponents" + SEPA + "R" + SEPA + "C" + SEPA + "R~C"
				+ SEPA + "T~R" + SEPA + "T~C" + SEPA + "T~R~C";
		str = str + "\r\n" + "components" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBRecord.DBMPanelresult[0][i]) + SEPA;
		str = str + "\r\n" + "coeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBRecord.DBMPanelresult[1][i]) + SEPA;
		str = str + "\r\n" + "total" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBRecord.DBMPanelresult[2][i]) + SEPA;
		str = str +"\r\n"; 
		str = str
				+ "\r\n**********************OR output Results***************************";
		str = str + "\r\nComponents" + SEPA + "R" + SEPA + "TR" + SEPA + "COV1"
				+ SEPA + "COV2" + SEPA + "COV3" + SEPA + "ERROR";
		str = str + "\r\n" + "components" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBRecord.ORPanelresult[0][i]) + SEPA;
		str = str + "\r\n" + "coeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBRecord.ORPanelresult[1][i]) + SEPA;
		str = str + "\r\n" + "total" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBRecord.ORPanelresult[2][i]) + SEPA;
		str = str +"\r\n"; 
		str = str
				+ "\r\n**********************MS output Results***************************";
		str = str + "\r\nComponents" + SEPA + "R" + SEPA + "C" + SEPA + "RC"
				+ SEPA + "MR" + SEPA + "MC" + SEPA + "MRC";
		str = str + "\r\ncomponents" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBRecord.MSPanelresult[0][i]) + SEPA;
		str = str + "\r\ncoeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBRecord.MSPanelresult[1][i]) + SEPA;
		str = str + "\r\n" + "total"+ SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBRecord.MSPanelresult[2][i]) + SEPA;
		str = str +"\r\n";

		return str;
	}


}
