/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "mapinfo")
public class MapInfo {
    String		name;		// Unique name for this map. Expected to be [a-z][a-z0-9_]*
    String		title;   	// Descriptive name for this map.
    String		style;		// Style of the icons.
    int			width;   	// Width, in hexes.
    int			height;  	// Height, in hexes.
    int			scale;   	// Width of each hex, in metres.
    boolean		world;   	// Is this a world map? If so, will be discontinuous.
    int			template;  	// Use the specified map as a template. If zero, ignored.
    int			background; // Default backgroup terrain id.
    int			oob;		// Out of Bounds terrain id.
}
