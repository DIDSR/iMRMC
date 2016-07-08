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
 * Entry point of application <br>
 * ---- Launches {@link mrmc.gui.GUInterface GUInterface} and {@link mrmc.gui.GUImenubar GUImenubar}.
 * 
 * @author Xin He, Ph.D,
 * @author Brandon D. Gallas, Ph.D
 * @author Rohan Pathare
 */
public class MRMC extends JApplet {	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static JFrame mrmcFrame;
	public static final String versionname ="iMRMC Version 2p9 Beta";
	public static boolean commandStart = false;
	static GUInterface gui;
	GUImenubar menuBar;

	/**
	 * Gets the application frame, used when launching dialog boxes
	 * 
	 * @return Application JFrame
	 */
	public JFrame getFrame() {
		return mrmcFrame;
	}

	/**
	 * Creates gui and menuBar from constructors {mrmc.gui.GUInterface} and {@link mrmc.gui.GUImenubar} <br>
	 * <br>
	 * CALLED FROM: {@link #run(JApplet, int, int)}
	 */
	@Override
	public void init() {

		super.init();
		setLayout(null);
		resize(6, 6);

		Container cp = getContentPane();
		// Create the interface
		gui = new GUInterface(this, cp);
		menuBar = new GUImenubar(this);
		setJMenuBar(menuBar.getMenuBar());

	}

	/**
	 * Sets the look-and-feel to match that of
	 * the user's OS and starts the application with the specified frame size. <br>
	 * ---- Creates object MRMC from class jApplet
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
		String inputFileFullName = "";
		if (args.length != 0){
			inputFileFullName = args[0];
			commandStart = true;			
		}
		int width = 1000, height = 750;
		run(new MRMC(), width, height);
		cmonnandStartFunction cmonnandStartFunction1 = new cmonnandStartFunction();
		cmonnandStartFunction1.cmonnandStartFunction(gui, inputFileFullName);
	}

	/**
	 * Creates the frame for the application; adds, inits, and starts MRMC applet.
	 * 
	 * @param applet This application. It is called "applet" but we only run
	 *            from a standalone jar so it is more of an "application"
	 * @param width Width of the application frame in pixels
	 * @param height Height of the application frame in pixels
	 */
	public static void run(JApplet MRMC, int width, int height) {
		mrmcFrame = new JFrame(versionname);
		mrmcFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mrmcFrame.getContentPane().add(MRMC);
		mrmcFrame.pack();
		mrmcFrame.setSize(width, height);

		// We shall override the normal init method.
		// Please refer to the init method in this (sub)class 
		MRMC.init();
		MRMC.start();
		if (!commandStart)
		mrmcFrame.setVisible(true);
	}
}
