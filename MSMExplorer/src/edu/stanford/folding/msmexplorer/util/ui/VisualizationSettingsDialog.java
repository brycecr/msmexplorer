package edu.stanford.folding.msmexplorer.util.ui;

import edu.stanford.folding.msmexplorer.MSMConstants;
import edu.stanford.folding.msmexplorer.MSMExplorer;
import edu.stanford.folding.msmexplorer.util.FlexDataColorAction;
import edu.stanford.folding.msmexplorer.util.render.SelfRefEdgeRenderer;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
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
import prefuse.action.assignment.DataShapeAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.column.Column;
import prefuse.render.ImageFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
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

	// Visualization we're modifying. Action and group names depend
	// on what was registered with this visualization object
	private final Visualization m_vis;
	private final LabelRenderer m_lr;
	private final ShapeRenderer m_sr;
	private final SelfRefEdgeRenderer m_er;
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

	private static final String[] PALETTE_LABELS = {"Interpolated", "Category", "Cool", 
		"Hot", "Grayscale", "HSB"};

	public VisualizationSettingsDialog(final Frame f, Visualization vis, LabelRenderer lr, 
					ShapeRenderer sr, SelfRefEdgeRenderer er, PolygonRenderer pr) {
		//Init instance members
		super(f);
		m_vis = vis;
		m_lr = lr;
		m_sr = sr;
		m_er = er;
		m_pr = pr;

		//Pane for options related to different renderers.
		JTabbedPane pane = new JTabbedPane();

		//main layout box
		Box main = new Box(BoxLayout.Y_AXIS);

		/* ----------------------- GENERAL PANE ----------------------- */
		//General pane (displayed above the tabbed pane,
		//but could be it's own tab as well...
		JPanel gen_Panel = new JPanel();
		gen_Panel.setLayout(new BoxLayout(gen_Panel, BoxLayout.Y_AXIS));

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
				m_vis.run("aggLayout");
				m_vis.run("animate");
			}
		});



		final Table nt = ((Graph)m_vis.getGroup(GRAPH)).getNodeTable();
		int numCols = nt.getColumnCount();

		//just the numerical type fields. It doesn't make sense to scale
		//nodes based on an ordinal range, so we restrict to a numerical types
		final Vector<String> numericalFields = new Vector<String>(5);

		//all data fields (except visualization backing junk)
		final Vector<String> fields = new Vector<String>(5);

		//we start at Label to skip all the backing data
		for (int i = nt.getColumnNumber(LABEL); i < numCols; ++i) {
			if (isNumerical(nt.getColumn(i))) {
				numericalFields.add(nt.getColumnName(i));
			}
			fields.add(nt.getColumnName(i));
		}
		final JComboBox nodeSizeActionField = new JComboBox(numericalFields);
		nodeSizeActionField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				nodeSizeAction.setDataField((String)nodeSizeActionField.getSelectedItem());
				m_vis.run("nodeSize");
				m_vis.repaint();
			}
		});
		nodeSizeActionField.setSelectedItem(nodeSizeAction.getDataField());

		final FlexDataColorAction nodeColorAction = (FlexDataColorAction)m_vis.getAction("nodeFill");
		final JComboBox nodeColorActionField = new JComboBox(fields);
		nodeColorActionField.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int dataType = Constants.ORDINAL;
				String col = (String)nodeColorActionField.getSelectedItem();
				if (isNumerical(nt.getColumn(col))) {
					dataType = Constants.NUMERICAL;
				}
				nodeColorAction.setDataField(col);
				nodeColorAction.setDataType(dataType);
				m_vis.run("nodeFill");
				m_vis.repaint();
			}
		});
		nodeColorActionField.setSelectedItem(nodeColorAction.getDataField());

		int[] palette = nodeColorAction.getPalette();
		final JComboBox presetPalettes = new JComboBox(PALETTE_LABELS);

		final JButton startColorButton = new JButton("Start Color", new
				ColorSwatch(new Color(palette[palette.length-1])));
		startColorButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					Color newColor = JColorChooser.showDialog(f, "Choose Node Color", 
							new Color(nodeColorAction.getDefaultColor()));
					if (newColor != null) {
						int[] oldPalette = nodeColorAction.getPalette();
						int[] palette = ColorLib.getInterpolatedPalette(newColor.getRGB(), oldPalette[oldPalette.length-1]);
						nodeColorAction.setPalette(palette);
						((ColorSwatch)startColorButton.getIcon()).setColor(newColor);
						presetPalettes.setSelectedIndex(0);
					}
				} catch (Exception e) {
					Logger.getLogger(MSMExplorer.class.getName()).log(Level.SEVERE, null, e);
				}
			}
		});

		final JButton endColorButton = new JButton("End Color", 
			new ColorSwatch(new Color(palette[0])));
		endColorButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					Color newColor = JColorChooser.showDialog(f, "Choose Node Color", 
							new Color(nodeColorAction.getDefaultColor()));
					if (newColor != null) {
						int[] palette = ColorLib.getInterpolatedPalette(nodeColorAction.getPalette()[0], newColor.getRGB());
						nodeColorAction.setPalette(palette);
						((ColorSwatch)endColorButton.getIcon()).setColor(newColor);
						presetPalettes.setSelectedIndex(0);
					}
				} catch (Exception e) {
					Logger.getLogger(MSMExplorer.class.getName()).log(Level.SEVERE, null, e);
				}
			}
		});

		final JButton showColorChooser = new JButton("Node Color", new ColorSwatch(new Color(nodeColorAction.getPalette()[0])));
		showColorChooser.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					FlexDataColorAction fill = (FlexDataColorAction)((ActionList)m_vis.getAction("animate")).get(2);
					Color newFill = JColorChooser.showDialog(f, "Choose Node Color", new Color(fill.getDefaultColor()));
					if (newFill != null) {
						int[] palette = {newFill.getRGB()};
						fill.setPalette(palette);
						((ColorSwatch)showColorChooser.getIcon()).setColor(newFill);
						((ColorSwatch)startColorButton.getIcon()).setColor(newFill);
						((ColorSwatch)endColorButton.getIcon()).setColor(newFill);
						showColorChooser.repaint();
						startColorButton.repaint();
						endColorButton.repaint();
					}
				} catch (Exception e) {
					Logger.getLogger(MSMExplorer.class.getName()).log(Level.SEVERE, null, e);
				}
			}
		});
		showColorChooser.setToolTipText("Open a dialog to select a new node color.");


	//private static final String[] PALETTE_LABELS = {"Interpolated", "Category", "Cool", 
	//	"Hot", "Grayscale", "HSB"};
		presetPalettes.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int[] palette;
				switch (presetPalettes.getSelectedIndex()) {
					case 0:
						palette = ColorLib.getInterpolatedPalette(((ColorSwatch)
							startColorButton.getIcon()).getColor().getRGB(), 
							((ColorSwatch)endColorButton.getIcon()).getColor().getRGB());
						break;
					case 1:
						palette = ColorLib.getCategoryPalette(12);
						break;
					case 2:
						palette = ColorLib.getCoolPalette();
						break;
					case 3:
						palette = ColorLib.getHotPalette();
						break;
					case 4:
						palette = ColorLib.getGrayscalePalette();
						break;
					case 5:
						palette = ColorLib.getHSBPalette();
						break;
					default:
						return;
				}
				nodeColorAction.setPalette(palette);
				m_vis.run("nodeFill");
				m_vis.repaint();
			}
		}); 

		JPanel gen_node = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		gen_node.setBorder(BorderFactory.createTitledBorder("Gen. Node Appearance"));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		gen_node.add(new JLabel("Node Size Range: "), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 4;
		c.gridx = 1;
		c.gridy = 0;
		gen_node.add(nodeSizeSlider, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.gridx = 2;
		c.gridy = 1;
		gen_node.add(showColorChooser, c);

		JPanel gen_nodeAction = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		gen_nodeAction.setBorder(BorderFactory.createTitledBorder("Node Data Actions"));
		c.gridx = 0;
		c.gridy = 0;
		gen_nodeAction.add(new JLabel("Node Size Field: "), c);
		c.gridwidth = 3;
		c.gridx = 1;
		gen_nodeAction.add(nodeSizeActionField, c);
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
		gen_nodeAction.add(new JLabel("Node Color Field"), c);
		c.gridx = 1;
		gen_nodeAction.add(nodeColorActionField, c);
		c.gridx = 2;
		gen_nodeAction.add(startColorButton, c);
		c.gridx = 3;
		gen_nodeAction.add(endColorButton, c);
		c.gridx = 4;
		gen_nodeAction.add(presetPalettes, c);

		gen_Panel.add(gen_node);
		gen_Panel.add(gen_nodeAction);

		main.add(gen_Panel);
		
		/* ----------------------- LABEL PANE ----------------------- */
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

		JButton lr_showLabelColorChooser = new JButton("Text Color"); 
		lr_showLabelColorChooser.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					FlexDataColorAction fill = (FlexDataColorAction)((ActionList)m_vis.getAction("animate")).get(2);
					Color newFill = JColorChooser.showDialog(f, "Choose Node Color", new Color(fill.getDefaultColor()));
					if (newFill != null) {
						m_vis.setValue(NODES, null, VisualItem.TEXTCOLOR, newFill.getRGB());
					}
				} catch (Exception e) {
					Logger.getLogger(MSMExplorer.class.getName()).log(Level.SEVERE, null, e);
				}
			}
		});
		lr_showLabelColorChooser.setToolTipText("Open a dialog to select a new label text color.");
		lr_Panel.add(lr_showLabelColorChooser);


		/* ------------------ SHAPE PANE --------------------- */
		JPanel sr_Panel = new JPanel(new GridLayout(0,2));
		pane.addTab("Shape Render", sr_Panel);
		
		final JComboBox shapeComboBox = new JComboBox(SHAPES_LABELS);
		shapeComboBox.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				m_vis.setValue(NODES, null, VisualItem.SHAPE, SHAPES[shapeComboBox.getSelectedIndex()]);
				m_vis.run("nodeFill");
				m_vis.repaint();
			}
		});

		
		final JComboBox shapeActionField = new JComboBox(fields);
		shapeActionField.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ActionList draw = (ActionList)m_vis.getAction("draw");
				DataShapeAction dataShapeAction = null;
				assert draw.size() > 1;
				if (draw.get(draw.size() - 1) instanceof DataShapeAction) { 
					dataShapeAction = (DataShapeAction)draw.get(draw.size() - 1);
				} else if (draw.get(draw.size() - 2) instanceof DataShapeAction) {
					dataShapeAction = (DataShapeAction)draw.get(draw.size() - 2);
				} 

				if (dataShapeAction != null) {
					dataShapeAction.setDataField((String)shapeActionField.getSelectedItem());
				} else {
					dataShapeAction = new DataShapeAction(NODES, (String)shapeActionField.getSelectedItem());
					dataShapeAction.setVisualization(m_vis);
					draw.add(dataShapeAction);
				}
				m_vis.run("draw");
				m_vis.repaint();
			}
		});
		
		sr_Panel.add(new JLabel("Node Shape: "));
		sr_Panel.add(shapeComboBox);
		sr_Panel.add(new JLabel("Node Shape Field:"));
		sr_Panel.add(shapeActionField);
		
		/* -------------------- EDGE PANE --------------------- */
		JPanel er_Panel = new JPanel(new GridLayout(0,2));
		pane.addTab("Edge Render", er_Panel);

		final JToggleButton showSelfEdges = new JToggleButton("Show Self Edges", m_er.getRenderSelfEdges());
		showSelfEdges.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				m_er.setRenderSelfEdges(showSelfEdges.isSelected());
				m_vis.run("draw");
				m_vis.run("animate");
			}
		});

		final Table et = ((Graph)m_vis.getGroup(GRAPH)).getEdgeTable();
		final Vector<String> etFields = new Vector<String>(5);
		for (int i = et.getColumnNumber(TPROB); i < et.getColumnCount(); ++i) {
			etFields.add(et.getColumnName(i));
		}
		final JComboBox edgeColorField = new JComboBox(etFields);
		edgeColorField.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ActionList animate = (ActionList)m_vis.getAction("animate");
				FlexDataColorAction edgeArrowColorAction = (FlexDataColorAction)animate.get(1);
				FlexDataColorAction edgeColorAction = (FlexDataColorAction)animate.get(2);
				int dataType = Constants.ORDINAL;
				String col = (String)edgeColorField.getSelectedItem();
				if (isNumerical(et.getColumn(col))) {
					dataType = Constants.NUMERICAL;
					edgeColorAction.setBinCount(10);
					edgeColorAction.setScale(Constants.QUANTILE_SCALE);
				}
				edgeColorAction.setDataField((String)edgeColorField.getSelectedItem());
				edgeColorAction.setDataType(dataType);
				edgeArrowColorAction.setDataField((String)edgeColorField.getSelectedItem());
				edgeArrowColorAction.setDataType(dataType);
				m_vis.run("animate");
				m_vis.repaint();
			}
		});

		er_Panel.add(edgeColorField);
		er_Panel.add(showSelfEdges);

		/* --------------------- AGG PANE ----------------------- */
		JPanel pr_Panel = new JPanel();
		pane.addTab("Aggregate Render", pr_Panel);

		main.add(pane);
		add(main);
		pack();
	}

	public void showDialog() {
		setLocationByPlatform(true);
		setVisible(true);
	}

	private boolean isNumerical(Column c) {
		Class<?> cls = c.getColumnType();
		if (cls == double.class || cls == int.class || cls == float.class ||
			cls == long.class) {
			return true;
		}
		return false;
	}
}
