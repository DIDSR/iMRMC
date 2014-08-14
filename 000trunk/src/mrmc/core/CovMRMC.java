/**
 * CovMRMC.java
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

/**
 * Estimates covariance statistics given reader data for two modalities <br>
 * <ul>
 *   <li> {@link #getAUCsReaderAvg getAUCsReaderAvg},
 *   <li> {@link #getAUCs getAUCs},
 *   <li> {@link #getMoments getMoments},
 *   <li> {@link #getMomentsBiased getMomentsBiased},
 *   <li> {@link #getCoefficients getCoefficients},
 * </ul>
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
public class CovMRMC {
	
	/**
	 * The reader-averaged auc for each modality
	 */
	private double[] AUCsReaderAvg = new double[2];
	/**
	 * The AUCs for each reader and modality [Nreader][2]
	 */
	private double[][] AUCs;
	/**
	 * The U-statistic moments according to Gallas2009_Commun-Stat-A-Theor_v38p2586 (first element is empty).
	 */
	private double[] moments = new double[9];
	/**
	 * The MLE moments according to Gallas2009_Commun-Stat-A-Theor_v38p2586 (first element is empty).
	 */
	private double[] momentsBiased = new double[9];
	/**
	 * The coefficients according to Gallas2009_Commun-Stat-A-Theor_v38p2586 (first element is empty)
	 */
	private double[] coefficients = new double[9];

	/**
	 * Gets the reader-averaged AUCs for both modalities
	 * 
	 * @return {@link #AUCsReaderAvg}
	 */
	public double[] getAUCsReaderAvg() {
		return AUCsReaderAvg;
	}
	/**
	 * Gets the AUCs for each reader and modality [Nreader][2]
	 * 
	 * @return {@link #AUCs}
	 */
	public double[][] getAUCs() {
		return AUCs;
	}
	/**
	 * @return {@link #moments}
	 */
	public double[] getMoments() {
		return moments;
	}
	/**
	 * @return {@link #momentsBiased}
	 */
	public double[] getMomentsBiased() {
		return momentsBiased;
	}
	/**
	 * Gets the coefficients vector
	 * 
	 * @return Coefficients vector
	 */
	public double[] getCoefficients() {
		return coefficients;
	}

	/**
	 * Sole constructor for CovMRMC.
	 * 
	 * @param t0 Normal scores for all readers and both modalities [Nnormal][Nreader][2]
	 * @param d0 Design matrix for all readers and both modalities [Nnormal][Nreader][2]
	 * @param t1 Scores scores for all readers and both modalities [Ndisease][Nreader][2] 
	 * @param d1 Design matrix for all readers and both modalities [Ndisease][Nreader][2]
	 * @param Nreader Number of readers
	 * @param Nnormal Number of normal cases
	 * @param Ndisease Number of disease cases
	 */
	public CovMRMC(double[][][] t0, int[][][] d0, double[][][] t1,
			int[][][] d1, long Nreader, long Nnormal, long Ndisease) {

		AUCs = new double[(int) Nreader][2];
		double aucA = 0.0;
		double aucB = 0.0;
		
		double[][] w = new double[(int) Nreader][2];
		int[] pairs = new int[3];
		double totalwada = 0;
		double totalwbdb = 0;
		double[] bnumer = new double[9];
		double[][] wadasaSumr = new double[(int) Nnormal][(int) Ndisease];
		double[][] wbdbsbSumr = new double[(int) Nnormal][(int) Ndisease];
		double[] wadasaSumir = new double[(int) Ndisease];
		double[] wbdbsbSumir = new double[(int) Ndisease];
		double[] wadasaSumjr = new double[(int) Nnormal];
		double[] wbdbsbSumjr = new double[(int) Nnormal];
		double wadasaSumijr = 0.0;
		double wbdbsbSumijr = 0.0;

		double[] bdenom = new double[9];
		double[][] wadaSumr = new double[(int) Nnormal][(int) Ndisease];
		double[][] wbdbSumr = new double[(int) Nnormal][(int) Ndisease];
		double[] wadaSumir = new double[(int) Ndisease];
		double[] wbdbSumir = new double[(int) Ndisease];
		double[] wadaSumjr = new double[(int) Nnormal];
		double[] wbdbSumjr = new double[(int) Nnormal];
		double wadaSumijr = 0.0;
		double wbdbSumijr = 0.0;

		for (int i = 0; i < Nreader; i++) {
			for (int j = 0; j < 2; j++) {
				w[i][j] = 1.0;
			}
		}
		for (int i = 0; i < 3; i++) {
			pairs[i] = 0;
		}

		for (int ir = 0; ir < Nreader; ir++) {
			// ***************for the first modality******************
			int[][] designA0 = Matrix.extractFirstDimension(d0, ir, 0);
			int[][] designA1 = Matrix.extractFirstDimension(d1, ir, 0);
			int[][] da = Matrix.multiply(designA0,
					Matrix.matrixTranspose(designA1));
			int totalda = Matrix.total(da);
			double wa = w[ir][0];
			double[][] t0A_ir = Matrix.extractFirstDimension(t0, ir, 0);
			double[][] t1A_ir = Matrix.extractFirstDimension(t1, ir, 0);

			// make vectors of ones
			double[][] ones_vect0 = Matrix.linearTrans(t0A_ir, 0.0, 1.0);
			double[][] ones_vect1 = Matrix.linearTrans(t1A_ir, 0.0, 1.0);

			double[][] sa0 = Matrix.multiply(t0A_ir,
					Matrix.matrixTranspose(ones_vect1));
			double[][] sa1 = Matrix.multiply(ones_vect0,
					Matrix.matrixTranspose(t1A_ir));
			double[][] sa = Matrix.subtract(sa1, sa0);
			for (int i = 0; i < Nnormal; i++)
				for (int j = 0; j < Ndisease; j++) {
					if (sa[i][j] < 0)
						sa[i][j] = 0.0;
					else if (sa[i][j] == 0)
						sa[i][j] = 0.5;
					else if (sa[i][j] > 0)
						sa[i][j] = 1.0;
				}
			double[][] wada = Matrix.linearTrans(da, wa, 0);
			double[][] wadasa = Matrix.elementMultiply(wada, sa);
			// ***************for the second modality******************
			int[][] designB0 = Matrix.extractFirstDimension(d0, ir, 1);
			int[][] designB1 = Matrix.extractFirstDimension(d1, ir, 1);
			int[][] db = Matrix.multiply(designB0,
					Matrix.matrixTranspose(designB1));
			int totaldb = Matrix.total(db);
			double wb = w[ir][1];
			double[][] t0B_ir = Matrix.extractFirstDimension(t0, ir, 1);
			double[][] t1B_ir = Matrix.extractFirstDimension(t1, ir, 1);

			double[][] sb0 = Matrix.multiply(t0B_ir,
					Matrix.matrixTranspose(ones_vect1));
			double[][] sb1 = Matrix.multiply(ones_vect0,
					Matrix.matrixTranspose(t1B_ir));
			double[][] sb = Matrix.subtract(sb1, sb0);
			for (int i = 0; i < Nnormal; i++)
				for (int j = 0; j < Ndisease; j++) {
					if (sb[i][j] < 0)
						sb[i][j] = 0.0;
					else if (sb[i][j] == 0)
						sb[i][j] = 0.5;
					else if (sb[i][j] > 0)
						sb[i][j] = 1.0;
				}
			double[][] wbdb = Matrix.linearTrans(db, wb, 0);
			double[][] wbdbsb = Matrix.elementMultiply(wbdb, sb);

			// ************count the readings ***********************
			pairs[0] = pairs[0] + totalda;
			pairs[1] = pairs[1] + totaldb;
			pairs[2] = pairs[2] + Matrix.total(Matrix.elementMultiply(da, db));

			// ***********precompute row (col???) sums***********************
			double[] wada_sumi = Matrix.colSum(wada);
			double[] wbdb_sumi = Matrix.colSum(wbdb);
			double[] wadasa_sumi = Matrix.colSum(wadasa);
			double[] wbdbsb_sumi = Matrix.colSum(wbdbsb);
			// ***********precompute col (row?????) sums***********************
			double[] wada_sumj = Matrix.rowSum(wada);
			double[] wbdb_sumj = Matrix.rowSum(wbdb);
			double[] wadasa_sumj = Matrix.rowSum(wadasa);
			double[] wbdbsb_sumj = Matrix.rowSum(wbdbsb);
			// **********precompute the matrix sums*****************
			double wada_sumij = Matrix.total(wada);
			double wbdb_sumij = Matrix.total(wbdb);
			double wadasa_sumij = Matrix.total(wadasa);
			double wbdbsb_sumij = Matrix.total(wbdbsb);

			// *********aggregate the sum over readers that will feed M1-M4
			bdenom[1] = bdenom[1]
					+ Matrix.total(Matrix.elementMultiply(wada, wbdb));
			bdenom[2] = bdenom[2]
					+ Matrix.total(Matrix.elementMultiply(wada_sumi, wbdb_sumi));
			bdenom[3] = bdenom[3]
					+ Matrix.total(Matrix.elementMultiply(wada_sumj, wbdb_sumj));
			bdenom[4] = bdenom[4] + wada_sumij * wbdb_sumij;
			bnumer[1] = bnumer[1]
					+ Matrix.total(Matrix.elementMultiply(wadasa, wbdbsb));
			bnumer[2] = bnumer[2]
					+ Matrix.total(Matrix.elementMultiply(wadasa_sumi,
							wbdbsb_sumi));
			bnumer[3] = bnumer[3]
					+ Matrix.total(Matrix.elementMultiply(wadasa_sumj,
							wbdbsb_sumj));
			bnumer[4] = bnumer[4] + wadasa_sumij * wbdbsb_sumij;

			// *********aggregate the sum over readers that will feed M5-M8
			wadaSumr = Matrix.matrixAdd(wadaSumr, wada);
			wbdbSumr = Matrix.matrixAdd(wbdbSumr, wbdb);
			wadasaSumr = Matrix.matrixAdd(wadasaSumr, wadasa);
			wbdbsbSumr = Matrix.matrixAdd(wbdbsbSumr, wbdbsb);
			wadaSumir = Matrix.matrixAdd(wadaSumir, wada_sumi);
			wbdbSumir = Matrix.matrixAdd(wbdbSumir, wbdb_sumi);
			wadasaSumir = Matrix.matrixAdd(wadasaSumir, wadasa_sumi);
			wbdbsbSumir = Matrix.matrixAdd(wbdbsbSumir, wbdbsb_sumi);

			wadaSumjr = Matrix.matrixAdd(wadaSumjr, wada_sumj);
			wbdbSumjr = Matrix.matrixAdd(wbdbSumjr, wbdb_sumj);
			wadasaSumjr = Matrix.matrixAdd(wadasaSumjr, wadasa_sumj);
			wbdbsbSumjr = Matrix.matrixAdd(wbdbsbSumjr, wbdbsb_sumj);
			wadaSumijr = wadaSumijr + wada_sumij;
			wbdbSumijr = wbdbSumijr + wbdb_sumij;
			wadasaSumijr = wadasaSumijr + wadasa_sumij;
			wbdbsbSumijr = wbdbsbSumijr + wbdbsb_sumij;

			// ------------------------------------------
			// calculate AUCs
			// ------------------------------------------
			// evaluate AUCs modality a
			if (totalda > 0) {
				totalwada = totalwada + wa * totalda;
				AUCs[ir][0] = Matrix.total(wadasa) / totalda;
				aucA = aucA + totalda * AUCs[ir][0];
			}
			// evaluate AUCs modality b
			if (totaldb > 0) {
				totalwbdb = totalwbdb + wb * totaldb;
				AUCs[ir][1] = Matrix.total(wbdbsb) / totaldb;
				aucB = aucB + totaldb * AUCs[ir][1];
			}

		} // end reader loop

		bdenom[5] = Matrix.total(Matrix.elementMultiply(wadaSumr, wbdbSumr));
		bdenom[6] = Matrix.total(Matrix.elementMultiply(wadaSumir, wbdbSumir));
		bdenom[7] = Matrix.total(Matrix.elementMultiply(wadaSumjr, wbdbSumjr));
		bdenom[8] = wadaSumijr * wbdbSumijr;
		bnumer[5] = Matrix
				.total(Matrix.elementMultiply(wadasaSumr, wbdbsbSumr));
		bnumer[6] = Matrix.total(Matrix.elementMultiply(wadasaSumir,
				wbdbsbSumir));
		bnumer[7] = Matrix.total(Matrix.elementMultiply(wadasaSumjr,
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

		double[] denom = Matrix.multiply(bias2unbias, bdenom);
		double[] numer = Matrix.multiply(bias2unbias, bnumer);
		// biased moments
		momentsBiased = bnumer;
		for (int i = 0; i < momentsBiased.length; i++) {
			if (bdenom[i] > Matrix.min(w) / 2.0)
				momentsBiased[i] = momentsBiased[i] / bdenom[i];
		}

		// unbiased moment
		// double[] m = numer;
		moments = numer;
		for (int i = 0; i < moments.length; i++) {
			if (denom[i] > Matrix.min(w) / 2.0)
				moments[i] = moments[i] / denom[i];
		}

		// coefficients
		coefficients = Matrix.linearTrans(denom, 1.0 / (totalwada * totalwbdb), 0);
		coefficients[8] = coefficients[8] - 1.0;

		AUCsReaderAvg[0] = aucA / totalwada;
		AUCsReaderAvg[1] = aucB / totalwbdb;
	}
}
