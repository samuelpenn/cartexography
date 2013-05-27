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
import uk.org.glendale.hexweb.Hex
import uk.org.glendale.hexweb.MapInfo
import uk.org.glendale.hexweb.Place
import uk.org.glendale.hexweb.Terrain
import uk.org.glendale.hexweb.Thing
import uk.org.glendale.hexweb.Area


import groovy.sql.Sql

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
		
		for (int y=0; y < info.height; y++) {
			for (int x=0; x < info.width; x++) {
				if (mapService.getHex(info, x, y) == null) {
					Hex hex = new Hex(mapInfo: info, x: x, y: y, terrain: fill)
					hex.save()
				}
			}
		}
		
		render "Done"
	}
	
	def randomFill(String id) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		
		Terrain		ocean = Terrain.findById(1)
		Terrain 	land = Terrain.findById(3)
		
		for (int y=0; y < info.height; y++) {
			for (int x=0; x < info.width; x++) {
				Terrain t = ocean
				if ((int)(Math.random()*3) == 0) {
					t = land
				}
				Hex hex = new Hex(mapInfo: info, x: x, y: y, terrain: t)
				hex.save()
			}
		}
		
		render "Done"
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
		
		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
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
				property("terrain.id")
				property("areaId")
			}
		})
		
		list.each { hex ->
			map[hex[1] - y][hex[0] - x] = hex[2]
			area[hex[1] - y][hex[0] - x] = hex[3]
		}
		
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
		
		render data as JSON
	}
	
	/**
	 * Updates a hex with a new terrain.
	 */
	def update(String id, int x, int y, int radius, int terrain) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		
		println "Update: ${id}-${x},${y} radius ${radius}"
		if (radius < 1) {
			radius == 1;
		} else if (radius %2 == 0) {
			radius--;
		}
		
		int ox = x;
		int oy = y;
		for (int px = 0; px < (int)Math.floor(radius / 2) + 1; px++) {
			int	 h = radius - px;
			
			for (int py = 0; py < h; py ++) {
				y = oy + py - (int)Math.floor(h / 2);
				if (px%2 == 1) {
					y += ox%2;
				}
				
				x = ox + px;
				setHex(info, x, y, terrain);
				x = ox - px;
				setHex(info, x, y, terrain);
			}
		}
	
		
		render terrain
	}

	/**
	 * Set a specific hex to be of the specified terrain type.
	 * TODO: Can we make this more efficient?
	 */
	private void setHex(MapInfo info, int x, int y, int terrain) {
		Hex hex = Hex.find ({
			eq("mapInfo", info)
			eq("x", x)
			eq("y", y)			
		});
		if (hex == null) {
			hex = new Hex(x: x, y: y, mapInfo: info)
		}
		hex.terrain = Terrain.findById(terrain);
		hex.save();
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
	
	def copy(int src, int dest) {
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
}
