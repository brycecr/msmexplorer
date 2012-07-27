package edu.stanford.folding.msmexplorer.io;

import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.apache.batik.swing.svg.SVGFileFilter;
import prefuse.Display;
import prefuse.util.display.ScaleSelector;
import prefuse.util.io.IOLib;
import prefuse.util.io.SimpleFileFilter;

/**
 *
 * @author gestalt
 */
public class ExportMSMImageAction extends AbstractAction {

    private Display display;
    private JFileChooser chooser;
    private ScaleSelector scaler;

    /**
     * Create a new ExportDisplayAction for the given Display.
     * @param display the Display to capture
     */
    public ExportMSMImageAction(Display display) {
        this.display = display;
    }
    
    private void init() {
        scaler  = new ScaleSelector();
        chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle("Export Prefuse Display...");
        chooser.setAcceptAllFileFilterUsed(false);
        
        HashSet seen = new HashSet();
        String[] fmts = ImageIO.getWriterFormatNames();
        for ( int i=0; i<fmts.length; i++ ) {
            String s = fmts[i].toLowerCase();
            if ( s.length() == 3 && !seen.contains(s) ) {
                seen.add(s);
                chooser.setFileFilter(new SimpleFileFilter(s, 
                        s.toUpperCase()+" Image (*."+s+")"));
            }
        }
	chooser.setFileFilter(new SVGFileFilter());
        seen.clear(); seen = null;
        chooser.setAccessory(scaler);
    }
    
    /**
     * Shows the image export dialog and processes the results.
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        // lazy initialization
        if ( chooser == null ) {
		init();
	}
        
        // open image save dialog
        File f;
        scaler.setImage(display.getOffscreenBuffer());
        int returnVal = chooser.showSaveDialog(display);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
           f = chooser.getSelectedFile();
        } else {
            return;
        }
	FileFilter ff = chooser.getFileFilter();
	String format;
	if (ff instanceof SVGFileFilter) {
		format = "svg";
	} else {
        format =
            ((SimpleFileFilter)chooser.getFileFilter()).getExtension();
	}
        String ext = IOLib.getExtension(f);        
        if ( !format.equals(ext) ) {
            f = new File(f.toString()+"."+format);
        }
        
        double scale = scaler.getScale();
        
        // save image
        boolean success;
        try {
	    if (ff instanceof SVGFileFilter) {
		    success = SVGWriter.saveSVG(display, f);
	    } else {
		    OutputStream out = new BufferedOutputStream(
			    new FileOutputStream(f));
		    success = display.saveImage(out, format, scale);
		    out.flush();
		    out.close();
	    }
        } catch ( Exception e ) {
            success = false;
        }
        // show result dialog on failure
        if ( !success ) {
            JOptionPane.showMessageDialog(display,
                    "Error Saving Image!",
                    "Image Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
} // end of class ExportMSMImageAction
