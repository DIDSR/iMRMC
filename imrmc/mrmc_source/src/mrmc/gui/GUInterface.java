package mrmc.gui;

/* **************************************************
 * ****************GUInterface.java******************
 * **************************************************
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
 ************************************************************
 ************************************************************/

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.lang.Math;
import javax.swing.table.*;
import java.text.DecimalFormat;
import mrmc.chart.BarGraph;
import mrmc.chart.PresencePlot;
import mrmc.core.MRMC;
import mrmc.core.dbRecord;
import mrmc.core.inputFile;
import mrmc.core.mrmcDB;
import mrmc.core.statTest;

import org.jfree.ui.RefineryUtilities;

import java.awt.datatransfer.*;

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
	JLabel currentMod;

	// JTextField sigLevel;
	// JTextField effSize;
	// JLabel effSizeLabel;
	JLabel HillisPower;
	JLabel ZPower;
	JLabel Delta;
	JLabel DDF;
	JLabel CVF;
	JLabel tStat;
	JLabel sqrtTotalVar;
	JLabel df;
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
	ModSelect MS, MS2;
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
		results = results + "\t" + df.getText();
		results = results + "\t" + pVal.getText();
		results = results + "\t" + confInt.getText();
		results = results + "\t" + sqrtTotalVar.getText();
		return results;
	}

	public String getStat2() {
		String results = MSvar2.getText();
		results = results + "\t" + Delta.getText();
		results = results + "\t" + DDF.getText();
		results = results + "\t" + CVF.getText();
		results = results + "\t" + HillisPower.getText();
		results = results + "\t" + ZPower.getText();
		return results;
	}

	/* ****************************** reset************************ */
	public void resetTab2() {
		for (int i = 0; i < 3; i++) {
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
		for (int i = 0; i < 3; i++) {
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
		DDF.setText("");
		CVF.setText("");
		tStat.setText("");
		sqrtTotalVar.setText("");
		df.setText("");
		pVal.setText("");
		confInt.setText("");

		HillisPower.setText("");
		// Selections
		setUseBiasM(0);
		MS.setUseBiasM(0);
		MS2.setUseBiasM(0);
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
		if (!lst.getIsApplet())
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
		dbRecord tempRecord = new dbRecord();
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
			df.setText("  df(Hillis 2008)= " + output);
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
			df.setText("  df(Hillis 2008)= ");
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
	public void sizeTrial(int[] Parms, double[] Parms2) {
		int i, j;
		int newR = Parms[0];
		int newN = Parms[1];
		int newD = Parms[2];
		dbRecord tempRecord = getCurrentRecord();

		double[][] BDGcoeff = tempRecord.genBDGCoeff(newR, newN, newD);
		double[][] DBMcoeff = tempRecord.genDBMCoeff(newR, newN, newD);
		double[][] BCKcoeff = tempRecord.genBCKCoeff(newR, newN, newD);
		double[][] ORcoeff = tempRecord.genORCoeff(newR, newN, newD);
		double[][] MScoeff = tempRecord.genMSCoeff(newR, newN, newD);

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
				BCK = tempRecord.BCKresize(tempRecord.getBCK(0), newR, newN,
						newD);
				DBM = tempRecord.DBMresize(tempRecord.getDBM(0), newR, newN,
						newD);
				OR = tempRecord.DBM2OR(0, DBM, newR, newN, newD);
				MS = tempRecord.DBM2MS(DBM, newR, newN, newD);
			} else if (MC.getSelectedComp() == 2) // DBM input is used
			{
				DBM = tempRecord.DBMresize(tempRecord.getDBM(0), newR, newN,
						newD);
				OR = tempRecord.DBM2OR(0, DBM, newR, newN, newD);
				MS = tempRecord.DBM2MS(DBM, newR, newN, newD);
			} else if (MC.getSelectedComp() == 3) // OR input is used
			{
				DBM = tempRecord.DBM2OR(1, tempRecord.getOR(0), newR, newN,
						newD);
				DBM = tempRecord.DBMresize(DBM, newR, newN, newD);
				OR = tempRecord.DBM2OR(0, DBM, newR, newN, newD);
				MS = tempRecord.DBM2MS(DBM, newR, newN, newD);
			} else if (MC.getSelectedComp() == 4) // MS input is used
			{
				DBM = tempRecord.DBM2OR(1, tempRecord.getOR(0), newR, newN,
						newD);
				DBM = tempRecord.DBMresize(DBM, newR, newN, newD);
				MS = tempRecord.DBM2MS(DBM, newR, newN, newD);
			}

		} else {
			BDG = tempRecord.getBDG(useBiasM);
			BCK = tempRecord.BDG2BCK(BDG);
			// DBM = tempRecord.BDG2DBM(BDG,newR,newN,newD);
			// OR = tempRecord.BDG2OR(BDG, newR,newN,newD);
			DBM = tempRecord.BCK2DBM(BCK, newR, newN, newD);
			OR = tempRecord.DBM2OR(0, DBM, newR, newN, newD);
			MS = tempRecord.DBM2MS(DBM, newR, newN, newD);
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
			OR = tempRecord.DBM2OR(0, DBM, newR, newN, newD);
			MS = tempRecord.DBM2MS(DBM, newR, newN, newD);

		}

		double[][] BDGdata1 = tempRecord.getBDGTab(selectedMod, BDG, BDGcoeff);
		double[][] BCKdata1 = tempRecord.getBCKTab(selectedMod, BCK, BCKcoeff);
		double[][] DBMdata1 = tempRecord.getDBMTab(selectedMod, DBM, DBMcoeff);
		double[][] ORdata1 = tempRecord.getORTab(selectedMod, OR, ORcoeff);
		double[][] MSdata1 = tempRecord.getMSTab(selectedMod, MS, MScoeff);

		double BDGv = 0;
		double BCKv = 0;
		double DBMv = 0;
		double ORv = 0;
		double MSv = 0;

		for (i = 0; i < 3; i++) {
			for (j = 0; j < 8; j++) {
				BDGtable2.setValueAt(BDGdata1[i][j], i, j);
				BDGtable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				if (i == 2) {
					BDGv = BDGv + BDGdata1[i][j];
				}
			}
			for (j = 0; j < 7; j++) {
				BCKtable2.setValueAt(BCKdata1[i][j], i, j);
				BCKtable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				if (i == 2) {
					BCKv = BCKv + BCKdata1[i][j];
				}
			}
			for (j = 0; j < 6; j++) {
				MStable2.setValueAt(MSdata1[i][j], i, j);
				MStable2.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				if (i == 2) {
					MSv = MSv + MSdata1[i][j];
				}
			}
			for (j = 0; j < 6; j++) {
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
			}
		}

		DecimalFormat formatter = new DecimalFormat("0.000");
		String output = formatter.format(Math.sqrt(BDGv));
		BDGvar2.setText("sqrt(Var)=" + output);
		output = formatter.format(Math.sqrt(BCKv));
		BCKvar2.setText("sqrt(Var)=" + output);
		output = formatter.format(Math.sqrt(DBMv));
		DBMvar2.setText("sqrt(Var)=" + output);
		output = formatter.format(Math.sqrt(ORv));
		ORvar2.setText("sqrt(Var)=" + output);
		output = formatter.format(Math.sqrt(MSv));
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
		double obsDiff = Math.abs(tempRecord.getAUCinNumber(0)
				- tempRecord.getAUCinNumber(1));
		if (selectedMod == 0 || selectedMod == 1)
			obsDiff = tempRecord.getAUCinNumber(selectedMod) - 0.5;
		if (sum != 0 || sum == 0 && selectedMod != 3) {
			statTest stat = new statTest(var, OR[selectedMod], newR, newN
					+ newD, sig, eff, obsDiff, BDGv);
			formatter = new DecimalFormat("0.00");
			output = formatter.format(stat.getHillisPower());
			HillisPower.setText("      Power(Hillis 2011) = " + output);
			output = formatter.format(stat.getZPower());
			ZPower.setText("  Power(Z test)= " + output);
			output = formatter.format(stat.getDelta());
			Delta.setText("  Delta= " + output);
			output = formatter.format(stat.getDDF());
			DDF.setText("  DDF= " + output);
			output = formatter.format(stat.getCVF());
			CVF.setText("  CVF= " + output);
		} else {
			HillisPower.setText("      Power(Hillis 2011) = ");
			ZPower.setText("  Power(Z test)= ");
			Delta.setText("  Delta= ");
			DDF.setText("  DDF= ");
			CVF.setText("  CVF= ");
		}
		if (selectedMod == 3) {
			int[] oldParms = tempRecord.getParmInt();
			double[][] oldOR = tempRecord.getOR(useBiasM);
			// double power2011 = stat.FTest2011(oldOR[selectedMod],
			// oldParms[0], oldParms[1]+oldParms[2], newR, newN+newD, sig, eff);
			// System.out.println("Print POwer 2011="+power2011);
		}
		// output = formatter.format(stat2.getDOF());
		// df.setText("  df= "+output);
		// output = formatter.format(stat2.gettStat());
		// tStat.setText("  tStat= "+output);
		// output = formatter.format(stat2.getpValF());
		// pVal.setText("  p-Value= "+output);
		// output = formatter.format(stat2.getciTop());
		// String output2 = formatter.format(stat2.getciBot());
		// confInt.setText("Conf. Int.=("+output2+", "+output+")");

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

	public int getSelectedMod() {
		return selectedMod;
	}

	public void setSelectedMod(int sel) {
		selectedMod = sel;
		ZPower.setText("Power(Z test) = ");
		HillisPower.setText("Power(Hillis 2011) = ");
		Delta.setText("Delta = ");
		CVF.setText("CVF = ");
		DDF.setText("DDF = ");
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
		tabbedPane2.setEnabledAt(0, false);
		tabbedPane2.setEnabledAt(1, false);
		tabbedPane2.setEnabledAt(2, false);
		tabbedPane2.setEnabledAt(3, false);
	}

	public void enableTabs() {
		tabbedPane1.setEnabledAt(0, true);
		tabbedPane1.setEnabledAt(1, true);
		tabbedPane1.setEnabledAt(2, true);
		tabbedPane1.setEnabledAt(3, true);
		tabbedPane2.setEnabledAt(0, true);
		tabbedPane2.setEnabledAt(1, true);
		tabbedPane2.setEnabledAt(2, true);
		tabbedPane2.setEnabledAt(3, true);
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
		int i, j;

		double[][] BDGdata1 = new double[3][8];
		double[][] BCKdata1 = new double[3][7];
		double[][] DBMdata1 = new double[3][6];
		double[][] ORdata1 = new double[3][6];
		double[][] MSdata1 = new double[3][6];

		dbRecord tempRecord = getCurrentRecord();
		if (selectedInput == 1 && filename.equals(null))
			return;

		BDGdata1 = tempRecord.getBDGTab(selectedMod,
				tempRecord.getBDG(useBiasM), tempRecord.getBDGcoeff());
		BCKdata1 = tempRecord.getBCKTab(selectedMod,
				tempRecord.getBCK(useBiasM), tempRecord.getBCKcoeff());
		DBMdata1 = tempRecord.getDBMTab(selectedMod,
				tempRecord.getDBM(useBiasM), tempRecord.getDBMcoeff());
		ORdata1 = tempRecord.getORTab(selectedMod, tempRecord.getOR(useBiasM),
				tempRecord.getORcoeff());
		MSdata1 = tempRecord.getMSTab(selectedMod, tempRecord.getMS(useBiasM),
				tempRecord.getMScoeff());

		double BDGv = 0;
		double BCKv = 0;
		double DBMv = 0;
		double ORv = 0;
		double MSv = 0;
		for (i = 0; i < 3; i++) {
			for (j = 0; j < 8; j++) {
				BDGtable1.setValueAt(BDGdata1[i][j], i, j);
				BDGtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				if (i == 2) {
					BDGv = BDGv + BDGdata1[i][j];
				}
			}
			for (j = 0; j < 7; j++) {
				BCKtable1.setValueAt(BCKdata1[i][j], i, j);
				BCKtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				if (i == 2) {
					BCKv = BCKv + BCKdata1[i][j];
				}
			}
			for (j = 0; j < 6; j++) {
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
			}
			for (j = 0; j < 6; j++) {
				MStable1.setValueAt(MSdata1[i][j], i, j);
				MStable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				if (i == 2) {
					MSv = MSv + MSdata1[i][j];
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
		JComboBox<String> cb = new JComboBox<String>(comboBoxItems);
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
		JComboBox<String> dbCB = new JComboBox<String>(dbBoxItems);
		dbCB.setEditable(false);
		dbCB.addActionListener(new dbActionListener());
		dbCB.setSelectedIndex(0);
		JButton descButton = new JButton("Record Description");
		descButton.addActionListener(new descButtonListner());
		JPanel modSelPanel = new JPanel();
		MS = new ModSelect(modSelPanel, this);

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
		MS2 = new ModSelect(pilotCard2, this);
		JButton fmtHelpButton = new JButton("Format Info.");
		JButton graphButton = new JButton("Input Statistics Charts");
		JButton holesButton = new JButton("Show Data Holes");
		JButton modsButton = new JButton("Select Modalities");
		currentMod = new JLabel("");

		if (!lst.getIsApplet()) {
			pilotFile = new JTextField(20);
			JButton browseButton = new JButton("Browse...");
			browseButton.addActionListener(new brwsButtonListner());
			fmtHelpButton.addActionListener(new fmtHelpButtonListner());
			graphButton.addActionListener(new graphButtonListner());
			holesButton.addActionListener(new holesButtonListner());
			modsButton.addActionListener(new modsButtonListner());
			layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(
					layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addGroup(
									layout.createSequentialGroup()
											.addComponent(studyLabel)
											.addComponent(pilotFile)
											.addComponent(browseButton)
											.addComponent(fmtHelpButton)
											.addComponent(graphButton)
											.addComponent(holesButton)
											.addComponent(modsButton))
							.addGroup(
									layout.createSequentialGroup()
											.addComponent(pilotCard2))
							.addGroup(
									layout.createSequentialGroup()
											.addComponent(currentMod))));

			layout.setVerticalGroup(layout
					.createSequentialGroup()
					.addGroup(
							layout.createParallelGroup(
									GroupLayout.Alignment.BASELINE)
									.addComponent(studyLabel)
									.addComponent(pilotFile)
									.addComponent(browseButton)
									.addComponent(fmtHelpButton)
									.addComponent(graphButton)
									.addComponent(holesButton)
									.addComponent(modsButton))
					.addGroup(
							layout.createParallelGroup(
									GroupLayout.Alignment.LEADING)
									.addComponent(pilotCard2))
					.addGroup(
							layout.createParallelGroup(
									GroupLayout.Alignment.CENTER).addComponent(
									currentMod)));
		} else {
			JButton inputByHandButton = new JButton("Input Pilot study Data");
			inputByHandButton.addActionListener(new InputByHandListner());
			fmtHelpButton.addActionListener(new fmtHelpButtonListner());
			graphButton.addActionListener(new graphButtonListner());
			holesButton.addActionListener(new holesButtonListner());
			modsButton.addActionListener(new modsButtonListner());
			layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(
					layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addGroup(
									layout.createSequentialGroup()
											.addComponent(studyLabel)
											.addComponent(inputByHandButton)
											.addComponent(fmtHelpButton)
											.addComponent(graphButton)
											.addComponent(holesButton)
											.addComponent(modsButton)
											.addComponent(currentMod))
							.addGroup(
									layout.createSequentialGroup()
											.addComponent(pilotCard2))));

			layout.setVerticalGroup(layout
					.createSequentialGroup()
					.addGroup(
							layout.createParallelGroup(
									GroupLayout.Alignment.BASELINE)
									.addComponent(studyLabel)
									.addComponent(inputByHandButton)
									.addComponent(fmtHelpButton)
									.addComponent(graphButton))
					.addGroup(
							layout.createParallelGroup(
									GroupLayout.Alignment.LEADING)
									.addComponent(pilotCard2)));
		}

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
		JPanel panelBDG1 = new JPanel();
		String[] BDGnames = { "M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8" };
		DefaultTableModel dm = new DefaultTableModel(3, 8);
		BDGtable1 = new JTable(dm);
		JScrollPane scroll = genTable(BDGtable1, BDGnames);
		panelBDG1.add(scroll);
		int height = BDGtable1.getRowHeight();
		BDGtable1.setPreferredScrollableViewportSize(new Dimension(650,
				height * 4));
		BDGtable1.setFillsViewportHeight(true);
		BDGvar = new JLabel("sqrt(Var)=0.00");
		panelBDG1.add(BDGvar);
		// Create BCK tab
		JPanel panelBCK1 = new JPanel();
		String[] BCKnames = { "N", "D", "N~D", "R", "N~R", "D~R", "R~N~D" };
		dm = new DefaultTableModel(3, 7);
		BCKtable1 = new JTable(dm);
		JScrollPane scroll2 = genTable(BCKtable1, BCKnames);
		panelBCK1.add(scroll2);
		height = BCKtable1.getRowHeight();
		BCKtable1.setPreferredScrollableViewportSize(new Dimension(650,
				height * 4));
		BCKtable1.setFillsViewportHeight(true);
		BCKvar = new JLabel("sqrt(Var)=0.00");
		panelBCK1.add(BCKvar);
		// Create DBM tab
		JPanel panelDBM1 = new JPanel();
		String[] DBMnames = { "R", "C", "R~C", "T~R", "T~C", "T~R~C" };
		dm = new DefaultTableModel(3, 6);
		DBMtable1 = new JTable(dm);
		JScrollPane scroll3 = genTable(DBMtable1, DBMnames);
		panelDBM1.add(scroll3);
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
		JScrollPane scroll4 = genTable(ORtable1, ORnames);
		panelOR1.add(scroll4);
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
		JScrollPane scroll5 = genTable(MStable1, MSnames);
		panelMS1.add(scroll5);
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
		// BDGnames={"M1","M2","M3","M4","M5","M6","M7","M8"};
		dm = new DefaultTableModel(3, 8);
		BDGtable2 = new JTable(dm);
		scroll2 = genTable(BDGtable2, BDGnames);
		panelBDG2.add(scroll2);
		height = BDGtable2.getRowHeight();
		BDGtable2.setPreferredScrollableViewportSize(new Dimension(650,
				height * 4));
		BDGtable2.setFillsViewportHeight(true);
		BDGvar2 = new JLabel("sqrt(Var)=0.00");
		panelBDG2.add(BDGvar2);
		// Create BCK tab
		JPanel panelBCK2 = new JPanel();
		dm = new DefaultTableModel(3, 7);
		BCKtable2 = new JTable(dm);
		scroll2 = genTable(BCKtable2, BCKnames);
		panelBCK2.add(scroll2);
		height = BCKtable2.getRowHeight();
		BCKtable2.setPreferredScrollableViewportSize(new Dimension(650,
				height * 4));
		BCKtable2.setFillsViewportHeight(true);
		BCKvar2 = new JLabel("sqrt(Var)=0.00");
		panelBCK2.add(BCKvar2);
		// Create DBM tab
		JPanel panelDBM2 = new JPanel();
		dm = new DefaultTableModel(3, 6);
		DBMtable2 = new JTable(dm);
		scroll2 = genTable(DBMtable2, DBMnames);
		panelDBM2.add(scroll2);
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
		scroll2 = genTable(ORtable2, ORnames);
		panelOR2.add(scroll2);
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
		scroll5 = genTable(MStable2, MSnames);
		panelMS2.add(scroll5);
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
		DDF = new JLabel("  DDF= 0.00");
		CVF = new JLabel("  CVF= 0.00");
		ZPower = new JLabel("  Power(Z test)= 0.00");
		HillisPower = new JLabel("      Power(Hillis 2011) = 0.00");
		MSvar2 = new JLabel("sqrt(Var)=0.00");
		// panelStat11.add(new JLabel("Significance Level"));
		// panelStat11.add(sigLevel);
		// panelStat11.add(effSizeLabel);
		// panelStat11.add(effSize);
		panelStat12.add(new JLabel("Sizing Results:   "));
		panelStat12.add(MSvar2);
		panelStat12.add(Delta);
		panelStat12.add(DDF);
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
		df = new JLabel("  df(Hillis 2008)=0.00");
		pVal = new JLabel("  p-value=0.00");
		tStat = new JLabel("  t-Stat=0.00");
		confInt = new JLabel("  confInt=0.00");
		sqrtTotalVar = new JLabel("  sqrt(total var)=0.00");
		panelStat2.add(StatResults);
		panelStat2.add(sqrtTotalVar);
		panelStat2.add(tStat);
		panelStat2.add(df);
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
			JComboBox<String> cb = (JComboBox<String>) evt.getSource();
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
			JComboBox<String> cb = (JComboBox<String>) evt.getSource();
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
			System.out.println("browse pressed");
			JFileChooser fc = new JFileChooser();
			// Don't get rid of this despite being unused
			int returnVal = fc.showOpenDialog((Component) e.getSource());
			File f = fc.getSelectedFile();
			if (f != null) {
				filename = f.getPath();
				pilotFile.setText(filename);
				// check the input format
				try {
					usr = new inputFile(filename);
				} catch (Exception except) {
					except.printStackTrace();
					JOptionPane.showMessageDialog(lst.getFrame(),
							"invalid input format", "Error",
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
				}
				currMod1 = 1;
				currMod2 = 2;
				currentMod.setText("Modality A = " + currMod1
						+ "     Modality B = " + currMod2);
				usr.dotheWork(1, 2);

				if (!lst.getIsApplet())
					usrFile = new dbRecord(usr);
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

	class graphButtonListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("graph button pressed");
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

	class holesButtonListner implements ActionListener {
		int holesMod1 = 1;

		public void actionPerformed(ActionEvent e) {
			System.out.println("holes button pressed");
			if (usr != null && usr.isLoaded()) {
				JComboBox<Integer> choose1 = new JComboBox<Integer>();
				for (int i = 1; i <= usr.getModality(); i++) {
					choose1.addItem(i);
				}
				choose1.setSelectedItem((Integer) currMod1);
				Object[] message = { "Which modality would you like view?\n",
						choose1 };
				JOptionPane.showMessageDialog(lst.getFrame(), message,
						"Choose Modality", JOptionPane.INFORMATION_MESSAGE,
						null);
				boolean[][] holes = usr.getDataHoles((Integer) choose1
						.getSelectedItem());
				holesMod1 = (Integer) choose1.getSelectedItem();
				final PresencePlot chart = new PresencePlot(
						"Missing Data Points: Modality " + currMod1, "Case",
						"Reader", holes);
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

	class modsButtonListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("modality selection button pressed");
			if (usr != null && usr.isLoaded()) {
				if (usr.getModality() > 2) {
					JComboBox<Integer> choose1 = new JComboBox<Integer>();
					JComboBox<Integer> choose2 = new JComboBox<Integer>();
					for (int i = 1; i <= usr.getModality(); i++) {
						choose1.addItem(i);
						choose2.addItem(i);
					}
					choose1.setSelectedItem((Integer) currMod1);
					choose2.setSelectedItem((Integer) currMod2);
					Object[] message = {
							"There are "
									+ usr.getModality()
									+ " modalities. Which would you like to use?\n",
							"Modality A: ", choose1, "Modality B: ", choose2 };
					JOptionPane.showMessageDialog(lst.getFrame(), message,
							"Choose Modalities",
							JOptionPane.INFORMATION_MESSAGE, null);
					System.out.println(choose1.getSelectedItem() + ","
							+ choose2.getSelectedItem());
					usr.dotheWork((Integer) choose1.getSelectedItem(),
							(Integer) choose2.getSelectedItem());
					currMod1 = (Integer) choose1.getSelectedItem();
					currMod2 = (Integer) choose2.getSelectedItem();
					currentMod.setText("Modality A = " + currMod1
							+ "     Modality B = " + currMod2);
					if (!lst.getIsApplet()) {
						usrFile = new dbRecord(usr);
					}

				} else {
					JOptionPane
							.showMessageDialog(
									lst.getFrame(),
									"The only available modalities have already been selected",
									"Info", JOptionPane.INFORMATION_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(lst.getFrame(),
						"Pilot study data has not yet been input.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/*
	 * in case of applet, the user click "input pilot study" button, and a text
	 * editor shows up. The user may paste the raw data into the text editor
	 */
	class InputByHandListner implements ActionListener {
		JTextArea pilot;
		JFrame pilotFrame;

		public void actionPerformed(ActionEvent e) {
			System.out.println("Input by Hand");

			pilotFrame = new JFrame("Please paste your raw data here...");

			pilotFrame.getRootPane().setWindowDecorationStyle(
					JRootPane.PLAIN_DIALOG);

			pilotFrame.getContentPane()
					.setLayout(
							new BoxLayout(pilotFrame.getContentPane(),
									BoxLayout.Y_AXIS));
			pilot = new JTextArea(30, 30);
			JScrollPane scrollPane = new JScrollPane(pilot,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			pilotFrame.getContentPane().add(scrollPane);
			JPanel btPanel = new JPanel();

			final Clipboard clipboard;

			try {
				clipboard = pilotFrame.getToolkit().getSystemClipboard();
			} catch (Exception except) {
				System.out.println("caught it ****$$%%^&&");
				JOptionPane
						.showMessageDialog(
								lst.getFrame(),
								"please copy file .java.policy to C:/Documents and Settings/{User},\n close your browser and try again\n"
										+ "For instructions, please go to \n"
										+ "https://www.member-data.com/rdc/help.aspx?topic=JavaClipboard#ptool",
								"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			JButton paste = new JButton("Paste");
			paste.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					Transferable clipData = clipboard.getContents(clipboard);
					try {
						if (clipData
								.isDataFlavorSupported(DataFlavor.stringFlavor)) {
							String s = (String) (clipData
									.getTransferData(DataFlavor.stringFlavor));
							pilot.replaceSelection(s);
						}
					} catch (Exception ufe) {
					}
				}
			});
			btPanel.add(paste);

			JButton okButton = new JButton("OK");
			okButton.addActionListener(new OKListner());
			btPanel.add(okButton);
			JButton clearButton = new JButton("Clear");
			clearButton.addActionListener(new clearListner());
			btPanel.add(clearButton);
			pilotFrame.getContentPane().add(btPanel);
			pilot.setLineWrap(true);
			pilot.setEditable(true);
			pilotFrame.pack();
			pilotFrame.setVisible(true);
		}

		/*
		 * class TextUtilities { private TextUtilities() { }
		 * 
		 * public static Action findAction(Action actions[], String key) {
		 * Hashtable commands = new Hashtable(); for (int i = 0; i <
		 * actions.length; i++) { Action action = actions[i];
		 * commands.put(action.getValue(Action.NAME), action); } return (Action)
		 * commands.get(key); } }
		 */

		class pasteListner implements ActionListener {
			public void actionPerformed(ActionEvent e) {
			}
		}

		class OKListner implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				String content = "";
				content = pilot.getText();
				try {
					usr = new inputFile(content, 1);
				} catch (Exception except) {
					except.printStackTrace();
					System.out
							.println("caught it at OKListner ****!!!!@@@@$$%%^&&");
					JOptionPane.showMessageDialog(lst.getFrame(),
							"invalid input format", "Error",
							JOptionPane.ERROR_MESSAGE);
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
							"The study is not fully crossed", "Error",
							JOptionPane.ERROR_MESSAGE);
				}

				usrFile = new dbRecord(usr);
				pilotFrame.setVisible(false);
			}
		}

		class clearListner implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				pilot.setText("");
			}
		}

	}

	/* check whether there is negative components */
	public int checkNegative() {
		hasNegative = 0;
		if (selectedInput == 1 && !lst.getIsApplet()) {
			System.out.println("test" + lst.getIsApplet() + "input"
					+ selectedInput);
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
				MS2.setUseBiasM(1);
				useBiasM = 1;
			} else if (JOptionPane.NO_OPTION == result) {
				MS.setUseBiasM(0);
				MS2.setUseBiasM(0);
				useBiasM = 0;
			}

		}
		return 1;
	}

	/*
	 * generate a table, call this function to generate the table for each set
	 * of components. This function sets the format and headers of each table
	 */
	public JScrollPane genTable(JTable table, String[] names) {
		ListModel lm = new AbstractListModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			String headers[] = rowhead;

			public int getSize() {
				return headers.length;
			}

			public Object getElementAt(int index) {
				return headers[index];
			}
		};
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int i = 0; i < names.length; i++)
			table.getColumnModel().getColumn(i).setHeaderValue(names[i]);

		JList rowHeader = new JList(lm);
		rowHeader.setFixedCellWidth(80);

		rowHeader.setFixedCellHeight(table.getRowHeight()
				+ table.getRowMargin());
		rowHeader.setCellRenderer(new RowHeaderRenderer(table));

		JScrollPane scroll = new JScrollPane(table);
		scroll.setRowHeaderView(rowHeader);
		return scroll;

	}

	/* Table cell formater renderer */
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
				value = Double.parseDouble((String) value);
			}
			return super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
		}
	}
}
