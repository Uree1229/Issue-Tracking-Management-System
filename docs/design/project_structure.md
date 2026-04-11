its-project/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── README.md
├── .gitignore
│
├── docs/
│   ├── requirements/
│   ├── design/
│   └── test/
│
├── database/
│   ├── its.db
│   ├── schema.sql
│   └── seed-data.sql
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/its/
│   │   │       ├── ItsApplication.java
│   │   │       │
│   │   │       ├── common/
│   │   │       │   ├── config/
│   │   │       │   ├── exception/
│   │   │       │   └── util/
│   │   │       │
│   │   │       ├── shared/
│   │   │       │   └── dto/
│   │   │       │       ├── account/
│   │   │       │       │   ├── AccountCreateRequest.java
│   │   │       │       │   ├── AccountUpdateRequest.java
│   │   │       │       │   └── AccountResponse.java
│   │   │       │       ├── project/
│   │   │       │       │   ├── ProjectCreateRequest.java
│   │   │       │       │   ├── ProjectUpdateRequest.java
│   │   │       │       │   └── ProjectResponse.java
│   │   │       │       └── issue/
│   │   │       │           ├── IssueCreateRequest.java
│   │   │       │           ├── IssueUpdateRequest.java
│   │   │       │           ├── IssueSearchCondition.java
│   │   │       │           ├── IssueDetailResponse.java
│   │   │       │           ├── IssueSummaryResponse.java
│   │   │       │           ├── CommentCreateRequest.java
│   │   │       │           ├── CommentResponse.java
│   │   │       │           ├── StatisticsResponse.java
│   │   │       │           └── RecommendationResponse.java
│   │   │       │
│   │   │       ├── persistence/
│   │   │       │   ├── entity/
│   │   │       │   │   ├── Account.java
│   │   │       │   │   ├── Role.java
│   │   │       │   │   ├── Project.java
│   │   │       │   │   ├── Issue.java
│   │   │       │   │   ├── Comment.java
│   │   │       │   │   ├── Tag.java
│   │   │       │   │   ├── IssueHistory.java
│   │   │       │   │   ├── IssueDelta.java
│   │   │       │   │   ├── IssueStatus.java
│   │   │       │   │   └── Priority.java
│   │   │       │   ├── repository/
│   │   │       │   │   ├── AccountRepository.java
│   │   │       │   │   ├── ProjectRepository.java
│   │   │       │   │   ├── IssueRepository.java
│   │   │       │   │   ├── CommentRepository.java
│   │   │       │   │   ├── TagRepository.java
│   │   │       │   │   ├── IssueHistoryRepository.java
│   │   │       │   │   └── IssueDeltaRepository.java
│   │   │       │   └── query/
│   │   │       │       ├── IssueQueryRepository.java
│   │   │       │       └── StatisticsQueryRepository.java
│   │   │       │
│   │   │       ├── application/
│   │   │       │   ├── service/
│   │   │       │   │   ├── AccountService.java
│   │   │       │   │   ├── ProjectService.java
│   │   │       │   │   ├── IssueService.java
│   │   │       │   │   ├── IssueSearchService.java
│   │   │       │   │   ├── IssueStatisticsService.java
│   │   │       │   │   └── AssigneeRecommendationService.java
│   │   │       │   ├── facade/
│   │   │       │   │   ├── AccountFacade.java
│   │   │       │   │   ├── ProjectFacade.java
│   │   │       │   │   └── IssueFacade.java
│   │   │       │   └── mapper/
│   │   │       │       ├── AccountMapper.java
│   │   │       │       ├── ProjectMapper.java
│   │   │       │       └── IssueMapper.java
│   │   │       │
│   │   │       └── ui/
│   │   │           ├── swing/
│   │   │           │   ├── controller/
│   │   │           │   ├── view/
│   │   │           │   └── model/
│   │   │           └── javafx/
│   │   │               ├── controller/
│   │   │               ├── view/
│   │   │               └── fxml/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── fxml/
│   │
│   └── test/
│       ├── java/
│       │   └── com/example/its/
│       │       ├── persistence/
│       │       │   └── repository/
│       │       ├── application/
│       │       │   ├── service/
│       │       │   └── mapper/
│       │       └── ui/
│       └── resources/
│           └── application-test.yml
│
└── scripts/
    ├── run-swing.sh
    ├── run-javafx.sh
    ├── init-db.sh
    └── backup-db.sh
