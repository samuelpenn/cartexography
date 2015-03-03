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
import uk.org.glendale.hexweb.*

/**
 * Controller which produces images.
 */
class ImageAPIController {
	def mapService
	def grailsApplication
	def pathService
	def thingService
	def imageService
	def areaService
	
	private Image getImage(Terrain terrain, int variant, String path, int width, int height) {
		URL		url = new URL("file://" + path + "/terrain/${terrain.name}_${variant}.png")
		
		Image image = SimpleImage.createImage(width, height, url)
		if (image == null) {
			println "Null image created"
		}
		
		return image
	}

	private Image getImage(Thing thing, String path, int width, int height) {
		URL		url = new URL("file://" + path + "/things/${thing.name}.png")
		
		Image image = SimpleImage.createImage(width, height, url)
		if (image == null) {
			println "Null image created"
		}
		
		return image
	}
	
	/**
	 * Draws a single hexagon of the specified height.
	 * 
	 * @param 	height
	 * @return	PNG image containing a hexagon.
	 */
	def Image drawHex(int height) {
		int		width = (int) (2.0 * height / Math.sqrt(3))
		
		SimpleImage image = new SimpleImage(width, height, "#FFFFFF")
		
		image.hexByHeight(0, 0, height, "#000000")
				
		byte[] data = image.toPng().toByteArray()
		
		response.setContentType("image/png")
		response.setContentLength(data.length)
		OutputStream	out = response.getOutputStream();
		out.write(data)
		out.close()
		return null
	}
	
	/**
	 * Test API to display a list of all images for a particular graphic
	 * style. Displays a web page with a count of available images for each
	 * terrain type.
	 * 
	 * @param id		Map to check.
	 * @param style		Style to use, leave blank for default.
	 * @return
	 */
	def String dumpTerrain(String id, String style) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		
		if (style == null) {
			style = info.style
		}
		render "<b>" + style + "</b><br/>"
		String BASE_PATH = grailsApplication.parentContext.getResource("WEB-INF/../images/style/"+style).file.absolutePath
		
		List<Terrain>  list = Terrain.findAllByMapInfo(info)
		while (info.template > 0) {
			MapInfo	template = mapService.getMapByNameOrId(info.template)
			list.addAll(Terrain.findAllByMapInfo(template))
			info = mapService.getMapByNameOrId(info.template)
		}

		render "<ul>"
		list.each() { t ->
			render "<li>"
			
			File file = new File(BASE_PATH + "/terrain/" + t.name + ".png")
			if (file.exists()) {
				render "<b>${t.name}</b>"
			} else {
				file = new File(BASE_PATH + "/terrain/" + t.name + "_0.png")
				if (file.exists()) {
					int count = 1
					while (new File("${BASE_PATH}/terrain/${t.name}_${count}.png").exists()) {
						count++
					}
					render "<b>${t.name} ${count}</b>"
				} else {
					render "${t.name}"
				}
			}
			render "</li>"
		}
		render "</ul>"
		
	}
	
	def imageByArea(String id, String areaId, int border, int s, String style) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		Area		area = areaService.getAreaByName(info, areaId)
		
		if (area == null) {
			throw new IllegalArgumentException("Unknown area [${areaId}]")
		}
		
		def bounds = areaService.getBounds(info, area)
		int x = bounds.min_x
		int y = bounds.min_y
		int w = (bounds.max_x - x)
		int h = (bounds.max_y - y)
		
		if (border > 0) {
			x -= border
			w += border * 2 + 1
			y -= border
			h += border * 2 + 1
		}
		if (x%2 == 1) {
			x -= 1
			w += 1
		}

		print bounds
		print "${x} ${y} ${w} ${h}"
		
		return imageByCoord(id, x, y, w, h, s, style)
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
    def imageByCoord(String id, int x, int y, int w, int h, int s, String style) { 
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
		
		SimpleImage image = getMapImage(info, x, y, w, h, s, style)
		
		byte[] data = image.toPng().toByteArray()
		
		response.setContentType("image/png")
		response.setContentLength(data.length)
		OutputStream	out = response.getOutputStream();
		out.write(data)
		out.close()
		return null
    }
	
	private addVariantImage(Map images, Terrain terrain, int variant, String path, int w, int h) {
		if (images.get((int)terrain.id) == null) {
			images.put((int)terrain.id, [:]) 
		}
		Map varmap = images.get((int)terrain.id)
		if (varmap.get(variant) == null) {
			varmap.put(variant, getImage(terrain, variant, path, w, h))
		} else {
			varmap.put(variant, getImage(terrain, variant, path, w, h))
		}
	}
	
	private Image getVariantImage(Map images, int tid, int var) {
		Map varmap = images.get(tid)
		if (varmap != null) {
			return varmap.get(var)
		}
		return null
	}
	
	private SimpleImage getMapImage(MapInfo info, int x, int y, int w, int h, int s, String style) {
		// Use a default scale if none is given. Based on largest dimension.
		if (s < 1) {
			int size = w * h
			if (size > 10000) {
				s = 1000000 / size 
			} else {
				s = 100
			}
			print "Using scale: " + s
		}
		
		int			height = (h * s + s / 2) * 0.86
		int			width = (w * s) * 0.73 + s * 0.25
		
		if (style == null || style.length() == 0) {
			style = info.style;
		}
		
		SimpleImage image = new SimpleImage(width, height, "#ffffff")

		String BASE_PATH = grailsApplication.parentContext.getResource("WEB-INF/../images/style/"+style).file.absolutePath
				
		int[][]		map = new int[h][w]
		int[][]		area = new int[h][w]
		Area		selectedArea = null
		if (params.areaId != null) {
			selectedArea = areaService.getAreaByName(info, params.areaId)
		}
		
		List list = Hex.findAll ({
			eq('mapInfo', info)
			between('x', x, x + w -1)
			between('y', y, y + h - 1)
			
			projections {
				property("x")
				property("y")
				property("terrainId")
				property("areaId")
				property("variant")
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
		Terrain unknown = Terrain.findById(Terrain.UNKNOWN)
		
		addVariantImage(images, background, 0, BASE_PATH, tileWidth, tileHeight)
		addVariantImage(images, oob, 0, BASE_PATH, tileWidth, tileHeight)
		addVariantImage(images, unknown, 0, BASE_PATH, tileWidth, tileHeight)

		list.each { hex ->
			//println "${hex[0]},${hex[1]}"
			map[hex[1] - y][hex[0] - x] = hex[2] * 10 + hex[4]
			area[hex[1] - y][hex[0] - x] = hex[3]
			if (getVariantImage(images, hex[2], hex[4]) == null) {
				Terrain 	t = Terrain.findById(hex[2])
				if (t != null) {
					addVariantImage(images, t, (int)hex[4], BASE_PATH, tileWidth, tileHeight)
				}
			}
		}
		
		// Draw terrain layer.
		for (int px = 0; px < w; px ++) {
			for (int py = 0; py < h; py ++) {
				int		tid = (int)(map[py][px] / 10)
				int		var = map[py][px] % 10
				if (tid == 0) {
					// No hex data, do we have sparse data?
					tid = map[py - py%10][px - px%10];
					if (tid == 0) {
						// Default to background terrain.
						if (mapService.isOut(info, x + px, y + py)) {
							tid = oob.id
						} else {
							tid = background.id
						}
						var = 0
					}
				}
				Image img = getVariantImage(images, tid, var)
				if (img != null) {
					int		xx = px * columnWidth
					int		yy = py * tileHeight
					if (px %2 == 1) {
						yy += tileHeight / 2
					}
					if (selectedArea == null || selectedArea.id == area[py][px]) {
						image.paint(img, xx, yy, tileWidth, tileHeight)
					} else {
						image.paint(img, xx, yy, tileWidth, tileHeight)
						image.paint(getVariantImage(images, Terrain.UNKNOWN, 0), xx, yy, tileWidth, tileHeight)
					}
				} else {
					//println "No image for ${px}, ${py} ${tid} ${var}"
				}
			}
		}
		
		if (params.hex == "1") {
			String hexColour = "#44444444"
			float hexThickness = 3		
			// Draw a hex grid
			for (int px = 0; px < w; px ++) {
				for (int py = 0; py < h; py ++) {
					double xx = px * columnWidth;
					double yy = py * tileHeight + (px%2 * tileHeight/2);
					
					image.line(xx + columnWidth / 3, yy, xx + columnWidth, yy, hexColour, hexThickness)
					image.line(xx + columnWidth, yy, xx + columnWidth + columnWidth / 3, yy + tileHeight / 2, hexColour, hexThickness)
					image.line(xx + columnWidth + columnWidth / 3, yy + tileHeight / 2, xx + columnWidth, yy + tileHeight, hexColour, hexThickness)
					image.line(xx + columnWidth, yy + tileHeight, xx + columnWidth/3, yy + tileHeight, hexColour, hexThickness)
					image.line(xx + columnWidth/3, yy + tileHeight, xx, yy + tileHeight/2, hexColour, hexThickness)
					image.line(xx, yy + tileHeight/2, xx + columnWidth/3, yy, hexColour, hexThickness)		
				}	
			}
		}
		if (params.areas == "1") {
			// Now do the area borders
			String borderColour = "#ff0000"
			float borderThickness = 5
			for (int px = 0; px < w; px ++) {
				for (int py = 0; py < h; py ++) {
					double xx = px * columnWidth;
					double yy = py * tileHeight + (px%2 * tileHeight/2);
					
					if (py > 0 && area[py][px] != area[py-1][px]) {
						image.line(xx + columnWidth / 3, yy, xx + columnWidth, yy, borderColour, borderThickness)
					}
					if (px%2 == 1) {
						if (px > 0 && area[py][px] != area[py][px-1]) {
							image.line(xx, yy + tileHeight/2, xx + columnWidth/3, yy, borderColour, borderThickness);
						}
						if (px > 0 && py < h - 1 && area[py][px] != area[py+1][px-1]) {
							image.line(xx, yy + tileHeight/2, xx + columnWidth/3, yy + tileHeight, borderColour, borderThickness);
						}
					} else {
						if (px > 0 && py > 0 && area[py][px] != area[py-1][px-1]) {
							image.line(xx, yy + tileHeight/2, xx + columnWidth/3, yy, borderColour, borderThickness)
						}
						if (px > 0 && area[py][px] != area[py][px-1]) {
							image.line(xx, yy + tileHeight / 2, xx + columnWidth / 3, yy + tileHeight, borderColour, borderThickness)
						}					
					}
				}
			}
		}
		// Draw rivers
		drawRivers(info, image, columnWidth, tileHeight, s, x, y, w, h)
		
		// Draw places
		List places = Place.findAll ({
			eq('mapInfo', info)
			between('tileX', x, x + w -1)
			between('tileY', y, y + h - 1)
		})
		Map	things = [:]
		places.each { place ->
			if (things.get(place.thingId) == null) {
				Thing thing = Thing.findById(place.thingId)
				things.put(thing.id, getImage((Thing)thing, BASE_PATH, tileWidth, tileHeight))
			}
			if (selectedArea == null || selectedArea.id == area[place.tileY - y][place.tileX - x]) {
				Image	img = things.get(place.thingId)
				if (img != null) {
					int		xx = (place.tileX - x) * columnWidth - columnWidth / 2
					int		yy = (place.tileY - y) * tileHeight - tileHeight / 2
					if ((place.tileX - x) %2 == 1) {
						yy += tileHeight / 2
					}
					xx += (place.subX * tileWidth) / 100
					yy += (place.subY * tileHeight) / 100
					image.paint(img, xx, yy, tileWidth, tileHeight)
					int	fontSize = s / 5 + place.importance * 2
					int fontWidth = image.getTextWidth(place.title, 0, fontSize)
					xx += tileWidth / 2 - fontWidth / 2
					yy += tileHeight
					if (params.l != "0") {
						image.text(xx, yy, place.title, 0,  fontSize, "#000000")
					}
				}
			}
		}
		
		// Draw labels
		if (params.l != "0") {
			List labels = Label.findAll ({
				eq('mapInfo', info)
				between('tileX', x, x + w -1)
				between('tileY', y, y + h - 1)
			})
			labels.each { label ->
				if (selectedArea == null || selectedArea.id == area[label.tileY - y][label.tileX - x]) {
					int		xx = (label.tileX - x) * columnWidth
					int		yy = (label.tileY - y) * tileHeight
					if ((label.tileX - x) %2 == 1) {
						yy += tileHeight / 2
					}
					xx += (label.subX * tileWidth) / 100
					yy += (label.subY * tileHeight) / 100
		
					int fontSize = imageService.getLabelSize(label, columnWidth)
					int alpha = imageService.getLabelAlpha(label, columnWidth)
					
					if (alpha > 0) {
						alpha *= 2.55
						String colour = label.style.fill + Integer.toHexString(alpha)
						int fontWidth = image.getTextWidth(label.title, 0, fontSize)
						//image.circle(xx, yy, 8, "#000000")
						xx -= fontWidth / 2
						image.text(xx, yy, label.title, 0, fontSize, colour, label.rotation)
						
					}
				}
			}
		}
		
		return image
	}
	
	/**
	 * Draw rivers on the map as bezier curves. Extra control points are calculated
	 * dynamically to give a smooth curve across the length of the river.
	 * 
	 * Paths are cropped to the area specified.
	 * 
	 * @param info			Map to display.
	 * @param image			Image to write into.
	 * @param columnWidth	Width of a hex column.
	 * @param tileHeight	Height of a hex tile.
	 * @param s				Scale of the map.
	 * @param x				X coordinate to start from.
	 * @param y				Y coordinate to start from.
	 * @param w				Width of map to display.
	 * @param h				Height of map to display.
	 */
	private void drawRivers(MapInfo info, SimpleImage image, int columnWidth, int tileHeight, int s, int x, int y, int w, int h) {
		List paths = pathService.getPathsInArea(info, x, y, w, h)
		
		def roads = []
		paths.each { path ->
			String colour = "#b7f9ff"
			String style = "${path.style}"
			boolean isRoad = false
			if (style.equals("ROAD")) {
				isRoad = true
				colour = "#000000"
			}

			Vertex[] vertices = path.vertex.toArray()
			double[]	vx = new double[vertices.length+2]
			double[]	vy = new double[vertices.length+2]

			// Work out actual coordinates of each vertex on the map.
			for (int i=0; i < vertices.length; i++) {
				vx[i+1] = vertices[i].x - x
				vy[i+1] = vertices[i].y - y
				
				vx[i+1] += vertices[i].subX / 100.0
				vy[i+1] += vertices[i].subY / 100.0
				
				if (vertices[i].x %2 == 1) {
					vy[i+1] += 0.5
				}				
				vx[i+1] *= columnWidth
				vy[i+1] *= tileHeight
			}
			vx[0] = vx[1]
			vy[0] = vy[1]
			vx[vx.length-1] = vx[vx.length-2]
			vy[vy.length-1] = vy[vy.length-2]
			
			// Now calculate bezier control points and draw.
			drawBezierPath(image, vx, vy, colour, 
				path.thickness1, path.thickness2, (double)(s / 20.0),
				vertices.length, isRoad)
			if (path.thickness1 == 4 && isRoad) {
				// If this is a full road, need to append to list to draw again
				// later in an overlay. All overlays must be drawn after all base
				// roads, so that crossroads look right.
				roads.add([ vx: vx, vy: vy, width: path.thickness1, len: vertices.length ] )
			}
		}
		
		// Draw full roads again, this time in white but slightly thinner. This
		// gives roads as a white line with a black border.
		roads.each { road ->
			drawBezierPath(image, road.vx, road.vy, "#ffffff",
				road.width, road.width, (double)(s / 30.0), road.len, true)
		}
	}
	
	/**
	 * Actually draw a path on the image. Called after all the coordinates
	 * have been calculated. For roads, this may get called twice, so we
	 * can have a white road bordered with black.
	 * 
	 * @param image		SimpleImage to draw onto.
	 * @param vx		Array of X coordinates.
	 * @param vy		Array of Y coordinates.
	 * @param colour	Colour to use.
	 * @param t1		Starting thickness.
	 * @param t2		Ending thickness.
	 * @param scale		Scale factor for thickness.
	 * @param length	Actual length of path.
	 * @param isRoad	iff true if a road, otherwise it's a river or coastline.
	 */
	private void drawBezierPath(SimpleImage image, double[] vx, double[] vy, 
		String colour, int t1, int t2, double scale, int length, boolean isRoad) {
		for (int i=1; i < vx.length - 2; i++) {
			double[]	xp = new double[4];
			double[]	yp = new double[4];
			xp[0] = vx[i]
			yp[0] = vy[i]
			xp[3] = vx[i+1]
			yp[3] = vy[i+1]
			
			// Work out control points dynamically.
			int ax, bx, cx, dx, xx
			int ay, by, cy, dy, yy
			// A is halfway point on previous line.
			ax = (vx[i-1] + vx[i]) / 2.0
			ay = (vy[i-1] + vy[i]) / 2.0
			// B is halfway point on this line.
			bx = (vx[i] + vx[i+1]) / 2.0
			by = (vy[i] + vy[i+1]) / 2.0
			// Halfway point between A and B
			xx = (ax + bx) / 2.0
			yy = (ay + by) / 2.0
			// Shift B control point up so A/B line intersects start of line
			ax -= xx - vx[i]
			ay -= yy - vy[i]
			bx -= xx - vx[i]
			by -= yy - vy[i]
			
			// C is equal to B
			cx = bx
			cy = by
			// D is halfway point on next line.
			dx = (vx[i+1] + vx[i+2]) / 2.0
			dy = (vy[i+1] + vy[i+2]) / 2.0
			// Halfway point between A and B
			xx = (cx + dx) / 2.0
			yy = (cy + dy) / 2.0
			// Shift B control point up so A/B line intersects start of line
			cx -= xx - vx[i+1]
			cy -= yy - vy[i+1]

			xp[1] = bx
			yp[1] = by
			xp[2] = cx
			yp[2] = cy

			double thickness = t1 - i * (t1 - t2) / length
			if (isRoad) {
				// This is currently ugly. Want to be able to change the dash
				// pattern based on the thickness. Also, roads should be thinner
				// than rivers. Roads of thickness '4' are drawn twice, first
				// time in black, second time in white but thinner.
				double width = thickness * scale / 3.0
				switch ((int)(thickness + 0.5)) {
				case 0:
					// Roads of thickness 0 shouldn't actually exist.
					colour = "#997733"
					image.curve(xp, yp, colour, width, (double)2.0, (double)5.0)
					break;
				case 1:
					colour = "#774400"
					image.curve(xp, yp, colour, width, (double)5.0, (double)5.0)
					break;
				case 2:
					colour = "#774400"
					image.curve(xp, yp, colour, width, (double)20.0, (double)10.0)
					break;
				case 3:
					colour = "#555555"
					image.curve(xp, yp, colour, width)
					break;
				default:
					image.curve(xp, yp, colour, width)
					break;
				}
			} else {
				image.curve(xp, yp, colour, thickness * scale)
			}
		}
	}
}
