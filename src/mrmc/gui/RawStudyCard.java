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
public class RawStudyCard {
	private GUInterface gui;

	private int useBiasM = 0;
	private JCheckBox mleCheckBox;
	private JComboBox<String> chooseA, chooseB;
	private JButton varAnalysisButton, showAUCsButton;

	/**
	 * Sets whether bias is to be used when performing variance analysis
	 * 
	 * @param bias Whether bias is used
	 */
	public void setFlagMLE(int bias) {
		useBiasM = bias;
		if (bias == GUInterface.NO_MLE) {
			mleCheckBox.setSelected(false);
		} else {
			mleCheckBox.setSelected(true);
		}
	}

	/**
	 * Sets study panel to default values and updates menus to choose modality
	 * with values from current study file
	 */
	public void updateStudyPanel() {
		chooseA.removeAllItems();
		chooseB.removeAllItems();
		chooseA.addItem("Choose Modality A");
		chooseB.addItem("Choose Modality B");
		
		for (String ModalityID : gui.InputFile1.getModalityIDs()) {
			chooseA.addItem(ModalityID);
			chooseB.addItem(ModalityID);
		}
	}

	/**
	 * Sets study panel to default values, removes modalities from drop down
	 * menus
	 */
	public void resetModPanel() {
		chooseA.removeAllItems();
		chooseB.removeAllItems();
		chooseA.addItem("n/a");
		chooseB.addItem("n/a");
	}

	/**
	 * Sole constructor. Creates and initializes GUI elements <br>
	 * <br>
	 * CALLED BY: {@link mrmc.gui.GUInterface#GUInterface GUInterface constructor}
	 * 
	 * @param CardInputModeImrmc Panel containing elements for raw study input card
	 * @param guitemp Application's instance of the GUI
	 */
	public RawStudyCard(JPanel CardInputModeImrmc, GUInterface guitemp) {
		gui = guitemp;

		/*
		 * Elements of RawStudyCardRow1
		 */
		// Browse for input file
		JLabel studyLabel = new JLabel(".imrmc file  ");
		gui.JTextFilename = new JTextField(20);
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
		RawStudyCardRow1.add(gui.JTextFilename);
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
		chooseA.addItem("n/a");
		chooseB.addItem("n/a");
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
	 * file chooser and reads input file {@link mrmc.core.InputFile}
	 */
	class brwsButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			gui.resetGUI();
			
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"iMRMC Input Files (.imrmc)", "imrmc");
			fc.setFileFilter(filter);
			int returnVal = fc.showOpenDialog((Component) e.getSource());
			if( returnVal==JFileChooser.CANCEL_OPTION || returnVal==JFileChooser.ERROR_OPTION) return;
			
			/*
			 *  Get a pointer to the input file and the filename
			 */
			File f = fc.getSelectedFile();
			if( f==null ) return;
			gui.InputFile1.filename = f.getPath();
			gui.JTextFilename.setText(f.getPath());

			/*
			 *  Read the .imrmc input file, check for exceptions
			 */				
			try {
				gui.InputFile1.ReadInputFile();
			} catch (IOException except) {
				except.printStackTrace();
				JOptionPane.showMessageDialog(gui.MRMCobject.getFrame(),
						except.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				gui.resetGUI();
				gui.JTextFilename.setText("");
				return;
			}
			
			/*
			 * Compare the experimental design as determined from fields in the header vs. the data
			 */
			if (!gui.InputFile1.numsVerified()) {
				JOptionPane
						.showMessageDialog(
								gui.MRMCobject.getFrame(),
								gui.InputFile1.showUnverified(),
								"Warning: Input Header Values Do Not Match Actual Values",
								JOptionPane.WARNING_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(
						gui.MRMCobject.getFrame(),
						"NR = " + gui.InputFile1.getNreader() + 
						" N0 = "+ gui.InputFile1.getNnormal() +
						" N1 = "+ gui.InputFile1.getNdisease() +
						" NM = "+ gui.InputFile1.getNmodality(), 
						"Study Info", JOptionPane.INFORMATION_MESSAGE);
			}
			// TO-DO: Eliminate this from here. Check study design in DBRecord.
			if (!gui.InputFile1.getFullyCrossedStatus()) {
				JOptionPane.showMessageDialog(gui.MRMCobject.getFrame(),
						"The study is not fully crossed", "Warning",
						JOptionPane.WARNING_MESSAGE);
				gui.tabbedPane1.setEnabledAt(2, false);
				gui.tabbedPane1.setEnabledAt(3, false);
				gui.tabbedPane1.setEnabledAt(4, false);

			} else {
				gui.tabbedPane1.setEnabledAt(2, true);
				gui.tabbedPane1.setEnabledAt(3, true);
				gui.tabbedPane1.setEnabledAt(4, true);
			}

			gui.currModA = GUInterface.NO_MOD;
			gui.currModB = GUInterface.NO_MOD;
			updateStudyPanel();

		}
	}
	
	
	/**
	 * Handler for "Input Statistics Charts" button, displays charts for study
	 * design at a glance
	 */
	class ReadersCasesButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// System.out.println("graph button pressed");
			if (gui.InputFile1 != null && gui.InputFile1.isLoaded()) {
				final BarGraph cpr = new BarGraph("Cases per Reader",
						"Readers", "Cases", gui.InputFile1.casesPerReader());
				cpr.pack();
				RefineryUtilities.centerFrameOnScreen(cpr);
				cpr.setVisible(true);

				final BarGraph rpc = new BarGraph("Readers per Case", "Cases",
						"Readers", gui.InputFile1.readersPerCase());
				rpc.pack();
				RefineryUtilities.centerFrameOnScreen(rpc);
				RefineryUtilities.positionFrameOnScreen(rpc, 0.6, 0.6);
				rpc.setVisible(true);
			} else {
				JOptionPane.showMessageDialog(gui.MRMCobject.getFrame(),
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
			if (gui.InputFile1.isLoaded()) {
				JComboBox<String> choose1 = new JComboBox<String>();

				for (String Modality : gui.InputFile1.getModalityIDs()){
					choose1.addItem(Modality);
				}
				choose1.setSelectedIndex(0);
				Object[] message = { "Which modality would you like view?\n",
						choose1 };
				JOptionPane.showMessageDialog(gui.MRMCobject.getFrame(), message,
						"Choose Modality", JOptionPane.INFORMATION_MESSAGE,
						null);
				designMod1 = (String) choose1.getSelectedItem();
				boolean[][] design = gui.InputFile1.getStudyDesign( 
						(String) choose1.getSelectedItem());
				final StudyDesignPlot chart = new StudyDesignPlot(
						"Study Design: Modality " + designMod1, "Case",
						"Reader", design);
				chart.pack();
				RefineryUtilities.centerFrameOnScreen(chart);
				chart.setVisible(true);

			} else {
				JOptionPane.showMessageDialog(gui.MRMCobject.getFrame(),
						"Pilot study data has not yet been input.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Handler for "Show ROC Curve" button, displays interactive ROC charts
	 */
	class ROCButtonListener implements ActionListener {
		String rocMod = "";

		public void actionPerformed(ActionEvent e) {

			// If input file is loaded, then show ROC curves
			// Otherwise as for pilot study to be inpu
			if (gui.InputFile1.isLoaded()) {
				JComboBox<String> chooseMod = new JComboBox<String>();
				
				for (String Modality : gui.InputFile1.getModalityIDs()){
					chooseMod.addItem(Modality);
				}

				chooseMod.setSelectedIndex(0);
				Object[] message = { "Which modality would you like view?\n",
						chooseMod };
				JOptionPane.showMessageDialog(gui.MRMCobject.getFrame(), message,
						"Choose Modality and Reader",
						JOptionPane.INFORMATION_MESSAGE, null);
				rocMod = (String) chooseMod.getSelectedItem();
				final ROCCurvePlot roc = new ROCCurvePlot(
						"ROC Curve: Modality " + rocMod,
						"FPF (1 - Specificity)", "TPF (Sensitivity)",
						gui.InputFile1.generateROCpoints(rocMod));
				roc.addData(gui.InputFile1.generatePooledROC(rocMod), "Pooled Average");
				roc.pack();
				RefineryUtilities.centerFrameOnScreen(roc);
				roc.setVisible(true);

			} else {
				JOptionPane.showMessageDialog(gui.MRMCobject.getFrame(),
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
				useBiasM = GUInterface.USE_MLE;
			} else {
				useBiasM = GUInterface.NO_MLE;
			}
			gui.setFlagMLE(useBiasM);
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
			
			gui.resetStatPanel();
			gui.resetTable1();
			gui.resetSizePanel();
			gui.resetTable2();
			
			boolean modA, modB;
			modA = (!chooseA.getSelectedItem().equals("n/a"))
				&& (!chooseA.getSelectedItem().equals("Choose Modality A"));
			modB = (!chooseB.getSelectedItem().equals("n/a"))
				&& (!chooseB.getSelectedItem().equals("Choose Modality B"));
			
			if (modA && !modB) {
				gui.selectedMod = 0;
				gui.currModA = (String) chooseA.getSelectedItem();
				gui.currModB = (String) chooseA.getSelectedItem();
				varAnalysisButton.setText("MRMC Variance Analysis (A)");
			} else if (!modA && modB) {
				gui.selectedMod = 1;
				gui.currModA = (String) chooseB.getSelectedItem();
				gui.currModB = (String) chooseB.getSelectedItem();
				varAnalysisButton.setText("MRMC Variance Analysis (B)");
			} else if (modA && modB) {
				gui.selectedMod = 3;
				gui.currModA = (String) chooseA.getSelectedItem();
				gui.currModB = (String) chooseB.getSelectedItem();
				varAnalysisButton.setText("MRMC Variance Analysis (Difference)");
			} else {
				varAnalysisButton.setText("MRMC Variance Analysis");
				gui.currModA = GUInterface.NO_MOD;
				gui.currModB = GUInterface.NO_MOD;
				return;
			}

		} // method
	} // class
	
	/**
	 * Handler for MRMC variance analysis button. Verifies that input is valid
	 * and updates statistical analysis panel
	 */
	class varAnalysisListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("MRMC Variance analysis button clicked. RawStudyCard.varAnalysisListener");
			
			gui.DBRecordStat.DBRecordInputFile(
					gui.InputFile1, gui.currModA, gui.currModB, gui.selectedMod);
			gui.SizePanel1.JTextNreader.setText(Long.toString(gui.DBRecordStat.Nreader));
			gui.SizePanel1.JTextNnormal.setText(Long.toString(gui.DBRecordStat.Nnormal));
			gui.SizePanel1.JTextNdisease.setText(Long.toString(gui.DBRecordStat.Ndisease));
//			gui.SizePanel1.Nreader = (int) gui.DBRecordStat.Nreader;
//			gui.SizePanel1.Nnormal = (int) gui.DBRecordStat.Nnormal;
//			gui.SizePanel1.Ndisease = (int) gui.DBRecordStat.Ndisease;
			
			if ( gui.checkRawInput() && gui.checkNegative(gui.DBRecordStat)) {
				gui.setStatPanel();
				gui.setTable1();
			}
		}
	}
	
	/**
	 * Creates a table showing individual reader AUCs
	 * 
	 */
	class showAUCsButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			if( gui.DBRecordStat.totalVar > 0.0) {
			JFrame JFrameAUC= new JFrame("AUCs for each reader and modality");
			RefineryUtilities.centerFrameOnScreen(JFrameAUC);
			JFrameAUC.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			Object[] colNames = { "ReaderID", "AUC "+gui.currModA, "AUC "+gui.currModB };
			Object[][] rowContent = new String[(int) gui.DBRecordStat.Nreader][3];
			int i=0;
			for(String desc_temp : gui.InputFile1.readerIDs.keySet() ) {
				rowContent[i][0] = desc_temp;
				rowContent[i][1] = Double.toString(gui.DBRecordStat.AUCs[i][0]);
				rowContent[i][2] = Double.toString(gui.DBRecordStat.AUCs[i][1]);
				i++;
			}

			JTable tableAUC = new JTable(rowContent, colNames);
			JScrollPane scrollPaneAUC = new JScrollPane(tableAUC);
			JFrameAUC.add(scrollPaneAUC, BorderLayout.CENTER);
			JFrameAUC.setSize(600, 300);
			JFrameAUC.setVisible(true);
			}
			else {
				JOptionPane.showMessageDialog(gui.MRMCobject.getFrame(),
						"Pilot study data has not yet been analyzed.", "Error",
						JOptionPane.ERROR_MESSAGE);

			}
		}
	}
}
