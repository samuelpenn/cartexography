/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.api

import grails.converters.JSON
import uk.org.glendale.hexweb.MapInfo
import uk.org.glendale.hexweb.Terrain

/**
 * Controller for high level events, such as listing all the resources, or
 * creating new maps etc.
 */
class AppAPIController {
	
	def mapService
	def terrainService

	/**
	 * Gets a list of all the maps defined on this system. Information about each
	 * map is returned as well.
	 * 
	 * @return List of MapInfo objects as JSON.
	 */
    def info() { 
		List<MapInfo>	list = MapInfo.findAll()
		render list as JSON
	}
	
	/**
	 * Create a new map. A map can be based on another map, in which case the
	 * terrain, location and other icons will be shared from the parent. All
	 * standard (non-template) maps
	 * 
	 * This expects a POST request.
	 * 
	 * @param name		Unique name of the map. [a-z0-9_]
	 * @param title		Full descriptive title of the map.
	 * @param width		Width of the map, in hexes.
	 * @param height	Height of the map, in hexes.
	 * @param scale		Scale of each hex, in metres.
	 * @param template	Map to base this on.
	 * 
	 * @return			New MapInfo object as JSON
	 */
	def createMap(String name, String title, int width, int height, int scale, String template) {
		
		println "Creating a new map [${name}]"
		
		MapInfo		info = MapInfo.findByName(name)
		MapInfo		templateInfo = mapService.getMapByNameOrId(template)
		
		if (info != null) {
			println "Map [${name}] already exists"
			throw new IllegalStateException("A map with this name already exists")
		}
		if (!name.matches("[a-z][a-z0-9_]*")) {
			println "Name [${name}] is invalid"
			throw new IllegalArgumentException("Illegal map name, must be [a-z][a-z0-0_]*")
		}
		if (templateInfo == null) {
			println "Map must specify a template to be used"
			throw new IllegalArgumentException("Map must specify a template to be used")
		}
		if (width < 8 || height < 10) {
			println "Map must be at least 8x10 in size"
			throw new IllegalArgumentException("Map must be at least 8x10 hexes in size")
		}
		if (scale < 1) {
			println "Map must have a scale of at least 1"
		}
		println "Using template [${templateInfo.name}]"
		
		info = new MapInfo(name: name, title: title, width: width, height: height, scale: scale, world: false, template: templateInfo.id)
		info.save()
		
		render info as JSON
	}
	
	def test(String map, String terrain, String feature) {
		MapInfo	info = mapService.getMapByNameOrId(map)
		Terrain t = terrainService.getTerrainFromName(info, terrain, feature)
		
		render t.name
	}
}
