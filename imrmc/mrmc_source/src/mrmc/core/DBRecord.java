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
 * One entry in the database. This class includes all the information on one
 * study. it also includes all the formulas to convert one decomposition to the
 * other The conversion formulas are based on Gallas BD, Bandos A, Samuelson F,
 * Wagner RF. A Framework for random-effects ROC analsysis: Biases with the
 * Bootstrap and other variance estimators�, Commun Stat A-Theory 38(15),
 * 2586-2603 (2009).
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 * @version 2.0b
 */
public class DBRecord {
	private String recordDesc = "";
	private String recordTitle = "";
	private String filename = "";
	private String[] Modality = new String[2];
	private String Task = "";
	private double[][] BDGbias = new double[4][8];
	private double[][] BDG = new double[4][8];
	private double[][] BDGcoeff = new double[4][8];
	private double[][] BCKbias = new double[4][7];
	private double[][] BCK = new double[4][7];
	private double[][] BCKcoeff = new double[4][7];
	private double[][] DBMbias = new double[4][6];
	private double[][] DBM = new double[4][6];
	private double[][] DBMcoeff = new double[4][6];
	private double[][] ORbias = new double[4][6];
	private double[][] OR = new double[4][6];
	private double[][] ORcoeff = new double[4][6];
	private double[][] MS = new double[4][6];
	private double[][] MSbias = new double[4][6];
	private double[][] MScoeff = new double[4][6];
	private double[] AUC = new double[2];
	private int nReader;
	private int nNormal;
	private int nDisease;
	private boolean fullyCrossed;
	private int[][][] mod0StudyDesign;
	private int[][][] mod1StudyDesign;
	private InputFile inputFile;
	private int currentModality0;
	private int currentModality1;

	/**
	 * Gets a description of the task
	 * 
	 * @return String describing task performed
	 */
	public String getTask() {
		return Task;
	}

	/**
	 * Gets description of a particular modality
	 * 
	 * @param i Number of the modality specified
	 * @return String describing the specified modality
	 */
	public String getModality(int i) {
		return Modality[i];
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
	public int getReader() {
		return nReader;
	}

	/**
	 * Gets the number of normal cases in the record
	 * 
	 * @return Number of normal cases
	 */
	public int getNormal() {
		return nNormal;
	}

	/**
	 * Gets the number of disease cases in the record
	 * 
	 * @return Number of disease cases
	 */
	public int getDisease() {
		return nDisease;
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
	public String getRecordDesp() {
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
	 * Gets the study design for modality 0
	 * 
	 * @return Study design for modality 0, where first dimension is readers,
	 *         second dimension is normal cases, and third dimension is disease
	 *         cases. A one at a particular position indicates that the specific
	 *         reader scored both the normal and disease case, and a zero
	 *         indicates otherwise.
	 */
	public int[][][] getMod0StudyDesign() {
		return mod0StudyDesign;
	}

	/**
	 * Gets the study design for modality 1
	 * 
	 * @return Study design for modality 1, where first dimension is readers,
	 *         second dimension is normal cases, and third dimension is disease
	 *         cases. A one at a particular position indicates that the specific
	 *         reader scored both the normal and disease case, and a zero
	 *         indicates otherwise.
	 */
	public int[][][] getMod1StudyDesign() {
		return mod1StudyDesign;
	}

	/**
	 * Constructor for creating a record from the internal database
	 * 
	 * @param fname Filename of the database file
	 * @param componentStrings Array of strings containing component information
	 * @param desp Study info description
	 * @param AUCstr AUCs in string form
	 */
	public DBRecord(String fname, String[] componentStrings,
			ArrayList<String> desp, String AUCstr) {
		int i, j;
		recordTitle = desp.get(0).substring(2);
		filename = fname;
		// Currently all files in DB are fully crossed, but this may change
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

		checkStudyDesign();

		for (i = 0; i < desp.size(); i++) {
			recordDesc = recordDesc + desp.get(i) + "\n";
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
	 * Constructor for creating a record from raw study data input file
	 * 
	 * @param input InputFile containing information about the study
	 * @param currMod0 Which modality within the study we are using as Mod0
	 * @param currMod1 Which modality within the study we are using as Mod1
	 */
	public DBRecord(InputFile input, int currMod0, int currMod1) {
		inputFile = input;
		recordTitle = input.getTitle();
		recordDesc = input.getDesc();
		filename = input.getFilename();
		nReader = input.getReader();
		nNormal = input.getNormal();
		nDisease = input.getDisease();
		AUC = input.getaucMod();
		fullyCrossed = input.getFullyCrossedStatus();
		Modality[0] = "Mod1";
		Modality[1] = "Mod2";
		currentModality0 = currMod0;
		currentModality1 = currMod1;
		Task = "task unspecified";

		checkStudyDesign();

		String[] descLines = recordDesc.split("\n");

		int i = 0;
		while (descLines[i].equals("BEGIN DATA")) {
			String descLine = descLines[i];
			if (descLine.startsWith("*  Modal")
					|| descLine.startsWith("*  MODAL")) {
				String[] tempStr2 = descLine.split(",");
				Modality[0] = tempStr2[1];
				Modality[1] = tempStr2[2];
			}
			if (descLine.startsWith("*  Task")
					|| descLine.startsWith("*  TASK")) {
				String[] tempStr2 = descLine.split(",");
				Task = tempStr2[1];
			}
			i++;
		}

		BDG = inputFile.getBDG();
		BDGbias = inputFile.getBDGbias();

		generateDecompositions();
	}

	/**
	 * Constructor for creating a record from manual input of components of
	 * variance
	 * 
	 * @param components Components of variance entered from GUI
	 * @param whichComp Specifies with decomposition of components are entered
	 * @param Reader Number of readers
	 * @param Normal Number of normal cases
	 * @param Disease Number of disease cases
	 * @param auc AUCs
	 */
	public DBRecord(double[] components, int whichComp, int Reader, int Normal,
			int Disease, double[] auc) {
		AUC = auc;
		nReader = Reader;
		nNormal = Normal;
		nDisease = Disease;

		fullyCrossed = true;

		checkStudyDesign();

		switch (whichComp) {
		case 0: // BDG
			for (int i = 0; i < 4; i++) {
				BDG[i] = components;
				BDGbias[i] = components;
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
				BCK[i] = components;
				BCKbias[i] = components;
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
				DBM[i] = components;
				DBMbias[i] = components;
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
				OR[i] = components;
				ORbias[i] = components;
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

	/**
	 * Defines mod0StudyDesign and mod1StudyDesign fields with design for
	 * current study. If study is fully crossed simply fills study design with
	 * 1's
	 */
	private void checkStudyDesign() {
		if (fullyCrossed) {
			mod0StudyDesign = new int[nReader][nNormal][nDisease];
			for (int m = 0; m < mod0StudyDesign.length; m++) {
				for (int n = 0; n < mod0StudyDesign[m].length; n++) {
					Arrays.fill(mod0StudyDesign[m][n], 1);
				}
			}
			mod1StudyDesign = new int[nReader][nNormal][nDisease];
			for (int m = 0; m < mod1StudyDesign.length; m++) {
				for (int n = 0; n < mod1StudyDesign[m].length; n++) {
					Arrays.fill(mod1StudyDesign[m][n], 1);
				}
			}
		} else {
			mod0StudyDesign = inputFile
					.getStudyDesignSeparated(currentModality0);
			mod1StudyDesign = inputFile
					.getStudyDesignSeparated(currentModality1);
		}
	}

	/**
	 * Derives all decompositions and coefficient matrices from predefined BDG
	 * components and experiment size
	 */
	private void generateDecompositions() {
		if (fullyCrossed) {
			BDGcoeff = genBDGCoeff(nReader, nNormal, nDisease);
			BCKcoeff = genBCKCoeff(nReader, nNormal, nDisease);
		} else {
			BDGcoeff = genBDGCoeff(nReader, nNormal, nDisease, mod0StudyDesign,
					mod1StudyDesign);
			BCKcoeff = genBCKCoeff(nReader, nNormal, nDisease, BDGcoeff[0]);
		}

		DBMcoeff = genDBMCoeff(nReader, nNormal, nDisease);
		ORcoeff = genORCoeff(nReader, nNormal, nDisease);
		MScoeff = genMSCoeff(nReader, nNormal, nDisease);

		BCK = BDG2BCK(BDG);
		DBM = BCK2DBM(BCK, nReader, nNormal, nDisease);
		OR = DBM2OR(0, DBM, nReader, nNormal, nDisease);
		MS = DBM2MS(DBM, nReader, nNormal, nDisease);

		BCKbias = BDG2BCK(BDGbias);
		DBMbias = BCK2DBM(BCKbias, nReader, nNormal, nDisease);
		ORbias = DBM2OR(0, DBMbias, nReader, nNormal, nDisease);
		MSbias = DBM2MS(DBMbias, nReader, nNormal, nDisease);
	}

	/**
	 * Gets the AUCs of the record in String format
	 * 
	 * @param whichMod specifies modality 0, 1, or difference
	 * @return String containing AUCs
	 */
	public String getAUC(int whichMod) {
		DecimalFormat threeDec = new DecimalFormat("0.000");
		threeDec.setGroupingUsed(false);

		String temp = "";
		switch (whichMod) {
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

	/**
	 * Gets the AUCs of the record as a double
	 * 
	 * @param whichMod specifies modality 0, 1, or difference
	 * @return Double representation of AUC
	 */
	public double getAUCinNumber(int whichMod) {
		return AUC[whichMod];
	}

	/**
	 * Gets the experiment sizes in string format
	 * 
	 * @return String containing experiment sizes
	 */
	public String getSizes() {
		return (Integer.toString(nReader) + " Readers,  "
				+ Integer.toString(nNormal) + " Normal cases,  "
				+ Integer.toString(nDisease) + " Disease cases.");
	}

	/**
	 * Gets the experiment sizes in int representation
	 * 
	 * @return Array containing experiment sizes
	 */
	public int[] getSizesInt() {
		int[] c = { nReader, nNormal, nDisease };
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
			BDGTab1[5] = Matrix.scaleVector(BDGc[3], 2);
		}
		for (int i = 0; i < 8; i++) {
			BDGTab1[6][i] = (BDGTab1[0][i] * BDGTab1[1][i])
					+ (BDGTab1[2][i] * BDGTab1[3][i])
					- (BDGTab1[4][i] * BDGTab1[5][i]);
		}
		return BDGTab1;
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
	 * @param NR Number of readers
	 * @param N0 Number of normal cases
	 * @param N1 Number of disease cases
	 * @return Matrix of MS representation of variance components
	 */
	public static double[][] DBM2MS(double[][] DBM, int NR, int N0, int N1) {
		double[][] c = new double[4][6];
		double[][] BAlpha = new double[][] {
				{ 2 * (N0 + N1), 0, 2, (N0 + N1), 0, 1 },
				{ 0, 2 * NR, 2, 0, NR, 1 }, { 0, 0, 0, (N0 + N1), 0, 1 },
				{ 0, 0, 0, 0, NR, 1 }, { 0, 0, 2, 0, 0, 1 },
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
	 * @param NR Number of readers
	 * @param N0 Number of normal cases
	 * @param N1 Number of disease cases
	 * @return Matrix of DBM representation of variance components
	 */
	public static double[][] BCK2DBM(double[][] BCK, int NR, int N0, int N1) {
		double[] c = new double[7];
		double[][] tmp = new double[4][7];
		double[][] tmp1 = new double[4][3];
		double[][] results = new double[4][6];

		c[0] = 1.0 / N0;
		c[1] = 1.0 / N1;
		c[2] = 1.0 / (N0 * N1);
		c[3] = 1.0 / NR;
		c[4] = 1.0 / (N0 * NR);
		c[5] = 1.0 / (N1 * NR);
		c[6] = 1.0 / (N0 * N1 * NR);

		for (int i = 0; i < 4; i++)
			tmp[i] = Matrix.dotProduct(BCK[i], c);

		double[][] alpha = new double[][] { { 0, 1, 0 }, { 0, 1, 0 },
				{ 0, 1, 0 }, { NR, 0, 0 }, { 0, 0, NR }, { 0, 0, NR },
				{ 0, 0, NR } };

		tmp1 = Matrix.multiply(tmp, alpha);

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

	/**
	 * Transforms BDG representation of variance components into BCK
	 * representation of variance components
	 * 
	 * @param BDG Matrix of BDG variance components
	 * @return Matrix of BCK representation of variance components
	 */
	public static double[][] BDG2BCK(double[][] BDG) {
		double[][] c = new double[4][7];
		double[][] BAlpha = new double[][] { { 0, 0, 0, 0, 0, 0, 1, -1 },
				{ 0, 0, 0, 0, 0, 1, 0, -1 }, { 0, 0, 0, 0, 1, -1, -1, 1 },
				{ 0, 0, 0, 1, 0, 0, 0, -1 }, { 0, 0, 1, -1, 0, 0, -1, 1 },
				{ 0, 1, 0, -1, 0, -1, 0, 1 }, { 1, -1, -1, 1, -1, 1, 1, -1 } };
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 7; j++)
				c[i][j] = 0;
		c = Matrix.matrixTranspose(Matrix.multiply(BAlpha,
				Matrix.matrixTranspose(BDG)));

		return c;
	}

	/**
	 * Determines the coefficient matrix for BDG variance components given a
	 * non-fully crossed study design
	 * 
	 * @param NR Number of readers
	 * @param N0 Number of normal cases
	 * @param N1 Number of disease cases
	 * @param mod0design Study design for modality 0
	 * @param mod1design Study design for modality 1
	 * @return Matrix containing coefficients corresponding to BDG variance
	 *         components
	 */
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
		// M1, N*m, N*m'
		for (int r = 0; r < NR; r++) {
			for (int i = 0; i < N0; i++) {
				for (int j = 0; j < N1; j++) {
					nStarM0 += (double) mod0design[r][i][j];
					nStarM1 += (double) mod1design[r][i][j];
					coeffM1 += ((double) mod0design[r][i][j] * (double) mod1design[r][i][j]);
				}
			}
		}
		// M2
		for (int j = 0; j < N1; j++) {
			for (int r = 0; r < NR; r++) {
				double iSum = 0, iprSum = 0;
				for (int i = 0; i < N0; i++) {
					iSum += (double) mod0design[r][i][j];
					iprSum += (double) mod1design[r][i][j];
				}
				coeffM2 += iSum * iprSum;
			}
		}
		// M3
		for (int i = 0; i < N0; i++) {
			for (int r = 0; r < NR; r++) {
				double jSum = 0, jprSum = 0;
				for (int j = 0; j < N1; j++) {
					jSum += (double) mod0design[r][i][j];
					jprSum += (double) mod1design[r][i][j];
				}
				coeffM3 += jSum * jprSum;
			}
		}
		// M4
		for (int r = 0; r < NR; r++) {
			double ijSum = 0, iprJprSum = 0;
			for (int i = 0; i < N0; i++) {
				for (int j = 0; j < N1; j++) {
					ijSum += (double) mod0design[r][i][j];
					iprJprSum += (double) mod1design[r][i][j];
				}
			}
			coeffM4 += ijSum * iprJprSum;
		}
		// M5
		for (int i = 0; i < N0; i++) {
			for (int j = 0; j < N1; j++) {
				double rSum = 0, rprSum = 0;
				for (int r = 0; r < NR; r++) {
					rSum += (double) mod0design[r][i][j];
					rprSum += (double) mod1design[r][i][j];
				}
				coeffM5 += rSum * rprSum;
			}
		}
		// M6
		for (int j = 0; j < N1; j++) {
			double irSum = 0, iprRprSum = 0;
			for (int i = 0; i < N0; i++) {
				for (int r = 0; r < NR; r++) {
					irSum += (double) mod0design[r][i][j];
					iprRprSum += (double) mod1design[r][i][j];
				}
			}
			coeffM6 += irSum * iprRprSum;
		}
		// M7
		for (int i = 0; i < N0; i++) {
			double jrSum = 0, jprRprSum = 0;
			for (int j = 0; j < N1; j++) {
				for (int r = 0; r < NR; r++) {
					jrSum += (double) mod0design[r][i][j];
					jprRprSum += (double) mod1design[r][i][j];
				}
			}
			coeffM7 += jrSum * jprRprSum;
		}
		// M8
		double ijrSum = 0, iprJprRprSum = 0;
		for (int i = 0; i < N0; i++) {
			for (int j = 0; j < N1; j++) {
				for (int r = 0; r < NR; r++) {
					ijrSum += (double) mod0design[r][i][j];
					iprJprRprSum += (double) mod1design[r][i][j];
				}
			}
		}
		coeffM8 = ijrSum * iprJprRprSum;
		double[] cBiased = new double[] { coeffM1, coeffM2, coeffM3, coeffM4,
				coeffM5, coeffM6, coeffM7, coeffM8 };
		double[][] bias2unbias = new double[][] { { 1, 0, 0, 0, 0, 0, 0, 0 },
				{ -1, 1, 0, 0, 0, 0, 0, 0 }, { -1, 0, 1, 0, 0, 0, 0, 0 },
				{ 1, -1, -1, 1, 0, 0, 0, 0 }, { -1, 0, 0, 0, 1, 0, 0, 0 },
				{ 1, -1, 0, 0, -1, 1, 0, 0 }, { 1, 0, -1, 0, -1, 0, 1, 0 },
				{ -1, 1, 1, -1, 1, -1, -1, 1 } };

		double[] cUnbiased = Matrix.multiply(bias2unbias, cBiased);
		cUnbiased = Matrix.scaleVector(cUnbiased, 1.0 / (nStarM0 * nStarM1));
		cUnbiased[7]--;
		c[0] = cUnbiased;
		c[1] = c[0];
		c[2] = c[0];
		c[3] = c[0];
		return c;
	}

	/**
	 * Determines the coefficient matrix for BDG variance components given a
	 * fully crossed study design
	 * 
	 * @param NR Number of readers
	 * @param N0 Number of normal cases
	 * @param N1 Number of disease cases
	 * @return Matrix containing coefficients corresponding to BDG variance
	 *         components
	 */
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

	/**
	 * Determines the coefficient matrix for BCK variance components given a
	 * non-fully crossed study design
	 * 
	 * @param NR Number of readers
	 * @param N0 Number of normal cases
	 * @param N1 Number of disease cases
	 * @param c BDG coefficient matrix
	 * @return Matrix containing coefficients corresponding to BCK variance
	 *         components
	 */
	public static double[][] genBCKCoeff(int NR, int N0, int N1, double[] c) {
		double[][] c2ca = new double[][] {
				{ 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0 },
				{ 1.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0 },
				{ 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 },
				{ 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0 },
				{ 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 } };

		double[] cAlpha = Matrix.multiply(c2ca, c);
		return new double[][] { cAlpha, cAlpha, cAlpha, cAlpha };
	}

	/**
	 * Determines the coefficient matrix for BCK variance components given a
	 * fully crossed study design
	 * 
	 * @param NR Number of readers
	 * @param N0 Number of normal cases
	 * @param N1 Number of disease cases
	 * @return Matrix containing coefficients corresponding to BCK variance
	 *         components
	 */
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

	/**
	 * Determines the coefficient matrix for DBM variance components given a
	 * fully crossed study design
	 * 
	 * @param NR Number of readers
	 * @param N0 Number of normal cases
	 * @param N1 Number of disease cases
	 * @return Matrix containing coefficients corresponding to DBM variance
	 *         components
	 */
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
		c[2] = Matrix.scaleVector(c[0], 0);
		c[3] = Matrix.scaleVector(c[0], 2);
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

	/**
	 * Determines the coefficient matrix for MS variance components given a
	 * fully crossed study design
	 * 
	 * @param NR Number of readers
	 * @param N0 Number of normal cases
	 * @param N1 Number of disease cases
	 * @return Matrix containing coefficients corresponding to MS variance
	 *         components
	 */
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
	 * fully crossed study design
	 * 
	 * @param NR Number of readers
	 * @param N0 Number of normal cases
	 * @param N1 Number of disease cases
	 * @return Matrix containing coefficients corresponding to OR variance
	 *         components
	 */
	public static double[][] genORCoeff(int NR, int N0, int N1) {
		double[][] c = new double[4][6];
		c[0][0] = 1.0 / NR;
		c[0][1] = 0;
		c[0][2] = 0;
		c[0][3] = 1.0 / NR * (NR - 1);
		c[0][4] = 0;
		c[0][5] = 1.0 / NR;
		c[1] = c[0];
		c[2] = Matrix.scaleVector(c[0], 0);
		c[3][0] = 0;
		c[3][1] = 2.0 / NR;
		c[3][2] = -2.0 / NR;
		c[3][3] = 2.0 / NR * (NR - 1);
		c[3][4] = -2.0 / NR * (NR - 1);
		c[3][5] = 2.0 / NR;

		return c;
	}

	/**
	 * Creates temporary DBM matrix to be used in BDG2BM and BDG2OR methods
	 * 
	 * @param BDG Matrix of BDG variance components
	 * @param NR Number of readers
	 * @param N0 Number of normal cases
	 * @param N1 Number of disease cases
	 * @return Matrix of DBM components
	 */
	public double[][] computeTempDBM(double[][] BDG, int NR, int N0, int N1) {
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
				B[i + 4][j] = B25[i][j] / NR;
				B[i + 4][j + 4] = B25[i][j] * (NR - 1.0) / NR;
			}
		double[][] BTheta = new double[][] {
				{ 1.0 / (N0 + N1), 0, -1.0 / (N0 + N1) },
				{ 0, 1.0 / (N0 + N1) / NR, -1.0 / (N0 + N1) / NR },
				{ 0, 0, 1.0 / (N0 + N1) } };

		double b1 = (N0 + N1) * NR / (NR - 1.0);
		double b2 = (N0 + N1 - 1.0) * N1 * NR / (N1 - 1.0) / (N1 - 1.0);
		double b3 = (N0 + N1 - 1.0) * N0 * NR / (N0 - 1.0) / (N0 - 1.0);
		double[][] Bms = new double[][] {
				{ 0, 0, 0, b1, 0, 0, 0, -b1 },
				{ 0, 0, 0, 0, 0, b2, b3, -b2 - b3 },
				{ 0, b2 / (NR - 1.0), b3 / (NR - 1.0), -(b2 + b3) / (NR - 1.0),
						0, -b2 / (NR - 1.0), -b3 / (NR - 1.0),
						(b2 + b3) / (NR - 1.0) } };
		double[][] c = new double[4][3];
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 3; j++)
				c[i][j] = 0;
		c = Matrix.matrixTranspose(Matrix.multiply(
				BTheta,
				Matrix.multiply(Bms,
						Matrix.multiply(B, Matrix.matrixTranspose(BDG)))));

		return c;
	}

	/**
	 * Transforms matrix of BDG variance components into matrix of DBM variance
	 * components
	 * 
	 * @param BDG Matrix of BDG variance components
	 * @param NR Number of readers
	 * @param N0 Number of normal cases
	 * @param N1 Number of disease cases
	 * @return Matrix of DBM variance components
	 */
	public double[][] BDG2DBM(double[][] BDG, int NR, int N0, int N1) {
		double[][] c = new double[4][6];
		double[][] tempDBM = computeTempDBM(BDG, NR, N0, N1);
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

	/**
	 * Transforms matrix of BDG variance components into matrix of OR variance
	 * components
	 * 
	 * @param BDG Matrix of BDG variance components
	 * @param NR Number of readers
	 * @param N0 Number of normal cases
	 * @param N1 Number of disease cases
	 * @return Matrix of OR variance components
	 */
	public double[][] BDG2OR(double[][] BDG, int NR, int N0, int N1) {
		double[][] c = new double[4][6];
		double[][] tempDBM = computeTempDBM(BDG, NR, N0, N1);
		double[][] ThetaOR = new double[][] { { 1, 0, 0 }, { 0, 1, 0 },
				{ 0, 1, 1 } };
		double[][] tempOR = Matrix.matrixTranspose(Matrix.multiply(ThetaOR,
				Matrix.matrixTranspose(tempDBM)));
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

	/**
	 * Transforms matrix of DBM variance components into matrix of OR variance
	 * components, or vice versa
	 * 
	 * @param index Whether the in[][] parameter is DBM or OR components
	 * @param in Matrix of DBM or OR variance components
	 * @param NR Number of readers
	 * @param N0 Number of normal cases
	 * @param N1 Number of disease cases
	 * @return Matrix of OR or DBM variance components
	 */
	public static double[][] DBM2OR(int index, double[][] in, int NR, int N0,
			int N1) {
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
				orVar[i][2] = (dbm[i][1] + dbm[i][2]) / (N0 + N1);
				orVar[i][3] = (dbm[i][1] + dbm[i][4]) / (N0 + N1);
				orVar[i][4] = dbm[i][1] / (N0 + N1);
				orVar[i][5] = (dbm[i][1] + dbm[i][2] + dbm[i][4] + dbm[i][5])
						/ (N0 + N1);

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
				dbm[i][1] = orVar[i][4] * (N0 + N1);
				dbm[i][2] = (orVar[i][2] - orVar[i][4]) * (N0 + N1);
				dbm[i][3] = orVar[i][1];
				dbm[i][4] = (orVar[i][3] - orVar[i][4]) * (N0 + N1);
				dbm[i][5] = (orVar[i][5] - orVar[i][2] - orVar[i][3] + orVar[i][4])
						* (N0 + N1);
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
}
