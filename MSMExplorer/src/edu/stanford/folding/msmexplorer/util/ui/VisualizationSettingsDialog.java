/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.folding.msmexplorer.util.ui;

import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import prefuse.Visualization;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.ShapeRenderer;

/**
 *
 * @author gestalt
 */
public class VisualizationSettingsDialog extends JDialog {

	private final Visualization m_vis;
	private final LabelRenderer m_lr;
	private final ShapeRenderer m_sr;
	private final EdgeRenderer m_er;
	private final PolygonRenderer m_pr;

	// Visualization group names. Should be same as MSMExplorer
	private static final String aggr = "aggregates";
	private static final String graph = "graph";
	private static final String nodes = "graph.nodes";
	private static final String edges = "graph.edges";

	public VisualizationSettingsDialog(Frame f, Visualization vis, LabelRenderer lr, 
					ShapeRenderer sr, EdgeRenderer er, PolygonRenderer pr) {
		super(f);
		m_vis = vis;
		m_lr = lr;
		m_sr = sr;
		m_er = er;
		m_pr = pr;

		final JSpinner lr_Rounded = new JSpinner(new SpinnerNumberModel(8, 0, 1000, 1));
		lr_Rounded.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				m_lr.setRoundedCorner((Integer)lr_Rounded.getValue(), (Integer)lr_Rounded.getValue());
				m_vis.run("animate");
			}
		});
		this.add(lr_Rounded);
	}

	public void showDialog() {
		setVisible(true);
	}
}
