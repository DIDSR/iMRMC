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
import java.util.Random;

import mrmc.core.DBRecord;
import mrmc.core.InputFile;
import mrmc.core.Matrix;

/**
 * Simulates scores according to Roe and Metz simulation. Adapted for java from
 * sim_roemetz.pro (Brandon D. Gallas, PhD)
 * 
 * @author Rohan Pathare
 */
public class SimRoeMetz {
	
	private String[][] fData;
	private double[][] tA0;
	private double[][] tB0;
	private double[][] tA1;
	private double[][] tB1;
	private double[] auc;
	private double[][] BDG;
	private double[][] BCK;
	private double[][] DBM;
	private double[][] OR;
	private double[][] MS;
	private long n0;
	private long n1;
	private long nr;
	private double u0;
	private double u1;
	private int useMLE;

	/**
	 * Sole constructor. Upon invocation, verifies the parameters, initializes
	 * class members, performs simulation experiment, and processes the results.
	 * 
	 * @param u Contains experiment means. Has 2 elements.
	 * @param var_t Contains variance components. Has 18 elements.
	 * @param n Contains experiment sizes. Has 3 elements.
	 * @param rand Random number generator initialized with seed from GUI. No
	 *            guarantee that the generator is at any particular position
	 *            within its sequence for a given seed.
	 * @param useMLEs Indicates whether to used biased estimates in the results
	 *            of the simulation
	 * @throws IOException 
	 */
	public SimRoeMetz(double[] u, double[] var_t, long[] n, Random rand,
			int useMLEs) throws IOException {
		if (u.length != 2) {
			System.out.println("input u is of incorrect size");
			return;
		}
		if (var_t.length != 18) {
			System.out.println("input var_t is of incorrect size");
			return;
		}
		if (n.length != 3) {
			System.out.println("input n is of incorrect size");
			return;
		}

		n0 = n[0];
		n1 = n[1];
		nr = n[2];
		u0 = u[0];
		u1 = u[1];
		useMLE = useMLEs;

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
			long[] n = new long[3];
			String[] ns = args[2].substring(args[2].indexOf("[") + 1,
					args[2].indexOf("]")).split(",");
			if (ns.length != 3) {
				System.out.println("Expected input n to contain 3 elements");
				return;
			} else {
				for (int i = 0; i < ns.length; i++) {
					n[i] = Integer.parseInt(ns[i]);
				}
			}
			Random rand = new Random(Long.parseLong(args[3]));
			int useMLE = Integer.parseInt(args[4]);
			SimRoeMetz exp = new SimRoeMetz(u, var_t, n, rand, useMLE);
			exp.printResults();
		} catch (NumberFormatException e) {
			System.out.println("Incorrectly Formatted Input");
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Missing Arguments");
			System.out
					.println("Format is: SimRoeMetz [u0,u1] [R00,C00,RC00,R10,"
							+ "C10,RC10,R01,C01,RC01,R11,C11,RC11,R0,C0,RC0,R1,"
							+ "C1,RC1] [n0,n1,nr] seed useMLE");
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
	 * Get the AUCs for this experiment
	 * 
	 * @return AUCs for this experiment
	 */
	public double[] getAUC() {
		return auc;
	}

	/**
	 * Get the BDG decomposition for this experiment
	 * 
	 * @return BDG decomposition of experiment results
	 */
	public double[][] getBDGdata() {
		return BDG;
	}

	/**
	 * Get the BCK decomposition for this experiment
	 * 
	 * @return BCK decomposition of experiment results
	 */
	public double[][] getBCKdata() {
		return BCK;
	}

	/**
	 * Get the DBM decomposition for this experiment
	 * 
	 * @return DBM decomposition of experiment results
	 */
	public double[][] getDBMdata() {
		return DBM;
	}

	/**
	 * Get the OR decomposition for this experiment
	 * 
	 * @return OR decomposition of experiment results
	 */
	public double[][] getORdata() {
		return OR;
	}

	/**
	 * Get the MS decomposition for this experiment
	 * 
	 * @return MS decomposition of experiment results
	 */
	public double[][] getMSdata() {
		return MS;
	}

	/**
	 * Get the simulated scores for normal cases, modality 0
	 * 
	 * @return Simulated scores for truth=0, modality=0
	 */
	public double[][] gettA0() {
		return tA0;
	}

	/**
	 * Get the simulated scores for disease cases, modality 0
	 * 
	 * @return Simulated scores for modality=A, truth=1 
	 */
	public double[][] gettA1() {
		return tA1;
	}

	/**
	 * Get the simulated scores for normal cases, modality 1
	 * 
	 * @return Simulated scores for truth=0, modality=1
	 */
	public double[][] gettB0() {
		return tB0;
	}

	/**
	 * Get the simulated scores for disease cases, modality 1
	 * 
	 * @return Simulated scores for truth=1, modality=1
	 */
	public double[][] gettB1() {
		return tB1;
	}

	/**
	 * Performs one simulation experiment based on the Roe & Metz model
	 * generalized for 18 variances. Fills four t-matrices (class members tA0,
	 * tB0, tA1, tB1) with results
	 * 
	 * @param rand Random number generator initialized with seed from GUI. No
	 *            guarantee that the generator is at any particular position
	 *            within its sequence for a given seed.
	 */
	public void doSim(double[] var_t, Random rand) {
		double[] stdDevs = new double[var_t.length];
		for (int i = 0; i < var_t.length; i++) {
			stdDevs[i] = Math.sqrt(var_t[i]);
		}

		double[] RA0 = fillGaussian(stdDevs[0], rand, nr);
		double[] CA0 = fillGaussian(stdDevs[1], rand, n0);
		double[][] RCA0 = fillGaussian(stdDevs[2], rand, nr, n0);
		double[] RA1 = fillGaussian(stdDevs[3], rand, nr);
		double[] CA1 = fillGaussian(stdDevs[4], rand, n1);
		double[][] RCA1 = fillGaussian(stdDevs[5], rand, nr, n1);
		double[] RB0 = fillGaussian(stdDevs[6], rand, nr);
		double[] CB0 = fillGaussian(stdDevs[7], rand, n0);
		double[][] RCB0 = fillGaussian(stdDevs[8], rand, nr, n0);
		double[] RB1 = fillGaussian(stdDevs[9], rand, nr);
		double[] CB1 = fillGaussian(stdDevs[10], rand, n1);
		double[][] RCB1 = fillGaussian(stdDevs[11], rand, nr, n1);
		double[] R0 = fillGaussian(stdDevs[12], rand, nr);
		double[] C0 = fillGaussian(stdDevs[13], rand, n0);
		double[][] RC0 = fillGaussian(stdDevs[14], rand, nr, n0);
		double[] R1 = fillGaussian(stdDevs[15], rand, nr);
		double[] C1 = fillGaussian(stdDevs[16], rand, n1);
		double[][] RC1 = fillGaussian(stdDevs[17], rand, nr, n1);

		tA0 = new double[(int) nr][(int) n0];
		tB0 = new double[(int) nr][(int) n0];
		tA1 = new double[(int) nr][(int) n1];
		tB1 = new double[(int) nr][(int) n1];

		for (int i = 0; i < nr; i++) {
			Arrays.fill(tA1[i], u0);
			Arrays.fill(tB1[i], u1);
		}

		/*
		 * Create the array as it would appear in an input file.
		 * nrows includes n0+n1 rows defining truth
		 * plus 2 modalities * nr * (n0+n1)
		 */
		int nrows = (int) (2 * nr * (n0+n1) + (n0+n1));
		fData = new String[nrows][4];

		/*
		 * Create the rows defining truth states
		 */
		int irow=0;
		for(int normalID=0; normalID<n0; normalID++) {
			fData[irow][0] = "-1";
			fData[irow][1] = "normal"+Integer.toString(normalID);
			fData[irow][2] = "truth";
			fData[irow][3] = Integer.toString(0);
			irow++;
		}
		for(int diseaseID=0; diseaseID<n1; diseaseID++) {
			fData[irow][0] = "-1";
			fData[irow][1] = "disease"+Integer.toString(diseaseID);
			fData[irow][2] = "truth";
			fData[irow][3] = Integer.toString(1);
			irow++;
		}
		
		for (int readerID = 0; readerID < nr; readerID++) {
			for (int normalID = 0; normalID < n0; normalID++) {
				tA0[readerID][normalID] += R0[readerID] + C0[normalID] + RA0[readerID]
						+ CA0[normalID] + RC0[readerID][normalID] + RCA0[readerID][normalID];
				fData[irow][0] = Integer.toString(readerID);
				fData[irow][1] = "normal"+Integer.toString(normalID);
				fData[irow][2] = "ModalityA";
				fData[irow][3] = Double.toString(tA0[readerID][normalID]);
				irow++;

				tB0[readerID][normalID] += R0[readerID] + C0[normalID] + RB0[readerID]
						+ CB0[normalID] + RC0[readerID][normalID] + RCB0[readerID][normalID];
				fData[irow][0] = Integer.toString(readerID);
				fData[irow][1] = "normal"+Integer.toString(normalID);
				fData[irow][2] = "ModalityB";
				fData[irow][3] = Double.toString(tB0[readerID][normalID]);
				irow++;
			}
			for (int diseaseID = 0; diseaseID < n1; diseaseID++) {
				tA1[readerID][diseaseID] += R1[readerID] + C1[diseaseID] + RA1[readerID]
						+ CA1[diseaseID] + RC1[readerID][diseaseID] + RCA1[readerID][diseaseID];
				fData[irow][0] = Integer.toString(readerID);
				fData[irow][1] = "disease"+Integer.toString(diseaseID);
				fData[irow][2] = "ModalityA";
				fData[irow][3] = Double.toString(tA1[readerID][diseaseID]);
				irow++;
				
				tB1[readerID][diseaseID] += R1[readerID] + C1[diseaseID] + RB1[readerID]
						+ CB1[diseaseID] + RC1[readerID][diseaseID] + RCB1[readerID][diseaseID];
				fData[irow][0] = Integer.toString(readerID);
				fData[irow][1] = "disease"+Integer.toString(diseaseID);
				fData[irow][2] = "ModalityB";
				fData[irow][3] = Double.toString(tB1[readerID][diseaseID]);
				irow++;
			}
		}

//		tA0 = Matrix.matrixAdd(tA0, RC0);
//		tB0 = Matrix.matrixAdd(tB0, RC0);
//		tA0 = Matrix.matrixAdd(tA0, RCA0);
//		tB0 = Matrix.matrixAdd(tB0, RCB0);

//		tA1 = Matrix.matrixAdd(tA1, RC1);
//		tB1 = Matrix.matrixAdd(tB1, RC1);
//		tA1 = Matrix.matrixAdd(tA1, RCA1);
//		tB1 = Matrix.matrixAdd(tB1, RCB1);
	}

	/**
	 * Performs conversions on results of the simulation experiment so that
	 * decompositions and AUC can be determined.
	 * @throws IOException 
	 */
	public void processSimExperiment() throws IOException {

		// Creates an "InputFile" of the scores from simulated experiment so
		// that we can perform variance analysis on them.
		InputFile toCalc = new InputFile(fData);
		// Creates a record of the variance analysis so decompositions can be
		// accessed
		DBRecord rec = new DBRecord();
		rec.DBRecordInputFile(toCalc, "ModalityA", "ModalityB", 3);

		BDG = rec.getBDG(useMLE);
		BCK = rec.getBCK(useMLE);
		DBM = rec.getDBM(useMLE);
		OR = rec.getOR(useMLE);
		MS = rec.getMS(useMLE);
		auc = new double[] { rec.getAUCinNumber(0), rec.getAUCinNumber(1),
				(rec.getAUCinNumber(0) - rec.getAUCinNumber(1)) };
	}

	/**
	 * Fills a vector with x random numbers according to a Gaussian
	 * distribution.
	 * 
	 * @param scalar Width of distribution
	 * @param rand Random number generator initialized with seed from GUI. No
	 *            guarantee that the generator is at any particular position
	 *            within its sequence for a given seed.
	 * @param nr2 length of vector to fill
	 * @return 1-d array containing random gaussian numbers
	 */
	public static double[] fillGaussian(double scalar, Random rand, long nr2) {
		double[] toReturn = new double[(int) nr2];
		for (int i = 0; i < nr2; i++) {
			toReturn[i] = scalar * rand.nextGaussian();
		}
		return toReturn;
	}

	/**
	 * Fills a matrix with x * y random numbers according to a Gaussian
	 * distribution.
	 * 
	 * @param scalar Width of distribution
	 * @param rand Random number generator initialized with seed from GUI. No
	 *            guarantee that the generator is at any particular position
	 *            within its sequence for a given seed.
	 * @param nr2 length of array to fill
	 * @param n02 width of array to fill
	 * @return 2-d array containing random gaussian numbers
	 */
	public static double[][] fillGaussian(double scalar, Random rand, long nr2,
			long n02) {
		double[][] toReturn = new double[(int) nr2][(int) n02];
		for (int i = 0; i < nr2; i++) {
			for (int j = 0; j < n02; j++) {
				toReturn[i][j] = scalar * rand.nextGaussian();
			}
		}
		return toReturn;
	}

}
