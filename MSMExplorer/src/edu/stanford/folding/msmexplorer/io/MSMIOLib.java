package edu.stanford.folding.msmexplorer.io;

import edu.stanford.folding.msmexplorer.io.hierarchy.HierarchySketcher;
import edu.stanford.folding.msmexplorer.io.hierarchy.HierarchyBundle;
import edu.stanford.folding.msmexplorer.io.hierarchy.HierarchyIOLib;
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
	private static final String MAPPING = "mapping";

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

	public static HierarchyBundle openMSMHierarchy(Component c) {
		return openMSMHierarchy(c, DEFAULT_DIRECTORY);
	}

	public static HierarchyBundle openMSMHierarchy(Component c, String path) {
		return HierarchyIOLib.openMSMHierarchy(c, path);
	}

	public static Dictionary<Integer, JLabel> getHierarchyLabels(Graph[] gs) {
		return HierarchyIOLib.getHierarchyLabels(gs);
	}
	
	public static Dictionary<Integer, JLabel> getAltHierarchyLabels(Graph[] gs) {
		return HierarchyIOLib.getAltHierarchyLabels(gs);
	}

	public static boolean setMapping(HierarchyBundle hb, int bottom, int top) {
		return HierarchyIOLib.setMapping(hb, bottom, top);
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
		//XXX what is this??
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
