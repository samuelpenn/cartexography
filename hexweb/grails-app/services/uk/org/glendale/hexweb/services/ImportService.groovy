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
import uk.org.glendale.hexweb.Place
import uk.org.glendale.hexweb.Terrain
import uk.org.glendale.hexweb.Thing

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
	def thingService
	
	private static String BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
	private int fromBase64(String code) {
		int value = 0
		
		code.reverse().eachWithIndex { c, i -> 
			value += (BASE64.indexOf(c) * 64 ** i)
		}
		
		return value
	}
	
	int getTerrainCode(String blob) {
		return fromBase64(blob[0..1])
	}
	
	int getHeightCode(String blob) {
		return fromBase64(blob[2..3])
	}
	
	int getFeatureCode(String blob) {
		return fromBase64(blob[4..5])
	}
	
	int getAreaCode(String blob) {
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
		def  featureMap = [:]
		def	 thingMap = [:]
		
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
					
					terrainMap.put(terrainId, name)
				}
			} else if (set.'@id' == "features") {
				set.terrain.each { t ->
					int		featureId = t.'@id' as int
					String	name = t.name.text()
					featureMap.put(featureId, name)
				}
			} else if (set.'@id' == "things") {
				set.terrain.each { t ->
					int		thingId = t.'@id' as int
					String	name = t.name.text()
					thingMap.put(thingId, thingService.getThingFromName(mapInfo, name))
				}	
			}
		}
		// Make sure that the map is empty before we import.
		mapService.clearMap(mapInfo)
		terrainService.terrainCache = [:]
		
		// Read blob data.
		mapcraft.tileset.tiles.column.each { column ->
			int x = column.'@x' as int
			int y = 0
			println "Column ${x}"
			column.text().split().each { blob ->
				int t = getTerrainCode(blob)
				int f = getFeatureCode(blob)
				int a = getAreaCode(blob)
				
				Terrain terrain = terrainService.getTerrainFromName(template, 
										terrainMap.get(t), featureMap.get(f))
				
				if (a > 0) {
					a = areaMap.get(a)
				}
				
				Hex hex = new Hex(mapInfo: mapInfo, x: x, y: y, terrainId: terrain.id, areaId: a)
				hex.save()
				y++
			}
		}
		println "Done tiles"
		
		// Import places
		mapcraft.tileset.things.thing.each { thing ->
			int type = thing.'@type' as int
			int x = thing.'@x' as int
			int y = thing.'@y' as int
			String name = thing.name.text()
			String description = thing.description.text()
			int importance = thing.importance.text() as int
			
			println "Importing [${name}] of type [${type}]"
			
			Thing	t = thingMap.get(type)
			if (t != null) {
				Place place = new Place(mapInfo: mapInfo, thing: t, name: name, title: name)
				place.importance = importance
				place.tileX = x / 100
				place.tileY = y / 100
				place.subX = x % 100
				place.subY = y % 100
				place.save()
			} else {
				println "Unrecognised thing [${name}]"
			}
		}
		println "Done places"
    }
}
