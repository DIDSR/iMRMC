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

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import mrmc.chart.XYPair;
import mrmc.gui.GUInterface;
import mrmc.gui.SizePanel;


/**
 * <b>Flow 1)</b> Object InputFile1 created in {@link mrmc.gui.GUInterface#GUInterface(MRMC, java.awt.Container)} <br>
 * -- {@link mrmc.gui.InputFileCard.brwsButtonListener} calls {@link #ReadInputFile()} <br>
 * <br>
 * <b>Flow 2)</b> Object currInputFile created in {@link roemetz.gui.RMGUInterface.SimExperiments_thread#doInBackground()} <br>
 * -- {@link roemetz.gui.RMGUInterface.SimExperiments_thread#doInBackground()} 
 *    calls {@link roemetz.core.SimRoeMetz#doSim(DBRecord)} <br>
 * <br>
 * <ul>
 *   <li> ----KEY FIELDS----
 *   <li> {@link #observerData},
 *   <li> {@link #readerIDs}, {@link #Nreader}, 
 *   <li> {@link #normalIDs}, {@link #Nnormal},
 *   <li> {@link #diseaseIDs}, {@link #Ndisease},
 *   <li> {@link #modalityIDs}, {@link #Nmodality},
 *   <li> {@link #keyedData}, {@link #truthVals},
 *   <li> {@link #caseIDs}
 *   <li> These fields are not private and are accessed directly, like a structure.
 * </ul>
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */

public class InputFile {
	
	/**
	 *  Contains all score data organized as a TreeMap of a fully-crossed study 
	 *  (Reader, (Case, (Modality, Score))), sorted by default
	 */
	TreeMap<String, TreeMap<String, TreeMap<String, Double>>> 
		keyedData = new TreeMap<String, TreeMap<String, TreeMap<String, Double>>>();
	/**
	 * Contains counts for normal and disease cases for each modality and reader
	 */
	public TreeMap<String, TreeMap<String,Integer[]>> casecount= new  TreeMap<String, TreeMap<String,Integer[]>>();       //modality<reader,count[][]>;
	/**
	 * Contains reader, normal cases and disease cases list for each modality
	 */
    final TreeMap<String, TreeMap<String,ArrayList<String>>> modinformation =new  TreeMap<String, TreeMap<String,ArrayList<String>>>();
	/**
	 *  Contains the truth status for each case organized as a TreeMap (Case, Score)
	 */
	TreeMap<String, Integer> 
		truthVals = new TreeMap<String, Integer>();
	/**
	 * The number of readers, normal cases, disease cases, and modalities
	 */
	public long Nreader, Nnormal, Ndisease, Nmodality;
	/**
	 * A sorted tree (list) containing all the readerIDs in the data (IDs of the readers)
	 */
	public TreeMap<String, Integer> readerIDs = new TreeMap<String, Integer>();
	/**
	 * A sorted tree (list) containing all the normalIDs in the data (IDs of normal cases)
	 */
	public TreeMap<String, Integer> normalIDs = new TreeMap<String, Integer>();
	/**
	 * A sorted tree (list) containing all the diseaseIDs in the data (IDs of disease cases)
	 */
	public TreeMap<String, Integer> diseaseIDs = new TreeMap<String, Integer>();
	/**
	 * A sorted tree (list) containing all the caseIDs in the data (IDs of all cases), 
	 * including {@link #normalIDs} and {@link #diseaseIDs}
	 */
	public TreeMap<String, Integer> caseIDs = new TreeMap<String, Integer>();
	/**
	 * A sorted tree (list) containing all the modalityIDs in the data (IDs of the modalities)
	 */
	public TreeMap<String, Integer> modalityIDs = new TreeMap<String, Integer>();
	
	/**
	 * Filename for the .imrmc reader study data
	 */
	public String filename;
	public SizePanel SizePanel1;
	/**
	 * to do
	 */
	private GUInterface GUI;
	private DBRecord DBRecordStat;
	/*
	 * 
	 */
	ArrayList<String> fileContent = new ArrayList<String>();
	
;
	/**
	 * number of lines in {@link #fileContent}
	 */
	int NlinesFileContent;
	/**
	 * keeps track of file position while reading .imrmc input file
	 */
	int filePosition;
	/**
	 * The row being read from .imrmc summary file 
	 */
	int summaryPosition;
	/**
	 * The number of rows in .imrmc reader study data file before "BEGIN SUMMARY:"
	 */
	int beginSummaryPosition = 0;
	/**
	 * The number of rows in .imrmc reader study data file before "END SUMMARY:"
	 */
	int endSummaryPosition = 0;
	/**
	 * The last line of the .imrmc summary data
	 */
	public int NrowsInHeader = 0;	
	/**
	 * The first line of the .imrmc reader study data file acts as the record title
	 */
	public String recordTitle = "Default Record Title";
	/**
	 * The content of .imrmc reader study data file before "BEGIN DATA:"
	 */
	public String fileHeader = "Default Record Title,\n" +
			"All rows of input file before BEGIN DATA:";
	/**
	 * Rows of observation data, each row contains: <br>
	 *   reader_id, case_id, modality_id, score
	 */
	public String[][] observerData;
	

	public boolean isLoaded = false;
	/** 
	 * String describing inconsistencies between header info and actual study info
	 */
	public String dataCheckResults = "";
	/**
	 * Gets completed status of loading input
	 * 
	 * @return True if file has been processed, false otherwise
	 */
	public boolean isLoaded() {
		return isLoaded;
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
		return fileHeader;
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
	public TreeMap<String, TreeMap<String, TreeSet<XYPair>>> generateROCpoints(String[] modchosen) {
		int samples = 100;
		TreeMap<String, TreeMap<String, TreeSet<XYPair>>> allrocPoints = new TreeMap<String, TreeMap<String, TreeSet<XYPair>>>(); 
		for (String mod :modchosen){
			TreeMap<String, TreeSet<XYPair>> rocPoints = new TreeMap<String, TreeSet<XYPair>>();
			double min = getMinScore(mod);
			double max = getMaxScore(mod);
			double inc = (max - min) / samples;
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
					if (normCount!=0 & disCount!=0){          // don't create treemap if there is no data.
						if (rocPoints.containsKey(r)) {
							rocPoints.get(r).add(new XYPair(fpf, tpf));
						} else {
							TreeSet<XYPair> temp = new TreeSet<XYPair>();
							temp.add(new XYPair(fpf, tpf));
							rocPoints.put(r, temp);
						}
					}
				}
			}
			allrocPoints.put(mod, rocPoints);
		}
		return allrocPoints;
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
	public TreeMap<String, TreeSet<XYPair>> generatePooledROC(String[] rocModchosen) {
		int samples = 100;
		TreeMap<String, TreeSet<XYPair>> allpooledCurve = new TreeMap<String, TreeSet<XYPair>>();
		for (String rocMod :rocModchosen){	
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
			allpooledCurve.put(rocMod, pooledCurve);
		}
		return allpooledCurve;
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
	public TreeMap<String,String[][]>  getStudyDesign(String modalityID) {
		String[][] design = new String[(int) Nreader][(int) (Nnormal + Ndisease+1)];
		String[][] readerrelation = new String[(int) Nreader][2];
		String[][] caserelation = new String[(int) (Nnormal + Ndisease)][2];
		TreeMap<String,String[][]> StudyDesignData= new TreeMap<String,String[][]>();
		//String labelreaderID[]=new String[5];
		int r = 0, i = 0;
		for (String readerID : keyedData.keySet()) {
			i = 0;
			design[r][i] = readerID;
			for (String caseID : keyedData.get(readerID).keySet()) {
				if (r==0){
					caserelation[i][0]=Integer.toString(i);
					caserelation[i][1]=caseID;
				}
				i++;
				if (keyedData.get(readerID).get(caseID).get(modalityID) != null) {
					design[r][i] = "true";
				} else {
					design[r][i] = "false";
				}
			}
			readerrelation[r][0]=Integer.toString(r);
			readerrelation[r][1]=readerID;
			r++;
		}
		StudyDesignData.put("readerrelation", readerrelation);
		StudyDesignData.put("caserelation", caserelation);
		StudyDesignData.put("data", design);
		return StudyDesignData;
	}
	
/*	public boolean[][] getStudyDesign(String modalityID) {
		boolean[][] design = new boolean[(int) Nreader][(int) (Nnormal + Ndisease)];
		int r = 0, i = 0;
		for (String readerID : keyedData.keySet()) {
			i = 0;
			for (String caseID : keyedData.get(readerID).keySet()) {
				if (keyedData.get(readerID).get(caseID).get(modalityID) != null) {
					design[r][i] = true;
				} else {
					design[r][i] = false;
				}
				i++;
			}
			r++;
		}
		return design;
	}*/
	
	/**
	 * Constructor for initializing the object. <br>
	 * If reading an .imrmc input file, then {@link #ReadInputFile()}
	 * 
	 */
	public InputFile() {
	
		Nreader = 0;
		Ndisease = 0;
		Nnormal = 0;
		
		recordTitle = "Default Record Title";
		fileHeader = "Default Record Title,\n" +
				"All rows of input file before BEGIN DATA:";
			
	}

	/**
	 * Given .imrmc input file {@link #filename}, create {@link #fileContent}, then <br>
	 * ----{@link #getExperimentSizeFromHeader()} <br>
	 * ----{@link #parseObserverData()} <br>
	 * ----{@link #verifySizesAndGetIDs(boolean)} <br>
	 * ----{@link #processScoresAndTruth(boolean)} <br>
	 * 
	 * @see #dataCheckResults
	 * @see #isLoaded
	 * 
	 * CALLED BY: {@link mrmc.gui.InputFileCard.brwsButtonListener}
	 * 
	 * @throws IOException
	 */
	public void ReadInputFile(GUInterface GUInterface_temp) throws IOException {
	    GUI = GUInterface_temp;
		try {
			//InputStreamReader isr;
			//DataInputStream din;
			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream din = new DataInputStream(fstream);
			InputStreamReader isr = new InputStreamReader(din);
			BufferedReader br = new BufferedReader(isr);
			String strtemp;
			try {
				String inputformat = filename.substring(filename.lastIndexOf(".")+1);
				if (inputformat.equals("csv")||inputformat.equals("imrmc")||inputformat.equals("omrmc")){
					while ((strtemp = br.readLine()) != null) {
						if (strtemp.length()>0 && strtemp.substring(strtemp.length()-2).equals(",,"))
						strtemp = strtemp.substring(0, strtemp.indexOf(",,"));
						if (strtemp.length()>0 && strtemp.substring(strtemp.length()-1).equals(","))
							strtemp = strtemp.substring(0, strtemp.length()-1);
						fileContent.add(strtemp);
					}
				}else{
					JOptionPane.showMessageDialog(GUI.MRMCobject.getFrame(),
							"Please choose .imrmc, .omrmc or .csv format inputfile.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				din.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				System.err.println("read record Error in inputFile.java: "
						+ e.getMessage());
			}

		} catch (Exception e) {
			System.err
					.println("Error reading file" + filename + e.getMessage());
		}
		
			NlinesFileContent = fileContent.size();



			
			// Function determines readerIDs, normalIDs, diseaseIDs, modalityIDs from the data
			// Return holds string indicating inconsistencies between header and data
			// User will be made aware of inconsistencies and header info will be ignored
			if  (GUInterface.selectedInput == GUInterface.DescInputModeImrmc){    // if input raw data
				filePosition = 0;
				getExperimentSizeFromHeader();
				observerData = new String[NlinesFileContent - (filePosition + 1)][4];
				parseObserverData();
				boolean VerboseTrue=true;
				verifySizesAndGetIDs(VerboseTrue);		
				// fills keyedData and truthVals structures with proper values
				processScoresAndTruth(VerboseTrue);
				System.out.println("Input Raw File Successfully Read!");
				isLoaded = true;
			}
			if  (GUInterface.selectedInput == GUInterface.DescInputModeOmrmc){    // if input raw data
				summaryPosition = 0;
				findSummaryBegin();
				findSummaryEnd();				
				DBRecordStat = GUI.DBRecordStat;
				loadSummaryData();
				System.out.println("Input Summary File Successfully Read!");
			}
			

		
	}





	private void findSummaryBegin() throws IOException {
		String toReturn = "";
		String tempstr = fileContent.get(0).toUpperCase();
		int dataloc = tempstr.indexOf("BEGIN SUMMARY");
		while (dataloc != 0) {
			try{
			summaryPosition++;
			tempstr = fileContent.get(summaryPosition).toUpperCase();
			dataloc = tempstr.indexOf("BEGIN SUMMARY");
			}catch (Exception e) {
				toReturn = "ERROR: Can't find BEGIN SUMMARY";
				throw new IOException(toReturn, e);
			}
		}
		beginSummaryPosition = summaryPosition;	
	}

	private void findSummaryEnd() throws IOException {
		// TODO Auto-generated method stub
		String toReturn = "";
		String tempstr = fileContent.get(0).toUpperCase();
		int dataloc = tempstr.indexOf("END SUMMARY");
		while (dataloc != 0) {
			try{
			summaryPosition++;
			tempstr = fileContent.get(summaryPosition).toUpperCase();
			dataloc = tempstr.indexOf("END SUMMARY");
			}catch (Exception e) {
				toReturn = "ERROR: Can't find END SUMMARY";
				throw new IOException(toReturn, e);
			}
		}
		endSummaryPosition = summaryPosition;	
	}

	private void loadSummaryData() throws IOException {
		// TODO Auto-generated method stub
		String toReturn = "";
		DBRecordStat.AUCsReaderAvg = new double[3];
		String tempstr = fileContent.get(beginSummaryPosition+1).toUpperCase();
		try {
		DBRecordStat.Nreader= Integer.valueOf(tempstr.substring(tempstr.lastIndexOf("=")+1).trim());
		DBRecordStat.NreaderDB= Integer.valueOf(tempstr.substring(tempstr.lastIndexOf("=")+1).trim());
		}catch(NumberFormatException e) {
			toReturn = "Found NReaderSize =: Text following is not an integer \n"+tempstr;
			throw new IOException(toReturn);
		}
		tempstr = fileContent.get(beginSummaryPosition+2).toUpperCase();
		try {
		DBRecordStat.Nnormal= Integer.valueOf(tempstr.substring(tempstr.lastIndexOf("=")+1).trim());
		DBRecordStat.NnormalDB= Integer.valueOf(tempstr.substring(tempstr.lastIndexOf("=")+1).trim());
		}catch(NumberFormatException e) {
			toReturn = "Found NReaderSize =: Text following is not an integer \n"+tempstr;
			throw new IOException(toReturn);
		}
		tempstr = fileContent.get(beginSummaryPosition+3).toUpperCase();
		try {
		DBRecordStat.Ndisease= Integer.valueOf(tempstr.substring(tempstr.lastIndexOf("=")+1).trim());
		DBRecordStat.NdiseaseDB= Integer.valueOf(tempstr.substring(tempstr.lastIndexOf("=")+1).trim());
		}catch(NumberFormatException e) {
			toReturn = "Found NReaderSize =: Text following is not an integer \n"+tempstr;
			throw new IOException(toReturn);
		}
		 
		tempstr = fileContent.get(beginSummaryPosition+5).toUpperCase();
		try {
		DBRecordStat.modalityA= tempstr.substring(tempstr.lastIndexOf("=")+1).trim();
		}catch(NumberFormatException e) {
			toReturn = "Fail to load Modality A information \n"+tempstr;
			throw new IOException(toReturn);
		}
		tempstr = fileContent.get(beginSummaryPosition+6).toUpperCase();
		try {
			DBRecordStat.modalityB= tempstr.substring(tempstr.lastIndexOf("=")+1).trim();
		}catch(NumberFormatException e) {
			toReturn = "Fail to load Modality B information \n"+tempstr;
			throw new IOException(toReturn);
		}
	   DBRecordStat.AUCs = new double [(int) DBRecordStat.Nreader][3];
	   for (int i = beginSummaryPosition+7; i < endSummaryPosition+1; i++) {
			tempstr = fileContent.get(i).toUpperCase().trim();
			int loc = tempstr.indexOf("AUC_A =");
			if (loc == 0) {
				try{
					DBRecordStat.AUCsReaderAvg[0]= Double.valueOf(tempstr.substring(tempstr.lastIndexOf("=")+1).trim());
					}catch(NumberFormatException e) {
						toReturn = "Found AUC_A =: Text following is not an number \n"+tempstr;
						throw new IOException(toReturn);
					}
			}
			loc = tempstr.indexOf("AUC_B =");
			if (loc == 0) {
				try{
					DBRecordStat.AUCsReaderAvg[1]= Double.valueOf(tempstr.substring(tempstr.lastIndexOf("=")+1).trim());
					}catch(NumberFormatException e) {
						toReturn = "Found AUC_B =: Text following is not an number \n"+tempstr;
						throw new IOException(toReturn);
					}
			}

			// AUCs
			loc = tempstr.indexOf("READER SPECIFIC AUC");
			if (loc != -1 ) {
				for (int k=2;k<DBRecordStat.Nreader+2;k++){
					tempstr = fileContent.get(i+k).toUpperCase().trim();
					String tempAUCs[] = fileContent.get(i+k).split(",");
					readerIDs.put(tempAUCs[0].trim().toUpperCase(),k-2);
					for (int j = 1; j<tempAUCs.length;j++ ){
						try{
						String AUCsnum = tempAUCs[j];
						DBRecordStat.AUCs[k-2][j-1] = Double.valueOf(AUCsnum.trim());
						}catch (Exception e) {
							toReturn = "ERROR: Invalid input of AUCs";
							toReturn = toReturn + "      row = " +   (i+k+1) + " \n";
							toReturn = toReturn + fileContent.get(i+k) + " \n";

							throw new IOException(toReturn, e);
						}
					}

				}
			}
			// BDG moments
			loc = tempstr.indexOf("MODALITY1(AUC_A)");
			if (loc != -1) {
				String studyinfo[] = fileContent.get(i).split(",");
				for (int j = 1; j<studyinfo.length;j++ ){
					try{
						String tstr = studyinfo[j];
						DBRecordStat.BDG[0][j-1]= Double.valueOf(tstr.trim());
					}catch (Exception e) {
						toReturn = "ERROR: Invalid input of MODALITY1(AUC_A)";
						toReturn = toReturn + "      row = " +   (i+1) + " \n";
						toReturn = toReturn + fileContent.get(i) + " \n";

						throw new IOException(toReturn, e);
					}
				}
			}
			loc = tempstr.indexOf("MODALITY2(AUC_B)");
			if (loc != -1) {
				String studyinfo[] = fileContent.get(i).split(",");
				for (int j = 1; j<studyinfo.length;j++ ){
					try{
						String tstr = studyinfo[j];
						DBRecordStat.BDG[1][j-1]= Double.valueOf(tstr.trim());
					}catch (Exception e) {
						toReturn = "ERROR: Invalid input of MODALITY2(AUC_B)";
						toReturn = toReturn + "      row = " +   (i+1) + " \n";
						toReturn = toReturn + fileContent.get(i) + " \n";
	
						throw new IOException(toReturn, e);
					}
				}
			}
			loc = tempstr.indexOf("COMP PRODUCT");
			if (loc != -1) {
				String studyinfo[] = fileContent.get(i).split(",");
				for (int j = 1; j<studyinfo.length;j++ ){
					try{
						String tstr = studyinfo[j];
						DBRecordStat.BDG[2][j-1]= Double.valueOf(tstr.trim());
					}catch (Exception e) {
						toReturn = "ERROR: Invalid input of COMP PRODUCT";
						toReturn = toReturn + "      row = " +   (i+1) + " \n";
						toReturn = toReturn + fileContent.get(i) + " \n";

						throw new IOException(toReturn, e);
					}
				}
			}
		}		
		DBRecordStat.AUCsReaderAvg[2]=DBRecordStat.AUCsReaderAvg[0]-DBRecordStat.AUCsReaderAvg[1];
		// TODO Auto-generated method stub
		if (DBRecordStat.AUCsReaderAvg[0]!=0 &DBRecordStat.AUCsReaderAvg[1]==0)
			DBRecordStat.inputMod = 0;
		if (DBRecordStat.AUCsReaderAvg[1]!=0 &DBRecordStat.AUCsReaderAvg[0]==0)
			DBRecordStat.inputMod = 1;
		if (DBRecordStat.AUCsReaderAvg[1]!=0 &DBRecordStat.AUCsReaderAvg[0]!=0)
			DBRecordStat.inputMod = 2;
	   
	}
	


	/**
	 * Given {@link #fileContent}, determine <br>
 	 * ----{@link #recordTitle} <br>
 	 * ----{@link #NrowsInHeader} <br>
 	 * ----{@link #fileHeader} <br>
	 * ----{@link #Nreader} <br>
	 * ----{@link #Nnormal} <br>
	 * ----{@link #Ndisease} <br>
	 * ----{@link #Nmodality} <br>
	 * 
	 * @throws IOException 
	 */
	private void getExperimentSizeFromHeader() throws IOException {
		
		recordTitle = fileContent.get(0);
		
		String tempstr = fileContent.get(0).toUpperCase();
		int dataloc = tempstr.indexOf("BEGIN DATA:");
		String toReturn = "";
		while (dataloc != 0) {
			fileHeader = fileHeader + fileContent.get(filePosition) + "\n";
			tempstr = fileContent.get(filePosition).toUpperCase();

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
            try{
               filePosition++;
               tempstr = fileContent.get(filePosition).toUpperCase();
               dataloc = tempstr.indexOf("BEGIN DATA:");
			}catch (Exception e) {
				toReturn = "ERROR: Can't find BEGIN DATA";
				throw new IOException(toReturn, e);
			}
			
		}

		NrowsInHeader = filePosition;
		System.out.println("a total of " + NrowsInHeader + " lines in header");

	}

	/**
	 * Given {@link #fileContent} parse rows following "BEGIN DATA:" into {@link #observerData}
	 * 
	 * @throws IOException
	 */
	private void parseObserverData() throws IOException {
		String[] tempNumbers;

		// parse each line of input into its separate fields (still ordered by line)
		for (int i = 0; i < NlinesFileContent - (filePosition + 1); i++) {
			tempNumbers = fileContent.get((filePosition + 1) + i).split(",");
			try {
				observerData[i][0] = String.valueOf(tempNumbers[0]).replaceAll("\\s",""); // Reader
				observerData[i][1] = String.valueOf(tempNumbers[1]).replaceAll("\\s",""); // Case
				observerData[i][2] = String.valueOf(tempNumbers[2]).replaceAll("\\s",""); // Modality id
				observerData[i][3] = String.valueOf(tempNumbers[3]).replaceAll("\\s",""); // Score
			} catch (Exception e) {
				String toReturn = "ERROR: Invalid input";
				toReturn = toReturn + "      row = " +    (NrowsInHeader+i+2) + " \n";
				toReturn = toReturn + fileContent.get(filePosition+1+i) + " \n";

				throw new IOException(toReturn, e);
			}
		}
	}
	
	/**
	 * Given {@link #observerData}, determine <br>
	 * ----{@link #Nreader}, {@link #readerIDs} <br>
	 * ----{@link #Nnormal}, {@link #normalIDs} <br>
	 * ----{@link #Ndisease}, {@link #diseaseIDs} <br>
	 * ----{@link #Nnormal}, {@link #modalityIDs} <br>
	 * ----{@link #dataCheckResults}<br>
	 * <br>
	 * Determines truth status of cases <br>
	 * Checks for duplicate cases <br>
	 * Checks that cases have truth <br>
	 * Copmpares experiment size from data versus header
	 * 
	 * @param verbose indicates whether or not to write info to the console
	 *            
	 * @throws IOException 
	 */
	public void verifySizesAndGetIDs(boolean verbose)
					throws IOException {
		
		Integer in0=-1, in1=-1, inr=-1, inm=-1;
		
		// Find the rows corresponding to truth
		// Check for duplicate cases
		// Create normalIDs and diseaseIDs
		for (int i = 0; i < observerData.length; i++) {

			if(observerData[i][0] == null) break;

			if ( observerData[i][0].equals("-1")){																 // Load truth lines
				if ( normalIDs.containsKey(observerData[i][1]) || diseaseIDs.containsKey(observerData[i][1]) ){  // Check Duplicate case truth define 
					dataCheckResults = "ERROR: Duplicate case found"                 + " \n";
					dataCheckResults = dataCheckResults + "      row = " + (NrowsInHeader+i+2)+ " \n";
					dataCheckResults = dataCheckResults + "Check for an earlier occurrence:" + " \n";
					dataCheckResults = dataCheckResults + "      readerID = " + observerData[i][0]   + " \n";
					dataCheckResults = dataCheckResults + "      caseID = " + observerData[i][1]     + " \n";
					dataCheckResults = dataCheckResults + "      modalityID = " + observerData[i][2] + " \n";
					
					throw new IOException(dataCheckResults);

				}
				// New normal ID?
				if ( !normalIDs.containsKey(observerData[i][1])  && observerData[i][3].equals("0")) {
					normalIDs.put(observerData[i][1], in0);
				}
				// New disease ID?
				if ( !diseaseIDs.containsKey(observerData[i][1]) && observerData[i][3].equals("1")) {
					diseaseIDs.put(observerData[i][1], in1);
				}
			}
		}
		
		// Find all the rows not corresponding to truth
		// Check that cases have truth, find readers and modalities
		for (int i = 0; i < observerData.length; i++) {

			if(observerData[i][0] == null) break;

			if ( !observerData[i][0].equals("-1")){

				if ( !normalIDs.containsKey(observerData[i][1]) && !diseaseIDs.containsKey(observerData[i][1]) ){
					dataCheckResults = "ERROR: No truth for case"                     + " \n";
					dataCheckResults = dataCheckResults + "      row = " + (NrowsInHeader+i+2) + " \n";
					dataCheckResults = dataCheckResults + "      readerID = " + observerData[i][0]   + " \n";
					dataCheckResults = dataCheckResults + "      caseID = " + observerData[i][1]     + " \n";
					dataCheckResults = dataCheckResults + "      modalityID = " + observerData[i][2] + " \n";
					
					throw new IOException(dataCheckResults);

				}
				// New reader ID?
				if ( !readerIDs.containsKey(observerData[i][0])  ) {
					readerIDs.put(observerData[i][0], inr);					
				}
				// New modality ID?
				if ( !modalityIDs.containsKey(observerData[i][2])) {
					modalityIDs.put(observerData[i][2], inm);			
					casecount.put(observerData[i][2], new TreeMap<String, Integer[]>());
				}
				if (!casecount.get(observerData[i][2]).containsKey(observerData[i][0])) {
					Integer[] startnum={0,0};
					casecount.get(observerData[i][2]).put(observerData[i][0],startnum);		
				}
				if( normalIDs.containsKey(observerData[i][1]) ) {
					Integer[] countnum = casecount.get(observerData[i][2]).get(observerData[i][0]);
					countnum[0]++;
					casecount.get(observerData[i][2]).put(observerData[i][0],countnum);									
				}
				if( diseaseIDs.containsKey(observerData[i][1]) ) {
					Integer[] countnum = casecount.get(observerData[i][2]).get(observerData[i][0]);
					countnum[1]++;
					casecount.get(observerData[i][2]).put(observerData[i][0],countnum);									
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
			dataCheckResults = dataCheckResults + "NR Given = " + Nreader + " NR Found = "
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
			dataCheckResults = dataCheckResults + "N0 Given = " + Nnormal + " N0 Found = "
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
			dataCheckResults = dataCheckResults + "N1 Given = " + Ndisease + " N1 Found = "
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
			dataCheckResults = dataCheckResults + "NM Given = " + Nmodality + " NM Found = "
					+ (modalityIDs.size());
			Nmodality = modalityIDs.size();
		}
		
		if(verbose) {
			System.out.println("caseIDs: " + caseIDs);
		}
		String misscasemessage="";
		int messagecount=0;
		for (String m : modalityIDs.keySet()){
			for(String r : readerIDs.keySet()){
				Integer[] countnum = casecount.get(m).get(r);
				if (countnum==null||Math.min(countnum[0],countnum[1])<2){
					misscasemessage = misscasemessage + "Reader: "+r+" reads less than 2 normal or disease cases in "+"Modality: "+m+"\n";
					messagecount++;
				}
			}
		}
		if (messagecount>0){
			misscasemessage = misscasemessage + "We ingore scores in these conditions";
		    JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(
					frame,misscasemessage, 
					"Not fully data loaded", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * Maps {@link #observerData} to the core data structures <br>
	 * ----{@link #keyedData} <br>
	 * ----{@link #truthVals} <br>
	 * Also creates {@link #caseIDs} by combining {@link #normalIDs} and {@link #diseaseIDs}<br>
	 * Also checks for duplicate observations 
	 * 
	 * @param verbose indicates whether or not to write info to the console
	 * 
	 * @throws IOException 
	 */
	public void processScoresAndTruth(boolean verbose) 
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
		
		for (String m : modalityIDs.keySet()) {
			ArrayList<String> readerlist = new ArrayList<String>();
			ArrayList<String> normallist = new ArrayList<String>();
			ArrayList<String> diseaselist = new ArrayList<String>();
			modinformation.put(m, new TreeMap<String,ArrayList<String>>()) ;
			modinformation.get(m).put("reader", readerlist);
			modinformation.get(m).put("normal", normallist);
			modinformation.get(m).put("disease", diseaselist);
		}

		// Determine caseIDs
		Integer ic=0;
		for ( String desc : keyedData.get(keyedData.firstKey()).keySet() ) {
			caseIDs.put(desc, ic++);
		}

		// Populate keyedData with the observed scores
		for (int i = 0; i < observerData.length; i++) {
			
			if(observerData[i][0] == null) break;
			String readerID   = observerData[i][0];
			String caseID     = observerData[i][1];
			String modalityID = observerData[i][2];
			double score = Double.valueOf(observerData[i][3]).doubleValue();
			if (readerID.equals("-1")) {
				truthVals.put(caseID, Integer.valueOf(observerData[i][3]).intValue());
			} else {
				if (keyedData.get(readerID).get(caseID).containsKey(modalityID)	){
					String toReturn = "ERROR: Replicate observation found"    + " \n";
					toReturn = toReturn + "      row = " + (NrowsInHeader+i+2) + " \n";
					toReturn = toReturn + "Check for an earlier occurrence: " + " \n";
					toReturn = toReturn + "      readerID = " + observerData[i][0]   + " \n";
					toReturn = toReturn + "      caseID = " + observerData[i][1]     + " \n";
					toReturn = toReturn + "      modalityID = " + observerData[i][2] + " \n";

					throw new IOException(toReturn);

				}
				else {
					Integer[] countnum = casecount.get(modalityID).get(readerID);
					if (Math.min(countnum[0],countnum[1])>2){
						keyedData.get(readerID).get(caseID).put(modalityID, score);
						if (!modinformation.get(modalityID).get("reader").contains(readerID)){
							modinformation.get(modalityID).get("reader").add(readerID);
						}
						if ( normalIDs.containsKey(caseID) & !modinformation.get(modalityID).get("normal").contains(caseID)){
							modinformation.get(modalityID).get("normal").add(caseID);
						}
						if ( diseaseIDs.containsKey(caseID) & !modinformation.get(modalityID).get("disease").contains(caseID)){
							modinformation.get(modalityID).get("disease").add(caseID);
						}
					}
				}
			}

		}
	}

	public void resetInputFile() {

		observerData = null;
		
		keyedData.clear();
		truthVals.clear();

		Nreader = 0;
		Nnormal = 0;
		Ndisease = 0;
		Nmodality = 0;
		
		resetIDs();

		filename = "";
		fileContent.clear();
		NlinesFileContent = 0;
		filePosition = 0;
		NrowsInHeader = 0;

		recordTitle = "Default Record Title";
		fileHeader = "Default Record Title,\n" +
				"All rows of input file before BEGIN DATA:";
		
		dataCheckResults = "";
		
	}
	
	public void resetIDs() {

		readerIDs.clear();
		caseIDs.clear();
		normalIDs.clear();
		diseaseIDs.clear();
		modalityIDs.clear();

	}

} // Close InputFile class
