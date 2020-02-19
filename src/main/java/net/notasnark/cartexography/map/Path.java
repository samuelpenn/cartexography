/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map;

import net.notasnark.cartexography.map.info.MapInfo;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "path")
public class Path {
    MapInfo mapInfo;
    int         id;
    String		name;
    PathStyle	style;
    int			thickness1;
    int			thickness2;

    List<Vertex> vertices;

}
