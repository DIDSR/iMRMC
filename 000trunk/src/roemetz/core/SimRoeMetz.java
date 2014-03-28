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
	private int n0;
	private int n1;
	private int nr;
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
	public SimRoeMetz(double[] u, double[] var_t, int[] n, Random rand,
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
		doSim(var_t, rand);
		processSimExperiment();
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
			int[] n = new int[3];
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
	private void doSim(double[] var_t, Random rand) {
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

		tA0 = new double[nr][n0];
		tB0 = new double[nr][n0];
		tA1 = new double[nr][n1];
		tB1 = new double[nr][n1];

		for (int i = 0; i < nr; i++) {
			Arrays.fill(tA1[i], u0);
			Arrays.fill(tB1[i], u1);
		}

		for (int r = 0; r < nr; r++) {
			for (int i = 0; i < n0; i++) {
				tA0[r][i] += R0[r] + C0[i] + RA0[r] + CA0[i];
				tB0[r][i] += R0[r] + C0[i] + RB0[r] + CB0[i];
			}
			for (int j = 0; j < n1; j++) {
				tA1[r][j] += R1[r] + C1[j] + RA1[r] + CA1[j];
				tB1[r][j] += R1[r] + C1[j] + RB1[r] + CB1[j];
			}
		}

		tA0 = Matrix.matrixAdd(tA0, RC0);
		tB0 = Matrix.matrixAdd(tB0, RC0);
		tA0 = Matrix.matrixAdd(tA0, RCA0);
		tB0 = Matrix.matrixAdd(tB0, RCB0);

		tA1 = Matrix.matrixAdd(tA1, RC1);
		tB1 = Matrix.matrixAdd(tB1, RC1);
		tA1 = Matrix.matrixAdd(tA1, RCA1);
		tB1 = Matrix.matrixAdd(tB1, RCB1);
	}

	/**
	 * Performs conversions on results of the simulation experiment so that
	 * decompositions and AUC can be determined.
	 * @throws IOException 
	 */
	private void processSimExperiment() throws IOException {
		double[][][][] newTMatrices = convertTMatrices();
		int[][][][] dMatrices = createDMatrices();

		// Creates an "InputFile" of the scores from simulated experiment so
		// that we can perform variance analysis on them.
		InputFile toCalc = new InputFile(newTMatrices, dMatrices, nr, n0, n1,
				"SimExp", "Simulated Experiment");
		toCalc.calculateCovMRMC();
		// Creates a record of the variance analysis so decompositions can be
		// accessed
		DBRecord rec = new DBRecord(toCalc, "1", "2");

		BDG = rec.getBDG(useMLE);
		BCK = rec.getBCK(useMLE);
		DBM = rec.getDBM(useMLE);
		OR = rec.getOR(useMLE);
		MS = rec.getMS(useMLE);
		auc = new double[] { rec.getAUCinNumber(0), rec.getAUCinNumber(1),
				(rec.getAUCinNumber(0) - rec.getAUCinNumber(1)) };
	}

	/**
	 * Reorganizes the t-matrices into format that is convenient for variance
	 * analysis by iMRMC methods. Additionally creates the t0 and t1 matrices
	 * which are modality-independent
	 * 
	 * @return Array consisting of all converted t-matrices necessary for
	 *         variance analysis
	 */
	private double[][][][] convertTMatrices() {
		double[][][] newtA0 = new double[n0][nr][2];
		double[][][] newtB0 = new double[n0][nr][2];
		double[][][] newtA1 = new double[n1][nr][2];
		double[][][] newtB1 = new double[n1][nr][2];
		double[][][] newt0 = new double[n0][nr][2];
		double[][][] newt1 = new double[n1][nr][2];

		for (int reader = 0; reader < tA0.length; reader++) {
			for (int cases = 0; cases < tA0[reader].length; cases++) {
				newtA0[cases][reader][0] = tA0[reader][cases];
				newtA0[cases][reader][1] = tA0[reader][cases];
				newt0[cases][reader][0] = tA0[reader][cases];
			}
		}
		for (int reader = 0; reader < tB0.length; reader++) {
			for (int cases = 0; cases < tB0[reader].length; cases++) {
				newtB0[cases][reader][0] = tB0[reader][cases];
				newtB0[cases][reader][1] = tB0[reader][cases];
				newt0[cases][reader][1] = tB0[reader][cases];
			}
		}
		for (int reader = 0; reader < tA1.length; reader++) {
			for (int cases = 0; cases < tA1[reader].length; cases++) {
				newtA1[cases][reader][0] = tA1[reader][cases];
				newtA1[cases][reader][1] = tA1[reader][cases];
				newt1[cases][reader][0] = tA1[reader][cases];
			}
		}
		for (int reader = 0; reader < tB1.length; reader++) {
			for (int cases = 0; cases < tB1[reader].length; cases++) {
				newtB1[cases][reader][0] = tB1[reader][cases];
				newtB1[cases][reader][1] = tB1[reader][cases];
				newt1[cases][reader][1] = tB1[reader][cases];
			}
		}

		return new double[][][][] { newtA0, newtB0, newtA1, newtB1, newt0,
				newt1 };
	}

	/**
	 * Creates design matrices for the simulated experiment. The design matrices
	 * for both normal and disease cases both indicate a fully-crossed design
	 * (consist of all 1's), since a simulated experiment does not have any
	 * missing scores.
	 * 
	 * @return Array containing design matrices for normal cases and disease
	 *         cases.
	 */
	private int[][][][] createDMatrices() {
		int[][][] d0 = new int[n0][nr][2];
		int[][][] d1 = new int[n1][nr][2];

		for (int i = 0; i < nr; i++) {
			for (int j = 0; j < n1; j++) {
				Arrays.fill(d1[j][i], 1);
			}
			for (int j = 0; j < n0; j++) {
				Arrays.fill(d0[j][i], 1);
			}
		}
		return new int[][][][] { d0, d1 };
	}

	/**
	 * Fills a vector with x random numbers according to a Gaussian
	 * distribution.
	 * 
	 * @param scalar Width of distribution
	 * @param rand Random number generator initialized with seed from GUI. No
	 *            guarantee that the generator is at any particular position
	 *            within its sequence for a given seed.
	 * @param x length of vector to fill
	 * @return 1-d array containing random gaussian numbers
	 */
	public static double[] fillGaussian(double scalar, Random rand, int x) {
		double[] toReturn = new double[x];
		for (int i = 0; i < x; i++) {
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
	 * @param x length of array to fill
	 * @param y width of array to fill
	 * @return 2-d array containing random gaussian numbers
	 */
	public static double[][] fillGaussian(double scalar, Random rand, int x,
			int y) {
		double[][] toReturn = new double[x][y];
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				toReturn[i][j] = scalar * rand.nextGaussian();
			}
		}
		return toReturn;
	}

}
