/**
 * SimRoeMetz.java
 * 
 * @version 1.0b
 * 
 * @author Rohan Pathare
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
 *     Simulates scores according to Roe and Metz simulation. Adapted for java from sim_roemetz.pro 
 *     (Brandon D. Gallas, PhD)
 */

package roemetz.core;

import java.util.Arrays;
import java.util.Random;
import mrmc.core.DBRecord;
import mrmc.core.InputFile;
import mrmc.core.Matrix;

public class SimRoeMetz {
	private double[][] t00;
	private double[][] t01;
	private double[][] t10;
	private double[][] t11;
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
	private int useBiasM;

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
	 * @param useBias Indicates whether to used biased estimates in the results
	 *            of the simulation
	 */
	public SimRoeMetz(double[] u, double[] var_t, int[] n, Random rand,
			int useBias) {
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
		useBiasM = useBias;
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
	 */
	public static void main(String[] args) {
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
			int useBias = Integer.parseInt(args[4]);
			SimRoeMetz exp = new SimRoeMetz(u, var_t, n, rand, useBias);
			exp.printResults();
		} catch (NumberFormatException e) {
			System.out.println("Incorrectly Formatted Input");
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Missing Arguments");
			System.out
					.println("Format is: SimRoeMetz [u0,u1] [R00,C00,RC00,R10,"
							+ "C10,RC10,R01,C01,RC01,R11,C11,RC11,R0,C0,RC0,R1,"
							+ "C1,RC1] [n0,n1,nr] seed useBias");
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
	public double[][] gett00() {
		return t00;
	}

	/**
	 * Get the simulated scores for disease cases, modality 0
	 * 
	 * @return Simulated scores for truth=1, modality=0
	 */
	public double[][] gett10() {
		return t10;
	}

	/**
	 * Get the simulated scores for normal cases, modality 1
	 * 
	 * @return Simulated scores for truth=0, modality=1
	 */
	public double[][] gett01() {
		return t01;
	}

	/**
	 * Get the simulated scores for disease cases, modality 1
	 * 
	 * @return Simulated scores for truth=1, modality=1
	 */
	public double[][] gett11() {
		return t11;
	}

	/**
	 * Performs one simulation experiment based on the Roe & Metz model
	 * generalized for 18 variances. Fills four t-matrices (class members t00,
	 * t01, t10, t11) with results
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

		double[] R00 = fillGaussian(stdDevs[0], rand, nr);
		double[] C00 = fillGaussian(stdDevs[1], rand, n0);
		double[][] RC00 = fillGaussian(stdDevs[2], rand, nr, n0);
		double[] R10 = fillGaussian(stdDevs[3], rand, nr);
		double[] C10 = fillGaussian(stdDevs[4], rand, n1);
		double[][] RC10 = fillGaussian(stdDevs[5], rand, nr, n1);
		double[] R01 = fillGaussian(stdDevs[6], rand, nr);
		double[] C01 = fillGaussian(stdDevs[7], rand, n0);
		double[][] RC01 = fillGaussian(stdDevs[8], rand, nr, n0);
		double[] R11 = fillGaussian(stdDevs[9], rand, nr);
		double[] C11 = fillGaussian(stdDevs[10], rand, n1);
		double[][] RC11 = fillGaussian(stdDevs[11], rand, nr, n1);
		double[] R0 = fillGaussian(stdDevs[12], rand, nr);
		double[] C0 = fillGaussian(stdDevs[13], rand, n0);
		double[][] RC0 = fillGaussian(stdDevs[14], rand, nr, n0);
		double[] R1 = fillGaussian(stdDevs[15], rand, nr);
		double[] C1 = fillGaussian(stdDevs[16], rand, n1);
		double[][] RC1 = fillGaussian(stdDevs[17], rand, nr, n1);

		t00 = new double[nr][n0];
		t01 = new double[nr][n0];
		t10 = new double[nr][n1];
		t11 = new double[nr][n1];

		for (int i = 0; i < nr; i++) {
			Arrays.fill(t10[i], u0);
			Arrays.fill(t11[i], u1);
		}

		for (int r = 0; r < nr; r++) {
			for (int i = 0; i < n0; i++) {
				t00[r][i] += R0[r];
				t00[r][i] += R00[r];
				t01[r][i] += R0[r];
				t01[r][i] += R01[r];

				t00[r][i] += C0[i];
				t00[r][i] += C00[i];
				t01[r][i] += C0[i];
				t01[r][i] += C01[i];
			}
			for (int j = 0; j < n1; j++) {
				t10[r][j] += R1[r];
				t10[r][j] += R10[r];
				t11[r][j] += R1[r];
				t11[r][j] += R11[r];

				t10[r][j] += C1[j];
				t10[r][j] += C10[j];
				t11[r][j] += C1[j];
				t11[r][j] += C11[j];
			}
		}

		t00 = Matrix.matrixAdd(t00, RC0);
		t01 = Matrix.matrixAdd(t01, RC0);
		t00 = Matrix.matrixAdd(t00, RC00);
		t01 = Matrix.matrixAdd(t01, RC01);

		t10 = Matrix.matrixAdd(t10, RC1);
		t11 = Matrix.matrixAdd(t11, RC1);
		t10 = Matrix.matrixAdd(t10, RC10);
		t11 = Matrix.matrixAdd(t11, RC11);
	}

	/**
	 * Performs conversions on results of the simulation experiment so that
	 * decompositions and AUC can be determined.
	 */
	private void processSimExperiment() {
		double[][][][] newTMatrices = convertTMatrices();
		int[][][][] dMatrices = createDMatrices();

		// Creates an "InputFile" of the scores from simulated experiment so
		// that we can perform variance analysis on them.
		InputFile toCalc = new InputFile(newTMatrices, dMatrices, nr, n0, n1,
				"SimExp", "Simulated Experiment");
		toCalc.calculateCovMRMC();
		// Creates a record of the variance analysis so decompositions can be
		// accessed
		DBRecord rec = new DBRecord(toCalc, 1, 2);

		BDG = rec.getBDG(useBiasM);
		BCK = rec.getBCK(useBiasM);
		DBM = rec.getDBM(useBiasM);
		OR = rec.getOR(useBiasM);
		MS = rec.getMS(useBiasM);
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
		double[][][] newt00 = new double[n0][nr][2];
		double[][][] newt01 = new double[n0][nr][2];
		double[][][] newt10 = new double[n1][nr][2];
		double[][][] newt11 = new double[n1][nr][2];
		double[][][] newt0 = new double[n0][nr][2];
		double[][][] newt1 = new double[n1][nr][2];

		for (int reader = 0; reader < t00.length; reader++) {
			for (int cases = 0; cases < t00[reader].length; cases++) {
				newt00[cases][reader][0] = t00[reader][cases];
				newt00[cases][reader][1] = t00[reader][cases];
				newt0[cases][reader][0] = t00[reader][cases];
			}
		}
		for (int reader = 0; reader < t01.length; reader++) {
			for (int cases = 0; cases < t01[reader].length; cases++) {
				newt01[cases][reader][0] = t01[reader][cases];
				newt01[cases][reader][1] = t01[reader][cases];
				newt0[cases][reader][1] = t01[reader][cases];
			}
		}
		for (int reader = 0; reader < t10.length; reader++) {
			for (int cases = 0; cases < t10[reader].length; cases++) {
				newt10[cases][reader][0] = t10[reader][cases];
				newt10[cases][reader][1] = t10[reader][cases];
				newt1[cases][reader][0] = t10[reader][cases];
			}
		}
		for (int reader = 0; reader < t11.length; reader++) {
			for (int cases = 0; cases < t11[reader].length; cases++) {
				newt11[cases][reader][0] = t11[reader][cases];
				newt11[cases][reader][1] = t11[reader][cases];
				newt1[cases][reader][1] = t11[reader][cases];
			}
		}

		return new double[][][][] { newt00, newt01, newt10, newt11, newt0,
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
