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
 * Configuration for the map. Each map has a unique name, which can be used
 * to identify it. Alternatively, it can be identified by its numeric id.
 * 
 * Templates are maps which define terrain and location icons. Any map can be
 * used as a template, but if the template is set to zero then the map is a
 * base template. Most maps created by the user will have to define a template
 * to be used. Map 1 is normally the base template.
 */
class MapInfo {
	String		name		// Unique name for this map. Expected to be [a-z][a-z0-9_]*
	String		title   	// Descriptive name for this map.
	int			width   	// Width, in hexes.
	int			height  	// Height, in hexes.
	int			scale   	// Width of each hex, in metres.
	boolean		world   	// Is this a world map? If so, will be discontinuous.
	int			template  	// Use the specified map as a template. If zero, ignored.
	int			background  // Default backgroup terrain id.
	int			oob			// Out of Bounds terrain id.

    static constraints = {
    }
	
	static mapping = {
		table "mapinfo"
	}
}
