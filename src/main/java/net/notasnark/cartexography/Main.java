/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography;

import net.notasnark.cartexography.map.area.Area;
import net.notasnark.cartexography.map.area.AreaDao;
import net.notasnark.cartexography.map.area.Bounds;
import net.notasnark.cartexography.map.info.MapInfoDao;
import net.notasnark.cartexography.map.info.MapInfo;
import net.notasnark.cartexography.map.path.Path;
import net.notasnark.cartexography.map.path.PathDao;
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
        testMaps();
        //testAreas();
        testPaths();

    }

    private static void testMaps() {
        System.out.println("\n---- Maps ----\n");
        MapInfoDao dao = new MapInfoDao(sessionFactory.createEntityManager());
        List<MapInfo> maps = dao.getAll();

        System.out.println("Maps:");
        for (MapInfo map : maps) {
            System.out.println(map.getName());
        }
    }

    private static void testAreas() {
        System.out.println("\n---- Areas ----\n");
        AreaDao areaDao = new AreaDao(sessionFactory.createEntityManager());
        Area a = areaDao.get(793);
        System.out.println(a.getTitle());
        Bounds b = areaDao.getBounds(a);
        System.out.println(b.minX);
        System.out.println(b.maxX);
        System.out.println(b.minY);
        System.out.println(b.maxY);
    }

    private static void testPaths() {
        System.out.println("\n---- Paths ----\n");
        PathDao pathDao = new PathDao(sessionFactory.createEntityManager());

        Path p = pathDao.get(322);
        System.out.println(p.getName());
        System.out.println("Vertices: " + p.getVertices().size());


    }
}
