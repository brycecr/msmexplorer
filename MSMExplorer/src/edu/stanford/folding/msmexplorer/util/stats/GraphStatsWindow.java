/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
