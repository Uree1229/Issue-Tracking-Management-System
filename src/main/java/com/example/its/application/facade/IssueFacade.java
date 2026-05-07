package com.example.its.application.facade;

import com.example.its.application.service.*;
import com.example.its.persistence.repository.*;
import com.example.its.persistence.query.*; // 쿼리 리포지토리 import 추가
import com.example.its.shared.dto.issue.*;

import java.util.List;

// Fix: 5/4 피드백 반영
public class IssueFacade {

    private final IssueService issueService;
    private final IssueSearchService issueSearchService;
    private final IssueStatisticsService issueStatisticsService;
    private final AssigneeRecommendationService recommendationService;

    public IssueFacade() {
        // 1. 필요한 Repository 및 QueryRepository 부품들 일괄 생성
        IssueRepository issueRepository = new IssueRepository();
        AccountRepository accountRepository = new AccountRepository();
        ProjectRepository projectRepository = new ProjectRepository();
        TagRepository tagRepository = new TagRepository();
        
        // 새로 추가된 전용 쿼리 리포지토리들 생성
        IssueQueryRepository issueQueryRepository = new IssueQueryRepository();
        StatisticsQueryRepository statisticsQueryRepository = new StatisticsQueryRepository();

        // 2. Service 조립
        this.issueService = new IssueService(issueRepository, accountRepository, projectRepository, tagRepository);
        
        // 에러가 났던 3개의 서비스에 알맞은 쿼리 객체 주입
        this.issueSearchService = new IssueSearchService(issueQueryRepository);
        this.issueStatisticsService = new IssueStatisticsService(statisticsQueryRepository);
        
        // Fix: 파라미터 순서 변경: Account -> Issue 순서
        this.recommendationService = new AssigneeRecommendationService(accountRepository, issueRepository);
    }

    public IssueDetailResponse getIssue(Long id) {
        return issueService.getIssueDetail(id);
    }

    public IssueDetailResponse registerIssue(IssueCreateRequest request) {
        return issueService.registerIssue(request);
    }

    public List<IssueSummaryResponse> searchIssues(IssueSearchCondition condition) {
        return issueSearchService.searchIssues(condition);
    }

    public StatisticsResponse getStatistics(Long projectId) {
        return issueStatisticsService.getProjectStatistics(projectId);
    }

    public List<RecommendationResponse> recommendAssignees(Long projectId, List<Long> tagIds) {
        return recommendationService.recommendAssignees(projectId, tagIds);
    }

    public IssueDetailResponse assign(IssueAssignRequest request) {
        return issueService.assignAssignee(request);
    }

    public IssueDetailResponse fix(IssueFixRequest request) {
        return issueService.markFixed(request);
    }

    public IssueDetailResponse resolve(IssueResolveRequest request) {
        return issueService.verifyResolved(request);
    }

    public IssueDetailResponse fail(IssueFailRequest request) {
        return issueService.verifyFailed(request);
    }

    public IssueDetailResponse close(IssueCloseRequest request) {
        return issueService.closeIssue(request);
    }

    public IssueDetailResponse reopen(IssueReopenRequest request) {
        return issueService.reOpenIssue(request);
    }
}

