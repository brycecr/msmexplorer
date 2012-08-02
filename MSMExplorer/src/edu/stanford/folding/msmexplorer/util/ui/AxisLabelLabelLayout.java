package edu.stanford.folding.msmexplorer.util.ui;

import java.awt.geom.Rectangle2D;
import javax.swing.JLabel;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
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

	public JLabel getlabel() {
		return label;
	}

	@Override
	public void run(double frac) {
		super.run(frac);
		VisualTable labels = getTable();
		VisualItem item = labels.addItem();
		item.setVisible(true);
		item.setEndVisible(true);
		item.set(LABEL, "HELLO");
		Rectangle2D bounds = getLayoutBounds();
		double length = Math.max(bounds.getWidth(), bounds.getHeight());
		set(item, length/2.0d, bounds);
		setY(item,item, 1000);
	}	

}
