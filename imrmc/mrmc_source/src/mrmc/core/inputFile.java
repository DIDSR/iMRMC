package mrmc.core;

import java.util.*;
import java.io.*;

public class inputFile {
	private String filename;
	private String desc = "";
	private String recordTitle = "";
	private int Reader, Normal, Disease, Modality;
	private double[][][] t0, t1, t01, t11, t02, t12;
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

	public double[][][] generateROCpoints(int mod) {
		int samples = 100;
		double min = getMinScore(mod);
		double max = getMaxScore(mod);
		double inc = (max - min) / samples;
		double[][][] rocPoints = new double[Reader][samples + 2][2];
		for (int r = 1; r <= Reader; r++) {
			int index = 0;
			for (double thresh = min - inc; thresh <= max + inc; thresh += inc) {
				int fp = 0;
				int tp = 0;
				int normCount = 0;
				int disCount = 0;
				for (Integer c : keyedData.get(r).keySet()) {
					if (!keyedData.get(r).get(c).isEmpty()) {
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

				float fpf = (float) fp / normCount;
				float tpf = (float) tp / disCount;
				if (index < (samples + 2)) {
					rocPoints[r - 1][index][0] = fpf;
					rocPoints[r - 1][index][1] = tpf;
					System.out.println(r + ": " + fpf+ ", " + tpf + " : " + thresh);
				}
				index++;
			}
		}
		return rocPoints;
	}

	// TODO verify that this is the correct method to determined Pooled Average
	// ROC points
	public double[][] generatePooledROC(int mod) {
		int samples = 100;
		double min = getMinScore(mod);
		double max = getMaxScore(mod);
		double inc = (max - min) / samples;
		double[][] pooledCurve = new double[samples + 2][2];
		int index = 0;
		for (double thresh = min - inc; thresh <= max + inc; thresh += inc) {
			int fp = 0;
			int tp = 0;
			int normCount = 0;
			int disCount = 0;
			for (Integer r : keyedData.keySet()) {
				for (Integer c : keyedData.get(r).keySet()) {
					if (!keyedData.get(r).get(c).isEmpty()) {
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
			float fpf = (float) fp / normCount;
			float tpf = (float) tp / disCount;
			if (index < (samples + 2)) {
				pooledCurve[index][0] = fpf;
				pooledCurve[index][1] = tpf;
			}
			index++;
		}

		return pooledCurve;
	}
	
	public double[][] generateVerticalROC(int mod){
		int samples = 100;
		double min = getMinScore(mod);
		double max = getMaxScore(mod);
		double inc = (max - min) / samples;
		double[][] verticalCurve = new double[samples + 2][2];
		int index = 0;
		
		return verticalCurve;
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

	public boolean[][] getStudyDesign(int modality) {
		boolean[][] design = new boolean[Reader][Normal + Disease];
		int i = 0, j = 0;
		for (Integer r : keyedData.keySet()) {
			j = 0;
			for (Integer c : keyedData.get(r).keySet()) {
				if (keyedData.get(r).get(c).get(modality) != null) {
					design[i][j] = true;
				} else {
					design[i][j] = false;
				}
				j++;
			}
			i++;
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

	public void dotheWork(int modality1, int modality2) {
		getT0T1s(modality1, modality2);
		covMRMC mod1 = new covMRMC(t01, d0, t11, d1, Reader, Normal, Disease);
		covMRMC mod2 = new covMRMC(t02, d0, t12, d1, Reader, Normal, Disease);
		covMRMC covMod12 = new covMRMC(t0, d0, t1, d1, Reader, Normal, Disease);
		double[] M1 = mod1.getM();
		double[] M2 = mod2.getM();
		double[] Mcov = covMod12.getM();
		double[] Coeff = mod1.getC();
		double[] Mb1 = mod1.getMb();
		double[] Mb2 = mod2.getMb();
		double[] Mbcov = covMod12.getMb();

		System.out.println("Mb1\t");
		for (int i = 1; i < 9; i++)
			System.out.println(Mb1[i] + "\t");
		System.out.println("\n");

		System.out.println("Mb2\t");
		for (int i = 1; i < 9; i++)
			System.out.println(Mb2[i] + "\t");
		System.out.println("\n");

		System.out.println("Mbcov\t");
		for (int i = 1; i < 9; i++)
			System.out.println(Mbcov[i] + "\t");
		System.out.println("\n");

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

		System.out.println("BDGbias\t");
		for (int i = 0; i < 8; i++)
			System.out.println(BDGbias[3][i] + "\t");
		System.out.println("\n");
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
	private void getT0T1s(int modality1, int modality2) {
		t0 = new double[Normal][Reader][2];
		t1 = new double[Disease][Reader][2];
		t01 = new double[Normal][Reader][2];
		t11 = new double[Disease][Reader][2];
		t02 = new double[Normal][Reader][2];
		t12 = new double[Disease][Reader][2];
		d0 = new int[Normal][Reader][2];
		d1 = new int[Disease][Reader][2];
		int m, n;
		int k = 0;
		for (Integer r : keyedData.keySet()) {
			m = 0; // number of false cases
			n = 0; // number of true cases
			for (Integer c : keyedData.get(r).keySet()) {
				double currMod1;
				double currMod2;
				if (keyedData.get(r).containsKey(c)) {
					if (keyedData.get(r).get(c).containsKey(modality1)) {
						currMod1 = keyedData.get(r).get(c).get(modality1);
					} else {
						currMod1 = 0;
					}
					if (keyedData.get(r).get(c).containsKey(modality2)) {
						currMod2 = keyedData.get(r).get(c).get(modality2);
					} else {
						currMod2 = 0;
					}
				} else {
					currMod1 = 0;
					currMod2 = 0;
				}
				if (truthVals.get(c) == 0) {
					t0[m][k][0] = currMod1;
					t0[m][k][1] = currMod2;
					t01[m][k][0] = currMod1;
					t01[m][k][1] = currMod1;
					t02[m][k][0] = currMod2;
					t02[m][k][1] = currMod2;
					m++;
				} else {
					t1[n][k][0] = currMod1;
					t1[n][k][1] = currMod2;
					t11[n][k][0] = currMod1;
					t11[n][k][1] = currMod1;
					t12[n][k][0] = currMod2;
					t12[n][k][1] = currMod2;
					n++;
				}
			}
			k++;
		}
		for (int i = 0; i < Reader; i++) {
			for (int j = 0; j < Disease; j++) {
				d1[j][i][0] = 1;
				d1[j][i][1] = 1;
			}
			for (int j = 0; j < Normal; j++) {
				d0[j][i][0] = 1;
				d0[j][i][1] = 1;
			}
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
