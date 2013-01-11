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
package edu.stanford.folding.msmexplorer.io;

import edu.stanford.folding.msmexplorer.io.hierarchy.HierarchyBundle;
import edu.stanford.folding.msmexplorer.io.hierarchy.HierarchyIOLib;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.DataIOException;
import prefuse.data.io.DelimitedTextTableReader;
import prefuse.data.io.GraphMLReader;
import prefuse.data.io.GraphMLWriter;
import prefuse.data.io.GraphReader;
import prefuse.data.io.TableReader;
import prefuse.data.tuple.TupleSet;
import prefuse.util.io.IOLib;
import prefuse.util.io.SimpleFileFilter;

/**
 * MSMExplorer I/O basic functions, including open and save
 * operations. All methods should be static.
 *
 * @author brycecr
 */
public class MSMIOLib {

	private static final String DEFAULT_DIRECTORY = "";

	/** It's kinda funky that this is static...but it's cool I guess */
	private static final JFileChooser jfc = new JFileChooser();


	private MSMIOLib() {
		// disallow instantiation
	}

	/**
	 * Open a newline delimited file and attempt to add the contents
	 * of that file to the graph g into a new column of name.
	 * 
	 * @param g graph to add values to
	 * @param name name to give the new column
	 * @return string pathname for the folder where the file was opened
	 */
	public static String applyNewlineDelimitedFile(TupleSet g, String name, Class<?> cls) {
		return applyNewlineDelimitedFile(null, DEFAULT_DIRECTORY, g, name, cls);
	}

	/**
	 * Open a newline delimited file and attempt to add the contents
	 * of that file to the graph g.
	 * 
	 * @param c parent component
	 * @param g graph to add values to
	 * @param name name to give the new column
	 * @param cls type of new column
	 * @return string pathname for the folder where the file was opened
	 */
	public static String applyNewlineDelimitedFile(Component c, TupleSet g, String name, Class<?> cls) {
		return applyNewlineDelimitedFile(c, DEFAULT_DIRECTORY, g, name, cls);
	}
	
	/**
	 * Open a newline delimited file and attempt to add the contents
	 * of that file to the graph g.
	 * 
	 * @param c parent component
	 * @param path pathname at which to open the JFileChooser
	 * @param g graph to add values to
	 * @param name name to give the new column
	 * @return string pathname for the folder where the file was opened
	 */
	public static String applyNewlineDelimitedFile(Component c, String path, TupleSet g, String name) {
		return applyNewlineDelimitedFile(c, path, g, name, null);
	}

	/**
	 * Open a newline delimited file and attempt to add the contents
	 * of that file to the graph g into a column of name name and type cls.
	 * 
	 * @param c parent component
	 * @param path pathname at which to open the JFileChooser
	 * @param g graph to add values to
	 * @param name name to give the new column
	 * @param cls type of new column
	 * @return string pathname for the folder where the file was opened
	 */
	public static String applyNewlineDelimitedFile(Component c, String path, TupleSet g, String name, Class<?> cls) {

		if (!checkApplyFileParams(c, g, name)) {
			return null;
		}

		ArrayList<FileFilter> filters = new ArrayList<FileFilter>();
		SimpleFileFilter ff1 = new SimpleFileFilter("dat",
			"newline delim file (*.dat, *.txt)",
			null);
		ff1.addExtension("txt");
		filters.add(ff1);
		
		File f = getFileFromUser(c, path, "Open newline delimited file...", filters);
		if (f == null) {
			return null;
		}
		Object[] contents = NewlineDelimitedReader.read(f);

		// if the type is recognized and specified, make a column
		// of that type
		if (((Tuple)g.tuples().next()).getColumnIndex(name) < 0) {
			if (cls == String.class || cls == int.class || cls == double.class
				|| cls == float.class || cls == long.class || cls == boolean.class
				|| cls == Date.class) {
				g.addColumn(name, cls);
				
			} else { // else, make a general object column
				g.addColumn(name, Object.class);
			}
		}

		if (g.getTupleCount() != contents.length) {
			JOptionPane.showMessageDialog(c, "Length of newline delmited file "
				+ "differs from number of nodes in graph; ambiguity in assigning"
				+ " values to nodes.", "Newline File Length Error", 
				JOptionPane.ERROR_MESSAGE);
			return null;
		}

		Iterator<Tuple> itr = g.tuples();
		while (itr.hasNext()) {
			Tuple tup = itr.next();
			tup.set(name, contents[tup.getRow()]);
		}
		return f.getParent();
	}

	public static String applyMatrixFile(Component c, String path, TupleSet g, String name, Class<?> cls) {


		if (!checkApplyFileParams(c, g, name)) {
			return null;
		}

		ArrayList<FileFilter> filters = new ArrayList<FileFilter>();
		SimpleFileFilter ff1 = new SimpleFileFilter("dat",
			"DAT dense matrix (*.dat, *.txt)",
			new DatGraphReader());
		ff1.addExtension("txt");

		SimpleFileFilter ff2 = new SimpleFileFilter("mtx",
			"MTX Sparse Format (*.mtx)",
			new MtxGraphReader());
		filters.add(ff1);
		filters.add(ff2);

		File f = getFileFromUser(c, path, "Open matrix file...", filters);
		if (f == null) {
			return null;
		}
		String ext = getExtension(f);
		
		Table t = null;
		try {
			if (ext.equals("mtx")) {
				MtxTableReader tr = new MtxTableReader();
				t = tr.readTable(f);
				
			} else {
				DelimitedTextTableReader  dttr = new DelimitedTextTableReader(" ");
				dttr.setHasHeader(false);
				t = dttr.readTable(f);
			}
		} catch (Exception ex) {
			Logger.getLogger(MSMIOLib.class.getName()).log(Level.SEVERE, null, ex);
		}

		if (t == null) {
			return null;
		}

		try {
			g.addColumn(name, cls);
		} catch (Exception ex) {
			Logger.getLogger(MSMIOLib.class.getName()).log(Level.WARNING, null, ex);
		}

		int tRows = t.getRowCount();
		int tCols = t.getColumnCount();
		Object def = t.getColumn(0).getDefaultValue();

		Iterator<Tuple> itr = g.tuples();
		while (itr.hasNext()) {
			Tuple tup = itr.next();
			int row = tup.getInt("source");
			int col = tup.getInt("target");
			if (row >= tRows || col >= tCols) {
				tup.set(name, def);
			} else {
				tup.set(name, t.get(row, col));
			}
		}
		return name;
	}
	
	private static boolean checkApplyFileParams(Component c, TupleSet g, String name) {
		if (g == null) {
			JOptionPane.showMessageDialog(c, "Attempting to apply a newline"
				+ " delimited file to a null group.", "Newline Delimited IO Error",
				JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (name == null) {
			JOptionPane.showMessageDialog(c, "Please provide a name for the new data.", 
				"Nameless Data Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (!g.isAddColumnSupported()) {
			JOptionPane.showMessageDialog(c, "Cannot add a new column to this graph.", 
				"New Column Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private static File getFileFromUser(Component c, String path, String title, ArrayList<FileFilter> filters) {
		jfc.resetChoosableFileFilters();

		jfc.setDialogType(JFileChooser.OPEN_DIALOG);
		jfc.setDialogTitle(title);
		for (FileFilter ff : filters) {
			jfc.setFileFilter(ff);
		}

		int opt = jfc.showOpenDialog(c);
		if (opt != JFileChooser.APPROVE_OPTION) {
			return null; // no file selected and okayed
		}
		filters.clear();
		filters.add(jfc.getFileFilter());

		return jfc.getSelectedFile();
	}

	/**
	 * Opens a load dialog to get a new MSM. 
	 * Starts path at home directory.
	 * 
	 * @param c parent component (optional)
	 * @return the opened graph, or null if no new graph successfully opened
	 */
	public static Graph getMSMFile(Component c) {
		return getMSMFile(c, DEFAULT_DIRECTORY);
	}

	/**
	 * Opens a load dialog to get a new MSM. Starts the dialog at location
	 * specified by path. or the home directory if path doesn't 
	 * resolve to a directory.
	 * 
	 * @param c parent component (optional)
	 * @param path where to start load dialog
	 * @return the opened graph, or null if no new graph successfully opened
	 */
	public static Graph getMSMFile(Component c, String path) {
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
				g = EQProbReader.getEqProbs(null, g, f.getParent());
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
		String path = (jfc.getSelectedFile() != null) ? 
			jfc.getSelectedFile().getAbsolutePath() : DEFAULT_DIRECTORY;
		return openMSMHierarchy(c, path);
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
		return saveGML(null, DEFAULT_DIRECTORY, g);
	}

	/**
	 * Saves the current graph as a GraphML file via a GUI save dialog.
	 *
	 * @param g graph to save
	 * @param c swing component to display notifications to
	 * @return string of location at which file saved.
	 */
	public static String saveGML(Component c, Graph g) {
		return saveGML(c, DEFAULT_DIRECTORY, g);
	}

	/**
	 * Saves the current graph as a GraphML file via a GUI save dialog.
	 *
	 * @param g graph to save
	 * @param path where to start save dialog
	 * @param c swing component to display notifications to
	 * @return string of location at which file saved.
	 */
	public static String saveGML(Component c, String path, Graph g) {
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

	
	/**
	 * Method from velocityreview.com. Yes, I'm that lazy.
	 * @param f
	 * @return 
	 */
	private static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		
		if (i > 0 && i < s.length() - 2) {
			ext = s.substring(i+1).toLowerCase();
		}
		
		if(ext == null) {
			return "";
		}
		return ext;
	}
}
