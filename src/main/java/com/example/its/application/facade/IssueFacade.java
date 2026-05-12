package com.example.its.application.facade;

import com.example.its.application.mapper.IssueMapper;
import com.example.its.application.service.*;
import com.example.its.shared.dto.issue.*;

import java.util.List;

// Fix: 5/4 피드백 반영
public class IssueFacade {

    private final IssueService issueService;
    private final IssueSearchService issueSearchService;
    private final IssueStatisticsService issueStatisticsService;
    private final AssigneeRecommendationService recommendationService;

    public IssueFacade() {
        IssueMapper issueMapper = new IssueMapper();
        this.issueService = new IssueService(issueMapper);
        this.issueSearchService = new IssueSearchService(issueMapper);
        this.issueStatisticsService = new IssueStatisticsService();
        this.recommendationService = new AssigneeRecommendationService();
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

    // Feat: 단순 코멘트 기능 추가
    public IssueDetailResponse addComment(CommentCreateRequest request) {
        return issueService.addComment(request);
    }

    // Feat: 이슈 태그 업데이트 기능 추가
    public IssueDetailResponse updateIssueTags(IssueTagUpdateRequest request) {
        return issueService.updateIssueTags(request);
    }
}
