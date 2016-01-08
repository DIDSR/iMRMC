package roemetz.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import mrmc.chart.exportToFile;
import roemetz.gui.RMGUInterface;

public class validateFunction {

	public static void validateFunction(RMGUInterface RMGUInterface1) {
		// TODO Auto-generated method stub
		System.out.println("Input file full name:");
		Scanner inputScanner = new Scanner(System.in);
		String inputFileFullName = inputScanner.nextLine();
		System.out.println(inputFileFullName);
		File inputFile = new File (inputFileFullName);
		RMGUInterface1.parseCofVfile(inputFile);
		RMGUInterface.DoSimBtnListener DoSimBtnListener1 =  RMGUInterface1.new DoSimBtnListener();
		DoSimBtnListener1.doSimulationAnalysis();
		RMGUInterface.analysisExportListener analysisExportListener1 = RMGUInterface1.new analysisExportListener(RMGUInterface1.avgDBRecordStat,"SimulationOutput",RMGUInterface1.StatPanel1);
	//	RMGUInterface1.createProgressBar();
	//	RMGUInterface1.simulationExport.doClick();

	//	analysisExportListener1.exportResult();
		
	}
}
