/*
 * RoeMetz.java
 * 
 * v2.0b
 * 
 * @Author Rohan Pathare
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
 *     Entry point of iRoeMetz application
 */

package roemetz.core;

import java.awt.Container;

import javax.swing.*;

import roemetz.gui.RMGUInterface;

public class RoeMetz extends JApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static JFrame iRMFrame;
	private RMGUInterface gui;

	public JFrame getFrame() {
		return iRMFrame;
	}

	public void init() {
		super.init();
		setLayout(null);
		resize(6, 6);

		Container cp = getContentPane();
		gui = new RMGUInterface(this, cp);
	}

	public static void main(String[] args) {
		run(new RoeMetz(), 1000, 550);
	}

	public static void run(JApplet applet, int width, int height) {
		iRMFrame = new JFrame("iRoeMetz 2.0b");
		iRMFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		iRMFrame.getContentPane().add(applet);
		iRMFrame.pack();
		iRMFrame.setSize(width, height);
		applet.init();
		applet.start();
		iRMFrame.setVisible(true);
	}
}
