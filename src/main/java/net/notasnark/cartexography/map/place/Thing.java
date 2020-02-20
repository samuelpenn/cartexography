/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.place;

import net.notasnark.cartexography.map.info.MapInfo;

import javax.persistence.*;

/**
 * A Thing is something that could be on a map, but which aren't hexes themselves.
 * Once a thing is put on a map, it becomes a Place, linked to a tile but positioned
 * more accurately.
 */
@Entity
@Table(name = "thing")
public class Thing {
    @Id @GeneratedValue @Column(name = "id")
    int         id;

    @Column(name = "mapinfo_id")
    int         mapInfoId;

    @Column(name = "name")
    String		name;

    @Column(name = "title")
    String		title;

    @Column(name = "importance")
    int			importance;

    public Thing() {
        this.id = id;
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
