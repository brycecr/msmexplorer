package edu.stanford.folding.msmexplorer.io;

import java.awt.Component;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.data.io.GraphMLWriter;
import prefuse.data.io.GraphReader;
import prefuse.util.StringLib;
import prefuse.util.io.IOLib;
import prefuse.util.io.SimpleFileFilter;

/**
 * MSMExplorer I/O basic functions, including open and save
 * operations.
 *
 * @author brycecr
 */
public class MSMIOLib {

	private static final String DEFAULT_DIRECTORY = "~/Documents";

	private MSMIOLib(){}; // no instantiation

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
			"DAT simple format (*.dat, *.txt)",
			new DatGraphReader());
		ff.addExtension("txt");
		jfc.setFileFilter(ff);

		ff = new SimpleFileFilter("mtx",
			"MTX Sparse Format (*.mtx)",
			new MtxGraphReader());
		jfc.setFileFilter(ff);
		
		int opt = jfc.showOpenDialog(c);
		if (opt != JFileChooser.APPROVE_OPTION)
			return null;

		File f = jfc.getSelectedFile();
		ff = (SimpleFileFilter)(FileFilter)jfc.getFileFilter();
		GraphReader gr = (GraphReader)ff.getUserData();

		try {
			return gr.readGraph(IOLib.streamFromString(f.getAbsolutePath()));
		} catch ( Exception e ) {
			Logger.getLogger(MSMIOLib.class.getName()).log(
				Level.WARNING, "{0}\n{1}", 
				new Object[]{e.getMessage(),StringLib.getStackTrace(e)});
			return null;
		}
	}

	public static String saveGML (Graph g) {
		return saveGML(g, DEFAULT_DIRECTORY, null);
	}

	public static String saveGML (Graph g, Component c) {
		return saveGML(g, DEFAULT_DIRECTORY, c);
	}

	/**
	 * Saves the current graph as a GraphML file
	 *
	 * @param g graph to save
	 * @param path location to save file
	 * @param c swing component to display notifications to
	 */
	public static String saveGML (Graph g, String path, Component c) {
		JFileChooser jfc = new JFileChooser(path);
		jfc.setDialogType(JFileChooser.SAVE_DIALOG);
		jfc.setDialogTitle("Save MSM as GraphML");

		SimpleFileFilter ff;

		ff = new SimpleFileFilter("graphml",
			"GraphML File(*.xml, *.graphml)",
			new GraphMLReader());

		jfc.setFileFilter(ff);

		int opt = jfc.showSaveDialog(c);
		if (opt != JFileChooser.APPROVE_OPTION)
			return null;

		String loc = jfc.getSelectedFile().getAbsolutePath();
		int pos = -1;
		if (!(loc.matches(".*\056xml") || loc.matches(".*\056graphml")))
			loc += ".graphml";

		GraphMLWriter gmlwr = new GraphMLWriter(); 
		try {
			gmlwr.writeGraph(g, loc);
		} catch (DataIOException ex) {
			Logger.getLogger(MSMIOLib.class.getName()).log(Level.SEVERE, null, ex);
		}

		return loc;
		
	}
}
