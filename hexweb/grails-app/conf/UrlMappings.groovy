class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
		
		"/map/$id" {
			controller = "map"
			action = "editMap"
			view = "index"
		}
		
		"/map/$id/view" {
			controller = "map"
			action = "viewMap"
			view = "index"
		}
		
		
		"/api/app/info" {
			controller = "appAPI"
			action = "info"
		}

		"/api/app/create" {
			controller = "appAPI"
			action = [ POST: "createMap" ]
		}

		"/api/map/$id/info" {
			controller = "mapAPI"
			action = [ GET: "info", PUT: "updateInfo" ]
		}
		
		"/api/map/$id/terrain" {
			controller = "mapAPI"
			action = "terrain"
		}

		"/api/map/$id/map" {
			controller = "mapAPI"
			action = "map"
		}
		
		"/api/map/$id/update" {
			controller = "mapAPI"
			action = "update"
		}

		"/api/map/$id/place" {
			controller = "mapAPI"
			action = [ POST: "addPlace" ]
		}

		"/api/map/$id/place/$placeId" {
			controller = "mapAPI"
			action = [ PUT: "updatePlace", DELETE: "deletePlace" ]
		}

		"/api/map/$id/fill" {
			controller = "mapAPI"
			action = "fillMap"
		}

	}
}
