/*
 * Copyright (C) 2012 Pande Lab
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
package edu.stanford.folding.msmexplorer.util.stats;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import prefuse.data.Graph;

/**
 * JFrame that holds a private GraphStatsManager instance & puts that GSM's
 * output into a cute swing table.
 *
 * @author brycecr
 */
public class GraphStatsWindow extends JFrame {

	private final GraphStatsManager gsm;
	private final JTable table;
	private final String[] columns = {"Statistic", "Value"};

	/**
	 * Create a GraphStatsWindow instance. On construction,
	 * creates private GraphStatsManager instance and puts all the
	 * requested calculated material into table in the JFrame.
	 *
	 * @param graph to calculate statistics over
	 */
	public GraphStatsWindow(Graph g) {
		gsm = new GraphStatsManager(g);
		table = new JTable(gsm.getStatsArray(), columns);
		table.setFillsViewportHeight(true);
		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		//Add the scroll pane to this panel.
		add(scrollPane);
		setTitle("Graph Statistics");
		setSize(300, 140);
	}
}
