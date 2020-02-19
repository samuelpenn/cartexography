/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography;

import net.notasnark.cartexography.map.area.AreaDao;
import net.notasnark.cartexography.map.info.MapInfoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceException;


public class Cartexography implements AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(Cartexography.class);

	private EntityManager       session;
	private EntityTransaction 	transaction;
	private Config              configuration;

	private static int			count = 0;

	public Cartexography(EntityManager session, Config configuration) {
		logger.info(String.format("Cartexography: Creating new database session [%d]", ++count));
		if (session == null || !session.isOpen()) {
			throw new IllegalArgumentException("Cannot create Cartexography object with a non-open session.");
		}
		this.configuration = configuration;
		this.session = session;
		session.setFlushMode(FlushModeType.COMMIT);
		transaction = session.getTransaction();
		transaction.begin();
	}

	public void close() {
		if (session == null || transaction == null) {
			// Cannot close twice.
			return;
		}
		try {
			count--;
			if (transaction.getRollbackOnly()) {
				System.out.println("Transaction marked for rollback.");
				transaction.rollback();
			} else {
				transaction.commit();
			}
		} catch (PersistenceException e) {
			System.out.println(String.format("Cannot commit transaction (%s), rolling back.", e.getMessage()));
			transaction.rollback();
		}
		transaction = null;
		session = null;
	}

	public void flush() {
		session.flush();
		session.clear();
	}

	public Config getConfig() {
		return configuration;
	}

	public MapInfoDao getMapInfoDao() {
		return new MapInfoDao(session);
	}

	public AreaDao getAreaDao() {
		return new AreaDao(session);
	}

	public static void main(String[] args) {
		System.out.println("Cartexography");
	}
}
