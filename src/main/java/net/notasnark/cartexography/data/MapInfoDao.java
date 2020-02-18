/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.data;

import net.notasnark.cartexography.Cartexography;
import net.notasnark.cartexography.map.MapInfo;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class MapInfoDao extends Dao {
    private static String NAME_QUERY = "FROM MapInfo WHERE name = :name";

    public MapInfoDao(final EntityManager session) {
        super(session);
    }

    public List<MapInfo> getAll() {
        return (List<MapInfo>) session.createQuery("FROM MapInfo").getResultList();
    }

    public MapInfo get(int id) {
        MapInfo map = (MapInfo) session.find(MapInfo.class, id);

        return map;
    }

    public MapInfo get(String name) {
        Query query = session.createQuery(NAME_QUERY);
        query.setParameter("name", name);
        MapInfo map = (MapInfo) query.getSingleResult();

        return map;
    }
}
