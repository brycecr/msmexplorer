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
package edu.stanford.folding.msmexplorer.util.axis;

import java.awt.geom.AffineTransform;
import prefuse.render.AxisRenderer;
import prefuse.visual.VisualItem;

/**
 * It renders axis, but it can rotate axis labels. Party time. 
 *
 * @author brycecr
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
