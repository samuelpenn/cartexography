/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

import uk.org.glendale.hexweb.MapInfo

/**
 * Provides services for the import and conversion of Mapcraft v1 maps.
 * 
 * Mapcraft stored its data in XML files, which used a special compressed
 * format for the tile data. This needs to be parsed.
 * 
 * Mapcraft terrains and things also need to be translated into Hexweb
 * equivalents. This is complicated by the fact that Mapcraft had two layers
 * for tiles - terrain and features. Features were hills and mountains, so
 * a 'Woods' tile with 'Hills' needs to be translated to a single terrain
 * type in Hexweb.
 */
class ImportService {

	/**
	 * Import a map into the database. 
	 * 
	 * @param mapInfo		Map to import into.
	 * @param mapcraft		Mapcraft format XML document.
	 * @return
	 */
    def importMap(MapInfo mapInfo, Node mapcraft) {

    }
}
