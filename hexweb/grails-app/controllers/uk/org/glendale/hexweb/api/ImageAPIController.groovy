/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.api

import java.awt.Image
import uk.org.glendale.graphics.SimpleImage
import uk.org.glendale.hexweb.MapInfo
import uk.org.glendale.hexweb.Terrain
import uk.org.glendale.hexweb.Hex
import uk.org.glendale.hexweb.Path
import uk.org.glendale.hexweb.Vertex

/**
 * Controller which produces images.
 */
class ImageAPIController {
	def mapService
	def grailsApplication
	def pathService
	
	private Image getImage(Terrain terrain, String path, int width, int height) {
		URL		url = new URL("file://" + path + "/terrain/${terrain.name}.png")
		
		println "Adding image for " + url.toString()
		
		Image image = SimpleImage.createImage(width, height, url)
		if (image == null) {
			println "Null image created"
		}
		
		return image
	}

	/**
	 * Get an image of the map according to the given set of coordinates.
	 * 
	 * @param id		Map to get image of.
	 * @param x			Origin x coordinate (top left).
	 * @param y			Origin y coordinate (top left).
	 * @param w			Width in tiles.
	 * @param h			Height in tiles.
	 * @param s			Size (width) of each tile.
	 * @return
	 */
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
		
		SimpleImage image = getMapImage(info, x, y, w, h, s)
		
		byte[] data = image.save().toByteArray()
		
		response.setContentType("image/jpeg")
		response.setContentLength(data.length)
		OutputStream	out = response.getOutputStream();
		out.write(data)
		out.close()
		return null
    }
	
	private SimpleImage getMapImage(MapInfo info, int x, int y, int w, int h, int s) {
		int			height = h * s + s / 2
		int			width = w * s
		
		SimpleImage image = new SimpleImage(width, height, "#ffffff")

		String BASE_PATH = grailsApplication.parentContext.getResource("WEB-INF/../images/style/"+info.style).file.absolutePath
				
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

		Map terrain = [:]
		Map	images = [:]
		
		int		tileWidth = s
		int		tileHeight = s * 0.86
		int		columnWidth = s * 0.73
		
		Terrain background = Terrain.findById(info.background)
		terrain.put(info.background, background)
		Terrain oob = Terrain.findById(info.oob)
		terrain.put(info.oob, oob)
		
		images.put(info.background, getImage(background, BASE_PATH, tileWidth, tileHeight))
		images.put(info.oob, getImage(oob, BASE_PATH, tileWidth, tileHeight))

		list.each { hex ->
			//println "${hex[0]},${hex[1]}"
			map[hex[1] - y][hex[0] - x] = hex[2]
			area[hex[1] - y][hex[0] - x] = hex[3]
			if (images.get(hex[2]) == null) {
				Terrain 	t = Terrain.findById(hex[2])
				images.put(hex[2], getImage(t, BASE_PATH, tileWidth, tileHeight))
			}
		}
		
		// Draw terrain layer.
		for (int px = 0; px < w; px ++) {
			for (int py = 0; py < h; py ++) {
				int		tid = map[py][px]
				if (tid == 0) {
					tid = background.id
				}
				Image img = images[tid]
				if (img != null) {
					int		xx = px * columnWidth
					int		yy = py * tileHeight
					if (px %2 == 1) {
						yy += tileHeight / 2
					}
					image.paint(img, xx, yy, tileWidth, tileHeight)
				} else {
					println "No image for ${px}, ${py}"
				}
			}
		}
		
		// Draw rivers
		List paths = pathService.getPathsInArea(info, x, y, w, h)
		paths.each { path ->
			Vertex[] vertices = path.vertex.toArray()
			println path.name + " " +  vertices.length
			
			for (int i=0; i < vertices.length - 1; i++) {
				double		x0 = vertices[i].x - x
				double		y0 = vertices[i].y - y
				double		x1 = vertices[i+1].x - x
				double		y1 = vertices[i+1].y - y
				
				x0 += vertices[i].subX / 100.0
				y0 += vertices[i].subY / 100.0
				x1 += vertices[i + 1].subX / 100.0
				y1 += vertices[i + 1].subY / 100.0
				
				float thickness = path.thickness1 - i * (path.thickness1 - path.thickness2) / vertices.length
				image.line(x0 * columnWidth, y0 * tileHeight, x1 * columnWidth, y1 * tileHeight, 
					       "#a4f8ff", thickness)
			}
		}
		
		
		return image
	}
}
