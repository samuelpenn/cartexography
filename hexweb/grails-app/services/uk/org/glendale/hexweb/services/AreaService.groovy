/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

import uk.org.glendale.hexweb.Area
import uk.org.glendale.hexweb.MapInfo


/**
 * Provide services for managing named areas. Each hex tile on the map
 * can belong to a single area. Areas can have parents/children, and are
 * visibly denoted on the map by borders.
 */
class AreaService {

    def getAreaByName(MapInfo info, String name) {
		return Area.find ({
			eq("mapInfo", info)
			eq("name", name)
		});
    }
	
	/**
	 * Remove all named areas from the map. This does not unassign
	 * any tile data, so after calling this, tiles may point to areas
	 * which no longer exist.
	 * 
	 * @param info		Map to remove the areas from.
	 */
	def clearAreas(MapInfo info) {
		Area.findAll {
			eq("mapInfo", info)
		}.each {
			it.delete()
		}
	}
}
