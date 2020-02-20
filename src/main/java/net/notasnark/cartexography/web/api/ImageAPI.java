/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.web.api;

import net.notasnark.cartexography.Cartexography;
import net.notasnark.cartexography.map.area.Area;
import net.notasnark.cartexography.map.hex.Hex;
import net.notasnark.cartexography.map.hex.HexDao;
import net.notasnark.cartexography.map.hex.Terrain;
import net.notasnark.cartexography.map.place.Thing;
import net.notasnark.cartexography.map.area.AreaDao;
import net.notasnark.cartexography.map.area.Bounds;
import net.notasnark.cartexography.map.info.MapInfo;
import net.notasnark.cartexography.map.info.MapInfoDao;
import net.notasnark.cartexography.map.hex.TerrainDao;
import net.notasnark.cartexography.web.Controller;
import net.notasnark.cartexography.web.Server;
import net.notasnark.utils.SimpleImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;

public class ImageAPI extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(ImageAPI.class);

    public void setupEndpoints() {
        logger.info("Setting up endpoints for ImageAPI");
        get("/api/image/hex", (request, response) -> drawHex(request, response));
        get( "/api/image/map/:id", (request, response) -> imageByCoord(request, response));
    }

    private Image getImage(Terrain terrain, int variant, String path, int width, int height) throws MalformedURLException {
        String fullpath = "file://" + path + "/terrain/" + terrain.getName() + "_" + variant + ".png";
        System.out.println(fullpath);
        URL url = new URL(fullpath);

        return SimpleImage.createImage(width, height, url);
    }

    private Image getImage(Thing thing, String path, int width, int height) throws MalformedURLException {
        URL		url = new URL("file://" + path + "/things/${thing.name}.png");

        return SimpleImage.createImage(width, height, url);
    }

    /**
     * Draws a single hexagon of the specified height.
     *
     * @return	PNG image containing a hexagon.
     */
    public Object drawHex(Request request, Response response) throws IOException, ApiException {
        int     height = getIntParamWithDefault(request, "height", 32);
        int		width = (int) (2.0 * height / Math.sqrt(3));

        SimpleImage image = new SimpleImage(width, height, "#FFFFFF");

        image.hexByHeight(0, 0, height, "#000000");

        response.type("image/png");
        return image.save().toByteArray();
    }

    public Object imageByArea(Request request, Response response) throws ApiException {
        try (Cartexography app = Server.getApp()) {
            MapInfoDao  mapInfoDao = app.getMapInfoDao();

            String mapId = getStringParam(request, "id");
            MapInfo info = mapInfoDao.get(mapId);

            String areaId = getStringParam(request, "area");
            int border = getIntParamWithDefault(request, "border", 1);
            int s = getIntParamWithDefault(request, "s", 32);
            String style = getStringParamWithDefault(request, "style", info.getStyle());

            AreaDao areaDao = app.getAreaDao();
            Area area = areaDao.get(info, areaId);

            if (area == null) {
                throw new IllegalArgumentException("Unknown area [${areaId}]");
            }

            Bounds bounds = areaDao.getBounds(area);
            int x = bounds.minX;
            int y = bounds.minY;
            int w = (bounds.maxX - x);
            int h = (bounds.maxY - y);

            if (border > 0) {
                x -= border;
                w += border * 2 + 1;
                y -= border;
                h += border * 2 + 1;
            }
            if (x % 2 == 1) {
                x -= 1;
                w += 1;
            }

            getImageByCoord(app, info, x, y, w, h, s, style);
        } catch (Exception e) {
            response.status(404);
        }
        return null;
    }

    /**
     * Get an image of the map according to the given set of coordinates.
     *
     * @return
     */
    Object imageByCoord(Request request, Response response) throws ApiException {
        logger.info("imageByCoord:");
        try (Cartexography app = Server.getApp()) {
            MapInfoDao mapInfoDao = app.getMapInfoDao();

            String mapId = getStringParam(request, "id");
            MapInfo info = mapInfoDao.get(mapId);

            System.out.println("Looking for " + mapId);

            if (info == null) {
                throw new ApiException("Unable to find map with name [" + mapId + "]");
            }

            int x = getIntParamWithDefault(request, "x", 0);
            int y = getIntParamWithDefault(request, "y", 0);
            int w = getIntParamWithDefault(request, "w", info.getWidth() - x);
            int h = getIntParamWithDefault(request, "h", info.getHeight() - y);
            String style = getStringParamWithDefault(request, "style", info.getStyle());
            int s = getIntParamWithDefault(request, "s", 32);


            response.type("image/png");
            SimpleImage image = getImageByCoord(app, info, x, y, w, h, s, style);

            return image.save().toByteArray();
        } catch (Exception e) {
            response.status(404);
        }
        return null;
    }

    private SimpleImage getImageByCoord(Cartexography app, MapInfo info, int x, int y, int w, int h, int scale, String style) throws MalformedURLException {

        logger.info(String.format("getImageByCoord: [%s] [%d]+[%d] [%d]x[%d]", info.getTitle(), x, y, w, h));

        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (x % 2 == 1) {
            x--;
        }
        if (x + w > info.getWidth()) {
            w = info.getWidth() - x;
        }
        if (y + h > info.getHeight()) {
            h = info.getHeight() - y;
        }

        return getMapImage(app, info, x, y, w, h, scale, style);

    }

    private void addVariantImage(Map<Integer, Map> images, Terrain terrain, int variant, String path, int w, int h) throws MalformedURLException {
        if (images.get((int)terrain.getId()) == null) {
            images.put((int)terrain.getId(), new HashMap());
        }
        Map varmap = images.get((int)terrain.getId());
        if (varmap.get(variant) == null) {
            varmap.put(variant, getImage(terrain, variant, path, w, h));
        } else {
            varmap.put(variant, getImage(terrain, variant, path, w, h));
        }
    }

    private Image getVariantImage(Map<Integer,Map> images, int tid, int var) {
        Map varmap = images.get(tid);
        if (varmap != null) {
            return (Image) varmap.get(var);
        }
        return null;
    }

    private SimpleImage getMapImage(Cartexography app, MapInfo info, int x, int y, int w, int h, int s, String style) throws MalformedURLException {
        // Use a default scale if none is given. Based on largest dimension.
        logger.debug("getMapImage: ");
        if (s < 1) {
            int size = w * h;
            if (size > 10000) {
                s = 1000000 / size;
            } else {
                s = 100;
            }
        }

        int			height = (int) ((h * s + s / 2) * 0.86);
        int			width = (int) ((w * s) * 0.73 + s * 0.25);

        if (style == null || style.length() == 0) {
            style = info.getStyle();
        }
        logger.debug("getMapImage: Have style of [" + style + "]");

        SimpleImage image = new SimpleImage(width, height, "#ffffff");

        String BASE_PATH = "cartexography/web-app/images/style/" + style; //grailsApplication.parentContext.getResource("WEB-INF/../images/style/"+style).file.absolutePath;
        BASE_PATH = new File(BASE_PATH).getAbsolutePath();

        int[][]		map = new int[h][w];
        int[][]		area = new int[h][w];
        Area        selectedArea = null;
//        if (params.areaId != null) {
//            selectedArea = areaService.getAreaByName(info, params.areaId);
//        }

        HexDao hexDao = app.getHexDao();
        TerrainDao terrainDao = app.getTerrainDao();
        AreaDao areaDao = app.getAreaDao();

        logger.debug("Got DAOs");

        Map terrain = new HashMap();
        Map	images = new HashMap();

        int		tileWidth = s;
        int		tileHeight = (int) (s * 0.86);
        int		columnWidth = (int) (s * 0.73);

        Terrain background = terrainDao.get(info.getBackground());
        terrain.put(info.getBackground(), background);
        Terrain oob = terrainDao.get(info.getOob());
        terrain.put(info.getOob(), oob);
        Terrain unknown = terrainDao.get(Terrain.UNKNOWN);

        logger.debug("Getting variants");

        addVariantImage(images, background, 0, BASE_PATH, tileWidth, tileHeight);
        addVariantImage(images, oob, 0, BASE_PATH, tileWidth, tileHeight);
        addVariantImage(images, unknown, 0, BASE_PATH, tileWidth, tileHeight);

        logger.debug("Got variants");

        List<Hex> hexes = hexDao.getAll(info, x, y, w, h);

        logger.debug("Found " + hexes.size() + " hexes");

        for (Hex hex : hexes) {
            //println "${hex[0]},${hex[1]}"
            System.out.println(hex);
            map[hex.getY() - y][hex.getX() - x] = hex.getTerrainId() * 10 + hex.getVariant();
            area[hex.getY() - y][hex.getX() - x] = hex.getAreaId();
            if (getVariantImage(images, hex.getTerrainId(), hex.getVariant()) == null) {
                Terrain 	t = terrainDao.get(hex.getTerrainId());
                System.out.println(hex.getTerrainId());
                if (t != null) {
                    System.out.println("Adding terrain " + t.getName());
                    addVariantImage(images, t, (int)hex.getVariant(), BASE_PATH, tileWidth, tileHeight);
                }
            }
        }

        // Draw terrain layer.
        for (int px = 0; px < w; px ++) {
            for (int py = 0; py < h; py ++) {
                int		tid = (int)(map[py][px] / 10);
                int		var = map[py][px] % 10;
                if (tid == 0) {
                    // No hex data, do we have sparse data?
                    tid = map[py - py%10][px - px%10];
                    if (tid == 0) {
                        // Default to background terrain.
                        //if (mapService.isOut(info, x + px, y + py)) {
                        //    tid = oob.id;
                        //} else {
                            tid = background.getId();
                        //}
                        var = 0;
                    }
                }
                Image img = getVariantImage(images, tid, var);
                if (img != null) {
                    int		xx = px * columnWidth;
                    int		yy = py * tileHeight;
                    if (px %2 == 1) {
                        yy += tileHeight / 2;
                    }
                    if (selectedArea == null || selectedArea.getId() == area[py][px]) {
                        image.paint(img, xx, yy, tileWidth, tileHeight);
                    } else {
                        image.paint(img, xx, yy, tileWidth, tileHeight);
                        image.paint(getVariantImage(images, Terrain.UNKNOWN, 0), xx, yy, tileWidth, tileHeight);
                    }
                } else {
                    System.out.println("No image for ${px}, ${py} ${tid} ${var}");
                }
            }
        }

        /*
        if (params.hex == "1") {
            String hexColour = "#44444444";
            float hexThickness = 3;
            // Draw a hex grid
            for (int px = 0; px < w; px ++) {
                for (int py = 0; py < h; py ++) {
                    double xx = px * columnWidth;
                    double yy = py * tileHeight + (px%2 * tileHeight/2);

                    image.line(xx + columnWidth / 3, yy, xx + columnWidth, yy, hexColour, hexThickness);
                    image.line(xx + columnWidth, yy, xx + columnWidth + columnWidth / 3, yy + tileHeight / 2, hexColour, hexThickness);
                    image.line(xx + columnWidth + columnWidth / 3, yy + tileHeight / 2, xx + columnWidth, yy + tileHeight, hexColour, hexThickness);
                    image.line(xx + columnWidth, yy + tileHeight, xx + columnWidth/3, yy + tileHeight, hexColour, hexThickness);
                    image.line(xx + columnWidth/3, yy + tileHeight, xx, yy + tileHeight/2, hexColour, hexThickness);
                    image.line(xx, yy + tileHeight/2, xx + columnWidth/3, yy, hexColour, hexThickness);
                }
            }
        }

         */
        /*
        if (params.areas == "1") {
            // Now do the area borders
            String borderColour = "#ff0000";
            float borderThickness = 5;
            for (int px = 0; px < w; px ++) {
                for (int py = 0; py < h; py ++) {
                    double xx = px * columnWidth;
                    double yy = py * tileHeight + (px%2 * tileHeight/2);

                    if (py > 0 && area[py][px] != area[py-1][px]) {
                        image.line(xx + columnWidth / 3, yy, xx + columnWidth, yy, borderColour, borderThickness);
                    }
                    if (px%2 == 1) {
                        if (px > 0 && area[py][px] != area[py][px-1]) {
                            image.line(xx, yy + tileHeight/2, xx + columnWidth/3, yy, borderColour, borderThickness);
                        }
                        if (px > 0 && py < h - 1 && area[py][px] != area[py+1][px-1]) {
                            image.line(xx, yy + tileHeight/2, xx + columnWidth/3, yy + tileHeight, borderColour, borderThickness);
                        }
                    } else {
                        if (px > 0 && py > 0 && area[py][px] != area[py-1][px-1]) {
                            image.line(xx, yy + tileHeight/2, xx + columnWidth/3, yy, borderColour, borderThickness);
                        }
                        if (px > 0 && area[py][px] != area[py][px-1]) {
                            image.line(xx, yy + tileHeight / 2, xx + columnWidth / 3, yy + tileHeight, borderColour, borderThickness);
                        }
                    }
                }
            }
        }

         */
        // Draw rivers
        //drawRivers(info, image, columnWidth, tileHeight, s, x, y, w, h)


        // Draw places
        /*
        java.util.List places = Place.findAll ({
                eq('mapInfo', info);
                between('tileX', x, x + w -1);
                between('tileY', y, y + h - 1);
        })
        Map	things = [:]
        places.each { place ->
            if (things.get(place.thingId) == null) {
                Thing thing = Thing.findById(place.thingId);
                things.put(thing.id, getImage((Thing)thing, BASE_PATH, tileWidth, tileHeight));
            }
            if (selectedArea == null || selectedArea.id == area[place.tileY - y][place.tileX - x]) {
                Image	img = things.get(place.thingId);
                if (img != null) {
                    int		xx = (place.tileX - x) * columnWidth - columnWidth / 2;
                    int		yy = (place.tileY - y) * tileHeight - tileHeight / 2;
                    if ((place.tileX - x) %2 == 1) {
                        yy += tileHeight / 2;
                    }
                    xx += (place.subX * tileWidth) / 100;
                    yy += (place.subY * tileHeight) / 100;
                    image.paint(img, xx, yy, tileWidth, tileHeight);
                    int	fontSize = s / 5 + place.importance * 2;
                    int fontWidth = image.getTextWidth(place.title, 0, fontSize);
                    xx += tileWidth / 2 - fontWidth / 2;
                    yy += tileHeight;
                    if (params.l != "0") {
                        image.text(xx, yy, place.title, 0,  fontSize, "#000000");
                    }
                }
            }
        }

         */

        // Draw labels
        /*
        if (params.l != "0") {
            java.util.List labels = Label.findAll ({
                    eq('mapInfo', info);
                    between('tileX', x, x + w -1);
                    between('tileY', y, y + h - 1);
            })
            labels.each { label ->
                if (selectedArea == null || selectedArea.id == area[label.tileY - y][label.tileX - x]) {
                    int		xx = (label.tileX - x) * columnWidth;
                    int		yy = (label.tileY - y) * tileHeight;
                    if ((label.tileX - x) %2 == 1) {
                        yy += tileHeight / 2;
                    }
                    xx += (label.subX * tileWidth) / 100;
                    yy += (label.subY * tileHeight) / 100;

                    int fontSize = imageService.getLabelSize(label, columnWidth);
                    int alpha = imageService.getLabelAlpha(label, columnWidth);

                    if (alpha > 0) {
                        alpha *= 2.55;
                        String colour = label.style.fill + Integer.toHexString(alpha);
                        int fontWidth = image.getTextWidth(label.title, 0, fontSize);
                        //image.circle(xx, yy, 8, "#000000")
                        xx -= fontWidth / 2;
                        image.text(xx, yy, label.title, 0, fontSize, colour, label.rotation);

                    }
                }
            }
        }
        */

        return image;
    }

        /**
         * Draw rivers on the map as bezier curves. Extra control points are calculated
         * dynamically to give a smooth curve across the length of the river.
         *
         * Paths are cropped to the area specified.
         *
         * @param info			Map to display.
         * @param image			Image to write into.
         * @param columnWidth	Width of a hex column.
         * @param tileHeight	Height of a hex tile.
         * @param s				Scale of the map.
         * @param x				X coordinate to start from.
         * @param y				Y coordinate to start from.
         * @param w				Width of map to display.
         * @param h				Height of map to display.
         *
        private void drawRivers(MapInfo info, SimpleImage image, int columnWidth, int tileHeight, int s, int x, int y, int w, int h) {
            List paths = pathService.getPathsInArea(info, x, y, w, h)

            def roads = []
            paths.each { path ->
                    String colour = "#b7f9ff"
                String style = "${path.style}"
                boolean isRoad = false
                if (style.equals("ROAD")) {
                    isRoad = true
                    colour = "#000000"
                }

                Vertex[] vertices = path.vertex.toArray()
                double[]	vx = new double[vertices.length+2]
                double[]	vy = new double[vertices.length+2]

                // Work out actual coordinates of each vertex on the map.
                for (int i=0; i < vertices.length; i++) {
                    vx[i+1] = vertices[i].x - x
                    vy[i+1] = vertices[i].y - y

                    vx[i+1] += vertices[i].subX / 100.0
                    vy[i+1] += vertices[i].subY / 100.0

                    if (vertices[i].x %2 == 1) {
                        vy[i+1] += 0.5
                    }
                    vx[i+1] *= columnWidth
                    vy[i+1] *= tileHeight
                }
                vx[0] = vx[1]
                vy[0] = vy[1]
                vx[vx.length-1] = vx[vx.length-2]
                vy[vy.length-1] = vy[vy.length-2]

                // Now calculate bezier control points and draw.
                drawBezierPath(image, vx, vy, colour,
                        path.thickness1, path.thickness2, (double)(s / 20.0),
                        vertices.length, isRoad)
                if (path.thickness1 == 4 && isRoad) {
                    // If this is a full road, need to append to list to draw again
                    // later in an overlay. All overlays must be drawn after all base
                    // roads, so that crossroads look right.
                    roads.add([ vx: vx, vy: vy, width: path.thickness1, len: vertices.length ] )
                }
            }

            // Draw full roads again, this time in white but slightly thinner. This
            // gives roads as a white line with a black border.
            roads.each { road ->
                    drawBezierPath(image, road.vx, road.vy, "#ffffff",
                            road.width, road.width, (double)(s / 30.0), road.len, true)
            }
        }

        /**
         * Actually draw a path on the image. Called after all the coordinates
         * have been calculated. For roads, this may get called twice, so we
         * can have a white road bordered with black.
         *
         * @param image		SimpleImage to draw onto.
         * @param vx		Array of X coordinates.
         * @param vy		Array of Y coordinates.
         * @param colour	Colour to use.
         * @param t1		Starting thickness.
         * @param t2		Ending thickness.
         * @param scale		Scale factor for thickness.
         * @param length	Actual length of path.
         * @param isRoad	iff true if a road, otherwise it's a river or coastline.
         *
        private void drawBezierPath(SimpleImage image, double[] vx, double[] vy,
                                    String colour, int t1, int t2, double scale, int length, boolean isRoad) {
            for (int i=1; i < vx.length - 2; i++) {
                double[]	xp = new double[4];
                double[]	yp = new double[4];
                xp[0] = vx[i]
                yp[0] = vy[i]
                xp[3] = vx[i+1]
                yp[3] = vy[i+1]

                // Work out control points dynamically.
                int ax, bx, cx, dx, xx
                int ay, by, cy, dy, yy
                // A is halfway point on previous line.
                ax = (vx[i-1] + vx[i]) / 2.0
                ay = (vy[i-1] + vy[i]) / 2.0
                // B is halfway point on this line.
                bx = (vx[i] + vx[i+1]) / 2.0
                by = (vy[i] + vy[i+1]) / 2.0
                // Halfway point between A and B
                xx = (ax + bx) / 2.0
                yy = (ay + by) / 2.0
                // Shift B control point up so A/B line intersects start of line
                ax -= xx - vx[i]
                ay -= yy - vy[i]
                bx -= xx - vx[i]
                by -= yy - vy[i]

                // C is equal to B
                cx = bx
                cy = by
                // D is halfway point on next line.
                dx = (vx[i+1] + vx[i+2]) / 2.0
                dy = (vy[i+1] + vy[i+2]) / 2.0
                // Halfway point between A and B
                xx = (cx + dx) / 2.0
                yy = (cy + dy) / 2.0
                // Shift B control point up so A/B line intersects start of line
                cx -= xx - vx[i+1]
                cy -= yy - vy[i+1]

                xp[1] = bx
                yp[1] = by
                xp[2] = cx
                yp[2] = cy

                double thickness = t1 - i * (t1 - t2) / length
                if (isRoad) {
                    // This is currently ugly. Want to be able to change the dash
                    // pattern based on the thickness. Also, roads should be thinner
                    // than rivers. Roads of thickness '4' are drawn twice, first
                    // time in black, second time in white but thinner.
                    double width = thickness * scale / 3.0
                    switch ((int)(thickness + 0.5)) {
                        case 0:
                            // Roads of thickness 0 shouldn't actually exist.
                            colour = "#997733"
                            image.curve(xp, yp, colour, width, (double)2.0, (double)5.0)
                            break;
                        case 1:
                            colour = "#774400"
                            image.curve(xp, yp, colour, width, (double)5.0, (double)5.0)
                            break;
                        case 2:
                            colour = "#774400"
                            image.curve(xp, yp, colour, width, (double)20.0, (double)10.0)
                            break;
                        case 3:
                            colour = "#555555"
                            image.curve(xp, yp, colour, width)
                            break;
                        default:
                            image.curve(xp, yp, colour, width)
                            break;
                    }
                } else {
                    image.curve(xp, yp, colour, thickness * scale)
                }
            }
        }
    }
    */
}
