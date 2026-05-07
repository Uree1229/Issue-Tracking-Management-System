package com.example.its.ui.javafx.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class UiDatabaseBootstrap {

    private static final String JDBC_URL = "jdbc:sqlite:database/its.db?busy_timeout=5000&journal_mode=WAL";
    private static final Path DATABASE_PATH = Path.of("database", "its.db");
    private static final Path SCHEMA_PATH = Path.of("database", "schema.sql");
    private static final Path SEED_PATH = Path.of("database", "seed-data.sql");

    private UiDatabaseBootstrap() {
    }

    public static void ensureInitialized() {
        try {
            Files.createDirectories(Path.of("database"));
            if (Files.exists(DATABASE_PATH) && Files.size(DATABASE_PATH) > 0L) {
                return;
            }

            try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
                connection.setAutoCommit(false);
                executeStatement(connection, "PRAGMA journal_mode=WAL");
                executeStatement(connection, "PRAGMA busy_timeout=5000");
                executeScript(connection, SCHEMA_PATH);
                executeScript(connection, SEED_PATH);
                connection.commit();
            }
        } catch (IOException | SQLException exception) {
            throw new IllegalStateException("Failed to initialize the local ITS database.", exception);
        }
    }

    private static void executeScript(Connection connection, Path scriptPath) throws IOException, SQLException {
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("SQL script is missing: " + scriptPath);
        }

        String sql = Files.readString(scriptPath, StandardCharsets.UTF_8);
        for (String statementText : sql.split(";")) {
            String trimmed = statementText.trim();
            if (trimmed.isBlank()) {
                continue;
            }

            executeStatement(connection, trimmed);
        }
    }

    private static void executeStatement(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            boolean hasResultSet = statement.execute(sql);
            if (hasResultSet) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    while (resultSet.next()) {
                        // Consume pragma/query rows so SQLite can finish the statement cleanly.
                    }
                }
            }
        }
    }
}
