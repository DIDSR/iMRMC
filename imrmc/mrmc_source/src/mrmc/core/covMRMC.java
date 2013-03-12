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
		matrix mx = new matrix();
		double aucA = 0.0;
		double aucB = 0.0;
		double[][] w = new double[Reader][nmod];
		int[] pairs = new int[3];
		double totalwada = 0;
		double totalwbdb = 0;
		auc = new double[Reader][2];
		double[] bnumer = mx.setZero(9);
		double[][] wadasaSumr = mx.setZero(Normal, Disease);
		double[][] wbdbsbSumr = mx.setZero(Normal, Disease);
		double[] wadasaSumir = mx.setZero(Disease);
		double[] wbdbsbSumir = mx.setZero(Disease);
		double[] wadasaSumjr = mx.setZero(Normal);
		double[] wbdbsbSumjr = mx.setZero(Normal);
		double wadasaSumijr = 0.0;
		double wbdbsbSumijr = 0.0;

		double[] bdenom = mx.setZero(9);
		double[][] wadaSumr = mx.setZero(Normal, Disease);
		double[][] wbdbSumr = mx.setZero(Normal, Disease);
		double[] wadaSumir = mx.setZero(Disease);
		double[] wbdbSumir = mx.setZero(Disease);
		double[] wadaSumjr = mx.setZero(Normal);
		double[] wbdbSumjr = mx.setZero(Normal);
		double wadaSumijr = 0.0;
		double wbdbSumijr = 0.0;

		for (int i = 0; i < Reader; i++)
			for (int j = 0; j < nmod; j++)
				w[i][j] = 1.0;
		for (int i = 0; i < 3; i++)
			pairs[i] = 0;

		for (int ir = 0; ir < Reader; ir++) {
			// ***************for the first modality******************
			int[][] designA0 = mx.extractFirstDimention(d0, ir, 0);
			int[][] designA1 = mx.extractFirstDimention(d1, ir, 0);
			int[][] da = mx.multiply(designA0, mx.matrixTranspose(designA1));
			int totalda = mx.total(da);
			double wa = w[ir][0];
			double[][] ta0 = mx.extractFirstDimention(t0, ir, 0);
			double[][] ta0temp = mx.linearTrans(ta0, 0.0, 1.0);
			double[][] ta1 = mx.extractFirstDimention(t1, ir, 0);
			double[][] ta1temp = mx.linearTrans(ta1, 0.0, 1.0);

			double[][] sa0 = mx.multiply(ta0, mx.matrixTranspose(ta1temp));
			double[][] sa1 = mx.multiply(ta0temp, mx.matrixTranspose(ta1));
			double[][] sa = mx.subtract(sa1, sa0);
			for (int i = 0; i < Normal; i++)
				for (int j = 0; j < Disease; j++) {
					if (sa[i][j] < 0)
						sa[i][j] = 0.0;
					else if (sa[i][j] == 0)
						sa[i][j] = 0.5;
					else if (sa[i][j] > 0)
						sa[i][j] = 1.0;
				}
			double[][] wada = mx.linearTrans(da, wa, 0);
			double[][] wadasa = mx.elementMultiply(wada, sa);
			// ***************for the second modality******************
			int[][] designB0 = mx.extractFirstDimention(d0, ir, 1);
			int[][] designB1 = mx.extractFirstDimention(d1, ir, 1);
			int[][] db = mx.multiply(designB0, mx.matrixTranspose(designB1));
			int totaldb = mx.total(db);
			double wb = w[ir][1];
			double[][] tb0 = mx.extractFirstDimention(t0, ir, 1);
			double[][] tb0temp = mx.linearTrans(tb0, 0.0, 1.0);
			double[][] tb1 = mx.extractFirstDimention(t1, ir, 1);
			double[][] tb1temp = mx.linearTrans(tb1, 0.0, 1.0);

			double[][] sb0 = mx.multiply(tb0, mx.matrixTranspose(tb1temp));
			double[][] sb1 = mx.multiply(tb0temp, mx.matrixTranspose(tb1));
			double[][] sb = mx.subtract(sb1, sb0);
			for (int i = 0; i < Normal; i++)
				for (int j = 0; j < Disease; j++) {
					if (sb[i][j] < 0)
						sb[i][j] = 0.0;
					else if (sb[i][j] == 0)
						sb[i][j] = 0.5;
					else if (sb[i][j] > 0)
						sb[i][j] = 1.0;
				}
			double[][] wbdb = mx.linearTrans(db, wb, 0);
			double[][] wbdbsb = mx.elementMultiply(wbdb, sb);

			// ************count the readings ***********************
			pairs[0] = pairs[0] + totalda;
			pairs[1] = pairs[1] + totaldb;
			pairs[2] = pairs[2] + mx.total(mx.elementMultiply(da, db));

			// ***********precompute row (col???) sums***********************
			double[] wada_sumi = mx.colSum(wada);
			double[] wbdb_sumi = mx.colSum(wbdb);
			double[] wadasa_sumi = mx.colSum(wadasa);
			double[] wbdbsb_sumi = mx.colSum(wbdbsb);
			// ***********precompute col (row?????) sums***********************
			double[] wada_sumj = mx.rowSum(wada);
			double[] wbdb_sumj = mx.rowSum(wbdb);
			double[] wadasa_sumj = mx.rowSum(wadasa);
			double[] wbdbsb_sumj = mx.rowSum(wbdbsb);
			// **********precompute the matrix sums*****************
			double wada_sumij = mx.total(wada);
			double wbdb_sumij = mx.total(wbdb);
			double wadasa_sumij = mx.total(wadasa);
			double wbdbsb_sumij = mx.total(wbdbsb);

			// *********aggregate the sum over readers that will feed M1-M4
			bdenom[1] = bdenom[1] + mx.total(mx.elementMultiply(wada, wbdb));
			bdenom[2] = bdenom[2]
					+ mx.total(mx.elementMultiply(wada_sumi, wbdb_sumi));
			bdenom[3] = bdenom[3]
					+ mx.total(mx.elementMultiply(wada_sumj, wbdb_sumj));
			bdenom[4] = bdenom[4] + wada_sumij * wbdb_sumij;
			bnumer[1] = bnumer[1]
					+ mx.total(mx.elementMultiply(wadasa, wbdbsb));
			bnumer[2] = bnumer[2]
					+ mx.total(mx.elementMultiply(wadasa_sumi, wbdbsb_sumi));
			bnumer[3] = bnumer[3]
					+ mx.total(mx.elementMultiply(wadasa_sumj, wbdbsb_sumj));
			bnumer[4] = bnumer[4] + wadasa_sumij * wbdbsb_sumij;

			// *********aggregate the sum over readers that will feed M5-M8
			wadaSumr = mx.matrixAdd(wadaSumr, wada);
			wbdbSumr = mx.matrixAdd(wbdbSumr, wbdb);
			wadasaSumr = mx.matrixAdd(wadasaSumr, wadasa);
			wbdbsbSumr = mx.matrixAdd(wbdbsbSumr, wbdbsb);
			wadaSumir = mx.matrixAdd(wadaSumir, wada_sumi);
			wbdbSumir = mx.matrixAdd(wbdbSumir, wbdb_sumi);
			wadasaSumir = mx.matrixAdd(wadasaSumir, wadasa_sumi);
			wbdbsbSumir = mx.matrixAdd(wbdbsbSumir, wbdbsb_sumi);

			wadaSumjr = mx.matrixAdd(wadaSumjr, wada_sumj);
			wbdbSumjr = mx.matrixAdd(wbdbSumjr, wbdb_sumj);
			wadasaSumjr = mx.matrixAdd(wadasaSumjr, wadasa_sumj);
			wbdbsbSumjr = mx.matrixAdd(wbdbsbSumjr, wbdbsb_sumj);
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
				auc[ir][0] = mx.total(wadasa) / totalda;
				aucA = aucA + totalda * auc[ir][0];
			}
			// evaluate auc modality b
			if (totaldb > 0) {
				totalwbdb = totalwbdb + wb * totaldb;
				auc[ir][1] = mx.total(wbdbsb) / totaldb;
				aucB = aucB + totaldb * auc[ir][1];
			}

		} // end reader loop
		bdenom[5] = mx.total(mx.elementMultiply(wadaSumr, wbdbSumr));
		bdenom[6] = mx.total(mx.elementMultiply(wadaSumir, wbdbSumir));
		bdenom[7] = mx.total(mx.elementMultiply(wadaSumjr, wbdbSumjr));
		bdenom[8] = wadaSumijr * wbdbSumijr;
		bnumer[5] = mx.total(mx.elementMultiply(wadasaSumr, wbdbsbSumr));
		bnumer[6] = mx.total(mx.elementMultiply(wadasaSumir, wbdbsbSumir));
		bnumer[7] = mx.total(mx.elementMultiply(wadasaSumjr, wbdbsbSumjr));
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

		double[] denom = mx.multiply(bias2unbias, bdenom);
		double[] numer = mx.multiply(bias2unbias, bnumer);
		// biased moments
		biasM = bnumer;
		for (int i = 0; i < biasM.length; i++) {
			if (bdenom[i] > mx.min(w) / 2.0)
				biasM[i] = biasM[i] / bdenom[i];
		}

		// unbiased moment
		// double[] m = numer;
		moments = numer;
		for (int i = 0; i < moments.length; i++) {
			if (denom[i] > mx.min(w) / 2.0)
				moments[i] = moments[i] / denom[i];
		}

		// coefficients
		double[] c = mx.linearTrans(denom, 1.0 / (totalwada * totalwbdb), 0);
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
