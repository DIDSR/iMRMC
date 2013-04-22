package mrmc.core;

import java.util.Random;

public class SimRoeMetz {

	// Should use matrix library for most functions

	double[] stdDevs;
	int nr;
	int n0;
	int n1;
	double mu_0;
	double mu_1;
	double snr_0;
	double snr_1;
	double auc_0;
	double auc_1;
	double auc[];
	double[] R0;
	double[] C0;
	double[][] RC0;
	double[] R1;
	double[] C1;
	double[][] RC1;
	double[] R00;
	double[] C00;
	double[][] RC00;
	double[] R01;
	double[] C01;
	double[][] RC01;
	double[] R10;
	double[] C10;
	double[][] RC10;
	double[] R11;
	double[] C11;
	double[][] RC11;
	
	
	public void doSim(double[] u, double[] var_t, int[] n) {
		// TODO perform size checks
		
		mu_0 = u[0];
		mu_1 = u[1];
		
		stdDevs = new double[var_t.length];
		for (int i = 0; i < var_t.length; i++) {
			stdDevs[i] = Math.sqrt(var_t[i]);
		}
		
		n0 = n[0];
		n1 = n[1];
		nr = n[2];
		
		snr_0 = mu_0 / matrix.total(var_t);
		snr_1 = mu_1 / matrix.total(var_t);
		
		// auc_0 = snrtoauc(auc_0);
		// auc_1 = snrtoauc(auc_1);
		auc = new double[]{auc_0, auc_1, auc_0 - auc_1};
	
		Random rand = new Random(); // uses currentTimeMillis() as seed by default
		
		R0 = fillGaussian(stdDevs[0], nr, rand);
		C0 = fillGaussian(stdDevs[1], n0, rand);
		
		R00 = fillGaussian(stdDevs[3], nr, rand);
		
		
		R1 = fillGaussian(stdDevs[0], nr, rand);
		C1 = fillGaussian(stdDevs[1], n1, rand);
	
	}
	
	public double[] fillGaussian(double scalar, int n, Random rand){
		double[] toReturn = new double[n];
		for (int i = 0; i < n; i++){
			toReturn[i] = scalar * rand.nextGaussian();
		}
		return toReturn;
	}

}
