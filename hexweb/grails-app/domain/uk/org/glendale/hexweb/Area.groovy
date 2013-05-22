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
 * A named area of a map. Each tile may have an area associated with it.
 * 
 */
class Area {
	MapInfo		mapInfo
	String		name
	
	static hasMany = [children: Area]
	static belongsTo = [parent: Area]
	
	static constraints = {
		parent(nullable:true)
	}

    static mapping = {
		table "area"
		version false
		mapInfo column: "mapinfo_id"
    }
}
