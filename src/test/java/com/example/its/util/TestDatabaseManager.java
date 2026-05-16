package com.example.its.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class TestDatabaseManager {
    private static final Path TEST_DATABASE_PATH = Path.of("build", "test-database", "its-test.db");
    private static final String TEST_DATABASE_URL = "jdbc:sqlite:" + toSqlitePath(TEST_DATABASE_PATH);
    private static final Path SCHEMA_SQL_PATH = Path.of("database", "schema.sql");
    private static final Path TEST_SEED_SQL_PATH = Path.of("src", "test", "resources", "database", "test-seed-data.sql");
    private static final String CLEAR_DATABASE_SQL = """
            PRAGMA foreign_keys = OFF;
            DELETE FROM issue_tags;
            DELETE FROM project_members;
            DELETE FROM issue_deltas;
            DELETE FROM issue_histories;
            DELETE FROM comments;
            DELETE FROM issues;
            DELETE FROM tags;
            DELETE FROM projects;
            DELETE FROM accounts;
            DELETE FROM sqlite_sequence;
            PRAGMA foreign_keys = ON;
            """;

    private TestDatabaseManager() {
    }

    // 테스트 DB 초기화 메서드
    // DB 파일은 유지하고 테이블 데이터만 삭제한 뒤 테스트 시드 데이터를 다시 삽입합니다.
    // 이미 열린 SQLite 연결이 있을 때 DB 파일을 삭제하면 SQLITE_READONLY_DBMOVED가 발생할 수 있습니다.
    public static void resetDatabase() {
        try {

            // 테스트 DB 파일과 관련된 디렉터리 생성
            Files.createDirectories(TEST_DATABASE_PATH.getParent());

            try (Connection connection = DriverManager.getConnection(TEST_DATABASE_URL)) {
                executeSql(connection, readSqlFile(SCHEMA_SQL_PATH));
                executeSql(connection, CLEAR_DATABASE_SQL);
                executeSql(connection, readSqlFile(TEST_SEED_SQL_PATH));
            }
        } catch (IOException | SQLException exception) {
            throw new IllegalStateException("테스트 DB 초기화에 실패했습니다.", exception);
        }
    }

    private static String readSqlFile(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static String toSqlitePath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private static void executeSql(Connection connection, String sql) throws SQLException {
        for (String statementSql : sql.split(";")) {
            String trimmedSql = statementSql.trim();
            if (!trimmedSql.isEmpty()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(trimmedSql);
                }
            }
        }
    }
}
