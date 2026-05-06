package com.example.its.ui.javafx.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class UiDatabaseBootstrap {

    private static final String JDBC_URL = "jdbc:sqlite:database/its.db";
    private static final Path SCHEMA_PATH = Path.of("database", "schema.sql");
    private static final Path SEED_PATH = Path.of("database", "seed-data.sql");

    private UiDatabaseBootstrap() {
    }

    public static void ensureInitialized() {
        try {
            Files.createDirectories(Path.of("database"));
            try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
                executeScript(connection, SCHEMA_PATH);
                executeScript(connection, SEED_PATH);
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

            try (Statement statement = connection.createStatement()) {
                statement.execute(trimmed);
            }
        }
    }
}
