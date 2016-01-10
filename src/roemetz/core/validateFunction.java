package roemetz.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import mrmc.chart.exportToFile;
import roemetz.gui.RMGUInterface;
import roemetz.gui.RMGUInterface.analysisExportListener;

public class validateFunction {

	public static void validateFunction(RMGUInterface RMGUInterface1) {
		// TODO Auto-generated method stub
		
		System.out.println("Input file full name:");
		Scanner inputScanner = new Scanner(System.in);
		String inputFileFullName = inputScanner.nextLine();
		System.out.println(inputFileFullName);
		File inputFile = new File (inputFileFullName);
		RMGUInterface1.parseCofVfile(inputFile);
		RoeMetz.doValidation = true;
		String[] readerNumberList = {"6","7","8","9","10","11","12"};
		//String[] readerNumberList = {"7"};
		String[] SplitPlotNumberList = {"1","2","3"};
		for (int i=0; i<readerNumberList.length;i++){
			RMGUInterface1.NreaderJTextField.setText(readerNumberList[i]);
			for (int j=0; j<SplitPlotNumberList.length;j++){
				// simulation
				RMGUInterface1.SizePanelRoeMetz.NumSplitPlotsJTextField.setText(SplitPlotNumberList[j]);
				RMGUInterface1.SizePanelRoeMetz.numSplitPlots = Integer.parseInt(SplitPlotNumberList[j]);
				RMGUInterface.DoSimBtnListener DoSimBtnListener1 =  RMGUInterface1.new DoSimBtnListener();
				RMGUInterface1.processDone = false;
				DoSimBtnListener1.doSimulationAnalysis();
				while(!RMGUInterface1.processDone){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				RMGUInterface.analysisExportListener analysisExportListener1 =RMGUInterface1. new analysisExportListener(RMGUInterface1.avgDBRecordStat,"SimulationOutput",RMGUInterface1.StatPanel1);
				analysisExportListener1.exportResult();
				
				// numerical
				RMGUInterface.DoNumericalIntegrationBtnListener DoNumericalIntegrationBtnListener1 =  
						RMGUInterface1.new DoNumericalIntegrationBtnListener();
				RMGUInterface1.processDone = false;
				DoNumericalIntegrationBtnListener1.doSimulationAnalysis();
				while(!RMGUInterface1.processDone){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				RMGUInterface.analysisExportListener analysisExportListener2 =
						RMGUInterface1. new analysisExportListener(DoNumericalIntegrationBtnListener1.DBRecordNumerical,"NumericalOutput",RMGUInterface1.StatPanelNumerical);
				analysisExportListener2.exportResult();
				
			}
		}

	}
}
