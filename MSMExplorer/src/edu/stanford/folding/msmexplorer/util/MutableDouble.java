/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.folding.msmexplorer.util;

/**
 *
 * @author gestalt
 */
public class MutableDouble {

	private Double d;

	public MutableDouble(String str) {
		d = new Double(str);
	}

	public MutableDouble(Double doub) {
		d = new Double(doub);
	}

	public void setValue(Double d) {
		d = new Double(d);
	}

	public Double getValue() {
		return d;
	}
}
