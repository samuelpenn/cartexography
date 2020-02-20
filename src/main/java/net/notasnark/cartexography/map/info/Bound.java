/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.info;

import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * Defines the bounds for a world map. This is used for discontinuous maps where the extreme North and South
 * areas will have gaps in them. Normally such maps are displayed as Icosahedral maps. The Bounds defines the
 * minimum and maximum y coordinate for each column of tiles.
 */
@Entity
@Table(name = "bound")
public class Bound {
    int		mapInfoId;
    int		x;
    int		min;
    int		max;
}
