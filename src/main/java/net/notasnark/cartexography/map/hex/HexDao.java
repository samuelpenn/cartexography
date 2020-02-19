/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.hex;

import net.notasnark.cartexography.data.Dao;
import net.notasnark.cartexography.map.info.MapInfo;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class HexDao extends Dao {
    private static String XY_QUERY = "FROM Hex WHERE mapinfo_id = :map AND x = :x AND y = :y";
    private static String RECT_QUERY = "FROM Hex WHERE mapinfo_id = :map AND x >= :x1 AND y >= :y1 AND x < :x2 AND y < :y2 ORDER BY y, x";

    public HexDao(final EntityManager session) {
        super(session);
    }

    /**
     * Gets an area according to its unique id.
     *
     * @param id    Id of the area to get.
     * @return      Area if found.
     */
    public Hex get(int id) {
        return (Hex) session.find(Hex.class, id);
    }

    public Hex get(MapInfo info, int x, int y) {
        Query  query = session.createQuery(XY_QUERY);
        query.setParameter("map", info.getId());
        query.setParameter("x", x);
        query.setParameter("y", y);

        Hex hex = (Hex) query.getSingleResult();

        return hex;
    }


    public List<Hex> getAll(MapInfo info, int x, int y, int w, int h) {
        try {
            Query query = session.createQuery(RECT_QUERY);
            query.setParameter("map", info.getId());
            query.setParameter("x1", x);
            query.setParameter("y1", y);
            query.setParameter("x2", x + w);
            query.setParameter("y2", y + h);

            List<Hex> hexes = query.getResultList();

            return hexes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
