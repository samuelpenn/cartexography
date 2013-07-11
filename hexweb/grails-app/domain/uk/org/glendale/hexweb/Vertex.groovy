/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb

/**
 * A Vertex is a point on a path. It is positioned in 100ths of a hex tile.
 */
class Vertex {
	int		vertex
	int		x
	int		y
	int		subX
	int		subY
	
	static belongsTo = [path: Path]
    static mapping = {
		table "vertex"
		subX column: "sub_x"
		subY column: "sub_y"
    }
}
