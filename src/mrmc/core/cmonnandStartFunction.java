package mrmc.core;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFrame;

import mrmc.gui.GUInterface;
import roemetz.core.RoeMetz;
import roemetz.gui.RMGUInterface;
import roemetz.gui.RMGUInterface.DoNumericalIntegrationBtnListener;
import roemetz.gui.RMGUInterface.DoSimBtnListener;
import roemetz.gui.RMGUInterface.analysisExportListener;

public class cmonnandStartFunction {
	private GUInterface GUI;
	private InputFile InputFile1;
	public void cmonnandStartFunction(GUInterface gui, String inputFileFullName) {
		// TODO Auto-generated method stub
		
		if (inputFileFullName.length()==0){
			System.out.println("Input file full name:");		
			Scanner inputScanner = new Scanner(System.in);
			inputFileFullName = inputScanner.nextLine();
		}
		System.out.println(inputFileFullName);
		String inputFormat = inputFileFullName.substring(inputFileFullName.lastIndexOf(".")+1);
		if (inputFormat.equals("imrmc")){
			GUInterface.selectedInput = GUInterface.DescInputModeImrmc;
		}else if(inputFormat.equals("omrmc")){
			GUInterface.selectedInput = GUInterface.DescInputModeOmrmc;
		}else{
			System.out.println("please input a valid input file");
			return;
		}
		GUI = gui;
		InputFile1 = GUI.InputFile1;
		InputFile1.filename = inputFileFullName;	
		
		try {
			InputFile1.ReadInputFile(GUI);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MRMC.commandStart = true;
		GUInterface.SaveAllStatListener ExportAllListener1 = GUI. new SaveAllStatListener();
		ExportAllListener1.exportResult();
	}
}
