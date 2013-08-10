/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

import java.sql.Connection;
import java.sql.ResultSet
import java.sql.Statement

import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;

import uk.org.glendale.hexweb.MapInfo
import uk.org.glendale.hexweb.Path
import uk.org.glendale.hexweb.Vertex

/**
 * Service methods for handling vector paths.
 */
class PathService {
	def SessionFactory		sessionFactory
	
	/**
	 * Choose a default unique name for this path. Currently just appends
	 * a count to "untitled-".
	 */
	private void setUniquePathName(Path path) {
		int count = Path.count()
		
		path.name = "untitled-" + (count + 1)
	}
	
	/**
	 * Convert JSON data to a path object. This should work automatically,
	 * but seems to prevent saving the domain object.
	 * 
	 * @param json
	 * @return
	 */
	def jsonToPath(MapInfo info, json) {
		Path path = new Path()
		
		path.id = json.id
		path.mapInfo = info
		path.name = json.name
		if (path.name == "untitled") {
			setUniquePathName(path)
		}
		path.thickness1 = json.thickness1
		path.thickness2 = json.thickness2
		path.style = json.style

		path.vertex = new HashSet()
		json["vertex"].each { v ->
			Vertex vertex = new Vertex()
			if (v.vertex != null) {
				vertex.id = 0
				vertex.vertex = v.vertex
				vertex.x = v.x
				vertex.y = v.y
				vertex.subX = v.subX
				vertex.subY = v.subY
				vertex.path = path;
				path.vertex.add(vertex)
			}
		}
		return path
	}

	/**
	 * Store a new path in the database. The path and all its vertices
	 * are stored.
	 * 
	 * @param path		Path to be saved.
	 */
    def createPath(Path path) {
		println "Save path ${path.name}"
		path.save(failOnError: true)
		
		println "Saved as ${path.id}"
    }
	
	/**
	 * Needed because saving a path that is passed back from JSON doesn't seem to work.
	 * Something about the GORM configuration is wrong. This is a hack to get things
	 * working until the GORM configuration is fixed.
	 * 
	 * All existing vertices on the path are deleted from the database, then recreated
	 * from the object.
	 * 
	 * @param path		Path to update the vertices for.
	 */
	private void saveVertices(Path path) {
		Statement stmnt
		
		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				String sql = String.format("DELETE FROM vertex WHERE path_id=%d", path.id)
				stmnt = connection.prepareStatement(sql)
				stmnt.executeUpdate(sql)				
			}
		})
		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				path.vertex.each { v ->
					String sql = String.format("INSERT INTO vertex (path_id, vertex, x, y, sub_x, sub_y) VALUES (%d, %d, %d, %d, %d, %d)",
						path.id, v.vertex, v.x, v.y, v.subX, v.subY)
					stmnt = connection.prepareStatement(sql)
					stmnt.executeUpdate()
				}
			}
		})

	}
	
	/**
	 * Store an existing path in the database. The path and all its
	 * vertices are stored.
	 * 
	 * @param path		Path to be saved.
	 */
	def updatePath(Path path) {
		println "Save path ${path.name}"
		
		Path	p = Path.findById(path.id)
		p.name = path.name
		p.thickness1 = path.thickness1
		p.thickness2 = path.thickness2
		p.style = path.style
		p.save()
		
		saveVertices(path)
	}
	
	/**
	 * Get data on all the paths that pass through the given area. We only
	 * count paths that have a vertex actually within the area.
	 * 
	 * @param info	Map that this applies to.
	 * @param x		X coordinate of top left.
	 * @param y		Y coordinate of top left.
	 * @param w		Width of area.
	 * @param h		Height of area.
	 * @return
	 */
	def getPathsInArea(MapInfo info, int x, int y, int w, int h) {
		ArrayList	ids = new ArrayList()
		Statement	stmnt
		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				String sql = String.format("SELECT DISTINCT(path_id) FROM path, vertex WHERE "+
					"mapinfo_id = %d AND path.id = vertex.path_id AND "+
					"x BETWEEN %d AND %d AND y BETWEEN %d AND %d",
					info.id, x, x + w, y, y + h)
				stmnt = connection.prepareStatement(sql)
				ResultSet rs = stmnt.executeQuery()
				while (rs.next()) {
					println "Found matching path ${rs.getInt(1)}"
					ids.add(rs.getInt(1))
				}
				rs.close()
			}
		})
		ArrayList	list = new ArrayList()
		ids.each { id ->
			Path path = Path.findById(id)
			list.add(path)
			println "Path ${path.id}:  "
			path.vertex.each { v ->
				print "${v.vertex}, "
			}
			println ""
		}
		
		return list
	}
}
