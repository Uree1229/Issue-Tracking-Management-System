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
- `dto`와 `repository`는 기본 골격을 구현해 두었고, `application`, `ui`, `query`, `common` 패키지는 역할별 구현을 위해 비워 두었습니다.
- 엔티티 필드명과 DB 컬럼명은 맞춰 두었으므로, 서비스/리포지토리 구현 시 기존 매핑을 그대로 사용하는 것을 권장합니다.
- 상태 전이 로직은 엔티티 내부가 아니라 상위 서비스 계층에서 처리하는 전제를 유지합니다.

## DTO / Repository Guide

### FE에서 사용 가능한 영역

- `shared/dto/account`
- `shared/dto/project`
- `shared/dto/issue`

FE는 `Request DTO`를 만들어 상위 `facade/service`에 넘기고, `Response DTO`를 받아 화면에 표시하면 됩니다.  
FE는 `persistence/entity` 와 `persistence/repository`를 사용하지 않는 것을 권장합니다. (계층간 분리가 깨지기 때문에 발표때 태클 들어와요)

자주 쓰게 될 DTO 예시는 아래와 같습니다.

- 계정 생성: `AccountCreateRequest -> AccountResponse`
- 프로젝트 생성/수정: `ProjectCreateRequest`, `ProjectUpdateRequest -> ProjectResponse`
- 이슈 생성/수정: `IssueCreateRequest`, `IssueUpdateRequest -> IssueDetailResponse`
- 이슈 목록/검색: `IssueSearchCondition -> List<IssueSummaryResponse>`
- 댓글 등록: `CommentCreateRequest -> CommentResponse`

### BE에서 사용 가능한 영역

- `shared/dto/*`: 계층 간 입출력
- `persistence/entity/*`: 도메인 영속 객체
- `persistence/repository/*`: JPA 기반 DB 접근

현재 Repository는 `TransactionManager`가 제공하는 `EntityManager`로 생성해서 사용합니다.
트랜잭션 시작, 커밋, 롤백, `EntityManager` 종료는 `TransactionManager`가 담당합니다.

```java
TransactionManager.execute(entityManager -> {
    IssueRepository issueRepository = new IssueRepository(entityManager);
    var issues = issueRepository.findByProjectId(projectId);
    return issues;
});
```

### Repository API

공통적으로 모든 Repository는 아래 메서드를 가집니다.

- `findById(Long id)`
- `findAll()`
- `save(T entity)`
- `delete(T entity)`
- `deleteById(Long id)`

추가로 구현된 주요 조회 메서드는 아래와 같습니다.

- `AccountRepository`
  - `findByLoginId(String loginId)`
  - `findByEmail(String email)`
  - `findByRole(Role role)`
  - `findActiveAccounts()`
- `ProjectRepository`
  - `findByName(String name)`
  - `findByCreatedByAccountId(Long accountId)`
- `IssueRepository`
  - `findByProjectId(Long projectId)`
  - `findByReporterAccountId(Long accountId)`
  - `findByAssigneeAccountId(Long accountId)`
  - `findByStatus(IssueStatus status)`
  - `findByPriority(Priority priority)`
- `CommentRepository`
  - `findByIssueId(Long issueId)`
  - `findByAuthorAccountId(Long accountId)`
- `TagRepository`
  - `findByProjectId(Long projectId)`
  - `findByProjectIdAndName(Long projectId, String name)`
- `IssueHistoryRepository`
  - `findByIssueId(Long issueId)`
  - `findByChangedByAccountId(Long accountId)`
- `IssueDeltaRepository`
  - `findByHistoryId(Long historyId)`

### 추천 계층 흐름

권장 흐름은 아래와 같습니다.

`FE/UI -> Facade/Service -> Repository -> Entity`

`DTO`는 데이터 전달 전용이고, `Repository`는 DB 접근 전용입니다.  
즉, DTO가 Repository를 호출하는 구조로 만들지 않고, BE 서비스 계층이 DTO를 받아 Entity/Repository를 조합하는 방식으로 가는 것이 좋습니다.

### 추천 API 형태

아직 `service/facade`는 비어 있지만, 아래 같은 시그니처로 맞추면 FE와 BE가 연결하기 쉽습니다.

```java
AccountResponse createAccount(AccountCreateRequest request)
AccountResponse updateAccount(Long accountId, AccountUpdateRequest request)

ProjectResponse createProject(ProjectCreateRequest request)
ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request)

IssueDetailResponse createIssue(IssueCreateRequest request)
IssueDetailResponse updateIssue(Long issueId, IssueUpdateRequest request)
IssueDetailResponse getIssueDetail(Long issueId)
List<IssueSummaryResponse> searchIssues(IssueSearchCondition condition)

CommentResponse addComment(CommentCreateRequest request)
```
