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

import roemetz.gui.RMGUInterface.SeedInputListener;

import java.awt.event.*;
import java.awt.*;

import mrmc.core.DBRecord;
import mrmc.core.StatTest;

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
	public JPanel JPanelSize = new JPanel();
	private JFrame reportFrame;

	public JTextField
		NumSplitPlotsJTextField,
		NreaderJTextField,
		NnormalJTextField,
		NdiseaseJTextField,
		SigLevelJTextField,
		EffSizeJTextField;

	public JLabel 
		SizeJLabel = new JLabel("Sizing Analysis: "), 
		SizeJLabelSqrtVar = new JLabel(),
		SizeJLabelTStat = new JLabel(),
		SizeJLabelPowerNormal = new JLabel(),
		SizeJLabelCINormal = new JLabel(),
		SizeJLabelDFBDG = new JLabel(),
		SizeJLabelLambdaBDG = new JLabel(),
		SizeJLabelPowerBDG = new JLabel(),
		SizeJLabelCIBDG = new JLabel(),
		SizeJLabelDFHillis = new JLabel(),
		SizeJLabelLambdaHillis = new JLabel(),
		SizeJLabelPowerHillis = new JLabel(),
		SizeJLabelCIHillis = new JLabel();

	JPanel SizePanelRow1, SizePanelRow2, SizePanelRow3, SizePanelRow4, SizePanelRow5, SizePanelRow6;

	public int
		numSplitPlots = 1,
		pairedNormalsFlag = 1,
		pairedDiseasedFlag = 1,
		pairedReadersFlag = 1;
	
	public double
		sigLevel,
		effSize;
	double[] statParms = new double[2];
	private String SEPA = ",";

	DecimalFormat twoDec = new DecimalFormat("0.00");
	DecimalFormat threeDec = new DecimalFormat("0.000");
	DecimalFormat fourDec = new DecimalFormat("0.0000");
	DecimalFormat threeDecE = new DecimalFormat("0.000E0");
	DecimalFormat fiveDecE = new DecimalFormat("0.00000E0");

	/**
	 * Empty constructor for accessing panels
	 * 
	 */
	public SizePanel() {
		
	}
	
	/**
	 * iMRMC constructor for sizing panel. Creates and initializes related GUI
	 * elements.
	 * 
	 * @param GUItemp is the GUInterface that created this sizing panel 
	 * 
	 */
	public SizePanel(GUInterface GUItemp) {
		
		GUI = GUItemp;
		DBRecordSize = GUI.DBRecordSize;
		JPanelSize.setLayout(new BoxLayout(JPanelSize, BoxLayout.Y_AXIS));

		SizePanelRow1 = setStudyDesign();
		SizePanelRow2 = new JPanel(new FlowLayout());
		SizePanelRow3 = new JPanel(new FlowLayout());
		SizePanelRow4 = new JPanel(new FlowLayout());
		SizePanelRow5 = new JPanel(new FlowLayout());
		SizePanelRow6 = new JPanel(new FlowLayout());

		/*
		 * Populate the row by adding elements
		 */
		SigLevelJTextField = new JTextField("0.05", 3);
		SizePanelRow2.add(new JLabel("Significance level"));
		SizePanelRow2.add(SigLevelJTextField);
		//
		EffSizeJTextField = new JTextField("0.05", 3);
		SizePanelRow2.add(new JLabel("Effect Size"));
		SizePanelRow2.add(EffSizeJTextField);
		//
		NreaderJTextField = new JTextField("0",3);
		SizePanelRow2.add(new Label("#Reader"));
		SizePanelRow2.add(NreaderJTextField);
		//
		NnormalJTextField = new JTextField("0",3);
		SizePanelRow2.add(new Label("#Normal"));
		SizePanelRow2.add(NnormalJTextField);
		//
		NdiseaseJTextField = new JTextField("0",3);
		SizePanelRow2.add(new Label("#Diseased"));
		SizePanelRow2.add(NdiseaseJTextField);
		//
		JButton sizeTrial = new JButton("Size a Trial");
		sizeTrial.addActionListener(new sizeTrialListener());
		SizePanelRow2.add(sizeTrial);

		/*
		 * Populate the row by adding elements
		 */
		SizePanelRow3.add(SizeJLabel);
		SizePanelRow3.add(SizeJLabelSqrtVar);
		SizePanelRow3.add(SizeJLabelTStat);

		SizePanelRow4.add(SizeJLabelPowerNormal);
		SizePanelRow4.add(SizeJLabelCINormal);

		SizePanelRow5.add(SizeJLabelDFBDG);
		SizePanelRow5.add(SizeJLabelLambdaBDG);
		SizePanelRow5.add(SizeJLabelPowerBDG);
		SizePanelRow5.add(SizeJLabelCIBDG);

		SizePanelRow6.add(SizeJLabelDFHillis);
		SizePanelRow6.add(SizeJLabelLambdaHillis);
		SizePanelRow6.add(SizeJLabelPowerHillis);
		SizePanelRow6.add(SizeJLabelCIHillis);

		JPanelSize.add(SizePanelRow1);
		JPanelSize.add(SizePanelRow2);
		JPanelSize.add(SizePanelRow3);
		JPanelSize.add(SizePanelRow4);
		JPanelSize.add(SizePanelRow5);
		JPanelSize.add(SizePanelRow6);

	}

	public JPanel setStudyDesign() {
		
		JPanel JPanelStudyDesign = new JPanel(new FlowLayout(FlowLayout.LEFT));

		/*
		 * Populate the row by adding elements
		 */
		JPanelStudyDesign.add(new JLabel("Study Design:  "));
		/*
		 * User can choose the number of groups in a split-plot study design
		 * Please refer to Obuchowski 2012 Academic Radiology regarding study design
		 */
		NumSplitPlotsJTextField = new JTextField("1", 3);
		JPanelStudyDesign.add(new JLabel("# of Split-Plot Groups"));
		JPanelStudyDesign.add(NumSplitPlotsJTextField);
		NumSplitPlotsJTextField.addFocusListener(new NumSplitPlotsListener());

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
		JPanelStudyDesign.add(new JLabel("    Paired Readers? "));
		JPanelStudyDesign.add(ButtonPairedReadersYes);
		JPanelStudyDesign.add(ButtonPairedReadersNo);
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
		JPanelStudyDesign.add(new JLabel("    Pair Normal Cases? "));
		JPanelStudyDesign.add(ButtonPairedNormalsYes);
		JPanelStudyDesign.add(ButtonPairedNormalsNo);
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
		JPanelStudyDesign.add(new JLabel("    Pair Disease Cases? "));
		JPanelStudyDesign.add(ButtonPairedDiseasedYes);
		JPanelStudyDesign.add(ButtonPairedDiseasedNo);

		return JPanelStudyDesign;
		
	}

	/**
	 * Clears all input fields and statistics labels
	 */
	void resetSizePanel() {
		
		SizeJLabelSqrtVar.setText("SqrtVar=");
		SizeJLabelTStat.setText(",  Test Stat=");
		
		SizeJLabelPowerNormal.setText("Normal Approx:  df= \u221e,  Power=");
//		SizeJLabelCINormal.setText("Conf. Int.=");

		SizeJLabelDFBDG.setText("          BDG:  df=");
		SizeJLabelLambdaBDG.setText(",  Lambda=");
		SizeJLabelPowerBDG.setText(",  Power=");
//		SizeJLabelCIBDG.setText("Conf. Int.=");

		SizeJLabelDFHillis.setText("  Hillis 2011:  df=");
		SizeJLabelLambdaHillis.setText(",  Lambda=");
		SizeJLabelPowerHillis.setText(",  Power=");
//		SizeJLabelCIHillis.setText("Conf. Int.=");
	
	}
	
	/**
	 * Sets the trial sizing panel inputs with default values based on the
	 * current record
	 */
	public void setSizePanel() {

		StatTest testSize = DBRecordSize.testSize;
		String output;
		
		output = "SqrtVar=" 
				+ threeDecE.format(Math.sqrt(DBRecordSize.totalVar));
		SizeJLabelSqrtVar.setText(output);
		output = ",  Stat= "
				+ threeDecE.format(testSize.tStatCalc);
		SizeJLabelTStat.setText(output);

		output = "Normal Approx:  df= \u221e ,  Power= "
				+ twoDec.format(testSize.powerNormal);
		SizeJLabelPowerNormal.setText(output);
		output = ",  Conf. Int.=("
				+ fourDec.format(testSize.ciBotNormal)
				+ ", "
				+ fourDec.format(testSize.ciTopNormal)
				+ ")";
//		SizeJLabelCINormal.setText(output);

		output = "          BDG:  df= "
				+ twoDec.format(testSize.DF_BDG);
		SizeJLabelDFBDG.setText(output);
		output = ",  Lambda= "
				+ twoDec.format(testSize.lambdaBDG);
		SizeJLabelLambdaBDG.setText(output);
		output = ",  Power= "
				+ twoDec.format(testSize.powerBDG);
		SizeJLabelPowerBDG.setText(output);
		output = ",  Conf. Int.=("
				+ fourDec.format(testSize.ciBotBDG)
				+ ", "
				+ fourDec.format(testSize.ciTopBDG)
				+ ")";
//		SizeJLabelCIBDG.setText(output);

		/*
		 *  Hillis DoF is not applicable for non-fully crossed studies
		 */
		if (this.numSplitPlots == 1 
				&& this.pairedReadersFlag == 1
				&& this.pairedNormalsFlag == 1
				&& this.pairedDiseasedFlag ==1) {
			
			output = "   Hillis 2011:  df= "
					+ twoDec.format(testSize.DF_Hillis);
			SizeJLabelDFHillis.setText(output);
			output = ",  Lambda= "
					+ twoDec.format(testSize.lambdaHillis);
			SizeJLabelLambdaHillis.setText(output);
			output = ",  Power= "
					+ twoDec.format(testSize.powerHillis);
			SizeJLabelPowerHillis.setText(output);
			output = ",  Conf. Int.=("
					+ fourDec.format(testSize.ciBotHillis)
					+ ", "
					+ fourDec.format(testSize.ciTopHillis)
					+ ")";
//			SizeJLabelCIHillis.setText(output);
		} else {
			SizeJLabelDFHillis.setText("  Hillis 2011:  df=");
			SizeJLabelLambdaHillis.setText(",  Lambda=");
			SizeJLabelPowerHillis.setText(",  Power=");
//			SizeJLabelCIHillis.setText("Conf. Int.=");
		}

	}
	
	/**
	 * Gets statistics for new trial sizing in String format
	 * 
	 * @return String of statistics for new trial sizing
	 */
	public String getSizeResults() {
		String results = SizeJLabelSqrtVar.getText();
		results = results + "\t" + SizeJLabelTStat.getText();
		results = results + "\t" + SizeJLabelPowerNormal.getText();
		results = results + "\t" + SizeJLabelCINormal.getText();
		results = results + "\n";
		results = results + "\t" + SizeJLabelDFBDG.getText();
		results = results + "\t" + SizeJLabelPowerBDG.getText();
		results = results + "\t" + SizeJLabelCIBDG.getText();
		results = results + "\n";
		results = results + "\t" + SizeJLabelDFHillis.getText();
		results = results + "\t" + SizeJLabelPowerHillis.getText();
		results = results + "\t" + SizeJLabelCIHillis.getText();

		return results;
	}

	/**
	 * Creates a textual representation of the current record analysis and trial
	 * sizing results
	 * 
	 * @return String containing experiment parameters, components, trial size
	 *         info
	 */
	public String genReport() {

		int useMLE = DBRecordSize.flagMLE;

		double[][] BDG = DBRecordSize.BDG;
		double[][] DBM = DBRecordSize.DBM;
		double[][] BCK = DBRecordSize.BCK;
		double[][] OR = DBRecordSize.OR;
		double[][] MS = DBRecordSize.MS;
		if(useMLE == 1) {
			BDG = DBRecordSize.BDGbias;
			DBM = DBRecordSize.DBMbias;
			BCK = DBRecordSize.BCKbias;
			OR = DBRecordSize.ORbias;
			MS = DBRecordSize.MSbias;
		}
		double[][] BDGcoeff = DBRecordSize.BDGcoeff;
		double[][] BCKcoeff = DBRecordSize.BCKcoeff;
		double[][] DBMcoeff = DBRecordSize.DBMcoeff;
		double[][] ORcoeff = DBRecordSize.ORcoeff;
		double[][] MScoeff = DBRecordSize.MScoeff;

		statParms[0] = Double.parseDouble(SigLevelJTextField.getText());
		statParms[1] = Double.parseDouble(EffSizeJTextField.getText());
		String result = GUI.StatPanel1.getStatResults();

		int NreaderSize = Integer.parseInt(NreaderJTextField.getText());
		int NnormalSize = Integer.parseInt(NnormalJTextField.getText());
		int NdiseaseSize = Integer.parseInt(NdiseaseJTextField.getText());

		String resultnew = getSizeResults();

		String str = "";
		str = str + "Filename: " + DBRecordSize.filename + "\n";
		str = str + DBRecordSize.recordDesc;
		str = str + "Reader=" + Long.toString(DBRecordSize.Nreader) + SEPA
				+ "Normal=" + Long.toString(DBRecordSize.Nnormal) + SEPA
				+ "Disease=" + Long.toString(DBRecordSize.Ndisease)
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
		str = str + "\n" + GUI.DBRecordStat.getAUCsReaderAvgString(DBRecordSize.selectedMod);
		str = str + "\n Statistical Tests:" + result + SEPA;

		str = str
				+ "\n*****************************************************************\n\n\n\n";
		str = str + "NReaderSize=" + NreaderSize + SEPA + "NnormalSize=" + NnormalSize + SEPA
				+ "nDiseaseSize=" + NdiseaseSize + "\n";
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

	public class NumSplitPlotsListener implements FocusListener {
	
		@Override
		public void focusGained(FocusEvent arg0) {
			// TODO Auto-generated method stub
	
		}
	
		@Override
		public void focusLost(FocusEvent arg0) {

			numSplitPlots = Integer.parseInt(NumSplitPlotsJTextField.getText());
	
		}
	
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
				str = genReport();
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

				DBRecordSize.Nreader = Integer.parseInt(NreaderJTextField.getText());
				DBRecordSize.Nnormal = Integer.parseInt(NnormalJTextField.getText());
				DBRecordSize.Ndisease = Integer.parseInt(NdiseaseJTextField.getText());

				sigLevel = Double.parseDouble(SigLevelJTextField.getText());
				effSize = Double.parseDouble(EffSizeJTextField.getText());

				DBRecordSize.DBRecordSizeFill(GUI.SizePanel1);
				setSizePanel();

			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(reportFrame, "Invalid Input",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
