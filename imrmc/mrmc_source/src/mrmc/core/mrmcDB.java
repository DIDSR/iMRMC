/*
 * mrmcDB.java
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
 *     Handles accessing files/data from database folder. Generates textual summary
 *     for a given database record.
 */

package mrmc.core;

import java.io.*;
import java.util.*;
import java.io.FileInputStream;

public class mrmcDB {
	private int noOfItems;
	private File[] dbFiles;
	private dbRecord[] Records;
	private ArrayList<String> dbFilenamesInJar = new ArrayList<String>();

	public int getNoOfItems() {
		return noOfItems;
	}

	public dbRecord[] getRecords() {
		return Records;
	}

	/*
	 * generate record summary. This function is called when the buttons in
	 * record summary Panel are clicked. The summary will be displayed in a pop
	 * up text editor
	 */
	public String recordsSummary(int selectedSummary, int SummaryUseMLE,
			String method) {
		String Summary = "";
		int K = 0;
		if (method.equals("BDG")) {
			Summary = "filename\tmodality\tM1\tM2\tM3\tM4\tM5\tM6\tM7\tM8\n";
			K = 8;
		} else if (method.equals("DBM")) {
			Summary = "filename\tmodality\tR\tC\tRC\tTR\tTC\tTRC\n";
			K = 6;
		} else if (method.equals("BCK")) {
			Summary = "filename\tmodality\tN\tD\tND\tR\tNR\tDR\tRND\n";
			K = 7;
		} else if (method.equals("OR")) {
			Summary = "filename\tmodality\tR\tTR\tCOV1\tCOV2\tCOV3\tERROR\n";
			K = 6;
		} else
			;
		String each = "";
		for (int i = 0; i < noOfItems; i++) {
			each = "";

			double[][] data = new double[4][K];
			if (method.equals("BDG"))
				data = Records[i].getBDG(SummaryUseMLE);
			else if (method.equals("DBM"))
				data = Records[i].getDBM(SummaryUseMLE);
			else if (method.equals("BCK"))
				data = Records[i].getBCK(SummaryUseMLE);
			else if (method.equals("OR"))
				data = Records[i].getOR(SummaryUseMLE);
			else
				;
			if (selectedSummary == 0)
				for (int j = 0; j < 2; j++) // two modalities
				{
					each = each + Records[i].getFilename() + "\t";
					// each=each+"Modality"+Integer.toString(j+1)+"\t";
					each = each + Records[i].getModality(j) + "\t";
					each = each + Records[i].getTask() + "\t";
					for (int k = 0; k < K; k++) {
						each = each + Double.toString(data[j][k]) + "\t";
					}
					each = each + "\n";
				}
			else {
				each = each + Records[i].getFilename() + "\t";
				each = each + Records[i].getModality(0) + "\t"
						+ Records[i].getModality(1) + "\t";
				each = each + Records[i].getTask() + "\t";
				for (int k = 0; k < K; k++) {
					each = each + Double.toString(data[3][k]) + "\t";
				}
				each = each + "\n";

			}
			Summary = Summary + each;
		}
		return Summary;
	}

	/* Read one DB file from the ./DB/ folder */
	public void readRecord(String filename, InputStreamReader isr, int i) {
		int j;
		BufferedReader br = new BufferedReader(isr);
		String[] strLine = new String[8];
		ArrayList<String> dbDesp = new ArrayList<String>();
		String strtemp;
		String AUCstr = new String();
		;
		j = 0;
		try {
			while ((strtemp = br.readLine()) != null) {
				if (strtemp.startsWith("*")) {
					dbDesp.add(strtemp);
				} else if (strtemp.startsWith("#")) {
					AUCstr = strtemp;
				} else {
					strLine[j] = strtemp;
					j++;
				}

			}
			Records[i] = new dbRecord(filename, strLine, dbDesp, AUCstr);
		} catch (Exception e) {
			System.err
					.println("read record Error in function readRecord in mrmcDB.java: "
							+ e.getMessage());
		}
	}

	/*
	 * load the database.
	 */
	public void loadDB() {

		int i;
		String filename;
		for (i = 0; i < noOfItems; i++) {
			try {
				InputStreamReader isr;
				DataInputStream din;

				filename = "DB/" + dbFiles[i].getName();
				// System.out.println(filename);
				FileInputStream fstream = new FileInputStream(filename);
				din = new DataInputStream(fstream);
				isr = new InputStreamReader(din);
				readRecord(filename, isr, i);
				din.close();

			} catch (Exception e) {
				System.err.println("Load DB Error in mrmcDB.java"
						+ e.getMessage());
			}
		}

	}

	/* constructor */
	public mrmcDB(MRMC mrmc) {
		FilenameFilter filefilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				// if the file extension is .jdb return true, else false
				return name.endsWith(".jdb");
			}
		};

		dbFiles = new File("DB").listFiles(filefilter);
		noOfItems = dbFiles.length;
		// System.out.println("a total of "+noOfItems+"files in DB");
		Records = new dbRecord[noOfItems];
		loadDB();

	}

	/*
	 * In the DB folder, there is a file named TableOfContent.txt, which
	 * contains the files names of all the DB files. The program will read
	 * TableOfContent.txt file, and count how many database files are there in
	 * this DB folder. I commented out the code to automatically count the
	 * database files from the Jar file. Because that results in Permission
	 * denied error due to the security features of java applet.
	 */
	public int countDBFilesInJar() {
		int i = 0;

		// JarFile jf;
		// try {
		// jf = new JarFile("List.jar");
		// Enumeration<JarEntry> e = jf.entries();
		// while (e.hasMoreElements()) {
		// JarEntry je = e.nextElement();
		// String nam = je.getName();
		// if (nam.endsWith(".jdb")) {
		// dbFilenamesInJar.add(nam);
		// i++;
		// }
		// }
		// } catch (IOException ioe) {
		// System.out.println("danger...");
		// }

		String tablefile = "DB/TableOfContent.txt";
		String strtemp;
		try {
			InputStream in = getClass().getResourceAsStream(tablefile);
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);
			while ((strtemp = br.readLine()) != null) {
				dbFilenamesInJar.add("DB/" + strtemp);
				i++;

			}
			in.close();
		} catch (Exception e) {
			System.err
					.println("read record Error in reading tableofContentfile in mrmcDB.java: "
							+ e.getMessage());
		}

		return i;
	}

}
