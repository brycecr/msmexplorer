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
package edu.stanford.folding.msmexplorer.util.aggregate;

import java.awt.event.MouseEvent;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.controls.DragControl;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualItem;

/**
 * Interactive drag control that is "aggregate-aware"
 *
 * @auhtor Jeffrey Heer
 */
public class AggregateDragControl extends DragControl {
	
	/**
	 * Creates a new drag control that issues repaint requests as an item
	 * is dragged.
	 */
	public AggregateDragControl() {
	}

	public AggregateDragControl(String action) {
		super(action);
	}

	public void itemEntered(VisualItem item, MouseEvent e) {
		super.itemEntered(item, e);
		setFixed(item, true);
	}

	public void itemExited(VisualItem item, MouseEvent e) {
		super.itemExited(item, e);
		setFixed(item, false);
	}
	
	@Override
	public void itemReleased(VisualItem item, MouseEvent e) {
		super.itemReleased(item, e);
	}
	/**
	 * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
	 */
	@Override
	public void itemDragged(VisualItem item, MouseEvent e) {
		if ((item instanceof AggregateItem)) {
			if (!SwingUtilities.isLeftMouseButton(e)) return;
			dragged = true;
			Display d = (Display)e.getComponent();
			d.getAbsoluteCoordinate(e.getPoint(), temp);
			double dx = temp.getX()-down.getX();
			double dy = temp.getY()-down.getY();
			
			move(item, dx, dy);
			
			down.setLocation(temp);
		} else {
			super.itemDragged(item, e);
		}
		Visualization vis = ((Display)e.getComponent()).getVisualization();
		Action a = vis.getAction(action);
		if (a != null) {
			((Display)e.getComponent()).getVisualization().run(action);
		}
	}
	
	protected static void setFixed(VisualItem item, boolean fixed) {
		if ( item instanceof AggregateItem ) {
			Iterator items = ((AggregateItem)item).items();
			while ( items.hasNext() ) {
				setFixed((VisualItem)items.next(), fixed);
			}
		} else {
			item.setFixed(fixed);
		}
	}
	
	protected static void move(VisualItem item, double dx, double dy) {
		if (item == null) {
			return;
		}

		if ( item instanceof AggregateItem ) {
			Iterator items = ((AggregateItem)item).items();
			while ( items.hasNext() ) {
				VisualItem aitem = (VisualItem)items.next();
				move(aitem, dx, dy);
			}
		} else {
			double x = item.getX();
			double y = item.getY();
			item.setStartX(x);  item.setStartY(y);
			item.setX(x+dx);    item.setY(y+dy);
			item.setEndX(x+dx); item.setEndY(y+dy);
		}
	}
} // end of class AggregateDragControl