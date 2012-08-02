package edu.stanford.folding.msmexplorer.io;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.io.DataIOException;

/**
 *
 * @author gestalt
 */
public class MtxGraphReader extends AbstractMSMReader {

	private void init(int nodes, int edges) {
		m_nodeTable = new Table(nodes, 2);
		m_nodeTable.addColumn(LABEL, String.class, "0");
		m_nodeTable.addColumn(EQPROB, double.class, 1);	
		for (int i = 0; i < nodes;) {
			m_nodeTable.setString(i, 0, Integer.toString(++i));
		}

		m_edgeTable = new Table(edges, 3);
		m_edgeTable.addColumn(Graph.DEFAULT_SOURCE_KEY, int.class); //0th
		m_edgeTable.addColumn(Graph.DEFAULT_TARGET_KEY, int.class); //1st
		m_edgeTable.addColumn(TPROB, double.class); //2nd
	}

	private boolean handleOverviewLine(String line) {
		String[] splits = line.split(" ");
		try {
			init(Integer.parseInt(splits[0]), Integer.parseInt(splits[2]));
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public Graph readGraph(InputStream is) throws DataIOException {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = null;
			while ((line = br.readLine()) != null && !line.isEmpty() && line.charAt(0) == '%') {
			}
			if (line == null || line.isEmpty()) {
				throw new DataIOException("Unexpected empty line or End of File. "
					+ "Terminating read.");
			}

			boolean success = handleOverviewLine(line);
			if (!success) {
				throw new DataIOException("Integer Parse Failure On: " + line);
			}

			int edgeNum = 0;
			while ((line = br.readLine()) != null) {
				if (edgeNum >= m_edgeTable.getRowCount())
					throw new DataIOException("More edges than specified");
				String[] splits = line.split(" ");
				try {
					m_edgeTable.set(edgeNum, 0, Integer.parseInt(splits[0])-1);
					m_edgeTable.set(edgeNum, 1, Integer.parseInt(splits[1])-1);
					m_edgeTable.set(edgeNum, 2, Double.parseDouble(splits[2]));
					edgeNum++;
				} catch (NumberFormatException nfe) {
					throw new DataIOException("Parse failure on: " + line, nfe);
				}
			}
			return new Graph(m_nodeTable, m_edgeTable, true);
		} catch (IOException ioex) {
			throw new DataIOException("BufferedReader/InputStreamReader failure in MtxGraphReader", ioex);
		}
	}
}
