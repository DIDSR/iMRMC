package mrmc.chart;

import java.text.DecimalFormat;

import roemetz.gui.RMGUInterface;
import mrmc.core.DBRecord;
import mrmc.gui.InputFileCard;
import mrmc.gui.SizePanel;
import mrmc.gui.StatPanel;


/**
 * This class works for export analysis results to files
 * It includes 4 methods,
 * 1. exportStatPanel: export statpanel and table
 * 2. exportSizePanel: export sizepanel in imrmc gui 
 * 3. exportMCvariance: export MC variance result in iRoeMetz simulation. 
 * 4. exportSummary: export analysis summary.
 * 

 * @author Brandon D. Gallas, Ph.D
 * @author Qi Gong
 */


public class exportToFile {
	
	static DecimalFormat twoDec = new DecimalFormat("0.00");
	static DecimalFormat threeDec = new DecimalFormat("0.000");
	static DecimalFormat fourDec = new DecimalFormat("0.0000");
	static DecimalFormat threeDecE = new DecimalFormat("0.000E0");
	static DecimalFormat fourDecE = new DecimalFormat("0.0000E0");
	static DecimalFormat fiveDecE = new DecimalFormat("0.00000E0");
	private static String SEPA = ",";
	
	// export statpanel and table for both iMRMC and iRoeMetz
	public static String exportStatPanel(String oldReport, DBRecord StatDBRecord, StatPanel processStatPanel ) {
		String str = oldReport;
		int useMLE = StatDBRecord.flagMLE;		
		String result = processStatPanel.getStatResults();
		str = str
				+ "\r\n**********************StatPanel outputs ***************************\r\n";
		str = str + StatDBRecord.recordDesc;
		str = str + "Modality A = " + StatDBRecord.modalityA + "\r\n";
		str = str + "Modality B = " + StatDBRecord.modalityB + "\r\n";
		if (useMLE == 1)
			str = str + "this report uses MLE estimate of components.\r\n";
		
		str = str + "\r\n" + StatDBRecord.getAUCsReaderAvgString(StatDBRecord.selectedMod);
		str = str + "\r\nStatistical Tests:\r\n" + result + SEPA;		
		return str;
	}
	
	//export BDG and BCK tables
	public static String exportTable1(String oldReport, DBRecord DBRecordTable) {
		String str = oldReport;
		double[][] BDGdata1 = DBRecord.getBDGTab(DBRecordTable.selectedMod,
				DBRecordTable.BDG, DBRecordTable.BDGcoeff);
		double[][] BCKdata1 = DBRecord.getBCKTab(DBRecordTable.selectedMod,
				DBRecordTable.BCK, DBRecordTable.BCKcoeff);
		if(DBRecordTable.flagMLE == 1) {
			BDGdata1 = DBRecord.getBDGTab(DBRecordTable.selectedMod,
					DBRecordTable.BDGbias, DBRecordTable.BDGcoeff);
			BCKdata1 = DBRecord.getBCKTab(DBRecordTable.selectedMod,
					DBRecordTable.BCKbias, DBRecordTable.BCKcoeff);	
		}
		
		str = str
				+ "\r\n**********************BDG output Results***************************\r\n";
		str = str + "Moments" + SEPA + "M1" + SEPA + "M2" + SEPA + "M3" + SEPA
				+ "M4" + SEPA + "M5" + SEPA + "M6" + SEPA + "M7" + SEPA + "M8";
		/*
		 * added for saving the results
		 */
		str = str + "\r\n" + "comp MA" + SEPA;
		for(int i = 0; i<8; i++)
			str = str + fiveDecE.format(BDGdata1[0][i]) + SEPA;
		str = str + "\r\n" + "coeff MA" + SEPA;
		for(int i = 0; i<8; i++)
			str = str + fiveDecE.format(BDGdata1[1][i]) + SEPA;
		str = str + "\r\n" + "comp MB" + SEPA;
		for(int i = 0; i<8; i++)
			str = str + fiveDecE.format(BDGdata1[2][i]) + SEPA;
		str = str + "\r\n" + "coeff MB" + SEPA;
		for(int i = 0; i<8; i++)
			str = str + fiveDecE.format(BDGdata1[3][i]) + SEPA;
		str = str + "\r\n" + "comp product" + SEPA;
		for(int i = 0; i<8; i++)
			str = str + fiveDecE.format(BDGdata1[4][i]) + SEPA;
		str = str + "\r\n" + "-coeff product" + SEPA;
		for(int i = 0; i<8; i++)
			str = str + fiveDecE.format(BDGdata1[5][i]) + SEPA;
		str = str + "\r\n" + "total" + SEPA;
		for(int i = 0; i<8; i++)
			str = str + fiveDecE.format(BDGdata1[6][i]) + SEPA;
		str = str +"\r\n"; 
		str = str
				+ "\r\n**********************BCK output Results***************************";
		str = str + "\r\nMoments" + SEPA + "N" + SEPA + "D" + SEPA + "N~D" + SEPA
				+ "R" + SEPA + "N~R" + SEPA + "D~R" + SEPA + "R~N~D";
		str = str + "\r\n" + "comp MA" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCKdata1[0][i]) + SEPA;
		str = str + "\r\n" + "coeff MA" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCKdata1[1][i]) + SEPA;
		str = str + "\r\n" + "comp MB" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCKdata1[2][i]) + SEPA;
		str = str + "\r\n" + "coeff MB" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCKdata1[3][i]) + SEPA;
		str = str + "\r\n" + "comp product" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCKdata1[4][i]) + SEPA;
		str = str + "\r\n" + "-coeff product" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCKdata1[5][i]) + SEPA;
		str = str + "\r\n" + "total" + SEPA;
		for (int i = 0; i < 7; i++)
			str = str + fiveDecE.format(BCKdata1[6][i]) + SEPA;
		str = str +"\r\n"; 
		return str;
	}
	
	//export DBM, MS, BCK tables
	public static String exportTable2(String oldReport, DBRecord DBRecordTable) {
		String str = oldReport;
		double[][] DBMdata1 = DBRecord.getDBMTab(DBRecordTable.selectedMod,
				DBRecordTable.DBM, DBRecordTable.DBMcoeff);
		double[][] ORdata1 = DBRecord.getORTab(DBRecordTable.selectedMod,
				DBRecordTable.OR, DBRecordTable.ORcoeff);
		double[][] MSdata1 = DBRecord.getMSTab(DBRecordTable.selectedMod,
				DBRecordTable.MS, DBRecordTable.MScoeff);
		if(DBRecordTable.flagMLE == 1) {
			DBMdata1 = DBRecord.getDBMTab(DBRecordTable.selectedMod,
					DBRecordTable.DBMbias, DBRecordTable.DBMcoeff);
			ORdata1 = DBRecord.getORTab(DBRecordTable.selectedMod,
					DBRecordTable.ORbias, DBRecordTable.ORcoeff);
			MSdata1 = DBRecord.getMSTab(DBRecordTable.selectedMod,
					DBRecordTable.MSbias, DBRecordTable.MScoeff);			
		}
		str = str
				+ "\r\n**********************DBM output Results***************************";
		str = str + "\r\nComponents" + SEPA + "R" + SEPA + "C" + SEPA + "R~C"
				+ SEPA + "T~R" + SEPA + "T~C" + SEPA + "T~R~C";
		str = str + "\r\n" + "components" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBMdata1[0][i]) + SEPA;
		str = str + "\r\n" + "coeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBMdata1[1][i]) + SEPA;
		str = str + "\r\n" + "total" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(DBMdata1[2][i]) + SEPA;
		str = str +"\r\n"; 
		str = str
				+ "\r\n**********************OR output Results***************************";
		str = str + "\r\nComponents" + SEPA + "R" + SEPA + "TR" + SEPA + "COV1"
				+ SEPA + "COV2" + SEPA + "COV3" + SEPA + "ERROR";
		str = str + "\r\n" + "components" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(ORdata1[0][i]) + SEPA;
		str = str + "\r\n" + "coeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(ORdata1[1][i]) + SEPA;
		str = str + "\r\n" + "total" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(ORdata1[2][i]) + SEPA;
		str = str +"\r\n"; 
		str = str
				+ "\r\n**********************MS output Results***************************";
		str = str + "\r\nComponents" + SEPA + "R" + SEPA + "C" + SEPA + "RC"
				+ SEPA + "MR" + SEPA + "MC" + SEPA + "MRC";
		str = str + "\r\ncomponents" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(MSdata1[0][i]) + SEPA;
		str = str + "\r\ncoeff" + SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(MSdata1[1][i]) + SEPA;
		str = str + "\r\n" + "total"+ SEPA;
		for (int i = 0; i < 6; i++)
			str = str + fiveDecE.format(MSdata1[2][i]) + SEPA;
		str = str +"\r\n"; 
		return str;
	}
	// export sizepanel for iMRMC
	public static String exportSizePanel(String oldReport, DBRecord SizeDBRecord, SizePanel processSizePanel) {
		String str = oldReport;
/*		double[][] BDG = SizeDBRecord.BDGresult;
		double[][] DBM = SizeDBRecord.DBMresult;
		double[][] BCK = SizeDBRecord.BCKresult;
		double[][] OR = SizeDBRecord.ORresult;
		double[][] MS = SizeDBRecord.MSresult;
		int useMLE = SizeDBRecord.flagMLE;		
		if(useMLE == 1) {
			BDG = SizeDBRecord.BDGbiasresult;
			DBM = SizeDBRecord.DBMbiasresult;
			BCK = SizeDBRecord.BCKbiasresult;
			OR = SizeDBRecord.ORbiasresult;
			MS = SizeDBRecord.MSbiasresult;
		}

		
		double[][] BDGcoeff = SizeDBRecord.BDGcoeffresult;
		double[][] BCKcoeff = SizeDBRecord.BCKcoeffresult;
		double[][] DBMcoeff = SizeDBRecord.DBMcoeffresult;
		double[][] ORcoeff = SizeDBRecord.ORcoeffresult;
		double[][] MScoeff = SizeDBRecord.MScoeffresult;*/
		double[] statParms = new double[2];
		
		statParms[0] = Double.parseDouble(processSizePanel.SigLevelJTextField.getText());
		statParms[1] = Double.parseDouble(processSizePanel.EffSizeJTextField.getText());
		
		int NreaderSize = Integer.parseInt(processSizePanel.NreaderJTextField.getText());
		int NnormalSize = Integer.parseInt(processSizePanel.NnormalJTextField.getText());
		int NdiseaseSize = Integer.parseInt(processSizePanel.NdiseaseJTextField.getText());
		
		String resultnew = processSizePanel.exportSizeResults();

		
		str = str
				+ "\r\n*********************Sizing parameters***************************";
		str = str + "\r\n" + "Effective Size = " + twoDec.format(statParms[1])
				+ SEPA + "Significance Level = " + twoDec.format(statParms[0])+"\r\n";
		str = str + "NReaderSize=  " +NreaderSize + SEPA
		          + "NnormalSize=  " + NnormalSize + SEPA
		          + "NDiseaseSize= " + NdiseaseSize ;
		
		str = str 
				+ "\r\n*****************************************************************";
		str = str + "\r\nSizing Results:\r\n";
		
		str = str + resultnew;
		str = str
				+ "\r\n*****************************************************************\r\n";
		return str;
	}
	
	// export MC variance result for iRoeMetz
	public static String exportMCvariance(String oldReport, DBRecord VarDBRecord) {
		String str = oldReport;
		double mcVarAUC_A       = VarDBRecord.AUCsReaderAvg[0];
		double mcVarAUC_B       = VarDBRecord.AUCsReaderAvg[1];
		double mcVarAUC_AminusB = VarDBRecord.AUCsReaderAvg[2];
		double sqrtMCvarAUC_A       = Math.sqrt(mcVarAUC_A);
		double sqrtMCvarAUC_B       = Math.sqrt(mcVarAUC_B);
		double sqrtMCvarAUC_AminusB = Math.sqrt(mcVarAUC_AminusB);
	   str = str
				+ "\r\n**********************MC Variance Results***************************\r\n";	
		str =  str + "      mcVarAUC_A = " + fourDecE.format(mcVarAUC_A) + "," + "      sqrtMCvarAUC_A = " + fourDecE.format(sqrtMCvarAUC_A) + "\r\n";
		str =  str + "      mcVarAUC_B = " + fourDecE.format(mcVarAUC_B) + "," + "      sqrtMCvarAUC_B = " + fourDecE.format(sqrtMCvarAUC_B) + "\r\n";
		str =  str + "mcVarAUC_AminusB = " + fourDecE.format(mcVarAUC_AminusB) + "," + "sqrtMCvarAUC_AminusB = " + fourDecE.format(sqrtMCvarAUC_AminusB) + "\r\n";
		return str;
	}
	
	
	
	// export summary for both iMRMC and iRoeMetz
	public static String exportSummary(String oldReport, DBRecord SummaryDBRecord) {
		String str = oldReport;
		str = str + "BEGIN SUMMARY\r\n";
		str = str + "NReader=  " + SummaryDBRecord.Nreader + "\r\n";
		str = str + "Nnormal=  " + SummaryDBRecord.Nnormal + "\r\n";
		str = str + "NDisease= " + SummaryDBRecord.Ndisease + "\r\n" + "\r\n";
		str = str + "Modality A = " + SummaryDBRecord.modalityA + "\r\n";
		str = str + "Modality B = " + SummaryDBRecord.modalityB + "\r\n" + "\r\n";
		str = str + "Reader-Averaged AUCs" + "\r\n";
		str = str +  SummaryDBRecord.getAUCsReaderAvgString(SummaryDBRecord.selectedMod).replaceAll(",   ", "\r\n") + "\r\n" + "\r\n";
		str = str +  "Reader Specific AUCs" +"\r\n";
		int k=1;
		int IDlength = 0;
		for(String desc_temp : SummaryDBRecord.InputFile1.readerIDs.keySet() ) {
			IDlength = Math.max(IDlength,desc_temp.length());
		}
		if (IDlength>9){
			for (int i=0; i<IDlength-9; i++){
				str = str + " ";
			}
			str = str + "Reader ID";
		    str = str+SEPA + "       AUC_A" + SEPA +  "      AUCs_B" + SEPA +  "   AUC_A - AUCs_B";
		} else{
			str = str + "Reader ID" +SEPA + "       AUC_A" + SEPA +  "      AUCs_B" + SEPA +  "   AUC_A - AUCs_B";
		}
		
		k=1;
		for(String desc_temp : SummaryDBRecord.InputFile1.readerIDs.keySet() ) {
		//for (int i = 1; i < GUI.DBRecordStat.Nreader+1; i++){
			str = str + "\r\n";
			for (int i=0; i<Math.max(IDlength,9) - desc_temp.length(); i++){
				str = str + " ";
			}
			str = str + desc_temp;
			str = str+ SEPA + "  " +
					fiveDecE.format(SummaryDBRecord.AUCs[k-1][0]) + SEPA + "  " +
					fiveDecE.format(SummaryDBRecord.AUCs[k-1][1]) + SEPA;
				double AUC_dif = SummaryDBRecord.AUCs[k-1][0]-SummaryDBRecord.AUCs[k-1][1];
					if(AUC_dif<0)
						str = str + "      " + fiveDecE.format(AUC_dif);
					else if (AUC_dif>0)
						str = str + "       " + fiveDecE.format(AUC_dif);
					else
						str = str + "        " + fiveDecE.format(AUC_dif);
			k=k+1;
		}
		str = str + "\r\n**********************BDG Moments***************************\r\n";
		str = str + "         Moments" + SEPA + "         M1" + SEPA + "         M2" + SEPA + "         M3" + SEPA
				+ "         M4" + SEPA + "         M5" + SEPA + "         M6" + SEPA + "         M7" + SEPA + "         M8"
				+ "\r\n";
		str = str + "Modality1(AUC_A)" + SEPA;
		for (int i = 0; i < 8; i++){
			if(SummaryDBRecord.BDG[0][i]>0)
				str = str + " " + fiveDecE.format(SummaryDBRecord.BDG[0][i])+SEPA;
			else
				str = str + "  " + fiveDecE.format(SummaryDBRecord.BDG[0][i])+SEPA;
		}
		str = str + "\r\n" + "Modality2(AUC_B)" + SEPA;
		for (int i = 0; i < 8; i++){
			if(SummaryDBRecord.BDG[1][i]>0)
				str = str + " " + fiveDecE.format(SummaryDBRecord.BDG[1][i])+SEPA;
			else
				str = str + "  " + fiveDecE.format(SummaryDBRecord.BDG[1][i])+SEPA;
		}
		str = str + "\r\n" + "    comp product" + SEPA;
		for (int i = 0; i < 8; i++){
			if(SummaryDBRecord.BDG[2][i]>0)
				str = str + " " + fiveDecE.format(SummaryDBRecord.BDG[2][i])+SEPA;
			else
				str = str + "  " + fiveDecE.format(SummaryDBRecord.BDG[2][i])+SEPA;
		}
		str = str +"\r\n"; 
		str = str +"END SUMMARY \r\n"; 
		return str;
	}


	public static String exoprtiRoeMetzSet(String oldReport, SizePanel sizePanelRoeMetz) {
		// TODO Auto-generated method stub
		String str = oldReport;
		str = str + "\r\n*******************iRoeMetz parameter************************\r\n";
		str = str + "u_A: " + RMGUInterface.mu0.getText() + ",     ";
		str = str + "u_B: " + RMGUInterface.mu1.getText() + "\n";
		
        str = str +"Input Variances Invariant to Modality: " +"\n";
		str = str + "R0: " + RMGUInterface.v_R0.getText() + ",    ";
		str = str + "C0: " + RMGUInterface.v_C0.getText() + ",    ";
		str = str + "RC0: " + RMGUInterface.v_RC0.getText() + ",    ";
		str = str + "R1: " + RMGUInterface.v_R1.getText() + ",    ";
		str = str + "C1: " + RMGUInterface.v_C1.getText() + ",    ";
		str = str + "RC1: " + RMGUInterface.v_RC1.getText() + "\n";
		
		str = str +"Input Variances Specific to Modality A: " +"\n";
		str = str + "AR0: " + RMGUInterface.v_AR0.getText() + ",   ";
		str = str + "AC0: " + RMGUInterface.v_AC0.getText() + ",   ";
		str = str + "ARC0: " + RMGUInterface.v_ARC0.getText() + ",   ";
		str = str + "AR1: " + RMGUInterface.v_AR1.getText() + ",   ";
		str = str + "AC1: " + RMGUInterface.v_AC1.getText() + ",   ";
		str = str + "ARC1: " + RMGUInterface.v_ARC1.getText() + "\n";
			
		str = str +"Input Variances Specific to Modality B: " +"\n";
		str = str + "BR0: " + RMGUInterface.v_BR0.getText() + ",   ";
		str = str + "BC0: " + RMGUInterface.v_BC0.getText() + ",   ";
		str = str + "BRC0: " + RMGUInterface.v_BRC0.getText() + ",   ";
		str = str + "BR1 " + RMGUInterface.v_BR1.getText() + ",   ";
		str = str + "BC1: " + RMGUInterface.v_BC1.getText() + ",   ";
		str = str + "BRC1: " + RMGUInterface.v_BRC1.getText() + "\n";
		
		str = str +"Input Experiment Size: " +"\n";
		str = str + "N0: " + RMGUInterface.NnormalJTextField.getText() + ",   ";
		str = str + "N1: " + RMGUInterface.NdiseaseJTextField.getText() + ",   ";
		str = str + "NR: " + RMGUInterface.NreaderJTextField.getText() + "\n";
		
		str = str +"Study Design: " +"\n";
		str = str +	"# of Split-Plot Groups: " + sizePanelRoeMetz.numSplitPlots + ",  ";
		if (sizePanelRoeMetz.pairedReadersFlag == 1)
			str = str +	"Paired Readers: Yes,  ";
		else
			str = str +	"Paired Readers: No,  ";
		if (sizePanelRoeMetz.pairedNormalsFlag == 1)
			str = str +	"Paired Normal: Yes,  ";
		else
			str = str +	"Paired Normal: No,  ";
		if (sizePanelRoeMetz.pairedDiseasedFlag == 1)
			str = str +	"Paired Diesase: Yes \n";
		else
			str = str +	"Paired Diesase: No \n";
		return str;
	}
}
