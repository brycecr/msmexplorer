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

import prefuse.data.Graph;

/**
 * A simple class to store all the info needed by an MSMExplorer
 * instance to successfully traverse an MSMHierarchy. Currently,
 * that includes only an arragy of graphs (ordered by ascending
 * height in the hierarchy) and an array of strings of mapping file
 * locations index-aligned with the graph array.
 *
 * @author brycecr
 */
public class HierarchyBundle {

	public final Graph[] graphs;
	public final String[] mappings;

	/** 
	 * Construct an empty HierarchyBundle 
	 */
	protected HierarchyBundle() {
		graphs = null;
		mappings = null;
	}
	
	HierarchyBundle(Graph[] gs, FileNode[] fs) {
		graphs = gs;
		mappings = new String[fs.length];
		for (int i = 0; i < fs.length; ++i) {
			mappings[i] = fs[i].mmapFilename;
		}
	}
}
