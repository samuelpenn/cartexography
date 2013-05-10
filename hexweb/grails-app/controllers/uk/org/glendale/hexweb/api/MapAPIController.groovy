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
import uk.org.glendale.hexweb.Terrain

import groovy.sql.Sql

class MapAPIController {
	/** Common services for maps */
	def mapService
	def terrainService

	
	/**
	 * Returns information about this map.
	 * 
	 * GET: /api/map/{id}/info
	 */
	def info(String id) {
		MapInfo	info = mapService.getMapByNameOrId(id)
		
		def data = [ info: info, terrain: getTerrain(info) ]
		
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
	 * @param map	Map to get.
	 * @param x		X coordinate of top left.
	 * @param y		Y coordinate of top left.
	 * @param w		Width.
	 * @param h		Height.
	 * @return
	 */
	def map(String id, int x, int y, int w, int h) {
		MapInfo		info = mapService.getMapByNameOrId(id)

		int[][]		map = new int[h][w]
		
		List list = Hex.findAll ({
			eq('mapInfo', info)
			between('x', x, x + w -1)
			between('y', y, y + h - 1)
			
			projections {
				property("x")
				property("y")
				property("terrain.id")
			}
		})
		
		list.each { hex ->
			map[hex[1] - y][hex[0] - x] = hex[2]
		}
		
		println "Size: " + list.size()
		
		render map as JSON
	}
	
	def update(String id, int x, int y, int terrain) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		
		println "Update: ${id}-${x},${y}"

		Hex hex = Hex.find ({
			eq("mapInfo", info)
			eq("x", x)
			eq("y", y)
		});
		if (hex == null) {
			println "Nothing found"
			hex = new Hex(x: x, y: y, mapInfo: info)
		} else {
			println "Found " + hex.x + "," + hex.y
		}
		hex.terrain = Terrain.findById(terrain);
		hex.save();
		println "Saved"
		
		render terrain
	}
}
