/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map;

import net.notasnark.cartexography.map.info.MapInfo;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Defines the type of a hex tile. Specifies the image that is used, the flat
 * colour for large scale maps, the name and title. The order is used to sort
 * terrain, so similar types of terrain appear together in the palates.
 */
@Entity
@Table(name = "terrain")
public class Terrain {
    static final int UNKNOWN = 1;
    int         id;
    MapInfo mapInfo;
    String		name;
    int			variants;
    String		title;
    boolean		water;
    String		colour;
    /* Order used to group similar types. An order of zero means that
     * this terrain type is for display purposes only - it cannot be
     * drawn onto the map.
     */
    int			ordering;
}
