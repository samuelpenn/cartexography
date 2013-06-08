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

class AdminController {
	/**
	 * Top level page of the site. Display a gallery of all the maps
	 * available, with option to create a new map.
	 */
	def index() {
		List<MapInfo> list = MapInfo.findAll({
			gt("template", 0)
		})
		
		render(view: "/index", model: [maps: list])
	}
	
	/**
	 * Create a new map. Provides a list of all the template maps.
	 */
	def create() {
		List<MapInfo> list = MapInfo.findAll({
			eq("template", 0)
		})
			
		
		render(view: "create", model: [maps: list])
	}
	
	def createMap() {
		println "createMap: ${params.name}"
		
		render "Done"
	}
}
