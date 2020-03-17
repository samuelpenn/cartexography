/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.web.api;

import net.notasnark.cartexography.web.Controller;
import org.slf4j.Logger;
import spark.Request;

/**
 * Object for obtaining and passing around map style information from web parameters.
 */
public class MapStyle {
    String      style = "standard";
    boolean     showHexes = false;
    boolean     showAreas = false;
    int         scale = 50;
    String      selectedArea = null;


    public MapStyle(Request request, Logger logger) {
        try {
            scale = Controller.getIntParamWithDefault(request, "scale", 50);
        } catch (ApiException e) {
            logger.warn("Ignoring invalid parameter value for 'scale'");
        }

        try {
            showHexes = Controller.getBooleanParamWithDefault(request, "showHexes", false);
        } catch (ApiException e) {
            logger.warn("Ignoring invalid parameter value for 'showHexes'");
        }

        try {
            showAreas = Controller.getBooleanParamWithDefault(request, "showAreas", false);
        } catch (ApiException e) {
            logger.warn("Ignoring invalid parameter value for 'showAreas'");
        }

        try {
            selectedArea = Controller.getStringParamWithDefault(request, "area", null);
        } catch (ApiException e) {
            logger.warn("Ignoring invalid parameter value for 'selectedArea'");
        }

        try {
            style = Controller.getStringParamWithDefault(request, "style", "standard");
        } catch (ApiException e) {
            logger.warn("Ignoring invalid parameter value for 'standard'");
        }

    }
}
