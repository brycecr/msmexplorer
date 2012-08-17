   __  __ ___ __  __ ___          _                 
  |  \/  / __|  \/  | __|_ ___ __| |___ _ _ ___ _ _ 
  | |\/| \__ \ |\/| | _|\ \ / '_ \ / _ \ '_/ -_) '_|
  |_|  |_|___/_|  |_|___/_\_\ .__/_\___/_| \___|_|  
                          |_|                     

*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*
   *=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*
      *=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*=-*

Date: (Version 0.03) July 2012


MSMExplorer 
-----------
A visualization module for MSMBuilder-generated Markov State Models for protein-folding simulations.

Primary contact: msmexplorer@gmail.com

Please report bugs, comments, and feature requests on MSMExplorer's smitk page, or email msmexplorer@gmail.com directly.

NOTE: The information contained herein is also explained in REFERENCE_TUTORIAL.pdf, which is recommended instead of this if you have the facility for opening it. A bit more detail is provided there.

Introduction
------------
MSMExplorer is a java-based visualization package for protein folding Markov State Models (MSMs) that have been produced using MSMBuilder. This software hopes to provide a simple and intuitive interface for visual analysis of MSMs, including the production of publication-quality visualizations.

This program is released under the terms of the GNU GPL. Copyright Pande Lab 2010, 2011, 2012. As of June 2012, primary development has moved to github at github.com/brycecr/msmexplorer and source code is availble at that URL:
> git clone https://github.com/brycecr/msmexplorer/
The default branch is master, which will generally be updated once a feature is some version of fully implemented. 
The branch acacia is the primary development branch for work in progress. 
If you want to mess around with writing Python for MSMExplorer via Jython, checkout out the kulkulkan branch (which should follow master in terms of Java code).
Additionally, the indd file for the pdf REFERENCE_TUTORIAL is in another repository, github.com/brycecr/msmexplorer_doc

NOTE: This program is currently an ALPHA release. Please forward all feature requests and bug reports to msmexplorer@gmail.com

Requirements
------------
Requires the Java SE version 6. 4 GB or Ram or greater recommended for visualization of graphs with more than 1000 nodes.

Documentation
-------------

Table of Contents:
1) Loading and viewing MSMs
2) Features and controls
	a) Graph View Features
	b) TPT Features
	c) Additional Features, including unimplemented.
3) Future developments


1) Loading, Viewing, and Saving MSMs
----------------------------
As of version 0.02, MSMExplorer has a graphical interface for opening MSMs directly. In Graph View, go to Data -> Open File, or press ctrl-o. Select the .dat (simple matrix) or .mtx (sparse matrix) transition probability matrix. You will then be prompted to select an equilibrium probabilities file, expected in simple newline-delimited format. This is optional but suggested if you have such a file on hand. You may also be prompted for an image folder. This  solely to allow you to place images on top of the nodes. The image files inside the image directory should be png files named "State[state # for image].png" e.g. for state 0, the corresponding image would be "State0.png" . Yes, this is a bit arcane, and will probably be made more smooth in the future. 

GraphML format files of MSMs loaded into MSMExplorer can be saved by going to Data -> Save File, or pressing ctrl-s.

Note: The size of graphs you may feasibly load and display is dependent upon your hardware restrictions and the stack and heap size allocated by the JVM. The -Xmx and -Xss command-line flags (max heap and stack size, respectively) may be used to set these parameters. We recommend at least 2 or 4 GB of heap space. On the development hardware (2.66 GHz Intel Core Duo and 4 GB RAM), we were able to comfortably load and display graphs of at least a couple thousand nodes with average degree around 15. We could load 20,000 nodes, but could not display them effectively due to stack overflow errors from graph layout calculations. Note that MSMExplorer uses some very simple heuristics to turn on or off fancy layout animations for small and large graphs, respectively. We would appreciate feedback as to the effectiveness of these heuristics. 

Another Note: The first release of MSMExplorer used a command line utility msm2gml to convert Markov state models into graphml format. The same functionality is provided through the MSMExplorer gui, as MSMExplorer can load raw MSM files and output GraphMLs. Thus, msm2gml is not included in releases after 0.01 and the command line utility will probably not see much development beyond its current state. Still, we recognize the utility has its uses (for converting very large graphs MSMExplorer can't open, or for other specialized or adaptive uses), so the msm2gml code and a precompiled OSX binary can be found in the version 0.01 release on smitk. If there are any questions, please email msmexplorer@gmail.com.

*****Start Screen*****
When the program is first run, a screen pops up with version information and two buttons:
	> Graph View: Load entire MSM into the primary graph display interface
		Choosing Graph View will bring you directly to the Graph View window displaying a simple sample graph with which to test program features. To load your own data, use the Open File option in the Data menu at upper left. 
	> Just TPT: Bypass viewing the entire MSM and just perform TPT (opens and interface to choose start and end states). This is recommended for very large graphs that may not display well or at all in the primary graph view. 
		Choosing Just TPT will bring you to a dialog box where you may select the file to use for TPT.

2) Features and Controls
------------------------

\----=====GRAPH VIEW=====----/

INFORMATION ENCODED IN GRAPH:
> Edge shade is correlated to transition probability (darker is higher).
> Node size is correlated to the equilibrium probability of that state.

INTERACTING WITH THE GRAPH:
>	Hovering over a node with the mouse will highlight the node, the edges entering or leaving from that node, and the neighboring nodes.
>	Clicking on a node will select that node. This will fix its position and highlight the node and the edges entering or leaving from that node. 
>	With a node selected, ctrl-click on a second node to run TPT between those two nodes (1st node selected -> 2nd node clicked (source -> sink)).
>	Click on the background of the graph window to clear the current selection. 

BUTTONS AND GUI COMPONENTS:
> 	The File Menu (Upper Left) :
		Open File:
			> Open a dialog box to select a new graph to display. After a graph is selected, the user will be prompted to select an equilibrium probability file (recommended) and then the image directory for the new graph (optional). MSMExplorer can open normal dense, space-delimited .dat matrices, .mtx sparse format matrices, and .graphml files. 
		Open Hierarchy:
			> Open a dialog to select a folder containing an hierarchical MSM to load. A minimal example of the required format can be browsed in the simple_hierarchy folder. Note that the filenames (tProb, Populations, and MacroMapping) are important for loading correctly, although MSMExplorer attempts to provide some flexibility. In particular, each subfolder participating in the hierarchy must contain a transition probability matrix (called tProb with an extension specifying the format) and a file specifying the mapping from a hierarchy-common base (the most populated MSM in the hierarchy) to the macrostates in the transition probability matrix (mapping file called MacroMapping). These should be MSMBuilder defaults. Equilibrium probabilities may be optionally provided (recommended) 
		Import Data Column:
			> Open a dialog to import a new data column for either the nodes, edges, or aggregates.
		Save Image: 
			> Export a raster or vector image of the current display. Vector (svg) will rescale infinitely, but may result in some weird font appearance on occasion.
		Save File: 
			> Open a dialog to save the current graph (in its entirety) as a .graphml file, which are convenient because they package all data loaded into the table (though support for their handling is not complete).
>	The Panels Menu:
		Force Panel:
			> Opens a panel to adjust the force parameters of the graph layout algorithm.
		Stats Panel:
			> Opens a panel that contains some simple graph statics about the current graph: the number of nodes, the number of edges, the graph density, average node degree measures, and eigenvector centrality. 
		Open Node Table:
		Open Edge Table:
			> These two buttons open panels to view the respective backing data tables. Some data (most String data) -- especially the "label" field for nodes -- is modifiable.	
>	The Control Panel (right) :
		Press the small arrows at the top of the divider to collapse or expand the panel.
		Connectivity Filter: The following filters act together, as if they're logical ANDed together.
			> EqProb: Hide nodes that have an equilibrium probability lower than the threshold value.
			> Distance: Filters visible nodes and edges by only displaying those within the specified number of edges away from the currently selected node.
		General Renderer Options:
			> Picture Mode: Toggle between high and low quality rendering. Low renders more quickly but looks less nice, mostly because low lacks anti-aliasing. The default is high quality.
			> Edge Type: Alternate between curved and straight edges. Curved edges make it easier to see two-way transitions and their relative probabilities, but tend to slow down rendering and clutter the view because two-way transitions aren't rendered on top of each other.
		Node Renderer:
			Changes the shape of the nodes. The two options are:
				> Label: rounded rectangles containing text labels corresponding to state "number"
				> Circle: Empty circle. This reflects the general tradition in literature.
		Run Control:
			Start or stop the graph layout algorithm. The stop setting is useful to position individual nodes or to make viewing easier or interaction more smooth. The algorithm must be showing to activlely "minimize" the graph or to realize force parameter changes fro the Force Panel.
		Image Controls:
			> Open Image: If images are provided, opens the image corresponding to the selected state and displays it in a new window at its original size.
			> Save Image: Opens a dialog box to save a raster image of the display. A range of output magnifications/resolutions and file formats are avaliable.
		Function Control:
			> Show Images: Places images corresponding to each node on top of the nodes, if and image directory was provided. Small text labels are also included on the nodes. 
			> Open a TPT Selector window to allow selection of source and target sets to run TPT on.
		Search:
			> Input a label. Will report if any matches were found and will center the screen on the highlighted matching node (if any).		
		Axis Controls:
			> X-Axis and Y-Axis: select which node data field to use to plot the nodes along the corresponding axis. "Load new..." allows users to add new data files (must be tab delimited, same length as number of nodes in graph) to plot, via a couple dialogs that ask for the new columns name, data type, and file source. 
			> Axis settings opens a dialog to modify some visual setting for the axis layout. For numerical column type axis (others are disabled in the dialog), the user can choose whether to set the layout bounds manually or have them calculated automatically to fit. Additionally, this panel can add axis labels, change label (and grid label) font size, and alter the spacing of gridlines.
			> Show Axis will run the axis layout for the axes specified in the drop-down menus and in Axis Settings. May be necessary to toggle this on an off to cause alterations to the axis settings to take effect.
	
		Aesthetic:
			> Hide Edges: don't show edges; useful for schematics (especially axis-based graphs) where edges are distracting.
			> Show Edges: show edges after they have been hidden. Does nothing if edges are already visible. Some other operatios (such a Run Layout or Edge Type) may also cause edges to re-display.
			> Force Panel: Open a panel to adjust the force parameters used to layout the graph automatically. Can result in better (especially more spaced-out) layouts or different interactive behavior.
			> Vis Settings: A whole boatload of visual tweaks possible here, including disabling self-edges, changing node color to scale with any data type, changing node size, edge weight, edge color, aggregate color, label font size, node shape, and more...see REFERENCE_TUTORIAL.pdf for more information
Hierarchy Controls (Upper left, visible only when hierarchy loaded):
		Level Slider: 
			> Indicates which level of the hierarchy is viewed as the current graph.
		Overlay Slider: 
			> Indicates which level of the hierarchy is overlayed on the graph indicated by level to show membership of the "level-indicated" graph in the overlaid graph.


KEY BINDINGS:
Note that these bindings apply to when the primary graph display is "in focus." You can cycle between elements by pressing TAB. These bindings will behave the same on the overview window, and are the same regardless of which OS the program is running on.
>	Ctrl-E: Export display to a raster image file. You have a choice between several resolutions and file formats. This should have the same functionality as the "Save Image" button.
>	Ctrl-H: Toggle between high and low rendering quality. The default is high. Low quality looks worse but renders more quickly, and thus make interacting with the graph a much smoother process with relatively large graphs. This should have the same effect as the "Picture Mode" toggle button.
>	Ctrl-D: Displays various Debug information, including frame rate and memory usage.
>	Ctrl-O: Opens a dialog to select a new graph to load. Note that loading a new graph discards the current graph.
>	Ctrl-Shift-O: Opens a dialog to select a new MSM hierarchy to load. Discards any currently loaded graphs.
>	Ctrl-S: Opens a dialog to save the current graph (in its entirety) in graphml format. 


\----=====TPT View=====----/

NOTE: The current algorithm is a forward greedy algorithm, so graphs will tend to have one singly high population bottleneck at the front of the graph (i.e. starting at the source into the mass of states).

INFORMATION ENCODED IN GRAPH:
> Edge weight is determined by total flux along that edge (sum of minimum flux for all paths involving that edge)
> Red stroke on node indicates the selected node
> Node color significance is determined relative to "Color Mode" (toggled by the "Color Mode" button)
	> For default mode: green is default color, blue indicates a fixed node
	> For alternate color mode: green is default, blue is target (sink), red is source.
> Nodes are sized much like edge weights, according to the reactive flux that travels through them.
> By default, nodes are layout on a graph; the y-axis is equilibrium probability and the nodes are spaced along the x-axis according to their "distance" from the source node (the sink nodes appear at far right, the source nodes are far left). Thus, moving the nodes around horizontally should still mean the graph doesn't lie about equilibrium probabilities (vertical is a different story).

BUTTONS AND GUI COMPONENTS:
> Show Axis: Toggle EqProb y-Axis visibility. Layout will hold after axis is disabled, and can then be moved around. When re-enabled, fixes the position of all nodes again. 
> Fix Position: fix the position of the currently selected node (indicated by red outline). This may be used to "anchor" a node to allow other nodes to layout while fixing specific nodes. The node may be dragged in this state. In default color mode, fixed nodes are blue. By default, everything is fixed, and things ought to be fixed when the graph is enabled. 
> Show Images: Show structure images on nodes, according to image location specified when the graph file was loaded. Node that the resolution and size of these images is dependent upon the zoom level at which the "Show Images" option was selected. Deselect and reselect this button to reload the images for the current zoom level. 
> Save Image: Export a raster image of the current display. A range of sizes and file formats are available. 
> Color Mode: Toggle between default and alternate color modes. See INFORMATION ENCODED IN GRAPH for information about these nodes.
> Force Panel: Open a force panel to adjust the force parameters of the layout. 
> Node Labels: Toggle the appearance of state labels on the nodes. Note that this works for while Show Images is selected, as well as when images are not showing, but that the effect is slightly different. When images are showing, the label is removed and the node resized accordingly. When images are not showing, the rectangular node is replaced with a filled circle.
> Num Paths: Enter the number of paths to display. Those with the highest reactive flux (according to the algorithm used) will be displayed first (i.e. setting this field to 5 will display the top 5 paths in terms of reactive flux).
> Edge Weight: This range slider controls the relative width of edges. The left arrow indicates the minimum edge weight, the right arrow indicates the maximum edge width. Note that force adjustments are useful to control the distance between nodes.
> Node Size: This range slider controls the relative size of nodes. The left arrow indicates the minimum node size, the right arrow indicates the maximum node size. Note that force adjustments are useful to control the distance between nodes.

KEY BINDINGS: 
Note that these bindings apply to when the primary graph display is "in focus." You can cycle between elements by pressing TAB. These bindings will behave the same on the overview window, and are the same regardless of which OS the program is running on.
>	Ctrl-E: Export display to a raster image file. You have a choice between several resolutions and file formats. This should have the same functionality as the "Save Image" button.
>	Ctrl-H: Toggle between high and low rendering quality. The default is high. Low quality looks worse but renders more quickly, and thus make interacting with the graph a much smoother process with relatively large graphs. This should have the same effect as the "Picture Mode" toggle button.
>	Ctrl-D: Displays various Debug information, including frame rate and memory usage.

Credits and Acknowledgments
------------------------
Primary Author: 
Bryce Cronkite-Ratcliff

General Acknowledgements:
Robert McGibbon
Trevor Gokey
Lorne Vanatta
Vincent Voelz
Vijay Pande
Imran Haque
Jeremy Lai
Gregory Bowman
Kyle Beauchamp
Pande Lab

Programming Acknowledgments:
Jeffrey Heer and the Prefuse Team
ownlife (PAP forums)
Robert Sedgwick and Kevin Wayne
34all (PAP forums) 
Nazri and Ashwin (sourceforge prefuse forums)
Luis Miguel Rodriguez (sourceforge prefuse forums)

MSMExplorer was developed on the Java Development Kit version 6 using The Prefuse Visualization Kit beta version 2007.10.21 (prefuse.org) and Netbeans versions 6.8 through 7.0.

