/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.folding.msmexplorer.io.hierarchy;

import edu.stanford.folding.msmexplorer.io.DatGraphReader;
import edu.stanford.folding.msmexplorer.io.EQProbReader;
import edu.stanford.folding.msmexplorer.io.MSMIOLib;
import edu.stanford.folding.msmexplorer.io.MtxGraphReader;
import edu.stanford.folding.msmexplorer.io.NewlineDelimitedReader;

import java.awt.Component;
import java.io.File;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;

//supposedly the Hashtable class is obsolete, but
//it seems to be the only reasonable implementation
//of Dictionary, which JSlider needs for labels.
import java.util.Hashtable; 
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.io.GraphReader;
import prefuse.util.StringLib;
import prefuse.util.io.IOLib;

/**
 *
 * @author gestalt
 */
public class HierarchyIOLib {

	private static final String DEFAULT_DIRECTORY = "~/Documents";
	private static final String MAPPING = "mapping";

	private HierarchyIOLib() {
		//prevent instantiation
	}

	public static HierarchyBundle openMSMHierarchy(Component c) {
		return openMSMHierarchy(c, DEFAULT_DIRECTORY);
	}

	public static HierarchyBundle openMSMHierarchy(Component c, String path) {
		JFileChooser jfc = new JFileChooser(path);
		jfc.setDialogType(JFileChooser.OPEN_DIALOG);
		jfc.setDialogTitle("Open Hierarchical MSM Directory");
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int opt = jfc.showOpenDialog(c);
		if (opt != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		File f = jfc.getSelectedFile();

		FileNode newNode[] = HierarchySketcher.sketch(f.getAbsolutePath());
		if (newNode.length == 0) {
			JOptionPane.showMessageDialog(null, 
				"Could not find MSM hierarchy at " 
				+ f.getAbsolutePath() + "\nNot a "
				+ "properly formatted hierarchy", 
				"Hierarchy Open Error", 
				JOptionPane.ERROR_MESSAGE);
			return new HierarchyBundle();
		}

		assert newNode.length > 0;

		GraphReader gr;
		if (newNode[0].tProbFilename.endsWith(".mtx")
				|| newNode[0].tProbFilename.endsWith(".MTX")) {
			gr = new MtxGraphReader();
		} else {
			gr = new DatGraphReader();
		}

		Graph[] graphs = new Graph[newNode.length];
		for (int i = 0; i < newNode.length; ++i) {
			try {
				graphs[i] = gr.readGraph(IOLib.streamFromString(newNode[i].tProbFilename));
				if (newNode[i].eqProbFilename == null) {
					graphs[i] = EQProbReader.getEqProbs(null, graphs[i]);
				} else {
					graphs[i] = EQProbReader.addEqProbs(graphs[i], 
						new File(newNode[i].eqProbFilename));
				}

			} catch (Exception e) {
				Logger.getLogger(MSMIOLib.class.getName()).log(
					Level.WARNING, "{0}\n{1}",
					new Object[]{e.getMessage(), StringLib.getStackTrace(e)});
				return null;
			}
		}
		HierarchyBundle hb = new HierarchyBundle(graphs, newNode);
		assert hb != null;
		return hb;
	}

	/**
	 * Generates hierarchy labels for an array of graphs. Returned in the form of
	 * a Dictionary so that labels can be applied to JSliders, which for
	 * some reason insist on using the deprecated Dictionary types.
	 * Labels are just the number of nodes in the corresponding graph. 
	 * 
	 * @param gs the array of graphs to generate labels for
	 * @return a Dictionary containing a mapping of ints (graph indicies)
	 * to JLabels which contain the mapping for the int key.
	 */
	public static Dictionary<Integer, JLabel> getHierarchyLabels(Graph[] gs) {
		Dictionary<Integer, JLabel> dict = new Hashtable<Integer, JLabel>(gs.length);
		for (int i = 0; i < gs.length; ++i) {
			dict.put(i, new JLabel(Integer.toString(gs[i].getNodeCount())));
		}
		return dict;
	}

	/**
	 * Used to label the overlay slider. Replaces the top (highest population)
	 * member of the dictionary with the label "None".
	 * 
	 * @param gs Graph array to use
	 * @return Dictionary of labels
	 */
	public static Dictionary<Integer, JLabel> getAltHierarchyLabels(Graph[] gs) {
		Dictionary<Integer, JLabel> dict = getHierarchyLabels(gs);
		Enumeration e = dict.keys();
		Integer i = -1;
		while (e.hasMoreElements()) {
			Integer current = (Integer)e.nextElement();
			if (current > i) {
				i = current;
			}
		}
		dict.remove(i);
		dict.put(i, new JLabel("None"));
		return dict;
	}

	public static boolean setMapping(HierarchyBundle hb, int bottom, int top) {
		
		Object[] top_mappings = NewlineDelimitedReader.read(hb.mappings[top]);

		Table nt = hb.graphs[bottom].getNodeTable();
		if (nt.getColumnNumber(MAPPING) < 0) {
			nt.addColumn(MAPPING, int.class);
		}

		// The simple case: we're mapping to the model with the most
		// states, so we can just copy from the file
		if (bottom == hb.mappings.length - 1) {
			for (int j = 0; j < top_mappings.length; ++j) {
				hb.graphs[bottom].getNode(j).setInt(MAPPING, (Integer)top_mappings[j]);
			}
			return true;
		}

		//The more complex case: generate the mapping
		Object[] bottom_mappings = NewlineDelimitedReader.read(hb.mappings[bottom]);
		
		//if no mapping column currently available
		try {
			HashMap<Integer, Integer> seen_mappings = new HashMap<Integer, Integer>();
			boolean nonnested = false;
			for (int row = 0; row < bottom_mappings.length; ++row) {
				int top_state = (Integer)top_mappings[row];
				int bottom_state = (Integer)bottom_mappings[row];
				if (!seen_mappings.containsKey(bottom_state)) {
					seen_mappings.put(bottom_state, top_state);
				} else if (top_state != seen_mappings.get(bottom_state) && !nonnested) {
					//FIXME handle this case...with arrays?
					nonnested = true;
					JOptionPane.showMessageDialog(null, 
						"The hierarchy you are trying "
						+ "to display is not nested. We"
						+ " currently don't support "
						+ "non-nesting hierarchies, so"
						+ " you'll get a faux nested "
						+ "analog of the model", 
						MAPPING, JOptionPane.ERROR_MESSAGE);
				}
			}
			for (Integer i : seen_mappings.keySet()) {
				nt.set(i, MAPPING, seen_mappings.get(i));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, 
				"Exception while attempting to set the mapping "
				+ "for this hierarchy overlay: \n" + 
				e.toString(), "Mapping Overlay Error", 
				JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	
}
