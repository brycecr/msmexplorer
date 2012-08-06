/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.folding.msmexplorer.util.ui;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import prefuse.Visualization;
import prefuse.action.assignment.DataSizeAction;
import prefuse.render.EdgeRenderer;
import prefuse.render.ImageFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ui.JRangeSlider;

/**
 * A general visualization properties settings dialog
 * designed for MSMExplorer. If you're trying to use this for 
 * something else, you'll have to be careful with all of the
 * action and group names...
 *
 * @author brycecr
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

		JTabbedPane pane = new JTabbedPane();

		/* GENERAL PANE */
		JPanel gen_Panel = new JPanel();
		pane.addTab("General", gen_Panel);

		final DataSizeAction nodeSizeAction = (DataSizeAction)m_vis.getAction("nodeSize");
		final JRangeSlider nodeSizeSlider = new JRangeSlider(1, 2000, 
			(int)nodeSizeAction.getMinimumSize(), (int)nodeSizeAction.getMaximumSize(), 
			JRangeSlider.HORIZONTAL);
		nodeSizeSlider.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				int lowVal = nodeSizeSlider.getLowValue();
				int highVal = nodeSizeSlider.getHighValue();

				if (m_lr.getImageField() == null) {
					nodeSizeAction.setMaximumSize(highVal);
					nodeSizeAction.setMinimumSize(lowVal);
				} else {
					double scale = 1.0d / m_vis.getDisplay(0).getScale();
					double highDub = highVal / 100.0;
					if (highDub < lowVal) {
						highDub = lowVal;
					}
					nodeSizeAction.setMinimumSize(lowVal);
					nodeSizeAction.setMaximumSize(highDub);
					m_lr.setImageFactory(new ImageFactory());
					m_lr.getImageFactory().setMaxImageDimensions((int) (150.0d * highDub), (int) (150.0d * highDub));
					m_lr.getImageFactory().preloadImages(m_vis.getGroup(nodes).tuples(), "image");
					m_lr.setImageField("image");
				}
				m_vis.run("nodeSize");
				m_vis.run("animate");
			}
		});
		gen_Panel.add(new JLabel("Node Size Range:"));
		gen_Panel.add(nodeSizeSlider);
		
		/* LABEL PANE */
		JPanel lr_Panel = new JPanel();
		lr_Panel.setLayout(new GridLayout(0,2));
		pane.addTab("Label Render", lr_Panel);

		final JSpinner lr_Rounded = new JSpinner(new SpinnerNumberModel(8, 0, 1000, 1));
		lr_Rounded.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				m_lr.setRoundedCorner((Integer)lr_Rounded.getValue(), (Integer)lr_Rounded.getValue());
				m_vis.run("animate");
			}
		});
		lr_Panel.add(new JLabel("Rounding Radius:"));
		lr_Panel.add(lr_Rounded);

		final JToggleButton lr_showLabel = new JToggleButton("Images Only", false);
		lr_showLabel.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (lr_showLabel.isSelected()) {
					if (m_lr.getImageField() != null) {
						m_lr.setTextField(null);
						m_vis.run("animate");
					} else {
						//if no images are visble, we don't want
						//to make nodes disappear, so refuse
						//to select
						lr_showLabel.setSelected(false);
					}
				}
			}
		});
		lr_showLabel.setSelected(m_lr.getImageField() != null);
		lr_Panel.add(lr_showLabel);

		/* SHAPE PANE */
		JPanel sr_Panel = new JPanel();
		pane.addTab("Shape Render", sr_Panel);
		
		/* EDGE PANE */
		JPanel er_Panel = new JPanel();
		pane.addTab("Edge Render", er_Panel);

		/* AGG PANE */
		JPanel pr_Panel = new JPanel();
		pane.addTab("Aggregate Render", pr_Panel);

		add(pane);
		pack();
	}

	public void showDialog() {
		setLocationByPlatform(true);
		setVisible(true);
	}
}
