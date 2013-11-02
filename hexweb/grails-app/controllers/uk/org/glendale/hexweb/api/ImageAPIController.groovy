/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.api

import uk.org.glendale.graphics.SimpleImage
import uk.org.glendale.hexweb.MapInfo
import uk.org.glendale.hexweb.Terrain
import uk.org.glendale.hexweb.Hex

/**
 * Controller which produces images.
 */
class ImageAPIController {
	def mapService

    def imageByCoord(String id, int x, int y, int w, int h, int s) { 
		MapInfo		info = mapService.getMapByNameOrId(id)
		
		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}
		if (x%2 == 1) {
			x --
		}
		if (x + w > info.width) {
			w = info.width - x;
		}
		if (y + h > info.height) {
			h = info.height - y;
		}
		
		int			height = h * s + s / 2
		int			width = w * s
		
		SimpleImage image = new SimpleImage(width, height, "#ffffff")
		
		int[][]		map = new int[h][w]
		int[][]		area = new int[h][w]
		
		List list = Hex.findAll ({
			eq('mapInfo', info)
			between('x', x, x + w -1)
			between('y', y, y + h - 1)
			
			projections {
				property("x")
				property("y")
				property("terrainId")
				property("areaId")
			}
			order("y")
			order("x")
		})

		Map colours = [:]
		
		Terrain background = Terrain.findById(info.background)
		colours.put(info.background, background.colour)
		Terrain oob = Terrain.findById(info.oob)
		colours.put(info.oob, oob.colour)

		list.each { hex ->
			map[hex[1] - y][hex[0] - x] = hex[2]
			area[hex[1] - y][hex[0] - x] = hex[3]
		}
		
		for (int px = 0; px < w; px ++) {
			for (int py = 0; py < h; py ++) {
				int	tid = map[py][px]
				String colour = colours.get(tid)
				if (colour == null) {
					Terrain t = Terrain.findById(tid)
					if (t != null) {
						colours.put(tid, t.colour)
						colour = t.colour
					} else {
						println "Cannot find terrain ${tid} at ${x},${y}"
						colour = colours.get(info.background)
					}
				}
				image.rectangleFill(px * s, py * s, s, s, colour)
			}
		}

		
		byte[] data = image.save().toByteArray()
		
		response.setContentType("image/jpeg")
		response.setContentLength(data.length)
		OutputStream	out = response.getOutputStream();
		out.write(data)
		out.close()
		return null

	}
}
