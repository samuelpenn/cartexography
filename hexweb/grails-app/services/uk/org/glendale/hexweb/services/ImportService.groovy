/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

import uk.org.glendale.hexweb.Area
import uk.org.glendale.hexweb.Hex
import uk.org.glendale.hexweb.MapInfo
import uk.org.glendale.hexweb.Terrain

/**
 * Provides services for the import and conversion of Mapcraft v1 maps.
 * 
 * Mapcraft stored its data in XML files, which used a special compressed
 * format for the tile data. This needs to be parsed.
 * 
 * Mapcraft terrains and things also need to be translated into Hexweb
 * equivalents. This is complicated by the fact that Mapcraft had two layers
 * for tiles - terrain and features. Features were hills and mountains, so
 * a 'Woods' tile with 'Hills' needs to be translated to a single terrain
 * type in Hexweb.
 */
class ImportService {
	def areaService
	def terrainService
	def mapService
	
	private static String BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
	private int fromBase64(String code) {
		int value = 0
		
		code.reverse().eachWithIndex { c, i -> 
			value += (BASE64.indexOf(c) * 64 ** i)
		}
		
		return value
	}
	
	private int getTerrainCode(String blob) {
		return fromBase64(blob[0..1])
	}
	
	private int getHeightCode(String blob) {
		return fromBase64(blob[2..3])
	}
	
	private int getFeatureCode(String blob) {
		return fromBase64(blob[4..5])
	}
	
	private int getAreaCode(String blob) {
		return fromBase64(blob[6..7])
	}

	/**
	 * Import a map into the database. 
	 * 
	 * @param mapInfo		Map to import into.
	 * @param mapcraft		Mapcraft format XML document.
	 * @return
	 */
    def importMap(MapInfo mapInfo, Node mapcraft) {
		MapInfo	template = mapService.getMapByNameOrId(mapInfo.template)
		def  areaMap = [:]
		def	 terrainMap = [:]
		
		// Read areas.
		mapcraft.areas.area.each { a ->
			int areaId = a.'@id' as int
			String areaName = a.'@name' as String
			println areaName
			
			Area area = areaService.getAreaByName(mapInfo, areaName)
			if (area == null) {
				area = new Area(mapInfo: mapInfo, name: areaName)
				area.save()
			}
			println "Mapping [${areaId}] to [${area.id}]"
			areaMap.put(areaId, area.id)
		}
		
		// Read terrain
		mapcraft.terrainset.each { set ->
			if (set.'@id' == "basic") {
				set.terrain.each { t ->
					int  	terrainId = t.'@id' as int
					String 	name = t.name.text()
					
					Terrain terrain = terrainService.getTerrainFromName(template, name)
					terrainMap.put(terrainId, terrain.id)
				}
			}
		}
		// Make sure that the map is empty before we import.
		mapService.clearMap(mapInfo)
		
		// Read blob data.
		mapcraft.tileset.tiles.column.each { column ->
			int x = column.'@x' as int
			int y = 0
			column.text().split().each { blob ->
				int t = getTerrainCode(blob)
				int a = getAreaCode(blob)
				
				t = terrainMap.get(t)
				if (a > 0) {
					a = areaMap.get(a)
				}
				
				Hex hex = new Hex(mapInfo: mapInfo, x: x, y: y, terrainId: t, areaId: a)
				hex.save()
				y++
			}
		}
    }
}
