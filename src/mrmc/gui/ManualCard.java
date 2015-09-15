/**
 * ManualCard.java
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
import mrmc.core.MRMC;
import mrmc.core.DBRecord;

/**
 * Card handling manual input of data components
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
public class ManualCard {
	private GUInterface gui;
	private MRMC lst;
	private JTextField manualInReader;
	private JTextField manualInNormal;
	private JTextField manualInDisease;
	private JTextField AUCText1;
	private JTextField AUCText2;
	private DBRecord record;
	private double[] auc = new double[2];
	private JRadioButton com1Button;
	private JRadioButton SingleMod;

	private int Reader;
	private int Normal;
	private int Disease;
	private int selectedManualComp = 0;
	private String BDGlabel = "M1, M2, M3, M4, M5, M6, M7, M8";
	private String DBMlabel = "R, C, RC, TR, TC, TRC";
	private String BCKlabel = "N, D, ND, R, NR, DR, RND";
	private String ORlabel = "R, TR, COV1, COV2, COV3, ERROR";
	private JLabel compLabel;
	private JTextField compText;
	private int[] numberOfComps = new int[] { 8, 7, 6, 6 };
	private String[] com = new String[] { "BDG", "BCK", "DBM", "OR" };
	private String[] mod = new String[] { "Single Modality", "Difference" };
	private int SingleOrDiff = 0;

	/**
	 * Gets which decomposition of the variance components is being used
	 * 
	 * @return Which decomposition is being used
	 */
	public int getSelectedManualComp() {
		return selectedManualComp;
	}

	/**
	 * Gets whether single modality or difference between modalities is being
	 * analyzed when using manually input components
	 * 
	 * @return Whether single modality or difference
	 */
	public int getSingleOrDiff() {
		return SingleOrDiff;
	}

	/**
	 * Resets the Manual input panel to default values
	 */
	public void reset() {
		com1Button.setSelected(true);
		gui.StatPanel1.enableTabs();
		SingleMod.setSelected(true);
		AUCText2.setEnabled(false);
		manualInReader.setText("22");
		manualInNormal.setText("22");
		manualInDisease.setText("22");
		AUCText1.setText("0.8");
		AUCText2.setText("0.85");
		compText.setText(" 0.116943691, 0.071012127, 0.046916759, 0.022056021, 0.051145786, 0.044660693, 0.027096782, 0.022590056");
	}

	/**
	 * Gets the DBRecord created from analysis of the manual components
	 * 
	 * @return Record of current analysis
	 */
	public DBRecord getManualRecord() {
		return record;
	}

	/**
	 * Gets the number of readers
	 * 
	 * @return number of readers
	 */
	public int getReader() {
		return Reader;
	}

	/**
	 * Gets the number of normal cases
	 * 
	 * @return Number of normal cases
	 */
	public int getNormal() {
		return Normal;
	}

	/**
	 * Gets the number of disease cases
	 * 
	 * @return Number of disease cases
	 */
	public int getDisease() {
		return Disease;
	}

	/**
	 * Sole constructor. Initializes GUI elements.
	 * 
	 * @param manualCard Panel containing the elements of the manual input card
	 * @param guitemp Application's instance of the GUI
	 * @param lsttemp Application frame.
	 */
	public ManualCard(JPanel manualCard, GUInterface guitemp, MRMC lsttemp) {
		lst = lsttemp;
		gui = guitemp;
		GroupLayout layout = new GroupLayout(manualCard);
		manualCard.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel studyInfoLabel = new JLabel("Information of the study\t");
		JLabel readerLabel = new JLabel("# Readers");
		manualInReader = new JTextField(5);
		JLabel normalLabel = new JLabel("# Normal");
		manualInNormal = new JTextField(5);
		JLabel diseaseLabel = new JLabel("# Disease");
		manualInDisease = new JTextField(5);
		JLabel componentsLabel = new JLabel("Which components are you using?");
		// Create the radio buttons.
		com1Button = new JRadioButton(com[0]);
		com1Button.setActionCommand(com[0]);
		com1Button.setSelected(true);
		JRadioButton com2Button = new JRadioButton(com[1]);
		com2Button.setActionCommand(com[1]);
		JRadioButton com3Button = new JRadioButton(com[2]);
		com3Button.setActionCommand(com[2]);
		JRadioButton com4Button = new JRadioButton(com[3]);
		com4Button.setActionCommand(com[3]);
		// Group the radio buttons.
		ButtonGroup groupcom = new ButtonGroup();
		groupcom.add(com1Button);
		groupcom.add(com2Button);
		groupcom.add(com3Button);
		groupcom.add(com4Button);

		// Register a listener for the radio buttons.
		comSelListener comListener = new comSelListener();
		com1Button.addActionListener(comListener);
		com2Button.addActionListener(comListener);
		com3Button.addActionListener(comListener);
		com4Button.addActionListener(comListener);

		JLabel space = new JLabel("       The components are for");
		SingleMod = new JRadioButton(mod[0]);
		SingleMod.setActionCommand(mod[0]);
		SingleMod.setSelected(true);
		JRadioButton DiffMod = new JRadioButton(mod[1]);
		DiffMod.setActionCommand(mod[1]);
		ButtonGroup groupmod = new ButtonGroup();
		groupmod.add(SingleMod);
		groupmod.add(DiffMod);
		modSelListener modListener = new modSelListener();
		SingleMod.addActionListener(modListener);
		DiffMod.addActionListener(modListener);

		JButton varAnalysisButton = new JButton("MRMC Variance Analysis");
		varAnalysisButton.addActionListener(new VarAnalysisBtnListener());

		JLabel aucLabel1 = new JLabel("AUC1 ");
		AUCText1 = new JTextField(10);
		JLabel aucLabel2 = new JLabel("AUC2 ");
		AUCText2 = new JTextField(10);
		AUCText2.setEnabled(false);
		String tempspace = "                                                             ";
		tempspace = tempspace + tempspace + tempspace;
		JLabel space2 = new JLabel(tempspace);
		compLabel = new JLabel(BDGlabel);
		compText = new JTextField(50);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								layout.createSequentialGroup()
										.addComponent(studyInfoLabel)
										.addComponent(readerLabel)
										.addComponent(manualInReader)
										.addComponent(normalLabel)
										.addComponent(manualInNormal)
										.addComponent(diseaseLabel)
										.addComponent(manualInDisease))
						.addGroup(
								layout.createSequentialGroup()
										.addComponent(aucLabel1)
										.addComponent(AUCText1)
										.addComponent(aucLabel2)
										.addComponent(AUCText2)
										.addComponent(space2))
						.addGroup(
								layout.createSequentialGroup()
										.addComponent(componentsLabel)
										.addComponent(com1Button)
										.addComponent(com2Button)
										.addComponent(com3Button)
										.addComponent(com4Button)
										.addComponent(space)
										.addComponent(SingleMod)
										.addComponent(DiffMod)
										.addComponent(varAnalysisButton))
						.addGroup(
								layout.createSequentialGroup()
										.addComponent(compLabel)
										.addComponent(compText))));

		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(studyInfoLabel)
								.addComponent(readerLabel)
								.addComponent(manualInReader)
								.addComponent(normalLabel)
								.addComponent(manualInNormal)
								.addComponent(diseaseLabel)
								.addComponent(manualInDisease))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING)
								.addComponent(aucLabel1).addComponent(AUCText1)
								.addComponent(aucLabel2).addComponent(AUCText2)
								.addComponent(space2))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(componentsLabel)
								.addComponent(com1Button)
								.addComponent(com2Button)
								.addComponent(com3Button)
								.addComponent(com4Button).addComponent(space)
								.addComponent(SingleMod).addComponent(DiffMod)
								.addComponent(varAnalysisButton))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING)
								.addComponent(compLabel).addComponent(compText)));
	}

	/**
	 * Handler for radio buttons to select which type of components are being
	 * used
	 */
	class comSelListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String str;
			gui.StatPanel1.disableTabs();
			str = e.getActionCommand();
			if (str.equals("BDG")) {
				selectedManualComp = 0;
				compLabel.setText(BDGlabel);
				gui.StatPanel1.enableTabs();
			} else if (str.equals("BCK")) {
				selectedManualComp = 1;
				compLabel.setText(BCKlabel);
				gui.StatPanel1.enableBCKTab();
			} else if (str.equals("DBM")) {
				selectedManualComp = 2;
				compLabel.setText(DBMlabel);
				gui.StatPanel1.enableDBMORTabs();
			} else if (str.equals("OR")) {
				selectedManualComp = 3;
				compLabel.setText(ORlabel);
				gui.StatPanel1.enableDBMORTabs();
			}
			compText.setText("");
		}
	}

	/**
	 * Handler for radio buttons to select whether components are used for
	 * single modality or difference
	 */
	class modSelListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			/** TODO
			String str;
			str = e.getActionCommand();
			if (str.equals(mod[0])) {
				gui.selectedMod=0;
				AUCText2.setEnabled(false);
				SingleOrDiff = 0;
			} else if (str.equals(mod[1])) {
				gui.selectedMod=3;
				AUCText2.setEnabled(true);
				SingleOrDiff = 1;
			}
			*/
		}
	}

	/**
	 * Handler for button to confirm entry of manual input. Verfies that input
	 * is properly formatted and performs variance analysis
	 */
	class VarAnalysisBtnListener implements ActionListener {
		public boolean checkinput() {
			String readerInput = manualInReader.getText();
			String normalInput = manualInNormal.getText();
			String diseaseInput = manualInDisease.getText();
			String componentsInput = compText.getText();
			String auc1Input = AUCText1.getText();
			String auc2Input = AUCText2.getText();

			if (readerInput.equals("") || normalInput.equals("")
					|| diseaseInput.equals("") || componentsInput.equals("")
					|| auc1Input.equals("")
					|| (auc2Input.equals("") && SingleOrDiff == 1)) {
				JFrame frame = lst.getFrame();
				JOptionPane.showMessageDialog(frame,
						"The input is not complete", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				String[] tempComp = componentsInput.split(",");
				if (tempComp.length != numberOfComps[selectedManualComp]) {
					JFrame frame = lst.getFrame();
					String strtemp = "The "
							+ com[selectedManualComp]
							+ " components has "
							+ Integer
									.toString(numberOfComps[selectedManualComp])
							+ " elements!\n";
					JOptionPane.showMessageDialog(frame, strtemp, "Error",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
			return true;
		}

		public void actionPerformed(ActionEvent e) {

			if (!checkinput())
				return;
			Reader = Integer.parseInt(manualInReader.getText());
			Normal = Integer.parseInt(manualInNormal.getText());
			Disease = Integer.parseInt(manualInDisease.getText());

			String[] temp = compText.getText().split(",");
			double[] data = new double[temp.length];
			for (int i = 0; i < numberOfComps[selectedManualComp]; i++) {
				data[i] = Double.valueOf(temp[i]);
			}
			auc[0] = Double.valueOf(AUCText1.getText());
			auc[1] = auc[0];
			if (SingleOrDiff == 1)
				auc[1] = Double.valueOf(AUCText2.getText());
			double[][] tempData = new double[][]{data, data, data, data};
			record = new DBRecord(tempData, selectedManualComp, Reader, Normal,
					Disease, auc);
			gui.StatPanel1.setStatPanel();
			gui.StatPanel1.setTable1();
			gui.SizePanel1.setSizePanel();
		}
	}

}
