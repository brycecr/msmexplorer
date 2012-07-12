package edu.stanford.folding.msmexplorer.util.render;


import prefuse.render.ImageFactory;
import prefuse.render.LabelRenderer;


/**
 * A simple subclass of {@link LabelRenderer} to allow for more efficient 
 * toggling of images on and off by not reassigning the ImageFactory to 
 * a new instance every time the imageField is set to null.
 * 
 * @author brycecr
 */
public class ImageToggleLabelRenderer extends LabelRenderer {
    /**
     * Create a new LabelRenderer. By default the field "label" is used
     * as the field name for looking up text, and no image is used.
     */
    public ImageToggleLabelRenderer() {
		m_images = new ImageFactory();
    }
    
    /**
     * Returns the image factory used by this renderer.
     * @return the image factory
     */
	@Override
    public ImageFactory getImageFactory() {
        return m_images;
    }
    
    /**
     * Sets the image factory used by this renderer.
     * @param ifact the image factory
     */
	@Override
    public void setImageFactory(ImageFactory ifact) {
        m_images = ifact;
    }
    
} // end of class ImageToggleLabelRenderer
