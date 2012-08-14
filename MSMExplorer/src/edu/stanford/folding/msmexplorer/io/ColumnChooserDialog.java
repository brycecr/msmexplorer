/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author gestalt
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
