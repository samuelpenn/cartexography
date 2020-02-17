/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * A Vertex is a point on a path. It is positioned to 1/100ths of a tile using the sub values.
 */
@Entity
@Table(name = "vertex")
public class Vertex {
    int     id;     // Unique identifier.
    int     pathId;
    int		vertex; // Index on this path.
    int		x;
    int		y;
    int		subX;
    int		subY;
}
