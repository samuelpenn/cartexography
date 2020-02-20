/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.path;

import javax.persistence.*;

/**
 * A Vertex is a point on a path. It is positioned to 1/100ths of a tile using the sub values.
 */
@Entity
@Table(name = "vertex")
public class Vertex {
    @Id @GeneratedValue @Column(name = "id")
    int     id;     // Unique identifier.

    @ManyToOne
    @JoinColumn(name = "path_id", referencedColumnName = "id")
    //@Column(name = "path_id")
    Path     path;

    @Column(name = "vertex")
    int		vertex; // Index on this path.

    @Column(name = "x")
    int		x;

    @Column(name = "y")
    int		y;

    @Column(name = "sub_x")
    int		subX;

    @Column(name = "sub_y")
    int		subY;

    public Vertex() {
        this.id = 0;
    }

    public int getId() {
        return id;
    }

    public Path getPath() {
        return path;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSubX() {
        return subX;
    }

    public int getSubY() {
        return subY;
    }

}
