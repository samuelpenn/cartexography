/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.path;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "path")
public class Path {
    @Id @GeneratedValue @Column(name = "id")
    long        id;

    @Column(name = "mapinfo_id")
    int         mapInfoId;

    @Column(name = "name")
    String		name;

    @Enumerated (EnumType.STRING)
    @Column(name = "style")
    PathStyle   style;

    @Column(name = "thickness1")
    int			thickness1;

    @Column(name = "thickness2")
    int			thickness2;

    @OneToMany(mappedBy = "path", fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    List<Vertex> vertices;

    public Path() {
        this.id = 0;
        vertices = new ArrayList<Vertex>();
    }

    public long getId() {
        return id;
    }

    public int getMapInfoId() {
        return mapInfoId;
    }

    public String getName() {
        return name;
    }

    public PathStyle getStyle() {
        return style;
    }

    public int getThickness1() {
        return thickness1;
    }

    public int getThickness2() {
        return thickness2;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }


}
