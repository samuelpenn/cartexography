/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.web.ui;

import net.notasnark.cartexography.Cartexography;
import net.notasnark.cartexography.map.info.MapInfo;
import net.notasnark.cartexography.web.Controller;
import net.notasnark.cartexography.web.api.ApiException;
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
        get("/", this::index);
        get("/index", this::index);
        get("/index.html", this::index);
        get("/map/:id", this::map);
    }

    /**
     * Provide a list of maps that can be clicked on to take user to a specific map.
     *
     * @param request
     * @param response
     * @return
     */
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

    /**
     * Display a map for viewing and editing.
     *
     * @param request
     * @param response
     * @return
     */
    private Object map(Request request, Response response) throws ApiException {
        System.out.println("IndexUI.map:");
        String mapId = getStringParam(request, "id");

        try (Cartexography app = getApp()) {
            Map<String,Object> model = new HashMap<>();

            MapInfo map = app.getMapInfoDao().get(mapId);
            System.out.println(map.getTitle());
            model.put("version", "2.0");
            model.put("mapId", mapId);
            model.put("mapInfo", map);
            model.put("mapTitle", map.getTitle());

            return new VelocityTemplateEngine().render(
                    new ModelAndView(model, "templates/map.vm")
            );

        } catch (Exception e) {
            logger.error("Internal error", e);
        }
        return null;
    }
}
