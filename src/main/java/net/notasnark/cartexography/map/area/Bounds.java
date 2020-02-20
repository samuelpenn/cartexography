/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.area;

/**
 * Class which defines the bounds of a region on a map. All coordinates are inclusive.
 * minX and minY are the top left, and maxX and maxY are the bottom right.
 */
public class Bounds {
    public int minX = 0;
    public int maxX = 0;
    public int minY = 0;
    public int maxY = 0;

    public Bounds() {
        // Default constructor.
    }

    public Bounds(int minX, int minY, int maxX, int maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

}
