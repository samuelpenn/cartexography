/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.terrain;

import net.notasnark.cartexography.data.Dao;
import net.notasnark.cartexography.map.info.MapInfo;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class TerrainDao extends Dao {
    private static String NAME_QUERY = "FROM MapInfo WHERE name = :name";

    public TerrainDao(final EntityManager session) {
        super(session);
    }

    public List<Terrain> getAll() {
        return (List<Terrain>) session.createQuery("FROM Terrain").getResultList();
    }

    public List<Terrain> getAll(MapInfo map) {
        Query query =  session.createQuery("FROM Terrain WHERE mapinfo_id = :map");
        query.setParameter("map", map.getId());

        return (List<Terrain>) query.getResultList();
    }

    public Terrain get(int id) {
        return (Terrain) session.find(Terrain.class, id);
    }

}
