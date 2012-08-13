package edu.stanford.folding.msmexplorer;

import edu.stanford.folding.msmexplorer.io.ColumnChooserDialog;
import edu.stanford.folding.msmexplorer.io.ExportMSMImageAction;
import edu.stanford.folding.msmexplorer.io.MSMIOLib;
import edu.stanford.folding.msmexplorer.io.hierarchy.HierarchyBundle;
import edu.stanford.folding.msmexplorer.tpt.TPTSetupBox;
import edu.stanford.folding.msmexplorer.tpt.TPTWindow;
import edu.stanford.folding.msmexplorer.util.FlexDataColorAction;
import edu.stanford.folding.msmexplorer.util.MutableDouble;
import edu.stanford.folding.msmexplorer.util.aggregate.AggregateDragControl;
import edu.stanford.folding.msmexplorer.util.aggregate.AggregateLayout;
import edu.stanford.folding.msmexplorer.util.aggregate.AggregatePrioritySorter;
import edu.stanford.folding.msmexplorer.util.movie.*;
import edu.stanford.folding.msmexplorer.util.render.ImageToggleLabelRenderer;
import edu.stanford.folding.msmexplorer.util.render.SelfRefEdgeRenderer;
import edu.stanford.folding.msmexplorer.util.stats.GraphStatsManager;
import edu.stanford.folding.msmexplorer.util.stats.GraphStatsWindow;
import edu.stanford.folding.msmexplorer.util.axis.AxisLabelLabelLayout;
import edu.stanford.folding.msmexplorer.util.axis.AxisRotateRenderer;
import edu.stanford.folding.msmexplorer.util.axis.AxisSettingsDialog;
import edu.stanford.folding.msmexplorer.util.ui.FocusControlWithDeselect;
import edu.stanford.folding.msmexplorer.util.ui.JValueSliderFlammable;
import edu.stanford.folding.msmexplorer.util.ui.Picture;
import edu.stanford.folding.msmexplorer.util.ui.VisualizationSettingsDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.AxisLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.CascadedTable;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.expression.OrPredicate;
import prefuse.data.io.GraphMLReader;
import prefuse.data.query.NumberRangeModel;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.search.KeywordSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.DefaultTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.NamedColumnProjection;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.GraphicsLib;
import prefuse.util.PrefuseLib;
import prefuse.util.StrokeLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.force.Force;
import prefuse.util.force.ForceSimulator;
import prefuse.util.ui.JForcePanel;
import prefuse.util.ui.JPrefuseTable;
import prefuse.util.ui.JSearchPanel;
import prefuse.util.ui.JValueSlider;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.expression.VisiblePredicate;

/**
 * Class to execute MSMExplorer, a visualization module for
 * protein folding Markov State Models generated with
 * MSMBuilder.
 *
 * Built on GraphView framework by Jeffrey Heer.
 *
 * @author Bryce Cronkite-Ratcliff, brycecr@stanford.edu
 */
public class MSMExplorer extends JPanel implements MSMConstants {

	// Thresholds for detecting "large" graphs to change some behavior
	private static final int SIZE_THRESHOLD = 250; //Threshold for "big" graph behavior
	private static final int DEGREE_THRESHOLD = 60; //Degree threshold for "big" graph


	//Current Version #
	private static final String version = "v0.04";

	// The Visualization object.
	private final Visualization m_vis = new Visualization();

	// The GraphView frame
	private JFrame frame; //Graph view frame
	
	// where to look for images by default
	private String imageLocation = "'./lib/images'";

	// Hierarchy objects. We keep these as instance variables
	// so that we can pass them to new MSMExplorer objects, because
	// we currently do basic graph switching and reseting by making
	// new objects. This could probably be done better, but it seems to 
	// mostly serve the purpose for now (no noticeable performance loss)
	private HierarchyBundle hierarchy = null; // holds the hierarchy data
	private JPanel harchPanel = null; // panel with hierarchy gui elements

	private boolean autoRange = true; //whether to set axis ranges automatically

	/**
	 * Initial program constructor; creates the initial splash screen
	 * and all that good stuff.
	 */
	public MSMExplorer() {

		try {
			// Set System L&F
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Exception e) {
			// handle exception
		}

		//Selector window
		final JFrame selector = new JFrame("W e l c o m e  |  M S M E x p l o r e r");
		selector.setLayout(new BorderLayout());
		selector.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Enter graph view
		JButton graphButton = new JButton("Graph View");
		graphButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				selector.dispose();
				graphView("./lib/5macro.xml", "label"); //HERE
			}
		});
		graphButton.setToolTipText("Visualize a Markov State Model");
		graphButton.setSelected(true);
		graphButton.setMnemonic(KeyEvent.VK_ENTER);

		//Perform TPT without proceeding through Graph View
		JButton tptButton = new JButton("Just TPT");
		tptButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				preemptiveTPT(selector);
			}
		});
		tptButton.setToolTipText("Run Transition Path Theory without visualizing the whole graph first.");

		Box container = new Box(BoxLayout.X_AXIS);
		container.add(Box.createHorizontalStrut(130));
		container.add(graphButton);
		container.add(tptButton);
		container.add(Box.createHorizontalGlue());

		JLabel versionLabel = new JLabel(version + "     ");
		versionLabel.setFont(FontLib.getFont("Tahoma", 10));
		container.add(versionLabel);
		container.setBackground(Color.WHITE);

		// Amusing spalsh image
		BufferedImage image;
		try {
			image = ImageIO.read(new File("./lib/splash.jpg"));
		} catch (IOException ex) {
			Logger.getLogger(MSMExplorer.class.getName()).log(Level.WARNING, null, ex);
			image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}
		ImageIcon splash = new ImageIcon(image);
		selector.add(new JLabel(splash), BorderLayout.CENTER);
		selector.add(container, BorderLayout.SOUTH);
		selector.setBackground(Color.WHITE);
		selector.pack();
		selector.setSize(500, 380);
		selector.setLocationRelativeTo(null);
		selector.setVisible(true);
	}

	/**
	 * Constructor, initializes controls and gui elements.
	 *
	 * @param g graph to visualize
	 * @param label field in graph for node labels. Usually "label"
	 */
	public MSMExplorer(final Graph g, String label) {
		super(new BorderLayout());

		// boolean used to sacrifice fancy functionality for speed
		// wheen dealing with big graphs.
		boolean isBigGraph = (g.getNodeCount() > SIZE_THRESHOLD);

		// create a new, empty visualization for our data

		final ImageToggleLabelRenderer tr = new ImageToggleLabelRenderer();
		tr.setVerticalAlignment(Constants.CENTER);
		tr.setRoundedCorner(8, 8);
		m_vis.setRendererFactory(new DefaultRendererFactory(tr, new SelfRefEdgeRenderer()));

		final ShapeRenderer sr = new ShapeRenderer();


		// --------------------------------------------------------------------
		// register the data with a visualization

		// adds graph to visualization and sets renderer label field
		setGraph(g, label);

		// set up Focus behavior
		final TupleSet focusGroup = m_vis.getGroup(Visualization.FOCUS_ITEMS);
		focusGroup.addTupleSetListener(new TupleSetListener() {

			public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
				//We backpedal if we're trying to focus to a 
				//aggregate item because we don't want to loose
				//focus on a node (if we do, this causes weird
				//behavior with the distance slider)
				for (int i = 0; i < add.length; ++i) {
					if (add[i] instanceof AggregateItem) {
						ts.removeTuple(add[i]);
						for (int j = 0; j < rem.length; ++j) {
							ts.addTuple(rem[j]);
						}
						return;
					}
				}
				for (int i = 0; i < rem.length; ++i) {
					if (rem[i] instanceof AggregateItem) {
						return;
					}
				}
				for (int i = 0; i < rem.length; ++i) {
					((VisualItem) rem[i]).setFixed(false);
					Iterator focusEdges = ((Node) rem[i]).edges();
					while (focusEdges.hasNext()) {
						VisualItem currEdge = (VisualItem) focusEdges.next();
						currEdge.setHover(false);
					}
				}
				for (int i = 0; i < add.length; ++i) {
					((VisualItem) add[i]).setFixed(false);
					if (add[i] instanceof Node) {
						Iterator focusEdges = ((Node) add[i]).edges();
						while (focusEdges.hasNext()) {
							VisualItem currEdge = (VisualItem) focusEdges.next();
							currEdge.setHover(false);
						}
						((VisualItem) add[i]).setFixed(true);
						Iterator focusEdges2 = ((Node) add[i]).edges();
						while (focusEdges2.hasNext()) {
							VisualItem currEdge = (VisualItem) focusEdges2.next();
							currEdge.setHover(true);
						}
					}
				}
				if (ts.getTupleCount() == 0) {
					ts.addTuple(rem[0]);
					((VisualItem) rem[0]).setFixed(false);
					Iterator focusEdges2 = ((Node) rem[0]).edges();
					while (focusEdges2.hasNext()) {
						VisualItem currEdge = (VisualItem) focusEdges2.next();
						currEdge.setHover(false);
					}
				}
				if (ts.getTupleCount() > 1) {
					Iterator tuples = ts.tuples();

					//TODO: enable selection of sets of points for target/source
					DefaultTupleSet A = new DefaultTupleSet();
					A.addTuple((Tuple) tuples.next());

					DefaultTupleSet B = new DefaultTupleSet();
					B.addTuple((Tuple) tuples.next());

					TPTWindow tptw = new TPTWindow(g, A, B);

					focusGroup.clear();
				}

				m_vis.run("draw");
			}
		});

		initGraph(g, m_vis);

		// --------------------------------------------------------------------
		// set up a display to show the visualization

		final Display display = new Display(m_vis);
		display.setSize(1200, 900);
		display.pan(350, 350); // start centered
		display.setForeground(Color.GRAY);
		display.setBackground(Color.WHITE);
		display.setHighQuality(true); //Default to High Quality

		// main display controls
		display.addControlListener(new FocusControlWithDeselect(1));
		display.addControlListener(new PanControl());
		display.addControlListener(new AggregateDragControl("aggLayout"));
		display.addControlListener(new ZoomControl());
		display.addControlListener(new WheelZoomControl());
		display.addControlListener(new ZoomToFitControl());
		display.addControlListener(new NeighborHighlightControl());
		display.setItemSorter(new AggregatePrioritySorter());

		// Main control panel
		JPanel fpanel = new JPanel();
		fpanel.setLayout(new BoxLayout(fpanel, BoxLayout.Y_AXIS));
		fpanel.setBackground(Color.WHITE);
		fpanel.setToolTipText("Collapse this panel with the small arrows on the divider.");

		//we used absolute (i.e. null) layout so we could get the hierarchy sliders
		//to overlay on the visualization; thus, because no layout is doing it for
		// us, we have to handle resizing of the main visualization.
		this.addComponentListener(new ComponentListener() {

			public void componentResized(ComponentEvent ce) {
				display.setBounds(0, 0, MSMExplorer.this.getWidth(), MSMExplorer.this.getHeight());
			}

			public void componentMoved(ComponentEvent ce) {
				/*empty*/
			}

			public void componentShown(ComponentEvent ce) {
				/*empty*/
			}

			public void componentHidden(ComponentEvent ce) {
				/*empty*/
			}
		});


		//the 'Flammable' lets other code fire without a value change.
		//note that currently this orchestrates the behavior of both filters
		final JValueSliderFlammable eqProbSlider = new JValueSliderFlammable("EqProb Cutoff", 0., 1., 0.);
		eqProbSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent ce) {
				//This disgusting bit of code is all to force execution of the graph
				//distance filter before filtering on eqProb. It should be changed
				//probably by building a new class that takes flexible predicates
				//and actually works.
				((GraphDistanceFilter) ((ActionList) m_vis.getAction("draw")).get(0)).run(10);
				try {
					Thread.sleep(10); //Gives Draw enough time to finish
				} catch (InterruptedException ex) {
					Logger.getLogger(MSMExplorer.class.getName()).log(Level.SEVERE, null, ex);
				}
				double val = eqProbSlider.getValue().doubleValue();

				Iterator itr = m_vis.visibleItems(NODES);
				while (itr.hasNext()) {
					VisualItem i = (VisualItem) itr.next();
					Tuple n = m_vis.getSourceTuple(i);
					if (n.getDouble(EQPROB) < val) {
						PrefuseLib.updateVisible(i, false);
						Iterator edgeItr = ((NodeItem) i).edges();
						while (edgeItr.hasNext()) {
							((VisualItem) edgeItr.next()).setVisible(false);
						}

					} else {
						PrefuseLib.updateVisible(i, true);
						EdgeItem ei;

						Iterator edgeItr = ((NodeItem) i).outEdges();
						while (edgeItr.hasNext()) {
							if ((ei = (EdgeItem) edgeItr.next()).getTargetItem().isVisible()) {
								ei.setVisible(true);
							}
						}

						edgeItr = ((NodeItem) i).inEdges();
						while (edgeItr.hasNext()) {
							if ((ei = (EdgeItem) edgeItr.next()).getSourceItem().isVisible()) {
								ei.setVisible(true);
							}
						}
					}
				}
			}
		});
		eqProbSlider.setLayout(new GridLayout(0, 2));
		String eqProbSliderToolTip = "Only show states above the equilibrium probability "
			+"shown on this slider.";
		Component[] esjc = eqProbSlider.getComponents();
		for (int i = 0; i < esjc.length; ++i) {
			((JComponent)esjc[i]).setToolTipText(eqProbSliderToolTip);
		}
		eqProbSlider.setOpaque(false);

		final JTextField eqProbText = new JTextField("EqProb Thresh");
		eqProbText.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				double d = Double.parseDouble(((JTextField) ae.getSource()).getText());
				eqProbSlider.setValue(d);
			}
		});
		eqProbSlider.add(eqProbText);
		eqProbText.setToolTipText("Only show states above the equilibrium probability "
			+"shown in this field");

		// Graph Distance slider
		final JValueSlider distSlider = new JValueSlider("Distance", 0, 40, 37);
		distSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				((GraphDistanceFilter) ((ActionList) m_vis.getAction("draw")).get(0)).setDistance(distSlider.getValue().intValue());
				eqProbSlider.fire();
				m_vis.run("aggLayout");
			}
		});
		distSlider.setBackground(Color.WHITE);
		distSlider.setPreferredSize(new Dimension(300, 30));
		distSlider.setMaximumSize(new Dimension(300, 30));
		String distSliderToolTip = "<html>Only show states within the number of hops "
			+ "<br>indicated on this slider from the currently selected state.</html>";
		Component[] dsjc = distSlider.getComponents();
		for (int i = 0; i < dsjc.length; ++i) {
			((JComponent)dsjc[i]).setToolTipText(distSliderToolTip);
		}

		Box eqBox = new Box(BoxLayout.Y_AXIS);
		eqBox.add(eqProbSlider);
		eqBox.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		Box cf = new Box(BoxLayout.Y_AXIS);
		cf.add(eqBox);
		cf.add(distSlider);
		cf.setBorder(BorderFactory.createTitledBorder("Connectivity Filter"));
		fpanel.add(cf);

		// Toggle Picture mode (high quality toggle)
		// TODO: doesn't correctly respond to ctrl-H
		JToggleButton togglePM = new JToggleButton("Picture Mode", false);
		togglePM.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				display.setHighQuality(((JToggleButton) ae.getSource()).isSelected());
			}
		});
		togglePM.setSelected(true);
		togglePM.setToolTipText("<html>Toggle between aliased (toggled off) and anti-aliased (toggled on) rendering. "
		+"<br>On looks much better but will slow down graph interactivity.</html>");

		// Toggle between curve and straight lines
		JToggleButton curveBtn = new JToggleButton("Edge Type", false);
		curveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (((JToggleButton) ae.getSource()).isSelected()) {
					((EdgeRenderer) ((DefaultRendererFactory) m_vis.getRendererFactory()).getDefaultEdgeRenderer()).setEdgeType(Constants.EDGE_TYPE_CURVE);
				} else {
					((EdgeRenderer) ((DefaultRendererFactory) m_vis.getRendererFactory()).getDefaultEdgeRenderer()).setEdgeType(Constants.EDGE_TYPE_LINE);
				}

				m_vis.run("draw");
			}
		});
		curveBtn.setToolTipText("<html>Alternate between straight edges and curved edges. "
			+"<br>Straight is more space effecient, but curved allows one to see the"
			+ "<br> different colors (i.e. transition probabilities) of the incoming"
			+ "<br> and outgoing edges, and curved gives a different look.</html>");

		Box rendBox = new Box(BoxLayout.X_AXIS);
		rendBox.setBorder(BorderFactory.createTitledBorder("Gen. Renderer"));
		rendBox.add(togglePM);
		rendBox.add(curveBtn);
		fpanel.add(rendBox);

		// Button group to select which node Renderer to use
		// (circle or rounded-rectangle label)
		ButtonGroup nodeRenderers = new ButtonGroup();

		m_vis.setValue(NODES, null, VisualItem.SHAPE, Constants.SHAPE_ELLIPSE);
		JRadioButton circleRB = new JRadioButton("Shape", false);
		circleRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JRadioButton circleRB = (JRadioButton) ae.getSource();
				if (circleRB.isSelected()) {
					DefaultRendererFactory drf = (DefaultRendererFactory)m_vis.getRendererFactory();
					drf.setDefaultRenderer(sr);
					m_vis.run("draw");
				}
			}
		});
		circleRB.setToolTipText("Render nodes as circles without labels.");
		circleRB.setOpaque(false);
		nodeRenderers.add(circleRB);

		JRadioButton labelRB = new JRadioButton("Label", false);
		labelRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JRadioButton labelRB = (JRadioButton) ae.getSource();
				if (labelRB.isSelected()) {
					DefaultRendererFactory drf = (DefaultRendererFactory)m_vis.getRendererFactory();
					drf.setDefaultRenderer(tr);
					m_vis.run("draw");
				}
			}
		});
		labelRB.setToolTipText("Render nodes as rounded rectangles with labels");
		circleRB.setOpaque(false);
		nodeRenderers.add(labelRB);

		Box nrBox = new Box(BoxLayout.X_AXIS);
		nrBox.setBorder(BorderFactory.createTitledBorder("Node Renderer"));
		nrBox.add(circleRB);
		nrBox.add(labelRB);
		labelRB.setSelected(true);

		fpanel.add(nrBox);

		final JButton pause = new JButton("Stop Layout");
		pause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Action layoutAction = m_vis.getAction("lll");
				layoutAction.cancel();
			}
		});
		pause.setToolTipText("<html>Pause the layout animation. "
			+"<br>Useful for speeding up interactivity, or positioning"
			+ "<br> nodes by hand.</html>");

		JButton start = new JButton("Run Layout");
		start.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (!m_vis.getAction("lll").isRunning()) {
					ActionList layoutAction = (ActionList) m_vis.getAction("lll");
					layoutAction.run();
				}
			}
		});
		start.setToolTipText("<html>Run the physics-based layout animation, which"
			+ "<br>attempts to automatically layout the graph in a useful manner"
			+ "<br>by finding the local energy minima for graph layout, where"
			+ "<br>edges are springs and nodes are masses. Note that for \"large\""
			+ "<br>graphs this will cause the layout to run once in the background"
			+ "<br>instead of constant animation.</html>");

		// Run or stop layout
		Box runControls = new Box(BoxLayout.X_AXIS);
		runControls.add(start);
		runControls.add(pause);
		runControls.setBorder(
			BorderFactory.createTitledBorder("Run Control"));
		fpanel.add(runControls);

		// Open full-size image of selected node
		JButton openImg = new JButton("Open Image");
		openImg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Tuple focus = (Tuple) m_vis.getGroup(Visualization.FOCUS_ITEMS).tuples().next();
				Picture imgFrame = new Picture((String) focus.get("image"));
				imgFrame.show();
			}
		});
		openImg.setToolTipText("<html>Open a window with a full-size rendering of the image"
			+ "<br>corresponding to this node. Only works correctly if the location"
			+ "<br>of that image has been correctly specified.</html>");

		//Save raster image file
		JButton exportDisplay = new JButton("Save Image");
		exportDisplay.addActionListener(new ExportMSMImageAction(display));
		exportDisplay.setToolTipText("<html>Save the current visualization as an"
			+ "<br>image file. Vector (svg) and various raster formats available.</html>");

		Box imgControls = new Box(BoxLayout.X_AXIS);
		imgControls.setBorder(BorderFactory.createTitledBorder("Image Controls"));
		imgControls.add(openImg);
		imgControls.add(exportDisplay);
		fpanel.add(imgControls);

		//Show images on node
		JToggleButton togglePics = new JToggleButton("Show Images", false);
		togglePics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JToggleButton tp = (JToggleButton) ae.getSource();
				if (!tp.isSelected()) {
					tr.setImageField(null);
					((DataSizeAction) m_vis.getAction("nodeSize")).setMaximumSize(50.0);
					m_vis.run("nodeSize");
				} else {
					DataSizeAction nodeSize = (DataSizeAction)m_vis.getAction("nodeSize");
					tr.setImageField("image");
					nodeSize.setMaximumSize(1.0);
					nodeSize.setMinimumSize(1.0);
					tr.getImageFactory().setMaxImageDimensions(150, 150);
					m_vis.run("nodeSize");
				}
				m_vis.run("draw");
			}
		});
		togglePics.setToolTipText("<html>Show images on top of nodes. Only works if the location and "
			+ "<br>format of the images is correctly specified. If some nodes do not have corresponding"
			+ "<br>images, they will apppear without them.</html>");

		// Open selector window to select start and end states for TPT
		JButton runTPT = new JButton("TPT Selector");
		runTPT.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				TPTSetupBox tsb = new TPTSetupBox(g, true, m_vis);
			}
		});
		runTPT.setToolTipText("<html>Open a dialog to select the start and end sets for"
			+ "<br>transition path theory calculations. Note that TPT between two nodes"
			+ "<br>can also be initiated by selecting the start node and then ctrl-clicking"
			+ "<br>on the destination node. Currently, only source & target sets of size"
			+ "<br>one are fully supported.</html>");

		
		Box renderControls = new Box(BoxLayout.X_AXIS);
		renderControls.setBorder(BorderFactory.createTitledBorder("Function Control"));
		renderControls.add(togglePics);
		renderControls.add(runTPT);
		fpanel.add(renderControls);

		/* ------------- SEARCH GUI ELEMENTS ------------------- */
		// Set up search Panel
		SearchTupleSet searchGroup = new KeywordSearchTupleSet();
		m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, searchGroup);
		searchGroup.addTupleSetListener(new TupleSetListener() {

			public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
				if (add.length > 0) {
					Point2D p = new Point2D.Double();
					p.setLocation(((VisualItem) add[0]).getX(), ((VisualItem) add[0]).getY());
					display.animatePanToAbs(p, 1000);
				}
			}
		});

		SearchQueryBinding sq = new SearchQueryBinding((Table) m_vis.getGroup(NODES), "label",
			(SearchTupleSet) m_vis.getGroup(Visualization.SEARCH_ITEMS));

		final JSearchPanel search = sq.createSearchPanel(false);
		search.setShowResultCount(true);
		search.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 0));
		search.setToolTipText("<html>Search for a node by label. "
			+ "<br>If found, focus the display on that node."
			+ "<br>Note that the found node is not"
			+ "<br>automatically selected.</html>");

		Box searchBox = new Box(BoxLayout.X_AXIS);
		searchBox.setBorder(BorderFactory.createTitledBorder("Search"));
		searchBox.add(search);
		fpanel.add(searchBox);
		/* ------------- END SEARCH GUI ELEMENTS ------------------- */

		/* -------------- AXIS GUI ELEMENTS ------------------------ */
		Table nt = g.getNodeTable();
		int numCols = nt.getColumnCount();
		final Vector<String> axisFields = new Vector<String>(numCols);
		for (int i = 0; i < numCols; ++i) {
			axisFields.add(nt.getColumnName(i));
		}
		axisFields.add ("Load new...");
		axisFields.add ("No Axis");

		final JComboBox yAxisSelector = new JComboBox(axisFields);
		final JComboBox xAxisSelector = new JComboBox(axisFields);

		xAxisSelector.setSelectedIndex(0);
		xAxisSelector.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String selected = (String)xAxisSelector.getSelectedItem();
				if (selected.equals("Load new...")) {
					ColumnChooserDialog ccf = new ColumnChooserDialog(frame, m_vis, NODES);
					String name = ccf.showDialog();
					if (name != null && !axisFields.contains(name)) {
						axisFields.insertElementAt(name, axisFields.size()-2);
						xAxisSelector.setModel(new DefaultComboBoxModel(axisFields));
						yAxisSelector.setModel(new DefaultComboBoxModel(axisFields));
					} else {
						xAxisSelector.setSelectedIndex(0);
					}
				} else if (!axisFields.contains(selected)) {
					JOptionPane.showMessageDialog(MSMExplorer.this, 
						"JComboBox got out of sync with itself..."
						+ "recommend restart.", 
						"Axis Selection Error", 
						JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		String xAxisToolTip = "<html>Select the node data field to use "
			+ "<br>to layout the nodes along the X axis."
			+ "<br>Toggle SHOW AXIS to apply changes to this field/menu.</html>";
		xAxisSelector.setToolTipText(xAxisToolTip);

		yAxisSelector.setSelectedIndex(0);
		yAxisSelector.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String selected = (String)yAxisSelector.getSelectedItem();
				if (selected.equals("Load new...")) {
					ColumnChooserDialog ccf = new ColumnChooserDialog(frame, m_vis, NODES);
					String name = ccf.showDialog();
					if (name != null) {
						axisFields.insertElementAt(name, axisFields.size()-2);
						yAxisSelector.setModel(new DefaultComboBoxModel(axisFields));
						xAxisSelector.setModel(new DefaultComboBoxModel(axisFields));
					} else {
						yAxisSelector.setSelectedIndex(0); //so we don't try to graph against "Load new"
					}
				} else if (!axisFields.contains(selected)) {
					JOptionPane.showMessageDialog(MSMExplorer.this, 
						"JComboBox got out of sync with itself..."
						+ "recommend restart.", 
						"Axis Selection Error", 
						JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		String yAxisToolTip = "<html>Select the node data field to use "
			+ "<br>to layout the nodes along the Y axis."
			+ "<br>Toggle SHOW AXIS to apply changes to this field/menu.</html>";
		yAxisSelector.setToolTipText(yAxisToolTip);

		final NumberRangeModel xAxisRange = new NumberRangeModel(0, 1, 0, 1);
		final NumberRangeModel yAxisRange = new NumberRangeModel(0, 1, 0, 1);
		final JLabel xAxisLabel = new JLabel();
		final JLabel yAxisLabel = new JLabel();
		final JLabel axisGridLabel = new JLabel();
		//XXX maybe chage default spacing to something more intelligent?
		final MutableDouble xSpacing = new MutableDouble(50.0);
		final MutableDouble ySpacing = new MutableDouble(50.0);

		JButton openAxisSettings = new JButton ("Axis Settings");
		openAxisSettings.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				Table nt = g.getNodeTable();
				AxisSettingsDialog asd = new AxisSettingsDialog(frame, 
					xAxisRange, yAxisRange, 
					nt.getColumnType((String)xAxisSelector.getSelectedItem()), 
					nt.getColumnType((String)yAxisSelector.getSelectedItem()), 
					autoRange, xAxisLabel, yAxisLabel, axisGridLabel,
					xSpacing, ySpacing);
				autoRange = asd.showDialog();
			}
		});
		openAxisSettings.setToolTipText("<html>Open a dialog to modify axis layout parameters."
			+ "<br>Currently supports changing the axis layout range for "
			+ "<br>numerical data fields. Toggle SHOW AXIS to apply changes.</html>");

		final JToggleButton axisToggle = new JToggleButton("Show Axis", false);
		axisToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (axisToggle.isSelected()) {
					m_vis.removeAction("axes");
					m_vis.cancel("lll");
					
					((DefaultRendererFactory) m_vis.getRendererFactory()).add(
						new OrPredicate(new InGroupPredicate("xlabels"),
							new InGroupPredicate("ylabels")),
						new AxisRotateRenderer(Constants.FAR_LEFT, Constants.FAR_BOTTOM));
					
					Rectangle2D bounds = display.getItemBounds();
					AxisLayout xaxis = new AxisLayout(NODES, (String)xAxisSelector.getSelectedItem(), Constants.X_AXIS, VisiblePredicate.TRUE);
					xaxis.setLayoutBounds(bounds);
					AxisLayout yaxis = new AxisLayout(NODES, (String)yAxisSelector.getSelectedItem(), Constants.Y_AXIS, VisiblePredicate.TRUE);
					yaxis.setLayoutBounds(bounds);
					
					//Apply custom numerical range, if user asked for it
					//and the axis in question is in fact numerical
					if (!autoRange) {
						if (xaxis.getDataType() == Constants.NUMERICAL
							|| isNumerical(xAxisSelector)) {
							xaxis.setRangeModel(xAxisRange);
						}
						if (yaxis.getDataType() == Constants.NUMERICAL
							|| isNumerical(yAxisSelector)) {
							yaxis.setRangeModel(yAxisRange);
						}
					}
					
					Rectangle2D ybounds = new Rectangle2D.Double(bounds.getX() - 10, bounds.getY(), bounds.getWidth() + 10, bounds.getHeight());
					AxisLabelLabelLayout ylabels = new AxisLabelLabelLayout("ylabels", yaxis, ybounds);
					ylabels.setLabel(yAxisLabel);
					ylabels.setSpacing(ySpacing.getValue());
					ylabels.setGridLabel(axisGridLabel);
					Rectangle2D xbounds = new Rectangle2D.Double(bounds.getX(), bounds.getY() + bounds.getHeight() - 11, bounds.getWidth(), 10);
					AxisLabelLabelLayout xlabels = new AxisLabelLabelLayout("xlabels", xaxis, xbounds);
					xlabels.setLabel(xAxisLabel);
					xlabels.setSpacing(xSpacing.getValue());
					xlabels.setGridLabel(axisGridLabel);
					/*
					 * if (isIntType(xAxisSelector)) {
					 * xlabels.setScale(Constants.NOMINAL);
					 * }
					 * if (isIntType(yAxisSelector)) {
					 * ylabels.setSpacing(Math.ceil(ylabels.getSpacing()));
					 * }
					 * */
					
					ColorAction yAxisColor = new ColorAction("ylabels", VisualItem.STROKECOLOR, ColorLib.gray(100));
					ColorAction yLabColor = new ColorAction("ylabels", VisualItem.TEXTCOLOR, ColorLib.gray(0));
					ColorAction xAxisColor = new ColorAction("xlabels", VisualItem.STROKECOLOR, ColorLib.gray(100));
					ColorAction xLabColor = new ColorAction("xlabels", VisualItem.TEXTCOLOR, ColorLib.gray(0));
					
					ActionList axisColor = new ActionList();
					axisColor.add(yAxisColor);
					axisColor.add(yLabColor);
					axisColor.add(xAxisColor);
					axisColor.add(xLabColor);
					
					final ActionList axes = new ActionList();
					axes.add(xaxis);
					axes.add(yaxis);
					axes.add(ylabels);
					axes.add(xlabels);
					axes.add(axisColor);
					axes.add(new RepaintAction());
					
					m_vis.putAction("axes", axes);
					m_vis.run("axes");
					m_vis.run("aggLayout");
					Rectangle2D lbounds = m_vis.getBounds(GRAPH);
					GraphicsLib.expand(lbounds, 100 + (int) (1 / display.getScale()));
					DisplayLib.fitViewToBounds(display, lbounds, 1000);
				} else {
					m_vis.cancel("axes");
					m_vis.getGroup("xlabels").clear();
					m_vis.getGroup("ylabels").clear();
				}
			}
			
			/**
			 * Determines whether the selected data column in selector
			 * is of a numerical type. This is sometimes necessary instead of
			 * AxisLayout.getDataType() because the type isn't initialized until
			 * after the first run, or something like that. This is a pretty
			 * foolproof way to check.
			 *
			 * @param selector to get field from
			 * @return whether field selected in selector is numerical in graph
			 */
			private boolean isNumerical(JComboBox selector) {
				String selected = (String)selector.getSelectedItem();
				Class<?> cls = ((Graph)m_vis.getGroup(GRAPH)).getNodeTable().getColumnType(selected);
				if (cls == double.class || cls == int.class || cls == float.class ||
						cls == long.class) {
					return true;	
				}
				return false;
			}

			private boolean isIntType(JComboBox selector) {
				String selected = (String)selector.getSelectedItem();
				Class<?> cls = ((Graph)m_vis.getGroup(GRAPH)).getNodeTable().getColumnType(selected);
				if (cls == int.class || cls == long.class) {
					return true;	
				}
				return false;
			}
		});
		axisToggle.setToolTipText("<html>Hide or show axes. "
			+ "<br>Also, toggle this on and off to affect any changes"
			+ "<br>made in AXIS SETTINGS or the axis LABEL fields.</html>");

		JPanel axisPane = new JPanel();
		axisPane.setLayout(new GridLayout(0,2));
		axisPane.add(new JLabel("X Axis"));
		axisPane.add(xAxisSelector);
		axisPane.add(new JLabel("Y Axis"));
		axisPane.add(yAxisSelector);
		axisPane.add(openAxisSettings);
		axisPane.add(axisToggle);
		axisPane.setOpaque(false);
		axisPane.setBorder(BorderFactory.createTitledBorder("Axis Control"));

		fpanel.add(axisPane);
		/* -------------- AXIS GUI ELEMENTS ------------------------ */

		/* -------------- AESTHETIC ADJUST ELEMENTS ---------------- */


		final JButton showEdges = new JButton("Show Edges");
		showEdges.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				m_vis.setVisible(EDGES, null, true);
			}
		});
		showEdges.setToolTipText("Re-show edges if they hidden.");

		final JButton hideEdges = new JButton("Hide Edges");
		hideEdges.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				m_vis.setVisible(EDGES, null, false);
				m_vis.cancel("lll");
			}
		});
		hideEdges.setToolTipText("Hide edges. Useful for making graphs on axes cleaner.");

		final JButton openForcePanel = new JButton("Force Panel");
		openForcePanel.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JFrame forceFrame = new JFrame("Force Panel");
				ForceSimulator fsim = ((ForceDirectedLayout) ((ActionList) m_vis.getAction("lll")).get(0)).getForceSimulator();
				JForcePanel fPanel = new JForcePanel(fsim);
				forceFrame.add(fPanel);
				forceFrame.pack();
				forceFrame.setVisible(true);
				forceFrame.setAlwaysOnTop(true);
			}
		});
		openForcePanel.setToolTipText("<html>Open a panel to adjust the force parameters of the layout. "
			+ "<br>Can be useful to spread out a crowded graph or get different interaction behavior."
			+ "<br>It's also physicy phun.</html>");

		final JButton openVisSettingsPanel = new JButton("Vis Settings");
		openVisSettingsPanel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				SelfRefEdgeRenderer er = (SelfRefEdgeRenderer)((DefaultRendererFactory)m_vis.getRendererFactory()).getDefaultEdgeRenderer();
				PolygonRenderer pr = null;
				if (m_vis.getVisualGroup(AGGR) != null && m_vis.getVisualGroup(AGGR).getTupleCount() > 0) {
					pr = (PolygonRenderer)((VisualItem)m_vis.getVisualGroup(AGGR).tuples().next()).getRenderer();
				}
				VisualizationSettingsDialog vsd = new VisualizationSettingsDialog(frame, m_vis, tr, sr, er, pr);
				vsd.showDialog();
			}
		});
		

		JPanel aesPane = new JPanel();
		aesPane.setBorder(BorderFactory.createTitledBorder("Aesthetic"));
		aesPane.setLayout(new GridLayout(0,2));
		aesPane.add(showEdges);
		aesPane.add(hideEdges);
		aesPane.add(openForcePanel);
		aesPane.add(openVisSettingsPanel);
		aesPane.setOpaque(false);

		fpanel.add(aesPane);

		/* ----------- HIERARCHY GUI ELEMENTS ------------ */
		final JSlider zoomSlider = new JSlider(SwingConstants.VERTICAL, 0, 0, 0);
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ae) {
				int pos = zoomSlider.getValue();
				if (zoomSlider.isEnabled() && !zoomSlider.getValueIsAdjusting()) {
					assert hierarchy != null;
					//	MSMExplorer.this.getImagePath();
					JFrame toDie = MSMExplorer.this.frame;
					MSMExplorer msme = graphView(hierarchy.graphs[pos], "label");
					msme.setHierarchy(hierarchy, pos);
					toDie.dispose();
				}
			}
		});
		zoomSlider.setMajorTickSpacing(1);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);
		zoomSlider.setSnapToTicks(true);
		zoomSlider.setEnabled(false);
		zoomSlider.setOpaque(false);
		zoomSlider.setToolTipText("<html>Select the level of the hierarchy to display."
			+ "<br>The labels indicate the number of states in the model at that "
			+ "level.</html>");

		final JSlider overSlider = new JSlider(SwingConstants.VERTICAL, -1, -1, -1);
		overSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent ae) {
				int top = overSlider.getValue();
				int bottom = zoomSlider.getValue();
				if (overSlider.getValueIsAdjusting()
					|| top < 0 || !overSlider.isEnabled()) {
					//return
				} else if (top >= bottom) {
					/*
					JFrame toDie = MSMExplorer.this.frame;
					MSMExplorer msme = graphView(hierarchy.graphs[bottom], "label");
					msme.setHierarchy(hierarchy, bottom);
					toDie.dispose();
					* */
					if (m_vis.getGroup(AGGR) != null) {
						m_vis.getGroup(AGGR).clear();
						m_vis.removeAction(AGGR);
					}
					overSlider.setValue(overSlider.getMaximum());
				} else if (top < hierarchy.graphs.length - 1) {
					MSMExplorer.this.setAggregates(bottom, top);
					if (g.getNodeTable().getColumnNumber("mapping") >= 0) {
						axisFields.remove("mapping");
						axisFields.insertElementAt("mapping", axisFields.size() - 2);
					}
				}
			}
		});
		overSlider.setMajorTickSpacing(1);
		overSlider.setPaintTicks(true);
		overSlider.setPaintLabels(true);
		overSlider.setSnapToTicks(true);
		overSlider.setEnabled(false);
		overSlider.setOpaque(false);
		overSlider.setToolTipText("<html>Set the model to overlay on the current graph,"
			+ "<br>indicating the membership of the nodes in the graph"
			+ "<br>indicated by the LEVEL slider in the graph"
			+ "<br>selected by this slider. Only graphs of fewer"
			+ "<br>nodes than the underlying graph may be overlaid."
			+ "<br>Other selections will snap back to None (no overlay)</html>");

		JLabel harchLabel = new JLabel("Level");
		harchLabel.setFont(FontLib.getFont("Tahoma", 11));

		JLabel overLabel = new JLabel("Overlay");
		overLabel.setFont(FontLib.getFont("Tahoma", 11));

		harchPanel = new JPanel();
		harchPanel.setLayout(null);
		harchPanel.add(zoomSlider);
		harchPanel.add(overSlider);
		harchPanel.add(overLabel);
		harchPanel.add(harchLabel);
		zoomSlider.setBounds(5, 15, 60, 150);
		overSlider.setBounds(65, 15, 60, 150);
		harchLabel.setBounds(10, 0, 60, 20);
		overLabel.setBounds(65, 0, 60, 20);
		harchPanel.setEnabled(false);
		harchPanel.setVisible(false);
		harchPanel.setOpaque(false);
		/* ----------- END HIERARCHY GUI ELEMENTS ------------ */

		JLayeredPane graphPane = new JLayeredPane();
		graphPane.setLayout(null);
		graphPane.add(display, new Integer(0));
		graphPane.add(harchPanel, new Integer(1));
		graphPane.add(xAxisLabel, new Integer(1));
		harchPanel.setBounds(0, 0, 150, 170);
		harchPanel.setOpaque(false);
		graphPane.setPreferredSize(new Dimension(1000, 800));

		fpanel.add(Box.createRigidArea(new Dimension(0, 200)));

		// create a new JSplitPane to present the interface
		JSplitPane split = new JSplitPane();
		split.setLeftComponent(graphPane);
		split.setRightComponent(fpanel);
		split.setOneTouchExpandable(true);
		split.setContinuousLayout(false);
		split.setDividerLocation(1150);

		// now we run our action list
		m_vis.run("draw");
		m_vis.run("lll");
		m_vis.run("nodeSize");

		add(split);

		//Force panel menu item
		JMenuItem forcePanel = new JMenuItem("Force Panel");
		forcePanel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				JFrame forceFrame = new JFrame("Force Panel");

				ForceSimulator fsim = ((ForceDirectedLayout)((ActionList)m_vis.getAction("lll")).get(0)).getForceSimulator();
				JForcePanel fPanel = new JForcePanel(fsim);
				forceFrame.add(fPanel);
				forceFrame.pack();
				forceFrame.setVisible(true);
			}
		});

		JMenuItem statsPanel = new JMenuItem("Stats Panel");
		statsPanel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				GraphStatsWindow gsw = new GraphStatsWindow(g);
				gsw.setVisible(true);
			}
		});

		final JMenuItem openTable = new JMenuItem("Open Node Table");
		openTable.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JPrefuseTable.showTableWindow(new 
					CascadedTable(((Graph)m_vis.getGroup(GRAPH)).
					getNodeTable(), new NamedColumnProjection(
					Arrays.copyOf(axisFields.toArray(), 
					axisFields.size(), String[].class), true)));
			}	
		});

		// The following block is the gui boilerplate for a
		// currently unimplemented automated PDB concatenation function
		JMenuItem makeMovie = new JMenuItem("Create PDB Movie");
		makeMovie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				PDBFrame pdbf = new PDBFrame(g, m_vis);
			}
		});

		JMenuItem saveSVG = new JMenuItem("Save Image...");
		saveSVG.addActionListener(new ExportMSMImageAction(m_vis.getDisplay(0)));
		saveSVG.setAccelerator(KeyStroke.getKeyStroke("ctrl shift S"));

		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new OpenMSMAction(this));
		fileMenu.add(new OpenHierarchyAction());
		fileMenu.addSeparator();
		fileMenu.add(new SaveMSMAction(g, this));
		fileMenu.add(saveSVG);
		
		// set up menu
		JMenu dataMenu = new JMenu("Panels");
		dataMenu.add(forcePanel);
		dataMenu.add(statsPanel);
		dataMenu.addSeparator();
		dataMenu.add(openTable);
		//dataMenu.add(makeMovie); XXX put this back when implemented...

		JMenuBar menubar = new JMenuBar();
		menubar.add(fileMenu);
		menubar.add(dataMenu);

		// launch window
		JFrame frm = new JFrame("G r a p h  V i e w  |  M S M E x p l o r e r");
		frm.setJMenuBar(menubar);
		frm.setContentPane(this);
		frm.pack();
		frm.setVisible(true);
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//XXX must not be quite right...
		this.frame = frm;

		// Window activate/deactivate behavior
		frm.addWindowListener(new WindowAdapter() {

			@Override
			public void windowActivated(WindowEvent e) {
				m_vis.run("animate");
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// Stop layout, unless you are adjusting forces
				JFrame oppositeFrame;
				try {
					oppositeFrame = (JFrame) e.getOppositeWindow();
				} catch (ClassCastException cce) {
					oppositeFrame = null;
				}

				if (oppositeFrame != null
					&& oppositeFrame.getTitle().equals("Force Panel")); else {
					m_vis.cancel("animate");
				}
			}
		});
	}

	/**
	 * Open the array of graphs gs as a related hierarchy. Initializes
	 * slider to switch between levels of the hierarchy.
	 *
	 * @param gs
	 * @param pos
	 */
	public void setHierarchy(HierarchyBundle hb, int pos) {
		hierarchy = hb;
		JSlider zoomSlider = (JSlider) harchPanel.getComponent(0);
		zoomSlider.setMaximum(hb.graphs.length - 1);
		zoomSlider.setValue(pos);
		zoomSlider.setLabelTable(MSMIOLib.getHierarchyLabels(hb.graphs));

		JSlider overSlider = (JSlider) harchPanel.getComponent(1);
		overSlider.setMaximum(hb.graphs.length - 1);
		overSlider.setMinimum(0);
		overSlider.setValue(hb.graphs.length - 1);
		overSlider.setLabelTable(MSMIOLib.getAltHierarchyLabels(hb.graphs));

		zoomSlider.setEnabled(true);
		overSlider.setEnabled(true);
		harchPanel.setEnabled(true);
		harchPanel.setVisible(true);
	}

	/**
	 * Can only be called on a MSMExplorer instance that already has its
	 * own hierarchy list. Displays the aggregate graph that represents
	 * the mapping from the graph at position bottom to the graph at
	 * position top.
	 * Should only be called after already initing a MSMExplorer
	 * instance displaying the graph at hierarchy position bottom
	 *
	 * @param bottom
	 * @param top
	 */
	public void setAggregates(int bottom, int top) {

		JSlider overSlider = (JSlider) harchPanel.getComponent(1);
		if (top == overSlider.getMaximum()) {
			return;
		}

		//Intercept weird requests, inform user, and then do nothing
		if (bottom == 0) {
			JOptionPane.showMessageDialog(this, "Cannot display"
				+ "an overlay on top of the highest level"
				+ "of the hieararchy.");
			overSlider.setValue(hierarchy.graphs.length);
			return;

		} else if (bottom == top) {
			JOptionPane.showMessageDialog(this, "Can't overlay"
				+ " a model with itself!");
			return;
		}

		MSMIOLib.setMapping(hierarchy, bottom, top);

		TupleSet vg = m_vis.getGroup(NODES);
		Iterator<VisualItem> vNodes = vg.tuples();

		AggregateTable at;
		if (m_vis.getGroup(AGGR) == null) {
			at = m_vis.addAggregates(AGGR);
			at.addColumn(VisualItem.POLYGON, float[].class);
			at.addColumn("id", int.class);


			Renderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
			((PolygonRenderer)polyR).setCurveSlack(0.1f);
			((DefaultRendererFactory) m_vis.getRendererFactory()).add("ingroup('aggregates')", polyR);

			final ColorAction aStroke = new ColorAction(AGGR, VisualItem.STROKECOLOR);
			aStroke.setDefaultColor(ColorLib.gray(200));
			aStroke.add(VisualItem.FIXED, ColorLib.rgb(240, 150, 100));
			aStroke.setVisualization(m_vis);

			final ColorAction aFill = new FlexDataColorAction(AGGR, "id",
				Constants.NOMINAL, VisualItem.FILLCOLOR, ColorLib.getCategoryPalette(50, 0.95f, .15f, .9f, .5f));
			aFill.setVisualization(m_vis);

			ActionList draw = (ActionList) m_vis.getAction("draw");
			draw.add(aFill);

			ActionList animate = (ActionList) m_vis.getAction("animate");
			animate.add(aStroke);

		} else {
			at = (AggregateTable) m_vis.getGroup(AGGR);
			Table nt = (Table) ((Graph) m_vis.getGroup(GRAPH)).getNodeTable();
			for (int row = 0; row < nt.getRowCount(); ++row) {
				at.removeRow(row);
			}
		}
		// we use a HashMap so mappings can be arbitrarily assigned
		HashMap<Integer, AggregateItem> aggs = new HashMap<Integer, AggregateItem>();
		while (vNodes.hasNext()) {
			Node vNode = (Node) vNodes.next();
			assert vNode != null;
			int mapping = vNode.getInt("mapping");
			AggregateItem ai;
			if (!aggs.containsKey(mapping)) {
				ai = (AggregateItem) at.addItem();
				ai.setInt("id", mapping);
				aggs.put(mapping, ai);
			} else {
				ai = (AggregateItem) aggs.get(mapping);
				assert ai != null;
			}
			ai.addItem((VisualItem) vNode);
		}
		
		final Action aggLayout = new AggregateLayout(AGGR, m_vis);
		m_vis.putAction("aggLayout", aggLayout);
		((ActionList) m_vis.getAction("lll")).add(aggLayout);
		m_vis.run("draw");
		m_vis.run("aggLayout");
	}

	/**
	 * Registers the graph with the Visualization.
	 *
	 * @param g
	 * @param label
	 */
	public void setGraph(Graph g, String label) {
		// update labeling
		DefaultRendererFactory drf = (DefaultRendererFactory) m_vis.getRendererFactory();

		// update graph
		m_vis.removeGroup(GRAPH);
		VisualGraph vg = m_vis.addGraph(GRAPH, g);
		m_vis.setValue(EDGES, null, VisualItem.INTERACTIVE, Boolean.FALSE);
		VisualItem f = (VisualItem) vg.getNode(0);
		m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
		f.setFixed(false);
	}

	/**
	 * Initializes visualization parameters, behavior, and actions.
	 *
	 * @param g graph to display
	 * @param vis visualization object to display graph with
	 */
	public void initGraph(Graph g, Visualization vis) {

		/*
		final ColorAction fill = new ColorAction(nodes,
		  VisualItem.FILLCOLOR, ColorLib.rgb(179, 255, 156));
		 * 
		 */
		int[] palette = {ColorLib.rgb(179, 255, 156)};
		final FlexDataColorAction fill = new FlexDataColorAction(NODES,
			LABEL, Constants.ORDINAL, VisualItem.FILLCOLOR, palette);
		fill.add(VisualItem.FIXED, ColorLib.rgb(255, 100, 100));
		fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 200, 125));
		fill.add(new InGroupPredicate(Visualization.SEARCH_ITEMS), 
			  ColorLib.rgb(200, 40, 55));

		FlexDataColorAction edgeColor = new FlexDataColorAction(EDGES, TPROB,
			Constants.NOMINAL, VisualItem.STROKECOLOR,
			ColorLib.getGrayscalePalette(100));
		edgeColor.add(VisualItem.HOVER, ColorLib.rgb(200, 40, 60));
		edgeColor.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 150, 68));
		edgeColor.setFilterPredicate(new VisiblePredicate());

		FlexDataColorAction edgeFill = new FlexDataColorAction(EDGES, TPROB,
			Constants.NOMINAL, VisualItem.FILLCOLOR,
			ColorLib.getGrayscalePalette(100));
		edgeFill.add(VisualItem.HOVER, ColorLib.rgb(200, 40, 60));
		edgeFill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 150, 68));
		edgeFill.setFilterPredicate(new VisiblePredicate());

		DataSizeAction edgeWeight = new DataSizeAction(EDGES, "probability", 100, Constants.LOG_SCALE);
		edgeWeight.setMaximumSize(1.0);
		edgeWeight.setMinimumSize(1.0);

		//currently unused
		StrokeAction nodeWeight = new StrokeAction(NODES,
			StrokeLib.getStroke(1.0f));
		//nodeWeight.add(new InGroupPredicate(Visualization.FOCUS_ITEMS), StrokeLib.getStroke(2.0f));

		final DataSizeAction nodeSize = new DataSizeAction(NODES,
			EQPROB, 50, Constants.LOG_SCALE);
		nodeSize.setMaximumSize(50.0);

		if (g.getNodeTable().getColumnNumber("image") < 0) {
			String expression = "CONCAT(" + imageLocation + ",'/State',label,'.png')";
			g.getNodes().addColumn("image", expression);
		}

		// Set up filter
		//int hops = 5;
		final GraphDistanceFilter distFilter = new GraphDistanceFilter(GRAPH, 40);
		//Predicate eqProbPredicate = ExpressionParser.predicate("NOT(ISNODE() AND [eqProb] > .5)");
		//final VisibilityFilter eqProbFilter = new VisibilityFilter(eqProbPredicate);

		// Set up actionlists
		final ActionList draw = new ActionList();
		draw.add(distFilter);
		draw.add(new ColorAction(NODES, VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0)));
		draw.add(new ColorAction(NODES, VisualItem.STROKECOLOR, ColorLib.gray(50)));
		//draw.add(eqProbFilter);

		ActionList animate = new ActionList(ActionList.INFINITY);
		animate.add(edgeWeight);
		animate.add(edgeFill);
		animate.add(edgeColor);
		animate.add(fill);
		animate.add(new RepaintAction());

		final ActionList lll = new ActionList();

		//If graph is "large",
		if (g.getNodeCount() > SIZE_THRESHOLD
			|| GraphStatsManager.calcAvgDegree(g) > DEGREE_THRESHOLD) {
			lll.add(new ForceDirectedLayout(GRAPH, false, true)); //Then run-once
		} else {
			lll.setDuration(ActionList.INFINITY);
			lll.add(new ForceDirectedLayout(GRAPH));              //Else, continually animate
		}


		// finally, we register our ActionList with the Visualization.
		// we can later execute our Actions by invoking a method on our
		// Visualization, using the name we've chosen below.
		m_vis.putAction("draw", draw);
		m_vis.putAction("lll", lll);
		m_vis.putAction("nodeSize", nodeSize);
		m_vis.putAction("animate", animate);
		m_vis.putAction("nodeFill", fill);

		m_vis.runAfter("draw", "animate");
		m_vis.alwaysRunAfter("lll", "draw");

		// get forces from simulator
		ForceSimulator fsim = ((ForceDirectedLayout) ((ActionList) m_vis.getAction("lll")).get(0)).getForceSimulator(); //HERE
		Force[] forces = fsim.getForces();

		//extract forces
		final Force NBodyForce = forces[0];
		//final Force DragForce = forces[1];
		final Force SpringForce = forces[2];

		// Set initial forces and expand slider range
		NBodyForce.setMinValue(0, -40.0f);
		NBodyForce.setParameter(0, -40.0f);
		NBodyForce.setParameter(1, -1);

		SpringForce.setMinValue(0, .00000099f);
		SpringForce.setParameter(0, .00001f);
		SpringForce.setMaxValue(1, 3200f);
		SpringForce.setParameter(1, 400f);
	}

	/**
	 * Gets graph from user input and opens
	 * the selection box to choose start and end states
	 * for TPT.
	 *
	 * @param selector
	 */
	private static void preemptiveTPT(JFrame selector) {
		Graph g = MSMIOLib.getMSMFile(null);
		if (g == null) {
			return;
		}

		selector.dispose();

		TPTSetupBox setup = new TPTSetupBox(g, false);
	}

	// ------------------------------------------------------------------------
	// Main and graphView methods
	// ------------------------------------------------------------------------
	/**
	 * Main creates a start screen to select where the user would
	 * like to begin. Main MSMExplorer modules depart from here.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		MSMExplorer msme = new MSMExplorer();
	}   // end of main

	/**
	 * graphView function. Retrieves graph from provided filename.
	 *
	 * @param datafile
	 * @param label
	 * @return JFrame with new MSMExplorer
	 */
	public MSMExplorer graphView(String datafile, String label) {
		Graph g;

		try {
			g = new GraphMLReader().readGraph(datafile);
		} catch (Exception e) {
			Logger.getLogger(MSMExplorer.class.getName()).log(Level.WARNING, null, e);
			JOptionPane.showMessageDialog(null, "Could not open graph at "+datafile
				+"\nWill now present you with a dialog to open a different graph.", 
				"Could not open default graph", JOptionPane.WARNING_MESSAGE);
			g = MSMIOLib.getMSMFile(null);
		}
		return graphView(g, label);
	} // end of class graphView(String, String)

	/**
	 * graphView function, initializes MSMExplorer with provided graph
	 * and label creates the menu bar and buttons, and sets
	 * window focus behavior.
	 * @param g
	 * @param label
	 * @return newly constructed JFrame
	 */
	public MSMExplorer graphView(final Graph g, String label) {
		final MSMExplorer view = new MSMExplorer(g, label);

		view.frame.setPreferredSize(new Dimension(1200,800));
		return view;
	}   //end of class graphView(Graph, String)

	private void getImagePath() {

		int response = JOptionPane.showConfirmDialog(frame, "Would you like to"
			+ " locate the image folder for this graph?",
			"Locate Image Files",
			JOptionPane.YES_NO_OPTION);

		if (response == JOptionPane.YES_OPTION) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int returnVal = chooser.showOpenDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				imageLocation = chooser.getSelectedFile().getAbsolutePath();
			}
		}
	}

	// ------------------------------------------------------------------------
	//  Utility classes
	// ------------------------------------------------------------------------
	private class OpenHierarchyAction extends AbstractAction {

		public OpenHierarchyAction() {
			this.putValue(AbstractAction.NAME, "Open Hierarchy");
			this.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke("ctrl shift O"));
		}

		public void actionPerformed(ActionEvent ae) {
			hierarchy = MSMIOLib.openMSMHierarchy(MSMExplorer.this);
			if (hierarchy == null || hierarchy.graphs == null
				|| hierarchy.mappings == null || hierarchy.graphs.length < 1) {
				return;
			}

			//MSMExplorer.this.getImagePath();
			MSMExplorer.this.frame.dispose();
			MSMExplorer msme = graphView(hierarchy.graphs[0], "label");
			msme.setHierarchy(hierarchy, 0);
		}
	}

	/**
	 * Action to open a new graph, disposing the old graph.
	 */
	public class OpenMSMAction extends AbstractAction {

		private MSMExplorer m_view;

		/**
		 * Constructor, initializes name, accelkey, vis
		 * @param the MSMExplorer instance to affect
		 */
		public OpenMSMAction(MSMExplorer view) {
			m_view = view;
			this.putValue(AbstractAction.NAME, "Open File...");
			this.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke("ctrl O"));
		}

		public void actionPerformed(ActionEvent e) {
			Graph g = MSMIOLib.getMSMFile(m_view);

			if (g != null) {
				m_view.getImagePath();
				frame.dispose();
				graphView(g, "label");
			}

		}
	} 

	public class SaveMSMAction extends AbstractAction {

		private Graph graph;
		private MSMExplorer m_view;

		public SaveMSMAction(Graph g, MSMExplorer view) {
			graph = g;
			m_view = view;
			this.putValue(AbstractAction.NAME, "Save File...");
			this.putValue(AbstractAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke("ctrl S"));
		}

		public void actionPerformed(ActionEvent e) {
			MSMIOLib.saveGML(m_view, graph);

		}
	}
} // end of class MSMExplorer

