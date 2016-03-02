package mrmc.chart;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import mrmc.chart.ROCCurvePlot.exportROCresult;
import mrmc.core.DBRecord;
import mrmc.core.StatTest;
import mrmc.gui.GUInterface;
import mrmc.gui.RowHeaderRenderer;
import mrmc.gui.SizePanel;
import mrmc.gui.StatPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYSeries;
/**
 * Creates a chart displaying sizing result for multiple size of reader 
 * normal case and disease case. Consider speed of the software, we could
 * calculate S.E. and don't calculate df, Lambda, power 
 * @author Qi Gong
 */
public class exploreExpSize extends JFrame {
	private DBRecord DBRecordSize;
	private DBRecord DBRecordStat;
	private SizePanel SizePanel1;
	private JComboBox<String> combination;					// pull down menu for choose R*NO, R*N1 or N0*N1
	private JComboBox<String> subNumber;                    // pull down menu for choose NR , N0 or N1
	private JComboBox<String> varPowerSwitch;                    // pull down menu for choose NR , N0 or N1
	//private ArrayList<Integer> Nreader;
	private int[] Nreader = {5,10,15,20}; 										//Number of Reader list
	private int[] Nnormal = {32,64,128,256,512,1024,2048,4096,8192};			//Number of Normal list
	private int[] Ndisease = {32,64,128,256,512,1024,2048,4096,8192};			//Number of Disease list
	private String[] readerTitle = { "5","10","15","20" };
	private String[] caseTitle = {"32","64","128","256","512","1024","2048","4096","8192"};
	private int chooseNumber;						// chose number for subNumber pull down menu
	private String varOrPower;
	private JTable fullyTable;						// variance table
	private JPanel fullydisplay;				    // panel for variance table
	private JFrame fullyFrame;                		// fully predict window
	private JScrollPane fullyScroll;
	private String[] tableRowHeader;
	private GUInterface GUI;
	public static boolean doFullSize = false;
	
	public exploreExpSize(DBRecord inputDBRecordSize, GUInterface GUIin, SizePanel inputSizePanel ) {
		DBRecordSize = inputDBRecordSize;
		GUI= GUIin;
		SizePanel1 = inputSizePanel;
		DBRecordStat = GUI.DBRecordStat;
		DBRecordSize.DBRecordStat=DBRecordStat;
		fullyFrame= new JFrame("Explore Experiment Size");    
		fullydisplay = new JPanel();               
		JPanel fullyoptions = new JPanel();         // panel for 2 pull down manual

		combination = new JComboBox<String>();
		combination.addItem("Nreader (row) vs Nnormal (column)");
		combination.addItem("Nreader (row) vs Ndisease (column)");
		combination.addItem("Nnormal (row) vs Ndisease (column)");
		combination.addItemListener(new combinationSelectListener());
		
		subNumber = new JComboBox<String>();
		for (int i = 0; i<caseTitle.length;i++)
			subNumber.addItem("NDisease: " + caseTitle[i]);
		subNumber.addItemListener(new subNumberSelectListener());

		JButton exportSize = new JButton("Export"); 
		exportSize.addActionListener(new exportSize());
		
		varPowerSwitch = new JComboBox<String>();
		varPowerSwitch.addItemListener(new varPowerSwitchSelectListener());	
		varPowerSwitch.addItem("S.E.");
		varPowerSwitch.addItem("BDG Power");
		
		fullyoptions.add(combination);
		fullyoptions.add(subNumber);
		fullyoptions.add(varPowerSwitch);
		fullyoptions.add(exportSize);
		DefaultTableModel dm = new DefaultTableModel(4,9);
		fullyTable = new JTable(dm);
		chooseNumber = 32;
		varOrPower = "S.E.";
		varReaderVsNormal(chooseNumber);
		JScrollPane fullyScroll = new JScrollPane();
		fullyScroll	= genTable(fullyTable,caseTitle, readerTitle);
		fullydisplay.add(fullyScroll);
		fullyFrame.add(fullydisplay);
		fullyFrame.add(fullyoptions,BorderLayout.PAGE_START);
		fullyFrame.setSize(700,180);
		fullyFrame.setVisible(true);

	}
    
	
	// calculate variance for R*N0
	private void varReaderVsNormal(int ChooseNdisease) {
		// TODO Auto-generated method stub
		for (int i=0 ; i<Nreader.length;i++ ){
			for (int j=0 ; j < Nnormal.length;j++){
				DBRecordSize.Nreader = Nreader[i];
				DBRecordSize.Nnormal = Nnormal[j];
				DBRecordSize.Ndisease = ChooseNdisease;
				DBRecordSize.BDGcoeff = DBRecordSize.genBDGCoeff(Nreader[i],Nnormal[j],ChooseNdisease);
				
				DBRecordSize.BDGforSizeFullPanel();

				if (varOrPower.equals("S.E."))
				fullyTable.setValueAt(SizePanel1.threeDecE.format(Math.sqrt(DBRecordSize.totalVar)), i, j);
				else {
					DBRecordSize.BCKcoeff = DBRecordSize.genBCKCoeff(DBRecordSize.BDGcoeff);
					DBRecordSize.BCK = DBRecordSize.BDG2BCK(DBRecordSize.BDG, DBRecordSize.BCKcoeff);
					DBRecordSize.BCKbias = DBRecordSize.BDG2BCK(DBRecordSize.BDGbias, DBRecordSize.BCKcoeff);
					doFullSize =true;
					StatTest testSize = new StatTest(SizePanel1, DBRecordStat, DBRecordSize);
					doFullSize =false;
					fullyTable.setValueAt(SizePanel1.threeDecE.format(testSize.powerBDG), i, j);
				}
			}
		}
	}

	
	// calculate variance for R*N1
	private void varReaderVsDisease(int ChooseNormal) {
		// TODO Auto-generated method stub
		for (int i=0 ; i<Nreader.length;i++ ){
			for (int j=0 ; j < Ndisease.length;j++){
				DBRecordSize.Nreader = Nreader[i];
				DBRecordSize.Nnormal = ChooseNormal;
				DBRecordSize.Ndisease = Ndisease[j];
				DBRecordSize.BDGcoeff = DBRecordSize.genBDGCoeff(Nreader[i],ChooseNormal,Ndisease[j]);			    
				DBRecordSize.BDGforSizeFullPanel();
				if (varOrPower.equals("S.E."))
				fullyTable.setValueAt(SizePanel1.threeDecE.format(Math.sqrt(DBRecordSize.totalVar)), i, j);
				else {
					DBRecordSize.BCKcoeff = DBRecordSize.genBCKCoeff(DBRecordSize.BDGcoeff);
					DBRecordSize.BCK = DBRecordSize.BDG2BCK(DBRecordSize.BDG, DBRecordSize.BCKcoeff);
					DBRecordSize.BCKbias = DBRecordSize.BDG2BCK(DBRecordSize.BDGbias, DBRecordSize.BCKcoeff);
					doFullSize =true;
					StatTest testSize = new StatTest(SizePanel1, DBRecordStat, DBRecordSize);
					doFullSize =false;
					fullyTable.setValueAt(SizePanel1.threeDecE.format(testSize.powerBDG), i, j);
				}
			}
		}
		
	}
	
	
	
	// calculate variance for N0*N1
	private void varNormalVsDisease(int ChooseReader) {
		// TODO Auto-generated method stub
		for (int i=0 ; i<Nnormal.length;i++ ){
			for (int j=0 ; j <Ndisease.length;j++){
				DBRecordSize.Nreader = ChooseReader;
				DBRecordSize.Nnormal = Nnormal[i];
				DBRecordSize.Ndisease = Ndisease[j];
				DBRecordSize.BDGcoeff = DBRecordSize.genBDGCoeff(ChooseReader,Nnormal[i],Ndisease[j]);			    
				DBRecordSize.BDGforSizeFullPanel();
				if (varOrPower.equals("S.E."))
				fullyTable.setValueAt(SizePanel1.threeDecE.format(Math.sqrt(DBRecordSize.totalVar)), i, j);
				else {
					DBRecordSize.BCKcoeff = DBRecordSize.genBCKCoeff(DBRecordSize.BDGcoeff);
					DBRecordSize.BCK = DBRecordSize.BDG2BCK(DBRecordSize.BDG, DBRecordSize.BCKcoeff);
					DBRecordSize.BCKbias = DBRecordSize.BDG2BCK(DBRecordSize.BDGbias, DBRecordSize.BCKcoeff);
					doFullSize =true;
					StatTest testSize = new StatTest(SizePanel1, DBRecordStat, DBRecordSize);
					doFullSize =false;
					fullyTable.setValueAt(SizePanel1.threeDecE.format(testSize.powerBDG), i, j);
				}
				
			}
		}
		
	}
	
	
	
	// generate table format
	public JScrollPane genTable(JTable table, String[] colNames,
			String[] rowNames) {
		for (int i = 0; i < colNames.length; i++)
			table.getColumnModel().getColumn(i).setHeaderValue(colNames[i]);
		tableRowHeader = rowNames;
		JList<String> rowHeader = new JList<String>(rowNames);
		rowHeader.setFixedCellWidth(80);

		rowHeader.setFixedCellHeight(table.getRowHeight());
		rowHeader.setCellRenderer(new RowHeaderRenderer(table));

		JScrollPane scroll = new JScrollPane(table);
		scroll.setRowHeaderView(rowHeader);
		return scroll;

	}
	
	
	
	// combination pull down menu listener
	class combinationSelectListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if ((String) combination.getSelectedItem() =="Nreader (row) vs Nnormal (column)"){
				subNumber.removeAllItems();
				for (int i = 0; i<caseTitle.length;i++)
					subNumber.addItem("NDisease: " + caseTitle[i]);
				chooseNumber=32;
			}
			else if ((String) combination.getSelectedItem() =="Nreader (row) vs Ndisease (column)"){
				subNumber.removeAllItems();
				for (int i = 0; i<caseTitle.length;i++)
					subNumber.addItem("NNormal: " + caseTitle[i]);
				chooseNumber=32;
			}
			else{
				subNumber.removeAllItems();
				for (int i = 0; i<readerTitle.length;i++)
					subNumber.addItem("NReader: " + readerTitle[i]);
				chooseNumber=5;
			}
			updateFullytable();
		} // method

	} // class
	
	
	// subNumber pull down menu listener
	class subNumberSelectListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() != ItemEvent.DESELECTED) { return; }
			if (subNumber.getSelectedItem() == null) { return;}
				String chooseString = (String) subNumber.getSelectedItem();
			    chooseNumber =  Integer.valueOf(chooseString.substring(chooseString.lastIndexOf(":") + 1).trim());
				updateFullytable();
		} // method
	} // class
	
	
	
	// subNumber pull down menu listener
	class varPowerSwitchSelectListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() != ItemEvent.DESELECTED) { return; }
			if (varPowerSwitch.getSelectedItem() == null) { return;}
				varOrPower =  (String) varPowerSwitch.getSelectedItem();
				updateFullytable();
		} // method
	} // class
	
	
	
	
	// Update date frame function. 
	private void updateFullytable() {
		// TODO Auto-generated method stub
		if ((String) combination.getSelectedItem() =="Nreader (row) vs Nnormal (column)"){
			DefaultTableModel dm = new DefaultTableModel(4,9);
			fullyTable = new JTable(dm);
			fullyScroll = new JScrollPane();
			fullyScroll	= genTable(fullyTable,caseTitle, readerTitle);
			int ChooseNdisease = chooseNumber;
			varReaderVsNormal(ChooseNdisease);
			fullydisplay.removeAll();
			fullydisplay.add(fullyScroll);
			fullyFrame.remove(fullydisplay);
			fullyFrame.add(fullydisplay);
			fullyFrame.setVisible(false);
			fullyFrame.setSize(700, 180);
			fullyFrame.setVisible(true);
		} else if ((String) combination.getSelectedItem() =="Nreader (row) vs Ndisease (column)"){
			DefaultTableModel dm = new DefaultTableModel(4,9);
			fullyTable = new JTable(dm);
			fullyScroll = new JScrollPane();
			fullyScroll	= genTable(fullyTable,caseTitle, readerTitle);
			int ChooseNormal = chooseNumber;
			varReaderVsDisease(ChooseNormal);
			fullydisplay.removeAll();
			fullydisplay.add(fullyScroll);
			fullyFrame.remove(fullydisplay);
			fullyFrame.add(fullydisplay);
			fullyFrame.setVisible(false);
			fullyFrame.setSize(700, 180);
			fullyFrame.setVisible(true);
		}else{
			DefaultTableModel dm = new DefaultTableModel(9,9);
			fullyTable = new JTable(dm);
			fullyScroll = new JScrollPane();
			fullyScroll	= genTable(fullyTable,caseTitle, caseTitle);
			int ChooseNreader = chooseNumber;
			varNormalVsDisease(ChooseNreader);
			fullydisplay.removeAll();
			fullydisplay.add(fullyScroll);
			fullyFrame.remove(fullydisplay);
			fullyFrame.add(fullydisplay);
			fullyFrame.setVisible(false);
			fullyFrame.setSize(700, 250);
			fullyFrame.setVisible(true);
			
		}
	}
	
	
	class exportSize implements ActionListener{
		public void actionPerformed(ActionEvent e) {

			String outputTable = "";
			outputTable = outputTable + (String) combination.getSelectedItem() +"\r\n";
			outputTable = outputTable + (String) subNumber.getSelectedItem() + "\r\n";
			outputTable = outputTable + (String) varPowerSwitch.getSelectedItem() + "\r\n" + "\r\n";
			outputTable = outputTable + ",";
			for (int i = 0; i<fullyTable.getColumnCount(); i++)
				outputTable= outputTable + (String) fullyTable.getColumnModel().getColumn(i).getHeaderValue() +",";
			for (int i = 0; i<fullyTable.getRowCount(); i++){
				outputTable =  outputTable + "\r\n"+ tableRowHeader[i] + ",";
				for (int j = 0; j<fullyTable.getColumnCount(); j++){
					outputTable = outputTable + (String) fullyTable.getModel().getValueAt(i,j) + ",";
				}
			}
			
			try {
				JFileChooser fc = new JFileChooser();
	            DateFormat dateForm = new SimpleDateFormat("yyyyMMddHHmm");
				Date currDate = new Date();
				final String fileTime = dateForm.format(currDate);
				String exportFileName = "ExploreExperimentSizeOutput"+fileTime+".csv";
				fc.setSelectedFile(new File(GUI.outputfileDirectory+"//"+exportFileName));
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Explore Experiment Size Files (csv)", "csv");
				fc.setFileFilter(filter);	
				int fcReturn = fc.showSaveDialog((Component) e.getSource());
				if (fcReturn == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					if (!f.exists()) {
						f.createNewFile();
					}
					FileWriter fw = new FileWriter(f.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					GUI.outputfileDirectory = fc.getCurrentDirectory();			
					String savedFileName = fc.getSelectedFile().getName();	
					bw.write(outputTable);
					bw.close();
					JOptionPane.showMessageDialog(
							GUI.MRMCobject.getFrame(), "Explore Experiment Size File" + " has been succeed export to " + GUI.outputfileDirectory + " !\n"+ "Filename = " +savedFileName, 
							"Exported", JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (HeadlessException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} 
				

		}
	}
}
