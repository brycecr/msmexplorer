package edu.stanford.folding.msmexplorer.util.ui;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import prefuse.data.query.NumberRangeModel;

/**
 * Dialog to allow users to control various aspects of the axis layout, including
 * axis ranges, axis and graph labels, etc.
 * 
 * @author brycecr
 */
public class AxisSettingsDialog extends JDialog {

	private final NumberRangeModel x;
	private final NumberRangeModel y;
	private boolean auto;

	public AxisSettingsDialog(Frame f, final NumberRangeModel xAxis, NumberRangeModel yAxis, 
			final Class<?> xType, final Class<?> yType, boolean autoRange) {
		super(f, true); // set modal
		x = xAxis;
		y = yAxis;
		auto = autoRange;

		final JCheckBox autoCheckBox = new JCheckBox("Auto Set Bounds", auto);
		final JTextField xMin = new JTextField();
		xMin.setText(""+xAxis.getLowValue());
		final JTextField xMax = new JTextField();
		xMax.setText(""+xAxis.getHighValue());
		final JTextField yMin = new JTextField();
		yMin.setText(""+yAxis.getLowValue());
		final JTextField yMax = new JTextField();
		yMax.setText(""+yAxis.getHighValue());
		final JButton okButton = new JButton("Ok");
		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
				dispose();
			}
		});

		okButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (okButton.isEnabled()) {
					auto = autoCheckBox.isSelected();
					if (!yMin.isVisible() && !xMin.isVisible()) {
						auto = true;
					}
					if (!auto) {
						if (xMin.isVisible()) {
							if (xType == double.class || xType == float.class) {
								x.setLowValue(Double.parseDouble(xMin.getText()));
								x.setHighValue(Double.parseDouble(xMax.getText()));
								x.setMinValue(Double.parseDouble(xMin.getText()));
								x.setMaxValue(Double.parseDouble(xMax.getText()));
								
							} else if (xType == int.class || xType == long.class) {
								x.setLowValue(Long.parseLong(xMin.getText()));
								x.setHighValue(Long.parseLong(xMax.getText()));
								x.setMinValue(Long.parseLong(xMin.getText()));
								x.setMaxValue(Long.parseLong(xMax.getText()));
							}
						}
						
						if (yMin.isVisible()) {
							if (yType == double.class || yType == float.class) {
								y.setLowValue(Double.parseDouble(yMin.getText()));
								y.setHighValue(Double.parseDouble(yMax.getText()));
								y.setMinValue(Double.parseDouble(yMin.getText()));
								y.setMaxValue(Double.parseDouble(yMax.getText()));
								
							} else if (yType == int.class || yType == long.class) {
								y.setLowValue(Long.parseLong(yMin.getText()));
								y.setHighValue(Long.parseLong(yMax.getText()));
								y.setMinValue(Long.parseLong(yMin.getText()));
								y.setMaxValue(Long.parseLong(yMax.getText()));
							}
						}
					}
				}
				setVisible(false);
				dispose();
			}
		});

		autoCheckBox.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				boolean enable = true;
				if (autoCheckBox.isSelected()) {
					enable = false;
				} 
				xMin.setEnabled(enable);
				xMax.setEnabled(enable);
				yMin.setEnabled(enable);
				yMax.setEnabled(enable);
			}
		});
		if (!(xType == double.class || xType == float.class
			|| xType == int.class || xType == long.class)) {
			xMin.setVisible(false);
			xMax.setVisible(false);
		}
		if (!(yType == double.class || yType == float.class
			|| yType == int.class || yType == long.class)) {
			yMin.setVisible(false);
			yMax.setVisible(false);
		}
		xMin.setEnabled(!auto);
		xMax.setEnabled(!auto);
		yMin.setEnabled(!auto);
		yMax.setEnabled(!auto);
		
		setLayout(new GridLayout(0,4));
		add(autoCheckBox);
		add(new JLabel());
		add(new JLabel());
		add(new JLabel());
		add(new JLabel("X Min:"));
		add(xMin);
		add(new JLabel("X Max:"));
		add(xMax);
		add(new JLabel("Y Min:"));
		add(yMin);
		add(new JLabel("Y Max:"));
		add(yMax);
		add(new JLabel());
		add(new JLabel());
		add(cancelButton);
		add(okButton);
		pack();
	}

	public boolean showDialog() {
		setVisible(true);
		return auto;
	}
}
