/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.web;

import net.notasnark.cartexography.Cartexography;
import net.notasnark.cartexography.Main;
import org.quartz.SchedulerException;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.util.Set;

import static net.notasnark.cartexography.Config.getConfiguration;

public class Server extends Main {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    /**
     * Starts the web application server running. Configures the ticker event, sets up controllers
     * on the endpoints and then waits for connections.
     */
    public void startServer() {
        logger.info("== WorldGen AppServer ==");

        Spark.staticFileLocation("/public");
        Spark.port(getConfiguration().getHttpPort());

        try (Cartexography wg = Server.getApp()) {
            // Check that we are configured.

            Reflections reflections = new Reflections("net.notasnark.cartexography");
            Set<Class<? extends Controller>> controllers = reflections.getSubTypesOf(Controller.class);

            logger.debug("Started controller loading");
            for (Class controller : controllers) {
                logger.info("Adding controller [" + controller.getSimpleName() + "]");
                try {
                    Controller c = (Controller) controller.newInstance();
                    c.setupEndpoints();
                } catch (InstantiationException e) {
                    System.out.println("Failed to instantiate new controller (" + e.getMessage() + ")");
                } catch (IllegalAccessException e) {
                    System.out.println("Failed to access new controller (" + e.getMessage() + ")");
                }
            }
            logger.debug("Finished controller loading");

        } catch (Exception e) {

        }

    }

    /**
     * Defaults to http://localhost:4567/index
     */
    public static void main(String[] args) {
        Server server = new Server();

        System.out.println("Starting...");

        server.startServer();
    }
}
