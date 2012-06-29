package edu.stanford.folding.msmexplorer.io;

import java.io.InputStream;

import prefuse.data.Graph;
import prefuse.data.io.DataIOException;

/**
 * Mostly a convenience hack. Should probably be phased out in favor
 * of a more sensible object hierarchy for the MSM readers.
 */
public class DefaultMSMReader extends AbstractMSMReader {

	public Graph readGraph (InputStream is) throws DataIOException {return new Graph();}
	public Graph readGraph (InputStream is, String eqProbLocation) throws DataIOException {return new Graph();}

}