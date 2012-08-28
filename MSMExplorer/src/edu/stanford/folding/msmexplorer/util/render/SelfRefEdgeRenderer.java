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
package edu.stanford.folding.msmexplorer.util.render;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import prefuse.data.Edge;
import prefuse.render.EdgeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.EdgeItem;

/**
 * Renders self edges as circles...
 *
 * @author pap forums
 */
public class SelfRefEdgeRenderer extends EdgeRenderer {
	private Ellipse2D m_ellipse = new Ellipse2D.Float();
	private boolean showSelfEdges = true;
	
	public boolean getRenderSelfEdges() {
		return showSelfEdges;
	}
	
	public void setRenderSelfEdges(boolean show) {
		showSelfEdges = show;
	}
	
	@Override
	protected Shape getRawShape(VisualItem item) {
		try  	  {
			EdgeItem edge = (EdgeItem) item;
			VisualItem item1 = edge.getSourceItem();
			VisualItem item2 = edge.getTargetItem();
			
			//  self interaction
			if (item1 == item2)
			{
				if (!showSelfEdges) {
					return null;
				}
				getAlignedPoint(m_tmpPoints[0], item1.getBounds(), m_xAlign1, m_yAlign1);
				getAlignedPoint(m_tmpPoints[1], item2.getBounds(), m_xAlign2, m_yAlign2);
				
				m_curWidth = (int) Math.round(m_width * getLineWidth(item));
				
				double w = item1.getBounds().getWidth();
				double h = item1.getBounds().getHeight();
				double halfAvgSize = (h + w) / 4.;
				
				m_ellipse.setFrame(m_tmpPoints[0].getX() + w/4,
					m_tmpPoints[0].getY() + h/4,
					halfAvgSize, halfAvgSize);
				
				return m_ellipse;
			}
		}
		catch(Exception ex) {
			return null;
		}
		
		return super.getRawShape(item);
	} //getRawShape
	
	
	/**
	 * @see prefuse.render.Renderer#render(java.awt.Graphics2D, prefuse.visual.VisualItem)
	 */
	public void render(Graphics2D g, VisualItem item) {
		// render the edge line
		super.render(g, item);
		// render the edge arrow head, if appropriate
		if ( m_curArrow != null && !isSelfEdge(item)) {
			g.setPaint(ColorLib.getColor(item.getFillColor()));
			g.fill(m_curArrow);
		}
	}

	/**
	 * Does not assume item is an edge; returns whether item is a self-edge.
	 * 
	 * @param item in question
	 * @return boolean indicating whether item is a self-edge
	 */
	protected boolean isSelfEdge(VisualItem item) {
		if (item instanceof EdgeItem) {
			VisualItem source = ((EdgeItem)item).getSourceItem();
			VisualItem target = ((EdgeItem)item).getTargetItem();
			if (source.equals(target)) {
				return true;
			}
		}
		return false;
	}
}