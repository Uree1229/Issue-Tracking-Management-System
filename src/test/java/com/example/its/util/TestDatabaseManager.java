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
    private static final String TEST_DATABASE_URL = "jdbc:sqlite:" + TEST_DATABASE_PATH;
    private static final Path SCHEMA_SQL_PATH = Path.of("database", "schema.sql");
    private static final Path TEST_SEED_SQL_PATH = Path.of("src", "test", "resources", "database", "test-seed-data.sql");

    private TestDatabaseManager() {
    }

    // 테스트 DB 초기화 메서드
    // 기존 테스트 DB 파일과 관련된 파일들을 삭제하고, 새로운 테스트 DB를 생성하여 초기화
    // @BeforeEach에서 호출해야 합니다.
    public static void resetDatabase() {
        try {

            // 테스트 DB 파일과 관련된 디렉터리 생성
            Files.createDirectories(TEST_DATABASE_PATH.getParent());

            // 기존 테스트 DB 파일과 관련된 파일들 삭제
            Files.deleteIfExists(TEST_DATABASE_PATH);
            Files.deleteIfExists(Path.of(TEST_DATABASE_PATH + "-shm"));
            Files.deleteIfExists(Path.of(TEST_DATABASE_PATH + "-wal"));

            // 새로운 테스트 DB 생성
            try (Connection connection = DriverManager.getConnection(TEST_DATABASE_URL)) {
                executeSql(connection, readSqlFile(SCHEMA_SQL_PATH));
                executeSql(connection, readSqlFile(TEST_SEED_SQL_PATH));
            }
        } catch (IOException | SQLException exception) {
            throw new IllegalStateException("테스트 DB 초기화에 실패했습니다.", exception);
        }
    }

    private static String readSqlFile(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
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
