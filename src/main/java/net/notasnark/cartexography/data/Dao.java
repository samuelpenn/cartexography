/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.data;

import javax.persistence.EntityManager;

public class Dao {
    protected final EntityManager session;

    protected Dao(EntityManager session) {
        if (session == null || !session.isOpen()) {
            throw new IllegalArgumentException("Session object must be open and non-null.");
        }
        this.session = session;
    }
}
