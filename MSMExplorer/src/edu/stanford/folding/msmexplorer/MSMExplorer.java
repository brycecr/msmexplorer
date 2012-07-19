package edu.stanford.folding.msmexplorer;

import edu.stanford.folding.msmexplorer.io.hierarchy.HierarchyBundle;
import edu.stanford.folding.msmexplorer.io.MSMIOLib;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JToggleButton;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import prefuse.Display;
import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Node;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.io.GraphMLReader;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.search.KeywordSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.data.tuple.DefaultTupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.StrokeLib;
import prefuse.util.display.ExportDisplayAction;
import prefuse.util.force.Force;
import prefuse.util.force.ForceSimulator;
import prefuse.util.ui.JForcePanel;
import prefuse.util.ui.JValueSlider;
import prefuse.util.ui.JSearchPanel;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import edu.stanford.folding.msmexplorer.tpt.TPTSetupBox;
import edu.stanford.folding.msmexplorer.tpt.TPTWindow;
import edu.stanford.folding.msmexplorer.util.aggregate.AggregateDragControl;
import edu.stanford.folding.msmexplorer.util.render.SelfRefEdgeRenderer;
import edu.stanford.folding.msmexplorer.util.stats.GraphStatsManager;
import edu.stanford.folding.msmexplorer.util.stats.GraphStatsWindow;
import edu.stanford.folding.msmexplorer.util.movie.*;
import edu.stanford.folding.msmexplorer.util.ui.Picture;
import edu.stanford.folding.msmexplorer.util.ui.FocusControlWithDeselect;
import edu.stanford.folding.msmexplorer.util.ui.FitOverviewListener;
import edu.stanford.folding.msmexplorer.util.ui.JValueSliderF;
import edu.stanford.folding.msmexplorer.util.aggregate.AggregateLayout;
import edu.stanford.folding.msmexplorer.util.aggregate.AggregatePrioritySorter;

import edu.stanford.folding.msmexplorer.util.render.ImageToggleLabelRenderer;
import java.awt.GridLayout;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import javax.swing.SwingConstants;
import javax.swing.JSlider;
import javax.swing.JFileChooser;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import prefuse.action.Action;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.util.PrefuseLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;

/**
 * Class to execute MSMExplorer, a visualization module for
 * protein folding Markov State Models generated with
 * MSMBuilder.
 *
 * Built on GraphView framework by Jeffrey Heer.
 *
 * @author Bryce Cronkite-Ratcliff, brycecr@stanford.edu
 */
public final class MSMExplorer extends JPanel implements MSMConstants {

	private static final int SIZE_THRESHOLD = 250; //Threshold for "big" graph behavior
	private static final int DEGREE_THRESHOLD = 30;
	private static final String aggr = "aggregates";
	private static final String graph = "graph";
	private static final String nodes = "graph.nodes";
	private static final String edges = "graph.edges";
	private final Visualization m_vis = new Visualization();
	private JFrame frame; //Graph view frame
	private static final String version = "v0.03"; //Current Version
	private String imageLocation = "'./lib/images'";
	private HierarchyBundle hierarchy = null;
	private JSlider zoomSlider = null;
	private JSlider overSlider = null;

	public MSMExplorer() {

		//Selector window
		final JFrame selector = new JFrame("W e l c o m e  |  M S M E x p// l o r e r");
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

		//Perform TPT without proceeding through Graph View
		JButton tptButton = new JButton("Just TPT");
		tptButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				preemptiveTPT(selector);
			}
		});

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
			System.err.println("Could not find splash image");
			image = new BufferedImage(500, 300, BufferedImage.TYPE_INT_RGB);
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
	 * @param label
	 */
	public MSMExplorer(final Graph g, String label) {
		super(new BorderLayout());

		GraphStatsManager gsm = new GraphStatsManager(g); //TODO: remove. Only for testing
		gsm.printStats();

		// boolean used to sacrifice fancy functionality for speed
		// wheen dealing with big graphs.
		boolean isBigGraph = (g.getNodeCount() > SIZE_THRESHOLD);

		// create a new, empty visualization for our data

		final ImageToggleLabelRenderer tr = new ImageToggleLabelRenderer();
		tr.setVerticalAlignment(Constants.BOTTOM);
		tr.setRoundedCorner(8, 8);
		// selfref renderer is for self-transitions. However, at this point,
		// it does not respond to probability color adjustments.
		m_vis.setRendererFactory(
			new DefaultRendererFactory(tr, new SelfRefEdgeRenderer()));

		// --------------------------------------------------------------------
		// register the data with a visualization

		// adds graph to visualization and sets renderer label field
		setGraph(g, label);

		// set up Focus behavior
		final TupleSet focusGroup = m_vis.getGroup(Visualization.FOCUS_ITEMS);
		focusGroup.addTupleSetListener(new TupleSetListener() {

			public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
				for (int i = 0; i < add.length; ++i) {
					if (add[i] instanceof AggregateItem) {
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
		//tr.getImageFactory().preloadImages(g.nodes(), "image");

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

		// Main control panel
		JPanel fpanel = new JPanel();
		fpanel.setLayout(new BoxLayout(fpanel, BoxLayout.Y_AXIS));
		fpanel.setBackground(Color.WHITE);

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


		//the F lets other code fire without a value change.
		//note that currently this orchestrates the behavior of both filters
		final JValueSliderF eqProbSlider = new JValueSliderF("EqProb Cutoff", 0., 1., 0.);
		eqProbSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
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
				//Iterator itr = m_vis.items(ExpressionParser.predicate("ISNODE()"));
				//Iterator itr = m_vis.items(nodes);
				Iterator itr = m_vis.visibleItems(nodes);
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
		//eqProbSlider.setLayout(new BoxLayout(eqProbSlider, BoxLayout.Y_AXIS));
		eqProbSlider.setLayout(new GridLayout(0,2));

		final JTextField eqProbText = new JTextField("EqProb Thresh");
		eqProbText.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				double d = Double.parseDouble(((JTextField) ae.getSource()).getText());
				eqProbSlider.setValue(d);
			}
		});
		eqProbSlider.add(eqProbText);

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

		Box eqBox = new Box(BoxLayout.Y_AXIS);
		//eqBox.add(eqProbText);
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

		Box rendBox = new Box(BoxLayout.X_AXIS);
		rendBox.setBorder(BorderFactory.createTitledBorder("Gen. Renderer"));
		rendBox.add(togglePM);
		rendBox.add(curveBtn);
		fpanel.add(rendBox);

		// Button group to select which node Renderer to use
		// (circle or rounded-rectangle label)
		ButtonGroup nodeRenderers = new ButtonGroup();

		JRadioButton circleRB = new JRadioButton("Circle", false);
		circleRB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				JRadioButton circleRB = (JRadioButton) ae.getSource();
				if (circleRB.isSelected()) {
					m_vis.setRendererFactory(new DefaultRendererFactory(new ShapeRenderer()/*, new SelfRefEdgeRenderer()*/));
					((DefaultRendererFactory)m_vis.getRendererFactory()).add("ingroup('aggregates')", new PolygonRenderer(Constants.POLY_TYPE_CURVE));
					m_vis.setValue(nodes, null, VisualItem.SHAPE, Constants.SHAPE_ELLIPSE);
				}
			}
		});
		nodeRenderers.add(circleRB);

		JRadioButton labelRB = new JRadioButton("Label", false);
		labelRB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				JRadioButton labelRB = (JRadioButton) ae.getSource();
				if (labelRB.isSelected()) {
					m_vis.setRendererFactory(new DefaultRendererFactory(tr/*, new SelfRefEdgeRenderer()*/));
				}
			}
		});
		nodeRenderers.add(labelRB);

		Box nrBox = new Box(BoxLayout.X_AXIS);
		nrBox.setBorder(BorderFactory.createTitledBorder("Node Renderer"));
		nrBox.add(circleRB);
		nrBox.add(labelRB);
		labelRB.setSelected(true);

		fpanel.add(nrBox);

		JButton pause = new JButton("Stop Layout");
		pause.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Action layoutAction = m_vis.getAction("lll");
				layoutAction.cancel();
			}
		});

		JButton start = new JButton("Run Layout");
		start.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (!m_vis.getAction("lll").isRunning()) {
					ActionList layoutAction = (ActionList)m_vis.getAction("lll");
					layoutAction.run();
				}
			}
		});

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

		//Save raster image file
		JButton exportDisplay = new JButton("Save Image");
		exportDisplay.addActionListener(new ExportDisplayAction(display));

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
					((DataSizeAction) m_vis.getAction("nodeSize")).setMaximumSize(1.0);
					m_vis.run("nodeSize");
					tr.setImageField("image");
					tr.setImagePosition(Constants.TOP);
					tr.getImageFactory().setMaxImageDimensions(100, 100);
				}
				m_vis.run("draw");
			}
		});

		// Open selector window to select start and end states for TPT
		JButton runTPT = new JButton("TPT Selector");
		runTPT.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				TPTSetupBox tsb = new TPTSetupBox(g, true, m_vis);
			}
		});
		Box renderControls = new Box(BoxLayout.X_AXIS);
		renderControls.setBorder(BorderFactory.createTitledBorder("Function Control"));
		renderControls.add(togglePics);
		renderControls.add(runTPT);
		fpanel.add(renderControls);

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

		SearchQueryBinding sq = new SearchQueryBinding((Table) m_vis.getGroup(nodes), "label",
			(SearchTupleSet) m_vis.getGroup(Visualization.SEARCH_ITEMS));

		JSearchPanel search = sq.createSearchPanel(false);
		search.setShowResultCount(true);
		search.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 0));

		Box searchBox = new Box(BoxLayout.X_AXIS);
		searchBox.setBorder(BorderFactory.createTitledBorder("Search"));
		searchBox.add(search);
		fpanel.add(searchBox);

		zoomSlider = new JSlider(SwingConstants.VERTICAL, 0, 0, 0);
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

		overSlider = new JSlider(SwingConstants.VERTICAL, -1, -1, -1);
		overSlider.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent ae) {
				int top = overSlider.getValue();
				int bottom = zoomSlider.getValue();
				if (overSlider.getValueIsAdjusting() ||
					top < 0 || !overSlider.isEnabled() ) {
					return;
				} else if (top >= bottom) {
					JFrame toDie = MSMExplorer.this.frame;
					MSMExplorer msme = graphView(hierarchy.graphs[bottom], "label");
					msme.setHierarchy(hierarchy, bottom);
					toDie.dispose();
					overSlider.setValue(overSlider.getMaximum());
				} else if (top < hierarchy.graphs.length - 1) {
					MSMExplorer.this.setAggregates(bottom, top);
				}
			}
		});
		overSlider.setMajorTickSpacing(1);
		overSlider.setPaintTicks(true);
		overSlider.setPaintLabels(true);
		overSlider.setSnapToTicks(true);
		overSlider.setEnabled(false);

		// overview display window
		Display overview = new Display(m_vis);
		overview.setSize(290, 290);
		overview.addItemBoundsListener(new FitOverviewListener());
		overview.setHighQuality(true);

		JPanel opanel = new JPanel();
		opanel.setBorder(BorderFactory.createTitledBorder("Overview"));
		opanel.setBackground(Color.WHITE);
		opanel.add(overview);

		fpanel.add(opanel);

		fpanel.add(Box.createVerticalGlue());

		JLayeredPane graphPane = new JLayeredPane();
		graphPane.setLayout(null);
		graphPane.add(display, new Integer(0));
		graphPane.add(zoomSlider, new Integer(1));
		graphPane.add(overSlider, new Integer(1));
		graphPane.setPreferredSize(new Dimension(1000, 800));
		zoomSlider.setBounds(5, 10, 60, 150);
		zoomSlider.setVisible(false);
		overSlider.setBounds(65, 10, 60, 150);
		overSlider.setVisible(false);


		// create a new JSplitPane to present the interface
		JSplitPane split = new JSplitPane();
		split.setLeftComponent(graphPane);
		split.setRightComponent(fpanel);
		split.setOneTouchExpandable(true);
		split.setContinuousLayout(false);
		split.setDividerLocation(1100);

		// now we run our action list
		m_vis.run("draw");
		m_vis.run("lll");
		m_vis.run("nodeSize");

		add(split);
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
		zoomSlider.setMaximum(hb.graphs.length - 1);
		zoomSlider.setValue(pos);
		zoomSlider.setLabelTable(MSMIOLib.getHierarchyLabels(hb.graphs));
		zoomSlider.setEnabled(true);
		zoomSlider.setVisible(true);
		overSlider.setMaximum(hb.graphs.length - 1);
		overSlider.setMinimum(0);
		overSlider.setValue(hb.graphs.length - 1);
		overSlider.setLabelTable(MSMIOLib.getAltHierarchyLabels(hb.graphs));
		overSlider.setVisible(true);
		overSlider.setEnabled(true);
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

		TupleSet vg = m_vis.getGroup(nodes);
		Iterator<VisualItem> vNodes = vg.tuples();

		AggregateTable at = null;
		if (m_vis.getGroup(aggr) == null) {
			at = m_vis.addAggregates(aggr);
			at.addColumn(VisualItem.POLYGON, float[].class);
			at.addColumn("id", int.class);


			Renderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
			//((PolygonRenderer)polyR).setCurveSlack(0.1f);
			((DefaultRendererFactory) m_vis.getRendererFactory()).add("ingroup('aggregates')", polyR);

			final ColorAction aStroke = new ColorAction(aggr, VisualItem.STROKECOLOR);
			aStroke.setDefaultColor(ColorLib.gray(200));
			aStroke.add(VisualItem.FIXED, ColorLib.rgb(240, 150, 100));
			aStroke.setVisualization(m_vis);

			final ColorAction aFill = new DataColorAction(aggr, "id",
				Constants.NOMINAL, VisualItem.FILLCOLOR, ColorLib.getCategoryPalette(50, 0.95f, .15f, .9f, .5f));
			aFill.setVisualization(m_vis);

			ActionList draw = (ActionList) m_vis.getAction("draw");
			draw.add(aFill);

			ActionList animate = (ActionList)m_vis.getAction("animate");
			animate.add(aStroke);

			m_vis.getDisplay(0).setItemSorter(new AggregatePrioritySorter());

			final Action aggLayout = new AggregateLayout(aggr, m_vis);
			m_vis.putAction("aggLayout", aggLayout);
			((ActionList) m_vis.getAction("lll")).add(aggLayout);
		} else {
			at = (AggregateTable) m_vis.getGroup(aggr);
			Table nt = (Table) ((Graph) m_vis.getGroup(graph)).getNodeTable();
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
			AggregateItem ai = null;
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
		m_vis.run("draw");
		m_vis.run("lll");
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
		m_vis.removeGroup(graph);
		VisualGraph vg = m_vis.addGraph(graph, g);
		m_vis.setValue(edges, null, VisualItem.INTERACTIVE, Boolean.FALSE);
		VisualItem f = (VisualItem) vg.getNode(0);
		m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
		f.setFixed(false);
	}

	/**
	 * Initializes visualization parameters, behavior, and actions.
	 *
	 * @param g
	 * @param vis
	 */
	public void initGraph(Graph g, Visualization vis) {

		// Set up aciton
		ColorAction fill = new NodeColorAction(nodes);

		/*ColorAction fill = new ColorAction(nodes,
		VisualItem.FILLCOLOR, ColorLib.rgb(179, 255, 156));
		fill.add(VisualItem.FIXED, ColorLib.rgb(255, 100, 100));
		fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 200, 125));*/

		DataColorAction edgeColor = new DataColorAction(edges, TPROB,
			Constants.NOMINAL, VisualItem.STROKECOLOR,
			ColorLib.getGrayscalePalette());
		edgeColor.add(VisualItem.HOVER, ColorLib.rgb(200, 40, 60));
		edgeColor.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 150, 68));

		DataColorAction edgeFill = new DataColorAction(edges, TPROB,
			Constants.NOMINAL, VisualItem.FILLCOLOR,
			ColorLib.getGrayscalePalette());
		edgeFill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(200, 0, 0));

		StrokeAction edgeWeight = new StrokeAction(edges,
			StrokeLib.getStroke(1.0f));
		edgeWeight.add(VisualItem.HIGHLIGHT, StrokeLib.getStroke(2.0f));

		StrokeAction nodeWeight = new StrokeAction(nodes,
			StrokeLib.getStroke(1.0f));
		nodeWeight.add(VisualItem.HIGHLIGHT, StrokeLib.getStroke(2.0f));

		DataSizeAction nodeSize = new DataSizeAction(nodes,
			EQPROB, 50, Constants.LOG_SCALE);
		nodeSize.setMaximumSize(50.0);

		if (g.getNodeTable().getColumnNumber("image") < 0) {
			String expression = "CONCAT(" + imageLocation + ",'/State',label,'.png')";
			g.getNodes().addColumn("image", expression);
		}

		// Set up filter
		//int hops = 5;
		final GraphDistanceFilter distFilter = new GraphDistanceFilter(graph, 40);
		//Predicate eqProbPredicate = ExpressionParser.predicate("NOT(ISNODE() AND [eqProb] > .5)");
		//final VisibilityFilter eqProbFilter = new VisibilityFilter(eqProbPredicate);

		// Set up actionlists
		final ActionList draw = new ActionList();
		draw.add(distFilter);
		draw.add(edgeFill);
		draw.add(new ColorAction(nodes, VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0)));
		draw.add(new ColorAction(nodes, VisualItem.STROKECOLOR, ColorLib.gray(50)));
		//draw.add(eqProbFilter);

		ActionList animate = new ActionList(ActionList.INFINITY);
		animate.add(edgeWeight);
		animate.add(edgeColor);
		animate.add(fill);
		animate.add(new RepaintAction());

		final ActionList lll = new ActionList();
		if (g.getNodeCount() > SIZE_THRESHOLD || GraphStatsManager.calcAvgDegree(g) > DEGREE_THRESHOLD) //If graph is large
		{
			lll.add(new ForceDirectedLayout(graph, false, true)); //Then run-once
		} else {
			lll.setDuration(ActionList.INFINITY);
			lll.add(new ForceDirectedLayout(graph));              //Else, continually animate
		}


		// finally, we register our ActionList with the Visualization.
		// we can later execute our Actions by invoking a method on our
		// Visualization, using the name we've chosen below.
		m_vis.putAction("draw", draw);
		m_vis.putAction("lll", lll);
		m_vis.putAction("nodeSize", nodeSize);
		m_vis.putAction("animate", animate);

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
		SpringForce.setParameter(0, .000001f);
		SpringForce.setMaxValue(1, 1600f);
		SpringForce.setParameter(1, 300f);
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
		Graph g = null;

		try {
			g = new GraphMLReader().readGraph(datafile);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.err.println(e.getCause());
			System.exit(1);
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

		//Force panel menu item
		JMenuItem forcePanel = new JMenuItem("Force Panel");
		forcePanel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				JFrame forceFrame = new JFrame("Force Panel");

				ForceSimulator fsim = ((ForceDirectedLayout) ((ActionList) view.m_vis.getAction("lll")).get(0)).getForceSimulator();
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

		// The following block is the gui boilerplate for a
		// currently unimplemented automated PDB concatenation function

		JMenuItem makeMovie = new JMenuItem("Create PDB Movie");
		makeMovie.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				PDBFrame pdbf = new PDBFrame(g, view.m_vis);
			}
		});


		// set up menu
		JMenu dataMenu = new JMenu("Data");
		dataMenu.add(forcePanel);
		dataMenu.add(statsPanel);
		dataMenu.add(makeMovie);
		dataMenu.add(new OpenMSMAction(view));
		dataMenu.add(new OpenHierarchyAction());
		dataMenu.add(new SaveMSMAction(g, view));

		JMenuBar menubar = new JMenuBar();
		menubar.add(dataMenu);

		// launch window
		JFrame frm = new JFrame("G r a p h  V i e w  |  M S M E x p l o r e r");
		frm.setJMenuBar(menubar);
		frm.setContentPane(view);
		frm.pack();
		frm.setVisible(true);
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//XXX must not be quite right...
		this.frame = frm;
		view.frame = frm;

		// Window activate/deactivate behavior
		frm.addWindowListener(new WindowAdapter() {

			@Override
			public void windowActivated(WindowEvent e) {
				view.m_vis.run("animate");
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
					view.m_vis.cancel("animate");
				}
			}
		});

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
			/*
			String s = (String)JOptionPane.showInputDialog(
			frame,
			"Enter the complete filename of the image for "
			+ "state 1, \n"
			+ " e.g. \"macro1.png\"",
			"Image name format",
			JOptionPane.QUESTION_MESSAGE,
			null, null, "macro1.png");
			
			//			If a string was returned, say so.
			if ((s != null) && (s.length() > 0)) {
			//setLabel("image format: " + s);
			}
			 * 
			 */
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
		 * Constructor, initializes action.
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
	} // end of class

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
			MSMIOLib.saveGML(graph, m_view);

		}
	}

	/**
	 * Custom color class to color fill.
	 */
	public class NodeColorAction extends ColorAction {

		/*
		 * Constructor
		 *
		 * @param String name of group to affect
		 */
		public NodeColorAction(String group) {
			super(group, VisualItem.FILLCOLOR);
		}

		@Override
		public int getColor(VisualItem item) {
			if (m_vis.isInGroup(item, Visualization.SEARCH_ITEMS)) {
				return ColorLib.rgb(255, 120, 120);
			} else if (item.isFixed()) {
				return ColorLib.rgb(245, 150, 50);
			} else if (item.isHighlighted()) {
				return ColorLib.rgb(245, 200, 100);
			} else {
				return ColorLib.rgb(245, 230, 210);
			}
		}
	} // end of inner class NodeColorAction
} // end of class GraphView

