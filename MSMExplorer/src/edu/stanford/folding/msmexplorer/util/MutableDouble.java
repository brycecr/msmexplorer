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
