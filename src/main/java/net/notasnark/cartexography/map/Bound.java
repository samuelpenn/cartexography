/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map;

import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * Defines the bounds for a map.
 */
@Entity
@Table(name = "bound")
public class Bound {
    int		mapInfoId;
    int		x;
    int		min;
    int		max;
}
