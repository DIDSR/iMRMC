/**
 * DBRecord.java
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

package mrmc.core;

import java.util.*;
import java.text.DecimalFormat;

/**
 * The contents of an .omrmc file. Summary info from a reader study.
 * This class includes all the information on one
 * study. it also includes all the formulas to convert one decomposition to the
 * other. The conversion formulas are based on Gallas BD, Bandos A, Samuelson F,
 * Wagner RF. A Framework for random-effects ROC analsysis: Biases with the
 * Bootstrap and other variance estimators�, Commun Stat A-Theory 38(15),
 * 2586-2603 (2009). <br>
 * 
 * Important fields<br>
 * ----<code>recordDesc </code><br>
 * ----<code>recordTitle </code><br>
 * ----<code>filename </code><br>
 * ----<code>Task </code><br>
 * <br>

 * {@link #makeTMatrices} takes study data ({@link mrmc.core.InputFile#keyedData}, {@link mrmc.core.InputFile#truthVals})
 * and creates data for {@link mrmc.core.CovMRMC} <br>
 * --t-matrices: reader scores <br>
 * ----<code>t0_modAA, t0_modAB, t0_modBB</code>: signal-absent scores  [Nnormal ][Nreader][2 modalities] <br>
 * ----<code>t1_modAA, t1_modAB, t1_modBB</code>: signal-present scores [Ndisease][Nreader][2 modalities] <br>
 * --d-matrices: study design <br>
 * ----<code>d0_modAA, d0_modAB, d0_modBB</code>: signal absent scores  [Nnormal ][Nreader][2 modalities] <br>
 * ----<code>d1_modAA, d1_modAB, d1_modBB</code>: signal present scores [Ndisease][Nreader][2 modalities] <br>
 * CALLED BY: {@link mrmc.gui.RawStudyCard.varAnalysisListener} <br>
 * <br>
 * {@link #calculateCovMRMC} <br>
 * ----<code>BDG, BDGbiased, BDGcoeff</code>, [AA, BB, AB, A+B-2AB], [4][8] arrays  <br>
 * ----<br>
 * ----<code>AUCs</code>, [Nreader][2] array <br>
 * ----<code>AUCsReaderAvg</code>, [2] array <br>
 * ----<code>totalVar</code> based on the current study <br>
 * <br>
 * {@link #generateDecompositions} <br>
 * ----<code>BCK, BCKbias, BCKcoeff</code>, [4][7] arrays  <br>
 * ----If fully-crossed data, <br>
 * --------<code>DBM, DBMbias, DBMcoeff </code> [4][6] arrays: <br>
 * --------<code>OR, ORbias, ORcoeff</code>, [4][6] arrays: <br>
 * --------<code>MS, MSbias, MScoeff</code>, [4][6] arrays: <br>
 * <br>
 * 

 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
public class DBRecord {
	/**
	 * The contents of .omrmc file. The summary info from a reader study.
	 */
	private String recordDesc = "";
	/**
	 * The title of the reader study. The first line of .omrmc file.
	 */
	private String recordTitle = "";
	private String filename = "";
	private String Task = "";
	
	/**
	 * The scores from the readers
	 */
	public double[][][] t0_modAB, t1_modAB, t0_modAA, t1_modAA, t0_modBB, t1_modBB;
	public int[][][] d0_modAB, d1_modAB, d0_modAA, d1_modAA, d0_modBB, d1_modBB;

	/**
	 * The total variance of the reader-averaged AUC for <br>
	 * ----the modality selected <br>
	 * ----the difference in modalities
	 * 
	 */
	public double totalVar;
	/**
	 * The BDG unbiased moments, biased moments, and coefficients <br>
	 * ----Gallas2006_Acad-Radiol_v13p353 <br>
	 * ----Gallas2009_Commun-Stat-A-Theor_v38p2586 <br>
	 * 
	 */
	private double[][] BDG = new double[4][8];
	private double[][] BDGbias = new double[4][8];
	private double[][] BDGcoeff = new double[4][8];

	/**
	 * The BCK (Barrett, Clarkson, and Kupinski) variance components <br>
	 * ----Clarkson2006_Acad-Radiol_v13p1410 <br>
	 * ----Gallas2009_Commun-Stat-A-Theor_v38p2586 <br>
	 * 
	 */
	private double[][] BCKbias = new double[4][7], BCK = new double[4][7], BCKcoeff = new double[4][7];
	
	/** 
	 * The DBM (Dorfman, Berbaum, and Metz) variance components.
	 * Perhaps it would be better to refer to these as the 
	 * RM (Roe and Metz) variance components. <br>
	 * ----RM solidified the model <br>
	 * ----DBM presented an estimation  method <br>
	 * ---- <br>
	 * ----Dorfman1992_Invest-Radiol_v27p723 <br>
	 * ----Roe1997_Acad-Radiol_v4p587 <br>
	 * 
	 */
	private double[][] DBMbias = new double[4][6], DBM = new double[4][6], DBMcoeff = new double[4][6];
	
	/**
	 * The OR (Obuchowski and Rockette) variance components <br>
	 * ----Obuchowski1995_Commun-Stat-Simulat_v24p285 <br>
	 * ----Hillis2014_Stat-Med_v33p330 <br>
	 * 
	 */
	private double[][] ORbias = new double[4][6], OR = new double[4][6], ORcoeff = new double[4][6];

	/**
	 * The mean squares from a 3-way ANOVA
	 * ----Gallas2009_Commun-Stat-A-Theor_v38p2586 <br>
	 * 
	 */
	private double[][] MS = new double[4][6], MSbias = new double[4][6], MScoeff = new double[4][6];

	/**
	 * The reader-averaged AUCs for both modalities
	 * 
	 */
	private double[] AUCsReaderAvg = new double[2];

	/**
	 * The AUCs for each reader and modality [Nreader][2]
	 */
	private double[][] AUCs;

	private long Nreader;
	private long Nnormal;
	private long Ndisease;
	private boolean fullyCrossed = true;

	private InputFile inputFile;

	/**
	 * Gets a description of the task
	 * 
	 * @return String describing task performed
	 */
	public String getTask() {
		return Task;
	}

	/**
	 * Gets the filename of the raw data file or database file used to create
	 * the record
	 * 
	 * @return String containing filename path
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Gets the number of readers in the record
	 * 
	 * @return Number of readers
	 */
	public long getReader() {
		return Nreader;
	}

	/**
	 * Gets the number of normal cases in the record
	 * 
	 * @return Number of normal cases
	 */
	public long getNormal() {
		return Nnormal;
	}

	/**
	 * Gets the number of disease cases in the record
	 * 
	 * @return Number of disease cases
	 */
	public long getDisease() {
		return Ndisease;
	}

	/**
	 * Gets the title of the study record
	 * 
	 * @return String with title of the record
	 */
	public String getRecordTitle() {
		return recordTitle;
	}

	/**
	 * Gets a textual description of the study record
	 * 
	 * @return String with description of the record
	 */
	public String getRecordDesc() {
		return recordDesc;
	}

	/**
	 * Gets whether the record is fully-crossed or not
	 * 
	 * @return True if fully crossed, false otherwise
	 */
	public boolean getFullyCrossedStatus() {
		return fullyCrossed;
	}

	/**
	 * Get the reader-averaged AUCs for both modalities
	 * 
	 * @return Array containing AUCs for both modalities
	 */
	public double[] getAUCsReaderAvg() {
		return AUCsReaderAvg;
	}

	/**
	 * Get AUCs for each reader and modality [Nreader][2]
	 * 
	 * @return Array containing AUCs for each reader and modality [Nreader][2]
	 */
	public double[][] getAUCs() {
		return AUCs;
	}

	/**
	 * Gets the BDG components decomposition with bias or not
	 * 
	 * @param useBiasM Whether to get biased decomposition or not
	 * @return Matrix of BDG component decomposition
	 */
	public double[][] getBDG(int useBiasM) {
		if (useBiasM == 1)
			return BDGbias;
		else
			return BDG;
	}

	/**
	 * Gets the coefficient matrix for BDG decomposition
	 * 
	 * @return Coefficients matrix
	 */
	public double[][] getBDGcoeff() {
		return BDGcoeff;
	}

	/**
	 * Gets the BCK components decomposition with bias or not
	 * 
	 * @param useBiasM Whether to get biased decomposition or not
	 * @return Matrix of BCK component decomposition
	 */
	public double[][] getBCK(int useBiasM) {
		if (useBiasM == 1)
			return BCKbias;
		else
			return BCK;
	}

	/**
	 * Gets the coefficient matrix for BCK decomposition
	 * 
	 * @return Coefficients matrix
	 */
	public double[][] getBCKcoeff() {
		return BCKcoeff;
	}

	/**
	 * Gets the DBM components decomposition with bias or not
	 * 
	 * @param useBias Whether to get biased decomposition or not
	 * @return Matrix of DBM component decomposition
	 */
	public double[][] getDBM(int useBias) {
		if (useBias == 1)
			return DBMbias;
		else
			return DBM;
	}

	/**
	 * Gets the coefficient matrix for DBM decomposition
	 * 
	 * @return Coefficients matrix
	 */
	public double[][] getDBMcoeff() {
		return DBMcoeff;
	}

	/**
	 * Gets the OR components decomposition with bias or not
	 * 
	 * @param useBias Whether to get biased decomposition or not
	 * @return Matrix of OR component decomposition
	 */
	public double[][] getOR(int useBias) {
		if (useBias == 1)
			return ORbias;
		else
			return OR;
	}

	/**
	 * Gets the coefficient matrix for OR decomposition
	 * 
	 * @return Coefficients matrix
	 */
	public double[][] getORcoeff() {
		return ORcoeff;
	}

	/**
	 * Gets the MS components decomposition with bias or not
	 * 
	 * @param useBias Whether to get biased decomposition or not
	 * @return Matrix of MS component decomposition
	 */
	public double[][] getMS(int useBias) {
		if (useBias == 1)
			return MSbias;
		else
			return MS;
	}

	/**
	 * Gets the coefficient matrix for MS decomposition
	 * 
	 * @return Coefficients matrix
	 */
	public double[][] getMScoeff() {
		return MScoeff;
	}


	/**
	 * Constructor for creating a record from raw study data input file (two modalities)
	 * 
	 * @param input InputFile object containing information about the study
	 * @param selectedMod Identifies which pull-down menu is active: 0=modA, 1=modB, or 3=both
	 * @param currModA Which modality within the study we are using as ModA
	 * @param currModB Which modality within the study we are using as ModB
	 */
	public DBRecord(InputFile input, Integer selectedMod, String currModA, String currModB) {
		
		Task = "task unspecified";
	
		inputFile = input;
		Nnormal = inputFile.getNnormal();
		Ndisease = inputFile.getNdisease();
		Nreader = inputFile.getNreader();
	
		makeTMatrices(currModA, currModB);
		calculateCovMRMC(selectedMod);	
		generateDecompositions();
		
	}
	
	/**
	 * Constructor for creating a record from raw study data input file (single modality)
	 * 
	 * @param input InputFile object containing information about the study
	 * @param selectedMod Which modality within the study we are using
	 * @param currMod Which modality within the study we are using
	 */
	public DBRecord(InputFile input, Integer selectedMod, String currMod) {
		
		Task = "task unspecified";
	
		inputFile = input;
		Nnormal = inputFile.getNnormal();
		Ndisease = inputFile.getNdisease();
		Nreader = inputFile.getNreader();
	
		makeTMatrices(currMod, currMod);
		calculateCovMRMC(selectedMod);
	
		generateDecompositions();
		
	}

	/**
	 * Constructor for creating a record from the internal database
	 * 
	 * @param fname Filename of the database file
	 * @param componentStrings Array of strings containing component information
	 * @param desc Contents of .omrmc file, summary stats from reader study
	 * @param AUCstr AUCs in string form
	 */
	public DBRecord(String fname, String[] componentStrings,
			ArrayList<String> desc, String AUCstr) {
		int i, j;
		recordTitle = desc.get(0).substring(2);
		filename = fname;
		// Currently all files in DB are fully crossed, but this may change
		fullyCrossed = true;

		for (i = 0; i < 7; i++) {
			String tempStr = desc.get(i);
			if (tempStr.startsWith("*  Modal")) {
			}
			if (tempStr.startsWith("*  Task") || tempStr.startsWith("*  TASK")) {
				String[] tempStr2 = tempStr.split(",");
				Task = tempStr2[1];
			}
		}

		String[] tempAUC = AUCstr.split(",");
		AUCsReaderAvg[0] = Double.valueOf(tempAUC[1]);
		AUCsReaderAvg[1] = Double.valueOf(tempAUC[2]);

		Nreader = Integer.valueOf(tempAUC[3]);
		Nnormal = Integer.valueOf(tempAUC[4]);
		Ndisease = Integer.valueOf(tempAUC[5]);

// TODO		checkStudyDesign();

		for (i = 0; i < desc.size(); i++) {
			recordDesc = recordDesc + desc.get(i) + "\n";
		}

		// Grab BDG components from file string
		for (i = 0; i < componentStrings.length / 2; i++) {
			String[] temp = componentStrings[i].split(",");
			for (j = 0; j < 8; j++) {
				BDG[i][j] = Double.valueOf(temp[j]);
			}
		}
		for (i = componentStrings.length / 2; i < componentStrings.length; i++) {
			String[] temp = componentStrings[i].split(",");
			for (j = 0; j < 8; j++) {
				BDGbias[i - componentStrings.length / 2][j] = Double
						.valueOf(temp[j]);
			}
		}
		generateDecompositions();
	}

	/**
	 * Constructor for creating a record from manual input of components of
	 * variance
	 * 
	 * @param components Components of variance entered from GUI
	 * @param whichComp Specifies with decomposition of components are entered
	 * @param n Number of readers
	 * @param n2 Number of normal cases
	 * @param n3 Number of disease cases
	 * @param auc The reader-averaged auc for each modality
	 */
	public DBRecord(double[][] components, int whichComp, long n, long n2,
			long n3, double[] auc) {
		AUCsReaderAvg = auc;
		Nreader = n;
		Nnormal = n2;
		Ndisease = n3;

		fullyCrossed = true;

// TODO		checkStudyDesign();

		switch (whichComp) {
		case 0: // BDG
			BDG = components;
			BDGbias = components;
			BDGcoeff = genBDGCoeff(Nreader, Nnormal, Ndisease);
			BCKcoeff = genBCKCoeff(Nreader, Nnormal, Ndisease);
			DBMcoeff = genDBMCoeff(Nreader, Nnormal, Ndisease);
			ORcoeff = genORCoeff(Nreader, Nnormal, Ndisease);
			MScoeff = genMSCoeff(Nreader, Nnormal, Ndisease);
			BCK = BDG2BCK(BDG);
			OR = BDG2OR(BDG, Nreader, Nnormal, Ndisease);
			DBM = BCK2DBM(BCK, Nreader, Nnormal, Ndisease);

			BCKbias = BCK;
			DBMbias = DBM;
			ORbias = OR;
			DBM[3][0] = 0;
			DBM[3][1] = 0;
			DBM[3][2] = 0;
			DBM[3][3] = DBM[0][0];
			DBM[3][4] = DBM[0][1];
			DBM[3][5] = DBM[0][2];
			MS = DBM2MS(DBM, Nreader, Nnormal, Ndisease);
			OR = DBM2OR(0, DBM, Nreader, Nnormal, Ndisease);
			break;
		case 1: // BCK
			BDG = components;
			BCK = components;
			BCKcoeff = genBCKCoeff(Nreader, Nnormal, Ndisease);
			DBMcoeff = genDBMCoeff(Nreader, Nnormal, Ndisease);
			ORcoeff = genORCoeff(Nreader, Nnormal, Ndisease);
			MScoeff = genMSCoeff(Nreader, Nnormal, Ndisease);
			DBM = BCK2DBM(BCK, Nreader, Nnormal, Ndisease);
			OR = DBM2OR(0, DBM, Nreader, Nnormal, Ndisease);
			BCKbias = BCK;
			DBMbias = DBM;
			ORbias = OR;
			DBM[3][0] = 0;
			DBM[3][1] = 0;
			DBM[3][2] = 0;
			DBM[3][3] = DBM[0][0];
			DBM[3][4] = DBM[0][1];
			DBM[3][5] = DBM[0][2];
			OR = DBM2OR(0, DBM, Nreader, Nnormal, Ndisease);
			MS = DBM2MS(DBM, Nreader, Nnormal, Ndisease);
			break;
		case 2: // DBM
			DBM = components;
			DBMbias = components;
			OR = DBM2OR(0, DBM, Nreader, Nnormal, Ndisease);
			MS = DBM2MS(DBM, Nreader, Nnormal, Ndisease);
			ORbias = OR;
			DBMcoeff = genDBMCoeff(Nreader, Nnormal, Ndisease);
			ORcoeff = genORCoeff(Nreader, Nnormal, Ndisease);
			MScoeff = genMSCoeff(Nreader, Nnormal, Ndisease);
			break;
		case 3: // OR
			OR = components;
			ORbias = components;
			DBM = DBM2OR(1, OR, Nreader, Nnormal, Ndisease);
			MS = DBM2MS(DBM, Nreader, Nnormal, Ndisease);
			DBMbias = DBM;
			DBMcoeff = genDBMCoeff(Nreader, Nnormal, Ndisease);
			ORcoeff = genORCoeff(Nreader, Nnormal, Ndisease);
			MScoeff = genMSCoeff(Nreader, Nnormal, Ndisease);
			break;
		default:
			break;

		}
	}

	/**
	 * Derives all decompositions and coefficient matrices from predefined BDG
	 * components and experiment size
	 */
	private void generateDecompositions() {

		BCK = BDG2BCK(BDG);
		BCKbias = BDG2BCK(BDGbias);
		BCKcoeff = genBCKCoeff(BDGcoeff);
		
		if(fullyCrossed) {
			DBMcoeff = genDBMCoeff(Nreader, Nnormal, Ndisease);
			ORcoeff = genORCoeff(Nreader, Nnormal, Ndisease);
			MScoeff = genMSCoeff(Nreader, Nnormal, Ndisease);
	
			DBM = BCK2DBM(BCK, Nreader, Nnormal, Ndisease);
			OR = BDG2OR(BDG, Nreader, Nnormal, Ndisease);
			// OR = DBM2OR(0, DBM, Nreader, Nnormal, Ndisease);
			MS = DBM2MS(DBM, Nreader, Nnormal, Ndisease);
			
			DBMbias = BCK2DBM(BCKbias, Nreader, Nnormal, Ndisease);
			ORbias = DBM2OR(0, DBMbias, Nreader, Nnormal, Ndisease);
			MSbias = DBM2MS(DBMbias, Nreader, Nnormal, Ndisease);
		}
	}

	/**
	 * Gets the AUCsReaderAvg of the record in String format
	 * 
	 * @param selectedMod specifies modality A, B, or difference
	 * @return String containing AUCsReaderAvg
	 */
	public String getAUCsReaderAvg(int selectedMod) {
		DecimalFormat threeDec = new DecimalFormat("0.000");
		threeDec.setGroupingUsed(false);

		String temp = "";
		switch (selectedMod) {
		case 0:
			temp = "AUC1=" + threeDec.format(AUCsReaderAvg[0]) + "       ";
			break;
		case 1:
			temp = "AUC2=" + threeDec.format(AUCsReaderAvg[1]) + "       ";
			break;
		case 3:
			temp = "AUC1=" + threeDec.format(AUCsReaderAvg[0]) + "       AUC2="
					+ threeDec.format(AUCsReaderAvg[1]) + "       AUC1-AUC2="
					+ threeDec.format(AUCsReaderAvg[0] - AUCsReaderAvg[1]) + "       ";
		}
		return temp;
	}

	/**
	 * Gets the AUCs of the record as a double
	 * 
	 * @param whichMod specifies modality 0, 1, or difference
	 * @return Double representation of AUC
	 */
	public double getAUCinNumber(int whichMod) {
		return AUCsReaderAvg[whichMod];
	}

	/**
	 * Gets the experiment sizes in string format
	 * 
	 * @return String containing experiment sizes
	 */
	public String getSizes() {
		return (Long.toString(Nreader) + " Readers,  "
				+ Long.toString(Nnormal) + " Normal cases,  "
				+ Long.toString(Ndisease) + " Disease cases.");
	}

	/**
	 * Gets the experiment sizes in int representation
	 * 
	 * @return Array containing experiment sizes
	 */
	public long[] getSizesInt() {
		long[] c = { Nreader, Nnormal, Ndisease };
		return c;
	}

	/**
	 * Gets the components, coefficients, and total variance for BDG
	 * representation in format corresponding to display table
	 * 
	 * @param selectedMod Which modality the analysis is performed on, or
	 *            difference
	 * @param BDGtemp BDG decomposition of variance components
	 * @param BDGc Coefficient matrix for BDG decomposition
	 * @return Matrix of data for display in table
	 */
	public static double[][] getBDGTab(int selectedMod, double[][] BDGtemp,
			double[][] BDGc) {
		double[][] BDGTab1 = new double[7][8];
		if (selectedMod == 0) {
			BDGTab1[0] = BDGtemp[0];
			BDGTab1[1] = BDGc[0];
		} else if (selectedMod == 1) {
			BDGTab1[2] = BDGtemp[1];
			BDGTab1[3] = BDGc[1];
		} else if (selectedMod == 3) {
			BDGTab1[0] = BDGtemp[0];
			BDGTab1[1] = BDGc[0];
			BDGTab1[2] = BDGtemp[1];
			BDGTab1[3] = BDGc[1];
			BDGTab1[4] = BDGtemp[2]; // covariance
			BDGTab1[5] = Matrix.scaleVector(BDGc[2], 2);
		}
		for (int i = 0; i < 8; i++) {
			BDGTab1[6][i] = (BDGTab1[0][i] * BDGTab1[1][i])
					+ (BDGTab1[2][i] * BDGTab1[3][i])
					- (BDGTab1[4][i] * BDGTab1[5][i]);
		}
		return BDGTab1;
	}
	
	/**
	 * Gets the components, coefficients, and total variance for DBM
	 * representation in format corresponding to display table
	 * 
	 * @param selectedMod Which modality the analysis is performed on, or
	 *            difference
	 * @param DBMtemp DBM decomposition of variance components
	 * @param DBMc Coefficient matrix for DBM decomposition
	 * @return Matrix of data for display in table
	 */
	public static double[][] getDBMTab(int selectedMod, double[][] DBMtemp,
			double[][] DBMc) {
		double[][] DBMTab1 = new double[3][6];
		DBMTab1[0] = DBMtemp[selectedMod];
		DBMTab1[1] = DBMc[selectedMod];
		DBMTab1[2] = Matrix.dotProduct(DBMTab1[0], DBMTab1[1]);
		return DBMTab1;
	}

	/**
	 * Gets the components, coefficients, and total variance for BCK
	 * representation in format corresponding to display table
	 * 
	 * @param selectedMod Which modality the analysis is performed on, or
	 *            difference
	 * @param BCKtemp BCK decomposition of variance components
	 * @param BCKc Coefficient matrix for BCK decomposition
	 * @return Matrix of data for display in table
	 */
	public static double[][] getBCKTab(int selectedMod, double[][] BCKtemp,
			double[][] BCKc) {
		double[][] BCKTab1 = new double[7][7];
		if (selectedMod == 0) {
			BCKTab1[0] = BCKtemp[0];
			BCKTab1[1] = BCKc[0];
		} else if (selectedMod == 1) {
			BCKTab1[2] = BCKtemp[1];
			BCKTab1[3] = BCKc[1];
		} else if (selectedMod == 3) {
			BCKTab1[0] = BCKtemp[0];
			BCKTab1[1] = BCKc[0];
			BCKTab1[2] = BCKtemp[1];
			BCKTab1[3] = BCKc[1];
			BCKTab1[4] = BCKtemp[2]; // covariance
			BCKTab1[5] = Matrix.scaleVector(BCKc[3], 2);
		}
		for (int i = 0; i < 7; i++) {
			BCKTab1[6][i] = (BCKTab1[0][i] * BCKTab1[1][i])
					+ (BCKTab1[2][i] * BCKTab1[3][i])
					- (BCKTab1[4][i] * BCKTab1[5][i]);
		}
		return BCKTab1;
	}



	/**
	 * Gets the components, coefficients, and total variance for OR
	 * representation in format corresponding to display table
	 * 
	 * @param selectedMod Which modality the analysis is performed on, or
	 *            difference
	 * @param ORtemp OR decomposition of variance components
	 * @param ORc Coefficient matrix for OR decomposition
	 * @return Matrix of data for display in table
	 */
	public static double[][] getORTab(int selectedMod, double[][] ORtemp,
			double[][] ORc) {
		double[][] ORTab1 = new double[3][6];
		ORTab1[0] = ORtemp[selectedMod];
		ORTab1[1] = ORc[selectedMod];
		ORTab1[2] = Matrix.dotProduct(ORTab1[0], ORTab1[1]);
		return ORTab1;
	}

	/**
	 * Gets the components, coefficients, and total variance for MS
	 * representation in format corresponding to display table
	 * 
	 * @param selectedMod Which modality the analysis is performed on, or
	 *            difference
	 * @param MStemp MS decomposition of variance components
	 * @param MSc Coefficient matrix for MS decomposition
	 * @return Matrix of data for display in table
	 */
	public static double[][] getMSTab(int selectedMod, double[][] MStemp,
			double[][] MSc) {
		double[][] MSTab1 = new double[3][6];
		MSTab1[0] = MStemp[selectedMod];
		MSTab1[1] = MSc[selectedMod];
		MSTab1[2] = Matrix.dotProduct(MSTab1[0], MSTab1[1]);
		return MSTab1;
	}

	/**
	 * Transform DBM representation of variance components into MS
	 * representation of variance components
	 * 
	 * @param DBM Matrix of DBM variance components
	 * @param Nreader2 Number of readers
	 * @param Nnormal2 Number of normal cases
	 * @param Ndisease2 Number of disease cases
	 * @return Matrix of MS representation of variance components
	 */
	public static double[][] DBM2MS(double[][] DBM, long Nreader2, long Nnormal2, long Ndisease2) {
		double[][] c = new double[4][6];
		double[][] BAlpha = new double[][] {
				{ 2 * (Nnormal2 + Ndisease2), 0, 2, (Nnormal2 + Ndisease2), 0, 1 },
				{ 0, 2 * Nreader2, 2, 0, Nreader2, 1 }, { 0, 0, 0, (Nnormal2 + Ndisease2), 0, 1 },
				{ 0, 0, 0, 0, Nreader2, 1 }, { 0, 0, 2, 0, 0, 1 },
				{ 0, 0, 0, 0, 0, 1 } };
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 6; j++)
				c[i][j] = 0;
		c = Matrix.matrixTranspose(Matrix.multiply(BAlpha,
				Matrix.matrixTranspose(DBM)));
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 6; j++)
				c[i][j] = c[i][j] / 2.0;

		return c;

	}

	/**
	 * Transforms BCK representation of variance components into DBM
	 * representation of variance components
	 * 
	 * @param BCK Matrix of BCK variance components
	 * @param Nreader2 Number of readers
	 * @param Nnormal2 Number of normal cases
	 * @param Ndisease2 Number of disease cases
	 * @return Matrix of DBM representation of variance components
	 */
	public static double[][] BCK2DBM(double[][] BCK, long Nreader2, long Nnormal2, long Ndisease2) {
		double[] c = new double[7];
		double[][] tmp = new double[4][7];
		double[][] tmp1 = new double[4][3];
		double[][] results = new double[4][6];

		// Scale the BCK components to the experiment
		c[0] = 1.0 / Nnormal2;
		c[1] = 1.0 / Ndisease2;
		c[2] = 1.0 / (Nnormal2 * Ndisease2);
		c[3] = 1.0 / Nreader2;
		c[4] = 1.0 / (Nnormal2 * Nreader2);
		c[5] = 1.0 / (Ndisease2 * Nreader2);
		c[6] = 1.0 / (Nnormal2 * Ndisease2 * Nreader2);
		for (int i = 0; i < 3; i++)
			tmp[i] = Matrix.dotProduct(BCK[i], c);

		// alpha is the matrix that maps BCK to DBM
		double[][] alpha = new double[][] { { 0, 1, 0 }, { 0, 1, 0 },
				{ 0, 1, 0 }, { Nreader2, 0, 0 }, { 0, 0, Nreader2 }, { 0, 0, Nreader2 },
				{ 0, 0, Nreader2 } };

		tmp1 = Matrix.multiply(tmp, alpha);

		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 6; j++)
				results[i][j] = 0;

		results[0][0] = tmp1[0][0];
		results[0][1] = tmp1[0][1] * (Nnormal2 + Ndisease2);
		results[0][2] = tmp1[0][2] * (Nnormal2 + Ndisease2);

		results[1][0] = tmp1[1][0];
		results[1][1] = tmp1[1][1] * (Nnormal2 + Ndisease2);
		results[1][2] = tmp1[1][2] * (Nnormal2 + Ndisease2);

		results[3][0] = tmp1[2][0];
		results[3][1] = tmp1[2][1] * (Nnormal2 + Ndisease2);
		results[3][2] = tmp1[2][2] * (Nnormal2 + Ndisease2);

		results[3][3] = (tmp1[0][0] + tmp1[1][0]) / 2.0 - tmp1[2][0];
		results[3][4] = ((tmp1[0][1] + tmp1[1][1]) / 2.0 - tmp1[2][1])
				* (Nnormal2 + Ndisease2);
		results[3][5] = ((tmp1[0][2] + tmp1[1][2]) / 2.0 - tmp1[2][2])
				* (Nnormal2 + Ndisease2);

		return results;
	}

	/**
	 * Transforms BDG representation of variance components into BCK
	 * representation of variance components
	 * 
	 * @param BDG Matrix of BDG variance components
	 * @return Matrix of BCK representation of variance components
	 */
	public static double[][] BDG2BCK(double[][] BDG) {
		double[][] c = new double[3][7];
		double[][] BAlpha = new double[][] { { 0, 0, 0, 0, 0, 0, 1, -1 },
				{ 0, 0, 0, 0, 0, 1, 0, -1 }, { 0, 0, 0, 0, 1, -1, -1, 1 },
				{ 0, 0, 0, 1, 0, 0, 0, -1 }, { 0, 0, 1, -1, 0, 0, -1, 1 },
				{ 0, 1, 0, -1, 0, -1, 0, 1 }, { 1, -1, -1, 1, -1, 1, 1, -1 } };
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 7; j++)
				c[i][j] = 0;
				c = Matrix.matrixTranspose(Matrix.multiply(BAlpha,Matrix.matrixTranspose(BDG)));

		return c;
	}



	/**
	 * Determines the coefficient matrix for BDG variance components given a
	 * fully crossed study design [4][8].
	 * This is only executed when the components of variance are input by hand or during iRoeMetz
	 * 
	 * @param Nreader2 Number of readers
	 * @param Nnormal2 Number of normal cases
	 * @param Ndisease2 Number of disease cases
	 * @return Matrix containing coefficients corresponding to BDG variance
	 *         components
	 */
	public static double[][] genBDGCoeff(long Nreader2, long Nnormal2, long Ndisease2) {
		double[][] c = new double[4][8];
		c[0][0] = 1.0 / (Nreader2 * Nnormal2 * Ndisease2);
		c[0][1] = c[0][0] * (Nnormal2 - 1.0);
		c[0][2] = c[0][0] * (Ndisease2 - 1.0);
		c[0][3] = c[0][0] * (Nnormal2 - 1.0) * (Ndisease2 - 1.0);
		c[0][4] = c[0][0] * (Nreader2 - 1.0);
		c[0][5] = c[0][0] * (Nnormal2 - 1.0) * (Nreader2 - 1.0);
		c[0][6] = c[0][0] * (Ndisease2 - 1.0) * (Nreader2 - 1.0);
		c[0][7] = c[0][0] * (Ndisease2 - 1.0) * (Nnormal2 - 1.0) * (Nreader2 - 1.0);
		c[0][7] = c[0][7] - 1;
		c[1] = c[0];
		c[2] = c[0];
		c[3] = c[0];

		return c;
	}

	/**
	 * Determines the coefficient matrix for BCK variance components given <code>BDGcoeff</code> <br>
	 * There is another function that creates the coefficients assuming a fully-crossed study design.
	 * The inputs in that case are the number of readers, diseased cases, and nondiseased cases
	 * 
	 * @return Matrix containing coefficients corresponding to BCK variance [4][7]
	 *         components
	 */
	public static double[][] genBCKCoeff(double[][] BDGcoeff_temp) {
		double[][] c2ca = new double[][] {
				{ 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0 },
				{ 1.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0 },
				{ 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 },
				{ 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0 },
				{ 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 } };

		double[][] cAlpha = Matrix.multiply(BDGcoeff_temp, Matrix.matrixTranspose(c2ca));
		for(int i=0; i<7; i++) cAlpha[3][i] = 1.0;
		return cAlpha;
	}
	
	/**
	 * Determines the coefficient matrix for BCK variance components given <code>BDGcoeff</code>
	 * 
	 * @return Matrix containing coefficients corresponding to BCK variance [4][7]
	 *         components
	 */
	public static double[][] genBCKCoeff(long Nreader, long Nnormal, long Ndisease) {
		double[][] cAlpha = new double[][] {
				{ 1.0/Nnormal, 1.0/Ndisease, 1.0/Nnormal/Ndisease, 
					1.0/Nreader, 1.0/Nreader/Nnormal, 1.0/Nreader/Ndisease, 1.0/Nreader/Nnormal/Ndisease},
				{ 1.0/Nnormal, 1.0/Ndisease, 1.0/Nnormal/Ndisease, 
					1.0/Nreader, 1.0/Nreader/Nnormal, 1.0/Nreader/Ndisease, 1.0/Nreader/Nnormal/Ndisease},
				{ 1.0/Nnormal, 1.0/Ndisease, 1.0/Nnormal/Ndisease, 
					1.0/Nreader, 1.0/Nreader/Nnormal, 1.0/Nreader/Ndisease, 1.0/Nreader/Nnormal/Ndisease},
				{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}};

		return cAlpha;
	}



	/**
	 * Determines the coefficient matrix for DBM variance components given a
	 * fully crossed study design [4][6]
	 * 
	 * @param Nreader2 Number of readers
	 * @param Nnormal2 Number of normal cases
	 * @param Ndisease2 Number of disease cases
	 * @return Matrix containing coefficients corresponding to DBM variance
	 *         components
	 */
	public static double[][] genDBMCoeff(long Nreader2, long Nnormal2, long Ndisease2) {
		double[][] c = new double[4][6];

		/* per unit */
		c[0][0] = 1.0 / Nreader2;
		c[0][1] = 1.0 / (Nnormal2 + Ndisease2);
		c[0][2] = 1.0 / Nreader2 / (Nnormal2 + Ndisease2);
		c[0][3] = 1.0 / Nreader2;
		c[0][4] = 1.0 / (Nnormal2 + Ndisease2);
		c[0][5] = 1.0 / Nreader2 / (Nnormal2 + Ndisease2);

		c[1] = c[0];
		c[2] = Matrix.scaleVector(c[0], 0);
		c[3] = Matrix.scaleVector(c[0], 2);

		c[0][3] = 0;
		c[0][4] = 0;
		c[0][5] = 0;
		c[1][3] = 0;
		c[1][4] = 0;
		c[1][5] = 0;
		c[3][0] = 0;
		c[3][1] = 0;
		c[3][2] = 0;
		return c;
	}

	/**
	 * Determines the coefficient matrix for MS variance components given a
	 * fully crossed study design [4][6]
	 * 
	 * @param Nreader2 Number of readers
	 * @param Nnormal2 Number of normal cases
	 * @param Ndisease2 Number of disease cases
	 * @return Matrix containing coefficients corresponding to MS variance
	 *         components
	 */
	public static double[][] genMSCoeff(long Nreader2, long Nnormal2, long Ndisease2) {
		double[][] c = new double[4][6];
		double tmp = 1.0 / (Nreader2 * (Nnormal2 + Ndisease2));
		c[0][0] = tmp;
		c[0][1] = tmp;
		c[0][2] = 0;
		c[0][3] = 0;
		c[0][4] = -tmp;
		c[0][5] = 0;
		c[1] = c[0];
		c[2] = Matrix.scaleVector(c[0], 0);
		c[3][0] = 0;
		c[3][1] = 0;
		c[3][2] = tmp * 2.0;
		c[3][3] = tmp * 2.0;
		c[3][4] = 0;
		c[3][5] = -tmp * 2.0;
		return c;
	}

	/**
	 * Determines the coefficient matrix for OR variance components given a
	 * fully crossed study design [4][6]
	 * 
	 * @param Nreader2 Number of readers
	 * @param Nnormal2 Number of normal cases
	 * @param Ndisease2 Number of disease cases
	 * @return Matrix containing coefficients corresponding to OR variance
	 *         components
	 */
	public static double[][] genORCoeff(long Nreader2, long Nnormal2, long Ndisease2) {
		double[][] c = new double[4][6];
		c[0][0] = 1.0 / Nreader2;
		c[0][1] = 0;
		c[0][2] = 0;
		c[0][3] = 1.0 / Nreader2 * (Nreader2 - 1);
		c[0][4] = 0;
		c[0][5] = 1.0 / Nreader2;
		c[1] = c[0];
		c[2] = Matrix.scaleVector(c[0], 0);
		c[3][0] = 0;
		c[3][1] = 2.0 / Nreader2;
		c[3][2] = -2.0 / Nreader2;
		c[3][3] = 2.0 / Nreader2 * (Nreader2 - 1);
		c[3][4] = -2.0 / Nreader2 * (Nreader2 - 1);
		c[3][5] = 2.0 / Nreader2;

		return c;
	}



	/**
	 * Transforms matrix of BDG variance components into matrix of OR variance
	 * components
	 * 
	 * @param BDG Matrix of BDG variance components
	 * @param nreader2 Number of readers
	 * @param nnormal2 Number of normal cases
	 * @param ndisease2 Number of disease cases
	 * @return Matrix of OR variance components
	 */
	public double[][] BDG2OR(double[][] BDG, long nreader2, long nnormal2, long ndisease2) {
		double[][] c = new double[4][6];
		
		double temp, dnr, dn0, dn1, dnm;
		dnr = nreader2;
		dn0 = nnormal2;
		dn1 = ndisease2;
		dnm = 2;
		
		double ms_t, ms_r, ms_rA, ms_rB, ms_tr, ms_trA, ms_trB;
		double errorA, errorB, error, cov1A, cov1B, cov1, cov2A, cov2B, cov2, cov3A, cov3B, cov3;
		double c1 = 1.0/dn0/dn1;
		double c2 = (dn0-1.0)/dn0/dn1;
		double c3 = (dn1-1.0)/dn0/dn1;
		double c4 = (dn0-1.0)*(dn1-1.0)/dn0/dn1 - 1.0;

		ms_t = 0.0;
		ms_tr = 0.0;
		ms_trA = 0.0;
		ms_trB = 0.0;
		for(int i=0; i<2; i++) {
			temp = AUCsReaderAvg[i] - (AUCsReaderAvg[0] + AUCsReaderAvg[1])/2.0;
			ms_t = ms_t + Math.pow(temp, 2); 
			for(int j=0; j<dnr; j++) {
				temp = AUCs[j][i] - 
						AUCsReaderAvg[i] - 
						(AUCs[j][0] + AUCs[j][1])/2.0 + 
						(AUCsReaderAvg[0] + AUCsReaderAvg[1])/2.0;
				ms_tr = ms_tr + Math.pow(temp, 2); 
			}
		}
		ms_t = dnr*ms_t/(dnm-1.0);
		ms_tr = ms_tr/(dnr-1.0)/(dnm-1.0);
		
		ms_r = 0.0;
		ms_rA = 0.0;
		ms_rB = 0.0;
		for(int j=0; j<dnr; j++) {
			temp = AUCs[j][0] - AUCsReaderAvg[0];
			ms_rA = ms_rA + Math.pow(temp, 2); 

			temp = AUCs[j][1] - AUCsReaderAvg[1];
			ms_rB = ms_rB + Math.pow(temp, 2); 

			temp = (AUCs[j][0]+AUCs[j][1])/2.0 - (AUCsReaderAvg[0]+AUCsReaderAvg[1])/2.0;
			ms_r = ms_r   + Math.pow(temp, 2); 
		}
		ms_rA = ms_rA/(dnr-1.0);
		ms_rB = ms_rB/(dnr-1.0);
		ms_r  = dnm*ms_r /(dnr-1.0);

		errorA = c1*BDG[0][0] + c2*BDG[0][1] + c3*BDG[0][2] + c4*BDG[0][3];
		cov1A =  0.0;
		cov2A =  c1*BDG[0][4] + c2*BDG[0][5] + c3*BDG[0][6] + c4*BDG[0][7];
		cov3A =  0.0;

		errorB = c1*BDG[1][0] + c2*BDG[1][1]+ c3*BDG[1][2] + c4*BDG[1][3];
		cov1B =  0.0;
		cov2B =  c1*BDG[1][4] + c2*BDG[1][5]+ c3*BDG[1][6] + c4*BDG[1][7];
		cov3B =  0.0;

		error =  (errorA + errorB)/2.0;
		cov1 =   c1*BDG[2][0] + c2*BDG[2][1]+ c3*BDG[2][2] + c4*BDG[2][3];
		cov2 =   (cov2A  + cov2B )/2.0;
		cov3 =   c1*BDG[2][4] + c2*BDG[2][5]+ c3*BDG[2][6] + c4*BDG[2][7];

		c[0][0] = ms_rA;
		c[0][1] = ms_trA;
		c[0][2] = cov1A;
		c[0][3] = cov2A;
		c[0][4] = cov3A;
		c[0][5] = errorA;

		c[1][0] = ms_rB;
		c[1][1] = ms_trB;
		c[1][2] = cov1B;
		c[1][3] = cov2B;
		c[1][4] = cov3B;
		c[1][5] = errorB;

		c[3][0] = (ms_r - ms_tr)/2.0 - cov1 + cov3;
		c[3][1] = ms_tr - error + cov1 + (cov2-cov3);
		c[3][2] = cov1;
		c[3][3] = cov2;
		c[3][4] = cov3;
		c[3][5] = error;

		return c;
	}

	/**
	 * Transforms matrix of DBM variance components into matrix of OR variance
	 * components, or vice versa
	 * 
	 * @param index Whether the in[][] parameter is DBM or OR components
	 * @param in Matrix of DBM or OR variance components
	 * @param Nreader2 Number of readers
	 * @param Nnormal2 Number of normal cases
	 * @param Ndisease2 Number of disease cases
	 * @return Matrix of OR or DBM variance components
	 */
	public static double[][] DBM2OR(int index, double[][] in, long Nreader2, long Nnormal2,
			long Ndisease2) {
		double[][] toReturn = new double[4][6];
		double[][] dbm = new double[4][6];
		double[][] orVar = new double[4][6];
		if (index == 0) {// the input is DBM;
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
				orVar[i][2] = (dbm[i][1] + dbm[i][2]) / (Nnormal2 + Ndisease2);
				orVar[i][3] = (dbm[i][1] + dbm[i][4]) / (Nnormal2 + Ndisease2);
				orVar[i][4] = dbm[i][1] / (Nnormal2 + Ndisease2);
				orVar[i][5] = (dbm[i][1] + dbm[i][2] + dbm[i][4] + dbm[i][5])
						/ (Nnormal2 + Ndisease2);

			}
			toReturn = orVar;
		} else { // the input is OR
			orVar = in;
			for (int i = 0; i < 4; i++) {
				/*
				 * dbm[i][0]=orVar[i][0]; dbm[i][1]=orVar[i][4];
				 * dbm[i][2]=orVar[i][2]-orVar[i][4]; dbm[i][3]=orVar[i][1];
				 * dbm[i][4]=orVar[i][3]-orVar[i][4];
				 * dbm[i][5]=orVar[i][5]-orVar[i][2]-orVar[i][3]+orVar[i][4];
				 */

				dbm[i][0] = orVar[i][0];
				dbm[i][1] = orVar[i][4] * (Nnormal2 + Ndisease2);
				dbm[i][2] = (orVar[i][2] - orVar[i][4]) * (Nnormal2 + Ndisease2);
				dbm[i][3] = orVar[i][1];
				dbm[i][4] = (orVar[i][3] - orVar[i][4]) * (Nnormal2 + Ndisease2);
				dbm[i][5] = (orVar[i][5] - orVar[i][2] - orVar[i][3] + orVar[i][4])
						* (Nnormal2 + Ndisease2);
			}
			toReturn = dbm;

		}
		return toReturn;
	}

	/**
	 * Resizes BCK variance components matrix, but actually currently does
	 * nothing
	 * 
	 * @param bck BCK variance components matrix
	 * @param newR Number of readers
	 * @param newN Number of normal cases
	 * @param newD Number of disease cases
	 * @return BCK variance components matrix
	 */
	public double[][] BCKresize(double[][] bck, int newR, int newN, int newD) {
		return bck;
	}

	/**
	 * Scales/resizes parts of DBM variance components matrix according to
	 * number of normal/disease cases
	 * 
	 * @param dbm DBM variance components matrix
	 * @param newR Number of readers
	 * @param newN Number of normal cases
	 * @param newD Number of disease cases
	 * @return Resized DBM variance components matrix
	 */
	public double[][] DBMresize(double[][] dbm, int newR, int newN, int newD) {
		double[][] DBMnew = new double[4][6];
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
	
	/**
	 * Takes study data ({@link mrmc.core.InputFile#keyedData}, {@link mrmc.core.InputFile#keyedData})
	 *  and creates data for {@link mrmc.core.CovMRMC} <br>
	 * --t-matrices: reader scores <br>
 	 * ----t0_modAA, t0_modAB, t0_modBB: signal-absent scores  [Nnormal ][Nreader][2 modalities] <br>
	 * ----t1_modAA, t1_modAB, t1_modBB: signal-present scores [Ndisease][Nreader][2 modalities] <br>
	 * --d-matrices: study design <br>
	 * ----d0_modAA, d0_modAB, d0_modBB: signal absent scores  [Nnormal ][Nreader][2 modalities] <br>
	 * ----d1_modAA, d1_modAB, d1_modBB: signal present scores [Ndisease][Nreader][2 modalities] <br>
	 *
	 * 
	 * @param modA Modality to be used as mod 0
	 * @param modB Modality to be used as mod 1
	 */
	public void makeTMatrices(String modA, String modB) {
		
		t0_modAB = new double[(int) Nnormal][(int) Nreader][2];
		t1_modAB = new double[(int) Ndisease][(int) Nreader][2];
		t0_modAA = new double[(int) Nnormal][(int) Nreader][2];
		t0_modBB = new double[(int) Nnormal][(int) Nreader][2];
		t1_modAA = new double[(int) Ndisease][(int) Nreader][2];
		t1_modBB = new double[(int) Ndisease][(int) Nreader][2];
		d0_modAA = new int[(int) Nnormal][(int) Nreader][2];
		d1_modAA = new int[(int) Ndisease][(int) Nreader][2];
		d0_modBB = new int[(int) Nnormal][(int) Nreader][2];
		d1_modBB = new int[(int) Ndisease][(int) Nreader][2];
		d0_modAB = new int[(int) Nnormal][(int) Nreader][2];
		d1_modAB = new int[(int) Ndisease][(int) Nreader][2];

		double ScoreModA;
		double ScoreModB;
		int PresentModA;
		int PresentModB;

		int m, n;
		int k = 0; // reader index
		for (String r : inputFile.keyedData.keySet()) {
			m = 0; // signal-absent case index
			n = 0; // signal-present case index
			for (String c : inputFile.keyedData.get(r).keySet()) {

				// For all readers and cases, determine which had observations
				if (inputFile.keyedData.get(r).containsKey(c)) {
					if (inputFile.keyedData.get(r).get(c).containsKey(modA)) {
						ScoreModA = inputFile.keyedData.get(r).get(c).get(modA);
						PresentModA = 1;
					} else {
						ScoreModA = -1000000;
						PresentModA = 0;
						fullyCrossed = false;
					}
					if (inputFile.keyedData.get(r).get(c).containsKey(modB)) {
						ScoreModB = inputFile.keyedData.get(r).get(c).get(modB);
						PresentModB = 1;
					} else {
						ScoreModB = -1000000;
						PresentModB = 0;
						fullyCrossed = false;
					}
				} else {
					ScoreModA = -1000000;
					ScoreModB = -1000000;
					PresentModA = 0;
					PresentModB = 0;
					fullyCrossed = false;
				}
				
				// Fill in the score and design matrices
				if (inputFile.truthVals.get(c) == 0) {
					t0_modAB[m][k][0] = ScoreModA;
					t0_modAB[m][k][1] = ScoreModB;
					t0_modAA[m][k][0] = ScoreModA;
					t0_modAA[m][k][1] = ScoreModA;
					t0_modBB[m][k][0] = ScoreModB;
					t0_modBB[m][k][1] = ScoreModB;
					
					d0_modAB[m][k][0] = PresentModA;
					d0_modAB[m][k][1] = PresentModB;
					d0_modAA[m][k][0] = PresentModA;
					d0_modAA[m][k][1] = PresentModA;
					d0_modBB[m][k][0] = PresentModB;
					d0_modBB[m][k][1] = PresentModB;

					m++;
				} else {
					t1_modAB[n][k][0] = ScoreModA;
					t1_modAB[n][k][1] = ScoreModB;
					t1_modAA[n][k][0] = ScoreModA;
					t1_modAA[n][k][1] = ScoreModA;
					t1_modBB[n][k][0] = ScoreModB;
					t1_modBB[n][k][1] = ScoreModB;

					d1_modAB[n][k][0] = PresentModA;
					d1_modAB[n][k][1] = PresentModB;
					d1_modAA[n][k][0] = PresentModA;
					d1_modAA[n][k][1] = PresentModA;
					d1_modBB[n][k][0] = PresentModB;
					d1_modBB[n][k][1] = PresentModB;

					n++;
				}
			}
			k++;
		}
	}
	
	/**
	 * Perform variance analysis with scores, study design, experiment size. <br>
	 * ----Creates BDG moments [AA, BB, AB][8] <br>
	 * ----Creates BDGbiased moments [AA, BB, AB] <br>
	 * ----Creates BDGcoeff based on the current study
	 * 
	 */
	public void calculateCovMRMC(Integer selectedMod) {
		
		double[] M_AA  = new double[9];
		double[] M_BB  = new double[9];
		double[] M_AB  = new double[9];
		double[] Mb_AA = new double[9];
		double[] Mb_BB = new double[9];
		double[] Mb_AB = new double[9];
		double[] C_AA  = new double[9];
		double[] C_BB  = new double[9];
		double[] C_AB  = new double[9];
	
		CovMRMC cov_AA = new CovMRMC(t0_modAA, d0_modAA, t1_modAA, d1_modAA, Nreader, Nnormal, Ndisease);
		CovMRMC cov_BB = new CovMRMC(t0_modBB, d0_modBB, t1_modBB, d1_modBB, Nreader, Nnormal, Ndisease);
		CovMRMC cov_AB = new CovMRMC(t0_modAB, d0_modAB, t1_modAB, d1_modAB, Nreader, Nnormal, Ndisease);

		AUCs = cov_AB.getAUCs();
		AUCsReaderAvg = cov_AB.getAUCsReaderAvg();
		BDG = new double[4][8];
		BDGbias = new double[4][8];
		BDGcoeff = new double[4][8];
		totalVar = 0.0;
		
		if(selectedMod==0) {
			M_AA  = cov_AA.getMoments();
			Mb_AA = cov_AA.getMomentsBiased();
			C_AA  = cov_AA.getCoefficients();
			for(int i=0; i<Nreader; i++) AUCs[i][1] = 0.0;
			AUCsReaderAvg[1] = 0.0;
		}
		if(selectedMod==1) {
			M_BB  = cov_BB.getMoments();
			Mb_BB = cov_BB.getMomentsBiased();
			C_BB  = cov_BB.getCoefficients();
			for(int i=0; i<Nreader; i++) AUCs[i][0] = 0.0;
			AUCsReaderAvg[0] = 0.0;
		}
		if(selectedMod==3) {
			M_AA  = cov_AA.getMoments();
			Mb_AA = cov_AA.getMomentsBiased();
			C_AA  = cov_AA.getCoefficients();
			
			M_BB  = cov_BB.getMoments();
			Mb_BB = cov_BB.getMomentsBiased();
			C_BB  = cov_BB.getCoefficients();
			
			M_AB  = cov_AB.getMoments();
			Mb_AB = cov_AB.getMomentsBiased();
			C_AB  = cov_AB.getCoefficients();
		}
		
		for (int i = 0; i < 8; i++) {
			BDG[0][i] = M_AA[i + 1];
			BDG[1][i] = M_BB[i + 1];
			BDG[2][i] = M_AB[i + 1];
			BDGbias[0][i] = Mb_AA[i + 1];
			BDGbias[1][i] = Mb_BB[i + 1];
			BDGbias[2][i] = Mb_AB[i + 1];
			BDGcoeff[0][i] = C_AA[i + 1];
			BDGcoeff[1][i] = C_BB[i + 1];
			BDGcoeff[2][i] = C_AB[i + 1];
			
			BDGcoeff[3][i] = 1.0;

			BDG[3][i] =     (BDG[0][i] * BDGcoeff[0][i])
					  +     (BDG[1][i] * BDGcoeff[1][i])
					  - 2.0*(BDG[2][i] * BDGcoeff[2][i]);
			
			totalVar += BDGcoeff[3][i] * BDG[3][i];
			
		}
		
		totalVar = totalVar*1.0;
	}
}
