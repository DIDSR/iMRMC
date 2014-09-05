/**
 * SizePanel.java
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

package mrmc.gui;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;

import mrmc.core.DBRecord;

import java.text.DecimalFormat;

/**
 * Panel for sizing new trials and generating reports for given input
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
public class SizePanel {
	private GUInterface GUI;
	private DBRecord DBRecordSize;
	SizePanel thisSizePanel = this;

	private JFrame reportFrame;
	public JTextField
		JTextNumSplitPlots,
		JTextNreader,
		JTextNnormal,
		JTextNdisease,
		JTextSigLevel,
		JTextEffSize;
	public int
		numSplitPlots,
		Nreader,
		Nnormal,
		Ndisease,
		pairedNormalsFlag = 1,
		pairedDiseasedFlag = 1,
		pairedReadersFlag = 1;
	public double
		sigLevel,
		effSize;

	double[] statParms = new double[2];
	private String SEPA = ",";
	private DecimalFormat fiveDecE = new DecimalFormat("0.00000E0");
	private DecimalFormat twoDec = new DecimalFormat("0.00");

	/**
	 * Sole constructor for sizing panel. Creates and initializes related GUI
	 * elements.
	 * 
	 * @param guitemp is the GUInterface that created this sizing panel 
	 * 
	 */
	public SizePanel(GUInterface guitemp) {
		GUI = guitemp;
		DBRecordSize = GUI.DBRecordSize;

		/*
		 * Populate the row by adding elements
		 */
		GUI.SizePanelRow1.add(new JLabel("Study Design:  "));
		/*
		 * User can choose the number of groups in a split-plot study design
		 * Please refer to Obuchowski 2012 Academic Radiology regarding study design
		 */
		JTextNumSplitPlots = new JTextField("1", 3);
		GUI.SizePanelRow1.add(new JLabel("# of Split-Plot Groups"));
		GUI.SizePanelRow1.add(JTextNumSplitPlots);
		/*
		 * Radio buttons to select paired readers
		 * The same readers will read all the cases in both modalities
		 */
		JRadioButton ButtonPairedReadersYes = new JRadioButton("Yes");
		ButtonPairedReadersYes.setActionCommand("Yes");
		JRadioButton ButtonPairedReadersNo = new JRadioButton("No");
		ButtonPairedReadersNo.setActionCommand("No");
		if( pairedReadersFlag==1 ) ButtonPairedReadersYes.setSelected(true);
		else ButtonPairedReadersNo.setSelected(true);
		//
		ButtonGroup pairedRGroup = new ButtonGroup();
		pairedRGroup.add(ButtonPairedReadersYes);
		pairedRGroup.add(ButtonPairedReadersNo);
		PairedRListener pairedReaders = new PairedRListener();
		ButtonPairedReadersYes.addActionListener(pairedReaders);
		ButtonPairedReadersNo.addActionListener(pairedReaders);
		//
		GUI.SizePanelRow1.add(new JLabel("    Paired Readers? "));
		GUI.SizePanelRow1.add(ButtonPairedReadersYes);
		GUI.SizePanelRow1.add(ButtonPairedReadersNo);
		/*
		 * Radio buttons to select paired normals
		 * The same cases will be read in both modalites by all the readers
		 */
		JRadioButton ButtonPairedNormalsYes = new JRadioButton("Yes");
		ButtonPairedNormalsYes.setActionCommand("Yes");
		JRadioButton ButtonPairedNormalsNo = new JRadioButton("No");
		ButtonPairedNormalsNo.setActionCommand("No");
		if( pairedNormalsFlag==1 ) ButtonPairedNormalsYes.setSelected(true);
		else ButtonPairedNormalsNo.setSelected(true);
		//
		ButtonGroup pairedNGroup = new ButtonGroup();
		pairedNGroup.add(ButtonPairedNormalsYes);
		pairedNGroup.add(ButtonPairedNormalsNo);
		PairedNListener pairedNormals = new PairedNListener();
		ButtonPairedNormalsYes.addActionListener(pairedNormals);
		ButtonPairedNormalsNo.addActionListener(pairedNormals);
		//
		GUI.SizePanelRow1.add(new JLabel("    Pair Normal Cases? "));
		GUI.SizePanelRow1.add(ButtonPairedNormalsYes);
		GUI.SizePanelRow1.add(ButtonPairedNormalsNo);
		/*
		 * Radio buttons to select paired diseased
		 * The same cases will be read in both modalites by all the readers
		 */
		JRadioButton ButtonPairedDiseasedYes = new JRadioButton("Yes");
		ButtonPairedDiseasedYes.setActionCommand("Yes");
		JRadioButton ButtonPairedDiseasedNo = new JRadioButton("No");
		ButtonPairedDiseasedNo.setActionCommand("No");
		if( pairedDiseasedFlag==1 ) ButtonPairedDiseasedYes.setSelected(true);
		else ButtonPairedDiseasedNo.setSelected(true);
		//
		ButtonGroup pairedDGroup = new ButtonGroup();
		pairedDGroup.add(ButtonPairedDiseasedYes);
		pairedDGroup.add(ButtonPairedDiseasedNo);
		PairedDListener pairedDiseased = new PairedDListener();
		ButtonPairedDiseasedYes.addActionListener(pairedDiseased);
		ButtonPairedDiseasedNo.addActionListener(pairedDiseased);
		//
		GUI.SizePanelRow1.add(new JLabel("    Pair Disease Cases? "));
		GUI.SizePanelRow1.add(ButtonPairedDiseasedYes);
		GUI.SizePanelRow1.add(ButtonPairedDiseasedNo);

		/*
		 * Populate the row by adding elements
		 */
		JTextSigLevel = new JTextField("0.05", 3);
		GUI.SizePanelRow2.add(new JLabel("Significance level"));
		GUI.SizePanelRow2.add(JTextSigLevel);
		//
		JTextEffSize = new JTextField("0.05", 3);
		GUI.SizePanelRow2.add(new JLabel("Effect Size"));
		GUI.SizePanelRow2.add(JTextEffSize);
		//
		JTextNreader = new JTextField("0",3);
		GUI.SizePanelRow2.add(new Label("#Reader"));
		GUI.SizePanelRow2.add(JTextNreader);
		//
		JTextNnormal = new JTextField("0",3);
		GUI.SizePanelRow2.add(new Label("#Normal"));
		GUI.SizePanelRow2.add(JTextNnormal);
		//
		JTextNdisease = new JTextField("0",3);
		GUI.SizePanelRow2.add(new Label("#Diseased"));
		GUI.SizePanelRow2.add(JTextNdisease);
		//
		JButton sizeTrial = new JButton("Size a Trial");
		sizeTrial.addActionListener(new sizeTrialListener());
		GUI.SizePanelRow2.add(sizeTrial);

		/*
		 * Populate the row by adding elements
		 */
		GUI.SizePanelRow3.add(GUI.SizeJLabel);
		GUI.SizePanelRow3.add(GUI.SizeJLabelSqrtVar);
		GUI.SizePanelRow3.add(GUI.SizeJLabelTStat);

		GUI.SizePanelRow4.add(GUI.SizeJLabelPowerNormal);
		GUI.SizePanelRow4.add(GUI.SizeJLabelCINormal);

		GUI.SizePanelRow5.add(GUI.SizeJLabelDFBDG);
		GUI.SizePanelRow5.add(GUI.SizeJLabelLambdaBDG);
		GUI.SizePanelRow5.add(GUI.SizeJLabelPowerBDG);
		GUI.SizePanelRow5.add(GUI.SizeJLabelCIBDG);

		GUI.SizePanelRow6.add(GUI.SizeJLabelDFHillis);
		GUI.SizePanelRow6.add(GUI.SizeJLabelLambdaHillis);
		GUI.SizePanelRow6.add(GUI.SizeJLabelPowerHillis);
		GUI.SizePanelRow6.add(GUI.SizeJLabelCIHillis);

	}

	/**
	 * Creates a textual representation of the current record analysis and trial
	 * sizing results
	 * 
	 * @return String containing experiment parameters, components, trial size
	 *         info
	 */
	public String genReport() {

		int useMLE = GUI.getFlagMLE();

		double[][] BDG = DBRecordSize.getBDG(useMLE);
		double[][] DBM = DBRecordSize.getDBM(useMLE);
		double[][] BCK = DBRecordSize.getBCK(useMLE);
		double[][] OR = DBRecordSize.getOR(useMLE);
		double[][] BDGcoeff = DBRecordSize.getBDGcoeff();
		double[][] BCKcoeff = DBRecordSize.getBCKcoeff();
		double[][] DBMcoeff = DBRecordSize.getDBMcoeff();
		double[][] ORcoeff = DBRecordSize.getORcoeff();

		statParms[0] = Double.parseDouble(JTextSigLevel.getText());
		statParms[1] = Double.parseDouble(JTextEffSize.getText());
		String result = GUI.getStatResults();

		int newR = Integer.parseInt(JTextNreader.getText());
		int newN = Integer.parseInt(JTextNnormal.getText());
		int newD = Integer.parseInt(JTextNdisease.getText());

		String resultnew = GUI.getSizeResults();

		String str = "";
		str = str + "Filename: " + DBRecordSize.getFilename() + "\n";
		str = str + DBRecordSize.getRecordDesc();
		str = str + "Reader=" + Long.toString(DBRecordSize.getReader()) + SEPA
				+ "Normal=" + Long.toString(DBRecordSize.getNormal()) + SEPA
				+ "Disease=" + Long.toString(DBRecordSize.getDisease())
				+ "\n\n";
		if (useMLE == 1)
			str = str + "this report uses MLE estimate of components.\n";
		str = str
				+ "**********************BDG Results***************************\n";
		str = str + "Moments" + SEPA + "M1" + SEPA + "M2" + SEPA + "M3" + SEPA
				+ "M4" + SEPA + "M5" + SEPA + "M6" + SEPA + "M7" + SEPA + "M8"
				+ "\n";
		str = str + "Modality1" + SEPA;
		for (int i = 0; i < 8; i++)
			str = str + fiveDecE.format(BDG[0][i]) + SEPA;
		str = str + "\n" + "Modality2" + SEPA;
		for (int i = 0; i < 8; i++)
			str = str + fiveDecE.format(BDG[1][i]) + SEPA;
		str = str + "\n" + "Difference" + SEPA;
		for (int i = 0; i < 8; i++)
			str = str + fiveDecE.format(BDG[3][i]) + SEPA;
		str = str + "\n" + "Coeff" + SEPA;
		for (int i = 0; i < 8; i++)
			str = str + fiveDecE.format(BDGcoeff[0][i]) + SEPA;
		str = str
				+ "\n**********************BCK Results***************************";
		str = str + "\nMoments" + SEPA + "N" + SEPA + "D" + SEPA + "N~D" + SEPA
				+ "R" + SEPA + "N~R" + SEPA + "D~R" + SEPA + "R~N~D";
		str = str + "\nModality1" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCK[0][i]) + SEPA;
		str = str + "\nModality2" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCK[1][i]) + SEPA;
		str = str + "\nDifference" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCK[3][i]) + SEPA;
		str = str + "\nCoeff" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCKcoeff[0][i]) + SEPA;
		str = str
				+ "\n**********************DBM Results***************************";
		str = str + "\nComponents" + SEPA + "R" + SEPA + "C" + SEPA + "R~C"
				+ SEPA + "T~R" + SEPA + "T~C" + SEPA + "T~R~C";
		str = str + "\nModality1" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBM[0][i]) + SEPA;
		str = str + "\nModality2" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBM[1][i]) + SEPA;
		str = str + "\nDifference" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBM[3][i]) + SEPA;
		str = str + "\nCoeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBMcoeff[3][i]) + SEPA;
		str = str
				+ "\n**********************OR Results***************************";
		str = str + "\nComponents" + SEPA + "R" + SEPA + "TR" + SEPA + "COV1"
				+ SEPA + "COV2" + SEPA + "COV3" + SEPA + "ERROR";
		str = str + "\nModality1" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(OR[0][i]) + SEPA;
		str = str + "\nModality2" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(OR[1][i]) + SEPA;
		str = str + "\nDifference" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(OR[3][i]) + SEPA;
		str = str + "\nCoeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(ORcoeff[3][i]) + SEPA;
		str = str
				+ "\n*****************************************************************";
		str = str
				+ "\n*****************************************************************";
		str = str + "\n" + GUI.getAUCoutput();
		str = str + "\n Statistical Tests:" + result + SEPA;

		str = str
				+ "\n*****************************************************************\n\n\n\n";
		str = str + "new Reader=" + newR + SEPA + "new Normal=" + newN + SEPA
				+ "new Disease=" + newD + "\n";
		str = str
				+ "\n*****************************************************************";
		str = str
				+ "\n*****************************************************************";
		str = str + "\n" + "Effective Size = " + twoDec.format(statParms[1])
				+ SEPA + "Significance Level = " + twoDec.format(statParms[0]);

		str = str
				+ "\n*****************************************************************";
		str = str + "\n Sizing Results: ";
		str = str + resultnew;
		str = str
				+ "\n*****************************************************************\n\n\n";

		return str;
	}

	// TODO consolidate with other genReport
	/**
	 * Creates a textual representation of the current record analysis and trial
	 * sizing results. Used when manual component input is selected
	 * 
	 * @param flag Indicates manual component input is being used
	 * @return String containing experiment parameters, components, trial size
	 *         info
	 */
	public String genReport(int flag) {

		double[][] BDG = DBRecordSize.getBDG(0);
		double[][] DBM = DBRecordSize.getDBM(0);
		double[][] BCK = DBRecordSize.getBCK(0);
		double[][] OR = DBRecordSize.getOR(0);
		double[][] BDGcoeff = DBRecordSize.getBDGcoeff();
		double[][] BCKcoeff = DBRecordSize.getBCKcoeff();
		double[][] DBMcoeff = DBRecordSize.getDBMcoeff();
		double[][] ORcoeff = DBRecordSize.getORcoeff();

		statParms[0] = Double.parseDouble(JTextSigLevel.getText());
		statParms[1] = Double.parseDouble(JTextEffSize.getText());
		String results = GUI.getStatResults();

		int newR = Integer.parseInt(JTextNreader.getText());
		int newN = Integer.parseInt(JTextNnormal.getText());
		int newD = Integer.parseInt(JTextNdisease.getText());

		String resultnew = GUI.getSizeResults();

		String str = "";
		str = str + "Reader=" + Long.toString(DBRecordSize.getReader()) + SEPA
				+ "Normal=" + Long.toString(DBRecordSize.getNormal()) + SEPA
				+ "Disease=" + Long.toString(DBRecordSize.getDisease())
				+ "\n\n";
		int singleOrDiff = GUI.getSingleOrDiff();
		if (singleOrDiff == 0)
			str = str + "Single Modality, AUC=" + DBRecordSize.getAUCinNumber(0)
					+ "\n";
		else
			str = str
					+ "Comparing two modalities, AUC1="
					+ DBRecordSize.getAUCinNumber(0)
					+ ", AUC2="
					+ DBRecordSize.getAUCinNumber(1)
					+ ", the difference is"
					+ (DBRecordSize.getAUCinNumber(0) - DBRecordSize
							.getAUCinNumber(1)) + ".\n";
		int selectedManualComp = GUI.getSelectedManualComp();
		if (selectedManualComp == 0) // BDG input is selected
		{
			str = str
					+ "**********************BDG Results***************************\n";
			str = str + "Moments" + SEPA + "M1" + SEPA + "M2" + SEPA + "M3"
					+ SEPA + "M4" + SEPA + "M5" + SEPA + "M6" + SEPA + "M7"
					+ SEPA + "M8" + "\n";
			str = str + "moments" + SEPA;
			for (int i = 0; i < 8; i++)
				str = str + fiveDecE.format(BDG[0][i]) + SEPA;
			str = str + "\n" + "Coeff" + SEPA;
			for (int i = 0; i < 8; i++)
				str = str + fiveDecE.format(BDGcoeff[0][i]) + SEPA;
		} else if (selectedManualComp == 1) {
			str = str
					+ "\n**********************BCK Results***************************";
			str = str + "\nMoments" + SEPA + "N" + SEPA + "D" + SEPA + "N~D"
					+ SEPA + "R" + SEPA + "N~R" + SEPA + "D~R" + SEPA + "R~N~D";
			str = str + "\nMoments" + SEPA;
			for (int i = 0; i < 7; i++)
				str = str + fiveDecE.format(BCK[0][i]) + SEPA;
			str = str + "\nCoeff" + SEPA;
			for (int i = 0; i < 7; i++)
				str = str + fiveDecE.format(BCKcoeff[0][i]) + SEPA;
		}

		str = str
				+ "\n**********************DBM Results***************************";
		str = str + "\nComponents" + SEPA + "R" + SEPA + "C" + SEPA + "R~C"
				+ SEPA + "T~R" + SEPA + "T~C" + SEPA + "T~R~C";
		str = str + "\ncompnents" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBM[0][i]) + SEPA;
		str = str + "\nCoeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBMcoeff[3][i]) + SEPA;
		str = str
				+ "\n**********************OR Results***************************";
		str = str + "\nComponents" + SEPA + "R" + SEPA + "TR" + SEPA + "COV1"
				+ SEPA + "COV2" + SEPA + "COV3" + SEPA + "ERROR";
		str = str + "\ncomponents" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(OR[0][i]) + SEPA;
		str = str + "\nCoeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(ORcoeff[3][i]) + SEPA;
		str = str
				+ "\n*****************************************************************";
		str = str
				+ "\n*****************************************************************";
		str = str + "\n" + GUI.getAUCoutput();
		str = str + "\n Statistical Tests:\n" + results + SEPA;

		str = str + "new Reader=" + newR + SEPA + "new Normal=" + newN + SEPA
				+ "new Disease=" + newD + "\n";

		str = str
				+ "\n*****************************************************************";
		str = str
				+ "\n*****************************************************************";
		str = str + "\n" + "Effective Size = " + twoDec.format(statParms[1])
				+ SEPA + "Significance Level = " + twoDec.format(statParms[0]);

		str = str
				+ "\n*****************************************************************";
		str = str + "\n Sizing Results\n";
		str = str + resultnew;
		str = str
				+ "\n*****************************************************************\n\n\n";

		return str;
	}

	/**
	 * Handler for radio buttons to select if study design has paired readers
	 */
	class PairedRListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String str;
			str = e.getActionCommand();
			if (str == "Yes") {
				pairedReadersFlag = 1;
			}
			if (str == "No") {
				pairedReadersFlag = 0;
			}
		}
	}

	/**
	 * Handler for radio buttons to select if study design has paired normal cases
	 */
	class PairedNListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String str;
			str = e.getActionCommand();
			if (str == "Yes") {
				pairedNormalsFlag = 1;
			}
			if (str == "No") {
				pairedNormalsFlag = 0;
			}
		}
	}
	/**
	 * Handler for radio buttons to select if study design has paired diseased cases
	 */
	class PairedDListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String str;
			str = e.getActionCommand();
			if (str == "Yes") {
				pairedDiseasedFlag = 1;
			}
			if (str == "No") {
				pairedDiseasedFlag = 0;
			}
		}
	}

	/**
	 * Handler for button to generate report of analysis
	 */
	class genReportListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			reportFrame = new JFrame(
					" please copy and paste the report to your own editor");

			reportFrame.getRootPane().setWindowDecorationStyle(
					JRootPane.PLAIN_DIALOG);
			String str = "";
			if (GUI.getSelectedInput() == GUInterface.DescInputModeManual)
				str = genReport(1);
			else
				str = genReport();
			JTextArea report = new JTextArea(str, 50, 50);
			JScrollPane scrollPane = new JScrollPane(report,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			reportFrame.getContentPane().add(scrollPane);
			report.setLineWrap(true);
			reportFrame.pack();
			reportFrame.setVisible(true);
		}
	}

	/**
	 * Handler for button to size trial based on specified parameters
	 */
	class sizeTrialListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
//				JOptionPane.showMessageDialog(reportFrame, "Under Construction",
//						"Error", JOptionPane.ERROR_MESSAGE);
//				return;

				numSplitPlots = Integer.parseInt(JTextNumSplitPlots.getText());
				Nreader = Integer.parseInt(JTextNreader.getText());
				Nnormal = Integer.parseInt(JTextNnormal.getText());
				Ndisease = Integer.parseInt(JTextNdisease.getText());

				sigLevel = Double.parseDouble(JTextSigLevel.getText());
				effSize = Double.parseDouble(JTextEffSize.getText());

				DBRecordSize.DBRecordSizeTrial(thisSizePanel);
				GUI.setSizePanel();

			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(reportFrame, "Invalid Input",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
