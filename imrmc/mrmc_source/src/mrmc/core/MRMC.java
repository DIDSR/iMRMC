/*
 * MRMC.java
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
 *     Entry point of application
 */

package mrmc.core;

import javax.swing.*;

import mrmc.gui.GUImenubar;
import mrmc.gui.GUInterface;

import java.awt.*;

public class MRMC extends JApplet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static JFrame mainFrame;
	GUInterface gui;
	GUImenubar menuBar;
	mrmcDB db;

	public int getDBSize() {
		return db.getNoOfItems();
	}

	public mrmcDB getDB() {
		return db;
	}

	public JFrame getFrame() {
		return mainFrame;
	}

	public void init() {

		super.init();
		setLayout(null);
		resize(6, 6);

		// create the database
		db = new mrmcDB(this);

		Container cp = getContentPane();
		// Create the interface
		gui = new GUInterface(this, cp);
		menuBar = new GUImenubar(this);
		setJMenuBar(menuBar.getMenuBar());

	}

	public static void main(String[] args) {

		/*
		 * test whether the user is using it as a command line application or
		 * load it with a web browser The main function is not called in a
		 * applet. the program enters from init() so isApplet remains true in
		 * that case. if it is a command line application, the program enters
		 * here
		 */
		 run(new MRMC(), 1000, 550);
	}

	public static void run(JApplet applet, int width, int height) {
		mainFrame = new JFrame("iMRMC 2.0b");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.getContentPane().add(applet);
		mainFrame.pack();
		mainFrame.setSize(width, height);
		applet.init();
		applet.start();
		mainFrame.setVisible(true);
	}
} 
