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
import uk.org.glendale.hexweb.Path
import uk.org.glendale.hexweb.Place
import uk.org.glendale.hexweb.Terrain
import uk.org.glendale.hexweb.Thing
import uk.org.glendale.hexweb.Area
import uk.org.glendale.hexweb.Vertex
import uk.org.glendale.hexweb.Label
import uk.org.glendale.hexweb.LabelStyle
import uk.org.glendale.hexweb.services.AreaService;


import groovy.sql.Sql
import java.awt.Image
import java.sql.Connection
import java.sql.PreparedStatement;
import java.sql.ResultSet
import java.sql.Statement
import javax.servlet.ServletOutputStream
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.jdbc.Work

class MapAPIController {
	/** Common services for maps */
	def mapService
	def terrainService
	def thingService
	def scaleService
	def textureService
	def pathService
	def areaService
	
	private static int MAX_VARIANTS = 10
	
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
		while (info.template > 0) {
			MapInfo	template = mapService.getMapByNameOrId(info.template)
			list.addAll(Terrain.findAllByMapInfo(template))
			info = mapService.getMapByNameOrId(info.template)
		}
		return list
	}
	
	private List getThings(MapInfo info) {
		List<Thing>  list = Thing.findAllByMapInfo(info)
		while (info.template > 0) {
			MapInfo	template = mapService.getMapByNameOrId(info.template)
			list.addAll(Thing.findAllByMapInfo(template))
			info = mapService.getMapByNameOrId(info.template)
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
	 * Also returns any places and paths that are located within the map area.
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
				property("variant")
			}
			order("y")
			order("x")
		})
		
		list.each { hex ->
			map[hex[1] - y][hex[0] - x] = hex[2] * MAX_VARIANTS + hex[4]
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
						map[yy][xx] = info.oob * MAX_VARIANTS
					} else {
						if (map[yy][xx] == 0) {
							int b = map[yy - yy%10][xx10]
							//println "${xx},${yy} is zero, b is ${b} from ${xx10},${yy - yy%10}"
							if (b != info.oob * MAX_VARIANTS && b != 0) {
								map[yy][xx] = b
							} else {
								map[yy][xx] = info.background * MAX_VARIANTS
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
		List paths = pathService.getPathsInArea(info, x, y, w, h)
		List areas = Area.findAll({
			eq("mapInfo", info)
		});
		List labels = Label.findAll ({
			eq('mapInfo', info)
			between('tileX', x, x + w -1)
			between('tileY', y, y + h - 1)
		})
		
		Map data = new HashMap();
		data.put("map", map)
		data.put("area", area)
		data.put("places", places)
		data.put("paths", paths)
		data.put("areas", areas)
		data.put("labels", labels)
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
		println "Data returned: ${list.size()}"
		List bounds = null
		if (info.world || list.size() != w * h) {
			println "Filtering large map ${list.size()} of ${w * h}"
			// We have gaps in the data, so blank the whole map first. There's
			// no point doing this if there are no gaps. If it is a world map,
			// then we always need to do this.
			Terrain		unknown = terrainService.getTerrainByNameOrId("unknown")
			bounds = mapService.getBounds(info, x, w, scale)
			int	s = scale / 10;
			for (int xx=0; xx < w/scale; xx++) {
				int xx10 = xx - xx%s;
				for (int yy=0; yy < h/scale; yy++) {
					if (bounds.size() > 0 && (yy*scale + y < bounds[xx].min || yy*scale + y > bounds[xx].max)) {
						map[yy][xx] = info.oob
					} else {
						if (map[yy][xx] == 0) {
							int b = map[yy - yy%10][xx10]
							// If blank, fall back to '10th parent', else use background.
							if (s > 1 && b != info.oob && b != 0) {
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
	static int lastVariant = -1
	static int lastArea = -1
	
	/**
	 * True if this update request is identical to the previous update request.
	 * Helps performance if we get multiple updates to the same hex.
	 */
	private synchronized boolean isdup(id, x, y, radius, terrain, variant, area) {
		if (x == lastX && y == lastY && radius == lastRadius && terrain == lastTerrain && id == lastId && area == lastArea && variant == lastVariant) {
			return true
		}
		lastX = x
		lastY = y
		lastTerrain = terrain
		lastVariant = variant
		lastRadius = radius
		lastId = id
		lastArea = area

		return false
	}
	
	/**
	 * Updates a hex with a new terrain, variant and area.
	 * The radius is actually the diameter, and is used for a large brush.
	 */
	def update(String id, int x, int y, int radius, int terrain, int variant, int area, int scale) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		
		if (isdup(info.id, x, y, radius, terrain, variant, area)) {
			render terrain
			return
		}		
		println "Update: ${id}-${x},${y} radius ${radius} scale ${scale} area ${area}"
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
						if (x%10 == 0 && y%10 == 0) {
							mapService.fillBlock(info, x, y);
						}
						setHex(info, x, y, terrain, variant, area);
					}
					x = ox - px;
					if (x >= 0 && x < info.width) {
						if (x%10 == 0 && y%10 == 0) {
							mapService.fillBlock(info, x, y);
						}
						setHex(info, x, y, terrain, variant, area);
					}
				}
			}
		} else {
			// Larger scale
			x -= x%10;
			y -= y%10;
			int r = Math.floor(radius / 2) * 10;
			for (int px = x - r; px <= x + r; px+=10) {
				for (int py = y - r; py <= y + r; py+=10) {
					mapService.deleteRectangle(info, px, py, scale, scale)
					setHex(info, px, py, terrain, variant, area)
				}
			}
		}
		
		render terrain
	}
	
	private int getRandomVariant(int x, int y, int terrain) {
		Terrain		t = terrainService.getTerrainByNameOrId(terrain)
		
		if (t.variants > 0) {
			String 	r = "0000" + Math.sqrt(x*x + y*y)
			r = r.replaceAll('\\.', '')[-5..-1]
			long	l = Long.parseLong(r)
			return l % (t.variants + 1)
		} else {
			return 0
		}
	}

	/**
	 * Set a specific hex to be of the specified terrain type. If variant is
	 * negative, leave it unset if the terrain type itself is not changing.
	 * If the terrain type is also changing, set variant to zero.
	 * 
	 * TODO: Can we make this more efficient?
	 */
	private void setHex(MapInfo info, int x, int y, int terrain, int variant, int area) {
		if (!mapService.isOut(info, x, y)) {
			Hex hex = Hex.find ({
				eq("mapInfo", info)
				eq("x", x)
				eq("y", y)
			});
			if (hex == null) {
				hex = new Hex(x: x, y: y, mapInfo: info)
				variant = getRandomVariant(x, y, terrain)
			} else if (variant == -1 && terrain == hex.terrainId) {
				variant = hex.variant
			} else if (variant == -1) {
				variant = getRandomVariant(x, y, terrain)
			}
			if (terrain > 0) {
				hex.terrainId = terrain
				hex.variant = variant
			} else {
				hex.areaId = area
			}
			//mapService.insertToMap(info, x, y, area, terrain)
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
	
	def updatePlace(String id, String placeId, String name, String title, int thingId, int x, int y, int sx, int sy) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		Place		place = thingService.getPlaceByNameOrId(info, placeId)
		
		println "Updating place [${place.id}:${place.name}] to [${name}/${title}] thing [${thingId}]"
		if (name != null) {
			place.name = name;
		}
		if (title != null) {
			place.title = title;
		}
		if (thingId > 0) {
			println "Getting a thing"
			Thing thing = thingService.getThingByNameOrId(thingId)
			println "Have a thing"
			if (thing != null) {
				println "Setting thing"
				place.thing = thing
			}
		}
		if (x >= 0 && y >= 0) {
			place.tileX = x;
			place.tileY = y;
			place.subX = sx;
			place.subY = sy;
		}
		println "Saving the place"
		place.save();
		render place as JSON
	}
	
	def deletePlace(String id, String placeId) {
		println "Delete place ${placeId} in map ${id}"
		MapInfo		info = mapService.getMapByNameOrId(id)
		Place		place = thingService.getPlaceByNameOrId(info, placeId)
		if (place != null) {
			// XXX: place.delete() doesn't seem to work. Just get transaction errors.
			sessionFactory.currentSession.doWork(new Work() {
				public void execute(Connection connection) {
					String sql = String.format("DELETE FROM place WHERE id=%d", place.id)
					connection.createStatement().executeUpdate(sql)
				}
			})
		}
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
	def copy(String src, String dest, int x, int y) {
		MapInfo		srcInfo = mapService.getMapByNameOrId(src)
		MapInfo		destInfo = mapService.getMapByNameOrId(dest)
		
		if (srcInfo == null || destInfo == null) {
			throw new IllegalArgumentException("Maps not found")
		}
		
		int width = srcInfo.width
		int height = srcInfo.height
		
		int scaleDiff = destInfo.scale / srcInfo.scale;
		if (scaleDiff > 1) {
			println scaleDiff
		}
		render "Clearing destination...<br/>"

		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				String sql = String.format("DELETE FROM map WHERE mapinfo_id=%d AND "+
					"x BETWEEN %d AND %d AND y BETWEEN %d AND %d", destInfo.id, 
					x, x + srcInfo.width * scaleDiff,
					y, y + srcInfo.height * scaleDiff)
				connection.createStatement().executeUpdate(sql)
			}
		})
		render "Destination cleared<br/>"
		
		mapService.copy(srcInfo, destInfo, x, y);
	
		render "Done"
	}
	
	/**
	 * Display a thumbnail of the map. The thumbnail is generated for the specified
	 * width. Larger thumbnails will take longer to render.
	 * @param id
	 * @param w
	 * @return
	 */
	def thumb(String id, int w, boolean forceWidth) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		
		int			min = Math.min(info.width, info.height)
		int			max = Math.max(info.width, info.height)
		
		int			step = max / w
		if (forceWidth) {
			step = info.width / w
		}
		if (step != 1 && step%10 < 5) {
			step -= step%10;
		} else if (step != 1 && step%10 >= 5) {
			step += (10 - step%10)
		}
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
					if (colour == null) {
						println "Cannot get terrain for ${px}, ${py}"	
					} else {
						img.rectangleFill(px, py, 1, 1, colour)
					}
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
				if (t != null) {
					colours.put(tid, t.colour)
					colour = t.colour
				} else {
					println "Cannot find terrain ${tid} at ${x},${y}"
					colour = "#ffffff"
				}
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
		if (height < w && !forceWidth) {
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
	
	def texture(String id) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		
		SimpleImage image = textureService.getTexture(info, 1024)
		
		byte[] data = image.save().toByteArray()
		
		response.setContentType("image/jpeg")
		response.setContentLength(data.length)
		OutputStream	out = response.getOutputStream();
		out.write(data)
		out.close()
		return null
	}
	
	/**
	 * Draws a hexagon of the specified size and returns it as an image.
	 * This is for development purposes only, to be able to generate
	 * plain hexagons to use as a basis for hex tiles.
	 * 
	 * @param size		Length of a hex side, in pixels.
	 */
	def hex (int size) {
		SimpleImage image = new SimpleImage(512, 512, "#000000")
		
		image.hex(10, 10, size, "#ffffff")
		
		byte[] data = image.save().toByteArray()
		
		response.setContentType("image/jpeg")
		response.setContentLength(data.length)
		OutputStream	out = response.getOutputStream();
		out.write(data)
		out.close()
		return null

	}
	
	/**
	 * Create a new path, or update an existing one. The path is passed as
	 * JSON data, and if the id is non-zero then this is considered to be
	 * an update, otherwise it is a creation.
	 * 
	 * @param id	Id of the map this path applies to.
	 */
	def createPath(String id) {
		MapInfo		info = mapService.getMapByNameOrId(id)

		JSON.use('deep')
		def data = request.JSON
		Path path = pathService.jsonToPath(info, data)
		if (path == null) {
			return null
		}
		path.mapInfo = info
		
		if (path.id == 0 || path.id == null) {
			path.id = null
			pathService.createPath(path)
		} else {
			pathService.updatePath(path)
			println "Updated path"
		}
		
		render path as JSON
	}
	
	def deletePath(String id, int pathId) {
		MapInfo		info = mapService.getMapByNameOrId(id)
		Path		path = Path.findById(pathId)
		if (path != null) {
			pathService.deletePath(path)
		}
		render pathId
	}
	
	def test() {
		Path p = Path.findById(19)
		p.name += "x"
		
		pathService.updatePath(p)
		
		render p as JSON
	}
	
	/**
	 * Create a new label on the map.
	 * 
	 * @param id
	 * @param name
	 * @param title
	 * @param x
	 * @param y
	 * @param sx
	 * @param sy
	 * @param style
	 * @param rotation
	 * @return
	 */
	def createLabel(String id, String name, String title, int x, int y, int sx, int sy, int fontSize, String style, int rotation) {
		MapInfo	info = mapService.getMapByNameOrId(id)
		
		Label	label = new Label()
		label.mapInfo = info
		label.name = name
		label.title = title
		label.tileX = x
		label.tileY = y
		label.subX = sx
		label.subY = sy
		label.rotation = rotation
		if (fontSize > 0) {
			label.fontSize = fontSize
		} else {
			label.fontSize = 2
		}
		if (label.style != null) {
			label.style = LabelStyle.valueOf(style)
		} else {
			label.style = LabelStyle.STANDARD
		}
		label.save()
		
		render label as JSON
	}
	
	def updateLabel(String id, int labelId) {
		MapInfo	info = mapService.getMapByNameOrId(id)
		
		println "updateLabel: ${labelId}"
		
		Label	label = Label.findById(labelId)
		
		if (label != null) {
			println "Title: " + params.title
			if (params.name != null) {
				label.name = params.name
				label.title = params.title
				label.fontSize = params.fontSize as int
				label.rotation = params.rotation as int
				label.style = LabelStyle.valueOf(params.style)
			} else if ((params.x as int) > -1) {
				label.tileX = params.x as int
				label.tileY = params.y as int
				label.subX = params.sx as int
				label.subY = params.sy as int
			}
			label.save()
		} else {
			println "No label found"
		}
		
		render label as JSON
	}
	
	def deleteLabel(String id, int labelId) {
		Label label = Label.findById(labelId)
		if (label != null) {
			label.delete()
		}
		render labelId
	}
}
