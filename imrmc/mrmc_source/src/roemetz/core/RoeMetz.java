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
	RMGUInterface gui;

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
		iRMFrame.setSize(width, height);
		iRMFrame.pack();
		applet.init();
		applet.start();
		iRMFrame.setVisible(true);
	}
}
