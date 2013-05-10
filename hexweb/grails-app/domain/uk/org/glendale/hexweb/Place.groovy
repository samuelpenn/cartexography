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
 * A Place is a Thing which has been fixed onto a map. Positioning is at a sub-hex
 * level of accuracy, and multiple places can be tied to a single hex. A Place has
 * a name (for identification) and title independently of the thing.
 */
class Place {
	MapInfo		mapInfo
	Thing		thing
	
	int			importance
	int			tileX
	int			tileY
	int			subX
	int			subY
	String		name
	String		title

    static mapping = {
		mapInfo column: "mapinfo_id"
		tileX column: "tile_x"
		tileY column: "tile_y"
		subX column: "sub_x"
		subY column: "sub_y"
    }
}
