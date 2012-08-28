/*
 * Copyright (C) 2012 Stanford University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package edu.stanford.folding.msmexplorer;

import javax.swing.UIManager;

/**
 * A simple launcher class that serves as the main class for 
 * the application. This was built to replace the main class
 * in the MSMExplorer class, because for some reason the OSX
 * native Menu Bar name has to be set from a main loop before
 * the main GUI thread starts.
 * 
 * @author brycecr
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
