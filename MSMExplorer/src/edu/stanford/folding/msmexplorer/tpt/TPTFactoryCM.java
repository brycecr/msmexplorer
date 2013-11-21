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
package edu.stanford.folding.msmexplorer.tpt;

import edu.stanford.folding.msmexplorer.MSMExplorer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Comparator;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import prefuse.data.Graph;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;
import prefuse.data.column.Column;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.DefaultRealMatrixPreservingVisitor;


/**
 * Does the actual work of doing TPT calculations.
 * Uses the Apache Commons Math package.
 *
 * We store matricies as OpenMapRealMatrix objects because that's
 * the only instantiation of the SparseRealMatrix interface in the
 * commons math library currently. 
 *
 * I make no claim to the correctness or good performance of this
 * implementation, but it does seem to provide reasonable results.
 *
 * @author brycecr
 */
public class TPTFactoryCM {


	/**
	 * A simple pair class representing an edge by a pair
	 * of source, target indicies into the flux matrix.
	 *
	 * The instance vars are public because java syntax
	 * is annoying and long winded.
	 */
	private static class DijkstraEdge {
		public int row;
		public int col;

		public DijkstraEdge (int r, int c) {
			row = r;
			col = c;
		}

		public void setRow (int r) {
			row = r;
		}

		public void setCol (int c) {
			col = c;
		}

		public void set (int r, int c) {
			row = r;
			col = c;
		}
	}

	private static final int kSTROKE_INCREMENT = 2;

	private static final int kDEFAULT_PQUEUE_SIZE = 50;

	//getHighFluxPathV2 will give up after this many iterations
	private static final int kMAX_ITERS = 100000;

	private boolean error;
	private final Graph m_graph;               //graph
	private final TupleSet m_source;           //source node(s)
	private final TupleSet m_target;           //target node(s)
	private final OpenMapRealMatrix m_tProb;   //transition probability matrix
	private final int kNumKStates;             //number target states;
	private final int kMatSize;
	private OpenMapRealMatrix m_fFluxes;       //current flux matrix. Changes as paths are extracted 
	private OpenMapRealMatrix m_old_fFluxes;   //we keep this so we can reset fluxes

	/**
	 * Constructor for TPTFactry. Sets graph to use
	 * for this TPTFactory instance as well as source
	 * and target nodes/groups thereof.
	 *
	 * @param g, Graph to analyze
	 * @param source, Group of source Nodes
	 * @param target, Group of Target Nodes
	 */
	public TPTFactoryCM(Graph g, TupleSet source, TupleSet target) {

		this.m_graph = g;
		this.m_source = source;
		this.m_target = target;
		this.kMatSize = m_graph.getNodeCount();

		this.m_tProb = transitionMatrix();


		this.kNumKStates = target.getTupleCount();

		double[] forwardCommittors = getForwardCommittors();
		double[] backwardCommittors = getBackwardCommittors(forwardCommittors);
		double[] eqProbs = getEqProbs();
		eqProbs = normalize(eqProbs);

		this.m_old_fFluxes = getFluxes(forwardCommittors, backwardCommittors, eqProbs);
		this.m_fFluxes = deepCopy(m_old_fFluxes);

	}

	/**
	 * Extract an edge from the current m_fFluxes. That is, keeps the current
	 * state of the flux matrix so as to respect paths that have already
	 * been extracted. 
	 *
	 * @return an ArrayList of edges that represents the subset of the graph to draw.
	 */
	public ArrayList<Edge> getNextEdge() {
		return getHighFluxPathV3();
	}

	/**
	 * Set the flux matrix to the saved original copy.
	 */
	public void reset() {
		this.m_fFluxes = deepCopy(m_old_fFluxes);

	}

	private double[] normalize(double[] arr) {

		double sum = 0.0;

		for (int i = 0; i < arr.length; ++i)
			sum += arr[i];

		for (int i = 0; i < arr.length; ++i)
			arr[i] /= sum;

		return arr;
	}

	/**
	 * Creates a transitionMatrix from graph backing data.
	 * This should match the .dat file from which the graphml
	 * file was created (or equivalently, the corresponding
	 * tProb.dat for whatever input data was provided)
	 */
	private OpenMapRealMatrix transitionMatrix() {

		//        double[][] tProb = new double[kMatSize][kMatSize];
		OpenMapRealMatrix tProb = new OpenMapRealMatrix(kMatSize, kMatSize);

		for (int i = 0; i < m_graph.getEdgeCount(); ++i) {
			Edge e = m_graph.getEdge(i);
			int source = e.getSourceNode().getRow();
			int target = e.getTargetNode().getRow();

			Double prob = e.getDouble("probability");
			if (prob != 0.0)
				tProb.setEntry(source, target, prob);
		}

		return tProb;
	}

	/**
	 * Calculates forward committors and returns them in an array. Index into
	 * array corresponds to graph Node id and tProb row (ith state, standardly)
	 */
	private double[] getForwardCommittors() {

		ArrayList<Integer> source = getIndicies(m_source);
		ArrayList<Integer> target = getIndicies(m_target);

		//Copy of tProb in RealMatrix form
		OpenMapRealMatrix tProbRM = new OpenMapRealMatrix(m_tProb.copy());

		RealVector aug = new ArrayRealVector(kMatSize); //Holds "augmented" col

		for (int i = 0; i < kMatSize; ++i)
			tProbRM.addToEntry(i, i, -1);
		//        tProbRM.subtract(MatrixUtils.createRealIdentityMatrix(kMatSize));

		for (int i = 0; i < kMatSize; ++i) {

			if ( target.contains(new Integer(i)) ) {
				tProbRM.setColumn(i, new double[kMatSize]);
				tProbRM.setRow(i, new double[kMatSize]);
				tProbRM.setEntry(i, i, 1);
				aug.setEntry(i, 1);

			} else if ( source.contains(new Integer(i)) ) {
				tProbRM.setColumn(i, new double[kMatSize]);
				tProbRM.setRow(i, new double[kMatSize]);
				tProbRM.setEntry(i, i, 1); //Note that aug[i] is already 0.0

			} else {
				aug.setEntry(i, -sumOverTarget(i));
			}
		}


		DecompositionSolver solver = new LUDecompositionImpl(tProbRM).getSolver();

		return (solver.solve(aug)).getData();
	}


	private double[] getBackwardCommittors(double[] fCommittors) {

		double[] bCommittors = new double[kMatSize];

		for ( int i = 0; i < kMatSize; ++i )
			bCommittors[i] = 1.0d - fCommittors[i];

		return bCommittors;
	}


	private ArrayList<Integer> getIndicies(TupleSet ts) {


		ArrayList<Integer> indicies = new ArrayList<Integer>();

		Iterator tuples = ts.tuples();
		while (tuples.hasNext())
			indicies.add(new Integer(((Tuple)tuples.next()).getRow()));


		return indicies;
	}


	private double sumOverTarget(int index) {

		ArrayList<Integer> indicies = getIndicies(m_target);

		double sum = 0;

		for (Integer i : indicies)
			sum += m_tProb.getEntry(index, i);

		return sum;
	}


	private double[] getEqProbs() {

		Column source = m_graph.getNodeTable().getColumn("eqProb");

		double[] eqProb = new double[kMatSize];

		for ( int i = 0; i < kMatSize; ++i )
			eqProb[i] = source.getDouble(i);

		return eqProb;
	}


	private OpenMapRealMatrix getFluxes( double[] fCommittors, double[] bCommittors, double[] eqProbs ) {

		OpenMapRealMatrix fFluxes = new OpenMapRealMatrix(kMatSize,kMatSize);
		OpenMapRealMatrix nFluxes = new OpenMapRealMatrix(kMatSize,kMatSize);

		for ( int i = 0; i < kMatSize; ++i)
			for (int j = 0; j < kMatSize; ++j)
				if ( m_tProb.getEntry(i,j) != 0.0d && i != j )
					fFluxes.setEntry(i, j, eqProbs[i]*bCommittors[i]*m_tProb.getEntry(i,j)*fCommittors[j]);

		for ( int i = 0; i < kMatSize; ++i )
			for( int j = 0; j < kMatSize; ++j ) {
				double netFlux = fFluxes.getEntry(i, j) - fFluxes.getEntry(j, i);
				if (netFlux < 0.0)
					nFluxes.setEntry(i,j,0.0d);
				else
					nFluxes.setEntry(i, j, netFlux);
			}

		return nFluxes;
	}

	/**
	 * Suck the hightest flux paths out of a TPT matrix (the one associated
	 * with this class). This is a version that was adapted from Kyle's
	 * TPT code; it does a highest-flux reverse walk from the native state.
	 *
	 */
	private ArrayList<Edge> GetHighFluxPath() {

		OpenMapRealMatrix fluxes = new OpenMapRealMatrix(m_fFluxes);

		ArrayList<Integer> indicies = getIndicies(m_source);

		ArrayList<Integer> iList = new ArrayList<Integer>();
		ArrayList<Double> fluxList = new ArrayList<Double>();

		int index = getIndicies(m_target).get(0).intValue();

		iList.add(index);

		// int itr = 0;
		final int maxItr = m_fFluxes.getRowDimension();
		do {

			double[] arr = fluxes.getColumnVector(index).getData();
			index = argmax(arr);

			int numTimes = 0;
			while (iList.contains(index)) {
				arr[index] = -1.0d;
				index = argmax(arr);
				if (++numTimes >= maxItr)
					return new ArrayList();
			}

			iList.add(index);
			fluxList.add(arr[index]);
			//++itr;

			//if ( itr > maxItr )
			//  return new ArrayList();

		} while ( (!(indicies.contains(index))) );

		double f = fluxList.get(argmin(fluxList)).doubleValue();

		ArrayList<Edge> edgeList = new ArrayList<Edge>();

		for ( int k = iList.size() - 1; k < 0; --k ) {

			int i = iList.get(k);
			int j = iList.get(k-1);

			this.m_fFluxes.addToEntry(i, j, -f);

			Node source = m_graph.getNode(i);
			Node target = m_graph.getNode(j);
			Edge e = m_graph.getEdge(source, target);

			source.setDouble("flux", source.getDouble("flux") + f);
			target.setDouble("flux", target.getDouble("flux") + f);
			e.setDouble("flux", e.getDouble("flux") + f);

			edgeList.add(e);
		}

		return edgeList;
	}

	/**
	 * Forward search exhaustive recursive backtracking version of the
	 * algorithm to select highest flux paths. Suffers from tail recursion
	 * which causes hardcore stack explosions once the graphs get larger than
	 * small.
	 */
	private ArrayList<Edge> getHighFluxPathV1() {
		OpenMapRealMatrix fluxes = new OpenMapRealMatrix(this.m_fFluxes);

		ArrayList<Integer> indicies = getIndicies(m_target);

		ArrayList<Integer> iList = new ArrayList<Integer>();
		ArrayList<Double> fluxList = new ArrayList<Double>();

		int index = getIndicies(m_source).get(0).intValue();

		boolean hasPath = false;
		try {
			/*
			 * This recursion is dangerous because it can easily cause stack overflows
			 * truthfully, this should be rewritten iteratively
			 *
			 */
			hasPath = decompose( index, iList, fluxList, indicies, fluxes );
		} catch (StackOverflowError soe) {
			if (!this.error) {
				JOptionPane.showMessageDialog( null,
						"TPT Calculation Caused a Stack Overflow. Increase stack space, or MSMExplorer may be unable to handle a graph this large at the moment",
						"TPT Overflow",
						JOptionPane.ERROR_MESSAGE);
				this.error = true;
			}
		}
		if ( hasPath == false )
			return new ArrayList();

		iList.add(index);

		double f = fluxList.get(argmin(fluxList)).doubleValue();

		ArrayList<Edge> edgeList = new ArrayList<Edge>();

		for ( int k = 0; k < iList.size()-1; ++k ) {

			int j = iList.get(k);
			int i = iList.get(k+1);

			this.m_fFluxes.addToEntry(i, j, -f);

			Node source = m_graph.getNode(i);
			Node target = m_graph.getNode(j);
			Edge e = m_graph.getEdge(source, target);

			source.setDouble("flux", source.getDouble("flux") + f);
			target.setDouble("flux", target.getDouble("flux") + f);
			e.setDouble("flux", fluxList.get(k) + f);

			edgeList.add(e);
		}

		return edgeList;
	}

	final private boolean decompose( int index, ArrayList<Integer> iList,
			ArrayList<Double> fList, ArrayList<Integer> target, RealMatrix fluxes ) {

		if ( target.contains(index) )
			return true;

		OpenMapRealMatrix arr = (OpenMapRealMatrix)fluxes.getRowMatrix(index);

		while (true) {

			index = argmax(arr);
			if (index < 0)
				break;

			if (decompose(index, iList, fList, target, fluxes)) {
				iList.add(index);
				fList.add(arr.getEntry(0,index));
				return true;
			} else {
				arr.setEntry (0, index, 0);
			}
		}

		return false;
	}


	private ArrayList<Edge> getHighFluxPathV2() {
		OpenMapRealMatrix fluxes = new OpenMapRealMatrix(this.m_fFluxes.copy());

		ArrayList<Integer> target = getIndicies(m_target);

		ArrayDeque<Integer> iList = new ArrayDeque<Integer>();
		ArrayDeque<Double> fList = new ArrayDeque<Double>();

		int index = getIndicies(m_source).get(0).intValue();
		iList.push(index);
		OpenMapRealMatrix arr = (OpenMapRealMatrix)fluxes.getRowMatrix(index);

		boolean pathFound = false;

		int iters = 0;
FIND_PATH:
		{
			while (iters < kMAX_ITERS) {

				if (target.contains(index)) {
					pathFound = true;
					break;
				}


				index = argmax (arr);
				while (index < 0) { //row is out of paths

					int dead_index = -1;
					try {
						dead_index = iList.pop();
						fList.pop();
					} catch (NoSuchElementException nsee) {
						//TPT tried to remove element on empty stack.
						//Stack should not be empty
						Logger.getLogger(MSMExplorer.class.getName()).log(Level.WARNING, null, nsee);
					}

					if (iList.isEmpty()) {
						break FIND_PATH;
					} else {
						assert dead_index >= 0;
						//Reset this row
						fluxes.setRowMatrix (dead_index, this.m_fFluxes.getRowMatrix(dead_index));
						int backup_index = iList.peek();
						arr = (OpenMapRealMatrix)fluxes.getRowMatrix(backup_index);
						arr.setEntry(0, dead_index, 0.0d);
						fluxes.setEntry(backup_index, dead_index, 0.0d);
						index = argmax(arr);
					}
				}
				iList.push(index);
				fList.push(arr.getEntry(0, index));
				arr = (OpenMapRealMatrix)fluxes.getRowMatrix(index);
				iters++;
			}
			if (iters >= kMAX_ITERS) {
				//TODO update this when preferences are available
				JOptionPane.showMessageDialog( null,
						"TPT calculation timed out; all the significant paths should be in the current graph. Change kMAX_ITERS in TPTFactoryCM.java to change timeout.",
						"TPT Overflow",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		if ( !pathFound ) {
			return new ArrayList();
		}

		//iList.addLast(index); //XXX not needed???

		double f = min(fList);

		ArrayList<Edge> edgeList = new ArrayList<Edge>();

		//translate our findings into a graph for Prefuse to visualize
		while (true) {

			//because the stack operatons on ArrayDeques add to the front of
			//the deque, we add edges backwards
			int j = iList.pop();
			if (iList.isEmpty()) { //if deque empty, no more edges present
				break;
			}
			int i = iList.peek();

			this.m_fFluxes.addToEntry(i, j, -f);

			Node source = m_graph.getNode(i);
			Node targ = m_graph.getNode(j);
			Edge e = m_graph.getEdge(source, targ);

			source.setDouble("flux", source.getDouble("flux") + f);
			targ.setDouble("flux", targ.getDouble("flux") + f);
			e.setDouble("flux", /*fList.pop() +*/ e.getDouble("flux") + f);

			edgeList.add(e);
		}

		return edgeList;
	}

	/*-_-_-_-_-_-_-_-_-/  Dijkstra Edge Processing /-_-_-_-_-_-_-_-_-_-_-*/

	/**
	 * Yet another path extraction algorithm. This one basically follows
	 * Dijkstra's algorithm, which is nifty because it's pretty fast
	 * and is not greedy. The paths returned here should be optimal.
	 */
	private ArrayList<Edge> getHighFluxPathV3() {

		ArrayDeque<DijkstraEdge> path = new ArrayDeque<DijkstraEdge>();
		PriorityQueue< ArrayDeque<DijkstraEdge> > queue = 
			new PriorityQueue< ArrayDeque<DijkstraEdge> >(kDEFAULT_PQUEUE_SIZE, new DPathComparator());
		HashMap<Integer, Double> fixed = new HashMap<Integer, Double>();

		int index;
		ArrayList<Integer> target = getIndicies(m_target);
		for (int i = 0; i < m_source.getTupleCount(); ++i) {
			index = getIndicies(m_source).get(i).intValue();
			
			if (!fixed.containsKey(index)) {
				fixed.put(index, getCost(path));
				
				//For each edge leaving from index
				OpenMapRealMatrix outedges = (OpenMapRealMatrix)this.m_fFluxes.getRowMatrix(index);
				DijkstraRowVisitor drv = new DijkstraRowVisitor();
				drv.path = path;
				drv.queue = queue;
				drv.fixed = fixed;
				drv.index = index;
				outedges.walkInOptimizedOrder(drv);
			}
		}
		path = queue.remove();
		index = path.peekLast().col; 

		while (!target.contains(index)) {
			if (!fixed.containsKey(index)) {
				fixed.put(index, getCost(path));

				//For each edge leaving from index
				OpenMapRealMatrix outedges = (OpenMapRealMatrix)this.m_fFluxes.getRowMatrix(index);
				DijkstraRowVisitor drv = new DijkstraRowVisitor();
				drv.path = path;
				drv.queue = queue;
				drv.fixed = fixed;
				drv.index = index;
				outedges.walkInOptimizedOrder(drv);
			}
			if (queue.isEmpty()) {
				return new ArrayList<Edge>();
			}

			path = queue.remove(); //means dequeue. weird interface.
			index = path.peekLast().col; 
		}

		ArrayList<Edge> edgeList = new ArrayList<Edge>();
		double cost = getCost(path);
		for (DijkstraEdge de : path) {

			this.m_fFluxes.setEntry(de.row, de.col, Math.max (0.0d, 
						this.m_fFluxes.getEntry(de.row, de.col) - cost));

			Node source = m_graph.getNode(de.row);
			Node targ = m_graph.getNode(de.col);
			Edge e = m_graph.getEdge(source, targ);

			source.setDouble("flux", source.getDouble("flux") + cost);
			targ.setDouble("flux", targ.getDouble("flux") + cost);
			e.setDouble("flux", e.getDouble("flux") + cost);

			edgeList.add(e);
		}

		return edgeList;
	} 

	private class DPathComparator implements Comparator< ArrayDeque<DijkstraEdge> > {
		public int compare (ArrayDeque<DijkstraEdge> a, ArrayDeque<DijkstraEdge> b) {
			return (int)Math.signum(getCost(b) - getCost(a)); //purposefully switched to get opposite order	
		}
	}

	private class DijkstraRowVisitor extends DefaultRealMatrixPreservingVisitor {
		public ArrayDeque<DijkstraEdge> path = new ArrayDeque<DijkstraEdge>();
		public PriorityQueue< ArrayDeque<DijkstraEdge> > queue = 
			new PriorityQueue< ArrayDeque<DijkstraEdge> >(kDEFAULT_PQUEUE_SIZE, new DPathComparator());
		HashMap<Integer, Double> fixed;
		public int index; //The row this visitor is traversing

		@Override
			public void visit (int row, int column, double value) {
				if (!fixed.containsKey(column) && value > 0.0d) {
					path.addLast(new DijkstraEdge(index, column));
					queue.add(path.clone());
					path.removeLast();
				}
			}
	}

	/**
	 * Should return the minimum flux along the path
	 * passed as parameter.
	 *
	 * @param ArrayDeque representing the path to extract cost from
	 */
	private double getCost (ArrayDeque<DijkstraEdge> path) {
		assert path != null;
		if (path.isEmpty()) {
			return 0.0d;
		}
		DijkstraEdge first = path.peekFirst();

		double cost = this.m_fFluxes.getEntry(first.row, first.col);
		assert (cost > 0.0d) : cost + "" + first.row + "" + first.col;
		for (DijkstraEdge de : path) {
			double edgeFlux = m_fFluxes.getEntry(de.row, de.col);
			if (edgeFlux < cost) {
				cost = edgeFlux;
			}
		}
		return cost;
	}

	/*-_-_-_-_-_-_-_-_-/  Dijkstra End /-_-_-_-_-_-_-_-_-_-_-*/


	private boolean hasNonZero( double[] arr ) {

		int length = arr.length;

		for ( int i = 0; i < length; ++i ) {
			if (arr[i] > 0.0d)
				return true;
		}
		return false;
	}

	private int argmax ( OpenMapRealMatrix row ) {
		return (int)row.walkInOptimizedOrder( new ArgMaxRowVisitor() );
	}

	private int argmax( double[] arr ) {
		int index = -1;
		double max = arr[0];

		for (int i = 1; i < arr.length; ++i)
			if ( arr[i] > max ) {
				index = i;
				max = arr[i];
			}

		return index;
	}


	private int argmin( double[] arr ) {
		int index = 0;
		double min = arr[0];

		for (int i = 1; i < arr.length; ++i)
			if ( arr[i] < min ) {
				index = i;
				min = arr[i];
			}

		return index;
	}


	private int argmin( ArrayList<Double> arr ) {

		Object[] bigD = arr.toArray();
		double[] smallD = new double[bigD.length];

		for ( int i = 0; i < bigD.length; ++i )
			smallD[i] = ((Double)bigD[i]).doubleValue();

		return argmin( smallD );
	}

	private double min( ArrayDeque<Double> arr ) {
		assert !arr.isEmpty();
		double min = arr.peek();

		for (double d : arr) {
			if (d < min) {
				min = d;
			}
		}
		return min;
	}

	public static double[][] deepCopy( double[][] source ) {
		int length = source.length;

		double[][] target = new double[length][source[0].length];

		for ( int i = 0; i < length; ++i )
			target[i] = source[i].clone();

		return target;
	}

	public static OpenMapRealMatrix deepCopy( OpenMapRealMatrix source) {

		OpenMapRealMatrix target = new OpenMapRealMatrix(source.copy());

		return target;
	}

	/**
	 * A utility class that does the visiting for a walk over a RealMatrix
	 * to perform ArgMax.
	 * Here we assume we are walking over a row, so we return the index
	 * max.
	 * We extend the Default...Visitor so we don't have to implement start.
	 */
	private static class ArgMaxRowVisitor extends DefaultRealMatrixPreservingVisitor {

		private int index = -1; //-1 is sentinal for failure
		private double max = 0.0d;

		/**
		 * Override of empty function. Does the little bit of work for argmax.
		 */
		@Override
			public void visit (int row, int column, double value) {
				if (value > max) {
					index = column;
					max = value;
				}
			}

		/**
		 * Override of empty function. the walk function has
		 * to return a double, so we need to perform the annoying
		 * cast. This is argmax, so we return the column index at which the
		 * max is found in the row.
		 */
		@Override
			public double end () {
				return (double)index;
			}
	}
}
