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
import mrmc.core.InputFile;
import mrmc.core.MRMC;
import mrmc.core.StatTest;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Panel for sizing new trials and generating reports for given input
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
public class SizePanel {
	public static String viewresultnew; // added for saving the results
	private GUInterface GUI;
	private InputFile InputFile1;
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
		
		JButton sizeHillis = new JButton("Hillis Approx");
		JPanel SizePanelRow6 = new JPanel();
		sizeHillis.addActionListener(new SizeHillisButtonListener());
		SizePanelRow6.add(sizeHillis);
		//SizePanelRow6.add(SizeJLabelDFHillis);
		//SizePanelRow6.add(SizeJLabelLambdaHillis);
		//SizePanelRow6.add(SizeJLabelPowerHillis);
		//SizePanelRow6.add(SizeJLabelCIHillis);

		// not ready to add split plot, an pairing readers or cases to sizing panel
		// JPanelSize.add(SizePanelRow1);
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
		
		SizeJLabelSqrtVar.setText("S.E=");
//		SizeJLabelTStat.setText(",  Test Stat=");
		
		SizeJLabelPowerNormal.setText("Large Sample Approx(Normal),  Power=");
//		SizeJLabelCINormal.setText("Conf. Int.=");

		SizeJLabelDFBDG.setText("          BDG:  df=");
		SizeJLabelLambdaBDG.setText(",  Lambda=");
		SizeJLabelPowerBDG.setText(",  Power=");
//		SizeJLabelCIBDG.setText("Conf. Int.=");

		SizeJLabelDFHillis.setText("df=");
		SizeJLabelLambdaHillis.setText("Lambda=");
		SizeJLabelPowerHillis.setText("Power=");
//		SizeJLabelCIHillis.setText("Conf. Int.=");
	
	}
	
	/**
	 * Sets the trial sizing panel inputs with default values based on the
	 * current record
	 */
	public void setSizePanel() {

		StatTest testSize = DBRecordSize.testSize;
		String output;
		
		output = "S.E=" 
				+ threeDecE.format(Math.sqrt(DBRecordSize.totalVar));
		
		
		SizeJLabelSqrtVar.setText(output);
		//output = ",  Stat= "
		//		+ threeDecE.format(testSize.tStatCalc);
		//SizeJLabelTStat.setText(output);

		output = "Large Sample Approx(Normal) ,  Power= "
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
			
			output = "df= "
					+ twoDec.format(testSize.DF_Hillis);
			SizeJLabelDFHillis.setText(output);
			output = "Lambda= "
					+ twoDec.format(testSize.lambdaHillis);
			SizeJLabelLambdaHillis.setText(output);
			output = "Power= "
					+ twoDec.format(testSize.powerHillis);
			SizeJLabelPowerHillis.setText(output);
			output = "Conf. Int.=("
					+ fourDec.format(testSize.ciBotHillis)
					+ ", "
					+ fourDec.format(testSize.ciTopHillis)
					+ ")";
//			SizeJLabelCIHillis.setText(output);
		} else {
			SizeJLabelDFHillis.setText("df=");
			SizeJLabelLambdaHillis.setText("Lambda=");
			SizeJLabelPowerHillis.setText("Power=");
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
		results = results + "\r\n";
		results = results + "\t" + SizeJLabelDFBDG.getText();
		results = results + "\t" + SizeJLabelPowerBDG.getText();
		results = results + "\t" + SizeJLabelCIBDG.getText();
		results = results + "\r\n";
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
	public String genReport(InputFile InputFile) {
		InputFile1 = InputFile;
		int useMLE = DBRecordSize.flagMLE;

//		double[][] BDG = DBRecordSize.BDG;
//		double[][] DBM = DBRecordSize.DBM;
//		double[][] BCK = DBRecordSize.BCK;
//		double[][] OR = DBRecordSize.OR;
//		double[][] MS = DBRecordSize.MS;
		
		double[][] BDG = DBRecordSize.BDGresult;
		double[][] DBM = DBRecordSize.DBMresult;
		double[][] BCK = DBRecordSize.BCKresult;
		double[][] OR = DBRecordSize.ORresult;
		double[][] MS = DBRecordSize.MSresult;
		
		if(useMLE == 1) {
			BDG = DBRecordSize.BDGbiasresult;
			DBM = DBRecordSize.DBMbiasresult;
			BCK = DBRecordSize.BCKbiasresult;
			OR = DBRecordSize.ORbiasresult;
			MS = DBRecordSize.MSbiasresult;
		}
//		double[][] BDGcoeff = DBRecordSize.BDGcoeff;
//		double[][] BCKcoeff = DBRecordSize.BCKcoeff;
//		double[][] DBMcoeff = DBRecordSize.DBMcoeff;
//		double[][] ORcoeff = DBRecordSize.ORcoeff;
//		double[][] MScoeff = DBRecordSize.MScoeff;
		
		double[][] BDGcoeff = DBRecordSize.BDGcoeffresult;
		double[][] BCKcoeff = DBRecordSize.BCKcoeffresult;
		double[][] DBMcoeff = DBRecordSize.DBMcoeffresult;
		double[][] ORcoeff = DBRecordSize.ORcoeffresult;
		double[][] MScoeff = DBRecordSize.MScoeffresult;

		statParms[0] = Double.parseDouble(SigLevelJTextField.getText());
		statParms[1] = Double.parseDouble(EffSizeJTextField.getText());
		String result = GUI.StatPanel1.getStatResults();

		int NreaderSize = Integer.parseInt(NreaderJTextField.getText());
		int NnormalSize = Integer.parseInt(NnormalJTextField.getText());
		int NdiseaseSize = Integer.parseInt(NdiseaseJTextField.getText());

		String resultnew = getSizeResults();
		

		String str = "";
		str = str + "MRMC summary statistics from " +MRMC.versionname + "\r\n";
		str = str + "Summary statistics written to file named:" + "\r\n";
		str = str + GUInterface.summaryfilename + "\r\n";
		str = str + DBRecordSize.recordDesc;

		str = str + "\r\n*****************************************************************\r\n";
		str = str + "Reader=" + Long.toString(GUI.DBRecordStat.Nreader) + "\r\n"
				+ "Normal=" + Long.toString(GUI.DBRecordStat.Nnormal) + "\r\n"
				+ "Disease=" + Long.toString(GUI.DBRecordStat.Ndisease)+"\r\n";
		str = str + "Modality A = " +  GUI.DBRecordStat.modalityA + "\r\n";
		str = str + "Modality B = " + GUI.DBRecordStat.modalityB + "\r\n";
		if (useMLE == 1)
			str = str + "this report uses MLE estimate of components.\r\n";
		
		str = str + "\r\n" + GUI.DBRecordStat.getAUCsReaderAvgString(DBRecordSize.selectedMod);
		str = str + "\r\nStatistical Tests:\r\n" + result + SEPA;

		str = str
				+ "\r\n*****************************************************************\r\n";
		
		
		str = str + "BEGIN SUMMARY\r\n";
		str = str + "NReader=  " + GUI.DBRecordStat.Nreader + "\r\n";
		str = str + "Nnormal=  " + GUI.DBRecordStat.Nnormal + "\r\n";
		str = str + "NDisease= " + GUI.DBRecordStat.Ndisease + "\r\n" + "\r\n";
		str = str + "Modality A = " + GUI.DBRecordStat.modalityA + "\r\n";
		str = str + "Modality B = " + GUI.DBRecordStat.modalityB + "\r\n" + "\r\n";
		str = str + "Reader-Averaged AUCs" + "\r\n";
		str = str +  GUI.DBRecordStat.getAUCsReaderAvgString(GUI.DBRecordStat.selectedMod).replaceAll(",   ", "\r\n") + "\r\n" + "\r\n";
		str = str +  "Reader Specific AUCs" +"\r\n";
		int k=1;
		int IDlength = 0;
		for(String desc_temp : InputFile.readerIDs.keySet() ) {
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
		for(String desc_temp : InputFile.readerIDs.keySet() ) {
		//for (int i = 1; i < GUI.DBRecordStat.Nreader+1; i++){
			str = str + "\r\n";
			for (int i=0; i<Math.max(IDlength,9) - desc_temp.length(); i++){
				str = str + " ";
			}
			str = str + desc_temp;
			str = str+ SEPA + "  " +
					fiveDecE.format(GUI.DBRecordStat.AUCs[k-1][0]) + SEPA + "  " +
					fiveDecE.format(GUI.DBRecordStat.AUCs[k-1][1]) + SEPA;
					if(GUI.DBRecordStat.AUCs[k-1][2]<0)
						str = str + "      " + fiveDecE.format(GUI.DBRecordStat.AUCs[k-1][2]);
					else if (GUI.DBRecordStat.AUCs[k-1][2]>0)
						str = str + "       " + fiveDecE.format(GUI.DBRecordStat.AUCs[k-1][2]);
					else
						str = str + "        " + fiveDecE.format(GUI.DBRecordStat.AUCs[k-1][2]);
			k=k+1;
		}
		str = str + "\r\n**********************BDG Moments***************************\r\n";
		str = str + "         Moments" + SEPA + "         M1" + SEPA + "         M2" + SEPA + "         M3" + SEPA
				+ "         M4" + SEPA + "         M5" + SEPA + "         M6" + SEPA + "         M7" + SEPA + "         M8"
				+ "\r\n";
		str = str + "Modality1(AUC_A)" + SEPA;
		for (int i = 0; i < 8; i++){
			if(GUI.DBRecordStat.BDG[0][i]>0)
				str = str + " " + fiveDecE.format(GUI.DBRecordStat.BDG[0][i])+SEPA;
			else
				str = str + "  " + fiveDecE.format(GUI.DBRecordStat.BDG[0][i])+SEPA;
		}
		str = str + "\r\n" + "Modality2(AUC_B)" + SEPA;
		for (int i = 0; i < 8; i++){
			if(GUI.DBRecordStat.BDG[1][i]>0)
				str = str + " " + fiveDecE.format(GUI.DBRecordStat.BDG[1][i])+SEPA;
			else
				str = str + "  " + fiveDecE.format(GUI.DBRecordStat.BDG[1][i])+SEPA;
		}
		str = str + "\r\n" + "    comp product" + SEPA;
		for (int i = 0; i < 8; i++){
			if(GUI.DBRecordStat.BDG[2][i]>0)
				str = str + " " + fiveDecE.format(GUI.DBRecordStat.BDG[2][i])+SEPA;
			else
				str = str + "  " + fiveDecE.format(GUI.DBRecordStat.BDG[2][i])+SEPA;
		}
		str = str +"\r\n"; 
		str = str +"END SUMMARY \r\n"; 
		
		
		str = str + "\r\n**********************BDG Results***************************\r\n";
		str = str + "         Moments" + SEPA + "         M1" + SEPA + "         M2" + SEPA + "         M3" + SEPA
				+ "         M4" + SEPA + "         M5" + SEPA + "         M6" + SEPA + "         M7" + SEPA + "         M8"
				+ "\r\n";
		str = str + "Modality1(AUC_A)" + SEPA;
		for (int i = 0; i < 8; i++){
			if(BDG[0][i]>0)
				str = str + " " +fiveDecE.format(BDG[0][i])+SEPA;
			else
				str = str + "  " + fiveDecE.format(BDG[0][i])+SEPA;
		}
		str = str + "\r\n" + "Modality2(AUC_B)" + SEPA;
		for (int i = 0; i < 8; i++){
			if(BDG[1][i]>0)
				str = str + " " +fiveDecE.format(BDG[1][i])+SEPA;
			else
				str = str + "  " + fiveDecE.format(BDG[1][i])+SEPA;
		}
		str = str + "\r\n" + "    comp product" + SEPA;
		for (int i = 0; i < 8; i++){
			if(BDG[2][i]>0)
				str = str + " " +fiveDecE.format(BDG[2][i])+SEPA;
			else
				str = str + "  " + fiveDecE.format(BDG[2][i])+SEPA;
		}
		str = str +"\r\n"; 
		
		str = str
				+ "\r\n**********************BCK Results***************************";
		str = str + "\r\nMoments" + SEPA + "N" + SEPA + "D" + SEPA + "N~D" + SEPA
				+ "R" + SEPA + "N~R" + SEPA + "D~R" + SEPA + "R~N~D";
		str = str + "\r\nModality1(AUC_A)" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCK[0][i]) + SEPA;
		str = str + "\r\nModality2(AUC_B)" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCK[1][i]) + SEPA;
		str = str + "\r\nDifference(AUC_A - AUC_B)" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCK[3][i]) + SEPA;
		str = str + "\r\nCoeff" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCKcoeff[0][i]) + SEPA;
		str = str +"\r\n"; 
		str = str
				+ "\r\n**********************DBM Results***************************";
		str = str + "\r\nComponents" + SEPA + "R" + SEPA + "C" + SEPA + "R~C"
				+ SEPA + "T~R" + SEPA + "T~C" + SEPA + "T~R~C";
		str = str + "\r\nModality1(AUC_A)" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBM[0][i]) + SEPA;
		str = str + "\r\nModality2(AUC_B)" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBM[1][i]) + SEPA;
		str = str + "\r\nDifference(AUC_A - AUC_B)" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBM[3][i]) + SEPA;
		str = str + "\r\nCoeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBMcoeff[3][i]) + SEPA;
		str = str +"\r\n"; 
		str = str
				+ "\r\n**********************OR Results***************************";
		str = str + "\r\nComponents" + SEPA + "R" + SEPA + "TR" + SEPA + "COV1"
				+ SEPA + "COV2" + SEPA + "COV3" + SEPA + "ERROR";
		str = str + "\r\nModality1(AUC_A)" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(OR[0][i]) + SEPA;
		str = str + "\r\nModality2(AUC_B)" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(OR[1][i]) + SEPA;
		str = str + "\r\nDifference(AUC_A - AUC_B)" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(OR[3][i]) + SEPA;
		str = str + "\r\nCoeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(ORcoeff[3][i]) + SEPA;
		str = str +"\r\n"; 
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
		str = str
				+ "\r\n*********************Sizing parameters***************************";
		str = str + "\r\n" + "Effective Size = " + twoDec.format(statParms[1])
				+ SEPA + "Significance Level = " + twoDec.format(statParms[0])+"\r\n";
		str = str + "NReaderSize=  " +NreaderSize + SEPA
		          + "NnormalSize=  " + NnormalSize + SEPA
		          + "NDiseaseSize= " + NdiseaseSize ;
		str = str 
				+ "\r\n*****************************************************************";
		str = str + "\r\nSizing Results:\r\n";
		str = str + resultnew;
		str = str
				+ "\r\n*****************************************************************\r\n";

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
			if (GUI.getSelectedInput() == GUInterface.DescInputChooseMode)
				str = genReport(InputFile1);
			else
				str = genReport(InputFile1);
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
	
	/**
	 * Handler for button to Hillis Approx on specified parameters
	 */
	public class SizeHillisButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String hillisValues = "Hillis 2011:"  +"\n"+  
					SizeJLabelDFHillis.getText() +"\n"+ 
					SizeJLabelLambdaHillis.getText() + "\n" + 
					SizeJLabelPowerHillis.getText() + "\n" + 
					SizeJLabelCIHillis.getText();
					
			// TODO Auto-generated method stub
			JOptionPane.showMessageDialog(reportFrame,
					hillisValues, "Hillis Approximation",
					JOptionPane.PLAIN_MESSAGE);
		}

	}
}
