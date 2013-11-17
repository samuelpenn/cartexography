class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/" {
			controller = "admin"
			action = "index"
		}
		"/admin/create" {
			controller = "admin"
			action = "create"
		}
		"500"(view:'/error')
		
		"/map/$id" {
			controller = "map"
			action = "editMap"
			view = "index"
		}

		"/view/$id" {
			controller = "view"
			action = "viewMap"
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

		"/api/app/test" {
			controller = "appAPI"
			action = "test"
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

		"/api/map/$id/largemap" {
			controller = "mapAPI"
			action = "largeMap"
		}

		"/api/map/$id/update" {
			controller = "mapAPI"
			action = [ PUT: "update" ]
		}

		"/api/map/$id/place" {
			controller = "mapAPI"
			action = [ POST: "addPlace" ]
		}

		"/api/map/$id/place/$placeId" {
			controller = "mapAPI"
			action = [ PUT: "updatePlace", DELETE: "deletePlace" ]
		}

		"/api/map/$id/label" {
			controller = "mapAPI"
			action = [ POST: "createLabel" ]
		}

		"/api/map/$id/label/$labelId" {
			controller = "mapAPI"
			action = [ PUT: "updateLabel" ]
			parseRequest: true
		}

		"/api/map/$id/fill" {
			controller = "mapAPI"
			action = "fillMap"
		}

		"/api/map/copy" {
			controller = "mapAPI"
			action = "copy"
		}

		"/api/map/$id/area" {
			controller = "mapAPI"
			action = "areas"
		}

		"/api/map/$id/thumb" {
			controller = "mapAPI"
			action = "thumb"
		}

		"/api/map/$id/texture" {
			controller = "mapAPI"
			action = "texture"
		}
		
		"/api/map/$id/path" {
			controller = "mapAPI"
			action = [ POST: "createPath", PUT: "updatePath" ]
			parseRequest: true
		}

		"/api/map/$id/path/$pathId" {
			controller = "mapAPI"
			action = [ DELETE: "deletePath" ]
		}

		"/api/map/test" {
			controller = "mapAPI"
			action = "test"
		}
		
		"/image/$id" {
			controller = "imageAPI"
			action = "imageByCoord"
		}

	}
}
