package edu.stanford.folding.msmexplorer.io;

import edu.stanford.folding.msmexplorer.MSMConstants;
import prefuse.data.Table;
import prefuse.data.io.AbstractGraphReader;

/**
 *
 * @author gestalt
 */
public abstract class AbstractMSMReader extends AbstractGraphReader implements MSMConstants {

	protected Table m_nodeTable;
	protected Table m_edgeTable;

	
}
