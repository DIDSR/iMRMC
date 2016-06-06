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

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import roemetz.core.RoeMetz;
import mrmc.chart.exploreExpSize;
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
	

	DBRecord DBRecordStat, DBRecordSize;

	private final int INFINITY = 500;
	private final double ZERO = 1E-300;
//	private final static int USE_MLE = 1;
	private final static int NO_MLE = 0;

	public double tStatEst = 0;
	public double effSize;
	public double sigLevel;
	public double tStatCalc;

	public double DF_BDG, DF_Hillis;
	public double pValNormal, pValBDG, pValHillis;
	public double cutoffNormal, cutoffBDG, cutoffHillis;
	public double lambdaBDG, lambdaHillis;
	public double powerNormal, powerBDG, powerHillis;
	public double ciBotNormal, ciBotBDG, ciBotHillis; 
	public double ciTopNormal, ciTopBDG, ciTopHillis;
	public double rejectNormal, rejectBDG, rejectHillis;

	/**
	 * Constructor used for calculating statistics when performing initial variance analysis
	 * 
	 * @param InputFileTemp is the source that yields results shown in the statistical panel of the GUI
	 * @param DBRecordStatTemp is the record to have a statistical analysis
	 */
	public StatTest(DBRecord DBRecordStatTemp) {

		DBRecordStat = DBRecordStatTemp;
		double sig = 0.05;
		double meanCI;

		if (DBRecordStat.selectedMod == 1 || DBRecordStat.selectedMod == 0) {
			meanCI = DBRecordStat.AUCsReaderAvg[DBRecordStat.selectedMod];
			/* Compare single-modality AUC to 0.5 */
			tStatEst = Math.sqrt(Math.pow(meanCI - 0.5, 2)/DBRecordStat.totalVar);
		} else {
			meanCI = DBRecordStat.AUCsReaderAvg[0] - DBRecordStat.AUCsReaderAvg[1];
			/* Compare difference in modality AUCs to 0.0 */
			tStatEst = Math.sqrt(Math.pow(meanCI, 2)/DBRecordStat.totalVar);
		}	

//		DF_BDG = calcDF_BDGbckIndep(DBRecordStat);
//		DF_BDG = calcDF_BDGbckGroup(DBRecordStat);
		DF_BDG = calcDF_BDGms(DBRecordStat);


		// calculate p-value and cutoff assuming normality
		NormalDist ndist = new NormalDist();
		pValNormal = 2*(1 - ndist.cdf(tStatEst));
		cutoffNormal = ndist.inverseF(1 - sig/2.0);

		// calculate p-value and cutoff assuming t-distribution with DF_BDG or DF_Hillis
		// Use normal distribution if df > 50, since they are approximately
		// equal at that point, and StudentDist can't handle large dfs
		if (DF_BDG >= 50) {
			pValBDG = pValNormal;
			cutoffBDG = cutoffNormal;
		} else {
			StudentDist tdist = new StudentDist((int) DF_BDG );
			pValBDG = 2*(1 - tdist.cdf( tStatEst ));
			cutoffBDG = tdist.inverseF( 1-sig/2 );
		}
		
		ciBotNormal = meanCI - Math.sqrt(DBRecordStat.totalVar) * cutoffNormal; // normal approx
		ciTopNormal = meanCI + Math.sqrt(DBRecordStat.totalVar) * cutoffNormal; // normal approx
		ciBotBDG = meanCI - Math.sqrt(DBRecordStat.totalVar) * cutoffBDG;
		ciTopBDG = meanCI + Math.sqrt(DBRecordStat.totalVar) * cutoffBDG;


		if(pValNormal < sig) rejectNormal = 1;
		if(pValBDG < sig) rejectBDG = 1;
		
        // if study is fully crossed calculate DF_Hillis
		// calculate p-value and cutoff assuming t-distribution with DF_Hillis
		// Use normal distribution if df > 50, since they are approximately
		// equal at that point, and FisherFDist can't handle large dfs
		// And also calculate ciBotHillis , ciPotHillis and rejectHillis
		if (DBRecordStat.flagFullyCrossed){
			DF_Hillis = calcDF_Hillis(DBRecordStat);
			if (DF_Hillis >= 50) {
				pValHillis = pValNormal;
				cutoffHillis = cutoffNormal;
			} else {
				StudentDist tdist = new StudentDist((int) DF_Hillis );
				pValHillis = 2*(1 - tdist.cdf( tStatEst ));
				cutoffHillis = tdist.inverseF( 1-sig/2 );
			}
			ciBotHillis = meanCI - Math.sqrt(DBRecordStat.totalVar) * cutoffHillis;
			ciTopHillis = meanCI + Math.sqrt(DBRecordStat.totalVar) * cutoffHillis;
			if(pValHillis < sig) rejectHillis = 1;
		}else{
			rejectHillis = Double.NaN;
		}
		if(DBRecordStat.verbose) {
			System.out.println("NR=" + DBRecordStat.Nreader + 
			           ",  N0=" + DBRecordStat.Nnormal + 
			           ",  N1=" + DBRecordStat.Ndisease);
			System.out.println("auc0=" + DBRecordStat.AUCsReaderAvg[0] + 
					         "  auc1=" + DBRecordStat.AUCsReaderAvg[1]);
			System.out.println("auc0-auc1=" + meanCI);
			System.out.println("totalVar=" + DBRecordStat.totalVar);
			System.out.println("varA=" + DBRecordStat.varA);
			System.out.println("varB=" + DBRecordStat.varB);
		

			System.out.println("tStatEst=" + tStatEst);
			System.out.println("Normal approx:" + 
					"  pValF=" + pValNormal + "  cutoffNormal=" + cutoffNormal + 
					"  ci=" + ciBotNormal + ","  + ciTopNormal);
			System.out.println("DF_BDG=" + DF_BDG 
					+ "  pValBDG=" + pValBDG + "  cutoffBDG=" + cutoffBDG 
					+ "  ci=" + ciBotBDG + ","  + ciTopBDG);
			if (DBRecordStat.flagFullyCrossed){
				System.out.println("DF_Hillis=" + DF_Hillis 
						+ "  pValHillis=" + pValHillis + "  cutoffHillis=" + cutoffHillis 
						+ "  ci=" + ciBotHillis + ","  + ciTopHillis);
			}
		}

	}

	/**
	 * Constructor used for calculating statistics when sizing a new trial
	 * 
	 * @param SizePanel is the bottom panel of the GUI
	 * @param GUItemp The GUI that launched this stat analysis
	 */
	public StatTest(SizePanel SizePanel, DBRecord DBRecordStat, DBRecord DBRecordSize) {

		this.DBRecordStat = DBRecordStat;
		this.DBRecordSize = DBRecordSize;
		int selectedMod = DBRecordSize.selectedMod;
		
		effSize = SizePanel.effSize;
		sigLevel = SizePanel.sigLevel;
		tStatCalc = effSize / Math.sqrt(DBRecordSize.totalVar);
		
		System.out.println("\nBegin Sizing Analysis");
		System.out.println("effSize= " + effSize 
				+ " totalVar= " + DBRecordSize.totalVar 
				+ " tStatCalc=" + tStatCalc);

//		DF_BDG = calcDF_BDGbckIndep(DBRecordSize);
//		DF_BDG = calcDF_BDGbckGroup(DBRecordSize);
		DF_BDG = calcDF_BDGms(DBRecordSize);
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
		if (DF_Hillis < 2) {
			if (!exploreExpSize.doFullSize){
				JFrame frame = new JFrame();
				JOptionPane.showMessageDialog(frame,
						"DF_Hillis was calculated to be " + DF_Hillis +
						"\nDF_Hillis less than 2 cannot be handled" +
						"\nTherefore, it is being set to 2", "Warning",
						JOptionPane.ERROR_MESSAGE);
			}
			DF_Hillis = 2;
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
* In this DF estimation method ... <br>
* The BCK components of variance are used following Satterthwaite instead of mean squares <br>
* This follows Eq. 4 Gaylor1969_Technometrics_v4p691 <br>
* Each contribution to the denominator of the Satterthwaite approximation <br>
*     takes the form (contribution to the variance)^2 / (df of contribution to the variance) <br>
* Each contribution to var_A, var_B, covAB is treated separately. <br>
 * 
 * @param curRecord {@link mrmc.core.DBRecord}
 * @return Degrees of freedom
 */
public double calcDF_BDGbckIndep(DBRecord curRecord) {
	
	/*
	 */
	boolean verbose = curRecord.verbose;
	int selectedMod = curRecord.selectedMod;
	double totalVar = curRecord.totalVar;
	double[][] tempBCK = curRecord.BCK;
	double[][] tempBCKcoeff = curRecord.BCKcoeff;
	
	// According to Gaylor1969_Technometrics_v4p691, we will set DF_min
	double DF_min = 0.0;

	// Note: let c = the coefficient in front of each BCK term = one over the number of corresponding samples = 1/n
	// Note: The degrees of freedom of each term is the number of corresponding samples minus one = n-1
	// Note: Therefore, the degrees of freedom are 1/c - 1

	
	double DF_denom = 0.0;
	// Analysis of modality A
	if (selectedMod == 0){
		// DF leading term corresponding to normal cases
		double DFnormal  = 1.0/tempBCKcoeff[selectedMod][0] - 1.0;
		// DF leading term corresponding to disese cases
		double DFdisease = 1.0/tempBCKcoeff[selectedMod][1] - 1.0;
		// DF leading term corresponding to reader cases
		double DFreader  = 1.0/tempBCKcoeff[selectedMod][3] - 1.0;

		// According to Gaylor1969_Technometrics_v4p691, we will set DF_min
		DF_min =          DFnormal;          
		DF_min = Math.min(DFdisease, DF_min); 
		DF_min = Math.min(DFreader,  DF_min); 

		for(int i=0; i<7; i++) {
			double componentDF = (1.0/tempBCKcoeff[selectedMod][i] - 1.0);
			DF_denom = DF_denom
				+ Math.pow(tempBCKcoeff[selectedMod][i]*tempBCK[selectedMod][i],  2) / componentDF;
		}
	}
	// Analysis of modality B
	if (selectedMod == 1){
		// DF leading term corresponding to normal cases
		double DFnormal  = 1.0/tempBCKcoeff[selectedMod][0] - 1.0;
		// DF leading term corresponding to disese cases
		double DFdisease = 1.0/tempBCKcoeff[selectedMod][1] - 1.0;
		// DF leading term corresponding to reader cases
		double DFreader  = 1.0/tempBCKcoeff[selectedMod][3] - 1.0;

		// According to Gaylor1969_Technometrics_v4p691, we will set DF_min
		DF_min =          DFnormal;          
		DF_min = Math.min(DFdisease, DF_min); 
		DF_min = Math.min(DFreader,  DF_min); 

		for(int i=0; i<7; i++) {
			double componentDF = (1.0/tempBCKcoeff[selectedMod][i] - 1.0);
			DF_denom = DF_denom
				+ Math.pow(tempBCKcoeff[selectedMod][i]*tempBCK[selectedMod][i],  2) / componentDF;
		}
	}
	// Analysis of difference in modalities
	if(selectedMod == 3) {
		// DF leading term corresponding to normal cases
		double DFnormalA  = 1.0/tempBCKcoeff[0][0] - 1.0;
		double DFnormalB  = 1.0/tempBCKcoeff[1][0] - 1.0;
		// DF leading term corresponding to disese cases
		double DFdiseaseA = 1.0/tempBCKcoeff[0][1] - 1.0;
		double DFdiseaseB = 1.0/tempBCKcoeff[1][1] - 1.0;
		// DF leading term corresponding to reader cases
		double DFreaderA  = 1.0/tempBCKcoeff[0][3] - 1.0;
		double DFreaderB  = 1.0/tempBCKcoeff[1][3] - 1.0;

		// componentsDF[0][i] = componentsDF[1][i] when readers and cases are paired across modalities
		// Averaging the DF's across modalities doesn't change this
		// Averaging the DF's across modalities should provide robustness to instances of missing data
		double DFnormal  = DFnormalA /2.0 + DFnormalB /2.0;
		double DFdisease = DFdiseaseA/2.0 + DFdiseaseB/2.0;
		double DFreader  = DFreaderA /2.0 + DFreaderB /2.0;
		
		// According to Gaylor1969_Technometrics_v4p691, we will set DF_min
		DF_min =          DFnormal;          
		DF_min = Math.min(DFdisease, DF_min); 
		DF_min = Math.min(DFreader,  DF_min); 

		for(int i=0; i<7; i++) {
			
			double componentDF_A = (1.0/tempBCKcoeff[0][i] - 1.0);
			double componentDF_B = (1.0/tempBCKcoeff[1][i] - 1.0);
			DF_denom = DF_denom
				+ Math.pow(tempBCKcoeff[0][i]*tempBCK[0][i],  2) / componentDF_A
				+ Math.pow(tempBCKcoeff[1][i]*tempBCK[1][i],  2) / componentDF_B;
			if(tempBCKcoeff[2][i] > 0.0) {

				double componentDF_AB =	(1.0/tempBCKcoeff[2][i] - 1.0);
				DF_denom = DF_denom
					+ Math.pow(2.0*tempBCKcoeff[2][i]*tempBCK[2][i], 2) / componentDF_AB;
			}
		}
	}

	DF_BDG = Math.pow(totalVar, 2) / DF_denom;

	/**
	 * TODO Investigate a minimum df
	 */
	// According to Gaylor1969_Technometrics_v4p691, there is a minimum DF
	if (DF_BDG < DF_min) {
		if(verbose) {
			JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(frame,
					"DF_BDG is below a minimum." +
					"\nDF_BDG was calculated to be " + DF_BDG + "\nIt is being set to DF_min = " + DF_min +
					"\nThis follows Gaylor1969_Technometrics_v4p691" +
					"\nand indicates that your data is very limited", "Warning",
					JOptionPane.ERROR_MESSAGE);
		}
		DF_BDG = DF_min;
	}

	// Do not return a DF_BDG that is less than 2
	if (DF_BDG < 2) {
		if(verbose) {
 			JFrame frame = new JFrame();
 			JOptionPane.showMessageDialog(frame,
 					"DF_BDG is below a minimum." +
					"\nDF_BDG was calculated to be " + DF_BDG +
					"\nDF_BDG less than 2 cannot be handled" +
					"\nTherefore, it is being set to 2", "Warning",
					JOptionPane.ERROR_MESSAGE);
		}
		DF_BDG = 2;
	}

	return DF_BDG;
}

/**
* In this DF estimation method ... <br>
* The BCK components of variance are used following Satterthwaite instead of mean squares <br>
* This follows Eq. 4 Gaylor1969_Technometrics_v4p691 <br>
* Each contribution to the denominator of the Satterthwaite approximation <br>
*     takes the form (contribution to the variance)^2 / (df of contribution to the variance) <br>
* When the covariance term exists, the <br>
*     contribution to the variance = contribution to (var_A + var_B - 2*covAB) <br>
* Otherwise the contribution to var_A and var_B are treated separately. <br>
* 
* @param curRecord {@link mrmc.core.DBRecord}
* @return Degrees of freedom
*/
public double calcDF_BDGbckGroup(DBRecord curRecord) {

boolean verbose = curRecord.verbose;
int selectedMod = curRecord.selectedMod;
double totalVar = curRecord.totalVar;
double[][] tempBCK = curRecord.BCK;
double[][] tempBCKcoeff = curRecord.BCKcoeff;

// According to Gaylor1969_Technometrics_v4p691, we will set DF_min
double DF_min = 0.0;

// Note: let c = the coefficient in front of each BCK term = one over the number of corresponding samples = 1/n
// Note: The degrees of freedom of each term is the number of corresponding samples minus one = n-1
// Note: Therefore, the degrees of freedom are 1/c - 1


double DF_denom = 0.0;
// Analysis of modality A
if (selectedMod == 0){
	// DF leading term corresponding to normal cases
	double DFnormal  = 1.0/tempBCKcoeff[selectedMod][0] - 1.0;
	// DF leading term corresponding to disese cases
	double DFdisease = 1.0/tempBCKcoeff[selectedMod][1] - 1.0;
	// DF leading term corresponding to reader cases
	double DFreader  = 1.0/tempBCKcoeff[selectedMod][3] - 1.0;

	// According to Gaylor1969_Technometrics_v4p691, we will set DF_min
	DF_min =          DFnormal;          
	DF_min = Math.min(DFdisease, DF_min); 
	DF_min = Math.min(DFreader,  DF_min); 

	for(int i=0; i<7; i++) {
		double componentDF = (1.0/tempBCKcoeff[selectedMod][i] - 1.0);
		DF_denom = DF_denom
			+ Math.pow(tempBCKcoeff[selectedMod][i]*tempBCK[selectedMod][i],  2) / componentDF;
	}
}
// Analysis of modality B
if (selectedMod == 1){
	// DF leading term corresponding to normal cases
	double DFnormal  = 1.0/tempBCKcoeff[selectedMod][0] - 1.0;
	// DF leading term corresponding to disese cases
	double DFdisease = 1.0/tempBCKcoeff[selectedMod][1] - 1.0;
	// DF leading term corresponding to reader cases
	double DFreader  = 1.0/tempBCKcoeff[selectedMod][3] - 1.0;

	// According to Gaylor1969_Technometrics_v4p691, we will set DF_min
	DF_min =          DFnormal;          
	DF_min = Math.min(DFdisease, DF_min); 
	DF_min = Math.min(DFreader,  DF_min); 

	for(int i=0; i<7; i++) {
		double componentDF = (1.0/tempBCKcoeff[selectedMod][i] - 1.0);
		DF_denom = DF_denom
			+ Math.pow(tempBCKcoeff[selectedMod][i]*tempBCK[selectedMod][i],  2) / componentDF;
	}
}
// Analysis of difference in modalities
if(selectedMod == 3) {
	// DF leading term corresponding to normal cases
	double DFnormalA  = 1.0/tempBCKcoeff[0][0] - 1.0;
	double DFnormalB  = 1.0/tempBCKcoeff[1][0] - 1.0;
	// DF leading term corresponding to disese cases
	double DFdiseaseA = 1.0/tempBCKcoeff[0][1] - 1.0;
	double DFdiseaseB = 1.0/tempBCKcoeff[1][1] - 1.0;
	// DF leading term corresponding to reader cases
	double DFreaderA  = 1.0/tempBCKcoeff[0][3] - 1.0;
	double DFreaderB  = 1.0/tempBCKcoeff[1][3] - 1.0;

	// componentsDF[0][i] = componentsDF[1][i] when readers and cases are paired across modalities
	// Averaging the DF's across modalities doesn't change this
	// Averaging the DF's across modalities should provide robustness to instances of missing data
	double DFnormal  = DFnormalA /2.0 + DFnormalB /2.0;
	double DFdisease = DFdiseaseA/2.0 + DFdiseaseB/2.0;
	double DFreader  = DFreaderA /2.0 + DFreaderB /2.0;
	
	// According to Gaylor1969_Technometrics_v4p691, we will set DF_min
	DF_min =          DFnormal;          
	DF_min = Math.min(DFdisease, DF_min); 
	DF_min = Math.min(DFreader,  DF_min); 

	for(int i=0; i<7; i++) {
		
		// When readers or cases are paired, there will be a covariance component 
		// In these cases we should use the variance components of AUC_A - AUC_B
		// Otherwise, we should use the variance components of AUC_A and AUC_B separately
		if(tempBCKcoeff[2][i] > 0.0) {
			double currContributionToVariance = 
				      + tempBCKcoeff[0][i]*tempBCK[0][i]
				      + tempBCKcoeff[1][i]*tempBCK[1][i]
				- 2.0 * tempBCKcoeff[2][i]*tempBCK[2][i];

			// componentsDF[0][i] = componentsDF[1][i] when readers and cases are paired across modalities
			// Averaging the DF's across modalities doesn't change this
			// Averaging the DF's across modalities should provide robustness to instances of missing data
			double currContributionToVarianceDF =
					(1.0/tempBCKcoeff[0][i] - 1.0)/2.0 + (1.0/tempBCKcoeff[1][i] - 1.0)/2.0;

			DF_denom = DF_denom
				+ Math.pow(currContributionToVariance,  2) / currContributionToVarianceDF;
		}
		else {

			DF_denom = DF_denom
				+ Math.pow(tempBCKcoeff[0][i]*tempBCK[0][i],  2) / (1.0/tempBCKcoeff[0][i] - 1.0)
				+ Math.pow(tempBCKcoeff[1][i]*tempBCK[1][i],  2) / (1.0/tempBCKcoeff[1][i] - 1.0);
		}
	}
}

DF_BDG = Math.pow(totalVar, 2) / DF_denom;

/**
 * TODO Investigate a minimum df
 */
//According to Gaylor1969_Technometrics_v4p691, there is a minimum DF
if (DF_BDG < DF_min) {
	if(verbose) {
		JFrame frame = new JFrame();
		JOptionPane.showMessageDialog(frame,
				"DF_BDG is below a minimum." +
				"\nDF_BDG was calculated to be " + DF_BDG + "\nIt is being set to DF_min = " + DF_min +
				"\nThis follows Gaylor1969_Technometrics_v4p691" +
				"\nand indicates that your data is very limited", "Warning",
				JOptionPane.ERROR_MESSAGE);
	}
	DF_BDG = DF_min;
}

//Do not return a DF_BDG that is less than 2
if (DF_BDG < 2) {
	if(verbose) {
			JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(frame,
				"DF_BDG is below a minimum." +
				"\nDF_BDG was calculated to be " + DF_BDG +
				"\nDF_BDG less than 2 cannot be handled" +
				"\nTherefore, it is being set to 2", "Warning",
				JOptionPane.ERROR_MESSAGE);
	}
	DF_BDG = 2;
}

return DF_BDG;
}

/**
* In this DF estimation method ...
* When the readers, normal cases, disease cases are all paired across modalities
*     I use the an approximate Satterthwaite method based on mean squares
*     as outlined in Obuchowski2012_Acad-Radiol_v19p1508
* When the normal cases are unpaired,
*     I treat the modality specific mean squares separately.
* When the disease cases are unpaired,
*     I treat the modality specific mean squares separately.
* When readers are unpaired,
*     I treat the modality specific mean squares separately.
* 
* @param curRecord {@link mrmc.core.DBRecord}
* @return Degrees of freedom
*/
public double calcDF_BDGms(DBRecord curRecord) {

boolean verbose = curRecord.verbose;
int selectedMod = curRecord.selectedMod;
double totalVar = curRecord.totalVar;
double[][] tempBCKbias = curRecord.BCKbias;
double[][] tempBCKcoeff = curRecord.BCKcoeff;

double DFnormalA  = Math.round(1.0/tempBCKcoeff[0][0] - 1.0);
double DFnormalB  = Math.round(1.0/tempBCKcoeff[1][0] - 1.0);
double DFdiseaseA = Math.round(1.0/tempBCKcoeff[0][1] - 1.0);
double DFdiseaseB = Math.round(1.0/tempBCKcoeff[1][1] - 1.0);
double DFreaderA  = Math.round(1.0/tempBCKcoeff[0][3] - 1.0);
double DFreaderB  = Math.round(1.0/tempBCKcoeff[1][3] - 1.0);

/*double DFnormalA  = 1.0/tempBCKcoeff[0][0] - 1.0;
double DFnormalB  = 1.0/tempBCKcoeff[1][0] - 1.0;
double DFdiseaseA = 1.0/tempBCKcoeff[0][1] - 1.0;
double DFdiseaseB = 1.0/tempBCKcoeff[1][1] - 1.0;
double DFreaderA  = 1.0/tempBCKcoeff[0][3] - 1.0;
double DFreaderB  = 1.0/tempBCKcoeff[1][3] - 1.0;*/

/*
 * The following are mean squares times 2/N0/N1/NR
 * The factor is not necessary since it cancels in the DF calculation
 */
double MSnormalA  = tempBCKbias[0][0];
double MSnormalB  = tempBCKbias[1][0];
double MSnormal   = tempBCKbias[0][0] + tempBCKbias[1][0] - 2.0*tempBCKbias[2][0];
double MSdiseaseA = tempBCKbias[0][1];
double MSdiseaseB = tempBCKbias[1][1];
double MSdisease  = tempBCKbias[0][1] + tempBCKbias[1][1] - 2.0*tempBCKbias[2][1];
double MSreaderA  = tempBCKbias[0][3];
double MSreaderB  = tempBCKbias[1][3];
double MSreader   = tempBCKbias[0][3] + tempBCKbias[1][3] - 2.0*tempBCKbias[2][3];

double DF_denom = 0.0, DF_min = 0.0;
// Analysis of modality A
if (selectedMod == 0){
	DF_denom =
			+ Math.pow(MSnormalA  / DFnormalA,  2) / DFnormalA
			+ Math.pow(MSdiseaseA / DFdiseaseA, 2) / DFdiseaseA
			+ Math.pow(MSreaderA  / DFreaderA,  2) / DFreaderA;
	DF_min = Math.min(DFnormalA,  DFdiseaseA);
	DF_min = Math.min(DF_min,  DFreaderA);
}
// Analysis of modality B
if (selectedMod == 1){
	DF_denom =
			+ Math.pow(MSnormalB  / DFnormalB,  2) / DFnormalB
			+ Math.pow(MSdiseaseB / DFdiseaseB, 2) / DFdiseaseB
			+ Math.pow(MSreaderB  / DFreaderB,  2) / DFreaderB;
	DF_min = Math.min(DFnormalB,  DFdiseaseB);
	DF_min = Math.min(DF_min,  DFreaderB);
}
// Analysis of difference in modalities
if(selectedMod == 3) {

	// DFnormalA = DFnormalB when normal cases are paired across modalities
	// DFdiseaseA = DFdiseaseB when disease cases are paired across modalities
	// DFreaderA = DFreaderB when readers are paired across modalities
	// Their average should provide robustness to instances of missing data
	// Averaging the DF's across modalities should provide robustness to instances of missing data
	double DFnormal   = Math.min(DFnormalA, DFnormalB);
	double DFdisease  = Math.min(DFdiseaseA, DFdiseaseB);
	double DFreader   = Math.min(DFreaderA, DFreaderB);
	DF_min = Math.min(DFnormal,  DFdisease);
	DF_min = Math.min(DF_min,  DFreader);

	// If normal cases are paired across modalities
	if(tempBCKcoeff[2][0] > 0.0) {
		DF_denom = DF_denom
				+ Math.pow(MSnormal  / DFnormal,  2) / DFnormal;
	} else {
		DF_denom = DF_denom
				+ Math.pow(MSnormalA / DFnormalA, 2) / DFnormalA
				+ Math.pow(MSnormalB / DFnormalB, 2) / DFnormalB;
	}
	
	// If disease cases are paired across modalities
	if(tempBCKcoeff[2][1] > 0.0) {
		DF_denom = DF_denom
				+ Math.pow(MSdisease  / DFdisease,  2) / DFdisease;
	} else {
		DF_denom = DF_denom
				+ Math.pow(MSdiseaseA / DFdiseaseA, 2) / DFdiseaseA
				+ Math.pow(MSdiseaseB / DFdiseaseB, 2) / DFdiseaseB;
	}
	
	// If readers are paired across modalities
	if(tempBCKcoeff[2][3] > 0.0) {
		DF_denom = DF_denom
				+ Math.pow(MSreader  / DFreader,  2) / DFreader;
	} else {
		DF_denom = DF_denom
				+ Math.pow(MSreaderA / DFreaderA, 2) / DFreaderA
				+ Math.pow(MSreaderB / DFreaderB, 2) / DFreaderB;
	}
}

DF_BDG = Math.pow(totalVar, 2) / DF_denom;

/**
 * TODO Investigate a minimum df
 */
// According to Gaylor1969_Technometrics_v4p691, there is a minimum DF
if (DF_BDG < DF_min) {
	if(verbose&&!RoeMetz.doValidation&&!exploreExpSize.doFullSize) {
		JFrame frame = new JFrame();
		JOptionPane.showMessageDialog(frame,
				"DF_BDG is below a minimum." +
				"\nDF_BDG was calculated to be " + DF_BDG + "\nIt is being set to DF_min = " + DF_min +
				"\nThis follows Gaylor1969_Technometrics_v4p691" +
				"\nand indicates that your data is very limited", "Warning",
				JOptionPane.ERROR_MESSAGE);
	}
	DF_BDG = DF_min;
}

//Do not return a DF_BDG that is less than 2
if (DF_BDG < 2) {
	if((verbose&&!RoeMetz.doValidation&&!exploreExpSize.doFullSize)) {
			JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(frame,
				"DF_BDG is below a minimum." +
				"\nDF_BDG was calculated to be " + DF_BDG +
				"\nDF_BDG less than 2 cannot be handled" +
				"\nTherefore, it is being set to 2", "Warning",
				JOptionPane.ERROR_MESSAGE);
	}
	DF_BDG = 2;
}

return DF_BDG;
}

	/**
	 * Calculates the denominator degrees of freedom by Hillis 2008 method
	 * 
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
	*/
	/**
	 * Calculates the denominator degrees of freedom by Hillis 2008 method given the curRecord, 
	 * which can be DBRecordStat or DBRecordSize 
	 * 
	 */
	public double calcDF_Hillis(DBRecord curRecord) {
		
		double[] AUCsReaderAvg = curRecord.AUCsReaderAvg;
		double dnm = 2.0;
		double dnr = curRecord.Nreader;
		double denom;

		double[][] OR = curRecord.OR;

		if(curRecord.selectedMod == 3) {
			// Hillis2014, pg 332
			denom = curRecord.ms_tr + Math.max(dnr*(OR[3][3]-OR[3][4]),0);
			DF_Hillis = (dnr-1.0)*(dnm-1.0)*denom*denom/curRecord.ms_tr/curRecord.ms_tr;

			// tStatEst = Math.pow(AUCsReaderAvg[0] - AUCsReaderAvg[1], 2)/curRecord.totalVar;
			// tStatEst = Math.sqrt(tStatEst);
			// The expression above is equivalent to the expression below
			// tStatEst = Math.sqrt(curRecord.ms_t/denom);

			// Here is another equivalent expression for the total variance
			// The ones are from the contrast between modalities 1*AUC1 + (-1)*AUC2
			// curRecord.totalVar = (1*1 + (-1)*(-1))*denom/dnr;

		} else {
			// Hillis2014, pg 333
			
			double ms_r=0.0;
			if(curRecord.selectedMod == 0) ms_r = curRecord.ms_rA;
			if(curRecord.selectedMod == 1) ms_r = curRecord.ms_rB;
					
			denom = ms_r + Math.max(dnr*OR[curRecord.selectedMod][3],0);
			DF_Hillis = (dnr-1.0)*denom*denom/ms_r/ms_r;
			
			// tStatEst = (AUCsReaderAvg[selectedMod]-0.5)/Math.sqrt(curRecord.totalVar);
			
			// Below is an equivalent expression for the total variance
			// curRecord.totalVar = denom/dnr;

		}

		if (DF_Hillis < 2) {
			JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(frame,
					"DF_Hillis was calculated to be " + DF_Hillis + "\nIt is being set to 2", "Error",
					JOptionPane.ERROR_MESSAGE);
			DF_Hillis = 2;
		}
		if( Double.isInfinite(DF_Hillis) ) {
		    if (!RoeMetz.doValidation){
			JFrame frame = new JFrame();
			JOptionPane.showMessageDialog(frame,
					"DF_Hillis was calculated to be infinite." + 
					"\nIt is likely due to ms_tr = 0.0 (difference in modalities analysis)" +
					"\n    or ms_r = 0.0 (single modality analysis)." + 
					"\nDF_Hillis is being set to 50." +
					"\nPlease check your data.", "Error",
					JOptionPane.ERROR_MESSAGE);
		    }
			DF_Hillis = 50.0;			
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
			double tempF = BetaDist.cdf(df1 / 2.0 + j, df2 / 2.0,
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
	*/
	
}
