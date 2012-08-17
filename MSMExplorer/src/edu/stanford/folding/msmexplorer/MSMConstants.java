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
package edu.stanford.folding.msmexplorer;

/**
 * Old-skool java style, this interface acts as a repository for common
 * static fields. It should probably be statically imported and not
 * implemented, properly speeking.
 *
 * @author brycecr 
 */
public interface MSMConstants {

	//comon data field names
	public static final String LABEL = "label";
	public static final String EQPROB = "eqProb";
	public static final String TPROB = "probability";

	// visualization group names 
	public static final String AGGR = "aggregates";
	public static final String GRAPH = "graph";
	public static final String NODES = "graph.nodes";
	public static final String EDGES = "graph.edges";
}
