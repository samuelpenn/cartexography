/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.label;

import javax.persistence.*;

/**
 * A label is a piece of text that appears on the map. It is placed like places,
 * but consists of text, font size and colour and rotation. Whether a label
 * appears is based on its size, and may be hidden if the map is zoomed in or
 * zoomed out too far.
 */
@Entity
@Table(name = "label")
public class Label {
    @Id
    @GeneratedValue
    @Column(name = "id")
    int         id;

    @Column(name = "mapinfo_id")
    int         mapInfoId;

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

    @Column(name = "font_size")
    int			fontSize;

    @Column(name = "rotation")
    int			rotation;

    @Enumerated (EnumType.STRING)
    @Column(name = "style")
    LabelStyle style;

    public Label() {
        this.id = 0;
        this.mapInfoId = 0;
        this.style = LabelStyle.STANDARD;
    }

    public int getId() {
        return id;
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

    public int getFontSize() {
        return fontSize;
    }

    public int getRotation() {
        return rotation;
    }

    public LabelStyle getStyle() {
        return style;
    }
}
