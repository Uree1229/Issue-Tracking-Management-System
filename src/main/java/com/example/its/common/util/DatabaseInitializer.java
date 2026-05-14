package com.example.its.common.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private static final Path DATABASE_PATH = Path.of("database", "its.db");
    private static final String DATABASE_URL = "jdbc:sqlite:" + toSqlitePath(DATABASE_PATH);
    private static final Path SCHEMA_SQL_PATH = Path.of("database", "schema.sql");
    private static final Path SEED_SQL_PATH = Path.of("database", "seed-data.sql");

    private DatabaseInitializer() {
    }

    /**
     * 프로그램 실행 시 DB를 새로 만들고 기본 seed data를 넣을 때 호출합니다.
     *
     * 사용 예:
     * <pre>
     * public static void main(String[] args) {
     *     DatabaseInitializer.resetAndSeed();
     * }
     * </pre>
     *
     * 실행 내용:
     * 1. database/its.db 파일과 SQLite WAL 관련 파일을 삭제합니다.
     * 2. database/schema.sql을 실행해 테이블을 생성합니다.
     * 3. database/seed-data.sql을 실행해 기본 데이터를 넣습니다.
     *
     * 주의: 실행할 때마다 기존 database/its.db 데이터가 모두 초기화됩니다.
     */
    public static void resetDatabaseAndSeed() {
        try {
            Files.createDirectories(DATABASE_PATH.getParent());
            deleteDatabaseFiles();

            try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
                executeSql(connection, readSqlFile(SCHEMA_SQL_PATH));
                executeSql(connection, readSqlFile(SEED_SQL_PATH));
            }
        } catch (IOException | SQLException exception) {
            throw new IllegalStateException("DB 초기화 및 seed data 삽입에 실패했습니다.", exception);
        }
    }

    private static void deleteDatabaseFiles() throws IOException {
        Files.deleteIfExists(DATABASE_PATH);
        Files.deleteIfExists(Path.of(DATABASE_PATH + "-shm"));
        Files.deleteIfExists(Path.of(DATABASE_PATH + "-wal"));
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
