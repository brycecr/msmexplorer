/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.stanford.folding.msmexplorer.util;

/**
 * Because sometimes you need to pass by reference.
 *
 * @author brycecr
 */
public class MutableDouble {

	private Double d;

	public MutableDouble(String str) {
		d = new Double(str);
	}

	public MutableDouble(Double doub) {
		d = new Double(doub);
	}

	public void setValue(Double doub) {
		d = new Double(doub);
	}

	public Double getValue() {
		return d;
	}
}
