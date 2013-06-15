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

var BRUSH_SIZE = new Object();
BRUSH_SIZE.SMALL = 1;
BRUSH_SIZE.MEDIUM = 3;
BRUSH_SIZE.LARGE = 5;

var EDIT_MODE = new Object();
EDIT_MODE.PAINT = "PAINT";    // Set target
EDIT_MODE.ADD = "ADD";        // Add new items
EDIT_MODE.SELECT = "SELECT";  // Select existing items
EDIT_MODE.EDIT = "EDIT";	  // Edit existing items
EDIT_MODE.DELETE = "DELETE";  // Delete existing items


/* Global variables */
var MAP = { id: 0 };					// This will be populated directly from JSON
var VIEW = { width: 32, height: 20, x: 0, y: 0, context: null } 	// View port configuration.

VIEW.brushMode = BRUSH_MODE.TERRAIN;
VIEW.brushSize = BRUSH_SIZE.SMALL;
VIEW.editMode = EDIT_MODE.PAINT;

VIEW.terrainBrush = 0;
VIEW.thingBrush = 0;
VIEW.showGrid = false;

VIEW.zoom = 0;
VIEW.port= { width: 1600, height: 1200 };

VIEW.scale = [ { column: 48, row: 56, width: 65, height: 56, font: 12, step: 4 },
               { column: 24, row: 28, width: 33, height: 28, font: 9 , step: 8},
               { column: 12, row: 14, width: 17, height: 14, font: 6 , step: 16 }, 
               { column: 6, row: 7, width: 9, height: 7, font: 4, step: 32  }, 
             ];
VIEW.currentScale = VIEW.scale[0];

function setZoom(zoom) {
	$("#zoomBtn"+VIEW.zoom).removeClass("selectedButton");
	VIEW.zoom = zoom;
	$("#zoomBtn"+VIEW.zoom).addClass("selectedButton");
	VIEW.currentScale = VIEW.scale[VIEW.zoom];
	VIEW.port.lastWidth = -1;
	refreshMap();
}

function setBrush(size) {
	$("#brushBtn"+VIEW.brushSize).removeClass("selectedButton");
	VIEW.brushSize = size;
	$("#brushBtn"+VIEW.brushSize).addClass("selectedButton");
}

function toggleGrid() {
	if (VIEW.showGrid) {
		// Switch off
		VIEW.showGrid = false;
		$("#showGrid").removeClass("selectedButton");
	} else {
		// Switch on
		VIEW.showGrid = true;
		$("#showGrid").addClass("selectedButton");
	}
	setViewPort();
	refreshMap();
}

function setViewPort() {
	VIEW.port.width = $("body").width() - VIEW.xMargins;
	VIEW.port.height = $("body").height() - VIEW.yMargins;
	
	var		tileWidth = VIEW.scale[VIEW.zoom].column;
	var		tileHeight = VIEW.scale[VIEW.zoom].height;
	VIEW.width = parseInt(VIEW.port.width / tileWidth) - 1;
	VIEW.height = parseInt(VIEW.port.height / tileHeight) - 1;
	if (VIEW.width % 2 == 1) {
		VIEW.width = VIEW.width + 1;
	}
	
	var canvas = document.getElementById("map");
	VIEW.context.save();
	VIEW.context.setTransform(1, 0, 0, 1, 0, 0);
	VIEW.context.clearRect(0, 0, canvas.width, canvas.height);
	VIEW.context.restore();
}

function drawHexGrid(x, y) {
	var tileWidth = VIEW.tileWidth;
	var tileHeight = VIEW.tileHeight;
	var halfOffset = VIEW.halfOffset

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

	$.getJSON("/hexweb/api/map/"+MAP.info.id+"/map?x="+startX+"&y="+startY+"&w="+mapWidth+"&h="+mapHeight, function(data) {
		MAP.map = data.map;
		MAP.area = data.area;
		MAP.places = data.places;

		startX = data.info.x;
		startY = data.info.Y;
		mapWidth = data.info.width;
		mapHeight = data.info.height;

		for (var y=0; y < mapHeight; y++) {
			for (var x=0; x < mapWidth; x++) {
				var t = MAP.map[y][x];
				var px = x * tileWidth + 8;
				var py = y * tileHeight + (x%2 * halfOffset) + 8;
				
				VIEW.context.drawImage(MAP.images[t].image, 
						px, py, imageWidth, imageHeight);
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
		$("#x-orig-view").html(VIEW.x + " / " + MAP.info.width)
		$("#y-orig-view").html(VIEW.y + " / " + MAP.info.height)
		
		for (var i=0; i < MAP.places.length; i++) {
			drawPlace(MAP.places[i]);
		}
	});
};

/**
 * Draw the specified place on the map.
 */
function drawPlace(p) {
	if (p.importance < VIEW.zoom) {
		return;
	}
	var x = (p.x - VIEW.x) * 48 - 24 + (p.sx * 65)/100;
	var y = (p.y - VIEW.y) * 56 + (p.x %2 * 28) - 20 + (p.sy * 56)/100;
	
	var tileWidth = VIEW.currentScale.column;
	var tileHeight = VIEW.currentScale.row;
	
	var x = (p.x - VIEW.x) * tileWidth - tileWidth/2 + (p.sx * VIEW.imageWidth)/100 + 8;
	var y = (p.y - VIEW.y) * tileHeight + (p.x %2 * VIEW.halfOffset) - tileHeight/2 + (p.sy * VIEW.imageHeight)/100 + 8;

	VIEW.context.drawImage(MAP.things[p.thing_id].image, x, y, 
			VIEW.imageWidth, VIEW.imageHeight);
	VIEW.context.font = VIEW.currentScale.font + "px Arial";
	var w = VIEW.context.measureText(p.title).width;
	VIEW.context.fillText(p.title, x + VIEW.imageWidth/2 - w / 2, y + VIEW.imageHeight);
}
