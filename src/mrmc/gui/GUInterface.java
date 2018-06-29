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
import mrmc.chart.exportToFile;
import mrmc.core.MRMC;
import mrmc.core.DBRecord;
import mrmc.core.InputFile;
import mrmc.core.Matrix;
import mrmc.core.StatTest;

import org.jfree.ui.RefineryUtilities;

import roemetz.core.validateFunction;

/*import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;*/


import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;



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
	public static File outputfileDirectory = null;   //input file last time visit directory

	/**
	 * InputFile1 {@link mrmc.core.InputFile}
	 */
	public InputFile InputFile1 = new InputFile();
	/**
	 * All analysis output files directory {@link mrmc.core.InputFile}
	 */
	
	public String allAnalysisOutput="";
	/**
	 * DBrecord object holds all the processed info related to a reader study
	 */
	public DBRecord DBRecordStat = new DBRecord(this);
	public DBRecord DBRecordSize = new DBRecord(this);
	private DBRecord DBRecordStatAll = new DBRecord(this);
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
			DBRecordStat.resetDBRecord();
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
		JButton saveStatAnalysis = new JButton("Save Stat Analysis");
		saveStatAnalysis.addActionListener(new SaveStatAnalysisButtonListener());
		
		panelSummary.add(saveStatAnalysis);

		JButton saveSize = new JButton("Save Size Analysis");
		saveSize.addActionListener(new SaveGUISizeListener());
		
		panelSummary.add(saveSize);

		//Use Save Stat Analysis replace this button 
		//JButton saveStat = new JButton("Save Stat");
		//saveStat.addActionListener(new SaveGUIStatListener());	
		//panelSummary.add(saveStat);
		
		JButton saveAll = new JButton("Analysis All Modalities");
		saveAll.addActionListener(new SaveAllStatListener());
		
		panelSummary.add(saveAll);
		
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



	/**	 * Handler for button to save current GUI to file
	 */
	class SaveStatAnalysisButtonListener implements ActionListener {

	//	@Override
		//public String sFileName="";
		public void actionPerformed(ActionEvent e) {
			double aaa=DBRecordStat.totalVar;
			if( DBRecordStat.totalVar > 0.0) {
				DBRecordStat.InputFile1 = InputFile1;
	    		String head =  "inputFile,date,iMRMCversion,NR,N0,N1,modalityA,modalityB,UstatOrMLE,AUCA,varAUCA,AUCB,varAUCB,AUCAminusAUCB,varAUCAminusAUCB,"
	    				+"pValueNormal,botCInormal,topCInormal,rejectNormal,dfBDG,pValueBDG,botCIBDG,topCIBDG,rejectBDG,dfHillis,pValueHillis,botCIHillis,topCIHillis,rejectHillis";
				String reportData = head +"\r\n";
				String reportSummary = "";
				String reportResult1 = "";
				String reportResult2 = "";
	            DateFormat dateForm = new SimpleDateFormat("yyyyMMddHHmm");
				Date currDate = new Date();
				final String fileTime = dateForm.format(currDate);
				//String FileName=InputFile1.filename;
				//FileName= FileName.substring(0,FileName.lastIndexOf("."));
				//String summaryfilenamewithpath = FileName+"MRMCsummary"+fileTime+".omrmc";
				//summaryfilename = summaryfilenamewithpath.substring(FileName.lastIndexOf("\\")+1);
				String FileName=InputFile1.fileName;
				//String FilePathAndName=InputFile1.filePathAndName;
				String FilePath = InputFile1.filePath;
				String FilePathName = InputFile1.filePathAndName;
				//FilePathAndName= FilePathAndName.substring(0,FilePathAndName.lastIndexOf("."));
				FileName= FileName.substring(0,FileName.lastIndexOf("."));
				//summaryfilename = FileName+"MRMCsize"+fileTime+".omrmc";
				try {
					
					
					JFileChooser fc = new JFileChooser();
					String exportFolderName = FileName + "StatResults" + fileTime;
					if (outputfileDirectory!=null){
						fc.setSelectedFile(new File(outputfileDirectory+"//"+exportFolderName));						
					}else{
						fc.setSelectedFile(new File(FilePath+"//"+exportFolderName));
					}
					int fcReturn = fc.showSaveDialog((Component) e.getSource());
					File outputPackage = fc.getSelectedFile();					
					if(!outputPackage.exists() && !outputPackage.isDirectory()) 
						outputPackage.mkdir();		
					File fSummary = new File (outputPackage +"//"+"MRMCSummary.omrmc" );
					File fData = new File (outputPackage +"//" + "MRMCStat.csv");
					String fPdfPathName = outputPackage +"//"+"MRMCResult.pdf";
					File fPdf = new File(fPdfPathName);
					outputfileDirectory = fc.getCurrentDirectory();	
					PdfPTable[] pdfAUCTable = new PdfPTable[3];
					for ( int i = 0; i<3;i++ ){
				        pdfAUCTable[i] = new PdfPTable(5);
				        pdfAUCTable[i].setTotalWidth(new float[]{300,550,550,250,350});
					}

					if (fcReturn == JFileChooser.APPROVE_OPTION) {
						//File f = fc.getSelectedFile();
						//if (!f.exists()) {
						//	f.createNewFile();
						//}
						//String savedFileName = f.getPath();
						//summaryfilename = savedFileName.substring(savedFileName.lastIndexOf("\\")+1);
						summaryfilename = fSummary.getName();
						reportSummary = reportSummary + "MRMC summary statistics from " +MRMC.versionname + "\r\n";
						reportSummary = reportSummary + "Summary statistics based on input file named:" + "\r\n";
						reportResult1 = "This file," + "\r\n" + fPdf.getAbsolutePath() + ",\r\n" ;
						reportResult1 = reportResult1 + "shows the MRMC ROC analysis results for the data in:" + "\r\n";
						reportResult1 = reportResult1 + FilePathName + "\r\n";
						reportResult1 = reportResult1 + "using " + MRMC.versionname + ", https://github.com/DIDSR/iMRMC/releases" + "\r\n" + "\r\n";
						reportSummary = reportSummary + InputFile1.filePathAndName + "\r\n" + "\r\n";
						if (selectedInput == DescInputChooseMode) {
							// generate summary string
							reportSummary = exportToFile.exportSummary(reportSummary, DBRecordStat);
							reportSummary = exportToFile.exportStatPanel(reportSummary, DBRecordStat, StatPanel1);
							reportSummary = exportToFile.exportTable1(reportSummary, DBRecordStat);
							reportSummary = exportToFile.exportTable2(reportSummary, DBRecordStat);
							// generate pdf result 
							reportResult1 = exportToFile.pdfResult1(reportResult1, DBRecordStat);
							pdfAUCTable = exportToFile.pdfTable(pdfAUCTable, DBRecordStat);
							reportResult2 = exportToFile.pdfResult2(DBRecordStat);
							// generate one line data string
							reportData = exportToFile.exportStat(reportData, DBRecordStat, fileTime);
						} else {
							// generate summary string
							reportSummary = exportToFile.exportSummary(reportSummary, DBRecordStat);
							reportSummary = exportToFile.exportStatPanel(reportSummary, DBRecordStat, StatPanel1);
							reportSummary = exportToFile.exportTable1(reportSummary, DBRecordStat);
							reportSummary = exportToFile.exportTable2(reportSummary, DBRecordStat);
							// generate pdf result 
							reportResult1 = exportToFile.pdfResult1(reportResult1, DBRecordStat);
							pdfAUCTable = exportToFile.pdfTable(pdfAUCTable, DBRecordStat);
							reportResult2 = exportToFile.pdfResult2(DBRecordStat);
							// generate one line data string
							reportData = exportToFile.exportStat(reportData, DBRecordStat, fileTime);
						}
						// write summary to disk
						FileWriter fwSummary = new FileWriter(fSummary.getAbsoluteFile());
						BufferedWriter bwSummary = new BufferedWriter(fwSummary);
						bwSummary.write(reportSummary);
						bwSummary.close();
						// write one line data to disk
						FileWriter fwData = new FileWriter(fData.getAbsoluteFile());
						BufferedWriter bwData = new BufferedWriter(fwData);
						bwData.write(reportData);
						bwData.close();
						// output pdf
						Document document = new Document();
						PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fPdfPathName));
						document.open();
				        document.add(new Paragraph(reportResult1));
				        for (int i = 0; i<3;i++ ){
				        	document.add(pdfAUCTable[i]);
				        	 document.add(new Paragraph("\r\n"));
				        }
				        document.add(new Paragraph(reportResult2));
				        document.close();
						outputfileDirectory = fc.getCurrentDirectory();
					    String savedfilename = fc.getSelectedFile().getName();
						JOptionPane.showMessageDialog(
								thisGUI.MRMCobject.getFrame(),"The stat analysis results and summary has been succeed export to "+outputfileDirectory+ " !\n"+ "Foldername = " +outputPackage, 
								"Exported", JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (DocumentException e1) {
					// TODO Auto-generated catch block
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

	
	/**	 * Handler for button to save current GUI size to file
	 */
	class SaveGUISizeListener implements ActionListener {

	//	@Override
		//public String sFileName="";
		public void actionPerformed(ActionEvent e) {
			if( DBRecordSize.totalVar > 0.0) {
				DBRecordSize.InputFile1 = InputFile1;
				String head =  "inputFile,date,iMRMCversion,modalityA,modalityB,NR,N0,N1,NG,EffectiveSize,SignificanceLevel,UstatOrMLE,NormalPower,BDGDf,BDGSE,BDGPower,HillisDf,HillisPower";
				String reportcsv = head +"\r\n";
				String reportomrmc = "";
	            DateFormat dateForm = new SimpleDateFormat("yyyyMMddHHmm");
				Date currDate = new Date();
				final String fileTime = dateForm.format(currDate);
				//String FileName=InputFile1.filename;
				//FileName= FileName.substring(0,FileName.lastIndexOf("."));
				//String sizeFilenamewithpath = FileName+"MRMCSize"+fileTime+".omrmc";
				//String sizeFilename = sizeFilenamewithpath.substring(sizeFilenamewithpath.lastIndexOf("\\")+1);	
				String FileName=InputFile1.fileName;
				String FilePathAndName=InputFile1.filePathAndName;
				String FilePath = InputFile1.filePath;
				FilePathAndName= FilePathAndName.substring(0,FilePathAndName.lastIndexOf("."));
				FileName= FileName.substring(0,FileName.lastIndexOf("."));
				//String sizeFilenamewithpath = FilePathAndName+"MRMCSize"+fileTime+".omrmc";
				String sizeFilename = FileName+"MRMCsize"+fileTime+".omrmc";
				try {
					
					JFileChooser fc = new JFileChooser();
					String exportFolderName = FileName + "SizeResults" + fileTime;
					if (outputfileDirectory!=null){
						fc.setSelectedFile(new File(outputfileDirectory+"//"+exportFolderName));						
					}else{
						fc.setSelectedFile(new File(FilePath+"//"+exportFolderName));
					}
					int fcReturn = fc.showSaveDialog((Component) e.getSource());
					File outputPackage = fc.getSelectedFile();					
					if(!outputPackage.exists() && !outputPackage.isDirectory()) 
						outputPackage.mkdir();		
					File fomrmc = new File (outputPackage +"//"+"MRMCsize.omrmc" );
					File fcsv = new File (outputPackage +"//" + "MRMCsize.csv");
					outputfileDirectory = fc.getCurrentDirectory();		
					
					
					
				/*	JFileChooser fc = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter(
							"iMRMC Summary Files (.omrmc or csv)", "csv","omrmc");
					fc.setFileFilter(filter);
					if (outputfileDirectory!=null){
						// fc.setSelectedFile(new File(outputfileDirectory+"\\"+sizeFilename));			
						fc.setSelectedFile(new File(outputfileDirectory+"//"+sizeFilename));			
					}						
					else					
					    fc.setSelectedFile(new File(sizeFilenamewithpath));
					int fcReturn = fc.showSaveDialog((Component) e.getSource());*/
					if (fcReturn == JFileChooser.APPROVE_OPTION) {
						/*File f = fc.getSelectedFile();
						if (!f.exists()) {
							f.createNewFile();
						}*/
						//String savedFileName = f.getPath();
						//sizeFilename = savedFileName.substring(savedFileName.lastIndexOf("\\")+1);
						sizeFilename = fomrmc.getName();
						reportomrmc = reportomrmc + "MRMC size statistics from " +MRMC.versionname + "\r\n";
						reportomrmc = reportomrmc + "Size statistics written to file named:" + "\r\n";
						reportomrmc = reportomrmc + sizeFilename + "\r\n" + "\r\n";
						
						if (selectedInput == DescInputChooseMode) {
							// generate omrmc string
							reportomrmc = exportToFile.exportSizePanel(reportomrmc, DBRecordSize, SizePanel1);
							reportomrmc = exportToFile.exportTable1(reportomrmc, DBRecordSize);
							// generate csv string
							reportcsv = exportToFile.exportSizeCsv(reportcsv, DBRecordSize, SizePanel1,fileTime);
						} else {
							// generate omrmc string
							reportomrmc = exportToFile.exportSizePanel(reportomrmc, DBRecordSize, SizePanel1);
							reportomrmc = exportToFile.exportTable1(reportomrmc, DBRecordSize);
							// generate csv string
							reportcsv = exportToFile.exportSizeCsv(reportcsv, DBRecordSize, SizePanel1,fileTime);
						}
						
						// write omrmc to disk
						FileWriter fwomrmc = new FileWriter(fomrmc.getAbsoluteFile());
						BufferedWriter bwomrmc = new BufferedWriter(fwomrmc);
						bwomrmc.write(reportomrmc);
						bwomrmc.close();
						// write one line data to disk
						FileWriter fwcsv = new FileWriter(fcsv.getAbsoluteFile());
						BufferedWriter bwcsv = new BufferedWriter(fwcsv);
						bwcsv.write(reportcsv);
						bwcsv.close();
						outputfileDirectory = fc.getCurrentDirectory();
					    String savedfilename = fc.getSelectedFile().getName();
						JOptionPane.showMessageDialog(
								thisGUI.MRMCobject.getFrame(),"The size analysis results and summary has been succeed export to "+outputfileDirectory+ " !\n"+ "Foldername = " +outputPackage, 
								"Exported", JOptionPane.INFORMATION_MESSAGE);
						/*FileWriter fw = new FileWriter(f.getAbsoluteFile());
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(report);
						bw.close();
						outputfileDirectory = fc.getCurrentDirectory();
					    String savedfilename = fc.getSelectedFile().getName();
						JOptionPane.showMessageDialog(
								thisGUI.MRMCobject.getFrame(),"The size result has been succeed export to "+outputfileDirectory+ " !\n"+ "Filename = " +savedfilename, 
								"Exported", JOptionPane.INFORMATION_MESSAGE);*/
					}
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} 
	            
			}else{
				JOptionPane.showMessageDialog(thisGUI.MRMCobject.getFrame(),
						"Size study has not yet been predicted.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
						
		}
	}
	
	
	
	/**	 * Handler for button to save current GUI state to file
	 */
	/* Use Save Stat Analysis replace this button 
	class SaveGUIStatListener implements ActionListener {

	//	@Override
		//public String sFileName="";
		public void actionPerformed(ActionEvent e) {
			if( DBRecordStat.totalVar > 0.0) {
				DBRecordStat.InputFile1 = InputFile1;
	    		String head =  "inputFile,date,iMRMCversion,NR,N0,N1,modalityA,modalityB,UstatOrMLE,AUCA,varAUCA,AUCB,varAUCB,AUCAminusAUCB,varAUCAminusAUCB,"
	    				+"pValueNormal,botCInormal,topCInormal,rejectNormal,dfBDG,pValueBDG,botCIBDG,topCIBDG,rejectBDG,dfHillis,pValueHillis,botCIHillis,topCIHillis,rejectHillis";
				String report = head +"\r\n";
	            DateFormat dateForm = new SimpleDateFormat("yyyyMMddHHmm");
				Date currDate = new Date();
				final String fileTime = dateForm.format(currDate);
				//String FileName=InputFile1.filename;
				//FileName= FileName.substring(0,FileName.lastIndexOf("."));
				//String sizeFilenamewithpath = FileName+"MRMCStat"+fileTime+".csv";
				//String sizeFilename = sizeFilenamewithpath.substring(sizeFilenamewithpath.lastIndexOf("\\")+1);	
				String FileName=InputFile1.fileName;
				String FilePathAndName=InputFile1.filePathAndName;
				FilePathAndName= FilePathAndName.substring(0,FilePathAndName.lastIndexOf("."));
				FileName= FileName.substring(0,FileName.lastIndexOf("."));
				String saveStatFilePathAndName = FilePathAndName+"MRMCStat"+fileTime+".csv";
				String saveStatFileName = FileName+"MRMCStat"+fileTime+".csv";
				try {
					JFileChooser fc = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter(
							"iMRMC Summary Files (.csv)", "csv");
					fc.setFileFilter(filter);
					if (outputfileDirectory!=null){
						// fc.setSelectedFile(new File(outputfileDirectory+"\\"+saveStatFileName));		
						fc.setSelectedFile(new File(outputfileDirectory+"//"+saveStatFileName));			
					}						
					else					
					    fc.setSelectedFile(new File(saveStatFilePathAndName));
					int fcReturn = fc.showSaveDialog((Component) e.getSource());
					if (fcReturn == JFileChooser.APPROVE_OPTION) {
						File f = fc.getSelectedFile();
						if (!f.exists()) {
							f.createNewFile();
						}
						String savedFileName = f.getPath();
						if (selectedInput == DescInputChooseMode) {
							report = exportToFile.exportStat(report, DBRecordStat, fileTime);
						} else {
							report = exportToFile.exportStat(report, DBRecordStat, fileTime);
						}
						
						
						FileWriter fw = new FileWriter(f.getAbsoluteFile());
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(report);
						bw.close();
						outputfileDirectory = fc.getCurrentDirectory();
					    String savedfilename = fc.getSelectedFile().getName();
						JOptionPane.showMessageDialog(
								thisGUI.MRMCobject.getFrame(),"The size result has been succeed export to "+outputfileDirectory+ " !\n"+ "Filename = " +savedfilename, 
								"Exported", JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} 
	            
			}else{
				JOptionPane.showMessageDialog(thisGUI.MRMCobject.getFrame(),
						"Pilot study data has not yet been analyzed.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
						
		}
	}
	*/
	
	/**	 * Handler for button to save all stat analysis result to file
	 */
	public class SaveAllStatListener implements ActionListener {

	//	@Override
		//public String sFileName="";
		private String BDGout,BCKout,DBMout,ORout,MSout;
     	public void actionPerformed(ActionEvent e) {
			exportResult();

		 }
		public void exportResult() {
			if (InputFile1.isLoaded()) {    	// check raw data is loaded
	            DateFormat dateForm = new SimpleDateFormat("yyyyMMddHHmm");
				Date currDate = new Date();
				final String fileTime = dateForm.format(currDate);
				//String fileWholeName=InputFile1.filename;
				//String fileName= fileWholeName.substring(fileWholeName.lastIndexOf("\\")+1,fileWholeName.lastIndexOf("."));
				//String filePath = fileWholeName.substring(0,fileWholeName.lastIndexOf("\\"));
				//String fileWholeName=InputFile1.filename;
				String filePathAndName =InputFile1.filePathAndName;
				filePathAndName = filePathAndName.substring(0,filePathAndName.lastIndexOf("."));	
				// allow user choose output directory. If not define use input file directory
				if (allAnalysisOutput.length()!=0 ){
					filePathAndName = allAnalysisOutput;					
				}else{
					filePathAndName = filePathAndName+ fileTime;
				}
				File outputDir = new File (filePathAndName);				
				if(!outputDir.exists() && !outputDir.isDirectory()) 
					outputDir.mkdir();					
				// create file save all stat analysis result
				String AllStatPath = outputDir+ "//statAnalysis.csv";
				// create file save all stat analysis MLE result
				String AllStatMLEPath = outputDir+ "//statAnalysisMLE.csv";
				// create file save all readers analysis result
				String AllAUCsPath = outputDir+ "//AUCperReader.csv";
				// create file save all readers covariance 
				String AllReaderCovPath = outputDir+ "//readerCovariance.csv";
				// create file save ROC result
				String AllROCPath = outputDir+ "//ROCcurves.csv";
				// create file save BDG table
				String BDGtable = outputDir+ "//BDGtable.csv";
				// create file save BCK table
				String BCKtable = outputDir+ "//BCKtable.csv";
				// create file save DBM table
				String DBMtable = outputDir+ "//DBMtable.csv";
				// create file save OR table
				String ORtable = outputDir+ "//ORtable.csv";
				// create file save MS table
				String MStable = outputDir+ "//MStable.csv";
	            // initial 3 string to save All stat, AUCs and ROC result
	    		String statHead =  "inputFile,date,iMRMCversion,NR,N0,N1,modalityA,modalityB,UstatOrMLE,AUCA,varAUCA,AUCB,varAUCB,AUCAminusAUCB,varAUCAminusAUCB,"
	    				+"pValueNormal,botCInormal,topCInormal,rejectNormal,dfBDG,pValueBDG,botCIBDG,topCIBDG,rejectBDG,dfHillis,pValueHillis,botCIHillis,topCIHillis,rejectHillis";
	    		BDGout =  "modalityA,modalityB,UstatOrMLE,compOrCoeff,M1,M2,M3,M4,M5,M6,M7,M8" +"\r\n";
	    		BCKout =  "modalityA,modalityB,UstatOrMLE,Moments,N,D,ND,R,NR,DR,RND" +"\r\n";
	    		DBMout =  "modalityA,modalityB,UstatOrMLE,Components,R,C,RC,TR,TC,TRC" +"\r\n";
	    		ORout =  "modalityA,modalityB,UstatOrMLE,Components,R,TR,COV1,COV2,COV3,ERROR" +"\r\n";
	    		MSout =  "modalityA,modalityB,UstatOrMLE,Components,R,C,RC,MR,MC,MRC" +"\r\n";
	            String AllStatreport = statHead+"\r\n";
	            String AllStatMLEreport = statHead+"\r\n";
	            String AllAUCsreport = "inputFile,date,iMRMCversion,readerID,N0,N1,modalityA,modalityB,AUCA,varAUCA,AUCB,varAUCB,AUCAminusAUCB,varAUCAminusAUCB,"
	    				+"pValueNormal,botCInormal,topCInormal,rejectNormal,dfBDG,pValueBDG,botCIBDG,topCIBDG,rejectBDG,dfHillis,pValueHillis,botCIHillis,topCIHillis,rejectHillis"+"\r\n";
	            String readerCovReport = "inputFile,data,iMRMCversion" +"\r\n" + InputFile1.fileName + "," + fileTime + ',' + MRMC.versionname + "\r\n";
	            String AllROCreport = "";
				if  (GUInterface.selectedInput == GUInterface.DescInputModeImrmc){
					System.out.println("MRMC Save All Stat button clicked");
					// Create a list for all combination of modality
					int numMod = InputFile1.getModalityIDs().size();
					int modCombination  = calFactorial(numMod)/calFactorial(numMod-2)/calFactorial(2)+numMod;      // find how many combination in total
					String[][] modCombinationList = new String[modCombination] [2];
					String[] rocMod = new String[numMod];
					int count = 0;
					for (String ModalityID : InputFile1.getModalityIDs()) {
						modCombinationList[count][0]= ModalityID;
						modCombinationList[count][1]= "NO_MOD";
						rocMod[count] = ModalityID;
						count++;
					}
					for (int i=0; i<numMod-1; i++) {
						for(int j=i+1 ; j<numMod; j++){
							modCombinationList[count][0]= modCombinationList[i][0];
							modCombinationList[count][1]= modCombinationList[j][0];
							count++;
						}
					}
					
		            //Do simulation for each group of modality, save Stat and reader AUC result
					for (int i = 0; i<count; i ++ ){
						DBRecordStatAll.flagMLE = 0;
						DBRecordStatAll.modalityA = modCombinationList[i][0];
						DBRecordStatAll.modalityB = modCombinationList[i][1];
						if (i<numMod){
							DBRecordStatAll.selectedMod = 0;
						}else{
							DBRecordStatAll.selectedMod = 3;
						}
						// calculate and save Ustat result
						DBRecordStatAll.DBRecordStatFill(InputFile1, DBRecordStatAll);
						AllStatreport = exportToFile.exportStat(AllStatreport, DBRecordStatAll, fileTime);
						AllAUCsreport = exportToFile.exportReaders(AllAUCsreport, DBRecordStatAll,InputFile1, fileTime);
						readerCovReport =  exportToFile.exportReadersCov(readerCovReport, DBRecordStatAll,InputFile1);
						savetable();
						// calculate and save MLE result
						DBRecordStatAll.flagMLE = 1;
						DBRecordStatAll.DBRecordStatFill(InputFile1, DBRecordStatAll);
						AllStatMLEreport = exportToFile.exportStat(AllStatMLEreport, DBRecordStatAll, fileTime);
						savetable();
					}
					
					//Get ROC result
					final ROCCurvePlot roc = new ROCCurvePlot(
							"ROC Curve: All Modality ",
							"FPF (1 - Specificity), legend shows symbols for each modalityID:readerID", "TPF (Sensitivity)",
							InputFile1.generateROCpoints(rocMod),InputFile1.filePathAndName,InputFile1.fileName);
					roc.addData(InputFile1.generatePooledROC(rocMod), "Pooled Average");
					AllROCreport = exportToFile.exportROC(roc.seriesCollection,AllROCreport);
					
					// summary input
				}else{
					DBRecord a = DBRecordStat;
					DBRecordStatAll.AUCs = Matrix.copy(DBRecordStat.AUCs);
					DBRecordStatAll.AUCsReaderAvg = Matrix.copy(DBRecordStat.AUCsReaderAvg);
					DBRecordStatAll.LoadBDG = Matrix.copy(DBRecordStat.LoadBDG);
					DBRecordStatAll.Ndisease = DBRecordStat.Ndisease;
					DBRecordStatAll.NdiseaseDB = DBRecordStat.NdiseaseDB;
					DBRecordStatAll.Nnormal = DBRecordStat.Nnormal;
					DBRecordStatAll.NnormalDB = DBRecordStat.NnormalDB;
					DBRecordStatAll.Nreader = DBRecordStat.Nreader;
					DBRecordStatAll.NreaderDB = DBRecordStat.NreaderDB;
					DBRecord track = DBRecordStatAll;
					DBRecordStatAll.InputFile1 = InputFile1;
					String tempModA = "";
					String tempModB = "";
					if (MRMC.commandStart){
						tempModA = DBRecordStat.modalityA;
						tempModB = DBRecordStat.modalityB;
					}else{
						tempModA = InputSummaryCard.loadmodalityA;
						tempModB = InputSummaryCard.loadmodalityB;
					}
					// Analysis modality A
					if (tempModA!=null&&!tempModA.equals("NO_MOD")){
						DBRecordStatAll.modalityA = tempModA;
						DBRecordStatAll.modalityB = "NO_MOD";
						DBRecordStatAll.selectedMod = 0;
						DBRecordStatAll.flagMLE = 0;
						DBRecordStatAll.DBRecordStatFillSummary(DBRecordStatAll);
						AllStatreport = exportToFile.exportStat(AllStatreport, DBRecordStatAll, fileTime);	
						savetable();
						DBRecordStatAll.flagMLE = 1;
						DBRecordStatAll.DBRecordStatFillSummary(DBRecordStatAll);
						AllStatMLEreport = exportToFile.exportStat(AllStatMLEreport, DBRecordStatAll, fileTime);
						savetable();
					}
					DBRecord track1 = DBRecordStat;
					// Analysis modality B
					if (tempModB!=null&&!tempModB.equals("NO_MOD")){
						DBRecordStatAll.modalityA = "NO_MOD";
						DBRecordStatAll.modalityB = tempModB;
						DBRecordStatAll.selectedMod = 1;
						DBRecordStatAll.flagMLE = 0;
						DBRecordStatAll.DBRecordStatFillSummary(DBRecordStatAll);
						AllStatreport = exportToFile.exportStat(AllStatreport, DBRecordStatAll, fileTime);	
						savetable();
						DBRecordStatAll.flagMLE = 1;
						DBRecordStatAll.DBRecordStatFillSummary(DBRecordStatAll);
						AllStatMLEreport = exportToFile.exportStat(AllStatMLEreport, DBRecordStatAll, fileTime);
						savetable();
					}
					// Analysis modality A and B
					if (tempModA!=null&&tempModB!=null&&!tempModA.equals("NO_MOD")&&!tempModB.equals("NO_MOD")){
						DBRecordStatAll.modalityA = tempModA;
						DBRecordStatAll.modalityB = tempModB;
						DBRecordStatAll.selectedMod = 3;
						DBRecordStatAll.flagMLE = 0;
						DBRecordStatAll.DBRecordStatFillSummary(DBRecordStatAll);
						AllStatreport = exportToFile.exportStat(AllStatreport, DBRecordStatAll, fileTime);	
						savetable();
						DBRecordStatAll.flagMLE = 1;
						DBRecordStatAll.DBRecordStatFillSummary(DBRecordStatAll);
						AllStatMLEreport = exportToFile.exportStat(AllStatMLEreport, DBRecordStatAll, fileTime);
						savetable();
					}
				}
				// export result to disk
				try {
					FileWriter fwAllStat = new FileWriter(AllStatPath);
					FileWriter fwAllStatMLE = new FileWriter(AllStatMLEPath);
					FileWriter fwBDGtable = new FileWriter(BDGtable);
					FileWriter fwBCKtable = new FileWriter(BCKtable);
					FileWriter fwDBMtable = new FileWriter(DBMtable);
					FileWriter fwORtable = new FileWriter(ORtable);
					FileWriter fwMStable = new FileWriter(MStable);				
					BufferedWriter bwAllStat = new BufferedWriter(fwAllStat);
					BufferedWriter bwAllStatMLE = new BufferedWriter(fwAllStatMLE);
					BufferedWriter bwBDGtable = new BufferedWriter(fwBDGtable);
					BufferedWriter bwBCKtable = new BufferedWriter(fwBCKtable);
					BufferedWriter bwDBMtable = new BufferedWriter(fwDBMtable);
					BufferedWriter bwORtable = new BufferedWriter(fwORtable);
					BufferedWriter bwMStable = new BufferedWriter(fwMStable);										
					bwAllStat.write(AllStatreport);
					bwAllStat.close();
					bwAllStatMLE.write(AllStatMLEreport);
					bwAllStatMLE.close();
					bwBDGtable.write(BDGout);
					bwBDGtable.close();
					bwBCKtable.write(BCKout);
					bwBCKtable.close();
					bwDBMtable.write(DBMout);
					bwDBMtable.close();
					bwORtable.write(ORout);
					bwORtable.close();
					bwMStable.write(MSout);
					bwMStable.close();					
					
					// only export ROC and each reader information for raw data 
					if  (GUInterface.selectedInput == GUInterface.DescInputModeImrmc){
						FileWriter fwAllAUCs = new FileWriter(AllAUCsPath);
						FileWriter fwAllROC = new FileWriter(AllROCPath);
						FileWriter fwAllCov = new FileWriter(AllReaderCovPath);
						BufferedWriter bwAllAUCs = new BufferedWriter(fwAllAUCs);
						BufferedWriter bwAllROC = new BufferedWriter(fwAllROC);	
						BufferedWriter bwAllCov = new BufferedWriter(fwAllCov);	
						bwAllAUCs.write(AllAUCsreport);
						bwAllAUCs.close();
						bwAllROC.write(AllROCreport);
						bwAllROC.close();
						bwAllCov.write(readerCovReport);
						bwAllCov.close();
						if (!MRMC.commandStart){
							JOptionPane.showMessageDialog(
											thisGUI.MRMCobject.getFrame(),"All modalities combinations analysis table, result, AUCs and ROC have been succeed export to \n " + outputDir, 
											"Exported", JOptionPane.INFORMATION_MESSAGE);
						}else{
							System.out.println("All modalities combinations analysis table, result, AUCs and ROC have been succeed export to \n " + outputDir);		
							System.exit(0);
						}
					} else{
						if (!MRMC.commandStart){
							JOptionPane.showMessageDialog(
									thisGUI.MRMCobject.getFrame(),"All modalities combinations analysis table and result have been succeed export to \n " + outputDir, 
									"Exported", JOptionPane.INFORMATION_MESSAGE);
						}else{
							System.out.println("All modalities combinations analysis table and result have been succeed export to \n" + outputDir);		
							System.exit(0);
						}
					}
				} catch (HeadlessException e1) {
						e1.printStackTrace();
				} catch (IOException e1) {
						e1.printStackTrace();
				} 
			}else{
				JOptionPane.showMessageDialog(thisGUI.MRMCobject.getFrame(),
						"Please load Reader study data input file.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
						
		}
		private void savetable() {
			// save tables result
			BDGout = exportToFile.exportTableBDG(BDGout,DBRecordStatAll);
    		BCKout = exportToFile.exportTableBCK(BCKout,DBRecordStatAll);
	    	DBMout = exportToFile.exportTableDBM(DBMout,DBRecordStatAll);
	    	ORout = exportToFile.exportTableOR(ORout,DBRecordStatAll);
			MSout = exportToFile.exportTableMS(MSout,DBRecordStatAll);}
			
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

	/**
     * Calculate Factorial for a number
	 */

	public int calFactorial(int n){
		int factorial = 1;
		for (int i = 1; i<=n; i++){
			factorial = factorial * i;
		}
		return factorial;
	}







}
