/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.folding.msmexplorer.util.axis;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.render.AxisRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.visual.VisualItem;

/**
 *
 * @author gestalt
 */
public class AxisRotateRenderer extends AxisRenderer {

	 /**
     * Create a new AxisRotateRenderer. By default, axis labels are drawn along the
     * left edge and underneath the tick marks.
     */
    public AxisRotateRenderer() {
	    super();
    }
    
    /**
     * Create a new AxisRotateRenderer.
     * @param xalign the horizontal alignment for the axis label. One of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT},
     * or {@link prefuse.Constants#CENTER}.
     * @param yalign the vertical alignment for the axis label. One of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM},
     * or {@link prefuse.Constants#CENTER}.
     */
    public AxisRotateRenderer(int xalign, int yalign) {
	    super(xalign, yalign);
    }

	@Override
	protected AffineTransform getTransform(VisualItem item) {
		if (item.getDOI() == 1.23456789) {
			double width = item.getString(AxisLabelLabelLayout.LABEL).length()*item.getFont().getSize()/3.0;
			AffineTransform at = AffineTransform.getQuadrantRotateInstance(-1, item.getX(), item.getY());
			at.translate(width, -(2.0d*item.getFont().getSize() + 5));
			return at;
		}		
		return null;
	}
}
