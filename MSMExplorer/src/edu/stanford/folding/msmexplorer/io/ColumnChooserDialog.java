/*
 * Copyright (C) 2012 Stanford University
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
package edu.stanford.folding.msmexplorer.io;

import edu.stanford.folding.msmexplorer.MSMConstants;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import prefuse.Visualization;
import prefuse.data.tuple.TupleSet;

/**
 * A simple dialog to add new data columns to the MSM
 *
 * @author brycecr
 */
public class ColumnChooserDialog extends JDialog implements MSMConstants {

	private final Visualization m_vis;
	private final JComboBox typeComboBox;
	private final JButton loadButton;
	private final JTextField nameField;
	private final JComboBox groupField;
	private static final String[] types = { "Double", "Integer", "String", 
		"Boolean", "Float", "Long", "Date", "Other (Object)" };

	private boolean success = false;


	public ColumnChooserDialog(Frame f, Visualization vis, String group) {
		super (f, "New Column Properties", true);
		m_vis = vis;

		Vector<String> visFields = new Vector<String>(4);
		visFields.add(NODES);
		visFields.add(EDGES);
		if (m_vis.getGroup(AGGR) != null) {
			visFields.add(AGGR);
		}

		String[] options = new String[8];
		typeComboBox = new JComboBox(types); 
		nameField = new JTextField("name");
		groupField = new JComboBox(visFields);
		if (group != null && visFields.contains(group)) {
			groupField.setEnabled(false);
			groupField.setSelectedItem(group);
		}
		loadButton = new JButton("Load File");
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				loadButtonActionPerformed(ae);
				setVisible(false);
				dispose();
			}
		});

		this.setLayout(new GridLayout(0,2));
		this.add(new JLabel("Column Name: "));
		this.add(nameField);
		this.add(new JLabel("Column Type: "));
		this.add(typeComboBox);
		this.add(new JLabel("Vis Group: "));
		this.add(groupField);
		this.add(new JLabel());
		this.add(loadButton);
		this.pack();
	}

	public String showDialog() {
		setLocationByPlatform(true);
		setVisible(true);
		if (success == true) {
			return nameField.getText();
		} else {
			return null;
		}
	}

	private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
		String group = (String) groupField.getSelectedItem();
		String ret = null;
		if (group.equals(NODES) || group.equals(AGGR)) {
			TupleSet g;
			if (group.equals(AGGR)) {
				g = m_vis.getGroup(AGGR);
			} else if (group.equals(NODES)) {
				g = m_vis.getSourceData(NODES);
			} else {
				g = null;
				assert false;
			}
			ret = MSMIOLib.applyNewlineDelimitedFile(g, nameField.getText(), getClassOf(typeComboBox.getSelectedIndex()));
		} else if (group.equals(EDGES)) {
			ret = MSMIOLib.applyMatrixFile(null, null, m_vis.getSourceData((String)groupField.getSelectedItem()), nameField.getText(), getClassOf(typeComboBox.getSelectedIndex()));
		}
		if (ret != null) {
			success = true;
			if (!group.equals(NODES)) {
				ret = null;
			}
		} 
		setVisible(false);
		dispose();
	}                                          

	private Class<?> getClassOf(int selection) {
		switch (selection) {
			case 0:
				return double.class;
			case 1:
				return int.class;
			case 2:
				return String.class;
			case 3:
				return boolean.class;
			case 4:
				return float.class;
			case 5:
				return long.class;
			case 6:
				return Date.class;
			case 7:
			default:
				return Object.class;
		}
	}

	
}
