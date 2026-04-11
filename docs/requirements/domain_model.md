# Account

- accountId
- loginId
- password
- name
- email
- role : ADMIN | PL | DEV | TESTER
- createdAt
- isActive

### 기능

### 책임

- 시스템에 로그인 가능한 사용자 표현
- Issue의 reporter / assignee / fixer / comment author가 됨
- 권한 판단의 기준이 됨

# Project

이슈가 소속되는 작업 단위.

### **속성**

- projectId
- name
- description
- createdAt
- createdBy : Account

### **책임**

- Issue들의 소속 컨텍스트 제공
- Tag들의 소속 범위 제공
- 특정 프로젝트 기준 검색/통계/추천의 단위가 됨

# **Issue**

### **속성 (과제 필수 필드 포함)**

- issueId
- title : not null
- description : not null
- status : IssueStatus
- priority : Priority
- reporter : Account
- assignee : Account?
- fixer : Account?
- Comments: list[Comment] ?
- reportedAt
- lastModifiedAt
- project: Project

### **책임**

- 이슈의 상태 보유
- 담당자 배정
- 등록자/수정자 추적
- 코멘트와 태그를 포함하는 중심 객체
- 상태 전이 규칙 보장

### 비고

- Issue의 변경 내역을 보려면 IssueHistory와 IssueDelta를 함께 참조하면 됨.
- Issue 등록은 PL, Dev, Tester 아무나 가능 (과제 시나리오에서는 Tester가 issue등록하는 예시 나옴)

# Comment

Issue에 누적되는 대화/기록/댓글

과제 명세에 코멘트는 추가 순서대로 누적되고 날짜를 포함해야 한다고 되어 있으므로 독립 엔티티가 적절하다.

### **속성**

- commentId
- content
- createdAt
- author : Account
- issue : Issue

### **책임**

- 이슈에 대한 설명, 진행상황, 검증 결과, 지연 사유 기록
- wontfix, 지연, 재현 안 됨 같은 부가 판단을 텍스트로 남김 (bugzilla의 resolution기능을 comment에 적는 것으로 대체)

### **비고**

resolution(이슈가 해결된 방향, wontfix나 delay 등)을 도입하지 않기로 했기 때문에,

이제 Comment는 단순 부속 정보가 아니라 **보조 의사결정 기록 수단** 역할도 하게 됩니다

예를 들어, 심각한 문제로 인해 프로젝트 완성 기한 내에 고칠 수 없는 버그가 있다면

dev는 ‘fixed’로 지정 후, comment에 “fixed: 버그 수정 불가능. 사유: ~~” 와 같은 형식으로 작성해야 합니다.

# **Tag**

이슈 연관성 및 추천을 위한 분류 수단.

### **속성**

- tagId
- name
- description
- project : Project

### **책임**

- 이슈 분류
- 검색 보조
- 추천 알고리즘의 feature 역할

### **관계**

- 하나의 Project는 여러 Tag를 가질 수 있음
- 하나의 Issue는 여러 Tag를 가질 수 있음

즉:

- Project 1 --- * Tag
- Issue * --- * Tag

# **IssueHistory**

이슈 변경 이력

### **속성**

- historyId
- issue: Issue
- issueDelta : IssueDelta
- changedBy : Account
- changedAt
- getDelta() : 내부에서 IssueDelta 객체에 접근하여 변경 내용만

### **책임**

- 상태 전이 기록 보존
- 감사(audit) 로그 역할
- 통계/분석 보조

### 비고

Issue 1 ——— * IssueHistory

# **IssueDelta**

이슈 변경 내용

### **속성**

- deltaId
- issueHistory: IssueHistory
- title: [oldTitle, NewTitle]?
- content: [oldContent, newContent]?
- priority: [oldPriority, newPriority]?
- status: [oldIssueStatus, newIssueStatus]?
- getTitleDelta() :  제목 변경내용(전후) 불러옴, 없으면 null 리턴
- getContentDelta() : 내용 변경내용(전후) 불러옴, 없으면 null 리턴
- getPriorityDelta() : 우선순위 변경내용(전후) 불러옴, 없으면 null 리턴
- getStatusDelta() : 상태 변경내용(전후) 불러옴, 없으면 null 리턴

# 상태 전이 표

| 현재 status | 발생 이벤트 | 행위자 | **다음 Status** |
| --- | --- | --- | --- |
| NULL | Issue Registration | Tester / PL / Dev | NEW |
| NEW | Assign Assignee | PL | ASSIGNED |
| ASSIGNED | Modify / Mark Fixed | Dev | FIXED |
| FIXED | Verify Success | Tester | RESOLVED |
| FIXED | Verify Failure | Tester | REOPENED |
| RESOLVED | Close Issue | PL | CLOSED |
| CLOSED | Reopen Issue | PL / Tester | REOPENED |
| REOPENED | Reassign | PL | ASSIGNED |

### 상태 전이 관련 함수 - Issue 외부에서 구현 (IssueService같은 상위 클래스)

모든 상태 전이 함수는 IssueHistory와 그에 연결된 IssueDelta를 생성하고 저장해야 합니다.

**registerIssue()**

- 수행자: TESTER / PL / DEV
- 입력 필수값: title, description
- 효과:
    - reporter = 현재 사용자
    - reportedAt = 현재 시각
    - status = NEW
    - priority가 없으면 MAJOR
- 추가 가능:
    - 첫 comment 작성

---

### 상태 전이 관련 함수 - Issue 객체 내부 메서드

**assignAssignee()**

- 수행자: PL
- 선행 조건:
    - 현재 상태가 NEW
    - assignee의 role은 DEV
- 효과:
    - assignee 설정
    - status = ASSIGNED
    - 상태 이력 추가
    - 필요한 경우 comment 추가

---

**markFixed()**

- 수행자: DEV
- 선행 조건:
    - 현재 상태가 ASSIGNED
    - 수행자가 assignee 자신
- 효과:
    - status = FIXED
    - fixer = 수행자
    - fixedAt 기록
    - 상태 이력 추가

---

**verifyResolved()**

- 수행자: TESTER
- 선행 조건:
    - 현재 상태가 FIXED
- 효과:
    - status = RESOLVED
    - resolvedAt 기록
    - 상태 이력 추가

---

**verifyFailed()**

- 수행자: TESTER
- 선행 조건:
    - 현재 상태가 FIXED
- 효과:
    - status = REOPENED
    - 검증 실패 내용을 comment로 남김
    - 상태 이력 추가

---

**closeIssue()**

- 수행자: PL
- 선행 조건:
    - 현재 상태가 RESOLVED
- 효과:
    - status = CLOSED
    - closedAt 기록
    - 상태 이력 추가

---

**reOpenIssue()**

- 수행자: PL 또는 TESTER
- 선행 조건:
    - 현재 상태가 CLOSED
- 효과:
    - status = REOPENED
    - 재오픈 사유 comment 가능
    - 상태 이력 추가

---

**reAssignAssignee()**

- 수행자: PL
- 선행 조건:
    - 현재 상태가 REOPENED
- 효과:
    - 새 assignee 지정
    - status = ASSIGNED
    - 상태 이력 추가
