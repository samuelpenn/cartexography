/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "area")
public class Area {
    MapInfo		mapInfo;
    int         id;
    String		name;
    String		title;

    List<Area>  children;
    Area        parent;

}

