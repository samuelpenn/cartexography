/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

import uk.org.glendale.hexweb.Thing

class ThingService {
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

}
