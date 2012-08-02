/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.folding.msmexplorer;

import javax.swing.UIManager;

/**
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
