/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.folding.msmexplorer.io;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import prefuse.data.Graph;

/**
 *
 * @author gestalt
 */
public class ColumnChooserDialog extends JDialog {

	private final Graph graph;
	private final JComboBox typeComboBox;
	private final JButton loadButton;
	private final JTextField nameField;
	private static final String[] types = { "Double", "Integer", "String", 
		"Boolean", "Float", "Long", "Date", "Other (Object)" };

	private boolean success = false;


	public ColumnChooserDialog(Frame f, Graph g) {
		super (f, "New Column Properties", true);
		graph = g;

		String[] options = new String[8];
		typeComboBox = new JComboBox(types); 
		nameField = new JTextField("name");
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
		String ret = MSMIOLib.applyNewlineDelimitedFile(graph, nameField.getText(), 
			getClassOf(typeComboBox.getSelectedIndex()));
		if (ret != null) {
			success = true;
		}
		setVisible(false);
		dispose();
	}                                          

	private Class<?> getClassOf(int selection) {
		switch (selection) {
			case 0:
				return int.class;
			case 1:
				return double.class;
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
