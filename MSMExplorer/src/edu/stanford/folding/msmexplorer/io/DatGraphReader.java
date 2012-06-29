package edu.stanford.folding.msmexplorer.io;

import javax.swing.JOptionPane;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.io.DataIOException;
import prefuse.data.parser.DataParseException;
import prefuse.data.parser.DoubleArrayParser;



/**
 *
 * @author gestalt
 */
public class DatGraphReader extends DefaultMSMReader {

	private void init(int length) {
		m_nodeTable = new Table(length, 2);
		m_nodeTable.addColumn(LABEL, int.class, 0);
		m_nodeTable.addColumn(EQPROB, double.class, 1);
		for (int i = 1; i < length; ++i)
			m_nodeTable.setInt(i, 0, i);

		m_edgeTable = new Table();
		m_edgeTable.addColumn(Graph.DEFAULT_SOURCE_KEY, int.class); //0th
		m_edgeTable.addColumn(Graph.DEFAULT_TARGET_KEY, int.class); //1st
		m_edgeTable.addColumn(TPROB, double.class); //2nd
	}
	
	public Graph readGraph(InputStream is, String eqProbPath) throws DataIOException {

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			DoubleArrayParser dap = new DoubleArrayParser();

			String line = null;
			int edgeId = 0;
			int node = 0;
			while ((line = br.readLine()) != null && dap.canParse(line)) {
					try {
						double[] d = (double[]) dap.parse(line);
						if (node == 0)
							init(d.length);

						for (int target = 0; target < d.length; ++target) {
							if (d[target] == 0.) 
								continue;

							m_edgeTable.addRow();
							m_edgeTable.set(edgeId, 0, node);
							m_edgeTable.set(edgeId, 1, target);
							m_edgeTable.set(edgeId, 2, d[target]);
							edgeId++;
						}

					} catch (DataParseException dioe) {
						throw new DataIOException("Double Parse Failure", dioe);
					}
				node++;
			}
			Graph g =  new Graph(m_nodeTable, m_edgeTable, true);

			if (eqProbPath == null) {
				return getEqProbs(null, g);
			} else {
				try {
					return addEqProbs(g, new File(eqProbPath));
				} catch (DataIOException dioe) {
					JOptionPane.showMessageDialog(null, "problem reading eqprobs file at " + eqProbPath);
				}
			}

		} catch (Exception ex) {
			throw new DataIOException("Buffered Reader Failure", ex);
		}
		return null;
	}

	public Graph readGraph(InputStream is) throws DataIOException {
		return readGraph(is, null);
	}

		
}
