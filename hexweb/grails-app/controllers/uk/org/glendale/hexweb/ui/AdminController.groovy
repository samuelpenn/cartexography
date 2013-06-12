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
	def mapService

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
		
		def name = params.name as String
		def title = params.title as String
		def width = params.width as int
		def height = params.height as int
		def scale = params.scale as int
		def template = params.template as String
		def world = params.world as boolean
		
		if (template == null) {
			println "Template not set"
			throw new IllegalArgumentException("Must specify a template name or id")
		}
		
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
		if (title == null || title.trim().length() == 0) {
			println "Title [${title}] is invalid"
			title = title.trim()
			throw new IllegalArgumentException("Map must have a title")
		}
		if (templateInfo == null) {
			println "Map must specify a template to be used"
			throw new IllegalArgumentException("Map must specify a valid template to be used")
		}
		if (width < 8 || height < 10) {
			println "Map must be at least 8x10 in size"
			throw new IllegalArgumentException("Map must be at least 8x10 hexes in size")
		}
		if (scale < 1) {
			println "Map must have a scale of at least 1"
		}
		println "Using template [${templateInfo.name}]"
		
		if (world) {
			// If a world map, then there are restrictions on the size.
			if (width%22 > 0) {
				width += (22 - width % 22)
			}
			height = width / 2
		}
		
		info = new MapInfo(name: name, title: title, width: width, height: height, scale: scale, world: world, template: templateInfo.id)
		info.save()

		
		render "Done"
	}
}
