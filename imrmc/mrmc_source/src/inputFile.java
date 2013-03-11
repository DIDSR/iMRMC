import java.util.*;
import java.io.*;

public class inputFile {
	String filename;
	String desc = "";
	String recordTitle = "";
	int Reader, Normal, Disease, Modality;
	double[][][] t0, t1, t01, t11, t02, t12;
	int[][][] d0, d1;
	int nmod = 2;
	boolean isFullyCrossed = true;
	boolean verified = false;
	boolean isLoaded;
	String verifiedNums = "";
	double[][] BDG = new double[4][8];
	double[][] BDGbias = new double[4][8];
	double[][] BDGcoeff = new double[4][8];
	double[] aucMod = new double[2];
	TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, Double>>> keyedData = new TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, Double>>>();

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

	public TreeMap<Integer, Integer> readersPerCase() {
		TreeMap<Integer, Integer> rpc = new TreeMap<Integer, Integer>();
		for (Integer r : keyedData.keySet()){
			for (Integer c : keyedData.get(r).keySet()){
				if (rpc.get(c) == null){
					rpc.put(c, 1);
				} else {
					rpc.put(c, rpc.get(c)+1);
				}
			}
		}
		return rpc;
	}

	public TreeMap<Integer, Integer> casesPerReader() {
		TreeMap<Integer, Integer> cpr = new TreeMap<Integer, Integer>();
		for (Integer r : keyedData.keySet()) {
			cpr.put(r, keyedData.get(r).size());
		}
		return cpr;
	}

	/*
	 * shows missing data points for a given modality TODO use this in visual
	 * graph
	 */
	public boolean[][] getDataHoles(int modality) {
		boolean[][] holes = new boolean[Reader][Normal + Disease];
		int i = 0, j = 0;
		for (Integer r : keyedData.keySet()) {
			j = 0;
			for (Integer c : keyedData.get(r).keySet()) {
				if (keyedData.get(r).get(c).get(modality) != null) {
					holes[i][j] = true;
				} else {
					holes[i][j] = false;
				}
				j++;
			}
			i++;
		}
		return holes;
	}

	// this is the constructor for the stand alone application
	// reading a file locally

	public inputFile(String file) {
		filename = file;
		isLoaded = false;
		ArrayList<String> fileContent = new ArrayList<String>();
		try {
			InputStreamReader isr;
			DataInputStream din;
			InputStream in;
			FileInputStream fstream = new FileInputStream(filename);
			din = new DataInputStream(fstream);
			isr = new InputStreamReader(din);
			fileContent = readFile(isr);
			din.close();
		} catch (Exception e) {
			System.err
					.println("Error reading file" + filename + e.getMessage());
		}
		dotheWork(fileContent);

	}

	// this is the constructor for Java Applet
	// read data from the text field
	// flag is simply there to enable method overloading
	public inputFile(String content, int flag) {
		ArrayList<String> fileContent = new ArrayList<String>();
		String[] temp = content.split("\n");
		for (int i = 0; i < temp.length; i++)
			fileContent.add(temp[i]);
		dotheWork(fileContent);
	}

	/* TODO: understand all parts of this method and what it does */
	private void dotheWork(ArrayList<String> fileContent) {
		matrix mx = new matrix();
		organizeData(fileContent);
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
		BDG = mx.setZero(4, 8);
		BDGcoeff = mx.setZero(4, 8);
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
		isLoaded = true;
	}

	/*
	 * performs parsing of data from input file in fileContent, stores in local
	 * variables
	 */
	public void organizeData(ArrayList<String> fileContent) {
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
			// TODO update official file format to reflect that
			// NM (Modality is written in)
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
		System.out.println("a total of " + counter);

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
			fData[i][0] = Integer.valueOf(tempNumbers[0]); // Reader
			fData[i][1] = Integer.valueOf(tempNumbers[1]); // Case
			fData[i][2] = Integer.valueOf(tempNumbers[2]); // Modality id
			fData[i][3] = Double.valueOf(tempNumbers[3]); // Score
		}

		System.out.println("filled up fData");

		verifiedNums = verifyNums(fData, readerIDs, normalIDs, diseaseIDs,
				modalityIDs);

		if (verifiedNums.isEmpty()) {
			verified = true;
		} else {
			verified = false;
		}

		System.out.println("verified nums");

		keyedData = new TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, Double>>>();
		HashMap<Integer, Integer> truthVals = new HashMap<Integer, Integer>();

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

		System.out.println("filled up keyedData");

		for (Integer m : modalityIDs) {
			boolean[][] holes = getDataHoles(m);
			for (int i = 0; i < holes.length; i++) {
				for (int j = 0; j < holes[i].length; j++) {
					if (!holes[i][j]) {
						isFullyCrossed = false;
					}
				}
			}
		}

		System.out.println("determined if fully crossed");

		// TODO allow user to choose which modalities if more than 2
		getT0T1s(1, 2, truthVals);

		System.out.println("got t0t1s");
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

	public double[][] sortData(double[][] fData, double[] ReaderIDs) {
		double[][] fData1 = new double[Reader * (Normal + Disease)][5];
		// get case ids
		double[] caseIDs = new double[Normal + Disease];
		int i = 0;
		for (int k = 0; k < Reader * (Normal + Disease); k++) {
			if (fData[k][0] == ReaderIDs[0]) {
				caseIDs[i] = fData[k][1];
				i++;
			}
		}
		int counter = 0;
		for (int k = 0; k < Reader; k++) {
			for (int j = 0; j < (Normal + Disease); j++) {
				for (int t = 0; t < Reader * (Normal + Disease); t++) {
					if (caseIDs[j] == fData[t][1]
							&& ReaderIDs[k] == fData[t][0]) {
						for (int ss = 0; ss < 5; ss++)
							fData1[counter][ss] = fData[t][ss];
						counter++;
					}
				}
			}
		}
		return fData1;
	}

	/* this does not work if study is not fully crossed */
	public void getT0T1s(int modality1, int modality2,
			HashMap<Integer, Integer> truthVals) {
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
				if (c == 270) {
					int asd = 0;
				}
				if (truthVals.get(c) == 0) {
					t0[m][k][0] = keyedData.get(r).get(c).get(modality1);
					t0[m][k][1] = keyedData.get(r).get(c).get(modality2);
					t01[m][k][0] = keyedData.get(r).get(c).get(modality1);
					t01[m][k][1] = keyedData.get(r).get(c).get(modality1);
					t02[m][k][0] = keyedData.get(r).get(c).get(modality2);
					t02[m][k][1] = keyedData.get(r).get(c).get(modality2);
					m++;
				} else {
					t1[n][k][0] = keyedData.get(r).get(c).get(modality1);
					t1[n][k][1] = keyedData.get(r).get(c).get(modality2);
					t11[n][k][0] = keyedData.get(r).get(c).get(modality1);
					t11[n][k][1] = keyedData.get(r).get(c).get(modality1);
					t12[n][k][0] = keyedData.get(r).get(c).get(modality2);
					t12[n][k][1] = keyedData.get(r).get(c).get(modality2);
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

	// TODO test that this operates identically to deprecated get T0T1s method
	// TODO add support for t's with more than 2 modalities
	public void getT0T1s(double[][][] rawData) {
		t0 = new double[Normal][Reader][2];
		t1 = new double[Disease][Reader][2];
		t01 = new double[Normal][Reader][2];
		t11 = new double[Disease][Reader][2];
		t02 = new double[Normal][Reader][2];
		t12 = new double[Disease][Reader][2];
		d0 = new int[Normal][Reader][2];
		d1 = new int[Disease][Reader][2];
		int m, n;
		for (int i = 0; i < rawData.length; i++) {
			// number of false cases
			m = 0;
			// number of true cases
			n = 0;
			for (int j = 0; j < rawData[i].length; j++) {
				if (rawData[i][j][0] == 0) {
					t0[m][i][0] = rawData[i][j][1];
					t0[m][i][1] = rawData[i][j][2];
					t01[m][i][0] = rawData[i][j][1];
					t01[m][i][1] = rawData[i][j][1];
					t02[m][i][0] = rawData[i][j][2];
					t02[m][i][1] = rawData[i][j][2];
					m++;
				}
				if (rawData[i][j][0] > 0) {
					t1[n][i][0] = rawData[i][j][1];
					t1[n][i][1] = rawData[i][j][2];
					t11[n][i][0] = rawData[i][j][1];
					t11[n][i][1] = rawData[i][j][1];
					t12[n][i][0] = rawData[i][j][2];
					t12[n][i][1] = rawData[i][j][2];
					n++;
				}
			}
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

	public ArrayList<String> readFile(InputStreamReader isr) {
		int j;
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

	public static void main(String[] args) {

		inputFile file = new inputFile("1992_Franken.txt");
	}
}
