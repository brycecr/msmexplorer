package edu.stanford.folding.msmexplorer.util.stats;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * Class to contain graph statistics calculations and provide an interface
 * for retrieval of all statistics in various formats for use in other parts 
 * of the program.
 * 
 * @author brycecr
 */
public class GraphStatsManager {

	private static final double BAD_INPUT = -1.;
	private final Graph graph;
	private final int numNodes;
	private final int numEdges;
	private final double avgDegree;
	private final double density;
	private final boolean directed;
	private final double evCentrality;

	/**
	 * Upon construction, all statistics are carried out and stored.
	 * 
	 * @param graph to calculate stats over
	 */
	public GraphStatsManager(Graph g) {
		graph = g;
		numNodes = g.getNodeCount();
		numEdges = g.getEdgeCount();
		directed = g.isDirected();
		avgDegree = pCalcAvgDegree();
		density = pCalcDensity();
		evCentrality = eigenvectorCentrality();
	}

	/**
	 * Calculate average degree. Adjusts for undirected/directed graphs.
	 * Private version uses predetermined values.
	 *
	 * @param graph g to calculate over
	 * @return double average degree
	 */
	private double pCalcAvgDegree() {
		return 2 * (double)numEdges / numNodes;
	}


	/**
	 * Calculate density. Adjusts for undirected/directed graphs.
	 * Private version uses predetermined values.
	 * Note that this counts self-edges (and thus can be > 1.0)
	 *
	 * @param graph g to calculate over
	 * @return double density
	 */
	private double pCalcDensity() {
		return ((directed) ? 1. : 2.) * numEdges / ((double)numNodes * (numNodes - 1));
	}

	/**
	 * Calculate average degree. Adjusts for undirected/directed graphs.
	 *
	 *
	 * @param graph g to calculate over
	 * @return double average degree
	 */
	public static double calcAvgDegree(Graph g) {
		if (g.getNodeCount() <= 0) //Avoid Division by 0
			return BAD_INPUT;
		return 2. * g.getEdgeCount() / (double)g.getNodeCount();
	}

	/**
	 * Calculate graph density. Adjusts for undirected/directed graphs.
	 *
	 *
	 * @param graph g to calculate over
	 * @return double density
	 */
	public static double calcDensity(Graph g) {
		if (g.getNodeCount() <= 1) //Avoid division by 0
			return BAD_INPUT;

		return ((g.isDirected()) ? 1. : 2.) * g.getEdgeCount() / ((double)g.getNodeCount() * (g.getNodeCount() - 1));
	}

	/**
	 * Returns a formatted representation of the statistics
	 * contained in this class instance.
	 * 
	 * @return string representation of the manager's statistics 
	 */
	public String printStats() {
		String stats = "Nodes: " + numNodes + '\n'
			+ "Edges: " + numEdges + '\n'
			+ "Density: " + density + '\n'
			+ "Avg Deg: " + avgDegree + '\n'
			+ "Avg In/Out Deg" + avgDegree / 2. + '\n'
			+ "Eigenvector Centrality: " + evCentrality;
		System.out.println(stats);

		return stats;
	}

	/**
	 * Returns a 2D array: the first column is labels for the
	 * (numeric) values in the second column. Format is intended
	 * for feeding to a JTable, but could serve other uses as well.
	 * 
	 * @return formatted array of statistics contained in the manager 
	 * and their corresponding labels
	 */
	public Object[][] getStatsArray() {
		Object[][] arr = {{"Nodes", numNodes},
			{"Edges", numEdges},
			{"Density", density},
			{"Average Degree", avgDegree},
			{"Average In/Out Degree", avgDegree / 2.},
			{"Eigenvector Centrality", evCentrality}};

		return arr;
	}

	/**
	 * Returns eigenvector centrality calculation for the manager's graph.
	 * Form adapted from a similar Gephi function.
	 * 
	 * @return highest centrality score in the graph
	 */
	private double eigenvectorCentrality() {

		int numRuns = 100;
		int N = graph.getNodeCount();

		double[] tmp = new double[N];
		double[] centralities = new double[N];

		HashMap<Integer, Node> indicies = new HashMap<Integer, Node>();
		HashMap<Node, Integer> invIndicies = new HashMap<Node, Integer>();
		int count = 0;
		Iterator nodeItr = graph.nodes();
		while (nodeItr.hasNext()) {
			Node u = (Node)nodeItr.next();
			indicies.put(count, u);
			invIndicies.put(u, count);
			centralities[count] = 1;
			count++;
		}
		for (int s = 0; s < numRuns; s++) {
			double max = 0;
			for (int i = 0; i < N; i++) {
				Node u = indicies.get(i);
				Iterator iter = graph.inEdges(u); 


				while (iter.hasNext())	{
					Edge e = (Edge)iter.next();
					Node v = e.getTargetNode();
					Integer id = invIndicies.get(v);
					tmp[i] += centralities[id];
				}
				max = Math.max(max, tmp[i]);
			}
			double sumChange = 0;
			for (int k = 0; k < N; k++) {
				if (max != 0) {
					sumChange += Math.abs(centralities[k] - (tmp[k] / max));
					centralities[k] = tmp[k] / max;
					//tmp[k] = 0;
				}
			}
		}

		//System.out.println(Arrays.toString(centralities));
		//System.out.println("Max: " + max(centralities));
		return max(centralities);
	}

	/**
	 * Convenience function to return max value of a double array.
	 * 
	 * @return max double value
	 */
	private static double max(double[] c) {
		double max = 0.;
		for (double d : c)
			if (d > max)
				max = d;
		return max;

	}
}
