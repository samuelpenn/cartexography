/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.area;

import net.notasnark.cartexography.data.Dao;
import net.notasnark.cartexography.map.info.MapInfo;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class AreaDao extends Dao {
    private static String NAME_QUERY = "FROM Area WHERE mapinfo_id = :map AND name = :name";
    private static String BOUNDS_QUERY = "FROM Hex WHERE mapinfo_id = :map AND area_id = :area";

    public AreaDao(final EntityManager session) {
        super(session);
    }

    /**
     * Gets an area according to its unique id.
     *
     * @param id    Id of the area to get.
     * @return      Area if found.
     */
    public Area get(int id) {
        return (Area) session.find(Area.class, id);
    }

    public Area get(MapInfo map, String name) {
        Query query = session.createQuery(NAME_QUERY);
        query.setParameter("map", map.getId());
        query.setParameter("name", name);
        Area area = (Area) query.getSingleResult();

        return area;
    }

    /**
     * Get the rectangular bounds for a named area on a map.
     *
     * @param area  Area to get the bounds for.
     * @return      The X and Y bounds.
     */
    public Bounds getBounds(Area area) {
        Bounds bounds = new Bounds();

        Query query = session.createNativeQuery("SELECT min(x) AS MINX, max(x) AS MAXX, min(y) AS MINY, max(y) AS MAXY FROM map WHERE mapinfo_id = :map AND area_id = :area");
        query.setParameter("map", area.getMapInfoId());
        query.setParameter("area", area.getId());

        Object[] result = (Object[]) query.getSingleResult();
        bounds.minX = (int) result[0];
        bounds.maxX = (int) result[1];
        bounds.minY = (int) result[2];
        bounds.maxY = (int) result[3];

        return bounds;
    }
}
