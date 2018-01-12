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
		versionName = "iRoeMetz 2.2beta";
		iRMFrame = new JFrame("iRoeMetz 2.2beta");
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
