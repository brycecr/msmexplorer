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
import java.io.InputStream;
import java.io.InputStreamReader;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.io.DataIOException;
import prefuse.data.parser.DataParseException;
import prefuse.data.parser.DoubleArrayParser;



/**
 * Read a graph from a dense space delimited matrix file (usually .dat)
 * Should mostly be phased out as of MSMBuilder 2.0, but still fully
 * supported here.
 *
 * @author brycecr
 */
public class DatGraphReader extends AbstractMSMReader {

	/**
	 * Prepare backing graph tables for reading.
	 * 
	 * @param length 
	 */
	private void init(int length) {

		m_nodeTable = new Table(length, 2);

		//There's a tradeoff here between String (relabelable)
		//and int type (adjustable axis bounds). Relabable is pretty nice...
		m_nodeTable.addColumn(LABEL, String.class, "0"); 
		m_nodeTable.addColumn(EQPROB, double.class, 1);	

		//Initialize axis labels to correspond to row numbers, 0-indexed
		for (int i = 0; i < length; ++i) {
			m_nodeTable.setString(i, 0, Integer.toString(i));
		}

		m_edgeTable = new Table();
		m_edgeTable.addColumn(Graph.DEFAULT_SOURCE_KEY, int.class); //0th
		m_edgeTable.addColumn(Graph.DEFAULT_TARGET_KEY, int.class); //1st
		m_edgeTable.addColumn(TPROB, double.class); //2nd
	}
	
	/**
	 * Read a dense matrix in from an InputStream
	 * 
	 * @param is
	 * @return the graph read in, or an empty new graph on failure.
	 * @throws DataIOException 
	 */
	public Graph readGraph(InputStream is) throws DataIOException {

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
			return new Graph(m_nodeTable, m_edgeTable, true);
		} catch (Exception ex) {
			throw new DataIOException("Buffered Reader Failure", ex);
		}
	}
}
