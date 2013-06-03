/*
 * sPanel.java
 * 
 * v2.0b
 * 
 * @Author Xin He, Phd, Brandon D. Gallas, PhD, Rohan Pathare
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
 *     Panel for sizing new trials and generating reports for given input
 */

package mrmc.gui;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.lang.Math;
import mrmc.core.dbRecord;
import mrmc.core.statTest;

import java.text.DecimalFormat;

public class sPanel {
	GUInterface gui;
	JFrame reportFrame;
	JTextField sizeR;
	JTextField sizeN;
	JTextField sizeD;
	JTextField sigLevel;
	JTextField effSize;
	JLabel effSizeLabel;
	double[] statParms = new double[2];
	dbRecord curRecord;
	String SEPA = ",";
	DecimalFormat formatter = new DecimalFormat("0.00000E0");
	DecimalFormat formatter2 = new DecimalFormat("0.00");
	private JTextField numSplitPlot;
	public int pairedCs = 1;
	public int pairedRs = 1;

	public void setNumbers(int[] Parms) {
		sizeR.setText(Integer.toString(Parms[0]));
		sizeN.setText(Integer.toString(Parms[1]));
		sizeD.setText(Integer.toString(Parms[2]));
	}

	public void setEff(String text, String val) {
		effSizeLabel.setText(text);
		effSize.setText(val);
	}

	public sPanel(int[] Parms, JPanel sizingPanel, GUInterface guitemp) {
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
		setNumbers(Parms);
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

		// statParms = gui.getSiglevel();
		statParms[0] = Double.parseDouble(sigLevel.getText());
		statParms[1] = Double.parseDouble(effSize.getText());
		// double[] results = sizeTrial(DBM, OR, statParms[0], statParms[1],
		// curRecord.getReader(),
		// curRecord.getNormal(), curRecord.getDisease());
		String result = gui.getStat1();

		int newR = Integer.parseInt(sizeR.getText());
		int newN = Integer.parseInt(sizeN.getText());
		int newD = Integer.parseInt(sizeD.getText());
		double[][] newBDGcoeff = dbRecord.genBDGCoeff(newR, newN, newD);
		double[][] newDBMcoeff = dbRecord.genDBMCoeff(newR, newN, newD);
		double[][] newBCKcoeff = dbRecord.genBCKCoeff(newR, newN, newD);
		double[][] newORcoeff = dbRecord.genORCoeff(newR, newN, newD);
		// double[][] DBMnew = curRecord.BDG2DBM(BDG,newR,newN,newD);
		// double[][] ORnew = curRecord.BDG2OR(BDG, newR,newN,newD);
		double[][] DBMnew = dbRecord.BCK2DBM(BCK, newR, newN, newD);
		double[][] ORnew = dbRecord.DBM2OR(0, DBMnew, newR, newN, newD);

		// double[] resultsNew = sizeTrial(DBMnew, ORnew, statParms[0],
		// statParms[1], newR, newN, newD);
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
			str = str + formatter.format(BDG[0][i]) + SEPA;
		str = str + "\n" + "Modality2" + SEPA;
		for (int i = 0; i < 8; i++)
			str = str + formatter.format(BDG[1][i]) + SEPA;
		str = str + "\n" + "Difference" + SEPA;
		for (int i = 0; i < 8; i++)
			str = str + formatter.format(BDG[3][i]) + SEPA;
		str = str + "\n" + "Coeff" + SEPA;
		for (int i = 0; i < 8; i++)
			str = str + formatter.format(BDGcoeff[0][i]) + SEPA;
		str = str
				+ "\n**********************BCK Results***************************";
		str = str + "\nMoments" + SEPA + "N" + SEPA + "D" + SEPA + "N~D" + SEPA
				+ "R" + SEPA + "N~R" + SEPA + "D~R" + SEPA + "R~N~D";
		str = str + "\nModality1" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + formatter.format(BCK[0][i]) + SEPA;
		str = str + "\nModality2" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + formatter.format(BCK[1][i]) + SEPA;
		str = str + "\nDifference" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + formatter.format(BCK[3][i]) + SEPA;
		str = str + "\nCoeff" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + formatter.format(BCKcoeff[0][i]) + SEPA;
		str = str
				+ "\n**********************DBM Results***************************";
		str = str + "\nComponents" + SEPA + "R" + SEPA + "C" + SEPA + "R~C"
				+ SEPA + "T~R" + SEPA + "T~C" + SEPA + "T~R~C";
		str = str + "\nModality1" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + formatter.format(DBM[0][i]) + SEPA;
		str = str + "\nModality2" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + formatter.format(DBM[1][i]) + SEPA;
		str = str + "\nDifference" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + formatter.format(DBM[3][i]) + SEPA;
		str = str + "\nCoeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + formatter.format(DBMcoeff[3][i]) + SEPA;
		str = str
				+ "\n**********************OR Results***************************";
		str = str + "\nComponents" + SEPA + "R" + SEPA + "TR" + SEPA + "COV1"
				+ SEPA + "COV2" + SEPA + "COV3" + SEPA + "ERROR";
		str = str + "\nModality1" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + formatter.format(OR[0][i]) + SEPA;
		str = str + "\nModality2" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + formatter.format(OR[1][i]) + SEPA;
		str = str + "\nDifference" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + formatter.format(OR[3][i]) + SEPA;
		str = str + "\nCoeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + formatter.format(ORcoeff[3][i]) + SEPA;
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
		str = str + "\n" + "Effective Size = "
				+ formatter2.format(statParms[1]) + SEPA
				+ "Significance Level = " + formatter2.format(statParms[0]);

		str = str
				+ "\n*****************************************************************";
		str = str + "\n Sizing Results";
		str = str + resultnew;
		str = str
				+ "\n*****************************************************************\n\n\n";

		return str;
	}

	public String genReport(int flag) {
		double[][] BDG = curRecord.getBDG(0);
		double[][] DBM = curRecord.getDBM(0);
		double[][] BCK = curRecord.getBCK(0);
		double[][] OR = curRecord.getOR(0);
		double[][] BDGcoeff = curRecord.getBDGcoeff();
		double[][] BCKcoeff = curRecord.getBCKcoeff();
		double[][] DBMcoeff = curRecord.getDBMcoeff();
		double[][] ORcoeff = curRecord.getORcoeff();
		// statParms = gui.getSiglevel();
		statParms[0] = Double.parseDouble(sigLevel.getText());
		statParms[1] = Double.parseDouble(effSize.getText());
		String results = gui.getStat1();

		int newR = Integer.parseInt(sizeR.getText());
		int newN = Integer.parseInt(sizeN.getText());
		int newD = Integer.parseInt(sizeD.getText());
		double[][] newBDGcoeff = dbRecord.genBDGCoeff(newR, newN, newD);
		double[][] newDBMcoeff = dbRecord.genDBMCoeff(newR, newN, newD);
		double[][] newBCKcoeff = dbRecord.genBCKCoeff(newR, newN, newD);
		double[][] newORcoeff = dbRecord.genORCoeff(newR, newN, newD);
		// double[][] DBMnew = curRecord.BDG2DBM(BDG,newR,newN,newD);
		// double[][] ORnew = curRecord.BDG2OR(BDG, newR,newN,newD);
		double[][] DBMnew = dbRecord.BCK2DBM(BCK, newR, newN, newD);
		double[][] ORnew = dbRecord.DBM2OR(0, DBMnew, newR, newN, newD);

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
				str = str + formatter.format(BDG[0][i]) + SEPA;
			str = str + "\n" + "Coeff" + SEPA;
			for (int i = 0; i < 8; i++)
				str = str + formatter.format(BDGcoeff[0][i]) + SEPA;
		} else if (selectedManualComp == 1) {
			str = str
					+ "\n**********************BCK Results***************************";
			str = str + "\nMoments" + SEPA + "N" + SEPA + "D" + SEPA + "N~D"
					+ SEPA + "R" + SEPA + "N~R" + SEPA + "D~R" + SEPA + "R~N~D";
			str = str + "\nMoments" + SEPA;
			for (int i = 0; i < 7; i++)
				str = str + formatter.format(BCK[0][i]) + SEPA;
			str = str + "\nCoeff" + SEPA;
			for (int i = 0; i < 7; i++)
				str = str + formatter.format(BCKcoeff[0][i]) + SEPA;
		}

		str = str
				+ "\n**********************DBM Results***************************";
		str = str + "\nComponents" + SEPA + "R" + SEPA + "C" + SEPA + "R~C"
				+ SEPA + "T~R" + SEPA + "T~C" + SEPA + "T~R~C";
		str = str + "\ncompnents" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + formatter.format(DBM[0][i]) + SEPA;
		str = str + "\nCoeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + formatter.format(DBMcoeff[3][i]) + SEPA;
		str = str
				+ "\n**********************OR Results***************************";
		str = str + "\nComponents" + SEPA + "R" + SEPA + "TR" + SEPA + "COV1"
				+ SEPA + "COV2" + SEPA + "COV3" + SEPA + "ERROR";
		str = str + "\ncomponents" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + formatter.format(OR[0][i]) + SEPA;
		str = str + "\nCoeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + formatter.format(ORcoeff[3][i]) + SEPA;
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
		str = str + "\n" + "Effective Size = "
				+ formatter2.format(statParms[1]) + SEPA
				+ "Significance Level = " + formatter2.format(statParms[0]);

		str = str
				+ "\n*****************************************************************";
		str = str + "\n Sizing Results\n";
		str = str + resultnew;
		str = str
				+ "\n*****************************************************************\n\n\n";

		return str;
	}

	/*
	 * radio buttons to choose if study design has paired readers
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

	/*
	 * radio buttons to choose if study design has paired cases
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

	/* button to generate report from dataset */
	class genReportListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			curRecord = gui.getCurrentRecord();
			reportFrame = new JFrame(
					" please copy and paste the report to your own editor");

			reportFrame.getRootPane().setWindowDecorationStyle(
					JRootPane.PLAIN_DIALOG);
			String str = "";
			if (gui.getSelectedInput() == 2)
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

	/* button to size a trial from dataset */
	class sizeTrialListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int[] Parms = { Integer.parseInt(sizeR.getText()),
					Integer.parseInt(sizeN.getText()),
					Integer.parseInt(sizeD.getText()) };
			// int[] Parms={Integer.parseInt(sizeR.getText()),
			// Integer.parseInt(sizeN.getText()),
			// Integer.parseInt(sizeD.getText()),
			// Integer.parseInt(sigLevel.getText()),
			// Integer.parseInt(effSize.getText())};
			double[] Parms1 = { Double.parseDouble(sigLevel.getText()),
					Double.parseDouble(effSize.getText()) };
			int[] Parms2 = { Integer.parseInt(numSplitPlot.getText()),
					pairedRs, pairedCs };
			gui.sizeTrial(Parms, Parms1, Parms2);
		}
	}

	public double[] sizeTrial(double[][] DBM, double[][] OR, double sig,
			double eff, int R, int N, int D) {

		double[] var = new double[3];
		int selectedMod = gui.getSelectedMod();
		int selectedInput = gui.getSelectedInput();
		if (selectedInput == 0 || selectedInput == 1) {
			if (selectedMod == 3) {
				var[0] = DBM[3][3];
				var[1] = DBM[3][4];
				var[2] = DBM[3][5];
			} else {
				var[0] = DBM[selectedMod][0];
				var[1] = DBM[selectedMod][1];
				var[2] = DBM[selectedMod][2];
			}
		}
		if (selectedInput == 2) {
			var[0] = DBM[0][0];
			var[1] = DBM[0][1];
			var[2] = DBM[0][2];

		}

		double totalVar = 1.0 / (R * (N + D))
				* ((N + D) * var[0] + R * var[1] + var[2]);
		if (selectedMod == 3)
			totalVar = totalVar * 2;
		statTest stat = new statTest(var, OR[selectedMod], R, N + D, sig, eff,
				totalVar);

		double[] results = new double[3];
		results[0] = stat.getDOF();
		results[1] = stat.getHillisPower();
		results[2] = stat.getZPower();
		return results;
	}

}
