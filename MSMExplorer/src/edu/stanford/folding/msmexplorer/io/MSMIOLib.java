package edu.stanford.folding.msmexplorer.io;

import java.awt.Component;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Hashtable;
import java.util.Dictionary;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;
import javax.swing.JOptionPane;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.data.io.GraphMLWriter;
import prefuse.data.io.GraphReader;
import prefuse.util.StringLib;
import prefuse.util.io.IOLib;
import prefuse.util.io.SimpleFileFilter;

/**
 * MSMExplorer I/O basic functions, including open and save
 * operations. All methods should be static.
 *
 * @author brycecr
 */
public class MSMIOLib {

	private static final String DEFAULT_DIRECTORY = "~/Documents";

	private MSMIOLib() {
	}

	; // no instantiation

	public static Graph getMSMFile(Component c) {
		return getMSMFile(c, DEFAULT_DIRECTORY);
	}

	public static Graph getMSMFile(Component c, String path) {
		JFileChooser jfc = new JFileChooser(path);
		jfc.setDialogType(JFileChooser.OPEN_DIALOG);
		jfc.setDialogTitle("Open GraphML or MSM file");

		SimpleFileFilter ff;

		ff = new SimpleFileFilter("xml",
			"GraphML File (*.xml, *.graphml)",
			new GraphMLReader());
		ff.addExtension("graphml");
		ff.addExtension("gz");
		jfc.setFileFilter(ff);

		ff = new SimpleFileFilter("dat",
			"DAT dense matrix (*.dat, *.txt)",
			new DatGraphReader());
		ff.addExtension("txt");
		jfc.setFileFilter(ff);

		ff = new SimpleFileFilter("mtx",
			"MTX Sparse Format (*.mtx)",
			new MtxGraphReader());
		jfc.setFileFilter(ff);

		int opt = jfc.showOpenDialog(c);
		if (opt != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		File f = jfc.getSelectedFile();
		FileFilter selectedFF = (FileFilter) jfc.getFileFilter();

		//Assume file is dat unless a specific file filter is selected
		GraphReader gr = new DatGraphReader();
		if (!selectedFF.equals(jfc.getAcceptAllFileFilter())) {
			ff = (SimpleFileFilter) selectedFF;
			gr = (GraphReader) ff.getUserData();
		}

		try {
			Graph g = gr.readGraph(IOLib.streamFromString(f.getAbsolutePath()));
			if (!GraphMLReader.class.isAssignableFrom(gr.getClass())) {
				g = EQProbReader.getEqProbs(null, g);
			}
			return g;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(c, "Attempt to open MSM "
				+ "at " + f.getName() + " failed: file not "
				+ "found or not in the expected format. "
				+ "Exception: " + e.toString(), 
				"MSM Read Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	public static Graph[] openMSMHierarchy(Component c) {
		return openMSMHierarchy(c, DEFAULT_DIRECTORY);
	}

	public static Graph[] openMSMHierarchy(Component c, String path) {
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
			return new Graph[0];
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

				// Set mapping for this node to the next
				if (i == newNode.length - 1) {
					System.err.println("num states is " + graphs[i].getNodeCount());
					Table nt = graphs[i].getNodeTable();
					nt.addColumn("mapping", int.class);
					Object[] mappings = NewlineDelimitedReader.read(newNode[i - 1].mmapFilename);
					for (int j = 0; j < mappings.length; ++j) {
						graphs[i].getNode(j).setInt("mapping", (Integer)mappings[j]);
					}
				}
			} catch (Exception e) {
				Logger.getLogger(MSMIOLib.class.getName()).log(
					Level.WARNING, "{0}\n{1}",
					new Object[]{e.getMessage(), StringLib.getStackTrace(e)});
				return null;
			}
		}
		return graphs;
	}

	public static Dictionary<Integer, JLabel> getHierarchyLabels(Graph[] gs) {
		Dictionary<Integer, JLabel> dict = new Hashtable<Integer, JLabel>(gs.length);
		for (int i = 0; i < gs.length; ++i) {
			dict.put(i, new JLabel(Integer.toString(gs[i].getNodeCount())));
		}
		return dict;
	}

	public static String saveGML(Graph g) {
		return saveGML(g, DEFAULT_DIRECTORY, null);
	}

	public static String saveGML(Graph g, Component c) {
		return saveGML(g, DEFAULT_DIRECTORY, c);
	}

	/**
	 * Saves the current graph as a GraphML file
	 *
	 * @param g graph to save
	 * @param path location to save file
	 * @param c swing component to display notifications to
	 */
	public static String saveGML(Graph g, String path, Component c) {
		JFileChooser jfc = new JFileChooser(path);
		jfc.setDialogType(JFileChooser.SAVE_DIALOG);
		jfc.setDialogTitle("Save MSM as GraphML");

		SimpleFileFilter ff;

		ff = new SimpleFileFilter("graphml",
			"GraphML File(*.xml, *.graphml)",
			new GraphMLReader());

		jfc.setFileFilter(ff);

		int opt = jfc.showSaveDialog(c);
		if (opt != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		String loc = jfc.getSelectedFile().getAbsolutePath();
		int pos = -1;
		if (!(loc.matches(".*\056xml") || loc.matches(".*\056graphml"))) {
			loc += ".graphml";
		}

		GraphMLWriter gmlwr = new GraphMLWriter();
		try {
			gmlwr.writeGraph(g, loc);
		} catch (DataIOException ex) {
			Logger.getLogger(MSMIOLib.class.getName()).log(Level.SEVERE, null, ex);
		}

		return loc;

	}
}
