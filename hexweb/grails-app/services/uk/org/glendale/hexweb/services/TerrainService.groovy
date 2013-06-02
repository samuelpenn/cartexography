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

	def terrainCache = [:]
	
	/**
	 * Gets the type of terrain based on the Mapcraft terrain and feature type.
	 * Mappings are read from the Grails configuration file. If an exact match is
	 * not found, then a number of other alternatives are tried. Since Mapcraft
	 * uses two tile layers, and Hexweb only uses one, some mangling is required
	 * when performing the conversion. 
	 *
	 * @param map
	 * @param terrain
	 * @param feature
	 * @return
	 */
	def getTerrainFromName(MapInfo map, String terrain, String feature) {
		if (terrain == "null") {
			terrain = "xnull"
		}
		if (terrainCache["${terrain}.${feature}"] != null) {
			return terrainCache["${terrain}.${feature}"]
		}
		def confMap = grailsApplication.getFlatConfig()
		def img = confMap["mapcraft.terrain.${terrain}.${feature}"] as String
		if (img == null || img == "[:]") {
			img = confMap["mapcraft.terrain.${terrain}.clear"] as String
		}
		if (img == null || img == "[:]") {
			img = confMap["mapcraft.terrain.otherwise.${feature}"] as String
		}
		if (img == null || img == "[:]") {
			img = confMap["mapcraft.terrain.otherwise.clear"] as String
		}
		if (feature != "clear") {
			println "Converting [${terrain}+${feature}] to [${img}]"
		}
		Terrain t = getTerrainByNameOrId(map, img)
		if (t == null) {
			println "No terrain [${img}] found."
		}
		terrainCache["${terrain}.${feature}"] = t

		return t
	}
}
