/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static Config configuration;


    public static synchronized Config getConfiguration() {
        if (configuration == null) {
            if (System.getProperty("worldgen.config") != null) {
                String configFile = System.getProperty("worldgen.config");
                logger.info(String.format("Reading configuration from [%s]", configFile));

                try (FileInputStream fis = new FileInputStream(configFile)) {
                    configuration = new Config(new PropertyResourceBundle(fis));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                ResourceBundle bundle = ResourceBundle.getBundle("cartexography");
                configuration = new Config(bundle);
            }
        }
        return configuration;
    }

    private Config(ResourceBundle bundle) {
    }

    public String getDatabaseURL() {
        return null;
    }

    public String getDatabaseUsername() {
        return null;
    }

    public String getDatabasePassword() {
        return null;
    }
}
