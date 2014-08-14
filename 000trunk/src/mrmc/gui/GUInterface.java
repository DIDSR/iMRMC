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
Click the Browse button ({@link GUInterface.brwsButtonListener brwsButtonListener}) 
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
	JPanel inputCards;
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
	private int FlagMLE = 0;
	private int SummaryFlagMLE = 0;

	JTextField pilotFile;
	private String filename = "";
	private MRMC MRMCobject;
	private MrmcDB fdaDB;
	InputFile currentInputFile;
	DBRecord currentDBrecord;

	private DBCard DBC;
	private RawStudyCard RSC;
	private ManualCard MC;

	private JTable BDGtable1, BDGtable2;
	private JTable BCKtable1, BCKtable2;
	private JTable DBMtable1, DBMtable2;
	private JTable ORtable1, ORtable2;
	private JTable MStable1, MStable2;
	private JLabel aucOutput;
	private SizePanel genSP;
	private JTabbedPane tabbedPane1, tabbedPane2;
	private JLabel BDGvar1, BDGvar2;
	private JLabel BCKvar1, BCKvar2;
	private JLabel DBMvar1, DBMvar2;
	private JLabel ORvar1, ORvar2;
	private JLabel MSvar1, sizedSqrtVar;

	/**
	 * Indicates which of the {@link mrmc.gui.RawStudyCard} pull down menus is active <br>
	 * ----0 neither <br>
	 * ----1 first, named in string <code>currmodA</code> <br>
	 * ----2 second, named in string <code>currmodB</code> <br>
	 * ----3 both, named in strings <code>currmodA, currmodB</code> <br>
	 */
	int selectedMod = 0;
	/** 
	 * Strings holding the names of the modalities to be analyzed, read in with {@link mrmc.core.InputFile}
	 */
	String currModA, currModB;

	private JLabel HillisPowerWithdfHillis;
	private JLabel ZPower;
	private JLabel sizedStat;
	private JLabel sizedDFHillis;
	private JLabel CVF;
	private JLabel stat;
	private JLabel TotalVar_JLabel;
	private JLabel sqrtTotalVar_JLabel;
	private JLabel dfHillis;
	private JLabel pVal;
	private JLabel confInt;
	private JLabel dfBDG;
	private JLabel sizedDFBDG;

	DecimalFormat twoDec = new DecimalFormat("0.00");
	DecimalFormat threeDec = new DecimalFormat("0.000");
	DecimalFormat threeDecE = new DecimalFormat("0.000E0");
	DecimalFormat fourDec = new DecimalFormat("0.0000");
	private JLabel pValWithDFBDG;
	private JLabel confIntWithDFBDG;
	private JLabel pValWithDFHillis;
	private JLabel confIntWithDFHillis;
	private JLabel HillisPowerWithdfBDG;
	private JLabel sizedCIzTest;
	private JLabel sizedCIWithDFBDG;
	private JLabel sizedCIWithDFHillis;

 	/**
		 * Sets all GUI components to their default values
		 */
		public void resetGUI() {
			
			currentInputFile = null;
			pilotFile.setText("");
			reset1stStatPanel();
			resetTable1();
			resetTable2();
			resetSizePanel();
	
			// Selections
			selectedMod = 0;
// TODO		DBC.setSelectedMod(selectedMod);
			RSC.selectedMod = selectedMod;
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
		
		sizedSqrtVar.setText("sqrt(Var)=");
		ZPower.setText("Power(Z test)=");
		sizedStat.setText("Stat=");
		sizedDFHillis.setText("df(Hillis 2008)=");
		sizedDFBDG.setText("df(BDG)=");
		CVF.setText("CVF=");
		sizedCIzTest.setText("Conf. Int.=");
		sizedCIWithDFBDG.setText("Conf. Int.=");
		sizedCIWithDFHillis.setText("Conf. Int.=");
	
		HillisPowerWithdfHillis.setText("Power(Hillis 2011)=");
		HillisPowerWithdfBDG.setText("Power(Hillis 2011)=");

	}
	
	/**
	 * Clears all input fields and statistics labels
	 */
	void reset1stStatPanel() {
		
		aucOutput.setText("");
		stat.setText("Test Stat=");
		sqrtTotalVar_JLabel.setText("sqrt(total var)=");

		pVal.setText("p-Value=");
		confInt.setText("Conf. Int.=");

		dfBDG.setText("df(BDG)=");
		pValWithDFBDG.setText("p-Value=");
		confIntWithDFBDG.setText("Conf. Int.=");

		dfHillis.setText("df(Hillis 2008)=");
		pValWithDFHillis.setText("p-Value=");
		confIntWithDFHillis.setText("Conf. Int.=");

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
	 * Set the current study record being analyzed. <br>
	 * This function refers to {@link #selectedInput}
	 *  
	 * @return The current record
	 */
	public DBRecord setCurrentRecord() {
		DBRecord tempRecord = null;

//		if (selectedInput == SELECT_DB)
//			tempRecord = Records[selectedDB];
//		else if (selectedInput == SELECT_FILE)
//			tempRecord = currentDBrecord;
//		else if (selectedInput == SELECT_MANUAL) {
//			tempRecord = MC.getManualRecord();
//		}

		if (selectedInput == DescInputModeImrmc)
			tempRecord = currentDBrecord;
		
		return tempRecord;
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
		return aucOutput.getText();
	}

	/**
	 * Gets statistics for variance analysis in String format
	 * 
	 * @return String of statistics for variance analysis
	 */
	public String getStat1() {
		String results = stat.getText();
		results = results + "\t" + sqrtTotalVar_JLabel.getText();
		results = results + "\t" + pVal.getText();
		results = results + "\t" + confInt.getText();
		results = results + "\n";
		results = results + "\t" + dfBDG.getText();
		results = results + "\t" + pValWithDFBDG.getText();
		results = results + "\t" + confIntWithDFBDG.getText();
		results = results + "\n";
		results = results + "\t" + dfHillis.getText();
		results = results + "\t" + pValWithDFHillis.getText();
		results = results + "\t" + confIntWithDFHillis.getText();

		return results;
	}

	/**
	 * Gets statistics for new trial sizing in String format
	 * 
	 * @return String of statistics for new trial sizing
	 */
	public String getStat2() {
		String results = sizedSqrtVar.getText();
		results = results + "\t" + sizedStat.getText();
		results = results + "\t" + CVF.getText();
		results = results + "\t" + ZPower.getText();
		results = results + "\t" + sizedCIzTest.getText();
		results = results + "\n";
		results = results + "\t" + sizedDFBDG.getText();
		results = results + "\t" + HillisPowerWithdfBDG.getText();
		results = results + "\t" + sizedCIWithDFBDG.getText();
		results = results + "\n";
		results = results + "\t" + sizedDFHillis.getText();
		results = results + "\t" + HillisPowerWithdfHillis.getText();
		results = results + "\t" + sizedCIWithDFHillis.getText();

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
		DBRecord tempRecord = setCurrentRecord();
		long[] params = tempRecord.getSizesInt();
		genSP.setNumbers(params);
	}

	/**
	 * Sets the statistics labels for the variance analysis table based on the
	 * current record that has been analyzed
	 */
	public void set1stStatPanel() {
		
		DBRecord tempRecord = setCurrentRecord();
		
		String displayAUC = tempRecord.getAUCsReaderAvg(selectedMod);
		String displayParam = tempRecord.getSizes();
		aucOutput.setText(displayAUC + displayParam);
//		System.out.println("displayParam:");
//		System.out.println(displayParam);
		
		sqrtTotalVar_JLabel.setText("   sqrt(total var)=" + threeDecE.format(Math.sqrt(tempRecord.totalVar)));

		double eff;
		double[][] BDGtmp = tempRecord.getBDG(FlagMLE);
		double sum = 0;
		for (int i = 1; i < 8; i++)
			sum += (BDGtmp[0][i] - BDGtmp[1][i]);
		if (selectedMod == 1 || selectedMod == 0)
			eff = tempRecord.getAUCinNumber(selectedMod) - 0.5;
		else
			eff = Math.abs(tempRecord.getAUCinNumber(0)
					- tempRecord.getAUCinNumber(1));

		// two modalities are different
		if (sum != 0 || sum == 0 && selectedMod != 3) {
			StatTest varAnalStat = new StatTest(tempRecord, selectedMod,
					FlagMLE, 0.05, eff);
			// tStat
			String output = threeDecE.format(varAnalStat.getTStatEst());
			stat.setText("  Stat= " + output);
			output = fourDec.format(varAnalStat.getpValF());
			pVal.setText("  p-Value= " + output);
			output = fourDec.format(varAnalStat.getCI()[0]);
			String output2 = fourDec.format(varAnalStat.getCI()[1]);
			confInt.setText("Conf. Int.=(" + output + ", " + output2 + ")");
			output = twoDec.format(varAnalStat.getDF_BDG());
			dfBDG.setText("  df(BDG) = " + output);
			output = fourDec.format(varAnalStat.getpValFBDG());
			pValWithDFBDG.setText("  p-Value= " + output);
			output = fourDec.format(varAnalStat.getCIDF_BDG()[0]);
			output2 = fourDec.format(varAnalStat.getCIDF_BDG()[1]);
			confIntWithDFBDG.setText("Conf. Int.=(" + output + ", " + output2
					+ ")");

			if (tempRecord.getFullyCrossedStatus()) {
				output = twoDec.format(varAnalStat.getDF_Hillis());
				dfHillis.setText("  df(Hillis 2008)= " + output);
				output = fourDec.format(varAnalStat.getpValFHillis());
				pValWithDFHillis.setText("  p-Value= " + output);
				output = fourDec.format(varAnalStat.getCIDF_Hillis()[0]);
				output2 = fourDec.format(varAnalStat.getCIDF_Hillis()[1]);
				confIntWithDFHillis.setText("Conf. Int.=(" + output + ", "
						+ output2 + ")");
			} else {
				dfHillis.setText("");
				pValWithDFHillis.setText("");
				confIntWithDFHillis.setText("");
			}

		} else {
			TotalVar_JLabel.setText("  total var= ");
			sqrtTotalVar_JLabel.setText("  sqrt(total var)= ");
			stat.setText("  Stat= ");
			dfHillis.setText("  df(Hillis 2008)= ");
			pVal.setText("  p-Value= ");
			confInt.setText("Conf. Int.=");

			dfBDG.setText("  df(BDG)= ");
			pValWithDFBDG.setText("  p-Value= ");

		}
	}

	/**
	 * Gets variance analysis information from current study record, populates
	 * variance analysis table, sets statistics labels
	 */
	public void setTable1() {
		DBRecord tempRecord = setCurrentRecord();
	
		if (selectedInput == DescInputModeImrmc && filename.equals(null)) {
			return;
		}
	
		double[][] BDGdata1 = DBRecord.getBDGTab(selectedMod,
				tempRecord.getBDG(FlagMLE), tempRecord.getBDGcoeff());
		double[][] BCKdata1 = DBRecord.getBCKTab(selectedMod,
				tempRecord.getBCK(FlagMLE), tempRecord.getBCKcoeff());
		double[][] DBMdata1 = DBRecord.getDBMTab(selectedMod,
				tempRecord.getDBM(FlagMLE), tempRecord.getDBMcoeff());
		double[][] ORdata1 = DBRecord.getORTab(selectedMod,
				tempRecord.getOR(FlagMLE), tempRecord.getORcoeff());
		double[][] MSdata1 = DBRecord.getMSTab(selectedMod,
				tempRecord.getMS(FlagMLE), tempRecord.getMScoeff());
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
		TotalVar_JLabel.setText("total var=");

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
			sizedSqrtVar = new JLabel("sqrt(Var)=0.00");
			panelMS.add(sizedSqrtVar);
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
	public boolean checkNegative() {
		
		DBRecord tempRecord = setCurrentRecord();

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
		String name = pilotFile.getText();
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
	 * Performs calculations for sizing a new trial based on parameters
	 * specified. Sets GUI label with statistics info.
	 * 
	 * @param sizeParams New experiment size parameters
	 * @param sigEffParams Significance level and effect size parameters
	 * @param splitPlotPairParms Number of split plot groups and pairing of
	 *            readers/cases
	 */
	public void sizeTrial(int[] sizeParams, double[] sigEffParams,
			int[] splitPlotPairParms) {
		int newR = sizeParams[0];
		int newN = sizeParams[1];
		int newD = sizeParams[2];
		int numSplitPlots = splitPlotPairParms[0];
		int pairedReaders = splitPlotPairParms[1];
		int pairedCases = splitPlotPairParms[2];
		
		DBRecord tempRecord = setCurrentRecord();

		if (tempRecord == null) {
			JOptionPane.showMessageDialog(MRMCobject.getFrame(),
					"Must perform variance analysis first.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		double[] aucs = { tempRecord.getAUCinNumber(0),
				tempRecord.getAUCinNumber(1) };
		int[][][][] design = createSplitPlotDesign(newR, newN, newD,
				numSplitPlots, pairedReaders, pairedCases);

		/* Below all the coefficients assume a fully-crossed study */
//		double[][] BDGcoeff = currentDBrecord.getBDGcoeff();
		double[][] BDGcoeff = DBRecord.genBDGCoeff(newR,  newN,  newD);
		double[][] BCKcoeff = DBRecord.genBCKCoeff(BDGcoeff);
		double[][] DBMcoeff = DBRecord.genDBMCoeff(newR, newN, newD);
		double[][] ORcoeff = DBRecord.genORCoeff(newR, newN, newD);
		double[][] MScoeff = DBRecord.genMSCoeff(newR, newN, newD);

		double[][] BDG = new double[4][8];
		double[][] BCK = new double[4][7];
		double[][] DBM = new double[4][6];
		double[][] OR = new double[4][6];
		double[][] MS = new double[4][6];
		if (selectedInput == DescInputModeManual && MC.getSelectedManualComp() != 0) {
			BDG = tempRecord.getBDG(NO_MLE);
			if (MC.getSelectedManualComp() == 1) {
				// ******* Brandon's new formula
				// BCK = tempRecord.getBCK(0);
				// JFrame frame = MRMCobject.getFrame();
				// JOptionPane.showMessageDialog(frame,
				// "This function is not implemented for BCK manual input",
				// "error",
				// JOptionPane.ERROR_MESSAGE);
				// return ;
				BCK = tempRecord.BCKresize(tempRecord.getBCK(FlagMLE), newR,
						newN, newD);
				DBM = tempRecord.DBMresize(tempRecord.getDBM(FlagMLE), newR,
						newN, newD);
				OR = DBRecord.DBM2OR(0, DBM, newR, newN, newD);
				MS = DBRecord.DBM2MS(DBM, newR, newN, newD);
			} else if (MC.getSelectedManualComp() == 2) {// DBM input is used
				DBM = tempRecord.DBMresize(tempRecord.getDBM(FlagMLE), newR,
						newN, newD);
				OR = DBRecord.DBM2OR(0, DBM, newR, newN, newD);
				MS = DBRecord.DBM2MS(DBM, newR, newN, newD);
			} else if (MC.getSelectedManualComp() == 3) // OR input is used
			{
				DBM = DBRecord.DBM2OR(1, tempRecord.getOR(FlagMLE), newR, newN,
						newD);
				DBM = tempRecord.DBMresize(DBM, newR, newN, newD);
				OR = DBRecord.DBM2OR(0, DBM, newR, newN, newD);
				MS = DBRecord.DBM2MS(DBM, newR, newN, newD);
			} else if (MC.getSelectedManualComp() == 4) // MS input is used
			{
				DBM = DBRecord.DBM2OR(1, tempRecord.getOR(FlagMLE), newR, newN,
						newD);
				DBM = tempRecord.DBMresize(DBM, newR, newN, newD);
				MS = DBRecord.DBM2MS(DBM, newR, newN, newD);
			}

		} else {
			BDG = tempRecord.getBDG(FlagMLE);
			BCK = DBRecord.BDG2BCK(BDG);
			DBM = DBRecord.BCK2DBM(BCK, newR, newN, newD);
			OR = DBRecord.DBM2OR(0, DBM, newR, newN, newD);
			MS = DBRecord.DBM2MS(DBM, newR, newN, newD);
		}

		if (selectedInput == DescInputModeManual) {
			BDG[3] = BDG[0];
			BCK[3] = BCK[0];
			DBM[3][0] = 0;
			DBM[3][1] = 0;
			DBM[3][2] = 0;
			DBM[3][3] = DBM[0][0];
			DBM[3][4] = DBM[0][1];
			DBM[3][5] = DBM[0][2];
			OR = DBRecord.DBM2OR(0, DBM, newR, newN, newD);
			MS = DBRecord.DBM2MS(DBM, newR, newN, newD);

		}

		double[][] BDGdata = DBRecord.getBDGTab(selectedMod, BDG, BDGcoeff);
		double[][] BCKdata = DBRecord.getBCKTab(selectedMod, BCK, BCKcoeff);
		double[][] DBMdata = DBRecord.getDBMTab(selectedMod, DBM, DBMcoeff);
		double[][] ORdata = DBRecord.getORTab(selectedMod, OR, ORcoeff);
		double[][] MSdata = DBRecord.getMSTab(selectedMod, MS, MScoeff);

		double BDGv = Matrix.total(BDGdata[6]);
		double BCKv = Matrix.total(BCKdata[6]);
		double DBMv = Matrix.total(DBMdata[2]);
		double ORv = Matrix.total(ORdata[2]);
		double MSv = Matrix.total(MSdata[2]);

		double[][][] allTableData = new double[][][] { BDGdata, BCKdata,
				DBMdata, ORdata, MSdata };
		fillTable2(allTableData);

		String output = threeDec.format(Math.sqrt(BDGv));
		BDGvar2.setText("sqrt(Var)=" + output);
		output = threeDec.format(Math.sqrt(BCKv));
		BCKvar2.setText("sqrt(Var)=" + output);
		output = threeDec.format(Math.sqrt(DBMv));
		DBMvar2.setText("sqrt(Var)=" + output);
		output = threeDec.format(Math.sqrt(ORv));
		ORvar2.setText("sqrt(Var)=" + output);
		output = threeDec.format(Math.sqrt(MSv));
		sizedSqrtVar.setText("sqrt(Var)=" + output);

		double[] var = new double[3];
		double sig = sigEffParams[0];
		double eff = sigEffParams[1];
		System.out.println("inside GUI sig=" + sig + " eff=" + eff);
		if (selectedMod == 1 || selectedMod == 0) {
			// eff=tempRecord.getAUCinNumber(selectedMod)-eff;
			// if (eff <0)
			// eff = -1.0 * eff;
			var[0] = DBM[selectedMod][0];
			var[1] = DBM[selectedMod][1];
			var[2] = DBM[selectedMod][2];
		} else {
			var[0] = DBM[3][3];
			var[1] = DBM[3][4];
			var[2] = DBM[3][5];
		}

//		double[][] BCKbias = DBRecord.getBCKTab(selectedMod, BCK, BCKcoeff);
		StatTest sizingStat = new StatTest(var, newR, newN, newD, sig, eff,
				BDGv, tempRecord.getBCK(NO_MLE), aucs, selectedMod);
		output = twoDec.format(sizingStat.getZPower());
		ZPower.setText("  Power(Z test)= " + output);
		output = fourDec.format(sizingStat.getCI()[0]);
		String output2 = fourDec.format(sizingStat.getCI()[1]);
		sizedCIzTest.setText("Conf. Int.=(" + output + ", " + output2 + ")");
		output = twoDec.format(sizingStat.getHillisPowerWithBDGDF());
		HillisPowerWithdfBDG.setText("      Power(Hillis 2011) = " + output);
		output = fourDec.format(sizingStat.getCIDF_BDG()[0]);
		output2 = fourDec.format(sizingStat.getCIDF_BDG()[1]);
		sizedCIWithDFBDG
				.setText("Conf. Int.=(" + output + ", " + output2 + ")");

		output = threeDecE.format(sizingStat.getTStatCalc());
		sizedStat.setText("  Stat= " + output);
		output = twoDec.format(sizingStat.getDF_BDG());
		sizedDFBDG.setText("  df(BDG) = " + output);
		output = twoDec.format(sizingStat.getCVF());
		CVF.setText("  CVF= " + output);

		// Hillis DoF is not applicable for non-fully crossed studies
		if (tempRecord.getFullyCrossedStatus()) {
			output = twoDec.format(sizingStat.getDF_Hillis());
			sizedDFHillis.setText("  df(Hillis 2008)= " + output);
			output = twoDec.format(sizingStat.getHillisPowerWithHillisDF());
			HillisPowerWithdfHillis.setText("      Power(Hillis 2011) = "
					+ output);
			output = fourDec.format(sizingStat.getCIDF_Hillis()[0]);
			output2 = fourDec.format(sizingStat.getCIDF_Hillis()[1]);
			sizedCIWithDFHillis.setText("Conf. Int.=(" + output + ", "
					+ output2 + ")");
		} else {
			sizedDFHillis.setText("");
			HillisPowerWithdfHillis.setText("");
			sizedCIWithDFHillis.setText("");
		}

		if (FlagMLE == USE_MLE) {
			setTabTitlesBiased(2, true);
		} else if (FlagMLE == NO_MLE) {
			setTabTitlesBiased(2, false);
		}

	}

	/**
	 * Creates a study design for modality 0 and 1 based on designated
	 * split-plot design and pairing of readers and cases
	 * 
	 * @param newR Number of readers
	 * @param newN Number of normal cases
	 * @param newD Number of disease cases
	 * @param numSplitPlots Number of split-plot groups
	 * @param pairedReaders Whether or not readers are paired
	 * @param pairedCases Whether or not cases are paired
	 * @return Study design for modality 0 and 1
	 */
	private static int[][][][] createSplitPlotDesign(int newR, int newN,
			int newD, int numSplitPlots, int pairedReaders, int pairedCases) {
		int[][][] mod0design = new int[newR][newN][newD];
		int[][][] mod1design = new int[newR][newN][newD];
		int[][] mod0Normal = new int[newR][newN];
		int[][] mod1Normal = new int[newR][newN];
		int[][] mod0Disease = new int[newR][newD];
		int[][] mod1Disease = new int[newR][newD];
		int readersRange;
		int normalRange;
		int diseaseRange;

		if (pairedReaders == 1) {
			readersRange = newR;
		} else {
			readersRange = newR / 2;
		}
		if (pairedCases == 1) {
			normalRange = newN;
			diseaseRange = newD;
		} else {
			normalRange = newN / 2;
			diseaseRange = newD / 2;
		}

		int splitReadersRange = readersRange / numSplitPlots;
		int splitNormalRange = normalRange / numSplitPlots;
		int splitDiseaseRange = diseaseRange / numSplitPlots;

		for (int s = 0; s < numSplitPlots; s++) {
			for (int i = 0; i < splitReadersRange; i++) {
				int x0 = i + (splitReadersRange * s);
				int x1 = 0;
				if (pairedReaders == 1) {
					x1 = x0;
				} else {
					x1 = x0 + readersRange;
				}
				for (int j = 0; j < splitNormalRange; j++) {
					int y0 = j + (splitNormalRange * s);
					mod0Normal[x0][y0] = 1;
					int y1 = 0;

					if (pairedCases == 1) {
						y1 = y0;
					} else {
						y1 = y0 + normalRange;
					}
					mod1Normal[x1][y1] = 1;
				}
				if (newN % 2 == 1 && s == numSplitPlots - 1) {
					mod1Normal[x1][newN - 1] = 1;
				}

				for (int j = 0; j < splitDiseaseRange; j++) {
					int y0 = j + (splitDiseaseRange * s);
					mod0Disease[x0][y0] = 1;
					int y1 = 0;
					if (pairedCases == 1) {
						y1 = y0;
					} else {
						y1 = y0 + diseaseRange;
					}
					mod1Disease[x1][y1] = 1;
				}
				if (newD % 2 == 1 && s == numSplitPlots - 1) {
					mod1Disease[x1][newD - 1] = 1;
				}
			}
		}

		for (int i = splitReadersRange * (numSplitPlots - 1); i < readersRange; i++) {
			int x1 = 0;
			if (pairedReaders == 1) {
				x1 = i;
			} else {
				x1 = i + readersRange;
			}
			for (int j = splitNormalRange * (numSplitPlots - 1); j < normalRange; j++) {
				mod0Normal[i][j] = 1;
				int y1 = 0;
				if (pairedCases == 1) {
					y1 = j;
				} else {
					y1 = j + normalRange;
				}
				mod1Normal[x1][y1] = 1;
			}
			mod1Normal[x1][newN - 1] = 1;

			for (int j = splitDiseaseRange * (numSplitPlots - 1); j < diseaseRange; j++) {
				mod0Disease[i][j] = 1;
				int y1 = 0;
				if (pairedCases == 1) {
					y1 = j;
				} else {
					y1 = j + diseaseRange;
				}
				mod1Disease[x1][y1] = 1;
			}
			mod1Disease[x1][newD - 1] = 1;
		}

		for (int r = 0; r < newR; r++) {
			for (int n = 0; n < newN; n++) {
				for (int d = 0; d < newD; d++) {
					if (mod0Normal[r][n] == 1 && mod0Disease[r][d] == 1) {
						mod0design[r][n][d] = 1;
					}
					if (mod1Normal[r][n] == 1 && mod1Disease[r][d] == 1) {
						mod1design[r][n][d] = 1;
					}
				}
			}
		}

		return new int[][][][] { mod0design, mod1design };
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
		JPanel CardInputModeImrmc = new JPanel();
		RSC = new RawStudyCard(CardInputModeImrmc, this);
		// create manual panel
		JPanel CardInputModeManual = new JPanel();
		MC = new ManualCard(CardInputModeManual, this, MRMCobject);
		// create DB panel
// TODO	JPanel CardInputModeOmrmc = new JPanel();
// TODO	DBC = new DBCard(CardInputModeOmrmc, this, MRMCobject);

		// ***********************************************************************
		// ***********Create the panel that contains the "cards".*****************
		// ***********************************************************************
		inputCards = new JPanel(new CardLayout());
//		inputCards.add(dbCard, DescInputModeOmrmc);
		inputCards.add(CardInputModeImrmc, DescInputModeImrmc);
		inputCards.add(CardInputModeManual, DescInputModeManual);

		// ***********************************************************************
		// ***********Create the panel that contains Output
		// Labels*****************
		// ***********************************************************************
		JPanel outPane = new JPanel();
		aucOutput = new JLabel("");
		outPane.add(aucOutput);

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

		// *******************************************************************
		// *************statistical analysis ********************************
		// ********************************************************************
		JPanel sizeStatRow1 = new JPanel();
		sizedSqrtVar = new JLabel("sqrt(Var)=");
		sizedStat = new JLabel("  Stat=");
		CVF = new JLabel("  CVF=");
		ZPower = new JLabel("  Power(Z test)=");
		sizedCIzTest = new JLabel("Conf. Int.=");
		sizeStatRow1.add(new JLabel("Sizing Results (t or z):   "));
		sizeStatRow1.add(sizedSqrtVar);
		sizeStatRow1.add(sizedStat);
		sizeStatRow1.add(CVF);
		sizeStatRow1.add(ZPower);
		sizeStatRow1.add(sizedCIzTest);

		JPanel sizeStatRow2 = new JPanel();
		sizedDFBDG = new JLabel("  df(BDG) =");
		HillisPowerWithdfBDG = new JLabel("      Power(Hillis 2011) =");
		sizedCIWithDFBDG = new JLabel("Conf. Int.=");
		sizeStatRow2.add(sizedDFBDG);
		sizeStatRow2.add(HillisPowerWithdfBDG);
		sizeStatRow2.add(sizedCIWithDFBDG);

		JPanel sizeStatRow3 = new JPanel();
		sizedDFHillis = new JLabel("  df(Hillis 2008)=");
		HillisPowerWithdfHillis = new JLabel("      Power(Hillis 2011) =");
		sizedCIWithDFHillis = new JLabel("Conf. Int.=");
		sizeStatRow3.add(sizedDFHillis);
		sizeStatRow3.add(HillisPowerWithdfHillis);
		sizeStatRow3.add(sizedCIWithDFHillis);

		// *******************************************************************
		// *************Generate Sizing panel*********************************
		// *******************************************************************
		JPanel sizingPanel = new JPanel();
		long[] Parms = {0, 0, 0};
		genSP = new SizePanel(Parms, sizingPanel, this);

		JPanel statsRow1 = new JPanel();
		JLabel StatResults = new JLabel("Statistical Analysis:");
		TotalVar_JLabel = new JLabel("  total var=0.00");
		sqrtTotalVar_JLabel = new JLabel("  sqrt(total var)=0.00");
		stat = new JLabel("  Stat=0.00");
		pVal = new JLabel("  p-value=0.00");
		confInt = new JLabel("  Conf. Int.=(0.0000,0.0000)");
		statsRow1.add(StatResults);
		statsRow1.add(sqrtTotalVar_JLabel);
		statsRow1.add(stat);
		statsRow1.add(pVal);
		statsRow1.add(confInt);

		JPanel statsRow2 = new JPanel();
		dfBDG = new JLabel("  df(BDG)=0.00");
		pValWithDFBDG = new JLabel("  p-Value=0.00");
		confIntWithDFBDG = new JLabel("  Conf. Int.=(0.0000,0.0000)");
		statsRow2.add(dfBDG);
		statsRow2.add(pValWithDFBDG);
		statsRow2.add(confIntWithDFBDG);

		JPanel statsRow3 = new JPanel();
		dfHillis = new JLabel("  df(Hillis 2008)=0.00");
		pValWithDFHillis = new JLabel("  p-Value=0.00");
		confIntWithDFHillis = new JLabel("  Conf. Int.=(0.0000,0.0000)");
		statsRow3.add(dfHillis);
		statsRow3.add(pValWithDFHillis);
		statsRow3.add(confIntWithDFHillis);

		pilotFile.setText("");
		reset1stStatPanel();
		resetTable1();
		resetSizePanel();
		resetTable2();

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
		cp.add(inputCards);
		cp.add(panelSep);
		cp.add(outPane);
		cp.add(statsRow1);
		cp.add(statsRow2);
		cp.add(statsRow3);
		cp.add(tabbedPane1);
		// Hides the trial sizing table
		// cp.add(tabbedPane2);
		// cp.add(panelStat11);
		cp.add(panelSep2);
		
		// TODO
//		cp.add(sizingPanel);
//		cp.add(sizeStatRow1);
//		cp.add(sizeStatRow2);
//		cp.add(sizeStatRow3);
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
				report = genSP.genReport(1);
			} else {
				report = genSP.genReport();
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

			CardLayout cl = (CardLayout) (inputCards.getLayout());
			cl.show(inputCards, selectedInput);
			
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
			JTextArea desc = new JTextArea(currentDBrecord.getRecordDesc(),
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

	/**
	 * Handler for button to browse for raw study data input file. Resets the GUI. Displays a
	 * file chooser and reads input file {@link mrmc.core.InputFile}
	 */
	class brwsButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			resetGUI();
			
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"iMRMC Input Files (.imrmc)", "imrmc");
			fc.setFileFilter(filter);
			int returnVal = fc.showOpenDialog((Component) e.getSource());
			
			// Get a pointer to the input file
			File f = fc.getSelectedFile();

			// Read the input file
			if (f != null) {
				filename = f.getPath();
				pilotFile.setText(filename);
				
				// read the .imrmc input file, check for exceptions
				try {
					currentInputFile = new InputFile(filename);
				} catch (IOException except) {
					except.printStackTrace();
					JOptionPane.showMessageDialog(MRMCobject.getFrame(),
							except.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
					resetGUI();
					pilotFile.setText("");
					return;
				}
				if (!currentInputFile.numsVerified()) {
					JOptionPane
							.showMessageDialog(
									MRMCobject.getFrame(),
									currentInputFile.showUnverified(),
									"Warning: Input Header Values Do Not Match Actual Values",
									JOptionPane.WARNING_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(
							MRMCobject.getFrame(),
							"NR = " + currentInputFile.getNreader() + 
							" N0 = "+ currentInputFile.getNnormal() +
							" N1 = "+ currentInputFile.getNdisease() +
							" NM = "+ currentInputFile.getNmodality(), 
							"Study Info", JOptionPane.INFORMATION_MESSAGE);
				}
				// TO-DO: Eliminate this from here. Check study design in DBRecord.
				if (!currentInputFile.getFullyCrossedStatus()) {
					JOptionPane.showMessageDialog(MRMCobject.getFrame(),
							"The study is not fully crossed", "Warning",
							JOptionPane.WARNING_MESSAGE);
					tabbedPane1.setEnabledAt(2, false);
					tabbedPane1.setEnabledAt(3, false);
					tabbedPane1.setEnabledAt(4, false);

				} else {
					tabbedPane1.setEnabledAt(2, true);
					tabbedPane1.setEnabledAt(3, true);
					tabbedPane1.setEnabledAt(4, true);
				}

				currModA = NO_MOD;
				currModB = NO_MOD;
				RSC.updateStudyPanel();

			}
		}
	}

	/**
	 * Handler for "Format Info" button
	 */
	class fmtHelpButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			// Create Desktop object
			Desktop d=Desktop.getDesktop();

			// Browse a URL, say google.com
			try {
				d.browse(new URI("http://imrmc.googlecode.com/svn/standalone_application/iMRMCuserguide-current.pdf"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Handler for "Show ROC Curve" button, displays interactive ROC charts
	 */
	class ROCButtonListener implements ActionListener {
		String rocMod = "";

		public void actionPerformed(ActionEvent e) {
			// System.out.println("roc button pressed");
			if (currentInputFile != null && currentInputFile.isLoaded()) {
				JComboBox<String> chooseMod = new JComboBox<String>();
				
				for (String Modality : currentInputFile.getModalityIDs()){
					chooseMod.addItem(Modality);
				}

				chooseMod.setSelectedIndex(0);
				Object[] message = { "Which modality would you like view?\n",
						chooseMod };
				JOptionPane.showMessageDialog(MRMCobject.getFrame(), message,
						"Choose Modality and Reader",
						JOptionPane.INFORMATION_MESSAGE, null);
				rocMod = (String) chooseMod.getSelectedItem();
				final ROCCurvePlot roc = new ROCCurvePlot(
						"ROC Curve: Modality " + rocMod,
						"FPF (1 - Specificity)", "TPF (Sensitivity)",
						currentInputFile.generateROCpoints(rocMod));
				roc.addData(currentInputFile.generatePooledROC(rocMod), "Pooled Average");
				roc.pack();
				RefineryUtilities.centerFrameOnScreen(roc);
				roc.setVisible(true);

			} else {
				JOptionPane.showMessageDialog(MRMCobject.getFrame(),
						"Pilot study data has not yet been input.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Handler for "Input Statistics Charts" button, displays charts for study
	 * design at a glance
	 */
	class ReadersCasesButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// System.out.println("graph button pressed");
			if (currentInputFile != null && currentInputFile.isLoaded()) {
				final BarGraph cpr = new BarGraph("Cases per Reader",
						"Readers", "Cases", currentInputFile.casesPerReader());
				cpr.pack();
				RefineryUtilities.centerFrameOnScreen(cpr);
				cpr.setVisible(true);

				final BarGraph rpc = new BarGraph("Readers per Case", "Cases",
						"Readers", currentInputFile.readersPerCase());
				rpc.pack();
				RefineryUtilities.centerFrameOnScreen(rpc);
				RefineryUtilities.positionFrameOnScreen(rpc, 0.6, 0.6);
				rpc.setVisible(true);
			} else {
				JOptionPane.showMessageDialog(MRMCobject.getFrame(),
						"Pilot study data has not yet been input.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Handler for "Show Study Design" button, displays charts of more detailed
	 * study design for a given modality
	 */
	class designButtonListener implements ActionListener {
		String designMod1 = "empty";

		public void actionPerformed(ActionEvent e) {
			// System.out.println("study design button pressed");
			if (currentInputFile != null && currentInputFile.isLoaded()) {
				JComboBox<String> choose1 = new JComboBox<String>();
				ArrayList<String> temp;
				temp = currentInputFile.getModalityIDs();

				for (String Modality : currentInputFile.getModalityIDs()){
					choose1.addItem(Modality);
				}
				choose1.setSelectedIndex(0);
				Object[] message = { "Which modality would you like view?\n",
						choose1 };
				JOptionPane.showMessageDialog(MRMCobject.getFrame(), message,
						"Choose Modality", JOptionPane.INFORMATION_MESSAGE,
						null);
				boolean[][] design = currentInputFile.getStudyDesign( 
						(String) choose1.getSelectedItem());
				final StudyDesignPlot chart = new StudyDesignPlot(
						"Study Design: Modality " + designMod1, "Case",
						"Reader", design);
				chart.pack();
				RefineryUtilities.centerFrameOnScreen(chart);
				chart.setVisible(true);

			} else {
				JOptionPane.showMessageDialog(MRMCobject.getFrame(),
						"Pilot study data has not yet been input.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
