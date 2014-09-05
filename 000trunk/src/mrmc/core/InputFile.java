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
 * Handles input of a raw study data file<br>
 * <ul>
 *   <li> {@link #getReaderIDs getReaderIDs}, {@link #getNreader() getNreader}, 
 *   <li> {@link #getNormalIDs() getNormalIDs}, {@link #getNnormal() getNnormal},
 *   <li> {@link #getDiseaseIDs() getDiseaseIDs}, {@link #getNdisease() getNdisease},
 *   <li> {@link #getModalityIDs() getModalityIDs}, {@link #getNmodality() getNmodality},
 *   <li> ----KEY FIELDS----
 *   <li> {@link #keyedData}, {@link #truthVals} 
 *   <li> These are not private and are accessed directly, like a structure. Do not modify.
 * </ul>
 * CALLED FROM: {@link mrmc.gui.RawStudyCard.brwsButtonListener} and {@link roemetz.core.SimRoeMetz} <br>
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */

@SuppressWarnings("unused")
public class InputFile {
	
	/**
	 *  Contains all score data organized as a TreeMap of a fully-crossed study 
	 *  (Reader, (Case, (Modality, Score)))
	 */
	TreeMap<String, TreeMap<String, TreeMap<String, Double>>> 
		keyedData = new TreeMap<String, TreeMap<String, TreeMap<String, Double>>>();
	/**
	 *  Contains the truth status for each case organized as a TreeMap (Case, Score)
	 */
	TreeMap<String, Integer> 
		truthVals = new TreeMap<String, Integer>();
		
	/**
	 * Filename for the .imrmc reader study data
	 */
	public String filename;
	/**
	 * The content of .imrmc reader study data file before "BEGIN DATA:"
	 */
	private String Header = "";
	/**
	 * The number of rows in .imrmc reader study data file before "BEGIN DATA:"
	 */
	private int RowsInHeader = 0;	
	/**
	 * The first line of the .imrmc reader study data file acts as the study title
	 */
	private String recordTitle = "";
	
	/**
	 * The number of readers, normal cases, disease cases, and modalities
	 */
	private long Nreader, Nnormal, Ndisease, Nmodality;

	private boolean isFullyCrossed = true;
	private boolean verified = false;
	private boolean isLoaded = false;
	/** 
	 * String describing inconsistencies between header info and actual study info
	 * 
	 * @see #verificationDetails
	 */
	private String verificationDetails = ""; // describes experiment size
	/**
	 * A sorted tree (list) containing all the readerIDs in the data (IDs of the readers)
	 */
	public TreeMap<String, Integer> readerIDs = new TreeMap<String, Integer>();
	/**
	 * A sorted tree (list) containing all the readerIDs in the data (IDs of normal cases)
	 */
	public TreeMap<String, Integer> normalIDs = new TreeMap<String, Integer>();
	/**
	 * A sorted tree (list) containing all the diseaseIDs in the data (IDs of disease cases)
	 */
	public TreeMap<String, Integer> diseaseIDs = new TreeMap<String, Integer>();
	/**
	 * A sorted tree (list) containing all the caseIDs in the data (IDs of all cases)
	 */
	private TreeMap<String, Integer> caseIDs = new TreeMap<String, Integer>();
	/**
	 * A sorted tree (list) containing all the modalityIDs in the data (IDs of the modalities)
	 */
	private TreeMap<String, Integer> modalityIDs = new TreeMap<String, Integer>();

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
	 * Gets whether experiment size from header agree with those found in the data
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
	 * Gets the title of the study
	 * 
	 * @return String with title of the study
	 */
	public String getRecordTitle() {
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
	 * Gets number of readers in the study
	 * 
	 * @return Number of readers
	 */
	public long getNreader() {
		return Nreader;
	}

	/**
	 * Gets the number of normal cases in the study
	 * 
	 * @return Number of normal cases
	 */
	public long getNnormal() {
		return Nnormal;
	}

	/**
	 * Gets the number of disease cases in the study
	 * 
	 * @return Number of disease cases
	 */
	public long getNdisease() {
		return Ndisease;
	}

	/**
	 * Gets the number of modalities in the study
	 * 
	 * @return Number of modalities
	 */
	public long getNmodality() {
		return Nmodality;
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
	 * @param modalityID Modality for which to determine the study design
	 * @return 2-D array of readers by cases
	 */
	public boolean[][] getStudyDesign(String modalityID) {
		boolean[][] design = new boolean[(int) Nreader][(int) (Nnormal + Ndisease)];
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
	 * Reads the input file. <br>
	 * ----Takes lines from .imrmc file and gets experiment information from header. <br> 
	 * ----Determines the {@link #readerIDs}, {@link #normalIDs}, {@link #diseaseIDs} and {@link #modalityIDs}. <br>
	 * ----Compares experiment information in header to that found in the data. <br>
	 * ----Creates {@link #caseIDs} by concatenating {@link #normalIDs} and {@link #diseaseIDs} <br>
	 * ----Creates the core data structures {@link #keyedData} and {@link #truthVals}.  <br>

	 * 
	 * @param fileContent ArrayList of Strings of each line from file
	 * 
	 * @see #recordTitle
	 * @see #verificationDetails
	 * @see #processScoresAndTruth(String[][], boolean)
	 * 
	 * @throws IOException
	 */
	private void organizeData(ArrayList<String> fileContent) throws IOException {
		recordTitle = fileContent.get(0);
		int counter = getExperimentSizeFromHeader(fileContent);
		String[][] fData = parseContent(fileContent, counter);
		boolean VerboseTrue=true;
		
		// Function determines readerIDs, normalIDs, diseaseIDs, modalityIDs from the data
		// Return holds string indicating inconsistencies between header and data
		// User will be made aware of inconsistencies and header info will be ignored
		verificationDetails = verifySizesAndGetIDs(fData, VerboseTrue);
		// Indicates whether or not there were inconsistencies between header and data
		if (verificationDetails.isEmpty()) {
			verified = true;
		} else {
			verified = false;
		}

		// fills keyedData and truthVals structures with proper values
		processScoresAndTruth(fData, VerboseTrue);

		System.out.println("Input File Successfully Read!");
	}

	/**
	 * Creates the core data structures {@link #keyedData} and  {@link #truthVals}. <br>
	 * Creates caseIDs that are ordered with the normal cases first, followed by the disease cases <br>
	 * The structure is (readerIDs(caseIDs(modalityIDs, score))) <br>
	 * Checks for duplicate observations 
	 * 
	 * @param fData Individual scores with reader, case information
	 * 
	 * @see #readerIDs
	 * @see #normalIDs
	 * @see #diseaseIDs
	 * @see #caseIDs
	 * @see #modalityIDs
	 * 
	 * @throws IOException 
	 */
	private void processScoresAndTruth(String[][] fData, boolean verbose) 
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
		if(verbose) {
			System.out.println("caseIDs: " + caseIDs);
		}

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
	 * Parses out reader study observations from .imrmc file (rows following "BEGIN DATA:")
	 * 
	 * @param fileContent ArrayList of each line from file as a String
	 * @param counter Position of where score data begins in file
	 * 
	 * @return 2-D array where first dimension is line number from file, second
	 *         dimension is reader, case, modality, score information
	 *         
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
	 * Parses experiment size data from file header information. <br>
	 * 
	 * @param fileContent (input) = ArrayList of each line from .imrmc file as a String

	 * @return Position in file where header information ends/score data begins
	 * 
	 * @see #Nnormal
	 * @see #Ndisease
	 * @see #Nreader
	 * @see #Nmodality
	 * 
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
					Nnormal = Integer.valueOf(tempstr.substring(3).trim());
				} catch(NumberFormatException e) {
					toReturn = "Found N0: Text following is not an integer \n"+tempstr;
					throw new IOException(toReturn);
				}
			}
			loc = tempstr.indexOf("N1:");
			if (loc != -1) {
				System.out.println("Found N1: in header. N1="+tempstr.substring(3));

				try {
					Ndisease = Integer.valueOf(tempstr.substring(3).trim());
				} catch(NumberFormatException e) {
					toReturn = "Found N1: Text following is not an integer \n"+tempstr;
					throw new IOException(toReturn);
				}
			}
			loc = tempstr.indexOf("NR:");
			if (loc != -1) {
				System.out.println("Found NR: in header. NR="+tempstr.substring(3));

				try {
					Nreader = Integer.valueOf(tempstr.substring(3).trim());
				} catch(NumberFormatException e) {
					toReturn = "Found NR: Text following is not an integer \n"+tempstr;
					throw new IOException(toReturn);
				}
			}
			loc = tempstr.indexOf("NM:");
			if (loc != -1) {
				System.out.println("Found NM: in header. NM="+tempstr.substring(3));

				try {
					Nmodality = Integer.valueOf(tempstr.substring(3).trim());
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
	 * Determines the IDs of all the readers, cases, and modalities. <br>
	 * ----Determines truth status of cases <br>
	 * ----Checks for duplicate cases <br>
	 * ----Checks that cases have truth <br>
	 * ----Compares the number of modalities in the data to that specified in the header <br>
	 * ----Sets Nreader to the number of disease cases in the data <br>
	 * 
	 * @param fData 2-D array where first dimension is line number from file,
	 *            second dimension is reader, case, modality, score information
	 *            
	 * @return String describing inconsistencies between header info and actual
	 *         study info
	 *         
	 * @see #readerIDs
	 * @see #normalIDs
	 * @see #diseaseIDs
	 * @see #modalityIDs
	 * 
	 * @throws IOException 
	 */
	private String verifySizesAndGetIDs(String[][] fData, boolean verbose)
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

		// Given the set of readerIDs,
		// give them an index corresponding to the "keySet" sorted order
		for(String ID : readerIDs.keySet()) { readerIDs.put(ID, ++inr); }
		if(verbose) {
			System.out.println("readerIDs: " + readerIDs);
		}
		// Compare the number of readers in the data to that specified in the header
		// Set Nreader to the number of readers in the data
		if (Nreader != readerIDs.size()) {
			toReturn = toReturn + "NR Given = " + Nreader + " NR Found = "
					+ readerIDs.size() + " \n";
			Nreader = readerIDs.size();
		}
		// Given the set of normalIDs,
		// give them an index corresponding to the "keySet" sorted order
		for(String ID : normalIDs.keySet()) { normalIDs.put(ID, ++in0); }
		if(verbose) {
			System.out.println("normalIDs: " + normalIDs);
		}
		// Compare the number of normal cases in the data to that specified in the header
		// Set Nreader to the number of normal cases in the data
		if (Nnormal != normalIDs.size()) {
			toReturn = toReturn + "N0 Given = " + Nnormal + " N0 Found = "
					+ normalIDs.size() + " \n";
			Nnormal = normalIDs.size();
		}
		// Given the set of diseaseIDs,
		// give them an index corresponding to the "keySet" sorted order
		for(String ID : diseaseIDs.keySet()) { diseaseIDs.put(ID, ++in1); }
		if(verbose) {
			System.out.println("diseaseIDs: " + diseaseIDs);
		}
		// Compare the number of disease cases in the data to that specified in the header
		// Set Nreader to the number of disease cases in the data
		if (Ndisease != diseaseIDs.size()) {
			toReturn = toReturn + "N1 Given = " + Ndisease + " N1 Found = "
					+ diseaseIDs.size() + " \n";
			Ndisease = diseaseIDs.size();
		}
		// Given the set of modalityIDs,
		// give them an index corresponding to the "keySet" sorted order
		for(String ID : modalityIDs.keySet()) { modalityIDs.put(ID, ++inm); }
		if(verbose) {
			System.out.println("modalityIDs: " + modalityIDs);
		}
		// Compare the number of modalities in the data to that specified in the header
		// Set Nreader to the number of disease cases in the data
		if (Nmodality != (modalityIDs.size())) {
			toReturn = toReturn + "NM Given = " + Nmodality + " NM Found = "
					+ (modalityIDs.size());
			Nmodality = modalityIDs.size();
		}
		
		return toReturn;
		
	}

	/**
	 * Creates ArrayList of strings from all lines in the given file
	 * 
	 * @return ArrayList of strings from all lines in the given file
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
	 * Constructor for initializing the object
	 */
	public InputFile() {
	
		Nreader = 0;
		Ndisease = 0;
		Nnormal = 0;
		
	}

	/**
	 * Constructor used for reading in a raw study file. <br>
	 * 
	 * CALLED BY: {@link mrmc.gui.RawStudyCard.brwsButtonListener}
	 * 
	 * 
	 * @see #readFile()
	 * @see #organizeData(ArrayList)
	 * 
	 * @throws IOException
	 */
	public void ReadInputFile() throws IOException {
	
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

		//TODO Create the keyedData from the tMatrices
		System.out.println("Create the keyeData from the tMatrices!");

		this.Nreader = nr;
		this.Nnormal = n0;
		this.Ndisease = n1;
		this.recordTitle = title;
		this.Header = desc;
		this.isFullyCrossed = true;
	
	}

	/*
	 * Constructor for RoeMetz
	 */
	public InputFile(String[][] fData) throws IOException {
		
		this.recordTitle = "SimExp";
		this.Header = "Simulated Experiment";
		
		boolean VerboseFalse = false;
		verifySizesAndGetIDs(fData, VerboseFalse);
		processScoresAndTruth(fData, VerboseFalse);

	}




}
