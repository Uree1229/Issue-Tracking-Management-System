package com.example.its.ui.javafx.service;

import com.example.its.application.facade.AccountFacade;
import com.example.its.application.facade.IssueFacade;
import com.example.its.application.facade.ProjectFacade;
import com.example.its.persistence.entity.Role;
import com.example.its.shared.dto.account.AccountCreateRequest;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.shared.dto.issue.IssueAssignRequest;
import com.example.its.shared.dto.issue.IssueCloseRequest;
import com.example.its.shared.dto.issue.IssueCreateRequest;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.shared.dto.issue.IssueFailRequest;
import com.example.its.shared.dto.issue.IssueFixRequest;
import com.example.its.shared.dto.issue.IssueResolveRequest;
import com.example.its.shared.dto.issue.IssueReopenRequest;
import com.example.its.shared.dto.issue.IssueSearchCondition;
import com.example.its.shared.dto.issue.IssueSummaryResponse;
import com.example.its.shared.dto.issue.StatisticsResponse;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class JavaFxBackendBridge {

    private final AccountFacade accountFacade = new AccountFacade();
    private final IssueFacade issueFacade = new IssueFacade();
    private final ProjectFacade projectFacade = new ProjectFacade();

    private JavaFxBackendBridge() {
    }

    public static JavaFxBackendBridge getInstance() {
        return Holder.INSTANCE;
    }

    public AccountResponse login(String loginId, String password) {
        return accountFacade.login(loginId, password);
    }

    public AccountResponse register(AccountCreateRequest request) {
        return accountFacade.register(request);
    }

    public List<AccountResponse> getActiveAccounts() {
        return accountFacade.getActiveAccounts().stream()
            .sorted(Comparator.comparing(AccountResponse::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

    public List<AccountResponse> getDeveloperAccounts() {
        return getActiveAccounts().stream()
            .filter(account -> account.getRole() == Role.DEV)
            .collect(Collectors.toList());
    }

    public ProjectResponse createProject(ProjectCreateRequest request) {
        return projectFacade.createProject(request);
    }

    public List<ProjectResponse> getProjects() {
        return projectFacade.getAllProjects().stream()
            .sorted(Comparator.comparing(ProjectResponse::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

    public Optional<ProjectResponse> findPreferredProject() {
        List<ProjectResponse> projects = getProjects();
        return projects.stream()
            .filter(project -> "project1".equalsIgnoreCase(project.getName()))
            .findFirst()
            .or(() -> projects.stream().findFirst());
    }

    public IssueDetailResponse createIssue(IssueCreateRequest request) {
        return issueFacade.registerIssue(request);
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

    private static final class Holder {
        private static final JavaFxBackendBridge INSTANCE = new JavaFxBackendBridge();
    }
}
