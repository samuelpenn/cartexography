import grails.converters.JSON

import uk.org.glendale.hexweb.*

class BootStrap {

    def init = { servletContext ->
		JSON.registerObjectMarshaller(Place) {
			def map = [:]
			map['id'] = it.id
			map['name'] = it.name
			map['title'] = it.title
			map['x'] = it.tileX
			map['y'] = it.tileY
			map['sx'] = it.subX
			map['sy'] = it.subY
			map['importance'] = it.importance
			map['thing_id'] = it.thing.id
			
			return map
		}
		JSON.registerObjectMarshaller(Area) {
			def map = [:]
			map['id'] = it.id
			map['name'] = it.name
			map['parent'] = (it.parent != null)?it.parent.id:0
			
			return map
		}
    }
    def destroy = {
    }
}
