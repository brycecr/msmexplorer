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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.column.Column;
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
				+ f.getAbsolutePath(), "Hierarchy Open Error", 
				JOptionPane.ERROR_MESSAGE);
			return new HierarchyBundle();
		}

		assert newNode.length > 0;

		GraphReader gr = null;
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

	public static Dictionary<Integer, JLabel> getHierarchyLabels(Graph[] gs) {
		Dictionary<Integer, JLabel> dict = new Hashtable<Integer, JLabel>(gs.length);
		for (int i = 0; i < gs.length; ++i) {
			dict.put(i, new JLabel(Integer.toString(gs[i].getNodeCount())));
		}
		return dict;
	}


	public boolean setMapping(HierarchyBundle hb, int bottom, int top) {
		
		Object[] top_mappings = NewlineDelimitedReader.read(hb.mappings[top]);

		Table nt = hb.graphs[bottom].getNodeTable();
		if (nt.getColumnNumber(MAPPING) < 0) {
			nt.addColumn(MAPPING, Integer.class);
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
			for (int row = 0; row < t.getRowCount(); ++row) {
				int top_state = top.getInt(row);
				t.set(row, MAPPING, );
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
