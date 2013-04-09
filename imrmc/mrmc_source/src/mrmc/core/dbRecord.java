/*
 * dbRecord.java
 * 
 * v1.0
 * 
 * @Author Xin He, Phd, Brandon D. Gallas, PhD, Rohan Pathare
 * 
 * Copyright 2013 Food & Drug Administration, Division of Image Analysis & Mathematics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

	public double[][] getBDGTab(int i, double[][] BDGtemp, double[][] BDGc) {
		double[][] BDGTab1 = new double[3][8];
		BDGTab1[0] = BDGtemp[i];
		BDGTab1[1] = BDGc[i];
		BDGTab1[2] = matrix.dotProduct(BDGTab1[0], BDGTab1[1]);
		return BDGTab1;
	}

	public double[][] getBCKTab(int i, double[][] BCKtemp, double[][] BCKc) {
		double[][] BCKTab1 = new double[3][7];
		BCKTab1[0] = BCKtemp[i];
		BCKTab1[1] = BCKc[i];
		BCKTab1[2] = matrix.dotProduct(BCKTab1[0], BCKTab1[1]);
		return BCKTab1;
	}

	public double[][] getDBMTab(int i, double[][] DBMtemp, double[][] DBMc) {
		double[][] DBMTab1 = new double[3][6];
		DBMTab1[0] = DBMtemp[i];
		DBMTab1[1] = DBMc[i];
		DBMTab1[2] = matrix.dotProduct(DBMTab1[0], DBMTab1[1]);
		return DBMTab1;
	}

	public double[][] getORTab(int i, double[][] ORtemp, double[][] ORc) {
		double[][] ORTab1 = new double[3][6];
		ORTab1[0] = ORtemp[i];
		ORTab1[1] = ORc[i];
		ORTab1[2] = matrix.dotProduct(ORTab1[0], ORTab1[1]);
		return ORTab1;
	}

	public double[][] getMSTab(int i, double[][] MStemp, double[][] MSc) {
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

	public double[][] DBM2MS(double[][] DBM, int N2, int N0, int N1) {
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

	public double[][] BCK2DBM(double[][] BCK, int N2, int N0, int N1) {
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

	public dbRecord() {
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
	public dbRecord(inputFile input) {

		recordTitle = input.getTitle();
		recordDesp = input.getDesc();
		filename = input.getFilename();
		nReader = input.getReader();
		nNormal = input.getNormal();
		nDisease = input.getDisease();
		flagCompleteRecord = 1;
		AUC = input.getaucMod();

		String[] temp = recordDesp.split("\n");
		System.out.println("temp" + "0" + "  =  " + temp[0]);
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

		BDGcoeff = genBDGCoeff(nReader, nNormal, nDisease);
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

	public double[][] genBDGCoeff(int N2, int N0, int N1) {
		double[][] c = new double[4][8];
		c[0][0] = 1.0 / (N2 * N0 * N1);
		c[0][1] = c[0][0] * (N0 - 1.0);
		c[0][2] = c[0][0] * (N1 - 1.0);
		c[0][3] = c[0][0] * (N0 - 1.0) * (N1 - 1.0);
		c[0][4] = c[0][0] * (N2 - 1.0);
		c[0][5] = c[0][0] * (N0 - 1.0) * (N2 - 1.0);
		c[0][6] = c[0][0] * (N1 - 1.0) * (N2 - 1.0);
		c[0][7] = c[0][0] * (N1 - 1.0) * (N0 - 1) * (N2 - 1.0);
		c[0][7] = c[0][7] - 1;
		c[1] = c[0];
		c[2] = c[0];
		c[3] = c[0];

		return c;
	}

	public double[][] genBCKCoeff(int N2, int N0, int N1) {
		double[][] c = new double[4][7];
		c[0][0] = 1.0 / N0;
		c[0][1] = 1.0 / N1;
		c[0][2] = 1.0 / (N0 * N1);
		c[0][3] = 1.0 / N2;
		c[0][4] = 1.0 / (N0 * N2);
		c[0][5] = 1.0 / (N1 * N2);
		c[0][6] = 1.0 / (N1 * N0 * N2);

		c[1] = c[0];
		c[2] = c[0];
		c[3] = c[0];
		return c;
	}

	public double[][] genDBMCoeff(int N2, int N0, int N1) {
		double[][] c = new double[4][6];
		/*
		 * c[0][0] = 1.0/N2; c[0][1] = 1.0; c[0][2]= 1.0/N2; c[0][3] = 1.0/N2;
		 * c[0][4]= 1.0; c[0][5]= 1.0/N2;
		 */
		/* per unit */
		c[0][0] = 1.0 / N2;
		c[0][1] = 1.0 / (N0 + N1);
		c[0][2] = 1.0 / N2 / (N0 + N1);
		c[0][3] = 1.0 / N2;
		c[0][4] = 1.0 / (N0 + N1);
		c[0][5] = 1.0 / N2 / (N0 + N1);

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

	public double[][] genMSCoeff(int N2, int N0, int N1) {
		double[][] c = new double[4][6];
		double tmp = 1.0 / (N2 * (N0 + N1));
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

	public double[][] genORCoeff(int N2, int N0, int N1) {
		double[][] c = new double[4][6];
		c[0][0] = 1.0 / N2;
		c[0][1] = 0;
		c[0][2] = 0;
		c[0][3] = 1.0 / N2 * (N2 - 1);
		c[0][4] = 0;
		c[0][5] = 1.0 / N2;
		c[1] = c[0];
		c[2] = matrix.scaleVector(c[0], 0);
		c[3][0] = 0;
		c[3][1] = 2.0 / N2;
		c[3][2] = -2.0 / N2;
		c[3][3] = 2.0 / N2 * (N2 - 1);
		c[3][4] = -2.0 / N2 * (N2 - 1);
		c[3][5] = 2.0 / N2;

		return c;
	}

	public double[][] BDG2BCK(double[][] BDG) {
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

	public double[][] DBM2OR(int index, double[][] in, int N2, int N0, int N1) {
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
