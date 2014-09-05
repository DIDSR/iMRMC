/**
 * StatTest.java
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

import mrmc.gui.GUInterface;
import mrmc.gui.SizePanel;
import umontreal.iro.lecuyer.probdist.BetaDist;
import umontreal.iro.lecuyer.probdist.FisherFDist;
import umontreal.iro.lecuyer.probdist.StudentDist;
import umontreal.iro.lecuyer.probdist.NormalDist;

/**
 * Statistical calculations on study data. Hillis tests and Z test are
 * implemented Hillis tests require non-central F distribution. The formula is
 * from http://www.mathworks.com/help/toolbox/stats/brn2ivz-110.html
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
public class StatTest {
	
	GUInterface GUI;
	DBRecord DBRecordStat, DBRecordSize;

	private final int INFINITY = 500;
	private final int PRECISION = 6;
	private final double ZERO = 1E-300;
//	private final static int USE_MLE = 1;
	private final static int NO_MLE = 0;

	public double tStatEst = 0;
	public double effSize;
	public double sigLevel;
	public double tStatCalc;

	public double DF_BDG, DF_Hillis;
	public double pValF, pValFBDG, pValFHillis;
	public double cutoffNormal, cutoffBDG, cutoffHillis;
	public double lambdaBDG, lambdaHillis;
	public double powerNormal, powerBDG, powerHillis;
	public double ciBotNormal, ciBotBDG, ciBotHillis; 
	public double ciTopNormal, ciTopBDG, ciTopHillis;

	/**
	 * Gets the confidence interval
	 * 
	 * @return Lower and upper range of confidence interval
	 */
	public double[] getCI() {
		return new double[] { ciBotNormal, ciTopNormal };
	}

	/**
	 * Gets confidence interval calculated with degrees of freedom via BDG
	 * method
	 * 
	 * @return Lower and upper range of confidence interval
	 */
	public double[] getCIDF_BDG() {
		return new double[] { ciBotBDG, ciTopBDG };
	}

	/**
	 * Gets confidence interval calculated with degrees of freedom via Hillis
	 * 2008 method
	 * 
	 * @return Lower and upper range of confidence interval
	 */
	public double[] getCIDF_Hillis() {
		return new double[] { ciBotHillis, ciTopHillis };
	}

	/**
	 * Gets the power according to Hillis 2011 calculation with degrees of
	 * freedom via BDG method.
	 * 
	 * @return Power calculation
	 */
	public double getHillisPowerWithBDGDF() {
		return powerBDG;
	}

	/**
	 * Gets the power according to Hillis 2011 calculation with degrees of
	 * freedom via Hillis 2008 method.
	 * 
	 * @return Power calculation
	 */
	public double getHillisPowerWithHillisDF() {
		return powerHillis;
	}

	/**
	 * Gets the power according to Z test
	 * 
	 * @return Power calculation
	 */
	public double getZPower() {
		return powerNormal;
	}

	/**
	 * Gets the estimated t-statistic
	 * 
	 * @return Estimated t-statistic
	 */
	public double getTStatEst() {
		return tStatEst;
	}

	/**
	 * Gets the p-value
	 * 
	 * @return P-value
	 */
	public double getpValF() {
		return pValF;
	}

	/**
	 * Gets the p-value using degrees of freedom via BDG calculation
	 * 
	 * @return P-value
	 */
	public double getpValFBDG() {
		return pValFBDG;
	}

	/**
	 * Gets the p-value using degrees of freedom via Hillis calculation
	 * 
	 * @return P-value
	 */
	public double getpValFHillis() {
		return pValFHillis;
	}

	/**
	 * Gets the CVF
	 * 
	 * @return CVF
	 */
	public double getCVF() {
		return cutoffNormal;
	}

	/**
	 * Gets the calculated t-statistic
	 * 
	 * @return Calculated t-statistic
	 */
	public double getTStatCalc() {
		return tStatCalc;
	}

	/**
	 * Gets the denominator degrees of freedom (Hillis 2008 method)
	 * 
	 * @return Degrees of freedom
	 */
	public double getDF_Hillis() {
		return DF_Hillis;
	}

	/**
	 * Gets the degrees of freedom (Obuchowski, BDG method)
	 * 
	 * @return Degrees of freedom
	 */
	public double getDF_BDG() {
		return DF_BDG;
	}

	/**
	 * Constructor used for calculating statistics when performing initial variance analysis
	 * @param DBRecordStatTemp is the record to have a statistical analysis
	 * @param selectedMod is 0 for modality A, 1 for modality B, and 3 for the difference in modalities
	 */
	public StatTest(DBRecord DBRecordStatTemp, int selectedMod) {

		DBRecordStat = DBRecordStatTemp;
		double sig = 0.05;
		
		double totalVar = DBRecordStat.totalVar;
		double[] aucs = { DBRecordStat.getAUCinNumber(0), DBRecordStat.getAUCinNumber(1)};

		double meanCI, tStatEst, cutoffZ, cutoffBDG, cutoffHillis;

		System.out.println("NR=" + DBRecordStat.getReader() + 
				           ",  N0=" + DBRecordStat.getNormal() + 
				           ",  N1=" + DBRecordStat.getDisease());
		System.out.println("auc0=" + aucs[0] + "  auc1=" + aucs[1]);
		System.out.println("totalVar=" + DBRecordStat.totalVar);

		if (selectedMod == 1 || selectedMod == 0) {
			meanCI = aucs[selectedMod];
			/* Compare single-modality AUC to 0.5 */
			tStatEst = Math.sqrt(Math.pow(meanCI - 0.5, 2)/DBRecordStat.totalVar);
		} else {
			meanCI = aucs[1] - aucs[0];
			/* Compare difference in modality AUCs to 0.0 */
			tStatEst = Math.sqrt(Math.pow(meanCI, 2)/DBRecordStat.totalVar);
		}	

		DF_BDG = calcDF_BDG(DBRecordStat, selectedMod);
		DF_Hillis = calcDF_Hillis(DBRecordStat, selectedMod);

		System.out.println("DF_BDG=" + DF_BDG);
		System.out.println("DF_Hillis=" + DF_Hillis);

		// calculate p-value and cutoff assuming normality
		NormalDist ndist = new NormalDist();
		pValF = 2*(1 - ndist.cdf(tStatEst));
		cutoffZ = ndist.inverseF(1 - sig/2.0);

		// calculate p-value and cutoff assuming t-distribution with DF_BDG or DF_Hillis
		// Use normal distribution if df > 50, since they are approximately
		// equal at that point, and StudentDist can't handle large dfs
		if (DF_BDG >= 50) {
			pValFBDG = pValF;
			cutoffBDG = cutoffZ;
		} else {
			StudentDist tdist = new StudentDist((int) DF_BDG );
			pValFBDG = 2*(1 - tdist.cdf( tStatEst ));
			cutoffBDG = tdist.inverseF( 1-sig/2 );
		}

		// calculate p-value and cutoff assuming t-distribution with DF_Hillis
		// Use normal distribution if df > 50, since they are approximately
		// equal at that point, and FisherFDist can't handle large dfs
		if (DF_Hillis >= 50) {
			pValFHillis = pValF;
			cutoffHillis = cutoffZ;
		} else {
			StudentDist tdist = new StudentDist((int) DF_Hillis );
			pValFHillis = 2*(1 - tdist.cdf( tStatEst ));
			cutoffHillis = tdist.inverseF( 1-sig/2 );
		}
		
		ciBotNormal = meanCI - Math.sqrt(totalVar) * cutoffZ; // normal approx
		ciTopNormal = meanCI + Math.sqrt(totalVar) * cutoffZ; // normal approx
		ciBotBDG = meanCI - Math.sqrt(totalVar) * cutoffBDG;
		ciTopBDG = meanCI + Math.sqrt(totalVar) * cutoffBDG;
		ciBotHillis = meanCI - Math.sqrt(totalVar) * cutoffHillis;
		ciTopHillis = meanCI + Math.sqrt(totalVar) * cutoffHillis;

		System.out.println("mean=" + meanCI);
		System.out.println("tStatEst=" + tStatEst);
		System.out.println("Normal approx:" + "  pValF=" + pValF + "  cutoffZ=" + cutoffZ + "  ci=" + ciBotNormal + ","  + ciTopNormal);
		System.out.println("DF_BDG=" + DF_BDG 
				+ "  pValBDG=" + pValFBDG + "  cutoffBDG=" + cutoffBDG 
				+ "  ci=" + ciBotBDG + ","  + ciTopBDG);
		System.out.println("DF_Hillis=" + DF_Hillis 
				+ "  pValHillis=" + pValFHillis + "  cutoffHillis=" + cutoffHillis 
				+ "  ci=" + ciBotHillis + ","  + ciTopHillis);
	}

	/**
	 * Constructor used for calculating statistics when sizing a new trial
	 * 
	 * @param GUI The GUI that launched this statistical analysis
	 * @param SizePanel is the bottom panel of the GUI
	 */
	public StatTest(GUInterface GUI, SizePanel SizePanel) {

		DBRecordStat = GUI.DBRecordStat;
		DBRecordSize = GUI.DBRecordSize;
		int selectedMod = GUI.selectedMod;
		
		effSize = SizePanel.effSize;
		sigLevel = SizePanel.sigLevel;
		tStatCalc = effSize / Math.sqrt(DBRecordSize.totalVar);
		
		System.out.println("\nBegin Sizing Analysis");
		System.out.println("effSize= " + effSize 
				+ " totalVar= " + DBRecordSize.totalVar 
				+ " tStatCalc=" + tStatCalc);

		DF_BDG = calcDF_BDG(DBRecordSize, selectedMod);
		lambdaBDG = tStatCalc*tStatCalc;
		
		/*
		 * Here we calculate the parameters Hillis 2011 Eq. 10
		 */
		double[][] OR = DBRecordStat.OR;
		double dnr = DBRecordSize.Nreader;
		double resizeFactor = ((double) (DBRecordStat.Nnormal+DBRecordStat.Ndisease))
				/ ((double) (DBRecordSize.Nnormal+DBRecordSize.Ndisease));
		double ms_r, var_r, var_tr, bracket1, bracket2, variance;
		
		if(selectedMod == 3) {
			var_tr = DBRecordStat.ms_tr - OR[3][5] + OR[3][2]
					+  Math.max(OR[3][3]-OR[3][4],0);
			bracket1 = OR[3][5] - OR[3][2] 
					+ (dnr-1.0) * Math.max((OR[3][3]-OR[3][4]),0);
			variance = 2.0/dnr * (var_tr + resizeFactor*bracket1);
			lambdaHillis = effSize*effSize/variance;
			bracket2 = OR[3][5] - OR[3][2] - Math.max((OR[3][3]-OR[3][4]),0); // different from bracket above 
			DF_Hillis = (dnr-1.0)*dnr*dnr*variance*variance/2.0/2.0
					/ (var_tr + resizeFactor * bracket2) / (var_tr + resizeFactor * bracket2);
		}
		else {
			ms_r = 0.0;
			if(selectedMod == 0) ms_r = DBRecordStat.ms_rA; 
			if(selectedMod == 1) ms_r = DBRecordStat.ms_rB; 
			var_r = ms_r - OR[selectedMod][5] + OR[selectedMod][3]; 
			bracket1 = OR[selectedMod][5] + (dnr-1)*OR[selectedMod][3];
			variance = 1.0/dnr * (var_r + resizeFactor*bracket1);
			lambdaHillis = effSize*effSize/variance;
			bracket2 = OR[selectedMod][5] - OR[selectedMod][3]; // different from bracket above
			DF_Hillis = (dnr-1.0)*dnr*dnr*variance*variance
					/ (var_r + resizeFactor * bracket2) / (var_r + resizeFactor * bracket2);
		}

		double[] result = new double[4];
		result = PowerZtest();
		cutoffNormal = result[0];
		powerNormal = result[1];
		System.out.println("Normal power analysis:");
		System.out.println("cutoff=" + result[0]
				+ ", power=" + result[1]);

		result = PowerFtest(DF_BDG, lambdaBDG);
		cutoffBDG = result[0];
		powerBDG = result[1];
		System.out.println("BDG power analysis (t):");
		System.out.println("DF_BDG=" + DF_BDG
				+ ", cutoff=" + result[0]
				+ ", noncentrality=" + lambdaBDG
				+ ", power=" + result[1]);
		
		result = PowerFtest(DF_Hillis, lambdaHillis);
		cutoffHillis = result[0];
		powerHillis = result[1];
		System.out.println("Hillis power analysis (t):");
		System.out.println("DF_Hillis=" + DF_Hillis
				+ ", cutoff=" + result[0]
				+ ", noncentrality=" + lambdaHillis
				+ ", power=" + result[1]);

	}

	/**
	 * Finds the variance of two doubles
	 * 
	 * @param a First number
	 * @param b Second number
	 * @return Variance of the two numbers
	 */
	public double doVar(double a, double b) {
		double mean = (a + b) / 2.0;
		double var = (a - mean) * (a - mean) + (b - mean) * (b - mean);
		return var;
	}

	/**
	 * Calculates the denominator degrees of freedom by Hillis 2008 method
	 * 
	 */
	public double calcDF_Hillis(double[] varDBM, long l, long m, double totalVar) {
		
		double ddf_hillis;

		double SigTR = varDBM[0];
		double SigTC = varDBM[1];
		double SigTRC = varDBM[2];

		if (SigTR < 0) {
			SigTR = 0;
		}
		if (SigTC < 0) {
			SigTC = 0;
		}
		if (SigTRC < 0) {
			SigTRC = 0;
		}

		// F statistics Hillis 2004
		double Nu, De1, De2, De3;
		if (SigTC == 0) {
			ddf_hillis = l - 1;
		} else {
			Nu = m * SigTR + l * SigTC + SigTRC;
			Nu = Nu * Nu;
			De1 = m * SigTR + SigTRC;
			De1 = De1 * De1 / (l - 1);
			De2 = l * SigTC + SigTRC;
			De2 = De2 * De2 / (m - 1);
			De3 = SigTRC * SigTRC / (l - 1) / (m - 1);
			ddf_hillis = Nu / (De1 + De2 + De3);
			ddf_hillis = Nu / (De1);
		}
		
		return ddf_hillis;
	}
	/**
	 * Calculates the denominator degrees of freedom by Hillis 2008 method given the curRecord
	 * 
	 */
	public double calcDF_Hillis(DBRecord curRecord, int selectedMod) {
		
		double[] AUCsReaderAvg = curRecord.getAUCsReaderAvg();
		double dnm = 2.0;
		double dnr = curRecord.getReader();
		double denom;

		double[][] OR = curRecord.getOR(NO_MLE);

		if(selectedMod == 3) {
			// Hillis2014, pg 332
			denom = curRecord.ms_tr + Math.max(dnr*(OR[3][3]-OR[3][4]),0);

			tStatEst = Math.sqrt(curRecord.ms_t/denom);
			// These expressions are equivalent
			tStatEst = (AUCsReaderAvg[0] - AUCsReaderAvg[1])/Math.sqrt(curRecord.totalVar);

			DF_Hillis = (dnr-1.0)*(dnm-1.0)*denom*denom/curRecord.ms_tr/curRecord.ms_tr;

			// Here is another equivalent expression for the total variance
			// The ones are from the contrast between modalities 1*AUC1 + (-1)*AUC2
			// curRecord.totalVar = (1*1 + (-1)*(-1))*denom/dnr;

		} else {
			// Hillis2014, pg 333
			
			double ms_r=0.0;
			if(selectedMod == 0) ms_r = curRecord.ms_rA;
			if(selectedMod == 1) ms_r = curRecord.ms_rB;
					
			denom = ms_r + Math.max(dnr*OR[selectedMod][3],0);
			tStatEst = (AUCsReaderAvg[selectedMod]-0.5)/Math.sqrt(curRecord.totalVar);
			
			DF_Hillis = (dnr-1.0)*denom*denom/ms_r/ms_r;
			
			// Here is another equivalent expression for the total variance
			// curRecord.totalVar = denom/dnr;

		}

		return DF_Hillis;
	}

	/**
	 * Calculates the statistical power by non-central f-test
	 * 
	 * @param df Degrees of freedom
	 * @param lambda Non-centrality parameter
	 * @return Statistical power assuming a non-central F-distribution
	 */
	public double[] PowerFtest(double df, double lambda) {

		double[] result = new double[4];
		double cutoff, power;

		// we use normal distribution for df > 50 since it is close to Fisher F
		// and fisherF fails for DOF > 2000
		if (df >= 50) {
			cutoff = NormalDist.inverseF(0, 1, 1 - sigLevel / 2.0);
			power = 1 - NormalDist.cdf(Math.sqrt(lambda), 1, cutoff);
			result = PowerZtest();
			
		} else {
			FisherFDist fdist = new FisherFDist(1, (int) df);
			cutoff = fdist.inverseF(1 - sigLevel);
			power = 1 - cdfNonCentralF(1, (int) df-1, lambda, cutoff);
		}

		result[0] = cutoff;
		result[1] = power;
		return result;
	}

	/**
	 * Calculates the cumulative distribution function for a beta distribution
	 * 
	 * @param df1 First degrees of freedom parameter
	 * @param df2 Second degrees of freedom parameter
	 * @param delta f-statistic
	 * @param x f0/CVF
	 * @return CDF value
	 */
	public double cdfNonCentralF(int df1, int df2, double delta, double x) {
		double cdf = 0;
		for (int j = 0; j < INFINITY; j++) {
			double tempF = BetaDist.cdf(df1 / 2.0 + j, df2 / 2.0, PRECISION,
					df1 * x / (df2 + df1 * x));
			double sfactor = 1;
			for (int k = j; k > 0; k--) {
				sfactor = sfactor * (0.5 * delta) / (double) k;
			}
			tempF = sfactor * Math.exp(-delta / 2.0) * tempF;
			cdf = cdf + tempF;
			if (tempF > -ZERO && tempF < ZERO) {
				break;
			}
		}
		return cdf;
	}

	/**
	 * Calculates statistical power via z-test
	 * 
	 * @return Statistical power assuming normality
	 */
	public double[] PowerZtest() {

		double cutoff = NormalDist.inverseF(0, 1, 1 - sigLevel / 2.0);
		double power = 1 - NormalDist.cdf(tStatCalc, 1, cutoff);
		double ciBot = effSize - NormalDist.inverseF(0, 1.0, sigLevel/2.0);
		double ciTop = effSize + NormalDist.inverseF(0, 1.0, sigLevel/2.0);
		
		double[] result = new double[4];
		result[0] = cutoff;
		result[1] = power;
		result[2] = ciBot;
		result[3] = ciTop;
		
		return result;
	}

	// TODO verify correctness
	/**
	 * Calculates degrees of freedom by BDG <br>
	 * ----Obuchowski2012_Acad-Radiol_v19p1508
	 * 
	 * @param BCK Components of variance for BCK decomposition
	 * @param Nreader Number of readers
	 * @param Nnormal Number of normal cases
	 * @param Ndisease Number of disease cases
	 * @param totalVar Total variance of components
	 * @return Degrees of freedom
	 */
	public double calcDF_BDG(double[][] BCK, long Nreader, long Nnormal, long Ndisease,
			double totalVar) {
		
		System.out.println("total var " + totalVar);
		double s2b0 = 0.0;
		s2b0 = s2b0 + Nreader*Ndisease*(BCK[0][0] + BCK[1][0] - 2*BCK[2][0]);
		s2b0 = s2b0 +         Ndisease*(BCK[0][4] + BCK[1][4] - 2*BCK[2][4]);
		s2b0 = s2b0 + Nreader*         (BCK[0][2] + BCK[1][2] - 2*BCK[2][2]);
		s2b0 = s2b0 +                  (BCK[0][6] + BCK[1][6] - 2*BCK[2][6]);
		s2b0 = s2b0  /Nreader/Nnormal/Ndisease;
		
		double s2b1 = 0.0;
		s2b1 = s2b1 + Nreader*Nnormal*(BCK[0][1] + BCK[1][1] - (2 * BCK[2][1]));
		s2b1 = s2b1 +         Nnormal*(BCK[0][5] + BCK[1][5] - (2 * BCK[2][5]));
		s2b1 = s2b1 + Nreader*        (BCK[0][2] + BCK[1][2] - (2 * BCK[2][2]));
		s2b1 = s2b1 +                 (BCK[0][6] + BCK[1][6] - (2 * BCK[2][6]));
		s2b1 = s2b1  /Nreader/Nnormal/Ndisease;

		double s2br = 0.0;
		s2br = s2br + Nnormal*Ndisease*(BCK[0][3] + BCK[1][3] - (2 * BCK[2][3]));
		s2br = s2br +         Ndisease*(BCK[0][4] + BCK[1][4] - (2 * BCK[2][4]));
		s2br = s2br + Nnormal*         (BCK[0][5] + BCK[1][5] - (2 * BCK[2][5]));
		s2br = s2br +                  (BCK[0][6] + BCK[1][6] - (2 * BCK[2][6]));
		s2br = s2br  /Nreader/Nnormal/Ndisease;

//		double balanceR = (Math.pow(s2br, 2) / Math.pow(r - 1, 3));
//		double balance0 = (Math.pow(s2b0, 2) / Math.pow(Ndisease - 1, 3));
//		double balance1 = (Math.pow(s2b1, 2) / Math.pow(d - 1, 3));

		double balanceR = (Math.pow(s2br, 2) / (Nreader - 1));
		double balance0 = (Math.pow(s2b0, 2) / (Nnormal - 1));
		double balance1 = (Math.pow(s2b1, 2) / (Ndisease - 1));
		double df = Math.pow(totalVar, 2) / (balanceR + balance0 + balance1);

		return df;
	}
	
	// TODO verify correctness
	/**
	 * Calculates degrees of freedom by BDG <br>
	 * ----Obuchowski2012_Acad-Radiol_v19p1508
	 * 
	 * @param selectedMod 0 indicates modality 1, 1 indicates modality 2, 3 indicates both
	 * @return Degrees of freedom
	 */
	public double calcDF_BDG(DBRecord curRecord, int selectedMod) {
		
		double[][] BCK = curRecord.getBCK(NO_MLE); 
		long Nreader=curRecord.getReader(), Nnormal=curRecord.getNormal(), Ndisease=curRecord.getDisease();
		
		double s2b0 = 0.0, s2b1 = 0.0, s2br = 0.0;

		if(selectedMod == 3) {
			s2b0 = s2b0 + Nreader*Ndisease*(BCK[0][0] + BCK[1][0] - 2*BCK[2][0]);
			s2b0 = s2b0 +         Ndisease*(BCK[0][4] + BCK[1][4] - 2*BCK[2][4]);
			s2b0 = s2b0 + Nreader*         (BCK[0][2] + BCK[1][2] - 2*BCK[2][2]);
			s2b0 = s2b0 +                  (BCK[0][6] + BCK[1][6] - 2*BCK[2][6]);
			s2b0 = s2b0  /Nreader/Nnormal/Ndisease;
			
			s2b1 = s2b1 + Nreader*Nnormal*(BCK[0][1] + BCK[1][1] - (2 * BCK[2][1]));
			s2b1 = s2b1 +         Nnormal*(BCK[0][5] + BCK[1][5] - (2 * BCK[2][5]));
			s2b1 = s2b1 + Nreader*        (BCK[0][2] + BCK[1][2] - (2 * BCK[2][2]));
			s2b1 = s2b1 +                 (BCK[0][6] + BCK[1][6] - (2 * BCK[2][6]));
			s2b1 = s2b1  /Nreader/Nnormal/Ndisease;
	
			s2br = s2br + Nnormal*Ndisease*(BCK[0][3] + BCK[1][3] - (2 * BCK[2][3]));
			s2br = s2br +         Ndisease*(BCK[0][4] + BCK[1][4] - (2 * BCK[2][4]));
			s2br = s2br + Nnormal*         (BCK[0][5] + BCK[1][5] - (2 * BCK[2][5]));
			s2br = s2br +                  (BCK[0][6] + BCK[1][6] - (2 * BCK[2][6]));
			s2br = s2br  /Nreader/Nnormal/Ndisease;
	
		}
		else {
			s2b0 = s2b0 + Nreader*Ndisease*(BCK[selectedMod][0]);
			s2b0 = s2b0 +         Ndisease*(BCK[selectedMod][4]);
			s2b0 = s2b0 + Nreader*         (BCK[selectedMod][2]);
			s2b0 = s2b0 +                  (BCK[selectedMod][6]);
			s2b0 = s2b0  /Nreader/Nnormal/Ndisease;
			
			s2b1 = s2b1 + Nreader*Nnormal*(BCK[selectedMod][1]);
			s2b1 = s2b1 +         Nnormal*(BCK[selectedMod][5]);
			s2b1 = s2b1 + Nreader*        (BCK[selectedMod][2]);
			s2b1 = s2b1 +                 (BCK[selectedMod][6]);
			s2b1 = s2b1  /Nreader/Nnormal/Ndisease;
	
			s2br = s2br + Nnormal*Ndisease*(BCK[selectedMod][3]);
			s2br = s2br +         Ndisease*(BCK[selectedMod][4]);
			s2br = s2br + Nnormal*         (BCK[selectedMod][5]);
			s2br = s2br +                  (BCK[selectedMod][6]);
			s2br = s2br  /Nreader/Nnormal/Ndisease;
	
		}
		
		double balanceR = (Math.pow(s2br, 2) / (Nreader - 1));
		double balance0 = (Math.pow(s2b0, 2) / (Nnormal - 1));
		double balance1 = (Math.pow(s2b1, 2) / (Ndisease - 1));
		DF_BDG = Math.pow(curRecord.totalVar, 2) / (balanceR + balance0 + balance1);

		return DF_BDG;
	}
	/*
	 * public double FTest2011(double[] OR,int r, int c, int rnew, int
	 * cnew,double sig, double eff ) { double varTR =0 ; double H23=0;
	 * if(OR[3]-OR[4] > 0) H23=OR[3]-OR[4]; varTR=OR[2]+OR[1]-OR[5]+H23; double
	 * ratio = (double)c/(double)cnew;
	 * 
	 * double delDen =varTR+ratio*(OR[5]-OR[2]+(r-1.0)*H23); double
	 * Delta=r/2.0*effSize*effSize/delDen; // System.out.println(varTR);
	 * 
	 * double num=delDen*delDen; double den=varTR+ratio*(OR[5]-OR[2]-H23);
	 * double df2=num/(den*den/(r-1)); // System.out.println("df2="+df2+"c="+c);
	 * FisherFDist fdist = new FisherFDist(1,(int)df2); double
	 * Fval=fdist.inverseF(1-sigLevel); double
	 * cdftemp=cdfNonCentralF(1,(int)df2, Delta, Fval); powerF2011=1-cdftemp;
	 * DOF2011=df2; return powerF2011;
	 * 
	 * }
	 */
}
