/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.web.api;

/**
 * Object for holding map data, including tiles and other information.
 * Array data is held as rows, so [Y][X].
 */
public class MapData {
    // Map X origin.
    int         x;
    // Map Y origin.
    int         y;

    int[][]     terrain;
    int[][]     areas;

    public MapData(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;

        terrain = new int[height][width];
        areas = new int[height][width];
    }
}
