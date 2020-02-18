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
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static Config configuration;

    private String databaseUrl;
    private String databaseUsername;
    private String databasePassword;

    private int    httpPort;

    public static synchronized Config getConfiguration() {
        if (configuration == null) {
            if (System.getProperty("cartexography.config") != null) {
                String configFile = System.getProperty("cartexography.config");
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
    private String getString(ResourceBundle bundle, String key) {
        try {
            String value = bundle.getString(key);
            if (value == null || value.trim().length() == 0) {
                throw new InvalidConfigurationException(key, value);
            }
            return value;
        } catch (MissingResourceException e) {
            throw new InvalidConfigurationException(key);
        }
    }

    private String getString(ResourceBundle bundle, String key, String value) {
        try {
            return getString(bundle, key);
        } catch (InvalidConfigurationException e) {
            return value;
        }
    }

    private int getInt(ResourceBundle bundle, String key) {
        String str = getString(bundle, key);
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException(key, str);
        }
    }

    private int getInt(ResourceBundle bundle, String key, int value) {
        String str = null;
        try {
            str = getString(bundle, key);
            return Integer.parseInt(str);
        } catch (InvalidConfigurationException e) {
            return value;
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException(key, str);
        }
    }

    private boolean getBoolean(ResourceBundle bundle, String key) {
        String str = getString(bundle, key);

        return Boolean.parseBoolean(str);
    }

    private boolean getBoolean(ResourceBundle bundle, String key, boolean value) {
        try {
            return getBoolean(bundle, key);
        } catch (InvalidConfigurationException e) {
            return value;
        }
    }

    private Config(ResourceBundle bundle) {
        Enumeration<String> keys =  bundle.getKeys();

        setDatabaseURL(getString(bundle, "database.url"));
        setDatabaseUsername(getString(bundle, "database.username"));
        setDatabasePassword(getString(bundle, "database.password"));

        setHttpPort(getInt(bundle, "server.port", 4567));
    }

    private void setDatabaseURL(String url) {
        if (url == null || url.trim().length() == 0) {
            throw new InvalidConfigurationException("database.url");
        }
        logger.info(String.format("DatabaseURL [%s]", url));
        this.databaseUrl = url;
    }

    /**
     * Gets the database URL to connect to.
     *
     * @return  Database URL.
     */
    public String getDatabaseURL() {
        return databaseUrl;
    }

    private void setDatabaseUsername(String username) {
        if (username == null || username.trim().length() == 0) {
            throw new InvalidConfigurationException("database.username");
        }
        logger.info(String.format("DatabaseUsername [%s]", username));
        this.databaseUsername = username;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    private void setDatabasePassword(String password) {
        if (password == null || password.length() == 0) {
            throw new InvalidConfigurationException("database.password");
        }
        logger.info(String.format("DatabasePassword [%s]", "********"));
        this.databasePassword = password;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    private void setHttpPort(int port) {
        if (port < 1) {
            throw new InvalidConfigurationException("Server Port must be greater than zero");
        }
        this.httpPort = port;
    }

    /**
     * Gets the port the HTTP server should listen on. This defaults to 4567.
     *
     * @return      HTTP Port the server listens on.
     */
    public int getHttpPort() {
        return httpPort;
    }

}
