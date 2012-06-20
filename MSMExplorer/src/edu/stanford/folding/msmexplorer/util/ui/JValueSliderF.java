/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author gestalt
 */
public class JValueSliderF extends JValueSlider {

	public JValueSliderF(String name, double min, double max, double param) {
		super(name, min, max, param);
	}
	public JValueSliderF(String name, int min, int max, int param) {
		super(name, min, max, param);
	}

	public void fire() {
		fireChangeEvent();
	}
	
}
