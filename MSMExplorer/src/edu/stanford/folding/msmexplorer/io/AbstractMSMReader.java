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
package edu.stanford.folding.msmexplorer.io;

import edu.stanford.folding.msmexplorer.MSMConstants;
import prefuse.data.Table;
import prefuse.data.io.AbstractGraphReader;

/**
 * An abstract superclass for building nodes. This used to be more useful.
 * Maybe it will be useful again...
 *
 * @author brycecr
 */
public abstract class AbstractMSMReader extends AbstractGraphReader implements MSMConstants {

	protected Table m_nodeTable;
	protected Table m_edgeTable;

	
}
