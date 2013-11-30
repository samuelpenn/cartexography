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
 * A Thing is something that could be on a map, but which aren't hexes themselves.
 * Once a thing is put on a map, it becomes a Place, linked to a tile but positioned
 * more accurately.
 */
class Thing {
	MapInfo		mapInfo
	String		name
	String		title
	int			importance
	
    static mapping = {
		mapInfo column: "mapinfo_id"
    }
}
