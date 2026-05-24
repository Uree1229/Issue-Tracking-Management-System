package com.example.its.ui.javafx.service;

import com.example.its.application.facade.AccountFacade;
import com.example.its.application.facade.IssueFacade;
import com.example.its.application.facade.ProjectFacade;
import com.example.its.persistence.entity.Role;
import com.example.its.shared.dto.account.AccountCreateRequest;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.shared.dto.issue.CommentCreateRequest;
import com.example.its.shared.dto.issue.DailyIssueStatisticsRequest;
import com.example.its.shared.dto.issue.IssueAssignRequest;
import com.example.its.shared.dto.issue.IssueCloseRequest;
import com.example.its.shared.dto.issue.IssueCreateRequest;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.shared.dto.issue.IssueFailRequest;
import com.example.its.shared.dto.issue.IssueFixRequest;
import com.example.its.shared.dto.issue.IssueReopenRequest;
import com.example.its.shared.dto.issue.IssueResolveRequest;
import com.example.its.shared.dto.issue.IssueSearchCondition;
import com.example.its.shared.dto.issue.IssueSummaryResponse;
import com.example.its.shared.dto.issue.IssueTagUpdateRequest;
import com.example.its.shared.dto.issue.MonthlyIssueStatisticsRequest;
import com.example.its.shared.dto.issue.RecommendationResponse;
import com.example.its.shared.dto.issue.StatisticsResponse;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.shared.dto.project.ProjectTagUpdateRequest;
import com.example.its.shared.dto.tag.TagResponse;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.ProjectTagOption;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class JavaFxBackendBridge {

    private static final JavaFxBackendBridge INSTANCE = new JavaFxBackendBridge();

    private final AccountFacade accountFacade;
    private final IssueFacade issueFacade;
    private final ProjectFacade projectFacade;

    private JavaFxBackendBridge() {
        this.accountFacade = new AccountFacade();
        this.issueFacade = new IssueFacade();
        this.projectFacade = new ProjectFacade();
    }

    public static JavaFxBackendBridge getInstance() {
        return INSTANCE;
    }

    public AccountResponse login(String loginId, String password) {
        return accountFacade.login(loginId, password);
    }

    public AccountResponse register(AccountCreateRequest request) {
        return accountFacade.register(request);
    }

    public List<AccountResponse> getActiveAccounts() {
        return accountFacade.getActiveAccounts();
    }

    public List<AccountResponse> getDeveloperAccounts() {
        return getActiveAccounts().stream()
            .filter(account -> account.getRole() == Role.DEV)
            .sorted(Comparator.comparing(AccountResponse::getName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    public ProjectResponse createProject(ProjectCreateRequest request) {
        return projectFacade.createProject(request);
    }

    public ProjectResponse getProject(Long projectId) {
        return projectFacade.getProject(projectId);
    }

    public ProjectResponse updateProjectTags(Long projectId, List<String> tagNamesToAdd, List<Long> tagIdsToRemove) {
        ProjectTagUpdateRequest request = new ProjectTagUpdateRequest(projectId, tagNamesToAdd, tagIdsToRemove);
        return projectFacade.updateProjectTags(request);
    }

    public List<ProjectResponse> getProjects() {
        return projectFacade.getAllProjects().stream()
            .map(project -> project.getProjectId() == null ? project : projectFacade.getProject(project.getProjectId()))
            .sorted(Comparator.comparing(ProjectResponse::getName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    public List<ProjectResponse> getAccessibleProjects(AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return List.of();
        }
        return getProjects();
    }

    public Optional<ProjectResponse> findPreferredProject(AuthenticatedUser currentUser) {
        return getAccessibleProjects(currentUser).stream().findFirst();
    }

    public List<ProjectTagOption> getProjectTags(Long projectId) {
        if (projectId == null) {
            return List.of();
        }

        ProjectResponse project = projectFacade.getProject(projectId);
        if (project == null || project.getTags() == null) {
            return List.of();
        }

        return project.getTags().stream()
            .map(this::toProjectTagOption)
            .sorted(Comparator.comparing(ProjectTagOption::name, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    public IssueDetailResponse createIssue(IssueCreateRequest request) {
        return issueFacade.registerIssue(request);
    }

    public IssueDetailResponse updateIssueTags(Long issueId, List<Long> tagIdsToAdd, List<Long> tagIdsToRemove) {
        IssueTagUpdateRequest request = new IssueTagUpdateRequest(issueId, tagIdsToAdd, tagIdsToRemove);
        return issueFacade.updateIssueTags(request);
    }

    public IssueDetailResponse addComment(CommentCreateRequest request) {
        return issueFacade.addComment(request);
    }

    public List<IssueSummaryResponse> searchIssues(IssueSearchCondition condition) {
        return issueFacade.searchIssues(condition);
    }

    public IssueDetailResponse getIssue(Long issueId) {
        return issueFacade.getIssue(issueId);
    }

    public StatisticsResponse getStatistics(Long projectId) {
        return issueFacade.getStatistics(projectId);
    }

    public Map<String, Long> getDailyIssueStatistics(Long projectId, int days) {
        if (projectId == null || days <= 0) {
            return Map.of();
        }
        return issueFacade.getDailyIssueStatistics(new DailyIssueStatisticsRequest(projectId, days));
    }

    public Map<String, Long> getMonthlyIssueStatistics(Long projectId, int months) {
        if (projectId == null || months <= 0) {
            return Map.of();
        }
        return issueFacade.getMonthlyIssueStatistics(new MonthlyIssueStatisticsRequest(projectId, months));
    }

    public List<RecommendationResponse> recommendAssignees(Long projectId, List<Long> tagIds) {
        return issueFacade.recommendAssignees(projectId, tagIds);
    }

    public IssueDetailResponse assign(IssueAssignRequest request) {
        return issueFacade.assign(request);
    }

    public IssueDetailResponse fix(IssueFixRequest request) {
        return issueFacade.fix(request);
    }

    public IssueDetailResponse resolve(IssueResolveRequest request) {
        return issueFacade.resolve(request);
    }

    public IssueDetailResponse fail(IssueFailRequest request) {
        return issueFacade.fail(request);
    }

    public IssueDetailResponse close(IssueCloseRequest request) {
        return issueFacade.close(request);
    }

    public IssueDetailResponse reopen(IssueReopenRequest request) {
        return issueFacade.reopen(request);
    }

    private ProjectTagOption toProjectTagOption(TagResponse tag) {
        return new ProjectTagOption(tag.getTagId(), tag.getName(), tag.getDescription());
    }
}
