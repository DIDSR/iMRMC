/*
 * PilotModSelect.java
 * 
 * v2.0b
 * 
 * @Author Brandon D. Gallas, PhD, Rohan Pathare
 * 
 * This software and documentation (the "Software") were developed at the Food and Drug Administration (FDA) 
 * by employees of the Federal Government in the course of their official duties. Pursuant to Title 17, Section 
 * 105 of the United States Code, this work is not subject to copyright protection and is in the public domain. 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of the Software, to deal in the 
 * Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, or sell copies of the Software or derivatives, and to permit persons to whom the 
 * Software is furnished to do so. FDA assumes no responsibility whatsoever for use by other parties of the 
 * Software, its source code, documentation or compiled executables, and makes no guarantees, expressed or 
 * implied, about its quality, reliability, or any other characteristic.   Further, use of this code in no way 
 * implies endorsement by the FDA or confers any advantage in regulatory decisions.  Although this software 
 * can be redistributed and/or modified freely, we ask that any derivative works bear some notice that they 
 * are derived from it, and any modified versions bear some notice that they have been modified.
 *     
 *     Panel for selecting modalities, displaying charts of data statistics, and performing
 *     MRMC variance analysis with data from pilot study raw data files.
 */

package mrmc.gui;

import javax.swing.*;

import mrmc.core.dbRecord;

import java.awt.event.*;

public class PilotModSelect {
	GUInterface gui;
	int selectedMod = 0;
	int useBiasM = 0;
	JCheckBox negBox;
	JComboBox chooseA, chooseB;
	JButton varAnalysisButton;

	public void setUseBiasM(boolean bias) {
		if (bias) {
			useBiasM = 1;
		} else {
			useBiasM = 0;
		}
		negBox.setSelected(bias);
	}

	public void setSelectedMod(int input) {
		selectedMod = input;
		gui.setSelectedMod(selectedMod);
	}

	public void updateModPanel() {
		chooseA.removeAllItems();
		chooseB.removeAllItems();
		chooseA.addItem("none");
		chooseB.addItem("none");
		for (int i = 1; i <= gui.usr.getModality(); i++) {
			chooseA.addItem("" + i);
			chooseB.addItem("" + i);
		}
	}

	public PilotModSelect(JPanel mPanel, GUInterface guitemp) {
		gui = guitemp;
		negBox = new JCheckBox(
				"use MLE estimates of moments to avoid negatives    ");
		negBox.setSelected(false);
		negBox.addItemListener(new allNegativeListner());

		chooseA = new JComboBox();
		chooseB = new JComboBox();
		chooseA.addItem("n/a");
		chooseB.addItem("n/a");

		chooseA.addItemListener(new comboSelectionListner());
		chooseB.addItemListener(new comboSelectionListner());

		mPanel.add(negBox);
		mPanel.add(new JLabel("Modality A: "));
		mPanel.add(chooseA);
		mPanel.add(new JLabel("Modality B: "));
		mPanel.add(chooseB);

		varAnalysisButton = new JButton("MRMC Variance Analysis");
		mPanel.add(varAnalysisButton);
		varAnalysisButton.addActionListener(new varAnalysisListner());

	}

	class comboSelectionListner implements ItemListener {
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
						gui.currMod1 = Integer.parseInt(chooseA
								.getSelectedItem().toString());
						gui.currMod2 = -1;
						varAnalysisButton.setText("MRMC Variance Analysis (A)");
						gui.usr.getT0T1s(gui.currMod1, gui.currMod1);
						gui.usr.calculateCovMRMC();
						// gui.usr.calcCofV(gui.currMod1, gui.currMod1);
						gui.usrFile = new dbRecord(gui.usr);
						setSelectedMod(0);
					} else if (!modA && modB) {
						gui.currMod2 = Integer.parseInt(chooseB
								.getSelectedItem().toString());
						gui.currMod1 = -1;
						varAnalysisButton.setText("MRMC Variance Analysis (B)");
						gui.usr.getT0T1s(gui.currMod2, gui.currMod2);
						gui.usr.calculateCovMRMC();
						// gui.usr.calcCofV(gui.currMod2, gui.currMod2);
						gui.usrFile = new dbRecord(gui.usr);
						setSelectedMod(1);
					} else if (modA && modB) {
						gui.currMod1 = Integer.parseInt(chooseA
								.getSelectedItem().toString());
						gui.currMod2 = Integer.parseInt(chooseB
								.getSelectedItem().toString());
						varAnalysisButton
								.setText("MRMC Variance Analysis (Difference)");
						gui.usr.getT0T1s(gui.currMod1, gui.currMod2);
						gui.usr.calculateCovMRMC();
						// gui.usr.calcCofV(gui.currMod1, gui.currMod2);
						gui.usrFile = new dbRecord(gui.usr);
						setSelectedMod(3);
					} else {
						varAnalysisButton.setText("MRMC Variance Analysis");
						gui.currMod1 = -1;
						gui.currMod2 = -1;
					}
				}
			}
		}
	}

	class allNegativeListner implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (negBox.isSelected()) {
				useBiasM = 1;
			} else {
				useBiasM = 0;
			}
			gui.setUseBiasM(useBiasM);
		}
	}

	class varAnalysisListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// System.out.println("MRMC Variance analysis button clicked");
			int check = gui.checkNegative();
			if (check == 1) {
				gui.setTab1();
				gui.setAUCoutput();
				gui.setSPanel();
				gui.set1stStatPanel();
			}
		}
	}
}
