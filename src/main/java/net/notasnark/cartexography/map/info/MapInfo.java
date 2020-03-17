/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.info;

import javax.persistence.*;

@Entity
@Table(name = "mapinfo")
public class MapInfo {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int         id;

    @Column(name = "name")
    private String		name;		// Unique name for this map. Expected to be [a-z][a-z0-9_]*

    @Column(name = "title")
    private String		title;   	// Descriptive name for this map.

    @Column(name = "style")
    private String		style;		// Style of the icons.

    @Column(name = "width")
    private int			width;   	// Width, in hexes.

    @Column(name = "height")
    private int			height;  	// Height, in hexes.

    @Column(name = "scale")
    private int			scale;   	// Width of each hex, in metres.

    @Column(name = "world")
    private boolean		world;   	// Is this a world map? If so, will be discontinuous.

    @Column(name = "template")
    private int			template;  	// Use the specified map as a template. If zero, ignored.

    @Column(name = "background")
    private int			background; // Default backgroup terrain id.

    @Column(name = "oob")
    private int			oob;		// Out of Bounds terrain id.

    MapInfo() {
        this.id = 0;
        this.name = "unnamed";
        this.title = "Unnamed";
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

    public void setName(String name) {
        if (name != null && name.trim().length() > 0) {
            this.name = name.trim();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title != null && title.trim().length() > 0) {
            this.title = title.trim();
        }
    }

    public String getStyle() {
        return style;
    }

    public int getOob() {
        return oob;
    }

    public int getBackground() {
        return background;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getScale() {
        return scale;
    }

    public boolean isWorld() {
        return world;
    }

    public int getTemplate() { return template; }


}
