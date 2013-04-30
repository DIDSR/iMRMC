/* class to run tests on generalized roe & metz */

package simroemetz.core;

import mrmc.core.matrix;

public class runGen {
	public static void main(String[] args) {
		int nmc = 10001;
		double[] hypothesis = { 1.5, 1.0, 1.5, 1.0 };

		double[] genCofV_scores = { 1.0 / 6.0, 1.0 / 6.0, 1.0 / 6.0, 1.0 / 6.0,
				1.0 / 6.0, 1.0 / 6.0, 1.0 / 6.0, 1.0 / 6.0, 1.0 / 6.0,
				1.0 / 6.0, 1.0 / 6.0, 1.0 / 6.0, 1.0 / 6.0, 1.0 / 6.0,
				1.0 / 6.0, 1.0 / 6.0, 1.0 / 6.0, 1.0 / 6.0 };
		int[] n = { 200, 200, 20 };

		SimRoeMetz.doSim(new double[] { hypothesis[0], hypothesis[1] },
				genCofV_scores, n);

		System.out.println("t00:");
		matrix.printMatrix(SimRoeMetz.t00);
		System.out.println();
		System.out.println("t01:");
		matrix.printMatrix(SimRoeMetz.t01);
		System.out.println();
		System.out.println("t10:");
		matrix.printMatrix(SimRoeMetz.t10);
		System.out.println();
		System.out.println("t11:");
		matrix.printMatrix(SimRoeMetz.t11);
		System.out.println();
		System.out.println("AUCA\t\tAUCB\t\tAUCA-B");
		matrix.printVector(SimRoeMetz.auc);
		System.out.println();

		CofVGenRoeMetz.genRoeMetz(
				new double[] { hypothesis[0], hypothesis[1] }, 256,
				genCofV_scores);

		double[][][] genCofV_auc = CofVGenRoeMetz.cofv_auc;

		System.out
				.println("Components of variance and variance from cofv_genroemetz");
		System.out.println("mod0\t\tmod1\t\tcov01\t\tvardiff");
		double[] temp1 = matrix.matrixAdd(
				matrix.get1Dimension(0, genCofV_auc, "0", "0", "*"),
				matrix.get1Dimension(0, genCofV_auc, "1", "1", "*"));
		double[] temp2 = matrix.matrixAdd(temp1, matrix.scaleVector(
				matrix.get1Dimension(0, genCofV_auc, "1", "0", "*"), -2.0));
		double[][] temp = {
				matrix.get1Dimension(0, genCofV_auc, "0", "0", "*"),
				matrix.get1Dimension(0, genCofV_auc, "1", "1", "*"),
				matrix.get1Dimension(0, genCofV_auc, "1", "0", "*"), temp2 };
		double[][] toPrint = matrix.matrixTranspose(temp);
		matrix.printMatrix(toPrint);
	}
}
