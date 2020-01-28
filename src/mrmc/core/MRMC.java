package mrmc.core;

import javax.swing.*;

import mrmc.gui.GUImenubar;
import mrmc.gui.GUInterface;

import java.awt.*;
import java.util.Locale;

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
	public static final String versionname ="iMRMC Version 4.0.3";
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
		Locale.setDefault(Locale.US);
		String inputFileFullName = "";
		String outputFolderFullName = "";
		if (args.length == 1){
			inputFileFullName = args[0];
			commandStart = true;			
		}else if(args.length == 2){
			inputFileFullName = args[0];
			outputFolderFullName = args[1];
			commandStart = true;
		}
		int width = 900, height = 620;
		run(new MRMC(), width, height);
		commandStartFunction commandStartFunction = new commandStartFunction();
		commandStartFunction.commandStartFunction(gui, inputFileFullName,outputFolderFullName);
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
		mrmcFrame.setMinimumSize(new Dimension(width, height));
		// We shall override the normal init method.
		// Please refer to the init method in this (sub)class 
		MRMC.init();
		MRMC.start();
		if (!commandStart)
		mrmcFrame.setVisible(true);
	}
}
