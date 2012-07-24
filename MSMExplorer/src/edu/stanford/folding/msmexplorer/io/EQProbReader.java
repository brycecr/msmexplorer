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

	public static Graph getEqProbs(Component c, Graph g) {
		Object[] opts = {"Locate", "Not now"};
		int opt = JOptionPane.showOptionDialog(c, 
			"Would you like to locate an equilibrium probabilities file? "
			+ '\n' + "This is recommended for more informative visualization.", 
			"Locate Equilibrium Probabilities", 
			JOptionPane.YES_NO_OPTION, 
			JOptionPane.WARNING_MESSAGE, null, opts, opts[0]);

		if (opt == JOptionPane.NO_OPTION)
			return g;
		
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogType(JFileChooser.OPEN_DIALOG);
		jfc.setDialogTitle("Select Equilibrium Probabilities File");

		SimpleFileFilter ff = new SimpleFileFilter("dat", 
			"DAT EqProbs File");

		int retval = jfc.showOpenDialog(c);
		if (retval != JFileChooser.APPROVE_OPTION)
			return g;

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
		Object[] probs = NewlineDelimitedReader.read(f);
		Table nt = g.getNodeTable();

		int ind = 1;
		if ((ind = nt.getColumnNumber("eqProb")) == -1) {
			nt.addColumn(EQPROB, double.class, 1);	
		}

		for (int row = 0; row < probs.length; ++row) {
			nt.setDouble(row, ind, (Double)probs[row]);
		}

		return g;
	}
}
