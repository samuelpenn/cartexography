/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb

/**
 * Configuration for the map.
 */
class MapInfo {
	String		name
	String		title
	int			width
	int			height
	boolean		world

    static constraints = {
    }
	
	static mapping = {
		table "mapinfo"
	}
}
