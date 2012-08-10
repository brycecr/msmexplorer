package edu.stanford.folding.msmexplorer.util.ui;

import edu.stanford.folding.msmexplorer.MSMConstants;
import edu.stanford.folding.msmexplorer.MSMExplorer;
import edu.stanford.folding.msmexplorer.util.FlexDataColorAction;
import edu.stanford.folding.msmexplorer.util.render.SelfRefEdgeRenderer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
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
		startColorButton.addActionListener( new PaletteColorButtonActionListener(f, startColorButton,
			nodeColorAction, PaletteColorButtonActionListener.START, presetPalettes));

		final JButton endColorButton = new JButton("End Color", 
			new ColorSwatch(new Color(palette[0])));
		endColorButton.addActionListener( new PaletteColorButtonActionListener(f, endColorButton,
			nodeColorAction, PaletteColorButtonActionListener.END, presetPalettes));

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
		lr_Panel.setOpaque(false);


		/* ------------------ SHAPE PANE --------------------- */
		JPanel sr_Panel = new JPanel(new GridBagLayout());
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
		
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		sr_Panel.add(new JLabel("Node Shape: "), c);
		c.gridx = 2;
		sr_Panel.add(shapeComboBox, c);
		c.insets = new Insets(10,0,0,0);
		c.gridx = 0;
		c.gridy = 1;
		sr_Panel.add(new JLabel("Node Shape Field:"), c);
		c.gridx = 2;
		c.gridy = 1;
		sr_Panel.add(shapeActionField, c);
		sr_Panel.setOpaque(false);
		
		/* -------------------- EDGE PANE --------------------- */
		JPanel er_Panel = new JPanel(new GridBagLayout());
		pane.addTab("Edge Render", er_Panel);

		final JToggleButton showSelfEdges = new JToggleButton("Show Self Edges", m_er.getRenderSelfEdges());
		showSelfEdges.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				m_er.setRenderSelfEdges(showSelfEdges.isSelected());
				m_vis.run("draw");
				m_vis.run("animate");
			}
		});

		ActionList animate = (ActionList)m_vis.getAction("animate");
		final FlexDataColorAction edgeArrowColorAction = (FlexDataColorAction)animate.get(1);
		final FlexDataColorAction edgeColorAction = (FlexDataColorAction)animate.get(2);

		final Table et = ((Graph)m_vis.getGroup(GRAPH)).getEdgeTable();
		final Vector<String> etFields = new Vector<String>(5);
		final Vector<String> etNumFields = new Vector<String>(5);
		for (int i = et.getColumnNumber(TPROB); i < et.getColumnCount(); ++i) {
			if (isNumerical(et.getColumn(i))) {
				etNumFields.add(et.getColumnName(i));
			}
			etFields.add(et.getColumnName(i));
		}
		final JComboBox edgeColorField = new JComboBox(etFields);
		edgeColorField.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int dataType = Constants.ORDINAL;
				String col = (String)edgeColorField.getSelectedItem();
				if (isNumerical(et.getColumn(col))) {
					dataType = Constants.NUMERICAL;
					edgeColorAction.setBinCount(10);
					edgeColorAction.setScale(Constants.QUANTILE_SCALE);
				}
				edgeColorAction.setDataField(col);
				edgeColorAction.setDataType(dataType);
				edgeArrowColorAction.setDataField((String)edgeColorField.getSelectedItem());
				edgeArrowColorAction.setDataType(dataType);
				m_vis.run("animate");
				m_vis.repaint();
			}
		});

		final JComboBox edgePresetPalettes = new JComboBox(PALETTE_LABELS);
		
		int[] er_palette = edgeColorAction.getPalette();

		final JButton edgeStartColorButton = new JButton("Start Color", new
				ColorSwatch(new Color(er_palette[0])));
		edgeStartColorButton.addActionListener( new PaletteColorButtonActionListener(f, edgeStartColorButton,
			new ArrayList<FlexDataColorAction>() {{add(edgeColorAction); add(edgeArrowColorAction);}},
			PaletteColorButtonActionListener.START, edgePresetPalettes));

		final JButton edgeEndColorButton = new JButton("End Color", new
				ColorSwatch(new Color(er_palette[er_palette.length-1])));
		edgeEndColorButton.addActionListener( new PaletteColorButtonActionListener(f, edgeEndColorButton, 
			new ArrayList<FlexDataColorAction>() {{add(edgeColorAction); add(edgeArrowColorAction);}},
			PaletteColorButtonActionListener.END, edgePresetPalettes));

		edgePresetPalettes.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int[] palette;
				switch (edgePresetPalettes.getSelectedIndex()) {
					case 0:
						palette = ColorLib.getInterpolatedPalette(((ColorSwatch)
							edgeStartColorButton.getIcon()).getColor().getRGB(), 
							((ColorSwatch)edgeEndColorButton.getIcon()).getColor().getRGB());
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
				edgeColorAction.setPalette(palette);
				edgeArrowColorAction.setPalette(palette);
				m_vis.run("nodeFill");
				m_vis.repaint();
			}
		}); 

		final JRangeSlider edgeWeightSlider = new JRangeSlider(1, 80000, 1, 400, Constants.ORIENT_TOP_BOTTOM);
		edgeWeightSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				ActionList animate = (ActionList)m_vis.getAction("animate");
				DataSizeAction edgeWeightAction = (DataSizeAction)animate.get(0);
				JRangeSlider slider = (JRangeSlider) e.getSource();
				edgeWeightAction.setMinimumSize(slider.getLowValue()/4.0d);
				edgeWeightAction.setMaximumSize(slider.getHighValue()/4.0d);
				m_vis.run("animate");
				m_vis.repaint();
			}
		});
		edgeWeightSlider.setToolTipText("Set the range for edge thickness.");

		final JComboBox edgeWeightField = new JComboBox(etNumFields);
		edgeWeightField.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ActionList animate = (ActionList)m_vis.getAction("animate");
				DataSizeAction edgeWeightAction = (DataSizeAction)animate.get(0);
				edgeWeightAction.setDataField((String)edgeWeightField.getSelectedItem());
				edgeWeightAction.setMinimumSize(edgeWeightSlider.getLowValue());
				edgeWeightAction.setMaximumSize(edgeWeightSlider.getHighValue());
				m_vis.run("animate");
				m_vis.repaint();
			}
		});

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 0;
		er_Panel.add(showSelfEdges, c);
		c.insets = new Insets(10,0,0,0);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		er_Panel.add(new JLabel("Edge Color Field: "), c);
		c.gridx = 1;
		er_Panel.add(edgeColorField, c);
		c.gridx = 2;
		er_Panel.add(edgeStartColorButton, c);
		c.gridx = 3;
		er_Panel.add(edgeEndColorButton, c);
		c.gridx = 0;
		c.gridy = 2;
		er_Panel.add(new JLabel("Edge Weight Field: "), c);
		c.gridx = 1;
		c.gridy = 2;
		er_Panel.add(edgeWeightField, c);
		c.insets = new Insets(0,0,0,0);
		c.gridy = 3;
		c.gridx = 0;
		er_Panel.add(new JLabel("Weight Range: "), c);
		c.gridx = 1;
		c.gridwidth = 3;
		er_Panel.add(edgeWeightSlider, c);
		er_Panel.setOpaque(false);

		/* --------------------- AGG PANE ----------------------- */
		if (m_vis.getGroup(AGGR) != null) {
			assert m_pr != null;
			JPanel pr_Panel = new JPanel( new GridBagLayout());
			pane.addTab("Aggregate Render", pr_Panel);

			final JSpinner aggCurve = new JSpinner(new SpinnerNumberModel(m_pr.getCurveSlack(), 0.0d, 10.0d, .02d));
			aggCurve.addChangeListener( new ChangeListener() {
				public void stateChanged(ChangeEvent ce) {
					m_pr.setCurveSlack(((Double)aggCurve.getValue()).floatValue());
					m_vis.run("aggLayout");
					m_vis.repaint();
				}
			});
			aggCurve.setPreferredSize(new Dimension(100, 30));


			final JComboBox pr_presetPalettes = new JComboBox(PALETTE_LABELS);
			ActionList draw = (ActionList)m_vis.getAction("draw");
			final FlexDataColorAction aggrColorAction;
			if (draw.get(draw.size() - 1) instanceof FlexDataColorAction) {
				aggrColorAction = (FlexDataColorAction)draw.get(draw.size() - 1);
			} else if (draw.get(draw.size() - 2) instanceof FlexDataColorAction) {
				aggrColorAction = (FlexDataColorAction)draw.get(draw.size() - 2);
			} else {
				assert 1 == 0;
				aggrColorAction = null;
			}

			int[] aggrPalette = aggrColorAction.getPalette();

			final JButton aggrStartColorButton = new JButton("Start Color", new ColorSwatch(new Color(aggrPalette[0])));
			aggrStartColorButton.addActionListener( new PaletteColorButtonActionListener(f, aggrStartColorButton,
				aggrColorAction, PaletteColorButtonActionListener.START, pr_presetPalettes));

			final JButton aggrEndColorButton = new JButton("End Color", new ColorSwatch(new Color(aggrPalette[aggrPalette.length-1])));
			aggrEndColorButton.addActionListener( new PaletteColorButtonActionListener(f, aggrEndColorButton,
				aggrColorAction, PaletteColorButtonActionListener.END, pr_presetPalettes));

			pr_presetPalettes.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					int[] palette;
					switch (pr_presetPalettes.getSelectedIndex()) {
						case 0:
							Color start = ((ColorSwatch)aggrStartColorButton.getIcon()).getColor();
							start = new Color(start.getRed(), start.getGreen(), start.getBlue(), 128);
							Color end = ((ColorSwatch)aggrEndColorButton.getIcon()).getColor();
							end = new Color(end.getRed(), end.getGreen(), end.getBlue(), 128);
							palette = ColorLib.getInterpolatedPalette(start.getRGB(), end.getRGB());
							break;
						case 1:
							palette = ColorLib.getCategoryPalette(50, 0.95f, .15f, .9f, .5f);
							break;
						case 2:
							palette = ColorLib.getCoolPalette();
							for (int i = 0; i < palette.length; ++i) {
								palette[i] = (palette[i] | 0x80000000) & 0x8000ffff;
							}
							break;
						case 3:
							palette = ColorLib.getHotPalette();
							for (int i = 0; i < palette.length; ++i) {
								palette[i] = (palette[i] | 0x80000000) & 0x8000ffff;
							}
							break;
						case 4:
							palette = ColorLib.getGrayscalePalette();
							for (int i = 0; i < palette.length; ++i) {
								palette[i] = (palette[i] | 0x80000000) & 0x8000ffff;
							}
							break;
						case 5:
							palette = ColorLib.getHSBPalette();
							for (int i = 0; i < palette.length; ++i) {
								palette[i] = (palette[i] | 0x80000000) & 0x8000ffff;
							}
							break;
						default:
							return;
					}
					aggrColorAction.setPalette(palette);
					m_vis.run("draw");
					m_vis.repaint();
				}
			});

			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 1;
			pr_Panel.add(new JLabel("Aggregate Curve Slack: "), c);
			c.gridx = 1;
			pr_Panel.add(aggCurve, c);
			c.insets = new Insets(10,0,0,0);
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 1;
			pr_Panel.add(aggrStartColorButton, c);
			c.gridx = 1;
			pr_Panel.add(aggrEndColorButton, c);
			c.gridx = 2;
			pr_Panel.add(pr_presetPalettes, c);
			pr_Panel.setOpaque(false);
		}




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

	/**
	 * An ActionListener that backs buttons that select the start
	 * and end colors for palettes for FlexDataColorActions.
	 */
	private class PaletteColorButtonActionListener implements ActionListener {

		//Constants denoting which end of the palette this button sets
		//the start or the beginning...
		public static final int START = 0;
		public static final int END = 0;

		private int m_end;
		private Frame m_frame;
		private JButton m_colorButton;
		private FlexDataColorAction[] m_actions;
		private JComboBox m_presetPalette;

		public PaletteColorButtonActionListener(Frame f, JButton colorButton, final FlexDataColorAction action, int end, JComboBox presetPalette) {
			this(f, colorButton, new ArrayList<FlexDataColorAction>() {{add(action);}}, end, presetPalette); 
		}
		
		/**
		 * Initializes ActionListener with gui elements, actions, and an end of the spectrum.
		 * 
		 * @param f parent component (can be NULL)
		 * @param colorButton the button this action is being assigned to. If button has ColorSwatch object as its icon, it will be updated on color change
		 * @param actions an ArrayList of all the actions to set the new palette for. All should use identical palettes.
		 * @param end which end of the spectrum does this button set? Values must come from static constants in this class
		 * @param presetPalette JComboBox that has palette presets so that adjusting the color on this button sets to "Interpolated" (can be NULL)
		 */
		public PaletteColorButtonActionListener(Frame f, JButton colorButton, ArrayList<FlexDataColorAction> actions, int end, JComboBox presetPalette) {
			//Can't construct if any of these are the case...
			if (colorButton == null || actions == null || actions.isEmpty() || (end != START && end != END)) {
				Logger.getLogger(MSMExplorer.class.getName()).log(Level.SEVERE, null, new Exception("Bad ColorButtonAction params"));
				return;
			}
			m_frame = f;
			m_colorButton = colorButton;
			m_actions = Arrays.copyOf(actions.toArray(), actions.size(), FlexDataColorAction[].class);
			m_presetPalette = presetPalette;
		}

		/**
		 * Implements actionPerformed. Opens a color chooser dialog
		 * to select a new color for the actions in m_actions.
		 * Assigns that action to all the actions in m_actions
		 * at the end specified by m_end and updates gui components
		 * button color and presetPalette to reflect the change.
		 * 
		 * @param ae ActionEvent that initiated this call
		 */
		public void actionPerformed(ActionEvent ae) {
			try {
				Color newColor = JColorChooser.showDialog(m_frame, "Choose Node Color",
					new Color(m_actions[0].getDefaultColor()));
				if (newColor != null) {
					int[] oldPalette = m_actions[0].getPalette();
					int[] newPalette;
					if (m_end == START) {
						newPalette = ColorLib.getInterpolatedPalette(newColor.getRGB(), oldPalette[oldPalette.length-1]);
					} else if (m_end == END) {
						newPalette = ColorLib.getInterpolatedPalette(oldPalette[0], newColor.getRGB());
					} else {
						assert 5 == 3;
						return;
					}
					for (int i = 0; i < m_actions.length; ++i) {
						m_actions[i].setPalette(newPalette);
					}
					Icon icn = m_colorButton.getIcon();
					if (icn instanceof ColorSwatch) {
						((ColorSwatch)icn).setColor(newColor);
					}
					if (m_presetPalette != null) {
						m_presetPalette.setSelectedItem("Interpolated");
					}
				}
			} catch (Exception e) {
				Logger.getLogger(MSMExplorer.class.getName()).log(Level.SEVERE, null, e);
			}
		}
	}
}
