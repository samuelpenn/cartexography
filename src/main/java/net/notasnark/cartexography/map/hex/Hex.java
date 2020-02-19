/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.hex;

import javax.persistence.*;

@Entity
@Table(name = "map")
public class Hex {
    @Id
    @GeneratedValue
    @Column(name = "id")
    long        id;

    @Column(name = "mapinfo_id")
    int 		mapInfoId;

    @Column(name = "x")
    int			x;

    @Column(name = "y")
    int			y;

    @Column(name = "terrain_id")
    int			terrainId;

    @Column(name = "area_id")
    int			areaId;

    @Column(name = "variant")
    int			variant;

    public Hex() {
        this.id = 0;
    }

    Hex(Hex h) {
        this.mapInfoId = h.mapInfoId;
        this.x = h.x;
        this.y = h.y;
        this.terrainId = h.terrainId;
        this.areaId = h.areaId;
        //this.variant = h.variant;     // Not sure if this is wanted.
    }

    public long getId() {
        return id;
    }

    public int getMapInfoId() {
        return mapInfoId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getTerrainId() {
        return terrainId;
    }

    public int getAreaId() {
        return areaId;
    }

    public int getVariant() {
        return variant;
    }

    public String toString() {
        return String.format("[%d,%d] [T:%d] [A:%d]", x, y, terrainId, areaId);
    }

}
