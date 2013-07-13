/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

import uk.org.glendale.hexweb.MapInfo
import uk.org.glendale.hexweb.Path
import uk.org.glendale.hexweb.Vertex

/**
 * Service methods for handling vector paths.
 */
class PathService {

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

    def createPath(Path path) {
		println "Save path ${path.name}"
		path.save(failOnError: true)
		
		println "Saved as ${path.id}"
    }
	
	def updatePath(Path path) {
		println "Save path ${path.name}"
		
		Path	p = Path.findById(path.id)
		p.name = path.name
		p.thickness1 = path.thickness1
		p.thickness2 = path.thickness2
		p.style = path.style
		
		p.vertex.each { v ->
			v.delete()
		}
		
		p.vertex = path.vertex
		p.save()
		
	}
}
