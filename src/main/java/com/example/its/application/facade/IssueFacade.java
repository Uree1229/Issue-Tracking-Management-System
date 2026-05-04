package com.example.its.application.facade;

import com.example.its.application.service.*;
import com.example.its.shared.dto.issue.*;

import java.util.List;

// Fix: 모든 상태 전이 함수의 파라미터를 신규 추가된 DTO 하나로 통합
// Fix: 리턴 타입을 void에서 IssueDetailResponse로 변경
// -> 프론트엔드에서 상태 변경 API를 호출하자마자 변경된 최신 상태의 이슈 정보를 즉시 렌더링 가능
public class IssueFacade {

    private final IssueService issueService;
    private final IssueSearchService issueSearchService;
    private final IssueStatisticsService issueStatisticsService;
    private final AssigneeRecommendationService recommendationService;

    public IssueFacade(IssueService issueService, IssueSearchService issueSearchService,
                       IssueStatisticsService issueStatisticsService,
                       AssigneeRecommendationService recommendationService) {
        this.issueService = issueService;
        this.issueSearchService = issueSearchService;
        this.issueStatisticsService = issueStatisticsService;
        this.recommendationService = recommendationService;
    }

    // 이슈 상세 조회 및 등록
    public IssueDetailResponse getIssue(Long id) {
        return issueService.getIssueDetail(id);
    }

    public IssueDetailResponse registerIssue(IssueCreateRequest request) {
        return issueService.registerIssue(request);
    }

    // 이슈 검색 및 통계
    public List<IssueSummaryResponse> searchIssues(IssueSearchCondition condition) {
        return issueSearchService.searchIssues(condition);
    }

    public StatisticsResponse getStatistics(Long projectId) {
        return issueStatisticsService.getProjectStatistics(projectId);
    }

    // 담당자 추천 알고리즘 호출
    public List<RecommendationResponse> recommendAssignees(Long projectId, List<Long> tagIds) {
        return recommendationService.recommendAssignees(projectId, tagIds);
    }

    // 상태 전이 (Status Transitions)
    // Fix: 5/1 피드백 3, 4번 반영
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