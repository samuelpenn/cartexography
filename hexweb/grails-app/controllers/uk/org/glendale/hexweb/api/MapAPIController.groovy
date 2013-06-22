/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.api

import grails.converters.JSON
import uk.org.glendale.graphics.SimpleImage
import uk.org.glendale.hexweb.Hex
import uk.org.glendale.hexweb.MapInfo
import uk.org.glendale.hexweb.Place
import uk.org.glendale.hexweb.Terrain
import uk.org.glendale.hexweb.Thing
import uk.org.glendale.hexweb.Area


import groovy.sql.Sql
import java.awt.Image
import javax.servlet.ServletOutputStream

class MapAPIController {
	/** Common services for maps */
	def mapService
	def terrainService
	def thingService
	def scaleService
	
	/**
	 * Returns information about this map. Includes the map metadata, list of
	 * terrain and things for the palettes.
	 * 
	 * GET: /api/map/{id}/info
	 */
	def info(String id) {
		MapInfo	info = mapService.getMapByNameOrId(id)
		
		def data = [ info: info, terrain: getTerrain(info), things: getThings(info) ]
		
		render data as JSON
	}
	
	private List getTerrain(MapInfo info) {
		List<Terrain>  list = Terrain.findAllByMapInfo(info)
		if (info.template > 0) {
			MapInfo	template = mapService.getMapByNameOrId(info.template)
			list.addAll(Terrain.findAllByMapInfo(template))
		}
		return list
	}
	
	private List getThings(MapInfo info) {
		List<Thing>  list = Thing.findAllByMapInfo(info)
		if (info.template > 0) {
			MapInfo	template = mapService.getMapByNameOrId(info.template)
			list.addAll(Thing.findAllByMapInfo(template))
		}
		return list
	}
	
	/**
	 * Updates information about this map.
	 * 
	 * PUT: /api/map/{id}/info
	 */
	def updateInfo(String id, String title, int width, int height, int scale) {
		MapInfo	info = mapService.getMapByNameOrId(id)
		if (title != null) {
			info.title = title
		}
		if (width > 0) {
			info.width = width
		}
		if (height > 0) {
			info.height = height
		}
		if (scale > 0) {
			info.scale = scale
		}
		
		render info as JSON
	}

	
    def terrain(String id) { 
		MapInfo		info = mapService.getMapByNameOrId(id)
		
		List<Terrain>  list = Terrain.findAllByMapInfo(info)
		if (info.template > 0) {
			MapInfo	template = mapService.getMapByNameOrId(info.template)
			list.addAll(Terrain.findAllByMapInfo(template))
		}
		
		render list as JSON
	}
	
	def fillMap(String id, String terrainId) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		Terrain		fill = terrainService.getTerrainByNameOrId(terrainId)
		
		if (info.world) {
			fillWorldMap(info, fill)
		} else {
			for (int y=0; y < info.height; y++) {
				for (int x=0; x < info.width; x++) {
					if (mapService.getHex(info, x, y) == null) {
						Hex hex = new Hex(mapInfo: info, x: x, y: y, terrainId: fill.id)
						hex.save()
					}
				}
			}
		}
		
		render "Done"
	}
	
	private void fillWorldMap(MapInfo info, Terrain fill) {
		int		oob = terrainService.getTerrainByNameOrId("oob").id
		int		ib = fill.id
		
		// Wipe the map clean.
		mapService.clearMap(info)
		for (int y=0; y < info.height; y++) {
			for (int x=0; x < info.width; x++) {
				if (mapService.isOut(info, x, y)) {
					mapService.insertToMap(info, x, y, 0, oob)
				} else {
					mapService.insertToMap(info, x, y, 0, ib)
				}
			}
		}
	}
	

	def sessionFactory
	
	/**
	 * Returns a map for the specified area at full resolution. Map details is a plain
	 * array consisting only of the terrain id. Array is an array of rows, containing
	 * columns.
	 * 
	 * Also returns any places that are located within the map area.
	 * 
	 * @param map	Map to get.
	 * @param x		X coordinate of top left.
	 * @param y		Y coordinate of top left.
	 * @param w		Width.
	 * @param h		Height.
	 * @return
	 */
	def map(String id, int x, int y, int w, int h) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		
		println("map: ${id} ${x},${y}+${w}+${h}")
		
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
		
		list.each { hex ->
			map[hex[1] - y][hex[0] - x] = hex[2]
			area[hex[1] - y][hex[0] - x] = hex[3]
		}
		long start = System.currentTimeMillis()
		List bounds = null
		if (info.world || list.size() != w * h) {
			println "Filtering map ${list.size()}"
			// We have gaps in the data, so blank the whole map first. There's
			// no point doing this if there are no gaps. If it is a world map,
			// then we always need to do this.
			Terrain		unknown = terrainService.getTerrainByNameOrId("unknown")
			bounds = mapService.getBounds(info, x, w, 1)
			for (int xx=0; xx < w; xx++) {
				int xx10 = xx - xx%10;
				for (int yy=0; yy < h; yy++) {
					if (bounds.size() > 0 && (yy+y < bounds[xx].min || yy+y > bounds[xx].max)) {
						map[yy][xx] = info.oob
					} else {
						if (map[yy][xx] == 0) {
							int b = map[yy - yy%10][xx10]
							if (b != info.oob && b != 0) {
								map[yy][xx] = b
							} else {
								map[yy][xx] = info.background
							}
						}
					}
				}
			}
		}
		println "Filter time = ${System.currentTimeMillis() - start}"

		println "Size: ${list.size()} x ${x} y ${y} w ${w} h ${h}" 
		
		List places = Place.findAll ({
			eq('mapInfo', info)
			between('tileX', x, x + w -1)
			between('tileY', y, y + h - 1)			
		})
		
		Map data = new HashMap();
		data.put("map", map)
		data.put("area", area)
		data.put("places", places)
		data.put("info", [ "x": x, "y": y, "width": w, "height": h ]);
		if (bounds != null) {
			data.put("bounds", bounds)
		}
		
		render data as JSON
	}
	
	/**
	 * Get low resolution data for a large scale map. Only every nth tile is
	 * actually returned, so the map covers a much larger area for course
	 * editing. The x/y coordinate must be aligned to the precision.
	 * 
	 * Only terrain data is returned. Places and areas are ignored at this
	 * level of detail.
	 */
	def largeMap(String id, int x, int y, int w, int h, int scale) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		
		println("largeMap: ${id} ${x},${y}+${w}+${h} ${scale}")
		
		if (scale < 1) {
			scale = 1
		}
		
		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}
		x -= x % scale;
		y -= y % scale;

		if (x + w*scale > info.width) {
			w = (info.width - x);
		}
		w -= w % scale
		if (y + h*scale > info.height) {
			h = (info.height - y);
		}
		y -= y % scale

		int[][]		map = new int[h/scale][w/scale]
		List list = mapService.getMapData(info, x, y, w, h, scale)
		list.each { hex ->
			int xx = (hex[0] - x) / scale
			int yy = (hex[1] - y) / scale
			map[yy][xx] = hex[2]
		}
		List bounds = null
		if (info.world || list.size() != w * h) {
			println "Filtering large map ${list.size()} of ${w * h}"
			// We have gaps in the data, so blank the whole map first. There's
			// no point doing this if there are no gaps. If it is a world map,
			// then we always need to do this.
			Terrain		unknown = terrainService.getTerrainByNameOrId("unknown")
			bounds = mapService.getBounds(info, x, w, scale)
			for (int xx=0; xx < w/scale; xx++) {
				int xx10 = xx - xx%10;
				for (int yy=0; yy < h/scale; yy++) {
					if (bounds.size() > 0 && (yy+y < bounds[xx].min / scale || yy+y > bounds[xx].max / scale)) {
						map[yy][xx] = info.oob
					} else {
						if (map[yy][xx] == 0) {
							int b = map[yy - yy%10][xx10]
							// If blank, fall back to '10th parent', else use background.
							if (b != info.oob && b != 0) {
								map[yy][xx] = b
							} else {
								map[yy][xx] = info.background
							}
						}
					}
				}
			}
		}
		Map data = new HashMap();
		data.put("map", map)
		data.put("info", [ "x": x, "y": y, "width": w, "height": h ]);
		if (bounds != null && bounds.size() > 0) {
			data.put("bounds", bounds)
		}
		
		render data as JSON

	}
	
	static int lastId = -1
	static int lastX = -1
	static int lastY = -1
	static int lastRadius = -1
	static int lastTerrain = -1
	
	private synchronized boolean isdup(id, x, y, radius, terrain) {
		if (x == lastX && y == lastY && radius == lastRadius && terrain == lastTerrain && id == lastId) {
			return true
		}
		lastX = x
		lastY = y
		lastTerrain = terrain
		lastRadius = radius
		lastId = id

		return false
	}
	
	/**
	 * Updates a hex with a new terrain.
	 */
	def update(String id, int x, int y, int radius, int terrain, int scale) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		
		if (isdup(info.id, x, y, radius, terrain)) {
			render terrain
			return
		}		
		println "Update: ${id}-${x},${y} radius ${radius} scale ${scale}"
		if (radius < 1) {
			radius == 1
		} else if (radius %2 == 0) {
			radius--
		}
		if (scale <= 1) {
			int ox = x;
			int oy = y;
			// XXX: Why is radius being used as a diameter?
			for (int px = 0; px < (int)Math.floor(radius / 2) + 1; px++) {
				int	 h = radius - px;
				
				for (int py = 0; py < h; py ++) {
					y = oy + py - (int)Math.floor(h / 2);
					if (px%2 == 1) {
						y += ox%2;
					}
					if (y < 0 || y >= info.height) {
						continue
					}
					
					x = ox + px;
					if (x >= 0 && x < info.width) {
						setHex(info, x, y, terrain);
					}
					x = ox - px;
					if (x >= 0 && x < info.width) {
						setHex(info, x, y, terrain);
					}
				}
			}
		} else {
			// Larger scale
			x -= x%10;
			y -= y%10;
			mapService.deleteRectangle(info, x, y, scale, scale)
			setHex(info, x, y, terrain)
		}
		
		render terrain
	}

	/**
	 * Set a specific hex to be of the specified terrain type.
	 * TODO: Can we make this more efficient?
	 */
	private void setHex(MapInfo info, int x, int y, int terrain) {
		if (!mapService.isOut(info, x, y)) {
			Hex hex = Hex.find ({
				eq("mapInfo", info)
				eq("x", x)
				eq("y", y)
			});
			if (hex == null) {
				hex = new Hex(x: x, y: y, mapInfo: info)
			}
			hex.terrainId = terrain
			hex.save();
		}
	}
	
	/**
	 * Adds a new place to the map. Specify a map, a thing and a location,
	 * and the place is created and attached to the map.
	 * 
	 * @param id
	 * @param thingId
	 * @param x
	 * @param y
	 * @param sx
	 * @param st
	 * @return
	 */
	def addPlace(String id, String thingId, int x, int y, int sx, int sy) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		Thing		thing = thingService.getThingByNameOrId(thingId)
		
		println "Adding [${thing.name}] to ${x}.${sx},${y}.${sy}"
		
		Place	place = new Place(mapInfo: info, thing: thing, tileX: x, tileY: y, subX: sx, subY: sy)
		place.name = thing.name
		place.title = thing.title
		place.importance = thing.importance
		place.save()
		place.name = thing.name + "-" + place.id
		place.save()
		
		render place as JSON
	}
	
	def updatePlace(String id, String placeId, String name, String title, int x, int y, int sx, int sy) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		Place		place = thingService.getPlaceByNameOrId(placeId)
		
		println "Updating place [${place.name}] to [${name}/${title}]"
		if (name != null) {
			place.name = name;
		}
		if (title != null) {
			place.title = title;
		}
		if (x >= 0 && y >= 0) {
			place.tileX = x;
			place.tileY = y;
			place.subX = sx;
			place.subY = sy;
			place.save();
		}
		render place as JSON
	}
	
	def deletePlace(String id, String placeId) {
		println "Delete " + placeId
		Place		place = thingService.getPlaceByNameOrId(placeId)
		place.delete()
	}
	
	def areas() {
		render Area.findAll() as JSON
	}
	
	/**
	 * Copy one map into another map. If the new map is a different scale, then
	 * scaling is also performed.
	 * 
	 * @param src	Source map to copy from.
	 * @param dest	Destination map to copy to.
	 * @return
	 */
	def copy(String src, String dest) {
		MapInfo		srcInfo = mapService.getMapByNameOrId(src)
		MapInfo		destInfo = mapService.getMapByNameOrId(dest)
		
		if (srcInfo == null || destInfo == null) {
			throw new IllegalArgumentException("Maps not found")
		}
		
		int width = srcInfo.width
		int height = srcInfo.height
		
		for (int sx = 0; sx < width; sx++) {
			for (int sy = 0; sy < height; sy++) {
				Hex hex = Hex.find ({
					eq("mapInfo", srcInfo)
					eq("x", sx)
					eq("y", sy)			
				});

				List list = scaleService.getScaledHexes(sx, sy, srcInfo, destInfo)
				list.each {
					int xx = it.x
					int yy = it.y
					if (xx < 0 || yy < 0 || xx >= destInfo.width || yy >= destInfo.height) {
						// Skip.
					} else {
						println "${sx},${sy} -> ${xx},${yy}" 
						Hex.findAll ({
							eq("mapInfo", destInfo)
							eq("x", xx)
							eq("y", yy)
						}).each {
							it.delete()
						}
		
						Hex n = new Hex(hex)
						n.mapInfo = destInfo
						n.x = xx
						n.y = yy
						n.save()
					}
					
				}
			}
		}
		render "Done"
	}
	
	/**
	 * Display a thumbnail of the map. The thumbnail is generated for the specified
	 * width. Larger thumbnails will take longer to render.
	 * @param id
	 * @param w
	 * @return
	 */
	def thumb(String id, int w) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		
		int			min = Math.min(info.width, info.height)
		int			max = Math.max(info.width, info.height)
		
		int			step = max / w
		if (step < 1) {
			step = 1;
		}
		
		int			width = info.width / step
		int			height = info.height / step
		
		List        terrain = mapService.getThumbData(info,  step)
		
		SimpleImage	img = new SimpleImage(width, height)
		Map colours = [:]
		
		Terrain background = Terrain.findById(info.background)
		colours.put(info.background, background.colour)
		Terrain oob = Terrain.findById(info.oob)
		colours.put(info.oob, oob.colour)
		List	bounds = null
		if (info.world) {
			bounds = mapService.getBounds(info, 0, info.width, step)
		}
		for (int y=0; y < info.height; y+=step) {
			for (int x=0; x < info.width; x+=step) {
				int px = x / step
				int py = y / step
				if (info.world && (y < bounds[px].min || y > bounds[px].max)) {
					String colour = colours.get(info.oob)
					img.rectangleFill(px, py, 1, 1, colour)
				} else {
					String colour = colours.get(info.background)
					img.rectangleFill(px, py, 1, 1, colour)
				}
			}
		}
		
		terrain.each { hex ->
			int tid = hex.t
			int x = hex.x
			int y = hex.y

			String colour = colours.get(tid)
			if (colour == null) {
				Terrain t = Terrain.findById(tid)
				colours.put(tid, t.colour)
				colour = t.colour
			}
			int px = x / step
			int py = y / step
			img.rectangleFill(px, py, 1, 1, colour)
		}
		
		// Work out accurate width/height.
		double scale = (1.0 * w) / Math.max(width, height)
		println "${info.name}: ${width}x${height} ${step} ${scale}"
		
		width = width * scale
		height = height * scale
		
		SimpleImage scaled = img.getScaled(width, height)
		if (height < w) {
			SimpleImage tmp = new SimpleImage(width, w, "#ffffff")
			tmp.paint(scaled.getImage(), 0, 0, width, height)
			scaled = tmp
		}
		byte[] data = scaled.save().toByteArray()
		//scaled.save(new File("/home/sam/thumbnail.jpg"))
		
		response.setContentType("image/jpeg")
		response.setContentLength(data.length)
		OutputStream	out = response.getOutputStream();
		out.write(data)
		out.close()
		return null
	}
}
