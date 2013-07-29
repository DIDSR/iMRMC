/**
 * MRMC.java
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

package mrmc.core;

import javax.swing.*;

import mrmc.gui.GUImenubar;
import mrmc.gui.GUInterface;

import java.awt.*;

/**
 * Entry point of application
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 * @version 2.0b
 */
public class MRMC extends JApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static JFrame mrmcFrame;
	GUInterface gui;
	GUImenubar menuBar;
	MrmcDB db;

	/**
	 * Gets the number of entries in the database
	 * 
	 * @return Number of entries in the database
	 */
	public int getDBSize() {
		return db.getNoOfItems();
	}

	/**
	 * Gets the database object
	 * 
	 * @return MrmcDB object for this instance of the application
	 */
	public MrmcDB getDB() {
		return db;
	}

	/**
	 * Gets the application frame, used when launching dialog boxes
	 * 
	 * @return Application JFrame
	 */
	public JFrame getFrame() {
		return mrmcFrame;
	}

	/**
	 * Creates the GUI and top-level menu bar
	 */
	public void init() {

		super.init();
		setLayout(null);
		resize(6, 6);

		// create the database
		db = new MrmcDB();

		Container cp = getContentPane();
		// Create the interface
		gui = new GUInterface(this, cp);
		menuBar = new GUImenubar(this);
		setJMenuBar(menuBar.getMenuBar());

	}

	/**
	 * Entry point of the application. Sets the look-and-feel to match that of
	 * the user's OS and starts the application with the specified frame size.
	 * 
	 * @param args Command-line arguments, not used
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		int width = 1000, height = 650;
		run(new MRMC(), width, height);
	}

	/**
	 * Creates the frame for the application, starts it and displays it
	 * 
	 * @param applet This application. It is called "applet" but we only run
	 *            from a standalone jar so it is more of an "application"
	 * @param width Width of the application frame in pixels
	 * @param height Height of the application frame in pixels
	 */
	public static void run(JApplet applet, int width, int height) {
		mrmcFrame = new JFrame("iMRMC 2.0b");
		mrmcFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mrmcFrame.getContentPane().add(applet);
		mrmcFrame.pack();
		mrmcFrame.setSize(width, height);
		applet.init();
		applet.start();
		mrmcFrame.setVisible(true);
	}
}
