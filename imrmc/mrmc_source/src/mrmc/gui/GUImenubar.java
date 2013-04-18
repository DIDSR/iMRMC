/*
 * GUImenubar.java
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
 *     Top menu bar with drop-down menu button displaying information about application.
 */

package mrmc.gui;

import javax.swing.*;
import java.awt.event.*;
import mrmc.core.MRMC;

public class GUImenubar {
	MRMC lst;
	JMenuBar menuBar = new JMenuBar();
	String Manual = "Dowload manual, sample file, source code and database at\n"
			+ "http://js.cx/~xin/download.html\n";
	String About = "MRMC 1.0\n" + "Developed by Xin He and Brandon Gallas\n"
			+ "http://www.fda.gov\n";
	String Ref = "1. A Probabilistic Development of the MRMC Method, Eric Clarkson, Matthew A.Kupinski,\n"
			+ "   Harrison H. Barrett, Academic Radiology. 2006 Mar;13(3):353-62.\n"
			+ "2. One-Shot Estimate of MRMC Variance: AUC, Brandon D. Gallas, Academic Radiology 13,\n"
			+ "   pp. 353-362, 2006.\n"
			+ "3. Reader studies for validation of CAD systems, Brandon D. Gallas, David G. Brown,\n"
			+ "   Neural Networks, 21,387-397, 2008)\n"
			+ "4. A Framework for Random-Effects ROC Analysis: Biases with the Bootstrap and Other\n"
			+ "   Variance Estimators, Communications in Statistics-Theory and Methods,38: 2586-2603,\n"
			+ "   2009.\n"
			+ "5. Power Estimation for the Dorfman-Berbaum-Metz Method,Hillis, S. L. & Berbaum, K. S.,\n"
			+ "   Acad Radiol 11(11), 1260-1273, 2004.\n"
			+ "6. A Comparison of the Dorfman-Berbaum-Metz and Obuchowski-Rockette Methods for Receiver\n"
			+ "   Operating Characteristic (ROC) Data, Hillis, S. L., Obuchowski, N. A., Schartz, K. M.,\n"
			+ "   and Berbaum, K. S. (2005), Stat Med 24(10), 1579-1607.\n"
			+ "7. Multireader, Multimodality Receiver Operating Characteristic Curve Studies: Hypothesis\n"
			+ "   Testing and Sample Size Estimation Using an Analysis of Variance Approach with Dependent\n"
			+ "   Observations,Obuchowski, N. A., Acad Radiol 2(Suppl 1), S22-S29.\n"
			+ "8. Receiver operating characteristic rating analysis: generalization to the population of\n"
			+ "   readers and patients with the jackknife method, D. D. Dorfman, K. S. Berbaum, and C. E.\n"
			+ "   Metz, Invest. Radiol. 27, 723-731 (1992).\n";

	public GUImenubar(MRMC lsttemp) {
		lst = lsttemp;
		createMenuBar();
	}

	public JMenuBar getMenuBar() {
		return menuBar;
	}

	class menuRefListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Menu Ref clicked");
			JFrame frame = lst.getFrame();
			JOptionPane.showMessageDialog(frame, Ref, "Reference",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	class menuAboutListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Menu about clicked");
			JFrame frame = lst.getFrame();
			JOptionPane.showMessageDialog(frame, About, "About",
					JOptionPane.INFORMATION_MESSAGE);
		}

	}

	class menuManualListener implements ActionListener {
		JFrame frame2 = new JFrame("diaglog manual");

		public void actionPerformed(ActionEvent event) {
			System.out.println("Manual about clicked");
			JFrame frame = lst.getFrame();
			JOptionPane.showMessageDialog(frame, Manual, "Download",
					JOptionPane.INFORMATION_MESSAGE);
			/*
			 * frame2.setSize(200, 200); JPanel ps=new JPanel();
			 * 
			 * try { URI uri = new
			 * URI("http://www.batan.go.id/ppin/lokakarya/LKSTN_12/Arko.pdf");
			 * JButton button = new JButton("OKOK"); button.setText(
			 * "<HTML>Click the <FONT color=\"#000099\"><U>link</U></FONT>" +
			 * " to go to the Java website.</HTML>");
			 * button.setHorizontalAlignment(SwingConstants.LEFT);
			 * button.setBorderPainted(false); button.setOpaque(false);
			 * button.setBackground(Color.WHITE);
			 * button.setToolTipText(uri.toString());
			 * button.addActionListener(new OpenUrlAction(uri)); ps.add(button);
			 * } catch (URISyntaxException e) { }
			 * 
			 * JButton buttonOK= new JButton("close");
			 * buttonOK.addActionListener(new ButtonOKListner());
			 * ps.add(buttonOK); frame2.add(ps); frame2.setVisible(true);
			 */
		}
		/*
		 * class ButtonOKListner implements ActionListener { public void
		 * actionPerformed(ActionEvent event) {frame2.dispose();} } private void
		 * open(URI uri) { if (Desktop.isDesktopSupported()) { try {
		 * Desktop.getDesktop().browse(uri); } catch (IOException e) { } } else
		 * { } }
		 * 
		 * class OpenUrlAction implements ActionListener { URI localURI; public
		 * OpenUrlAction(URI uri) { localURI=uri; }
		 * 
		 * @Override public void actionPerformed(ActionEvent e) {
		 * open(localURI); } }
		 */

	}

	private void createMenuBar() {
		JMenu menu;
		JMenuItem menuItem;
		menu = new JMenu("Menu");
		menuBar.add(menu);

		menuItem = new JMenuItem("Reference");
		menuItem.addActionListener(new menuRefListener());
		menu.add(menuItem);
		menuItem = new JMenuItem("About MRMC");
		menuItem.addActionListener(new menuAboutListener());
		menu.add(menuItem);
		menuItem = new JMenuItem("Manual");
		menuItem.addActionListener(new menuManualListener());
		menu.add(menuItem);
	}

}
