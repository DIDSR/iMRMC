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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import mrmc.core.DBRecord;

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
	int selectedMod = 0;
	private int useBiasM = 0;
	private JCheckBox mleCheckBox;
	private JComboBox<String> chooseA, chooseB;
	private JButton varAnalysisButton;

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
		
		for (String ModalityID : gui.currentInputFile.getModalityIDs()) {
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

		GroupLayout layout = new GroupLayout(CardInputModeImrmc);
		CardInputModeImrmc.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel studyLabel = new JLabel(".imrmc file  ");
		gui.pilotFile = new JTextField(20);
		JButton fmtHelpButton = new JButton("User Manual");
		JButton readerCasesButton = new JButton("Input Statistics Charts");
		JButton designButton = new JButton("Show Study Design");
		JButton ROCcurveButton = new JButton("Show ROC Curve");
	
		JPanel CardInputModeImrmc2 = new JPanel();
		// MLE Checkbox
		mleCheckBox = new JCheckBox("MLE (avoid negatives)");
		mleCheckBox.setSelected(false);
		mleCheckBox.addItemListener(new UseMLEListener());

		// Drop down menus to select modality
		chooseA = new JComboBox<String>();
		chooseB = new JComboBox<String>();
		chooseA.addItem("n/a");
		chooseB.addItem("n/a");
		chooseA.addItemListener(new ModSelectListener());
		chooseB.addItemListener(new ModSelectListener());

		// execute variance analysis button
		varAnalysisButton = new JButton("MRMC Variance Analysis");
		varAnalysisButton.addActionListener(new varAnalysisListener());

		CardInputModeImrmc2.add(mleCheckBox);
		CardInputModeImrmc2.add(chooseA);
		CardInputModeImrmc2.add(chooseB);
		CardInputModeImrmc2.add(varAnalysisButton);

		JButton browseButton = new JButton("Browse...");
		browseButton.addActionListener(gui.new brwsButtonListener());
		fmtHelpButton.addActionListener(gui.new fmtHelpButtonListener());
		readerCasesButton.addActionListener(gui.new ReadersCasesButtonListener());
		designButton.addActionListener(gui.new designButtonListener());
		ROCcurveButton.addActionListener(gui.new ROCButtonListener());
		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								layout.createSequentialGroup()
										.addComponent(studyLabel)
										.addComponent(gui.pilotFile)
										.addComponent(browseButton)
										.addComponent(fmtHelpButton)
										.addComponent(readerCasesButton)
										.addComponent(designButton)
										.addComponent(ROCcurveButton))
						.addGroup(
								layout.createSequentialGroup().addComponent(
										CardInputModeImrmc2))));
	
		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(studyLabel)
								.addComponent(gui.pilotFile)
								.addComponent(browseButton)
								.addComponent(fmtHelpButton)
								.addComponent(readerCasesButton)
								.addComponent(designButton)
								.addComponent(ROCcurveButton))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING).addComponent(
								CardInputModeImrmc2)));


		
	}

	/**
	 * Handler for drop down menus to select Modality A and Modality B. Changes
	 * variance analysis button text to reflect which type of analysis is being
	 * performed. <br>
	 * ----Creates ({@link mrmc.core.DBRecord}) <br>
	 */
	class ModSelectListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {

			if (e.getStateChange() != ItemEvent.DESELECTED) { return; }
			if (chooseA.getSelectedItem() == null
				|| chooseB.getSelectedItem() == null) { return; }
			
			gui.selectedMod = selectedMod;
			gui.reset1stStatPanel();
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
				gui.currModB = GUInterface.NO_MOD;
				varAnalysisButton.setText("MRMC Variance Analysis (A)");
				gui.currentDBrecord = new DBRecord(gui.currentInputFile, gui.selectedMod, gui.currModA);
			} else if (!modA && modB) {
				gui.selectedMod = 1;
				gui.currModA = GUInterface.NO_MOD;
				gui.currModB = (String) chooseB.getSelectedItem();
				varAnalysisButton.setText("MRMC Variance Analysis (B)");
				gui.currentDBrecord = new DBRecord(gui.currentInputFile, gui.selectedMod, gui.currModB);
			} else if (modA && modB) {
				gui.selectedMod = 3;
				gui.currModA = (String) chooseA.getSelectedItem();
				gui.currModB = (String) chooseB.getSelectedItem();
				varAnalysisButton.setText("MRMC Variance Analysis (Difference)");
				gui.currentDBrecord = new DBRecord(gui.currentInputFile, gui.selectedMod, gui.currModA, gui.currModB);
			} else {
				varAnalysisButton.setText("MRMC Variance Analysis");
				gui.currModA = GUInterface.NO_MOD;
				gui.currModB = GUInterface.NO_MOD;
			}
			
		} // method
	} // class

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
	 * Handler for MRMC variance analysis button. Verifies that input is valid
	 * and updates statistical analysis panel
	 */
	class varAnalysisListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("MRMC Variance analysis button clicked. RawStudyCard.varAnalysisListener");
			
			if ( gui.checkRawInput() && gui.checkNegative()) {
				gui.set1stStatPanel();
				gui.setTable1();
				gui.setSizePanel();
			}
		}
	}
}
