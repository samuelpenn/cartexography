package uk.org.glendale.hexweb

class Hex {
	MapInfo		mapInfo
	int			x
	int			y
	Terrain		terrain
	
    static constraints = {
    }
	
	static mapping = {
		table "map"
		version false
		mapInfo column: "mapinfo_id"
		terrain column: "terrain_id"
	}
}
