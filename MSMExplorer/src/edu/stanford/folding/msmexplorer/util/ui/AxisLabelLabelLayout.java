package edu.stanford.folding.msmexplorer.util.ui;

import java.awt.geom.Rectangle2D;
import javax.swing.JLabel;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.util.ui.ValuedRangeModel;

/**
 * I want axis labels on an AxisLabelLayout. Is that 
 * too much to ask?
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
	public void run (double frac) {
		super.run(frac);
		double xxpos = m_bounds.getX() + (m_bounds.getWidth() / 2.0) - (label.getWidth() / 2.0);
		double xypos = m_bounds.getY() + m_bounds.getHeight() + 5;
		label.setBounds((int)xxpos, (int)xypos, label.getWidth(), label.getHeight());
		label.repaint();
	}
}
