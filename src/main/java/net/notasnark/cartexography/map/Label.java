/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map;

import net.notasnark.cartexography.map.info.MapInfo;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * A label is a piece of text that appears on the map. It is placed like places,
 * but consists of text, font size and colour and rotation. Whether a label
 * appears is based on its size, and may be hidden if the map is zoomed in or
 * zoomed out too far.
 */
@Entity
@Table(name = "label")
public class Label {
    MapInfo mapInfo;
    int         id;
    int			tileX;
    int			tileY;
    int			subX;
    int			subY;
    String		name;
    String		title;
    int			fontSize;
    int			rotation;
    LabelStyle	style;
}
