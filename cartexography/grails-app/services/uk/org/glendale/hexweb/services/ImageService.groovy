/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services

import uk.org.glendale.hexweb.*

class ImageService {

	/**
	 * Gets the font size for a given label and map scale.
	 * 
	 * @param label		Label to get font size for.
	 * @param width		Column width of the map.
	 * 
	 * @return			Point size to use for label font.
	 */
	def getLabelSize(Label label, int width) {
		int size = 2;
		switch (label.fontSize) {
		case 0:
			// xx-small
			size = 1;
			break;
		case 1:
			// x-small
			size = 2;
			break;
		case 2:
			// small
			size = 4;
			break;
		case 3:
			// medium
			size = 8;
			break;
		case 4:
			// large
			size = 16;
			break;
		case 5:
			// x-large
			size = 32;
			break;
		case 6:
			// xx-large
			size = 48;
			break;
		}
		size = (int)(size * width / 3.0);
		
		println "${label.title} ${width} ${label.fontSize} ${size}"
		
		return size
	}
	
	def getLabelAlpha(Label label, int width, int min) {
		int alpha = 100;
		int size = getLabelSize(label, width)
		
		println "${label.title} - ${size}"
		
		if (size < 10) {
			alpha = 0
		} else if (size < 30) {
			alpha = 100
		} else {
			size /= width
			switch (size) {
			case 0: case 1:
				alpha = 100
				break;
			default:
				alpha = 100 - size * 10
			}
			alpha = Math.max(alpha, (int)((min - 50) / 2))
		}

		return alpha;
	}
}
