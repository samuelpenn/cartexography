class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
		
		"/api/app/info" {
			controller = "appAPI"
			action = "info"
		}
		
		"/api/map/$mapId/terrain" {
			controller = "mapAPI"
			action = "terrain"
		}
		
		"/api/map/$mapId/map" {
			controller = "mapAPI"
			action = "map"
		}
		
		"/api/map/$mapId/update" {
			controller = "mapAPI"
			action = "update"
		}

	}
}
