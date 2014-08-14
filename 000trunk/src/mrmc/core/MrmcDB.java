/**
 * MrmcDB.java
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

import java.io.*;
import java.util.*;
import java.io.FileInputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/**
 * Handles accessing files/data from database folder. Generates textual summary
 * for a given database record. *
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
public class MrmcDB {
	private String path; 					// path to db directory
	private int noOfItems;
	private File[] dbFiles;
	private DBRecord[] Records;
	private ArrayList<String> dbFilenamesInJar = new ArrayList<String>();

	/**
	 * Gets the number of entries in the database
	 * 
	 * @return Number of entries in the database
	 */
	public int getNoOfItems() {
		return noOfItems;
	}

	/**
	 * Gets all the records in the database
	 * 
	 * @return Array of DBRecords
	 */
	public DBRecord[] getRecords() {
		return Records;
	}

	/**
	 * Sole constructor. Checks the application directory for the presence of DB
	 * files and loads them
	 */
	public MrmcDB() {
		FilenameFilter filefilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				// if the file extension is .jdb return true, else false
				return name.endsWith(".jdb");
			}
		};
		
		path = "DB";
		
		File f = new File(path);
		if (!(f.exists() && f.isDirectory())) {
			JFileChooser j = new JFileChooser();
			j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			j.setDialogTitle("Select a directory where jdb files are located");
			
			Integer opt ;
			noOfItems = 0 ;
			do 
			{
				opt = j.showOpenDialog(null);
				if (opt == JFileChooser.APPROVE_OPTION) 
				{
					path=j.getSelectedFile().getPath() ;
					dbFiles = new File(path).listFiles(filefilter);
					noOfItems = dbFiles.length;
					if (noOfItems == 0) 
					{
						JOptionPane.showMessageDialog(null,"The slected directory does not contain any JDB files. Try again.");
					}
				}
				else 
				{
					int dialogButton = JOptionPane.YES_NO_OPTION;
					int dialogResult = JOptionPane.showConfirmDialog (null, "Terminate?","Warning",dialogButton);
					if(dialogResult == JOptionPane.YES_OPTION)
					{
						System.exit(0);
					}
				}
				
			} while ((opt != JFileChooser.APPROVE_OPTION) || (noOfItems == 0) )  ;
			
			// path=j.getSelectedFile().getPath() ;
		
		}
		else 
		{
			dbFiles = new File(path).listFiles(filefilter);
			noOfItems = dbFiles.length;
		}
		
		// dbFiles = new File(path).listFiles(filefilter);
		// noOfItems = dbFiles.length;
		// System.out.println("a total of "+noOfItems+"files in DB");
		Records = new DBRecord[noOfItems];
		loadDB();
	}

	/**
	 * Generates a textual summary of the database contents
	 * 
	 * @param selectedSummary Chooses between single modality or difference
	 *            summary
	 * @param SummaryUseMLE Use biased components for summary
	 * @param method Which decomposition is being used
	 * @return String with description of contents of the database
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
					for (int k = 0; k < K; k++) {
						each = each + Double.toString(data[j][k]) + "\t";
					}
					each = each + "\n";
				}
			else {
				each = each + Records[i].getFilename() + "\t";
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

	/**
	 * Reads a single DB file from the database folder
	 * 
	 * @param filename Filename of database file
	 * @param isr Stream reader containing DB file
	 * @param recordNum Identifying number for where to place record in internal
	 *            list of records
	 */
	public void readRecord(String filename, InputStreamReader isr, int recordNum) {
		int j;
		BufferedReader br = new BufferedReader(isr);
		String[] strLine = new String[8];
		ArrayList<String> dbDesp = new ArrayList<String>();
		String strtemp;
		String AUCstr = new String();
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
			Records[recordNum] = new DBRecord(filename, strLine, dbDesp, AUCstr);
		} catch (Exception e) {
			System.err
					.println("read record Error in function readRecord in mrmcDB.java: "
							+ e.getMessage());
		}
	}

	/**
	 * Loads all files in database folder into internal database
	 */
	public void loadDB() {

		int i;
		String filename;
		for (i = 0; i < noOfItems; i++) {
			try {
				InputStreamReader isr;
				DataInputStream din;

				// filename = "DB/" + dbFiles[i].getName();
				filename = path + "/" + dbFiles[i].getName();
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

	/**
	 * Reads in the table of contents file from the database folder and uses it
	 * to build list of all the database files
	 * 
	 * @return Number of database files
	 */
	public int countDBFilesInJar() {
		int numFiles = 0;

		// String tablefile = "DB/TableOfContent.txt";
		String tablefile = path + "/TableOfContent.txt";
		
		String strtemp;
		try {
			InputStream in = getClass().getResourceAsStream(tablefile);
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);
			while ((strtemp = br.readLine()) != null) {
				// dbFilenamesInJar.add("DB/" + strtemp);
				dbFilenamesInJar.add(path + "/" + strtemp);
				numFiles++;

			}
			in.close();
		} catch (Exception e) {
			System.err
					.println("read record Error in reading tableofContentfile in mrmcDB.java: "
							+ e.getMessage());
		}

		return numFiles;
	}

}
