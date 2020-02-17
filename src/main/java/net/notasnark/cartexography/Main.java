/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final SessionFactory sessionFactory;
    private static final Config configuration;

    static {
        configuration = Config.getConfiguration();

        Configuration cfg = new Configuration();
        cfg.configure("hibernate.cfg.xml");
        cfg.getProperties().setProperty("hibernate.connection.url", configuration.getDatabaseURL());
        cfg.getProperties().setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect");
        cfg.getProperties().setProperty("hibernate.connection.username", configuration.getDatabaseUsername());
        cfg.getProperties().setProperty("hibernate.connection.password", configuration.getDatabasePassword());

        sessionFactory = cfg.buildSessionFactory();
    }

    public static void main(String[] args) {

    }
}
