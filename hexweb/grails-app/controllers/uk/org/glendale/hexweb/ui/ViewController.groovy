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

/**
 * Controller to allow a map to be displayed in a view only mode. No editing
 * functionality is possible through this controller.
 */
class ViewController {
	def mapService
	
	def viewMap(String id, int zoom, int x, int y) {
		MapInfo info = mapService.getMapByNameOrId(id)
		
		if (info == null) {
			throw new IllegalArgumentException("Map [${id}] not found")
		}
		
		println "view: ${id}"
		render(view: "index", model: [mapInfo: info, zoom: zoom, x: x, y: y])
	}
}
