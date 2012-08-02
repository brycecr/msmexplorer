/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.folding.msmexplorer;

import javax.swing.UIManager;

/**
 * A simple launcher class that serves as the main class for 
 * the application. This was built to replace the main class
 * in the MSMExplorer class, because for some reason the OSX
 * native Menu Bar name has to be set from a main loop before
 * 
 * @author gestalt
 */
public class MSMExplorerLauncher {
	
	public static void main (String[] argv) {
		try {
			if (System.getProperty("os.name").startsWith("Mac")) {
				// Set System L&F
				System.setProperty("apple.laf.useScreenMenuBar", "true");
				System.setProperty("com.apple.macos.useScreenMenuBar", "true");
				System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MSMExplorer");
			}
			UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
			// handle exception
		}
		new MSMExplorer();
	}

}
