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

