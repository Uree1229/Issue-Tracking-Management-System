package com.example.its.ui.javafx.service;

import com.example.its.application.facade.AccountFacade;
import com.example.its.application.facade.IssueFacade;
import com.example.its.application.facade.ProjectFacade;
import com.example.its.persistence.entity.Project;
import com.example.its.persistence.entity.Role;
import com.example.its.persistence.entity.Tag;
import com.example.its.persistence.repository.ProjectRepository;
import com.example.its.persistence.repository.TagRepository;
import com.example.its.persistence.transaction.TransactionManager;
import com.example.its.shared.dto.account.AccountCreateRequest;
import com.example.its.shared.dto.account.AccountResponse;
import com.example.its.shared.dto.issue.IssueAssignRequest;
import com.example.its.shared.dto.issue.IssueCloseRequest;
import com.example.its.shared.dto.issue.CommentCreateRequest;
import com.example.its.shared.dto.issue.IssueCreateRequest;
import com.example.its.shared.dto.issue.IssueDetailResponse;
import com.example.its.shared.dto.issue.IssueFailRequest;
import com.example.its.shared.dto.issue.IssueFixRequest;
import com.example.its.shared.dto.issue.IssueResolveRequest;
import com.example.its.shared.dto.issue.IssueReopenRequest;
import com.example.its.shared.dto.issue.IssueSearchCondition;
import com.example.its.shared.dto.issue.IssueSummaryResponse;
import com.example.its.shared.dto.issue.RecommendationResponse;
import com.example.its.shared.dto.issue.StatisticsResponse;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.ui.javafx.model.ProjectTagOption;
import com.example.its.ui.javafx.support.TagMapper;

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

    public List<ProjectTagOption> getProjectTags(Long projectId) {
        if (projectId == null) {
            return List.of();
        }

        return TransactionManager.execute(entityManager -> new TagRepository(entityManager).findByProjectId(projectId).stream()
            .map(TagMapper::toOption)
            .sorted(Comparator.comparing(ProjectTagOption::name, String.CASE_INSENSITIVE_ORDER))
            .toList());
    }

    public ProjectTagOption ensureProjectTag(Long projectId, String name, String description) {
        return TransactionManager.execute(entityManager -> {
            TagRepository tagRepository = new TagRepository(entityManager);
            Tag existingTag = tagRepository.findByProjectIdAndName(projectId, name).orElse(null);
            if (existingTag != null) {
                if (description != null && !description.isBlank()) {
                    existingTag.setDescription(description);
                    tagRepository.save(existingTag);
                }
                return TagMapper.toOption(existingTag);
            }

            ProjectRepository projectRepository = new ProjectRepository(entityManager);
            Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

            Tag createdTag = Tag.create(name, description, project);
            tagRepository.save(createdTag);
            return TagMapper.toOption(createdTag);
        });
    }

    public IssueDetailResponse createIssue(IssueCreateRequest request) {
        return issueFacade.registerIssue(request);
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

    private static final class Holder {
        private static final JavaFxBackendBridge INSTANCE = new JavaFxBackendBridge();
    }
}
