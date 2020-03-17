/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.web.api;

import net.notasnark.cartexography.Cartexography;
import net.notasnark.cartexography.map.hex.Hex;
import net.notasnark.cartexography.map.hex.HexDao;
import net.notasnark.cartexography.map.hex.Terrain;
import net.notasnark.cartexography.map.hex.TerrainDao;
import net.notasnark.cartexography.map.info.MapInfo;
import net.notasnark.cartexography.map.info.MapInfoDao;
import net.notasnark.cartexography.web.Controller;
import net.notasnark.cartexography.web.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.List;

import static spark.Spark.get;

/**
 * API for handling map data.
 */
public class MapAPI extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(ImageAPI.class);

    public void setupEndpoints() {
        logger.info("Setting up endpoints for MapAPI");
        get("/api/map/:id", this::map, json());
        get("/api/map/:id/data", this::data, json());
        get("/api/map/:id/terrain", this::terrain, json());
    }

    /**
     * Return meta data on a map.
     *
     * @param request       Request object.
     * @param response      Response object.
     * @return              JSON data describing a MapInfo object.
     */
    private MapInfo map(Request request, Response response) {
        logger.info("map:");
        try (Cartexography app = Server.getApp()) {
            MapInfoDao mapInfoDao = app.getMapInfoDao();

            String mapId = getStringParam(request, "id");
            MapInfo info = mapInfoDao.get(mapId);

            response.type("application/json");
            return info;
        } catch (Exception e) {
            response.status(404);
        }
        return null;
    }

    private MapData data(Request request, Response response) {
        logger.info("data:");

        try (Cartexography app = Server.getApp()) {
            MapInfoDao mapInfoDao = app.getMapInfoDao();

            String  mapId = getStringParam(request, "id");
            int     x = getIntParamWithDefault(request, "x", 0);
            int     y = getIntParamWithDefault(request, "y", 0);
            int     w = getIntParamWithDefault(request, "w", 32);
            int     h = getIntParamWithDefault(request, "h", 20);

            if (w < 1 || h < 1) {
                throw new ApiException("Requested width and height must be at least 1");
            }

            MapInfo info = mapInfoDao.get(mapId);
            // Honour requested size of map before requested origin of map.
            x = Math.max(0, Math.min(x, info.getWidth() - w));
            y = Math.max(0, Math.min(y, info.getHeight() - h));

            MapData data = new MapData(x, y, w, h);

            HexDao hexDao = app.getHexDao();
            List<Hex> hexList = hexDao.getAll(info, x, y, w, h);

            for (Hex hex : hexList) {
                data.terrain[hex.getY() - y][hex.getX() - x] = hex.getTerrainId() * 100 + hex.getVariant();
                data.areas[hex.getY() - y][hex.getX() - x] = hex.getAreaId();
            }

            response.type("application/json");
            return data;
        } catch (Exception e) {
            response.status(404);
        }

        return null;
    }

    private Object terrain(Request request, Response response) {
        logger.info("terrain:");

        try (Cartexography app = Server.getApp()) {
            MapInfoDao mapInfoDao = app.getMapInfoDao();
            TerrainDao terrainDao = app.getTerrainDao();

            String  mapId = getStringParam(request, "id");
            MapInfo mapInfo = mapInfoDao.get(mapId);

            logger.debug("Getting terrain for " + mapInfo.getTemplate());
            List<Terrain> list = terrainDao.getAll(mapInfo);
            logger.debug("Got " + list.size() + " results");

            response.type("application/json");
            return list;
        } catch (Exception e) {
            response.status(404);
        }

        return null;
    }
}
