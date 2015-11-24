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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import mrmc.chart.BarGraph;
import mrmc.chart.ROCCurvePlot;
import mrmc.chart.StudyDesignPlot;


import mrmc.core.DBRecord;
import mrmc.core.InputFile;
import mrmc.core.StatTest;

import org.jfree.ui.RefineryUtilities;



/**
 * Panel for selecting modalities, displaying charts of data statistics, and
 * performing MRMC variance analysis with data from pilot study raw data files.
 * 
 * 
 * @author Xin He, Ph.D
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
public class InputFileCard {
	private GUInterface GUI;
	private InputFile InputFile1;
	private DBRecord DBRecordStat;
	private DBRecord DBRecordSize;
	JTextField JTextFilename;
	public final static int USE_MLE = 1;
	public final static int NO_MLE = 0;
	public int FlagMLE = NO_MLE;
	private JCheckBox mleCheckBox;
	private JComboBox<String> chooseA, chooseB;
	private JButton varAnalysisButton, showAUCsButton;

	/**
	 * Sets study panel to default values, removes modalities from drop down
	 * menus
	 */
	public void resetInputFileCard() {
		
		JTextFilename.setText("");
		FlagMLE = NO_MLE;
		mleCheckBox.setSelected(false);
		chooseA.removeAllItems();
		chooseB.removeAllItems();
		chooseA.addItem("Choose Modality A");
		chooseB.addItem("Choose Modality B");
		
	}

	/**
	 * Sole constructor. Creates and initializes GUI elements <br>
	 * <br>
	 * CALLED BY: {@link mrmc.gui.GUInterface#GUInterface GUInterface constructor}
	 * 
	 * @param CardInputModeImrmc Panel containing elements for raw study input card
	 * @param GUInterface_temp Application's instance of the GUI
	 */
	public InputFileCard(JPanel CardInputModeImrmc, GUInterface GUInterface_temp) {
		GUI = GUInterface_temp;
		InputFile1 = GUI.InputFile1;
		DBRecordStat = GUI.DBRecordStat;
		DBRecordSize = GUI.DBRecordSize;

		/*
		 * Elements of RawStudyCardRow1
		 */
		// Browse for input file
		JLabel studyLabel = new JLabel(".imrmc or .csv file  ");
		JTextFilename = new JTextField(20);
		JButton browseButton = new JButton("Browse...");
		browseButton.addActionListener(new brwsButtonListener());
		// Show plots of Cases Per Reader and Readers Per Case
		JButton readerCasesButton = new JButton("Input Statistics Charts");
		readerCasesButton.addActionListener(new ReadersCasesButtonListener());
		// Show reader x case image of design matrix for selected modality 
		JButton designButton = new JButton("Show Study Design");
		designButton.addActionListener(new designButtonListener());
		// Show ROC curves for selected modality
		JButton ROCcurveButton = new JButton("Show ROC Curve");
		ROCcurveButton.addActionListener(new ROCButtonListener());
		/*
		 * Create RawStudyCardRow2
		 */
		JPanel RawStudyCardRow1 = new JPanel();
		RawStudyCardRow1.add(studyLabel);
		RawStudyCardRow1.add(JTextFilename);
		RawStudyCardRow1.add(browseButton);
		RawStudyCardRow1.add(readerCasesButton);
		RawStudyCardRow1.add(designButton);
		RawStudyCardRow1.add(ROCcurveButton);

		/*
		 * Elements of RawStudyCardRow2
		 */
		// MLE Checkbox
		mleCheckBox = new JCheckBox("MLE (avoid negatives)");
		mleCheckBox.setSelected(false);
		mleCheckBox.addItemListener(new UseMLEListener());
		// Drop down menus to select modality
		chooseA = new JComboBox<String>();
		chooseB = new JComboBox<String>();
		chooseA.addItemListener(new ModalitySelectListener());
		chooseB.addItemListener(new ModalitySelectListener());
		// execute variance analysis button
		varAnalysisButton = new JButton("MRMC Variance Analysis");
		varAnalysisButton.addActionListener(new varAnalysisListener());
		// show the reader AUCs
		showAUCsButton = new JButton("Show Reader AUCs");
		showAUCsButton.addActionListener(new showAUCsButtonListener());
		/*
		 * Create RawStudyCardRow2
		 */
		JPanel RawStudyCardRow2 = new JPanel();
		RawStudyCardRow2.add(mleCheckBox);
		RawStudyCardRow2.add(chooseA);
		RawStudyCardRow2.add(chooseB);
		RawStudyCardRow2.add(varAnalysisButton);
		RawStudyCardRow2.add(showAUCsButton);

		/*
		 * Initialize InputFileCard
		 */
		resetInputFileCard();

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
					"iMRMC Input Files (.imrmc or csv)", "csv","imrmc");
		
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
//			GUI.inputfileDirectory = f.getPath();
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
						"NR = " + InputFile1.Nreader + 
						" N0 = "+ InputFile1.Nnormal +
						" N1 = "+ InputFile1.Ndisease +
						" NM = "+ InputFile1.Nmodality, 
						"Study Info", JOptionPane.INFORMATION_MESSAGE);
			}

			/* 
			 * Initialze modality pulldown menus
			 */
			for (String ModalityID : InputFile1.getModalityIDs()) {
				chooseA.addItem(ModalityID);
				chooseB.addItem(ModalityID);
			}
			DBRecordStat.modalityA = GUInterface.NO_MOD;
			DBRecordStat.modalityB = GUInterface.NO_MOD;

		}
	}
	
	
	/**
	 * Handler for "Input Statistics Charts" button, displays charts for study
	 * design at a glance
	 */
	class ReadersCasesButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// System.out.println("graph button pressed");
			if (InputFile1 != null && InputFile1.isLoaded()) {
				final BarGraph cpr = new BarGraph("Cases per Reader",
						"Readers", "Cases", InputFile1.casesPerReader());
				cpr.pack();
				RefineryUtilities.centerFrameOnScreen(cpr);
				cpr.setVisible(true);

				final BarGraph rpc = new BarGraph("Readers per Case", "Cases",
						"Readers", InputFile1.readersPerCase());
				rpc.pack();
				RefineryUtilities.centerFrameOnScreen(rpc);
				RefineryUtilities.positionFrameOnScreen(rpc, 0.6, 0.6);
				rpc.setVisible(true);
			} else {
				JOptionPane.showMessageDialog(GUI.MRMCobject.getFrame(),
						"Pilot study data has not yet been input.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Handler for "Show Study Design" button, displays charts of more detailed
	 * study design for a given modality
	 */
	class designButtonListener implements ActionListener {
		String designMod1 = "empty";

		public void actionPerformed(ActionEvent e) {
			// System.out.println("study design button pressed");
			if (InputFile1.isLoaded()) {
				JComboBox<String> choose1 = new JComboBox<String>();

				for (String Modality : InputFile1.getModalityIDs()){
					choose1.addItem(Modality);
				}
				choose1.setSelectedIndex(0);
				Object[] message = { "Which modality would you like view?\n",
						choose1 };
				JOptionPane.showMessageDialog(GUI.MRMCobject.getFrame(), message,
						"Choose Modality", JOptionPane.INFORMATION_MESSAGE,
						null);
				designMod1 = (String) choose1.getSelectedItem();
				TreeMap<String,String[][]> StudyDesignData = InputFile1.getStudyDesign( (String) choose1.getSelectedItem());
				final StudyDesignPlot chart = new StudyDesignPlot(
						"Study Design: Modality "+designMod1, designMod1, "Case Index",
						"Reader", StudyDesignData,InputFile1.filename);
				chart.pack();
				RefineryUtilities.centerFrameOnScreen(chart);
				chart.setVisible(true);

			} else {
				JOptionPane.showMessageDialog(GUI.MRMCobject.getFrame(),
						"Pilot study data has not yet been input.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Handler for "Show ROC Curve" button, displays interactive ROC charts
	 */
	class ROCButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// If input file is loaded, then show ROC curves
			// Otherwise as for pilot study to be inpu
			if (InputFile1.isLoaded()) {
				JPanel panel = new JPanel();
				int modalitynum = InputFile1.getModalityIDs().size();
				JCheckBox[] jCheckboxArray = new javax.swing.JCheckBox[modalitynum];
				for (int i = 0; i < modalitynum; i++) {
					String modID=InputFile1.getModalityIDs().get(i);
					jCheckboxArray[i] = new JCheckBox("" + modID);
					panel.add(jCheckboxArray[i]);
					}
				Object[] message = { "Which modality would you like view?\n",
						panel };
				JOptionPane.showMessageDialog(GUI.MRMCobject.getFrame(), message,
						"Choose Modality",
						JOptionPane.INFORMATION_MESSAGE, null);
				int checkedmod=0;
				for (int i = 0; i < modalitynum; i++) {
					if (jCheckboxArray[i].isSelected()){
						checkedmod++;
					}
				}
				String[] rocMod= new String[checkedmod];
				String roctitle="";
				int selectmod=0;
				for (int i = 0; i < modalitynum; i++) {
					if (jCheckboxArray[i].isSelected()){
					 String modID=InputFile1.getModalityIDs().get(i);
					 rocMod[selectmod]=modID;
					 roctitle=roctitle+modID+" ";
					 selectmod++;
					}
				}
				if (selectmod>0){
					final ROCCurvePlot roc = new ROCCurvePlot(
							"ROC Curve: Modality " + roctitle,
							"FPF (1 - Specificity), legend shows symbols for each modalityID:readerID", "TPF (Sensitivity)",
							InputFile1.generateROCpoints(rocMod),InputFile1.filename);
					roc.addData(InputFile1.generatePooledROC(rocMod), "Pooled Average");
					roc.pack();
					RefineryUtilities.centerFrameOnScreen(roc);
					roc.setVisible(true);
				}else{
					JOptionPane.showMessageDialog(GUI.MRMCobject.getFrame(),
							"Please choose at list one Modality.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(GUI.MRMCobject.getFrame(),
						"Pilot study data has not yet been input.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Handler for checkbox to
	 * "Use MLE estimates of moments to avoid negatives". Sets whether bias
	 * should be used when performing variance analysis
	 */
	class UseMLEListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (mleCheckBox.isSelected()) {
				FlagMLE = USE_MLE;
			} else {
				FlagMLE = NO_MLE;
			}
			DBRecordStat.flagMLE = FlagMLE;
			DBRecordSize.flagMLE = FlagMLE;
			GUI.StatPanel1.resetStatPanel();
			GUI.StatPanel1.resetTable1();
			GUI.SizePanel1.resetSizePanel();
			
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
			if (chooseA.getSelectedItem() == null
				|| chooseB.getSelectedItem() == null) { return; }
			
			GUI.StatPanel1.resetStatPanel();
			GUI.StatPanel1.resetTable1();
			GUI.SizePanel1.resetSizePanel();
			
			boolean modA, modB;
			modA = !chooseA.getSelectedItem().equals("Choose Modality A");
			modB = !chooseB.getSelectedItem().equals("Choose Modality B");
			
			if (modA && !modB) {
				DBRecordStat.selectedMod = 0;
				DBRecordStat.modalityA = (String) chooseA.getSelectedItem();
				DBRecordStat.modalityB = GUInterface.NO_MOD;
				varAnalysisButton.setText("MRMC Variance Analysis (A)");
			} else if (!modA && modB) {
				DBRecordStat.selectedMod = 1;
				DBRecordStat.modalityA = GUInterface.NO_MOD;
				DBRecordStat.modalityB = (String) chooseB.getSelectedItem();
				varAnalysisButton.setText("MRMC Variance Analysis (B)");
			} else if (modA && modB) {
				DBRecordStat.selectedMod = 3;
				DBRecordStat.modalityA = (String) chooseA.getSelectedItem();
				DBRecordStat.modalityB = (String) chooseB.getSelectedItem();
				varAnalysisButton.setText("MRMC Variance Analysis (Difference)");
			} else {
				varAnalysisButton.setText("MRMC Variance Analysis");
				DBRecordStat.modalityA = GUInterface.NO_MOD;
				DBRecordStat.modalityB = GUInterface.NO_MOD;
				return;
			}

			DBRecordSize.selectedMod = DBRecordStat.selectedMod;
			DBRecordSize.modalityA = DBRecordStat.modalityA;
			DBRecordSize.modalityB = DBRecordStat.modalityB;

		} // method
	} // class
	
	/**
	 * Handler for MRMC variance analysis button <br>
	 * -- Verifies that input is valid <br>
	 * -- {@link mrmc.core.DBRecord#DBRecordStatFill(InputFile, DBRecord)} <br>
	 * -- {@link mrmc.gui.StatPanel#setStatPanel()} <br>
	 * -- {@link mrmc.gui.StatPanel#setTable1()}	 
	 */
	class varAnalysisListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("MRMC Variance analysis button clicked. RawStudyCard.varAnalysisListener");
			// Check that .imrmc input file has been read
			// If there is no JTextFilename, then reader scores have not been read
			String name = JTextFilename.getText();
			System.out.println("name=" + name);
			if (name.equals(null) || name.equals("")) {
				JFrame frame = GUI.MRMCobject.getFrame();
				JOptionPane.showMessageDialog(frame, 
						"Please browse for .imrmc or.csv input file", " Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// Check that a modality has been selected
			if (DBRecordStat.modalityA == GUInterface.NO_MOD && DBRecordStat.modalityB == GUInterface.NO_MOD) {
				JFrame frame = GUI.MRMCobject.getFrame();
				JOptionPane.showMessageDialog(frame,
						"You must select at least one modality", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// Don't allow both modalities to be the same
			if(DBRecordStat.modalityA.compareTo(DBRecordStat.modalityB) == 0) {
				JFrame frame = GUI.MRMCobject.getFrame();
				JOptionPane.showMessageDialog(frame, 
						"Modalities must be different", " Error",
							JOptionPane.ERROR_MESSAGE);
				return;
			}
			// Analyze observerData
			DBRecordStat.DBRecordStatFill(InputFile1, DBRecordStat);
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
					mleCheckBox.setSelected(true);
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
	
	/**
	 * Creates a table showing individual reader AUCs
	 * 
	 */
	class showAUCsButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if( DBRecordStat.totalVar > 0.0) {
			JFrame JFrameAUC= new JFrame("AUCs for each reader and modality");
			RefineryUtilities.centerFrameOnScreen(JFrameAUC);
			JFrameAUC.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			Object[] colNames = { "ReaderID", "AUC "+DBRecordStat.modalityA, "AUC "+DBRecordStat.modalityB };
			Object[][] rowContent = new String[(int) DBRecordStat.Nreader][3];
			int i=0;
			for(String desc_temp : InputFile1.readerIDs.keySet() ) {
				rowContent[i][0] = desc_temp;
				rowContent[i][1] = Double.toString(DBRecordStat.AUCs[i][0]);
				rowContent[i][2] = Double.toString(DBRecordStat.AUCs[i][1]);
				i++;
			}

			JTable tableAUC = new JTable(rowContent, colNames);
			JScrollPane scrollPaneAUC = new JScrollPane(tableAUC);
			JFrameAUC.add(scrollPaneAUC, BorderLayout.CENTER);
			JFrameAUC.setSize(600, 300);
			JFrameAUC.setVisible(true);
			}
			else {
				JOptionPane.showMessageDialog(GUI.MRMCobject.getFrame(),
						"Pilot study data has not yet been analyzed.", "Error",
						JOptionPane.ERROR_MESSAGE);

			}
		}
	}
}
