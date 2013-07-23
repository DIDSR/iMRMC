/**
 * InputFile.java
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
import java.io.*;

import mrmc.chart.XYPair;

/**
 * Handles input of a raw study data file. Parses all information from file,
 * calculates various statistics about input data, including ROC information.
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 * @version 2.0b
 */
public class InputFile {
	private String filename;
	private String desc = "";
	private String recordTitle = "";
	private int Reader, Normal, Disease, Modality;
	private double[][][] t0, t1, t00, t10, t01, t11;
	private int[][][] d0, d1;
	private boolean isFullyCrossed;
	private boolean verified = false;
	private boolean isLoaded = false;
	private String verificationDetails = ""; // describes experiment size
	private double[][] BDG;
	private double[][] BDGbias;
	private double[][] BDGcoeff;
	private double[] aucMod;
	// contains all score data organized as <Reader<Case<Modality, Score>>>
	private TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, Double>>> keyedData = new TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, Double>>>();
	private TreeMap<Integer, Integer> truthVals; // truth status for each case

	/**
	 * Gets completed status of loading input
	 * 
	 * @return True if file has fully been processed, false otherwise
	 */
	public boolean isLoaded() {
		return isLoaded;
	}

	/**
	 * Gets whether input study is fully crossed or not
	 * 
	 * @return True if input study is fully crossed, false otherwise
	 */
	public boolean getFullyCrossedStatus() {
		return isFullyCrossed;
	}

	/**
	 * Gets whether experiment size values have been correctly determined either
	 * from file header or by processing file
	 * 
	 * @return True if correct experiment size is known, false otherwise
	 */
	public boolean numsVerified() {
		return verified;
	}

	/**
	 * Gets a description of experiment size values specified in header of input
	 * that do not match actual experiment size
	 * 
	 * @return String describing mismatched experiment size values, or empty if
	 *         values match
	 */
	public String showUnverified() {
		return verificationDetails;
	}

	/**
	 * Get AUCs for both modalities
	 * 
	 * @return Array containing AUCs for both modalities
	 */
	public double[] getaucMod() {
		return aucMod;
	}

	/**
	 * Gets the title of the study
	 * 
	 * @return String with title of the study
	 */
	public String getTitle() {
		return recordTitle;
	}

	/**
	 * Gets a textual description of the study
	 * 
	 * @return String with description of the study
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * Gets the filename of the raw data file from which the study was loaded
	 * 
	 * @return String containing filename path
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Gets number of readers in the study
	 * 
	 * @return Number of readers
	 */
	public int getReader() {
		return Reader;
	}

	/**
	 * Gets the number of normal cases in the study
	 * 
	 * @return Number of normal cases
	 */
	public int getNormal() {
		return Normal;
	}

	/**
	 * Gets the number of disease cases in the study
	 * 
	 * @return Number of disease cases
	 */
	public int getDisease() {
		return Disease;
	}

	/**
	 * Gets the number of modalities in the study
	 * 
	 * @return Number of modalities
	 */
	public int getModality() {
		return Modality;
	}

	/**
	 * Gets the matrix of BDG variance components
	 * 
	 * @return BDG variance components matrix
	 */
	public double[][] getBDG() {
		return BDG;
	}

	/**
	 * Gets the biased matrix of BDG variance components
	 * 
	 * @return BDG variance components matrix
	 */
	public double[][] getBDGbias() {
		return BDGbias;
	}

	/**
	 * Constructor used for reading in a raw study file locally
	 * 
	 * @param file Name/path of file to be loaded
	 * @throws IOException
	 */
	public InputFile(String file) throws IOException {
		filename = file;
		ArrayList<String> fileContent = new ArrayList<String>();
		try {
			fileContent = readFile();
		} catch (Exception e) {
			System.err
					.println("Error reading file" + filename + e.getMessage());
		}
		organizeData(fileContent);
		isLoaded = true;
	}

	/**
	 * Constructor used to create an InputFile for variance analysis from
	 * simulated experiment data from iRoeMetz
	 * 
	 * @param tMatrices Matrices of score data
	 * @param dMatrices Study design matrices
	 * @param nr Number of readers
	 * @param n0 Number of normal cases
	 * @param n1 Number of disease cases
	 * @param title Simulated study title
	 * @param desc Simulated study description
	 */
	public InputFile(double[][][][] tMatrices, int[][][][] dMatrices, int nr,
			int n0, int n1, String title, String desc) {
		this.t00 = tMatrices[0];
		this.t01 = tMatrices[1];
		this.t10 = tMatrices[2];
		this.t11 = tMatrices[3];
		this.t0 = tMatrices[4];
		this.t1 = tMatrices[5];
		this.d0 = dMatrices[0];
		this.d1 = dMatrices[1];
		this.Reader = nr;
		this.Normal = n0;
		this.Disease = n1;
		this.recordTitle = title;
		this.desc = desc;
		this.isFullyCrossed = true;
	}

	/**
	 * Cycles through all scores in study and determines the maximum score for a
	 * particular modality. Used to determine relative bounds for drawing ROC
	 * curves
	 * 
	 * @param mod The modality for which to find the maximum score.
	 * @return Maximum score found for the given modality
	 */
	public double getMaxScore(int mod) {
		double max = Double.MIN_VALUE;
		for (Integer r : keyedData.keySet()) {
			for (Integer c : keyedData.get(r).keySet()) {
				if (keyedData.get(r).get(c).get(mod) != null) {
					if (keyedData.get(r).get(c).get(mod) > max) {
						max = keyedData.get(r).get(c).get(mod);
					}
				}
			}
		}
		return max;
	}

	/**
	 * Cycles through all scores in study and determines the minimum score for a
	 * particular modality. Used to determine relative bounds for drawing ROC
	 * curves
	 * 
	 * @param mod The modality for which to find the minimum score.
	 * @return Minimum score found for the given modality
	 */
	public double getMinScore(int mod) {
		double min = Double.MAX_VALUE;
		for (Integer r : keyedData.keySet()) {
			for (Integer c : keyedData.get(r).keySet()) {
				if (keyedData.get(r).get(c).get(mod) != null) {
					if (keyedData.get(r).get(c).get(mod) < min) {
						min = keyedData.get(r).get(c).get(mod);
					}
				}
			}
		}
		return min;
	}

	/**
	 * For a given modality, determines XY coordinates of ROC points for each
	 * reader of the study. Moves threshold from minimum to maximum score and
	 * calculates false positive fraction and true positive fraction at each
	 * sample.
	 * 
	 * @param mod Modality for which ROC curves are being determined
	 * @return TreeMap where the key identifies a reader and the corresponding
	 *         value is a set of XY coordinates of ROC points
	 */
	public TreeMap<Integer, TreeSet<XYPair>> generateROCpoints(int mod) {
		int samples = 100;
		double min = getMinScore(mod);
		double max = getMaxScore(mod);
		double inc = (max - min) / samples;
		TreeMap<Integer, TreeSet<XYPair>> rocPoints = new TreeMap<Integer, TreeSet<XYPair>>();
		for (int r = 1; r <= Reader; r++) {
			for (double thresh = min - inc; thresh <= max + inc; thresh += inc) {
				int fp = 0;
				int tp = 0;
				int normCount = 0;
				int disCount = 0;
				for (Integer c : keyedData.get(r).keySet()) {
					if (!keyedData.get(r).get(c).isEmpty()
							&& (keyedData.get(r).get(c).get(mod) != null)) {
						double score = keyedData.get(r).get(c).get(mod);
						int caseTruth = truthVals.get(c);
						if (caseTruth == 0) {
							normCount++;
							if (score > thresh) {
								fp++;
							}
						} else {
							disCount++;
							if (score > thresh) {
								tp++;
							}
						}
					}
				}

				double fpf = (double) fp / normCount;
				double tpf = (double) tp / disCount;
				if (rocPoints.containsKey(r)) {
					rocPoints.get(r).add(new XYPair(fpf, tpf));
				} else {
					TreeSet<XYPair> temp = new TreeSet<XYPair>();
					temp.add(new XYPair(fpf, tpf));
					rocPoints.put(r, temp);
				}
			}
		}
		return rocPoints;
	}

	/**
	 * For a given modality, determines XY coordinates of ROC points for pooled
	 * scores of all readers as one group. Moves threshold from minimum to
	 * maximum score and calculates false positive fraction and true positive
	 * fraction at each sample.
	 * 
	 * @param mod Modality for which ROC curve is being determined
	 * @return Set of XY coordinates of ROC points
	 */
	public TreeSet<XYPair> generatePooledROC(int mod) {
		int samples = 100;
		double min = getMinScore(mod);
		double max = getMaxScore(mod);
		double inc = (max - min) / samples;
		TreeSet<XYPair> pooledCurve = new TreeSet<XYPair>();
		for (double thresh = min - inc; thresh <= max + inc; thresh += inc) {
			int fp = 0;
			int tp = 0;
			int normCount = 0;
			int disCount = 0;
			for (Integer r : keyedData.keySet()) {
				for (Integer c : keyedData.get(r).keySet()) {
					if (!keyedData.get(r).get(c).isEmpty()
							&& (keyedData.get(r).get(c).get(mod) != null)) {
						double score = keyedData.get(r).get(c).get(mod);
						int caseTruth = truthVals.get(c);
						if (caseTruth == 0) {
							normCount++;
							if (score > thresh) {
								fp++;
							}
						} else {
							disCount++;
							if (score > thresh) {
								tp++;
							}
						}
					}
				}
			}
			double fpf = (double) fp / normCount;
			double tpf = (double) tp / disCount;
			pooledCurve.add(new XYPair(fpf, tpf));
		}
		return pooledCurve;
	}

	/**
	 * For each case in the study, determine how many readers scored that case
	 * 
	 * @return Mapping of each case, to the number of readers that scored that
	 *         particular case
	 */
	public TreeMap<Integer, Double> readersPerCase() {
		TreeMap<Integer, Double> rpc = new TreeMap<Integer, Double>();
		for (Integer r : keyedData.keySet()) {
			for (Integer c : keyedData.get(r).keySet()) {
				if (!keyedData.get(r).get(c).isEmpty()) {
					if (rpc.get(c) == null) {
						rpc.put(c, 1.0);
					} else {
						rpc.put(c, rpc.get(c) + 1);
					}
				}
			}
		}
		return rpc;
	}

	/**
	 * For each reader in the study, determine how many cases that reader
	 * scored.
	 * 
	 * @return Mapping of each reader, to the number of cases that the reader
	 *         scored.
	 */
	public TreeMap<Integer, Double> casesPerReader() {
		TreeMap<Integer, Double> cpr = new TreeMap<Integer, Double>();
		for (Integer r : keyedData.keySet()) {
			for (Integer c : keyedData.get(r).keySet()) {
				if (!keyedData.get(r).get(c).isEmpty()) {
					if (cpr.get(r) == null) {
						cpr.put(r, 1.0);
					} else {
						cpr.put(r, cpr.get(r) + 1);
					}
				}
			}
		}
		return cpr;
	}

	/**
	 * Creates two ArrayLists with case numbers of normal cases and case numbers
	 * of disease cases
	 * 
	 * @return ArrayList consisting of two ArrayLists with case numbers
	 */
	public ArrayList<ArrayList<Integer>> getN0N1CaseNums() {
		ArrayList<Integer> diseaseCases = new ArrayList<Integer>();
		ArrayList<Integer> normalCases = new ArrayList<Integer>();
		for (Integer caseNum : keyedData.get(keyedData.firstKey()).keySet()) {
			if (truthVals.get(caseNum) == 0) {
				normalCases.add(caseNum);
			} else {
				diseaseCases.add(caseNum);
			}
		}
		ArrayList<ArrayList<Integer>> toReturn = new ArrayList<ArrayList<Integer>>();
		toReturn.add(normalCases);
		toReturn.add(diseaseCases);
		return toReturn;
	}

	/**
	 * Determines a shallow study design, such that if a reader has scored a
	 * case for a modality, it is marked as true, and false otherwise (no
	 * cross-referencing of normal and disease cases)
	 * 
	 * @param modality Modality for which to determine the study design
	 * @return 2-D array of readers by cases
	 */
	public boolean[][] getStudyDesign(int modality) {
		boolean[][] design = new boolean[Reader][Normal + Disease];
		int r = 0, i = 0;
		for (Integer reader : keyedData.keySet()) {
			i = 0;
			for (Integer caseNum : keyedData.get(reader).keySet()) {
				if (keyedData.get(reader).get(caseNum).get(modality) != null) {
					design[r][i] = true;
				} else {
					design[r][i] = false;
					isFullyCrossed = false;
				}
				i++;
			}
			r++;
		}
		return design;
	}

	/**
	 * Determines the full study design, such that design is fully crossed only
	 * if reader has scored all normal cases against all disease cases
	 * 
	 * @param modality Modality for which to determine the study design
	 * @return 3-D array of readers by normal cases by disease cases
	 */
	public int[][][] getStudyDesignSeparated(int modality) {
		int[][][] design = new int[Reader][Normal][Disease];
		ArrayList<ArrayList<Integer>> n0n1CaseNums = getN0N1CaseNums();
		ArrayList<Integer> normalCases = n0n1CaseNums.get(0);
		ArrayList<Integer> diseaseCases = n0n1CaseNums.get(1);
		int r = 0, i, j;
		for (Integer reader : keyedData.keySet()) {
			i = 0;
			for (Integer normCase : normalCases) {
				j = 0;
				for (Integer disCase : diseaseCases) {
					if ((keyedData.get(reader).get(normCase).get(modality) != null)
							&& (keyedData.get(reader).get(disCase)
									.get(modality) != null)) {
						design[r][i][j] = 1;
					} else {
						design[r][i][j] = 0;
					}
					j++;
				}
				i++;
			}
			r++;
		}
		return design;
	}

	/**
	 * Perform variance analysis with scores, study design, experiment size
	 */
	public void calculateCovMRMC() {
		CovMRMC mod1 = new CovMRMC(t00, d0, t10, d1, Reader, Normal, Disease);
		CovMRMC mod2 = new CovMRMC(t01, d0, t11, d1, Reader, Normal, Disease);
		CovMRMC covMod12 = new CovMRMC(t0, d0, t1, d1, Reader, Normal, Disease);
		double[] M1 = mod1.getMoments();
		double[] M2 = mod2.getMoments();
		double[] Mcov = covMod12.getMoments();
		double[] Coeff = mod1.getC();
		double[] Mb1 = mod1.getBiasedMoments();
		double[] Mb2 = mod2.getBiasedMoments();
		double[] Mbcov = covMod12.getBiasedMoments();

		aucMod = covMod12.getaucMod();
		BDG = new double[4][8];
		BDGbias = new double[4][8];
		BDGcoeff = new double[4][8];
		for (int i = 0; i < 8; i++) {
			BDG[0][i] = M1[i + 1];
			BDG[1][i] = M2[i + 1];
			BDG[2][i] = Mcov[i + 1];
			BDG[3][i] = M1[i + 1] + M2[i + 1] - 2 * Mcov[i + 1];
			BDGbias[0][i] = Mb1[i + 1];
			BDGbias[1][i] = Mb2[i + 1];
			BDGbias[2][i] = Mbcov[i + 1];
			BDGbias[3][i] = Mb1[i + 1] + Mb2[i + 1] - 2 * Mbcov[i + 1];
			BDGcoeff[0][i] = Coeff[i + 1];
			BDGcoeff[1][i] = Coeff[i + 1];
			BDGcoeff[2][i] = Coeff[i + 1];
			BDGcoeff[3][i] = Coeff[i + 1];
		}
	}

	/**
	 * Takes lines of Strings from file and gets experiment information, parses
	 * scores and truth statuses into structures
	 * 
	 * @param fileContent ArrayList of Strings of each line from file
	 * @throws IOException
	 */
	private void organizeData(ArrayList<String> fileContent) throws IOException {
		recordTitle = fileContent.get(0);
		int counter = getExperimentSizeFromHeader(fileContent);
		double[][] fData = parseContent(fileContent, counter);

		ArrayList<Integer> readerIDs = new ArrayList<Integer>();
		ArrayList<Integer> normalIDs = new ArrayList<Integer>();
		ArrayList<Integer> diseaseIDs = new ArrayList<Integer>();
		ArrayList<Integer> modalityIDs = new ArrayList<Integer>();
		verificationDetails = verifySizesAndGetIDs(fData, readerIDs, normalIDs,
				diseaseIDs, modalityIDs);

		if (verificationDetails.isEmpty()) {
			verified = true;
		} else {
			verified = false;
		}

		keyedData = new TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, Double>>>();
		truthVals = new TreeMap<Integer, Integer>();
		// fills keyedData and truthVals structures with proper values
		processScoresAndTruth(fData, readerIDs, normalIDs, diseaseIDs);

		isFullyCrossed = true;
		for (Integer m : modalityIDs) {
			getStudyDesign(m); // sets isFullyCrossed to false if finds missing
								// data
		}
	}

	// puts all scores in keyedData structure, truth status in truthVals
	/**
	 * Categorizes score data into structure organized by reader, normal,
	 * disease, and structure of truth status
	 * 
	 * @param fData Individual scores with reader, case information
	 * @param readerIDs List of each reader
	 * @param normalIDs List of all normal cases
	 * @param diseaseIDs List of all disease cases
	 */
	private void processScoresAndTruth(double[][] fData,
			ArrayList<Integer> readerIDs, ArrayList<Integer> normalIDs,
			ArrayList<Integer> diseaseIDs) {
		for (Integer r : readerIDs) {
			keyedData.put(r, new TreeMap<Integer, TreeMap<Integer, Double>>());
			for (Integer n : normalIDs) {
				keyedData.get(r).put(n, new TreeMap<Integer, Double>());
			}
			for (Integer d : diseaseIDs) {
				keyedData.get(r).put(d, new TreeMap<Integer, Double>());
			}
		}

		for (int i = 0; i < fData.length; i++) {
			int readerId = (int) (fData[i][0]);
			int caseId = (int) (fData[i][1]);
			int modalityIndex = (int) fData[i][2];
			double score = fData[i][3];
			if (readerId == -1) {
				truthVals.put(caseId, (int) fData[i][3]);
			} else {
				keyedData.get(readerId).get(caseId).put(modalityIndex, score);
			}
		}
	}

	/**
	 * Parses out relevant score information from each line of file
	 * 
	 * @param fileContent ArrayList of each line from file as a String
	 * @param counter Position of where score data begins in file
	 * @return 2-D array where first dimension is line number from file, second
	 *         dimension is reader, case, modality, score information
	 * @throws IOException
	 */
	private double[][] parseContent(ArrayList<String> fileContent, int counter)
			throws IOException {
		int totalLine = fileContent.size();
		String[] tempNumbers;
		double[][] fData = new double[totalLine - (counter + 1)][4];

		// parse each line of input into its separate fields (still ordered by
		// line)
		for (int i = 0; i < totalLine - (counter + 1); i++) {
			tempNumbers = fileContent.get((counter + 1) + i).split(",");
			try {
				fData[i][0] = Integer.valueOf(tempNumbers[0]); // Reader
				fData[i][1] = Integer.valueOf(tempNumbers[1]); // Case
				fData[i][2] = Integer.valueOf(tempNumbers[2]); // Modality id
				fData[i][3] = Double.valueOf(tempNumbers[3]); // Score
			} catch (Exception e) {
				throw new IOException("Invalid input at line "
						+ (i + counter + 2), e);
			}
		}
		return fData;
	}

	/**
	 * Parses experiment size data from file header information
	 * 
	 * @param fileContent ArrayList of each line from file as a String
	 * @return Position in file where header information ends/score data begins
	 */
	private int getExperimentSizeFromHeader(ArrayList<String> fileContent) {
		String tempstr = fileContent.get(0).toUpperCase();
		int dataloc = tempstr.indexOf("BEGIN DATA");
		int counter = 0;
		while (dataloc != 0) {
			desc = desc + fileContent.get(counter) + "\n";
			tempstr = fileContent.get(counter).toUpperCase();
			int loc = tempstr.indexOf("N0");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				Normal = Integer.valueOf(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("N1");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				Disease = Integer.valueOf(tempstr.substring(tmploc + 1).trim());
			}
			loc = tempstr.indexOf("NR");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				Reader = Integer.valueOf(tempstr.substring(tmploc + 1).trim());
			}

			loc = tempstr.indexOf("NM");
			if (loc != -1) {
				int tmploc = tempstr.indexOf(":");
				Modality = Integer
						.valueOf(tempstr.substring(tmploc + 1).trim());
			}
			counter++;
			tempstr = fileContent.get(counter).toUpperCase();
			dataloc = tempstr.indexOf("BEGIN DATA");
		}
		System.out.println("a total of " + counter + " lines in header");
		return counter;
	}

	/*
	 * Verifies that the numbers of readers, cases, and modalities read in
	 * header of file match the actual numbers of them within the file. After
	 * this method is executed, the Reader, Normal, Disease, and Modality class
	 * variables contain these actual values and ArrayLists contain individual
	 * IDs
	 */
	/**
	 * Verifies that the numbers of readers, cases, and modalities read in
	 * header of file match the actual numbers determined in the study.
	 * 
	 * @param fData 2-D array where first dimension is line number from file,
	 *            second dimension is reader, case, modality, score information
	 * @param readerIDs List of all readers
	 * @param normalIDs List of all normal cases
	 * @param diseaseIDs List of all disease cases
	 * @param modalityIDs List of all modalities
	 * @return String describing inconsistencies between header info and actual
	 *         study info
	 */
	private String verifySizesAndGetIDs(double[][] fData,
			ArrayList<Integer> readerIDs, ArrayList<Integer> normalIDs,
			ArrayList<Integer> diseaseIDs, ArrayList<Integer> modalityIDs) {
		String toReturn = new String();

		for (int i = 0; i < fData.length; i++) {
			if ((!readerIDs.contains((int) fData[i][0])) && (fData[i][0] != -1)) {
				readerIDs.add((int) fData[i][0]);
			}
			if (fData[i][2] == 0 && fData[i][3] == 0) {
				if (!normalIDs.contains(fData[i][1])) {
					normalIDs.add((int) fData[i][1]);
				}
			}
			if (fData[i][2] == 0 && fData[i][3] == 1) {
				if (!diseaseIDs.contains(fData[i][1])) {
					diseaseIDs.add((int) fData[i][1]);
				}
			}
			if (!modalityIDs.contains((int) fData[i][2]) && fData[i][2] != 0) {
				modalityIDs.add((int) fData[i][2]);
			}
		}

		if (Reader != readerIDs.size()) {
			toReturn = toReturn + "NR Given = " + Reader + " NR Found = "
					+ readerIDs.size() + " \n";
			Reader = readerIDs.size();
		}
		if (Normal != normalIDs.size()) {
			toReturn = toReturn + "N0 Given = " + Normal + " N0 Found = "
					+ normalIDs.size() + " \n";
			Normal = normalIDs.size();
		}
		if (Disease != diseaseIDs.size()) {
			toReturn = toReturn + "N1 Given = " + Disease + " N1 Found = "
					+ diseaseIDs.size() + " \n";
			Disease = diseaseIDs.size();
		}
		if (Modality != (modalityIDs.size())) {
			toReturn = toReturn + "NM Given = " + Modality + " NM Found = "
					+ (modalityIDs.size());
			Modality = modalityIDs.size();
		}
		return toReturn;
	}

	/**
	 * Creates list of all lines in the given file as Strings
	 * 
	 * @return ArrayList of all lines in the file
	 * @throws FileNotFoundException
	 */
	private ArrayList<String> readFile() throws FileNotFoundException {
		InputStreamReader isr;
		DataInputStream din;
		FileInputStream fstream = new FileInputStream(filename);
		din = new DataInputStream(fstream);
		isr = new InputStreamReader(din);
		BufferedReader br = new BufferedReader(isr);
		ArrayList<String> content = new ArrayList<String>();
		String strtemp;
		try {
			while ((strtemp = br.readLine()) != null) {
				content.add(strtemp);
			}
			din.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			System.err.println("read record Error in inputFile.java: "
					+ e.getMessage());
		}

		return content;
	}

	/* Fills matrices with 0s if data is not present */
	/**
	 * Takes scores of study and organizes them into t-matrices to be used for
	 * variance analysis. Organizes study design into d-matrices
	 * 
	 * @param modality0 Modality to be used as mod 0
	 * @param modality1 Modality to be used as mod 1
	 */
	public void makeTMatrices(int modality0, int modality1) {
		t0 = new double[Normal][Reader][2];
		t1 = new double[Disease][Reader][2];
		t00 = new double[Normal][Reader][2];
		t01 = new double[Normal][Reader][2];
		t10 = new double[Disease][Reader][2];
		t11 = new double[Disease][Reader][2];
		d0 = new int[Normal][Reader][2];
		d1 = new int[Disease][Reader][2];
		int m, n;
		int k = 0;
		for (Integer r : keyedData.keySet()) {
			m = 0; // number of false cases
			n = 0; // number of true cases
			for (Integer c : keyedData.get(r).keySet()) {
				double currScoreMod1;
				double currScoreMod2;
				int mod1Present = 1;
				int mod2Present = 1;
				if (keyedData.get(r).containsKey(c)) {
					if (keyedData.get(r).get(c).containsKey(modality0)) {
						currScoreMod1 = keyedData.get(r).get(c).get(modality0);
					} else {
						currScoreMod1 = 0;
						mod1Present = 0;
					}
					if (keyedData.get(r).get(c).containsKey(modality1)) {
						currScoreMod2 = keyedData.get(r).get(c).get(modality1);
					} else {
						currScoreMod2 = 0;
						mod2Present = 0;
					}
				} else {
					currScoreMod1 = 0;
					currScoreMod2 = 0;
					mod1Present = 0;
					mod2Present = 0;
				}
				if (truthVals.get(c) == 0) {
					t0[m][k][0] = currScoreMod1;
					t0[m][k][1] = currScoreMod2;
					t00[m][k][0] = currScoreMod1;
					t00[m][k][1] = currScoreMod1;
					t01[m][k][0] = currScoreMod2;
					t01[m][k][1] = currScoreMod2;
					d0[m][k][0] = mod1Present;
					d0[m][k][1] = mod2Present;
					m++;
				} else {
					t1[n][k][0] = currScoreMod1;
					t1[n][k][1] = currScoreMod2;
					t10[n][k][0] = currScoreMod1;
					t10[n][k][1] = currScoreMod1;
					t11[n][k][0] = currScoreMod2;
					t11[n][k][1] = currScoreMod2;
					d1[n][k][0] = mod1Present;
					d1[n][k][1] = mod2Present;
					n++;
				}
			}
			k++;
		}
	}

}
