/*
 * inputFile.java
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
 *     Handles input of a raw study data file. Parses all information from file, calculates 
 *     various statistics about input data, including ROC information.
 */

package mrmc.core;

import java.util.*;
import java.io.*;

import mrmc.chart.XYPair;

public class inputFile {
	private String filename;
	private String desc = "";
	private String recordTitle = "";
	private int Reader, Normal, Disease, Modality;
	private double[][][] t0, t1, t00, t10, t01, t11;
	private int[][][] d0, d1;
	private boolean isFullyCrossed;
	private boolean verified = false;
	private boolean isLoaded = false;
	private String verifiedNums = "";
	private double[][] BDG = new double[4][8];
	private double[][] BDGbias = new double[4][8];
	private double[][] BDGcoeff = new double[4][8];
	private double[] aucMod = new double[2];
	private TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, Double>>> keyedData = new TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, Double>>>();
	private TreeMap<Integer, Integer> truthVals;

	/* returns whether this inputFile has processed all input data */
	public boolean isLoaded() {
		return isLoaded;
	}

	public boolean getFullyCrossedStatus() {
		return isFullyCrossed;
	}

	public boolean numsVerified() {
		return verified;
	}

	public String showUnverified() {
		return verifiedNums;
	}

	public double[] getaucMod() {
		return aucMod;
	}

	public String getTitle() {
		return recordTitle;
	}

	public String getDesc() {
		return desc;
	}

	public String getFilename() {
		return filename;
	}

	public int getReader() {
		return Reader;
	}

	public int getNormal() {
		return Normal;
	}

	public int getDisease() {
		return Disease;
	}

	public int getModality() {
		return Modality;
	}

	public double[][] getBDG() {
		return BDG;
	}

	public double[][] getBDGbias() {
		return BDGbias;
	}

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

	// get study design that simply checks if data is present at the modality
	// for a reader of any case
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
				}
				i++;
			}
			r++;
		}
		return design;
	}

	// get study design that is separated, compares for normal and
	// disease cases
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

	// this is the constructor for the stand alone application
	// reading a file locally
	public inputFile(String file) throws IOException {
		filename = file;
		ArrayList<String> fileContent = new ArrayList<String>();
		try {
			InputStreamReader isr;
			DataInputStream din;
			FileInputStream fstream = new FileInputStream(filename);
			din = new DataInputStream(fstream);
			isr = new InputStreamReader(din);
			fileContent = readFile(isr);
			din.close();
		} catch (Exception e) {
			System.err
					.println("Error reading file" + filename + e.getMessage());
		}
		organizeData(fileContent);
		isLoaded = true;
	}

	/*
	 * This is the constructor to be used from iRoeMetz for calculating CovMRMC
	 * with simulated experiment data
	 */
	public inputFile(double[][][] t0, double[][][] t1, double[][][] t00,
			double[][][] t01, double[][][] t10, double[][][] t11, int[][][] d0,
			int[][][] d1, int nr, int n0, int n1, String title, String desc) {
		this.t0 = t0;
		this.t1 = t1;
		this.t00 = t00;
		this.t01 = t01;
		this.t10 = t10;
		this.t11 = t11;
		this.d0 = d0;
		this.d1 = d1;
		this.Reader = nr;
		this.Normal = n0;
		this.Disease = n1;
		this.recordTitle = title;
		this.desc = desc;
		this.isFullyCrossed = true;
	}

	public void calculateCovMRMC() {
		covMRMC mod1 = new covMRMC(t00, d0, t10, d1, Reader, Normal, Disease);
		covMRMC mod2 = new covMRMC(t01, d0, t11, d1, Reader, Normal, Disease);
		covMRMC covMod12 = new covMRMC(t0, d0, t1, d1, Reader, Normal, Disease);
		double[] M1 = mod1.getM();
		double[] M2 = mod2.getM();
		double[] Mcov = covMod12.getM();
		double[] Coeff = mod1.getC();
		// double[] Coeff = dbRecord.genBDGCoeff(Reader, Normal, Disease)[0];
		double[] Mb1 = mod1.getMb();
		double[] Mb2 = mod2.getMb();
		double[] Mbcov = covMod12.getMb();

		// System.out.println("Mb1\t");
		// for (int i = 1; i < 9; i++)
		// System.out.println(Mb1[i] + "\t");
		// System.out.println("\n");
		//
		// System.out.println("Mb2\t");
		// for (int i = 1; i < 9; i++)
		// System.out.println(Mb2[i] + "\t");
		// System.out.println("\n");
		//
		// System.out.println("Mbcov\t");
		// for (int i = 1; i < 9; i++)
		// System.out.println(Mbcov[i] + "\t");
		// System.out.println("\n");

		aucMod = covMod12.getaucMod();
		BDG = matrix.setZero(4, 8);
		BDGcoeff = matrix.setZero(4, 8);
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

		// System.out.println("BDGbias\t");
		// for (int i = 0; i < 8; i++)
		// System.out.println(BDGbias[3][i] + "\t");
		// System.out.println("\n");
	}

	/*
	 * performs parsing of data from input file in fileContent, stores in local
	 * variables
	 */
	private void organizeData(ArrayList<String> fileContent) throws IOException {
		recordTitle = fileContent.get(0);
		int totalLine = fileContent.size();
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

		String[] tempNumbers;
		double[][] fData = new double[totalLine - (counter + 1)][4];
		ArrayList<Integer> readerIDs = new ArrayList<Integer>();
		ArrayList<Integer> normalIDs = new ArrayList<Integer>();
		ArrayList<Integer> diseaseIDs = new ArrayList<Integer>();
		ArrayList<Integer> modalityIDs = new ArrayList<Integer>();

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

		verifiedNums = verifyNums(fData, readerIDs, normalIDs, diseaseIDs,
				modalityIDs);

		if (verifiedNums.isEmpty()) {
			verified = true;
		} else {
			verified = false;
		}

		keyedData = new TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, Double>>>();
		truthVals = new TreeMap<Integer, Integer>();

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

		isFullyCrossed = true;
		for (Integer m : modalityIDs) {
			boolean[][] design = getStudyDesign(m);
			for (int i = 0; i < design.length; i++) {
				for (int j = 0; j < design[i].length; j++) {
					if (!design[i][j]) {
						isFullyCrossed = false;
					}
				}
			}
		}
	}

	/* fills matrixes with 0s if data is not present */
	public void getT0T1s(int modality1, int modality2) {
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
					if (keyedData.get(r).get(c).containsKey(modality1)) {
						currScoreMod1 = keyedData.get(r).get(c).get(modality1);
					} else {
						currScoreMod1 = 0;
						mod1Present = 0;
					}
					if (keyedData.get(r).get(c).containsKey(modality2)) {
						currScoreMod2 = keyedData.get(r).get(c).get(modality2);
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

	/*
	 * Verifies that the numbers of readers, cases, and modalities read in
	 * header of file match the actual numbers of them within the file. After
	 * this method is executed, the Reader, Normal, Disease, and Modality class
	 * variables contain these actual values
	 */
	private String verifyNums(double[][] fData, ArrayList<Integer> readerIDs,
			ArrayList<Integer> normalIDs, ArrayList<Integer> diseaseIDs,
			ArrayList<Integer> modalityIDs) {
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

	private ArrayList<String> readFile(InputStreamReader isr) {
		BufferedReader br = new BufferedReader(isr);
		ArrayList<String> content = new ArrayList<String>();
		String strtemp;
		try {
			while ((strtemp = br.readLine()) != null) {
				content.add(strtemp);
			}
		} catch (Exception e) {
			System.err.println("read record Error in inputFile.java: "
					+ e.getMessage());
		}
		return content;
	}
}
