# ITS Project

## Requirements

- Java 17
- SQLite 3 CLI

## Build

macOS / Linux:

```bash
./gradlew clean build
```

Windows:

```bat
gradlew.bat clean build
```

## Database Initialization

첫 실행 전에 SQLite DB를 먼저 생성해야 합니다. 

macOS / Linux:

```bash
sqlite3 database/its.db < database/schema.sql
sqlite3 database/its.db < database/seed-data.sql
```

Windows `cmd`:

```bat
type database\schema.sql | sqlite3 database\its.db
type database\seed-data.sql | sqlite3 database\its.db
```

Windows PowerShell:

```powershell
Get-Content database/schema.sql | sqlite3 database/its.db
Get-Content database/seed-data.sql | sqlite3 database/its.db
```

## Run

JavaFX 버전 실행:
```bash
./gradlew runJavafx
```

Swing버전 실행:
```bash
./gradlew runSwing
```


## Default Admin

- loginId: `admin`
- password: `admin`

- admin 계정 이외의 시드 데이터는 없습니다.

## Configuration Files

- `database/its.db`: SQLite db 파일 (위 Database Initalization으로 직접 생성)
- `database/schema.sql`: SQLite 스키마 정의
- `database/seed-data.sql`: 기본 admin 계정 시드 데이터
- `src/main/resources/META-INF/persistence.xml`: JPA + Hibernate + SQLite 연결 설정 (DAO 파트)
- `src/main/resources/application.yml`: 애플리케이션 설정 요약

