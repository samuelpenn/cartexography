/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

import uk.org.glendale.graphics.SimpleImage
import uk.org.glendale.hexweb.MapInfo

/**
 * Provides methods for generating a texture map from a world map.
 * 
 * @author Samuel Penn
 */
class TextureService {
	static final int	TILE_WIDTH = 17
	static final int	TILE_HEIGHT = 14
	static final int	COLUMN_WIDTH = 12
	static final int	ROW_HEIGHT = 12
	static final int	ROW_OFFSET = 6
	
	/**
	 * Return a stretched rectangular image of the given pixel width.
	 * 
	 * Need to:
	 *   Get each row of tiles.
	 *   Need to fill in missing tiles, accounting for the OOB areas.
	 */
	def getTexture(MapInfo info, int width) {
		SimpleImage image = new SimpleImage(width, width / 2, "#000000")
		
		
		
		return image
	}
}
