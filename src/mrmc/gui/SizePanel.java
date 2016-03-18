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

import mrmc.chart.exploreExpSize;
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
	public JRadioButton 
		ButtonPairedReadersYes,
		ButtonPairedReadersNo,
		ButtonPairedNormalsYes,
		ButtonPairedNormalsNo,
		ButtonPairedDiseasedYes,
		ButtonPairedDiseasedNo;

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
	public DecimalFormat threeDecE = new DecimalFormat("0.000E0");
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
		//
		JButton fullyTrial = new JButton("Explore Experiment Size");
		fullyTrial.addActionListener(new fullyTrialListener());
		SizePanelRow2.add(fullyTrial);

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
		ButtonPairedReadersYes = new JRadioButton("Yes");
		ButtonPairedReadersYes.setActionCommand("Yes");
		ButtonPairedReadersNo = new JRadioButton("No");
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
		ButtonPairedNormalsYes = new JRadioButton("Yes");
		ButtonPairedNormalsYes.setActionCommand("Yes");
		ButtonPairedNormalsNo = new JRadioButton("No");
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
		ButtonPairedDiseasedYes = new JRadioButton("Yes");
		ButtonPairedDiseasedYes.setActionCommand("Yes");
		ButtonPairedDiseasedNo = new JRadioButton("No");
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
		DBRecordSize.totalVar = -1.0;
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
		
		// set study design to default 1, yes, yes, yes
	    NumSplitPlotsJTextField.setText("1");
	    numSplitPlots = 1;
		ButtonPairedReadersYes.setSelected(true);
		ButtonPairedReadersNo.setSelected(false);
		pairedReadersFlag=1;
	    ButtonPairedNormalsYes.setSelected(true);
	    ButtonPairedNormalsNo.setSelected(false);
	    pairedNormalsFlag=1;
	    ButtonPairedDiseasedYes.setSelected(true);
	    ButtonPairedDiseasedNo.setSelected(false);
	    pairedDiseasedFlag=1;
	
	}
	
	/**
	 * Sets the trial sizing panel inputs with default values based on the
	 * current record
	 */
	public void setSizePanel() {

		StatTest testSize = DBRecordSize.testSize;
		String output;
		
		//output = "S.E=" 
		//		+ threeDecE.format(Math.sqrt(DBRecordSize.totalVar));
		output = "S.E=" 
				+ threeDecE.format(DBRecordSize.SE);
		
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
		if (DBRecordSize.flagFullyCrossed){
			results = results + "Hillis:" ; 
			results = results + "\t" + SizeJLabelDFHillis.getText();
			results = results + "\t" + SizeJLabelPowerHillis.getText();
			results = results + "\t" + SizeJLabelCIHillis.getText();
		}else{
			results = results + "The Hillis degrees of freedom are not calculated when the data is not fully crossed.";
		}

		return results;
	}

	/**
	 * Gets statistics for new trial sizing in String format
	 * 
	 * @return String of statistics for new trial sizing for export to file
	 */
	public String exportSizeResults() {
		String results = SizeJLabelSqrtVar.getText() + "\r\n";;
		results = results + SizeJLabelTStat.getText();
		results = results +  SizeJLabelPowerNormal.getText();
		results = results + "\r\n";
		results = results + SizeJLabelDFBDG.getText().trim();
		results = results + SizeJLabelPowerBDG.getText();
		results = results + "\r\n";
		if (DBRecordSize.flagFullyCrossed){
		results = results + "Hills:" + SizeJLabelDFHillis.getText();
		results = results + ", " + SizeJLabelPowerHillis.getText();
		}else{
			results = results + "The Hillis degrees of freedom are not calculated when the data is not fully crossed.";
		}
		return results;
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

				boolean sizesucceed = DBRecordSize.DBRecordSizeFill(GUI.SizePanel1);
				if (!sizesucceed)
					return;
				setSizePanel();

			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(reportFrame, "Invalid Input",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Handler for button to size trial based on specified parameters
	 */
	class fullyTrialListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				if (GUI.DBRecordStat.totalVar <= 0.0) {
					JOptionPane.showMessageDialog(GUI.MRMCobject.getFrame(),
							"Must perform variance analysis first.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				sigLevel = Double.parseDouble(SigLevelJTextField.getText());
				effSize = Double.parseDouble(EffSizeJTextField.getText());
				exploreExpSize ees =  new exploreExpSize (DBRecordSize, GUI, GUI.SizePanel1);
				
		//		DBRecordSize.DBRecordSizeFill(GUI.SizePanel1);
			//	setSizePanel();

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
			String hillisValues;
			if (DBRecordSize.flagFullyCrossed){
			hillisValues = "Hillis 2011:"  +"\n"+  
					SizeJLabelDFHillis.getText() +"\n"+ 
					SizeJLabelLambdaHillis.getText() + "\n" + 
					SizeJLabelPowerHillis.getText() + "\n" + 
					SizeJLabelCIHillis.getText();
			}else{
				hillisValues = "The Hillis degrees of freedom are not calculated when the data is not fully crossed.";
			}
					
			// TODO Auto-generated method stub
			JOptionPane.showMessageDialog(reportFrame,
					hillisValues, "Hillis Approximation",
					JOptionPane.PLAIN_MESSAGE);
		}

	}
}
