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


import java.awt.Font;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel for selecting modalities, displaying charts of data statistics, and
 * performing MRMC variance analysis with data from pilot study raw data files.
 * 
 * 
 * @author Xin He, Ph.D
 * @author Brandon D. Gallas, Ph.D
 * @author Qi Gong, MS
 * @author Rohan Pathare
 */
public class InputStartCard {



	/**
	 * Sole constructor. Creates and initializes GUI elements <br>
	 * <br>
	 * CALLED BY: {@link mrmc.gui.GUInterface#GUInterface GUInterface constructor}
	 * 
	 * @param CardInputModeImrmc Panel containing elements for raw study input card
	 * @param GUInterface_temp Application's instance of the GUI
	 */
	public InputStartCard(JPanel CardInputModeImrmc, GUInterface GUInterface_temp) {
		/*
		 * Elements of RawStudyCardRow1
		 */
		// Browse for input file
		JLabel welcomeLabel = new JLabel("Welcome to use iMRMC software");
		welcomeLabel.setFont(new Font("Serif", Font.BOLD, 30));
		/*
		 * Create RawStudyCardRow2
		 */
		JPanel RawStudyCardRow1 = new JPanel();
		RawStudyCardRow1.add(welcomeLabel);

		JLabel welcomeLabel2 = new JLabel("Please choose one kind of input file");
		welcomeLabel2.setFont(new Font("Serif", Font.BOLD, 30));
		JPanel RawStudyCardRow2 = new JPanel();
		RawStudyCardRow2.add(welcomeLabel2);


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

}
