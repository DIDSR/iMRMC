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

import mrmc.core.CovMRMC;
import mrmc.core.DBRecord;
import mrmc.core.Matrix;
import mrmc.gui.SizePanel;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Calculates product moments from a generalized Roe and Metz model that allows
 * for differerent variances for each truth state and modality. Adapted for java
 * from cofv_genroemetz.pro (Brandon D. Gallas, PhD)
 * 
 * @author Rohan Pathare
 */
public class CalcGenRoeMetz {
	
	public static DBRecord DBRecordNumerical = new DBRecord();
	
	
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
//			genRoeMetz(u, var_t, Nreader, Nnormal, Ndisease);
			//  printResults();
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
	 * @param Nreader Number of readers in experiment.
	 * @param Nnormal Number of normal cases in experiment.
	 * @param Ndisease Number of disease cases in experiment.
	 */
	public static void genRoeMetz(double[] u, double[] var_t, SizePanel SizePanelRoeMetz) {
		
		DBRecordNumerical.Nreader = Integer.parseInt(SizePanelRoeMetz.NreaderJTextField.getText());
		DBRecordNumerical.Nnormal = Integer.parseInt(SizePanelRoeMetz.NnormalJTextField.getText());
		DBRecordNumerical.Ndisease = Integer.parseInt(SizePanelRoeMetz.NdiseaseJTextField.getText());


		// number of samples for numerical integration, can change
		final int numSamples = 256;

		NormalDistribution gauss = new NormalDistribution();

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

		// AUC
		double scale1 = v_R0 + v_C0 + v_RC0 + v_R1 + v_C1 + v_RC1;
		double scale20 = v_AR0 + v_AC0 + v_ARC0 + v_AR1 + v_AC1 + v_ARC1;
		double scale21 = v_BR0 + v_BC0 + v_BRC0 + v_BR1 + v_BC1 + v_BRC1;

		DBRecordNumerical.AUCsReaderAvg = new double[2];
		DBRecordNumerical.AUCs = new double[(int) DBRecordNumerical.Nreader][2];
		DBRecordNumerical.AUCsReaderAvg[0] = 
			gauss.cumulativeProbability(u[0] / Math.sqrt(scale1 + scale20));
		DBRecordNumerical.AUCsReaderAvg[1] = 
			gauss.cumulativeProbability(u[1] / Math.sqrt(scale1 + scale21));
		for(int r=0; r<DBRecordNumerical.Nreader; r++) {
			DBRecordNumerical.AUCs[r][0] = DBRecordNumerical.AUCsReaderAvg[0];
			DBRecordNumerical.AUCs[r][1] = DBRecordNumerical.AUCsReaderAvg[1];
		}

		// M1
		double[] scaleM1 = { scale1, scale20, scale21 };
		DBRecordNumerical.BDG[0][0] = DBRecordNumerical.AUCsReaderAvg[0];
		DBRecordNumerical.BDG[1][0] = DBRecordNumerical.AUCsReaderAvg[1];
		DBRecordNumerical.BDG[2][0] = prodMoment1(u, scaleM1, numSamples);

		// M2
		double scale30 = v_C0 + v_RC0 + v_AC0 + v_ARC0;
		double scale31 = v_C0 + v_RC0 + v_BC0 + v_BRC0;
		scale20 = v_AR1 + v_AC1 + v_ARC1 + v_AR0;
		scale21 = v_BR1 + v_BC1 + v_BRC1 + v_BR0;
		scale1 = v_R1 + v_C1 + v_RC1 + v_R0;

		scaleM1[0] = scale1 + scale20;
		scaleM1[1] = scale30;
		scaleM1[2] = scale30;
		DBRecordNumerical.BDG[0][1] = 
			prodMoment1(new double[] { u[0], u[0] }, scaleM1, numSamples);

		scaleM1[0] = scale1 + scale21;
		scaleM1[1] = scale31;
		scaleM1[2] = scale31;
		DBRecordNumerical.BDG[1][1] = 
			prodMoment1(new double[] { u[1], u[1] }, scaleM1, numSamples);

		double[] scaleM = { scale1, scale20, scale21, scale30, scale31 };
		DBRecordNumerical.BDG[2][1] = 
			prodMoment(new double[] { u[0], u[1] }, scaleM, numSamples);

		// M3
		scale30 = v_C1 + v_RC1 + v_AC1 + v_ARC1;
		scale31 = v_C1 + v_RC1 + v_BC1 + v_BRC1;
		scale20 = v_AR1 + v_AR0 + v_AC0 + v_ARC0;
		scale21 = v_BR1 + v_BR0 + v_BC0 + v_BRC0;
		scale1 = v_R1 + v_R0 + v_C0 + v_RC0;

		scaleM1[0] = scale1 + scale20;
		scaleM1[1] = scale30;
		scaleM1[2] = scale30;
		DBRecordNumerical.BDG[0][2] = 
			prodMoment1(new double[] { u[0], u[0] }, scaleM1, numSamples);

		scaleM1[0] = scale1 + scale21;
		scaleM1[1] = scale31;
		scaleM1[2] = scale31;
		DBRecordNumerical.BDG[1][2] = 
			prodMoment1(new double[] { u[1], u[1] }, scaleM1, numSamples);

		scaleM[0] = scale1;
		scaleM[1] = scale20;
		scaleM[2] = scale21;
		scaleM[3] = scale30;
		scaleM[4] = scale31;
		DBRecordNumerical.BDG[2][2] = 
			prodMoment(new double[] { u[0], u[1] }, scaleM, numSamples);

		// M4
		scale30 = v_C1 + v_RC1 + v_AC1 + v_ARC1 + v_C0 + v_RC0 + v_AC0 + v_ARC0;
		scale31 = v_C1 + v_RC1 + v_BC1 + v_BRC1 + v_C0 + v_RC0 + v_BC0 + v_BRC0;
		scale20 = v_AR1 + v_AR0;
		scale21 = v_BR1 + v_BR0;
		scale1 = v_R1 + v_R0;

		scaleM1[0] = scale1 + scale20;
		scaleM1[1] = scale30;
		scaleM1[2] = scale30;
		DBRecordNumerical.BDG[0][3] = 
			prodMoment1(new double[] { u[0], u[0] }, scaleM1, numSamples);

		scaleM1[0] = scale1 + scale21;
		scaleM1[1] = scale31;
		scaleM1[2] = scale31;
		DBRecordNumerical.BDG[1][3] = 
			prodMoment1(new double[] { u[1], u[1] }, scaleM1, numSamples);

		scaleM[0] = scale1;
		scaleM[1] = scale20;
		scaleM[2] = scale21;
		scaleM[3] = scale30;
		scaleM[4] = scale31;
		DBRecordNumerical.BDG[2][3] = 
			prodMoment(new double[] { u[0], u[1] }, scaleM, numSamples);

		// M5
		scale30 = v_R0 + v_R1 + v_RC0 + v_RC1 + v_AR0 + v_AR1 + v_ARC0 + v_ARC1;
		scale31 = v_R0 + v_R1 + v_RC0 + v_RC1 + v_BR0 + v_BR1 + v_BRC0 + v_BRC1;
		scale20 = v_AC0 + v_AC1;
		scale21 = v_BC0 + v_BC1;
		scale1 = v_C1 + v_C0;

		scaleM1[0] = scale1 + scale20;
		scaleM1[1] = scale30;
		scaleM1[2] = scale30;
		DBRecordNumerical.BDG[0][4] = 
			prodMoment1(new double[] { u[0], u[0] }, scaleM1, numSamples);

		scaleM1[0] = scale1 + scale21;
		scaleM1[1] = scale31;
		scaleM1[2] = scale31;
		DBRecordNumerical.BDG[1][4] = 
				prodMoment1(new double[] { u[1], u[1] }, scaleM1,
				numSamples);

		scaleM[0] = scale1;
		scaleM[1] = scale20;
		scaleM[2] = scale21;
		scaleM[3] = scale30;
		scaleM[4] = scale31;
		DBRecordNumerical.BDG[2][4] = 
			prodMoment(new double[] { u[0], u[1] }, scaleM, numSamples);

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
		DBRecordNumerical.BDG[0][5] = 
			prodMoment1(new double[] { u[0], u[0] }, scaleM1, numSamples);

		scaleM1[0] = scale1 + scale21;
		scaleM1[1] = scale31;
		scaleM1[2] = scale31;
		DBRecordNumerical.BDG[1][5] = 
			prodMoment1(new double[] { u[1], u[1] }, scaleM1, numSamples);

		scaleM[0] = scale1;
		scaleM[1] = scale20;
		scaleM[2] = scale21;
		scaleM[3] = scale30;
		scaleM[4] = scale31;
		DBRecordNumerical.BDG[2][5] = 
			prodMoment(new double[] { u[0], u[1] }, scaleM, numSamples);

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
		DBRecordNumerical.BDG[0][6] = 
			prodMoment1(new double[] { u[0], u[0] }, scaleM1, numSamples);

		scaleM1[0] = scale1 + scale21;
		scaleM1[1] = scale31;
		scaleM1[2] = scale31;
		DBRecordNumerical.BDG[1][6] = 
			prodMoment1(new double[] { u[1], u[1] }, scaleM1, numSamples);

		scaleM[0] = scale1;
		scaleM[1] = scale20;
		scaleM[2] = scale21;
		scaleM[3] = scale30;
		scaleM[4] = scale31;
		DBRecordNumerical.BDG[2][6] = 
			prodMoment(new double[] { u[0], u[1] }, scaleM, numSamples);

		// M8
		DBRecordNumerical.BDG[0][7] = 
				DBRecordNumerical.AUCsReaderAvg[0]*DBRecordNumerical.AUCsReaderAvg[0];
		DBRecordNumerical.BDG[1][7] = 
				DBRecordNumerical.AUCsReaderAvg[1]*DBRecordNumerical.AUCsReaderAvg[1];
		DBRecordNumerical.BDG[2][7] = 
				DBRecordNumerical.AUCsReaderAvg[0]*DBRecordNumerical.AUCsReaderAvg[1];
		
		// Set the coefficients
		DBRecordNumerical.DBRecordRoeMetzNumericalFill(SizePanelRoeMetz);
		
	}

}
