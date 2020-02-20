/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.place;

import net.notasnark.cartexography.data.Dao;
import net.notasnark.cartexography.map.area.Bounds;
import net.notasnark.cartexography.map.hex.Hex;
import net.notasnark.cartexography.map.info.MapInfo;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class PlaceDao extends Dao {
    private static String XY_QUERY = "FROM Place WHERE mapinfo_id = :map AND tileX = :x AND tileY = :y ORDER BY id";
    private static String RECT_QUERY = "FROM Place WHERE mapinfo_id = :map AND tileX >= :x1 AND tileY >= :y1 AND tileX < :x2 AND tileY < :y2 ORDER BY id";

    public PlaceDao(final EntityManager session) {
        super(session);
    }

    /**
     * Gets a Place according to its unique id.
     *
     * @param id    Id of the place to get.
     * @return      Place if found.
     */
    public Place get(int id) {
        return (Place) session.find(Place.class, id);
    }

    /**
     * Gets all the places that are defined on a particular hex tile.
     *
     * @param info
     * @param x
     * @param y
     * @return
     */
    public List<Place> getAll(MapInfo info, int x, int y) {
        Query query = session.createQuery(XY_QUERY);
        query.setParameter("map", info.getId());
        query.setParameter("x", x);
        query.setParameter("y", y);

        return (List<Place>) query.getResultList();
    }

    /**
     * Gets all the places that are defined without the bounds of the given map.
     *
     * @param info
     * @param bounds
     * @return
     */
    public List<Place> getAll(MapInfo info, Bounds bounds) {
        Query query = session.createQuery(XY_QUERY);
        query.setParameter("map", info.getId());
        query.setParameter("x1", bounds.minX);
        query.setParameter("y1", bounds.minY);
        query.setParameter("x2", bounds.maxX);
        query.setParameter("y2", bounds.maxY);

        return (List<Place>) query.getResultList();
    }
}
