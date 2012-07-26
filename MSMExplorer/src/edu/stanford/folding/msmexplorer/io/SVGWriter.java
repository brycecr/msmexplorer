package edu.stanford.folding.msmexplorer.io;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.swing.svg.SVGFileFilter;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import prefuse.Display;

/**
 * A nifty class to output SVG files of prefuse graphs.
 * The annoying part is that we have to pull in 8 Batik jars
 * to do this, but that's probably the way to go.
 * For the record, requires batik jars:
 * codec, awt-util, dom, svggen, swing, util, ext, and xml
 * 
 * Adapted from Luis Miguel Rodriguez's code 
 * on the Prefuse Sourceforge forums.
 * @author Luis Miguel Rodriguez with adaptations to MSMExplorer by brycecr
 */
public class SVGWriter {

	private SVGWriter() {
		//prevent instantiation
	}

	/**
	 * Displays a dialog to save contents of display as .svg file
	 * 
	 * @param display to output as SVG
	 * @return success or failure of operation
	 */
	public static boolean saveSVG(Display display) {
		try {
			double scale = display.getScale();

			Dimension d = new Dimension((int) (scale * display.getWidth()),
				(int) (scale * display.getHeight()));

			// Get a DOMImplementation.
			DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

			// Create an instance of org.w3c.dom.Document.
			String svgNS = "http://www.w3.org/2000/svg";
			Document document = domImpl.createDocument(svgNS, "svg", null);

			// Create an instance of the SVG Generator.
			SVGGraphics2D svgG = new SVGGraphics2D(document);

			// set up the display, render, then revert to normal settings
			Point2D p = new Point2D.Double(0, 0);
			display.zoom(p, scale); // also takes care of damage report
			boolean q = display.isHighQuality();
			display.setHighQuality(true);
			display.paintDisplay(svgG, d);
			display.setHighQuality(q);
			display.zoom(p, 1 / scale); // also takes care of damage report

			// Finally, stream out SVG to the standard output using
			// UTF-8 encoding.
			boolean useCSS = true; // we want to use CSS style attributes

			JFileChooser jfc = new JFileChooser();
			jfc.setDialogTitle("Save SVG file...");
			jfc.setFileFilter(new SVGFileFilter());
			int ret = jfc.showSaveDialog(null);
			if (ret == JFileChooser.APPROVE_OPTION) {
				String saveLoc = jfc.getSelectedFile().getAbsolutePath();
				if (!(saveLoc.endsWith(".svg") || saveLoc.endsWith(".svgz"))) {
					saveLoc = saveLoc + ".svg";
				}
				svgG.stream(saveLoc, useCSS);
				return true;
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Could not save SVG file, exception: "
				+ex.getCause().toString()+" "+ex.toString(), "SVG save error", 
				JOptionPane.ERROR_MESSAGE);
			Logger.getLogger(SVGWriter.class.getName()).log(Level.SEVERE, null, ex);
		}		return false;
	}
}
