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
 * Defines the type of a hex tile. Specifies the image that is used, the flat
 * colour for large scale maps, the name and title. The order is used to sort
 * terrain, so similar types of terrain appear together in the palates.
 */
class Terrain {
	static final UNKNOWN = 1

	MapInfo		mapInfo
	String		name
	String		title
	boolean		water
	String		colour
	/* Order used to group similar types. An order of zero means that
	 * this terrain type is for display purposes only - it cannot be
	 * drawn onto the map.
	 */
	int			ordering

    static constraints = {
    }
	
	static mapping = {
		table "terrain"
		mapInfo column: "mapinfo_id"
		sort "ordering"
	}
}
