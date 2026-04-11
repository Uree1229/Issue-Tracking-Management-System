# ITS Project

## Requirements

- Java 17
- SQLite 3 CLI
- Internet access on first Gradle Wrapper run

이 프로젝트는 Gradle Wrapper를 포함하고 있으므로 로컬에 Gradle을 따로 설치할 필요가 없습니다.

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

JPA 설정이 `validate` 모드이므로, 실행 전에 SQLite DB를 먼저 생성해야 합니다.

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

## 첫 실행 방법

1. Java 17을 설치합니다.
2. 저장소를 클론합니다.
3. SQLite CLI가 설치되어 있는지 확인합니다.
4. 위 명령으로 `database/its.db`를 초기화합니다.
5. Gradle Wrapper로 빌드합니다.

## Default Admin

- loginId: `admin`
- password: `admin`

## Project Layout

- `src/main/java/com/example/its/persistence/entity`: 실제 JPA 엔티티와 enum 구현 위치
- `database/its.db`: SQLite db 파일 (위 Database Initalization으로 직접 생성)
- `database/schema.sql`: SQLite 스키마 정의
- `database/seed-data.sql`: 기본 admin 계정 시드 데이터
- `src/main/resources/META-INF/persistence.xml`: JPA + Hibernate + SQLite 연결 설정 (DAO 파트)
- `src/main/resources/application.yml`: 애플리케이션 설정 요약

## Team Notes

- 현재 실제 구현이 들어간 영역은 `Gradle`, DB 설계, `persistence/entity` 입니다.
- `application`, `ui`, `repository`, `query`, `dto`, `common` 패키지는 역할별 구현을 위해 비워 두었습니다.
- 엔티티 필드명과 DB 컬럼명은 맞춰 두었으므로, 서비스/리포지토리 구현 시 기존 매핑을 그대로 사용하는 것을 권장합니다.
- 상태 전이 로직은 엔티티 내부가 아니라 상위 서비스 계층에서 처리하는 전제를 유지합니다.
