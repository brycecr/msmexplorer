package edu.stanford.folding.msmexplorer.io.hierarchy;

import prefuse.data.Graph;

/**
 *
 * @author gestalt
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
