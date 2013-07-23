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

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;

import java.text.DecimalFormat;

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
 * 1. Menu bar <br>
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
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 * @version 2.0b
 */
public class GUInterface {
	JPanel inputCards; // the panel that uses CardLayout, there are three cards
						// for three different input
	JPanel manual3; // the panel that shares different manual input components
	private final static String DB = "Input from database ... ";
	private final static String Pilot = "Input raw data...";
	private final static String Manual = "Manual input...";
	final static int USE_BIAS = 1;
	final static int NO_BIAS = 0;
	final static int NO_MOD = -1;
	final static int SELECT_DB = 0;
	final static int SELECT_FILE = 1;
	final static int SELECT_MAN = 2;
	private JTextField pilotFile;
	private String filename = "";
	private MRMC lst;
	private MrmcDB fdaDB;
	InputFile usr;
	private DBRecord[] Records;
	DBRecord usrFile;

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
	private JLabel MSvar1, MSvar2;

	int currMod0; // Chosen mod A when reading raw data
	int currMod1; // Chosen mod B when reading raw data

	private JLabel HillisPower;
	private JLabel ZPower;
	private JLabel Delta;
	private JLabel sizedDFHillis;
	private JLabel CVF;
	private JLabel tStat;
	private JLabel sqrtTotalVar;
	private JLabel dfHillis;
	private JLabel pVal;
	private JLabel confInt;
	private JLabel sizedDFBDG;
	private int selectedDB = 0;
	private int selectedMod = 0;
	private int selectedInput = 0;
	private int selectedSummary = 0;
	private boolean hasNegative = false;
	private int useBiasM = 0;
	private int SummaryUseMLE = 0;

	DecimalFormat twoDec = new DecimalFormat("0.00");
	DecimalFormat threeDec = new DecimalFormat("0.000");
	DecimalFormat threeDecE = new DecimalFormat("0.000E0");
	DecimalFormat fourDec = new DecimalFormat("0.0000");

	/**
	 * Gets whether bias is being used for variance components
	 * 
	 * @return Whether bias is used
	 */
	public int getuseBiasM() {
		return useBiasM;
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
	 * Gets whether input is from database, manual input or raw study input
	 * 
	 * @return Input source
	 */
	public int getSelectedInput() {
		return selectedInput;
	}

	/**
	 * Gets statistics for variance analysis in String format
	 * 
	 * @return String of statistics for variance analysis
	 */
	public String getStat1() {
		String results = tStat.getText();
		results = results + "\t" + dfHillis.getText();
		results = results + "\t" + pVal.getText();
		results = results + "\t" + confInt.getText();
		results = results + "\t" + sqrtTotalVar.getText();
		return results;
	}

	/**
	 * Gets statistics for new trial sizing in String format
	 * 
	 * @return String of statistics for new trial sizing
	 */
	public String getStat2() {
		String results = MSvar2.getText();
		results = results + "\t" + Delta.getText();
		results = results + "\t" + sizedDFHillis.getText();
		results = results + "\t" + sizedDFBDG.getText();
		results = results + "\t" + CVF.getText();
		results = results + "\t" + HillisPower.getText();
		results = results + "\t" + ZPower.getText();
		return results;
	}

	/**
	 * Sets all GUI components to their default values
	 */
	public void resetGUI() {
		resetTable1();
		resetTable2();
		clearFields();

		// Selections
		currMod0 = NO_MOD;
		currMod1 = NO_MOD;
		setUseBiasM(NO_BIAS);
		DBC.setUseBiasM(NO_BIAS);
		RSC.setUseBiasM(NO_BIAS);
		RSC.resetModPanel();
		setTabTitlesBiased(1, false);
		setTabTitlesBiased(2, false);
		// Modality on MS
		DBC.setSelectedMod(0);
		RSC.setSelectedMod(0);
		enableTabs();
		MC.reset();
		usr = null;
	}

	/**
	 * Clears all input fields and statistics labels
	 */
	private void clearFields() {
		aucOutput.setText("");
		BDGvar1.setText("");
		BDGvar2.setText("");
		BCKvar1.setText("");
		BCKvar2.setText("");
		DBMvar1.setText("");
		DBMvar2.setText("");
		ORvar1.setText("");
		ORvar2.setText("");
		MSvar1.setText("");
		MSvar2.setText("");
		ZPower.setText("");
		Delta.setText("");
		sizedDFHillis.setText("");
		sizedDFBDG.setText("");
		CVF.setText("");
		tStat.setText("");
		sqrtTotalVar.setText("");
		dfHillis.setText("");
		pVal.setText("");
		confInt.setText("");
		HillisPower.setText("");
		pilotFile.setText("");
	}

	/**
	 * Empties out all values in the variance analysis table
	 */
	private void resetTable1() {
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
	 * Empties out all values in the trial sizing table (which is not visible)
	 */
	private void resetTable2() {
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
	 * Gets the current study record which is being analyzed
	 * 
	 * @return The current record
	 */
	public DBRecord getCurrentRecord() {
		DBRecord tempRecord = null;
		if (selectedInput == SELECT_DB)
			tempRecord = Records[selectedDB];
		else if (selectedInput == SELECT_FILE)
			tempRecord = usrFile;
		else if (selectedInput == SELECT_MAN) {
			tempRecord = MC.getManualRecord();
		}
		return tempRecord;
	}

	/**
	 * Sets whether bias is being used in variance analysis
	 * 
	 * @param useBias Whether to use bias or not
	 */
	public void setUseBiasM(int useBias) {
		useBiasM = useBias;
	}

	/**
	 * Sets the trial sizing panel inputs with default values based on the
	 * current record
	 */
	public void setSizePanel() {
		DBRecord tempRecord = getCurrentRecord();
		int[] params = tempRecord.getSizesInt();
		genSP.setNumbers(params);
	}

	/**
	 * Sets the statistics labels for the variance analysis table based on the
	 * current record that has been analyzed
	 */
	public void set1stStatPanel() {
		DBRecord tempRecord = getCurrentRecord();
		double eff;
		double[][] BDGtmp = tempRecord.getBDG(useBiasM);
		double sum = 0;
		for (int i = 1; i < 8; i++)
			sum += (BDGtmp[0][i] - BDGtmp[1][i]);
		if (selectedMod == 1 || selectedMod == 0)
			eff = tempRecord.getAUCinNumber(selectedMod) - 0.5;
		else
			eff = Math.abs(tempRecord.getAUCinNumber(0)
					- tempRecord.getAUCinNumber(1));
		System.out.println("selectedMod" + selectedMod + "eff=" + eff + "sum"
				+ sum);
		if (sum != 0 || sum == 0 && selectedMod != 3) // two modalities are
														// different
		{
			StatTest stat2 = new StatTest(tempRecord, selectedMod, useBiasM,
					0.05, eff);
			// DOF
			String output = twoDec.format(stat2.getDOF());
			dfHillis.setText("  df(Hillis 2008)= " + output);
			// tStat
			output = twoDec.format(stat2.gettStat());
			tStat.setText("  tStat= " + output);
			// pVal
			output = fourDec.format(stat2.getpValF());
			pVal.setText("  p-Value= " + output);
			// ci
			output = twoDec.format(stat2.getciTop());
			String output2 = twoDec.format(stat2.getciBot());
			confInt.setText("Conf. Int.=(" + output2 + ", " + output + ")");
		} else {
			dfHillis.setText("  df(Hillis 2008)= ");
			tStat.setText("  tStat= ");
			sqrtTotalVar.setText("  sqrt(total var)= ");
			pVal.setText("  p-Value= ");
			confInt.setText("Conf. Int.=");

		}
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
		DBRecord tempRecord = getCurrentRecord();
		if (tempRecord == null) {
			JOptionPane.showMessageDialog(lst.getFrame(),
					"Must perform variance analysis first.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		int[][][][] design = createSplitPlotDesign(newR, newN, newD,
				numSplitPlots, pairedReaders, pairedCases);

		double[][] BDGcoeff = DBRecord.genBDGCoeff(newR, newN, newD, design[0],
				design[1]);
		double[][] BCKcoeff = DBRecord.genBCKCoeff(newR, newN, newD,
				BDGcoeff[0]);
		double[][] DBMcoeff = DBRecord.genDBMCoeff(newR, newN, newD);
		double[][] ORcoeff = DBRecord.genORCoeff(newR, newN, newD);
		double[][] MScoeff = DBRecord.genMSCoeff(newR, newN, newD);

		double[][] BDG = new double[4][8];
		double[][] BCK = new double[4][7];
		double[][] DBM = new double[4][6];
		double[][] OR = new double[4][6];
		double[][] MS = new double[4][6];
		if (selectedInput == SELECT_MAN && MC.getSelectedManualComp() != 0) {
			BDG = tempRecord.getBDG(NO_BIAS);
			if (MC.getSelectedManualComp() == 1) {
				// ******* Brandon's new formula
				// BCK = tempRecord.getBCK(0);
				// JFrame frame = lst.getFrame();
				// JOptionPane.showMessageDialog(frame,
				// "This function is not implemented for BCK manual input",
				// "error",
				// JOptionPane.ERROR_MESSAGE);
				// return ;
				BCK = tempRecord.BCKresize(tempRecord.getBCK(useBiasM), newR,
						newN, newD);
				DBM = tempRecord.DBMresize(tempRecord.getDBM(useBiasM), newR,
						newN, newD);
				OR = DBRecord.DBM2OR(0, DBM, newR, newN, newD);
				MS = DBRecord.DBM2MS(DBM, newR, newN, newD);
			} else if (MC.getSelectedManualComp() == 2) {// DBM input is used
				DBM = tempRecord.DBMresize(tempRecord.getDBM(useBiasM), newR,
						newN, newD);
				OR = DBRecord.DBM2OR(0, DBM, newR, newN, newD);
				MS = DBRecord.DBM2MS(DBM, newR, newN, newD);
			} else if (MC.getSelectedManualComp() == 3) // OR input is used
			{
				DBM = DBRecord.DBM2OR(1, tempRecord.getOR(useBiasM), newR,
						newN, newD);
				DBM = tempRecord.DBMresize(DBM, newR, newN, newD);
				OR = DBRecord.DBM2OR(0, DBM, newR, newN, newD);
				MS = DBRecord.DBM2MS(DBM, newR, newN, newD);
			} else if (MC.getSelectedManualComp() == 4) // MS input is used
			{
				DBM = DBRecord.DBM2OR(1, tempRecord.getOR(useBiasM), newR,
						newN, newD);
				DBM = tempRecord.DBMresize(DBM, newR, newN, newD);
				MS = DBRecord.DBM2MS(DBM, newR, newN, newD);
			}

		} else {
			BDG = tempRecord.getBDG(useBiasM);
			BCK = DBRecord.BDG2BCK(BDG);
			DBM = DBRecord.BCK2DBM(BCK, newR, newN, newD);
			OR = DBRecord.DBM2OR(0, DBM, newR, newN, newD);
			MS = DBRecord.DBM2MS(DBM, newR, newN, newD);
		}

		if (selectedInput == SELECT_MAN) {
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
		MSvar2.setText("sqrt(Var)=" + output);

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

		StatTest stat = new StatTest(var, newR, newN, newD, sig, eff, BDGv,
				tempRecord.getBCK(USE_BIAS));
		output = twoDec.format(stat.getHillisPower());
		HillisPower.setText("      Power(Hillis 2011) = " + output);
		output = twoDec.format(stat.getZPower());
		ZPower.setText("  Power(Z test)= " + output);
		output = threeDecE.format(stat.getDelta());
		Delta.setText("  Delta= " + output);
		output = twoDec.format(stat.getDDF());
		sizedDFHillis.setText("  df(Hillis 2008)= " + output);
		output = twoDec.format(stat.getDfBDG());
		sizedDFBDG.setText("  df(BDG) = " + output);
		output = twoDec.format(stat.getCVF());
		CVF.setText("  CVF= " + output);

		if (useBiasM == USE_BIAS) {
			setTabTitlesBiased(2, true);
		} else if (useBiasM == NO_BIAS) {
			setTabTitlesBiased(2, false);
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
	 * Gets whether analyzing modality 0, 1 or difference
	 * 
	 * @return Which modalities are being analyzed
	 */
	public int getSelectedMod() {
		return selectedMod;
	}

	/**
	 * Specifies which modalities are being analyzed. Clears statistics labels
	 * when doing so.
	 * 
	 * @param sel Whether we are analyzing modality 0, 1 or difference
	 */
	public void setSelectedMod(int sel) {
		selectedMod = sel;
		ZPower.setText("Power(Z test) = ");
		HillisPower.setText("Power(Hillis 2011) = ");
		Delta.setText("Delta = ");
		CVF.setText("CVF = ");
		sizedDFHillis.setText("df(Hillis 2008) = ");
		sizedDFBDG.setText("df(BDG) = ");
		genSP.setEff("Effect Size", "0.05");
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
	 * Gets variance analysis information from current study record, populates
	 * variance analysis table, sets statistics labels
	 */
	public void setTable1() {
		DBRecord tempRecord = getCurrentRecord();

		if (selectedInput == SELECT_FILE && filename.equals(null)) {
			return;
		}

		double[][] BDGdata1 = DBRecord.getBDGTab(selectedMod,
				tempRecord.getBDG(useBiasM), tempRecord.getBDGcoeff());
		double[][] BCKdata1 = DBRecord.getBCKTab(selectedMod,
				tempRecord.getBCK(useBiasM), tempRecord.getBCKcoeff());
		double[][] DBMdata1 = DBRecord.getDBMTab(selectedMod,
				tempRecord.getDBM(useBiasM), tempRecord.getDBMcoeff());
		double[][] ORdata1 = DBRecord.getORTab(selectedMod,
				tempRecord.getOR(useBiasM), tempRecord.getORcoeff());
		double[][] MSdata1 = DBRecord.getMSTab(selectedMod,
				tempRecord.getMS(useBiasM), tempRecord.getMScoeff());
		double BDGv = Matrix.total(BDGdata1[6]);
		double BCKv = Matrix.total(BCKdata1[6]);
		double DBMv = Matrix.total(DBMdata1[2]);
		double ORv = Matrix.total(ORdata1[2]);
		double MSv = Matrix.total(MSdata1[2]);

		double[][][] allTableData = new double[][][] { BDGdata1, BCKdata1,
				DBMdata1, ORdata1, MSdata1 };

		fillTable1(allTableData);

		resetTable2();
		String output = threeDec.format(Math.sqrt(BDGv));
		BDGvar1.setText("sqrt(Var)=" + output);
		output = threeDec.format(Math.sqrt(BCKv));
		BCKvar1.setText("sqrt(Var)=" + output);
		output = threeDec.format(Math.sqrt(DBMv));
		DBMvar1.setText("sqrt(Var)=" + output);
		output = threeDec.format(Math.sqrt(ORv));
		ORvar1.setText("sqrt(Var)=" + output);
		output = threeDec.format(Math.sqrt(MSv));
		MSvar1.setText("sqrt(Var)=" + output);
		sqrtTotalVar.setText("   sqrt(total var)=" + output);

		if (useBiasM == USE_BIAS) {
			setTabTitlesBiased(1, true);
		} else if (useBiasM == NO_BIAS) {
			setTabTitlesBiased(1, false);
		}
	}

	/**
	 * Sets labels of tabs on variance analysis/trial sizing tables to indicate
	 * whether bias is being used
	 * 
	 * @param paneNum Indicates whether variance analysis or trial sizing tabels
	 *            should be altered
	 * @param biased Whether or not bias is being used
	 */
	private void setTabTitlesBiased(int paneNum, boolean biased) {
		if (paneNum == 1) {
			if (biased) {
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
			if (biased) {
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
	 * Gets the AUCs from the current study record and displays them in the GUI
	 */
	public void setAUCoutput() {
		DBRecord tempRecord = getCurrentRecord();
		String displayAUC = tempRecord.getAUC(selectedMod);
		String displayParm = tempRecord.getSizes();
		aucOutput.setText(displayAUC + displayParm);
		System.out.println("displayParm" + displayParm);
	}

	/**
	 * Sole constructor, builds and displays the GUI
	 * 
	 * @param lsttemp Application frame
	 * @param cp Container for GUI elements
	 */
	public GUInterface(MRMC lsttemp, Container cp) {
		lst = lsttemp;
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

		JPanel inputSelectPane = new JPanel();
		inputSelectPane.setLayout(new FlowLayout());
		JLabel inLabel = new JLabel("Select an input method: ");
		String comboBoxItems[] = { DB, Pilot, Manual };
		JComboBox cb = new JComboBox(comboBoxItems);
		cb.setEditable(false);
		cb.setSelectedIndex(0);
		cb.addActionListener(new inputModListener());
		JButton buttonReset = new JButton("Reset");
		buttonReset.addActionListener(new ResetListner());
		inputSelectPane.add(inLabel);
		inputSelectPane.add(cb);
		inputSelectPane.add(buttonReset);

		// create DB panel
		JPanel dbCard = createDBPanel();

		// create pilot/raw study panel
		JPanel pilotCard = createPilotPanel();

		// create manual panel
		JPanel manualCard = new JPanel();
		MC = new ManualCard(manualCard, this, lst);

		// ***********************************************************************
		// ***********Create the panel that contains the
		// "cards".*****************
		// ***********************************************************************
		inputCards = new JPanel(new CardLayout());
		inputCards.add(dbCard, DB);
		inputCards.add(pilotCard, Pilot);
		inputCards.add(manualCard, Manual);

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
				"comp M1", "coeff M1", "product M0,M1", "2*coeff M0-M1",
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
		// JPanel panelStat11 = new JPanel();
		JPanel panelStat12 = new JPanel();
		// sigLevel = new JTextField ("0.05",3);
		// effSizeLabel = new JLabel("Effect Size");
		// effSize = new JTextField ("0.5",3);
		Delta = new JLabel("  Delta= 0.00");
		sizedDFHillis = new JLabel("  df(Hillis 2008)= 0.00");
		sizedDFBDG = new JLabel("  df(BDG) = 0.00");
		CVF = new JLabel("  CVF= 0.00");
		ZPower = new JLabel("  Power(Z test)= 0.00");
		HillisPower = new JLabel("      Power(Hillis 2011) = 0.00");
		MSvar2 = new JLabel("sqrt(Var)=0.00");

		// panelStat11.add(new JLabel("Significance Level"));
		// panelStat11.add(sigLevel);
		// panelStat11.add(effSizeLabel);
		// panelStat11.add(effSize);
		panelStat12.add(new JLabel("Sizing Results (t or z):   "));
		panelStat12.add(MSvar2);
		panelStat12.add(Delta);
		panelStat12.add(sizedDFHillis);
		panelStat12.add(sizedDFBDG);
		panelStat12.add(CVF);
		panelStat12.add(HillisPower);
		panelStat12.add(ZPower);

		// *******************************************************************
		// *************Generate Sizing panel*********************************
		// *******************************************************************
		JPanel sizingPanel = new JPanel();
		int[] Parms = Records[selectedDB].getSizesInt();
		genSP = new SizePanel(Parms, sizingPanel, this);

		JPanel panelStat2 = new JPanel();
		JLabel StatResults = new JLabel("Statistical Analysis:");
		dfHillis = new JLabel("  df(Hillis 2008)=0.00");
		pVal = new JLabel("  p-value=0.00");
		tStat = new JLabel("  t-Stat=0.00");
		confInt = new JLabel("  confInt=0.00");
		sqrtTotalVar = new JLabel("  sqrt(total var)=0.00");
		panelStat2.add(StatResults);
		panelStat2.add(sqrtTotalVar);
		panelStat2.add(tStat);
		panelStat2.add(dfHillis);
		panelStat2.add(pVal);
		panelStat2.add(confInt);

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
		SummarySelListner SummaryListener = new SummarySelListner();
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
		MLESelListner MLEListener = new MLESelListner();
		s1ButtonMLE.addActionListener(MLEListener);
		s2ButtonMLE.addActionListener(MLEListener);
		panelSummary.add(s1ButtonMLE);
		panelSummary.add(s2ButtonMLE);

		JButton bdgBtn = new JButton("BDG");
		bdgBtn.addActionListener(new bdgBtnListner());
		panelSummary.add(bdgBtn);
		JButton dbmBtn = new JButton("DBM");
		dbmBtn.addActionListener(new dbmBtnListner());
		panelSummary.add(dbmBtn);
		JButton bckBtn = new JButton("BCK");
		bckBtn.addActionListener(new bckBtnListner());
		panelSummary.add(bckBtn);
		JButton orBtn = new JButton("OR");
		orBtn.addActionListener(new orBtnListner());
		panelSummary.add(orBtn);

		cp.add(inputSelectPane);
		cp.add(inputCards);
		cp.add(panelSep);
		cp.add(outPane);
		cp.add(panelStat2);
		cp.add(tabbedPane1);
		// Hides the trial sizing table
		// cp.add(tabbedPane2);
		// cp.add(panelStat11);
		cp.add(panelSep2);
		cp.add(sizingPanel);
		cp.add(panelStat12);
		cp.add(panelSep3);
		cp.add(panelSummary);
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
	 * Creates the card for raw study data input and analysis
	 * 
	 * @return Panel containing raw study input card
	 */
	private JPanel createPilotPanel() {
		JPanel pilotCard = new JPanel();
		GroupLayout layout = new GroupLayout(pilotCard);
		pilotCard.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		JLabel studyLabel = new JLabel("pilot study...");
		JPanel pilotCard2 = new JPanel();
		RSC = new RawStudyCard(pilotCard2, this);
		JButton fmtHelpButton = new JButton("Format Info.");
		JButton readerCasesButton = new JButton("Input Statistics Charts");
		JButton designButton = new JButton("Show Study Design");
		JButton ROCcurveButton = new JButton("Show ROC Curve");

		pilotFile = new JTextField(20);
		JButton browseButton = new JButton("Browse...");
		browseButton.addActionListener(new brwsButtonListner());
		fmtHelpButton.addActionListener(new fmtHelpButtonListner());
		readerCasesButton.addActionListener(new ReadersCasesButtonListner());
		designButton.addActionListener(new designButtonListner());
		ROCcurveButton.addActionListener(new ROCButtonListner());
		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								layout.createSequentialGroup()
										.addComponent(studyLabel)
										.addComponent(pilotFile)
										.addComponent(browseButton)
										.addComponent(fmtHelpButton)
										.addComponent(readerCasesButton)
										.addComponent(designButton)
										.addComponent(ROCcurveButton))
						.addGroup(
								layout.createSequentialGroup().addComponent(
										pilotCard2))));

		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(studyLabel)
								.addComponent(pilotFile)
								.addComponent(browseButton)
								.addComponent(fmtHelpButton)
								.addComponent(readerCasesButton)
								.addComponent(designButton)
								.addComponent(ROCcurveButton))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING).addComponent(
								pilotCard2)));
		return pilotCard;
	}

	/**
	 * Creates the card for database input
	 * 
	 * @return Panel containing database input card
	 */
	private JPanel createDBPanel() {
		fdaDB = lst.getDB();
		int DBsize = fdaDB.getNoOfItems();
		String[] dbBoxItems = new String[DBsize];
		Records = fdaDB.getRecords();
		for (int i = 0; i < DBsize; i++) {
			dbBoxItems[i] = Records[i].getRecordTitle();
		}

		JPanel dbCard = new JPanel();
		GroupLayout layout = new GroupLayout(dbCard);
		dbCard.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel studyLabel = new JLabel("Database ");
		JComboBox dbCB = new JComboBox(dbBoxItems);
		dbCB.setEditable(false);
		dbCB.addActionListener(new dbActionListener());
		dbCB.setSelectedIndex(0);
		JButton descButton = new JButton("Record Description");
		descButton.addActionListener(new descButtonListner());
		JPanel modSelPanel = new JPanel();
		DBC = new DBCard(modSelPanel, this);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								layout.createSequentialGroup()
										.addComponent(studyLabel)
										.addComponent(dbCB)
										.addComponent(descButton))
						.addGroup(
								layout.createSequentialGroup().addComponent(
										modSelPanel))));

		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(studyLabel).addComponent(dbCB)
								.addComponent(descButton))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING).addComponent(
								modSelPanel)));
		return dbCard;
	}

	/**
	 * Checks whether there are negative components as a result of variance
	 * analysis, displays window suggesting use of MLE estimates (bias) to avoid
	 * negative values.
	 * 
	 * @return True if variance components have negative values, false otherwise
	 */
	public boolean checkNegative() {
		hasNegative = false;

		DBRecord tempRecord = getCurrentRecord();
		double[][] tempBDG = tempRecord.getBDG(NO_BIAS);
		for (int i = 0; i < 8; i++) {
			if (tempBDG[selectedMod][i] < 0)
				hasNegative = true;
		}
		if (hasNegative && useBiasM == NO_BIAS) {
			JFrame frame = lst.getFrame();
			int result = JOptionPane
					.showConfirmDialog(
							frame,
							"There are negative values in the components, do you want\nto proceed with MLE estimates to avoid negatives?");
			if (JOptionPane.CANCEL_OPTION == result) {
				System.out.println("cancel");
			} else if (JOptionPane.YES_OPTION == result) {
				DBC.setUseBiasM(USE_BIAS);
				RSC.setUseBiasM(USE_BIAS);
				useBiasM = USE_BIAS;
			} else if (JOptionPane.NO_OPTION == result) {
				DBC.setUseBiasM(NO_BIAS);
				RSC.setUseBiasM(NO_BIAS);
				useBiasM = NO_BIAS;
			}

		}
		return true;
	}

	/**
	 * Checks whether raw study input file has been loaded and if any modality
	 * has been selected for variance analysis
	 * 
	 * @return True if file is loaded and modality selected for analysis, false
	 *         otherwise.
	 */
	public boolean checkRawInput() {
		String name = pilotFile.getText();
		System.out.println("name=" + name);
		if (name.equals(null) || name.equals("")) {
			JFrame frame = lst.getFrame();
			JOptionPane.showMessageDialog(frame, "invalid input", " Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (currMod0 == NO_MOD && currMod1 == NO_MOD) {
			JFrame frame = lst.getFrame();
			JOptionPane.showMessageDialog(frame,
					"You must select at least one modality", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/*
	 * generate a table, call this function to generate the table for each set
	 * of components. This function sets the format and headers of each table
	 */
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

		JList rowHeader = new JList(rowNames);
		rowHeader.setFixedCellWidth(80);

		rowHeader.setFixedCellHeight(table.getRowHeight());
		rowHeader.setCellRenderer(new RowHeaderRenderer(table));

		JScrollPane scroll = new JScrollPane(table);
		scroll.setRowHeaderView(rowHeader);
		return scroll;

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
	 * Handler for button to show database summer with DBM method
	 */
	class dbmBtnListner implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTextArea desc = genFrame();
			desc.setText(fdaDB.recordsSummary(selectedSummary, SummaryUseMLE,
					"DBM"));
		}
	}

	/**
	 * Handler for button to show database summer with BDG method
	 */
	class bdgBtnListner implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTextArea desc = genFrame();
			desc.setText(fdaDB.recordsSummary(selectedSummary, SummaryUseMLE,
					"BDG"));
		}
	}

	/**
	 * Handler for button to show database summer with BCK method
	 */
	class bckBtnListner implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTextArea desc = genFrame();
			desc.setText(fdaDB.recordsSummary(selectedSummary, SummaryUseMLE,
					"BCK"));
		}
	}

	/**
	 * Handler for button to show database summer with OR method
	 */
	class orBtnListner implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTextArea desc = genFrame();
			desc.setText(fdaDB.recordsSummary(selectedSummary, SummaryUseMLE,
					"OR"));
		}
	}

	/**
	 * Handler for drop down menu to select data input source
	 */
	class inputModListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JComboBox cb = (JComboBox) evt.getSource();
			CardLayout cl = (CardLayout) (inputCards.getLayout());
			cl.show(inputCards, (String) cb.getSelectedItem());
			selectedInput = cb.getSelectedIndex();
			resetGUI();
		}
	}

	/**
	 * Handler for input reset button
	 */
	class ResetListner implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			resetGUI();
		}
	}

	/**
	 * Handler for database description button, displays in a separate window
	 */
	class descButtonListner implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JFrame descFrame = new JFrame();

			descFrame.getRootPane().setWindowDecorationStyle(
					JRootPane.PLAIN_DIALOG);
			JTextArea desc = new JTextArea(Records[selectedDB].getRecordDesp(),
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
			JComboBox cb = (JComboBox) evt.getSource();
			selectedDB = (int) cb.getSelectedIndex();
		}
	}

	/**
	 * Handler for radio buttons to select wither analyzing single modality or
	 * difference when using database input
	 */
	class SummarySelListner implements ActionListener {
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
	class MLESelListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String str = e.getActionCommand();
			if (str.equals("Yes")) {
				SummaryUseMLE = USE_BIAS;
			}
			if (str.equals("No")) {
				SummaryUseMLE = NO_BIAS;
			}
		}
	}

	/**
	 * Handler for button to browse for raw study data input file. Displays a
	 * file chooser and performs verification of input file
	 */
	class brwsButtonListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// System.out.println("browse pressed");
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"iMRMC Input Files (.imrmc)", "imrmc");
			fc.setFileFilter(filter);
			// Don't get rid of this despite being unused
			int returnVal = fc.showOpenDialog((Component) e.getSource());
			File f = fc.getSelectedFile();
			if (f != null) {
				filename = f.getPath();
				pilotFile.setText(filename);
				// check the input format
				try {
					usr = new InputFile(filename);
				} catch (IOException except) {
					except.printStackTrace();
					JOptionPane.showMessageDialog(lst.getFrame(),
							except.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
					pilotFile.setText("");
					return;
				}
				if (!usr.numsVerified()) {
					JOptionPane
							.showMessageDialog(
									lst.getFrame(),
									usr.showUnverified(),
									"Warning: Input Header Values Do Not Match Actual Values",
									JOptionPane.WARNING_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(
							lst.getFrame(),
							"NR = " + usr.getReader() + " N0 = "
									+ usr.getNormal() + " N1 = "
									+ usr.getDisease() + " NM = "
									+ usr.getModality(), "Study Info",
							JOptionPane.INFORMATION_MESSAGE);
				}
				if (!usr.getFullyCrossedStatus()) {
					JOptionPane.showMessageDialog(lst.getFrame(),
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

				currMod0 = NO_MOD;
				currMod1 = NO_MOD;
				RSC.updateStudyPanel();

			}
		}
	}

	/**
	 * Handler for "Format Info" button
	 */
	class fmtHelpButtonListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			DataFormat fmt = new DataFormat();
			JOptionPane.showMessageDialog(lst.getFrame(), fmt.getInfo(),
					"Information", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Handler for "Show ROC Curve" button, displays interactive ROC charts
	 */
	class ROCButtonListner implements ActionListener {
		int rocMod = 1;

		public void actionPerformed(ActionEvent e) {
			// System.out.println("roc button pressed");
			if (usr != null && usr.isLoaded()) {
				JComboBox chooseMod = new JComboBox();
				for (int i = 1; i <= usr.getModality(); i++) {
					chooseMod.addItem(i);
				}
				chooseMod.setSelectedItem((Integer) rocMod);
				Object[] message = { "Which modality would you like view?\n",
						chooseMod };
				JOptionPane.showMessageDialog(lst.getFrame(), message,
						"Choose Modality and Reader",
						JOptionPane.INFORMATION_MESSAGE, null);
				rocMod = (Integer) chooseMod.getSelectedItem();
				final ROCCurvePlot roc = new ROCCurvePlot(
						"ROC Curve: Modality " + rocMod,
						"FPF (1 - Specificity)", "TPF (Sensitivity)",
						usr.generateROCpoints(rocMod));
				roc.addData(usr.generatePooledROC(rocMod), "Pooled Average");
				roc.pack();
				RefineryUtilities.centerFrameOnScreen(roc);
				roc.setVisible(true);

			} else {
				JOptionPane.showMessageDialog(lst.getFrame(),
						"Pilot study data has not yet been input.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Handler for "Input Statistics Charts" button, displays charts for study
	 * design at a glance
	 */
	class ReadersCasesButtonListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// System.out.println("graph button pressed");
			if (usr != null && usr.isLoaded()) {
				final BarGraph cpr = new BarGraph("Cases per Reader",
						"Readers", "Cases", usr.casesPerReader());
				cpr.pack();
				RefineryUtilities.centerFrameOnScreen(cpr);
				cpr.setVisible(true);

				final BarGraph rpc = new BarGraph("Readers per Case", "Cases",
						"Readers", usr.readersPerCase());
				rpc.pack();
				RefineryUtilities.centerFrameOnScreen(rpc);
				RefineryUtilities.positionFrameOnScreen(rpc, 0.6, 0.6);
				rpc.setVisible(true);
			} else {
				JOptionPane.showMessageDialog(lst.getFrame(),
						"Pilot study data has not yet been input.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Handler for "Show Study Design" button, displays charts of more detailed
	 * study design for a given modality
	 */
	class designButtonListner implements ActionListener {
		int designMod1 = 1;

		public void actionPerformed(ActionEvent e) {
			// System.out.println("study design button pressed");
			if (usr != null && usr.isLoaded()) {
				JComboBox choose1 = new JComboBox();
				for (int i = 1; i <= usr.getModality(); i++) {
					choose1.addItem(i);
				}
				choose1.setSelectedItem((Integer) designMod1);
				Object[] message = { "Which modality would you like view?\n",
						choose1 };
				JOptionPane.showMessageDialog(lst.getFrame(), message,
						"Choose Modality", JOptionPane.INFORMATION_MESSAGE,
						null);
				boolean[][] design = usr.getStudyDesign((Integer) choose1
						.getSelectedItem());
				designMod1 = (Integer) choose1.getSelectedItem();
				final StudyDesignPlot chart = new StudyDesignPlot(
						"Study Design: Modality " + designMod1, "Case",
						"Reader", design);
				chart.pack();
				RefineryUtilities.centerFrameOnScreen(chart);
				chart.setVisible(true);

			} else {
				JOptionPane.showMessageDialog(lst.getFrame(),
						"Pilot study data has not yet been input.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
