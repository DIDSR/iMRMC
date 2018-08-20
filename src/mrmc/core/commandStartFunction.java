package mrmc.core;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import mrmc.gui.GUInterface;
import roemetz.core.RoeMetz;
import roemetz.gui.RMGUInterface;
import roemetz.gui.RMGUInterface.DoNumericalIntegrationBtnListener;
import roemetz.gui.RMGUInterface.DoSimBtnListener;
import roemetz.gui.RMGUInterface.analysisExportListener;

public class commandStartFunction {
	private GUInterface GUI;
	private InputFile InputFile1;
	public void commandStartFunction(GUInterface gui, String inputFileFullName, String outputFolderFullName) {
		// TODO Auto-generated method stub
		String inputAndOutput = "";
		if (inputFileFullName.length()==0){
			System.out.println("Input file full name:");		
			Scanner inputScanner = new Scanner(System.in);
			inputAndOutput = inputScanner.nextLine();
			if (inputAndOutput.lastIndexOf(" ")==-1)
				inputFileFullName = inputAndOutput;
			else{
				inputFileFullName = inputAndOutput.substring(0,inputAndOutput.lastIndexOf(" ")).trim();
				outputFolderFullName = inputAndOutput.substring(inputAndOutput.lastIndexOf(" ")+1).trim();
			}
		}
		String inputFormat = inputFileFullName.substring(inputFileFullName.lastIndexOf(".")+1);
		if (inputFormat.equals("imrmc")){
			GUInterface.selectedInput = GUInterface.DescInputModeImrmc;
		}else if(inputFormat.equals("omrmc")){
			GUInterface.selectedInput = GUInterface.DescInputModeOmrmc;
		}else{
			System.out.println("please input a valid input file");
			System.exit(0);
		}
		GUI = gui;
		InputFile1 = GUI.InputFile1;
		File f = new File(inputFileFullName);
		InputFile1.fileName = f.getName();
		InputFile1.filePath = f.getParent();
	    InputFile1.filePathAndName = f.getPath();
	    MRMC.commandStart = true;
		try {
			InputFile1.ReadInputFile(GUI);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		GUI.allAnalysisOutput = outputFolderFullName;
		GUInterface.SaveAllStatListener ExportAllListener1 = GUI. new SaveAllStatListener();
		ExportAllListener1.exportResult();
	}
	public static void displayConsoleError(JFrame frame, String errorStr){
		if(!MRMC.commandStart){
			System.out.println("Error: "+ errorStr);
			JOptionPane.showMessageDialog(frame,
					errorStr, "Error",
					JOptionPane.ERROR_MESSAGE);
		}else{
			System.out.println("Error: "+ errorStr);
			System.exit(0);
		}

	}
	public static void displayConsoleWarning(JFrame frame, String warningStr){
		if(!MRMC.commandStart){
			System.out.println("Warning: "+ warningStr);
			JOptionPane.showMessageDialog(frame,
					warningStr, "Warning",
					JOptionPane.ERROR_MESSAGE);
		}else{
			System.out.println("Warning: "+ warningStr);
		}
	}
}
