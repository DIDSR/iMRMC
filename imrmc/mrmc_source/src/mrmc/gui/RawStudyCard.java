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

import javax.swing.*;

import mrmc.core.DBRecord;

import java.awt.event.*;

/**
 * Panel for selecting modalities, displaying charts of data statistics, and
 * performing MRMC variance analysis with data from pilot study raw data files.
 * 
 * 
 * @author Xin He, Ph.D
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 * @version 2.0b
 */
public class RawStudyCard {
	private GUInterface gui;
	private int selectedMod = 0;
	private int useBiasM = 0;
	private JCheckBox negBox;
	private JComboBox chooseA, chooseB;
	private JButton varAnalysisButton;

	/**
	 * Sets whether bias is to be used when performing variance analysis
	 * 
	 * @param bias Whether bias is used
	 */
	public void setUseMLE(int bias) {
		useBiasM = bias;
		if (bias == GUInterface.NO_MLE) {
			negBox.setSelected(false);
		} else {
			negBox.setSelected(true);
		}
	}

	/**
	 * Sets which modality is being analyzed, or difference between them
	 * 
	 * @param input Which modality/difference is to be analyzed
	 */
	public void setSelectedMod(int input) {
		selectedMod = input;
		gui.setSelectedMod(selectedMod);
	}

	/**
	 * Sets study panel to default values and updates menus to choose modality
	 * with values from current study file
	 */
	public void updateStudyPanel() {
		chooseA.removeAllItems();
		chooseB.removeAllItems();
		chooseA.addItem("none");
		chooseB.addItem("none");
		for (int i = 1; i <= gui.usr.getModality(); i++) {
			chooseA.addItem("" + i);
			chooseB.addItem("" + i);
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
	 * Sole constructor. Creates and initializes GUI elements
	 * 
	 * @param studyPanel Panel containing elements for raw study input card
	 * @param guitemp Application's instance of the GUI
	 */
	public RawStudyCard(JPanel studyPanel, GUInterface guitemp) {
		gui = guitemp;
		negBox = new JCheckBox(
				"use MLE estimates of moments to avoid negatives    ");
		negBox.setSelected(false);
		negBox.addItemListener(new UseMLEListener());

		// Drop down menus to select modality
		chooseA = new JComboBox();
		chooseB = new JComboBox();
		chooseA.addItem("n/a");
		chooseB.addItem("n/a");

		chooseA.addItemListener(new ModSelectListener());
		chooseB.addItemListener(new ModSelectListener());

		studyPanel.add(negBox);
		studyPanel.add(new JLabel("Modality A: "));
		studyPanel.add(chooseA);
		studyPanel.add(new JLabel("Modality B: "));
		studyPanel.add(chooseB);

		varAnalysisButton = new JButton("MRMC Variance Analysis");
		studyPanel.add(varAnalysisButton);
		varAnalysisButton.addActionListener(new varAnalysisListener());

	}

	/**
	 * Handler for drop down menus to select Modality A and Modality B. Changes
	 * variance analysis button text to reflect which type of analysis is being
	 * performed
	 */
	class ModSelectListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				boolean modA, modB;
				if (chooseA.getSelectedItem() != null
						&& chooseB.getSelectedItem() != null) {
					modA = (!chooseA.getSelectedItem().equals("n/a"))
							&& (!chooseA.getSelectedItem().equals("none"));
					modB = (!chooseB.getSelectedItem().equals("n/a"))
							&& (!chooseB.getSelectedItem().equals("none"));
					if (modA && !modB) {
						gui.currMod0 = Integer.parseInt(chooseA
								.getSelectedItem().toString());
						gui.currMod1 = GUInterface.NO_MOD;
						varAnalysisButton.setText("MRMC Variance Analysis (A)");
						gui.usr.makeTMatrices(gui.currMod0, gui.currMod0);
						gui.usr.calculateCovMRMC();
						gui.usrFile = new DBRecord(gui.usr, gui.currMod0,
								gui.currMod0);
						setSelectedMod(0);
					} else if (!modA && modB) {
						gui.currMod1 = Integer.parseInt(chooseB
								.getSelectedItem().toString());
						gui.currMod0 = GUInterface.NO_MOD;
						varAnalysisButton.setText("MRMC Variance Analysis (B)");
						gui.usr.makeTMatrices(gui.currMod1, gui.currMod1);
						gui.usr.calculateCovMRMC();
						gui.usrFile = new DBRecord(gui.usr, gui.currMod1,
								gui.currMod1);
						setSelectedMod(1);
					} else if (modA && modB) {
						gui.currMod0 = Integer.parseInt(chooseA
								.getSelectedItem().toString());
						gui.currMod1 = Integer.parseInt(chooseB
								.getSelectedItem().toString());
						varAnalysisButton
								.setText("MRMC Variance Analysis (Difference)");
						gui.usr.makeTMatrices(gui.currMod0, gui.currMod1);
						gui.usr.calculateCovMRMC();
						gui.usrFile = new DBRecord(gui.usr, gui.currMod0,
								gui.currMod1);
						setSelectedMod(3);
					} else {
						varAnalysisButton.setText("MRMC Variance Analysis");
						gui.currMod0 = GUInterface.NO_MOD;
						gui.currMod1 = GUInterface.NO_MOD;
					}
				}
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
			if (negBox.isSelected()) {
				useBiasM = GUInterface.USE_MLE;
			} else {
				useBiasM = GUInterface.NO_MLE;
			}
			gui.setUseMLE(useBiasM);
		}
	}

	/**
	 * Handler for MRMC variance analysis button. Verifies that input is valid
	 * and updates statistical analysis pane
	 */
	class varAnalysisListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// System.out.println("MRMC Variance analysis button clicked");

			if (gui.checkRawInput() && gui.checkNegative()) {
				gui.setTable1();
				gui.setAUCoutput();
				gui.setSizePanel();
				gui.set1stStatPanel();
			}
		}
	}
}
