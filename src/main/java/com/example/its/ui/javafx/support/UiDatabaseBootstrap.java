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
            boolean databaseAlreadyExists = Files.exists(DATABASE_PATH) && Files.size(DATABASE_PATH) > 0L;

            try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
                connection.setAutoCommit(false);
                executeStatement(connection, "PRAGMA journal_mode=WAL");
                executeStatement(connection, "PRAGMA busy_timeout=5000");
                if (!databaseAlreadyExists) {
                    executeScript(connection, SCHEMA_PATH);
                    executeScript(connection, SEED_PATH);
                }
                applyProjectMembershipMigration(connection);
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

    private static void applyProjectMembershipMigration(Connection connection) throws SQLException {
        executeStatement(connection, """
            CREATE TABLE IF NOT EXISTS project_members (
                project_member_id INTEGER PRIMARY KEY AUTOINCREMENT,
                project_id INTEGER NOT NULL,
                account_id INTEGER NOT NULL,
                assigned_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT uq_project_members_project_account UNIQUE (project_id, account_id),
                CONSTRAINT fk_project_members_project
                    FOREIGN KEY (project_id) REFERENCES projects(project_id)
                    ON DELETE CASCADE,
                CONSTRAINT fk_project_members_account
                    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
                    ON DELETE CASCADE
            )
            """);
        executeStatement(connection, "CREATE INDEX IF NOT EXISTS idx_project_members_account ON project_members (account_id)");
        executeStatement(connection, "CREATE INDEX IF NOT EXISTS idx_project_members_project ON project_members (project_id)");

        // Backfill memberships for existing local data so seeded and in-progress projects remain reachable.
        executeStatement(connection, """
            INSERT OR IGNORE INTO project_members (project_id, account_id, assigned_at)
            SELECT p.project_id, p.created_by_account_id, COALESCE(p.created_at, CURRENT_TIMESTAMP)
            FROM projects p
            """);
        executeStatement(connection, """
            INSERT OR IGNORE INTO project_members (project_id, account_id, assigned_at)
            SELECT DISTINCT i.project_id, i.reporter_account_id, COALESCE(i.reported_at, CURRENT_TIMESTAMP)
            FROM issues i
            WHERE i.reporter_account_id IS NOT NULL
            """);
        executeStatement(connection, """
            INSERT OR IGNORE INTO project_members (project_id, account_id, assigned_at)
            SELECT DISTINCT i.project_id, i.assignee_account_id, COALESCE(i.last_modified_at, CURRENT_TIMESTAMP)
            FROM issues i
            WHERE i.assignee_account_id IS NOT NULL
            """);
        executeStatement(connection, """
            INSERT OR IGNORE INTO project_members (project_id, account_id, assigned_at)
            SELECT DISTINCT i.project_id, i.fixer_account_id, COALESCE(i.last_modified_at, CURRENT_TIMESTAMP)
            FROM issues i
            WHERE i.fixer_account_id IS NOT NULL
            """);
    }
}
