/*
 * DBModSelect.java
 * 
 * v2.0b
 * 
 * @Author Xin He, Phd, Brandon D. Gallas, PhD, Rohan Pathare
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
 *     Panel for selecting modalities and performing MRMC variance analysis with data
 *     from internal database
 */

package mrmc.gui;

import javax.swing.*;
import java.awt.event.*;

public class DBCard {
	GUInterface gui;
	int selectedMod = 0;
	int useBiasM = 0;
	JCheckBox cb;
	JRadioButton mod1Button;
	JRadioButton mod2Button;
	JRadioButton modDButton;

	public void setUseBiasM(int temp) {
		if (temp == 1) {
			useBiasM = 1;
			cb.setSelected(true);
		} else {
			useBiasM = 0;
			cb.setSelected(false);
		}
	}

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

		JButton okButton = new JButton("MRMC variance analysis"); // used to be
																	// the ok
																	// button
		mPanel.add(okButton);
		okButton.addActionListener(new okButtonListner());

	}

	/*
	 * checkbox to select whether to use MLE estimates of moments to avoid
	 * negatives
	 */
	class allNegativeListner implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (cb.isSelected()) {
				useBiasM = 1;
			} else {
				useBiasM = 0;
			}
			gui.setUseBiasM(useBiasM);
		}

	}

	/* radio buttons to select the type of modality when reading from database */
	class modSelListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String str;
			str = e.getActionCommand();
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

	/* "MRMC variance analysis" button to perform analysis on dataset */
	class okButtonListner implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("OK button clicked");
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
