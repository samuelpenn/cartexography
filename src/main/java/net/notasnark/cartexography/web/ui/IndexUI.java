/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.web.ui;

import net.notasnark.cartexography.Cartexography;
import net.notasnark.cartexography.map.info.MapInfo;
import net.notasnark.cartexography.web.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.notasnark.cartexography.Main.getApp;
import static spark.Spark.get;

public class IndexUI extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(IndexUI.class);

    public void setupEndpoints() {
        logger.info("Setting up endpoints for IndexUI");
        get("/", (request, response) -> index(request, response));
        get("/index", (request, response) -> index(request, response));
        get("/index.html", (request, response) -> index(request, response));
    }

    private Object index(Request request, Response response) {
        try (Cartexography app = getApp()) {
            Map<String,Object> model = new HashMap<>();

            List<MapInfo> maps = app.getMapInfoDao().getAll();
            model.put("version", "2.0");
            model.put("maps", maps);

            return new VelocityTemplateEngine().render(
                new ModelAndView(model, "templates/index.vm")
            );

        } catch (Exception e) {
            logger.error("Internal error", e);
        }
        return null;
    }
}
