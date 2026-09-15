package com.bankofcli.persistence;

import com.bankofcli.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {

    private static final String CONFIG_FILE = "db.properties";
    private static final ConnectionFactory connectionFactory = new ConnectionFactory();

    private final Properties props = new Properties();

    private ConnectionFactory() {
        try (InputStream input = ConnectionFactory.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IllegalStateException(CONFIG_FILE + " not found on classpath");
            }
            props.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
    }

    public static ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    props.getProperty("DB_URL"),
                    props.getProperty("DB_USER"),
                    props.getProperty("DB_PASSWORD"));
        } catch (SQLException e) {
            throw new DataAccessException("Could not connect to the database", e);
        }
    }
}
