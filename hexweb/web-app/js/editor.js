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
VIEW.selectedPathId = 0;
VIEW.selectedVertexId = 0;

function debug(msg) {
	console.log(msg);
}

function openMap() {
	var x = $("#mapMenu").position().left + 96;
	var y = $("#mapMenu").position().top;
	var	width = 640;
	var height = parseInt(MAP.info.height / (MAP.info.width * 1.0 / 640.0))
	debug(width + "x" + height);
	if (document.getElementById("mapPopout") != null) {
		// Toggle on/off.
		closeAllDialogs();
		return;
	}
	closeAllDialogs();

	$("body").append("<div id='mapPopout' class='floating'></div>");
	$("#mapPopout").css("position", "absolute");
	$("#mapPopout").css("left", x);
	$("#mapPopout").css("top", y);
	$("#mapPopout").css("width", (width)+"px");
	$("#mapPopout").css("height", (height)+"px");
	$("#mapPopout").css("border", "1px solid #999999");
	$("#mapPopout").css("opacity", "1");
	
	$("#mapPopout").html("<img id='mapThumb' src='/hexweb/api/map/"+MAP.info.name+
			               "/thumb?w=320&forceWidth=true' width='"+width+
			               "' height='"+height+"'/>");
	$("#mapThumb").css("opacity", "1");
	document.getElementById("mapThumb").addEventListener("mousedown", clickThumb, false);
	debug($("#mapPopout").width() + "x" + $("#mapPopout").height());
	debug($("#mapThumb").width() + "x" + $("#mapThumb").height());
}

function clickThumb(event) {
	var offset = $("#mapPopout").offset();
	var px = event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft - Math.floor(offset.left);
	var py = event.clientY + document.body.scrollTop + document.documentElement.scrollTop - Math.floor(offset.top) + 1;

	var x = parseInt(px * (MAP.info.width / (1.0 * $("#mapPopout").width())));
	var y = parseInt(py * (MAP.info.height / (1.0 * $("#mapPopout").height())));
	debug(px + "," + py + " -> " + x + ", " + y + " " + $("#mapPopout").height());

	closeAllDialogs();
	moveMapTo(x, y);
}

function selectTerrain(id) {
	VIEW.brushMode = BRUSH_MODE.TERRAIN;
	VIEW.editMode = EDIT_MODE.PAINT;
	
	if (id == 0) {
		id = 3;
	}
	$("#pathStyle"+VIEW.brushStyle).removeClass("selectedButton");
	closeAllDialogs();
	VIEW.terrainBrush = id;
	
	var url = VIEW.imageBase + "/terrain/" + MAP.images[VIEW.terrainBrush].name + ".png";
	$("#terrainMenu").attr("src", url);
}


function openTerrainMenu() {
	var x = $("#terrainMenu").position().left + 96;
	var y = $("#terrainMenu").position().top;
	
	if (document.getElementById("terrainPopout") != null) {
		// Toggle on/off.
		closeAllDialogs();
		return;
	}
	closeAllDialogs();

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
	closeAllDialogs();
	VIEW.thingBrush = id;
	
	var url = VIEW.imageBase + "/things/" + MAP.things[VIEW.thingBrush].name + ".png";
	$("#thingMenu").attr("src", url);
}

function openThingMenu() {
	var x = $("#thingMenu").position().left + 96;
	var y = $("#thingMenu").position().top;
	
	if (document.getElementById("thingPopout") != null) {
		// Toggle on/off.
		closeAllDialogs();
		return;
	}
	closeAllDialogs();

	$("body").append("<div id='thingPopout' class='floating'></div>");
	$("#thingPopout").css("position", "absolute");
	$("#thingPopout").css("left", x);
	$("#thingPopout").css("top", y);
	$("#thingPopout").css("width", "75%");
	$("#thingPopout").css("height", "auto");
	$("#thingPopout").css("border", "1px solid #999999");
	
	for (var id in MAP.things) {
		var  t = MAP.things[id];
		var  path = VIEW.imageBase + "things/" + t.name + ".png";
		
		$("#thingPopout").append("<div class='tilebox' id='th_"+id+"' onclick='selectThing("+id+")'></div>");
		
		$("#th_"+id).append("<img src='"+path+"' height='64px'/>");
		$("#th_"+id).append(t.title);
	}
}

function selectArea(id) {
	VIEW.brushMode = BRUSH_MODE.AREA;
	VIEW.editMode = EDIT_MODE.PAINT;

	$("#pathStyle"+VIEW.brushStyle).removeClass("selectedButton");
	closeAllDialogs();
	VIEW.areaBrush = id;
}

function openAreaMenu() {
	var x = $("#areaMenu").position().left + 96;
	var y = $("#areaMenu").position().top;
	
	if (document.getElementById("areaPopout") != null) {
		// Toggle on/off.
		closeAllDialogs();
		return;
	}
	closeAllDialogs();

	$("body").append("<div id='areaPopout' class='floating'></div>");
	$("#areaPopout").css("position", "absolute");
	$("#areaPopout").css("left", x);
	$("#areaPopout").css("top", y);
	$("#areaPopout").css("width", "75%");
	$("#areaPopout").css("height", "auto");
	$("#areaPopout").css("border", "1px solid #999999");
	
	$("#areaPopout").append("<div class='tilebox' id='ar_0' onclick='selectArea(0)'>Clear</div>");
	for (var id in MAP.areas) {
		var  a = MAP.areas[id];
		var  id = a.id
		
		$("#areaPopout").append("<div class='tilebox' id='ar_"+id+"' onclick='selectArea("+id+")'></div>");		
		$("#ar_"+id).append(a.title);
	}
}

function setPathStyle(style) {
	VIEW.brushMode = BRUSH_MODE.PATH
	VIEW.editMode = EDIT_MODE.NEW;

	closeAllDialogs();

	$("#pathStyle"+VIEW.brushStyle).removeClass("selectedButton");
	VIEW.brushStyle = style;
	$("#pathStyle"+VIEW.brushStyle).addClass("selectedButton");
	
	$("#brushBtn1").removeClass("selectedButton");
	$("#brushBtn3").removeClass("selectedButton");
	$("#brushBtn5").removeClass("selectedButton");
	
	$("#pathBtnSel").addClass("selectedButton");
	VIEW.editMode = EDIT_MODE.SELECT;
	
	refreshMap();
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
	paintUpdate(event, x, y, ox, oy, scale);
}

function paintArea(event, px, py) {
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
			"&radius="+VIEW.brushSize+"&scale="+scale+"&area="+VIEW.areaBrush,
		async: true
	});
	paintUpdate(event, x, y, ox, oy, scale);	
}

function paintUpdate(event, x, y, ox, oy, scale) {
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
				if (VIEW.brushMode == BRUSH_MODE.TERRAIN) {
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
				} else if (VIEW.brushMode == BRUSH_MODE.AREA) {
					x = ox + px;
					if (isIn(x, y)) {
						MAP.area[y][x] = VIEW.areaBrush;
						redrawHex(x, y);
						if (VIEW.showGrid) {
							drawHexGrid(x, y);
						}
					}
					x = ox - px;
					if (isIn(x, y)) {
						MAP.area[y][x] = VIEW.areaBrush;
						for (var xx=-1; xx < 2; xx++) {
							for (var yy=-1; yy < 2; yy++) {
								redrawHex(x+xx, y+yy);								
								if (VIEW.showGrid) {
									drawHexGrid(x, y);
								}
							}
						}
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

function showPathDialog() {
	closeAllDialogs();
	$("body").append("<div id='pathDialog' class='floating'></div>");
	$("#pathDialog").css("position", "absolute");
	$("#pathDialog").css("left", 128);
	$("#pathDialog").css("top", 32);
	$("#pathDialog").css("width", 250);
	$("#pathDialog").css("height", 120);
	$("#pathDialog").css("border", "1px solid #999999");
	
	var type = VIEW.currentPath.style;
	switch (type) {
	case BRUSH_STYLE.ROAD:
		type = "Road";
		break;
	case BRUSH_STYLE.RIVER:
		type = "River";
		break;
	case BRUSH_STYLE.COAST:
		type = "Coastline";
		break;
	default:
		type = "f"+VIEW.currentPath.type;
		break;
	}
	
	$("#pathDialog").append("<h4>" + type + ": <span id='pathNameLabel' onclick='changePathName()'>" + VIEW.currentPath.name + "</span></h4>");
	
	$("#pathDialog").append("<div id='pathLength'>1</div>");
	
	$("#pathDialog").append("<img src=\"/hexweb/images/icons/path_save.png\" onclick=\"saveCurrentPath()\"/>");
	$("#pathDialog").append("&nbsp;");
	$("#pathDialog").append("<img src=\"/hexweb/images/icons/path_del.png\" onclick=\"deleteCurrentPath()\"/>");
	$("#pathDialog").append("&nbsp;");
	$("#pathDialog").append("<img src=\"/hexweb/images/icons/node_add.png\" onclick=\"addNodeToPath()\"/>");
	$("#pathDialog").append("&nbsp;");
	$("#pathDialog").append("<img src=\"/hexweb/images/icons/node_del.png\" onclick=\"removeNodeFromPath()\"/>");
}

function changePathName() {
	var path = VIEW.currentPath;
	$("#pathNameLabel").removeAttr("onclick");
	$("#pathNameLabel").html("<input id='pathNameField' onblur='updatePathName()' value='"+path.name+"'/>");
}

function updatePathName() {
	var path = VIEW.currentPath;
	var name = $("#pathNameField").val();
	$("#pathNameLabel").html(name);
	path.name = name;
	saveCurrentPath();
	closeAllDialogs();
}

function findClosestPath(event, px, py) {
	var		pathId = 0;
	var		vertexId = -1;
	var		closest = 50;
	VIEW.currentPath = null;
	
	var		x = getMapX(px, py);
	var		y = getMapY(px, py);
	for (var i=0; i < MAP.paths.length; i++) {
		var p = MAP.paths[i];
		for (j=0; j < p.vertex.length; j++) {
			var v = p.vertex[j];
			var dx = (x - (v.x * 100 + v.subX));
			var dy = (y - (v.y * 100 + v.subY));
			var d = Math.sqrt(dx * dx + dy * dy);
			if (d < closest) {
				VIEW.currentPath = p;
				pathId = p.id;
				vertexId = v.vertex;
				VIEW.selectedVertexIndex = j;
				closest = d;
			}
		}
	}
	VIEW.selectedPathId = pathId;
	VIEW.selectedVertexId = vertexId;
}

/**
 * Select the path closest to where the user clicked.
 */
function selectPath(event, px, py) {
	findClosestPath(event, px, py);
	refreshMap();
	if (VIEW.selectedPathId != 0) {
		showPathDialog();
	} else {
		closeAllDialogs();
	}
}

function movePath(event, px, py) {
	findClosestPath(event, px, py);
	if (VIEW.currentPath != null && VIEW.selectedVertexId >= 0) {
		var		x = getMapX(px, py);
		var		y = getMapY(px, py);
		VIEW.currentPath.vertex[VIEW.selectedVertexIndex].x = parseInt(x / 100);
		VIEW.currentPath.vertex[VIEW.selectedVertexIndex].y = parseInt(y / 100);
		VIEW.currentPath.vertex[VIEW.selectedVertexIndex].subX = x % 100;
		VIEW.currentPath.vertex[VIEW.selectedVertexIndex].subY = y % 100;
		debug("Moved path to "+x+","+y);
		redrawMap();
	}
}

function removeNodeFromPath() {
	if (VIEW.currentPath == null || VIEW.selectedVertexId < 0) {
		return;
	}
	var path = VIEW.currentPath;
	debug("Removing path " + path.id);
	for (var i = VIEW.selectedVertexId; i < path.vertex.length-1; i++) {
		path.vertex[i] = path.vertex[i+1];
	}
	path.vertex.length --;
	debug("Path length is now " + path.vertex.length);
	redrawMap();
}

function getMapX(px, py) {
	var x = Math.floor(px / VIEW.currentScale.column);
	var sx = Math.floor(((px - x * VIEW.currentScale.column) * 100.0) / VIEW.currentScale.column);

	x += VIEW.x;
	
	return x * 100 + sx;
}

function getMapY(px, py) {
	var x = Math.floor(px / VIEW.currentScale.column);
	var sx = Math.floor(((px - x * VIEW.currentScale.column) * 100.0) / VIEW.currentScale.column);
	if (x %2 == 1) {
		py -= VIEW.currentScale.row / 2;
	} 
	var y = Math.floor(py / VIEW.currentScale.row);
	var sy = Math.floor(((py - y * VIEW.currentScale.row) * 100.0) / VIEW.currentScale.row);

	y += VIEW.y;
	
	return y * 100 + sy;
}

function paintPath(event, px, py) {
	var x = Math.floor(px / VIEW.currentScale.column);
	var sx = Math.floor(((px - x * VIEW.currentScale.column) * 100.0) / VIEW.currentScale.column);
	if (x %2 == 1) {
		py -= VIEW.currentScale.row / 2;
	} 
	var y = Math.floor(py / VIEW.currentScale.row);
	var sy = Math.floor(((py - y * VIEW.currentScale.row) * 100.0) / VIEW.currentScale.row);

	x += VIEW.x;
	y += VIEW.y;
	
	console.log(""+x+","+y);
	
	if (VIEW.editMode == EDIT_MODE.NEW) {
		// Create a new path.
		var path = new Object();
		path.id = 0;
		path.name = "untitled";
		path.style = VIEW.brushStyle;
		path.thickness1 = VIEW.brushSize;
		path.thickness2 = VIEW.brushSize;
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
		showPathDialog()
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
		$("#pathLength").html(path.vertex.length);
	}
}

function saveCurrentPath() {
	var path = VIEW.currentPath;

	$.ajax({
		contentType: 'application/json',
		dataType: 'json',
		type: "POST",
		url: "/hexweb/api/map/"+MAP.info.id+"/path",
		data: JSON.stringify(path),
		processData: false,
		success: function (data) {
			VIEW.currentPath = data;
			refreshMap();
			data.style = data.style.name
			console.log(data.name + ": " + data.style);
			drawPath(VIEW.currentPath);
		}
	});
}

function deleteCurrentPath() {
	var  path = VIEW.currentPath;
	$.ajax({
		type: "DELETE",
		url: "/hexweb/api/map/"+MAP.info.id+"/path/"+path.id,
		success: function (data) {
			VIEW.currentPath = null;
			refreshMap();
			debug("Deleted path");
		}
	});
}

var mouseHasBeenUp = false;

function updateInfoBar(px, py) {
	var x = Math.floor(px / VIEW.currentScale.column);
	var scale = VIEW.currentScale.scale;
	if (x %2 == 1 && scale == 1) {
		py -= VIEW.currentScale.row / 2;
	} 
	var y = Math.floor(py / VIEW.currentScale.row);
	
	if (MAP.map[y] == null || MAP.map[y][x] == null) {
		$("#infobar").html("");
		return;
	}
	
	var areaId = MAP.area[y][x];
	x += VIEW.x;
	y += VIEW.y;
	
	var title = ""
	if (areaId > 0 && MAP.areas[areaId] != null) {
		title = MAP.areas[areaId].title;
	} else if (areaId > 0) {
		title = "(" + areaId + ")";
	}
	
	
	$("#infobar").html("<b>X:</b> " + x + ", <b>Y:</b> " + y + "  " + title);
	
}

/**
 * Called when the user draws or clicks on the map.
 */
function drawMap(event) {
	// Update the info bar
	var canoffset = $("#map").offset();
	var px = event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft - Math.floor(canoffset.left) - 8;
	var py = event.clientY + document.body.scrollTop + document.documentElement.scrollTop - Math.floor(canoffset.top) + 1 - 8;

	updateInfoBar(px, py);
	
	if (VIEW.mouseDown == 0) {
		// Flag to prevent multiple events from a single click. Wait for the
		// mouse to go 'up' after a click.
		mouseHasBeenUp = true;
		if (VIEW.brushMode == BRUSH_MODE.TERRAIN || VIEW.brushMode == BRUSH_MODE.PATH || VIEW.brushMode == BRUSH_MODE.AREA) {
			return;
		}
	}
	
	if (VIEW.brushMode == BRUSH_MODE.PATH && VIEW.mouseDown == 1 && mouseHasBeenUp) {
		if (VIEW.editMode == EDIT_MODE.SELECT) {
			selectPath(event, px, py);
		} else if (VIEW.editMode == EDIT_MODE.NEW || VIEW.editMode == EDIT_MODE.ADD) {
			paintPath(event, px, py);
		}
		mouseHasBeenUp = false;
	} else if (VIEW.brushMode == BRUSH_MODE.PATH && VIEW.mouseDown == 1) {
		if (VIEW.editMode == EDIT_MODE.SELECT) {
			movePath(event, px, py);
		}
	} else if (VIEW.brushMode == BRUSH_MODE.TERRAIN) {
		// Paint a terrain hex whilst the mouse is held down.
		paintTerrain(event, px, py);
	} else if (VIEW.brushMode == BRUSH_MODE.AREA) {
		paintArea(event, px, py);
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
