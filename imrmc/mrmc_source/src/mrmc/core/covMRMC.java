/*
 * covMRMC.java
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
 *     Performs variance analysis over two modalities, determines AUC
 */

package mrmc.core;

public class covMRMC {
	int Reader, Normal, Disease;
	int nmod = 2;
	double[] moments = new double[9];
	double[] biasM = new double[9];
	double[] c = new double[9];
	double[][] auc; // auc for each reader each modality
	double[] aucMod = new double[2];

	public double[] getM() {
		return moments;
	}

	public double[] getMb() {
		return biasM;
	}

	public double[] getC() {
		return c;
	}

	public double[] getaucMod() {
		return aucMod;
	}

	public covMRMC(double[][][] t0, int[][][] d0, double[][][] t1,
			int[][][] d1, int R, int N, int D) {
		Reader = R;
		Normal = N;
		Disease = D;
		double aucA = 0.0;
		double aucB = 0.0;
		double[][] w = new double[Reader][nmod];
		int[] pairs = new int[3];
		double totalwada = 0;
		double totalwbdb = 0;
		auc = new double[Reader][2];
		double[] bnumer = matrix.setZero(9);
		double[][] wadasaSumr = matrix.setZero(Normal, Disease);
		double[][] wbdbsbSumr = matrix.setZero(Normal, Disease);
		double[] wadasaSumir = matrix.setZero(Disease);
		double[] wbdbsbSumir = matrix.setZero(Disease);
		double[] wadasaSumjr = matrix.setZero(Normal);
		double[] wbdbsbSumjr = matrix.setZero(Normal);
		double wadasaSumijr = 0.0;
		double wbdbsbSumijr = 0.0;

		double[] bdenom = matrix.setZero(9);
		double[][] wadaSumr = matrix.setZero(Normal, Disease);
		double[][] wbdbSumr = matrix.setZero(Normal, Disease);
		double[] wadaSumir = matrix.setZero(Disease);
		double[] wbdbSumir = matrix.setZero(Disease);
		double[] wadaSumjr = matrix.setZero(Normal);
		double[] wbdbSumjr = matrix.setZero(Normal);
		double wadaSumijr = 0.0;
		double wbdbSumijr = 0.0;

		for (int i = 0; i < Reader; i++)
			for (int j = 0; j < nmod; j++)
				w[i][j] = 1.0;
		for (int i = 0; i < 3; i++)
			pairs[i] = 0;

		for (int ir = 0; ir < Reader; ir++) {
			// ***************for the first modality******************
			int[][] designA0 = matrix.extractFirstDimention(d0, ir, 0);
			int[][] designA1 = matrix.extractFirstDimention(d1, ir, 0);
			int[][] da = matrix.multiply(designA0,
					matrix.matrixTranspose(designA1));
			int totalda = matrix.total(da);
			double wa = w[ir][0];
			double[][] ta0 = matrix.extractFirstDimention(t0, ir, 0);
			double[][] ta0temp = matrix.linearTrans(ta0, 0.0, 1.0);
			double[][] ta1 = matrix.extractFirstDimention(t1, ir, 0);
			double[][] ta1temp = matrix.linearTrans(ta1, 0.0, 1.0);

			double[][] sa0 = matrix.multiply(ta0,
					matrix.matrixTranspose(ta1temp));
			double[][] sa1 = matrix.multiply(ta0temp,
					matrix.matrixTranspose(ta1));
			double[][] sa = matrix.subtract(sa1, sa0);
			for (int i = 0; i < Normal; i++)
				for (int j = 0; j < Disease; j++) {
					if (sa[i][j] < 0)
						sa[i][j] = 0.0;
					else if (sa[i][j] == 0)
						sa[i][j] = 0.5;
					else if (sa[i][j] > 0)
						sa[i][j] = 1.0;
				}
			double[][] wada = matrix.linearTrans(da, wa, 0);
			double[][] wadasa = matrix.elementMultiply(wada, sa);
			// ***************for the second modality******************
			int[][] designB0 = matrix.extractFirstDimention(d0, ir, 1);
			int[][] designB1 = matrix.extractFirstDimention(d1, ir, 1);
			int[][] db = matrix.multiply(designB0,
					matrix.matrixTranspose(designB1));
			int totaldb = matrix.total(db);
			double wb = w[ir][1];
			double[][] tb0 = matrix.extractFirstDimention(t0, ir, 1);
			double[][] tb0temp = matrix.linearTrans(tb0, 0.0, 1.0);
			double[][] tb1 = matrix.extractFirstDimention(t1, ir, 1);
			double[][] tb1temp = matrix.linearTrans(tb1, 0.0, 1.0);

			double[][] sb0 = matrix.multiply(tb0,
					matrix.matrixTranspose(tb1temp));
			double[][] sb1 = matrix.multiply(tb0temp,
					matrix.matrixTranspose(tb1));
			double[][] sb = matrix.subtract(sb1, sb0);
			for (int i = 0; i < Normal; i++)
				for (int j = 0; j < Disease; j++) {
					if (sb[i][j] < 0)
						sb[i][j] = 0.0;
					else if (sb[i][j] == 0)
						sb[i][j] = 0.5;
					else if (sb[i][j] > 0)
						sb[i][j] = 1.0;
				}
			double[][] wbdb = matrix.linearTrans(db, wb, 0);
			double[][] wbdbsb = matrix.elementMultiply(wbdb, sb);

			// ************count the readings ***********************
			pairs[0] = pairs[0] + totalda;
			pairs[1] = pairs[1] + totaldb;
			pairs[2] = pairs[2] + matrix.total(matrix.elementMultiply(da, db));

			// ***********precompute row (col???) sums***********************
			double[] wada_sumi = matrix.colSum(wada);
			double[] wbdb_sumi = matrix.colSum(wbdb);
			double[] wadasa_sumi = matrix.colSum(wadasa);
			double[] wbdbsb_sumi = matrix.colSum(wbdbsb);
			// ***********precompute col (row?????) sums***********************
			double[] wada_sumj = matrix.rowSum(wada);
			double[] wbdb_sumj = matrix.rowSum(wbdb);
			double[] wadasa_sumj = matrix.rowSum(wadasa);
			double[] wbdbsb_sumj = matrix.rowSum(wbdbsb);
			// **********precompute the matrix sums*****************
			double wada_sumij = matrix.total(wada);
			double wbdb_sumij = matrix.total(wbdb);
			double wadasa_sumij = matrix.total(wadasa);
			double wbdbsb_sumij = matrix.total(wbdbsb);

			// *********aggregate the sum over readers that will feed M1-M4
			bdenom[1] = bdenom[1]
					+ matrix.total(matrix.elementMultiply(wada, wbdb));
			bdenom[2] = bdenom[2]
					+ matrix.total(matrix.elementMultiply(wada_sumi, wbdb_sumi));
			bdenom[3] = bdenom[3]
					+ matrix.total(matrix.elementMultiply(wada_sumj, wbdb_sumj));
			bdenom[4] = bdenom[4] + wada_sumij * wbdb_sumij;
			bnumer[1] = bnumer[1]
					+ matrix.total(matrix.elementMultiply(wadasa, wbdbsb));
			bnumer[2] = bnumer[2]
					+ matrix.total(matrix.elementMultiply(wadasa_sumi,
							wbdbsb_sumi));
			bnumer[3] = bnumer[3]
					+ matrix.total(matrix.elementMultiply(wadasa_sumj,
							wbdbsb_sumj));
			bnumer[4] = bnumer[4] + wadasa_sumij * wbdbsb_sumij;

			// *********aggregate the sum over readers that will feed M5-M8
			wadaSumr = matrix.matrixAdd(wadaSumr, wada);
			wbdbSumr = matrix.matrixAdd(wbdbSumr, wbdb);
			wadasaSumr = matrix.matrixAdd(wadasaSumr, wadasa);
			wbdbsbSumr = matrix.matrixAdd(wbdbsbSumr, wbdbsb);
			wadaSumir = matrix.matrixAdd(wadaSumir, wada_sumi);
			wbdbSumir = matrix.matrixAdd(wbdbSumir, wbdb_sumi);
			wadasaSumir = matrix.matrixAdd(wadasaSumir, wadasa_sumi);
			wbdbsbSumir = matrix.matrixAdd(wbdbsbSumir, wbdbsb_sumi);

			wadaSumjr = matrix.matrixAdd(wadaSumjr, wada_sumj);
			wbdbSumjr = matrix.matrixAdd(wbdbSumjr, wbdb_sumj);
			wadasaSumjr = matrix.matrixAdd(wadasaSumjr, wadasa_sumj);
			wbdbsbSumjr = matrix.matrixAdd(wbdbsbSumjr, wbdbsb_sumj);
			wadaSumijr = wadaSumijr + wada_sumij;
			wbdbSumijr = wbdbSumijr + wbdb_sumij;
			wadasaSumijr = wadasaSumijr + wadasa_sumij;
			wbdbsbSumijr = wbdbsbSumijr + wbdbsb_sumij;

			// ------------------------------------------
			// calculate AUCs
			// ------------------------------------------
			// evaluate auc modality a
			if (totalda > 0) {
				totalwada = totalwada + wa * totalda;
				auc[ir][0] = matrix.total(wadasa) / totalda;
				aucA = aucA + totalda * auc[ir][0];
			}
			// evaluate auc modality b
			if (totaldb > 0) {
				totalwbdb = totalwbdb + wb * totaldb;
				auc[ir][1] = matrix.total(wbdbsb) / totaldb;
				aucB = aucB + totaldb * auc[ir][1];
			}

		} // end reader loop
		bdenom[5] = matrix.total(matrix.elementMultiply(wadaSumr, wbdbSumr));
		bdenom[6] = matrix.total(matrix.elementMultiply(wadaSumir, wbdbSumir));
		bdenom[7] = matrix.total(matrix.elementMultiply(wadaSumjr, wbdbSumjr));
		bdenom[8] = wadaSumijr * wbdbSumijr;
		bnumer[5] = matrix
				.total(matrix.elementMultiply(wadasaSumr, wbdbsbSumr));
		bnumer[6] = matrix.total(matrix.elementMultiply(wadasaSumir,
				wbdbsbSumir));
		bnumer[7] = matrix.total(matrix.elementMultiply(wadasaSumjr,
				wbdbsbSumjr));
		bnumer[8] = wadasaSumijr * wbdbsbSumijr;

		double[][] bias2unbias = new double[][] {
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 1.0, 0, 0, 0, 0, 0, 0, 0 },
				{ 0, -1.0, 1.0, 0, 0, 0, 0, 0, 0 },
				{ 0, -1.0, 0, 1.0, 0, 0, 0, 0, 0 },
				{ 0, 1.0, -1.0, -1.0, 1.0, 0, 0, 0, 0 },
				{ 0, -1.0, 0, 0, 0, 1.0, 0, 0, 0 },
				{ 0, 1.0, -1.0, 0, 0, -1.0, 1.0, 0, 0 },
				{ 0, 1.0, 0, -1.0, 0, -1.0, 0, 1.0, 0 },
				{ 0, -1.0, 1.0, 1.0, -1.0, 1.0, -1.0, -1.0, 1.0 } };

		double[] denom = matrix.multiply(bias2unbias, bdenom);
		double[] numer = matrix.multiply(bias2unbias, bnumer);
		// biased moments
		biasM = bnumer;
		for (int i = 0; i < biasM.length; i++) {
			if (bdenom[i] > matrix.min(w) / 2.0)
				biasM[i] = biasM[i] / bdenom[i];
		}

		// unbiased moment
		// double[] m = numer;
		moments = numer;
		for (int i = 0; i < moments.length; i++) {
			if (denom[i] > matrix.min(w) / 2.0)
				moments[i] = moments[i] / denom[i];
		}

		// coefficients
		double[] c = matrix
				.linearTrans(denom, 1.0 / (totalwada * totalwbdb), 0);
		c[8] = c[8] - 1.0;

		aucMod[0] = aucA / totalwada;
		aucMod[1] = aucB / totalwbdb;
		// System.out.println("AUC1="+aucMod[0]+"  AUC2="+aucMod[1]);
		// System.out.println("M1="+moments[1]+"M2="+moments[2]+"M3="+moments[3]+"M4="+moments[4]+"M5="+moments[5]+"M6="+moments[6]+"M7="+moments[7]+"M8="+moments[8]);
		// print, nr_master, auc_master
		// auc_master = mx.linearTrans(mx.setZero(nr_master+1,2), 1, -1);
		// auc_master(indexr,0) = auc(*,0);
		// auc_master(nr_master,0) = auc_a / total_wada;
		// auc_master(indexr,1) = auc(*,1);
		// auc_master(nr_master,1) = auc_b / total_wbdb;
		// auc = auc_master;

		// print, nr_master, auc_master
		// var = total(c*m)

	}
}
