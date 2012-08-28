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
package edu.stanford.folding.msmexplorer.util.render;

import prefuse.render.ImageFactory;
import prefuse.render.LabelRenderer;

/**
 * A simple subclass of {@link LabelRenderer} to allow for more efficient 
 * toggling of images on and off by not reassigning the ImageFactory to 
 * a new instance every time the imageField is set to null.
 * Just overrides all the LabelRenderer methods that cause new ImageFactories
 * to be created.
 * 
 * @author brycecr
 */
public class ImageToggleLabelRenderer extends LabelRenderer {

	/**
	 * Create a new LabelRenderer. By default the field "label" is used
	 * as the field name for looking up text, and no image is used.
	 */
	public ImageToggleLabelRenderer() {
		//initializes an ImageFactory whether or not one is ever
		//used, and keeps this image factory around
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
	 * Sets the data field for image locations. The value stored
	 * in the data field should be a URL, a file within the current classpath,
	 * a file on the filesystem, or null for no image. If the 
	 * <code>imageField</code> parameter is null, no images at all will be 
	 * drawn.
	 * @param imageFiled the data field for image locations, or null for
	 * no images
	 */
	@Override
	public void setImageField(String imageField) {
		m_imageName = imageField;
	}

	/** 
	 * Sets the maximum image dimensions, used to control scaling of loaded
	 * images. This scaling is enforced immediately upon loading of the image.
	 * @param width the maximum width of images (-1 for no limit)
	 * @param height the maximum height of images (-1 for no limit)
	 */
	@Override
	public void setMaxImageDimensions(int width, int height) {
		//if ( m_images == null ) m_images = new ImageFactory();
		m_images.setMaxImageDimensions(width, height);
	}
} // end of class ImageToggleLabelRenderer
