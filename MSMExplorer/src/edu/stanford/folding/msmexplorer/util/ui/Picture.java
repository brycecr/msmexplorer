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
package edu.stanford.folding.msmexplorer.util.ui;

/*************************************************************************
 *  Compilation:  javac Picture.java
 *  Execution:    java Picture imagename
 *
 *  Data type for manipulating individual pixels of an image. The original
 *  image can be read from a file in jpg, gif, or png format, or the
 *  user can create a blank image of a given size. Includes methods for
 *  displaying the image in a window on the screen or saving to a file.
 *
 *  % java Picture mandrill.jpg
 *
 *  Remarks
 *  -------
 *   - pixel (x, y) is column x and row y, where (0, 0) is upper left
 *
 *   - see also GrayPicture.java for a grayscale version
 *
 *************************************************************************/
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 *  This class provides methods for manipulating individual pixels of
 *  an image. The original image can be read from a file in JPEG, GIF,
 *  or PNG format, or the user can create a blank image of a given size.
 *  This class includes methods for displaying the image in a window on
 *  the screen or saving to a file.
 *  <p>
 *  By default, pixel (x, y) is column x, row y, where (0, 0) is upper left.
 *  The method setOriginLowerLeft() change the origin to the lower left.
 *  <p>
 *  For additional documentation, see
 *  <a href="http://www.cs.princeton.edu/introcs/31datatype">Section 3.1</a> of
 *  <i>Introduction to Programming in Java: An Interdisciplinary Approach</i>
 *  by Robert Sedgewick and Kevin Wayne.
 */
public final class Picture implements ActionListener {

	private BufferedImage image;               // the rasterized image
	private JFrame frame;                      // on-screen view
	private String filename;                   // name of file
	private boolean isOriginUpperLeft = true;  // location of origin
	private int width, height;                 // width and height
	protected boolean found = true;

	/**
	 * Create a blank w-by-h picture, where each pixel is black.
	 */
	public Picture(int w, int h) {
		width = w;
		height = h;
		image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		// set to TYPE_INT_ARGB to support transparency
		filename = w + "-by-" + h;
	}

	/**
	 * Create a picture by reading in a .png, .gif, or .jpg from
	 * the given filename or URL name.
	 */
	public Picture(String filename) {
		this.filename = filename;
		try {
			// try to read from file in working directory
			File file = new File(filename);
			if (file.isFile()) {
				image = ImageIO.read(file);
			} // now try to read from file in same directory as this .class file
			else {
				URL url = getClass().getResource(filename);
				if (url == null) {
					url = new URL(filename);
				}
				image = ImageIO.read(url);
			}
			width = image.getWidth(null);
			height = image.getHeight(null);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "No image for this node found. "
				+ "Tried to find image at "+filename, 
				"No Image Found", JOptionPane.ERROR_MESSAGE);
			found = false;
		}

		// check that image was read in
		if (image == null && found) {
			JOptionPane.showMessageDialog(null, "No image for this node found. "
				+ "Tried to find image at "+filename, 
				"No Image Found", JOptionPane.ERROR_MESSAGE);
			found = false;
		}
	}

	/**
	 * Create a picture by reading in a .png, .gif, or .jpg from a File.
	 */
	public Picture(File file) {
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not open file: " + file);
		}
		if (image == null) {
			throw new RuntimeException("Invalid image file: " + file);
		}
	}

	/**
	 * Return a JLabel containing this Picture, for embedding in a JPanel,
	 * JFrame or other GUI widget.
	 */
	public JLabel getJLabel() {
		if (image == null) {
			return null;
		}         // no image available
		ImageIcon icon = new ImageIcon(image);
		return new JLabel(icon);
	}

	/**
	 * Set the origin to be the upper left pixel.
	 */
	public void setOriginUpperLeft() {
		isOriginUpperLeft = true;
	}

	/**
	 * Set the origin to be the lower left pixel.
	 */
	public void setOriginLowerLeft() {
		isOriginUpperLeft = false;
	}

	/**
	 * Display the picture in a window on the screen.
	 */
	public void show() {

		if (found == false) {
			return;
		}
		// create the GUI for viewing the image if needed
		if (frame == null) {
			frame = new JFrame();

			JMenuBar menuBar = new JMenuBar();
			JMenu menu = new JMenu("File");
			menuBar.add(menu);
			JMenuItem menuItem1 = new JMenuItem(" Save...   ");
			menuItem1.addActionListener(this);
			menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			menu.add(menuItem1);
			frame.setJMenuBar(menuBar);



			frame.setContentPane(getJLabel());
			// f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setTitle(filename);
			frame.setBackground(Color.black);
			frame.setResizable(false);
			frame.pack();
			frame.setVisible(true);
		}

		// draw
		frame.repaint();
	}

	/**
	 * Return the height of the picture in pixels.
	 */
	public int height() {
		return height;
	}

	/**
	 * Return the width of the picture in pixels.
	 */
	public int width() {
		return width;
	}

	/**
	 * Return the color of pixel (i, j).
	 */
	public Color get(int i, int j) {
		if (isOriginUpperLeft) {
			return new Color(image.getRGB(i, j));
		} else {
			return new Color(image.getRGB(i, height - j - 1));
		}
	}

	/**
	 * Set the color of pixel (i, j) to c.
	 */
	public void set(int i, int j, Color c) {
		if (c == null) {
			throw new RuntimeException("can't set Color to null");
		}
		if (isOriginUpperLeft) {
			image.setRGB(i, j, c.getRGB());
		} else {
			image.setRGB(i, height - j - 1, c.getRGB());
		}
	}

	/**
	 * Save the picture to a file in a standard image format.
	 * The filetype must be .png or .jpg.
	 */
	public void save(String name) {
		save(new File(name));
	}

	/**
	 * Save the picture to a file in a standard image format.
	 */
	public void save(File file) {
		this.filename = file.getName();
		if (frame != null) {
			frame.setTitle(filename);
		}
		String suffix = filename.substring(filename.lastIndexOf('.') + 1);
		suffix = suffix.toLowerCase();
		if (suffix.equals("jpg") || suffix.equals("png")) {
			try {
				ImageIO.write(image, suffix, file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			assert false;
		}
	}

	/**
	 * Opens a save dialog box when the user selects "Save As" from the menu.
	 */
	public void actionPerformed(ActionEvent e) {
		FileDialog chooser = new FileDialog(frame,
			"Use a .png or .jpg extension", FileDialog.SAVE);
		chooser.setVisible(true);
		if (chooser.getFile() != null) {
			save(chooser.getDirectory() + File.separator + chooser.getFile());
		}
	}

	/**
	 * Test client. Reads a picture specified by the command-line argument,
	 * and shows it in a window on the screen.
	 */
	public static void main(String[] args) {
		Picture pic = new Picture("/images/macro1.png");
		pic.show();
	}
}
