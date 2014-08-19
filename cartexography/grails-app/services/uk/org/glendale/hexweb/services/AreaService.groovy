/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work

import uk.org.glendale.hexweb.Area
import uk.org.glendale.hexweb.MapInfo


/**
 * Provide services for managing named areas. Each hex tile on the map
 * can belong to a single area. Areas can have parents/children, and are
 * visibly denoted on the map by borders.
 */
class AreaService {
	def SessionFactory		sessionFactory
	
    def getAreaByName(MapInfo info, String name) {
		return Area.find ({
			eq("mapInfo", info)
			eq("name", name)
		});
    }
	
	def getAreas(MapInfo info) {
		return Area.findAll ({
			eq("mapInfo", info)
		});
	}
	
	/**
	 * Remove all named areas from the map. This does not unassign
	 * any tile data, so after calling this, tiles may point to areas
	 * which no longer exist.
	 * 
	 * @param info		Map to remove the areas from.
	 */
	def clearAreas(MapInfo info) {
		Area.findAll {
			eq("mapInfo", info)
		}.each {
			it.delete()
		}
	}
	
	
	def getBounds(MapInfo info, Area area) {
		println "AreaService.getBounds: ${info.id} Area ${area.id}"

		def bounds = null
		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				
				def sql = String.format("select min(x) as min_x, max(x) as max_x, min(y) as min_y, max(y) as max_y from map "+
					"where mapinfo_id=%d and area_id = %d", info.id, area.id)

				Statement stmnt = connection.prepareStatement(sql)
				ResultSet rs = stmnt.executeQuery(sql)
				if (rs.next()) {
					bounds = [ "min_x": rs.getInt(1), "max_x": rs.getInt(2), "min_y" : rs.getInt(3), "max_y" : rs.getInt(4)]
				}
				rs.close()
			}
		})
		return bounds

	}
}
