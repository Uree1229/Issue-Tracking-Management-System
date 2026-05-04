package com.example.its.application.service;

import com.example.its.persistence.entity.*;
import com.example.its.persistence.repository.*;
import com.example.its.shared.dto.issue.*;

import java.util.List;
import java.util.stream.Collectors;

public class IssueService {

    private final IssueRepository issueRepository;
    private final AccountRepository accountRepository;
    private final ProjectRepository projectRepository;
    private final TagRepository tagRepository;

    public IssueService(IssueRepository issueRepository, AccountRepository accountRepository, 
                        ProjectRepository projectRepository, TagRepository tagRepository) {
        this.issueRepository = issueRepository;
        this.accountRepository = accountRepository;
        this.projectRepository = projectRepository;
        this.tagRepository = tagRepository;
    }

    // 1. 이슈 생성 (Register)
    public IssueDetailResponse registerIssue(IssueCreateRequest request) {
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

        // 초기 생성 이력 기록 (NULL -> NEW)
        recordHistory(issue, reporter, null, IssueStatus.NEW);

        Issue savedIssue = issueRepository.save(issue);
        return IssueDetailResponse.from(savedIssue);
    }

    // ------------------------------------------------------------- 2. 도메인 룰 기반 상태 전이 로직 ------------------------------------------------------------- //
    // [상태 전이: NEW, REOPENED -> ASSIGNED]
    // 수행자: PL / 조건: Assignee가 DEV 역할이어야 함
    public IssueDetailResponse assignAssignee(IssueAssignRequest request) {
        Issue issue = getIssueOrThrow(request.getIssueId());
        Account pl = getAccountOrThrow(request.getPlAccountId());
        Account assignee = getAccountOrThrow(request.getAssigneeAccountId());

        if (pl.getRole() != Role.PL) {
            throw new SecurityException("이슈 할당은 PL(Project Leader)만 가능합니다.");
        }
        if (issue.getStatus() != IssueStatus.NEW && issue.getStatus() != IssueStatus.REOPENED) {
            throw new IllegalStateException("NEW 또는 REOPENED 상태에서만 할당이 가능합니다.");
        }
        if (assignee.getRole() != Role.DEV) {
            throw new IllegalArgumentException("담당자는 DEV(Developer) 역할이어야 합니다.");
        }

        IssueStatus oldStatus = issue.getStatus();
        issue.setAssignee(assignee);
        issue.setStatus(IssueStatus.ASSIGNED);

        recordHistory(issue, pl, oldStatus, IssueStatus.ASSIGNED);
        return IssueDetailResponse.from(issueRepository.save(issue));
    }

    // [상태 전이: ASSIGNED -> FIXED]
    // 수행자: DEV (본인) / 조건: 필수 코멘트(사유) 작성
    public IssueDetailResponse markFixed(IssueFixRequest request) {
        Issue issue = getIssueOrThrow(request.getIssueId());
        Account dev = getAccountOrThrow(request.getDevAccountId());

        if (issue.getStatus() != IssueStatus.ASSIGNED) {
            throw new IllegalStateException("ASSIGNED 상태에서만 FIXED로 변경할 수 있습니다.");
        }
        if (issue.getAssignee() == null || !issue.getAssignee().getAccountId().equals(request.getDevAccountId())) {
            throw new SecurityException("본인에게 할당된 이슈만 완료(FIXED) 처리할 수 있습니다.");
        }
        if (request.getComment() == null || request.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("FIXED 처리 시 코멘트(작업 내용/사유) 작성이 필수입니다.");
        }

        IssueStatus oldStatus = issue.getStatus();
        issue.setStatus(IssueStatus.FIXED);
        issue.setFixer(dev);

        addCommentToIssue(issue, dev, request.getComment());
        recordHistory(issue, dev, oldStatus, IssueStatus.FIXED);
        
        return IssueDetailResponse.from(issueRepository.save(issue));
    }

    // [상태 전이: FIXED -> RESOLVED]
    // 수행자: TESTER
    public IssueDetailResponse verifyResolved(IssueResolveRequest request) {
        Issue issue = getIssueOrThrow(request.getIssueId());
        Account tester = getAccountOrThrow(request.getTesterAccountId());

        if (tester.getRole() != Role.TESTER) {
            throw new SecurityException("해결 검증(RESOLVED)은 TESTER만 가능합니다.");
        }
        if (issue.getStatus() != IssueStatus.FIXED) {
            throw new IllegalStateException("FIXED 상태의 이슈만 검증할 수 있습니다.");
        }

        IssueStatus oldStatus = issue.getStatus();
        issue.setStatus(IssueStatus.RESOLVED);

        recordHistory(issue, tester, oldStatus, IssueStatus.RESOLVED);
        return IssueDetailResponse.from(issueRepository.save(issue));
    }

    // [상태 전이: FIXED -> REOPENED] (검증 실패)
    // 수행자: TESTER / 조건: 필수 코멘트(실패 사유) 작성
    public IssueDetailResponse verifyFailed(IssueFailRequest request) {
        Issue issue = getIssueOrThrow(request.getIssueId());
        Account tester = getAccountOrThrow(request.getTesterAccountId());

        if (tester.getRole() != Role.TESTER) {
            throw new SecurityException("검증 실패(REOPENED) 처리는 TESTER만 가능합니다.");
        }
        if (issue.getStatus() != IssueStatus.FIXED) {
            throw new IllegalStateException("FIXED 상태의 이슈만 검증할 수 있습니다.");
        }
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("검증 실패 시 반려 사유(코멘트) 작성이 필수입니다.");
        }

        IssueStatus oldStatus = issue.getStatus();
        issue.setStatus(IssueStatus.REOPENED);

        addCommentToIssue(issue, tester, request.getReason());
        recordHistory(issue, tester, oldStatus, IssueStatus.REOPENED);
        
        return IssueDetailResponse.from(issueRepository.save(issue));
    }

    // [상태 전이: RESOLVED -> CLOSED]
    // 수행자: PL
    public IssueDetailResponse closeIssue(IssueCloseRequest request) {
        Issue issue = getIssueOrThrow(request.getIssueId());
        Account pl = getAccountOrThrow(request.getPlAccountId());

        if (pl.getRole() != Role.PL) {
            throw new SecurityException("이슈 종료(CLOSED)는 PL만 가능합니다.");
        }
        if (issue.getStatus() != IssueStatus.RESOLVED) {
            throw new IllegalStateException("RESOLVED 상태의 이슈만 종료할 수 있습니다.");
        }

        IssueStatus oldStatus = issue.getStatus();
        issue.setStatus(IssueStatus.CLOSED);

        recordHistory(issue, pl, oldStatus, IssueStatus.CLOSED);
        return IssueDetailResponse.from(issueRepository.save(issue));
    }

    // [상태 전이: CLOSED -> REOPENED]
    // 수행자: PL / TESTER
    public IssueDetailResponse reOpenIssue(IssueReopenRequest request) {
        Issue issue = getIssueOrThrow(request.getIssueId());
        Account account = getAccountOrThrow(request.getReopenAccountId());

        if (account.getRole() != Role.PL && account.getRole() != Role.TESTER) {
            throw new SecurityException("이슈 재오픈은 PL 또는 TESTER만 가능합니다.");
        }
        if (issue.getStatus() != IssueStatus.CLOSED) {
            throw new IllegalStateException("CLOSED 상태의 이슈만 재오픈할 수 있습니다.");
        }

        IssueStatus oldStatus = issue.getStatus();
        issue.setStatus(IssueStatus.REOPENED);

        if (request.getReason() != null && !request.getReason().trim().isEmpty()) {
            addCommentToIssue(issue, account, request.getReason());
        }
        recordHistory(issue, account, oldStatus, IssueStatus.REOPENED);
        
        return IssueDetailResponse.from(issueRepository.save(issue));
    }
    // ------------------------------------------------------------- 2. 도메인 룰 기반 상태 전이 로직 끝 ------------------------------------------------------------- //

    // 3. 조회 및 유틸리티 (Fetchers & Helpers)
    public IssueDetailResponse getIssueDetail(Long issueId) {
        Issue issue = getIssueOrThrow(issueId);
        return IssueDetailResponse.from(issue);
    }

    public List<IssueSummaryResponse> getAllIssuesByProject(Long projectId) {
        return issueRepository.findByProjectId(projectId).stream()
                .map(IssueSummaryResponse::from)
                .collect(Collectors.toList());
    }

    // 내부 헬퍼 메서드
    private Issue getIssueOrThrow(Long issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이슈입니다. ID: " + issueId));
    }

    private Account getAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다. ID: " + accountId));
    }

    private void addCommentToIssue(Issue issue, Account author, String content) {
        Comment comment = Comment.create(content, author, issue);
        issue.addComment(comment);
    }

    // ------------------------------------ 이력 기록 헬퍼 메서드 (오버로딩 적용) ------------------------------------
    // 1. 편의성 헬퍼: 기존처럼 상태(Status)만 변경될 때 호출하면 나머지는 알아서 채워주도록 구성
    private void recordHistory(Issue issue, Account actor, IssueStatus oldStatus, IssueStatus newStatus) {
        boolean isNew = (oldStatus == null); // 최초 생성 시에는 이전 값이 모두 null이어야 함
        
        // 상태 이외의 값들(제목, 내용, 우선순위)은 변경되지 않았으므로 현재 값을 그대로 세팅하여 마스터 함수 호출
        recordHistory(
                issue, actor,
                isNew ? null : issue.getTitle(), issue.getTitle(),
                isNew ? null : issue.getDescription(), issue.getDescription(),
                isNew ? null : issue.getPriority(), issue.getPriority(),
                oldStatus, newStatus
        );
    }

    // 2. 마스터 헬퍼: 5/1 메세지 5번 피드백 반영
    // 4가지 항목(제목, 내용, 우선순위, 상태)의 모든 변경 사항을 처리하는 진짜 함수
    private void recordHistory(Issue issue, Account actor, 
                               String oldTitle, String newTitle, 
                               String oldContent, String newContent, 
                               Priority oldPriority, Priority newPriority, 
                               IssueStatus oldStatus, IssueStatus newStatus) {
        
        IssueDelta delta = IssueDelta.create(oldTitle, newTitle, oldContent, newContent, 
                                             oldPriority, newPriority, oldStatus, newStatus);
        IssueHistory history = IssueHistory.create(actor, delta);
        
        issue.addIssueHistory(history);
    }
}