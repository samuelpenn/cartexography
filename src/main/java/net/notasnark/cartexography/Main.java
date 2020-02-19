/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography;

import net.notasnark.cartexography.map.info.MapInfoDao;
import net.notasnark.cartexography.map.info.MapInfo;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final SessionFactory sessionFactory;
    private static final Config configuration;

    static {
        configuration = Config.getConfiguration();

        System.out.println("Connecting to " + configuration.getDatabaseURL());

        Configuration cfg = new Configuration();
        cfg.configure("hibernate.cfg.xml");
        cfg.getProperties().setProperty("hibernate.connection.url", configuration.getDatabaseURL());
        cfg.getProperties().setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect");
        cfg.getProperties().setProperty("hibernate.connection.username", configuration.getDatabaseUsername());
        cfg.getProperties().setProperty("hibernate.connection.password", configuration.getDatabasePassword());

        sessionFactory = cfg.buildSessionFactory();
    }

    protected static EntityManager getSession() {
        return sessionFactory.createEntityManager();
    }

    public synchronized static Cartexography getApp() {
        return new Cartexography(getSession(), configuration);
    }

    public static void main(String[] args) {
        MapInfoDao dao = new MapInfoDao(sessionFactory.createEntityManager());
        List<MapInfo> maps = dao.getAll();

        System.out.println("Maps:");
        for (MapInfo map : maps) {
            System.out.println(map.getName());
        }
    }
}
