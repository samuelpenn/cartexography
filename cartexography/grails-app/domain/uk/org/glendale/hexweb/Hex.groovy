/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb

/**
 * A Hex is an individual tile on the map. Each hex has an x,y coordinate,
 * and there can only ever be zero or one hex with a particular coordinate
 * on a map.
 */
class Hex {
	MapInfo		mapInfo
	int			x
	int			y
	int			terrainId
	int			areaId
	int			variant

	Hex(Hex h) {
		this.mapInfo = h.mapInfo
		this.x = h.x
		this.y = h.y
		this.terrainId = h.terrainId
		this.areaId = h.areaId
	}
	
    static constraints = {
    }
	
	static mapping = {
		table "map"
		version false
		mapInfo column: "mapinfo_id"
		terrainId column: "terrain_id"
		areaId column: "area_id"
	}
}
