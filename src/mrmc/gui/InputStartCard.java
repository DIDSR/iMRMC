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
 * 
 * @author Brandon D. Gallas, Ph.D
 * @author Qi Gong, MS
 * @author Rohan Pathare
 * @author Xin He, Ph.D
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
