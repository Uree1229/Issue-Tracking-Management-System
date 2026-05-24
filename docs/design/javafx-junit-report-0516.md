# JavaFX JUnit Test Notes

- **파일명:** `UiRoleTest.java`
- **@BeforeAll, @BeforeEach, @AfterAll 내용 설명:**  
  JavaFX 권한 제어 로직은 DB나 외부 리소스에 의존하지 않는 enum 기반 정책 검증이므로, 별도의 테스트 환경 초기화가 필요하지 않다. 따라서 `@BeforeAll`, `@BeforeEach`, `@AfterAll` 없이 각 역할의 권한 메서드 결과를 직접 검증하는 방식으로 테스트를 구성하였다.
- **테스트 케이스 1:** `TC-01`
- **테스트 케이스 이름:** 역할별 기능 접근 제어 테스트
- **JUnit 코드:**

```java
@Test
void adminOnlyHasAdministrativeAccess() {
    // Then: admin should stay on the administrative path only
    assertTrue(UiRole.ADMIN.isAdmin());
    assertFalse(UiRole.ADMIN.canCreateIssue());
    assertFalse(UiRole.ADMIN.canOpenIssueBrowser());
    assertFalse(UiRole.ADMIN.canOpenSearch());
    assertFalse(UiRole.ADMIN.canViewAnalytics());
}

@Test
void projectLeadHasFullIssueManagementCapabilities() {
    // Then: PL should be able to create, browse, search, and analyze issues
    assertFalse(UiRole.PL.isAdmin());
    assertTrue(UiRole.PL.canCreateIssue());
    assertTrue(UiRole.PL.canOpenIssueBrowser());
    assertTrue(UiRole.PL.canOpenSearch());
    assertTrue(UiRole.PL.canViewAnalytics());
}

@Test
void developerAndTesterPermissionsMatchUseCaseBoundaries() {
    // Then: DEV can browse/search only, while TESTER can also create issues
    assertFalse(UiRole.DEV.canCreateIssue());
    assertTrue(UiRole.DEV.canOpenIssueBrowser());
    assertTrue(UiRole.DEV.canOpenSearch());
    assertFalse(UiRole.DEV.canViewAnalytics());

    assertTrue(UiRole.TESTER.canCreateIssue());
    assertTrue(UiRole.TESTER.canOpenIssueBrowser());
    assertTrue(UiRole.TESTER.canOpenSearch());
    assertFalse(UiRole.TESTER.canViewAnalytics());
}
```

- **테스트 케이스를 만든 목적:**  
  기획 명세에서 가장 중요한 부분 중 하나는 역할(Role)에 따라 접근 가능한 화면과 기능이 달라진다는 점이다. 이 테스트는 JavaFX 화면 가시성의 기준이 되는 `UiRole` 정책이 use-case와 정확히 일치하는지 검증하기 위한 테스트이다. 이를 통해 ADMIN, PL, DEV, TESTER별 기능 구분이 잘못 적용되는 문제를 사전에 방지할 수 있다.


- **파일명:** `SearchQueryPayloadTest.java`
- **@BeforeAll, @BeforeEach, @AfterAll 내용 설명:**  
  Search payload 테스트는 검색 화면에서 입력한 조건이 JavaFX 내부 모델에 정확히 보존되는지 검증하는 단위 테스트이다. 외부 의존성 없이 객체 생성만으로 검증 가능하므로 별도의 초기화 어노테이션은 사용하지 않았다.
- **테스트 케이스 2:** `TC-02`
- **테스트 케이스 이름:** 이슈 검색 조건 payload 생성 테스트
- **JUnit 코드:**

```java
@Test
void keywordOnlyFactorySetsExpectedDefaults() {
    // Given: a quick search request that starts from the home/search shortcut
    SearchQueryPayload payload = SearchQueryPayload.keywordOnly("login");

    // Then: only the keyword is filled and the rest of the filters stay empty
    assertNull(payload.issueId());
    assertEquals("login", payload.keyword());
    assertNull(payload.status());
    assertNull(payload.priority());
    assertNull(payload.reporterAccountId());
    assertNull(payload.assigneeAccountId());
    assertNull(payload.projectId());
    assertFalse(payload.activeOnly());
    assertTrue(payload.includeDescription());
}

@Test
void constructorRetainsStructuredSearchCriteria() {
    // Given: a detailed search created from the advanced search form
    SearchQueryPayload payload = new SearchQueryPayload(
        15L,
        "assign",
        UiIssueStatus.ASSIGNED,
        UiPriority.MAJOR,
        3L,
        7L,
        100L,
        true,
        false
    );

    // Then: every structured filter should stay intact for backend handoff
    assertEquals(15L, payload.issueId());
    assertEquals("assign", payload.keyword());
    assertEquals(UiIssueStatus.ASSIGNED, payload.status());
    assertEquals(UiPriority.MAJOR, payload.priority());
    assertEquals(3L, payload.reporterAccountId());
    assertEquals(7L, payload.assigneeAccountId());
    assertEquals(100L, payload.projectId());
    assertTrue(payload.activeOnly());
    assertFalse(payload.includeDescription());
}
```

- **테스트 케이스를 만든 목적:**  
  Search 화면은 여러 입력 조건을 조합하여 백엔드에 전달하는 구조이므로, 사용자가 입력한 값이 payload에서 손실되거나 뒤섞이면 검색 결과 전체가 잘못될 수 있다. 이 테스트는 키워드 검색과 상세 필터 검색 모두에서 조건 값이 정확히 유지되는지 검증하여, UI와 백엔드 사이의 검색 계약이 깨지지 않도록 보장한다.


- **파일명:** `InquiryQueryPayloadTest.java`
- **@BeforeAll, @BeforeEach, @AfterAll 내용 설명:**  
  Inquiry 관련 테스트는 최근 이슈 목록을 조회하기 위한 payload의 기본값을 검증하는 테스트이다. 단순 객체 생성 로직만 확인하면 되므로 테스트 환경 준비를 위한 lifecycle 어노테이션은 사용하지 않았다.
- **테스트 케이스 3:** `TC-03`
- **테스트 케이스 이름:** 최근 이슈 상세 조회 payload 생성 테스트
- **JUnit 코드:**

```java
@Test
void recentFactoryBuildsRecentOnlyPayload() {
    // Given: the user opens the recent-issues inquiry flow
    InquiryQueryPayload payload = InquiryQueryPayload.recent();

    // Then: the payload should request recent issues without an explicit id
    assertNull(payload.issueId());
    assertEquals("", payload.keyword());
    assertTrue(payload.recentOnly());
}
```

- **테스트 케이스를 만든 목적:**  
  Search 결과에서 상세 정보 창을 여는 흐름에서는 “최근 이슈 보기” 요청이 자주 사용된다. 이 테스트는 최근 이슈 조회 전용 payload가 올바른 기본값으로 생성되는지 검증하여, 상세 조회 창이 잘못된 검색 조건으로 열리는 문제를 방지하기 위한 것이다.


- **파일명:** `UiModelMapperTest.java`
- **@BeforeAll, @BeforeEach, @AfterAll 내용 설명:**  
  이 테스트는 백엔드 DTO를 JavaFX 화면 모델로 변환하는 매퍼의 결과를 검증하는 순수 단위 테스트이다. DTO와 응답 값을 테스트 코드 내부에서 직접 생성하여 사용하므로, 별도 DB 초기화나 공통 세팅은 필요하지 않다.
- **테스트 케이스 4:** `TC-04`
- **테스트 케이스 이름:** JavaFX 화면 모델 매핑 및 타임라인 정렬 테스트
- **JUnit 코드:**

```java
@Test
void toAuthenticatedUserConvertsRoleAndIdentity() {
    // Given: an authenticated backend response for a PL account
    AccountResponse response = new AccountResponse(
        11L,
        "pl1",
        "Project Lead",
        "pl1@its.local",
        Role.PL,
        LocalDateTime.of(2026, 5, 8, 9, 0),
        true
    );

    AuthenticatedUser user = UiModelMapper.toAuthenticatedUser(response);

    // Then: JavaFX session data should keep the identity and role intact
    assertEquals(11L, user.accountId());
    assertEquals("pl1", user.loginId());
    assertEquals("Project Lead (pl1)", user.displayName());
    assertEquals(UiRole.PL, user.role());
}

@Test
void toAdminProjectRowModelSummarizesSortedTagsAndCreator() {
    // Given: a project response that contains several tag names in mixed order
    ProjectResponse response = new ProjectResponse(
        3L,
        "Proj3",
        "Tag-ready project",
        LocalDateTime.of(2026, 5, 16, 10, 0),
        1L,
        "admin",
        List.of(
            new TagResponse(10L, 3L, "ui", "ui tag"),
            new TagResponse(11L, 3L, "auth", "auth tag"),
            new TagResponse(12L, 3L, "api", "api tag")
        )
    );

    AdminProjectRowModel row = UiModelMapper.toAdminProjectRowModel(response);

    // Then: the admin table should show a stable tag summary and creator id
    assertEquals(3L, row.getProjectId());
    assertEquals("Proj3", row.getName());
    assertEquals("api, auth, ui", row.getDefaultTag());
    assertEquals("admin", row.getDefaultAssignee());
}

@Test
void toActivityTimelineSortsCommentsAndHistoryChronologically() {
    // Given: comments and history records arrive out of order
    IssueDetailResponse response = /* ... */;

    List<String> timeline = UiModelMapper.toActivityTimeline(response);

    // Then: the detail timeline should be merged and sorted by time
    assertEquals("[2026-05-08 09:05] Developer One: comment first", timeline.get(0));
    assertEquals("[2026-05-08 09:10] dev1: created the issue with status NEW.", timeline.get(1));
    assertEquals("[2026-05-08 09:12] dev1: changed status from NEW to ASSIGNED.", timeline.get(2));
    assertEquals("[2026-05-08 09:15] Tester One: comment later", timeline.get(3));
}
```

- **테스트 케이스를 만든 목적:**  
  JavaFX 화면은 백엔드 DTO를 그대로 쓰지 않고, 화면 표시용 모델로 한 번 변환한 뒤 사용한다. 이 테스트는 사용자 정보, 프로젝트 태그 요약, 이슈 상세 타임라인이 화면에 맞는 형식으로 정확히 변환되는지 검증하기 위한 것이다. 특히 댓글과 상태 변경 기록이 시간순으로 정렬되는지 확인함으로써, 상세 조회 화면의 가독성과 신뢰성을 보장한다.
