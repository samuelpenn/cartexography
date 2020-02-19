/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map;

import net.notasnark.cartexography.map.info.MapInfo;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * A Thing is something that could be on a map, but which aren't hexes themselves.
 * Once a thing is put on a map, it becomes a Place, linked to a tile but positioned
 * more accurately.
 */
@Entity
@Table(name = "thing")
public class Thing {
    int         id;
    MapInfo mapInfo;
    String		name;
    String		title;
    int			importance;
}
