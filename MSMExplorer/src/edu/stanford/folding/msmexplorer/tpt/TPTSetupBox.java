/*
 * Copyright (C) 2012 Pande Lab
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package edu.stanford.folding.msmexplorer.tpt;

import java.util.Iterator;
import java.util.ArrayList;

import java.awt.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.Tuple;
import prefuse.util.ui.JSearchPanel;
import prefuse.data.search.KeywordSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.query.SearchQueryBinding;



/**
 * Frame to select source and target
 * Tuplesets to and initiate TPT run.
 *
 * @author brycecr
 */
public class TPTSetupBox {

	private List source;
	private List target;
	private ArrayList<Tuple> sourceSet;
	private ArrayList<Tuple> targetSet;
	private ArrayList<String> added;

	private final Visualization m_vis;

	public TPTSetupBox(final Graph g, boolean GraphExists) {
		this(g, GraphExists, null);
	}

	public TPTSetupBox(final Graph g, boolean GraphExists, final Visualization vis) {

		if (!GraphExists) {
			//Necessary so we are making sets of Nodes, not TableTuples
			//because the BFI in TPTWindow requires VisualItems
			m_vis = new Visualization();
			m_vis.addGraph("graph", g);
		} else {
			if (vis == null) {
				System.err.println("Null Visualization provided");
				System.exit(1);
			} 

			m_vis = vis;
		}
		
		this.source = new List();
		this.target = new List();
		this.sourceSet = new ArrayList<Tuple>();
		this.targetSet = new ArrayList<Tuple>();

		this.added = new ArrayList<String>();

		JScrollPane sourcePane = new JScrollPane(source);
		JScrollPane targetPane = new JScrollPane(target);


		final SearchTupleSet searchGroup = new KeywordSearchTupleSet();
		SearchQueryBinding sp = new SearchQueryBinding(g.getNodes(), "label", searchGroup);
		final JSearchPanel search = sp.createSearchPanel(true);
		search.setShowResultCount(true);

		final JButton runTPT = new JButton("Run TPT");
		runTPT.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				TPTWindow tpt = new TPTWindow(g, sourceSet, targetSet);
			}
		});
		runTPT.setEnabled(false);

		JButton addToSource = new JButton("<<< Source ");
		addToSource.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (searchGroup.getTupleCount() > 0
					&& !added.contains(searchGroup.getQuery())) {
					Iterator tuples = searchGroup.tuples();
					Tuple t = (Tuple)tuples.next();

					Tuple vt = m_vis.getVisualItem("graph.nodes", t);
					
					source.add(searchGroup.getQuery());
					sourceSet.add(vt);
					added.add("source");

					if (!targetSet.isEmpty())
						runTPT.setEnabled(true);
				}
			}
		});

		JButton addToTarget = new JButton("Target >>>");
		addToTarget.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (searchGroup.getTupleCount() > 0
					&& !added.contains(searchGroup.getQuery())) {
					Iterator tuples = searchGroup.tuples();
					Tuple t = (Tuple)tuples.next();

					Tuple vt = m_vis.getVisualItem("graph.nodes", t);

					target.add(searchGroup.getQuery());
					targetSet.add(vt);

					if (!sourceSet.isEmpty())
						runTPT.setEnabled(true);
				}
			}
		});

		JButton getSelected = new JButton("Get Selected");
		getSelected.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {

				Iterator itr = m_vis.getFocusGroup(Visualization.FOCUS_ITEMS).tuples();
				if (itr.hasNext()) {
					Tuple t = (Tuple)itr.next();
					search.setQuery(t.getString("label"));
				}
			}
		});
		getSelected.setVisible(GraphExists);

		JButton removeBtn = new JButton("Remove");
		removeBtn.addActionListener( new ActionListener () {
			public void actionPerformed( ActionEvent ae ) {
				int sInd = source.getSelectedIndex();
				int tInd = target.getSelectedIndex();

				if (sInd != -1) {
					sourceSet.remove(sInd);
					source.remove(sInd);
				}

				if (tInd != -1) {
					targetSet.remove(tInd);
					target.remove(tInd);
				}

				if (sourceSet.isEmpty() || targetSet.isEmpty())
					runTPT.setEnabled(false);

			}
		});

		


		Box topButtons = new Box(BoxLayout.X_AXIS);
		topButtons.add(addToSource);
		topButtons.add(removeBtn);
		topButtons.add(addToTarget);

		Box bottomButtons = new Box(BoxLayout.X_AXIS);
		bottomButtons.add(search);
		bottomButtons.add(getSelected);
		bottomButtons.add(runTPT);

		Box buttons = new Box(BoxLayout.Y_AXIS);
		buttons.add(topButtons);
		buttons.add(bottomButtons);

		Box panes = new Box(BoxLayout.X_AXIS);
		panes.add(sourcePane);
		panes.add(targetPane);

		JLabel sourceL = new JLabel("   Source");
		JLabel targetL = new JLabel("Target   ");

		Box labels = new Box(BoxLayout.X_AXIS);
		labels.add(sourceL);
		labels.add(Box.createHorizontalGlue());
		labels.add(targetL);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(buttons, BorderLayout.SOUTH);
		panel.add(panes, BorderLayout.CENTER);
		panel.add(labels, BorderLayout.NORTH);
		panel.setBackground(Color.WHITE);

		JFrame setupFrame = new JFrame("Set Source and Target States");
		setupFrame.add(panel);
		setupFrame.pack();
		setupFrame.setSize(500, 300);
		setupFrame.setVisible(true);
		setupFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
}
