/**
 * RawStudyCard.java
 * 
 * This software and documentation (the "Software") were developed at the Food
 * and Drug Administration (FDA) by employees of the Federal Government in the
 * course of their official duties. Pursuant to Title 17, Section 105 of the
 * United States Code, this work is not subject to copyright protection and is
 * in the public domain. Permission is hereby granted, free of charge, to any
 * person obtaining a copy of the Software, to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, or sell copies of the Software or
 * derivatives, and to permit persons to whom the Software is furnished to do
 * so. FDA assumes no responsibility whatsoever for use by other parties of the
 * Software, its source code, documentation or compiled executables, and makes
 * no guarantees, expressed or implied, about its quality, reliability, or any
 * other characteristic. Further, use of this code in no way implies endorsement
 * by the FDA or confers any advantage in regulatory decisions. Although this
 * software can be redistributed and/or modified freely, we ask that any
 * derivative works bear some notice that they are derived from it, and any
 * modified versions bear some notice that they have been modified.
 */

package mrmc.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;



import mrmc.core.DBRecord;
import mrmc.core.InputFile;
import mrmc.core.StatTest;



/**
 * Panel for selecting modalities, displaying charts of data statistics, and
 * performing MRMC variance analysis with data from pilot study raw data files.
 * 
 * 
 * @author Xin He, Ph.D
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
public class InputSummaryCard {
	private GUInterface GUI;
	private InputFile InputFile1;
	private DBRecord DBRecordStat;
	private DBRecord DBRecordSize;
 
	JTextField JTextFilename;
	public final static int USE_MLE = 1;
	public final static int NO_MLE = 0;
	public int FlagMLE = NO_MLE;
	private JCheckBox mleCheckBoxSummary;
	private JComboBox<String> chooseA;
	private String loadmodalityA;
	private String loadmodalityB;

	/**
	 * Sets study panel to default values, removes modalities from drop down
	 * menus
	 */
	public void resetInputSummaryCard() {
		JTextFilename.setText("");
		FlagMLE = NO_MLE;
		mleCheckBoxSummary.setSelected(false);
		chooseA.removeAllItems();
		chooseA.addItem("Choose Modality");
	}




	/**
	 * Sole constructor. Creates and initializes GUI elements <br>
	 * <br>
	 * CALLED BY: {@link mrmc.gui.GUInterface#GUInterface GUInterface constructor}
	 * 
	 * @param CardInputModeImrmc Panel containing elements for raw study input card
	 * @param GUInterface_temp Application's instance of the GUI
	 */
	public InputSummaryCard(JPanel CardInputModeImrmc, GUInterface GUInterface_temp) {
		GUI = GUInterface_temp;
		InputFile1 = GUI.InputFile1;
		DBRecordStat = GUI.DBRecordStat;
		DBRecordSize = GUI.DBRecordSize;

		/*
		 * Elements of RawStudyCardRow1
		 */
		// Browse for input file
		JLabel studyLabel = new JLabel(".imrmc, .omrmc or. csv file  ");
		JTextFilename = new JTextField(20);
		JButton browseButton = new JButton("Browse...");
		browseButton.addActionListener(new brwsButtonListener());
		/*
		 * Create RawStudyCardRow2
		 */
		JPanel RawStudyCardRow1 = new JPanel();
		RawStudyCardRow1.add(studyLabel);
		RawStudyCardRow1.add(JTextFilename);
		RawStudyCardRow1.add(browseButton);

		/*
		 * Elements of RawStudyCardRow2
		 */
		// MLE Checkbox
		mleCheckBoxSummary = new JCheckBox("MLE (avoid negatives)");
		mleCheckBoxSummary.setSelected(false);
		mleCheckBoxSummary.addItemListener(new UseMLEListener());
		// Drop down menus to select modality
		chooseA = new JComboBox<String>();
		chooseA.addItemListener(new ModalitySelectListener());
		/*
		 * Create RawStudyCardRow2
		 */
		JPanel RawStudyCardRow2 = new JPanel();
		RawStudyCardRow2.add(mleCheckBoxSummary);
		RawStudyCardRow2.add(chooseA);
		/*
		 * Initialize InputSummaryCard
		 */
		resetInputSummaryCard();

		/*
		 * Create the layout of the card
		 */
		GroupLayout layout = new GroupLayout(CardInputModeImrmc);
		CardInputModeImrmc.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING) // Parallel=Vertical?
					.addGroup(layout.createSequentialGroup()
							.addComponent(RawStudyCardRow1)) // Sequential=Horizontal
					.addGroup(layout.createSequentialGroup()
							.addComponent(RawStudyCardRow2)))); // Sequential=Horizontal

		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE) // Parallel=Horizontal?
				.addComponent(RawStudyCardRow1))
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING) // Parallel=Horizontal?
				.addComponent(RawStudyCardRow2)));
		
	}
	

	


	/**
	 * Handler for button to browse for raw study data input file. Resets the GUI. Displays a
	 * file chooser, creates instance of {@link mrmc.core.InputFile}, 
	 * reads .imrmc input file {@link mrmc.core.InputFile#ReadInputFile()}
	 */
	class brwsButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			GUI.resetGUI();
			if  (GUInterface.selectedInput == GUInterface.DescInputChooseMode){
				JOptionPane.showMessageDialog(GUI.MRMCobject.getFrame(),
						"Please choose one kind of input file.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"iMRMC Input Files (.imrmc, omrmc or csv)", "imrmc","omrmc","csv");
			
			if (GUI.inputfileDirectory!=null)
				fc.setCurrentDirectory(GUI.inputfileDirectory);
			
			fc.setFileFilter(filter);
			int returnVal = fc.showOpenDialog((Component) e.getSource());
			if( returnVal==JFileChooser.CANCEL_OPTION || returnVal==JFileChooser.ERROR_OPTION) return;
			GUI.inputfileDirectory = fc.getCurrentDirectory(); //save last time visit directory
			/*
			 *  Get a pointer to the input file and the filename
			 */
			File f = fc.getSelectedFile();
			if( f==null ) return;
			InputFile1.filename = f.getPath();
			JTextFilename.setText(f.getPath());

			/*
			 *  Read the .imrmc input file, check for exceptions
			 */				
			try {
				InputFile1.ReadInputFile(GUI);
			} catch (IOException except) {
				except.printStackTrace();
				JOptionPane.showMessageDialog
						(GUI.MRMCobject.getFrame(),
						except.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				GUI.resetGUI();
				JTextFilename.setText("");
				return;
			}
			
			/*
			 * Compare the experimental design as determined from fields in the header vs. the data
			 */
			if (!InputFile1.dataCheckResults.isEmpty()) {
				JOptionPane.showMessageDialog(
						GUI.MRMCobject.getFrame(),
						InputFile1.dataCheckResults,
						"Warning: Input Header Values Do Not Match Actual Values",
						JOptionPane.WARNING_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(
						GUI.MRMCobject.getFrame(),
						"NR = " + DBRecordStat.NreaderDB + 
						" N0 = "+ DBRecordStat.NnormalDB +
						" N1 = "+ DBRecordStat.NdiseaseDB, 
						"Summary Info", JOptionPane.INFORMATION_MESSAGE);
			}

			/* 
			 * Initialze modality pulldown menus
			 */

			if (DBRecordStat.inputMod == 0)	{
				chooseA.addItem("A:" + DBRecordStat.modalityA);
				loadmodalityA = DBRecordStat.modalityA;
			}else if(DBRecordStat.inputMod == 1){
				chooseA.addItem("B:" + DBRecordStat.modalityB);
				loadmodalityB = DBRecordStat.modalityB; 
			}else{
				loadmodalityA = DBRecordStat.modalityA;
				loadmodalityB = DBRecordStat.modalityB; 
				chooseA.addItem("A:" + DBRecordStat.modalityA);
				chooseA.addItem("B:" + DBRecordStat.modalityB);
				chooseA.addItem("A vs B:" + DBRecordStat.modalityA + " vs " + DBRecordStat.modalityB);
			}
			
			DBRecordStat.modalityA = GUInterface.NO_MOD;
			DBRecordStat.modalityB = GUInterface.NO_MOD;

		}
	}
	
	
	


	/**
	 * Handler for checkbox to
	 * "Use MLE estimates of moments to avoid negatives". Sets whether bias
	 * should be used when performing variance analysis
	 */
     class UseMLEListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (mleCheckBoxSummary.isSelected()) {
				FlagMLE = USE_MLE;
			} else {
				FlagMLE = NO_MLE;
			}
			DBRecordStat.flagMLE = FlagMLE;
			DBRecordSize.flagMLE = FlagMLE;
			GUI.StatPanel1.resetStatPanel();
			GUI.StatPanel1.resetTable1();
			GUI.SizePanel1.resetSizePanel();
			if (GUI.resetcall == 0)
			varianceAnalysis();
		}
	}

	/**
	 * Handler for drop down menus to select Modality A and Modality B. Changes
	 * variance analysis button text to reflect which type of analysis is being
	 * performed. <br>
	 * ----Creates ({@link mrmc.core.DBRecord}) <br>
	 */
	class ModalitySelectListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {

			if (e.getStateChange() != ItemEvent.DESELECTED) { return; }
			if (chooseA.getSelectedItem() == null) { return; }
			
			GUI.StatPanel1.resetStatPanel();
			GUI.StatPanel1.resetTable1();
			GUI.SizePanel1.resetSizePanel();
			
			boolean modA;
			modA = !chooseA.getSelectedItem().equals("Choose Modality");
			
			if (modA) {	
				DBRecordStat.modalityA = (String) chooseA.getSelectedItem();

			}  else {
				DBRecordStat.modalityA = GUInterface.NO_MOD;
			}
			if (DBRecordStat.modalityA.equals("A vs B:" + loadmodalityA + " vs " + loadmodalityB)) {
				DBRecordStat.modalityA =  loadmodalityA;
				DBRecordStat.modalityB =  loadmodalityB;
				DBRecordStat.selectedMod = 3;
				varianceAnalysis();
			} else if(DBRecordStat.modalityA.equals("A:" + loadmodalityA)){
				DBRecordStat.modalityA =  loadmodalityA;
				DBRecordStat.modalityB =  GUInterface.NO_MOD;
				DBRecordStat.selectedMod = 0;
				varianceAnalysis();
			} else if (DBRecordStat.modalityA.equals("B:" + loadmodalityB)) {
				DBRecordStat.modalityA =  GUInterface.NO_MOD;
				DBRecordStat.modalityB =  loadmodalityB;
				DBRecordStat.selectedMod = 1;
				varianceAnalysis();
			}
			
			DBRecordSize.selectedMod = DBRecordStat.selectedMod;
			DBRecordSize.modalityA = DBRecordStat.modalityA;

		} // method
	} // class
	
	/**
	 * Handler for MRMC variance analysis button <br>
	 * -- Verifies that input is valid <br>
	 * -- {@link mrmc.core.DBRecord#DBRecordStatFill(InputFile, DBRecord)} <br>
	 * -- {@link mrmc.gui.StatPanel#setStatPanel()} <br>
	 * -- {@link mrmc.gui.StatPanel#setTable1()}	 
	 */


	public void varianceAnalysis() {
		// TODO Auto-generated method stub
		System.out.println("MRMC Variance analysis button clicked. RawStudyCard.varAnalysisListener");
		// Check that .imrmc input file has been read
		// If there is no JTextFilename, then reader scores have not been read
		String name = JTextFilename.getText();
		System.out.println("name=" + name);
		if (name.equals(null) || name.equals("")) {
			JFrame frame = GUI.MRMCobject.getFrame();
			JOptionPane.showMessageDialog(frame, 
					"Please browse for .imrmc .omrmc or .csv input file", " Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Check that a modality has been selected
		if (DBRecordStat.modalityA == GUInterface.NO_MOD &&DBRecordStat.modalityB == GUInterface.NO_MOD) {
			JFrame frame = GUI.MRMCobject.getFrame();
			JOptionPane.showMessageDialog(frame,
					"You must select at least one modality", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Analyze observerData
		DBRecordStat.DBRecordStatFillSummary(DBRecordStat);
		// Check if variance estimate is negative
		if(DBRecordStat.totalVar > 0)
			GUI.hasNegative = false;
		else
			GUI.hasNegative = true;
				
		if (GUI.hasNegative && FlagMLE == NO_MLE) {
			JFrame frame = GUI.MRMCobject.getFrame();
			int result = JOptionPane.showConfirmDialog(frame,
				"The total variance estimate is negative.\n" +
				"Please report to the program developers. This is not expected.\n" +
				"Do you want to proceed with MLE estimates to avoid negatives?");
			if (JOptionPane.CANCEL_OPTION == result) {
				System.out.println("cancel");
			} else if (JOptionPane.YES_OPTION == result) {
				FlagMLE = USE_MLE;
				DBRecordStat.flagMLE = FlagMLE;
				mleCheckBoxSummary.setSelected(true);
				DBRecordStat.totalVar=DBRecordStat.totalVarMLE;
				DBRecordStat.testStat = new StatTest(DBRecordStat);
			} else if (JOptionPane.NO_OPTION == result) {
				FlagMLE = NO_MLE;
			}

		}

		// Update GUI
		DBRecordStat.flagMLE = FlagMLE;
		DBRecordSize.flagMLE = FlagMLE;

		GUI.StatPanel1.setStatPanel();
		GUI.StatPanel1.setTable1();
		DBRecordSize.Nreader = DBRecordStat.Nreader;
		DBRecordSize.Nnormal = DBRecordStat.Nnormal;
		DBRecordSize.Ndisease = DBRecordStat.Ndisease;
		GUI.SizePanel1.NreaderJTextField.setText(Long.toString(DBRecordStat.Nreader));
		GUI.SizePanel1.NnormalJTextField.setText(Long.toString(DBRecordStat.Nnormal));
		GUI.SizePanel1.NdiseaseJTextField.setText(Long.toString(DBRecordStat.Ndisease));
		
	}
	
}
