/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.area;

import javax.persistence.*;
import java.util.List;

/**
 * A named area of a map. Each tile may have an area associated with it.
 */
@Entity
@Table(name = "area")
public class Area {
    @Id
    @GeneratedValue
    @Column(name = "id")
    int         id;

    @Column(name = "mapinfo_id")
    int		mapInfoId;

    @Column(name = "name")
    String		name;

    @Column(name = "title")
    String		title;

    //List<Area>  children;
    //Area        parent;

    public Area() {
        this.id = 0;
        this.mapInfoId = 0;
        this.name = "untitled";
        this.title = "Untitled";
    }

    public int getId() {
        return id;
    }

    public int getMapInfoId() {
        return mapInfoId;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

}

