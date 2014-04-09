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
import java.awt.Component;
import java.io.*;

import javax.swing.JOptionPane;

import mrmc.chart.XYPair;
import mrmc.gui.GUInterface;


/**
 * Handles input of a raw study data file. Parses all information from file,
 * calculates various statistics about input data, including ROC information.
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */

@SuppressWarnings("unused")
public class InputFile {
	private String filename;
	private String Header = "";
	private int RowsInHeader = 0;	
	private String recordTitle = "";
	private long Reader, Normal, Disease;
	private int Modality;

	private double[][][] t0_modAB, t1_modAB, t0_modAA, t1_modAA, t0_modBB, t1_modBB;
	private int[][][] d0_modAB, d1_modAB, d0_modAA, d1_modAA, d0_modBB, d1_modBB;
	private boolean isFullyCrossed;
	private boolean verified = false;
	private boolean isLoaded = false;
	private String verificationDetails = ""; // describes experiment size
	private double[][] BDG;
	private double[][] BDGbias;
	private double[][] BDGcoeff;
	private double[] aucMod;

	// contains all score data organized as <Reader<Case<Modality, Score>>>
	private TreeMap<String, TreeMap<String, TreeMap<String, Double>>> keyedData;
	// truth status for each case
	private TreeMap<String, Integer> truthVals;

	private TreeMap<String, Integer> readerIDs = new TreeMap<String, Integer>();
	private TreeMap<String, Integer> normalIDs = new TreeMap<String, Integer>();
	private TreeMap<String, Integer> diseaseIDs = new TreeMap<String, Integer>();
	private TreeMap<String, Integer> caseIDs = new TreeMap<String, Integer>();
	private TreeMap<String, Integer> modalityIDs = new TreeMap<String, Integer>();
	//readerIDs, normalIDs, diseaseIDs, modalityIDs;

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
		return Header;
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
	public long getReader() {
		return Reader;
	}

	/**
	 * Gets the number of normal cases in the study
	 * 
	 * @return Number of normal cases
	 */
	public long getNormal() {
		return Normal;
	}

	/**
	 * Gets the number of disease cases in the study
	 * 
	 * @return Number of disease cases
	 */
	public long getDisease() {
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
	 * Gets readers in the study
	 * 
	 * @return readerIDs
	 */
	public ArrayList<String> getReaderIDs() {
		
		ArrayList<String> desc = new ArrayList<String>();
		for(String desc_temp : readerIDs.keySet() ) {
			desc.add(desc_temp);
		}

		return desc;

	}

	/**
	 * Gets normal cases in the study
	 * 
	 * @return normalIDs
	 */
	public ArrayList<String> getNormalIDs() {
		
		ArrayList<String> desc = new ArrayList<String>();
		for(String desc_temp : normalIDs.keySet() ) {
			desc.add(desc_temp);
		}

		return desc;

	}

	/**
	 * Gets disease cases in the study
	 * 
	 * @return diseaseIDs
	 */
	public ArrayList<String> getDiseaseIDs() {
		
		ArrayList<String> desc = new ArrayList<String>();
		for(String desc_temp : diseaseIDs.keySet() ) {
			desc.add(desc_temp);
		}

		return desc;

	}

	/**
	 * Gets modalities in the study
	 * 
	 * @return modalityIDs
	 */
	public ArrayList<String> getModalityIDs() {
		
		ArrayList<String> desc = new ArrayList<String>();
		for(String desc_temp : modalityIDs.keySet() ) {
			desc.add(desc_temp);
		}

		return desc;
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
	 * Gets the matrix of coefficients of BDG variance components
	 * 
	 * @return coefficients of BDG variance components matrix
	 */
	public double[][] getBDGcoeff() {
		return BDGcoeff;
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
	 * @throws IOException 
	 */
	public InputFile(double[][][][] tMatrices, int[][][][] dMatrices, long nr,
			long n0, long n1, String title, String desc) throws IOException {
		this.t0_modAA = tMatrices[0];
		this.t0_modBB = tMatrices[1];
		this.t1_modAA = tMatrices[2];
		this.t1_modBB = tMatrices[3];
		this.t0_modAB = tMatrices[4];
		this.t1_modAB = tMatrices[5];
		
		this.d0_modAA = dMatrices[0];
		this.d0_modBB = dMatrices[0];
		this.d0_modAB = dMatrices[0];
		this.d1_modAA = dMatrices[1];
		this.d1_modBB = dMatrices[1];
		this.d1_modAB = dMatrices[1];
		
		this.Reader = nr;
		this.Normal = n0;
		this.Disease = n1;
		this.recordTitle = title;
		this.Header = desc;
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
	public double getMaxScore(String mod) {
		double max = Double.MIN_VALUE;
		for (String r : keyedData.keySet()) {
			for (String c : keyedData.get(r).keySet()) {
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
	public double getMinScore(String mod) {
		double min = Double.MAX_VALUE;
		for (String r : keyedData.keySet()) {
			for (String c : keyedData.get(r).keySet()) {
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
	public TreeMap<String, TreeSet<XYPair>> generateROCpoints(String mod) {
		int samples = 100;
		double min = getMinScore(mod);
		double max = getMaxScore(mod);
		double inc = (max - min) / samples;
		TreeMap<String, TreeSet<XYPair>> rocPoints = new TreeMap<String, TreeSet<XYPair>>();
		for (String r : keyedData.keySet()) {
			for (double thresh = min - inc; thresh <= max + inc; thresh += inc) {
				int fp = 0;
				int tp = 0;
				int normCount = 0;
				int disCount = 0;
				for (String c : keyedData.get(r).keySet()) {
					if (!keyedData.get(r).get(c).isEmpty()
							&& (keyedData.get(r).get(c).get(mod) != null )) {
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
	 * @param rocMod Modality for which ROC curve is being determined
	 * @return Set of XY coordinates of ROC points
	 */
	public TreeSet<XYPair> generatePooledROC(String rocMod) {
		int samples = 100;
		double min = getMinScore(rocMod);
		double max = getMaxScore(rocMod);
		double inc = (max - min) / samples;
		TreeSet<XYPair> pooledCurve = new TreeSet<XYPair>();
		for (double thresh = min - inc; thresh <= max + inc; thresh += inc) {
			int fp = 0;
			int tp = 0;
			int normCount = 0;
			int disCount = 0;
			for (String r : keyedData.keySet()) {
				for (String c : keyedData.get(r).keySet()) {
					if (!keyedData.get(r).get(c).isEmpty()
							&& (keyedData.get(r).get(c).get(rocMod) != null)) {
						double score = keyedData.get(r).get(c).get(rocMod);
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
	public TreeMap<String, Double> readersPerCase() {
		TreeMap<String, Double> rpc = new TreeMap<String, Double>();
		for (String r : keyedData.keySet()) {
			for (String c : keyedData.get(r).keySet()) {
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
	public TreeMap<String, Double> casesPerReader() {
		TreeMap<String, Double> cpr = new TreeMap<String, Double>();
		for (String r : keyedData.keySet()) {
			for (String c : keyedData.get(r).keySet()) {
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
	public ArrayList<ArrayList<String>> getN0N1CaseNums() {
		ArrayList<ArrayList<String>> toReturn = new ArrayList<ArrayList<String>>();
		toReturn.add(getNormalIDs());
		toReturn.add(getDiseaseIDs());
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
	public boolean[][] getStudyDesign(String modalityID) {
		boolean[][] design = new boolean[(int) Reader][(int) (Normal + Disease)];
		int r = 0, i = 0;
		for (String readerID : keyedData.keySet()) {
			i = 0;
			for (String caseID : keyedData.get(readerID).keySet()) {
				if (keyedData.get(readerID).get(caseID).get(modalityID) != null) {
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
	 * @param currentModality0 Modality for which to determine the study design
	 * @return 3-D array of readers by normal cases by disease cases
	 */
	public int[][][] getStudyDesignSeparated(String currentModality0) {
		int[][][] design = new int[(int) Reader][(int) Normal][(int) Disease];
		ArrayList<ArrayList<String>> n0n1CaseNums = getN0N1CaseNums();
		ArrayList<String> normalCases = n0n1CaseNums.get(0);
		ArrayList<String> diseaseCases = n0n1CaseNums.get(1);
		int r = 0, i, j;
		for (String reader : keyedData.keySet()) {
			i = 0;
			for (String normCase : normalCases) {
				j = 0;
				for (String disCase : diseaseCases) {
					if ((keyedData.get(reader).get(normCase).get(currentModality0) != null)
							&& (keyedData.get(reader).get(disCase)
									.get(currentModality0) != null)) {
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
		CovMRMC cov_AA = new CovMRMC(t0_modAA, d0_modAA, t1_modAA, d1_modAA, Reader, Normal, Disease);
		CovMRMC cov_BB = new CovMRMC(t0_modBB, d0_modBB, t1_modBB, d1_modBB, Reader, Normal, Disease);
		CovMRMC cov_AB = new CovMRMC(t0_modAB, d0_modAB, t1_modAB, d1_modAB, Reader, Normal, Disease);
		double[] M_AA = cov_AA.getMoments();
		double[] M_BB = cov_BB.getMoments();
		double[] M_AB = cov_AB.getMoments();
		double[] Mb_AA = cov_AA.getBiasedMoments();
		double[] Mb_BB = cov_BB.getBiasedMoments();
		double[] Mb_AB = cov_AB.getBiasedMoments();
		double[] C_AA = cov_AA.getC();
		double[] C_BB = cov_BB.getC();
		double[] C_AB = cov_AB.getC();

		aucMod = cov_AB.getaucMod();
		BDG = new double[4][8];
		BDGbias = new double[4][8];
		BDGcoeff = new double[3][8];
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
			
			BDG[3][i] = BDGcoeff[0][i]*BDG[0][i]
					  + BDGcoeff[1][i]*BDG[1][i]
					- 2*BDGcoeff[2][i]*BDG[2][i];
			BDGbias[3][i] = BDGcoeff[0][i]*BDGbias[0][i]
					     + BDGcoeff[1][i]*BDGbias[1][i]
					   - 2*BDGcoeff[2][i]*BDGbias[2][i];
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
		String[][] fData = parseContent(fileContent, counter);

		// fills readerIDs, normalIDs, diseaseIDs, modalityIDs
		verificationDetails = verifySizesAndGetIDs(fData);

		if (verificationDetails.isEmpty()) {
			verified = true;
		} else {
			verified = false;
		}

		keyedData = new TreeMap<String, TreeMap<String, TreeMap<String, Double>>>();
		truthVals = new TreeMap<String, Integer>();		
		// fills keyedData and truthVals structures with proper values
		processScoresAndTruth(fData);

		isFullyCrossed = true;
		// check if isFullyCrossed is true
		for (String m : modalityIDs.keySet()) {
			getStudyDesign(m); 
		}
		
		System.out.println("Input File Successfully Read!");
	}

	/**
	 * Create the core data structure keyedData. <br>
	 * keyedData is a TreeMap corresponding to a fully-crossed experiment <br>
	 * the structure is keyedData.readerID.CaseID.(modalityID, score)
	 * 
	 * @param fData Individual scores with reader, case information
	 * @param readerIDs2 List of each reader
	 * @param normalIDs2 List of all normal cases
	 * @param diseaseIDs2 List of all disease cases
	 * @throws IOException 
	 */
	private void processScoresAndTruth(String[][] fData) 
					throws IOException {
		
		// Create a data structure corresponding to a fully-crossed experiment
		for (String r : readerIDs.keySet()) {
			keyedData.put(r, new TreeMap<String, TreeMap<String, Double>>());
			for (String n : normalIDs.keySet()) {
				keyedData.get(r).put(n, new TreeMap<String, Double>());
			}
			for (String d : diseaseIDs.keySet()) {
				keyedData.get(r).put(d, new TreeMap<String, Double>());
			}
		}
		
		Integer ic=0;
		for ( String desc : keyedData.get(keyedData.firstKey()).keySet() ) {
			caseIDs.put(desc, ic++);
		}
		System.out.println("caseIDs: " + caseIDs);

		for (int i = 0; i < fData.length; i++) {
			String readerID   = fData[i][0];
			String caseID     = fData[i][1];
			String modalityID = fData[i][2];
			double score = Double.valueOf(fData[i][3]).doubleValue();
			if (readerID.equals("-1")) {
				truthVals.put(caseID, Integer.valueOf(fData[i][3]).intValue());
			} else {
				if (keyedData.get(readerID).get(caseID).containsKey(modalityID)	){
					String toReturn = "ERROR: Replicate observation found"    + " \n";
					toReturn = toReturn + "      row = " + (RowsInHeader+i+2) + " \n";
					toReturn = toReturn + "Check for an earlier occurrence: " + " \n";
					toReturn = toReturn + "      readerID = " + fData[i][0]   + " \n";
					toReturn = toReturn + "      caseID = " + fData[i][1]     + " \n";
					toReturn = toReturn + "      modalityID = " + fData[i][2] + " \n";

					throw new IOException(toReturn);

				}
				else {
					keyedData.get(readerID).get(caseID).put(modalityID, score);
				}
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
	private String[][] parseContent(ArrayList<String> fileContent, int counter)
			throws IOException {
		int totalLine = fileContent.size();
		String[] tempNumbers;
		String[][] fData = new String[totalLine - (counter + 1)][4];

		// parse each line of input into its separate fields (still ordered by
		// line)
		for (int i = 0; i < totalLine - (counter + 1); i++) {
			tempNumbers = fileContent.get((counter + 1) + i).split(",");
			try {
				fData[i][0] = String.valueOf(tempNumbers[0]).replaceAll("\\s",""); // Reader
				fData[i][1] = String.valueOf(tempNumbers[1]).replaceAll("\\s",""); // Case
				fData[i][2] = String.valueOf(tempNumbers[2]).replaceAll("\\s",""); // Modality id
				fData[i][3] = String.valueOf(tempNumbers[3]).replaceAll("\\s",""); // Score
			} catch (Exception e) {
				String toReturn = "ERROR: Invalid input";
				toReturn = toReturn + "      row = " +    (RowsInHeader+i+2) + " \n";
				toReturn = toReturn + fileContent.get(counter+1+i) + " \n";

				throw new IOException(toReturn, e);
			}
		}
		return fData;
	}

	/**
	 * Parses experiment size data from file header information
	 * 		(the number of normal and disease cases, 
	 * 		the number of readers and the number of modalities)
	 * 
	 * @param fileContent (input) = ArrayList of each line from file as a String
	 * @param Normal (public) = the number of normal cases
	 * @param Disease (public) = the number of disease cases
	 * @param Reader (public) = the number of readers
	 * @param Modality (public) = the number of modalities
	 * @return Position in file where header information ends/score data begins
	 * @throws IOException 
	 */
	private int getExperimentSizeFromHeader(ArrayList<String> fileContent) throws IOException {
		String tempstr = fileContent.get(0).toUpperCase();
		int dataloc = tempstr.indexOf("BEGIN DATA:");
		int counter = 0;
		String toReturn = "";
		while (dataloc != 0) {
			Header = Header + fileContent.get(counter) + "\n";
			tempstr = fileContent.get(counter).toUpperCase();

			int loc = tempstr.indexOf("N0:");
			if (loc != -1) {
				System.out.println("Found N0: in header. N0="+tempstr.substring(3));

				try {
					Normal = Integer.valueOf(tempstr.substring(3).trim());
				} catch(NumberFormatException e) {
					toReturn = "Found N0: Text following is not an integer \n"+tempstr;
					throw new IOException(toReturn);
				}
			}
			loc = tempstr.indexOf("N1:");
			if (loc != -1) {
				System.out.println("Found N1: in header. N1="+tempstr.substring(3));

				try {
					Disease = Integer.valueOf(tempstr.substring(3).trim());
				} catch(NumberFormatException e) {
					toReturn = "Found N1: Text following is not an integer \n"+tempstr;
					throw new IOException(toReturn);
				}
			}
			loc = tempstr.indexOf("NR:");
			if (loc != -1) {
				System.out.println("Found NR: in header. NR="+tempstr.substring(3));

				try {
					Reader = Integer.valueOf(tempstr.substring(3).trim());
				} catch(NumberFormatException e) {
					toReturn = "Found NR: Text following is not an integer \n"+tempstr;
					throw new IOException(toReturn);
				}
			}
			loc = tempstr.indexOf("NM:");
			if (loc != -1) {
				System.out.println("Found NM: in header. NM="+tempstr.substring(3));

				try {
					Modality = Integer.valueOf(tempstr.substring(3).trim());
				} catch(NumberFormatException e) {
					toReturn = "Found NM: Text following is not an integer \n"+tempstr;
					throw new IOException(toReturn);
				}
			}

			counter++;
			tempstr = fileContent.get(counter).toUpperCase();
			dataloc = tempstr.indexOf("BEGIN DATA:");
		}

		RowsInHeader = counter;
		System.out.println("a total of " + RowsInHeader + " lines in header");

		return counter;
	}

	/**
	 * Verifies that the numbers of readers, cases, and modalities read in
	 * header of file match the actual numbers determined in the study.
	 * 
	 * @param fData 2-D array where first dimension is line number from file,
	 *            second dimension is reader, case, modality, score information
	 * @param readerIDs2 List of all readers
	 * @param normalIDs2 List of all normal cases
	 * @param diseaseIDs2 List of all disease cases
	 * @param modalityIDs2 List of all modalities
	 * @return String describing inconsistencies between header info and actual
	 *         study info
	 * @throws IOException 
	 */
	private String verifySizesAndGetIDs(String[][] fData)
					throws IOException {
		
		String toReturn = new String();
		Integer in0=-1, in1=-1, inr=-1, inm=-1;
		
		// Find the rows corresponding to truth
		// Check for duplicate cases
		// Create normalIDs and diseaseIDs
		for (int i = 0; i < fData.length; i++) {
			if ( fData[i][0].equals("-1")){
				if ( normalIDs.containsKey(fData[i][1]) || diseaseIDs.containsKey(fData[i][1]) ){
					toReturn = "ERROR: Duplicate case found"                 + " \n";
					toReturn = toReturn + "      row = " + (RowsInHeader+i+2)+ " \n";
					toReturn = toReturn + "Check for an earlier occurrence:" + " \n";
					toReturn = toReturn + "      readerID = " + fData[i][0]   + " \n";
					toReturn = toReturn + "      caseID = " + fData[i][1]     + " \n";
					toReturn = toReturn + "      modalityID = " + fData[i][2] + " \n";
					
					throw new IOException(toReturn);

				}
				// New normal ID?
				if ( !normalIDs.containsKey(fData[i][1])  && fData[i][3].equals("0")) {
					normalIDs.put(fData[i][1], in0);
				}
				// New disease ID?
				if ( !diseaseIDs.containsKey(fData[i][1]) && fData[i][3].equals("1")) {
					diseaseIDs.put(fData[i][1], in1);
				}
			}
		}
		
		// Find all the rows not corresponding to truth
		// Check that cases have truth, find readers and modalities
		for (int i = 0; i < fData.length; i++) {
			if ( !fData[i][0].equals("-1")){

				if ( !normalIDs.containsKey(fData[i][1]) && !diseaseIDs.containsKey(fData[i][1]) ){
					toReturn = "ERROR: No truth for case"                     + " \n";
					toReturn = toReturn + "      row = " + (RowsInHeader+i+2) + " \n";
					toReturn = toReturn + "      readerID = " + fData[i][0]   + " \n";
					toReturn = toReturn + "      caseID = " + fData[i][1]     + " \n";
					toReturn = toReturn + "      modalityID = " + fData[i][2] + " \n";
					
					throw new IOException(toReturn);

				}
				// New reader ID?
				if ( !readerIDs.containsKey(fData[i][0])  ) {
					readerIDs.put(fData[i][0], inr);
				}
				// New modality ID?
				if ( !modalityIDs.containsKey(fData[i][2])) {
					modalityIDs.put(fData[i][2], inm);
				}
			}
		}

		//keyedData.get(readerID).get(caseID).containsKey(modalityID)
		//keyedData.get(r).put(n, new TreeMap<String, Double>());
		//for (String r : readerIDs2.keySet())
		

		for(String ID : readerIDs.keySet()) { readerIDs.put(ID, ++inr); }
		System.out.println("readerIDs: " + readerIDs);
		if (Reader != readerIDs.size()) {
			toReturn = toReturn + "NR Given = " + Reader + " NR Found = "
					+ readerIDs.size() + " \n";
			Reader = readerIDs.size();
		}
		for(String ID : normalIDs.keySet()) { normalIDs.put(ID, ++in0); }
		System.out.println("normalIDs: " + normalIDs);
		if (Normal != normalIDs.size()) {
			toReturn = toReturn + "N0 Given = " + Normal + " N0 Found = "
					+ normalIDs.size() + " \n";
			Normal = normalIDs.size();
		}
		for(String ID : diseaseIDs.keySet()) { diseaseIDs.put(ID, ++in1); }
		System.out.println("diseaseIDs: " + diseaseIDs);
		if (Disease != diseaseIDs.size()) {
			toReturn = toReturn + "N1 Given = " + Disease + " N1 Found = "
					+ diseaseIDs.size() + " \n";
			Disease = diseaseIDs.size();
		}
		for(String ID : modalityIDs.keySet()) { modalityIDs.put(ID, ++inm); }
		System.out.println("modalityIDs: " + modalityIDs);
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
		//InputStreamReader isr;
		//DataInputStream din;
		FileInputStream fstream = new FileInputStream(filename);
		DataInputStream din = new DataInputStream(fstream);
		InputStreamReader isr = new InputStreamReader(din);
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

	/**
	 * Takes scores of study and organizes them into t-matrices to be used for
	 * variance analysis. Organizes study design into d-matrices
	 * 
	 * @param modality0 Modality to be used as mod 0
	 * @param modality1 Modality to be used as mod 1
	 */
	public void makeTMatrices(String modality0, String modality1) {
		t0_modAB = new double[(int) Normal][(int) Reader][2];
		t1_modAB = new double[(int) Disease][(int) Reader][2];
		t0_modAA = new double[(int) Normal][(int) Reader][2];
		t0_modBB = new double[(int) Normal][(int) Reader][2];
		t1_modAA = new double[(int) Disease][(int) Reader][2];
		t1_modBB = new double[(int) Disease][(int) Reader][2];
		d0_modAA = new int[(int) Normal][(int) Reader][2];
		d1_modAA = new int[(int) Disease][(int) Reader][2];
		d0_modBB = new int[(int) Normal][(int) Reader][2];
		d1_modBB = new int[(int) Disease][(int) Reader][2];
		d0_modAB = new int[(int) Normal][(int) Reader][2];
		d1_modAB = new int[(int) Disease][(int) Reader][2];
		int m, n;
		int k = 0; // reader index
		for (String r : keyedData.keySet()) {
			m = 0; // false case index
			n = 0; // true cases index
			for (String c : keyedData.get(r).keySet()) {
				double currScoreMod0;
				double currScoreMod1;
				int PresentMod0 = 1;
				int PresentMod1 = 1;
				if (keyedData.get(r).containsKey(c)) {
					if (keyedData.get(r).get(c).containsKey(modality0)) {
						currScoreMod0 = keyedData.get(r).get(c).get(modality0);
					} else {
						currScoreMod0 = 0;
						PresentMod0 = 0;
					}
					if (keyedData.get(r).get(c).containsKey(modality1)) {
						currScoreMod1 = keyedData.get(r).get(c).get(modality1);
					} else {
						currScoreMod1 = 0;
						PresentMod1 = 0;
					}
				} else {
					currScoreMod0 = 0;
					currScoreMod1 = 0;
					PresentMod0 = 0;
					PresentMod1 = 0;
				}
				if (truthVals.get(c) == 0) {
					t0_modAB[m][k][0] = currScoreMod0;
					t0_modAB[m][k][1] = currScoreMod1;
					t0_modAA[m][k][0] = currScoreMod0;
					t0_modAA[m][k][1] = currScoreMod0;
					t0_modBB[m][k][0] = currScoreMod1;
					t0_modBB[m][k][1] = currScoreMod1;
					
					d0_modAB[m][k][0] = PresentMod0;
					d0_modAB[m][k][1] = PresentMod1;
					d0_modAA[m][k][0] = PresentMod0;
					d0_modAA[m][k][1] = PresentMod0;
					d0_modBB[m][k][0] = PresentMod1;
					d0_modBB[m][k][1] = PresentMod1;

					m++;
				} else {
					t1_modAB[n][k][0] = currScoreMod0;
					t1_modAB[n][k][1] = currScoreMod1;
					t1_modAA[n][k][0] = currScoreMod0;
					t1_modAA[n][k][1] = currScoreMod0;
					t1_modBB[n][k][0] = currScoreMod1;
					t1_modBB[n][k][1] = currScoreMod1;

					d1_modAB[n][k][0] = PresentMod0;
					d1_modAB[n][k][1] = PresentMod1;
					d1_modAA[n][k][0] = PresentMod0;
					d1_modAA[n][k][1] = PresentMod0;
					d1_modBB[n][k][0] = PresentMod1;
					d1_modBB[n][k][1] = PresentMod1;

					n++;
				}
			}
			k++;
		}
	}
}
