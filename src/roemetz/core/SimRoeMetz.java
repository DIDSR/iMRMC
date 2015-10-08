/**
 * SimRoeMetz.java
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

package roemetz.core;

import java.io.IOException;
import java.util.Arrays;











import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.randvar.RandomVariateGen;
import umontreal.iro.lecuyer.rng.RandomStream;
import umontreal.iro.lecuyer.rng.WELL1024;
import mrmc.core.CovMRMC;
import mrmc.core.DBRecord;
import mrmc.core.InputFile;
import mrmc.core.Matrix;
import mrmc.gui.SizePanel;

/**
 * Simulates scores according to Roe and Metz simulation. Adapted for java from
 * sim_roemetz.pro (Brandon D. Gallas, PhD)
 * 
 * @author Rohan Pathare
 */
public class SimRoeMetz {
	
	private DBRecord DBRecordStat;
	private InputFile InputFileStat;
	private SizePanel sizePanel1;
	
	/**
	 * Data from one simulation experiment based on the Roe & Metz model <br>
	 * -- String[Nobservations][4], each observation has readerID, caseID, modalityID, score <br>
	 */
	private String[][] observerData;
	
	RandomStream RandomStreamI;
	RandomVariateGen gaussRV;
	
	private int Nnormal, Ndisease, Nreader;
	private double[] scoreMeans;
	private double[] scoreVariances;

	private double[][] tA0, tB0, tA1, tB1;
	private int[][] dA0, dB0, dA1, dB1;
	private double[] auc;
	private double[][] BDG;
	private double[][] BCK;
	private double[][] DBM;
	private double[][] OR;
	private double[][] MS;

	/**
	 * Sole constructor. Upon invocation, verifies the parameters, initializes
	 * class members, performs simulation experiment, and processes the results.
	 * 
	 * @param tempScoreMeans Contains experiment means. Has 2 elements.
	 * @param tempScoreVar Contains variance components. Has 18 elements.
	 * @param RandomStream0 Random numbergenerator at its current state
	 * @throws IOException 
	 */
	public SimRoeMetz(double[] tempScoreMeans, double[] tempScoreVar,
			RandomStream RandomStream0, SizePanel sizePanel1Temp) throws IOException {
		
		sizePanel1 = sizePanel1Temp;
		
		if (tempScoreMeans.length != 2) {
			System.out.println("input scoreMeans is of incorrect size");
			return;
		}
		if (tempScoreVar.length != 18) {
			System.out.println("input tempScoreVar is of incorrect size");
			return;
		}

		scoreMeans = tempScoreMeans;
		scoreVariances = tempScoreVar;
		Nreader = Integer.parseInt(sizePanel1Temp.NreaderJTextField.getText());
		Nnormal = Integer.parseInt(sizePanel1Temp.NnormalJTextField.getText());
		Ndisease = Integer.parseInt(sizePanel1Temp.NdiseaseJTextField.getText());

		// Get study design matrix
		CovMRMC covMRMC = new CovMRMC(sizePanel1);

		// The first rows of observerData will be the truth status of each case
		// The remaining rows are the reader-by-case observations determined by the study design
		int nrows = Nnormal + Ndisease;
		dA0 = new int[Nreader][Nnormal];
		dB0 = new int[Nreader][Nnormal];
		dA1 = new int[Nreader][Ndisease];
		dB1 = new int[Nreader][Ndisease];
		for(int r=0; r<Nreader; r++) {
			for(int i=0; i<Nnormal; i++) {
				dA0[r][i] = covMRMC.d0_modAA[i][r][0];
				dB0[r][i] = covMRMC.d0_modBB[i][r][0];
				nrows += dA0[r][i] + dB0[r][i];
			}
			for(int j=0; j<Ndisease; j++) {
				dA1[r][j] = covMRMC.d1_modAA[j][r][0];
				dB1[r][j] = covMRMC.d1_modBB[j][r][0];
				nrows += dA1[r][j] + dB1[r][j];
			}
		}
			
		/*
		 * Create the array as it would result from an input file.
		 * nrows includes n0+n1 rows defining truth
		 * plus 2 modalities * nr * (n0+n1)
		 */
		InputFileStat = new InputFile();
		InputFileStat.observerData = new String[nrows][4];
		
		RandomStreamI = RandomStream0;
		gaussRV = new NormalGen(RandomStreamI);
		
		System.out.print("ThreadName:"+Thread.currentThread().getName()+":");
		//each thread prints 5 random numbers for testing proper threading and reproducibility
        for(int j = 0 ; j < 5; j++) {
            System.out.print( RandomStreamI.nextInt(1, 100) + ",");
        }
        System.out.println();

	}

	/**
	 * Used when calling SimRoeMetz as a standalone application via
	 * command-line.
	 * 
	 * @param args command-line arguments. First element is experiment means,
	 *            second element is components of variance, third element is
	 *            experiment sizes, fourth element is seed for RNG, fifth
	 *            element specifies whether biased estimates will be used.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		try {
			
			SizePanel sizePanel1 = new SizePanel();

			double[] u = new double[2];
			String[] us = args[0].substring(args[0].lastIndexOf("[") + 1,
					args[0].indexOf("]")).split(",");
			if (us.length != 2) {
				System.out.println("Expected input u to contain 2 elements");
				return;
			} else {
				u = new double[] { Double.parseDouble(us[0]),
						Double.parseDouble(us[1]) };
			}
			double[] var_t = new double[18];
			String[] var_ts = args[1].substring(args[1].indexOf("[") + 1,
					args[1].indexOf("]")).split(",");
			if (var_ts.length != 18) {
				System.out
						.println("Expected input var_t to contain 18 elements");
				return;
			} else {
				for (int i = 0; i < var_ts.length; i++) {
					var_t[i] = Double.parseDouble(var_ts[i]);
				}
			}
			int Nreader, Nnormal, Ndisease;
			String[] ns = args[2].substring(args[2].indexOf("[") + 1,
					args[2].indexOf("]")).split(",");
			if (ns.length != 3) {
				System.out.println("Expected input n to contain 3 elements");
				return;
			} else {
				Nnormal = Integer.parseInt(ns[0]);
				Ndisease = Integer.parseInt(ns[1]);
				Nreader = Integer.parseInt(ns[2]);
			}
			sizePanel1.NreaderJTextField.setText(String.valueOf(Nreader));
			sizePanel1.NnormalJTextField.setText(String.valueOf(Nnormal));
			sizePanel1.NdiseaseJTextField.setText(String.valueOf(Ndisease));

			/*
			 * SSJ: Simulation Stochastique in Java
			 * http://simul.iro.umontreal.ca/ssj
			 * 
			 * The actual random number generators (RNGs) are provided in
			 * classes that implement this RandomStream interface. Each
			 * stream of random numbers is an object of the class that 
			 * implements this interface, and can be viewed as a virtual
			 * random number generator.
			 * ...
			 * Each time a new RandomStream is created, its starting point
			 * (initial seed) is computed automatically, Z steps ahead of
			 * the starting point of the previously created stream of the
			 * same type, and its current state is set equal to this starting point.
			 */
			int[] seedIntArr32 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 
				     10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 
				     20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31} ;
			//seedIntArr32[0] = Integer.parseInt(JTextField_seed.getText());
			WELL1024.setPackageSeed(seedIntArr32);
			WELL1024 RandomStreamI = new WELL1024();

			// Run the simulation
			SimRoeMetz exp = new SimRoeMetz(u, var_t, RandomStreamI, sizePanel1);
			// Print the results
			exp.printResults();
			
		} catch (NumberFormatException e) {
			System.out.println("Incorrectly Formatted Input");
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Missing Arguments");
			System.out
					.println("Format is: SimRoeMetz [u0,u1] [R00,C00,RC00,R10,"
							+ "C10,RC10,R01,C01,RC01,R11,C11,RC11,R0,C0,RC0,R1,"
							+ "C1,RC1] [Nnormal,Ndisease,Nreader] sNreaderd useMLE");
			e.printStackTrace();
		}
	}

	/**
	 * Prints the results of a simulation experiment to standard out. Only used
	 * when main method for this class is invoked.
	 */
	private void printResults() {
		System.out.println("BDG:");
		Matrix.printMatrix(BDG);
		System.out.println();
		System.out.println("AUCs:");
		Matrix.printVector(auc);
		System.out.println();
	}

	/**
	 * Given {@link #scoreMeans}, {@link #scoreVariances},
	 *    {@link #Nreader}, {@link #Nnormal}, {@link #Ndisease} <br>
	 * Create {@link #observerData} then <br>
	 * -- {@link mrmc.core.InputFile#verifySizesAndGetIDs(boolean)} <br>
	 * -- {@link mrmc.core.InputFile#processScoresAndTruth(boolean)} <br>
	 * -- {@link mrmc.core.DBRecord#DBRecordStatFill(InputFile, DBRecord)} <br>
	 * <br>
	 * CALLED FROM: {@link roemetz.gui.RMGUInterface.SimExperiments_thread#doInBackground()} <br>
	 * 
	 * @throws IOException 
	 */
	public void doSim(DBRecord tempDBRecord) throws IOException {

		DBRecordStat = tempDBRecord;
		observerData = InputFileStat.observerData;
		
		double[] RA0 = fillGaussian(Math.sqrt(scoreVariances[0]), Nreader);
		double[] CA0 = fillGaussian(Math.sqrt(scoreVariances[1]), Nnormal);
		double[][] RCA0 = fillGaussian(Math.sqrt(scoreVariances[2]), Nreader, Nnormal);
		double[] RA1 = fillGaussian(Math.sqrt(scoreVariances[3]), Nreader);
		double[] CA1 = fillGaussian(Math.sqrt(scoreVariances[4]), Ndisease);
		double[][] RCA1 = fillGaussian(Math.sqrt(scoreVariances[5]), Nreader, Ndisease);
		double[] RB0 = fillGaussian(Math.sqrt(scoreVariances[6]), Nreader);
		double[] CB0 = fillGaussian(Math.sqrt(scoreVariances[7]), Nnormal);
		double[][] RCB0 = fillGaussian(Math.sqrt(scoreVariances[8]), Nreader, Nnormal);
		double[] RB1 = fillGaussian(Math.sqrt(scoreVariances[9]), Nreader);
		double[] CB1 = fillGaussian(Math.sqrt(scoreVariances[10]), Ndisease);
		double[][] RCB1 = fillGaussian(Math.sqrt(scoreVariances[11]), Nreader, Ndisease);
		double[] R0 = fillGaussian(Math.sqrt(scoreVariances[12]), Nreader);
		double[] C0 = fillGaussian(Math.sqrt(scoreVariances[13]), Nnormal);
		double[][] RC0 = fillGaussian(Math.sqrt(scoreVariances[14]), Nreader, Nnormal);
		double[] R1 = fillGaussian(Math.sqrt(scoreVariances[15]), Nreader);
		double[] C1 = fillGaussian(Math.sqrt(scoreVariances[16]), Ndisease);
		double[][] RC1 = fillGaussian(Math.sqrt(scoreVariances[17]), Nreader, Ndisease);

		tA0 = new double[(int) Nreader][(int) Nnormal];
		tB0 = new double[(int) Nreader][(int) Nnormal];
		tA1 = new double[(int) Nreader][(int) Ndisease];
		tB1 = new double[(int) Nreader][(int) Ndisease];

		for (int i = 0; i < Nreader; i++) {
			Arrays.fill(tA1[i], scoreMeans[0]);
			Arrays.fill(tB1[i], scoreMeans[1]);
		}

		/*
		 * Create the rows defining truth states
		 */
		int irow=0;
		for(int normalID=0; normalID<Nnormal; normalID++) {
			observerData[irow][0] = "-1";
//			observerData[irow][1] = "normal"+Integer.toString(normalID);
			observerData[irow][1] = "normal"+String.format("%06d", normalID);
			observerData[irow][2] = "truth";
			observerData[irow][3] = Integer.toString(0);
			irow++;
		}
		for(int diseaseID=0; diseaseID<Ndisease; diseaseID++) {
			observerData[irow][0] = "-1";
//			observerData[irow][1] = "disease"+Integer.toString(diseaseID);
			observerData[irow][1] = "disease"+String.format("%06d", diseaseID);
			observerData[irow][2] = "truth";
			observerData[irow][3] = Integer.toString(1);
			irow++;
		}
		
		/*
		 * Create the observation data
		 */
		for (int readerID = 0; readerID < Nreader; readerID++) {
			for (int normalID = 0; normalID < Nnormal; normalID++) {
				
				if(dA0[readerID][normalID] == 1) {
					tA0[readerID][normalID] += R0[readerID] + C0[normalID] + RA0[readerID]
							+ CA0[normalID] + RC0[readerID][normalID] + RCA0[readerID][normalID];
//					observerData[irow][0] = "reader"+Integer.toString(readerID);
					observerData[irow][0] = "reader"+String.format("%03d", readerID);
//					observerData[irow][1] = "normal"+Integer.toString(normalID);
					observerData[irow][1] = "normal"+String.format("%06d", normalID);
					observerData[irow][2] = DBRecordStat.modalityA;
					observerData[irow][3] = Double.toString(tA0[readerID][normalID]);
					irow++;
				}
				if(dB0[readerID][normalID] == 1) {
					tB0[readerID][normalID] += R0[readerID] + C0[normalID] + RB0[readerID]
							+ CB0[normalID] + RC0[readerID][normalID] + RCB0[readerID][normalID];
//					observerData[irow][0] = "reader"+Integer.toString(readerID);
					observerData[irow][0] = "reader"+String.format("%03d", readerID);
//					observerData[irow][1] = "normal"+Integer.toString(normalID);
					observerData[irow][1] = "normal"+String.format("%06d", normalID);
					observerData[irow][2] = DBRecordStat.modalityB;
					observerData[irow][3] = Double.toString(tB0[readerID][normalID]);
					irow++;
				}
			}
			for (int diseaseID = 0; diseaseID < Ndisease; diseaseID++) {
				if(dA1[readerID][diseaseID] == 1) {
					tA1[readerID][diseaseID] += R1[readerID] + C1[diseaseID] + RA1[readerID]
							+ CA1[diseaseID] + RC1[readerID][diseaseID] + RCA1[readerID][diseaseID];
//					observerData[irow][0] = "reader"+Integer.toString(readerID);
					observerData[irow][0] = "reader"+String.format("%03d", readerID);
//					observerData[irow][1] = "disease"+Integer.toString(diseaseID);
					observerData[irow][1] = "disease"+String.format("%06d", diseaseID);
					observerData[irow][2] = DBRecordStat.modalityA;
					observerData[irow][3] = Double.toString(tA1[readerID][diseaseID]);
					irow++;
				}
				if(dB1[readerID][diseaseID] == 1) {
					tB1[readerID][diseaseID] += R1[readerID] + C1[diseaseID] + RB1[readerID]
							+ CB1[diseaseID] + RC1[readerID][diseaseID] + RCB1[readerID][diseaseID];
//					observerData[irow][0] = "reader"+Integer.toString(readerID);
					observerData[irow][0] = "reader"+String.format("%03d", readerID);
//					observerData[irow][1] = "disease"+Integer.toString(diseaseID);
					observerData[irow][1] = "disease"+String.format("%06d", diseaseID);
				observerData[irow][2] = DBRecordStat.modalityB;
					observerData[irow][3] = Double.toString(tB1[readerID][diseaseID]);
					irow++;
				}
			}
		}
		
		boolean VerboseFalse = false;
		InputFileStat.resetIDs();
		InputFileStat.verifySizesAndGetIDs(VerboseFalse);
		InputFileStat.processScoresAndTruth(VerboseFalse);

		DBRecordStat.DBRecordStatFill(InputFileStat, DBRecordStat);

		}

	/**
	 * Fills a vector with x random numbers according to a Gaussian
	 * distribution.
	 * 
	 * @param scalar Width of distribution
	 * @param Nreader2 length of vector to fill
	 * @return 1-d array containing random gaussian numbers
	 */
	public double[] fillGaussian(double scalar, long Nreader2) {
		double[] toReturn = new double[(int) Nreader2];
		for (int i = 0; i < Nreader2; i++) {
			toReturn[i] = scalar * gaussRV.nextDouble();
		}
		return toReturn;
	}

	/**
	 * Fills a matrix with x * y random numbers according to a Gaussian
	 * distribution.
	 * 
	 * @param scalar Width of distribution
	 * @param Nreader2 length of array to fill
	 * @param Nnormal2 width of array to fill
	 * @return 2-d array containing random gaussian numbers
	 */
	public double[][] fillGaussian(double scalar, long Nreader2,
			long Nnormal2) {
		double[][] toReturn = new double[(int) Nreader2][(int) Nnormal2];
		for (int i = 0; i < Nreader2; i++) {
			for (int j = 0; j < Nnormal2; j++) {
				toReturn[i][j] = scalar * gaussRV.nextDouble();
			}
		}
		return toReturn;
	}

}
