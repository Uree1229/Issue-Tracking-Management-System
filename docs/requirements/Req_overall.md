# 회의안

레퍼런스: https://www.bugzilla.org/

기능 구현 시 애매한 명세는 Bugzilla 기준으로 정하기

## ITS가 반드시 지원할 기능 & 지원하지 않을 기능

(지원 기능)

- Account (CRUD)
    - Permission
        - r : Read
        - w : Write (update)
        
    - Role 종류 세분화(admin, PL, dev, tester)
        - admin
            - 모든 계정 create, read, update, delete
                - 계정을 생성 시에 Role을 지정 (admin, PL, dev, tester)
            - project create, read, update, delete
            - project에 계정 추가 가능.
            - 기본 admin 계정은 자동 생성 (아이디 admin, 비번 admin)
                - admin 계정은 admin 계정으로만 생성 가능
        - PL ← 모든 Issue rw
            - Inquiry (Issue*, 통계)
            - Search (Issue*)
            - Assignee 지정
            - 변경 (Priority, Status)
            - 추천 확인
        - dev ← 자신에게 Assigned 된 Issue  rw
            - 조회 (Isssue, 상세 조회)
            - 코멘트 추가
        - tester
            - 이슈 등록
            - 상세 조회
            - 코멘트 추가
    - 로그인 하지 않은 상태로 계정 생성 시에, PL/dev/tester 중 하나의 role을 지정할 수 있음.
        
        role은 절대 변경 불가능. (이때의 role은 어떤 프로젝트에서도 동일한 전역 role임.)
        
        - ITS의 account은 프로젝트별로 role을 자유롭게 지정할 수 **없음**. 하나의 계정은 생성될때부터 role이 고정됨.
- Project (Create, delete)
    - Project 내부에 Issue가 있음
    - Tag (개발자 추천용, 태그 기반 연관성 추천)

- Issue
    - Registration → Title, 
                              Description, 
                              reporter
                              report date, 
                              priority(blocker/critical/major/minor/trivial (기본값은 major)), 
                              state(new, assigned, fixed, resolved, closed, ~~reopened~~ (issue 첫 생성 시에는 지정 불가))
    - Bugzilla에서는 ‘resolution’이라는 resolved 하위 속성에 fixed가 속해 있지만, 저희 ITS에서는 fixed를 status로 놓고 resolution에 해당하는 정보는 Issue의 comment에 넣는 걸로 하겠습니다.
        - (resolution: 어떻게 resolved됐는지에 대한 설명, fixed/wontfix/delayed/duplicate 등)
    - Index ← “priority” Or “Date(create)”
    - Search ← 조건을 걸어 정보를 찾는 것
        - filter → status, reporter, priority, keyword
        - Detail Search
    - Inquiry ← Index를 눌러 있는 정보를 보는 것
        - Detail → 모든 필드 대상,  Comment History 열람
    - Issue state change (new → assigned → fixed → resolved → closed)
    - Issue statistics → 일/월별 발생수, 상태별 집계, 추세
    - Comment to Issue(댓글)
    - 이메일 알림
- 해결된 이슈 이력을 활용한 assignee 자동 추천 기능 (ML, text similarity, cosine 유사도 등등 이용 가능)
    - 총 3명 추천
- DB에 data 영속저장
    - 다른 User가 같은 데이터에 쿼리를 날릴때, 동시에 접근하는 것을 막지 않고 단순히 쿼리가 날아온 순서대로 DB 반영하도록.

(지원하지 않을 기능)

- 첨부 파일 업로드
- 위키/문서 협업
- Git/SVN 연동
- 이슈 삭제
- 커스텀 필드 무한 확장
- 실시간 다중 사용자 동시 편집

- 너희 팀의 확정 사항:
    - fixed는 **IssueStatus의 값**
    - ADMIN/PL/DEV/TESTER는 모두 **전역 Role**
    - resolution은 도입하지 않음
    - 지연, wontfix 등은 **comment로 표현**

- 유즈케이스
    
    [UseCase](https://www.notion.so/33aab1d125c7801ea8b1f1a725a517c0?pvs=21)
    

- 이슈의 상태 흐름 정하기(언제 new이고, 정확히 언제 assigned로 바뀌고, …)
    
    
    | Current Status | Event | Performer | Next Status | Role |
    | --- | --- | --- | --- | --- |
    | NULL | Issue Registeration | Tester/PL/Dev | new | reporter/date  |
    | new | Assign Assignee | PL | assigned | asignee (필수) |
    | assigned | Modify | Dev | fixed | comment (optional), fixer = Dev (assigned) |
    | fixed | Verify Success | Tester | resolved | reporter or tester |
    | fixed | Verify failure | Tester | reopened | comment why fail (필수) |
    | resolved | End | PL | closed | U |
    | closed | reopen/ | PL / Tester | reopened | comment why |
    | reopened | reassign | PL | assigned | assign (new or ol |

- GUI 프레임워크
    - **Java FX & Swing**
    
    MVC 아키텍처 패턴을 사용하여 UI와 모델을 분리하여 설계 구현해야 함.
    
     두 개 이상의 UI를 구현. 데모시에 UI를 제외한 나머지 코드들이 거의 수정 없이 재사용 되는 것을, 직접 컴파일 및 빌드를 통
    해 보여야 함.
    

JUnit으로 테스트 케이스 만들때 어떤걸 기준으로 테스트할지 정하기

- Status Change (Issue)
- Permission
- Search
- Inquiry
- Comment
- 영속성 ?
- Statisics
- Recommand
- 자동 필드 (ex_ Issue report 할 때, reportedDate, reporter 값이 자동적으로  채워져야 함

깃허브 브랜치 전략

천지민 의견) BE, FE 1, FE 2, DAO가 Git에서 Conflict 나지않게 아예 물리적으로 폴더를 나눠버렸으면 좋겠습니다. (모두가 자기 폴더에서만 작업, 자기 폴더만 Commit) 

단, DAO 부분을 누가 맡을지에 대해서는 저랑 정우님이랑 한번 말씀나눠보면 좋을것같습니다! (보통은 Devops가 맡는게 맞긴 한데, 지금 정우님이 프로젝트 전체 총괄하시느라고 여러가지 정하셔야할 것들이 많으니 부담이되실것같아, 구현체(도메인 등)만 정해주시면 DAO+BE 계층을 제가 맡아도 괜찮습니다. 다만 이렇게되면 발표할 때 역할분담 어떻게했나요 하시면 좀 말씀하실 때 역할이 없어보일 순 있습니다 ㅎㅎ..)

→ DAO까지는 정우님이 해주시기로 결정. 

깃 브랜치

- main
- develop
    - BE
    - FE1
    - FE2
    - DAO

DAO

- DBMS는 단일파일 Sqlite를 사용하면 어떨까요? (프로젝트 상 경로 고정 필요)