/*
 * CalcGenRoeMetz.java
 * 
 * v1.0b
 * 
 * @Author Rohan Pathare
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
 *     Calculates product moments from a generalized Roe and Metz model that allows for differerent variances 
 *     for each truth state and modality. Adapted for java from cofv_genroemetz.pro (Brandon D. Gallas, PhD)
 */

package roemetz.core;

import mrmc.core.DBRecord;
import mrmc.core.Matrix;
import org.apache.commons.math3.distribution.NormalDistribution;

public class CalcGenRoeMetz {
	private static double[][][] cofv_auc;
	private static double[][][] cofv_pc;
	private static double[][][] m;
	private static double[][] BDG;
	private static double[][] BCK;
	private static double[][] DBM;
	private static double[][] OR;
	private static double[][] MS;

	/*
	 * Can be executed from command-line, or used as closed-box library function
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
			String[] ns = args[0].substring(args[0].lastIndexOf("[") + 1,
					args[0].indexOf("]")).split(",");
			if (ns.length != 2) {
				System.out.println("Expected input n to contain 3 elements");
				return;
			} else {
				n = new int[] { Integer.parseInt(ns[0]),
						Integer.parseInt(ns[1]), Integer.parseInt(ns[2]) };
			}
			genRoeMetz(u, var_t, n);
			printResults();
		} catch (NumberFormatException e) {
			System.out.println("Incorrectly Formatted Input");
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Missing Arguments");
			System.out
					.println("Format is: CofVGenRoeMetz [u0,u1] [R00,C00,RC00,R10,C10,RC10,R01,C01,RC01,R11,C11,RC11,R0,C0,RC0,R1,C1,RC1] numSamples");
			e.printStackTrace();
		}
	}

	public static void printResults() {
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

	public static double[][] getBDGdata() {
		return BDG;
	}

	public static double[][] getBCKdata() {
		return BCK;
	}

	public static double[][] getDBMdata() {
		return DBM;
	}

	public static double[][] getORdata() {
		return OR;
	}

	public static double[][] getMSdata() {
		return MS;
	}

	public static double prodMoment1(double[] u, double[] scale, int n) {
		NormalDistribution gauss = new NormalDistribution();

		double scale1 = scale[0];
		double scale20 = scale[1];
		double scale21 = scale[2];

		double lx = 10 * Math.sqrt(scale1);
		double dx = lx / (double) n;
		double[] x = new double[n];
		for (int i = 0; i < n; i++) {
			x[i] = ((double) i * dx) - (0.5 * lx);
		}

		double f[] = new double[n];
		for (int i = 0; i < n; i++) {
			f[i] = Math.exp((-(x[i] * x[i])) / 2.0 / scale1);
		}

		for (int i = 0; i < n; i++) {
			f[i] = f[i] / Math.sqrt(Math.PI * 2.0 * scale1);
		}

		double[] phi = new double[n];
		for (int i = 0; i < n; i++) {
			phi[i] = gauss.cumulativeProbability((u[0] + x[i])
					/ Math.sqrt(scale20))
					* gauss.cumulativeProbability((u[1] + x[i])
							/ Math.sqrt(scale21));
		}

		double[] toTotal = new double[n];
		for (int i = 0; i < n; i++) {
			toTotal[i] = dx * f[i] * phi[i];
		}
		return Matrix.total(toTotal);
	}

	public static double prodMoment(double[] u, double[] scale, int n) {
		NormalDistribution gauss = new NormalDistribution();
		double scale1 = scale[0];
		double scale20 = scale[1];
		double scale21 = scale[2];
		double scale30 = scale[3];
		double scale31 = scale[4];

		double lx = 10.0 * Math.sqrt(scale1);
		double dx = lx / (double) n;
		double[] x = new double[n];

		double ly0 = 10.0 * Math.sqrt(scale20);
		double dy0 = ly0 / (double) n;
		double[] y0 = new double[n];

		double ly1 = 10.0 * Math.sqrt(scale21);
		double dy1 = ly1 / (double) n;
		double[] y1 = new double[n];

		for (int i = 0; i < n; i++) {
			x[i] = ((double) i * dx) - (0.5 * lx);
			y0[i] = ((double) i * dy0) - (0.5 * ly0);
			y1[i] = ((double) i * dy1) - (0.5 * ly1);
		}

		double[] f0 = new double[n];
		double[] f1 = new double[n];

		double[] dy0xf0 = new double[n];
		double[] dy1xf1 = new double[n];

		for (int i = 0; i < n; i++) {
			f0[i] = Math.exp(-(y0[i] * y0[i]) / 2.0 / scale20)
					/ Math.sqrt(Math.PI * 2.0 * scale20);
			dy0xf0[i] = dy0 * f0[i];
			f1[i] = Math.exp(-(y1[i] * y1[i]) / 2.0 / scale21)
					/ Math.sqrt(Math.PI * 2.0 * scale21);
			dy1xf1[i] = dy1 * f1[i];
		}

		double[] ff = new double[n];
		for (int i = 0; i < n; i++) {
			double[] phi0 = new double[n];
			double[] phi1 = new double[n];
			double[] dy0xf0xphi0 = new double[n];
			double[] dy1xf1xphi1 = new double[n];

			for (int j = 0; j < n; j++) {
				phi0[j] = gauss.cumulativeProbability((u[0] + x[j] + y0[i])
						/ Math.sqrt(scale30));
				dy0xf0xphi0[j] = dy0xf0[j] * phi0[j];
				phi1[j] = gauss.cumulativeProbability((u[1] + x[j] + y1[i])
						/ Math.sqrt(scale31));
				dy1xf1xphi1[j] = dy1xf1[j] * phi1[j];
			}
			ff[i] = Matrix.total(dy0xf0xphi0) * Matrix.total(dy1xf1xphi1);
		}

		double[] f = new double[n];
		double[] toTotal = new double[n];
		for (int i = 0; i < n; i++) {
			f[i] = Math.exp(-(x[i] * x[i]) / 2.0 / scale1)
					/ Math.sqrt(Math.PI * 2.0 * scale1);
			toTotal[i] = dx * f[i] * ff[i];
		}

		return Matrix.total(toTotal);
	}

	public static void genRoeMetz(double[] u, double[] var_t, int[] n) {
		NormalDistribution gauss = new NormalDistribution();
		if (var_t.length != 18) {
			System.out
					.println("var_t should contain 18 components of variance");
			return;
		}

		double v00r = var_t[0];
		double v00c = var_t[1];
		double v00rc = var_t[2];
		double v10r = var_t[3];
		double v10c = var_t[4];
		double v10rc = var_t[5];
		double v01r = var_t[6];
		double v01c = var_t[7];
		double v01rc = var_t[8];
		double v11r = var_t[9];
		double v11c = var_t[10];
		double v11rc = var_t[11];
		double v0r = var_t[12];
		double v0c = var_t[13];
		double v0rc = var_t[14];
		double v1r = var_t[15];
		double v1c = var_t[16];
		double v1rc = var_t[17];

		int n0 = n[0];
		int n1 = n[1];
		int nr = n[2];

		// default value of n could be 256
		int numSamples = 256;

		m = new double[2][2][9];

		// AUC
		double scale1 = v0r + v0c + v0rc + v1r + v1c + v1rc;
		double scale20 = v00r + v00c + v00rc + v10r + v10c + v10rc;
		double scale21 = v01r + v01c + v01rc + v11r + v11c + v11rc;
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
		double scale30 = v0c + v0rc + v00c + v00rc;
		double scale31 = v0c + v0rc + v01c + v01rc;
		scale20 = v10r + v10c + v10rc + v00r;
		scale21 = v11r + v11c + v11rc + v01r;
		scale1 = v1r + v1c + v1rc + v0r;

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
		scale30 = v1c + v1rc + v10c + v10rc;
		scale31 = v1c + v1rc + v11c + v11rc;
		scale20 = v10r + v00r + v00c + v00rc;
		scale21 = v11r + v01r + v01c + v01rc;
		scale1 = v1r + v0r + v0c + v0rc;

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
		scale30 = v1c + v1rc + v10c + v10rc + v0c + v0rc + v00c + v00rc;
		scale31 = v1c + v1rc + v11c + v11rc + v0c + v0rc + v01c + v01rc;
		scale20 = v10r + v00r;
		scale21 = v11r + v01r;
		scale1 = v1r + v0r;

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
		scale30 = v0r + v1r + v0rc + v1rc + v00r + v10r + v00rc + v10rc;
		scale31 = v0r + v1r + v0rc + v1rc + v01r + v11r + v01rc + v11rc;
		scale20 = v00c + v10c;
		scale21 = v01c + v11c;
		scale1 = v1c + v0c;

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
		scale30 = v0r + v1r + v0c + v0rc + v1rc + v00r + v10r + v00c + v00rc
				+ v10rc;
		scale31 = v0r + v1r + v0c + v0rc + v1rc + v01r + v11r + v01c + v01rc
				+ v11rc;
		scale20 = v10c;
		scale21 = v11c;
		scale1 = v1c;

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
		scale30 = v0r + v1r + v1c + v0rc + v1rc + v00r + v10r + v10c + v00rc
				+ v10rc;
		scale31 = v0r + v1r + v1c + v0rc + v1rc + v01r + v11r + v11c + v01rc
				+ v11rc;
		scale20 = v00c;
		scale21 = v01c;
		scale1 = v0c;

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

		calculateStuff(n0, n1, nr);

	}

	public static void calculateStuff(int n0, int n1, int nr) {
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

		BCK = DBRecord.BDG2BCK(BDG);
		DBM = DBRecord.BCK2DBM(BCK, nr, n0, n1);
		OR = DBRecord.DBM2OR(0, DBM, nr, n0, n1);
		MS = DBRecord.DBM2MS(DBM, nr, n0, n1);
	}
}
