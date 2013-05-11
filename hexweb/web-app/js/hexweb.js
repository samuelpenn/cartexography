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
