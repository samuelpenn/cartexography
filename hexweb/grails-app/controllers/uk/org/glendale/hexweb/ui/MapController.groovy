/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.ui

import uk.org.glendale.hexweb.MapInfo



class MapController {
	def mapService

    def editMap(String id) {
		MapInfo info = mapService.getMapByNameOrId(id)
		
		if (info == null) {
			throw new IllegalArgumentException("Map [${id}] not found")
		}
		
		println "view: ${id}"
		render(view: "index", model: [mapInfo: info]) 
	}
}
