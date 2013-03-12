package mrmc.gui;

import javax.swing.*;
import java.awt.event.*;

public class ModSelect {
	GUInterface gui;
	int selectedMod = 0;
	int useBiasM = 0;
	JCheckBox cb;
	JRadioButton mod1Button;
	JRadioButton mod2Button;
	JRadioButton modDButton;
	JLabel currModalities;

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

	public ModSelect(JPanel mPanel, GUInterface guitemp) {
		gui = guitemp;
		// create the checkbox for allowing negative components
		cb = new JCheckBox(
				"use MLE estimates of moments to avoid negatives          \t\t");
		cb.setSelected(false);
		cb.addItemListener(new allNegativeListner());
		// Create the radio buttons.
		String str1 = "Modality A";
		mod1Button = new JRadioButton(str1);
		mod1Button.setActionCommand(str1);
		mod1Button.setSelected(true);

		String str2 = "Modality B";
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
			if (str == "Modality A") {
				selectedMod = 0;
			}
			if (str == "Modality B") {
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
