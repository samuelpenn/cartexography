/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */


/* Enum definitions */

var BRUSH_MODE = new Object();
BRUSH_MODE.TERRAIN = "TERRAIN";
BRUSH_MODE.THING = "THING";
BRUSH_MODE.PATH = "PATH";
BRUSH_MODE.AREA = "AREA";
BRUSH_MODE.LABEL = "LABEL";

var BRUSH_STYLE = new Object();
BRUSH_STYLE.ROAD = "ROAD";
BRUSH_STYLE.RIVER = "RIVER";
BRUSH_STYLE.COAST = "COAST";

var BRUSH_SIZE = new Object();
BRUSH_SIZE.SMALL = 1;
BRUSH_SIZE.MEDIUM = 3;
BRUSH_SIZE.LARGE = 5;

var EDIT_MODE = new Object();
EDIT_MODE.PAINT = "PAINT";    // Set target
EDIT_MODE.NEW = "NEW";        // Create new item
EDIT_MODE.ADD = "ADD";        // Append to existing item
EDIT_MODE.SELECT = "SELECT";  // Select existing items
EDIT_MODE.EDIT = "EDIT";	  // Edit existing items
EDIT_MODE.DELETE = "DELETE";  // Delete existing items


/* Global variables */
var MAP = { id: 0 };					// This will be populated directly from JSON
var VIEW = { width: 32, height: 20, x: 0, y: 0, context: null } 	// View port configuration.

VIEW.brushMode = BRUSH_MODE.TERRAIN;
VIEW.brushSize = BRUSH_SIZE.SMALL;
VIEW.editMode = EDIT_MODE.PAINT;
VIEW.brushStyle = BRUSH_STYLE.RIVER;

VIEW.terrainBrush = 0;
VIEW.thingBrush = 0;
VIEW.areaBrush = 0;
VIEW.showGrid = false;

VIEW.zoom = 0;
VIEW.port= { width: 1600, height: 1200 };

VIEW.scale = [ { column: 48, row: 56, width: 65, height: 56, font: 14, step: 10, scale: 1 },
               { column: 24, row: 28, width: 33, height: 28, font: 12, step: 10, scale: 1 },
               { column: 12, row: 14, width: 17, height: 14, font: 9,  step: 20, scale: 1 }, 
               { column: 6, row: 7, width: 9, height: 7, font: 6, step: 40, scale: 1  }, 
               { column: 8, row: 8, width: 8, height: 8, font: 0, step: 400, scale: 10 },
               { column: 4, row: 4, width: 4, height: 4, font: 0, step: 1600, scale: 20 }
             ];
VIEW.currentScale = VIEW.scale[0];

function setZoom(zoom) {
	closeAllDialogs();
	$("#zoomBtn"+VIEW.zoom).removeClass("selectedButton");
	VIEW.zoom = zoom;
	$("#zoomBtn"+VIEW.zoom).addClass("selectedButton");
	VIEW.currentScale = VIEW.scale[VIEW.zoom];
	VIEW.port.lastWidth = -1;
	refreshMap();
}

function setBrush(size) {
	closeAllDialogs();
	$("#brushBtn"+VIEW.brushSize).removeClass("selectedButton");
	VIEW.brushSize = size;
	$("#brushBtn"+VIEW.brushSize).addClass("selectedButton");
}

function setPathEdit(mode) {
	$("#pathBtnSel").removeClass("selectedButton");
	$("#pathBtnNew").removeClass("selectedButton");
	VIEW.editMode = mode;
	switch (mode) {
	case EDIT_MODE.SELECT:
		VIEW.editMode = EDIT_MODE.SELECT;
		$("#pathBtnSel").addClass("selectedButton");
		break;
	case EDIT_MODE.NEW:
		VIEW.editMode = EDIT_MODE.NEW;
		$("#pathBtnNew").addClass("selectedButton");
		break;
	}
}

function toggleGrid() {
	closeAllDialogs();
	if (VIEW.showGrid) {
		// Switch off
		VIEW.showGrid = false;
		$("#showGrid").removeClass("selectedButton");
		setViewPort(true);
	} else {
		// Switch on
		VIEW.showGrid = true;
		$("#showGrid").addClass("selectedButton");
		setViewPort(false);
	}
	refreshMap();
}

function setViewPort(forceClear) {
	VIEW.port.width = $("body").width() - VIEW.xMargins;
	VIEW.port.height = $("body").height() - VIEW.yMargins;
	
	var		tileWidth = VIEW.scale[VIEW.zoom].column;
	var		tileHeight = VIEW.scale[VIEW.zoom].height;
	VIEW.width = parseInt(VIEW.port.width / tileWidth) - 1;
	VIEW.height = parseInt(VIEW.port.height / tileHeight) - 1;
	VIEW.width *= VIEW.currentScale.scale;
	VIEW.height *= VIEW.currentScale.scale;
	if (VIEW.width % 2 == 1) {
		VIEW.width = VIEW.width + 1;
	}
	if (VIEW.width > MAP.width) {
		VIEW.width = MAP.width;
	}
	if (VIEW.height > MAP.height) {
		VIEW.height = MAP.height;
	}
	
	if (forceClear) {
		var canvas = document.getElementById("map");
		VIEW.context.save();
		VIEW.context.setTransform(1, 0, 0, 1, 0, 0);
		VIEW.context.clearRect(0, 0, canvas.width, canvas.height);
		VIEW.context.restore();
	}
}

function drawHexGrid(x, y) {
	var tileWidth = VIEW.tileWidth;
	var tileHeight = VIEW.tileHeight;
	var halfOffset = VIEW.halfOffset
	
	if (MAP.map[y][x] == MAP.info.oob) {
		return;
	}

	var px = x * tileWidth + 8;
	var py = y * tileHeight + (x%2 * halfOffset) + 8;
	
	VIEW.context.strokeStyle = '#444444';
	VIEW.context.lineWidth = 1;			
	
	VIEW.context.beginPath();
	VIEW.context.moveTo(px + tileWidth/3, py);
	VIEW.context.lineTo(px + tileWidth, py);
	VIEW.context.lineTo(px + tileWidth + tileWidth/3, py + tileHeight/2);
	VIEW.context.lineTo(px + tileWidth, py + tileHeight);
	VIEW.context.lineTo(px + tileWidth/3, py + tileHeight);
	VIEW.context.lineTo(px, py + tileHeight/2);
	VIEW.context.lineTo(px + tileWidth/3, py);
	VIEW.context.stroke();
}

/**
 * Download the map data for the current view and display it in the
 * canvas.
 */
function refreshMap() {
	setViewPort();
	if (VIEW.x < 0) {
		VIEW.x = 0;
	}
	if (VIEW.y < 0) {
		VIEW.y = 0;
	}
	VIEW.x -= VIEW.x%10;
	VIEW.y -= VIEW.y%10;

	if (VIEW.currentScale.scale == 1) {
		drawSmallScale();
	} else {
		drawLargeScale();
	}
}

/**
 * Draw a map where we only display every nth tile, and they are
 * displayed as flat colour squares.
 */
function drawLargeScale() {
	var		startX = VIEW.x;
	var 	startY = VIEW.y;
	var		mapWidth = VIEW.width;
	var		mapHeight = VIEW.height;

	if (VIEW.port.width != VIEW.port.lastWidth || VIEW.port.height != VIEW.port.lastHeight) {
		$("#map").attr("width", VIEW.port.width);
		$("#map").attr("height", VIEW.port.height);
		VIEW.port.lastWidth = VIEW.port.width;
		VIEW.port.lastHeight = VIEW.port.height;
	}
	var		tileWidth = VIEW.currentScale.column;
	var		tileHeight = VIEW.currentScale.height;

	mapWidth = VIEW.width;
	mapHeight = VIEW.height;
	
	VIEW.imageWidth = tileWidth;
	VIEW.imageHeight = tileHeight;
	VIEW.halfOffset = 0;
	VIEW.tileWidth = tileWidth;
	VIEW.tileHeight = tileHeight;
	$.getJSON(API_PATH+"/map/"+MAP.info.id+"/largemap?x="+startX+"&y="+startY+"&w="+
			  mapWidth+"&h="+mapHeight+"&scale="+VIEW.currentScale.scale, function(data) {
		MAP.map = data.map;
		MAP.bounds = data.bounds;

		startX = data.info.x;
		startY = data.info.Y;
		mapWidth = data.info.width / VIEW.currentScale.scale;
		mapHeight = data.info.height / VIEW.currentScale.scale;

		for (var y=0; y < mapHeight; y++) {
			for (var x=0; x < mapWidth; x++) {
				var t = MAP.map[y][x];
				var px = x * tileWidth + 8;
				var py = y * tileHeight + 8;
				
				VIEW.context.fillStyle = MAP.images[t].colour;
				VIEW.context.fillRect(px, py, VIEW.imageWidth, VIEW.imageHeight);
			}
		}

		$("#links").html("<a target='new' href='/cartexography/image/map/"+MAP.info.id+"?x="+VIEW.x+"&y="+VIEW.y+"&w="+VIEW.width+"&h="+VIEW.height+"&s="+tileHeight+"&areas=1'>Download</a>");
		
	});
}

/**
 * Paths are sometimes returned by JSON with their vertices
 * @param paths
 */
function sortPaths(paths) {
	if (paths != null) {
		for (var i=0; i < paths.length; i++) {
			var p = paths[i];
			p.style = p.style.name;
			p.vertex.sort(function(a,b) {
				return a.vertex - b.vertex;
			});
		}
	}
}

/**
 * Draw a map where every tile is displayed. Each tile is displayed
 * as a proper hex image.
 */
function drawSmallScale() {
	var		startX = VIEW.x;
	var 	startY = VIEW.y;
	var		mapWidth = VIEW.width;
	var		mapHeight = VIEW.height;

	if (VIEW.port.width != VIEW.port.lastWidth || VIEW.port.height != VIEW.port.lastHeight) {
		$("#map").attr("width", VIEW.port.width);
		$("#map").attr("height", VIEW.port.height);
		VIEW.port.lastWidth = VIEW.port.width;
		VIEW.port.lastHeight = VIEW.port.height;
	}
	var		tileWidth = VIEW.scale[VIEW.zoom].column;
	var		tileHeight = VIEW.scale[VIEW.zoom].height;

	mapWidth = parseInt(VIEW.port.width / tileWidth) - 1;
	mapHeight = parseInt(VIEW.port.height / tileHeight) - 1;
	mapWidth = VIEW.width;
	mapHeight = VIEW.height;

	var 	imageWidth = VIEW.scale[VIEW.zoom].width;
	var 	imageHeight = VIEW.scale[VIEW.zoom].height;
	var		halfOffset = parseInt(imageHeight / 2);
	
	VIEW.imageWidth = imageWidth;
	VIEW.imageHeight = imageHeight;
	VIEW.halfOffset = halfOffset;
	VIEW.tileWidth = tileWidth;
	VIEW.tileHeight = tileHeight;

	$.getJSON(API_PATH+"/map/"+MAP.info.id+"/map?x="+startX+"&y="+startY+"&w="+mapWidth+"&h="+mapHeight, function(data) {
		MAP.map = data.map;
		MAP.area = data.area;
		MAP.places = data.places;
		MAP.bounds = data.bounds;
		MAP.paths = data.paths;
		MAP.areas = data.areas;
		MAP.labels = data.labels;
		sortPaths(MAP.paths);
		
		if (MAP.paths != null) {
			console.log("Paths: " + MAP.paths.length);
		}
		if (MAP.areas != null) {
			var areas = new Array();
			for (var i=0; i < MAP.areas.length; i++) {
				var a = MAP.areas[i];
				areas[a.id] = a;
			}
			MAP.areas = areas;
		}
		
		if (MAP.places != null) {
			for (var i=0; i < MAP.places.length; i++) {
				var p = MAP.places[i];
				debug("Place [" + p.name + "] " + p.x +"," + p.y);
			}
		}

		startX = data.info.x;
		startY = data.info.Y;
		mapWidth = data.info.width;
		mapHeight = data.info.height;
		
		redrawMap();
	});
	

};

function redrawHex(x, y) {
	var		startX = VIEW.x;
	var 	startY = VIEW.y;
	var		mapWidth = VIEW.width;
	var		mapHeight = VIEW.height;
	var		tileWidth = VIEW.scale[VIEW.zoom].column;
	var		tileHeight = VIEW.scale[VIEW.zoom].height;

	mapWidth = parseInt(VIEW.port.width / tileWidth) - 1;
	mapHeight = parseInt(VIEW.port.height / tileHeight) - 1;
	mapWidth = VIEW.width;
	mapHeight = VIEW.height;

	var 	imageWidth = VIEW.scale[VIEW.zoom].width;
	var 	imageHeight = VIEW.scale[VIEW.zoom].height;
	var		halfOffset = parseInt(imageHeight / 2);

	var t = MAP.map[y][x];
	var px = x * tileWidth + 8;
	var py = y * tileHeight + (x%2 * halfOffset) + 8;
	
	VIEW.context.drawImage(MAP.images[t].image, 
			px, py, imageWidth, imageHeight);

	if (VIEW.showGrid) {
		drawHexGrid(x, y);
	}
	VIEW.context.strokeStyle = '#ff0000';
	VIEW.context.lineWidth = 3;
	var px = x * tileWidth + 8;
	var py = y * tileHeight + (x%2 * halfOffset) + 8;
	if (y > 0 && MAP.area[y][x] != MAP.area[y-1][x]) {
		VIEW.context.beginPath();
		VIEW.context.moveTo(px + tileWidth/3, py);
		VIEW.context.lineTo(px + tileWidth, py);
		VIEW.context.stroke();
	}
	if (x%2 == 1) {
		if (x > 0 && MAP.area[y][x] != MAP.area[y][x-1]) {
			VIEW.context.beginPath();
			VIEW.context.moveTo(px, py + tileHeight/2);
			VIEW.context.lineTo(px + tileWidth/3, py);
			VIEW.context.stroke();					
		}
		if (x > 0 && y < mapHeight - 1 && MAP.area[y][x] != MAP.area[y+1][x-1]) {
			VIEW.context.beginPath();
			VIEW.context.moveTo(px, py + tileHeight/2);
			VIEW.context.lineTo(px + tileWidth/3, py + tileHeight);
			VIEW.context.stroke();					
		}
	} else {
		if (x > 0 && y > 0 && MAP.area[y][x] != MAP.area[y-1][x-1]) {
			VIEW.context.beginPath();
			VIEW.context.moveTo(px, py + tileHeight/2);
			VIEW.context.lineTo(px + tileWidth/3, py);
			VIEW.context.stroke();					
		}
		if (x > 0 && MAP.area[y][x] != MAP.area[y][x-1]) {
			VIEW.context.beginPath();
			VIEW.context.moveTo(px, py + tileHeight/2);
			VIEW.context.lineTo(px + tileWidth/3, py + tileHeight);
			VIEW.context.stroke();					
		}					
	}
}

function redrawMap() {
	var		startX = VIEW.x;
	var 	startY = VIEW.y;
	var		mapWidth = VIEW.width;
	var		mapHeight = VIEW.height;
	var		tileWidth = VIEW.scale[VIEW.zoom].column;
	var		tileHeight = VIEW.scale[VIEW.zoom].height;

	mapWidth = parseInt(VIEW.port.width / tileWidth) - 1;
	mapHeight = parseInt(VIEW.port.height / tileHeight) - 1;
	mapWidth = VIEW.width;
	mapHeight = VIEW.height;

	var 	imageWidth = VIEW.scale[VIEW.zoom].width;
	var 	imageHeight = VIEW.scale[VIEW.zoom].height;
	var		halfOffset = parseInt(imageHeight / 2);

	for (var y=0; y < mapHeight; y++) {
		for (var x=0; x < mapWidth; x++) {
			var t = MAP.map[y][x];
			var px = x * tileWidth + 8;
			var py = y * tileHeight + (x%2 * halfOffset) + 8;
			
			if (MAP.images[t] == null) {
				debug("Cannot get terrain ["+t+"] for "+(VIEW.x + x)+","+(VIEW.y + y));
				continue;
			} else {
				VIEW.context.drawImage(MAP.images[t].image, 
						px, py, imageWidth, imageHeight);
			}
		}
	}
	if (VIEW.showGrid) {
		for (var y=0; y < mapHeight; y++) {
			for (var x=0; x < mapWidth; x++) {
				drawHexGrid(x, y);
			}
		}
	}
	VIEW.context.strokeStyle = '#ff0000';
	VIEW.context.lineWidth = 3;
	for (var y=0; y < mapHeight; y++) {
		for (var x=0; x < mapWidth; x++) {
			var px = x * tileWidth + 8;
			var py = y * tileHeight + (x%2 * halfOffset) + 8;
			if (y > 0 && MAP.area[y][x] != MAP.area[y-1][x]) {
				VIEW.context.beginPath();
				VIEW.context.moveTo(px + tileWidth/3, py);
				VIEW.context.lineTo(px + tileWidth, py);
				VIEW.context.stroke();
			}
			if (x%2 == 1) {
				if (x > 0 && MAP.area[y][x] != MAP.area[y][x-1]) {
					VIEW.context.beginPath();
					VIEW.context.moveTo(px, py + tileHeight/2);
					VIEW.context.lineTo(px + tileWidth/3, py);
					VIEW.context.stroke();					
				}
				if (x > 0 && y < mapHeight - 1 && MAP.area[y][x] != MAP.area[y+1][x-1]) {
					VIEW.context.beginPath();
					VIEW.context.moveTo(px, py + tileHeight/2);
					VIEW.context.lineTo(px + tileWidth/3, py + tileHeight);
					VIEW.context.stroke();					
				}
			} else {
				if (x > 0 && y > 0 && MAP.area[y][x] != MAP.area[y-1][x-1]) {
					VIEW.context.beginPath();
					VIEW.context.moveTo(px, py + tileHeight/2);
					VIEW.context.lineTo(px + tileWidth/3, py);
					VIEW.context.stroke();					
				}
				if (x > 0 && MAP.area[y][x] != MAP.area[y][x-1]) {
					VIEW.context.beginPath();
					VIEW.context.moveTo(px, py + tileHeight/2);
					VIEW.context.lineTo(px + tileWidth/3, py + tileHeight);
					VIEW.context.stroke();					
				}					
			}
		}
	}
	$("#links").html("<a target='new' href='/cartexography/image/map/"+MAP.info.id+"?x="+VIEW.x+"&y="+VIEW.y+"&w="+VIEW.width+"&h="+VIEW.height+"&s="+tileHeight+"&areas=1'>Download</a>");
	
	// Draw all the paths visible on this map view.
	for (var i=0; i < MAP.paths.length; i++) {
		drawPath(MAP.paths[i]);
	}
	// Draw all the places visible on this map view.
	for (var i=0; i < MAP.places.length; i++) {
		drawPlace(MAP.places[i]);
	}
	for (var i=0; i < MAP.labels.length; i++) {
		drawLabel(MAP.labels[i]);
	}
}

/**
 * Draw the specified place on the map.
 */
function drawPlace(p) {
	if (p.importance < VIEW.zoom) {
		return;
	}
	
	var tileWidth = VIEW.currentScale.column;
	var tileHeight = VIEW.currentScale.row;
	
	var x = (p.x - VIEW.x) * tileWidth - tileWidth/2 + (p.sx * VIEW.imageWidth)/100 + 8;
	var y = (p.y - VIEW.y) * tileHeight + (p.x %2 * VIEW.halfOffset) - tileHeight/2 + (p.sy * VIEW.imageHeight)/100 + 8;

	if (MAP.things[p.thing_id] == null) {
		debug("Unable to find thing "+p.thing_id + " for place " + p.title);
	} else {
		VIEW.context.drawImage(MAP.things[p.thing_id].image, x, y, 
				VIEW.imageWidth, VIEW.imageHeight);
		VIEW.context.font = VIEW.currentScale.font + "px Arial";
		var w = VIEW.context.measureText(p.title).width;
		VIEW.context.fillText(p.title, x + VIEW.imageWidth/2 - w / 2, y + VIEW.imageHeight);
	}
}

function getFontSize(p) {
	var size = 2;
	switch (p.fontSize) {
	case 0: 
		// xx-small
		size = 1;
		break;
	case 1:
		// x-small
		size = 2;
		break;
	case 2:
		// small
		size = 4;
		break;
	case 3:
		// medium
		size = 8;
		break;
	case 4:
		// large
		size = 16;
		break;
	case 5:
		// x-large
		size = 32;
		break;
	case 6:
		// xx-large
		size = 48;
		break;
	}
	return parseInt(size * VIEW.currentScale.column / 3);
}

function getFontAlpha(p) {
	var alpha = 1.00;
	var size = getFontSize(p);
	
	debug (p.title + " " + p.fontSize + " " + size + " " + VIEW.currentScale.column);
	size = parseInt(size / 10);
	switch (size) {
	case 0:
		debug(size);
		alpha = 0;
		break;
	case 1: case 2:
		debug(size);
		alpha = 1.00;
		break;
	default:
		debug("default: " + size);
		alpha = 1.00 - (size -2) * 0.25;
	}

	return alpha;
}

function drawLabel(p) {
	var size = getFontSize(p);
	var alpha = getFontAlpha(p);
	debug ("Alpha: " + alpha);
	if (alpha <= 0) {
		return;
	}

	var x = (p.x - VIEW.x) * 48 - 24 + (p.sx * 65)/100;
	var y = (p.y - VIEW.y) * 56 + (p.x %2 * 28) - 20 + (p.sy * 56)/100;
	
	var tileWidth = VIEW.currentScale.column;
	var tileHeight = VIEW.currentScale.row;
	
	var x = (p.x - VIEW.x) * tileWidth + (p.sx * VIEW.imageWidth)/100 + 8;
	var y = (p.y - VIEW.y) * tileHeight + (p.x %2 * VIEW.halfOffset) + (p.sy * VIEW.imageHeight)/100 + 8;

	VIEW.context.save();
	VIEW.context.strokeStyle = "#000000";
	VIEW.context.fillStyle = "rgba(0, 0, 0, " + alpha + ")";
	switch (p.style) {
	case "FOREST":
		debug("Forest");
		VIEW.context.strokeStyle = "rgba(0, 50, 0, " + alpha + ")";
		VIEW.context.fillStyle = "rgba(0, 50, 0, " + alpha + ")";
		break;
	case "WATER":
		debug("Water");
		VIEW.context.strokeStyle = "rgba(0, 0, 100, " + alpha + ")";
		VIEW.context.fillStyle = "rgba(0, 0, 100, " + alpha + ")";
		break;
	case "MOUNTAINS":
		VIEW.context.strokeStyle = "rgba(100, 0, 0, " + alpha + ")";
		VIEW.context.fillStyle = "rgba(100, 0, 0, " + alpha + ")";
		break;
	case "DESERT":
		VIEW.context.strokeStyle = "rgba(0, 0, 0, " + alpha + ")";
		VIEW.context.fillStyle = "rgba(150, 150, 0, " + alpha + ")";
		break;
	case "SNOW":
		VIEW.context.strokeStyle = "rgba(100, 100, 100, " + alpha + ")";
		VIEW.context.fillStyle = "rgba(100, 100, 100, " + alpha + ")";
		break;
	}
	VIEW.context.fillStyle += "77";
	VIEW.context.font = size + "px Arial";
	var w = VIEW.context.measureText(p.title).width;
	VIEW.context.translate(x, y);

	if (VIEW.brushMode == BRUSH_MODE.LABEL) {
		VIEW.context.beginPath();
		VIEW.context.arc(0, 0, 2, 0, 2 * Math.PI, false);
		VIEW.context.fill();
		VIEW.context.stroke();
	}

	VIEW.context.rotate(Math.PI * 2 / 360 * p.rotation);
	VIEW.context.fillText(p.title, - w/2, 0);
	VIEW.context.restore();
}

/* ---- Path drawing functions ---- */

function getVertexX(vertex) {
	var x = (vertex.x - VIEW.x) * VIEW.currentScale.column + (vertex.subX * VIEW.currentScale.column)/100 + 8;
	return x;
}

function getVertexY(vertex) {
	var y = (vertex.y - VIEW.y) * VIEW.currentScale.row + (vertex.subY * VIEW.currentScale.row)/100 + 8;
	if (vertex.x%2 == 1) {
		y += VIEW.currentScale.row / 2;
	}
	return y;
}

/**
 * Display a given path on the map.
 */
function drawPath(path) {
	debug("Path " + path.style);
	if (VIEW.selectedPathId == path.id) {
		VIEW.context.strokeStyle = "#FF4444";
	} else if (VIEW.brushMode == BRUSH_MODE.PATH && VIEW.brushStyle == path.style) {
		VIEW.context.strokeStyle = "#880000";
	} else {
		VIEW.context.strokeStyle = "#a4f8ff";
	}
	VIEW.context.lineWidth = 5;
	VIEW.context.beginPath();
	var v = path.vertex[0];
	VIEW.context.moveTo(getVertexX(v), getVertexY(v));
	var	selectedV = -1;
	if (v.vertex == VIEW.selectedVertexId) {
		selectedV = 0;
	}
	for (var i = 1; i < path.vertex.length; i++) {
		var v = path.vertex[i];
		VIEW.context.lineTo(getVertexX(v), getVertexY(v));
		if (v.vertex == VIEW.selectedVertexId) {
			selectedV = i;
		}
	}
	VIEW.context.stroke();
	
	if (VIEW.selectedPathId == path.id && selectedV >= 0) {
		VIEW.context.strokeStyle = "#000000";
		VIEW.context.beginPath();
		if (selectedV > 0) {
			var v = path.vertex[selectedV - 1];
			VIEW.context.moveTo(getVertexX(v), getVertexY(v));
			var v = path.vertex[selectedV];
			VIEW.context.lineTo(getVertexX(v), getVertexY(v));
		}
		var v = path.vertex[selectedV];
		VIEW.context.arc(getVertexX(v), getVertexY(v), 5, 0, 2 * Math.PI, false);
		VIEW.context.fillStyle = "black";
		VIEW.context.fill();
		VIEW.context.stroke();
		
	}
}

function closeAllDialogs() {
	$("#placeDialog").remove();
	$("#pathDialog").remove();
	$("#terrainPopout").remove();
	$("#thingPopout").remove();
	$("#mapPopout").remove();
	$("#areaPopout").remove();
}
