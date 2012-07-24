package edu.stanford.folding.msmexplorer.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.InputMismatchException;
import javax.swing.JOptionPane;

/**
 * A class to read generic newline delimited files.
 * Should be useful for reading in axes labels, mapping
 * files, other simple one-to-one data files.
 *
 * @author brycecr
 */
public class NewlineDelimitedReader {
	
	private NewlineDelimitedReader() {
		// disallow instantiation
	}
	
	/**
	 * Read the newline delimited file at location
	 * filePath. Returns the file contents in an object
	 * array. Determines what kind of data is in the file
	 * (currently supports ints, floats/doubles, and
	 * Strings (i.e. other)) by looking at the first
	 * token in the file and assuming the entire file
	 * is composed of tokens of that type.
	 * Failures generate dialog warnings that (sort of)
	 * attempt to explain the failure.
	 *
	 * @author brycecr
	 * @param filePath string location of where to find file to open
	 * @return the contents in the file as a file array
	 */
	public static Object[] read (String filePath) {
		try {
			File f = new File(filePath);
			return read (f);
		} catch (NullPointerException npe) {
			JOptionPane.showMessageDialog(null, "Null pathname for newline delimited "
				+ "reader.");
			return null;
		}
	}
	
	/**
	 * Read the newline delimited file f.
	 * Returns the file contents in an object
	 * array. Determines what kind of data is in the file
	 * (currently supports ints, floats/doubles, and
	 * Strings (i.e. other)) by looking at the first
	 * token in the file and assuming the entire file
	 * is composed of tokens of that type.
	 * Failures generate dialog warnings that (sort of)
	 * attempt to explain the failure.
	 *
	 * @author brycecr
	 * @param the file to open.
	 * @return the contents in the file as a file array
	 */
	public static Object[] read (File f) {
		Scanner scn = null;
		ArrayList lines = null;
		String filePath = f.getAbsolutePath();
		
		try {
			scn = new Scanner (f);
			
			// There probably is a slicker way to do this
			// but I don't think you can beat the nominal
			// programatic effeciency here, at least for
			// limited type support.
			if (scn.hasNextInt()) {
				lines = new ArrayList<Integer>();
				while (scn.hasNextInt()) {
					lines.add(scn.nextInt());
				}
			} else if (scn.hasNextDouble()) {
				lines = new ArrayList<Double>();
				while (scn.hasNextDouble()) {
					lines.add(scn.nextDouble());
				}
			} else if (scn.hasNextLine()) {
				lines = new ArrayList<String>();
				while (scn.hasNextLine()) {
					lines.add(scn.nextLine());
				}
			}
			
			return lines.toArray();
		} catch (FileNotFoundException fnfe) {
			JOptionPane.showMessageDialog(null, "Something slipped out from under us. "
				+ " Could not open file at " + filePath);
			return null;
		} catch (InputMismatchException ime) {
			JOptionPane.showMessageDialog(null, "File at " + filePath + " read failure. Possible mix of"
				+ "data types in file; file must be all one data type.");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Generic read failure on " + filePath);
		}
		return null;
	}
}
