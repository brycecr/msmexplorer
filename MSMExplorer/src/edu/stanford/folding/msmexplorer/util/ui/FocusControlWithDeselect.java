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
package edu.stanford.folding.msmexplorer.util.ui;

import java.awt.event.MouseEvent;

import prefuse.controls.FocusControl;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.tuple.TupleSet;

/**
 * Overrides vanilla FocusControl.
 *
 * A click on the background will deselect
 * the currently focused nodes.
 */
public class FocusControlWithDeselect extends FocusControl {

	public FocusControlWithDeselect(int clicks) {
		super(clicks);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) {
			return;
		}
		Visualization vis = ((Display) e.getSource()).getVisualization();

		this.curFocus = null;
		TupleSet ts = vis.getFocusGroup(Visualization.FOCUS_ITEMS);
		ts.clear();
		if (activity != null) {
			vis.run(activity);
		}
	}
} // end of class FocusControlWithDeselect

