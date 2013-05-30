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

class ImportController {
	def mapService
	
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
		
		
		
	}

    def index() { 
		return "import"
	}
}
