/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */

VIEW.mouseDown = 0;
VIEW.recordX = -1;
VIEW.recordY = -1;

function debug(msg) {
	$("#debug").append(msg + "<br/>");
}

function selectTerrain(id) {
	VIEW.brushMode = BRUSH_MODE.TERRAIN;
	VIEW.editMode = EDIT_MODE.PAINT;
	
	if (id == 0) {
		id = 3;
	}
	$("#pathStyle"+VIEW.brushStyle).removeClass("selectedButton");
	$("#terrainPopout").remove();
	VIEW.terrainBrush = id;
	
	var url = VIEW.imageBase + "/terrain/" + MAP.images[VIEW.terrainBrush].name + ".png";
	$("#terrainMenu").attr("src", url);
}


function openTerrainMenu() {
	var x = $("#terrainMenu").position().left + 96;
	var y = $("#terrainMenu").position().top;
	
	$("#thingPopout").remove();
	if (document.getElementById("terrainPopout") != null) {
		// Toggle on/off.
		$("#terrainPopout").remove();
		return;
	}

	$("body").append("<div id='terrainPopout' class='floating'></div>");
	$("#terrainPopout").css("position", "absolute");
	$("#terrainPopout").css("left", x);
	$("#terrainPopout").css("top", y);
	$("#terrainPopout").css("width", "75%");
	$("#terrainPopout").css("height", "auto");
	$("#terrainPopout").css("border", "1px solid #999999");
	
	for (var id in MAP.images) {
		var  t = MAP.images[id];
		if (id < 3) {
			continue;
		}
		var  path = VIEW.imageBase + "terrain/" + t.name + ".png";
		
		$("#terrainPopout").append("<div class='tilebox' id='t_"+id+"' onclick='selectTerrain("+id+")'></div>");
		
		$("#t_"+id).append("<img src='"+path+"' height='64px'/>");
		$("#t_"+id).append(t.title);
	}
}

function selectThing(id) {
	VIEW.brushMode = BRUSH_MODE.THING;
	VIEW.editMode = EDIT_MODE.ADD;

	$("#pathStyle"+VIEW.brushStyle).removeClass("selectedButton");
	$("#thingPopout").remove();
	VIEW.thingBrush = id;
	
	var url = VIEW.imageBase + "/things/" + MAP.things[VIEW.thingBrush].name + ".png";
	$("#thingMenu").attr("src", url);
}

function openThingMenu() {
	var x = $("#thingMenu").position().left + 96;
	var y = $("#thingMenu").position().top;
	
	$("#terrainPopout").remove();
	if (document.getElementById("thingPopout") != null) {
		// Toggle on/off.
		$("#thingPopout").remove();
		return;
	}

	$("body").append("<div id='thingPopout' class='floating'></div>");
	$("#thingPopout").css("position", "absolute");
	$("#thingPopout").css("left", x);
	$("#thingPopout").css("top", y);
	$("#thingPopout").css("width", "75%");
	$("#thingPopout").css("height", "auto");
	$("#thingPopout").css("border", "1px solid #999999");
	
	for (var id in MAP.things) {
		var  t = MAP.things[id];
		if (id < 3) {
			continue;
		}
		var  path = VIEW.imageBase + "things/" + t.name + ".png";
		
		$("#thingPopout").append("<div class='tilebox' id='th_"+id+"' onclick='selectThing("+id+")'></div>");
		
		$("#th_"+id).append("<img src='"+path+"' height='64px'/>");
		$("#th_"+id).append(t.title);
	}
}

function setPathStyle(style) {
	VIEW.brushMode = BRUSH_MODE.PATH
	VIEW.editMode = EDIT_MODE.NEW;

	$("#terrainPopout").remove();
	$("#thingPopout").remove();
	
	$("#pathStyle"+VIEW.brushStyle).removeClass("selectedButton");
	VIEW.brushStyle = style;
	$("#pathStyle"+VIEW.brushStyle).addClass("selectedButton");
}

function unclickMap(event) {
	VIEW.mouseDown = 0;
	drawMap(event);
}

function clickMap(event) {
	VIEW.mouseDown = 1;
	drawMap(event);
}

function dblclickMap(event) {
	// Not yet supported.
}

/**
 * Set the terrain value of the hex at the given coordinates to the
 * current selected terrain type. If the brush size is > 1, then paints
 * multiple hexes around the central point.
 */
function paintTerrain(event, px, py) {
	var x = Math.floor(px / VIEW.currentScale.column);
	var scale = VIEW.currentScale.scale;
	if (x %2 == 1 && scale == 1) {
		py -= VIEW.currentScale.row / 2;
	} 
	var y = Math.floor(py / VIEW.currentScale.row);
	
	var ox = x;
	var oy = y;
	
	$.ajax({
		type: "PUT",
		url: "/hexweb/api/map/"+MAP.info.id+"/update?x="+(VIEW.x+x*scale)+"&y="+(VIEW.y+y*scale)+
			"&radius="+VIEW.brushSize+"&scale="+scale+"&terrain="+VIEW.terrainBrush,
		async: true
	});
	if (scale == 1) {
		for (var px = 0; px < parseInt(VIEW.brushSize / 2 + 1); px++) {
			var	 h = VIEW.brushSize - px;
			
			for (var py = 0; py < h; py ++) {
				y = oy + py - parseInt(h/2);
				if (px%2 == 1) {
					y += ox%2;
				}
				if (y < 0) {
					continue;
				}
				
				x = ox + px;
				if (isIn(x, y)) {
					VIEW.context.drawImage(MAP.images[VIEW.terrainBrush].image, 
							x * VIEW.currentScale.column + 8, 
							y * VIEW.currentScale.row + (x%2 * VIEW.currentScale.row / 2) + 8, 
							VIEW.currentScale.width, VIEW.currentScale.height);
					if (VIEW.showGrid) {
						drawHexGrid(x, y);
					}
				}
				x = ox - px;
				if (isIn(x, y)) {
					VIEW.context.drawImage(MAP.images[VIEW.terrainBrush].image, 
							x * VIEW.currentScale.column + 8, 
							y * VIEW.currentScale.row + (x%2 * VIEW.currentScale.row / 2) + 8, 
							VIEW.currentScale.width, VIEW.currentScale.height);
					if (VIEW.showGrid) {
						drawHexGrid(x, y);
					}
				}
			}
		}
	} else {
		VIEW.context.fillStyle = MAP.images[VIEW.terrainBrush].colour;
		var r = Math.floor(VIEW.brushSize / 2);
		for (var px = ox-r; px <= ox + r; px++) {
			for (var py = oy-r; py <= oy + r; py++) {
				//if (isIn(px, py)) {
					VIEW.context.fillRect(px * VIEW.currentScale.column + 8, py * VIEW.currentScale.row + 8,
							VIEW.currentScale.width, VIEW.currentScale.height);
				//}
			}
		}
	}
}

/**
 * Returns true if the location is writable. This excludes anywhere outside
 * the view rectangle, or those areas outside the icosohedron for world maps.
 */
function isIn(x, y) {
	if (x < 0 || x >= VIEW.width) {
		return false;
	}
	if (y < 0 || y >= VIEW.height) {
		return false;
	}
	if (MAP.world && MAP.bounds != null) {
		if (y + VIEW.y < MAP.bounds[x].min || y + VIEW.y > MAP.bounds[x].max) {
			return false;
		}
	}
	return true;
}

function recordSubPosition(event, px, py) {
	var x = Math.floor(px / VIEW.currentScale.column);
	var sx = Math.floor(((px - x * VIEW.currentScale.column) * 100.0) / VIEW.currentScale.column);
	if (x %2 == 1) {
		py -= VIEW.currentScale.row / 2;
	} 
	var y = Math.floor(py / VIEW.currentScale.row);
	var sy = Math.floor(((py - y * VIEW.currentScale.row) * 100.0) / VIEW.currentScale.row);

	VIEW.recordX = x * 100 + sx;
	VIEW.recordY = y * 100 + sy;
}

/**
 * 
 * @param x		Coordinates in 1/100ths of a tile.
 * @param y		Coordinates in 1/100ths of a tile.
 */
function findNearestPlace(x, y) {
	var nearestPlace = null;
	var minDistance = 10000;
	
	x += VIEW.x * 100;
	y += VIEW.y * 100;

	for (var i=0; i < MAP.places.length; i++) {
		var p = MAP.places[i];
		var px = p.x * 100 + p.sx;
		var py = p.y * 100 + p.sy;

		if (p.importance < VIEW.zoom) {
			continue;
		}

		// Distance is actually square of distance.
		var d = (x - px) * (x - px) + (y - py) * (y - py);
		if (d < minDistance) {
			nearestPlace = p;
			minDistance = d;
		}
	}
	return nearestPlace;
}

function paintPath(event, px, py) {
	var x = Math.floor(px / VIEW.currentScale.column);
	var sx = Math.floor(((px - x * VIEW.currentScale.column) * 100.0) / VIEW.currentScale.column);
	if (x %2 == 1) {
		py -= VIEW.currentScale.row / 2;
	} 
	var y = Math.floor(py / VIEW.currentScale.row);
	var sy = Math.floor(((py - y * VIEW.currentScale.row) * 100.0) / VIEW.currentScale.row);

	console.log(""+x+","+y);
	
	if (VIEW.editMode == EDIT_MODE.NEW) {
		// Create a new path.
		var path = new Object();
		path.name = "untitled";
		path.type = VIEW.brushStyle;
		path.thickness = VIEW.brushSize;
		path.vertex = new Array();
		var v = new Object();
		v.vertex = 0;
		v.x = x;
		v.y = y;
		v.subX = sx;
		v.subY = sy;
		path.vertex.push(v);
		// Finally, change to append mode.
		VIEW.editMode = EDIT_MODE.ADD;
		VIEW.currentPath = path;
	} else if (VIEW.editMode == EDIT_MODE.ADD) {
		var path = VIEW.currentPath;
		var v = new Object();
		v.vertex = path.vertex.length;
		v.x = x;
		v.y = y;
		v.subX = sx;
		v.subY = sy;
		path.vertex.push(v);
		drawPath(path);
	}
}

function getVertexX(vertex) {
	return vertex.x * VIEW.currentScale.column + (vertex.subX * VIEW.currentScale.column)/100 + 8;
}

function getVertexY(vertex) {
	var y = vertex.y * VIEW.currentScale.row + (vertex.subY * VIEW.currentScale.row)/100 + 8;
	if (vertex.x%2 == 1) {
		y += VIEW.currentScale.row / 2;
	}
	return y;
}

function drawPath(path) {
	VIEW.context.strokeStyle = "#a4f8ff";
	VIEW.context.lineWidth = 5;
	VIEW.context.beginPath();
	var v = path.vertex[0];
	VIEW.context.moveTo(getVertexX(v), getVertexY(v));
	for (var i = 1; i < path.vertex.length; i++) {
		var v = path.vertex[i];
		VIEW.context.lineTo(getVertexX(v), getVertexY(v));
	}
	VIEW.context.stroke();
	
}

var mouseHasBeenUp = false;

/**
 * Called when the user draws on the map.
 */
function drawMap(event) {
	if (VIEW.mouseDown == 0) {
		mouseHasBeenUp = true;
		if (VIEW.brushMode == BRUSH_MODE.TERRAIN || VIEW.brushMode == BRUSH_MODE.PATH) {
			return;
		}
	}
	var canoffset = $("#map").offset();
	var px = event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft - Math.floor(canoffset.left) - 8;
	var py = event.clientY + document.body.scrollTop + document.documentElement.scrollTop - Math.floor(canoffset.top) + 1 - 8;
	
	if (VIEW.brushMode == BRUSH_MODE.PATH && VIEW.mouseDown == 1 && mouseHasBeenUp) {
		paintPath(event, px, py);
		mouseHasBeenUp = false;
	} else if (VIEW.brushMode == BRUSH_MODE.TERRAIN) {
		// Paint a terrain hex whilst the mouse is held down.
		paintTerrain(event, px, py)
	} else if (VIEW.brushMode == BRUSH_MODE.THING && VIEW.mouseDown == 1) {
		// This is a click. We don't draw on a click, but record the position.
		if (VIEW.recordX == -1 && VIEW.recordY == -1) {
			recordSubPosition(event, px, py);
		}
	} else if (VIEW.brushMode == BRUSH_MODE.THING && VIEW.mouseDown == 0) {
		// This is possibly a mouse up event.
		if (VIEW.recordX == -1 && VIEW.recordY == -1) {
			// No previous mouse down.
			return;
		}
		var oldRecordX = VIEW.recordX;
		var oldRecordY = VIEW.recordY;
		
		var x = Math.floor(VIEW.recordX / 100);
		var y = Math.floor(VIEW.recordY / 100);
		var sx = VIEW.recordX % 100;
		var sy = VIEW.recordY % 100;
		if (y < 0 || x < 0 || y >= MAP.info.height || x >= MAP.info.width) {
			VIEW.recordX = -1;
			VIEW.recordY = -1;
			return;
		}
		recordSubPosition(event, px, py);

		var place = findNearestPlace(oldRecordX, oldRecordY);
		if (place != null && Math.abs(oldRecordX - VIEW.recordX) > 15 && Math.abs(oldRecordY - VIEW.recordY) > 15) {
			// Click and drag event.
			place.x = Math.floor(VIEW.recordX / 100);
			place.y = Math.floor(VIEW.recordY / 100);
			place.sx = VIEW.recordX % 100;
			place.sy = VIEW.recordY % 100;
			$.ajax({
				type: "PUT",
				url: "/hexweb/api/map/"+MAP.info.id+"/place/"+place.id+"?x="+(VIEW.x+place.x)+"&y="+(VIEW.y+place.y)+"&sx="+place.sx+"&sy="+place.sy
			});
			refreshMap();
		} else if (place != null) {
			// Simple click next to an existing place.
			openEditPlaceDialog(place);
		} else if (place == null && Math.abs(oldRecordX - VIEW.recordX) < 50 && Math.abs(oldRecordY - VIEW.recordY) < 50) {
			// Paint a new object if the mouse hasn't moved that far.
			$.ajax({
				type: "POST",
				url: "/hexweb/api/map/"+MAP.info.id+"/place?x="+(VIEW.x+x)+"&y="+(VIEW.y+y)+"&sx="+sx+"&sy="+sy+"&thingId="+VIEW.thingBrush,
				async: false
			});
			refreshMap();
		}

		VIEW.recordX = -1;
		VIEW.recordY = -1;
	}
}

function openEditPlaceDialog(place) {
	$("#placeDialog").remove();

	$("body").append("<div id='placeDialog' class='floating'></div>");
	$("#placeDialog").append("<h4>Edit place</h4>");
	$("#placeDialog").append("<p>Name: <input id='placeName' type='text' width='24' value='"+place.name+"'/></p>");
	$("#placeDialog").append("<p>Title: <input id='placeTitle' type='text' width='40' value='"+place.title+"'/></p>");
	$("#placeDialog").append("<input id='placeId' type='hidden' value='"+place.id+"'/>");
	$("#placeDialog").append("<p><button onclick='deletePlace()'>Delete</button> <button onclick='saveEditPlaceDialog()'>Save</button></p>");
}

function deletePlace() {
	var id = $("#placeId").val();
	$.ajax({
		type: "DELETE",
		url: "/hexweb/api/map/"+MAP.info.id+"/place/"+id
	});

	$("#placeDialog").remove();
	refreshMap();
}

function saveEditPlaceDialog() {
	var id = $("#placeId").val();
	var name = $("#placeName").val();
	var title = $("#placeTitle").val();

	$.ajax({
		type: "PUT",
		url: "/hexweb/api/map/"+MAP.info.id+"/place/"+id + "?name="+name+"&title="+title+"&x=-1",
		data: {
			"name": name,
			"title": title,
			"x": -1,
			"y": -1
		}
	});
	refreshMap();

	$("#placeDialog").remove();
}
