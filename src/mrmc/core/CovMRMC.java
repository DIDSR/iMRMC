package mrmc.core;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import mrmc.gui.GUInterface;
import mrmc.gui.SizePanel;

/**
 * Estimates covariance statistics given reader data for two modalities <br>
 * <br>
 * Key fields
 * -- {@link #AUCsReaderAvg}
 * -- {@link #AUCs}
 * -- {@link #momentsAA}, {@link #momentsAB}, {@link #momentsBB}
 * -- {@link #momentsBiasedAA}, {@link #momentsBiasedAB}, {@link #momentsBiasedBB}
 * -- {@link #coefficientsAA}, {@link #coefficientsAB}, {@link #coefficientsBB}
 * -- {@link #fullyCrossedA}, {@link #fullyCrossedB}, {@link #fullyCrossedAB}
 * </ul>
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 * @author Qi Gong
 */
public class CovMRMC {
	
	/**
	 * {@link mrmc.core.InputFile}
	 */
	InputFile InputFileStat;
	/**
	 * {@link mrmc.core.DBRecord}
	 */
	DBRecord DBRecordStat, DBRecordSize;
	/**
	 * {@link mrmc.gui.SizePanel}
	 */
	SizePanel SizePanel1;
	/**
	 * The U-statistic moments according to Gallas2009_Commun-Stat-A-Theor_v38p2586 (first element is empty).
	 */
	public double[] 
			momentsAA = new double[9],
			momentsBB = new double[9],
			momentsAB = new double[9];
	/**
	 * The MLE moments according to Gallas2009_Commun-Stat-A-Theor_v38p2586 (first element is empty).
	 */
	public double[] 
			momentsBiasedAA = new double[9],
			momentsBiasedBB = new double[9],
			momentsBiasedAB = new double[9];
	/**
	 * The U-statistic readers moments according to Gallas2009_Commun-Stat-A-Theor_v38p2586 (first element is empty).
	 */
	public double[][] readerMomentsAA, readerMomentsBB, readerMomentsAB;
	/**
	 * The U-statistic readers Cov moments.
	 */
	public double[][][] readerMomentsAACov, readerMomentsBBCov, readerMomentsABCov;
	/**
	 * The MLE moments according to Gallas2009_Commun-Stat-A-Theor_v38p2586 (first element is empty).
	 */
	public double[][] readerMomentsBiasedAA, readerMomentsBiasedBB, readerMomentsBiasedAB;
	/**
	 * The MLE Cov moments.
	 */
	public double[][][] readerMomentsBiasedAACov, readerMomentsBiasedBBCov, readerMomentsBiasedABCov;
	
	/**
	 * The coefficients according to Gallas2009_Commun-Stat-A-Theor_v38p2586 (first element is empty)
	 */
	public double[] 
			coefficientsAA = new double[9],
			coefficientsBB = new double[9],
			coefficientsAB = new double[9];
	/**
	 * The coefficients for each reader according to Gallas2008_Neural-Networks_v21p387 and Gallas2006_Acad-Radiol_v13p353 (first element is empty)
	 */
	public double[][] 
			readerCoefficientsAA,
			readerCoefficientsBB,
			readerCoefficientsAB;
	/**
	 * The coefficients for each reader according to Gallas2008_Neural-Networks_v21p387 and Gallas2006_Acad-Radiol_v13p353 (first element is empty)
	 */
	public double[][][] 
			readerCoefficientsAACov,
			readerCoefficientsBBCov,
			readerCoefficientsABCov;
	/**
	 * The scores from the readers
	 */
	public double[][][] t0_modAB, t1_modAB, t0_modAA, t1_modAA, t0_modBB, t1_modBB;
	public int[][][] d0_modAB, d1_modAB, d0_modAA, d1_modAA, d0_modBB, d1_modBB;
	public int[][] N0perReader, N1perReader;
	public int totalscoredA, totalscoredB, totalscoredAB;

	/**
	 * The reader-averaged auc for each modality
	 */
	private double[] AUCsReaderAvg;
	/**
	 * The AUCs for each reader and modality [Nreader][2]
	 */
	private double[][] AUCs;

	private long Nnormal, Ndisease, Nreader;
	
	boolean fullyCrossedA = true, fullyCrossedB = true, fullyCrossedAB = true;

	/**
	 * Constructor for CovMRMC created in {link mrmc.core.DBRecord} workflow 1<br>
	 * Uses <br>
	 * -- {@link mrmc.core.InputFile} <br>
	 * -- {@link mrmc.core.DBRecord} <br>
	 * <br>
	 * Implements <br>
	 * -- {@link #makeTMatrices()} <br>
	 * -- {@link #doAUCcovUstatistics(String)}
	 * 
	 */
	public CovMRMC(InputFile InputFileStatTemp, DBRecord DBRecordStatTemp) {

		InputFileStat = InputFileStatTemp;
		DBRecordStat = DBRecordStatTemp;

		Nreader = InputFileStat.Nreader;
		Nnormal = InputFileStat.Nnormal;
		Ndisease = InputFileStat.Ndisease;

		DBRecordStat.Nnormal = Nnormal;
		DBRecordStat.Ndisease = Ndisease;
		DBRecordStat.Nreader = Nreader;
		
		readerMomentsAA = new double[(int)Nreader][5];
		readerMomentsBiasedAA = new double[(int)Nreader][5];
		readerMomentsBB = new double[(int)Nreader][5];
		readerMomentsBiasedBB = new double[(int)Nreader][5];
		readerMomentsAB = new double[(int)Nreader][5];
		readerMomentsBiasedAB = new double[(int)Nreader][5];
		
		readerMomentsAACov = new double[(int)Nreader][(int)Nreader][5];
		readerMomentsBiasedAACov = new double[(int)Nreader][(int)Nreader][5];
		readerMomentsBBCov = new double[(int)Nreader][(int)Nreader][5];
		readerMomentsBiasedBBCov = new double[(int)Nreader][(int)Nreader][5];
		readerMomentsABCov = new double[(int)Nreader][(int)Nreader][5];
		readerMomentsBiasedABCov = new double[(int)Nreader][(int)Nreader][5];
		
		readerCoefficientsAA = new double[(int)Nreader][5];
		readerCoefficientsBB = new double[(int)Nreader][5];
		readerCoefficientsAB = new double[(int)Nreader][5];
		
		readerCoefficientsAACov = new double[(int)Nreader][(int)Nreader][5];
		readerCoefficientsBBCov = new double[(int)Nreader][(int)Nreader][5];
		readerCoefficientsABCov = new double[(int)Nreader][(int)Nreader][5];
		
		
		makeTMatrices();
		if(DBRecordStatTemp.selectedMod == 0) {
			doAUCcovUstatistics("AA");
		}
		if(DBRecordStatTemp.selectedMod == 1) {
			doAUCcovUstatistics("BB");
		}
		if(DBRecordStatTemp.selectedMod == 3) {
			doAUCcovUstatistics("AA");
			doAUCcovUstatistics("BB");
			doAUCcovUstatistics("AB");
			// AUCs and AUCsReaderAvg are set in the last function call for "AB"
		}
		
		DBRecordStat.AUCs = AUCs;
		DBRecordStat.AUCsReaderAvg = AUCsReaderAvg;

	}
	
	/**
	 * Constructor for CovMRMC created in {link mrmc.core.DBRecord} workflow 2 <br>
	 * Uses <br>
	 * -- {@link mrmc.core.InputFile} <br>
	 * -- {@link mrmc.core.DBRecord} <br>
	 * 
	 * @param SizePanelTemp refers to {@link mrmc.gui.SizePanel}
	 * 
	 */
	public CovMRMC(SizePanel SizePanelTemp) {

		SizePanel1 = SizePanelTemp;
		Nreader = Integer.parseInt(SizePanel1.NreaderJTextField.getText());
		Nnormal = Integer.parseInt(SizePanel1.NnormalJTextField.getText());
		Ndisease = Integer.parseInt(SizePanel1.NdiseaseJTextField.getText());

		makeDMatrices();

	}
	
	/**
	 * Constructor for CovMRMC created in {link mrmc.core.DBRecord} workflow 2 <br>
	 * Uses <br>
	 * -- {@link mrmc.core.InputFile} <br>
	 * -- {@link mrmc.core.DBRecord} <br>
	 * 
	 * @param SizePanelTemp refers to {@link mrmc.gui.SizePanel}
	 * @param DBRecordSizeTemp refers to {@link mrmc.gui.GUInterface}
	 * 
	 */
	public CovMRMC(SizePanel SizePanelTemp, DBRecord DBRecordSizeTemp) {

		SizePanel1 = SizePanelTemp;
		DBRecordSize = DBRecordSizeTemp;

		Nreader = Integer.parseInt(SizePanel1.NreaderJTextField.getText());
		Nnormal = Integer.parseInt(SizePanel1.NnormalJTextField.getText());
		Ndisease = Integer.parseInt(SizePanel1.NdiseaseJTextField.getText());

		DBRecordSize.Nnormal = Nnormal;
		DBRecordSize.Ndisease = Ndisease;
		DBRecordSize.Nreader = Nreader;
		
		readerMomentsAA = new double[(int)Nreader][5];
		readerMomentsBiasedAA = new double[(int)Nreader][5];
		readerMomentsBB = new double[(int)Nreader][5];
		readerMomentsBiasedBB = new double[(int)Nreader][5];
		readerMomentsAB = new double[(int)Nreader][5];
		readerMomentsBiasedAB = new double[(int)Nreader][5];		
		
		readerCoefficientsAA = new double[(int)Nreader][5];
		readerCoefficientsBB = new double[(int)Nreader][5];
		readerCoefficientsAB = new double[(int)Nreader][5];
		
		readerMomentsAACov = new double[(int)Nreader][(int)Nreader][5];
		readerMomentsBiasedAACov = new double[(int)Nreader][(int)Nreader][5];
		readerMomentsBBCov = new double[(int)Nreader][(int)Nreader][5];
		readerMomentsBiasedBBCov = new double[(int)Nreader][(int)Nreader][5];
		readerMomentsABCov = new double[(int)Nreader][(int)Nreader][5];
		readerMomentsBiasedABCov = new double[(int)Nreader][(int)Nreader][5];
		
		readerCoefficientsAACov = new double[(int)Nreader][(int)Nreader][5];
		readerCoefficientsBBCov = new double[(int)Nreader][(int)Nreader][5];
		readerCoefficientsABCov = new double[(int)Nreader][(int)Nreader][5];
		
		
		makeDMatrices();
		if(DBRecordSize.selectedMod == 0) {
			doAUCcovUstatistics("AA");
		}
		if(DBRecordSize.selectedMod == 1) {
			doAUCcovUstatistics("BB");
		}
		if(DBRecordSize.selectedMod == 3) {
			doAUCcovUstatistics("AA");
			doAUCcovUstatistics("BB");
			doAUCcovUstatistics("AB");
			// AUCs and AUCsReaderAvg are set in the last function call for "AB"
		}
	
		DBRecordSize.AUCs = AUCs;
		DBRecordSize.AUCsReaderAvg = AUCsReaderAvg;

	}


/**
 * Perform variance analysis with  <br>
 * -- signal-absent scores {@link #t0_modAA}, {@link #t0_modBB}, {@link #t0_modAB} <br>
 * -- signal-present scores {@link #t1_modAA}, {@link #t1_modBB}, {@link #t1_modAB} <br>
 * -- signal-absent study design {@link #t0_modAA}, {@link #t0_modBB}, {@link #t0_modAB} <br>
 * -- signal-present study design {@link #t1_modAA}, {@link #t1_modBB}, {@link #t1_modAB} <br>
 * -- experiment size {@link #Nreader}, {@link #Nnormal}, {@link #Ndisease}. <br>
 *  <br>
 * -- Creates  <br>
 * -- {@link #AUCsReaderAvg}, {@link #AUCs}
 * -- {@link #momentsAA}, {@link #momentsBB}, {@link #momentsAB} are 1D arrays of length [8] <br>
 * -- {@link #momentsBiasedAA}, {@link #momentsBiasedBB}, {@link #momentsBiasedAB} are 1D arrays of length [8] <br>
 * -- {@link #coefficientsAA}, {@link #coefficientsBB}, {@link #coefficientsAB} are 1D arrays of length [8] <br>
 * 
 */
public void doAUCcovUstatistics(String flagModality) {

	AUCsReaderAvg = new double[3];
	AUCsReaderAvg[0] = -1.0;
	AUCsReaderAvg[1] = -1.0;
	AUCsReaderAvg[2] = -1.0;
	AUCs = new double[(int) Nreader][3];
	for(int i=0; i<Nreader; i++) {
		AUCs[i][0] = -1;
		AUCs[i][1] = -1;
		AUCs[i][2] = -1;
	}

	double[] moments = new double[9];
	double[][] readerMoments = new double[(int) Nreader][5];
	double[][][] readerMomentsCov = new double[(int) Nreader][(int) Nreader][5];
	 // The MLE moments according to Gallas2009_Commun-Stat-A-Theor_v38p2586 (first element is empty).
	double[] momentsBiased = new double[9];
	double[][] readerMomentsBiased = new double[(int) Nreader][5];
	double[][][] readerMomentsBiasedCov = new double[(int) Nreader][(int) Nreader][5];
	// The coefficients according to Gallas2009_Commun-Stat-A-Theor_v38p2586 (first element is empty)
	double[] coefficients = new double[9];
	double[][] readerCoefficients = new double [(int)Nreader][5];
	double[][][] readerCoefficientsCov = new double [(int)Nreader][(int)Nreader][5];
	//case AB
	double[][][] t0 = t0_modAB;
	double[][][] t1 = t1_modAB;
	int[][][]    d0 = d0_modAB;
	int[][][]    d1 = d1_modAB;
	//
	switch(flagModality) {
	case "AA":
		t0 = t0_modAA;
		d0 = d0_modAA;
		t1 = t1_modAA;
		d1 = d1_modAA;
		break;
	case "BB":
		t0 = t0_modBB;
		d0 = d0_modBB;
		t1 = t1_modBB;
		d1 = d1_modBB;
		break;
	}
	
	double aucA = 0.0;
	double aucB = 0.0;
	
	double[][] w = new double[(int) Nreader][2];
	int[] pairs = new int[3];
	double totalwada = 0;
	double totalwbdb = 0;
	double totalwbdbCov = 0;
	double[] readerTotalwada = new double[(int)Nreader];
	double[] readerTotalwbdb = new double[(int)Nreader];
	double[][] readerTotalwbdbCov = new double[(int)Nreader][(int)Nreader];
	double[] bnumer = new double[9];
	double[][] readerBnumer = new double[(int)Nreader][5];
	double[][][] readerBnumerCov = new double[(int)Nreader][(int)Nreader][5];
	double[][] wadasaSumr = new double[(int) Nnormal][(int) Ndisease];
	double[][] wbdbsbSumr = new double[(int) Nnormal][(int) Ndisease];
	double[] wadasaSumir = new double[(int) Ndisease];
	double[] wbdbsbSumir = new double[(int) Ndisease];
	double[] wadasaSumjr = new double[(int) Nnormal];
	double[] wbdbsbSumjr = new double[(int) Nnormal];
	double wadasaSumijr = 0.0;
	double wbdbsbSumijr = 0.0;

	double[] bdenom = new double[9];
	double[][] readerBdenom = new double[(int)Nreader][5];
	double[][][] readerBdenomCov = new double[(int)Nreader][(int)Nreader][5];
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
		
		

		// *********the sum for each reader that will feed ReaderM1-ReaderM4
		readerBdenom[ir][1]= Matrix.total(Matrix.elementMultiply(wada, wbdb));
		readerBdenom[ir][2] = Matrix.total(Matrix.elementMultiply(wada_sumi, wbdb_sumi));
		readerBdenom[ir][3] = Matrix.total(Matrix.elementMultiply(wada_sumj, wbdb_sumj));
		readerBdenom[ir][4] = wada_sumij * wbdb_sumij;
		readerBnumer[ir][1] = Matrix.total(Matrix.elementMultiply(wadasa, wbdbsb));
		readerBnumer[ir][2] = Matrix.total(Matrix.elementMultiply(wadasa_sumi,wbdbsb_sumi));
		readerBnumer[ir][3] = Matrix.total(Matrix.elementMultiply(wadasa_sumj,wbdbsb_sumj));
		readerBnumer[ir][4] = wadasa_sumij * wbdbsb_sumij;
		
		

		// *********aggregate the sum over readers that will feed M5-M8
		wadaSumr = Matrix.add(wadaSumr, wada);
		wbdbSumr = Matrix.add(wbdbSumr, wbdb);
		wadasaSumr = Matrix.add(wadasaSumr, wadasa);
		wbdbsbSumr = Matrix.add(wbdbsbSumr, wbdbsb);
		wadaSumir = Matrix.add(wadaSumir, wada_sumi);
		wbdbSumir = Matrix.add(wbdbSumir, wbdb_sumi);
		wadasaSumir = Matrix.add(wadasaSumir, wadasa_sumi);
		wbdbsbSumir = Matrix.add(wbdbsbSumir, wbdbsb_sumi);

		wadaSumjr = Matrix.add(wadaSumjr, wada_sumj);
		wbdbSumjr = Matrix.add(wbdbSumjr, wbdb_sumj);
		wadasaSumjr = Matrix.add(wadasaSumjr, wadasa_sumj);
		wbdbsbSumjr = Matrix.add(wbdbsbSumjr, wbdbsb_sumj);
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
			readerTotalwada[ir] = wa * totalda;
			AUCs[ir][0] = Matrix.total(wadasa) / totalda;
			aucA = aucA + totalda * AUCs[ir][0];
		}
		// evaluate AUCs modality b
		if (totaldb > 0) {
			totalwbdb = totalwbdb + wb * totaldb;
			readerTotalwbdb[ir] = wb * totaldb;
			AUCs[ir][1] = Matrix.total(wbdbsb) / totaldb;
			aucB = aucB + totaldb * AUCs[ir][1];
		}
		if(totalda > 0 && totaldb > 0 ) AUCs[ir][2] = AUCs[ir][0] - AUCs[ir][1];
		// Coveriance
		for (int irCov = 0; irCov < Nreader; irCov++) {
			// ***************for the second modality******************
			int[][] designB0Cov = Matrix.extractFirstDimension(d0, irCov, 1);
			int[][] designB1Cov = Matrix.extractFirstDimension(d1, irCov, 1);
			int[][] dbCov = Matrix.multiply(designB0Cov,
					Matrix.matrixTranspose(designB1Cov));
			int totaldbCov = Matrix.total(dbCov);
			if (totaldbCov > 0) {
				totalwbdbCov = totalwbdbCov + wb * totaldbCov;
				readerTotalwbdbCov[ir][irCov] = wb * totaldbCov;
			}
			double wbCov = w[irCov][1];
			double[][] t0B_irCov = Matrix.extractFirstDimension(t0, irCov, 1);
			double[][] t1B_irCov = Matrix.extractFirstDimension(t1, irCov, 1);

			double[][] sb0Cov = Matrix.multiply(t0B_irCov,
					Matrix.matrixTranspose(ones_vect1));
			double[][] sb1Cov = Matrix.multiply(ones_vect0,
					Matrix.matrixTranspose(t1B_irCov));
			double[][] sbCov = Matrix.subtract(sb1Cov, sb0Cov);
			for (int i = 0; i < Nnormal; i++)
				for (int j = 0; j < Ndisease; j++) {
					if (sbCov[i][j] < 0)
						sbCov[i][j] = 0.0;
					else if (sbCov[i][j] == 0)
						sbCov[i][j] = 0.5;
					else if (sbCov[i][j] > 0)
						sbCov[i][j] = 1.0;
				}
			double[][] wbdbCov = Matrix.linearTrans(dbCov, wbCov, 0);
			double[][] wbdbsbCov = Matrix.elementMultiply(wbdbCov, sbCov);
			
			// ***********precompute row (col???) sums***********************
			double[] wada_sumiCov = Matrix.colSum(wada);
			double[] wbdb_sumiCov = Matrix.colSum(wbdbCov);
			double[] wadasa_sumiCov = Matrix.colSum(wadasa);
			double[] wbdbsb_sumiCov = Matrix.colSum(wbdbsbCov);
			// ***********precompute col (row?????) sums***********************
			double[] wada_sumjCov = Matrix.rowSum(wada);
			double[] wbdb_sumjCov = Matrix.rowSum(wbdbCov);
			double[] wadasa_sumjCov = Matrix.rowSum(wadasa);
			double[] wbdbsb_sumjCov = Matrix.rowSum(wbdbsbCov);
			// **********precompute the matrix sums*****************
			double wada_sumijCov = Matrix.total(wada);
			double wbdb_sumijCov = Matrix.total(wbdbCov);
			double wadasa_sumijCov = Matrix.total(wadasa);
			double wbdbsb_sumijCov = Matrix.total(wbdbsbCov);
			// *********the sum for reader Cov that will feed ReaderM1-ReaderM4
			readerBdenomCov[ir][irCov][1]= Matrix.total(Matrix.elementMultiply(wada, wbdbCov));
			readerBdenomCov[ir][irCov][2] = Matrix.total(Matrix.elementMultiply(wada_sumiCov, wbdb_sumiCov));
			readerBdenomCov[ir][irCov][3] = Matrix.total(Matrix.elementMultiply(wada_sumjCov, wbdb_sumjCov));
			readerBdenomCov[ir][irCov][4] = wada_sumijCov * wbdb_sumijCov;
			readerBnumerCov[ir][irCov][1] = Matrix.total(Matrix.elementMultiply(wadasa, wbdbsbCov));
			readerBnumerCov[ir][irCov][2] = Matrix.total(Matrix.elementMultiply(wadasa_sumiCov,wbdbsb_sumiCov));
			readerBnumerCov[ir][irCov][3] = Matrix.total(Matrix.elementMultiply(wadasa_sumjCov,wbdbsb_sumjCov));
			readerBnumerCov[ir][irCov][4] = wadasa_sumijCov * wbdbsb_sumijCov;
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
	// readers moment
	double[][] readerDenom = new double[(int)Nreader][5];
	double[][] readerNumer = new double[(int)Nreader][5];
	double[][][] readerDenomCov = new double[(int)Nreader][(int)Nreader][5];
	double[][][] readerNumerCov = new double[(int)Nreader][(int)Nreader][5];
	double[][] readerBias2unbias = new double[][] {
				{ 0, 0, 0, 0, 0}, { 0, 1.0, 0, 0, 0},
				{ 0, -1.0, 1.0, 0, 0},{ 0, -1.0, 0, 1.0, 0},
				{ 0, 1.0, -1.0, -1.0, 1.0},};
	for (int ir = 0; ir < Nreader; ir++) {
		readerDenom[ir] = Matrix.multiply(readerBias2unbias, readerBdenom[ir]);
		readerNumer[ir] = Matrix.multiply(readerBias2unbias, readerBnumer[ir]);
		readerMomentsBiased = readerBnumer;
		// biased moment
		for (int i = 0; i < readerMomentsBiased[0].length; i++) {
			if (readerBdenom[ir][i] > Matrix.min(w) / 2.0)
				readerMomentsBiased[ir][i] = readerMomentsBiased[ir][i]/readerBdenom[ir][i];
		}
		// unbiased moment
		readerMoments = readerNumer;
		for (int i = 0; i < readerMoments[0].length; i++) {
			if (readerDenom[ir][i] > Matrix.min(w) / 2.0)
				readerMoments[ir][i] = readerMoments[ir][i]/readerDenom[ir][i];
		}
		// coefficients
		readerCoefficients[ir] = Matrix.linearTrans(readerDenom[ir], 1.0 / (readerTotalwada[ir] * readerTotalwbdb[ir]), 0);
		readerCoefficients[ir][4] = readerCoefficients[ir][4] - 1.0;
		// reader Covariance
		for (int irCov = 0; irCov < Nreader; irCov++) {
			readerDenomCov[ir][irCov] = Matrix.multiply(readerBias2unbias, readerBdenomCov[ir][irCov]);
			readerNumerCov[ir][irCov] = Matrix.multiply(readerBias2unbias, readerBnumerCov[ir][irCov]);
			readerMomentsBiasedCov = readerBnumerCov;
			// biased moment
			for (int i = 0; i < readerMomentsBiasedCov[0][0].length; i++) {
				if (readerBdenomCov[ir][irCov][i] > Matrix.min(w) / 2.0)
					readerMomentsBiasedCov[ir][irCov][i] = readerMomentsBiasedCov[ir][irCov][i]/readerBdenomCov[ir][irCov][i];
			}
			// unbiased moment
			readerMomentsCov = readerNumerCov;
			for (int i = 0; i < readerMomentsCov[0][0].length; i++) {
				if (readerDenomCov[ir][irCov][i] > Matrix.min(w) / 2.0)
					readerMomentsCov[ir][irCov][i] = readerMomentsCov[ir][irCov][i]/(readerDenomCov[ir][irCov][i]);
			}
			// coefficients
			readerCoefficientsCov[ir][irCov] = Matrix.linearTrans(readerDenomCov[ir][irCov], 1.0 / (readerTotalwada[ir] * readerTotalwbdbCov[ir][irCov]), 0);
			readerCoefficientsCov[ir][irCov][4] = readerCoefficientsCov[ir][irCov][4] - 1.0;
		}
		
	}
	// coefficients
	coefficients = Matrix.linearTrans(denom, 1.0 / (totalwada * totalwbdb), 0);
	coefficients[8] = coefficients[8] - 1.0;

	switch(flagModality) {
	case "AA":
		if( Double.isInfinite(1.0/totalwada) ) {
			AUCsReaderAvg[0] = -1;
			return;
		}

		AUCsReaderAvg[0] = aucA / totalwada;
		momentsAA = moments;
		momentsBiasedAA = momentsBiased;
		readerMomentsAA = readerMoments;
		readerMomentsBiasedAA = readerMomentsBiased;
		coefficientsAA = coefficients;
		readerCoefficientsAA = readerCoefficients;
		readerMomentsAACov = readerMomentsCov;
		readerMomentsBiasedAACov = readerMomentsBiasedCov;
		readerCoefficientsAACov = readerCoefficientsCov;
		break;
	case "BB":
		if( Double.isInfinite(1.0/totalwbdb) ) {
			AUCsReaderAvg[1] = -1;
			return;
		}

		AUCsReaderAvg[1] = aucB / totalwbdb;
		momentsBB = moments;
		momentsBiasedBB = momentsBiased;
		readerMomentsBB = readerMoments;
		readerMomentsBiasedBB = readerMomentsBiased;
		coefficientsBB = coefficients;
		readerCoefficientsBB = readerCoefficients;
		readerMomentsBBCov = readerMomentsCov;
		readerMomentsBiasedBBCov = readerMomentsBiasedCov;
		readerCoefficientsBBCov = readerCoefficientsCov;
		break;
	case "AB":
		if( Double.isInfinite(1.0/totalwada) ) {
			AUCsReaderAvg[0] = -1;
			return;
		}
		if( Double.isInfinite(1.0/totalwbdb) ) {
			AUCsReaderAvg[1] = -1;
			return;
		}

		AUCsReaderAvg[0] = aucA / totalwada;
		AUCsReaderAvg[1] = aucB / totalwbdb;
		AUCsReaderAvg[2] = AUCsReaderAvg[0] - AUCsReaderAvg[1];
		momentsAB = moments;
		momentsBiasedAB = momentsBiased;
		readerMomentsAB = readerMoments;
		readerMomentsBiasedAB = readerMomentsBiased;
		coefficientsAB = coefficients;
		readerCoefficientsAB = readerCoefficients;
		readerMomentsABCov = readerMomentsCov;
		readerMomentsBiasedABCov = readerMomentsBiasedCov;
		readerCoefficientsABCov = readerCoefficientsCov;
		break;
	}
	
}


/**
 * Takes study data ({@link mrmc.core.InputFile#keyedData}, {@link mrmc.core.InputFile#truthVals})
 *  and creates data for {@link mrmc.core.CovMRMC} <br>
 * --t-matrices: reader scores <br>
 * ---- {@link #t0_modAA}, {@link #t0_modAB}, {@link #t0_modBB}: signal-absent scores  [Nnormal ][Nreader][2 modalities] <br>
 * ---- {@link #t1_modAA}, {@link #t1_modAB}, {@link #t1_modBB}: signal-present scores [Ndisease][Nreader][2 modalities] <br>
 * --d-matrices: study design <br>
 * ---- {@link #d0_modAA}, {@link #d0_modAB}, {@link #d0_modBB}: signal absent indicator [Nnormal ][Nreader][2 modalities] <br>
 * ---- {@link #d1_modAA}, {@link #d1_modAB}, {@link #d1_modBB}: signal present scores   [Ndisease][Nreader][2 modalities] <br>
 * --fully crossed status
 * ---- {@link #fullyCrossedA}, {@link #fullyCrossedB}, {@link #fullyCrossedAB}
 *
 * CALLED BY: {@link mrmc.gui.InputFileCard.varAnalysisListener} <br>
 *
 */
public void makeTMatrices() {
	
	String modA = DBRecordStat.modalityA;
	String modB = DBRecordStat.modalityB;

	t0_modAB = new double[(int) Nnormal][(int) Nreader][2];
	t1_modAB = new double[(int) Ndisease][(int) Nreader][2];
	t0_modAA = new double[(int) Nnormal][(int) Nreader][2];
	t0_modBB = new double[(int) Nnormal][(int) Nreader][2];
	t1_modAA = new double[(int) Ndisease][(int) Nreader][2];
	t1_modBB = new double[(int) Ndisease][(int) Nreader][2];
	d0_modAA = new int[(int) Nnormal][(int) Nreader][2];
	d1_modAA = new int[(int) Ndisease][(int) Nreader][2];
	d0_modBB = new int[(int) Nnormal][(int) Nreader][2];
	d1_modBB = new int[(int) Ndisease][(int) Nreader][2];
	d0_modAB = new int[(int) Nnormal][(int) Nreader][2];
	d1_modAB = new int[(int) Ndisease][(int) Nreader][2];
	N0perReader =new int[(int) Nreader][3];
	N1perReader =new int[(int) Nreader][3];
	double ScoreModA;
	double ScoreModB;
	int PresentModA;
	int PresentModB;
	totalscoredA = 0;
	totalscoredB = 0;
	totalscoredAB = 0;
	
	int m = 0; // signal-absent case counter
	int n = 0; // signal-present case counter
	int k = 0; // reader counter
	for (String r : InputFileStat.keyedData.keySet()) {
		m = 0;
		n = 0;
		for (String c : InputFileStat.keyedData.get(r).keySet()) {

			// For all readers and cases, determine which had observations
			if (InputFileStat.keyedData.get(r).containsKey(c)) {
				if (InputFileStat.keyedData.get(r).get(c).containsKey(modA)) {
					ScoreModA = InputFileStat.keyedData.get(r).get(c).get(modA);
					PresentModA = 1;
				} else {
					ScoreModA = -1000000;
					PresentModA = 0;
					fullyCrossedA = false;
				}
				if (InputFileStat.keyedData.get(r).get(c).containsKey(modB)) {
					ScoreModB = InputFileStat.keyedData.get(r).get(c).get(modB);
					PresentModB = 1;
				} else {
					ScoreModB = -1000000;
					PresentModB = 0;
					fullyCrossedB = false;
				}
			} else {
				ScoreModA = -1000000;
				ScoreModB = -1000000;
				PresentModA = 0;
				PresentModB = 0;
				fullyCrossedAB = false;
			}
			
			// Fill in the score and design matrices
			if (InputFileStat.truthVals.get(c) == 0) {
				t0_modAB[m][k][0] = ScoreModA;
				t0_modAB[m][k][1] = ScoreModB;
				t0_modAA[m][k][0] = ScoreModA;
				t0_modAA[m][k][1] = ScoreModA;
				t0_modBB[m][k][0] = ScoreModB;
				t0_modBB[m][k][1] = ScoreModB;
				
				d0_modAB[m][k][0] = PresentModA;
				d0_modAB[m][k][1] = PresentModB;
				d0_modAA[m][k][0] = PresentModA;
				d0_modAA[m][k][1] = PresentModA;
				d0_modBB[m][k][0] = PresentModB;
				d0_modBB[m][k][1] = PresentModB;
				N0perReader[k][0] = N0perReader[k][0] + PresentModA;
				N0perReader[k][1] = N0perReader[k][1] + PresentModB;
				N0perReader[k][2] = N0perReader[k][2] + Math.min(PresentModA, PresentModB) ;
				totalscoredA = totalscoredA + PresentModA;
				totalscoredB = totalscoredB + PresentModB;
				totalscoredAB = totalscoredAB + PresentModA*PresentModB;
				m++;
			} else {
				t1_modAB[n][k][0] = ScoreModA;
				t1_modAB[n][k][1] = ScoreModB;
				t1_modAA[n][k][0] = ScoreModA;
				t1_modAA[n][k][1] = ScoreModA;
				t1_modBB[n][k][0] = ScoreModB;
				t1_modBB[n][k][1] = ScoreModB;

				d1_modAB[n][k][0] = PresentModA;
				d1_modAB[n][k][1] = PresentModB;
				d1_modAA[n][k][0] = PresentModA;
				d1_modAA[n][k][1] = PresentModA;
				d1_modBB[n][k][0] = PresentModB;
				d1_modBB[n][k][1] = PresentModB;
				N1perReader[k][0] = N1perReader[k][0] + PresentModA;
				N1perReader[k][1] = N1perReader[k][1] + PresentModB;
				N1perReader[k][2] = N1perReader[k][2] + Math.min(PresentModA, PresentModB) ;
				totalscoredA = totalscoredA + PresentModA;
				totalscoredB = totalscoredB + PresentModB;
				totalscoredAB = totalscoredAB + PresentModA*PresentModB;
				n++;
			}
		} // loop over cases
		k++;
	} // loop over readers

	
}

/**
 * Creates a study design for modality 0 and 1 based on designated
 * split-plot design and pairing of readers and cases
 * The scores do not matter. We only want the design arrays.
 * 
 */
public void makeDMatrices() {

	t0_modAB = new double[(int) Nnormal][(int) Nreader][2];
	t1_modAB = new double[(int) Ndisease][(int) Nreader][2];
	t0_modAA = new double[(int) Nnormal][(int) Nreader][2];
	t0_modBB = new double[(int) Nnormal][(int) Nreader][2];
	t1_modAA = new double[(int) Ndisease][(int) Nreader][2];
	t1_modBB = new double[(int) Ndisease][(int) Nreader][2];
	d0_modAA = new int[(int) Nnormal][(int) Nreader][2];
	d0_modBB = new int[(int) Nnormal][(int) Nreader][2];
	d0_modAB = new int[(int) Nnormal][(int) Nreader][2];
	d1_modAA = new int[(int) Ndisease][(int) Nreader][2];
	d1_modBB = new int[(int) Ndisease][(int) Nreader][2];
	d1_modAB = new int[(int) Ndisease][(int) Nreader][2];
	N0perReader = new int[1][3];
	N1perReader = new int[1][3];
	int NreaderPerModality, NreaderPerGroup;
	int NnormalPerModality, NnormalPerGroup;
	int NdiseasePerModality, NdiseasePerGroup;

	if (SizePanel1.pairedReadersFlag == 1) {
		NreaderPerModality = (int) Nreader;
	} else {
		NreaderPerModality = (int) (Nreader / 2);
		fullyCrossedA = false;
		fullyCrossedB = false;
		fullyCrossedAB = false;
	}
	if (SizePanel1.pairedNormalsFlag == 1) {
		NnormalPerModality = (int) Nnormal;
	} else {
		NnormalPerModality = (int) (Nnormal / 2);
		fullyCrossedA = false;
		fullyCrossedB = false;
		fullyCrossedAB = false;
	}
	if (SizePanel1.pairedDiseasedFlag == 1) {
		NdiseasePerModality = (int) Ndisease;
	} else {
		NdiseasePerModality = (int) (Ndisease / 2);
		fullyCrossedA = false;
		fullyCrossedB = false;
		fullyCrossedAB = false;
	}
	//
	if(SizePanel1.numSplitPlots > 1) {
		fullyCrossedA = false;
		fullyCrossedB = false;
		fullyCrossedAB = false;
	}
	
	NreaderPerGroup = NreaderPerModality / SizePanel1.numSplitPlots;
	NnormalPerGroup = NnormalPerModality / SizePanel1.numSplitPlots;
	NdiseasePerGroup = NdiseasePerModality / SizePanel1.numSplitPlots;
	N0perReader[0][0]= NnormalPerGroup;
	N0perReader[0][1]= NnormalPerGroup;
	if (SizePanel1.pairedNormalsFlag == 1) {
		N0perReader[0][2] = NnormalPerGroup;
	} else {
		N0perReader[0][2] = 0;
	}
	N1perReader[0][0]= NdiseasePerGroup;
	N1perReader[0][1]= NdiseasePerGroup;
	if (SizePanel1.pairedDiseasedFlag == 1) {
		N1perReader[0][2] = NdiseasePerGroup;
	} else {
		N1perReader[0][2] = 0;
	}
	int readerID_modA, caseID_modA;
	int readerID_modB, caseID_modB;
	for (int s = 0; s < SizePanel1.numSplitPlots; s++) {
		for (int i = 0; i < NreaderPerGroup; i++) {

			readerID_modA = i + (NreaderPerGroup * s);
			if (SizePanel1.pairedReadersFlag == 1) {
				readerID_modB = readerID_modA;
			} else {
				readerID_modB = readerID_modA + NreaderPerModality;
			}
			
			for (int j = 0; j < NnormalPerGroup; j++) {
				
				caseID_modA = j + (NnormalPerGroup * s);
				if (SizePanel1.pairedNormalsFlag == 1) {
					caseID_modB = caseID_modA;
				} else {
					caseID_modB = caseID_modA + NnormalPerModality;
				}

				d0_modAA[caseID_modA][readerID_modA][0] = 1;
				d0_modAA[caseID_modA][readerID_modA][1] = 1;
				d0_modBB[caseID_modB][readerID_modB][0] = 1;
				d0_modBB[caseID_modB][readerID_modB][1] = 1;
				d0_modAB[caseID_modA][readerID_modA][0] = 1;
				d0_modAB[caseID_modB][readerID_modB][1] = 1;
			}

			for (int j = 0; j < NdiseasePerGroup; j++) {

				caseID_modA = j + (NdiseasePerGroup * s);
				if (SizePanel1.pairedDiseasedFlag == 1) {
					caseID_modB = caseID_modA;
				} else {
					caseID_modB = caseID_modA + NdiseasePerModality;
				}
				
				d1_modAA[caseID_modA][readerID_modA][0] = 1;
				d1_modAA[caseID_modA][readerID_modA][1] = 1;
				d1_modBB[caseID_modB][readerID_modB][0] = 1;
				d1_modBB[caseID_modB][readerID_modB][1] = 1;
				d1_modAB[caseID_modA][readerID_modA][0] = 1;
				d1_modAB[caseID_modB][readerID_modB][1] = 1;
			}
		}
	}
}



}