package com.example.its.application.service;

import com.example.its.application.mapper.IssueMapper;
import com.example.its.persistence.entity.*;
import com.example.its.persistence.repository.*;
import com.example.its.persistence.transaction.TransactionManager;
import com.example.its.shared.dto.issue.*;

import java.util.List;

public class IssueService {

    private final IssueMapper issueMapper;

    public IssueService() {
        this(new IssueMapper());
    }

    public IssueService(IssueMapper issueMapper) {
        this.issueMapper = issueMapper;
    }

    // 1. 이슈 생성 (Register)
    public IssueDetailResponse registerIssue(IssueCreateRequest request) {
        return TransactionManager.execute(entityManager -> {
            IssueRepository issueRepository = new IssueRepository(entityManager);
            AccountRepository accountRepository = new AccountRepository(entityManager);
            ProjectRepository projectRepository = new ProjectRepository(entityManager);
            TagRepository tagRepository = new TagRepository(entityManager);

            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
            Account reporter = accountRepository.findById(request.getReporterAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 등록자입니다."));

            Issue issue = Issue.create(
                    request.getTitle(),
                    request.getDescription(),
                    request.getPriority(), // 내부에서 null 체크 후 Priority.MAJOR 세팅됨
                    project,
                    reporter
            );

            // 태그 처리
            if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
                for (Long tagId : request.getTagIds()) {
                    Tag tag = tagRepository.findById(tagId)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그 ID: " + tagId));
                    issue.addTag(tag);
                }
            }

            // 생성 시점에는 이전 상태(oldState)가 없으므로 null 전달
            recordHistory(reporter, null, issue);

            Issue savedIssue = issueRepository.save(issue);
            return issueMapper.toDetailResponse(savedIssue);
        });
    }

    // ------------------------------------------------------------- 2. 도메인 룰 기반 상태 전이 로직 ------------------------------------------------------------- //
    // [상태 전이: NEW, REOPENED -> ASSIGNED]
    // 수행자: PL / 조건: Assignee가 DEV 역할이어야 함
    public IssueDetailResponse assignAssignee(IssueAssignRequest request) {
        return TransactionManager.execute(entityManager -> {
            IssueRepository issueRepository = new IssueRepository(entityManager);
            AccountRepository accountRepository = new AccountRepository(entityManager);

            Issue issue = getIssueOrThrow(issueRepository, request.getIssueId());
            Account pl = getAccountOrThrow(accountRepository, request.getPlAccountId());
            Account assignee = getAccountOrThrow(accountRepository, request.getAssigneeAccountId());

            if (pl.getRole() != Role.PL) {
                throw new SecurityException("이슈 할당은 PL(Project Leader)만 가능합니다.");
            }
            if (issue.getStatus() != IssueStatus.NEW && issue.getStatus() != IssueStatus.REOPENED) {
                throw new IllegalStateException("NEW 또는 REOPENED 상태에서만 할당이 가능합니다.");
            }
            if (assignee.getRole() != Role.DEV) {
                throw new IllegalArgumentException("담당자는 DEV(Developer) 역할이어야 합니다.");
            }

            // 1. 상태 변경 전 스냅샷 캡처
            IssueSnapshot oldState = new IssueSnapshot(issue);

            // 2. 상태 변경
            issue.setAssignee(assignee);
            issue.setStatus(IssueStatus.ASSIGNED);

            // 3. 이력 기록 (oldState vs 현재 issue)
            recordHistory(pl, oldState, issue);
            return issueMapper.toDetailResponse(issueRepository.save(issue));
        });
    }

    // [상태 전이: ASSIGNED -> FIXED]
    // 수행자: DEV (본인) / 조건: 필수 코멘트(사유) 작성
    public IssueDetailResponse markFixed(IssueFixRequest request) {
        return TransactionManager.execute(entityManager -> {
            IssueRepository issueRepository = new IssueRepository(entityManager);
            AccountRepository accountRepository = new AccountRepository(entityManager);

            Issue issue = getIssueOrThrow(issueRepository, request.getIssueId());
            Account dev = getAccountOrThrow(accountRepository, request.getDevAccountId());

            if (issue.getStatus() != IssueStatus.ASSIGNED) {
                throw new IllegalStateException("ASSIGNED 상태에서만 FIXED로 변경할 수 있습니다.");
            }
            if (issue.getAssignee() == null || !issue.getAssignee().getAccountId().equals(request.getDevAccountId())) {
                throw new SecurityException("본인에게 할당된 이슈만 완료(FIXED) 처리할 수 있습니다.");
            }
            if (request.getComment() == null || request.getComment().trim().isEmpty()) {
                throw new IllegalArgumentException("FIXED 처리 시 코멘트 작성이 필수입니다.");
            }

            IssueSnapshot oldState = new IssueSnapshot(issue);

            issue.setStatus(IssueStatus.FIXED);
            issue.setFixer(dev);
            addCommentToIssue(issue, dev, request.getComment());

            recordHistory(dev, oldState, issue);
            return issueMapper.toDetailResponse(issueRepository.save(issue));
        });
    }

    // [상태 전이: FIXED -> RESOLVED]
    // 수행자: TESTER
    public IssueDetailResponse verifyResolved(IssueResolveRequest request) {
        return TransactionManager.execute(entityManager -> {
            IssueRepository issueRepository = new IssueRepository(entityManager);
            AccountRepository accountRepository = new AccountRepository(entityManager);

            Issue issue = getIssueOrThrow(issueRepository, request.getIssueId());
            Account tester = getAccountOrThrow(accountRepository, request.getTesterAccountId());

            if (tester.getRole() != Role.TESTER) {
                throw new SecurityException("해결 검증(RESOLVED)은 TESTER만 가능합니다.");
            }
            if (issue.getStatus() != IssueStatus.FIXED) {
                throw new IllegalStateException("FIXED 상태의 이슈만 검증할 수 있습니다.");
            }

            IssueSnapshot oldState = new IssueSnapshot(issue);

            issue.setStatus(IssueStatus.RESOLVED);

            recordHistory(tester, oldState, issue);
            return issueMapper.toDetailResponse(issueRepository.save(issue));
        });
    }

    // [상태 전이: FIXED -> REOPENED] (검증 실패)
    // 수행자: TESTER / 조건: 필수 코멘트(실패 사유) 작성
    public IssueDetailResponse verifyFailed(IssueFailRequest request) {
        return TransactionManager.execute(entityManager -> {
            IssueRepository issueRepository = new IssueRepository(entityManager);
            AccountRepository accountRepository = new AccountRepository(entityManager);

            Issue issue = getIssueOrThrow(issueRepository, request.getIssueId());
            Account tester = getAccountOrThrow(accountRepository, request.getTesterAccountId());

            if (tester.getRole() != Role.TESTER) {
                throw new SecurityException("검증 실패(REOPENED) 처리는 TESTER만 가능합니다.");
            }
            if (issue.getStatus() != IssueStatus.FIXED) {
                throw new IllegalStateException("FIXED 상태의 이슈만 검증할 수 있습니다.");
            }
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new IllegalArgumentException("검증 실패 시 반려 사유 작성이 필수입니다.");
            }

            IssueSnapshot oldState = new IssueSnapshot(issue);

            issue.setStatus(IssueStatus.REOPENED);
            addCommentToIssue(issue, tester, request.getReason());

            recordHistory(tester, oldState, issue);
            return issueMapper.toDetailResponse(issueRepository.save(issue));
        });
    }

    // [상태 전이: RESOLVED -> CLOSED]
    // 수행자: PL
    public IssueDetailResponse closeIssue(IssueCloseRequest request) {
        return TransactionManager.execute(entityManager -> {
            IssueRepository issueRepository = new IssueRepository(entityManager);
            AccountRepository accountRepository = new AccountRepository(entityManager);

            Issue issue = getIssueOrThrow(issueRepository, request.getIssueId());
            Account pl = getAccountOrThrow(accountRepository, request.getPlAccountId());

            if (pl.getRole() != Role.PL) {
                throw new SecurityException("이슈 종료(CLOSED)는 PL만 가능합니다.");
            }
            if (issue.getStatus() != IssueStatus.RESOLVED) {
                throw new IllegalStateException("RESOLVED 상태의 이슈만 종료할 수 있습니다.");
            }

            IssueSnapshot oldState = new IssueSnapshot(issue);

            issue.setStatus(IssueStatus.CLOSED);

            recordHistory(pl, oldState, issue);
            return issueMapper.toDetailResponse(issueRepository.save(issue));
        });
    }

    // [상태 전이: CLOSED -> REOPENED]
    // 수행자: PL / TESTER
    public IssueDetailResponse reOpenIssue(IssueReopenRequest request) {
        return TransactionManager.execute(entityManager -> {
            IssueRepository issueRepository = new IssueRepository(entityManager);
            AccountRepository accountRepository = new AccountRepository(entityManager);

            Issue issue = getIssueOrThrow(issueRepository, request.getIssueId());
            Account account = getAccountOrThrow(accountRepository, request.getReopenAccountId());

            if (account.getRole() != Role.PL && account.getRole() != Role.TESTER) {
                throw new SecurityException("이슈 재오픈은 PL 또는 TESTER만 가능합니다.");
            }
            if (issue.getStatus() != IssueStatus.CLOSED) {
                throw new IllegalStateException("CLOSED 상태의 이슈만 재오픈할 수 있습니다.");
            }

            IssueSnapshot oldState = new IssueSnapshot(issue);

            issue.setStatus(IssueStatus.REOPENED);
            if (request.getReason() != null && !request.getReason().trim().isEmpty()) {
                addCommentToIssue(issue, account, request.getReason());
            }

            recordHistory(account, oldState, issue);
            return issueMapper.toDetailResponse(issueRepository.save(issue));
        });
    }

    // Feat: 단순 코멘트 추가 기능 구현
    public IssueDetailResponse addComment(CommentCreateRequest request) {
        return TransactionManager.execute(entityManager -> {
            IssueRepository issueRepository = new IssueRepository(entityManager);
            AccountRepository accountRepository = new AccountRepository(entityManager);

            Issue issue = getIssueOrThrow(issueRepository, request.getIssueId());
            Account author = getAccountOrThrow(accountRepository, request.getAuthorAccountId());

            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("코멘트 내용이 비어있습니다.");
            }

            // 기존 헬퍼 메서드 재활용
            addCommentToIssue(issue, author, request.getContent());

            // 상태 변경은 없으므로 recordHistory()는 생략함.
            // DB에 저장하고 최신 상태의 이슈 DTO를 반환
            return issueMapper.toDetailResponse(issueRepository.save(issue));
        });
    }

    // 3. 조회 및 유틸리티
    public IssueDetailResponse getIssueDetail(Long issueId) {
        return TransactionManager.execute(entityManager -> {
            IssueRepository issueRepository = new IssueRepository(entityManager);
            return issueMapper.toDetailResponse(getIssueOrThrow(issueRepository, issueId));
        });
    }

    public List<IssueSummaryResponse> getAllIssuesByProject(Long projectId) {
        return TransactionManager.execute(entityManager -> {
            IssueRepository issueRepository = new IssueRepository(entityManager);
            return issueMapper.toSummaryResponseList(issueRepository.findByProjectId(projectId));
        });
    }

    private Issue getIssueOrThrow(IssueRepository issueRepository, Long issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이슈입니다. ID: " + issueId));
    }

    private Account getAccountOrThrow(AccountRepository accountRepository, Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다. ID: " + accountId));
    }

    private void addCommentToIssue(Issue issue, Account author, String content) {
        Comment comment = Comment.create(content, author, issue);
        issue.addComment(comment);
    }


    // --------------- 이력 기록 로직 (Snapshot 기반 변경 내역 추적) ---------------
    // 상태가 변경되기 전의 값을 임시 보관하는 DTO 클래스
    private static class IssueSnapshot {
        final String title;
        final String description;
        final Priority priority;
        final IssueStatus status;

        IssueSnapshot(Issue issue) {
            this.title = issue.getTitle();
            this.description = issue.getDescription();
            this.priority = issue.getPriority();
            this.status = issue.getStatus();
        }
    }

    // 과거 스냅샷과 현재 이슈를 비교하여 Delta를 생성하는 팩토리 메서드
    private void recordHistory(Account actor, IssueSnapshot oldState, Issue newState) {
        boolean isNew = (oldState == null);

        IssueDelta delta = IssueDelta.create(
                isNew ? null : oldState.title, newState.getTitle(),
                isNew ? null : oldState.description, newState.getDescription(),
                isNew ? null : oldState.priority, newState.getPriority(),
                isNew ? null : oldState.status, newState.getStatus()
        );
        IssueHistory history = IssueHistory.create(actor, delta);
        newState.addIssueHistory(history);
    }
}
