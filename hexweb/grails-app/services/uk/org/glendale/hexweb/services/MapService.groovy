/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

import uk.org.glendale.hexweb.Hex
import uk.org.glendale.hexweb.MapInfo

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import org.hibernate.Session
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work

/**
 * Services needed by Map.
 */
class MapService {
	def SessionFactory		sessionFactory
	
	/**
	 * Gets the map details for the map specified by unique id.
	 * 
	 * @param id	Numeric id.
	 */
	def getMapByNameOrId(int id) {
		return MapInfo.findById(id)
	}

	/**
	 * Gets the map details for the map specified by name. If the
	 * name is actually an integer, then call call the integer
	 * version of the method instead.
	 * 
	 * @param name	Name of the map, or its integer id.
	 */
    def getMapByNameOrId(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Map name cannot be null")
		}
		try {
			int id = Integer.parseInt(name)
			return getMapByNameOrId(id)	
		} catch (NumberFormatException e) {
			// Not an integer, so try by name.
		}
		return MapInfo.findByName(name)
    }
	
	/**
	 * Gets the hex at the specified coordinates for the map. If there is
	 * no hex defined, then null is returned.
	 * 
	 * @param info		Map to get hex for.
	 * @param x			X coordinate.
	 * @param y			Y coordinate.
	 */
	def getHex(MapInfo info, int x, int y) {
		return Hex.find ({
			eq("mapInfo", info)
			eq("x", x)
			eq("y", y)
		});
	}
	
	/**
	 * Gets a row of data from a map. The row may be missing data, and OOB tiles may
	 * not be set correctly. Only gets terrain data.
	 * 
	 * @param info	Map to get data from.
	 * @param y		Y coordinate of row of data.
	 * @return		Array of terrain ids.
	 */
	private int[] getRawMapRow(MapInfo info, y) {
		int[]	data = new int[info.width]
		
		List list = new ArrayList()
		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				String sql = String.format("select x, terrain_id from map where mapinfo_id=%d and y = %d order by x",
										   info.id, y)
				Statement stmnt = connection.prepareStatement(sql)
				ResultSet rs = stmnt.executeQuery(sql)
				while (rs.next()) {
					data[rs.getInt(1)] = rs.getInt(2)
				}
				rs.close()
			}
		})
		return data
	}
	
	/**
	 * Get a row of data for a map. The row is complete, so any sparse data has been
	 * filled in, and OOB tiles have been correctly set. Only terrain data is return.
	 * 
	 * Data is returned as an array of terrain ids.
	 */
	def getMapRow(MapInfo info, int y) {
		int[]	parent = null
		int[]	data = getRawMapRow(info, y)
		if (y%10 == 0) {
			parent = data
		} else {
			parent = getRawMapRow(info, y - y%10)
		}
		for (int x=0; x < info.width; x++) {
			if (isOut(info, x, y)) {
				data[x] = info.oob
			} else if (data[x] == 0 && parent[x - x%10] != 0) {
				data[x] = parent[x - x%10]
			} else if (data[x] == 0) {
				data[x] = info.background
			}
		}
		
		return data
	}
	
	def getMapData(MapInfo info, int x, int y, int w, int h, int resolution) {
		List list = null
		if (resolution < 2) {
			// Full resolution, get every tile within the bounds.
			list = Hex.findAll ({
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
		} else {
			list = new ArrayList()
			sessionFactory.currentSession.doWork(new Work() {
				public void execute(Connection connection) {
					String sql = String.format("select x, y, terrain_id, area_id from map where mapinfo_id=%d and "+
											   "x >= %d AND x < %d AND y >= %d AND y < %d AND x mod %d = 0 and y mod %d = 0 order by y, x",
											   info.id, x, x+w, y, y+h, resolution, resolution)
					println sql
					Statement stmnt = connection.prepareStatement(sql)
					ResultSet rs = stmnt.executeQuery(sql)
					while (rs.next()) {
						list.add([ rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4)])
					}
					rs.close()
				}
			})
		}
		return list
	}

	/**
	 * Removes all hex data from a map. Uses raw SQL for performance.
	 * 
	 * @param info
	 * @return
	 */
	def clearMap(MapInfo info) {
		def session = sessionFactory.getCurrentSession()
		
		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				Statement stmnt = connection.createStatement()
				stmnt.executeUpdate("DELETE FROM map WHERE mapinfo_id=${info.id}")
			}
		})
	}
	
	/**
	 * Delete all hex tiles within the given rectangle. This is used when
	 * performing updates on large scale maps, where we want to clear an
	 * area and revert to sparse data.
	 */
	def deleteRectangle(MapInfo info, int x, int y, int w, int h) {
		def session = sessionFactory.getCurrentSession()
		
		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				Statement stmnt = connection.createStatement()
				String    sql = "DELETE FROM map WHERE mapinfo_id=${info.id} AND " +
								"x BETWEEN ${x} AND ${x + w - 1} AND " +
								"y BETWEEN ${y} AND ${y + h - 1}"
				stmnt.executeUpdate(sql)
			}
		})
	}

	/**
	 * Called when we're about to set a block parent in detail mode. Any tiles
	 * which inherit from the parent need to be individually set to the parent's
	 * old value.	
	 * @param info
	 * @param x		X coordinate of parent.
	 * @param y		Y coordinate of parent.
	 * @return
	 */
	def fillBlock(MapInfo info, int x, int y) {
		// Get current value of parent.
		Hex 		hex = getHex(info, x, y)
		if (hex == null) {
			hex = new Hex()
			hex.terrainId = info.background
			hex.areaId = 0
		}
		boolean[][] map = new boolean[10][10]

		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				String sql = String.format("SELECT x, y FROM map WHERE mapinfo_id=%d AND "+
										   "x BETWEEN %d AND %d AND y BETWEEN %d AND %d",
										   info.id, x, x + 9, y, y+9)
				Statement stmnt = connection.prepareStatement(sql)
				ResultSet rs = stmnt.executeQuery(sql)
				while (rs.next()) {
					// Mark all those that are already set.
					map[rs.getInt(2)-y][rs.getInt(1)-x] = true
				}
				rs.close()
			}
		})
		
		for (int px=0; px < 10; px++) {
			for (int py=0; py < 10; py++) {
				if (!map[py][px]) {
					if (!isOut(info, x + px, y + py)) {
						insertToMap(info, x + px, y + py, hex.areaId, hex.terrainId)
					}
				}
			}
		}

	}
	
	/**
	 * Get terrain data from the map for generating a thumbnail. Uses raw SQL
	 * in order to get the data as quickly as possible.
	 * 
	 * @param info
	 * @param resolution
	 * @return
	 */
	def getThumbData(MapInfo info, int resolution) {
		List	list = new ArrayList()
		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				String sql = String.format("select x, y, terrain_id from map where mapinfo_id=%d and "+
					                       "x mod %d = 0 and y mod %d = 0 order by y, x",
										   info.id, resolution, resolution)
				Statement stmnt = connection.prepareStatement(sql)
				ResultSet rs = stmnt.executeQuery(sql)
				while (rs.next()) {
					list.add([ "x": rs.getInt(1), "y": rs.getInt(2), "t": rs.getInt(3)])
				}
				rs.close()
			}
		})
		return list
	}

	/**
	 * Fast map insert, using raw SQL.	
	 * @param info
	 * @param x
	 * @param y
	 * @param areaId
	 * @param terrainId
	 * @return
	 */
	def insertToMap(MapInfo info, int x, int y, int areaId, int terrainId) {
		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				String sql = String.format("insert into map (mapinfo_id, x, y, area_id, terrain_id) values(%d, %d, %d, %d, %d)",
										   info.id, x, y, areaId, terrainId)
				Statement stmnt = connection.prepareStatement(sql)
				stmnt.executeUpdate(sql)
			}
		})
	}

	def insertToBound(MapInfo info, int x, int min, int max) {
		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				String sql = String.format("insert into bound (mapinfo_id, x, min, max) values(%d, %d, %d, %d)",
										   info.id, x, min, max)
				Statement stmnt = connection.prepareStatement(sql)
				stmnt.executeUpdate(sql)
			}
		})
	}
	
	/**
	 * Gets the minimum and maximum Y values for a map by X column. For a
	 * rectangular (non-world) map, these values are always 0 and map height,
	 * so we don't bother to store them. For a world map however, they vary
	 * a lot because of out of bounds areas. We store these in the database
	 * because it is faster to retrieve them, then it is to re-calculate them
	 * each time.
	 * 
	 * Normally we don't get the whole range, just over the area being viewed
	 * (which is what 'x' and 'width' define). If we are displaying a low
	 * resolution map, then set resolution > 1, so we only get every 'resolution'
	 * column.
	 * 
	 * @param info			Map to get them for.
	 * @param x				X position to start from.
	 * @param width			Width of range to get.
	 * @param resolution	Step size, if > 1 only get every 'resolution' columns.
	 * @return
	 */
	def getBounds(MapInfo info, int x, int width, int resolution) {
		List	list = new ArrayList()
		println "getBounds: ${info.id} ${x} ${width}"
		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				String sql = null;
				
				if (resolution < 2) {
					sql = String.format("select min, max from bound where mapinfo_id=%d and "+
											   "x >= %d and x < %d order by x",
											   info.id, x, x + width)
				} else {
					sql = String.format("select min, max from bound where mapinfo_id=%d and "+
											   "x >= %d and x < %d and x mod %d = 0 order by x",
											   info.id, x, x + width, resolution)
				}
				Statement stmnt = connection.prepareStatement(sql)
				ResultSet rs = stmnt.executeQuery(sql)
				while (rs.next()) {
					list.add([ "min": rs.getInt(1), "max": rs.getInt(2)])
				}
				rs.close()
			}
		})
		println "Bounds size ${list.size()}"
		return list

	}

	/**
	 * Returns true if the coordinates are out of bounds of the world surface.
	 * This is used on world maps, where the map is a flattened icosohedron.
	 * The tiles 'outside' of this surface should be marked as such, and cannot
	 * be set either.
	 * 
	 * A world map looks something like this:
	 * 
	 *    /\  /\  /\  /\  /\
	 *   /  \/  \/  \/  \/  \
	 *   \                   \
	 *    \                   \
	 *     \  /\  /\  /\  /\  /
	 *      \/  \/  \/  \/  \/
	 * 
	 * @param info	Map.
	 * @param x		X coordinate of tile to check.
	 * @param y		Y coordinate of tile to check.
	 * @return		True iff location is out of bounds.
	 */
	def isOut(MapInfo info, x, y) {
		if (info.world) {
			int span = (info.width / 11)
			if (span %4 != 0) {
				span -= span%4
			}
			int topThird = span * 1.5
			int bottomThird = topThird * 2

			int min = Math.abs(((x) % (span * 2)) - span) * 1.5
			int max = (topThird * 3 + 2 - x%2) - Math.abs(((x - span - 1) % (span * 2)) - span) * 1.5
			if (x > span * 11 + 1) {
				min = max + 1
			} else if (x > span * 10) {
				min = topThird + (x - span*10) * 1.5
			}
			
			if (y < min || y > max) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Create a new map, and generate any required supporting data.
	 * 
	 * @param name
	 * @param title
	 * @param width
	 * @param height
	 * @param scale
	 * @param world
	 * @param template
	 * @return
	 */
	def createMap(String name, String title, int width, int height, int scale, boolean world, MapInfo template) {
		if (world) {
			// If a world map, then there are restrictions on the size.
			if (width%44 != 0) {
				width += 44 - (width % 44)
			}
			width += 2
			height = 2 + ((width / 11) * 1.5 * 3) as int
		}
		
		MapInfo info = new MapInfo(name: name, title: title, width: width, height: height, scale: scale, world: world, template: template.id)
		info.save()

		if (world) {
			int span = (info.width / 11)
			if (span %4 != 0) {
				span -= span%4
			}
			int topThird = span * 1.5
			int bottomThird = topThird * 2

			for (int x=0; x < width; x++) {
				int min = Math.abs(((x) % (span * 2)) - span) * 1.5
				int max = (topThird * 3 + 2 - x%2) - Math.abs(((x - span - 1) % (span * 2)) - span) * 1.5
				if (x > span * 11 + 1) {
					min = max + 1
				} else if (x > span * 10) {
					min = topThird + (x - span*10) * 1.5
				}
				insertToBound(info, x, min, max)
			}
		}
	}
}
