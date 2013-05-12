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

VIEW.zoom = 0;
VIEW.scale = [ { column: 48, row: 56, width: 65, height: 56, font: 12 },
               { column: 24, row: 28, width: 32, height: 28, font: 6 },
               { column: 12, row: 14, width: 16, height: 14, font: 3  } 
             ];


/**
 * Download the map data for the current view and display it in the
 * canvas.
 */
function refreshMap() {
	$.getJSON("/hexweb/api/map/"+MAP.info.id+"/map?x="+VIEW.x+"&y="+VIEW.y+"&w="+VIEW.width+"&h="+VIEW.height, function(data) {
		MAP.map = data.map;
		MAP.places = data.places;
		
		for (var y=0; y < 20; y++) {
			for (var x=0; x < 32; x++) {
				var t = MAP.map[y][x];
				VIEW.context.drawImage(MAP.images[t].image, 
						x * 48 + 8, y*56 + (x%2 * 28) + 8, 65, 56);
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
	var x = (p.x - VIEW.x) * 48 - 24 + (p.sx * 65)/100;
	var y = (p.y - VIEW.y) * 56 + (p.x %2 * 28) - 20 + (p.sy * 56)/100;
	VIEW.context.drawImage(MAP.things[p.thing_id].image, x, y, 65, 56);
	VIEW.context.font = "12px Arial";
	var w = VIEW.context.measureText(p.title).width;
	VIEW.context.fillText(p.title, x + 32 - w / 2, y + 50);
}
