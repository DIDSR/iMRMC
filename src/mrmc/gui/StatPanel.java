/**
 * 
 */
package mrmc.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import mrmc.core.DBRecord;
import mrmc.core.InputFile;
import mrmc.core.Matrix;


/**
 * Controls and shows the statistical analysis
 * 
 * @author BDG
 *
 */
public class StatPanel {

	private JFrame JFrameApp;
	private InputFile InputFile1;
	private DBRecord DBRecordStat;
	
	public JPanel JPanelStat = new JPanel();
	
	/**
	 * These JLabels make up the StatPanel
	 */
	private JLabel 
		StatJLabelH0 = new JLabel("1", JLabel.LEFT),
		StatJLabelAUC = new JLabel("1", JLabel.LEFT),
		StatJLabelDFNormal     = new JLabel("123456789012345678901234567890",JLabel.RIGHT),
		StatJLabelDFBDG        = new JLabel("123456789012345678901234567890",JLabel.RIGHT),
		StatJLabelDFHillis     = new JLabel("123456789012345678901234567890",JLabel.RIGHT),
		StatJLabelPValNormal   = new JLabel("12345678901234567890",JLabel.LEFT),
		StatJLabelPValBDG      = new JLabel("12345678901234567890",JLabel.LEFT),
		StatJLabelPValHillis   = new JLabel("12345678901234567890",JLabel.LEFT),
		StatJLabelCINormal     = new JLabel("123456789012345678901234567890",JLabel.LEFT),
		StatJLabelCIBDG        = new JLabel("123456789012345678901234567890",JLabel.LEFT),
		StatJLabelCIHillis     = new JLabel("123456789012345678901234567890",JLabel.LEFT),
		StatJLabelRejectNormal = new JLabel("12345678901234567890",JLabel.LEFT),
		StatJLabelRejectBDG    = new JLabel("12345678901234567890",JLabel.LEFT),
		StatJLabelRejectHillis = new JLabel("12345678901234567890",JLabel.LEFT),
		StatJLabelTotalVar = new JLabel();
	
		
	
	
	/**
	 * table1 corresponds to the variance analysis
	 */
	private JTable BDGtable1, BCKtable1, DBMtable1, ORtable1, MStable1;
	
	JTabbedPane tabbedPane1;
	private JLabel BDGvar1, BCKvar1, DBMvar1, ORvar1, MSvar1;

	DecimalFormat twoDec = new DecimalFormat("0.00");
	DecimalFormat threeDec = new DecimalFormat("0.000");
	DecimalFormat fourDec = new DecimalFormat("0.0000");
	DecimalFormat twoDecE = new DecimalFormat("0.00E0");
	DecimalFormat threeDecE = new DecimalFormat("0.000E0");
	DecimalFormat fourDecE = new DecimalFormat("0.0000E0");

	/**
	 * Sole constructor for stat panel. Creates and initializes related GUI
	 * elements.
	 * 
	 * @param DBRecordStatTemp Class containing results from analyzing an mrmc experiment 
	 * 
	 */
	public StatPanel(JFrame JFrameAppTemp, DBRecord DBRecordStatTemp) {
		
		DBRecordStat = DBRecordStatTemp;
		JFrameApp = JFrameAppTemp;
		InputFile1 = DBRecordStat.InputFile1;
		JPanelStat.setLayout(new BoxLayout(JPanelStat, BoxLayout.Y_AXIS));

		/*
		 * Format the label widths for each row of the analysis results
		 */
		StatJLabelDFNormal.setPreferredSize(StatJLabelDFNormal.getPreferredSize());
		StatJLabelPValNormal.setPreferredSize(StatJLabelPValNormal.getPreferredSize());
		StatJLabelCINormal.setPreferredSize(StatJLabelCINormal.getPreferredSize());
		StatJLabelRejectNormal.setPreferredSize(StatJLabelRejectNormal.getPreferredSize());
		//
		StatJLabelDFBDG.setPreferredSize(StatJLabelDFBDG.getPreferredSize());
		StatJLabelPValBDG.setPreferredSize(StatJLabelPValBDG.getPreferredSize());
		StatJLabelCIBDG.setPreferredSize(StatJLabelCIBDG.getPreferredSize());
		StatJLabelRejectBDG.setPreferredSize(StatJLabelRejectBDG.getPreferredSize());
		//
		StatJLabelDFHillis.setPreferredSize(StatJLabelDFHillis.getPreferredSize());
		StatJLabelPValHillis.setPreferredSize(StatJLabelPValHillis.getPreferredSize());
		StatJLabelCIHillis.setPreferredSize(StatJLabelCIHillis.getPreferredSize());
		StatJLabelRejectHillis.setPreferredSize(StatJLabelRejectHillis.getPreferredSize());
		
		
		/*
		 * Determine the width of the rows of the analysis results
		 */
		int width = 0;
		Dimension d;
		d = StatJLabelDFHillis.getPreferredSize();
		width = width + d.width;
		d = StatJLabelPValHillis.getPreferredSize();
		width = width + d.width;
		d = StatJLabelCIHillis.getPreferredSize();
		width = width + d.width;
		d = StatJLabelRejectHillis.getPreferredSize();
		width = width + d.width;
		/*
		 * Set the width of the two rows leading the analysis results
		 */
		StatJLabelH0.setPreferredSize(new Dimension(width, d.height));
		StatJLabelAUC.setPreferredSize(new Dimension(width, d.height));

		/*
		 * Generate the statistical analysis panel
		 */
		JPanel StatPanelRow1 = new JPanel();
		StatPanelRow1.add(StatJLabelH0);
		
		JPanel StatPanelRow2 = new JPanel();
		StatPanelRow2.add(StatJLabelAUC);

		JPanel StatPanelRow3 = new JPanel();
		StatPanelRow3.add(StatJLabelDFNormal);
		StatPanelRow3.add(StatJLabelPValNormal);
		StatPanelRow3.add(StatJLabelCINormal);
		StatPanelRow3.add(StatJLabelRejectNormal);

		JPanel StatPanelRow4 = new JPanel();
		StatPanelRow4.add(StatJLabelDFBDG);
		StatPanelRow4.add(StatJLabelPValBDG);
		StatPanelRow4.add(StatJLabelCIBDG);
		StatPanelRow4.add(StatJLabelRejectBDG);
		
		JButton statHillis = new JButton("Hillis Approx");
		JPanel StatPanelRow5 = new JPanel();
		statHillis.addActionListener(new StatHillisButtonListener());
		StatPanelRow5.add(statHillis);


		// *******************************************************************
		// *************tabbed panel 1*********************************
		// *********************************************************************
		String[] rowNamesDiff = new String[] { "comp MA", "coeff MA",
				"comp MB", "coeff MB", "comp product", "- coeff product",
				"total" };
		String[] rowNamesSingle = new String[] { "components", "coeff", "total" };

		// Create BDG tab
		JPanel panelBDG1 = makeBDGTab(rowNamesDiff);
		// Create BCK tab
		JPanel panelBCK1 = makeBCKTab(rowNamesDiff);
		// Create DBM tab
		JPanel panelDBM1 = makeDBMTab(rowNamesSingle);
		// Create OR tab
		JPanel panelOR1 = makeORTab(rowNamesSingle);
		// create MS tab
		JPanel panelMS1 = makeMSTab(rowNamesSingle);

		tabbedPane1 = new JTabbedPane();
		tabbedPane1.addTab("BDG", panelBDG1);
		tabbedPane1.addTab("BCK", panelBCK1);
		tabbedPane1.addTab("DBM", panelDBM1);
		tabbedPane1.addTab("OR", panelOR1);
		tabbedPane1.addTab("MS", panelMS1);

		JPanelStat.add(StatPanelRow1);
		JPanelStat.add(StatPanelRow2);
		JPanelStat.add(StatPanelRow3);
		JPanelStat.add(StatPanelRow4);
		JPanelStat.add(StatPanelRow5);
		JPanelStat.add(tabbedPane1);

	}

	/**
	 * Clears all input fields and statistics labels
	 */
	void resetStatPanel() {
		
		resetTable1();

		StatJLabelH0.setText("Statistical Analysis:");

		StatJLabelAUC.setText("AUC = ");
		DBRecordStat.totalVar = -1.0;

		StatJLabelDFNormal.setText("Large Sample Approx(Normal)");
		StatJLabelDFBDG.setText   ("         T-stat df(BDG) =      ");
		StatJLabelDFHillis.setText("T-stat df(Hillis 2008) = ");

		StatJLabelPValNormal.setText("p-Value = ");
		StatJLabelPValBDG.setText   ("p-Value = ");
		StatJLabelPValHillis.setText("p-Value = ");

		StatJLabelCINormal.setText("Conf. Int. = ");
		StatJLabelCIBDG.setText   ("Conf. Int. = ");
		StatJLabelCIHillis.setText("Conf. Int. = ");

		StatJLabelRejectNormal.setText("Reject Null? = ");
		StatJLabelRejectBDG.setText   ("Reject Null? = ");
		StatJLabelRejectHillis.setText("Reject Null? = ");

	}

	/**
	 * Sets the statistics labels for the variance analysis table based on the
	 * current record that has been analyzed <br>
	 * 
	 * This method is called from the RawStudyCard, the ManualCard, and the DBCard
	 */
	public void setStatPanel() {
		
		// If the study is not fully crossed, then don't show other variance decomposisions.
		if (!DBRecordStat.flagFullyCrossed) {
			if(DBRecordStat.verbose)
				JOptionPane.showMessageDialog(JFrameApp,
					"The study is not fully crossed", "Warning",
					JOptionPane.WARNING_MESSAGE);

			tabbedPane1.setEnabledAt(2, false);
			tabbedPane1.setEnabledAt(3, false);
			tabbedPane1.setEnabledAt(4, false);

		} else {
			tabbedPane1.setEnabledAt(2, true);
			tabbedPane1.setEnabledAt(3, true);
			tabbedPane1.setEnabledAt(4, true);
		}

		String output, output2;
		
		StatJLabelH0.setText("H0: AUC = 0.50,   two-sided alternative,   95% significance,   " + 
				DBRecordStat.getSizes());
		StatJLabelAUC.setText(DBRecordStat.getAUCsReaderAvgString(DBRecordStat.selectedMod) +
				",   S.E(total) = " + threeDecE.format(Math.sqrt(DBRecordStat.totalVar)));

		if(DBRecordStat.selectedMod == 3) {
			
			StatJLabelH0.setText("H0: AUC_A - AUC_B = 0.00,   two-sided alternative,   95% significance,   " + 
					DBRecordStat.getSizes());
			StatJLabelAUC.setText(DBRecordStat.getAUCsReaderAvgString(DBRecordStat.selectedMod) +
					",   S.E(total) = " + threeDecE.format(Math.sqrt(DBRecordStat.totalVar)));
		}

		
		StatJLabelDFNormal.setText("Large Sample Approx(Normal)");
		output = fourDec.format(DBRecordStat.testStat.pValNormal);
		StatJLabelPValNormal.setText("  p-Value = " + output);
		output = fourDec.format(DBRecordStat.testStat.ciBotNormal);
		output2 = fourDec.format(DBRecordStat.testStat.ciTopNormal);
		StatJLabelCINormal.setText("Conf. Int. = (" + output + ", " + output2 + ")");
		output = twoDec.format(DBRecordStat.testStat.rejectNormal);
		if (DBRecordStat.testStat.rejectNormal == 1) {
			StatJLabelRejectNormal.setText("Reject Null? = " + "Yes" + "(" + output + ")");
		} else {
			StatJLabelRejectNormal.setText("Reject Null? = " + "No" + "(" + output + ")");
		}
		

		output = twoDec.format(DBRecordStat.testStat.DF_BDG);
		StatJLabelDFBDG.setText("  df(BDG) = " + output + "     ");
		output = fourDec.format(DBRecordStat.testStat.pValBDG);
		StatJLabelPValBDG.setText("  p-Value = " + output);
		output = fourDec.format(DBRecordStat.testStat.ciBotBDG);
		output2 = fourDec.format(DBRecordStat.testStat.ciTopBDG);
		StatJLabelCIBDG.setText("Conf. Int. = (" + output + ", " + output2 + ")");
		output = twoDec.format(DBRecordStat.testStat.rejectBDG);
		if (DBRecordStat.testStat.rejectBDG == 1) {
			StatJLabelRejectBDG.setText("Reject Null? = " + "Yes" + "(" + output + ")");
		} else {
			StatJLabelRejectBDG.setText("Reject Null? = " + "No" + "(" + output + ")");
		}
		//StatJLabelRejectBDG.setText("Reject Null? = " + output);

		if (DBRecordStat.flagFullyCrossed) {
			output = twoDec.format(DBRecordStat.testStat.DF_Hillis);
			StatJLabelDFHillis.setText("df(Hillis 2008) = " + output + "     ");
			output = fourDec.format(DBRecordStat.testStat.pValHillis);
			StatJLabelPValHillis.setText("p-Value = " + output);
			output = fourDec.format(DBRecordStat.testStat.ciBotHillis);
			output2 = fourDec.format(DBRecordStat.testStat.ciTopHillis);
			StatJLabelCIHillis.setText("Conf. Int. = (" + output + ", "
					+ output2 + ")");
			output = twoDec.format(DBRecordStat.testStat.rejectHillis);
			if (DBRecordStat.testStat.rejectHillis == 1) {
				StatJLabelRejectHillis.setText("Reject Null? = " + "Yes" + "(" + output + ")");
			} else {
				StatJLabelRejectHillis.setText("Reject Null? = " + "No" + "(" + output + ")");
			}
			//StatJLabelRejectHillis.setText("Reject Null? = " + output);
		} else {
			StatJLabelDFHillis.setText("");
			StatJLabelPValHillis.setText("");
			StatJLabelCIHillis.setText("");
			StatJLabelRejectHillis.setText("");
		}

	}

	/**
	 * Empties out all values in the variance analysis table
	 */
	void resetTable1() {
		
		BDGvar1.setText("total var=");
		BCKvar1.setText("total var=");
		DBMvar1.setText("total var=");
		ORvar1.setText("total var=");
		MSvar1.setText("total var=");
		StatJLabelTotalVar.setText("total var=");

		for (int i = 0; i < BDGtable1.getRowCount(); i++) {
			for (int j = 0; j < 8; j++) {
				BDGtable1.setValueAt(0, i, j);
				BDGtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
			for (int j = 0; j < 7; j++) {
				BCKtable1.setValueAt(0, i, j);
				BCKtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
		}
		for (int i = 0; i < MStable1.getRowCount(); i++) {
			for (int j = 0; j < 6; j++) {
				MStable1.setValueAt(0, i, j);
				MStable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
			for (int j = 0; j < 6; j++) {
				DBMtable1.setValueAt(0, i, j);
				ORtable1.setValueAt(0, i, j);
				DBMtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				ORtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
		}
		
		tabbedPane1.setTitleAt(0, "BDG");
		tabbedPane1.setTitleAt(1, "BCK");
		tabbedPane1.setTitleAt(2, "DBM");
		tabbedPane1.setTitleAt(3, "OR");
		tabbedPane1.setTitleAt(4, "MS");
		
	}

	/**
	 * Gets variance analysis information from current study record, populates
	 * variance analysis table, sets statistics labels
	 */
	public void setTable1() {

		// FlagMLE == 0
		double[][] BDGdata1 = DBRecord.getBDGTab(DBRecordStat.selectedMod,
				DBRecordStat.BDG, DBRecordStat.BDGcoeff);
		double[][] BCKdata1 = DBRecord.getBCKTab(DBRecordStat.selectedMod,
				DBRecordStat.BCK, DBRecordStat.BCKcoeff);
		double[][] DBMdata1 = DBRecord.getDBMTab(DBRecordStat.selectedMod,
				DBRecordStat.DBM, DBRecordStat.DBMcoeff);
		double[][] ORdata1 = DBRecord.getORTab(DBRecordStat.selectedMod,
				DBRecordStat.OR, DBRecordStat.ORcoeff);
		double[][] MSdata1 = DBRecord.getMSTab(DBRecordStat.selectedMod,
				DBRecordStat.MS, DBRecordStat.MScoeff);
		if(DBRecordStat.flagMLE == 1) {
			BDGdata1 = DBRecord.getBDGTab(DBRecordStat.selectedMod,
					DBRecordStat.BDGbias, DBRecordStat.BDGcoeff);
			BCKdata1 = DBRecord.getBCKTab(DBRecordStat.selectedMod,
					DBRecordStat.BCKbias, DBRecordStat.BCKcoeff);
			DBMdata1 = DBRecord.getDBMTab(DBRecordStat.selectedMod,
					DBRecordStat.DBMbias, DBRecordStat.DBMcoeff);
			ORdata1 = DBRecord.getORTab(DBRecordStat.selectedMod,
					DBRecordStat.ORbias, DBRecordStat.ORcoeff);
			MSdata1 = DBRecord.getMSTab(DBRecordStat.selectedMod,
					DBRecordStat.MSbias, DBRecordStat.MScoeff);			
		}
		double BDGv = Matrix.total(BDGdata1[6]);
		double BCKv = Matrix.total(BCKdata1[6]);
		double DBMv = Matrix.total(DBMdata1[2]);
		double ORv = Matrix.total(ORdata1[2]);
		double MSv = Matrix.total(MSdata1[2]);
	
		double[][][] allTableData = new double[][][] { BDGdata1, BCKdata1,
				DBMdata1, ORdata1, MSdata1 };
	
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 8; j++) {
				BDGtable1.setValueAt(allTableData[0][i][j], i, j);
				BDGtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
			for (int j = 0; j < 7; j++) {
				BCKtable1.setValueAt(allTableData[1][i][j], i, j);
				BCKtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
			}
		}
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 6; j++) {
				DBMtable1.setValueAt(allTableData[2][i][j], i, j);
				ORtable1.setValueAt(allTableData[3][i][j], i, j);
				MStable1.setValueAt(allTableData[4][i][j], i, j);
				DBMtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				ORtable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				MStable1.getColumnModel().getColumn(j)
						.setCellRenderer(new DecimalFormatRenderer());
				// if study is not fully crossed, DBM, OR, MS calculation is
				// incorrect, tabs grayed out
			}
		}

		String output;
		
		output = threeDecE.format(BDGv);
		BDGvar1.setText("total var=" + output);
		output = threeDecE.format(BCKv);
		BCKvar1.setText("total var=" + output);
		output = threeDecE.format(DBMv);
		DBMvar1.setText("total var=" + output);
		output = threeDecE.format(ORv);
		ORvar1.setText("total var=" + output);
		output = threeDecE.format(MSv);
		MSvar1.setText("total var=" + output);

		if (DBRecordStat.flagMLE == 1) {
			tabbedPane1.setTitleAt(0, "BDG**");
			tabbedPane1.setTitleAt(1, "BCK**");
			tabbedPane1.setTitleAt(2, "DBM**");
			tabbedPane1.setTitleAt(3, "OR**");
			tabbedPane1.setTitleAt(4, "MS**");
		} else {
			tabbedPane1.setTitleAt(0, "BDG");
			tabbedPane1.setTitleAt(1, "BCK");
			tabbedPane1.setTitleAt(2, "DBM");
			tabbedPane1.setTitleAt(3, "OR");
			tabbedPane1.setTitleAt(4, "MS");
		}
		
	}

	/**
	 * Gets statistics for variance analysis in String format
	 * 
	 * @return String of statistics for variance analysis
	 */
	public String getStatResults() {
		String results = StatJLabelH0.getText();
		results = results + "\n";
		results = results + "\t" + StatJLabelAUC.getText();
		results = results + "\n";
		results = results + "\t" + StatJLabelDFNormal.getText();
		results = results + "\t" + StatJLabelPValNormal.getText();
		results = results + "\t" + StatJLabelCINormal.getText();
		results = results + "\n";
		results = results + "\t" + StatJLabelDFBDG.getText();
		results = results + "\t" + StatJLabelPValBDG.getText();
		results = results + "\t" + StatJLabelCIBDG.getText();
		results = results + "\n";
		results = results + "\t" + StatJLabelDFHillis.getText();
		results = results + "\t" + StatJLabelPValHillis.getText();
		results = results + "\t" + StatJLabelCIHillis.getText();

		return results;
	}

	/**
	 * Generates a table with the specified row and column names
	 * 
	 * @param table Table to make
	 * @param colNames List of column names
	 * @param rowNames List of row names
	 * @return Initialized table
	 */
	public JScrollPane genTable(JTable table, String[] colNames,
			String[] rowNames) {
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int i = 0; i < colNames.length; i++)
			table.getColumnModel().getColumn(i).setHeaderValue(colNames[i]);

//		JList rowHeader = new JList(rowNames);
		JList<String> rowHeader = new JList<String>(rowNames);
		rowHeader.setFixedCellWidth(80);

		rowHeader.setFixedCellHeight(table.getRowHeight());
//		rowHeader.setCellRenderer(new RowHeaderRenderer(table));
		rowHeader.setCellRenderer(new RowHeaderRenderer(table));

		JScrollPane scroll = new JScrollPane(table);
		scroll.setRowHeaderView(rowHeader);
		return scroll;

	}

	/**
	 * Initializes the BDG tab
	 * 
	 * @param rowNames Names for row labels of table
	 * @return JPanel containing BDG tab
	 */
	private JPanel makeBDGTab(String[] rowNames) {

		JPanel panelBDG = new JPanel();
		DefaultTableModel dm = new DefaultTableModel(7, 8);
		String[] BDGnames = { "M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8" };
		BDGtable1 = new JTable(dm);
		JScrollPane BDGscroll = genTable(BDGtable1, BDGnames, rowNames);
		int height = BDGtable1.getRowHeight();
		panelBDG.add(BDGscroll);
		BDGtable1.setPreferredScrollableViewportSize(new Dimension(650,
				height * 8));
		BDGtable1.setFillsViewportHeight(true);
		BDGvar1 = new JLabel("sqrt(Var)=0.00");
		panelBDG.add(BDGvar1);

		return panelBDG;
		
	}

	/**
	 * Initializes the BCK tab
	 * 
	 * @param rowNames Names for row labels of table
	 * @return JPanel containing BCK tab
	 */
	private JPanel makeBCKTab(String[] rowNames) {

		JPanel panelBCK = new JPanel();
		DefaultTableModel dm = new DefaultTableModel(7, 7);
		String[] BCKnames = { "N", "D", "N~D", "R", "N~R", "D~R", "R~N~D" };
		BCKtable1 = new JTable(dm);
		JScrollPane BCKscroll = genTable(BCKtable1, BCKnames, rowNames);
		panelBCK.add(BCKscroll);
		int height = BCKtable1.getRowHeight();
		BCKtable1.setPreferredScrollableViewportSize(new Dimension(575,
				height * 8));
		BCKtable1.setFillsViewportHeight(true);
		BCKvar1 = new JLabel("sqrt(Var)=0.00");
		panelBCK.add(BCKvar1);
		
		return panelBCK;

	}

	/**
	 * Initializes the DBM tab
	 * 
	 * @param rowNames Names for row labels of table
	 * @return JPanel containing DBM tab
	 */
	private JPanel makeDBMTab(String[] rowNames) {

		JPanel panelDBM = new JPanel();
		DefaultTableModel dm = new DefaultTableModel(3, 6);
		String[] DBMnames = { "R", "C", "R~C", "T~R", "T~C", "T~R~C" };
		DBMtable1 = new JTable(dm);
		JScrollPane DBMscroll = genTable(DBMtable1, DBMnames, rowNames);
		panelDBM.add(DBMscroll);
		int height = DBMtable1.getRowHeight();
		DBMtable1.setPreferredScrollableViewportSize(new Dimension(500,
				height * 4));
		DBMtable1.setFillsViewportHeight(true);
		DBMvar1 = new JLabel("sqrt(Var)=0.00");
		panelDBM.add(DBMvar1);

		return panelDBM;

	}

	/**
	 * Initializes the OR tab
	 * 
	 * @param rowNames Names for row labels of table
	 * @return JPanel containing OR tab
	 */
	private JPanel makeORTab(String[] rowNames) {

		JPanel panelOR = new JPanel();
		DefaultTableModel dm = new DefaultTableModel(3, 6);
		String[] ORnames = { "R", "TR", "COV1", "COV2", "COV3", "ERROR" };
		ORtable1 = new JTable(dm);
		JScrollPane ORscroll = genTable(ORtable1, ORnames, rowNames);
		panelOR.add(ORscroll);
		int height = ORtable1.getRowHeight();
		ORtable1.setPreferredScrollableViewportSize(new Dimension(500,
				height * 4));
		ORtable1.setFillsViewportHeight(true);
		ORvar1 = new JLabel("sqrt(Var)=0.00");
		panelOR.add(ORvar1);

		return panelOR;

	}

	/**
	 * Initializes the MS tab
	 * 
	 * @param rowNames Names for row labels of table
	 * @return JPanel containing MS tab
	 */
	private JPanel makeMSTab(String[] rowNames) {

		JPanel panelMS = new JPanel();
		DefaultTableModel dm = new DefaultTableModel(3, 6);
		String[] MSnames = { "R", "C", "RC", "MR", "MC", "MRC" };
		MStable1 = new JTable(dm);
		JScrollPane MSscroll = genTable(MStable1, MSnames, rowNames);
		panelMS.add(MSscroll);
		int height = MStable1.getRowHeight();
		MStable1.setPreferredScrollableViewportSize(new Dimension(500,
				height * 4));
		MStable1.setFillsViewportHeight(true);
		MSvar1 = new JLabel("sqrt(Var)=0.00");
		panelMS.add(MSvar1);

		return panelMS;

	}

	/**
	 * Enables all tabs of variance analysis
	 */
	public void enableTabs() {

		tabbedPane1.setEnabledAt(0, true);
		tabbedPane1.setEnabledAt(1, true);
		tabbedPane1.setEnabledAt(2, true);
		tabbedPane1.setEnabledAt(3, true);
		tabbedPane1.setEnabledAt(4, true);
	
	}

	/**
	 * Enables tabs relevant to BCK decomposition when performing variance
	 * analysis on manual component input
	 */
	public void enableBCKTab() {
		
		tabbedPane1.setEnabledAt(1, true);
		tabbedPane1.setEnabledAt(2, true);
		tabbedPane1.setEnabledAt(3, true);
		tabbedPane1.setSelectedIndex(1);

	}

	/**
	 * Enables tabs relevant to DBM/OR decomposition when performing variance
	 * analysis on manual component input
	 */
	public void enableDBMORTabs() {
		tabbedPane1.setEnabledAt(2, true);
		tabbedPane1.setEnabledAt(3, true);
		tabbedPane1.setSelectedIndex(2);
	}

	/**
	 * Disables all tabs of variance analysis
	 */
	public void disableTabs() {
		
		tabbedPane1.setEnabledAt(0, false);
		tabbedPane1.setEnabledAt(1, false);
		tabbedPane1.setEnabledAt(2, false);
		tabbedPane1.setEnabledAt(3, false);
		tabbedPane1.setEnabledAt(4, false);

	}
	
	/**
	 * Formats input to cells in variance analysis/trial sizing tables
	 */
	class DecimalFormatRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			DecimalFormat formatter = new DecimalFormat("0.00000E0");
			try {
				value = formatter.format((Number) value);
			} catch (ClassCastException e) {
				// for some reason sometimes value is a string containing
				// char representation of its actual value, so we parse it out
				// value = Double.parseDouble((String) value);
			}
			return super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
		}
	}

	public void setMCresults(DBRecord avgDBRecordStat, DBRecord varDBRecordStat) {

		double mcAvgAUC_A       = avgDBRecordStat.AUCsReaderAvg[0];
		double mcAvgAUC_B       = avgDBRecordStat.AUCsReaderAvg[1];
		double mcAvgAUC_AminusB = avgDBRecordStat.AUCsReaderAvg[2];
		double mcVarAUC_A       = varDBRecordStat.AUCsReaderAvg[0];
		double mcVarAUC_B       = varDBRecordStat.AUCsReaderAvg[1];
		double mcVarAUC_AminusB = varDBRecordStat.AUCsReaderAvg[2];
		double sqrtMCvarAUC_A       = Math.sqrt(mcVarAUC_A);
		double sqrtMCvarAUC_B       = Math.sqrt(mcVarAUC_B);
		double sqrtMCvarAUC_AminusB = Math.sqrt(mcVarAUC_AminusB);
		
		System.out.println("      mcAvgAUC_A = " + mcAvgAUC_A +
					   ",         mcVarAUC_A = " + mcVarAUC_A +
					   ",         mcStdAUC_A = " + Math.sqrt(mcVarAUC_A));
		System.out.println("      mcAvgAUC_B = " + mcAvgAUC_B +
					   ",         mcVarAUC1 = " + mcVarAUC_B +
					   ",         mcStdAUC1 = " + Math.sqrt(mcVarAUC_B));
		System.out.println("mcAvgAUC_AminusB = " + mcAvgAUC_AminusB +
					   ",   mcVarAUC_Aminus1 = " + mcVarAUC_AminusB +
					   ",   mcStdAUC_Aminus1 = " + Math.sqrt(mcVarAUC_AminusB));
		
		JLabel JLabelVarAUC_A              = new JLabel("12345678901234567890123456789012345",JLabel.RIGHT);
		JLabel JLabelVarAUC_B              = new JLabel("12345678901234567890123456789012345",JLabel.RIGHT);
		JLabel JLabelVarAUC_AminusB        = new JLabel("12345678901234567890123456789012345",JLabel.RIGHT);
		JLabel JLabelSqrtVarAUC_A          = new JLabel("12345678901234567890123456789012345",JLabel.RIGHT);
		JLabel JLabelSqrtVarAUC_B          = new JLabel("12345678901234567890123456789012345",JLabel.RIGHT);
		JLabel JLabelSqrtVarAUC_AminusB    = new JLabel("12345678901234567890123456789012345",JLabel.RIGHT);

		JLabelVarAUC_A.setPreferredSize(JLabelVarAUC_A.getPreferredSize());
		JLabelVarAUC_B.setPreferredSize(JLabelVarAUC_B.getPreferredSize());
		JLabelVarAUC_AminusB.setPreferredSize(JLabelVarAUC_AminusB.getPreferredSize());
		JLabelSqrtVarAUC_A.setPreferredSize(JLabelSqrtVarAUC_A.getPreferredSize());
		JLabelSqrtVarAUC_B.setPreferredSize(JLabelSqrtVarAUC_B.getPreferredSize());
		JLabelSqrtVarAUC_AminusB.setPreferredSize(JLabelSqrtVarAUC_AminusB.getPreferredSize());
		
		JLabelSqrtVarAUC_A.setText("sqrtMCvarAUC_A = " + fourDecE.format(sqrtMCvarAUC_A) + ",");
		JLabelVarAUC_A.setText("mcVarAUC_A = " + fourDecE.format(mcVarAUC_A));
		JLabelSqrtVarAUC_B.setText("sqrtMCvarAUC_B = " + fourDecE.format(sqrtMCvarAUC_B) + ",");
		JLabelVarAUC_B.setText("mcVarAUC_B = " + fourDecE.format(mcVarAUC_B));
		JLabelSqrtVarAUC_AminusB.setText("sqrtMCvarAUC_AminusB = " + fourDecE.format(sqrtMCvarAUC_AminusB) + ",");
		JLabelVarAUC_AminusB.setText("mcVarAUC_AminusB = " + fourDecE.format(mcVarAUC_AminusB));

		JPanel StatPanelBot1 = new JPanel();
		StatPanelBot1.add(JLabelVarAUC_A);
		StatPanelBot1.add(JLabelSqrtVarAUC_A);
		JPanel StatPanelBot2 = new JPanel();
		StatPanelBot2.add(JLabelVarAUC_B);
		StatPanelBot2.add(JLabelSqrtVarAUC_B);
		JPanel StatPanelBot3 = new JPanel();
		StatPanelBot3.add(JLabelVarAUC_AminusB);
		StatPanelBot3.add(JLabelSqrtVarAUC_AminusB);

		JPanelStat.add(StatPanelBot1);
		JPanelStat.add(StatPanelBot2);
		JPanelStat.add(StatPanelBot3);
	
	}

	
	public class StatHillisButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String hillisValues = StatJLabelDFHillis.getText() +"\n"+ 
					StatJLabelPValHillis.getText() + "\n" + 
					StatJLabelCIHillis.getText() + "\n" + 
					StatJLabelRejectHillis.getText();
					
			// TODO Auto-generated method stub
			JOptionPane.showMessageDialog(JFrameApp,
					hillisValues, "Hillis Approximation",
					JOptionPane.PLAIN_MESSAGE);
		}

	}
}
