/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.ui

import com.sun.org.apache.xerces.internal.parsers.XMLParser
import uk.org.glendale.hexweb.MapInfo

/**
 * Controller for the import and conversion of Mapcraft v1 maps.
 */
class ImportController {
	def mapService
	def importService

	def config = [ 'water.sea': 'sea' ]
	
	def upload() {
		println "upload:"
		
		def fileName = URLDecoder.decode(request.getHeader('X-File-Name'), 'UTF-8') as String
		
		def name 		= "mapUploadr"
		def info		= session.getAttribute('uploadr')
		def myInfo      = (name && info && info.containsKey(name)) ? info.get(name) : [:]
		def savePath	= ((myInfo.containsKey('path')) ? myInfo.get('path') : "/tmp") as String
		
		InputStream stream = request.getInputStream()
		String		xml = ""
		stream.eachLine { 
			xml += it + "\n"
		}

		def map = new XmlParser().parseText(xml)
		String mapName = map.header.name.text()
		String mapTitle = mapName
		
		int scale = (map.tileset.dimensions.scale.text() as int) * 1000
		int width = map.tileset.dimensions.width.text() as int
		int height = map.tileset.dimensions.height.text() as int
		
		
		MapInfo mapInfo = MapInfo.findByName(mapName)
		if (mapInfo == null) {
			mapInfo = new MapInfo()
			mapInfo.name = mapName
			mapInfo.title = mapName
			mapInfo.scale = scale
			mapInfo.width = width
			mapInfo.height = height
			mapInfo.template = 1
			mapInfo.save()
		}
		
		println "${width}x${height} at ${scale}"
		
		
	}

    def index() { 
		return "import"
	}
}
