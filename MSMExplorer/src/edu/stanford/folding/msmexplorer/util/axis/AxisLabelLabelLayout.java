package edu.stanford.folding.msmexplorer.util.axis;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import javax.swing.JLabel;
import prefuse.Constants;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.util.PrefuseLib;
import prefuse.util.ui.ValuedRangeModel;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

/**
 * I want axis title labels on an AxisLabelLayout. Is that
 * too much to ask? This class says no, it's not.
 *
 * @author gestalt
 */
public class AxisLabelLabelLayout extends AxisLabelLayout {
		
		protected JLabel label = new JLabel();
		protected JLabel gridLab = new JLabel();
		protected String str = "";
		
		public AxisLabelLabelLayout(String group, int axis, ValuedRangeModel values) {
				super(group, axis, values);
		}
		
		public AxisLabelLabelLayout(String group, int axis, ValuedRangeModel values, Rectangle2D bounds) {
				super(group, axis, values, bounds);
		}
		
		public AxisLabelLabelLayout(String group, AxisLayout layout) {
				super(group, layout);
		}
		
		public AxisLabelLabelLayout(String group, AxisLayout layout, Rectangle2D bounds) {
				super(group, layout, bounds);
		}
		
		public AxisLabelLabelLayout(String group, AxisLayout layout, Rectangle2D bounds, double spacing) {
				super(group,layout,bounds,spacing);
		}
		
		public void setLabel(String lab) {
				str = lab;
				label.setText(str);
		}

		public void setLabel(JLabel lab) {
				str = lab.getText();
				label = lab;
		}

		public void setGridLabel(JLabel lab) {
			gridLab = lab;
		}
		
		@Override
		public void run(double frac) {
			super.run(frac);
			VisualTable labels = getTable();
			Iterator<VisualItem> items = labels.tuples();
			while (items.hasNext()) {
					VisualItem i = items.next();
					i.setFont(gridLab.getFont());
			}

			VisualItem item = labels.addItem();
			item.setVisible(true);
			item.setStartVisible(false);
			item.setEndVisible(false);
			item.set(LABEL, str);
			item.setFont(label.getFont());
			Rectangle2D bounds = getLayoutBounds();
			double length = getBreadth(bounds);
			//double width = item.getFont().getSize()*6.0d/3.0d;
			double width = label.getPreferredSize().getWidth() / 2.0d;
			if (getAxis() == Constants.Y_AXIS) {
					item.setDOI(1.23456789);
					width = 0;
			}
			setLabPos(item, length/2.0d + width, bounds);
		}
		
		/**
		 * Set the layout values for an axis label item.
		 */
		protected void setLabPos(VisualItem item, double xOrY, Rectangle2D b) {
			switch (getAxis()) {
				case Constants.X_AXIS:
					xOrY = super.isAscending() ? xOrY + b.getMinX() : b.getMaxX() - xOrY;
					PrefuseLib.updateDouble(item, VisualItem.X,  xOrY);
					PrefuseLib.updateDouble(item, VisualItem.Y,  b.getMaxY() + label.getFont().getSize()/3.0d + gridLab.getFont().getSize());
					PrefuseLib.updateDouble(item, VisualItem.X2, xOrY);
					PrefuseLib.updateDouble(item, VisualItem.Y2, b.getMaxY() + label.getFont().getSize()/3.0d + gridLab.getFont().getSize());
					break;
				case Constants.Y_AXIS:
					xOrY = super.isAscending() ? b.getMaxY() - xOrY - 1 : xOrY + b.getMinY();
					PrefuseLib.updateDouble(item, VisualItem.X,  b.getMinX());
					PrefuseLib.updateDouble(item, VisualItem.Y,  xOrY);
					PrefuseLib.updateDouble(item, VisualItem.X2, b.getMinX());
					PrefuseLib.updateDouble(item, VisualItem.Y2, xOrY);
			}
		}
}
