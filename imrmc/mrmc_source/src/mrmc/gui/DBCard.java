/**
 * DBModSelect.java
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
 */

package mrmc.gui;

import javax.swing.*;
import java.awt.event.*;

/**
 * Performs variance analysis over two modalities, determines AUC
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 * @version 2.0b
 */
public class DBCard {
	private GUInterface gui;
	private int selectedMod = 0;
	private int useBiasM = 0;
	private JCheckBox cb;
	private JRadioButton mod1Button;
	private JRadioButton mod2Button;
	private JRadioButton modDButton;

	/**
	 * Sole constructor. Builds the DBCard gui panel
	 * 
	 * @param mPanel Panel containing different input type cards
	 * @param guitemp GUInterface for which mPanel is attached
	 */
	public DBCard(JPanel mPanel, GUInterface guitemp) {
		gui = guitemp;
		// create the checkbox for allowing negative components
		cb = new JCheckBox(
				"use MLE estimates of moments to avoid negatives          \t\t");
		cb.setSelected(false);
		cb.addItemListener(new allNegativeListner());
		// Create the radio buttons.
		String str1 = "Modality 1";
		mod1Button = new JRadioButton(str1);
		mod1Button.setActionCommand(str1);
		mod1Button.setSelected(true);

		String str2 = "Modality 2";
		mod2Button = new JRadioButton(str2);
		mod2Button.setActionCommand(str2);

		String strD = "Difference";
		modDButton = new JRadioButton(strD);
		modDButton.setActionCommand(strD);

		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(mod1Button);
		group.add(mod2Button);
		group.add(modDButton);

		// Register a listener for the radio buttons.
		modSelListner gListener = new modSelListner();
		mod1Button.addActionListener(gListener);
		mod2Button.addActionListener(gListener);
		modDButton.addActionListener(gListener);

		mPanel.add(cb);
		mPanel.add(mod1Button);
		mPanel.add(mod2Button);
		mPanel.add(modDButton);

		JButton varAnalysisBtn = new JButton("MRMC variance analysis");
		mPanel.add(varAnalysisBtn);
		varAnalysisBtn.addActionListener(new VarAnalysisListner());
	}

	/**
	 * Sets if bias is used for variance components
	 * 
	 * @param biasSetting Whether or not to use bias
	 */
	public void setUseBiasM(int biasSetting) {
		if (biasSetting == 1) {
			useBiasM = GUInterface.USE_BIAS;
			cb.setSelected(true);
		} else {
			useBiasM = GUInterface.NO_BIAS;
			cb.setSelected(false);
		}
	}

	/**
	 * Sets which modality/difference is being used, updates GUI elements to
	 * reflect choise
	 * 
	 * @param input Which modality/difference is being used
	 */
	public void setSelectedMod(int input) {
		if (input == 0)
			mod1Button.setSelected(true);
		else if (input == 1)
			mod2Button.setSelected(true);
		else if (input == 3)
			modDButton.setSelected(true);
		selectedMod = input;
		gui.setSelectedMod(selectedMod);
	}

	/**
	 * Updates GUI elements based on whether check-box indicates bias is being
	 * used
	 * 
	 */
	class allNegativeListner implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (cb.isSelected()) {
				useBiasM = GUInterface.USE_BIAS;
			} else {
				useBiasM = GUInterface.NO_BIAS;
			}
			gui.setUseBiasM(useBiasM);
		}
	}

	/**
	 * Handler for modality selection radio buttons, updates GUI accordingly
	 * 
	 */
	class modSelListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String str = e.getActionCommand();
			System.out.println(str + "radiobutton selected");
			if (str == "Modality 1") {
				selectedMod = 0;
			}
			if (str == "Modality 2") {
				selectedMod = 1;
			}
			if (str == "Difference") {
				selectedMod = 3;
			}
			gui.setSelectedMod(selectedMod);
		}
	}

	/**
	 * Handler for "MRMC Variance Analysis" button, updates GUI
	 * 
	 * @author rpathare
	 * 
	 */
	class VarAnalysisListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("Variance analysis button clicked");
			boolean check = gui.checkNegative();
			if (check) {
				gui.setTable1();
				gui.setAUCoutput();
				gui.setSizePanel();
				gui.set1stStatPanel();
			}
		}
	}

}
