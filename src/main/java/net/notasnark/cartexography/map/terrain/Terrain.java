/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.terrain;

import net.notasnark.cartexography.map.info.MapInfo;

import javax.persistence.*;

/**
 * Defines the type of a hex tile. Specifies the image that is used, the flat
 * colour for large scale maps, the name and title. The order is used to sort
 * terrain, so similar types of terrain appear together in the palates.
 */
@Entity
@Table(name = "terrain")
public class Terrain {
    public static final int UNKNOWN = 1;

    @Id
    @GeneratedValue
    @Column(name = "id")
    int         id;

    @Column(name = "mapinfo_id")
    int         mapInfoId;

    @Column(name = "name")
    String		name;

    @Column(name = "variants")
    int			variants;

    @Column(name = "title")
    String		title;

    @Column(name = "water")
    boolean		water;
    String		colour;
    /* Order used to group similar types. An order of zero means that
     * this terrain type is for display purposes only - it cannot be
     * drawn onto the map.
     */
    int			ordering;

    public Terrain() {
        this.id = 0;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getVariants() {
        return variants;
    }
}
