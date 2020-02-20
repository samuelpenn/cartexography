/*
 * Copyright (C) 2020 Samuel Penn, sam@notasnark.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package net.notasnark.cartexography.map.label;

/**
 * Define the available styles of labels.
 * The style defines the colours to be used when displaying
 * a label on the map.
 */
public enum LabelStyle {
	STANDARD("#000000", "#000000"),
	FOREST("#004400", "#004400"),
	WATER("#000088", "#000088"),
	MOUNTAINS("#880000", "#880000"),
	DESERT("#BBBB00", "#000000"),
	SNOW("#888888", "#888888");

	public final String  fill;
	public final String  stroke;
	
	private LabelStyle(String fill, String stroke) {
		this.fill = fill;
		this.stroke = stroke;
	}
}
