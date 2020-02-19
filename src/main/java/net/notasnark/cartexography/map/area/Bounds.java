/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.area;

/**
 * Class which defines the bounds of a region on a map.
 */
public class Bounds {
    public int minX = 0;
    public int maxX = 0;
    public int minY = 0;
    public int maxY = 0;

    protected Bounds() {
        // We can only define it locally.
    }

}
