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
package edu.stanford.folding.msmexplorer.util.ui;

import prefuse.util.ui.JValueSlider;

/**
 * An idiotic class to allow public access to the fireChangeEvent method.
 * Presumably, the method was made protected for a reason, but the JValueSlier
 * also creates new ChangeEvents for every fire call (read: it doesn't keep 
 * any around), so they can't be retrieved with a getListeners
 * call & then used to invoke stateChanged.
 * 
 * Hopefully should be phased out if filter stacking is implemented properly.
 *
 * @author brycecr
 */
public class JValueSliderFlammable extends JValueSlider {

	public JValueSliderFlammable(String name, double min, double max, double param) {
		super(name, min, max, param);
	}
	public JValueSliderFlammable(String name, int min, int max, int param) {
		super(name, min, max, param);
	}

	public void fire() {
		fireChangeEvent();
	}
	
}
