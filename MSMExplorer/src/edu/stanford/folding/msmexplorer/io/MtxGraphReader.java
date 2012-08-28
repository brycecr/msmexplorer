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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.io.DataIOException;

/**
 * Read in a .mtx sparse matrix format graph.
 * This format is the primary input format for most
 * MSMs as of MSMBuilder v2.0
 *
 * @author brycecr
 */
public class MtxGraphReader extends AbstractMSMReader {

	private void init(int nodes, int edges) {

		m_nodeTable = new Table(nodes, 2);

		//There's a tradeoff here between String (relabelable)
		//and int type (adjustable axis bounds). Relabable is pretty nice...
		m_nodeTable.addColumn(LABEL, String.class, "0");
		m_nodeTable.addColumn(EQPROB, double.class, 1);	

		//initialize labels as row number, 1-indexed as per the .mtx convention.
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
