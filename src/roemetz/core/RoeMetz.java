/**
 * RoeMetz.java
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

package roemetz.core;

import java.awt.Container;
import java.util.Scanner;

import javax.swing.*;

import roemetz.gui.RMGUInterface;

/**
 * Entry point of iRoeMetz application
 * 
 * @author Rohan Pathare
 */
public class RoeMetz extends JApplet {
    
	public static String versionName;
	private static final long serialVersionUID = 1L;
	private static JFrame iRMFrame;
    public static RMGUInterface RMGUInterface1;
    public static boolean doValidation = false;
	/**
	 * Gets the application frame, used when launching dialog boxes
	 * 
	 * @return Application JFrame
	 */
	public JFrame getFrame() {
		return iRMFrame;
	}

	/**
	 * Creates the GUI
	 */
	public void init() {
		super.init();
		setLayout(null);
		resize(6, 6);

		Container cp = getContentPane();
		RMGUInterface1 = new RMGUInterface(this, cp);
		
	}

	/**
	 * Entry point of application. Sets the look-and-feel to match that of the
	 * user's OS and starts the application with specified frame size.
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
			doValidation = true;
		}
		run(new RoeMetz(), 900, 600);
		validateFunction.validateFunction(RMGUInterface1,inputFileFullName);
	}

	/**
	 * Creates the frame for the application, starts it and displays it
	 * 
	 * @param applet This application. Is called "applet" but we only run from
	 *            standalone jar so it is more of an "application"
	 * @param width Width of the application frame in pixels
	 * @param height Height of the application frame in pixels
	 */
	public static void run(JApplet applet, int width, int height) {
		versionName = "iRoeMetz 2.0";
		iRMFrame = new JFrame("iRoeMetz 2.0");
		iRMFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		iRMFrame.getContentPane().add(applet);
		iRMFrame.pack();
		iRMFrame.setSize(width, height);
		applet.init();
		applet.start();
		if (!doValidation)
		iRMFrame.setVisible(true);
	}
}
