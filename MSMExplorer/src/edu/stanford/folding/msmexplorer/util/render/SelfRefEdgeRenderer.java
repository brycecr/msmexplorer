/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.folding.msmexplorer.util.render;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import prefuse.Constants;
import prefuse.render.EdgeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.EdgeItem;

/**
 *
 * @author gestalt
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
	@Override
	public void render(Graphics2D g, VisualItem item) {
		// render the edge line
		super.render(g, item);

		EdgeItem edge = (EdgeItem) item;
		VisualItem item1 = edge.getSourceItem();
		VisualItem item2 = edge.getTargetItem();

		// render the edge arrow head, if appropriate
		if ( m_curArrow != null && item1 == item2 ) {
			g.setPaint(ColorLib.getColor(item.getFillColor()));
			g.fill(m_curArrow);
		}
	}
}