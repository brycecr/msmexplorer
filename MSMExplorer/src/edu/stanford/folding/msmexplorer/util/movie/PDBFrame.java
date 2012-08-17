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
package edu.stanford.folding.msmexplorer.util.movie;

import java.util.Iterator;
import java.util.Vector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.List;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Edge;
import prefuse.data.Tuple;
import prefuse.Visualization;
import prefuse.data.tuple.TupleSet;
import prefuse.Constants;

/**
 * Frame to create simulation movies by
 * concatenating existing pdb files.
 *
 * @author brycecr
 */
public class PDBFrame
{

    private final Graph m_graph;
    private final Visualization m_vis;
    private Integer m_startNodeID;
    private Integer m_endNodeID;
    JFormattedTextField m_startTextField;
    JFormattedTextField m_endTextField;
    JTextField m_answerTextField;
    private final int row_offset;

    public PDBFrame(Graph g, Visualization vis)
    {

        this.m_vis = vis;
        this.m_graph = g;

	row_offset = Integer.parseInt(g.getNode(0).getString("label"));

        m_startTextField = new JFormattedTextField();
        m_startTextField.setColumns(4);

        JLabel startLabel = new JLabel("Start:");
        startLabel.setLabelFor(m_startTextField);

        m_endTextField = new JFormattedTextField();
        m_endTextField.setColumns(4);

        JLabel endLabel = new JLabel("Finish:");
        endLabel.setLabelFor(m_endTextField);

        m_answerTextField = new JTextField();
        m_answerTextField.setColumns(4);

        JButton make = new JButton("Make Movie");
        make.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent ae)
            {
                try
                {
                    m_startNodeID = Integer.parseInt(m_startTextField.getText());
                    m_endNodeID = Integer.parseInt(m_endTextField.getText());
                } catch (NullPointerException e)
                {
                }
                runDijkstra(m_startNodeID, m_endNodeID);
            }
        });

        JButton clear = new JButton("Clear");
        clear.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent ae)
            {
                m_startNodeID = m_endNodeID = 0;
            }
        });


        JButton setStart = new JButton("Set Start");
        setStart.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String tmp;
                TupleSet focusGroup = m_vis.getGroup(Visualization.FOCUS_ITEMS);
                Iterator tuples = focusGroup.tuples();
                while (tuples.hasNext()) {
                    tmp = ((Tuple) tuples.next()).getString("label");
                    m_startNodeID = Integer.parseInt(tmp);
                    m_startTextField.setValue(m_startNodeID);
                }
            }
        });

        JButton setFinish = new JButton("Set Finish");
        setFinish.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String tmp;
                TupleSet focusGroup = m_vis.getGroup(Visualization.FOCUS_ITEMS);
                Iterator tuples = focusGroup.tuples();
                //while (tuples.hasNext()) {
                tmp = ((Tuple) tuples.next()).getString("label");
                m_endNodeID = Integer.parseInt(tmp);
                m_endTextField.setValue(m_endNodeID);
                //}
            }
        });


        Box box = new Box(BoxLayout.X_AXIS);
        box.add(make);
        box.add(setStart);
        box.add(setFinish);
        box.add(clear);
        Box box2 = new Box(BoxLayout.X_AXIS);
        box2.add(startLabel);
        box2.add(m_startTextField);
        box2.add(endLabel);
        box2.add(m_endTextField);

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(box2, BorderLayout.NORTH);
        panel.add(m_answerTextField, BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);

        JFrame frame = new JFrame("Movie Maker");
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(325, 115);
    }

    private void concatPDBFiles(String[] list)
    {
        for (int i = 0; i < list.length; ++i)
        {
            //TODO: Concatenate PDB Files
        }
    }

    private void runDijkstra(Integer start, Integer end)
    {
        int count = m_graph.getNodeCount();
        Node currentNode = m_graph.getNode(start - row_offset);
        Integer currentNodeID = start - row_offset;
        Integer targetNodeID;
        Edge edge;
        Iterator edges;
        Float currentProb = 0.0f;
        Float prevProb = 0.0f;
        Float currentSigmaProb = 0.0f;
        int max = 0;
        Vector<Float> sigmaProbability = new Vector<Float>(count);
        Vector<Integer> previousNode = new Vector<Integer>(count);
        Boolean used[] = new Boolean[m_graph.getNodeCount()];

        for (int i = 0; i < count; ++i)
        {
            sigmaProbability.add(i, new Float(-1));
            previousNode.add(i, new Integer(-1));
            used[i] = false;
        }
        /*
         * go through each edge, and construct path to each node. -1 means
         * uninitialized, so its safe to replace with the current value.
         * If there is a value present, only replace if it is better (higher
         * probability).
         */
        while (currentNodeID != end)
        {
            edges = currentNode.outEdges();
            while (edges.hasNext())
            {
                edge = (Edge) edges.next();
                targetNodeID = Integer.parseInt(
                        edge.getTargetNode().getString("label")) - row_offset;
                currentProb = Float.parseFloat(edge.getString("probability"));
                currentSigmaProb = sigmaProbability.elementAt(targetNodeID);
                if (sigmaProbability.elementAt(targetNodeID) == -1)
                {
                    sigmaProbability.setElementAt(currentProb,
                            targetNodeID);
                    previousNode.setElementAt(currentNodeID, targetNodeID);
                    used[targetNodeID] = false;
                } 
                else
                {
                    prevProb = sigmaProbability.elementAt(currentNodeID);
                    if (prevProb * currentProb > currentSigmaProb)
                    {
                        sigmaProbability.setElementAt(prevProb * currentProb,
                                targetNodeID);
                        previousNode.setElementAt(currentNodeID, targetNodeID);
                        used[targetNodeID] = false;
                    }
                }
            }

            /*
            go to the first 'unused' value in the array. note that 'max' is
            used improperly here and was just done avoid additional variables.
             */
            max = -1;
            do
            {
                max++;
            } 
            while (used[max]);

            for (int i = row_offset; i < count; ++i)
            {
                if (used[i])
                {
                    continue;
                }
                max = sigmaProbability.elementAt(i - row_offset)
                        > sigmaProbability.elementAt(max) ? i : max;
            }
            /*found the most probably node to jump to..*/
            currentNode = m_graph.getNode(max - row_offset);
            currentNodeID = max;
            used[max] = true;
        }

        /*
        This is actually needed if someone puts "0 to 0" as most nodes seen
        so far are connected to themselves so it's technically valid.
         */
        Integer index = end - row_offset;
        String answer = end.toString() + " <- ";
        Integer node = 0;
        while (previousNode.elementAt(index) != start
                && previousNode.elementAt(index) != -1)
        {
            node = previousNode.elementAt(index);
            answer += node.toString() + " <- ";
            index = node;
        }
        answer += start.toString();
        m_answerTextField.setText(answer);
    }
}
