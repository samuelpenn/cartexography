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
 * Defines a vector path. Path consists of lines joining two or more vertices.
 * A path can be a river, road or something else.
 */
class Path {
	MapInfo		mapInfo
	String		name
	PathType	type
	int			thickness
	
	static hasMany = [vertex:Vertex]
	
    static mapping = {
		table "path"
		mapInfo column: "mapinfo_id"
    }
}
