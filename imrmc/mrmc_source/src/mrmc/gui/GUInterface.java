/*
 * GUInterface.java
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
 * This class describes the graphic interface. From top
 * to bottom, the GUI includes
 * 1. Menu bar
 * 2. Input Panel, which uses card layout and has 3 cards 
 *    1) database as input
 *    2) pilot study or raw data input
 *    3) manual input components 
 * 3. a label of AUC values, size of the study, etc.
 * 4. a table with all components of variance for the origianl study
 * 5. Sizing panel   
 * 6. a table with all components of variance for the resulting study
 * 7. a label of statistical analysis resutls 
 * 8. database summary panel
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
import mrmc.core.dbRecord;
import mrmc.core.inputFile;
import mrmc.core.matrix;
import mrmc.core.mrmcDB;
import mrmc.core.statTest;

import org.jfree.ui.RefineryUtilities;

public class GUInterface {
	JPanel inputCards; // the panel that uses CardLayout, there are three cards
						// for three different input
	JPanel manual3; // the panel that shares different manual input components
	final static String DB = "Input from database ... ";
	final static String Pilot = "Input raw data...";
	final static String Manual = "Manual input...";
	final static String[] rowhead = { "components", "coeff", "total" };
	JTextField pilotFile;
	String filename = "";
	MRMC lst;
	mrmcDB fdaDB;
	inputFile usr;
	dbRecord[] Records;
	dbRecord usrFile;
	JTable BDGtable1;
	JTable BCKtable1;
	JTable DBMtable1;
	JTable ORtable1;
	JTable MStable1;
	JTable BDGtable2;
	JTable BCKtable2;
	JTable DBMtable2;
	JTable ORtable2;
	JTable MStable2;
	JLabel aucOutput;
	sPanel genSP;

	JLabel BDGvar, BDGvar2;
	JLabel BCKvar, BCKvar2;
	JLabel DBMvar, DBMvar2;
	JLabel ORvar, ORvar2;
	JLabel MSvar, MSvar2;

	JTextField manualInReader;
	JTextField manualInNormal;
	JTextField manualInDisease;
	JTextField BDGcomp;
	JTextField DBMcomp;
	JTextField BCKcomp;
	JTextField ORcomp;
	int manualReader;
	int manualNormal;
	int manualDisease;

	int currMod1;
	int currMod2;

	// JTextField sigLevel;
	// JTextField effSize;
	// JLabel effSizeLabel;
	JLabel HillisPower;
	JLabel ZPower;
	JLabel Delta;
	JLabel sizedDFHillis;
	JLabel CVF;
	JLabel tStat;
	JLabel sqrtTotalVar;
	JLabel dfHillis;
	JLabel pVal;
	JLabel confInt;
	int selectedDB = 0;
	int selectedMod = 0;
	int selectedInput = 0;
	int selectedManualComp = 0;
	int selectedSummary = 0;
	int hasNegative = 0;
	int useBiasM = 0;
	int SummaryUseMLE = 0;

	double totalStd = 0;
	DBModSelect MS;
	PilotModSelect MS2;
	ManualCard MC;
	JTabbedPane tabbedPane1, tabbedPane2;

	public int getuseBiasM() {
		return useBiasM;
	}

	public int getSingleOrDiff() {
		return MC.getSingleOrDiff();
	}

	public int getSelectedManualComp() {
		return MC.getSelectedManualComp();
	}

	public String getAUCoutput() {
		return aucOutput.getText();
	}

	public int getSelectedInput() {
		return selectedInput;
	}

	public String getStat1() {
		String results = tStat.getText();
		results = results + "\t" + dfHillis.getText();
		results = results + "\t" + pVal.getText();
		results = results + "\t" + confInt.getText();
		results = results + "\t" + sqrtTotalVar.getText();
		return results;
	}

	public String getStat2() {
		String results = MSvar2.getText();
		results = results + "\t" + Delta.getText();
		results = results + "\t" + sizedDFHillis.getText();
		results = results + "\t" + CVF.getText();
		results = results + "\t" + HillisPower.getText();
		results = results + "\t" + ZPower.getText();
		return results;
	}

	/* ****************************** reset************************ */
	public void resetTab2() {
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

	public void reset() {
		// Clear Table
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
		resetTab2();
		// Clear Text Fields
		aucOutput.setText("");
		BDGvar.setText("");
		BDGvar2.setText("");
		BCKvar.setText("");
		BCKvar2.setText("");
		DBMvar.setText("");
		DBMvar2.setText("");
		ORvar.setText("");
		ORvar2.setText("");
		MSvar.setText("");
		MSvar2.setText("");
		ZPower.setText("");
		Delta.setText("");
		sizedDFHillis.setText("");
		CVF.setText("");
		tStat.setText("");
		sqrtTotalVar.setText("");
		dfHillis.setText("");
		pVal.setText("");
		confInt.setText("");

		HillisPower.setText("");
		// Selections
		setUseBiasM(0);
		MS.setUseBiasM(0);
		MS2.setUseBiasM(false);
		tabbedPane1.setTitleAt(0, "BDG");
		tabbedPane1.setTitleAt(1, "BCK");
		tabbedPane1.setTitleAt(2, "DBM");
		tabbedPane1.setTitleAt(3, "OR");
		tabbedPane2.setTitleAt(0, "BDG");
		tabbedPane2.setTitleAt(1, "BCK");
		tabbedPane2.setTitleAt(2, "DBM");
		tabbedPane2.setTitleAt(3, "OR");
		// Modality on MS
		MS.setSelectedMod(0);
		MS2.setSelectedMod(0);
		// pilot file input field
		pilotFile.setText("");
		enableTabs();
		MC.reset();
	}

	// public double[] getSiglevel()
	// {
	// double[] result = new double[2];
	// double sig=Double.parseDouble(sigLevel.getText());
	// double eff=Double.parseDouble(effSize.getText());
	// result[0]=sig;
	// result[1]=eff;
	// return result;
	// }

	public dbRecord getCurrentRecord() {
		dbRecord tempRecord = null;
		if (selectedInput == 0)
			tempRecord = Records[selectedDB];
		else if (selectedInput == 1)
			tempRecord = usrFile;
		else if (selectedInput == 2) {
			tempRecord = MC.getManualRecord();
		}
		return tempRecord;
	}

	public void setUseBiasM(int useBias) {
		useBiasM = useBias;
	}

	public void setSPanel() {
		dbRecord tempRecord = getCurrentRecord();
		int[] Parms = tempRecord.getParmInt();
		genSP.setNumbers(Parms);
	}

	public void set1stStatPanel() {
		dbRecord tempRecord = getCurrentRecord();
		double eff;

		double[][] BDGtmp = tempRecord.getBDG(useBiasM);

		double sum = 0;
		for (int i = 1; i < 8; i++)
			sum = sum + (BDGtmp[0][i] - BDGtmp[1][i]);
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
			statTest stat2 = new statTest(tempRecord, selectedMod, useBiasM,
					tempRecord.getReader(), tempRecord.getNormal(),
					tempRecord.getDisease(), 0.05, eff);
			DecimalFormat formatter = new DecimalFormat("0.00");
			// DOF
			String output = formatter.format(stat2.getDOF());
			dfHillis.setText("  df(Hillis 2008)= " + output);
			// tStat
			output = formatter.format(stat2.gettStat());
			tStat.setText("  tStat= " + output);
			// pVal
			formatter = new DecimalFormat("0.0000");
			output = formatter.format(stat2.getpValF());
			pVal.setText("  p-Value= " + output);
			// ci
			formatter = new DecimalFormat("0.00");
			output = formatter.format(stat2.getciTop());
			String output2 = formatter.format(stat2.getciBot());
			confInt.setText("Conf. Int.=(" + output2 + ", " + output + ")");
		} else {
			dfHillis.setText("  df(Hillis 2008)= ");
			tStat.setText("  tStat= ");
			sqrtTotalVar.setText("  sqrt(total var)= ");
			pVal.setText("  p-Value= ");
			confInt.setText("Conf. Int.=");

		}
	}

	/*
	 * ********click the size trial button, set Table 2 based on the new study
	 * size perform statistical analysis***
	 */
	public void sizeTrial(int[] Parms, double[] Parms2, int[] Parms3) {
		int newR = Parms[0];
		int newN = Parms[1];
		int newD = Parms[2];
		int numSplitPlots = Parms3[0];
		int pairedReaders = Parms3[1];
		int pairedCases = Parms3[2];
		dbRecord tempRecord = getCurrentRecord();

		int[][][][] design = createSplitPlotDesign(newR, newN, newD,
				numSplitPlots, pairedReaders, pairedCases);

		double[][] BDGcoeff = dbRecord.genBDGCoeff(newR, newN, newD, design[0],
				design[1]);
		double[][] BCKcoeff = dbRecord.genBCKCoeff(newR, newN, newD,
				BDGcoeff[0]);
		double[][] DBMcoeff = dbRecord.genDBMCoeff(newR, newN, newD);
		double[][] ORcoeff = dbRecord.genORCoeff(newR, newN, newD);
		double[][] MScoeff = dbRecord.genMSCoeff(newR, newN, newD);

		double[][] BDG = new double[4][8];
		double[][] BCK = new double[4][7];
		double[][] DBM = new double[4][6];
		double[][] OR = new double[4][6];
		double[][] MS = new double[4][6];
		if (selectedInput == 2 && MC.getSelectedComp() != 0) {
			BDG = tempRecord.getBDG(0);
			if (MC.getSelectedComp() == 1) {
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
				OR = dbRecord.DBM2OR(0, DBM, newR, newN, newD);
				MS = dbRecord.DBM2MS(DBM, newR, newN, newD);
			} else if (MC.getSelectedComp() == 2) // DBM input is used
			{
				DBM = tempRecord.DBMresize(tempRecord.getDBM(useBiasM), newR,
						newN, newD);
				OR = dbRecord.DBM2OR(0, DBM, newR, newN, newD);
				MS = dbRecord.DBM2MS(DBM, newR, newN, newD);
			} else if (MC.getSelectedComp() == 3) // OR input is used
			{
				DBM = dbRecord.DBM2OR(1, tempRecord.getOR(useBiasM), newR,
						newN, newD);
				DBM = tempRecord.DBMresize(DBM, newR, newN, newD);
				OR = dbRecord.DBM2OR(0, DBM, newR, newN, newD);
				MS = dbRecord.DBM2MS(DBM, newR, newN, newD);
			} else if (MC.getSelectedComp() == 4) // MS input is used
			{
				DBM = dbRecord.DBM2OR(1, tempRecord.getOR(useBiasM), newR,
						newN, newD);
				DBM = tempRecord.DBMresize(DBM, newR, newN, newD);
				MS = dbRecord.DBM2MS(DBM, newR, newN, newD);
			}

		} else {
			BDG = tempRecord.getBDG(useBiasM);
			BCK = dbRecord.BDG2BCK(BDG);
			// DBM = tempRecord.BDG2DBM(BDG,newR,newN,newD);
			// OR = tempRecord.BDG2OR(BDG, newR,newN,newD);
			DBM = dbRecord.BCK2DBM(BCK, newR, newN, newD);
			OR = dbRecord.DBM2OR(0, DBM, newR, newN, newD);
			MS = dbRecord.DBM2MS(DBM, newR, newN, newD);
		}

		double sum = 0;
		for (int sss = 1; sss < 8; sss++)
			sum = sum + (BDG[0][sss] - BDG[1][sss]);

		if (selectedInput == 2) {
			BDG[3] = BDG[0];
			BCK[3] = BCK[0];
			DBM[3][0] = 0;
			DBM[3][1] = 0;
			DBM[3][2] = 0;
			DBM[3][3] = DBM[0][0];
			DBM[3][4] = DBM[0][1];
			DBM[3][5] = DBM[0][2];
			OR = dbRecord.DBM2OR(0, DBM, newR, newN, newD);
			MS = dbRecord.DBM2MS(DBM, newR, newN, newD);

		}

		double[][] BDGdata1 = dbRecord.getBDGTab(selectedMod, BDG, BDGcoeff);
		double[][] BCKdata1 = dbRecord.getBCKTab(selectedMod, BCK, BCKcoeff);
		double[][] DBMdata1 = dbRecord.getDBMTab(selectedMod, DBM, DBMcoeff);
		double[][] ORdata1 = dbRecord.getORTab(selectedMod, OR, ORcoeff);
		double[][] MSdata1 = dbRecord.getMSTab(selectedMod, MS, MScoeff);

		double BDGv = 0;
		double BCKv = 0;
		double DBMv = 0;
		double ORv = 0;
		double MSv = 0;

		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 8; j++) {
				BDGtable2.setValueAt(BDGdata1[i][j], i, j);
				BDGtable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				if (i == 6) {
					BDGv = BDGv + BDGdata1[i][j];
				}
			}
			for (int j = 0; j < 7; j++) {
				BCKtable2.setValueAt(BCKdata1[i][j], i, j);
				BCKtable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				if (i == 6) {
					BCKv = BCKv + BCKdata1[i][j];
				}
			}
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 6; j++) {
				MStable2.setValueAt(MSdata1[i][j], i, j);
				MStable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				if (i == 2) {
					MSv = MSv + MSdata1[i][j];
				}
				// if study is not fully crossed, MS calculation is
				// incorrect
				if (!tempRecord.getFullyCrossedStatus()) {
					MStable2.setValueAt("***", i, j);
				}
			}
			for (int j = 0; j < 6; j++) {
				DBMtable2.setValueAt(DBMdata1[i][j], i, j);
				ORtable2.setValueAt(ORdata1[i][j], i, j);
				DBMtable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				ORtable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				if (i == 2) {
					DBMv = DBMv + DBMdata1[i][j];
					ORv = ORv + ORdata1[i][j];
				}
				// if study is not fully crossed, DBM, OR calculation is
				// incorrect
				if (!tempRecord.getFullyCrossedStatus()) {
					DBMtable2.setValueAt("***", i, j);
					ORtable2.setValueAt("***", i, j);
				}
			}
		}

		DecimalFormat formatter1 = new DecimalFormat("0.000");
		DecimalFormat formatter2 = new DecimalFormat("0.00");
		String output = formatter1.format(Math.sqrt(BDGv));
		BDGvar2.setText("sqrt(Var)=" + output);
		output = formatter1.format(Math.sqrt(BCKv));
		BCKvar2.setText("sqrt(Var)=" + output);
		output = formatter1.format(Math.sqrt(DBMv));
		DBMvar2.setText("sqrt(Var)=" + output);
		output = formatter1.format(Math.sqrt(ORv));
		ORvar2.setText("sqrt(Var)=" + output);
		output = formatter1.format(Math.sqrt(MSv));
		MSvar2.setText("sqrt(Var)=" + output);

		double[] var = new double[3];
		// double sig=Double.parseDouble(sigLevel.getText());
		// double eff=Double.parseDouble(effSize.getText());
		double sig = Parms2[0];
		double eff = Parms2[1];
		double effSize = eff;
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

		statTest stat = new statTest(var, OR[selectedMod], newR, newN + newD,
				sig, eff, BDGv);
		formatter1 = new DecimalFormat("0.000E0");
		formatter2 = new DecimalFormat("0.00");
		output = formatter2.format(stat.getHillisPower());
		HillisPower.setText("      Power(Hillis 2011) = " + output);
		// TODO zpower is undefined when total variance (BDGv) is <= 0
		output = formatter2.format(stat.getZPower());
		ZPower.setText("  Power(Z test)= " + output);
		output = formatter1.format(stat.getDelta());
		Delta.setText("  Delta= " + output);
		output = formatter2.format(stat.getDDF());
		sizedDFHillis.setText("  df(Hillis 2008)= " + output);
		output = formatter2.format(stat.getCVF());
		CVF.setText("  CVF= " + output);

		if (useBiasM == 1) {
			tabbedPane2.setTitleAt(0, "BDG**");
			tabbedPane2.setTitleAt(1, "BCK**");
			tabbedPane2.setTitleAt(2, "DBM**");
			tabbedPane2.setTitleAt(3, "OR**");
			tabbedPane2.setTitleAt(4, "MS**");
		} else if (useBiasM == 0) {
			tabbedPane2.setTitleAt(0, "BDG");
			tabbedPane2.setTitleAt(1, "BCK");
			tabbedPane2.setTitleAt(2, "DBM");
			tabbedPane2.setTitleAt(3, "OR");
			tabbedPane2.setTitleAt(4, "MS");
		}

	}

	// makes a matrix of study design for given split plot and pairing
	// parameters
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

	public int getSelectedMod() {
		return selectedMod;
	}

	public void setSelectedMod(int sel) {
		selectedMod = sel;
		ZPower.setText("Power(Z test) = ");
		HillisPower.setText("Power(Hillis 2011) = ");
		Delta.setText("Delta = ");
		CVF.setText("CVF = ");
		sizedDFHillis.setText("df(Hillis 2008) = ");
		if ((sel == 0) || (sel == 1)) {
			// sigLevel.setEnabled(false);
			// effSize.setEnabled(false);
			genSP.setEff("Effect Size", "0.05");
			// effSizeLabel.setText("Compare to AUC value ");
			// effSize.setText("0.5");
		} else {
			genSP.setEff("Effect Size", "0.05");
			// effSizeLabel.setText("Effect Size");
			// sigLevel.setEnabled(true);
			// effSize.setEnabled(true);
			// effSize.setText("0.05");
		}
	}

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

	public void enableDBMORTabs() {
		tabbedPane1.setEnabledAt(2, true);
		tabbedPane1.setEnabledAt(3, true);
		tabbedPane2.setEnabledAt(2, true);
		tabbedPane2.setEnabledAt(3, true);
		tabbedPane1.setSelectedIndex(2);
		tabbedPane2.setSelectedIndex(2);
	}

	public void setTab1() {
		double[][] BDGdata1 = new double[3][8];
		double[][] BCKdata1 = new double[3][7];
		double[][] DBMdata1 = new double[3][6];
		double[][] ORdata1 = new double[3][6];
		double[][] MSdata1 = new double[3][6];

		dbRecord tempRecord = getCurrentRecord();
		if (selectedInput == 1 && filename.equals(null))
			return;

		BDGdata1 = dbRecord.getBDGTab(selectedMod, tempRecord.getBDG(useBiasM),
				tempRecord.getBDGcoeff());
		BCKdata1 = dbRecord.getBCKTab(selectedMod, tempRecord.getBCK(useBiasM),
				tempRecord.getBCKcoeff());
		DBMdata1 = dbRecord.getDBMTab(selectedMod, tempRecord.getDBM(useBiasM),
				tempRecord.getDBMcoeff());
		ORdata1 = dbRecord.getORTab(selectedMod, tempRecord.getOR(useBiasM),
				tempRecord.getORcoeff());
		MSdata1 = dbRecord.getMSTab(selectedMod, tempRecord.getMS(useBiasM),
				tempRecord.getMScoeff());

		double BDGv = 0;
		double BCKv = 0;
		double DBMv = 0;
		double ORv = 0;
		double MSv = 0;

		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 8; j++) {
				BDGtable1.setValueAt(BDGdata1[i][j], i, j);
				BDGtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				if (i == 6) {
					BDGv = BDGv + BDGdata1[i][j];
				}
			}
			for (int j = 0; j < 7; j++) {
				BCKtable1.setValueAt(BCKdata1[i][j], i, j);
				BCKtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				if (i == 6) {
					BCKv = BCKv + BCKdata1[i][j];
				}
			}
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 6; j++) {
				DBMtable1.setValueAt(DBMdata1[i][j], i, j);
				ORtable1.setValueAt(ORdata1[i][j], i, j);
				DBMtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				ORtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				if (i == 2) {
					DBMv = DBMv + DBMdata1[i][j];
					ORv = ORv + ORdata1[i][j];
				}
				// if study is not fully crossed, DBM, OR calculation is
				// incorrect
				if (!tempRecord.getFullyCrossedStatus()) {
					DBMtable1.setValueAt("***", i, j);
					ORtable1.setValueAt("***", i, j);
				}

			}
			for (int j = 0; j < 6; j++) {
				MStable1.setValueAt(MSdata1[i][j], i, j);
				MStable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				if (i == 2) {
					MSv = MSv + MSdata1[i][j];
				}
				// if study is not fully crossed, MS calculation is
				// incorrect
				if (!tempRecord.getFullyCrossedStatus()) {
					MStable1.setValueAt("***", i, j);
				}
			}
		}
		resetTab2();
		DecimalFormat formatter = new DecimalFormat("0.000");
		String output = formatter.format(Math.sqrt(BDGv));
		BDGvar.setText("sqrt(Var)=" + output);
		output = formatter.format(Math.sqrt(BCKv));
		BCKvar.setText("sqrt(Var)=" + output);
		output = formatter.format(Math.sqrt(DBMv));
		DBMvar.setText("sqrt(Var)=" + output);
		output = formatter.format(Math.sqrt(ORv));
		totalStd = Math.sqrt(ORv);
		ORvar.setText("sqrt(Var)=" + output);
		output = formatter.format(Math.sqrt(MSv));
		MSvar.setText("sqrt(Var)=" + output);
		sqrtTotalVar.setText("   sqrt(total var)=" + output);

		// System.out.println("BDG="+BDGv+" BCK="+BCKv+" DBM="+DBMv+" OR="+ORv);

		if (useBiasM == 1) {
			tabbedPane1.setTitleAt(0, "BDG**");
			tabbedPane1.setTitleAt(1, "BCK**");
			tabbedPane1.setTitleAt(2, "DBM**");
			tabbedPane1.setTitleAt(3, "OR**");
			tabbedPane1.setTitleAt(4, "MS**");
		} else if (useBiasM == 0) {
			tabbedPane1.setTitleAt(0, "BDG");
			tabbedPane1.setTitleAt(1, "BCK");
			tabbedPane1.setTitleAt(2, "DBM");
			tabbedPane1.setTitleAt(3, "OR");
			tabbedPane1.setTitleAt(4, "MS");
		}
	}

	public void setAUCoutput() {
		dbRecord tempRecord = getCurrentRecord();
		String displayAUC = tempRecord.getAUC(selectedMod);
		String displayParm = tempRecord.getParm();
		aucOutput.setText(displayAUC + displayParm);
		System.out.println("displayParm" + displayParm);
	}

	/* constructor for hte graphic interface */
	public GUInterface(MRMC lsttemp, Container cp) {
		int i;
		lst = lsttemp;
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

		JPanel InputCBPane = new JPanel();
		InputCBPane.setLayout(new FlowLayout());
		JLabel inLabel = new JLabel("Select an input method: ");
		String comboBoxItems[] = { DB, Pilot, Manual };
		JComboBox cb = new JComboBox(comboBoxItems);
		cb.setEditable(false);
		cb.setSelectedIndex(0);
		cb.addActionListener(new inputModListener());
		JButton buttonReset = new JButton("Reset");
		buttonReset.addActionListener(new ResetListner());
		InputCBPane.add(inLabel);
		InputCBPane.add(cb);
		InputCBPane.add(buttonReset);

		// ***********************************************************************
		// *******************Create the DB
		// panel*********************************
		// ***********************************************************************
		fdaDB = lst.getDB();
		int DBsize = fdaDB.getNoOfItems();
		String[] dbBoxItems = new String[DBsize];
		Records = fdaDB.getRecords();
		for (i = 0; i < DBsize; i++) {
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
		MS = new DBModSelect(modSelPanel, this);

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

		// ***********************************************************************
		// *******************Create the Pilot
		// panel******************************
		// ***********************************************************************
		JPanel pilotCard = new JPanel();
		layout = new GroupLayout(pilotCard);
		pilotCard.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		studyLabel = new JLabel("pilot study...");
		JPanel pilotCard2 = new JPanel();
		MS2 = new PilotModSelect(pilotCard2, this);
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

		// ***********************************************************************
		// *******************Create the Manual
		// Panel*****************************
		// ***********************************************************************
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
		// String displayAUC=Records[selectedDB].getAUC(selectedMod);
		// String displayParm=Records[selectedDB].getParm();
		// aucOutput = new JLabel(displayAUC+"\t"+displayParm);
		aucOutput = new JLabel("");
		outPane.add(aucOutput);
		// *******************************************************************
		// *************tabbed panel 1*********************************
		// *********************************************************************
		// Create one-shot tab
		String[] rowNamesDiff = new String[] { "comp M0", "coeff M0",
				"comp M1", "coeff M1", "product M0,M1", "2*coeff M0-M1",
				"total" };
		String[] rowNamesSingle = new String[] { "components", "coeff", "total" };
		JPanel panelBDG1 = new JPanel();
		String[] BDGnames = { "M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8" };

		DefaultTableModel dm = new DefaultTableModel(7, 8);
		BDGtable1 = new JTable(dm);
		JScrollPane BDGscroll = genTable(BDGtable1, BDGnames, rowNamesDiff);
		int height = BDGtable1.getRowHeight();
		panelBDG1.add(BDGscroll);
		BDGtable1.setPreferredScrollableViewportSize(new Dimension(650,
				height * 8));
		BDGtable1.setFillsViewportHeight(true);
		BDGvar = new JLabel("sqrt(Var)=0.00");
		panelBDG1.add(BDGvar);
		// Create BCK tab
		JPanel panelBCK1 = new JPanel();
		String[] BCKnames = { "N", "D", "N~D", "R", "N~R", "D~R", "R~N~D" };
		dm = new DefaultTableModel(7, 7);
		BCKtable1 = new JTable(dm);
		JScrollPane BCKscroll = genTable(BCKtable1, BCKnames, rowNamesDiff);
		panelBCK1.add(BCKscroll);
		height = BCKtable1.getRowHeight();
		BCKtable1.setPreferredScrollableViewportSize(new Dimension(650,
				height * 8));
		BCKtable1.setFillsViewportHeight(true);
		BCKvar = new JLabel("sqrt(Var)=0.00");
		panelBCK1.add(BCKvar);
		// Create DBM tab
		JPanel panelDBM1 = new JPanel();
		String[] DBMnames = { "R", "C", "R~C", "T~R", "T~C", "T~R~C" };
		dm = new DefaultTableModel(3, 6);
		DBMtable1 = new JTable(dm);
		JScrollPane DBMscroll = genTable(DBMtable1, DBMnames, rowNamesSingle);
		panelDBM1.add(DBMscroll);
		height = DBMtable1.getRowHeight();
		DBMtable1.setPreferredScrollableViewportSize(new Dimension(650,
				height * 4));
		DBMtable1.setFillsViewportHeight(true);
		DBMvar = new JLabel("sqrt(Var)=0.00");
		panelDBM1.add(DBMvar);
		// Create OR tab
		JPanel panelOR1 = new JPanel();
		String[] ORnames = { "R", "TR", "COV1", "COV2", "COV3", "ERROR" };
		dm = new DefaultTableModel(3, 6);
		ORtable1 = new JTable(dm);
		JScrollPane ORscroll = genTable(ORtable1, ORnames, rowNamesSingle);
		panelOR1.add(ORscroll);
		height = ORtable1.getRowHeight();
		ORtable1.setPreferredScrollableViewportSize(new Dimension(650,
				height * 4));
		ORtable1.setFillsViewportHeight(true);
		ORvar = new JLabel("sqrt(Var)=0.00");
		panelOR1.add(ORvar);

		// create MS tab
		JPanel panelMS1 = new JPanel();
		String[] MSnames = { "R", "C", "RC", "MR", "MC", "MRC" };
		dm = new DefaultTableModel(3, 6);
		MStable1 = new JTable(dm);
		JScrollPane MSscroll = genTable(MStable1, MSnames, rowNamesSingle);
		panelMS1.add(MSscroll);
		height = MStable1.getRowHeight();
		MStable1.setPreferredScrollableViewportSize(new Dimension(650,
				height * 4));
		MStable1.setFillsViewportHeight(true);
		MSvar = new JLabel("sqrt(Var)=0.00");
		panelMS1.add(MSvar);

		tabbedPane1 = new JTabbedPane();
		tabbedPane1.addTab("BDG", panelBDG1);
		tabbedPane1.addTab("BCK", panelBCK1);
		tabbedPane1.addTab("DBM", panelDBM1);
		tabbedPane1.addTab("OR", panelOR1);
		tabbedPane1.addTab("MS", panelMS1);

		// *******************************************************************
		// *************tabbed panel 2*********************************
		// *********************************************************************
		// Create one-shot tab
		JPanel panelBDG2 = new JPanel();
		dm = new DefaultTableModel(7, 8);
		BDGtable2 = new JTable(dm);
		BCKscroll = genTable(BDGtable2, BDGnames, rowNamesDiff);
		panelBDG2.add(BCKscroll);
		height = BDGtable2.getRowHeight();
		BDGtable2.setPreferredScrollableViewportSize(new Dimension(650,
				height * 8));
		BDGtable2.setFillsViewportHeight(true);
		BDGvar2 = new JLabel("sqrt(Var)=0.00");
		panelBDG2.add(BDGvar2);
		// Create BCK tab
		JPanel panelBCK2 = new JPanel();
		dm = new DefaultTableModel(7, 7);
		BCKtable2 = new JTable(dm);
		BCKscroll = genTable(BCKtable2, BCKnames, rowNamesDiff);
		panelBCK2.add(BCKscroll);
		height = BCKtable2.getRowHeight();
		BCKtable2.setPreferredScrollableViewportSize(new Dimension(650,
				height * 8));
		BCKtable2.setFillsViewportHeight(true);
		BCKvar2 = new JLabel("sqrt(Var)=0.00");
		panelBCK2.add(BCKvar2);
		// Create DBM tab
		JPanel panelDBM2 = new JPanel();
		dm = new DefaultTableModel(3, 6);
		DBMtable2 = new JTable(dm);
		BCKscroll = genTable(DBMtable2, DBMnames, rowNamesSingle);
		panelDBM2.add(BCKscroll);
		height = DBMtable2.getRowHeight();
		DBMtable2.setPreferredScrollableViewportSize(new Dimension(650,
				height * 4));
		DBMtable2.setFillsViewportHeight(true);
		DBMvar2 = new JLabel("sqrt(Var)=0.00");
		panelDBM2.add(DBMvar2);
		// Create OR tab
		JPanel panelOR2 = new JPanel();
		dm = new DefaultTableModel(3, 6);
		ORtable2 = new JTable(dm);
		BCKscroll = genTable(ORtable2, ORnames, rowNamesSingle);
		panelOR2.add(BCKscroll);
		height = ORtable2.getRowHeight();
		ORtable2.setPreferredScrollableViewportSize(new Dimension(650,
				height * 4));
		ORtable2.setFillsViewportHeight(true);
		ORvar2 = new JLabel("sqrt(Var)=0.00");
		panelOR2.add(ORvar2);

		// create MS tab
		JPanel panelMS2 = new JPanel();
		// String[] MSnames={"MS1","MS2","MS3","MS4","MS5","MS6"};
		dm = new DefaultTableModel(3, 6);
		MStable2 = new JTable(dm);
		MSscroll = genTable(MStable2, MSnames, rowNamesSingle);
		panelMS2.add(MSscroll);
		height = MStable2.getRowHeight();
		MStable2.setPreferredScrollableViewportSize(new Dimension(650,
				height * 4));
		MStable2.setFillsViewportHeight(true);

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
		CVF = new JLabel("  CVF= 0.00");
		ZPower = new JLabel("  Power(Z test)= 0.00");
		HillisPower = new JLabel("      Power(Hillis 2011) = 0.00");
		MSvar2 = new JLabel("sqrt(Var)=0.00");
		// TODO why is sqrt(var) MSvar2?

		// panelStat11.add(new JLabel("Significance Level"));
		// panelStat11.add(sigLevel);
		// panelStat11.add(effSizeLabel);
		// panelStat11.add(effSize);
		panelStat12.add(new JLabel("Sizing Results:   "));
		panelStat12.add(MSvar2);
		panelStat12.add(Delta);
		panelStat12.add(sizedDFHillis);
		panelStat12.add(CVF);
		panelStat12.add(HillisPower);
		panelStat12.add(ZPower);

		// *******************************************************************
		// *************Generate Sizing panel*********************************
		// *******************************************************************
		JPanel sizingPanel = new JPanel();
		int[] Parms = Records[selectedDB].getParmInt();
		// genSP = new
		// sPanel(Parms,Float.parseInt(sigLevel.getText()),Float.parseInt(effSize.getText()),sizingPanel,this);
		genSP = new sPanel(Parms, sizingPanel, this);

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

		cp.add(InputCBPane);
		cp.add(inputCards);
		cp.add(panelSep);
		cp.add(outPane);
		cp.add(panelStat2);
		cp.add(tabbedPane1);
		// cp.add(tabbedPane2);
		// cp.add(panelStat11);
		cp.add(panelSep2);
		cp.add(sizingPanel);
		cp.add(panelStat12);
		cp.add(panelSep3);
		cp.add(panelSummary);
	}

	public JTextArea genFrame() {
		JFrame descFrame = new JFrame();
		descFrame.getRootPane()
				.setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
		String str = "temptemptemp";
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

	/* button to show window with database summary using DBM method */
	class dbmBtnListner implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTextArea desc = genFrame();
			desc.setText(fdaDB.recordsSummary(selectedSummary, SummaryUseMLE,
					"DBM"));
		}
	}

	/* button to show window with database summary using BDG method */
	class bdgBtnListner implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTextArea desc = genFrame();
			desc.setText(fdaDB.recordsSummary(selectedSummary, SummaryUseMLE,
					"BDG"));
		}
	}

	/* button to show window with database summary using BCK method */
	class bckBtnListner implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTextArea desc = genFrame();
			desc.setText(fdaDB.recordsSummary(selectedSummary, SummaryUseMLE,
					"BCK"));
		}
	}

	/* button to show window with database summary using OR method */
	class orBtnListner implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JTextArea desc = genFrame();
			desc.setText(fdaDB.recordsSummary(selectedSummary, SummaryUseMLE,
					"OR"));
		}
	}

	/* Drop down menu to select input method */
	class inputModListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			@SuppressWarnings("unchecked")
			JComboBox cb = (JComboBox) evt.getSource();
			CardLayout cl = (CardLayout) (inputCards.getLayout());
			cl.show(inputCards, (String) cb.getSelectedItem());
			selectedInput = cb.getSelectedIndex();
			reset();
			// System.out.println("Input method"+(String)cb.getSelectedItem()+"is selected");
		}
	}

	/* button to clear any input for analysis */
	class ResetListner implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			reset();
		}
	}

	/* button to open window describing record taken from internal database */
	class descButtonListner implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			// System.out.println("output record"+ selectedDB+"description");

			JFrame descFrame = new JFrame();

			descFrame.getRootPane().setWindowDecorationStyle(
					JRootPane.PLAIN_DIALOG);
			String str = "temptemptemp";
			JTextArea desc = new JTextArea(str, 18, 40);
			desc.setText(Records[selectedDB].getRecordDesp());
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

	/* drop down menu to choose a particular dataset from internal database */
	class dbActionListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			@SuppressWarnings("unchecked")
			JComboBox cb = (JComboBox) evt.getSource();
			selectedDB = (int) cb.getSelectedIndex();
		}
	}

	class fmtSelListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String str;
			str = e.getActionCommand();
			System.out.println(str + "format radiobutton selected");
		}
	}

	/*
	 * radio buttons to choose if database summary has single modality or
	 * difference
	 */
	class SummarySelListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String str;
			str = e.getActionCommand();
			if (str == "Single Modality") {
				selectedSummary = 0;
			}
			if (str == "Difference") {
				selectedSummary = 1;
			}
		}
	}

	/* radio buttons selecting whether or not to use MLE */
	class MLESelListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String str;
			str = e.getActionCommand();
			if (str == "Yes") {
				SummaryUseMLE = 1;
			}
			if (str == "No") {
				SummaryUseMLE = 0;
			}
		}
	}

	/*
	 * in case of command line / standalone application, the user clicks the
	 * browse button to load the raw data of a pilot study
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
					usr = new inputFile(filename);
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

				currMod1 = -1;
				currMod2 = -1;
				MS2.updateModPanel();

			}
		}
	}

	/* button to open window displaying info on how to format raw data for input */
	class fmtHelpButtonListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			dataFormat fmt = new dataFormat();
			JOptionPane.showMessageDialog(lst.getFrame(),
					fmt.getInfo() + fmt.getSample(), "Information",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

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

	/* check whether there is negative components */
	public int checkNegative() {
		hasNegative = 0;

		if (selectedInput == 1) {
			if (currMod1 == -1 && currMod2 == -1) {
				JFrame frame = lst.getFrame();
				JOptionPane.showMessageDialog(frame,
						"You must select at least one modality", "Error",
						JOptionPane.ERROR_MESSAGE);
				return 0;
			}
			System.out.println("test" + "input" + selectedInput);
			String name = pilotFile.getText();
			System.out.println("name=" + name);
			// if (name.equals(null) || !(new File("filename")).exists())
			if (name.equals(null) || name.equals("")) {
				JFrame frame = lst.getFrame();
				JOptionPane.showMessageDialog(frame, "invalid input", " Error",
						JOptionPane.ERROR_MESSAGE);
				return 0;
			}
		}

		dbRecord tempRecord = getCurrentRecord();
		double[][] tempBDG = tempRecord.getBDG(0);
		for (int i = 0; i < 8; i++) {
			if (tempBDG[selectedMod][i] < 0)
				hasNegative = 1;
		}
		if (hasNegative == 1 & useBiasM == 0) {
			JFrame frame = lst.getFrame();
			int result = JOptionPane
					.showConfirmDialog(
							frame,
							"There are negative values in the components, do you want\nto proceed with MLE estimates to avoid negatives?");
			if (JOptionPane.CANCEL_OPTION == result) {
				System.out.println("cancel");
			} else if (JOptionPane.YES_OPTION == result) {
				MS.setUseBiasM(1);
				MS2.setUseBiasM(true);
				useBiasM = 1;
			} else if (JOptionPane.NO_OPTION == result) {
				MS.setUseBiasM(0);
				MS2.setUseBiasM(false);
				useBiasM = 0;
			}

		}
		return 1;
	}

	/*
	 * generate a table, call this function to generate the table for each set
	 * of components. This function sets the format and headers of each table
	 */
	public JScrollPane genTable(JTable table, String[] names, String[] rowNames) {
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int i = 0; i < names.length; i++)
			table.getColumnModel().getColumn(i).setHeaderValue(names[i]);

		JList rowHeader = new JList(rowNames);
		rowHeader.setFixedCellWidth(80);

		rowHeader.setFixedCellHeight(table.getRowHeight());
		rowHeader.setCellRenderer(new RowHeaderRenderer(table));

		JScrollPane scroll = new JScrollPane(table);
		scroll.setRowHeaderView(rowHeader);
		return scroll;

	}

	/* Table cell formatter renderer */
	static class DecimalFormatRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final DecimalFormat formatter = new DecimalFormat(
				"0.00000E0");

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
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
}
