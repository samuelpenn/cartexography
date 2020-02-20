/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.place;

import net.notasnark.cartexography.map.info.MapInfo;

import javax.persistence.*;

@Entity
@Table(name = "place")
public class Place {
    @Id @GeneratedValue @Column(name = "id")
    int         id;

    @Column(name = "mapinfo_id")
    int         mapInfoId;

    @Column(name = "thing_id")
    int         thingId;

    @Column(name = "importance")
    int			importance;

    @Column(name = "tile_x")
    int			tileX;

    @Column(name = "tile_y")
    int			tileY;

    @Column(name = "sub_x")
    int			subX;

    @Column(name = "sub_y")
    int			subY;

    @Column(name = "name")
    String		name;

    @Column(name = "title")
    String		title;

    public Place() {
        this.id = 0;
    }

    public int getId() {
        return id;
    }

    public int getMapInfoId() {
        return mapInfoId;
    }

    public int getX() {
        return tileX;
    }

    public int getY() {
        return tileY;
    }

    public int getSubX() {
        return subX;
    }

    public int getSubY() {
        return subY;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public int getThingId() {
        return thingId;
    }

    public int getImportance() {
        return importance;
    }
}
