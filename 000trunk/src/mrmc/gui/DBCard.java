/**
 * DBCard.java
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import mrmc.core.DBRecord;
import mrmc.core.MRMC;
import mrmc.core.MrmcDB;

/**
 * Card handling input of study data components from internal database
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
public class DBCard {
	private GUInterface gui;
	private int selectedMod = 0;
	private int useMLE = 0;
	private JCheckBox cb;
	private JRadioButton mod1Button;
	private JRadioButton mod2Button;
	private JRadioButton modDButton;

	/**
	 * Sole constructor. Builds the DBCard gui panel
	 * 
	 * @param CardInputModeOmrmc Panel containing different input type cards
	 * @param MRMCobject GUInterface for which mPanel is attached
	 */
	public DBCard(JPanel CardInputModeOmrmc, GUInterface gui, MRMC MRMCobject) {
		
		// create the checkbox for allowing negative components
		cb = new JCheckBox(
				"use MLE (avoid negatives)");
		cb.setSelected(false);
		cb.addItemListener(new allNegativeListener());
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
		modSelListener gListener = new modSelListener();
		mod1Button.addActionListener(gListener);
		mod2Button.addActionListener(gListener);
		modDButton.addActionListener(gListener);

		JPanel CardInputModeOmrmc2 = new JPanel();
		CardInputModeOmrmc2.add(cb);
		CardInputModeOmrmc2.add(mod1Button);
		CardInputModeOmrmc2.add(mod2Button);
		CardInputModeOmrmc2.add(modDButton);

		JButton varAnalysisBtn = new JButton("MRMC variance analysis");
		CardInputModeOmrmc2.add(varAnalysisBtn);
		varAnalysisBtn.addActionListener(new VarAnalysisListener());
		
		MrmcDB fdaDB = MRMCobject.getDB();
		int DBsize = fdaDB.getNoOfItems();
		String[] dbBoxItems = new String[DBsize];
		DBRecord[] Records = fdaDB.getRecords();
		for (int i = 0; i < DBsize; i++) {
			dbBoxItems[i] = Records[i].getRecordTitle();
		}
	
		GroupLayout layout = new GroupLayout(CardInputModeOmrmc);
		CardInputModeOmrmc.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
	
		JLabel studyLabel = new JLabel("Database ");
		JComboBox<String> dbCB = new JComboBox<String>(dbBoxItems);
		dbCB.setEditable(false);
		dbCB.addActionListener(gui.new dbActionListener());
		dbCB.setSelectedIndex(0);
		JButton descButton = new JButton("Record Description");
		descButton.addActionListener(gui.new descButtonListener());
	
		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								layout.createSequentialGroup()
										.addComponent(studyLabel)
										.addComponent(dbCB)
										.addComponent(descButton))
						.addGroup(
								layout.createSequentialGroup().addComponent(
										CardInputModeOmrmc2))));
	
		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(studyLabel).addComponent(dbCB)
								.addComponent(descButton))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING).addComponent(
								CardInputModeOmrmc2)));

	}

	/**
	 * Sets if MLE (bias) is used for variance components
	 * 
	 * @param MLEsetting Whether or not to use bias
	 */
	public void setUseMLE(int MLEsetting) {
		if (MLEsetting == 1) {
			useMLE = GUInterface.USE_MLE;
			cb.setSelected(true);
		} else {
			useMLE = GUInterface.NO_MLE;
			cb.setSelected(false);
		}
	}

	/**
	 * Sets which modality/difference is being used, updates GUI elements to
	 * reflect choice
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
		gui.selectedMod=input;
	}

	/**
	 * Updates GUI elements based on whether check-box indicates MLE is being
	 * used
	 * 
	 */
	class allNegativeListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (cb.isSelected()) {
				useMLE = GUInterface.USE_MLE;
			} else {
				useMLE = GUInterface.NO_MLE;
			}
			gui.setFlagMLE(useMLE);
		}
	}

	/**
	 * Handler for modality selection radio buttons, updates GUI accordingly
	 * 
	 */
	class modSelListener implements ActionListener {
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
			gui.selectedMod = selectedMod;
			gui.pilotFile.setText("");
			gui.reset1stStatPanel();
			gui.resetTable1();
			gui.resetSizePanel();
			gui.resetTable2();
		}
	}

	/**
	 * Handler for "MRMC Variance Analysis" button, updates GUI
	 * 
	 * @author rpathare
	 * 
	 */
	class VarAnalysisListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("Variance analysis button clicked");
			boolean check = gui.checkNegative();
			if (check) {
				gui.set1stStatPanel();
				gui.setTable1();
				gui.setSizePanel();
			}
		}
	}

}
