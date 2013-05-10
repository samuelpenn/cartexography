/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

import uk.org.glendale.hexweb.Hex
import uk.org.glendale.hexweb.MapInfo

/**
 * Services needed by Map.
 */
class MapService {

	/**
	 * Gets the map details for the map specified by unique id.
	 * 
	 * @param id	Numeric id.
	 */
	def getMapByNameOrId(int id) {
		return MapInfo.findById(id)
	}

	/**
	 * Gets the map details for the map specified by name. If the
	 * name is actually an integer, then call call the integer
	 * version of the method instead.
	 * 
	 * @param name	Name of the map, or its integer id.
	 */
    def getMapByNameOrId(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Map name cannot be null")
		}
		try {
			int id = Integer.parseInt(name)
			return getMapByNameOrId(id)	
		} catch (NumberFormatException e) {
			// Not an integer, so try by name.
		}
		return MapInfo.findByName(name)
    }
	
	/**
	 * Gets the hex at the specified coordinates for the map. If there is
	 * no hex defined, then null is returned.
	 * 
	 * @param info		Map to get hex for.
	 * @param x			X coordinate.
	 * @param y			Y coordinate.
	 */
	def getHex(MapInfo info, int x, int y) {
		return Hex.find ({
			eq("mapInfo", info)
			eq("x", x)
			eq("y", y)
		});
	}
	
}
