/*
 * statTest.java
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
 * statistical tests. 
 * Hillis tests and Z test are implemented
 * Hillis tests require non-central F distribution. The formula is from
 * http://www.mathworks.com/help/toolbox/stats/brn2ivz-110.html 
 */

package mrmc.core;

import umontreal.iro.lecuyer.probdist.BetaDist;
import umontreal.iro.lecuyer.probdist.FisherFDist;
import umontreal.iro.lecuyer.probdist.NormalDist;

public class statTest {
	final int INFINITY = 500;
	final int PRECISION = 6;
	final double ZERO = 1E-300;
	private double powerF;
	private double powerZ;
	private double DOF = 0;
	private double dfBDG;
	private double tStat = 0;
	private double effSize;
	private double sigLevel;
	private double pValZ;
	private double pValF;
	private double ciBot, ciTop;
	private double f0;
	private double df2;
	private double fStat;

	public double getciBot() {
		return ciBot;
	}

	public double getciTop() {
		return ciTop;
	}

	public double getHillisPower() {
		return powerF;
	}

	public double getZPower() {
		return powerZ;
	}

	public double getDOF() {
		return DOF;
	}

	public double gettStat() {
		return tStat;
	}

	public double getpValZ() {
		return pValZ;
	}

	public double getpValF() {
		return pValF;
	}

	public double getCVF() {
		return f0;
	}

	public double getDelta() {
		return fStat;
	}

	public double getDDF() {
		return df2;
	}

	public double getDfBDG() {
		return dfBDG;
	}

	/*
	 * Constructor used for stats in sizing panel
	 * 
	 * @param DBMvar 3 elements, components for a given modality in DBM
	 * representation or total if difference in modalities
	 * 
	 * @param totalVar Summed total variance
	 */
	public statTest(double[] DBMvar, int r, int n, int d, double sig,
			double eff, double totalVar, double[][] BCKbias) {
		sigLevel = sig;
		effSize = eff;
		df2 = DDF_Hillis(DBMvar, r, n + d, totalVar);
		double df1 = 1.0; // where should we get this parameter?
		powerF = FTest_power(DBMvar, r, n + d, df1, totalVar);
		powerZ = ZTest(DBMvar, r, n + d, totalVar);
		dfBDG = calcDFBDG(BCKbias, r, n, d, totalVar);
	}

	/* Constructor used for stats in variance analysis panel */
	public statTest(dbRecord curRecord, int selectedMod, int useBiasM,
			double sig, double eff) {
		double mst = 0, denom = 0, statT = 0, ddf = 0;
		double[] aucs = { 0, 0 };
		double[][] MS = curRecord.getMS(useBiasM);
		double[][] coeff = new double[4][8];
		if (curRecord.getFullyCrossedStatus()) {
			coeff = dbRecord.genBDGCoeff(curRecord.getReader(),
					curRecord.getNormal(), curRecord.getDisease());
		} else {
			coeff = dbRecord.genBDGCoeff(curRecord.getReader(),
					curRecord.getNormal(), curRecord.getDisease(),
					curRecord.getMod0StudyDesign(),
					curRecord.getMod1StudyDesign());
		}

		double[][] BDG = curRecord.getBDG(useBiasM);
		double dn0 = curRecord.getNormal();
		double dn1 = curRecord.getDisease();
		double dnr = curRecord.getReader();
		double dnc = dn0 + dn1;

		double[] var = dbRecord.getBDGTab(selectedMod, BDG, coeff)[6];
		double var0 = 0;
		for (int j = 0; j < 8; j++) {
			var0 = var0 + var[j];
		}

		aucs[0] = curRecord.getAUCinNumber(0);
		aucs[1] = curRecord.getAUCinNumber(1);
		double meanCI = 0;
		// TODO these calculations are meaningless when non-fully crossed
		// because MS is not correct
		if (selectedMod == 0 || selectedMod == 1) {
			meanCI = aucs[selectedMod];
			// mst=dnr*dnc*(aucs[selectedMod]-eff)*(aucs[selectedMod]-eff);
			mst = dnr * dnc * eff * eff;
			denom = MS[selectedMod][0]
					+ Math.max(0.0, (MS[selectedMod][1] - MS[selectedMod][4]));
			statT = Math.sqrt(mst / denom);
			ddf = denom * denom
					/ (MS[selectedMod][0] * MS[selectedMod][0] / (dnr - 1));
		} else {
			meanCI = aucs[0] - aucs[1];
			mst = dnr * dnc * doVar(aucs[0], aucs[1]);
			denom = MS[selectedMod][2]
					+ Math.max(0.0, (MS[selectedMod][3] - MS[selectedMod][5]));
			statT = Math.sqrt(mst / denom);
			ddf = denom * denom
					/ (MS[selectedMod][2] * MS[selectedMod][2] / (dnr - 1));
			System.out.println("R=" + dnr + "N0=" + dn0 + "N1=" + dn1 + "eff="
					+ eff + "sig" + sig + "auc0=" + aucs[0] + "auc1=" + aucs[1]
					+ "df=" + ddf);
			System.out.println("denom=" + denom + "mst=" + mst);
		}
		tStat = statT;
		DOF = ddf;
		// Use normal distribution if DOF > 50, since they are approximately
		// equal at that point, and FisherFDist can't handle large DOFs
		double Fval = 0;
		if (ddf >= 50) {
			NormalDist ndist = new NormalDist();
			pValF = NormalDist.cdf(0, 1, statT * statT);
			Fval = ndist.inverseF(1 - sig);
		} else {
			FisherFDist fdist = new FisherFDist(1, (int) ddf);
			pValF = FisherFDist.cdf(1, (int) ddf, 5, statT * statT);
			Fval = fdist.inverseF(1 - sig);
		}
		pValF = 1 - pValF;

		ciBot = meanCI - Math.sqrt(var0) * Math.sqrt(Fval); // bdg
		ciTop = meanCI + Math.sqrt(var0) * Math.sqrt(Fval); // bdg
		// ***
		// StudentDist tdist = StudentDist((int)ddf);
		// double pValT=tdist.inverse((int)ddf, 1-sig/2); //
		// ciBot=meanCI-Math.sqrt(var[selectedMod])*pValT; //bdg
		// ciTop=meanCI+Math.sqrt(var[selectedMod])*pValT; //bdg
		// ***

		System.out.println("auc(0)=" + meanCI + "Fval=" + Math.sqrt(Fval)
				+ "std" + Math.sqrt(var[selectedMod]));
		// System.out.println("selectedMod="+selectedMod+" mst="+mst+" denom="+denom+" tstat="+statT+" df="+ddf+"Fval="+Fval);

	}

	public double doVar(double a, double b) {
		double mean = (a + b) / 2.0;
		double var = (a - mean) * (a - mean) + (b - mean) * (b - mean);
		return var;
	}

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

	public double FTest_power(double[] var, int r, int c, double df1,
			double totalVar) {
		double SigTR = var[0];
		double SigTC = var[1];
		double SigTRC = var[2];
		double ftestpower;

		if (SigTR < 0) {
			SigTR = 0;
		}
		if (SigTC < 0) {
			SigTC = 0;
		}
		if (SigTRC < 0) {
			SigTRC = 0;
		}

		fStat = c * SigTR + r * SigTC + SigTRC;
		System.out.println("delta=" + fStat);
		fStat = 2.0 * fStat / r / c;
		System.out.println("delta=" + fStat);
		// double denom=Delta;
		fStat = effSize * effSize / fStat;

		fStat = effSize * effSize / totalVar;
		System.out.println("delta=" + fStat);

		// use normal distribution for DOF > 50 since it is close to Fisher F
		// and fisherF fails for DOF > 2000
		double cdftemp;
		if (df2 >= 50) {
			fStat = Math.sqrt(fStat);
			// Inverse normal cumulative d.f.
			// return = NormalDist.inverseF(mean, var, prob);
			// prob = integral(-inf, return) normal_pdf(mean, var)
			f0 = NormalDist.inverseF(0, 1, (1 - sigLevel / 2.0));
			// Normal cumulative d.f.
			// return = NormalDist.cdf(mean, var, x0);
			// return = integral(-inf, x0) normal.pdf(mean, var)
			cdftemp = NormalDist.cdf(fStat, 1, f0);
		} else {
			FisherFDist fdist = new FisherFDist((int) df1, (int) df2);
			f0 = fdist.inverseF(1 - sigLevel);
			cdftemp = cdfNonCentralF((int) df1, (int) df2, fStat, f0);
			// Rescale Delta, CVF to correspond to t-distribution ~= normal
			// distribution
			fStat = Math.sqrt(fStat);
			f0 = Math.sqrt(f0);
		}

		ftestpower = 1 - cdftemp;
		System.out.println("delta=" + fStat + " df2=" + df2 + " CVF=" + f0
				+ " powerF= " + ftestpower + " totalVar= " + totalVar);

		return ftestpower;
	}

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

	public double ZTest(double[] var, int r, int c, double totalVar) {
		double sigma = Math.sqrt(totalVar);
		double v = NormalDist.inverseF(0, sigma, 1 - sigLevel / 2.0);
		powerZ = 1 - NormalDist.cdf(effSize, sigma, v);
		System.out.println("powerZ=" + powerZ + " effSize= " + effSize
				+ " CVF= " + v + " totalVar= " + totalVar);
		// pValZ = 1-centralN.cdf(0,sigma,v);

		return powerZ;
	}

	// TODO verify correctness
	private double calcDFBDG(double[][] BCKbias, int r, int n, int d,
			double totalVar) {
		double s2br = BCKbias[0][3] + BCKbias[1][3] - (2 * BCKbias[2][3]);
		double s2b0 = BCKbias[0][0] + BCKbias[1][0] - (2 * BCKbias[2][0]);
		double s2b1 = BCKbias[0][1] + BCKbias[1][1] - (2 * BCKbias[2][1]);
		double df = Math.pow(totalVar, 2)
				/ ((Math.pow(s2br, 2) / Math.pow(r - 1, 3))
						+ (Math.pow(s2b0, 2) / Math.pow(n - 1, 3)) + (Math.pow(
						s2b1, 2) / Math.pow(d - 1, 3)));
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
