/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.folding.msmexplorer.util.ui;

import edu.stanford.folding.msmexplorer.MSMConstants;
import edu.stanford.folding.msmexplorer.MSMExplorer;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.render.EdgeRenderer;
import prefuse.render.ImageFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.FontLib;
import prefuse.util.ui.JRangeSlider;
import prefuse.visual.VisualItem;

/**
 * A general visualization properties settings dialog
 * designed for MSMExplorer. If you're trying to use this for 
 * something else, you'll have to be careful with all of the
 * action and group names...
 *
 * @author brycecr
 */
public class VisualizationSettingsDialog extends JDialog implements MSMConstants {

	private final Visualization m_vis;
	private final LabelRenderer m_lr;
	private final ShapeRenderer m_sr;
	private final EdgeRenderer m_er;
	private final PolygonRenderer m_pr;

	//copied from AxisSettingsDialog...maybe there's a clean way to share this?
	private static final Integer[] FONTSIZES = {4,6,8,10,12,14,16,28,20,24,28,32,26,40,48,50,56,64,72,
	84,96,110,130,150,170,200,240,280,320,360,400,450,500};

	// Visualization group names. Should be same as MSMExplorer
	private static final String AGGR = "aggregates";
	private static final String GRAPH = "graph";
	private static final String NODES = "graph.nodes";
	private static final String EDGES = "graph.edges";

	//Shaperender node shapes and corresponding labels in the JComboBox
	private static final int[] SHAPES = {Constants.SHAPE_CROSS,
		Constants.SHAPE_DIAMOND, Constants.SHAPE_ELLIPSE, Constants.SHAPE_HEXAGON,
		Constants.SHAPE_RECTANGLE, Constants.SHAPE_STAR, Constants.SHAPE_TRIANGLE_UP,
		Constants.SHAPE_TRIANGLE_DOWN};
	private static final String[] SHAPES_LABELS = {"Cross", "Diamond",
		"Circle", "Hexagon", "Rectangle", "Star", "Up Triangle", "Down Triangle"};

	//LabelRenderer image positions and corresponding labels in JComboBox
	private static final int[] IMAGEPOS = {Constants.LEFT, Constants.RIGHT,
		Constants.TOP, Constants.BOTTOM};
	private static final String[] IMAGEPOS_LABELS = {"Left", "Right", "Top", "Bottom"};

	public VisualizationSettingsDialog(final Frame f, Visualization vis, LabelRenderer lr, 
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
		gen_Panel.setLayout(new BoxLayout(gen_Panel, BoxLayout.Y_AXIS));
		pane.addTab("General", gen_Panel);

		//Slider sets range for automatically interpolated node size
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
					//ineffecient, perhaps, but forces the resolution to be re-evaluated.
					//a better ImageFactory should cache the original images
					//instead of just the scaled ones
					m_lr.setImageFactory(new ImageFactory());
					m_lr.getImageFactory().setMaxImageDimensions((int) (150.0d * highDub), (int) (150.0d * highDub));
					m_lr.getImageFactory().preloadImages(m_vis.getGroup(NODES).tuples(), "image");
					m_lr.setImageField("image");
				}
				m_vis.run("nodeSize");
				m_vis.run("animate");
			}
		});

		JButton showColorChooser = new JButton("Node Color"); 
		showColorChooser.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					ColorAction fill = (ColorAction)((ActionList)m_vis.getAction("animate")).get(2);
					Color newFill = JColorChooser.showDialog(f, "Choose Node Color", new Color(fill.getDefaultColor()));
					if (newFill != null) {
						fill.setDefaultColor(newFill.getRGB());
					}
				} catch (Exception e) {
					Logger.getLogger(MSMExplorer.class.getName()).log(Level.SEVERE, null, e);
				}
			}
		});
		showColorChooser.setToolTipText("Open a dialog to select a new node color.");

		Box gen_node = new Box(BoxLayout.X_AXIS);
		gen_node.setBorder(BorderFactory.createTitledBorder("Gen. Node Appearance"));
		gen_node.add(new JLabel("Node Size Range: "));
		gen_node.add(nodeSizeSlider);
		gen_node.add(showColorChooser);
		gen_node.add(Box.createGlue());

		gen_Panel.add(gen_node);
		gen_Panel.add(Box.createVerticalStrut(100));
		
		
		/* LABEL PANE */
		JPanel lr_Panel = new JPanel();
		lr_Panel.setLayout(new GridLayout(0,2));
		pane.addTab("Label Render", lr_Panel);

		final JSpinner lr_Rounded = new JSpinner(new SpinnerNumberModel(8, 0, 1000, 1));
		lr_Rounded.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				m_lr.setRoundedCorner((Integer)lr_Rounded.getValue(), (Integer)lr_Rounded.getValue());
				m_vis.run("nodeFill");
				m_vis.repaint();
			}
		});
		lr_Panel.add(new JLabel("Rounding Radius:"));
		lr_Panel.add(lr_Rounded);

		final JComboBox lr_fontSize = new JComboBox(FONTSIZES);
		lr_fontSize.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				m_vis.setValue(NODES, null, VisualItem.FONT, FontLib.getFont("Tahoma", (Integer)lr_fontSize.getSelectedItem()));
				m_vis.run("nodeFill");
				m_vis.repaint();
			}
		});
		lr_fontSize.setSelectedItem(((VisualItem)m_vis.getVisualGroup(NODES).tuples().next()).getFont().getSize());
		lr_Panel.add(new JLabel("Label Font Size"));
		lr_Panel.add(lr_fontSize);

		final JComboBox lr_imagePos = new JComboBox(IMAGEPOS_LABELS);
		lr_imagePos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				m_lr.setImagePosition(IMAGEPOS[lr_imagePos.getSelectedIndex()]);
				m_vis.run("nodeFill");
				m_vis.repaint();
			}
		});
		lr_Panel.add(new JLabel("Image Position"));
		lr_Panel.add(lr_imagePos);

		final JToggleButton lr_showLabel = new JToggleButton("Images Only", m_lr.getTextField() == null);
		lr_showLabel.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (m_lr.getImageField() != null) {
					if (lr_showLabel.isSelected()) {
						m_lr.setTextField(null);
					} else {
						m_lr.setTextField(LABEL);
					}
					m_vis.run("nodeFill");
					m_vis.repaint();
				} else {
					//if no images are visble, we don't want
					//to make nodes disappear, so refuse
					//to select
					lr_showLabel.setSelected(false);
				}
			}
		});
		lr_Panel.add(lr_showLabel);


		/* SHAPE PANE */
		JPanel sr_Panel = new JPanel();
		pane.addTab("Shape Render", sr_Panel);
		
		final JComboBox shapeComboBox = new JComboBox(SHAPES_LABELS);
		shapeComboBox.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				m_vis.setValue(NODES, null, VisualItem.SHAPE, SHAPES[shapeComboBox.getSelectedIndex()]);
				m_vis.run("nodeFill");
				m_vis.repaint();
			}
		});
		
		sr_Panel.add(new JLabel("Node Shape: "));
		sr_Panel.add(shapeComboBox);
		
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
