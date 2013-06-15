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
			// Only bother to check if this is a world map.
			int span = (info.width / 11)
			if (span %4 != 0) {
				span -= span%4
			}
			int topThird = span * 1.5
			int bottomThird = topThird * 2
			
			//span = 16
			//topThird = 24
			
			bottomThird = topThird * 2
			
			
			if (y < topThird && x > span * 10) {
				return true
			} else if (y < topThird) {
				int d = Math.abs(((x) % (span * 2)) - span) * 1.5
				if (y < d) {
					return true
				}
			} else if (y <= bottomThird) {
				if (x <  (2 * (y - topThird)) / 3) {
					return true
				//} else if (x + 1 > span * 10 + (2 * (y - topThird) + 2 + x%2) / 3) {
				} else if (x - span*10 > (2 * (y - topThird) + 1) / 3) {
					return true
				}
			} else if (x > span * 11 + 1) {
				return true
			} else {
				int h = topThird * 3 + 1;
				int d = Math.abs(((x - span - 1) % (span * 2)) - span) * 1.5
				if ((h - y) < d) {
					return true
				}
			}
		} 
		return false
	}
}
