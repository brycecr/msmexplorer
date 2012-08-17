/*
 * Copyright (C) 2012 Pande Lab
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
package edu.stanford.folding.msmexplorer.io.hierarchy;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Comparator;
import java.io.File;
import java.io.FileNotFoundException;


class FileNode {
	public String tProbFilename;
	public String mmapFilename;
	public String eqProbFilename;
	public int numStates;
}

/**
 * The HierarchySketcher is responsible for combing a directory hierarchy and
 * indexing the available MSMs and their relative relationship.
 *
 * The layout of the hierarchy is important to making this work properly:
 * When prompted users should select a FOLDER that contains a selection of FOLDERS
 * where each folder that participates in the hierarchy contains:
 *	> a file that starts with the exact characters "tProb" . If this file has the extension ".mtx"
 * 		the sketcher assumes it is a sparse matrix file; otherwise, it assumes it is a dense matrix file.
 *  > a file that starts with the exact characters "MacroMapping" which contains a newline-delimited
 * 		mapping of each microstate to some macrostate in the model represented by tProb (i.e. in this macrostate model)
 *		The MacroMapping file can not exist in the model in the hierarchy with the largest number of states
 */
public class HierarchySketcher {

	private static final String mapping_col = "mapping";

	private HierarchySketcher() {
		//prevent instantiation
	}

	static FileNode[] sketch(String pathToHierarchy) {
		File topList[] = new File[0];

		try { 
			File top = new File (pathToHierarchy);
			topList = top.listFiles();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Unable to open Hierarchy at " + pathToHierarchy, 
				"Hierarchy Read Error", JOptionPane.ERROR_MESSAGE);
		}

		ArrayList<File> fileList = new ArrayList<File>(Arrays.asList(topList));		
		ArrayList<FileNode> nodes = new ArrayList<FileNode>(5);
		for (File f : fileList) {
			if (f.isDirectory()) {

				String filesInDir[] = f.list();

				boolean hasTProb = false;
				boolean hasMMap = false;
				boolean hasEQProb = false;
				FileNode node = new FileNode ();
				int maxStates = 0;
				for (String str : filesInDir) {

					if (str.startsWith("tProb")) {
						assert !hasTProb;
						hasTProb = true;
						node.tProbFilename = f.getAbsolutePath() + '/' + str;
						node.numStates = getNumStates(node.tProbFilename);
						if (node.numStates > maxStates) {
							maxStates = node.numStates;
						}
						if (hasMMap && hasEQProb) {
							break;
						}

					} else if (str.startsWith("MacroMapping")) {
						assert !hasMMap;
						hasMMap = true;
						node.mmapFilename = f.getAbsolutePath() + '/' + str;
						if (hasTProb && hasEQProb) {
							break;
						}
					} else if (str.startsWith("Population") ||
						str.startsWith("eqProb") ||
						str.startsWith("eqprob")) {
						assert !hasEQProb;
						node.eqProbFilename = f.getAbsolutePath() + '/' + str;
						if (hasTProb && hasMMap) {
							break;
						}
					}
				}
				if (hasTProb && (hasMMap || node.numStates == maxStates)) {
					nodes.add(node);
				}
			}
		}

		FileNode nodeArray[] = nodes.toArray(new FileNode[nodes.size()]);
		Arrays.sort(nodeArray, new FileNodeComparator());

		String agg = "MSMExplorer found these files to construct the hierarchy:\n\n";
		for (FileNode fn : nodeArray) {
			agg = agg + "Model with " + fn.numStates + " states" + "\n\t" 
				+ "tProb Matrix File: " + fn.tProbFilename + "\n\t" 
				+ "Mapping file: " + fn.mmapFilename + '\n';
		}

		JOptionPane.showMessageDialog(null, agg);

		return nodeArray;
	}

	private static int getNumStates(String filePath) {
		int i = 0;
		Scanner scn = null;
		try {
				scn = new Scanner (new File(filePath)); 
			} catch (FileNotFoundException fnfe) {
				JOptionPane.showMessageDialog(null, "Something slipped out from under us. Could not open file at " + filePath);
			}
		if (filePath.endsWith(".mtx")) {
			while (!scn.hasNextInt() && scn.hasNextLine()) {
				scn.nextLine();
			}
			if (scn.hasNextInt()) {
				i = scn.nextInt();
			}

		} else { //I guess we can pretend anything else is a dense matrix, but it might not go well later on...XXX should fix this
			while (scn.hasNextLine()) {
				scn.nextLine();
				i++;
			}
		}
		return i;
	}

	private static class FileNodeComparator implements Comparator<FileNode> {
		public int compare (FileNode a, FileNode b) {
			return a.numStates - b.numStates;
		}
	}
}