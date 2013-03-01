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
	String verifiedNums = "";
	double[][] BDG = new double[4][8];
	double[][] BDGbias = new double[4][8];
	double[][] BDGcoeff = new double[4][8];
	double[] aucMod = new double[2];

	public boolean getFullyCrossedStatus() {
		return isFullyCrossed;
	}
	
	public boolean numsVerified(){
		return verified;
	}

	public String showUnverified(){
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

	// this is the constructor for the stand alone application
	// reading a file locally

	public inputFile(String file) {
		filename = file;
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
		ArrayList<Double> readerIDs = new ArrayList<Double>();
		ArrayList<Double> normalIDs = new ArrayList<Double>();
		ArrayList<Double> diseaseIDs = new ArrayList<Double>();
		ArrayList<Double> modalityIDs = new ArrayList<Double>();

		// parse each line of input into its separate fields (still ordered by
		// line)
		for (int i = 0; i < totalLine - (counter + 1); i++) {
			tempNumbers = fileContent.get((counter + 1) + i).split("\t");
			fData[i][0] = Integer.valueOf(tempNumbers[0]); // Reader
			fData[i][1] = Integer.valueOf(tempNumbers[1]); // Case
			fData[i][2] = Integer.valueOf(tempNumbers[2]); // Modality id
			fData[i][3] = Double.valueOf(tempNumbers[3]); // Score
		}

		verifiedNums = verifyNums(fData, readerIDs, normalIDs,
				diseaseIDs, modalityIDs);

		if (verifiedNums.isEmpty()) {
			verified = true;
		} else {
			// TODO show user which numbers were incorrect
			verified = false;
		}

		double[][][] rawData = new double[Reader][Normal + Disease][Modality + 1];

		// convert fData into 3d matrix format
		for (int i = 0; i < fData.length; i++) {
			int readerId = (int) (fData[i][0] - 1);
			int caseId = (int) (fData[i][1] - 1);
			int modalityIndex = (int) fData[i][2];
			double score = fData[i][3];
			rawData[readerId][caseId][modalityIndex] = score;
		}

		// TODO display modalityPresent in a visual graph
		ArrayList<boolean[][]> modalityPresent = showDataHoles(rawData);
		isFullyCrossed = checkFullyCrossed(modalityPresent);

		getT0T1s(rawData);
	}

	/*
	 * For each modality, scans through raw input data structure and finds where
	 * combinations (reader, case) for which a data point is not present Creates
	 * a 2-d array for each modality with true/false specifying whether the data
	 * is present
	 */
	private ArrayList<boolean[][]> showDataHoles(double[][][] rawData) {
		ArrayList<boolean[][]> modalityPresent = new ArrayList<boolean[][]>();
		for (int mod = 0; mod < Modality; mod++) {
			boolean[][] currModality = new boolean[Reader][Normal + Disease];
			for (int i = 0; i < Reader; i++) {
				for (int j = 0; j < (Normal + Disease); j++) {
					// since double arrays are initialized to 0.0 and
					// modality scores can only be in range1-5 we know
					// there is a hole in the data if it = 0.0
					if (rawData[i][j][mod + 1] != 0.0) {
						currModality[i][j] = true;
					} else {
						currModality[i][j] = false;
					}
				}
			}
			modalityPresent.add(mod, currModality);
		}
		return modalityPresent;
	}

	/*
	 * Verifies that the numbers of readers, cases, and modalities read in
	 * header of file match the actual numbers of them within the file. After
	 * this method is executed, the Reader, Normal, Disease, and Modality class
	 * variables contain these actual values
	 */
	private String verifyNums(double[][] fData, ArrayList<Double> readerIDs,
			ArrayList<Double> normalIDs, ArrayList<Double> diseaseIDs,
			ArrayList<Double> modalityIDs) {
		String toReturn = new String();

		for (int i = 0; i < fData.length; i++) {
			if (!readerIDs.contains(fData[i][0])) {
				readerIDs.add(fData[i][0]);
			}
			if (fData[i][2] == 0 && fData[i][3] == 0) {
				if (!normalIDs.contains(fData[i][1])) {
					normalIDs.add(fData[i][1]);
				}
			}
			if (fData[i][2] == 0 && fData[i][3] == 1) {
				if (!diseaseIDs.contains(fData[i][1])) {
					diseaseIDs.add(fData[i][1]);
				}
			}
			if (!modalityIDs.contains(fData[i][2])) {
				modalityIDs.add(fData[i][2]);
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
		if (Modality != (modalityIDs.size() - 1)) {
			toReturn = toReturn + "NM Given = " + Modality + " NM Found = "
					+ (modalityIDs.size() - 1);
			Modality = modalityIDs.size() - 1;
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

	/*
	 * Do all readers read all cases? Based on old fData structure, cannot
	 * handle more than 2 modalities
	 */
	@Deprecated
	public int checkFullyCrossed(double[][] fData, double[] ReaderIDs) {
		double[][] alldata = new double[Reader][Normal + Disease];
		// store all the data IDs of Reader 1 into alldata[0][]
		for (int k = 0; k < Reader; k++) {
			int j = 0;
			for (int i = 0; i < (Normal + Disease) * Reader; i++) {
				if (fData[i][0] == ReaderIDs[k]) {
					alldata[k][j] = fData[i][1];
					j++;
				}
			}
		}
		int flag = 0;
		int flag2 = 1;
		for (int i = 1; i < Reader; i++) {
			for (int j = 0; j < (Normal + Disease); j++) {
				flag = 0;
				for (int k = 0; k < (Normal + Disease); k++) {
					if (alldata[i][j] == alldata[0][k]) {
						flag = 1;
					}
				}
				if (flag == 0)
					flag2 = 0;
			}
		}
		return flag2;
	}

	/*
	 * If there is a data hole, then the data cannot be fully crossed, right?
	 * TODO verify that this method works identically to deprecated version
	 */
	public boolean checkFullyCrossed(ArrayList<boolean[][]> modalityPresent) {
		boolean flag = true;
		for (int i = 0; i < modalityPresent.size(); i++) {
			for (int j = 0; j < modalityPresent.get(i).length; j++) {
				for (int k = 0; k < modalityPresent.get(i)[j].length; k++) {
					if (!modalityPresent.get(i)[j][k]) {
						flag = false;
					}
				}
			}
		}
		return flag;
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

	@Deprecated
	public void getT0T1s(double[][] fData, double[] ReaderIDs) {
		t0 = new double[Normal][Reader][2];
		t1 = new double[Disease][Reader][2];
		t01 = new double[Normal][Reader][2];
		t11 = new double[Disease][Reader][2];
		t02 = new double[Normal][Reader][2];
		t12 = new double[Disease][Reader][2];
		d0 = new int[Normal][Reader][2];
		d1 = new int[Disease][Reader][2];
		int m = 0, n = 0;
		for (int i = 0; i < Reader; i++) {
			m = 0;
			n = 0;
			double k = ReaderIDs[i];
			for (int j = 0; j < Reader * (Normal + Disease); j++) {
				if (fData[j][0] == k && fData[j][2] == 0) {
					t0[m][i][0] = fData[j][3];
					t0[m][i][1] = fData[j][4];
					t01[m][i][0] = fData[j][3];
					t01[m][i][1] = fData[j][3];
					t02[m][i][0] = fData[j][4];
					t02[m][i][1] = fData[j][4];
					m++;
				}
				if (fData[j][0] == k && fData[j][2] > 0) {
					t1[n][i][0] = fData[j][3];
					t1[n][i][1] = fData[j][4];
					t11[n][i][0] = fData[j][3];
					t11[n][i][1] = fData[j][3];
					t12[n][i][0] = fData[j][4];
					t12[n][i][1] = fData[j][4];
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
