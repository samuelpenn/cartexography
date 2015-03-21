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
		int count = Path.createCriteria().get {
			projections {
				max "id"
			}
		} as int
		
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
		
		if (json == null || json.size() == 0) {
			print "JSON is not set"
			return null
		}
		print "Json data for path is [${json}]"
		
		path.id = json.id
		path.mapInfo = info
		path.name = json.name
		if (path.name == "untitled") {
			setUniquePathName(path)
		}
		if (json.thickness1 != null) {
			path.thickness1 = json.thickness1
		}
		if (json.thickness2 != null) {
			path.thickness2 = json.thickness2
		}
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
	
	def deletePath(Path path) {
		Statement stmnt

		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				String sql = String.format("DELETE FROM vertex WHERE path_id=%d", path.id)
				stmnt = connection.prepareStatement(sql)
				stmnt.executeUpdate(sql)
				stmnt.close()
			}
		})
		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				String sql = String.format("DELETE FROM path WHERE id=%d", path.id)
				stmnt = connection.prepareStatement(sql)
				stmnt.executeUpdate(sql)
				stmnt.close()
			}
		})

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
				println sql
				stmnt = connection.prepareStatement(sql)
				stmnt.executeUpdate(sql)				
			}
		})
		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				path.vertex.each { v ->
					if (v != null) {
						println "Insert..."
						String sql = String.format("INSERT INTO vertex (path_id, vertex, x, y, sub_x, sub_y) VALUES (%d, %d, %d, %d, %d, %d)",
							path.id, v.vertex, v.x, v.y, v.subX, v.subY)
						println sql
						try {
							stmnt = connection.prepareStatement(sql)
							stmnt.executeUpdate()
						} catch (Exception e) {
							println "Exception ${e}"
							throw e
						}
						println "...Done"
					}
				}
				println "Done all vertices"
			}
		})
		println "Done all sessions"
	}
	
	def addVertices(Path path, List vertices) {
		sessionFactory.currentSession.doWork(new Work() {
			public void execute(Connection connection) {
				vertices.each { v ->
					String sql = String.format("INSERT INTO vertex (path_id, vertex, x, y, sub_x, sub_y) VALUES (%d, %d, %d, %d, %d, %d)",
						path.id, v.vertex, v.x, v.y, v.subX, v.subY)
					Statement stmnt = connection.prepareStatement(sql)
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
		println "Save path [${path.id}:${path.name}]"
		
		Path	p = Path.findById(path.id)
		p.name = path.name
		p.thickness1 = path.thickness1
		p.thickness2 = path.thickness2
		p.style = path.style
		p.save()
		println "Saved path"
		
		saveVertices(path)
		println "Saved vertices"
		
		return null
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
					//println "Found matching path ${rs.getInt(1)}"
					ids.add(rs.getInt(1))
				}
				rs.close()
			}
		})
		ArrayList	list = new ArrayList()
		ids.each { id ->
			Path path = Path.findById(id)
			list.add(path)
			//println "Path ${path.id}:  "
			path.vertex.each { v ->
				//print "${v.vertex}, "
			}
			//println ""
		}
		
		return list
	}
}
