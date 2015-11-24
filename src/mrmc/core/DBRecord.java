/**
 * DBRecord.java
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
import java.io.IOException;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import mrmc.gui.GUInterface;
import mrmc.gui.SizePanel;

/**
 * Analyze a reader study (a single modality or a difference of 2 modalities). <br>
 * This class processes the {@link mrmc.core.InputFile#observerData}. <br>
 * This class produces the AUC estimates, variance estimates, hypothesis test results,
 * and components of variance. <br>
 * This class includes all the formulas to convert one variance representation
 * to another. <br>
 * The conversion formulas are based on Gallas BD, Bandos A, Samuelson F,
 * Wagner RF. "A Framework for random-effects ROC analsysis: Biases with the
 * Bootstrap and other variance estimators", Commun Stat A-Theory 38(15),
 * 2586-2603 (2009). <br>
 * <br>
 * <b>Flow 1)</b> Object DBRecordStat created in {@link mrmc.gui.GUInterface#GUInterface(MRMC, java.awt.Container)} <br>
 * -- {@link mrmc.gui.InputFileCard.varAnalysisListener} calls <br>
 * -- -- {@link #DBRecordStatFill(InputFile, DBRecord)} <br>
 * <br>
 * <b>Flow 2)</b> Object DBRecordSize created in {@link mrmc.gui.GUInterface#GUInterface(MRMC, java.awt.Container)} <br>
 * -- {@link mrmc.gui.InputFileCard.brwsButtonListener} calls {@link mrmc.core.InputFile#ReadInputFile()} <br>
 * <br>
 * <b>Flow 3)</b> Object created in {@link roemetz.gui.RMGUInterface.SimExperiments_thread#doInBackground()} <br>
 * -- {@link roemetz.gui.RMGUInterface.SimExperiments_thread#doInBackground()} <br> 
 *    calls {@link roemetz.core.SimRoeMetz#doSim(DBRecord)} <br>
 * <br>
 * Important fields<br>
 * ----<code>recordDesc </code><br>
 * ----<code>recordTitle </code><br>
 * ----<code>filename </code><br>
 * ----<code>Task </code><br>
 * <br>
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
public class DBRecord {
	
	public boolean verbose = true;
    public double totalVarMLE;
	public GUInterface GUI;
	public DBRecord DBRecordStat, DBRecordSize;
	public InputFile InputFile1;
	public SizePanel SizePanel1;

	public StatTest testStat, testSize;
	
	/**
	 * raw data file or database file
	 */
	public String filename = "";
	/**
	 * The summary info from a reader study. The entire contents of .omrmc file.
	 */
	public String recordDesc = "";
	/**
	 * The title of the reader study. read from the .omrmc file.
	 */
	public String recordTitle = "";
	/**
	 * The task of the reader study. read from the .omrmc file.
	 */
	public String Task = "";

	/**
	 * The number of readers, normal cases, and diseased cases
	 */
	public long Nreader = -1;
	public long Nnormal = -1;
	public long Ndisease = -1;
	public long NreaderDB = -1;
	public long NnormalDB = -1;
	public long NdiseaseDB = -1;
	/** 
	 * Strings holding the names of the modalities to be analyzed, read in with {@link mrmc.gui.InputFileCard}
	 */
	public String modalityA = "modalityA", modalityB = "modalityB";
	/**
	 * Indicates which modalities to analyze, read in with {@link mrmc.gui.InputFileCard} <br>
	 * ----0 only {@link #modalityA} is active <br>
	 * ----1 only {@link #modalityB} is active <br>
	 * ----3 {@link #modalityA} and {@link #modalityB} are both active
	 */
	public int selectedMod = 3;
	/**
	 * Covariance information {@link mrmc.core.CovMRMC}
	 */
	public CovMRMC covMRMCstat, covMRMCsize;
	/**
	 * Reader-averaged auc for each modality
	 */
	public double[] AUCsReaderAvg;
	/**
	 * AUCs for each reader and modality [Nreader][2]
	 */
	public double[][] AUCs;
	/**
	 * Total variance of the reader-averaged AUC for <br>
	 * ----the modality selected <br>
	 * ----the difference in modalities
	 * 
	 */
	public double totalVar = -1.0;
	/**
	 * Indicator whether {mrmc.gui.InputFileCard#FlagMLE} is set
	 */
	public int flagMLE = 0;
	/**
	 * Indicator whether {mrmc.gui.InputFileCard#FlagMLE} is set
	 */
	public long flagTotalVarIsNegative = 0;
	/**
	 * Indicator whether the data is fully crossed or not.
	 */
	public boolean flagFullyCrossed = true;

	/**
	 * The BDG[4][8] (Barrett, Clarkson, and Kupinski) variance components <br>
	 * ----BDG[0] are the components of variance of AUC_A <br>
	 * ----BDG[1] are the components of variance of AUC_B <br>
	 * ----BDG[2] are the components of the covariance between AUC_A & AUC_B <br>
	 * ----BDG[3] are the components of variance of AUC_A - AUC_B <br>
	 * ----BDG[i][0 thru 7] refer to the following <br>
	 * ----Clarkson2006_Acad-Radiol_v13p1410 <br>
	 * ----Gallas2009_Commun-Stat-A-Theor_v38p2586 <br>
	 * 
	 */
	
	// added for saving the results (i.e., BDG)
	public double[][] BDG = new double[4][8],
						BDGbias = new double[4][8],
						BDGcoeff = new double[4][8];
	// added for saving the results
	public static double[][] BDGresult = new double[4][8];
	public static double[][] BDGbiasresult = new double[4][8];
	public static double[][] BDGcoeffresult = new double[4][8];
	public static double[][] BDGPanelresult = new double[7][8];

	
	/**
	 * The BCK[4][7] (Barrett, Clarkson, and Kupinski) variance components <br>
	 * ----BCK[0] are the components of variance of AUC_A <br>
	 * ----BCK[1] are the components of variance of AUC_B <br>
	 * ----BCK[2] are the components of the covariance between AUC_A & AUC_B <br>
	 * ----BCK[3] are the components of variance of AUC_A - AUC_B <br>
	 * ----BCK[i][0 thru 6] correspond to the following components <br>
	 * -------- 1/N0, 1/N1, 1/N0/N1, 1/Nr, 1/N0/Nr, 1/N1/Nr, 1/N0/N1/Nr <br>
	 * ----Clarkson2006_Acad-Radiol_v13p1410 <br>
	 * ----Gallas2009_Commun-Stat-A-Theor_v38p2586 <br>
	 * 
	 */
	public double[][] BCKbias = new double[4][7], 
						BCK = new double[4][7], 
						BCKcoeff = new double[4][7];
	
	public static double[][] BCKresult = new double[4][7],
			BCKbiasresult = new double[4][7],
			BCKcoeffresult = new double[4][7];

	public static double[][] BCKPanelresult = new double[7][7];
	
	/** 
	 * The DBM[4][6] (Dorfman, Berbaum, and Metz) variance components.
 	 * ----DBM[0] are the components of variance of AUC_A <br>
	 * ----DBM[1] are the components of variance of AUC_B <br>
	 * ----DBM[2] are the components of the covariance between AUC_A & AUC_B <br>
	 * ----DBM[3] are the components of variance of AUC_A - AUC_B <br>
	 * ----DBM[i][0 thru 5] correspond to the following components <br>
	 * -------- R, C, RC, TR, TC, TRC <br>
	 * Perhaps it would be better to refer to these as the 
	 * RM (Roe and Metz) variance components. <br>
	 * ----RM solidified the model <br>
	 * ----DBM presented an estimation method <br>
	 * ---- <br>
	 * ----Dorfman1992_Invest-Radiol_v27p723 <br>
	 * ----Roe1997_Acad-Radiol_v4p587 <br>
	 * 
	 */
	public double[][] DBMbias = new double[4][6],
						DBM = new double[4][6], 
						DBMcoeff = new double[4][6];
	public static double[][] DBMresult = new double[4][6],
			DBMbiasresult = new double[4][6],
			DBMcoeffresult = new double[4][6];
	
	public static double[][] DBMPanelresult = new double[3][6];
	
	/**
	 * The OR[4][6] (Obuchowski and Rockette) variance components <br>
  	 * ----OR[0] are the components of variance of AUC_A <br>
	 * ----OR[1] are the components of variance of AUC_B <br>
	 * ----OR[2] are the components of the covariance between AUC_A & AUC_B <br>
	 * ----OR[3] are the components of variance of AUC_A - AUC_B <br>
	 * ----OR[i][0 thru 5] correspond to the following components <br>
	 * -------- R, TR, Cov1, Cov2, Cov3, Error <br>
	 * ----Obuchowski1995_Commun-Stat-Simulat_v24p285 <br>
	 * ----Hillis2014_Stat-Med_v33p330 <br>
	 * 
	 */
	public double[][] ORbias = new double[4][6], 
						OR = new double[4][6], 
						ORcoeff = new double[4][6];
	
	public static double[][] ORbiasresult = new double[4][6],
			ORresult = new double[4][6],
			ORcoeffresult = new double[4][6];
	
	public static double[][] ORPanelresult = new double[3][6];
	
	/**
	 * The OR (Obuchowski and Rockette) variance components <br>
	 * ----Obuchowski1995_Commun-Stat-Simulat_v24p285 <br>
	 * ----Hillis2014_Stat-Med_v33p330 <br>
	 * 
	 */
	public double		ms_r, ms_rA, ms_rB, 
						ms_t, ms_tA, ms_tB,
						ms_tr, ms_trA, ms_trB;

	/**
	 * The mean squares from a 3-way ANOVA
	 * ----Gallas2009_Commun-Stat-A-Theor_v38p2586 <br>
	 * 
	 */
	public double[][] MS = new double[4][6], 
			MSbias = new double[4][6], 
			MScoeff = new double[4][6];
	public int inputMod;

	public static double[][] MSresult = new double[4][6],
			MSbiasresult = new double[4][6],
			MScoeffresult = new double[4][6];
	
	public static double[][] MSPanelresult = new double[3][6];
	
	/**
	 * Constructor for iMRMC
	 * @param GUItemp
	 */
	public DBRecord(GUInterface GUItemp) {
		
		GUI = GUItemp;
		
	}

	/**
	 *  Constructor for RoeMetz
	 */
	public DBRecord() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor for creating a record from the internal database
	 * 
	 * @param fname Filename of the database file
	 * @param componentStrings Array of strings containing component information
	 * @param desc Contents of .omrmc file, summary stats from reader study
	 * @param AUCstr AUCs in string form
	 */
	public DBRecord(String fname, String[] componentStrings,
			ArrayList<String> desc, String AUCstr) {

		double x = 1/0;
		/**
		 * TODO
		
		int i, j;
		recordTitle = desc.get(0).substring(2);
		filename = fname;
		// Currently all files in DB are fully crossed, but this may change

 		fullyCrossed = true;


		for (i = 0; i < 7; i++) {
			String tempStr = desc.get(i);
			if (tempStr.startsWith("*  Modal")) {
			}
			if (tempStr.startsWith("*  Task") || tempStr.startsWith("*  TASK")) {
				String[] tempStr2 = tempStr.split(",");
				Task = tempStr2[1];
			}
		}

		String[] tempAUC = AUCstr.split(",");
		AUCsReaderAvg[0] = Double.valueOf(tempAUC[1]);
		AUCsReaderAvg[1] = Double.valueOf(tempAUC[2]);
		
		Nreader = Integer.valueOf(tempAUC[3]);
		Nnormal = Integer.valueOf(tempAUC[4]);
		Ndisease = Integer.valueOf(tempAUC[5]);

		checkStudyDesign();

		for (i = 0; i < desc.size(); i++) {
			recordDesc = recordDesc + desc.get(i) + "\n";
		}

		// Grab BDG components from file string
 		for (i = 0; i < componentStrings.length / 2; i++) {
			String[] temp = componentStrings[i].split(",");
			for (j = 0; j < 8; j++) {
				BDG[i][j] = Double.valueOf(temp[j]);
			}
		}
		for (i = componentStrings.length / 2; i < componentStrings.length; i++) {
			String[] temp = componentStrings[i].split(",");
			for (j = 0; j < 8; j++) {
				BDGbias[i - componentStrings.length / 2][j] = Double
						.valueOf(temp[j]);
			}
		}
		generateDecompositions();
*/
		}

	/**
	 * Constructor for creating a record from manual input of components of
	 * variance
	 * 
	 * @param components Components of variance entered from GUI
	 * @param whichComp Specifies with decomposition of components are entered
	 * @param n Number of readers
	 * @param n2 Number of normal cases
	 * @param n3 Number of disease cases
	 * @param auc The reader-averaged auc for each modality
	 */
	public DBRecord(double[][] components, int whichComp, long n, long n2,
			long n3, double[] auc) {

		double x = 1/0;
		/**
		  TODO

		AUCsReaderAvg = auc;
		Nreader = n;
		Nnormal = n2;
		Ndisease = n3;

		fullyCrossed = true;

  		checkStudyDesign();

		switch (whichComp) {
		case 0: // BDG
			BDG = components;
			BDGbias = components;
			BDGcoeff = genBDGCoeff(Nreader, Nnormal, Ndisease);
			BCKcoeff = genBCKCoeff(Nreader, Nnormal, Ndisease);
			DBMcoeff = genDBMCoeff(Nreader, Nnormal, Ndisease);
			ORcoeff = genORCoeff(Nreader, Nnormal, Ndisease);
			MScoeff = genMSCoeff(Nreader, Nnormal, Ndisease);
			BCK = BDG2BCK(BDG);
			BDG2OR();
			DBM = BCK2DBM(BCK, Nreader, Nnormal, Ndisease);

			BCKbias = BCK;
			DBMbias = DBM;
			ORbias = OR;
			DBM[3][0] = 0;
			DBM[3][1] = 0;
			DBM[3][2] = 0;
			DBM[3][3] = DBM[0][0];
			DBM[3][4] = DBM[0][1];
			DBM[3][5] = DBM[0][2];
			MS = DBM2MS(DBM, Nreader, Nnormal, Ndisease);
			OR = DBM2OR(0, DBM, Nreader, Nnormal, Ndisease);
			break;
		case 1: // BCK
			BDG = components;
			BCK = components;
			BCKcoeff = genBCKCoeff(Nreader, Nnormal, Ndisease);
			DBMcoeff = genDBMCoeff(Nreader, Nnormal, Ndisease);
			ORcoeff = genORCoeff(Nreader, Nnormal, Ndisease);
			MScoeff = genMSCoeff(Nreader, Nnormal, Ndisease);
			DBM = BCK2DBM(BCK, Nreader, Nnormal, Ndisease);
			OR = DBM2OR(0, DBM, Nreader, Nnormal, Ndisease);
			BCKbias = BCK;
			DBMbias = DBM;
			ORbias = OR;
			DBM[3][0] = 0;
			DBM[3][1] = 0;
			DBM[3][2] = 0;
			DBM[3][3] = DBM[0][0];
			DBM[3][4] = DBM[0][1];
			DBM[3][5] = DBM[0][2];
			OR = DBM2OR(0, DBM, Nreader, Nnormal, Ndisease);
			MS = DBM2MS(DBM, Nreader, Nnormal, Ndisease);
			break;
		case 2: // DBM
			DBM = components;
			DBMbias = components;
			OR = DBM2OR(0, DBM, Nreader, Nnormal, Ndisease);
			MS = DBM2MS(DBM, Nreader, Nnormal, Ndisease);
			ORbias = OR;
			DBMcoeff = genDBMCoeff(Nreader, Nnormal, Ndisease);
			ORcoeff = genORCoeff(Nreader, Nnormal, Ndisease);
			MScoeff = genMSCoeff(Nreader, Nnormal, Ndisease);
			break;
		case 3: // OR
			OR = components;
			ORbias = components;
			DBM = DBM2OR(1, OR, Nreader, Nnormal, Ndisease);
			MS = DBM2MS(DBM, Nreader, Nnormal, Ndisease);
			DBMbias = DBM;
			DBMcoeff = genDBMCoeff(Nreader, Nnormal, Ndisease);
			ORcoeff = genORCoeff(Nreader, Nnormal, Ndisease);
			MScoeff = genMSCoeff(Nreader, Nnormal, Ndisease);
			break;
		default:
			break;

		}
*/
	}

	/**
	 *  Analyze {@link mrmc.core.InputFile#observerData} <br>
	 * <b>Flow 1)</b> Called from {@link mrmc.gui.InputFileCard.varAnalysisListener} <br>
	 * <br>
	 * <b>Flow 2)</b> Called from {@link roemetz.core.SimRoeMetz#doSim(DBRecord)} <br>
	 * <br>
	 * Creates {@link mrmc.core.CovMRMC} <br>
	 * 
	 * The required InputFile Fields are <br>
	 * -- {@link mrmc.core.InputFile#observerData} <br>
	 * -- {@link mrmc.core.InputFile#Nnormal} <br>
	 * -- {@link mrmc.core.InputFile#Ndisease} <br>
	 * -- {@link mrmc.core.InputFile#Nreader}
	 * 
	 * @param InputFileTemp instance of {@link mrmc.core.InputFile#InputFile()}
	 * @param DBRecordStatTemp instance of {@link mrmc.core.DBRecord#DBRecord(GUInterface)}
	 */
	public void DBRecordStatFill(InputFile InputFileTemp, DBRecord DBRecordStatTemp) {
		InputFile1 = InputFileTemp;
		DBRecordStat = DBRecordStatTemp;
		covMRMCstat = new CovMRMC(InputFile1, DBRecordStatTemp);
		BDGforStatPanel();
		Decompositions();
		TreeMap<String, TreeMap<String,ArrayList<String>>> modinformation1 =InputFile1.modinformation;
		ArrayList<String> chosenreaderlist = new ArrayList<String>();
		ArrayList<String> chosennormallist = new ArrayList<String>();
		ArrayList<String> chosendiseaselist = new ArrayList<String>();
		if(selectedMod == 0) {
			chosenreaderlist = modinformation1.get(modalityA).get("reader");
			chosennormallist = modinformation1.get(modalityA).get("normal");
			chosendiseaselist = modinformation1.get(modalityA).get("disease");
			flagFullyCrossed = covMRMCstat.fullyCrossedA;
			if(AUCsReaderAvg[0] < 0) {
				JFrame frame = new JFrame();
				JOptionPane.showMessageDialog(frame,
						"There are no observations for modality A." + 
						"\nPlease check your data.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		if(selectedMod == 1) {
			chosenreaderlist = modinformation1.get(modalityB).get("reader");
			chosennormallist = modinformation1.get(modalityB).get("normal");
			chosendiseaselist = modinformation1.get(modalityB).get("disease");
			flagFullyCrossed = covMRMCstat.fullyCrossedB;
			if(AUCsReaderAvg[1] < 0) {
				JFrame frame = new JFrame();
				JOptionPane.showMessageDialog(frame,
						"There are no observations for modality B." + 
						"\nPlease check your data.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		if(selectedMod == 3) {
			for (String r:  modinformation1.get(modalityA).get("reader")){
					chosenreaderlist.add(r);
			}
			for (String nor:  modinformation1.get(modalityA).get("normal")){
					chosennormallist.add(nor);
			}
			for (String dis:  modinformation1.get(modalityA).get("disease")){
					chosendiseaselist.add(dis);
			}
			for (String r:  modinformation1.get(modalityB).get("reader")){
				if (!chosenreaderlist.contains(r)){
					chosenreaderlist.add(r);
				}
			}
			for (String nor:  modinformation1.get(modalityB).get("normal")){
				if (!chosennormallist.contains(nor)){
					chosennormallist.add(nor);
				}
			}
			for (String dis:  modinformation1.get(modalityB).get("disease")){
				if (!chosendiseaselist.contains(dis)){
					chosendiseaselist.add(dis);
				}
			}
			flagFullyCrossed = covMRMCstat.fullyCrossedA && 
					covMRMCstat.fullyCrossedB && 
					covMRMCstat.fullyCrossedAB;
			if(AUCsReaderAvg[0] < 0) {
				JFrame frame = new JFrame();
				JOptionPane.showMessageDialog(frame,
						"There are no observations for modality A." + 
						"\nPlease check your data.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if(AUCsReaderAvg[1] < 0) {
				JFrame frame = new JFrame();
				JOptionPane.showMessageDialog(frame,
						"There are no observations for modality B." + 
						"\nPlease check your data.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		NreaderDB = chosenreaderlist.size();
		NnormalDB = chosennormallist.size();
		NdiseaseDB = chosendiseaselist.size();
		testStat = new StatTest(DBRecordStat);
	}
	/**
	 * Calculate DBRecord for sumary input file
	 */	
	
	public void DBRecordStatFillSummary(DBRecord dBRecordStat2) {
		// TODO Auto-generated method stub
		DBRecordStat = dBRecordStat2;
		double NR = Nreader;
		double N0 = Nnormal;
		double N1 = Ndisease;
		double [][] unbiasToBias = {
				{	       1,				0,				 0, 					  0,		      0, 					  0, 					  0, 							0 },
				{	   	1/N0, 	    (N0-1)/N0,				 0, 			    	  0,		      0, 					  0, 					  0, 							0 },
				{	    1/N1,	 	        0, 		 (N1-1)/N1, 					  0,		      0, 					  0, 					  0, 							0 },
				{ 	 1/N0/N1, 	 (N0-1)/N0/N1, 	  (N1-1)/N0/N1, 	(N0-1)*(N1-1)/N0/N1, 	 		  0, 				 	  0, 					  0, 							0 },
				{ 		1/NR, 		        0, 				 0,						  0,	  (NR-1)/NR, 					  0, 					  0,						    0 },
				{ 	 1/N0/NR, 	 (N0-1)/N0/NR,				 0,						  0,   (NR-1)/N0/NR,	(N0-1)*(NR-1)/N0/NR,				      0,						    0 },
				{    1/N1/NR, 	    	    0, 	  (N1-1)/N1/NR,						  0,   (NR-1)/N1/NR,					  0, 	(N1-1)*(NR-1)/N1/NR, 							0 },
				{ 1/N0/N1/NR, (N0-1)/N0/N1/NR, (N1-1)/N0/N1/NR, (N0-1)*(N1-1)/N0/N1/NR, (NR-1)/N0/N1/NR, (N0-1)*(NR-1)/N0/N1/NR, (N1-1)*(NR-1)/N0/N1/NR, (N0-1)*(N1-1)*(NR-1)/N0/N1/NR}};		
        double[][] tempBDG = new double[4][8];
		if(selectedMod==0)
			tempBDG[0]=	BDG[0];
		else if (selectedMod==1)
			tempBDG[1] = BDG[1];
		else if(selectedMod==3)
			tempBDG = BDG;
	    double [][] unbiasToBiast = Matrix.matrixTranspose(unbiasToBias);
	    BDGbias = Matrix.multiply(tempBDG , unbiasToBiast);		
		double totalVarnoMLE=0.0;
		totalVarMLE=0.0;
		totalVar=0.0;
		BDGcoeff = genBDGCoeff(DBRecordStat.Nreader,DBRecordStat.Nnormal,DBRecordStat.Ndisease);
	    double[] temp= new double[8];
		for (int i = 0; i < 8; i++) {
		     temp[i]=1.0;
		}
		DBRecordStat.BDGcoeff[3] = temp;
		for (int i = 0; i < 8; i++) {
			DBRecordStat.BDG[3][i] =     (tempBDG[0][i] * DBRecordStat.BDGcoeff[0][i])
					  +     (tempBDG[1][i] * DBRecordStat.BDGcoeff[1][i])
					  - 2.0*(tempBDG[2][i] * DBRecordStat.BDGcoeff[2][i]);
			DBRecordStat.BDGbias[3][i] =     (DBRecordStat.BDGbias[0][i] * DBRecordStat.BDGcoeff[0][i])
					  +     (DBRecordStat.BDGbias[1][i] * DBRecordStat.BDGcoeff[1][i])
					  - 2.0*(DBRecordStat.BDGbias[2][i] * DBRecordStat.BDGcoeff[2][i]);			
			totalVarnoMLE += BDGcoeff[3][i] * BDG[3][i];
			totalVarMLE  += BDGcoeff[3][i] * BDGbias[3][i];
		}
		if (flagMLE==0){
			totalVar= totalVarnoMLE;
		}else{
			totalVar=totalVarMLE;
		}
		

		if(totalVar < 0) {
			flagTotalVarIsNegative = 1;
		}
		
		BDGresult = BDG;
		BDGcoeffresult = BDGcoeff;
		BDGbiasresult = BDGbias;	
		
		
		DBRecordStat.Decompositions();
		DBRecordStat.testStat = new StatTest(DBRecordStat);
		
		
	}
	
	/**
	 * Performs calculations for sizing a new trial based on parameters
	 * specified. Sets GUI label with statistics info.
	 * 
	 * @param SizePanelTemp
	 */
	public void DBRecordSizeFill(SizePanel SizePanelTemp) {
		
		SizePanel1 = SizePanelTemp;
		DBRecordSize = GUI.DBRecordSize;
		DBRecordStat = GUI.DBRecordStat;

		if (DBRecordStat.totalVar <= 0.0) {
			JOptionPane.showMessageDialog(GUI.MRMCobject.getFrame(),
					"Must perform variance analysis first.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		covMRMCstat = DBRecordStat.covMRMCstat;
		covMRMCsize = new CovMRMC(SizePanel1, DBRecordSize);

		BDGforSizePanel();
		Decompositions();

		if(selectedMod == 0) {
			flagFullyCrossed = covMRMCsize.fullyCrossedA;
		}
		if(selectedMod == 1) {
			flagFullyCrossed = covMRMCsize.fullyCrossedB;
		}
		if(selectedMod == 3) {
			flagFullyCrossed = covMRMCsize.fullyCrossedA && 
					covMRMCsize.fullyCrossedB && 
					covMRMCsize.fullyCrossedAB;
		}

		testSize = new StatTest(SizePanel1, DBRecordStat, DBRecordSize);

	}
	
	/**
	 *
	 * 
	 * 
	 * @param SizePanelTemp
	 */
	public void DBRecordRoeMetzNumericalFill(SizePanel SizePanelRoeMetz) {
		
		DBRecord DBRecordTemp = new DBRecord();

		// We just need the coefficients
		covMRMCsize = new CovMRMC(SizePanelRoeMetz, DBRecordTemp);
		
		totalVar = 0.0;
		for(int i=0; i<8; i++) {
			BDGcoeff[0][i] = covMRMCsize.coefficientsAA[i+1];
			BDGcoeff[1][i] = covMRMCsize.coefficientsBB[i+1];
			BDGcoeff[2][i] = covMRMCsize.coefficientsAB[i+1];
			BDGcoeff[3][i] = 1.0;
			
			BDG[3][i] = 
					+    BDGcoeff[0][i]*BDG[0][i]
					+    BDGcoeff[1][i]*BDG[1][i]
					-2.0*BDGcoeff[2][i]*BDG[2][i];

			totalVar = totalVar + BDGcoeff[3][i]*BDG[3][i];
			
			BDGbias[0][i] = BDG[0][i];
			BDGbias[1][i] = BDG[1][i];
			BDGbias[2][i] = BDG[2][i];
			BDGbias[3][i] = BDG[3][i];
		}

		if(selectedMod == 0) {
			flagFullyCrossed = covMRMCsize.fullyCrossedA;
		}
		if(selectedMod == 1) {
			flagFullyCrossed = covMRMCsize.fullyCrossedB;
		}
		if(selectedMod == 3) {
			flagFullyCrossed = covMRMCsize.fullyCrossedA && 
					covMRMCsize.fullyCrossedB && 
					covMRMCsize.fullyCrossedAB;
		}

		Decompositions();
		testStat = new StatTest(this);
		
	}
	

	


	/**
	 * Determine BDG, BDGbias, and BDGcoeff from {@link #DBRecordStat}, <br>
	 * Calculate {@link #totalVar}
	 */
	private void BDGforStatPanel() {
		double totalVarnoMLE=0.0;
		totalVarMLE=0.0;
		for (int i = 0; i < 8; i++) {
			BDG[0][i] = covMRMCstat.momentsAA[i + 1];
			BDG[1][i] = covMRMCstat.momentsBB[i + 1];
			BDG[2][i] = covMRMCstat.momentsAB[i + 1];
			BDGbias[0][i] = covMRMCstat.momentsBiasedAA[i + 1];
			BDGbias[1][i] = covMRMCstat.momentsBiasedBB[i + 1];
			BDGbias[2][i] = covMRMCstat.momentsBiasedAB[i + 1];
			BDGcoeff[0][i] = covMRMCstat.coefficientsAA[i + 1];
			BDGcoeff[1][i] = covMRMCstat.coefficientsBB[i + 1];
			BDGcoeff[2][i] = covMRMCstat.coefficientsAB[i + 1];
			
			BDGcoeff[3][i] = 1.0;

			BDG[3][i] =     (BDG[0][i] * BDGcoeff[0][i])
					  +     (BDG[1][i] * BDGcoeff[1][i])
					  - 2.0*(BDG[2][i] * BDGcoeff[2][i]);
			BDGbias[3][i] = (BDGbias[0][i] * BDGcoeff[0][i])
					  +     (BDGbias[1][i] * BDGcoeff[1][i])
					  - 2.0*(BDGbias[2][i] * BDGcoeff[2][i]);
			totalVarnoMLE += BDGcoeff[3][i] * BDG[3][i];
			totalVarMLE  += BDGcoeff[3][i] * BDGbias[3][i];
			
		}
		if (flagMLE==0){
			totalVar= totalVarnoMLE;
		}else{
			totalVar=totalVarMLE;
		}
		

		if(totalVar < 0) {
			flagTotalVarIsNegative = 1;
		}
		/*
		 * added for saving the results 
		 */
		
		BDGresult = BDG;
		BDGcoeffresult = BDGcoeff;
		BDGbiasresult = BDGbias;	
	}
	
	/**
	 * Determine BDG and BDGbias from {@link #covMRMCstat}, <br>
	 * Determine BDGcoeff from {@link #covMRMCsize}
	 * Calculate {@link #totalVar}
	 */
	private void BDGforSizePanel() {

		totalVar = 0.0;
		for (int i = 0; i < 8; i++) {
		    BDG[0][i] = DBRecordStat.BDG[0][i];
			BDG[1][i] = DBRecordStat.BDG[1][i];
			BDG[2][i] = DBRecordStat.BDG[2][i];
			BDGbias[0][i] = DBRecordStat.BDGbias[0][i];
			BDGbias[1][i] = DBRecordStat.BDGbias[1][i];
			BDGbias[2][i] = DBRecordStat.BDGbias[2][i];			
			BDGcoeff[0][i] = covMRMCsize.coefficientsAA[i + 1];
			BDGcoeff[1][i] = covMRMCsize.coefficientsBB[i + 1];
			BDGcoeff[2][i] = covMRMCsize.coefficientsAB[i + 1];
			
			BDGcoeff[3][i] = 1.0;

			BDG[3][i] =     (BDG[0][i] * BDGcoeff[0][i])
					  +     (BDG[1][i] * BDGcoeff[1][i])
					  - 2.0*(BDG[2][i] * BDGcoeff[2][i]);
			BDGbias[3][i] = (BDGbias[0][i] * BDGcoeff[0][i])
					  +     (BDGbias[1][i] * BDGcoeff[1][i])
					  - 2.0*(BDGbias[2][i] * BDGcoeff[2][i]);
			
			totalVar += BDGcoeff[3][i] * BDG[3][i];
			
		}
		
		totalVar = totalVar*1.0;
		
	

	}
	
	/**
	 * Derives all decompositions and coefficient matrices from predefined BDG
	 * components and experiment size
	 */
	public void Decompositions() {

		BCKcoeff = genBCKCoeff(BDGcoeff);
		BCK = BDG2BCK(BDG, BCKcoeff);
		BCKbias = BDG2BCK(BDGbias, BCKcoeff);

		if(flagFullyCrossed) {
			DBMcoeff = genDBMCoeff(Nreader, Nnormal, Ndisease);
			ORcoeff = genORCoeff(Nreader, Nnormal, Ndisease);
			MScoeff = genMSCoeff(Nreader, Nnormal, Ndisease);

			DBM = BCK2DBM(BCK, Nreader, Nnormal, Ndisease);
			BDG2OR();
			// OR = DBM2OR(0, DBM, Nreader, Nnormal, Ndisease);
			MS = DBM2MS(DBM, Nreader, Nnormal, Ndisease);
			
			DBMbias = BCK2DBM(BCKbias, Nreader, Nnormal, Ndisease);
			ORbias = DBM2OR(0, DBMbias, Nreader, Nnormal, Ndisease);
			MSbias = DBM2MS(DBMbias, Nreader, Nnormal, Ndisease);
		
			// added for saving the results
			DBMresult = DBM;
			DBMcoeffresult = DBMcoeff;
			DBMbiasresult = DBMbias;
			
			ORresult = OR;
			ORcoeffresult = ORcoeff;
			ORbiasresult = ORbias;
			
			MSresult = MS;
			MScoeffresult = MScoeff;
			MSbiasresult = MSbias;
			
		}
		
		// added for saving the results
		BCKresult = BCK;
		BCKcoeffresult = BCKcoeff;
		BCKbiasresult = BCKbias;
	}

	/**
	 * Gets the AUCsReaderAvg of the record in String format
	 * 
	 * @param selectedMod specifies modality A, B, or difference
	 * @return String containing AUCsReaderAvg
	 */
	public String getAUCsReaderAvgString(int selectedMod) {
		DecimalFormat threeDec = new DecimalFormat("0.000");
		threeDec.setGroupingUsed(false);

		String temp = "";
		switch (selectedMod) {
		case 0:
			temp = "AUC_A = " + threeDec.format(AUCsReaderAvg[0]);
			break;
		case 1:
			temp = "AUC_B = " + threeDec.format(AUCsReaderAvg[1]);
			break;
		case 3:
			temp = "AUC_A = " + threeDec.format(AUCsReaderAvg[0]) + ",   AUC_B = "
					+ threeDec.format(AUCsReaderAvg[1]) + ",   AUC_A - AUC_B = "
					+ threeDec.format(AUCsReaderAvg[0] - AUCsReaderAvg[1]);
		}
		return temp;
	}

	/**
	 * Gets the AUCs of the record as a double
	 * 
	 * @param whichMod specifies modality 0, 1, or difference
	 * @return Double representation of AUC
	 */
	public double getAUCinNumber(int whichMod) {
		return AUCsReaderAvg[whichMod];
	}

	/**
	 * Gets the experiment sizes in string format
	 * 
	 * @return String containing experiment sizes
	 */
	public String getSizes() {
		return (Long.toString(NreaderDB) + " Readers,   "
				+ Long.toString(NnormalDB) + " Normal cases,   "
				+ Long.toString(NdiseaseDB) + " Disease cases.");
	}

	/**
	 * Gets the experiment sizes in int representation
	 * 
	 * @return Array containing experiment sizes
	 */
	public long[] getSizesInt() {
		long[] c = { Nreader, Nnormal, Ndisease };
		return c;
	}

	/**
	 * Gets the components, coefficients, and total variance for BDG
	 * representation in format corresponding to display table
	 * 
	 * @param selectedMod Which modality the analysis is performed on, or
	 *            difference
	 * @param BDGtemp BDG decomposition of variance components
	 * @param BDGc Coefficient matrix for BDG decomposition
	 * @return Matrix of data for display in table
	 */
	public static double[][] getBDGTab(int selectedMod, double[][] BDGtemp,
			double[][] BDGc) {
		double[][] BDGTab1 = new double[7][8];
		if (selectedMod == 0) {
			BDGTab1[0] = BDGtemp[0];
			BDGTab1[1] = BDGc[0];
		} else if (selectedMod == 1) {
			BDGTab1[2] = BDGtemp[1];
			BDGTab1[3] = BDGc[1];
		} else if (selectedMod == 3) {
			BDGTab1[0] = BDGtemp[0];
			BDGTab1[1] = BDGc[0];
			BDGTab1[2] = BDGtemp[1];
			BDGTab1[3] = BDGc[1];
			BDGTab1[4] = BDGtemp[2]; // covariance
			BDGTab1[5] = Matrix.scale(BDGc[2], 2);
		}
		for (int i = 0; i < 8; i++) {
			BDGTab1[6][i] = (BDGTab1[0][i] * BDGTab1[1][i])
					+ (BDGTab1[2][i] * BDGTab1[3][i])
					- (BDGTab1[4][i] * BDGTab1[5][i]);
		}
		
		BDGPanelresult = BDGTab1;
		
		// added for saving the results (i.e., BDG comp m0~total)
		return BDGTab1;
	}
	
	/**
	 * Gets the components, coefficients, and total variance for DBM
	 * representation in format corresponding to display table
	 * 
	 * @param selectedMod Which modality the analysis is performed on, or
	 *            difference
	 * @param DBMtemp DBM decomposition of variance components
	 * @param DBMc Coefficient matrix for DBM decomposition
	 * @return Matrix of data for display in table
	 */
	public static double[][] getDBMTab(int selectedMod, double[][] DBMtemp,
			double[][] DBMc) {
		double[][] DBMTab1 = new double[3][6];
		DBMTab1[0] = DBMtemp[selectedMod];
		DBMTab1[1] = DBMc[selectedMod];
		DBMTab1[2] = Matrix.dotProduct(DBMTab1[0], DBMTab1[1]);
		
		DBMPanelresult = DBMTab1;
		
		return DBMTab1;
	}

	/**
	 * Gets the components, coefficients, and total variance for BCK
	 * representation in format corresponding to display table
	 * 
	 * @param selectedMod Which modality the analysis is performed on, or
	 *            difference
	 * @param BCKtemp BCK decomposition of variance components
	 * @param BCKc Coefficient matrix for BCK decomposition
	 * @return Matrix of data for display in table
	 */
	public static double[][] getBCKTab(int selectedMod, double[][] BCKtemp,
			double[][] BCKc) {
		double[][] BCKTab1 = new double[7][7];
		if (selectedMod == 0) {
			BCKTab1[0] = BCKtemp[0];
			BCKTab1[1] = BCKc[0];
		} else if (selectedMod == 1) {
			BCKTab1[2] = BCKtemp[1];
			BCKTab1[3] = BCKc[1];
		} else if (selectedMod == 3) {
			BCKTab1[0] = BCKtemp[0];
			BCKTab1[1] = BCKc[0];
			BCKTab1[2] = BCKtemp[1];
			BCKTab1[3] = BCKc[1];
			BCKTab1[4] = BCKtemp[2]; // covariance
			BCKTab1[5] = Matrix.scale(BCKc[2], 2);
		}
		for (int i = 0; i < 7; i++) {
			BCKTab1[6][i] = (BCKTab1[0][i] * BCKTab1[1][i])
					+ (BCKTab1[2][i] * BCKTab1[3][i])
					- (BCKTab1[4][i] * BCKTab1[5][i]);
		}
		
		BCKPanelresult = BCKTab1;
		
		return BCKTab1;
	}



	/**
	 * Gets the components, coefficients, and total variance for OR
	 * representation in format corresponding to display table
	 * 
	 * @param selectedMod Which modality the analysis is performed on, or
	 *            difference
	 * @param ORtemp OR decomposition of variance components
	 * @param ORc Coefficient matrix for OR decomposition
	 * @return Matrix of data for display in table
	 */
	public static double[][] getORTab(int selectedMod, double[][] ORtemp,
			double[][] ORc) {
		double[][] ORTab1 = new double[3][6];
		ORTab1[0] = ORtemp[selectedMod];
		ORTab1[1] = ORc[selectedMod];
		ORTab1[2] = Matrix.dotProduct(ORTab1[0], ORTab1[1]);
		
		ORPanelresult = ORTab1;
		
		return ORTab1;
	}

	/**
	 * Gets the components, coefficients, and total variance for MS
	 * representation in format corresponding to display table
	 * 
	 * @param selectedMod Which modality the analysis is performed on, or
	 *            difference
	 * @param MStemp MS decomposition of variance components
	 * @param MSc Coefficient matrix for MS decomposition
	 * @return Matrix of data for display in table
	 */
	public static double[][] getMSTab(int selectedMod, double[][] MStemp,
			double[][] MSc) {
		double[][] MSTab1 = new double[3][6];
		MSTab1[0] = MStemp[selectedMod];
		MSTab1[1] = MSc[selectedMod];
		MSTab1[2] = Matrix.dotProduct(MSTab1[0], MSTab1[1]);
		
		MSPanelresult = MSTab1;
		
		return MSTab1;
	}

	/**
	 * Transform DBM representation of variance components into MS
	 * representation of variance components
	 * 
	 * @param DBM Matrix of DBM variance components
	 * @param Nreader2 Number of readers
	 * @param Nnormal2 Number of normal cases
	 * @param Ndisease2 Number of disease cases
	 * @return Matrix of MS representation of variance components
	 */
	public static double[][] DBM2MS(double[][] DBM, long Nreader2, long Nnormal2, long Ndisease2) {
		double[][] c = new double[4][6];
		double[][] BAlpha = new double[][] {
				{ 2 * (Nnormal2 + Ndisease2), 0, 2, (Nnormal2 + Ndisease2), 0, 1 },
				{ 0, 2 * Nreader2, 2, 0, Nreader2, 1 }, 
				{ 0, 0, 2, 0, 0, 1 },
				{ 0, 0, 0, (Nnormal2 + Ndisease2), 0, 1 },
				{ 0, 0, 0, 0, Nreader2, 1 }, 
				{ 0, 0, 0, 0, 0, 1 } };
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 6; j++)
				c[i][j] = 0;
		c = Matrix.matrixTranspose(Matrix.multiply(BAlpha,
				Matrix.matrixTranspose(DBM)));
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 6; j++)
				c[i][j] = c[i][j] / 2.0;

		return c;

	}

	/**
	 * Transforms BCK representation of variance components into DBM
	 * representation of variance components
	 * 
	 * @param BCK Matrix of BCK variance components
	 * @param Nreader2 Number of readers
	 * @param Nnormal2 Number of normal cases
	 * @param Ndisease2 Number of disease cases
	 * @return Matrix of DBM representation of variance components
	 */
	public static double[][] BCK2DBM(double[][] BCK, long Nreader2, long Nnormal2, long Ndisease2) {
		double[] c = new double[7];
		double[][] tmp = new double[4][7];
		double[][] tmp1 = new double[4][3];
		double[][] results = new double[4][6];

		// Scale the BCK components to the experiment
		c[0] = 1.0 / Nnormal2;
		c[1] = 1.0 / Ndisease2;
		c[2] = 1.0 / (Nnormal2 * Ndisease2);
		c[3] = 1.0 / Nreader2;
		c[4] = 1.0 / (Nnormal2 * Nreader2);
		c[5] = 1.0 / (Ndisease2 * Nreader2);
		c[6] = 1.0 / (Nnormal2 * Ndisease2 * Nreader2);
		for (int i = 0; i < 3; i++)
			tmp[i] = Matrix.dotProduct(BCK[i], c);

		// alpha is the matrix that maps BCK to DBM
		double[][] alpha = new double[][] { { 0, 1, 0 }, { 0, 1, 0 },
				{ 0, 1, 0 }, { Nreader2, 0, 0 }, { 0, 0, Nreader2 }, { 0, 0, Nreader2 },
				{ 0, 0, Nreader2 } };

		tmp1 = Matrix.multiply(tmp, alpha);

		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 6; j++)
				results[i][j] = 0;

		results[0][0] = tmp1[0][0];
		results[0][1] = tmp1[0][1] * (Nnormal2 + Ndisease2);
		results[0][2] = tmp1[0][2] * (Nnormal2 + Ndisease2);

		results[1][0] = tmp1[1][0];
		results[1][1] = tmp1[1][1] * (Nnormal2 + Ndisease2);
		results[1][2] = tmp1[1][2] * (Nnormal2 + Ndisease2);

		results[3][0] = tmp1[2][0];
		results[3][1] = tmp1[2][1] * (Nnormal2 + Ndisease2);
		results[3][2] = tmp1[2][2] * (Nnormal2 + Ndisease2);

		results[3][3] = (tmp1[0][0] + tmp1[1][0]) / 2.0 - tmp1[2][0];
		results[3][4] = ((tmp1[0][1] + tmp1[1][1]) / 2.0 - tmp1[2][1])
				* (Nnormal2 + Ndisease2);
		results[3][5] = ((tmp1[0][2] + tmp1[1][2]) / 2.0 - tmp1[2][2])
				* (Nnormal2 + Ndisease2);

		return results;
	}

	/**
	 * Transforms BDG representation of variance components into BCK
	 * representation of variance components
	 * 
	 * @param tempBDG BDG variance components
	 * @param tempBCKcoeff BDG variance components
	 * @return Matrix of BCK representation of variance components
	 */
	public static double[][] BDG2BCK(double[][] tempBDG, double[][] tempBCKcoeff) {
		
		double[][] c = new double[3][7];
		double[][] BAlpha = new double[][] { { 0, 0, 0, 0, 0, 0, 1, -1 },
				{ 0, 0, 0, 0, 0, 1, 0, -1 }, { 0, 0, 0, 0, 1, -1, -1, 1 },
				{ 0, 0, 0, 1, 0, 0, 0, -1 }, { 0, 0, 1, -1, 0, 0, -1, 1 },
				{ 0, 1, 0, -1, 0, -1, 0, 1 }, { 1, -1, -1, 1, -1, 1, 1, -1 } };

		c = Matrix.matrixTranspose(Matrix.multiply(BAlpha,Matrix.matrixTranspose(tempBDG)));

		for(int i=0; i<7; i++) {
			if(tempBCKcoeff[2][i] == 0) c[2][i] = 0;
		}
		
		return c;
	}



	/**
	 * Determines the coefficient matrix for BDG variance components given a
	 * fully crossed study design [4][8].
	 * This is only executed when the components of variance are input by hand or during iRoeMetz
	 * 
	 * @param Nreader2 Number of readers
	 * @param Nnormal2 Number of normal cases
	 * @param Ndisease2 Number of disease cases
	 * @return Matrix containing coefficients corresponding to BDG variance
	 *         components
	 */
	public static double[][] genBDGCoeff(long Nreader2, long Nnormal2, long Ndisease2) {
		double[][] c = new double[4][8];
		c[0][0] = 1.0 / (Nreader2 * Nnormal2 * Ndisease2);
		c[0][1] = c[0][0] * (Nnormal2 - 1.0);
		c[0][2] = c[0][0] * (Ndisease2 - 1.0);
		c[0][3] = c[0][0] * (Nnormal2 - 1.0) * (Ndisease2 - 1.0);
		c[0][4] = c[0][0] * (Nreader2 - 1.0);
		c[0][5] = c[0][0] * (Nnormal2 - 1.0) * (Nreader2 - 1.0);
		c[0][6] = c[0][0] * (Ndisease2 - 1.0) * (Nreader2 - 1.0);
		c[0][7] = c[0][0] * (Ndisease2 - 1.0) * (Nnormal2 - 1.0) * (Nreader2 - 1.0);
		c[0][7] = c[0][7] - 1;
		c[1] = c[0];
		c[2] = c[0];
		c[3] = c[0];

		return c;
	}

	/**
	 * Determines the coefficient matrix for BCK variance components given <code>BDGcoeff</code> <br>
	 * There is another function that creates the coefficients assuming a fully-crossed study design.
	 * The inputs in that case are the number of readers, diseased cases, and nondiseased cases
	 * 
	 * @return Matrix containing coefficients corresponding to BCK variance [4][7]
	 *         components
	 */
	public static double[][] genBCKCoeff(double[][] BDGcoeff_temp) {
		double[][] c2ca = new double[][] {
				{ 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0 },
				{ 1.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0 },
				{ 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 },
				{ 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0 },
				{ 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				{ 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 } };

		double[][] cAlpha = Matrix.multiply(BDGcoeff_temp, Matrix.matrixTranspose(c2ca));
		for(int i=0; i<7; i++) cAlpha[3][i] = 1.0;
		return cAlpha;
	}
	
	/**
	 * Determines the coefficient matrix for BCK variance components given experiment size
	 * 
	 * @return Matrix containing coefficients corresponding to BCK variance [4][7]
	 *         components
	 */
	public static double[][] genBCKCoeff(long Nreader, long Nnormal, long Ndisease) {
		double[][] cAlpha = new double[][] {
				{ 1.0/Nnormal, 1.0/Ndisease, 1.0/Nnormal/Ndisease, 
					1.0/Nreader, 1.0/Nreader/Nnormal, 1.0/Nreader/Ndisease, 1.0/Nreader/Nnormal/Ndisease},
				{ 1.0/Nnormal, 1.0/Ndisease, 1.0/Nnormal/Ndisease, 
					1.0/Nreader, 1.0/Nreader/Nnormal, 1.0/Nreader/Ndisease, 1.0/Nreader/Nnormal/Ndisease},
				{ 1.0/Nnormal, 1.0/Ndisease, 1.0/Nnormal/Ndisease, 
					1.0/Nreader, 1.0/Nreader/Nnormal, 1.0/Nreader/Ndisease, 1.0/Nreader/Nnormal/Ndisease},
				{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}};

		return cAlpha;
	}



	/**
	 * Determines the coefficient matrix for DBM variance components given a
	 * fully crossed study design [4][6]
	 * 
	 * @param Nreader2 Number of readers
	 * @param Nnormal2 Number of normal cases
	 * @param Ndisease2 Number of disease cases
	 * @return Matrix containing coefficients corresponding to DBM variance
	 *         components
	 */
	public static double[][] genDBMCoeff(long Nreader2, long Nnormal2, long Ndisease2) {
		double[][] c = new double[4][6];

		/* per unit */
		c[0][0] = 1.0 / Nreader2;
		c[0][1] = 1.0 / (Nnormal2 + Ndisease2);
		c[0][2] = 1.0 / Nreader2 / (Nnormal2 + Ndisease2);
		c[0][3] = 1.0 / Nreader2;
		c[0][4] = 1.0 / (Nnormal2 + Ndisease2);
		c[0][5] = 1.0 / Nreader2 / (Nnormal2 + Ndisease2);

		c[1] = c[0];
		c[2] = Matrix.scale(c[0], 0);
		c[3] = Matrix.scale(c[0], 2);

		c[0][3] = 0;
		c[0][4] = 0;
		c[0][5] = 0;
		c[1][3] = 0;
		c[1][4] = 0;
		c[1][5] = 0;
		c[3][0] = 0;
		c[3][1] = 0;
		c[3][2] = 0;
		return c;
	}

	/**
	 * Determines the coefficient matrix for MS variance components given a
	 * fully crossed study design [4][6]
	 * 
	 * @param Nreader2 Number of readers
	 * @param Nnormal2 Number of normal cases
	 * @param Ndisease2 Number of disease cases
	 * @return Matrix containing coefficients corresponding to MS variance
	 *         components
	 */
	public static double[][] genMSCoeff(long Nreader2, long Nnormal2, long Ndisease2) {
		double[][] c = new double[4][6];
		double tmp = 1.0 / (Nreader2 * (Nnormal2 + Ndisease2));
		c[0][0] = tmp;
		c[0][1] = tmp;
		c[0][2] = 0;
		c[0][3] = 0;
		c[0][4] = -tmp;
		c[0][5] = 0;
		c[1] = c[0];
		c[2] = Matrix.scale(c[0], 0);
		c[3][0] = 0;
		c[3][1] = 0;
		c[3][2] = tmp * 2.0;
		c[3][3] = tmp * 2.0;
		c[3][4] = 0;
		c[3][5] = -tmp * 2.0;
		return c;
	}

	/**
	 * Determines the coefficient matrix for OR variance components given a
	 * fully crossed study design [4][6]
	 * 
	 * @param Nreader2 Number of readers
	 * @param Nnormal2 Number of normal cases
	 * @param Ndisease2 Number of disease cases
	 * @return Matrix containing coefficients corresponding to OR variance
	 *         components
	 */
	public static double[][] genORCoeff(long Nreader2, long Nnormal2, long Ndisease2) {
		double[][] c = new double[4][6];
		c[0][0] = 1.0 / Nreader2;
		c[0][1] = 0;
		c[0][2] = 0;
		c[0][3] = 1.0 / Nreader2 * (Nreader2 - 1);
		c[0][4] = 0;
		c[0][5] = 1.0 / Nreader2;
		c[1] = c[0];
		c[2] = Matrix.scale(c[0], 0);
		c[3][0] = 0;
		c[3][1] = 2.0 / Nreader2;
		c[3][2] = -2.0 / Nreader2;
		c[3][3] = 2.0 / Nreader2 * (Nreader2 - 1);
		c[3][4] = -2.0 / Nreader2 * (Nreader2 - 1);
		c[3][5] = 2.0 / Nreader2;

		return c;
	}



	/**
	 * Transforms matrix of BDG variance components into matrix of OR variance
	 * components
	 * 
	 */
	public void BDG2OR() {
		
		double temp;
		double dnr = Nreader;
		double dn0 = Nnormal;
		double dn1 = Ndisease;
		double dnm = 2;
		
		double var_r, var_rA, var_rB, var_mr, var_mrA, var_mrB;
//		double ms_t, ms_tA, ms_tB, ms_r, ms_rA, ms_rB, ms_tr, ms_trA, ms_trB;
		double errorA, errorB, error, cov1A, cov1B, cov1, cov2A, cov2B, cov2, cov3A, cov3B, cov3;
		double c1 = 1.0/dn0/dn1;
		double c2 = (dn0-1.0)/dn0/dn1;
		double c3 = (dn1-1.0)/dn0/dn1;
		double c4 = (dn0-1.0)*(dn1-1.0)/dn0/dn1 - 1.0;

		/*
		 * From Hillis 2014 in-line Eqs following Eq 4 we can also write
		 */
		ms_t = 0.0;
		ms_tA = 0.0;
		ms_tB = 0.0;
		ms_tr = 0.0;
		ms_trA = 0.0;
		ms_trB = 0.0;
		for(int i=0; i<2; i++) {
			temp = AUCsReaderAvg[i] - (AUCsReaderAvg[0] + AUCsReaderAvg[1])/2.0;
			ms_t = ms_t + Math.pow(temp, 2); 
			for(int j=0; j<dnr; j++) {
				temp = AUCs[j][i] - 
						AUCsReaderAvg[i] - 
						(AUCs[j][0] + AUCs[j][1])/2.0 + 
						(AUCsReaderAvg[0] + AUCsReaderAvg[1])/2.0;
				ms_tr = ms_tr + Math.pow(temp, 2); 
			}
		}
		ms_t = dnr*ms_t/(dnm-1.0);
		ms_tr = ms_tr/(dnr-1.0)/(dnm-1.0);
		/* This is an equivalent expression
		ms_tr = OR[3][1] + OR[3][5] - OR[3][2] - OR[3][3] + OR[3][4];
		*/
		
		ms_r = 0.0;
		ms_rA = 0.0;
		ms_rB = 0.0;
		for(int j=0; j<dnr; j++) {
			temp = AUCs[j][0] - AUCsReaderAvg[0];
			ms_rA = ms_rA + Math.pow(temp, 2); 

			temp = AUCs[j][1] - AUCsReaderAvg[1];
			ms_rB = ms_rB + Math.pow(temp, 2); 

			temp = (AUCs[j][0]+AUCs[j][1])/2.0 - (AUCsReaderAvg[0]+AUCsReaderAvg[1])/2.0;
			ms_r = ms_r   + Math.pow(temp, 2); 
		}
		ms_rA = ms_rA/(dnr-1.0);
		ms_rB = ms_rB/(dnr-1.0);
		ms_r  = dnm*ms_r /(dnr-1.0);

		/*
		 * From notes in user manual
		 */
		errorA = c1*BDG[0][0] + c2*BDG[0][1] + c3*BDG[0][2] + c4*BDG[0][3];
		cov1A =  0.0;
		cov2A =  c1*BDG[0][4] + c2*BDG[0][5] + c3*BDG[0][6] + c4*BDG[0][7];
		cov3A =  0.0;

		errorB = c1*BDG[1][0] + c2*BDG[1][1]+ c3*BDG[1][2] + c4*BDG[1][3];
		cov1B =  0.0;
		cov2B =  c1*BDG[1][4] + c2*BDG[1][5]+ c3*BDG[1][6] + c4*BDG[1][7];
		cov3B =  0.0;

		error =  (errorA + errorB)/2.0;
		cov1 =   c1*BDG[2][0] + c2*BDG[2][1]+ c3*BDG[2][2] + c4*BDG[2][3];
		cov2 =   (cov2A  + cov2B )/2.0;
		cov3 =   c1*BDG[2][4] + c2*BDG[2][5]+ c3*BDG[2][6] + c4*BDG[2][7];

		/*
		 * From Hillis 2014 table 1, modified for single modality analysis
		 */
		var_rA = ms_rA + cov2A - errorA;
		var_mrA = ms_trA;
		OR[0][0] = var_rA;
		OR[0][1] = var_mrA;
		OR[0][2] = cov1A;
		OR[0][3] = cov2A;
		OR[0][4] = cov3A;
		OR[0][5] = errorA;

		var_rB = ms_rB + cov2B - errorB;
		var_mrB = ms_trB;
		OR[1][0] = var_rB;
		OR[1][1] = var_mrB;
		OR[1][2] = cov1B;
		OR[1][3] = cov2B;
		OR[1][4] = cov3B;
		OR[1][5] = errorB;

		var_r = (ms_r - ms_tr)/2.0 - cov1 + cov3;
		var_mr = ms_tr - error + cov1 + (cov2-cov3);
		OR[3][0] = var_r;
		OR[3][1] = var_mr;
		OR[3][2] = cov1;
		OR[3][3] = cov2;
		OR[3][4] = cov3;
		OR[3][5] = error;

		/*
		 * From Hillis 2014 Eqs 11-14 we can also write
		 */
		var_rA = DBM[0][0];
		var_mrA = 0;
		cov1A = 0;
		cov2A = DBM[0][1]/(dn0+dn1);
		cov3A = 0;
		errorA = (DBM[0][1]+DBM[0][2])/(dn0+dn1);
		//
		var_rB = DBM[1][0];
		var_mrB = 0;
		cov1B = 0;
		cov2B = DBM[1][1]/(dn0+dn1);
		cov3B = 0;
		errorB = (DBM[1][1]+DBM[1][2])/(dn0+dn1);
		//
		var_r = DBM[3][0];
		var_mr = DBM[3][3];
		cov1 = (DBM[3][1]+DBM[3][2])/(dn0+dn1);
		cov2 = (DBM[3][1]+DBM[3][4])/(dn0+dn1);
		cov3 = DBM[3][1]/(dn0+dn1);
		error = (DBM[3][1]+DBM[3][2]+DBM[3][4]+DBM[3][5])/(dn0+dn1);

	}

	/**
	 * Transforms matrix of DBM variance components into matrix of OR variance
	 * components, or vice versa
	 * 
	 * @param index Whether the in[][] parameter is DBM or OR components
	 * @param in Matrix of DBM or OR variance components
	 * @param Nreader2 Number of readers
	 * @param Nnormal2 Number of normal cases
	 * @param Ndisease2 Number of disease cases
	 * @return Matrix of OR or DBM variance components
	 */
	public static double[][] DBM2OR(int index, double[][] in, long Nreader2, long Nnormal2,
			long Ndisease2) {
		double[][] toReturn = new double[4][6];
		double[][] dbm = new double[4][6];
		double[][] orVar = new double[4][6];
		if (index == 0) {// the input is DBM;
			dbm = in;
			for (int i = 0; i < 4; i++) {
				/*
				 * orVar[i][0]=dbm[i][0]; orVar[i][1]=dbm[i][3];
				 * orVar[i][2]=dbm[i][1]+dbm[i][2];
				 * orVar[i][3]=dbm[i][1]+dbm[i][4]; orVar[i][4]=dbm[i][1];
				 * orVar[i][5]=dbm[i][1]+dbm[i][2]+dbm[i][4]+dbm[i][5];
				 */

				orVar[i][0] = dbm[i][0];
				orVar[i][1] = dbm[i][3];
				orVar[i][2] = (dbm[i][1] + dbm[i][2]) / (Nnormal2 + Ndisease2);
				orVar[i][3] = (dbm[i][1] + dbm[i][4]) / (Nnormal2 + Ndisease2);
				orVar[i][4] = dbm[i][1] / (Nnormal2 + Ndisease2);
				orVar[i][5] = (dbm[i][1] + dbm[i][2] + dbm[i][4] + dbm[i][5])
						/ (Nnormal2 + Ndisease2);

			}
			toReturn = orVar;
		} else { // the input is OR
			orVar = in;
			for (int i = 0; i < 4; i++) {
				/*
				 * dbm[i][0]=orVar[i][0]; dbm[i][1]=orVar[i][4];
				 * dbm[i][2]=orVar[i][2]-orVar[i][4]; dbm[i][3]=orVar[i][1];
				 * dbm[i][4]=orVar[i][3]-orVar[i][4];
				 * dbm[i][5]=orVar[i][5]-orVar[i][2]-orVar[i][3]+orVar[i][4];
				 */

				dbm[i][0] = orVar[i][0];
				dbm[i][1] = orVar[i][4] * (Nnormal2 + Ndisease2);
				dbm[i][2] = (orVar[i][2] - orVar[i][4]) * (Nnormal2 + Ndisease2);
				dbm[i][3] = orVar[i][1];
				dbm[i][4] = (orVar[i][3] - orVar[i][4]) * (Nnormal2 + Ndisease2);
				dbm[i][5] = (orVar[i][5] - orVar[i][2] - orVar[i][3] + orVar[i][4])
						* (Nnormal2 + Ndisease2);
			}
			toReturn = dbm;

		}
		return toReturn;
	}

	/**
	 * Resizes BCK variance components matrix, but actually currently does
	 * nothing
	 * 
	 * @param bck BCK variance components matrix
	 * @param newR Number of readers
	 * @param newN Number of normal cases
	 * @param newD Number of disease cases
	 * @return BCK variance components matrix
	 */
	public double[][] BCKresize(double[][] bck, int newR, int newN, int newD) {
		return bck;
	}

	/**
	 * Scales/resizes parts of DBM variance components matrix according to
	 * number of normal/disease cases
	 * 
	 * @param dbm DBM variance components matrix
	 * @param newR Number of readers
	 * @param newN Number of normal cases
	 * @param newD Number of disease cases
	 * @return Resized DBM variance components matrix
	 */
	public double[][] DBMresize(double[][] dbm, int newR, int newN, int newD) {
		double[][] DBMnew = new double[4][6];
		double lamda = 1.0 / Double.valueOf(newN + newD);
		for (int i = 0; i < 4; i++) {
			DBMnew[i][0] = dbm[i][0];
			DBMnew[i][1] = dbm[i][1] * lamda;
			DBMnew[i][2] = dbm[i][2] * lamda;
			DBMnew[i][3] = dbm[i][3];
			DBMnew[i][4] = dbm[i][4] * lamda;
			DBMnew[i][5] = dbm[i][5] * lamda;
		}

		return DBMnew;
	}


	
	public static void copy(DBRecord DBRecordTemp, DBRecord copyDBRecordTemp) {

		copyDBRecordTemp.AUCs = Matrix.copy(DBRecordTemp.AUCs);
		copyDBRecordTemp.AUCsReaderAvg = Matrix.copy(DBRecordTemp.AUCsReaderAvg);

		copyDBRecordTemp.BDG = Matrix.copy(DBRecordTemp.BDG);
		copyDBRecordTemp.BDGbias = Matrix.copy(DBRecordTemp.BDGbias);
		copyDBRecordTemp.BDGcoeff = Matrix.copy(DBRecordTemp.BDGcoeff);
		
		copyDBRecordTemp.testStat.ciBotNormal  = DBRecordTemp.testStat.ciBotNormal; 
		copyDBRecordTemp.testStat.ciBotBDG     = DBRecordTemp.testStat.ciBotBDG; 
		copyDBRecordTemp.testStat.ciBotHillis  = DBRecordTemp.testStat.ciBotHillis; 

		copyDBRecordTemp.testStat.ciTopNormal  = DBRecordTemp.testStat.ciTopNormal; 
		copyDBRecordTemp.testStat.ciTopBDG     = DBRecordTemp.testStat.ciTopBDG; 
		copyDBRecordTemp.testStat.ciTopHillis  = DBRecordTemp.testStat.ciTopHillis; 

		copyDBRecordTemp.testStat.cutoffNormal = DBRecordTemp.testStat.cutoffNormal; 
		copyDBRecordTemp.testStat.cutoffBDG    = DBRecordTemp.testStat.cutoffBDG; 
		copyDBRecordTemp.testStat.cutoffHillis = DBRecordTemp.testStat.cutoffHillis; 

		copyDBRecordTemp.testStat.DF_BDG       = DBRecordTemp.testStat.DF_BDG; 
		copyDBRecordTemp.testStat.DF_Hillis    = DBRecordTemp.testStat.DF_Hillis; 

		copyDBRecordTemp.testStat.pValNormal   = DBRecordTemp.testStat.pValNormal;
		copyDBRecordTemp.testStat.pValBDG      = DBRecordTemp.testStat.pValBDG;
		copyDBRecordTemp.testStat.pValHillis   = DBRecordTemp.testStat.pValHillis;
		
		copyDBRecordTemp.testStat.tStatEst     = DBRecordTemp.testStat.tStatEst;		
		copyDBRecordTemp.testStat.rejectNormal = DBRecordTemp.testStat.rejectNormal;
		copyDBRecordTemp.testStat.rejectBDG    = DBRecordTemp.testStat.rejectBDG;
		copyDBRecordTemp.testStat.rejectHillis = DBRecordTemp.testStat.rejectHillis;
		
		/**
		 * TODO we need to collect totalVar for modalityA and modalityB
		 */
		copyDBRecordTemp.totalVar = DBRecordTemp.totalVar;
		copyDBRecordTemp.flagTotalVarIsNegative = DBRecordTemp.flagTotalVarIsNegative;

	}
	
	public static void add(DBRecord DBRecordTemp, DBRecord sumDBRecordTemp) {

		sumDBRecordTemp.AUCs = 
				Matrix.add(sumDBRecordTemp.AUCs, DBRecordTemp.AUCs);
		sumDBRecordTemp.AUCsReaderAvg = 
				Matrix.add(sumDBRecordTemp.AUCsReaderAvg, DBRecordTemp.AUCsReaderAvg);
		
		sumDBRecordTemp.BDG = 
				Matrix.add(sumDBRecordTemp.BDG, DBRecordTemp.BDG);
		sumDBRecordTemp.BDGbias = 
				Matrix.add(sumDBRecordTemp.BDGbias, DBRecordTemp.BDGbias);
		sumDBRecordTemp.BDGcoeff = 
				Matrix.add(sumDBRecordTemp.BDGcoeff, DBRecordTemp.BDGcoeff);
		
		sumDBRecordTemp.testStat.ciBotNormal  += DBRecordTemp.testStat.ciBotNormal; 
		sumDBRecordTemp.testStat.ciBotBDG     += DBRecordTemp.testStat.ciBotBDG; 
		sumDBRecordTemp.testStat.ciBotHillis  += DBRecordTemp.testStat.ciBotHillis; 

		sumDBRecordTemp.testStat.ciTopNormal  += DBRecordTemp.testStat.ciTopNormal; 
		sumDBRecordTemp.testStat.ciTopBDG     += DBRecordTemp.testStat.ciTopBDG; 
		sumDBRecordTemp.testStat.ciTopHillis  += DBRecordTemp.testStat.ciTopHillis; 

		sumDBRecordTemp.testStat.cutoffNormal += DBRecordTemp.testStat.cutoffNormal; 
		sumDBRecordTemp.testStat.cutoffBDG    += DBRecordTemp.testStat.cutoffBDG; 
		sumDBRecordTemp.testStat.cutoffHillis += DBRecordTemp.testStat.cutoffHillis; 

		sumDBRecordTemp.testStat.DF_BDG       += DBRecordTemp.testStat.DF_BDG; 
		sumDBRecordTemp.testStat.DF_Hillis    += DBRecordTemp.testStat.DF_Hillis; 

		sumDBRecordTemp.testStat.pValNormal   += DBRecordTemp.testStat.pValNormal;
		sumDBRecordTemp.testStat.pValBDG      += DBRecordTemp.testStat.pValBDG;
		sumDBRecordTemp.testStat.pValHillis   += DBRecordTemp.testStat.pValHillis;
		
		sumDBRecordTemp.testStat.tStatEst     += DBRecordTemp.testStat.tStatEst;		
		sumDBRecordTemp.testStat.rejectNormal += DBRecordTemp.testStat.rejectNormal;
		sumDBRecordTemp.testStat.rejectBDG    += DBRecordTemp.testStat.rejectBDG;
		sumDBRecordTemp.testStat.rejectHillis += DBRecordTemp.testStat.rejectHillis;

		/**
		 * TODO we need to collect totalVar for modalityA and modalityB
		 */
		sumDBRecordTemp.totalVar += DBRecordTemp.totalVar;
		sumDBRecordTemp.flagTotalVarIsNegative += DBRecordTemp.flagTotalVarIsNegative;
		
	}
	
	public static void scale(DBRecord DBRecordTemp, double scaleFactor) {
	
		DBRecordTemp.AUCs = 
				Matrix.scale(DBRecordTemp.AUCs, scaleFactor);
		DBRecordTemp.AUCsReaderAvg = 
				Matrix.scale(DBRecordTemp.AUCsReaderAvg, scaleFactor);
		
		DBRecordTemp.BDG = 
				Matrix.scale(DBRecordTemp.BDG, scaleFactor);
		DBRecordTemp.BDGbias = 
				Matrix.scale(DBRecordTemp.BDGbias, scaleFactor);
		DBRecordTemp.BDGcoeff = 
				Matrix.scale(DBRecordTemp.BDGcoeff, scaleFactor);
	
		DBRecordTemp.testStat.ciBotNormal  *= scaleFactor; 
		DBRecordTemp.testStat.ciBotBDG     *= scaleFactor;
		DBRecordTemp.testStat.ciBotHillis  *= scaleFactor;
	
		DBRecordTemp.testStat.ciTopNormal  *= scaleFactor;
		DBRecordTemp.testStat.ciTopBDG     *= scaleFactor;
		DBRecordTemp.testStat.ciTopHillis  *= scaleFactor;
	
		DBRecordTemp.testStat.cutoffNormal *= scaleFactor;
		DBRecordTemp.testStat.cutoffBDG    *= scaleFactor;
		DBRecordTemp.testStat.cutoffHillis *= scaleFactor;
	
		DBRecordTemp.testStat.DF_BDG       *= scaleFactor;
		DBRecordTemp.testStat.DF_Hillis    *= scaleFactor;
	
		DBRecordTemp.testStat.pValNormal   *= scaleFactor;
		DBRecordTemp.testStat.pValBDG      *= scaleFactor;
		DBRecordTemp.testStat.pValHillis   *= scaleFactor;
		
		DBRecordTemp.testStat.tStatEst     *= scaleFactor;	
		DBRecordTemp.testStat.rejectNormal *= scaleFactor;
		DBRecordTemp.testStat.rejectBDG    *= scaleFactor;
		DBRecordTemp.testStat.rejectHillis *= scaleFactor;

		/**
		 * TODO we need to collect totalVar for modalityA and modalityB
		 */
		DBRecordTemp.totalVar *= scaleFactor;
		DBRecordTemp.flagTotalVarIsNegative *= scaleFactor;
		
	}

	public static void square(DBRecord DBRecordTemp) {

		DBRecordTemp.AUCs = 
				Matrix.squareTerms(DBRecordTemp.AUCs);
		DBRecordTemp.AUCsReaderAvg = 
				Matrix.squareTerms(DBRecordTemp.AUCsReaderAvg);

		DBRecordTemp.BDG = Matrix.squareTerms(DBRecordTemp.BDG);
		DBRecordTemp.BDGbias = Matrix.squareTerms(DBRecordTemp.BDGbias);
		DBRecordTemp.BDGcoeff = Matrix.squareTerms(DBRecordTemp.BDGcoeff);
		
		DBRecordTemp.testStat.ciBotNormal  *= DBRecordTemp.testStat.ciBotNormal; 
		DBRecordTemp.testStat.ciBotBDG     *= DBRecordTemp.testStat.ciBotBDG; 
		DBRecordTemp.testStat.ciBotHillis  *= DBRecordTemp.testStat.ciBotHillis; 

		DBRecordTemp.testStat.ciTopNormal  *= DBRecordTemp.testStat.ciTopNormal; 
		DBRecordTemp.testStat.ciTopBDG     *= DBRecordTemp.testStat.ciTopBDG; 
		DBRecordTemp.testStat.ciTopHillis  *= DBRecordTemp.testStat.ciTopHillis; 

		DBRecordTemp.testStat.cutoffNormal *= DBRecordTemp.testStat.cutoffNormal; 
		DBRecordTemp.testStat.cutoffBDG    *= DBRecordTemp.testStat.cutoffBDG; 
		DBRecordTemp.testStat.cutoffHillis *= DBRecordTemp.testStat.cutoffHillis; 

		DBRecordTemp.testStat.DF_BDG       *= DBRecordTemp.testStat.DF_BDG; 
		DBRecordTemp.testStat.DF_Hillis    *= DBRecordTemp.testStat.DF_Hillis; 

		DBRecordTemp.testStat.pValNormal   *= DBRecordTemp.testStat.pValNormal;
		DBRecordTemp.testStat.pValBDG      *= DBRecordTemp.testStat.pValBDG;
		DBRecordTemp.testStat.pValHillis   *= DBRecordTemp.testStat.pValHillis;
		
		DBRecordTemp.testStat.tStatEst     *= DBRecordTemp.testStat.tStatEst;
		DBRecordTemp.testStat.rejectNormal *= DBRecordTemp.testStat.rejectNormal;
		DBRecordTemp.testStat.rejectBDG    *= DBRecordTemp.testStat.rejectBDG;
		DBRecordTemp.testStat.rejectHillis *= DBRecordTemp.testStat.rejectHillis;

		/**
		 * TODO we need to collect totalVar for modalityA and modalityB
		 */
		DBRecordTemp.totalVar *= DBRecordTemp.totalVar;
		DBRecordTemp.flagTotalVarIsNegative *= DBRecordTemp.flagTotalVarIsNegative;
		
	}


	


	

}
