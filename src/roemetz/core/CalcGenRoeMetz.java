package roemetz.core;

import mrmc.core.CovMRMC;
import mrmc.core.DBRecord;
import mrmc.core.Matrix;
import mrmc.gui.SizePanel;

//import org.apache.commons.math3.distribution.NormalDistribution;

import umontreal.iro.lecuyer.probdist.NormalDist;

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
	 * cdfs follow Gallas2014_J-Med-Img_v1p031006 function 12
	 * 
	 * @param u Contains experiment means
	 * @param scale Contains one 1-D gaussian pdf and two 2-D normal cdfs
	 * @param numSamples Number of samples for numerical integration
	 * @return Integrated product moment
	 */
	private static double prodMomentSingleModality(double[] u, double sigmaSqOmega,
		double sigmaSqOmegaMoment, double sigmaSqOneMod, double sigmaSqOneModMoment, int numSamples) {
		NormalDist gaussNew = new NormalDist();
		// calculate temp Numerator
		double tempNum = Math.sqrt(sigmaSqOmega + sigmaSqOneMod - sigmaSqOmegaMoment - sigmaSqOneModMoment);
		// calculate temp Denominator
		double tempDen = Math.sqrt(sigmaSqOmegaMoment + sigmaSqOneModMoment);
		
		// calculate x
		double lx = 10.0;
		double dx = lx / (double) numSamples;
		double[] x = new double[numSamples];
		double Integral = 0.0;
		for (int i = 0; i < numSamples; i++) {
			x[i] = ((double) i * dx) - (0.5 * lx);
		}

		double[] phi_x = new double[numSamples];
		double[] cdfOneMod = new double[numSamples];

		for (int i = 0; i < numSamples; i++) {
			// calculate phi for normal gaussian distribution
			phi_x[i] = Math.exp(-(x[i] * x[i]) / 2.0 )
					/ Math.sqrt(Math.PI * 2.0);
			// if Denominator is 0, set cdf to 1
			if (tempDen!=0){
				cdfOneMod[i] = gaussNew.cdf((u[0] + x[i]*tempNum)/tempDen);
			}else{
				cdfOneMod[i] = 1;
			}		
			// do integral
			Integral = Integral + dx*phi_x[i]*cdfOneMod[i]*cdfOneMod[i];	
		}
		return Integral;
	}
	/**
	 * Numerically integrates a two dimensional gaussian pdf times a gaussian
	 * cdf follow Gallas2014_J-Med-Img_v1p031006 function 15
	 * 
	 * @param u Contains experiment means.
	 * @param scale Contains 2-D gaussian pdf and cdf
	 * @param numSamples Number of samples for numerical integration
	 * @return Integrated product moment
	 */
	private static double prodMomentTwoModalities(double[] u, double sigmaSqOmega,
			double sigmaSqOmegaMoment, double sigmaSqA, double sigmaSqB, int numSamples) {
		NormalDist gaussNew = new NormalDist();
		// calculate temp Numerator
		double tempNumA = Math.sqrt(sigmaSqOmega - sigmaSqOmegaMoment); 
		double tempNumB = Math.sqrt(sigmaSqOmega - sigmaSqOmegaMoment); 
		// calculate temp Denominator
		double tempDenA = Math.sqrt(sigmaSqA + sigmaSqOmegaMoment); 
		double tempDenB = Math.sqrt(sigmaSqB + sigmaSqOmegaMoment);
		
		// calculate x
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
			// calculate phi for normal gaussian distribution
			phi_x[i] = Math.exp(-(x[i] * x[i]) / 2.0 )
					/ Math.sqrt(Math.PI * 2.0);
			// if Denominator is 0, set cdf to 1
			if (tempDenA != 0){
				cdf_A[i] = gaussNew.cdf((u[0] + x[i]*tempNumA)/tempDenA);
			}else{
				cdf_A[i] = 1;
			}
			// if Denominator is 0, set cdf to 1
			if (tempDenB != 0){
				cdf_B[i] = gaussNew.cdf((u[1] + x[i]*tempNumB)/tempDenB);;
			}else{
				cdf_B[i] = 1;
			}
			// do integral
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

	//	NormalDistribution gauss = new NormalDistribution();
		NormalDist gaussNew = new NormalDist();
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
		
		// define sigmaSqOmega, simgaSqModality, and simgaSqMoment follow Gallas2014_J-Med-Img_v1p031006 
		double sigmaSqOmega = v_R0 + v_C0 + v_RC0 + v_R1 + v_C1 + v_RC1;
		double sigmaSqA =  v_AR0 + v_AC0 + v_ARC0 + v_AR1 + v_AC1 + v_ARC1;
		double sigmaSqB = v_BR0 + v_BC0 + v_BRC0 + v_BR1 + v_BC1 + v_BRC1;
		double sigmaSqAMoment = 0;
		double sigmaSqBMoment = 0;
		double sigmaSqOmegaMoment = 0;
		
		
		DBRecordNumerical.AUCsReaderAvg = new double[2];
		DBRecordNumerical.AUCs = new double[(int) DBRecordNumerical.Nreader][2];

		DBRecordNumerical.AUCsReaderAvg[0] = 
				gaussNew.cdf(u[0] / Math.sqrt(sigmaSqOmega + sigmaSqA));
		DBRecordNumerical.AUCsReaderAvg[1] = 
				gaussNew.cdf(u[1] / Math.sqrt(sigmaSqOmega + sigmaSqB));

		for(int r=0; r<DBRecordNumerical.Nreader; r++) {
			DBRecordNumerical.AUCs[r][0] = DBRecordNumerical.AUCsReaderAvg[0];
			DBRecordNumerical.AUCs[r][1] = DBRecordNumerical.AUCsReaderAvg[1];
		}
		
		// M1
		DBRecordNumerical.BDG[0][0] = DBRecordNumerical.AUCsReaderAvg[0];
		DBRecordNumerical.BDG[1][0] = DBRecordNumerical.AUCsReaderAvg[1];
		DBRecordNumerical.BDG[2][0] = 
			prodMomentTwoModalities(new double[] { u[0], u[1] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqA, sigmaSqB, numSamples);

		// M2
		sigmaSqAMoment = v_AC0 + v_ARC0;
		sigmaSqBMoment = v_BC0 + v_BRC0;
		sigmaSqOmegaMoment = v_C0 + v_RC0;

		DBRecordNumerical.BDG[0][1] = 
			prodMomentSingleModality(new double[] { u[0], u[0] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqA, sigmaSqAMoment, numSamples);
		DBRecordNumerical.BDG[1][1] = 
			prodMomentSingleModality(new double[] { u[1], u[1] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqB, sigmaSqBMoment, numSamples);
		DBRecordNumerical.BDG[2][1] = 
			prodMomentTwoModalities(new double[] { u[0], u[1] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqA, sigmaSqB, numSamples);
		
		// M3
		sigmaSqAMoment = v_AC1 + v_ARC1;
		sigmaSqBMoment = v_BC1 + v_BRC1;
		sigmaSqOmegaMoment = v_C1 + v_RC1 ;
		
		DBRecordNumerical.BDG[0][2] = 
			prodMomentSingleModality(new double[] { u[0], u[0] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqA, sigmaSqAMoment, numSamples);
		DBRecordNumerical.BDG[1][2] = 
			prodMomentSingleModality(new double[] { u[1], u[1] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqB, sigmaSqBMoment, numSamples);
		DBRecordNumerical.BDG[2][2] = 
			prodMomentTwoModalities(new double[] { u[0], u[1] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqA, sigmaSqB, numSamples);
		
		// M4
		sigmaSqAMoment = v_AC1 + v_ARC1 + v_AC0 + v_ARC0;
		sigmaSqBMoment = v_BC1 + v_BRC1 + v_BC0 + v_BRC0;
		sigmaSqOmegaMoment = v_C1 + v_RC1 + v_C0 + v_RC0;

		DBRecordNumerical.BDG[0][3] = 
			prodMomentSingleModality(new double[] { u[0], u[0] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqA, sigmaSqAMoment, numSamples);
		DBRecordNumerical.BDG[1][3] = 
			prodMomentSingleModality(new double[] { u[1], u[1] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqB, sigmaSqBMoment, numSamples);
		DBRecordNumerical.BDG[2][3] = 
			prodMomentTwoModalities(new double[] { u[0], u[1] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqA, sigmaSqB, numSamples);
		
		// M5
		sigmaSqAMoment = v_AR0 + v_ARC0 + v_AR1 + v_ARC1;
		sigmaSqBMoment = v_BR0 + v_BRC0 + v_BR1 + v_BRC1;
		sigmaSqOmegaMoment = v_R0 + v_RC0 + v_R1 + v_RC1;

		DBRecordNumerical.BDG[0][4] = 
			prodMomentSingleModality(new double[] { u[0], u[0] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqA, sigmaSqAMoment, numSamples);
		DBRecordNumerical.BDG[1][4] = 
			prodMomentSingleModality(new double[] { u[1], u[1] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqB, sigmaSqBMoment, numSamples);
		DBRecordNumerical.BDG[2][4] = 
			prodMomentTwoModalities(new double[] { u[0], u[1] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqA, sigmaSqB, numSamples);
		
		// M6	
		sigmaSqAMoment = v_AR0 + v_AC0  + v_ARC0 + v_AR1 + v_ARC1;
		sigmaSqBMoment = v_BR0 + v_BC0  + v_BRC0 + v_BR1 + v_BRC1;
		sigmaSqOmegaMoment = v_R0 + v_C0 + v_RC0 + v_R1 + v_RC1;
		
		DBRecordNumerical.BDG[0][5] = 
			prodMomentSingleModality(new double[] { u[0], u[0] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqA, sigmaSqAMoment, numSamples);
		DBRecordNumerical.BDG[1][5] = 
			prodMomentSingleModality(new double[] { u[1], u[1] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqB, sigmaSqBMoment, numSamples);
		DBRecordNumerical.BDG[2][5] = 
			prodMomentTwoModalities(new double[] { u[0], u[1] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqA, sigmaSqB, numSamples);
		
		// M7	
		sigmaSqAMoment = v_AR0 + v_ARC0 + v_AR1 + v_AC1 + v_ARC1;
		sigmaSqBMoment = v_BR0 + v_BRC0 + v_BR1 + v_BC1 + v_BRC1;
		sigmaSqOmegaMoment = v_R0 + v_RC0 + v_R1 + v_C1 + v_RC1;
		
		DBRecordNumerical.BDG[0][6] = 
			prodMomentSingleModality(new double[] { u[0], u[0] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqA, sigmaSqAMoment, numSamples);
		DBRecordNumerical.BDG[1][6] = 
			prodMomentSingleModality(new double[] { u[1], u[1] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqB, sigmaSqBMoment, numSamples);
		DBRecordNumerical.BDG[2][6] = 
			prodMomentTwoModalities(new double[] { u[0], u[1] }, sigmaSqOmega, sigmaSqOmegaMoment, sigmaSqA, sigmaSqB, numSamples);
		
		// M8
		DBRecordNumerical.BDG[0][7] = 
				DBRecordNumerical.AUCsReaderAvg[0]*DBRecordNumerical.AUCsReaderAvg[0];
		DBRecordNumerical.BDG[1][7] = 
				DBRecordNumerical.AUCsReaderAvg[1]*DBRecordNumerical.AUCsReaderAvg[1];
		DBRecordNumerical.BDG[2][7] = 
				DBRecordNumerical.AUCsReaderAvg[0]*DBRecordNumerical.AUCsReaderAvg[1];

		// Set the coefficients
		DBRecordNumerical.DBRecordRoeMetzNumericalFill(SizePanelRoeMetz);
		if (SizePanelRoeMetz.pairedReadersFlag == 0) {
			for(int i= 0; i<(DBRecordNumerical.Nreader/2);i++){
				DBRecordNumerical.N0perReader[i][1] = 0;
				DBRecordNumerical.N1perReader[i][1] = 0;
				DBRecordNumerical.N0perReader[i][2] = 0;
				DBRecordNumerical.N1perReader[i][2] = 0;
				DBRecordNumerical.AUCs[i][1] = Double.NaN;	
			}
			for(int i= (int) (DBRecordNumerical.Nreader/2); i<DBRecordNumerical.Nreader;i++){
				DBRecordNumerical.N0perReader[i][0] = 0;
				DBRecordNumerical.N1perReader[i][0] = 0;
				DBRecordNumerical.N0perReader[i][2] = 0;
				DBRecordNumerical.N1perReader[i][2] = 0;
				DBRecordNumerical.AUCs[i][0] = Double.NaN;
			}		
		}
	}

}
