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
    int 		mapInfoId;
    int			x;
    int			y;
    int			terrainId;
    int			areaId;
    int			variant;

    Hex(Hex h) {
        this.mapInfoId = h.mapInfoId;
        this.x = h.x;
        this.y = h.y;
        this.terrainId = h.terrainId;
        this.areaId = h.areaId;
        //this.variant = h.variant;     // Not sure if this is wanted.
    }

}
