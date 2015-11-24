/**
 * GUInterface.java
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
import java.awt.*;
import java.io.*;
import java.lang.Math;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import mrmc.chart.BarGraph;
import mrmc.chart.StudyDesignPlot;
import mrmc.chart.ROCCurvePlot;
import mrmc.core.MRMC;
import mrmc.core.DBRecord;
import mrmc.core.InputFile;
import mrmc.core.Matrix;
import mrmc.core.StatTest;

import org.jfree.ui.RefineryUtilities;

/**
 * This class describes the graphic interface. From top to bottom, the GUI
 * includes <br>
 * 1. Menu bar (References, About, Manual) <br>
 * 2. Input Panel, which uses card layout and has 3 cards <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;1) database as input <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;2) pilot study or raw data input <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;3) manual input components <br>
 * 3. a label of AUC values, size of the study, etc. <br>
 * 4. a table with all components of variance for the origianl study <br>
 * 5. Sizing panel <br>
 * 6. a table with all components of variance for the resulting study <br>
 * 7. a label of statistical analysis resutls <br>
 * 8. database summary panel <br>
 * 
 * <br>
 * 
Workflow possibilities are determined by {@link #selectedInput} <br>
1. If selectedInput == "IMRMC" then reader study data is from .imrmc file: <br>
Click the Browse button ({@link InputFileCard.brwsButtonListener brwsButtonListener}) 
<ul>
  <li> Resets GUI. 
  <li> Browses for reader study .imrmc file with file chooser.
  <li> Creates {@link mrmc.core.InputFile} object from .imrmc file <br>
  ---- Object contains IDs for readers, cases, modalities
  ---- Object contains core data structures {@link mrmc.core.InputFile#keyedData keyedData}
          and {@link mrmc.core.InputFile#truthVals truthVals}
</ul>
Click the Variance Analysis Button ({@link InputFileCard.varAnalysisListener})
 * 
 * <br>
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
@SuppressWarnings("unused")
public class GUInterface {
	
	private GUInterface thisGUI = this;
	public MRMC MRMCobject;
	public InputStartCard InputStartCard;
	public InputFileCard InputFileCard;
	public InputSummaryCard InputSummaryCard;
	private ManualCard MC;
	public File inputfileDirectory = null;   //input file last time visit directory
	public File outputfileDirectory = null;   //input file last time visit directory

	/**
	 * InputFile1 {@link mrmc.core.InputFile}
	 */
	InputFile InputFile1 = new InputFile();
	/**
	 * DBrecord object holds all the processed info related to a reader study
	 */
	public DBRecord DBRecordStat = new DBRecord(this);
	public DBRecord DBRecordSize = new DBRecord(this);
	public int resetcall = 0 ;
	public final static int USE_MLE = 1;
	public final static int NO_MLE = 0;
	public static String summaryfilename="";
	/**
	 * These strings describe the different input methods
	 * @see #selectedInput
	 */
	public final static String DescInputModeOmrmc = "Summary info from a reader study";
	public final static String DescInputModeImrmc = "Reader study data";
	public final static String DescInputChooseMode = "Please choose input file mode";

	/**
	 * <code> selectedInput </code> determines the workflow: <br>
	 * ----<code>DescInputModeOmrmc</code> = ".omrmc file: Summary info from a reader study" <br>
	 * ---- <br>
	 * ----<code>DescInputModeImrmc</code> = ".imrmc file: Reader study data" <br>
	 * ---- <br>
	 * ----<code>DescInputModeManual</code> = "Manual input" <br>
 	 * 
 	 */
	public static String selectedInput = DescInputChooseMode;

	/**
	 * the panel that uses CardLayout. There are three cards for three different input.
	 * @see #selectedInput
	 */
	JPanel InputPane;
	/**
	 * the panel that shares different manual input components
	 */
	JPanel manual3;
	/**
	 * {@link mrmc.gui.SizePanel}
	 */
	public StatPanel StatPanel1;
	/**
	 * {@link mrmc.gui.StatPanel}
	 */
	SizePanel SizePanel1;

	public final static String NO_MOD = "NO_MOD";
	private int selectedDB = 0;
	private int selectedSummary = 0;
	/** TODO
	 * Do we need this flag here?
	 */
	public boolean hasNegative = false;

	DecimalFormat twoDec = new DecimalFormat("0.00");
	DecimalFormat threeDec = new DecimalFormat("0.000");
	DecimalFormat threeDecE = new DecimalFormat("0.000E0");
	DecimalFormat fourDec = new DecimalFormat("0.0000");

 	/**
		 * Sets all GUI components to their default values
		 */
		public void resetGUI() {
			InputFile1.resetInputFile();
			resetcall = 1;
			InputFileCard.resetInputFileCard();
			InputSummaryCard.resetInputSummaryCard();
			resetcall = 0 ;
			StatPanel1.resetStatPanel();
			SizePanel1.resetSizePanel();
	
			StatPanel1.enableTabs();

		}

	/**
	 * Displays window containing large text area
	 * 
	 * @return TextArea
	 */
	public JTextArea genFrame() {
		JFrame descFrame = new JFrame();
		descFrame.getRootPane()
				.setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
		String str = "";
		JTextArea desc = new JTextArea(str, 18, 40);
		JScrollPane scrollPane = new JScrollPane(desc,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		descFrame.getContentPane().add(scrollPane);
		desc.setLineWrap(true);
		desc.setEditable(false);
		descFrame.pack();
		descFrame.setVisible(true);
		return desc;
	}



	/**
	 * 
	 * @return {@link #selectedInput}
	 */
	public String getSelectedInput() {
		return selectedInput;
	}




	/**
	 * Gets whether single modality or difference between modalities is being
	 * analyzed when using manually input components
	 * 
	 * @return Whether single modality or difference
	 */
	public int getSingleOrDiff() {
		return MC.getSingleOrDiff();
	}

	/**
	 * Gets which decomposition of the variance components is being used
	 * 
	 * @return Which decomposition is being used
	 */
	public int getSelectedManualComp() {
		return MC.getSelectedManualComp();
	}



	/**
	 * Sole constructor, builds and displays the GUI. <br>
	 * ----Creates {@link mrmc.gui.InputFileCard} 
	 * <br>
	 * CALLED FROM: {@link mrmc.core.MRMC#init}
	 * 
	 * 
	 * @param MRMCobjectTemp Application frame
	 * @param cp Container for GUI elements
	 * 
	 */
	public GUInterface(MRMC MRMCobjectTemp, Container cp) {
		MRMCobject = MRMCobjectTemp;
		SizePanel1 = new SizePanel(this);
		StatPanel1 = new StatPanel(MRMCobject.getFrame(), DBRecordStat);

		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

		// Select Input Pane
		JPanel inputSelectPane = new JPanel();
		inputSelectPane.setLayout(new FlowLayout());
		// Add Text
		JLabel inLabel = new JLabel("Select an input method: ");
		// Add Pull-down select input method
//		String comboBoxItems[] = { DB, Pilot, Manual };
//		String comboBoxItems[] = { Pilot, Manual };
		String comboBoxItems[] = { DescInputChooseMode };
		// Add Reset button
		JComboBox<String> cb = new JComboBox<String>(comboBoxItems);
		JComboBox<String> chooseMod = new JComboBox<String>();
		cb.addItem(DescInputModeImrmc);
		cb.addItem(DescInputModeOmrmc);
		cb.setEditable(false);
		cb.setSelectedIndex(0);
		cb.addActionListener(new inputModListener());
		JButton buttonReset = new JButton("Reset");
		buttonReset.addActionListener(new ResetListener());
		inputSelectPane.add(inLabel);
		inputSelectPane.add(cb);
		inputSelectPane.add(buttonReset);

		// Input method determines panel card to show
		//
		// create pilot/raw study panel
		JPanel JPanel_InputFileCard = new JPanel();
		InputFileCard = new InputFileCard(JPanel_InputFileCard, this);
		
		// create start panel
		JPanel JPanel_InputStartCard = new JPanel();
		InputStartCard = new InputStartCard(JPanel_InputStartCard, this);

		/*// create manual panel
		JPanel InputCardManual = new JPanel();
		MC = new ManualCard(InputCardManual, this, MRMCobject);*/
		
		// create summary panel
		JPanel JPanel_InputSummaryCard = new JPanel();
		InputSummaryCard = new InputSummaryCard(JPanel_InputSummaryCard, this);
		// create DB panel
// TODO	JPanel CardInputModeDB = new JPanel();
// TODO	DBC = new DBCard(CardInputModeDB, this, MRMCobject);

		// ***********************************************************************
		// ***********Create the panel that contains the "cards".*****************
		// ***********************************************************************
		InputPane = new JPanel(new CardLayout());
//		inputCards.add(CardInputModeDB, DescInputModeDB);
		InputPane.add(JPanel_InputStartCard, DescInputChooseMode);
		InputPane.add(JPanel_InputFileCard, DescInputModeImrmc);
		InputPane.add(JPanel_InputSummaryCard, DescInputModeOmrmc);
		

		/*
		 * Initialize all the elements of the GUI
		 */
		StatPanel1.resetStatPanel();
		StatPanel1.resetTable1();
		SizePanel1.resetSizePanel();

		JPanel panelSep = new JPanel(new BorderLayout());
		panelSep.setBorder(BorderFactory.createEmptyBorder(1, // top
				1, // left
				0, // bottom
				1)); // right
		panelSep.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.CENTER);
		JPanel panelSep2 = new JPanel(new BorderLayout());
		panelSep2.setBorder(BorderFactory.createEmptyBorder(10, // top
				1, // left
				0, // bottom
				1)); // right
		panelSep2.add(new JSeparator(JSeparator.HORIZONTAL),
				BorderLayout.CENTER);
		JPanel panelSep3 = new JPanel(new BorderLayout());
		panelSep3.setBorder(BorderFactory.createEmptyBorder(10, // top
				1, // left
				0, // bottom
				1)); // right
		panelSep3.add(new JSeparator(JSeparator.HORIZONTAL),
				BorderLayout.CENTER);

		/*
		 * This panel should allow for writing results of the current analysis to the hard drive.
		 */
		JPanel panelSummary = new JPanel();
		panelSummary.add(new JLabel("GUI Summary:"));
		JButton saveGUI = new JButton("Save to File");
		saveGUI.addActionListener(new SaveGUIButtonListener());
		panelSummary.add(saveGUI);

		cp.add(inputSelectPane);
		cp.add(InputPane);
		cp.add(panelSep);
		cp.add(StatPanel1.JPanelStat);
		// Hides the trial sizing table
//		 cp.add(tabbedPane2);
//		 cp.add(panelStat11);
		cp.add(panelSep2);

//		cp.add(SizePanelRow1);
//		cp.add(SizePanelRow2);
//		cp.add(SizePanelRow3);
//		cp.add(SizePanelRow4);
//		cp.add(SizePanelRow5);
//		cp.add(SizePanelRow6);
		cp.add(SizePanel1.JPanelSize);
		cp.add(panelSep3);
		/*
		 * This panel should allow for writing results of the current analysis to the hard drive.
		 */
		cp.add(panelSummary);
	}



	/**	 * Handler for button to save current GUI state to file
	 */
	class SaveGUIButtonListener implements ActionListener {

	//	@Override
		//public String sFileName="";
		public void actionPerformed(ActionEvent e) {
			double aaa=DBRecordStat.totalVar;
			if( DBRecordStat.totalVar > 0.0) {
				String report = "";
	            DateFormat dateForm = new SimpleDateFormat("yyyyMMddHHmm");
				Date currDate = new Date();
				final String fileTime = dateForm.format(currDate);
				String FileName=InputFile1.filename;
				FileName= FileName.substring(0,FileName.lastIndexOf("."));
				String summaryfilenamewithpath = FileName+"MRMCsummary"+fileTime+".omrmc";
				summaryfilename = summaryfilenamewithpath.substring(FileName.lastIndexOf("\\")+1);
				if (selectedInput == DescInputChooseMode) {
					report = SizePanel1.genReport(InputFile1);
				} else {
					report = SizePanel1.genReport(InputFile1);
				}
	
				try {
					JFileChooser fc = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter(
							"iMRMC Summary Files (.omrmc or csv)", "csv","omrmc");
					fc.setFileFilter(filter);
					if (outputfileDirectory!=null){
						 fc.setSelectedFile(new File(outputfileDirectory+"\\"+summaryfilename));						
					}						
					else					
					    fc.setSelectedFile(new File(summaryfilenamewithpath));
					int fcReturn = fc.showSaveDialog((Component) e.getSource());
					if (fcReturn == JFileChooser.APPROVE_OPTION) {
						File f = fc.getSelectedFile();
						if (!f.exists()) {
							f.createNewFile();
						}
						FileWriter fw = new FileWriter(f.getAbsoluteFile());
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(report);
						bw.close();
						outputfileDirectory = fc.getCurrentDirectory();
					    String savedfilename = fc.getSelectedFile().getName();
						JOptionPane.showMessageDialog(
								thisGUI.MRMCobject.getFrame(),"The summary has been succeed export to "+outputfileDirectory+ " !\n"+ "Filename = " +savedfilename, 
								"Exported", JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} 
				

	      /*      try {
					FileWriter fw = new FileWriter(summaryfilenamewithpath);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(report);
					bw.close();
					JOptionPane.showMessageDialog(
							thisGUI.MRMCobject.getFrame(),"The summary has been succeed export to input file directory!"+"\n"+"Filename="+summaryfilename, 
							"Exported", JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
	            
			}else{
				JOptionPane.showMessageDialog(thisGUI.MRMCobject.getFrame(),
						"Pilot study data has not yet been analyzed.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
						
		}
	}

	/**
	 * Handler for drop down menu to select data input source
	 * This changes the pane, what the user sees
	 * It can either be the pane for DB, FILE, MANUAL
	 */
	class inputModListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {

			JComboBox<?> cb = (JComboBox<?>) evt.getSource();
			selectedInput = (String) cb.getSelectedItem();

			CardLayout cl = (CardLayout) (InputPane.getLayout());
			cl.show(InputPane, selectedInput);
			
			resetGUI();
		}
	}

	/**
	 * Handler for input reset button
	 */
	class ResetListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			resetGUI();
		}
	}

	/**
	 * Handler for database description button, displays in a separate window
	 */
	class descButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JFrame descFrame = new JFrame();

			descFrame.getRootPane().setWindowDecorationStyle(
					JRootPane.PLAIN_DIALOG);
			JTextArea desc = new JTextArea("TODO: This should have something.",
					18, 40);
			JScrollPane scrollPane = new JScrollPane(desc,
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			descFrame.getContentPane().add(scrollPane);
			desc.setLineWrap(true);
			desc.setEditable(false);
			descFrame.pack();
			descFrame.setVisible(true);

		}
	}

	/**
	 * Handler for drop-down menu to select a particular record in the internal
	 * database
	 */
	class dbActionListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			JComboBox<?> cb = (JComboBox<?>) evt.getSource();
			selectedDB = (int) cb.getSelectedIndex();
		}
	}

	/**
	 * Handler for radio buttons to select wither analyzing single modality or
	 * difference when using database input
	 */
	class SummarySelListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String str = e.getActionCommand();
			if (str.equals("Single Modality")) {
				selectedSummary = 0;
			}
			if (str.equals("Difference")) {
				selectedSummary = 1;
			}
		}
	}

	/**
	 * Handler for radio buttons to select whether or not to use MLE (bias) in
	 * database summary
	 */
	class MLESelListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			double x = 1/0;

			/*
			TODO
			
			String str = e.getActionCommand();
			if (str.equals("Yes")) {
				
				DBRecordStat.flagMLE = useMLE;
			}
			if (str.equals("No")) {
				DBRecordStat.flagMLE = useMLE;
			}
			*/
			 
			/*
			 * revised 
			*/

				String str = e.getActionCommand();
				if (str.equals("Yes")) {
					DBRecordStat.flagMLE = USE_MLE;
				}
				if (str.equals("No")) {
					DBRecordStat.flagMLE = NO_MLE;
				}

		}
	}











}
