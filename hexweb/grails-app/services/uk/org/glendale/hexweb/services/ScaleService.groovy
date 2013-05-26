/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

/**
 * Manages the scaling of one map into another. For example, if the
 * source map has a scale of 1 hex = 25km, and the destination map has
 * a scale of 1 hex = 5km, then each hex in the source must be translated
 * to 25 hexes in the destination.
 */
class ScaleService {

	/**
	 * Returns a list of X/Y coordinates for the destination map.
	 * The destination map is assumed to be a smaller scale, i.e.
	 * one hex in the source translates into multiple hexes in the
	 * destination.
	 * 
	 * Needs to account for the staggered nature of hex maps, and
	 * edge conditions. Assume edges of the new map replicate the
	 * contents of the nearest hex.
	 * 
	 * @param x				X coordinate in the source map.
	 * @param y				Y coordinate in the destination map.
	 * @param srcScale		Size of each hex in the source.
	 * @param destScale		Size of each hex in the destination.
	 * @return				List of x/y coordinates.
	 */
    def getScaledHexes(int x, int y, int srcScale, int destScale) {
		if (srcScale < destScale) {
			throw new IllegalArgumentException("Destination map must be larger")
		} else if (srcScale % destScale != 0) {
			throw new IllegalArgumentException("Scales must be exactly divisibile")
		} else if (srcScale == destScale) {
			// Trivial case, they are the same.
			return [ "x": x, "y": y ]
		} else if (srcScale == destScale * 2) {
			return getScale2(x, y);
		}
		
		
    }
	
	def getScale2(int x, int y) {
		int offset = 0;
		if (x % 2 != 0) {
			offset = 1
		}
		List h = [
			[ "x" : x*2, "y": y*2 + offset ],
			[ "x" : x*2, "y": y*2+1 + offset ],
			[ "x" : x*2+1, "y": y*2 + offset ],
			[ "x" : x*2+1, "y": y*2+1 + offset ]
		 ]
		
		if (y == 0 && offset == 1) {
			h.add([ "x": x*2, "y": 0 ])
			h.add([ "x": x*2+1, "y": 0 ])
		}

		return h;
	}
}
