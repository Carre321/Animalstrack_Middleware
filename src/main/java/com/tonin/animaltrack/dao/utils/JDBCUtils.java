package com.tonin.animaltrack.dao.utils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JDBCUtils {
    private static final Logger logger = LogManager.getLogger(JDBCUtils.class);

    public static Connection getConnection() {
        try {
            Properties configuration = configuration();
            String address = value("ANIMALSTRACK_DB_URL", "db.url", configuration);
            String username = value("ANIMALSTRACK_DB_USER", "db.user", configuration);
            String password = value("ANIMALSTRACK_DB_PASSWORD", "db.password", configuration);
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(address, username, password);
        } catch (Exception exception) {
            logger.error("No se pudo conectar. Revise la configuracion privada de AnimalsTrack.", exception);
            return null;
        }
    }

    private static Properties configuration() throws Exception {
        Properties configuration = new Properties();
        String location = System.getProperty("animalstrack.config");
        Path configurationFile = location == null
            ? Paths.get(System.getProperty("user.home"), ".animalstrack", "database.properties")
            : Paths.get(location);
        if (Files.isRegularFile(configurationFile)) {
            try (InputStream input = Files.newInputStream(configurationFile)) {
                configuration.load(input);
            }
        } else if (location == null) {
            try (InputStream input = JDBCUtils.class.getResourceAsStream("/database.properties")) {
                if (input != null) configuration.load(input);
            }
        }
        return configuration;
    }

    private static String value(String environmentName, String propertyName, Properties configuration) {
        String configured = System.getenv(environmentName);
        if (configured == null) configured = configuration.getProperty(propertyName);
        if (configured == null) throw new IllegalStateException("Falta configurar " + environmentName);
        return configured;
    }

    public static void close(ResultSet result, PreparedStatement statement) {
        try { if (result != null) result.close(); } catch (Exception exception) { logger.error(exception.getMessage(), exception); }
        try { if (statement != null) statement.close(); } catch (Exception exception) { logger.error(exception.getMessage(), exception); }
    }

    public static void close(Connection connection) {
        try { if (connection != null) connection.close(); } catch (Exception exception) { logger.error(exception.getMessage(), exception); }
    }

    public static void close(Connection connection, boolean commitOrRollback) {
        if (connection != null) {
            try {
                if (commitOrRollback) connection.commit();
                else connection.rollback();
            } catch (SQLException exception) {
                logger.error(exception.getMessage(), exception);
            }
            try { connection.close(); } catch (SQLException exception) { logger.warn(exception.getMessage(), exception); }
        }
    }
}
