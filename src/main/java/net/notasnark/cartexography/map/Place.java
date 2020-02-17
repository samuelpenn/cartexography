/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "place")
public class Place {
    int         id;
    MapInfo		mapInfo;
    Thing		thing;

    int			importance;
    int			tileX;
    int			tileY;
    int			subX;
    int			subY;
    String		name;
    String		title;
}
