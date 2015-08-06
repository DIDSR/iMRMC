/**
 * CalcGenRoeMetz.java
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

import mrmc.core.DBRecord;
import mrmc.core.Matrix;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Calculates product moments from a generalized Roe and Metz model that allows
 * for differerent variances for each truth state and modality. Adapted for java
 * from cofv_genroemetz.pro (Brandon D. Gallas, PhD)
 * 
 * @author Rohan Pathare
 */
public class CalcGenRoeMetz {
	private static double[][][] cofv_auc;
	private static double[][][] cofv_pc;
	private static double[][][] m;
	private static double[][] BDG;
	private static double[][] BCK;
	private static double[][] DBM;
	private static double[][] OR;
	private static double[][] MS;

	/**
	 * Used when calling CalGenRoeMetz as a standalone application via
	 * command-line.
	 * 
	 * @param args command-line arguments. First element is experiment means,
	 *            second element is components of variance, third element is
	 *            experiment sizes
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
			int Nreader, Nnormal, Ndisease;
			String[] ns = args[2].substring(args[2].lastIndexOf("[") + 1,
					args[0].indexOf("]")).split(",");
			if (ns.length != 2) {
				System.out.println("Expected input n to contain 3 elements");
				return;
			} else {
				Nnormal = Integer.parseInt(ns[0]);
				Ndisease = Integer.parseInt(ns[1]);
				Nreader = Integer.parseInt(ns[2]);
			}
			genRoeMetz(u, var_t, Nreader, Nnormal, Ndisease);
			printResults();
		} catch (NumberFormatException e) {
			System.out.println("Incorrectly Formatted Input");
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Missing Arguments");
			System.out
					.println("Format is: CofVGenRoeMetz [u0,u1] [R00,C00,RC00,R10,C10,RC10,R01,C01,RC01,R11,C11,RC11,R0,C0,RC0,R1,C1,RC1] [n0,n1,nr]");
		}
	}

	/**
	 * Prints the results of a simulation experiment to standard out. Only used
	 * when main method for this class is invoked.
	 */
	private static void printResults() {
		System.out.println("cofv_auc:");
		for (int i = 0; i < cofv_auc.length; i++) {
			Matrix.printMatrix(cofv_auc[i]);
			System.out.println();
		}
		System.out.println();
		System.out.println("cofv_pc:");
		for (int i = 0; i < cofv_pc.length; i++) {
			Matrix.printMatrix(cofv_pc[i]);
			System.out.println();
		}
		System.out.println("\n");
		System.out.println("BDG:");
		Matrix.printMatrix(BDG);
		System.out.println();

		System.out.println("BCK:");
		Matrix.printMatrix(BCK);
		System.out.println();

		System.out.println("M:");
		Matrix.printMatrix(m[0]);
		Matrix.printMatrix(m[1]);
		System.out.println();
	}

	/**
	 * Get BDG decomposition of calculated variance components
	 * 
	 * @return BDG decomposition
	 */
	public static double[][] getBDGdata() {
		return BDG;
	}

	/**
	 * Get BCK decomposition of calculated variance components
	 * 
	 * @return BCK decomposition
	 */
	public static double[][] getBCKdata() {
		return BCK;
	}

	/**
	 * Get DBM decomposition of calculated variance components
	 * 
	 * @return DBM decomposition
	 */
	public static double[][] getDBMdata() {
		return DBM;
	}

	/**
	 * Get OR decomposition of calculated variance components
	 * 
	 * @return OR decomposition
	 */
	public static double[][] getORdata() {
		return OR;
	}

	/**
	 * Get MS decomposition of calculated variance components
	 * 
	 * @return MS decomposition
	 */
	public static double[][] getMSdata() {
		return MS;
	}

	/**
	 * Numerically integrates a one dimensional gaussian pdf times two normal
	 * cdfs
	 * 
	 * @param u Contains experiment means
	 * @param scale Contains one 1-D gaussian pdf and two 2-D normal cdfs
	 * @param numSamples Number of samples for numerical integration
	 * @return Integrated product moment
	 */
	public static double prodMoment1(double[] u, double[] scale, int numSamples) {
		NormalDistribution gauss = new NormalDistribution();

		double scale1 = scale[0];
		double scale20 = scale[1];
		double scale21 = scale[2];

		double lx = 10 * Math.sqrt(scale1);
		double dx = lx / (double) numSamples;
		double[] x = new double[numSamples];
		for (int i = 0; i < numSamples; i++) {
			x[i] = ((double) i * dx) - (0.5 * lx);
		}

		double f[] = new double[numSamples];
		for (int i = 0; i < numSamples; i++) {
			f[i] = Math.exp((-(x[i] * x[i])) / 2.0 / scale1);
		}

		for (int i = 0; i < numSamples; i++) {
			f[i] = f[i] / Math.sqrt(Math.PI * 2.0 * scale1);
		}

		double[] phi = new double[numSamples];
		for (int i = 0; i < numSamples; i++) {
			phi[i] = gauss.cumulativeProbability((u[0] + x[i])
					/ Math.sqrt(scale20))
					* gauss.cumulativeProbability((u[1] + x[i])
							/ Math.sqrt(scale21));
		}

		double[] toTotal = new double[numSamples];
		for (int i = 0; i < numSamples; i++) {
			toTotal[i] = dx * f[i] * phi[i];
		}
		return Matrix.total(toTotal);
	}

	/**
	 * Numerically integrates a two dimensional gaussian pdf times a gaussian
	 * cdf
	 * 
	 * @param u Contains experiment means.
	 * @param scale Contains 2-D gaussian pdf and cdf
	 * @param numSamples Number of samples for numerical integration
	 * @return Integrated product moment
	 */
	public static double prodMoment(double[] u, double[] scale, int numSamples) {
		NormalDistribution gauss = new NormalDistribution();
		double scaleFixed = scale[0];
		double scaleIndependentA = scale[1] + scale[3];
		double scaleIndependentB = scale[2] + scale[4];

		double lx = 10.0;
		double dx = lx / (double) numSamples;
		double[] x = new double[numSamples];
		double Integral = 0.0;
		
		for (int i = 0; i < numSamples; i++) {
			x[i] = ((double) i * dx) - (0.5 * lx);
		}

		double[] phi_x = new double[numSamples];
		double[] cdf_A = new double[numSamples];
		double[] cdf_B = new double[numSamples];

		for (int i = 0; i < numSamples; i++) {
			phi_x[i] = Math.exp(-(x[i] * x[i]) / 2.0 )
					/ Math.sqrt(Math.PI * 2.0);
			cdf_A[i] = gauss.cumulativeProbability(
					(u[0] + x[i]*Math.sqrt(scaleFixed))
					/ Math.sqrt(scaleIndependentA));
			cdf_B[i] = gauss.cumulativeProbability(
					(u[1] + x[i]*Math.sqrt(scaleFixed))
					/ Math.sqrt(scaleIndependentB));
			Integral = Integral + dx*phi_x[i]*cdf_A[i]*cdf_B[i];
		}

		return Integral;
	}

	/**
	 * Calculates AUC components of variance for given experiment parameters via
	 * numerical integration
	 * 
	 * @param u Contains experiment means. Has 2 elements.
	 * @param var_t Contains variance components. Has 18 elements.
	 * @param n Contains experiment sizes. Has 3 elements.
	 */
	public static void genRoeMetz(double[] u, double[] var_t, int Nreader, int Nnormal, int Ndisease) {
		NormalDistribution gauss = new NormalDistribution();

		// number of samples for numerical integration, can change
		final int numSamples = 256;

		double v_AR0 = var_t[0];
		double v_AC0 = var_t[1];
		double v_ARC0 = var_t[2];
		double v_AR1 = var_t[3];
		double v_AC1 = var_t[4];
		double v_ARC1 = var_t[5];
		double v_BR0 = var_t[6];
		double v_BC0 = var_t[7];
		double v_BRC0 = var_t[8];
		double v_BR1 = var_t[9];
		double v_BC1 = var_t[10];
		double v_BRC1 = var_t[11];
		double v_R0 = var_t[12];
		double v_C0 = var_t[13];
		double v_RC0 = var_t[14];
		double v_R1 = var_t[15];
		double v_C1 = var_t[16];
		double v_RC1 = var_t[17];

		m = new double[2][2][9];

		// AUC
		double scale1 = v_R0 + v_C0 + v_RC0 + v_R1 + v_C1 + v_RC1;
		double scale20 = v_AR0 + v_AC0 + v_ARC0 + v_AR1 + v_AC1 + v_ARC1;
		double scale21 = v_BR0 + v_BC0 + v_BRC0 + v_BR1 + v_BC1 + v_BRC1;
		m[0][0][0] = gauss.cumulativeProbability(u[0]
				/ Math.sqrt(scale1 + scale20));
		m[1][1][0] = gauss.cumulativeProbability(u[1]
				/ Math.sqrt(scale1 + scale21));
		m[1][0][0] = m[0][0][0] - m[1][1][0];
		m[0][1][0] = -m[1][0][0];

		// M1
		double[] scaleM1 = { scale1, scale20, scale21 };
		m[0][0][1] = m[0][0][0];
		m[1][1][1] = m[1][1][0];
		m[1][0][1] = prodMoment1(u, scaleM1, numSamples);
		m[0][1][1] = m[1][0][1];

		// M2
		double scale30 = v_C0 + v_RC0 + v_AC0 + v_ARC0;
		double scale31 = v_C0 + v_RC0 + v_BC0 + v_BRC0;
		scale20 = v_AR1 + v_AC1 + v_ARC1 + v_AR0;
		scale21 = v_BR1 + v_BC1 + v_BRC1 + v_BR0;
		scale1 = v_R1 + v_C1 + v_RC1 + v_R0;

		scaleM1[0] = scale1 + scale20;
		scaleM1[1] = scale30;
		scaleM1[2] = scale30;
		m[0][0][2] = prodMoment1(new double[] { u[0], u[0] }, scaleM1,
				numSamples);

		scaleM1[0] = scale1 + scale21;
		scaleM1[1] = scale31;
		scaleM1[2] = scale31;
		m[1][1][2] = prodMoment1(new double[] { u[1], u[1] }, scaleM1,
				numSamples);

		double[] scaleM = { scale1, scale20, scale21, scale30, scale31 };
		m[1][0][2] = prodMoment(new double[] { u[0], u[1] }, scaleM, numSamples);
		m[0][1][2] = m[1][0][2];

		// M3
		scale30 = v_C1 + v_RC1 + v_AC1 + v_ARC1;
		scale31 = v_C1 + v_RC1 + v_BC1 + v_BRC1;
		scale20 = v_AR1 + v_AR0 + v_AC0 + v_ARC0;
		scale21 = v_BR1 + v_BR0 + v_BC0 + v_BRC0;
		scale1 = v_R1 + v_R0 + v_C0 + v_RC0;

		scaleM1[0] = scale1 + scale20;
		scaleM1[1] = scale30;
		scaleM1[2] = scale30;
		m[0][0][3] = prodMoment1(new double[] { u[0], u[0] }, scaleM1,
				numSamples);

		scaleM1[0] = scale1 + scale21;
		scaleM1[1] = scale31;
		scaleM1[2] = scale31;
		m[1][1][3] = prodMoment1(new double[] { u[1], u[1] }, scaleM1,
				numSamples);

		scaleM[0] = scale1;
		scaleM[1] = scale20;
		scaleM[2] = scale21;
		scaleM[3] = scale30;
		scaleM[4] = scale31;
		m[1][0][3] = prodMoment(new double[] { u[0], u[1] }, scaleM, numSamples);
		m[0][1][3] = m[1][0][3];

		// M4
		scale30 = v_C1 + v_RC1 + v_AC1 + v_ARC1 + v_C0 + v_RC0 + v_AC0 + v_ARC0;
		scale31 = v_C1 + v_RC1 + v_BC1 + v_BRC1 + v_C0 + v_RC0 + v_BC0 + v_BRC0;
		scale20 = v_AR1 + v_AR0;
		scale21 = v_BR1 + v_BR0;
		scale1 = v_R1 + v_R0;

		scaleM1[0] = scale1 + scale20;
		scaleM1[1] = scale30;
		scaleM1[2] = scale30;
		m[0][0][4] = prodMoment1(new double[] { u[0], u[0] }, scaleM1,
				numSamples);

		scaleM1[0] = scale1 + scale21;
		scaleM1[1] = scale31;
		scaleM1[2] = scale31;
		m[1][1][4] = prodMoment1(new double[] { u[1], u[1] }, scaleM1,
				numSamples);

		scaleM[0] = scale1;
		scaleM[1] = scale20;
		scaleM[2] = scale21;
		scaleM[3] = scale30;
		scaleM[4] = scale31;
		m[1][0][4] = prodMoment(new double[] { u[0], u[1] }, scaleM, numSamples);
		m[0][1][4] = m[1][0][4];

		// M5
		scale30 = v_R0 + v_R1 + v_RC0 + v_RC1 + v_AR0 + v_AR1 + v_ARC0 + v_ARC1;
		scale31 = v_R0 + v_R1 + v_RC0 + v_RC1 + v_BR0 + v_BR1 + v_BRC0 + v_BRC1;
		scale20 = v_AC0 + v_AC1;
		scale21 = v_BC0 + v_BC1;
		scale1 = v_C1 + v_C0;

		scaleM1[0] = scale1 + scale20;
		scaleM1[1] = scale30;
		scaleM1[2] = scale30;
		m[0][0][5] = prodMoment1(new double[] { u[0], u[0] }, scaleM1,
				numSamples);

		scaleM1[0] = scale1 + scale21;
		scaleM1[1] = scale31;
		scaleM1[2] = scale31;
		m[1][1][5] = prodMoment1(new double[] { u[1], u[1] }, scaleM1,
				numSamples);

		scaleM[0] = scale1;
		scaleM[1] = scale20;
		scaleM[2] = scale21;
		scaleM[3] = scale30;
		scaleM[4] = scale31;
		m[1][0][5] = prodMoment(new double[] { u[0], u[1] }, scaleM, numSamples);
		m[0][1][5] = m[1][0][5];

		// M6
		scale30 = v_R0 + v_R1 + v_C0 + v_RC0 + v_RC1 + v_AR0 + v_AR1 + v_AC0 + v_ARC0
				+ v_ARC1;
		scale31 = v_R0 + v_R1 + v_C0 + v_RC0 + v_RC1 + v_BR0 + v_BR1 + v_BC0 + v_BRC0
				+ v_BRC1;
		scale20 = v_AC1;
		scale21 = v_BC1;
		scale1 = v_C1;

		scaleM1[0] = scale1 + scale20;
		scaleM1[1] = scale30;
		scaleM1[2] = scale30;
		m[0][0][6] = prodMoment1(new double[] { u[0], u[0] }, scaleM1,
				numSamples);

		scaleM1[0] = scale1 + scale21;
		scaleM1[1] = scale31;
		scaleM1[2] = scale31;
		m[1][1][6] = prodMoment1(new double[] { u[1], u[1] }, scaleM1,
				numSamples);

		scaleM[0] = scale1;
		scaleM[1] = scale20;
		scaleM[2] = scale21;
		scaleM[3] = scale30;
		scaleM[4] = scale31;
		m[1][0][6] = prodMoment(new double[] { u[0], u[1] }, scaleM, numSamples);
		m[0][1][6] = m[1][0][6];

		// M7
		scale30 = v_R0 + v_R1 + v_C1 + v_RC0 + v_RC1 + v_AR0 + v_AR1 + v_AC1 + v_ARC0
				+ v_ARC1;
		scale31 = v_R0 + v_R1 + v_C1 + v_RC0 + v_RC1 + v_BR0 + v_BR1 + v_BC1 + v_BRC0
				+ v_BRC1;
		scale20 = v_AC0;
		scale21 = v_BC0;
		scale1 = v_C0;

		scaleM1[0] = scale1 + scale20;
		scaleM1[1] = scale30;
		scaleM1[2] = scale30;
		m[0][0][7] = prodMoment1(new double[] { u[0], u[0] }, scaleM1,
				numSamples);

		scaleM1[0] = scale1 + scale21;
		scaleM1[1] = scale31;
		scaleM1[2] = scale31;
		m[1][1][7] = prodMoment1(new double[] { u[1], u[1] }, scaleM1,
				numSamples);

		scaleM[0] = scale1;
		scaleM[1] = scale20;
		scaleM[2] = scale21;
		scaleM[3] = scale30;
		scaleM[4] = scale31;
		m[1][0][7] = prodMoment(new double[] { u[0], u[1] }, scaleM, numSamples);
		m[0][1][7] = m[1][0][7];

		//

		m[0][0][8] = m[0][0][0] * m[0][0][0];
		m[1][1][8] = m[1][1][0] * m[1][1][0];
		m[1][0][8] = m[0][0][0] * m[1][1][0];
		m[0][1][8] = m[1][0][8];

		calcAUCsAndDecomps(Nnormal, Ndisease, Nreader);

	}

	/**
	 * Calculates AUCs and decompositions of components of variance from moment
	 * matrix according to experiment size
	 * 
	 * @param n0 Number of normal cases
	 * @param n1 Number of disease cases
	 * @param nr Number of readers
	 */
	public static void calcAUCsAndDecomps(long n0, long n1, long nr) {
		double[][] Bauc = { { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, -1.0 },
				{ 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, -1.0 },
				{ 0.0, 0.0, 0.0, 0.0, 1.0, -1.0, -1.0, 1.0 },
				{ 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, -1.0 },
				{ 0.0, 0.0, 1.0, -1.0, 0.0, 0.0, -1.0, 1.0 },
				{ 0.0, 1.0, 0.0, -1.0, 0.0, -1.0, 0.0, 1.0 },
				{ 1.0, -1.0, -1.0, 1.0, -1.0, 1.0, 1.0, -1.0 } };

		cofv_auc = new double[2][2][7];

		double[] Baucxm1 = Matrix.multiply(Bauc,
				Matrix.get1Dimension(1, m, "0", "0", "*"));
		for (int i = 0; i < cofv_auc[0][0].length; i++) {
			cofv_auc[0][0][i] = Baucxm1[i];
		}

		double[] Baucxm2 = Matrix.multiply(Bauc,
				Matrix.get1Dimension(1, m, "1", "1", "*"));
		for (int i = 0; i < cofv_auc[1][1].length; i++) {
			cofv_auc[1][1][i] = Baucxm2[i];
		}

		double[] Baucxm3 = Matrix.multiply(Bauc,
				Matrix.get1Dimension(1, m, "1", "0", "*"));
		for (int i = 0; i < cofv_auc[1][0].length; i++) {
			cofv_auc[1][0][i] = Baucxm3[i];
		}

		for (int i = 0; i < cofv_auc[0][1].length; i++) {
			cofv_auc[0][1][i] = cofv_auc[1][0][i];
		}

		double[][] Bpc = { { 0, 0, 0, 1, 0, 0, 0, -1 },
				{ 0, 0, 0, 0, 1, 0, 0, -1 }, { 1, 0, 0, -1, -1, 0, 0, 1 } };

		cofv_pc = new double[2][2][3];
		double[] Bpcxm1 = Matrix.multiply(Bpc,
				Matrix.get1Dimension(1, m, "0", "0", "*"));
		for (int i = 0; i < cofv_pc[0][0].length; i++) {
			cofv_pc[0][0][i] = Bpcxm1[i];
		}

		double[] Bpcxm2 = Matrix.multiply(Bpc,
				Matrix.get1Dimension(1, m, "1", "1", "*"));
		for (int i = 0; i < cofv_pc[1][1].length; i++) {
			cofv_pc[1][1][i] = Bpcxm2[i];
		}

		double[] Bpcxm3 = Matrix.multiply(Bpc,
				Matrix.get1Dimension(1, m, "1", "0", "*"));
		for (int i = 0; i < cofv_pc[1][0].length; i++) {
			cofv_pc[1][0][i] = Bpcxm3[i];
		}

		for (int i = 0; i < cofv_pc[0][1].length; i++) {
			cofv_pc[0][1][i] = cofv_pc[1][0][i];
		}

		BDG = new double[4][8];
		BDG[0] = Matrix.get1Dimension(1, m, "0", "0", "*");
		BDG[1] = Matrix.get1Dimension(1, m, "1", "1", "*");
		BDG[2] = Matrix.get1Dimension(1, m, "0", "1", "*");
		for (int i = 0; i < 8; i++) {
			BDG[3][i] = (m[0][0][i + 1] + m[1][1][i + 1] - (2 * m[0][1][i + 1]));
		}

		double[][] BCKcoeff = DBRecord.genBCKCoeff(n0, n1, nr);
		BCK = DBRecord.BDG2BCK(BDG, BCKcoeff);
		DBM = DBRecord.BCK2DBM(BCK, nr, n0, n1);
		OR = DBRecord.DBM2OR(0, DBM, nr, n0, n1);
		MS = DBRecord.DBM2MS(DBM, nr, n0, n1);
	}
}
