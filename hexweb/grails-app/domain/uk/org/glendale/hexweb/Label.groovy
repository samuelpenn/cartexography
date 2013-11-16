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
 * A label is a piece of text that appears on the map. It is placed like places,
 * but consists of text, font size and colour and rotation. Whether a label
 * appears is based on its size, and may be hidden if the map is zoomed in or
 * zoomed out too far.
 */
class Label {
	MapInfo		mapInfo
	
	int			tileX
	int			tileY
	int			subX
	int			subY
	String		name
	String		title
	int			size
	int			rotation
	LabelStyle	style	

    static mapping = {
		mapInfo column: "mapinfo_id"
		tileX column: "tile_x"
		tileY column: "tile_y"
		subX column: "sub_x"
		subY column: "sub_y"
    }
}
