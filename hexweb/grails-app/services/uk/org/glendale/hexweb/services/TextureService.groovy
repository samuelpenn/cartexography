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
	
	def mapService
	def terrainService
	
	/**
	 * Return a stretched rectangular image of the given pixel width.
	 * 
	 * Need to:
	 *   Get each row of tiles.
	 *   Need to fill in missing tiles, accounting for the OOB areas.
	 */
	def getTexture(MapInfo info, int width) {
		int			height = width / 2
		SimpleImage image = new SimpleImage(width, height, "#000000")
		int			columns = width / COLUMN_WIDTH
		int			rows = height / ROW_HEIGHT
		
		Map			terrain = [:]

		for (int yy=0; yy < rows; yy++) {
			int  y = Math.floor(yy * (info.height / rows))
			println "${yy}: ${y}"
			int[] data = mapService.getMapRow(info, y)
			
			for (int xx=0; xx < columns; xx++) {
				int x = Math.floor(xx * (info.width / columns))
				int t = data[x]

				String colour = terrain.get(t)
				if (colour == null) {
					colour = terrainService.getTerrainByNameOrId(info, t).colour
					terrain.put(t,  colour)
				}
				int px = xx * COLUMN_WIDTH
				int py = yy * ROW_HEIGHT
				image.rectangleFill(px, py, COLUMN_WIDTH, ROW_HEIGHT, colour)
			}
		}
		
		
		return image
	}
}
