package uk.org.glendale.hexweb.services

import uk.org.glendale.hexweb.Area
import uk.org.glendale.hexweb.MapInfo

class AreaService {

    def getAreaByName(MapInfo info, String name) {
		return Area.find ({
			eq("mapInfo", info)
			eq("name", name)
		});
    }
}
