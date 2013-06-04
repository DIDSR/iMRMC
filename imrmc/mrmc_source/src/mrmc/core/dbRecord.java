/*
 * dbRecord.java
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
 * one entry in the database
 * This class includes all the information on one study.
 * it also includes all the formulas convert one type 
 * of components to the other.
 * The convertion formulas are based on 
 * Gallas BD, Bandos A, Samuelson F, Wagner RF. A Framework 
 * for random-effects ROC analsysis: Biases with the Bootstrap 
 * and other variance estimators�, Commun Stat A-Theory 38(15), 
 * 2586-2603 (2009).
 */

package mrmc.core;

import java.util.*;
import java.text.DecimalFormat;

public class dbRecord {
	String recordDesp = "";
	String recordTitle = "";
	String filename = "";
	String[] Modality = new String[2];
	String Task = "";
	double[][] BDGbias = new double[4][8];
	double[][] BDG = new double[4][8];
	double[][] BDGcoeff = new double[4][8];

	double[][] BCKbias = new double[4][7];
	double[][] BCK = new double[4][7];
	double[][] BCKcoeff = new double[4][7];

	double[][] DBMbias = new double[4][6];
	double[][] DBM = new double[4][6];
	double[][] DBMcoeff = new double[4][6];

	double[][] ORbias = new double[4][6];
	double[][] OR = new double[4][6];
	double[][] ORcoeff = new double[4][6];

	// double[][] DBMfromU = new double[4][6];
	// double[][] DBMbiasfromU = new double[4][6];
	// double[][] ORfromU = new double[4][6];
	// double[][] ORbiasfromU = new double[4][6];

	double[][] MS = new double[4][6];
	double[][] MSbias = new double[4][6];
	double[][] MScoeff = new double[4][6];

	matrix mx = new matrix();

	double[] AUC = new double[2];
	int nReader;
	int nNormal;
	int nDisease;
	int flagCompleteRecord;
	int flagCom;
	private boolean fullyCrossed;

	public String getTask() {
		return Task;
	}

	public String getModality(int i) {
		return Modality[i];
	}

	public String getFilename() {
		return filename;
	}

	public int getReader() {
		return nReader;
	}

	public int getNormal() {
		return nNormal;
	}

	public int getDisease() {
		return nDisease;
	}

	public String getRecordTitle() {
		return recordTitle;
	}

	public String getRecordDesp() {
		return recordDesp;
	}

	public boolean getFullyCrossedStatus() {
		return fullyCrossed;
	}

	public double[][] getBDG(int useBiasM) {
		if (useBiasM == 1)
			return BDGbias;
		else
			return BDG;
	}

	public double[][] getBDGcoeff() {
		return BDGcoeff;
	}

	public double[][] getMScoeff() {
		return MScoeff;
	}

	public double[][] getBCK(int useBiasM) {
		if (useBiasM == 1)
			return BCKbias;
		else
			return BCK;
	}

	public double[][] getBCKcoeff() {
		return BCKcoeff;
	}

	public double[][] getDBM(int useBias) {
		if (useBias == 1)
			return DBMbias;
		else
			return DBM;
	}

	public double[][] getDBMcoeff() {
		return DBMcoeff;
	}

	public double[][] getOR(int useBias) {
		if (useBias == 1)
			return ORbias;
		else
			return OR;
	}

	public double[][] getMS(int useBias) {
		if (useBias == 1)
			return MSbias;
		else
			return MS;
	}

	public double[][] getORcoeff() {
		return ORcoeff;
	}

	public String getAUC(int i) {
		DecimalFormat threeDec = new DecimalFormat("0.000");
		threeDec.setGroupingUsed(false);
		// STring ss=threeDec.format(inValue);

		String temp = "";
		switch (i) {
		case 0:
			temp = "AUC1=" + threeDec.format(AUC[0]) + "       ";
			break;
		case 1:
			temp = "AUC2=" + threeDec.format(AUC[1]) + "       ";
			break;
		case 3:
			temp = "AUC1=" + threeDec.format(AUC[0]) + "       AUC2="
					+ threeDec.format(AUC[1]) + "       AUC1-AUC2="
					+ threeDec.format(AUC[0] - AUC[1]) + "       ";
		}
		return temp;
	}

	public double getAUCinNumber(int i) {
		return AUC[i];
	}

	public String getParm() {
		return (Integer.toString(nReader) + " Readers,  "
				+ Integer.toString(nNormal) + " Normal cases,  "
				+ Integer.toString(nDisease) + " Disease cases.");
	}

	public int[] getParmInt() {
		int[] c = { nReader, nNormal, nDisease };
		return c;
	}

	public static double[][] getBDGTab(int i, double[][] BDGtemp,
			double[][] BDGc) {
		double[][] BDGTab1 = new double[3][8];
		BDGTab1[0] = BDGtemp[i];
		BDGTab1[1] = BDGc[i];
		BDGTab1[2] = matrix.dotProduct(BDGTab1[0], BDGTab1[1]);
		return BDGTab1;
	}

	public static double[][] getBCKTab(int i, double[][] BCKtemp,
			double[][] BCKc) {
		double[][] BCKTab1 = new double[3][7];
		BCKTab1[0] = BCKtemp[i];
		BCKTab1[1] = BCKc[i];
		BCKTab1[2] = matrix.dotProduct(BCKTab1[0], BCKTab1[1]);
		return BCKTab1;
	}

	public static double[][] getDBMTab(int i, double[][] DBMtemp,
			double[][] DBMc) {
		double[][] DBMTab1 = new double[3][6];
		DBMTab1[0] = DBMtemp[i];
		DBMTab1[1] = DBMc[i];
		DBMTab1[2] = matrix.dotProduct(DBMTab1[0], DBMTab1[1]);
		return DBMTab1;
	}

	public static double[][] getORTab(int i, double[][] ORtemp, double[][] ORc) {
		double[][] ORTab1 = new double[3][6];
		ORTab1[0] = ORtemp[i];
		ORTab1[1] = ORc[i];
		ORTab1[2] = matrix.dotProduct(ORTab1[0], ORTab1[1]);
		return ORTab1;
	}

	public static double[][] getMSTab(int i, double[][] MStemp, double[][] MSc) {
		double[][] MSTab1 = new double[3][6];
		MSTab1[0] = MStemp[i];
		MSTab1[1] = MSc[i];
		MSTab1[2] = matrix.dotProduct(MSTab1[0], MSTab1[1]);
		return MSTab1;
	}

	/* constructor 1, create a record from database file */
	public dbRecord(String fname, String[] str, ArrayList<String> desp,
			String AUCstr) {
		int i, j;
		flagCompleteRecord = 1;
		recordTitle = desp.get(0).substring(2);
		filename = fname;
		fullyCrossed = true;

		for (i = 0; i < 7; i++) {
			String tempStr = desp.get(i);
			if (tempStr.startsWith("*  Modal")) {
				String[] tempStr2 = tempStr.split(",");
				Modality[0] = tempStr2[1];
				Modality[1] = tempStr2[2];
			}
			if (tempStr.startsWith("*  Task") || tempStr.startsWith("*  TASK")) {
				String[] tempStr2 = tempStr.split(",");
				Task = tempStr2[1];
			}
		}

		String[] tempAUC = AUCstr.split(",");
		AUC[0] = Double.valueOf(tempAUC[1]);
		AUC[1] = Double.valueOf(tempAUC[2]);

		nReader = Integer.valueOf(tempAUC[3]);
		nNormal = Integer.valueOf(tempAUC[4]);
		nDisease = Integer.valueOf(tempAUC[5]);

		for (i = 0; i < desp.size(); i++) {
			recordDesp = recordDesp + desp.get(i) + "\n";
		}

		for (i = 0; i < str.length / 2; i++) {
			String[] temp = str[i].split(",");
			for (j = 0; j < 8; j++) {
				BDG[i][j] = Double.valueOf(temp[j]);
			}
		}
		for (i = str.length / 2; i < str.length; i++) {
			String[] temp = str[i].split(",");
			for (j = 0; j < 8; j++) {
				BDGbias[i - str.length / 2][j] = Double.valueOf(temp[j]);
			}
		}

		BDGcoeff = genBDGCoeff(nReader, nNormal, nDisease);
		DBMcoeff = genDBMCoeff(nReader, nNormal, nDisease);
		BCKcoeff = genBCKCoeff(nReader, nNormal, nDisease);
		ORcoeff = genORCoeff(nReader, nNormal, nDisease);
		MScoeff = genMSCoeff(nReader, nNormal, nDisease);

		BCK = BDG2BCK(BDG);
		// DBM = BDG2DBM(BDG, nReader, nNormal, nDisease);
		// OR = BDG2OR(BDG, nReader, nNormal, nDisease);
		DBM = BCK2DBM(BCK, nReader, nNormal, nDisease);
		OR = DBM2OR(0, DBM, nReader, nNormal, nDisease);
		MS = DBM2MS(DBM, nReader, nNormal, nDisease);

		BCKbias = BDG2BCK(BDGbias);
		// DBMbias = BDG2DBM(BDGbias, nReader, nNormal, nDisease);
		// ORbias = BDG2OR(BDGbias, nReader, nNormal, nDisease);
		DBMbias = BCK2DBM(BCKbias, nReader, nNormal, nDisease);
		ORbias = DBM2OR(0, DBMbias, nReader, nNormal, nDisease);
		MSbias = DBM2MS(DBMbias, nReader, nNormal, nDisease);

	}

	public static double[][] DBM2MS(double[][] DBM, int N2, int N0, int N1) {
		double[][] c = new double[4][6];
		double[][] BAlpha = new double[][] {
				{ 2 * (N0 + N1), 0, 2, (N0 + N1), 0, 1 },
				{ 0, 2 * N2, 2, 0, N2, 1 }, { 0, 0, 0, (N0 + N1), 0, 1 },
				{ 0, 0, 0, 0, N2, 1 }, { 0, 0, 2, 0, 0, 1 },
				{ 0, 0, 0, 0, 0, 1 } };
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 6; j++)
				c[i][j] = 0;
		c = matrix.matrixTranspose(matrix.multiply(BAlpha,
				matrix.matrixTranspose(DBM)));
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 6; j++)
				c[i][j] = c[i][j] / 2.0;

		return c;

	}

	public static double[][] BCK2DBM(double[][] BCK, int N2, int N0, int N1) {
		double[] c = new double[7];
		double[][] tmp = new double[4][7];
		double[][] tmp1 = new double[4][3];
		double[][] results = new double[4][6];

		c[0] = 1.0 / N0;
		c[1] = 1.0 / N1;
		c[2] = 1.0 / (N0 * N1);
		c[3] = 1.0 / N2;
		c[4] = 1.0 / (N0 * N2);
		c[5] = 1.0 / (N1 * N2);
		c[6] = 1.0 / (N0 * N1 * N2);

		for (int i = 0; i < 4; i++)
			tmp[i] = matrix.dotProduct(BCK[i], c);

		double[][] alpha = new double[][] { { 0, 1, 0 }, { 0, 1, 0 },
				{ 0, 1, 0 }, { N2, 0, 0 }, { 0, 0, N2 }, { 0, 0, N2 },
				{ 0, 0, N2 } };

		tmp1 = matrix.multiply(tmp, alpha);

		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 6; j++)
				results[i][j] = 0;
		/*
		 * results[0][0]=tmp1[0][0]; results[0][1]=tmp1[0][1];
		 * results[0][2]=tmp1[0][2]; results[1][0]=tmp1[1][0];
		 * results[1][1]=tmp1[1][1]; results[1][2]=tmp1[1][2];
		 * results[3][0]=(tmp1[0][0]+tmp1[1][0])/2.0;
		 * results[3][1]=(tmp1[0][1]+tmp1[1][1])/2.0;
		 * results[3][2]=(tmp1[0][2]+tmp1[1][2])/2.0;
		 * results[3][3]=(tmp1[0][0]+tmp1[1][0])/2.0-tmp1[2][0];
		 * results[3][4]=(tmp1[0][1]+tmp1[1][1])/2.0-tmp1[2][1];
		 * results[3][5]=(tmp1[0][2]+tmp1[1][2])/2.0-tmp1[2][2];
		 */
		results[0][0] = tmp1[0][0];
		results[0][1] = tmp1[0][1] * (N0 + N1);
		results[0][2] = tmp1[0][2] * (N0 + N1);
		results[1][0] = tmp1[1][0];
		results[1][1] = tmp1[1][1] * (N0 + N1);
		results[1][2] = tmp1[1][2] * (N0 + N1);
		// results[3][0]=(tmp1[0][0]+tmp1[1][0])/2.0;
		// results[3][1]=(tmp1[0][1]+tmp1[1][1])/2.0*(N0+N1);
		// results[3][2]=(tmp1[0][2]+tmp1[1][2])/2.0*(N0+N1);
		results[3][0] = tmp1[2][0];
		results[3][1] = tmp1[2][1] * (N0 + N1);
		results[3][2] = tmp1[2][2] * (N0 + N1);

		results[3][3] = (tmp1[0][0] + tmp1[1][0]) / 2.0 - tmp1[2][0];
		results[3][4] = ((tmp1[0][1] + tmp1[1][1]) / 2.0 - tmp1[2][1])
				* (N0 + N1);
		results[3][5] = ((tmp1[0][2] + tmp1[1][2]) / 2.0 - tmp1[2][2])
				* (N0 + N1);

		// System.out.println("tmp1[2][0]=" + tmp1[2][0] +" tmp1[2][1]=" +
		// tmp1[2][1] +" tmp1[2][2]=" +tmp1[2][2]);

		return results;
	}

	/* constructor 3: generate a record from manually input information. */
	public dbRecord(double[] data, int flag, int Reader, int Normal,
			int Disease, double[] auc) {
		AUC = auc;
		nReader = Reader;
		nNormal = Normal;
		nDisease = Disease;
		AUC = auc;
		flagCompleteRecord = 0;
		flagCom = flag;
		fullyCrossed = true;
		switch (flag) {
		case 0: // BDG
			for (int i = 0; i < 4; i++) {
				BDG[i] = data;
				BDGbias[i] = data;
			}
			BDGcoeff = genBDGCoeff(nReader, nNormal, nDisease);
			BCKcoeff = genBCKCoeff(nReader, nNormal, nDisease);
			DBMcoeff = genDBMCoeff(nReader, nNormal, nDisease);
			ORcoeff = genORCoeff(nReader, nNormal, nDisease);
			MScoeff = genMSCoeff(nReader, nNormal, nDisease);
			BCK = BDG2BCK(BDG);
			// DBM = BDG2DBM(BDG, nReader, nNormal, nDisease);
			// OR = BDG2OR(BDG, nReader, nNormal, nDisease);
			DBM = BCK2DBM(BCK, nReader, nNormal, nDisease);
			OR = DBM2OR(0, DBM, nReader, nNormal, nDisease);

			BCKbias = BCK;
			DBMbias = DBM;
			ORbias = OR;
			BDG[3] = BDG[0];
			BCK[3] = BCK[0];
			DBM[3][0] = 0;
			DBM[3][1] = 0;
			DBM[3][2] = 0;
			DBM[3][3] = DBM[0][0];
			DBM[3][4] = DBM[0][1];
			DBM[3][5] = DBM[0][2];
			MS = DBM2MS(DBM, nReader, nNormal, nDisease);
			OR = DBM2OR(0, DBM, nReader, nNormal, nDisease);
			break;
		case 1: // BCK
			for (int i = 0; i < 4; i++) {
				BCK[i] = data;
				BCKbias[i] = data;
			}
			BCKcoeff = genBCKCoeff(nReader, nNormal, nDisease);
			DBMcoeff = genDBMCoeff(nReader, nNormal, nDisease);
			ORcoeff = genORCoeff(nReader, nNormal, nDisease);
			MScoeff = genMSCoeff(nReader, nNormal, nDisease);
			DBM = BCK2DBM(BCK, nReader, nNormal, nDisease);
			OR = DBM2OR(0, DBM, nReader, nNormal, nDisease);
			BCKbias = BCK;
			DBMbias = DBM;
			ORbias = OR;
			BDG[3] = BDG[0];
			BCK[3] = BCK[0];
			DBM[3][0] = 0;
			DBM[3][1] = 0;
			DBM[3][2] = 0;
			DBM[3][3] = DBM[0][0];
			DBM[3][4] = DBM[0][1];
			DBM[3][5] = DBM[0][2];
			OR = DBM2OR(0, DBM, nReader, nNormal, nDisease);
			MS = DBM2MS(DBM, nReader, nNormal, nDisease);
			break;
		case 2: // DBM
			for (int i = 0; i < 4; i++) {
				DBM[i] = data;
				DBMbias[i] = data;
			}
			OR = DBM2OR(0, DBM, nReader, nNormal, nDisease);
			MS = DBM2MS(DBM, nReader, nNormal, nDisease);
			ORbias = OR;
			DBMcoeff = genDBMCoeff(nReader, nNormal, nDisease);
			ORcoeff = genORCoeff(nReader, nNormal, nDisease);
			MScoeff = genMSCoeff(nReader, nNormal, nDisease);
			break;
		case 3: // OR
			for (int i = 0; i < 4; i++) {
				OR[i] = data;
				ORbias[i] = data;
			}
			DBM = DBM2OR(1, OR, nReader, nNormal, nDisease);
			MS = DBM2MS(DBM, nReader, nNormal, nDisease);
			DBMbias = DBM;
			DBMcoeff = genDBMCoeff(nReader, nNormal, nDisease);
			ORcoeff = genORCoeff(nReader, nNormal, nDisease);
			MScoeff = genMSCoeff(nReader, nNormal, nDisease);
			break;
		default:
			break;

		}

	}

	/* Constructor 2: generate a DB record from raw datafile */
	public dbRecord(inputFile input, int currMod1, int currMod2) {

		recordTitle = input.getTitle();
		recordDesp = input.getDesc();
		filename = input.getFilename();
		nReader = input.getReader();
		nNormal = input.getNormal();
		nDisease = input.getDisease();
		flagCompleteRecord = 1;
		AUC = input.getaucMod();
		fullyCrossed = input.getFullyCrossedStatus();

		String[] temp = recordDesp.split("\n");
		// System.out.println("temp" + "0" + "  =  " + temp[0]);
		int i = 0;
		// for(int i =0;i<7; i++)

		Modality[0] = "Mod1";
		Modality[1] = "Mod2";
		Task = "task unspecified";
		while (temp[i].equals("BEGIN DATA")) {
			String tempStr = temp[i];
			if (tempStr.startsWith("*  Modal")) {
				String[] tempStr2 = tempStr.split(",");
				Modality[0] = tempStr2[1];
				Modality[1] = tempStr2[2];
			}
			if (tempStr.startsWith("*  Task") || tempStr.startsWith("*  TASK")) {
				String[] tempStr2 = tempStr.split(",");
				Task = tempStr2[1];
			}
			i++;
		}

		if (fullyCrossed) {
			BDGcoeff = genBDGCoeff(nReader, nNormal, nDisease);
		} else {
			BDGcoeff = genBDGCoeff(nReader, nNormal, nDisease,
					input.getStudyDesignSeparated(currMod1),
					input.getStudyDesignSeparated(currMod2));
		}

		DBMcoeff = genDBMCoeff(nReader, nNormal, nDisease);
		BCKcoeff = genBCKCoeff(nReader, nNormal, nDisease);
		ORcoeff = genORCoeff(nReader, nNormal, nDisease);
		MScoeff = genMSCoeff(nReader, nNormal, nDisease);

		BDG = input.getBDG();
		BDGbias = input.getBDGbias();
		BCK = BDG2BCK(BDG);
		// DBM = BDG2DBM(BDG, nReader, nNormal, nDisease);
		// OR = BDG2OR(BDG, nReader, nNormal, nDisease);
		DBM = BCK2DBM(BCK, nReader, nNormal, nDisease);
		OR = DBM2OR(0, DBM, nReader, nNormal, nDisease);
		MS = DBM2MS(DBM, nReader, nNormal, nDisease);
		BCKbias = BDG2BCK(BDGbias);
		// DBMbias = BDG2DBM(BDGbias, nReader, nNormal, nDisease);
		// ORbias = BDG2OR(BDGbias, nReader, nNormal, nDisease);
		DBMbias = BCK2DBM(BCKbias, nReader, nNormal, nDisease);
		ORbias = DBM2OR(0, DBMbias, nReader, nNormal, nDisease);
		MSbias = DBM2MS(DBMbias, nReader, nNormal, nDisease);
	}

	// To determine BDG coefficient for non-fully-crossed data
	public static double[][] genBDGCoeff(int NR, int N0, int N1,
			int[][][] mod0design, int[][][] mod1design) {
		double[][] c = new double[4][8];
		double nStarM0 = 0;
		double nStarM1 = 0;
		double coeffM1 = 0;
		double coeffM2 = 0;
		double coeffM3 = 0;
		double coeffM4 = 0;
		double coeffM5 = 0;
		double coeffM6 = 0;
		double coeffM7 = 0;
		double coeffM8 = 0;
		for (int r = 0; r < NR; r++) {
			for (int i = 0; i < N0; i++) {
				for (int j = 0; j < N1; j++) {
					nStarM0 += (double) mod0design[r][i][j];
					nStarM1 += (double) mod1design[r][i][j];

					coeffM1 += ((double) mod0design[r][i][j] * (double) mod1design[r][i][j]);
				}
			}
		}

		// This is correct!
		for (int j = 0; j < N1; j++) {
			for (int r = 0; r < NR; r++) {
				double innerSum = 0;
				for (int ipr = 0; ipr < N0; ipr++) {
					innerSum += (double) mod1design[r][ipr][j];
				}
				for (int i = 0; i < N0; i++) {
					coeffM2 += (double) mod0design[r][i][j]
							* (innerSum - (double) mod1design[r][i][j]);

				}

			}
		}

		// This is correct!
		for (int i = 0; i < N0; i++) {
			for (int r = 0; r < NR; r++) {
				double innerSum = 0;
				for (int jpr = 0; jpr < N1; jpr++) {
					innerSum += (double) mod1design[r][i][jpr];
				}
				for (int j = 0; j < N1; j++) {
					coeffM3 += (double) mod0design[r][i][j]
							* (innerSum - (double) mod1design[r][i][j]);
				}
			}
		}

		// TODO these
		for (int r = 0; r < NR; r++) {
			for (int i = 0; i < N0; i++) {
				for (int j = 0; j < N1; j++) {
					double innerSum = 0;
					for (int ipr = 0; ipr < N0; ipr++) {
						double innerMostSum = 0;
						for (int jpr = 0; jpr < N1; jpr++) {
							innerMostSum += (double) mod1design[r][ipr][jpr];
						}
						innerSum += innerMostSum
								- (double) mod1design[r][ipr][j];
					}

					coeffM4 += (double) mod0design[r][i][j]
							* (innerSum - (double) mod1design[r][i][j]);
				}
			}
		}

		// for (int r = 0; r < NR; r++) {
		//
		// for (int i = 0; i < N0; i++) {
		// for (int j = 0; j < N1; j++) {
		// double totalSum = 0;
		//
		// for (int ipr = 0; ipr < N0; ipr++) {
		// if (ipr != i) {
		// for (int jpr = 0; jpr < N1; jpr++) {
		// if (jpr != j) {
		// totalSum += (double) mod1design[r][ipr][jpr];
		// }
		// }
		// }
		// }
		//
		// coeffM4 += mod0design[r][i][j] * totalSum;
		// }
		// }
		//
		// }

		// This is correct!
		for (int i = 0; i < N0; i++) {
			for (int j = 0; j < N1; j++) {
				double innerSum = 0;
				for (int rpr = 0; rpr < NR; rpr++) {
					innerSum += (double) mod1design[rpr][i][j];
				}
				for (int r = 0; r < NR; r++) {
					coeffM5 += (double) mod0design[r][i][j]
							* (innerSum - (double) mod1design[r][i][j]);
				}
			}
		}

		// M8
		double totalSum = 0;
		double isum = 0;
		for (int ipr = 0; ipr < N0; ipr++) {
			double jsum = 0;
			for (int jpr = 0; jpr < N1; jpr++) {
				double rsum = 0;
				for (int rpr = 0; rpr < NR; rpr++) {
					rsum += (double) mod1design[rpr][ipr][jpr];
				}
				for (int r = 0; r < NR; r++) {
					jsum += (rsum - (double) mod1design[r][ipr][jpr]);
				}
			}
			for (int j = 0; j < N1; j++) {
				double rToSubtract = 0;
				for (int r = 0; r < NR; r++) {
					rToSubtract += (double) mod1design[r][ipr][j];
				}
				isum += (jsum - rToSubtract);
			}
		}
		for (int i = 0; i < N0; i++) {
			double innerToSubtract = 0;
			for (int j = 0; j < N1; j++) {
				for (int r = 0; r < NR; r++) {
					innerToSubtract += (double) mod1design[r][i][j];
				}
			}

			totalSum += (isum - innerToSubtract);
		}
		for (int i = 0; i < N0; i++) {
			for (int j = 0; j < N1; j++) {
				for (int r = 0; r < NR; r++) {
					coeffM8 += ((double) mod0design[r][i][j] * totalSum);
				}
			}
		}

		coeffM1 = coeffM1 / (nStarM0 * nStarM1);
		coeffM2 = coeffM2 / (nStarM0 * nStarM1);
		coeffM3 = coeffM3 / (nStarM0 * nStarM1);
		coeffM4 = coeffM4 / (nStarM0 * nStarM1);
		coeffM5 = coeffM5 / (nStarM0 * nStarM1);
		coeffM6 = coeffM6 / (nStarM0 * nStarM1);
		coeffM7 = coeffM7 / (nStarM0 * nStarM1);
		coeffM8 = coeffM8 / (nStarM0 * nStarM1);
		coeffM8 -= 1.0;

		c[0][0] = coeffM1;
		c[0][1] = coeffM2;
		c[0][2] = coeffM3;
		c[0][3] = coeffM4;
		c[0][4] = coeffM5;
		c[0][5] = coeffM6;
		c[0][6] = coeffM7;
		c[0][7] = coeffM8;
		c[1] = c[0];
		c[2] = c[0];
		c[3] = c[0];
		return c;
	}

	// To determine BDG coefficient for fully-crossed data
	public static double[][] genBDGCoeff(int NR, int N0, int N1) {
		double[][] c = new double[4][8];
		c[0][0] = 1.0 / (NR * N0 * N1);
		c[0][1] = c[0][0] * (N0 - 1.0);
		c[0][2] = c[0][0] * (N1 - 1.0);
		c[0][3] = c[0][0] * (N0 - 1.0) * (N1 - 1.0);
		c[0][4] = c[0][0] * (NR - 1.0);
		c[0][5] = c[0][0] * (N0 - 1.0) * (NR - 1.0);
		c[0][6] = c[0][0] * (N1 - 1.0) * (NR - 1.0);
		c[0][7] = c[0][0] * (N1 - 1.0) * (N0 - 1.0) * (NR - 1.0);
		c[0][7] = c[0][7] - 1;
		c[1] = c[0];
		c[2] = c[0];
		c[3] = c[0];

		return c;
	}

	// TODO complete this
	public static double[][] genBCKCoeff(int NR, int N0, int N1, int m) {
		double[][] c = new double[4][7];
		int[][] c2ca = new int[][] { { 1, 0, 1, 0, 1, 0, 1, 0 },
				{ 1, 1, 0, 0, 1, 1, 0, 0 }, { 1, 0, 0, 0, 1, 0, 0, 0 },
				{ 1, 1, 1, 1, 0, 0, 0, 0 }, { 1, 0, 1, 0, 0, 0, 0, 0 },
				{ 1, 1, 0, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0, 0, 0 } };

		return c;
	}

	public static double[][] genBCKCoeff(int NR, int N0, int N1) {
		double[][] c = new double[4][7];
		c[0][0] = 1.0 / N0;
		c[0][1] = 1.0 / N1;
		c[0][2] = 1.0 / (N0 * N1);
		c[0][3] = 1.0 / NR;
		c[0][4] = 1.0 / (N0 * NR);
		c[0][5] = 1.0 / (N1 * NR);
		c[0][6] = 1.0 / (N1 * N0 * NR);

		c[1] = c[0];
		c[2] = c[0];
		c[3] = c[0];
		return c;
	}

	public static double[][] genDBMCoeff(int NR, int N0, int N1) {
		double[][] c = new double[4][6];
		/*
		 * c[0][0] = 1.0/N2; c[0][1] = 1.0; c[0][2]= 1.0/N2; c[0][3] = 1.0/N2;
		 * c[0][4]= 1.0; c[0][5]= 1.0/N2;
		 */
		/* per unit */
		c[0][0] = 1.0 / NR;
		c[0][1] = 1.0 / (N0 + N1);
		c[0][2] = 1.0 / NR / (N0 + N1);
		c[0][3] = 1.0 / NR;
		c[0][4] = 1.0 / (N0 + N1);
		c[0][5] = 1.0 / NR / (N0 + N1);

		c[1] = c[0];
		c[2] = matrix.scaleVector(c[0], 0);
		c[3] = matrix.scaleVector(c[0], 2);
		c[3][0] = 0;
		c[3][1] = 0;
		c[3][2] = 0;

		c[0][3] = 0;
		c[0][4] = 0;
		c[0][5] = 0;
		c[1][3] = 0;
		c[1][4] = 0;
		c[1][5] = 0;
		return c;
	}

	public static double[][] genMSCoeff(int NR, int N0, int N1) {
		double[][] c = new double[4][6];
		double tmp = 1.0 / (NR * (N0 + N1));
		c[0][0] = tmp;
		c[0][1] = tmp;
		c[0][2] = 0;
		c[0][3] = 0;
		c[0][4] = -tmp;
		c[0][5] = 0;
		c[1] = c[0];
		c[2] = matrix.scaleVector(c[0], 0);
		c[3][0] = 0;
		c[3][1] = 0;
		c[3][2] = tmp * 2.0;
		c[3][3] = tmp * 2.0;
		c[3][4] = 0;
		c[3][5] = -tmp * 2.0;
		return c;
	}

	public static double[][] genORCoeff(int NR, int N0, int N1) {
		double[][] c = new double[4][6];
		c[0][0] = 1.0 / NR;
		c[0][1] = 0;
		c[0][2] = 0;
		c[0][3] = 1.0 / NR * (NR - 1);
		c[0][4] = 0;
		c[0][5] = 1.0 / NR;
		c[1] = c[0];
		c[2] = matrix.scaleVector(c[0], 0);
		c[3][0] = 0;
		c[3][1] = 2.0 / NR;
		c[3][2] = -2.0 / NR;
		c[3][3] = 2.0 / NR * (NR - 1);
		c[3][4] = -2.0 / NR * (NR - 1);
		c[3][5] = 2.0 / NR;

		return c;
	}

	public static double[][] BDG2BCK(double[][] BDG) {
		double[][] c = new double[4][7];
		double[][] BAlpha = new double[][] { { 0, 0, 0, 0, 0, 0, 1, -1 },
				{ 0, 0, 0, 0, 0, 1, 0, -1 }, { 0, 0, 0, 0, 1, -1, -1, 1 },
				{ 0, 0, 0, 1, 0, 0, 0, -1 }, { 0, 0, 1, -1, 0, 0, -1, 1 },
				{ 0, 1, 0, -1, 0, -1, 0, 1 }, { 1, -1, -1, 1, -1, 1, 1, -1 } };
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 7; j++)
				c[i][j] = 0;
		c = matrix.matrixTranspose(matrix.multiply(BAlpha,
				matrix.matrixTranspose(BDG)));

		return c;

	}

	public double[][] computeTempDBM(double[][] BDG, int N2, int N0, int N1) {
		double[][] B25 = new double[][] {
				{ 1.0, 0, 0, 0 },
				{ 1.0 / N0, (N0 - 1.0) / N0, 0, 0 },
				{ 1.0 / N1, 0, (N1 - 1.0) / N1, 0 },
				{ 1.0 / (N1 * N0), (N0 - 1.0) / (N1 * N0),
						(N1 - 1.0) / (N1 * N0),
						(N1 - 1.0) * (N0 - 1.0) / (N1 * N0) } };

		double[][] B = new double[8][8];
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++) {
				B[i][j] = B25[i][j];
				B[i][j + 4] = 0.0;
				B[i + 4][j] = B25[i][j] / N2;
				B[i + 4][j + 4] = B25[i][j] * (N2 - 1.0) / N2;
			}
		double[][] BTheta = new double[][] {
				{ 1.0 / (N0 + N1), 0, -1.0 / (N0 + N1) },
				{ 0, 1.0 / (N0 + N1) / N2, -1.0 / (N0 + N1) / N2 },
				{ 0, 0, 1.0 / (N0 + N1) } };

		double b1 = (N0 + N1) * N2 / (N2 - 1.0);
		double b2 = (N0 + N1 - 1.0) * N1 * N2 / (N1 - 1.0) / (N1 - 1.0);
		double b3 = (N0 + N1 - 1.0) * N0 * N2 / (N0 - 1.0) / (N0 - 1.0);
		double[][] Bms = new double[][] {
				{ 0, 0, 0, b1, 0, 0, 0, -b1 },
				{ 0, 0, 0, 0, 0, b2, b3, -b2 - b3 },
				{ 0, b2 / (N2 - 1.0), b3 / (N2 - 1.0), -(b2 + b3) / (N2 - 1.0),
						0, -b2 / (N2 - 1.0), -b3 / (N2 - 1.0),
						(b2 + b3) / (N2 - 1.0) } };
		double[][] c = new double[4][3];
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 3; j++)
				c[i][j] = 0;
		c = matrix.matrixTranspose(matrix.multiply(
				BTheta,
				matrix.multiply(Bms,
						matrix.multiply(B, matrix.matrixTranspose(BDG)))));

		return c;
	}

	public double[][] BDG2DBM(double[][] BDG, int N2, int N0, int N1) {
		double[][] c = new double[4][6];
		double[][] tempDBM = computeTempDBM(BDG, N2, N0, N1);
		// compute DBM components
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 6; j++)
				c[i][j] = 0;
		/*
		 * c[0][0]=tempDBM[0][0]; c[0][1]=tempDBM[0][1]; c[0][2]=tempDBM[0][2];
		 * c[1][0]=tempDBM[1][0]; c[1][1]=tempDBM[1][1]; c[1][2]=tempDBM[1][2];
		 * c[3][0]=(tempDBM[0][0]+tempDBM[1][0])/2.0;
		 * c[3][1]=(tempDBM[0][1]+tempDBM[1][1])/2.0;
		 * c[3][2]=(tempDBM[0][2]+tempDBM[1][2])/2.0;
		 * c[3][3]=(tempDBM[0][0]+tempDBM[1][0])/2.0-tempDBM[2][0];
		 * c[3][4]=(tempDBM[0][1]+tempDBM[1][1])/2.0-tempDBM[2][1];
		 * c[3][5]=(tempDBM[0][2]+tempDBM[1][2])/2.0-tempDBM[2][2];
		 */

		c[0][0] = tempDBM[0][0];
		c[0][1] = tempDBM[0][1] * (N0 + N1);
		c[0][2] = tempDBM[0][2] * (N0 + N1);
		c[1][0] = tempDBM[1][0];
		c[1][1] = tempDBM[1][1] * (N0 + N1);
		c[1][2] = tempDBM[1][2] * (N0 + N1);
		// *************************************************
		// old way of getting var_r(diff), var_c(diff) and var_rc(diff)
		// c[3][0]=(tempDBM[0][0]+tempDBM[1][0])/2.0;
		// c[3][1]=(tempDBM[0][1]+tempDBM[1][1])/2.0*(N0+N1);
		// c[3][2]=(tempDBM[0][2]+tempDBM[1][2])/2.0*(N0+N1);
		// new way, see Brandon's email on 12/12/11
		c[3][0] = tempDBM[2][0];
		c[3][1] = tempDBM[2][1] * (N0 + N1);
		c[3][2] = tempDBM[2][2] * (N0 + N1);
		// *************************************************
		c[3][3] = (tempDBM[0][0] + tempDBM[1][0]) / 2.0 - tempDBM[2][0];
		c[3][4] = ((tempDBM[0][1] + tempDBM[1][1]) / 2.0 - tempDBM[2][1])
				* (N0 + N1);
		c[3][5] = ((tempDBM[0][2] + tempDBM[1][2]) / 2.0 - tempDBM[2][2])
				* (N0 + N1);

		return c;
	}

	public double[][] BDG2OR(double[][] BDG, int N2, int N0, int N1) {
		double[][] c = new double[4][6];
		double[][] tempDBM = computeTempDBM(BDG, N2, N0, N1);
		double[][] ThetaOR = new double[][] { { 1, 0, 0 }, { 0, 1, 0 },
				{ 0, 1, 1 } };
		double[][] tempOR = matrix.matrixTranspose(matrix.multiply(ThetaOR,
				matrix.matrixTranspose(tempDBM)));
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 6; j++)
				c[i][j] = 0;
		c[0][0] = tempOR[0][0];
		c[0][2] = tempOR[0][2];
		c[0][3] = tempOR[0][1];
		c[0][4] = tempOR[0][1];
		c[0][5] = tempOR[0][2];

		c[1][0] = tempOR[1][0];
		c[1][2] = tempOR[1][2];
		c[1][3] = tempOR[1][1];
		c[1][4] = tempOR[1][1];
		c[1][5] = tempOR[1][2];

		c[3][0] = tempOR[2][0];
		c[3][1] = (tempOR[0][0] + tempOR[1][0]) / 2.0 - tempOR[2][0];
		c[3][2] = tempOR[2][2];
		c[3][3] = (tempOR[0][1] + tempOR[1][1]) / 2.0;
		c[3][4] = tempOR[2][1];
		c[3][5] = (tempOR[0][2] + tempOR[1][2]) / 2.0;
		return c;
	}

	public static double[][] DBM2OR(int index, double[][] in, int N2, int N0,
			int N1) {
		double[][] out = new double[4][6];
		double[][] dbm = new double[4][6];
		double[][] orVar = new double[4][6];
		if (index == 0) // the input is DBM;
		{
			dbm = in;
			for (int i = 0; i < 4; i++) {
				/*
				 * orVar[i][0]=dbm[i][0]; orVar[i][1]=dbm[i][3];
				 * orVar[i][2]=dbm[i][1]+dbm[i][2];
				 * orVar[i][3]=dbm[i][1]+dbm[i][4]; orVar[i][4]=dbm[i][1];
				 * orVar[i][5]=dbm[i][1]+dbm[i][2]+dbm[i][4]+dbm[i][5];
				 */

				orVar[i][0] = dbm[i][0];
				orVar[i][1] = dbm[i][3];
				orVar[i][2] = (dbm[i][1] + dbm[i][2]) / (N0 + N1);
				orVar[i][3] = (dbm[i][1] + dbm[i][4]) / (N0 + N1);
				orVar[i][4] = dbm[i][1] / (N0 + N1);
				orVar[i][5] = (dbm[i][1] + dbm[i][2] + dbm[i][4] + dbm[i][5])
						/ (N0 + N1);

			}
			out = orVar;
		} else // the input is OR
		{
			orVar = in;
			for (int i = 0; i < 4; i++) {
				/*
				 * dbm[i][0]=orVar[i][0]; dbm[i][1]=orVar[i][4];
				 * dbm[i][2]=orVar[i][2]-orVar[i][4]; dbm[i][3]=orVar[i][1];
				 * dbm[i][4]=orVar[i][3]-orVar[i][4];
				 * dbm[i][5]=orVar[i][5]-orVar[i][2]-orVar[i][3]+orVar[i][4];
				 */

				dbm[i][0] = orVar[i][0];
				dbm[i][1] = orVar[i][4] * (N0 + N1);
				dbm[i][2] = (orVar[i][2] - orVar[i][4]) * (N0 + N1);
				dbm[i][3] = orVar[i][1];
				dbm[i][4] = (orVar[i][3] - orVar[i][4]) * (N0 + N1);
				dbm[i][5] = (orVar[i][5] - orVar[i][2] - orVar[i][3] + orVar[i][4])
						* (N0 + N1);
			}
			out = dbm;

		}
		return out;

	}

	public double[][] BCKresize(double[][] bck, int newR, int newN, int newD) {
		return bck;
	}

	public double[][] DBMresize(double[][] dbm, int newR, int newN, int newD) {
		double[][] DBMnew = new double[4][6];
		// double lamda =
		// Double.valueOf(nNormal+nDisease)/Double.valueOf(newN+newD);
		double lamda = 1.0 / Double.valueOf(newN + newD);
		for (int i = 0; i < 4; i++) {
			DBMnew[i][0] = dbm[i][0];
			DBMnew[i][1] = dbm[i][1] * lamda;
			DBMnew[i][2] = dbm[i][2] * lamda;
			DBMnew[i][3] = dbm[i][3];
			DBMnew[i][4] = dbm[i][4] * lamda;
			DBMnew[i][5] = dbm[i][5] * lamda;
		}

		return DBMnew;
	}
}
