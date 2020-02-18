/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map;

import javax.persistence.*;

@Entity
@Table(name = "mapinfo")
public class MapInfo {
    @Id
    @GeneratedValue
    @Column(name = "id")
    int         id;

    @Column(name = "name")
    String		name;		// Unique name for this map. Expected to be [a-z][a-z0-9_]*

    @Column(name = "title")
    String		title;   	// Descriptive name for this map.

    @Column(name = "style")
    String		style;		// Style of the icons.

    @Column(name = "width")
    int			width;   	// Width, in hexes.

    @Column(name = "height")
    int			height;  	// Height, in hexes.

    @Column(name = "scale")
    int			scale;   	// Width of each hex, in metres.

    @Column(name = "world")
    boolean		world;   	// Is this a world map? If so, will be discontinuous.

    @Column(name = "template")
    int			template;  	// Use the specified map as a template. If zero, ignored.

    @Column(name = "background")
    int			background; // Default backgroup terrain id.

    @Column(name = "oob")
    int			oob;		// Out of Bounds terrain id.

    MapInfo() {
        this.id = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.id = id;
    }

    public String getName() {
        return name;
    }
}
