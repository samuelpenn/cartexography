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

    def terrain(int mapId) { 
		MapInfo		info = MapInfo.findById(mapId)
		
		List<Terrain>  list = Terrain.findAll()
		
		render list as JSON
	}
	
	def oceanFill(int mapId) {
		MapInfo		info = MapInfo.findById(mapId)
		
		Terrain		ocean = Terrain.findById(1)
		
		for (int y=0; y < info.height; y++) {
			for (int x=0; x < info.width; x++) {
				Hex hex = new Hex(mapInfo: info, x: x, y: y, terrain: ocean)
				hex.save()
			}
		}
		
		render "Done"
	}
	
	def randomFill(int mapId) {
		MapInfo		info = MapInfo.findById(map)
		
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
	def map(int mapId, int x, int y, int w, int h) {
		MapInfo		info = MapInfo.findById(mapId)

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
	
	def update(int mapId, int x, int y, int terrain) {
		MapInfo		info = MapInfo.findById(mapId)
		
		println "Update: ${mapId}-${x},${y}"

		Hex hex = Hex.findAll ({
			eq("mapInfo", info)
			eq("x", x)
			eq("y", y)
		}).first();
		if (hex == null) {
			println "Nothing found"
		} else {
			println "Found " + hex.x + "," + hex.y
		}
		hex.terrain = Terrain.findById(terrain);
		hex.save();
	}
}
