/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

import java.awt.Image
import uk.org.glendale.graphics.SimpleImage
import uk.org.glendale.hexweb.MapInfo
import uk.org.glendale.hexweb.Terrain

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
		SimpleImage image = new SimpleImage(width - (int)(TILE_WIDTH*2/3), height - ROW_OFFSET * 2, "#000000")
		int			columns = width / COLUMN_WIDTH
		int			rows = height / ROW_HEIGHT
		
		Map terrain = [:]
		File f = new File(".")

		// Load all the images that we might need.
		Terrain.findAll().each { t ->
			try {
				//URL 	u = new URL("file:/home/sam/src/hexweb/hexweb/web-app/images/style/standard/terrain/"+t.name+".png")
				URL 	u = new URL("file:web-app/images/style/standard/terrain/"+t.name+".png")
				Image 	i = SimpleImage.createImage(TILE_WIDTH, TILE_HEIGHT, u)
				if (i != null) {
					int	id = t.id
					terrain.put(id, i)
				} else {
					println "[${t.name}] is null"
				}
			} catch (MalformedURLException e) {
				e.printStackTrace()
			}
		}

		// Draw the map.
		for (int yy=0; yy < rows; yy++) {
			int  y = Math.floor(yy * (info.height / rows))
			int[] data = mapService.getMapRow(info, y)
			
			// Work out which tiles are 'real', and stretch them out to fill oob space.
			int actualColumns = 0
			for (int x=0; x < data.length; x++) {
				if (data[x] != info.oob) {
					data[actualColumns++] = data[x]
				}
			}
			
			for (int xx=0; xx < columns; xx++) {
				int x = Math.floor(xx * (actualColumns / columns))
				int t = data[x]

				int px = xx * COLUMN_WIDTH - TILE_WIDTH / 3
				int py = yy * ROW_HEIGHT - ROW_OFFSET
				if (xx%2 == 1) {
					py += ROW_OFFSET
				}
				image.paint(terrain.get(t), px, py, TILE_WIDTH, TILE_HEIGHT)
			}
		}
		
		
		return image
	}
}
