/**
 * GUInterface.java
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
import java.io.*;
import java.lang.Math;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;

import java.text.DecimalFormat;
import java.util.ArrayList;

import mrmc.chart.BarGraph;
import mrmc.chart.StudyDesignPlot;
import mrmc.chart.ROCCurvePlot;
import mrmc.core.MRMC;
import mrmc.core.DBRecord;
import mrmc.core.InputFile;
import mrmc.core.Matrix;
import mrmc.core.MrmcDB;
import mrmc.core.StatTest;

import org.jfree.ui.RefineryUtilities;

/**
 * This class describes the graphic interface. From top to bottom, the GUI
 * includes <br>
 * 1. Menu bar (References, About, Manual) <br>
 * 2. Input Panel, which uses card layout and has 3 cards <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;1) database as input <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;2) pilot study or raw data input <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;3) manual input components <br>
 * 3. a label of AUC values, size of the study, etc. <br>
 * 4. a table with all components of variance for the origianl study <br>
 * 5. Sizing panel <br>
 * 6. a table with all components of variance for the resulting study <br>
 * 7. a label of statistical analysis resutls <br>
 * 8. database summary panel <br>
 * 
 * <br>
 * 
Workflow possibilities are determined by {@link #selectedInput} <br>
1. If selectedInput == "IMRMC" then reader study data is from .imrmc file: <br>
Click the Browse button ({@link RawStudyCard.brwsButtonListener brwsButtonListener}) 
<ul>
  <li> Resets GUI. 
  <li> Browses for reader study .imrmc file with file chooser.
  <li> Creates {@link mrmc.core.InputFile} object from .imrmc file <br>
  ---- Object contains IDs for readers, cases, modalities
  ---- Object contains core data structures {@link mrmc.core.InputFile#keyedData keyedData}
          and {@link mrmc.core.InputFile#truthVals truthVals}
</ul>
Click the Variance Analysis Button ({@link RawStudyCard.varAnalysisListener})
 * 
 * <br>
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
@SuppressWarnings("unused")
public class GUInterface {
	
	private GUInterface thisGUI = this;
	public MRMC MRMCobject;
	private MrmcDB fdaDB;

//	private DBCard DBC; // Deleted during commit 1047 
	private RawStudyCard RSC;
	private ManualCard MC;

	/*
	 * The SizePanel controls and shows the sizing analysis at the bottom of the GUI
	 */
	SizePanel SizePanel1;	
	/*
	 * InputFile1 object holds all the unprocessed info related to a reader study
	 */
	InputFile InputFile1 = new InputFile();
	/*
	 * DBrecord object holds all the processed info related to a reader study
	 */
	public DBRecord DBRecordStat = new DBRecord(this);
	public DBRecord DBRecordSize = new DBRecord(this);

	/**
	 * These strings describe the different input methods
	 * @see #selectedInput
	 */
	final static String DescInputModeOmrmc = ".omrmc file: Summary info from a reader study";
	final static String DescInputModeImrmc = ".imrmc file: Reader study data";
	final static String DescInputModeManual = "Manual input";

	/**
	 * <code> selectedInput </code> determines the workflow: <br>
	 * ----<code>DescInputModeOmrmc</code> = ".omrmc file: Summary info from a reader study" <br>
	 * ---- <br>
	 * ----<code>DescInputModeImrmc</code> = ".imrmc file: Reader study data" <br>
	 * ---- <br>
	 * ----<code>DescInputModeManual</code> = "Manual input" <br>
 	 * 
 	 */
	private String selectedInput = DescInputModeImrmc;

	/**
	 * the panel that uses CardLayout. There are three cards for three different input.
	 * @see #selectedInput
	 */
	JPanel InputPane;
	/**
	 * the panel that shares different manual input components
	 */
	JPanel manual3;
	final static int USE_MLE = 1;
	final static int NO_MLE = 0;
	final static String NO_MOD = "NO_MOD";
	private int selectedDB = 0;
	private int selectedSummary = 0;
	private boolean hasNegative = false;
	public int FlagMLE = 0;
	private int SummaryFlagMLE = 0;

	JTextField JTextFilename;
	/*
	 * table1 corresponds to the variance analysis, table2 corresponds to the sizing analysis
	 */
	private JTable BDGtable1, BDGtable2, 
					BCKtable1, BCKtable2,
					DBMtable1, DBMtable2,
					ORtable1, ORtable2,
					MStable1, MStable2;

	JTabbedPane tabbedPane1;
	private JTabbedPane tabbedPane2;
	private JLabel BDGvar1, BDGvar2;
	private JLabel BCKvar1, BCKvar2;
	private JLabel DBMvar1, DBMvar2;
	private JLabel ORvar1, ORvar2;
	private JLabel MSvar1, MSvar2;

	/**
	 * Indicates which of the {@link mrmc.gui.RawStudyCard} pull down menus is active <br>
	 * ----0 neither <br>
	 * ----1 first, named in string <code>currmodA</code> <br>
	 * ----2 second, named in string <code>currmodB</code> <br>
	 * ----3 both, named in strings <code>currmodA, currmodB</code> <br>
	 */
	public int selectedMod = 0;
	/** 
	 * Strings holding the names of the modalities to be analyzed, read in with {@link mrmc.core.InputFile}
	 */
	public String currModA, currModB;

	/*
	 * These JLabels make up the StatPanel
	 */
	private JLabel 
		StatJLabel = new JLabel("Statistical Analysis: "),
		StatJLabelAUC = new JLabel(),
		StatJLabelTotalVar = new JLabel(), 
		StatJLabelSqrtTotalVar = new JLabel(), 
		StatJLabelTstat = new JLabel(),
		StatJLabelDFNormal = new JLabel(),
		StatJLabelPValNormal = new JLabel(),
		StatJLabelCINormal = new JLabel(),
		StatJLabelDFBDG = new JLabel(),
		StatJLabelPValBDG = new JLabel(),
		StatJLabelCIBDG = new JLabel(),
		StatJLabelDFHillis = new JLabel(),
		StatJLabelPValHillis = new JLabel(),
		StatJLabelCIHillis = new JLabel();
	
	JPanel SizePanelRow1 = new JPanel(new FlowLayout()),
			SizePanelRow2 = new JPanel(new FlowLayout()),
			SizePanelRow3 = new JPanel(new FlowLayout()),
			SizePanelRow4 = new JPanel(new FlowLayout()),
			SizePanelRow5 = new JPanel(new FlowLayout()),
			SizePanelRow6 = new JPanel(new FlowLayout());

	/*
	 * These JLabels make up the SizePanel
	 */
	JLabel 
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

	DecimalFormat twoDec = new DecimalFormat("0.00");
	DecimalFormat threeDec = new DecimalFormat("0.000");
	DecimalFormat threeDecE = new DecimalFormat("0.000E0");
	DecimalFormat fourDec = new DecimalFormat("0.0000");

 	/**
		 * Sets all GUI components to their default values
		 */
		public void resetGUI() {
			
			InputFile1 = null;
			InputFile1 = new InputFile();
			JTextFilename.setText("");
			resetStatPanel();
			resetTable1();
			resetTable2();
			resetSizePanel();
	
			// Selections
			selectedMod = 0;
// TODO		DBC.setSelectedMod(selectedMod);
			currModA = NO_MOD;
			currModB = NO_MOD;
			RSC.resetModPanel();
			MC.reset();
			
			setFlagMLE(NO_MLE);
// TODO		DBC.setFlagMLE(NO_MLE);
			RSC.setFlagMLE(NO_MLE);
			setTabTitlesBiased(1, false);
			setTabTitlesBiased(2, false);
			enableTabs();

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
	 * Clears all input fields and statistics labels
	 */
	void resetStatPanel() {
		
		StatJLabelAUC.setText("AUC=");
		StatJLabelTstat.setText("Test Stat=");
		StatJLabelSqrtTotalVar.setText("sqrt(total var)=");
		DBRecordStat.totalVar = -1.0;

		StatJLabelDFNormal.setText("df(Normal Approx)= \u221e");
		StatJLabelDFBDG.setText   ("          df(BDG)= ");
		StatJLabelDFHillis.setText("  df(Hillis 2008)= ");

		StatJLabelPValNormal.setText("p-Value=");
		StatJLabelPValBDG.setText   ("p-Value=");
		StatJLabelPValHillis.setText("p-Value=");

		StatJLabelCINormal.setText("Conf. Int.=");
		StatJLabelCIBDG.setText   ("Conf. Int.=");
		StatJLabelCIHillis.setText("Conf. Int.=");

	}

	/**
		 * Generates a table with the specified row and column names
		 * 
		 * @param table Table to make
		 * @param colNames List of column names
		 * @param rowNames List of row names
		 * @return Initialized table
		 */
		public JScrollPane genTable(JTable table, String[] colNames,
				String[] rowNames) {
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			for (int i = 0; i < colNames.length; i++)
				table.getColumnModel().getColumn(i).setHeaderValue(colNames[i]);
	
	//		JList rowHeader = new JList(rowNames);
			JList<String> rowHeader = new JList<String>(rowNames);
			rowHeader.setFixedCellWidth(80);
	
			rowHeader.setFixedCellHeight(table.getRowHeight());
	//		rowHeader.setCellRenderer(new RowHeaderRenderer(table));
			rowHeader.setCellRenderer(new RowHeaderRenderer(table));
	
			JScrollPane scroll = new JScrollPane(table);
			scroll.setRowHeaderView(rowHeader);
			return scroll;
	
		}

	/**
	 * Displays window containing large text area
	 * 
	 * @return TextArea
	 */
	public JTextArea genFrame() {
		JFrame descFrame = new JFrame();
		descFrame.getRootPane()
				.setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
		String str = "";
		JTextArea desc = new JTextArea(str, 18, 40);
		JScrollPane scrollPane = new JScrollPane(desc,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		descFrame.getContentPane().add(scrollPane);
		desc.setLineWrap(true);
		desc.setEditable(false);
		descFrame.pack();
		descFrame.setVisible(true);
		return desc;
	}



	/**
	 * 
	 * @return {@link #selectedInput}
	 */
	public String getSelectedInput() {
		return selectedInput;
	}




	/**
	 * Gets whether analyzing modality 0, 1 or difference
	 * 
	 * @return Which modalities are being analyzed
	 */
	public int getSelectedMod() {
		return selectedMod;
	}

	/**
	 * Gets whether single modality or difference between modalities is being
	 * analyzed when using manually input components
	 * 
	 * @return Whether single modality or difference
	 */
	public int getSingleOrDiff() {
		return MC.getSingleOrDiff();
	}

	/**
	 * Gets whether MLE (bias) is being used for variance components
	 * 
	 * @return Whether MLE is used
	 */
	public int getFlagMLE() {
		return FlagMLE;
	}

	/**
	 * Gets which decomposition of the variance components is being used
	 * 
	 * @return Which decomposition is being used
	 */
	public int getSelectedManualComp() {
		return MC.getSelectedManualComp();
	}

	/**
	 * Gets the AUC in string format from the AUC label
	 * 
	 * @return AUC text
	 */
	public String getAUCoutput() {
		return StatJLabelAUC.getText();
	}

	/**
	 * Gets statistics for variance analysis in String format
	 * 
	 * @return String of statistics for variance analysis
	 */
	public String getStatResults() {
		String results = StatJLabelTstat.getText();
		results = results + "\t" + StatJLabelSqrtTotalVar.getText();
		results = results + "\t" + StatJLabelPValNormal.getText();
		results = results + "\t" + StatJLabelCINormal.getText();
		results = results + "\n";
		results = results + "\t" + StatJLabelDFBDG.getText();
		results = results + "\t" + StatJLabelPValBDG.getText();
		results = results + "\t" + StatJLabelCIBDG.getText();
		results = results + "\n";
		results = results + "\t" + StatJLabelDFHillis.getText();
		results = results + "\t" + StatJLabelPValHillis.getText();
		results = results + "\t" + StatJLabelCIHillis.getText();

		return results;
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
	 * Sets whether bias is being used in variance analysis
	 * 
	 * @param MLEuse Whether to use bias or not
	 */
	public void setFlagMLE(int MLEuse) {
		FlagMLE = MLEuse;
	}

	/**
	 * Sets the trial sizing panel inputs with default values based on the
	 * current record
	 */
	public void setSizePanel() {

		StatTest sizingStat = new StatTest(thisGUI, SizePanel1);

		String output, output2;
		
		output = "SqrtVar=" 
				+ threeDecE.format(Math.sqrt(DBRecordSize.totalVar));
		SizeJLabelSqrtVar.setText(output);
		output = ",  Stat= "
				+ threeDecE.format(sizingStat.tStatCalc);
		SizeJLabelTStat.setText(output);

		output = "Normal Approx:  df= \u221e ,  Power= "
				+ twoDec.format(sizingStat.powerNormal);
		SizeJLabelPowerNormal.setText(output);
		output = ",  Conf. Int.=("
				+ fourDec.format(sizingStat.getCI()[0])
				+ ", "
				+ fourDec.format(sizingStat.getCI()[1])
				+ ")";
//		SizeJLabelCINormal.setText(output);

		output = "          BDG:  df= "
				+ twoDec.format(sizingStat.DF_BDG);
		SizeJLabelDFBDG.setText(output);
		output = ",  Lambda= "
				+ twoDec.format(sizingStat.lambdaBDG);
		SizeJLabelLambdaBDG.setText(output);
		output = ",  Power= "
				+ twoDec.format(sizingStat.powerBDG);
		SizeJLabelPowerBDG.setText(output);
		output = ",  Conf. Int.=("
				+ fourDec.format(sizingStat.getCIDF_BDG()[0])
				+ ", "
				+ fourDec.format(sizingStat.getCIDF_BDG()[1])
				+ ")";
//		SizeJLabelCIBDG.setText(output);

		/*
		 *  Hillis DoF is not applicable for non-fully crossed studies
		 */
		if (SizePanel1.numSplitPlots == 1 
				&& SizePanel1.pairedReadersFlag == 1
				&& SizePanel1.pairedNormalsFlag == 1
				&& SizePanel1.pairedDiseasedFlag ==1) {
			
			output = "   Hillis 2011:  df= "
					+ twoDec.format(sizingStat.DF_Hillis);
			SizeJLabelDFHillis.setText(output);
			output = ",  Lambda= "
					+ twoDec.format(sizingStat.lambdaHillis);
			SizeJLabelLambdaHillis.setText(output);
			output = ",  Power= "
					+ twoDec.format(sizingStat.powerHillis);
			SizeJLabelPowerHillis.setText(output);
			output = ",  Conf. Int.=("
					+ fourDec.format(sizingStat.getCIDF_Hillis()[0])
					+ ", "
					+ fourDec.format(sizingStat.getCIDF_Hillis()[1])
					+ ")";
//			SizeJLabelCIHillis.setText(output);
		} else {
			SizeJLabelDFHillis.setText("  Hillis 2011:  df=");
			SizeJLabelLambdaHillis.setText(",  Lambda=");
			SizeJLabelPowerHillis.setText(",  Power=");
//			SizeJLabelCIHillis.setText("Conf. Int.=");
		}

		if (FlagMLE == USE_MLE) {
			setTabTitlesBiased(2, true);
		} else if (FlagMLE == NO_MLE) {
			setTabTitlesBiased(2, false);
		}

	}

	/**
	 * Sets the statistics labels for the variance analysis table based on the
	 * current record that has been analyzed <br>
	 * 
	 * This method is called from the RawStudyCard, the ManualCard, and the DBCard
	 */
	public void setStatPanel() {
		
		String displayAUC = DBRecordStat.getAUCsReaderAvg(selectedMod);
		String displaySizes = DBRecordStat.getSizes();
		StatJLabelAUC.setText(displayAUC + displaySizes);
		String displaySqrtTotalVar = threeDecE.format(Math.sqrt(DBRecordStat.totalVar));
		StatJLabelSqrtTotalVar.setText("   sqrt(total var)=" + displaySqrtTotalVar);

		double eff;
		double[][] BDGtmp = DBRecordStat.getBDG(FlagMLE);
		double sum = 0;
		for (int i = 1; i < 8; i++)
			sum += (BDGtmp[0][i] - BDGtmp[1][i]);
		if (selectedMod == 1 || selectedMod == 0)
			eff = DBRecordStat.getAUCinNumber(selectedMod) - 0.5;
		else
			eff = Math.abs(DBRecordStat.getAUCinNumber(0)
					- DBRecordStat.getAUCinNumber(1));

		// two modalities are different
		if (sum != 0 || sum == 0 && selectedMod != 3) {
			StatTest varAnalStat = new StatTest(DBRecordStat, selectedMod);
			// tStat
			String output = threeDecE.format(varAnalStat.getTStatEst());
			StatJLabelTstat.setText("  Stat= " + output);
			output = fourDec.format(varAnalStat.getpValF());
			StatJLabelPValNormal.setText("  p-Value= " + output);
			output = fourDec.format(varAnalStat.getCI()[0]);
			String output2 = fourDec.format(varAnalStat.getCI()[1]);
			StatJLabelCINormal.setText("Conf. Int.=(" + output + ", " + output2 + ")");
			output = twoDec.format(varAnalStat.getDF_BDG());
			StatJLabelDFBDG.setText("  df(BDG) = " + output);
			output = fourDec.format(varAnalStat.getpValFBDG());
			StatJLabelPValBDG.setText("  p-Value= " + output);
			output = fourDec.format(varAnalStat.getCIDF_BDG()[0]);
			output2 = fourDec.format(varAnalStat.getCIDF_BDG()[1]);
			StatJLabelCIBDG.setText("Conf. Int.=(" + output + ", " + output2
					+ ")");

			if (DBRecordStat.getFullyCrossedStatus()) {
				output = twoDec.format(varAnalStat.getDF_Hillis());
				StatJLabelDFHillis.setText("  df(Hillis 2008)= " + output);
				output = fourDec.format(varAnalStat.getpValFHillis());
				StatJLabelPValHillis.setText("  p-Value= " + output);
				output = fourDec.format(varAnalStat.getCIDF_Hillis()[0]);
				output2 = fourDec.format(varAnalStat.getCIDF_Hillis()[1]);
				StatJLabelCIHillis.setText("Conf. Int.=(" + output + ", "
						+ output2 + ")");
			} else {
				StatJLabelDFHillis.setText("");
				StatJLabelPValHillis.setText("");
				StatJLabelCIHillis.setText("");
			}

		} else {
			resetStatPanel();

		}
	}

	/**
	 * Gets variance analysis information from current study record, populates
	 * variance analysis table, sets statistics labels
	 */
	public void setTable1() {

		if (selectedInput == DescInputModeImrmc && InputFile1.filename.equals(null)) {
			return;
		}
	
		double[][] BDGdata1 = DBRecord.getBDGTab(selectedMod,
				DBRecordStat.getBDG(FlagMLE), DBRecordStat.getBDGcoeff());
		double[][] BCKdata1 = DBRecord.getBCKTab(selectedMod,
				DBRecordStat.getBCK(FlagMLE), DBRecordStat.getBCKcoeff());
		double[][] DBMdata1 = DBRecord.getDBMTab(selectedMod,
				DBRecordStat.getDBM(FlagMLE), DBRecordStat.getDBMcoeff());
		double[][] ORdata1 = DBRecord.getORTab(selectedMod,
				DBRecordStat.getOR(FlagMLE), DBRecordStat.getORcoeff());
		double[][] MSdata1 = DBRecord.getMSTab(selectedMod,
				DBRecordStat.getMS(FlagMLE), DBRecordStat.getMScoeff());
		double BDGv = Matrix.total(BDGdata1[6]);
		double BCKv = Matrix.total(BCKdata1[6]);
		double DBMv = Matrix.total(DBMdata1[2]);
		double ORv = Matrix.total(ORdata1[2]);
		double MSv = Matrix.total(MSdata1[2]);
	
		double[][][] allTableData = new double[][][] { BDGdata1, BCKdata1,
				DBMdata1, ORdata1, MSdata1 };
	
		fillTable1(allTableData);
		resetTable2();
		
		String output;
		
		output = threeDecE.format(BDGv);
		BDGvar1.setText("total var=" + output);
		output = threeDecE.format(BCKv);
		BCKvar1.setText("total var=" + output);
		output = threeDecE.format(DBMv);
		DBMvar1.setText("total var=" + output);
		output = threeDecE.format(ORv);
		ORvar1.setText("total var=" + output);
		output = threeDecE.format(MSv);
		MSvar1.setText("total var=" + output);
	
		if (FlagMLE == USE_MLE) {
			setTabTitlesBiased(1, true);
		} else if (FlagMLE == NO_MLE) {
			setTabTitlesBiased(1, false);
		}
	}

	/**
	 * Empties out all values in the variance analysis table
	 */
	void resetTable1() {
		
		BDGvar1.setText("total var=");
		BDGvar2.setText("total var=");
		BCKvar1.setText("total var=");
		BCKvar2.setText("total var=");
		DBMvar1.setText("total var=");
		DBMvar2.setText("total var=");
		ORvar1.setText("total var=");
		ORvar2.setText("total var=");
		MSvar1.setText("total var=");
		StatJLabelTotalVar.setText("total var=");

		for (int i = 0; i < BDGtable1.getRowCount(); i++) {
			for (int j = 0; j < 8; j++) {
				BDGtable1.setValueAt(0, i, j);
				BDGtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
			for (int j = 0; j < 7; j++) {
				BCKtable1.setValueAt(0, i, j);
				BCKtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
		}
		for (int i = 0; i < MStable1.getRowCount(); i++) {
			for (int j = 0; j < 6; j++) {
				MStable1.setValueAt(0, i, j);
				MStable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
			for (int j = 0; j < 6; j++) {
				DBMtable1.setValueAt(0, i, j);
				ORtable1.setValueAt(0, i, j);
				DBMtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				ORtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
		}
	}

	/**
	 * Populates variance analysis table with variance components values
	 * 
	 * @param allTableData Contains components, coefficients, totals for all
	 *            decompositions of components of variance
	 */
	private void fillTable1(double[][][] allTableData) {
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 8; j++) {
				BDGtable1.setValueAt(allTableData[0][i][j], i, j);
				BDGtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
			for (int j = 0; j < 7; j++) {
				BCKtable1.setValueAt(allTableData[1][i][j], i, j);
				BCKtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 6; j++) {
				DBMtable1.setValueAt(allTableData[2][i][j], i, j);
				ORtable1.setValueAt(allTableData[3][i][j], i, j);
				MStable1.setValueAt(allTableData[4][i][j], i, j);
				DBMtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				ORtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				MStable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				// if study is not fully crossed, DBM, OR, MS calculation is
				// incorrect, tabs grayed out
			}
		}
	}

	/**
	 * Sets labels of tabs on variance analysis/trial sizing tables to indicate
	 * whether MLE (bias) is being used
	 * 
	 * @param paneNum Indicates whether variance analysis or trial sizing tables
	 *            should be altered
	 * @param MLE Whether or not MLE is being used
	 */
	private void setTabTitlesBiased(int paneNum, boolean MLE) {
		if (paneNum == 1) {
			if (MLE) {
				tabbedPane1.setTitleAt(0, "BDG**");
				tabbedPane1.setTitleAt(1, "BCK**");
				tabbedPane1.setTitleAt(2, "DBM**");
				tabbedPane1.setTitleAt(3, "OR**");
				tabbedPane1.setTitleAt(4, "MS**");
			} else {
				tabbedPane1.setTitleAt(0, "BDG");
				tabbedPane1.setTitleAt(1, "BCK");
				tabbedPane1.setTitleAt(2, "DBM");
				tabbedPane1.setTitleAt(3, "OR");
				tabbedPane1.setTitleAt(4, "MS");
			}
		} else if (paneNum == 2) {
			if (MLE) {
				tabbedPane2.setTitleAt(0, "BDG**");
				tabbedPane2.setTitleAt(1, "BCK**");
				tabbedPane2.setTitleAt(2, "DBM**");
				tabbedPane2.setTitleAt(3, "OR**");
				tabbedPane2.setTitleAt(4, "MS**");
			} else {
				tabbedPane2.setTitleAt(0, "BDG");
				tabbedPane2.setTitleAt(1, "BCK");
				tabbedPane2.setTitleAt(2, "DBM");
				tabbedPane2.setTitleAt(3, "OR");
				tabbedPane2.setTitleAt(4, "MS");
			}
		}
	}



	/**
	 * Initializes the BDG tab
	 * 
	 * @param whichTable Variance analysis table or trial sizing table
	 * @param rowNames Names for row labels of table
	 * @return JPanel containing BDG tab
	 */
	private JPanel makeBDGTab(int whichTable, String[] rowNames) {
		JPanel panelBDG = new JPanel();
		DefaultTableModel dm = new DefaultTableModel(7, 8);
		String[] BDGnames = { "M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8" };
		if (whichTable == 1) {
			BDGtable1 = new JTable(dm);
			JScrollPane BDGscroll = genTable(BDGtable1, BDGnames, rowNames);
			int height = BDGtable1.getRowHeight();
			panelBDG.add(BDGscroll);
			BDGtable1.setPreferredScrollableViewportSize(new Dimension(650,
					height * 8));
			BDGtable1.setFillsViewportHeight(true);
			BDGvar1 = new JLabel("sqrt(Var)=0.00");
			panelBDG.add(BDGvar1);
		} else if (whichTable == 2) {
			BDGtable2 = new JTable(dm);
			JScrollPane BDGscroll = genTable(BDGtable2, BDGnames, rowNames);
			int height = BDGtable2.getRowHeight();
			panelBDG.add(BDGscroll);
			BDGtable2.setPreferredScrollableViewportSize(new Dimension(650,
					height * 8));
			BDGtable2.setFillsViewportHeight(true);
			BDGvar2 = new JLabel("sqrt(Var)=0.00");
			panelBDG.add(BDGvar2);
		}
		return panelBDG;
	}

	/**
	 * Initializes the BCK tab
	 * 
	 * @param whichTable Variance analysis table or trial sizing table
	 * @param rowNames Names for row labels of table
	 * @return JPanel containing BCK tab
	 */
	private JPanel makeBCKTab(int whichTable, String[] rowNames) {
		JPanel panelBCK = new JPanel();
		DefaultTableModel dm = new DefaultTableModel(7, 7);
		String[] BCKnames = { "N", "D", "N~D", "R", "N~R", "D~R", "R~N~D" };
		if (whichTable == 1) {
			BCKtable1 = new JTable(dm);
			JScrollPane BCKscroll = genTable(BCKtable1, BCKnames, rowNames);
			panelBCK.add(BCKscroll);
			int height = BCKtable1.getRowHeight();
			BCKtable1.setPreferredScrollableViewportSize(new Dimension(575,
					height * 8));
			BCKtable1.setFillsViewportHeight(true);
			BCKvar1 = new JLabel("sqrt(Var)=0.00");
			panelBCK.add(BCKvar1);
		} else if (whichTable == 2) {
			BCKtable2 = new JTable(dm);
			JScrollPane BCKscroll = genTable(BCKtable2, BCKnames, rowNames);
			panelBCK.add(BCKscroll);
			int height = BCKtable2.getRowHeight();
			BCKtable2.setPreferredScrollableViewportSize(new Dimension(575,
					height * 8));
			BCKtable2.setFillsViewportHeight(true);
			BCKvar2 = new JLabel("sqrt(Var)=0.00");
			panelBCK.add(BCKvar2);
		}
		return panelBCK;
	}

	/**
	 * Initializes the DBM tab
	 * 
	 * @param whichTable Variance analysis table or trial sizing table
	 * @param rowNames Names for row labels of table
	 * @return JPanel containing DBM tab
	 */
	private JPanel makeDBMTab(int whichTable, String[] rowNames) {
		JPanel panelDBM = new JPanel();
		DefaultTableModel dm = new DefaultTableModel(3, 6);
		String[] DBMnames = { "R", "C", "R~C", "T~R", "T~C", "T~R~C" };
		if (whichTable == 1) {
			DBMtable1 = new JTable(dm);
			JScrollPane DBMscroll = genTable(DBMtable1, DBMnames, rowNames);
			panelDBM.add(DBMscroll);
			int height = DBMtable1.getRowHeight();
			DBMtable1.setPreferredScrollableViewportSize(new Dimension(500,
					height * 4));
			DBMtable1.setFillsViewportHeight(true);
			DBMvar1 = new JLabel("sqrt(Var)=0.00");
			panelDBM.add(DBMvar1);
		} else if (whichTable == 2) {
			DBMtable2 = new JTable(dm);
			JScrollPane DBMscroll = genTable(DBMtable2, DBMnames, rowNames);
			panelDBM.add(DBMscroll);
			int height = DBMtable2.getRowHeight();
			DBMtable2.setPreferredScrollableViewportSize(new Dimension(500,
					height * 4));
			DBMtable2.setFillsViewportHeight(true);
			DBMvar2 = new JLabel("sqrt(Var)=0.00");
			panelDBM.add(DBMvar2);
		}
		return panelDBM;
	}

	/**
	 * Initializes the OR tab
	 * 
	 * @param whichTable Variance analysis table or trial sizing table
	 * @param rowNames Names for row labels of table
	 * @return JPanel containing OR tab
	 */
	private JPanel makeORTab(int whichTable, String[] rowNames) {
		JPanel panelOR = new JPanel();
		DefaultTableModel dm = new DefaultTableModel(3, 6);
		String[] ORnames = { "R", "TR", "COV1", "COV2", "COV3", "ERROR" };
		if (whichTable == 1) {
			ORtable1 = new JTable(dm);
			JScrollPane ORscroll = genTable(ORtable1, ORnames, rowNames);
			panelOR.add(ORscroll);
			int height = ORtable1.getRowHeight();
			ORtable1.setPreferredScrollableViewportSize(new Dimension(500,
					height * 4));
			ORtable1.setFillsViewportHeight(true);
			ORvar1 = new JLabel("sqrt(Var)=0.00");
			panelOR.add(ORvar1);
		} else if (whichTable == 2) {
			ORtable2 = new JTable(dm);
			JScrollPane ORscroll = genTable(ORtable2, ORnames, rowNames);
			panelOR.add(ORscroll);
			int height = ORtable2.getRowHeight();
			ORtable2.setPreferredScrollableViewportSize(new Dimension(500,
					height * 4));
			ORtable2.setFillsViewportHeight(true);
			ORvar2 = new JLabel("sqrt(Var)=0.00");
			panelOR.add(ORvar2);
		}
		return panelOR;
	}

	/**
	 * Initializes the MS tab
	 * 
	 * @param whichTable Variance analysis table or trial sizing table
	 * @param rowNames Names for row labels of table
	 * @return JPanel containing MS tab
	 */
	private JPanel makeMSTab(int whichTable, String[] rowNames) {
		JPanel panelMS = new JPanel();
		DefaultTableModel dm = new DefaultTableModel(3, 6);
		String[] MSnames = { "R", "C", "RC", "MR", "MC", "MRC" };
		if (whichTable == 1) {
			MStable1 = new JTable(dm);
			JScrollPane MSscroll = genTable(MStable1, MSnames, rowNames);
			panelMS.add(MSscroll);
			int height = MStable1.getRowHeight();
			MStable1.setPreferredScrollableViewportSize(new Dimension(500,
					height * 4));
			MStable1.setFillsViewportHeight(true);
			MSvar1 = new JLabel("sqrt(Var)=0.00");
			panelMS.add(MSvar1);
		} else if (whichTable == 2) {
			MStable2 = new JTable(dm);
			JScrollPane MSscroll = genTable(MStable2, MSnames, rowNames);
			panelMS.add(MSscroll);
			int height = MStable2.getRowHeight();
			MStable2.setPreferredScrollableViewportSize(new Dimension(500,
					height * 4));
			MStable2.setFillsViewportHeight(true);
			MSvar2 = new JLabel("sqrt(Var)=0.00");
			panelMS.add(MSvar2);
		}
		return panelMS;
	}

	/**
	 * Enables all tabs of variance analysis and trial sizing tables
	 */
	public void enableTabs() {
		tabbedPane1.setEnabledAt(0, true);
		tabbedPane1.setEnabledAt(1, true);
		tabbedPane1.setEnabledAt(2, true);
		tabbedPane1.setEnabledAt(3, true);
		tabbedPane1.setEnabledAt(4, true);
		tabbedPane2.setEnabledAt(0, true);
		tabbedPane2.setEnabledAt(1, true);
		tabbedPane2.setEnabledAt(2, true);
		tabbedPane2.setEnabledAt(3, true);
		tabbedPane2.setEnabledAt(4, true);
		tabbedPane2.setSelectedIndex(0);
	
	}

	/**
	 * Enables tabs relevant to BCK decomposition when performing variance
	 * analysis on manual component input
	 */
	public void enableBCKTab() {
		tabbedPane1.setEnabledAt(1, true);
		tabbedPane2.setEnabledAt(1, true);
		tabbedPane1.setEnabledAt(2, true);
		tabbedPane1.setEnabledAt(3, true);
		tabbedPane2.setEnabledAt(2, true);
		tabbedPane2.setEnabledAt(3, true);
		tabbedPane1.setSelectedIndex(1);
		tabbedPane2.setSelectedIndex(1);
	}

	/**
	 * Enables tabs relevant to DBM/OR decomposition when performing variance
	 * analysis on manual component input
	 */
	public void enableDBMORTabs() {
		tabbedPane1.setEnabledAt(2, true);
		tabbedPane1.setEnabledAt(3, true);
		tabbedPane2.setEnabledAt(2, true);
		tabbedPane2.setEnabledAt(3, true);
		tabbedPane1.setSelectedIndex(2);
		tabbedPane2.setSelectedIndex(2);
	}

	/**
	 * Disables all tabs of variance analysis and trial sizing tables
	 */
	public void disableTabs() {
		tabbedPane1.setEnabledAt(0, false);
		tabbedPane1.setEnabledAt(1, false);
		tabbedPane1.setEnabledAt(2, false);
		tabbedPane1.setEnabledAt(3, false);
		tabbedPane1.setEnabledAt(4, false);
		tabbedPane2.setEnabledAt(0, false);
		tabbedPane2.setEnabledAt(1, false);
		tabbedPane2.setEnabledAt(2, false);
		tabbedPane2.setEnabledAt(3, false);
		tabbedPane2.setEnabledAt(4, false);
	}

	/**
	 * Empties out all values in the trial sizing table (which is not visible)
	 */
	void resetTable2() {
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 8; j++) {
				BDGtable2.setValueAt(0, i, j);
				BDGtable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
			for (int j = 0; j < 7; j++) {
				BCKtable2.setValueAt(0, i, j);
				BCKtable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 6; j++) {
				MStable2.setValueAt(0, i, j);
				MStable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
			for (int j = 0; j < 6; j++) {
				DBMtable2.setValueAt(0, i, j);
				ORtable2.setValueAt(0, i, j);
				DBMtable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				ORtable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
		}
	}

	/**
	 * Populates variance components table for sizing a new trial (which is
	 * hidden)
	 * 
	 * @param allTableData Contains variance components, coefficients, total for
	 *            all decompositions
	 */
	private void fillTable2(double[][][] allTableData) {
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 8; j++) {
				BDGtable2.setValueAt(allTableData[0][i][j], i, j);
				BDGtable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
			for (int j = 0; j < 7; j++) {
				BCKtable2.setValueAt(allTableData[1][i][j], i, j);
				BCKtable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 6; j++) {
				DBMtable2.setValueAt(allTableData[2][i][j], i, j);
				ORtable2.setValueAt(allTableData[3][i][j], i, j);
				MStable2.setValueAt(allTableData[4][i][j], i, j);
				DBMtable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				ORtable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				MStable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				// if study is not fully crossed, DBM, OR, MS calculation is
				// incorrect, table is blocked out anyway
			}
		}
	}

	/**
	 * Checks whether the total variance is positive
	 * 
	 * @return True if variance is positive; false otherwise.
	 */
	public boolean checkNegative(DBRecord tempRecord) {
		
		// Check if variance estimate is negative
		if(tempRecord.totalVar > 0)
			hasNegative = false;
		else
			hasNegative = true;
				
		if (hasNegative && FlagMLE == NO_MLE) {
			JFrame frame = MRMCobject.getFrame();
			int result = JOptionPane
					.showConfirmDialog(
							frame,
							"There are negative values in the components, do you want\nto proceed with MLE estimates to avoid negatives?");
			if (JOptionPane.CANCEL_OPTION == result) {
				System.out.println("cancel");
			} else if (JOptionPane.YES_OPTION == result) {
// TODO			DBC.setFlagMLE(USE_MLE);
				RSC.setFlagMLE(USE_MLE);
				FlagMLE = USE_MLE;
			} else if (JOptionPane.NO_OPTION == result) {
// TODO			DBC.setFlagMLE(NO_MLE);
				RSC.setFlagMLE(NO_MLE);
				FlagMLE = NO_MLE;
			}

		}

		return true;

	}
	
	/**
	 * Checks whether raw study input file has been loaded, if any modality
	 * has been selected for variance analysis, and if the total variance is negative
	 * 
	 * @return True if file is loaded, modality selected, and variance is positive; false
	 *         otherwise.
	 */
	public boolean checkRawInput() {
		
		// If there is no pilotFile,
		//   then reader scores have not been read
		String name = JTextFilename.getText();
		System.out.println("name=" + name);
		if (name.equals(null) || name.equals("")) {
			JFrame frame = MRMCobject.getFrame();
			JOptionPane.showMessageDialog(frame, "invalid input", " Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// If neither currModA nor currModB have been set, 
		//   then reader scores have not been read
		if (currModA == NO_MOD && currModB == NO_MOD) {
			JFrame frame = MRMCobject.getFrame();
			JOptionPane.showMessageDialog(frame,
					"You must select at least one modality", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;

	}



	/**
	 * Sole constructor, builds and displays the GUI. <br>
	 * ----Creates {@link mrmc.gui.RawStudyCard RSC=RawStudyCard} 
	 * <br>
	 * CALLED FROM: {@link mrmc.core.MRMC#init}
	 * 
	 * 
	 * @param MRMCobjectTemp Application frame
	 * @param cp Container for GUI elements
	 * 
	 */
	public GUInterface(MRMC MRMCobjectTemp, Container cp) {
		MRMCobject = MRMCobjectTemp;
		SizePanel1 = new SizePanel(this);

		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

		// Select Input Pane
		JPanel inputSelectPane = new JPanel();
		inputSelectPane.setLayout(new FlowLayout());
		// Add Text
		JLabel inLabel = new JLabel("Select an input method: ");
		// Add Pull-down select input method
//		String comboBoxItems[] = { DB, Pilot, Manual };
//		String comboBoxItems[] = { Pilot, Manual };
		String comboBoxItems[] = { DescInputModeImrmc };
		// Add Reset button
		JComboBox<String> cb = new JComboBox<String>(comboBoxItems);
		cb.setEditable(false);
		cb.setSelectedIndex(0);
		cb.addActionListener(new inputModListener());
		JButton buttonReset = new JButton("Reset");
		buttonReset.addActionListener(new ResetListener());
		inputSelectPane.add(inLabel);
		inputSelectPane.add(cb);
		inputSelectPane.add(buttonReset);

		// Input method determines panel card to show
		//
		// create pilot/raw study panel
		JPanel InputCardImrmc = new JPanel();
		RSC = new RawStudyCard(InputCardImrmc, this);
		// create manual panel
		JPanel InputCardManual = new JPanel();
		MC = new ManualCard(InputCardManual, this, MRMCobject);
		// create DB panel
// TODO	JPanel CardInputModeDB = new JPanel();
// TODO	DBC = new DBCard(CardInputModeDB, this, MRMCobject);

		// ***********************************************************************
		// ***********Create the panel that contains the "cards".*****************
		// ***********************************************************************
		InputPane = new JPanel(new CardLayout());
//		inputCards.add(CardInputModeDB, DescInputModeDB);
		InputPane.add(InputCardImrmc, DescInputModeImrmc);
		InputPane.add(InputCardManual, DescInputModeManual);

		/*
		 * Generate the statistical analysis panel
		 */
		JPanel StatPanelRow1 = new JPanel();
		StatPanelRow1.add(StatJLabel);
		StatPanelRow1.add(StatJLabelAUC);
		
		JPanel StatPanelRow2 = new JPanel();
		StatPanelRow2.add(StatJLabelSqrtTotalVar);
		StatPanelRow2.add(StatJLabelTstat);

		JPanel StatPanelRow3 = new JPanel();
		StatPanelRow3.add(StatJLabelDFNormal);
		StatPanelRow3.add(StatJLabelPValNormal);
		StatPanelRow3.add(StatJLabelCINormal);

		JPanel StatPanelRow4 = new JPanel();
		StatPanelRow4.add(StatJLabelDFBDG);
		StatPanelRow4.add(StatJLabelPValBDG);
		StatPanelRow4.add(StatJLabelCIBDG);

		JPanel StatPanelRow5 = new JPanel();
		StatPanelRow5.add(StatJLabelDFHillis);
		StatPanelRow5.add(StatJLabelPValHillis);
		StatPanelRow5.add(StatJLabelCIHillis);

		// *******************************************************************
		// *************tabbed panel 1*********************************
		// *********************************************************************
		String[] rowNamesDiff = new String[] { "comp M0", "coeff M0",
				"comp M1", "coeff M1", "comp product", "- coeff product",
				"total" };
		String[] rowNamesSingle = new String[] { "components", "coeff", "total" };

		// Create BDG tab
		JPanel panelBDG1 = makeBDGTab(1, rowNamesDiff);
		// Create BCK tab
		JPanel panelBCK1 = makeBCKTab(1, rowNamesDiff);
		// Create DBM tab
		JPanel panelDBM1 = makeDBMTab(1, rowNamesSingle);
		// Create OR tab
		JPanel panelOR1 = makeORTab(1, rowNamesSingle);
		// create MS tab
		JPanel panelMS1 = makeMSTab(1, rowNamesSingle);

		tabbedPane1 = new JTabbedPane();
		tabbedPane1.addTab("BDG", panelBDG1);
		tabbedPane1.addTab("BCK", panelBCK1);
		tabbedPane1.addTab("DBM", panelDBM1);
		tabbedPane1.addTab("OR", panelOR1);
		tabbedPane1.addTab("MS", panelMS1);

		// *******************************************************************
		// *************tabbed panel 2*********************************
		// *********************************************************************
		// Create BDG tab
		JPanel panelBDG2 = makeBDGTab(2, rowNamesDiff);
		// Create BCK tab
		JPanel panelBCK2 = makeBCKTab(2, rowNamesDiff);
		// Create DBM tab
		JPanel panelDBM2 = makeDBMTab(2, rowNamesSingle);
		// Create OR tab
		JPanel panelOR2 = makeORTab(2, rowNamesSingle);
		// create MS tab
		JPanel panelMS2 = makeMSTab(2, rowNamesSingle);

		tabbedPane2 = new JTabbedPane();
		tabbedPane2.addTab("BDG", panelBDG2);
		tabbedPane2.addTab("BCK", panelBCK2);
		tabbedPane2.addTab("DBM", panelDBM2);
		tabbedPane2.addTab("OR", panelOR2);
		tabbedPane2.addTab("MS", panelMS2);

		/*
		 * Initialize all the elements of the GUI
		 */
		JTextFilename.setText("");
		resetStatPanel();
		resetTable1();
		resetTable2();
		resetSizePanel();

		JPanel panelSep = new JPanel(new BorderLayout());
		panelSep.setBorder(BorderFactory.createEmptyBorder(1, // top
				1, // left
				0, // bottom
				1)); // right
		panelSep.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.CENTER);
		JPanel panelSep2 = new JPanel(new BorderLayout());
		panelSep2.setBorder(BorderFactory.createEmptyBorder(10, // top
				1, // left
				0, // bottom
				1)); // right
		panelSep2.add(new JSeparator(JSeparator.HORIZONTAL),
				BorderLayout.CENTER);
		JPanel panelSep3 = new JPanel(new BorderLayout());
		panelSep3.setBorder(BorderFactory.createEmptyBorder(10, // top
				1, // left
				0, // bottom
				1)); // right
		panelSep3.add(new JSeparator(JSeparator.HORIZONTAL),
				BorderLayout.CENTER);

		// *******************************************************************
		JPanel panelSummary = new JPanel();
		panelSummary.add(new JLabel("GUI Summary:"));
		JButton saveGUI = new JButton("Save to File");
		saveGUI.addActionListener(new SaveGUIButtonListener());
		panelSummary.add(saveGUI);

		panelSummary.add(new JLabel("Database Summary:"));

		// Create the radio buttons.
		String s1 = "Single Modality";
		JRadioButton s1Button = new JRadioButton(s1);
		s1Button.setActionCommand(s1);
		s1Button.setSelected(true);
		String s2 = "Difference";
		JRadioButton s2Button = new JRadioButton(s2);
		s2Button.setActionCommand(s2);
		// Group the radio buttons.
		ButtonGroup groupS = new ButtonGroup();
		groupS.add(s1Button);
		groupS.add(s2Button);
		// Register a listener for the radio buttons.
		SummarySelListener SummaryListener = new SummarySelListener();
		s1Button.addActionListener(SummaryListener);
		s2Button.addActionListener(SummaryListener);
		panelSummary.add(s1Button);
		panelSummary.add(s2Button);

		// create radio buttons.
		panelSummary.add(new JLabel("          Use MLE?"));
		s1 = "Yes";
		JRadioButton s1ButtonMLE = new JRadioButton(s1);
		s1ButtonMLE.setActionCommand(s1);
		s2 = "No";
		JRadioButton s2ButtonMLE = new JRadioButton(s2);
		s2ButtonMLE.setActionCommand(s2);
		s2ButtonMLE.setSelected(true);
		// Group the radio buttons.
		ButtonGroup groupS2 = new ButtonGroup();
		groupS2.add(s1ButtonMLE);
		groupS2.add(s2ButtonMLE);
		// Register a listener for the radio buttons.
		MLESelListener MLEListener = new MLESelListener();
		s1ButtonMLE.addActionListener(MLEListener);
		s2ButtonMLE.addActionListener(MLEListener);
		panelSummary.add(s1ButtonMLE);
		panelSummary.add(s2ButtonMLE);

		JButton bdgBtn = new JButton("BDG");
		bdgBtn.addActionListener(new bdgBtnListener());
		panelSummary.add(bdgBtn);
		JButton dbmBtn = new JButton("DBM");
		dbmBtn.addActionListener(new dbmBtnListener());
		panelSummary.add(dbmBtn);
		JButton bckBtn = new JButton("BCK");
		bckBtn.addActionListener(new bckBtnListener());
		panelSummary.add(bckBtn);
		JButton orBtn = new JButton("OR");
		orBtn.addActionListener(new orBtnListener());
		panelSummary.add(orBtn);

		cp.add(inputSelectPane);
		cp.add(InputPane);
		cp.add(panelSep);
		cp.add(StatPanelRow1);
		cp.add(StatPanelRow2);
		cp.add(StatPanelRow3);
		cp.add(StatPanelRow4);
		cp.add(StatPanelRow5);
		cp.add(tabbedPane1);
		// Hides the trial sizing table
		// cp.add(tabbedPane2);
		// cp.add(panelStat11);
		cp.add(panelSep2);

//		cp.add(SizePanelRow1);
		cp.add(SizePanelRow2);
		cp.add(SizePanelRow3);
		cp.add(SizePanelRow4);
		cp.add(SizePanelRow5);
		cp.add(SizePanelRow6);
		cp.add(panelSep3);
//		cp.add(panelSummary);
	}

	/**
	 * Formats input to cells in variance analysis/trial sizing tables
	 */
	class DecimalFormatRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			DecimalFormat formatter = new DecimalFormat("0.00000E0");
			try {
				value = formatter.format((Number) value);
			} catch (ClassCastException e) {
				// for some reason sometimes value is a string containing
				// char representation of its actual value, so we parse it out
				// value = Double.parseDouble((String) value);
			}
			return super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
		}
	}

	/**
	 * Handler for button to save current GUI state to file
	 */
	class SaveGUIButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			String report = "";
			if (selectedInput == DescInputModeManual) {
				report = SizePanel1.genReport(1);
			} else {
				report = SizePanel1.genReport();
			}

			try {
				JFileChooser fc = new JFileChooser();
				int fcReturn = fc.showOpenDialog((Component) e.getSource());
				if (fcReturn == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					if (!f.exists()) {
						f.createNewFile();
					}
					FileWriter fw = new FileWriter(f.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(report);
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
	 * Handler for button to show database summer with DBM method
	 */
	class dbmBtnListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTextArea desc = genFrame();
			desc.setText(fdaDB.recordsSummary(selectedSummary, SummaryFlagMLE,
					"DBM"));
		}
	}

	/**
	 * Handler for button to show database summer with BDG method
	 */
	class bdgBtnListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTextArea desc = genFrame();
			desc.setText(fdaDB.recordsSummary(selectedSummary, SummaryFlagMLE,
					"BDG"));
		}
	}

	/**
	 * Handler for button to show database summer with BCK method
	 */
	class bckBtnListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTextArea desc = genFrame();
			desc.setText(fdaDB.recordsSummary(selectedSummary, SummaryFlagMLE,
					"BCK"));
		}
	}

	/**
	 * Handler for button to show database summer with OR method
	 */
	class orBtnListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTextArea desc = genFrame();
			desc.setText(fdaDB.recordsSummary(selectedSummary, SummaryFlagMLE, "OR"));
		}
	}

	/**
	 * Handler for drop down menu to select data input source
	 * This changes the pane, what the user sees
	 * It can either be the pane for DB, FILE, MANUAL
	 */
	class inputModListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {

			JComboBox<?> cb = (JComboBox<?>) evt.getSource();
			selectedInput = (String) cb.getSelectedItem();

			CardLayout cl = (CardLayout) (InputPane.getLayout());
			cl.show(InputPane, selectedInput);
			
			resetGUI();
		}
	}

	/**
	 * Handler for input reset button
	 */
	class ResetListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			resetGUI();
		}
	}

	/**
	 * Handler for database description button, displays in a separate window
	 */
	class descButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JFrame descFrame = new JFrame();

			descFrame.getRootPane().setWindowDecorationStyle(
					JRootPane.PLAIN_DIALOG);
			JTextArea desc = new JTextArea(DBRecordStat.getRecordDesc(),
					18, 40);
			JScrollPane scrollPane = new JScrollPane(desc,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			descFrame.getContentPane().add(scrollPane);
			desc.setLineWrap(true);
			desc.setEditable(false);
			descFrame.pack();
			descFrame.setVisible(true);

		}
	}

	/**
	 * Handler for drop-down menu to select a particular record in the internal
	 * database
	 */
	class dbActionListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JComboBox<?> cb = (JComboBox<?>) evt.getSource();
			selectedDB = (int) cb.getSelectedIndex();
		}
	}

	/**
	 * Handler for radio buttons to select wither analyzing single modality or
	 * difference when using database input
	 */
	class SummarySelListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String str = e.getActionCommand();
			if (str.equals("Single Modality")) {
				selectedSummary = 0;
			}
			if (str.equals("Difference")) {
				selectedSummary = 1;
			}
		}
	}

	/**
	 * Handler for radio buttons to select whether or not to use MLE (bias) in
	 * database summary
	 */
	class MLESelListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String str = e.getActionCommand();
			if (str.equals("Yes")) {
				SummaryFlagMLE = USE_MLE;
			}
			if (str.equals("No")) {
				SummaryFlagMLE = NO_MLE;
			}
		}
	}











}
