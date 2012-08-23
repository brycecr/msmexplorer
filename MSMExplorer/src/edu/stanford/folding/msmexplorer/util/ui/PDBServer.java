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
package edu.stanford.folding.msmexplorer.util.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 *
 * @author gestalt
 */
public class PDBServer {

	/*
	 * Opens the correspondig pdb in pymol or vmd
	 * 
	 */
	public static void openPDB (int row, String location) 
		throws InterruptedException, IOException {

/*
		
		ProcessBuilder builder = new ProcessBuilder("pymol", location+"State-"+row+"-0.pdb");

		Map<String,String> environ = builder.environment();
		environ.put("PATH", "/opt/local/bins);
		//builder.directory(new File(System.getProperty("user.home")));

		final Process godot = builder.start();
		godot.waitFor();
 * 
 */
		Process proc = Runtime.getRuntime().exec("pymol "+location+"State"+row+"-0.pdb");

		BufferedReader read=new BufferedReader(new InputStreamReader(proc.getInputStream()));
			
			while(read.ready())
			{
				System.out.println(read.readLine());
			}
	}
	
}
