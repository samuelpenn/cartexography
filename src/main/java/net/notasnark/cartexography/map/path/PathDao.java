/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.map.path;

import net.notasnark.cartexography.data.Dao;
import net.notasnark.cartexography.map.hex.Hex;

import javax.persistence.EntityManager;

public class PathDao extends Dao {
    public PathDao(EntityManager session) {
        super(session);
    }

    public Path get(long id) {
        return (Path) session.find(Path.class, id);
    }
}
