/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

import uk.org.glendale.hexweb.MapInfo
import uk.org.glendale.hexweb.Terrain

import org.codehaus.groovy.grails.commons.ConfigurationHolder


class TerrainService {
	def grailsApplication
	
	def getTerrainByNameOrId(int id) {
		return Terrain.findById(id)
	}

	def getTerrainByNameOrId(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Terrain name cannot be null")
		}
		try {
			int id = Integer.parseInt(name)
			return getTerrainByNameOrId(id)
		} catch (NumberFormatException e) {
			// Not an integer, so try by name.
		}
		return Terrain.findByName(name)
	}

	def getTerrainByNameOrId(MapInfo info, String name) {
		if (name == null) {
			throw new IllegalArgumentException("Terrain name cannot be null")
		}
		try {
			int id = Integer.parseInt(name)
			return getTerrainByNameOrId(id)
		} catch (NumberFormatException e) {
			// Not an integer, so try by name.
		}
		return Terrain.find ({
			eq("mapInfo", info)
			eq("name", name)
		})
	}

	def getTerrainFromName(MapInfo map, String name) {
		if (name == "null") {
			name = "xnull"
		}
		def confMap = grailsApplication.getFlatConfig()
		def img = confMap["mapcraft.terrain.${name}"] as String
		println "Converting [${name}] to [${img}]"
		Terrain terrain = getTerrainByNameOrId(map, img)
		if (terrain == null) {
			println "No terrain [${img}] found."
		}
		
		return terrain
	}
}
