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

import umontreal.iro.lecuyer.probdist.BetaDist;
import umontreal.iro.lecuyer.probdist.FisherFDist;
import umontreal.iro.lecuyer.probdist.NormalDist;

/**
 * Statistical calculations on study data. Hillis tests and Z test are
 * implemented Hillis tests require non-central F distribution. The formula is
 * from http://www.mathworks.com/help/toolbox/stats/brn2ivz-110.html
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 * @version 2.0b
 */
public class StatTest {
	private final int INFINITY = 500;
	private final int PRECISION = 6;
	private final double ZERO = 1E-300;
	private final static int USE_MLE = 1;
	private final static int NO_MLE = 0;
	private double powerWithHillisDF;
	private double powerZ;
	private double DOF = 0;
	private double dfBDG; // degrees of freedom based on Obuchowski/BDG method
	private double tStatEst = 0;
	private double effSize;
	private double sigLevel;
	private double pValF;
	private double ciBot, ciTop;
	private double f0;
	private double df1 = 1.0; // future versions may account for greater df1
	private double dfHillis;
	private double tStatCalc;
	private double pValFBDG;
	private double pValFHillis;
	private double powerWithBDGDF;
	private double ciBotDfBDG, ciTopDfBDG;
	private double ciBotDfHillis, ciTopDfHillis;

	/**
	 * Gets the confidence interval
	 * 
	 * @return Lower and upper range of confidence interval
	 */
	public double[] getCI() {
		return new double[] { ciBot, ciTop };
	}

	/**
	 * Gets confidence interval calculated with degrees of freedom via BDG
	 * method
	 * 
	 * @return Lower and upper range of confidence interval
	 */
	public double[] getCIDFBDG() {
		return new double[] { ciBotDfBDG, ciTopDfBDG };
	}

	/**
	 * Gets confidence interval calculated with degrees of freedom via Hillis
	 * 2008 method
	 * 
	 * @return Lower and upper range of confidence interval
	 */
	public double[] getCIDFHillis() {
		return new double[] { ciBotDfHillis, ciTopDfHillis };
	}

	/**
	 * Gets the power according to Hillis 2011 calculation with degrees of
	 * freedom via BDG method.
	 * 
	 * @return Power calculation
	 */
	public double getHillisPowerWithBDGDF() {
		return powerWithBDGDF;
	}

	/**
	 * Gets the power according to Hillis 2011 calculation with degrees of
	 * freedom via Hillis 2008 method.
	 * 
	 * @return Power calculation
	 */
	public double getHillisPowerWithHillisDF() {
		return powerWithHillisDF;
	}

	/**
	 * Gets the power according to Z test
	 * 
	 * @return Power calculation
	 */
	public double getZPower() {
		return powerZ;
	}

	/**
	 * Gets the degrees of freedom according to Hillis 2008 calculation
	 * 
	 * @return Degrees of freedom
	 */
	public double getDOF() {
		return DOF;
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
		return f0;
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
	public double getDDF() {
		return dfHillis;
	}

	/**
	 * Gets the degrees of freedom (Obuchowski, BDG method)
	 * 
	 * @return Degrees of freedom
	 */
	public double getDfBDG() {
		return dfBDG;
	}

	/**
	 * Constructor used for calculating statistics when performing initial
	 * variance analysis
	 * 
	 * @param curRecord The database record for which to calculate statistics
	 * @param selectedMod Which modality/difference
	 * @param useMLE Whether or not to use biased variance components
	 * @param sig Significance level
	 * @param eff Effect size
	 */
	public StatTest(DBRecord curRecord, int selectedMod, int useMLE,
			double sig, double eff) {
		double mst = 0, denom = 0, statT = 0;
		double[] aucs = { 0, 0 };
		double[][] coeff;
		if (curRecord.getFullyCrossedStatus()) {
			coeff = DBRecord.genBDGCoeff(curRecord.getReader(),
					curRecord.getNormal(), curRecord.getDisease());
		} else {
			coeff = DBRecord.genBDGCoeff(curRecord.getReader(),
					curRecord.getNormal(), curRecord.getDisease(),
					curRecord.getMod0StudyDesign(),
					curRecord.getMod1StudyDesign());
		}
		double[][] BDG = curRecord.getBDG(useMLE);
		double[][] MS = curRecord.getMS(useMLE);
		double dn0 = curRecord.getNormal();
		double dn1 = curRecord.getDisease();
		double dnr = curRecord.getReader();
		double dnc = dn0 + dn1;

		double[] var = DBRecord.getBDGTab(selectedMod, BDG, coeff)[6];
		double var0 = 0;
		for (int j = 0; j < 8; j++) {
			var0 = var0 + var[j];
		}

		dfBDG = calcDFBDG(curRecord.getBCK(USE_MLE), curRecord.getReader(),
				curRecord.getNormal(), curRecord.getDisease(), var0);
		double[] DBMvar = new double[3];
		if (selectedMod == 1 || selectedMod == 0) {
			DBMvar[0] = curRecord.getDBM(NO_MLE)[selectedMod][0];
			DBMvar[1] = curRecord.getDBM(NO_MLE)[selectedMod][1];
			DBMvar[2] = curRecord.getDBM(NO_MLE)[selectedMod][2];
		} else {
			DBMvar[0] = curRecord.getDBM(NO_MLE)[3][3];
			DBMvar[1] = curRecord.getDBM(NO_MLE)[3][4];
			DBMvar[2] = curRecord.getDBM(NO_MLE)[3][5];
		}
		dfHillis = DDF_Hillis(var, curRecord.getReader(), curRecord.getNormal()
				+ curRecord.getDisease(), var0);

		aucs[0] = curRecord.getAUCinNumber(0);
		aucs[1] = curRecord.getAUCinNumber(1);
		double meanCI = 0;

		// TODO So is this DOF z stat or what?
		if (selectedMod == 0 || selectedMod == 1) {
			meanCI = aucs[selectedMod];
			// mst=dnr*dnc*(aucs[selectedMod]-eff)*(aucs[selectedMod]-eff);
			mst = dnr * dnc * eff * eff;
			denom = MS[selectedMod][0]
					+ Math.max(0.0, (MS[selectedMod][1] - MS[selectedMod][4]));
			statT = Math.sqrt(mst / denom);
			DOF = denom * denom
					/ (MS[selectedMod][0] * MS[selectedMod][0] / (dnr - 1));
		} else {
			meanCI = aucs[0] - aucs[1];
			mst = dnr * dnc * doVar(aucs[0], aucs[1]);
			denom = MS[selectedMod][2]
					+ Math.max(0.0, (MS[selectedMod][3] - MS[selectedMod][5]));
			statT = Math.sqrt(mst / denom);
			DOF = denom * denom
					/ (MS[selectedMod][2] * MS[selectedMod][2] / (dnr - 1));
			System.out.println("R=" + dnr + "N0=" + dn0 + "N1=" + dn1 + "eff="
					+ eff + "sig" + sig + "auc0=" + aucs[0] + "auc1=" + aucs[1]
					+ "df=" + DOF);
			System.out.println("denom=" + denom + "mst=" + mst);
		}
		tStatEst = statT;

		// Use normal distribution if DOF > 50, since they are approximately
		// equal at that point, and FisherFDist can't handle large DOFs
		double fVal, fValBDG, fValHillis;
		if (DOF >= 50) {
			pValF = NormalDist.cdf(0, 1, statT * statT);
			NormalDist ndist = new NormalDist();
			fVal = ndist.inverseF(1 - sig);
		} else {
			pValF = FisherFDist.cdf(1, (int) DOF, 5, statT * statT);
			FisherFDist fdist = new FisherFDist(1, (int) DOF);
			fVal = fdist.inverseF(1 - sig);
		}
		pValF = 1 - pValF;

		// calculate p-value with dfbdg
		if (dfBDG >= 50) {
			pValFBDG = NormalDist.cdf(0, 1, statT * statT);
			NormalDist ndist = new NormalDist();
			fValBDG = ndist.inverseF(1 - sig);
		} else {
			pValFBDG = FisherFDist.cdf(1, (int) dfBDG, 5, statT * statT);
			FisherFDist fdist = new FisherFDist(1, (int) dfBDG);
			fValBDG = fdist.inverseF(1 - sig);
		}
		pValFBDG = 1 - pValFBDG;

		// calculate p-value with dfHillis
		if (dfHillis >= 50) {
			pValFHillis = NormalDist.cdf(0, 1, statT * statT);
			NormalDist ndist = new NormalDist();
			fValHillis = ndist.inverseF(1 - sig);
		} else {
			pValFHillis = FisherFDist.cdf(1, (int) dfHillis, 5, statT * statT);
			FisherFDist fdist = new FisherFDist(1, (int) dfHillis);
			fValHillis = fdist.inverseF(1 - sig);
		}
		pValFHillis = 1 - pValFHillis;

		ciBot = meanCI - Math.sqrt(var0) * Math.sqrt(fVal); // bdg
		ciTop = meanCI + Math.sqrt(var0) * Math.sqrt(fVal); // bdg
		ciBotDfBDG = meanCI - Math.sqrt(var0) * Math.sqrt(fValBDG);
		ciTopDfBDG = meanCI + Math.sqrt(var0) * Math.sqrt(fValBDG);
		ciBotDfHillis = meanCI - Math.sqrt(var0) * Math.sqrt(fValHillis);
		ciTopDfHillis = meanCI + Math.sqrt(var0) * Math.sqrt(fValHillis);
		// ***
		// StudentDist tdist = StudentDist((int)ddf);
		// double pValT=tdist.inverse((int)ddf, 1-sig/2); //
		// ciBot=meanCI-Math.sqrt(var[selectedMod])*pValT; //bdg
		// ciTop=meanCI+Math.sqrt(var[selectedMod])*pValT; //bdg
		// ***

		System.out.println("auc(0)=" + meanCI + "Fval=" + Math.sqrt(fVal)
				+ "std" + Math.sqrt(var[selectedMod]));
	}

	/**
	 * Constructor used for calculating statistics when sizing a new trial
	 * 
	 * @param DBMvar Subset of DBM variance components for the selected
	 *            modality/difference
	 * @param r Number of readers
	 * @param n Number of normal cases
	 * @param d Number of disease cases
	 * @param sig Significance level=
	 * @param eff Effect size
	 * @param totalVar Total variance of components
	 * @param BCKbias Biased BCK variance components
	 */
	public StatTest(double[] DBMvar, int r, int n, int d, double sig,
			double eff, double totalVar, double[][] BCKbias, double[] aucs,
			int selectedMod) {
		sigLevel = sig;
		effSize = eff;
		dfHillis = DDF_Hillis(DBMvar, r, n + d, totalVar);
		dfBDG = calcDFBDG(BCKbias, r, n, d, totalVar);
		powerWithHillisDF = FTest_power(DBMvar, r, n + d, totalVar, dfHillis);
		powerWithBDGDF = FTest_power(DBMvar, r, n + d, totalVar, dfBDG);
		powerZ = ZTest(DBMvar, r, n + d, totalVar);

		double fVal, fValBDG, fValHillis;
		NormalDist ndist = new NormalDist();
		fVal = ndist.inverseF(1 - sig);

		if (dfBDG >= 50) {
			fValBDG = ndist.inverseF(1 - sig);
		} else {
			FisherFDist fdist = new FisherFDist(1, (int) dfBDG);
			fValBDG = fdist.inverseF(1 - sig);
		}
		if (dfHillis >= 50) {
			fValHillis = ndist.inverseF(1 - sig);
		} else {
			FisherFDist fdist = new FisherFDist(1, (int) dfHillis);
			fValHillis = fdist.inverseF(1 - sig);
		}

		double meanCI;
		if (selectedMod == 0 || selectedMod == 1) {
			meanCI = aucs[selectedMod];
		} else {
			meanCI = aucs[0] - aucs[1];
		}
		ciBot = meanCI - Math.sqrt(totalVar) * Math.sqrt(fVal); // bdg
		ciTop = meanCI + Math.sqrt(totalVar) * Math.sqrt(fVal); // bdg
		ciBotDfBDG = meanCI - Math.sqrt(totalVar) * Math.sqrt(fValBDG);
		ciTopDfBDG = meanCI + Math.sqrt(totalVar) * Math.sqrt(fValBDG);
		ciBotDfHillis = meanCI - Math.sqrt(totalVar) * Math.sqrt(fValHillis);
		ciTopDfHillis = meanCI + Math.sqrt(totalVar) * Math.sqrt(fValHillis);
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
	 * @param var Subset of DBM variance components for the selected
	 *            modality/difference
	 * @param r Number of readers
	 * @param c Number of cases
	 * @param totalVar Total variance of components
	 * @return Denominator degrees of freedom
	 */
	public double DDF_Hillis(double[] var, int r, int c, double totalVar) {
		double SigTR = var[0];
		double SigTC = var[1];
		double SigTRC = var[2];
		double ddf_hillis;

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
			ddf_hillis = r - 1;
		} else {
			Nu = c * SigTR + r * SigTC + SigTRC;
			Nu = Nu * Nu;
			De1 = c * SigTR + SigTRC;
			De1 = De1 * De1 / (r - 1);
			De2 = r * SigTC + SigTRC;
			De2 = De2 * De2 / (c - 1);
			De3 = SigTRC * SigTRC / (r - 1) / (c - 1);
			ddf_hillis = Nu / (De1 + De2 + De3);
			ddf_hillis = Nu / (De1);
		}
		return ddf_hillis;
	}

	/**
	 * Calculates the statistical power by f-test
	 * 
	 * @param var Subset of DBM variance components for the selected
	 *            modality/difference
	 * @param r Number of readers
	 * @param c Number of cases
	 * @param totalVar Total variance of components
	 * @return Statistical power
	 */
	public double FTest_power(double[] var, int r, int c, double totalVar,
			double df) {
		double SigTR = var[0];
		double SigTC = var[1];
		double SigTRC = var[2];
		double fTestPower;

		if (SigTR < 0) {
			SigTR = 0;
		}
		if (SigTC < 0) {
			SigTC = 0;
		}
		if (SigTRC < 0) {
			SigTRC = 0;
		}

		tStatCalc = c * SigTR + r * SigTC + SigTRC;
		System.out.println("delta=" + tStatCalc);
		tStatCalc = 2.0 * tStatCalc / r / c;
		System.out.println("delta=" + tStatCalc);
		tStatCalc = effSize * effSize / tStatCalc;
		tStatCalc = effSize * effSize / totalVar;
		System.out.println("delta=" + tStatCalc);

		// we use normal distribution for DOF > 50 since it is close to Fisher F
		// and fisherF fails for DOF > 2000
		double cdftemp;
		if (df >= 50) {
			tStatCalc = Math.sqrt(tStatCalc);
			// Inverse normal cumulative d.f.
			// return = NormalDist.inverseF(mean, var, prob);
			// prob = integral(-inf, return) normal_pdf(mean, var)
			f0 = NormalDist.inverseF(0, 1, (1 - sigLevel / 2.0));
			// Normal cumulative d.f.
			// return = NormalDist.cdf(mean, var, x0);
			// return = integral(-inf, x0) normal.pdf(mean, var)
			cdftemp = NormalDist.cdf(tStatCalc, 1, f0);
		} else {
			tStatCalc = Math.sqrt(tStatCalc);
			FisherFDist fdist = new FisherFDist((int) df1, (int) df);
			f0 = fdist.inverseF(1 - sigLevel);
			cdftemp = cdfNonCentralF((int) df1, (int) df, tStatCalc, f0);
			// Rescale Delta, CVF to correspond to t-distribution ~= normal
			// distribution
			f0 = Math.sqrt(f0);
		}

		fTestPower = 1 - cdftemp;
		System.out.println("delta=" + tStatCalc + " df2=" + dfHillis + " CVF="
				+ f0 + " powerF= " + fTestPower + " totalVar= " + totalVar);

		return fTestPower;
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
	 * @param var Subset of DBM variance components for the selected
	 *            modality/difference
	 * @param r Number of readers
	 * @param c Number of cases
	 * @param totalVar Total variance of components
	 * @return Statistical power
	 */
	public double ZTest(double[] var, int r, int c, double totalVar) {
		double sigma = Math.sqrt(totalVar);
		double v = NormalDist.inverseF(0, sigma, 1 - sigLevel / 2.0);
		powerZ = 1 - NormalDist.cdf(effSize, sigma, v);
		System.out.println("powerZ=" + powerZ + " effSize= " + effSize
				+ " CVF= " + v + " totalVar= " + totalVar);
		return powerZ;
	}

	// TODO verify correctness
	/**
	 * Calculates degrees of freedom by Obuchowski, BDG method
	 * 
	 * @param BCKbias Biased components of variance for BCK decomposition
	 * @param r Number of readers
	 * @param n Number of normal cases
	 * @param d Number of disease cases
	 * @param totalVar Total variance of components
	 * @return Degrees of freedom
	 */
	private double calcDFBDG(double[][] BCKbias, int r, int n, int d,
			double totalVar) {
		System.out.println("total var " + totalVar);
		double s2br = BCKbias[0][3] + BCKbias[1][3] - (2 * BCKbias[2][3]);
		double s2b0 = BCKbias[0][0] + BCKbias[1][0] - (2 * BCKbias[2][0]);
		double s2b1 = BCKbias[0][1] + BCKbias[1][1] - (2 * BCKbias[2][1]);
		double df = Math.pow(totalVar, 2)
				/ ((Math.pow(s2br, 2) / Math.pow(r - 1, 3))
						+ (Math.pow(s2b0, 2) / Math.pow(n - 1, 3)) + (Math.pow(
						s2b1, 2) / Math.pow(d - 1, 3)));
		System.out.println("calcdfbdg totalVar = " + totalVar);
		return df;
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
