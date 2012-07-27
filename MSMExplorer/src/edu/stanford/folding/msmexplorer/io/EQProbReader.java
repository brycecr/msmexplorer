package edu.stanford.folding.msmexplorer.io;

import edu.stanford.folding.msmexplorer.MSMConstants;
import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.util.io.SimpleFileFilter;

public class EQProbReader implements MSMConstants {

	private EQProbReader() {
		//prevent instantiation
	}

	/**
	 * This version of getEqProbs attempts to automatically find an
	 * equilibrium probabilities file for the graph g by looking for
	 * likely equilibrium probabilities filenames in the directory pointed to
	 * by path. All else created equal, we recommend titling your eqProb
	 * files "Populations.dat" as per the MSMBuilder 2 default.
	 * Prompts the user to make sure that the auto-found should be
	 * added to the graph, and will ask the user once for each likely
	 * file found in the directory at path.
	 * If no files are automatically found, calls getEqProbs(Component, Graph)
	 * to ask for the eqProb file from the user.
	 * 
	 * @param c parent component
	 * @param g graph to add eqProbs to
	 * @param path string denoting directory to search for eqProb files
	 * @return on success, the graph g with equilibrium probabiliites,
	 * on failure, the original unmodified graph g
	 */
	public static Graph getEqProbs(Component c, Graph g, String path) {
		File topList[];

		if (path == null) {
			return getEqProbs(c, g);
		}
		try { 
			File top = new File (path);
			topList = top.listFiles();
		} catch (Exception e) {
			return getEqProbs(c, g);
		}

		for (File f : topList) {
			String name = f.getName();
			if (name.startsWith("Population") ||
				name.startsWith("eqProb") ||
				name.startsWith("eqprob") ||
				name.startsWith("EQPROB") ||
				name.startsWith("EqProb")) {
				int ret = JOptionPane.showConfirmDialog(c, "Found file " + name 
					+ "\nShould MSMExplorer add the contents of this file"
					+ "\nas the Equilibruim Probabilities for the graph"
					+ " being opened?", "Use Auto-found EqProbs?", 
					JOptionPane.YES_NO_OPTION);
				if (ret == JOptionPane.YES_OPTION) {
					return addEqProbs(g, f);
				}
			}
		}
		return getEqProbs(c, g);
	}
	
	/**
	 * Prompt the user whether an eqprob file is desired, and, if so,
	 * present user with an open dialog to select the equilibrium  probabilities
	 * file.
	 * 
	 * @param c parent component (or null)
	 * @param g graph to add eqprobs to
	 * @return on success, g with added eqprobs; on failure, original
	 * graph g
	 */
	public static Graph getEqProbs(Component c, Graph g) {
		Object[] opts = {"Locate", "Not now"};
		int opt = JOptionPane.showOptionDialog(c, 
			"Would you like to locate an equilibrium probabilities file? "
			+ '\n' + "This is recommended for more informative visualization.", 
			"Locate Equilibrium Probabilities", 
			JOptionPane.YES_NO_OPTION, 
			JOptionPane.WARNING_MESSAGE, null, opts, opts[0]);

		if (opt == JOptionPane.NO_OPTION) {
			return g;
		}
		
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogType(JFileChooser.OPEN_DIALOG);
		jfc.setDialogTitle("Select Equilibrium Probabilities File");

		SimpleFileFilter ff = new SimpleFileFilter("dat", 
			"DAT EqProbs File");

		int retval = jfc.showOpenDialog(c);
		if (retval != JFileChooser.APPROVE_OPTION) {
			return g;
		}

		File f = jfc.getSelectedFile();
		g = addEqProbs(g, f);

		return g;
	}

	/**
	 * Processes data from a newline-delimited file and insets the information
	 * into the eqProb column that should already be in the graph.
	 * 
	 * @param g
	 * @param f
	 * @return
	 */
	public static Graph addEqProbs(Graph g, File f) {
		if (g == null || f == null) {
			return null;
		}
		
		Object[] probs = NewlineDelimitedReader.read(f);
		Table nt = g.getNodeTable();

		if (nt == null || probs == null) {
			return null;
		}

		if (nt.getRowCount() != probs.length) {
			JOptionPane.showMessageDialog(null, "Equilibrium Probabilities file is"
				+ " different length than number of nodes in graph.", 
				"EqProb Read Error",
				JOptionPane.ERROR_MESSAGE);
			return g;
		}

		int ind;
		if ((ind = nt.getColumnNumber("eqProb")) == -1) {
			nt.addColumn(EQPROB, double.class, 1);	
			ind = nt.getColumnNumber("eqProb");
		}

		for (int row = 0; row < probs.length; ++row) {
			nt.setDouble(row, ind, (Double)probs[row]);
		}

		return g;
	}
}
