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
 * @version 2.0b
 */
public class SizePanel {
	private GUInterface gui;
	private JFrame reportFrame;
	private JTextField sizeR;
	private JTextField sizeN;
	private JTextField sizeD;
	private JTextField sigLevel;
	private JTextField effSize;
	private JLabel effSizeLabel;
	double[] statParms = new double[2];
	private DBRecord curRecord;
	private String SEPA = ",";
	private DecimalFormat fiveDecE = new DecimalFormat("0.00000E0");
	private DecimalFormat twoDec = new DecimalFormat("0.00");
	private JTextField numSplitPlot;
	private int pairedCs = 1;
	private int pairedRs = 1;

	/**
	 * Sole constructor for sizing panel. Creates and initializes related GUI
	 * elements.
	 * 
	 * @param expSizes Experiment size parameters. Taken from study which was
	 *            analyzed
	 * @param sizingPanel Panel containing elements for trial sizing
	 * @param guitemp Application's instance of the GUI
	 */
	public SizePanel(int[] expSizes, JPanel sizingPanel, GUInterface guitemp) {
		gui = guitemp;

		JPanel innerSizingPanel = new JPanel();
		innerSizingPanel.setLayout(new BoxLayout(innerSizingPanel,
				BoxLayout.PAGE_AXIS));
		JPanel studyDesignInput = new JPanel(new FlowLayout());
		JPanel sizeTrialInput = new JPanel(new FlowLayout());

		JLabel studyDesignLabel = new JLabel("Study Design:  ");
		numSplitPlot = new JTextField("1", 3);

		JRadioButton pairedRYes = new JRadioButton("Yes");
		pairedRYes.setActionCommand("Yes");
		pairedRYes.setSelected(true);
		JRadioButton pairedRNo = new JRadioButton("No");
		pairedRNo.setActionCommand("No");
		ButtonGroup pairedRGroup = new ButtonGroup();
		pairedRGroup.add(pairedRYes);
		pairedRGroup.add(pairedRNo);
		PairedRListener pairedReaders = new PairedRListener();
		pairedRYes.addActionListener(pairedReaders);
		pairedRNo.addActionListener(pairedReaders);

		JRadioButton pairedCYes = new JRadioButton("Yes");
		pairedCYes.setActionCommand("Yes");
		pairedCYes.setSelected(true);
		JRadioButton pairedCNo = new JRadioButton("No");
		pairedCNo.setActionCommand("No");
		ButtonGroup pairedCGroup = new ButtonGroup();
		pairedCGroup.add(pairedCYes);
		pairedCGroup.add(pairedCNo);
		PairedCListener pairedCases = new PairedCListener();
		pairedCYes.addActionListener(pairedCases);
		pairedCNo.addActionListener(pairedCases);

		studyDesignInput.add(studyDesignLabel);
		studyDesignInput.add(new JLabel("# of Split-Plot Groups"));
		studyDesignInput.add(numSplitPlot);
		studyDesignInput.add(new JLabel("    Paired Readers? "));
		studyDesignInput.add(pairedRYes);
		studyDesignInput.add(pairedRNo);
		studyDesignInput.add(new JLabel("    Paired Cases? "));
		studyDesignInput.add(pairedCYes);
		studyDesignInput.add(pairedCNo);

		JLabel sigLevelLabel = new JLabel("Significance level");
		sigLevel = new JTextField("0.05", 3);
		effSizeLabel = new JLabel("Effect Size");
		effSize = new JTextField("0.05", 3);
		sizeTrialInput.add(sigLevelLabel);
		sizeTrialInput.add(sigLevel);
		sizeTrialInput.add(effSizeLabel);
		sizeTrialInput.add(effSize);

		sizeTrialInput.add(new Label("#Reader"));
		sizeR = new JTextField(2);
		sizeTrialInput.add(sizeR);
		sizeTrialInput.add(new Label("#Normal"));
		sizeN = new JTextField(3);
		sizeTrialInput.add(sizeN);
		sizeTrialInput.add(new Label("#Diseased"));
		sizeD = new JTextField(3);
		sizeTrialInput.add(sizeD);
		setNumbers(expSizes);
		JButton sizeTrial = new JButton("Size a Trial");
		sizeTrial.addActionListener(new sizeTrialListner());
		sizeTrialInput.add(sizeTrial);
		JButton genReport = new JButton("Generate Report");
		genReport.addActionListener(new genReportListner());
		sizeTrialInput.add(genReport);

		innerSizingPanel.add(studyDesignInput);
		innerSizingPanel.add(sizeTrialInput);

		sizingPanel.add(innerSizingPanel);
	}

	/**
	 * Sets text boxes for experiment size
	 * 
	 * @param Parms Array containing size parameters
	 */
	public void setNumbers(int[] Parms) {
		sizeR.setText(Integer.toString(Parms[0]));
		sizeN.setText(Integer.toString(Parms[1]));
		sizeD.setText(Integer.toString(Parms[2]));
	}

	/**
	 * Sets text box and label for effect size
	 * 
	 * @param text Effect size label text
	 * @param val Value for effect size input box
	 */
	public void setEff(String text, String val) {
		effSizeLabel.setText(text);
		effSize.setText(val);
	}

	/**
	 * Creates a textual representation of the current record analysis and trial
	 * sizing results
	 * 
	 * @return String containing experiment parameters, components, trial size
	 *         info
	 */
	public String genReport() {
		int useBiasM = gui.getuseBiasM();

		double[][] BDG = curRecord.getBDG(useBiasM);
		double[][] DBM = curRecord.getDBM(useBiasM);
		double[][] BCK = curRecord.getBCK(useBiasM);
		double[][] OR = curRecord.getOR(useBiasM);
		double[][] BDGcoeff = curRecord.getBDGcoeff();
		double[][] BCKcoeff = curRecord.getBCKcoeff();
		double[][] DBMcoeff = curRecord.getDBMcoeff();
		double[][] ORcoeff = curRecord.getORcoeff();

		statParms[0] = Double.parseDouble(sigLevel.getText());
		statParms[1] = Double.parseDouble(effSize.getText());
		String result = gui.getStat1();

		int newR = Integer.parseInt(sizeR.getText());
		int newN = Integer.parseInt(sizeN.getText());
		int newD = Integer.parseInt(sizeD.getText());

		String resultnew = gui.getStat2();

		String str = "";
		str = str + "Filename: " + curRecord.getFilename() + "\n";
		str = str + curRecord.getRecordDesp();
		str = str + "Reader=" + Integer.toString(curRecord.getReader()) + SEPA
				+ "Normal=" + Integer.toString(curRecord.getNormal()) + SEPA
				+ "Disease=" + Integer.toString(curRecord.getDisease())
				+ "\n\n";
		if (useBiasM == 1)
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
		str = str + "\n" + gui.getAUCoutput();
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
		str = str + "\n Sizing Results";
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

		double[][] BDG = curRecord.getBDG(0);
		double[][] DBM = curRecord.getDBM(0);
		double[][] BCK = curRecord.getBCK(0);
		double[][] OR = curRecord.getOR(0);
		double[][] BDGcoeff = curRecord.getBDGcoeff();
		double[][] BCKcoeff = curRecord.getBCKcoeff();
		double[][] DBMcoeff = curRecord.getDBMcoeff();
		double[][] ORcoeff = curRecord.getORcoeff();

		statParms[0] = Double.parseDouble(sigLevel.getText());
		statParms[1] = Double.parseDouble(effSize.getText());
		String results = gui.getStat1();

		int newR = Integer.parseInt(sizeR.getText());
		int newN = Integer.parseInt(sizeN.getText());
		int newD = Integer.parseInt(sizeD.getText());

		String resultnew = gui.getStat2();

		String str = "";
		str = str + "Reader=" + Integer.toString(curRecord.getReader()) + SEPA
				+ "Normal=" + Integer.toString(curRecord.getNormal()) + SEPA
				+ "Disease=" + Integer.toString(curRecord.getDisease())
				+ "\n\n";
		int singleOrDiff = gui.getSingleOrDiff();
		if (singleOrDiff == 0)
			str = str + "Single Modality, AUC=" + curRecord.getAUCinNumber(0)
					+ "\n";
		else
			str = str
					+ "Comparing two modalities, AUC1="
					+ curRecord.getAUCinNumber(0)
					+ ", AUC2="
					+ curRecord.getAUCinNumber(1)
					+ ", the difference is"
					+ (curRecord.getAUCinNumber(0) - curRecord
							.getAUCinNumber(1)) + ".\n";
		int selectedManualComp = gui.getSelectedManualComp();
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
		str = str + "\n" + gui.getAUCoutput();
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
				pairedRs = 1;
			}
			if (str == "No") {
				pairedRs = 0;
			}
		}
	}

	/**
	 * Handler for radio buttons to select if study design has paired cases
	 */
	class PairedCListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String str;
			str = e.getActionCommand();
			if (str == "Yes") {
				pairedCs = 1;
			}
			if (str == "No") {
				pairedCs = 0;
			}
		}
	}

	/**
	 * Handler for button to generate report of analysis
	 */
	class genReportListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			curRecord = gui.getCurrentRecord();
			reportFrame = new JFrame(
					" please copy and paste the report to your own editor");

			reportFrame.getRootPane().setWindowDecorationStyle(
					JRootPane.PLAIN_DIALOG);
			String str = "";
			if (gui.getSelectedInput() == GUInterface.SELECT_MAN)
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
	class sizeTrialListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				int[] parms1 = { Integer.parseInt(sizeR.getText()),
						Integer.parseInt(sizeN.getText()),
						Integer.parseInt(sizeD.getText()) };
				double[] parms2 = { Double.parseDouble(sigLevel.getText()),
						Double.parseDouble(effSize.getText()) };
				int[] parms3 = { Integer.parseInt(numSplitPlot.getText()),
						pairedRs, pairedCs };
				gui.sizeTrial(parms1, parms2, parms3);
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(reportFrame, "Invalid Input",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
