package com.library.config;

import com.library.exception.DataAccessException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates JDBC connections. Settings are read from {@code db.properties} on the classpath
 * and can be overridden by the DB_URL, DB_USER and DB_PASSWORD environment variables.
 */
public final class DBConnection {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private static final Properties PROPS = load();

    private DBConnection() { }

    private static Properties load() {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read db.properties", e);
        }
        return props;
    }

    private static String setting(String envName, String propName, String fallback) {
        String env = System.getenv(envName);
        if (env != null && !env.isBlank()) {
            return env;
        }
        return PROPS.getProperty(propName, fallback);
    }

    public static Connection getConnection() {
        String url = setting("DB_URL", "db.url", DEFAULT_URL);
        String user = setting("DB_USER", "db.user", "root");
        String password = setting("DB_PASSWORD", "db.password", "");
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DataAccessException("Could not connect to the database. Check db.properties / DB_* "
                    + "environment variables and make sure MySQL is running.", e);
        }
    }
}
