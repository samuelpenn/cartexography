/*
 * Copyright (C) 2013 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 3, or
 * any later version. See the file COPYING.
 */
package uk.org.glendale.hexweb.services


import grails.test.mixin.*
import org.junit.*


/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ImportService)
class ImportServiceTests {

	/**
	 * Test that the blob reading is working correctly.
	 */
	void testBlobs() {
		String	blob = "AEBCABAFAA"
		
		assert service.getTerrainCode(blob) == 4
		assert service.getHeightCode(blob) == 66
		assert service.getFeatureCode(blob) == 1
		assert service.getAreaCode(blob) == 5
	}
}
