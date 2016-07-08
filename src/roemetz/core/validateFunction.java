package roemetz.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import mrmc.chart.exportToFile;
import roemetz.gui.RMGUInterface;
import roemetz.gui.RMGUInterface.analysisExportListener;

public class validateFunction {
public static File inputFile = null;
	public static void validateFunction(RMGUInterface RMGUInterface1, String inputFileFullName) {
		// TODO Auto-generated method stub
		if (inputFileFullName.length()==0){
			System.out.println("Input file full name:");		
			Scanner inputScanner = new Scanner(System.in);
			inputFileFullName = inputScanner.nextLine();
		}
		System.out.println(inputFileFullName);
		inputFile = new File (inputFileFullName);
		RMGUInterface1.parseCofVfile(inputFile);
		RoeMetz.doValidation = true;

				// numerical
				RMGUInterface.DoNumericalIntegrationBtnListener DoNumericalIntegrationBtnListener1 =  
						RMGUInterface1.new DoNumericalIntegrationBtnListener();
				DoNumericalIntegrationBtnListener1.doNumericalAnalysisSEQ();
				RMGUInterface.analysisExportListener analysisExportListener2 =
						RMGUInterface1. new analysisExportListener(DoNumericalIntegrationBtnListener1.DBRecordNumerical,"Numerical",RMGUInterface1.StatPanelNumerical);
				analysisExportListener2.exportResult();
				
				// simulation
				RMGUInterface.DoSimBtnListener DoSimBtnListener1 =  RMGUInterface1.new DoSimBtnListener();
				DoSimBtnListener1.doSimulationAnalysis();
			/*	while(!RMGUInterface1.processDone){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				RMGUInterface.analysisExportListener analysisExportListener1 =RMGUInterface1. new analysisExportListener(RMGUInterface1.avgDBRecordStat,"SimulationOutput",RMGUInterface1.StatPanel1);
				analysisExportListener1.exportResult();*/
	}
}
