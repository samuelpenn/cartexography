/*
bc * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
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
import uk.org.glendale.hexweb.Path
import uk.org.glendale.hexweb.PathStyle
import uk.org.glendale.hexweb.Place
import uk.org.glendale.hexweb.Terrain
import uk.org.glendale.hexweb.Thing
import uk.org.glendale.hexweb.Vertex

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
	def pathService
	
	private static String BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
	private int fromBase64(String code) {
		int value = 0
		
		code.reverse().eachWithIndex { c, i -> 
			value += (BASE64.indexOf(c) * 64 ** i)
		}
		
		return value
	}
	
	/**
	 * Gets the terrain id part of the tile blob.
	 *
	 * @param blob	Base64 data blob for the tile.
	 * @return		Terrain id.
	 */
	int getTerrainCode(String blob) {
		return fromBase64(blob[0..1])
	}

	/**
	 * Gets the height value part of the tile blob.
	 * 	
	 * @param blob	Base64 data blob for the tile.
	 * @return		Height, in metres.
	 */
	int getHeightCode(String blob) {
		return fromBase64(blob[2..3])
	}
	
	/**
	 * Gets the feature id part of the tile blob.
	 * 
	 * @param blob	Base64 data blob for the tile.
	 * @return		Feature id.
	 */
	int getFeatureCode(String blob) {
		return fromBase64(blob[4..5])
	}
	
	/**
	 * Gets the area id part of the tile blob.
	 * 
	 * @param blob  Base 64 data blob for the tile.
	 * @return		Area id.
	 */
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
		
		// Make sure that the map is empty before we import.
		mapService.clearMap(mapInfo)

		// Read areas.
		mapcraft.areas.area.each { a ->
			int areaId = a.'@id' as int
			String areaName = a.'@name' as String
			String areaUri = a.'@uri' as String
			if (areaUri == null) {
				areaUri = areaName.toLowerCase()
			}
			
			Area area = areaService.getAreaByName(mapInfo, areaUri)
			if (area == null) {
				area = new Area(mapInfo: mapInfo, name: areaUri, title: areaName)
				area.save(flush:true, failOnError: true)
			}
			println "Mapping [${areaName}] to [${area.id}]"
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
		terrainService.terrainCache = [:]
		
		// Read blob data.
		mapcraft.tileset.tiles.column.each { column ->
			int x = column.'@x' as int
			int y = 0
			if (x % 10 == 0) {
				println "Column ${x}"
			}
			column.text().split().each { blob ->
				int t = getTerrainCode(blob)
				int f = getFeatureCode(blob)
				int a = getAreaCode(blob)
				
				Terrain terrain = terrainService.getTerrainFromName(template, 
										terrainMap.get(t), featureMap.get(f))
				if (terrain == null) {
					println "NO TERRAIN MAP! t = ${t} f = ${f}"
				} else {
					
					if (a > 0 && areaMap.get(a) != null) {
						a = areaMap.get(a)
					} else {
						a = 0
					}
					
					Hex hex = new Hex(mapInfo: mapInfo, x: x, y: y, terrainId: terrain.id, areaId: a)
					hex.save()
				}
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
			
			name = mapInfo.name + "-" + name.toLowerCase().replaceAll(" ", "-")
			
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
		
		mapcraft.tileset.paths.path.each { p ->
			String 	name = p.'@name'
			String  type = p.'@type'
			String  style = p.'@style'
			
			println "Importing path [${name}]"
			
			Path	path = new Path(mapInfo: mapInfo, name: name)
			if (type == "river") {
				path.style = PathStyle.RIVER
			} else if (type == "road") {
				path.style = PathStyle.ROAD
			}
			
			Node	startNode = p.start[0]
			Node	endNode	= p.end[0]
			
			if (startNode == null || endNode == null) {
				println "Path has no start or end nodes"
			} else {
				path.thickness1 = startNode.'@width' as int
				path.thickness2 = endNode.'@width' as int
				
				path.save(failOnError: true, flush:true)
				println "Saved path as ${path.id}"
				
				Vertex vertex = new Vertex(vertex: 0, x: startNode.'@x' as int, y: startNode.'@y' as int)
				vertex.id = 0
				vertex.path = path
				vertex.subX = vertex.x % 100
				vertex.subY = vertex.y % 100
				vertex.x /= 100
				vertex.y /= 100
				
				List vertices = new ArrayList()
				vertices.add(vertex)
				println "Added first vertex"
				int i = 1
				p.path.each { v ->
					int x = v.'@x' as int
					int y = v.'@y' as int
					int sx = x % 100
					int sy = y % 100
					x /= 100
					y /= 100
					vertices.add(new Vertex(id: 0, path: path, vertex: i++, x: x, y: y, subX: sx, subY: sy))
				}
				vertex = new Vertex(vertex: i, x: endNode.'@x' as int, y: endNode.'@y' as int)
				vertex.subX = vertex.x % 100
				vertex.subY = vertex.y % 100
				vertex.x /= 100
				vertex.y /= 100
				vertices.add(vertex)
				
				pathService.addVertices(path, vertices)
			}
		}
    }
}
