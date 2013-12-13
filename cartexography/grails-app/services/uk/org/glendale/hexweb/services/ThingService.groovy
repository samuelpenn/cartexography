/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

import uk.org.glendale.hexweb.MapInfo;
import uk.org.glendale.hexweb.Thing
import uk.org.glendale.hexweb.Place

class ThingService {
	def grailsApplication

	def getThingByNameOrId(int id) {
		return Thing.findById(id)
	}

	def getThingByNameOrId(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Thing name cannot be null")
		}
		try {
			int id = Integer.parseInt(name)
			return getThingByNameOrId(id)
		} catch (NumberFormatException e) {
			// Not an integer, so try by name.
		}
		return Thing.findByName(name)
	}

	def getPlaceByNameOrId(MapInfo info, int id) {
		return Place.findById(id)
	}

	def getPlaceByNameOrId(MapInfo info, String name) {
		if (name == null) {
			throw new IllegalArgumentException("Place name cannot be null")
		}
		try {
			int id = Integer.parseInt(name)
			return getPlaceByNameOrId(info, id)
		} catch (NumberFormatException e) {
			// Not an integer, so try by name.
		}
		return Place.findByName(name)
	}
	
	/**
	 * Convert a Mapcraft Thing to a Hexweb Thing.
	 * @param map
	 * @param thing
	 * @return
	 */
	def getThingFromName(MapInfo map, String thing) {
		if (thing == "null") {
			return null
		}
		def confMap = grailsApplication.getFlatConfig()
		def img = confMap["mapcraft.thing.${thing}"] as String
		Thing t = getThingByNameOrId(img)
		if (t == null) {
			println "No thing [${img}] found."
		}

		return t
	}

}
