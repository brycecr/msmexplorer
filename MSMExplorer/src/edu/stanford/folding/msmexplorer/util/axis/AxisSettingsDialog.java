package edu.stanford.folding.msmexplorer.util.axis;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import prefuse.data.query.NumberRangeModel;
import prefuse.util.FontLib;

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

	private static final Integer[] fontSizes = {4,6,8,10,12,14,16,28,20,24,28,32,26,40,48,50,56,64,72,
	84,96,110,130,150,170,200,240,280,320,360,400,450,500};

	/**
	 * Constructor initializes a Dialog. Use showDialog to make visible.
	 * The range models and the JLabels will be modified by reference. If
	 * the user decides to change anything.
	 * 
	 * @param f parent frame. Can be null without much issue.
	 * @param xAxis the numerical range model that backs the X axis
	 * @param yAxis the numerical range model that backs the Y axis
	 * @param xType type of the data values for the X axis range model
	 * @param yType type of the data values for the Y axis range model
	 * @param autoRange is the axis range currently automatically set?
	 * @param xlab X Axis overall label. To ignore, put in a new JLabel()
	 * @param ylab Y Axis overall label. To ignore, put in a new JLabel()
	 */
	public AxisSettingsDialog(Frame f, final NumberRangeModel xAxis, NumberRangeModel yAxis, 
			final Class<?> xType, final Class<?> yType, boolean autoRange, final JLabel xlab, final JLabel ylab) {
		super(f, true); // set modal
		x = xAxis;
		y = yAxis;
		auto = autoRange;

		//Initialize all swing components
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
		final JTextField xlabField = new JTextField(xlab.getText());
		final JTextField ylabField = new JTextField(ylab.getText());
		final JComboBox xlabSize = new JComboBox(fontSizes);
		xlabSize.setSelectedItem(xlab.getFont().getSize());
		xlabSize.setEditable(true);
		final JComboBox ylabSize = new JComboBox(fontSizes);
		ylabSize.setSelectedItem(ylab.getFont().getSize());
		ylabSize.setEditable(true);

		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
				dispose();
			}
		});

		//initialize close behavior (ok/apply vs cancel)
		//note that we pass a whole bunch of information back
		//by reference
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
					xlab.setText(xlabField.getText());
					if ((Integer)xlabSize.getSelectedItem() > 0) {
						xlab.setFont(xlab.getFont().deriveFont(((Integer)xlabSize.getSelectedItem()).floatValue()));
					}
					ylab.setText(ylabField.getText());
					if ((Integer)ylabSize.getSelectedItem() > 0) {
						ylab.setFont(ylab.getFont().deriveFont(((Integer)ylabSize.getSelectedItem()).floatValue()));
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
	
		//Add parameters to dialog. Yes, the dialog is a bit ugly...
		//If you're feeling artistic, please do fix...
		setLayout(new GridLayout(0,4));
		add(autoCheckBox);
		add(new JLabel());
		JLabel lab1 = new JLabel("Note: Limits for non-numerical");
		lab1.setFont(FontLib.getFont("Tahoma", 11));
		JLabel lab2 = new JLabel(" datatypes not shown.");
		lab2.setFont(FontLib.getFont("Tahoma", 11));
		add(lab1);
		add(lab2);
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
		add(new JLabel());
		add(new JLabel());
		add(new JLabel("X Label: "));
		add(new JLabel("Size:"));
		add(new JLabel("Y Label: "));
		add(new JLabel("Size:"));
		add(xlabField);
		add(xlabSize);
		add(ylabField);
		add(ylabSize);
		add(new JLabel());
		add(new JLabel());
		add(cancelButton);
		add(okButton);
		pack();
	}

	/**
	 * Make the dialog visible. Note that the
	 * dialog is modal.
	 * 
	 * @return whether the axis bounds are set automatically
	 */
	public boolean showDialog() {
		setLocationByPlatform(true);
		setVisible(true);
		return auto;
	}

	/**
	 * We override this method in order to 
	 * disable it, because this will definitely mess
	 * up the dialog...
	 * 
	 * @param modal 
	 */
	@Override
	public void setModal(boolean modal) {
		throw new UnsupportedOperationException("Cannot set AxisSettingsDialog modality");
	}
}
