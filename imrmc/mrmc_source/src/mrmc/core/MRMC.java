/*
 * MRMC.java
 * 
 * v1.0
 * 
 * @Author Xin He, Phd, Brandon D. Gallas, PhD, Rohan Pathare
 * 
 * Copyright 2013 Food & Drug Administration, Division of Image Analysis & Mathematics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
		mainFrame = new JFrame("iMRMC 1.0");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.getContentPane().add(applet);
		mainFrame.pack();
		mainFrame.setSize(width, height);
		applet.init();
		applet.start();
		mainFrame.setVisible(true);
	}
} 
