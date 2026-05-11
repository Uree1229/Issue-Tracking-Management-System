package com.example.its.application.service;

import com.example.its.application.mapper.ProjectMapper;
import com.example.its.persistence.entity.Account;
import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Project;
import com.example.its.persistence.entity.ProjectMember;
import com.example.its.persistence.repository.AccountRepository;
import com.example.its.persistence.repository.ProjectMemberRepository;
import com.example.its.persistence.repository.ProjectRepository;
import com.example.its.persistence.transaction.TransactionManager;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.shared.dto.project.ProjectUpdateRequest;

import java.util.LinkedHashSet;
import java.util.List;

public class ProjectService {

    private final ProjectMapper projectMapper;

    public ProjectService(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    public ProjectResponse createProject(ProjectCreateRequest request) {
        return TransactionManager.execute(entityManager -> {
            ProjectRepository projectRepository = new ProjectRepository(entityManager);
            AccountRepository accountRepository = new AccountRepository(entityManager);
            ProjectMemberRepository projectMemberRepository = new ProjectMemberRepository(entityManager);

            if (projectRepository.findByName(request.getName()).isPresent()) {
                throw new IllegalArgumentException("이미 존재하는 프로젝트 이름입니다: " + request.getName());
            }

            Account creator = getAccountOrThrow(accountRepository, request.getCreatedByAccountId());
            Project project = Project.create(request.getName(), request.getDescription(), creator);
            Project savedProject = projectRepository.save(project);

            LinkedHashSet<Long> memberIds = new LinkedHashSet<>();
            if (request.getMemberAccountIds() != null) {
                memberIds.addAll(request.getMemberAccountIds());
            }
            memberIds.add(creator.getAccountId());

            for (Long memberAccountId : memberIds) {
                Account member = getAccountOrThrow(accountRepository, memberAccountId);
                projectMemberRepository.save(ProjectMember.create(savedProject, member));
            }

            return projectMapper.toResponse(savedProject);
        });
    }

    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request) {
        return TransactionManager.execute(entityManager -> {
            ProjectRepository projectRepository = new ProjectRepository(entityManager);
            Project project = getProjectOrThrow(projectRepository, projectId);

            if (request.getName() != null && !request.getName().equals(project.getName())) {
                if (projectRepository.findByName(request.getName()).isPresent()) {
                    throw new IllegalArgumentException("이미 존재하는 프로젝트 이름입니다: " + request.getName());
                }
                project.setName(request.getName());
            }
            if (request.getDescription() != null) {
                project.setDescription(request.getDescription());
            }
            return projectMapper.toResponse(projectRepository.save(project));
        });
    }

    public ProjectResponse getProject(Long projectId) {
        return TransactionManager.execute(entityManager -> {
            ProjectRepository projectRepository = new ProjectRepository(entityManager);
            return projectMapper.toResponse(getProjectOrThrow(projectRepository, projectId));
        });
    }

    public ProjectResponse getProjectByName(String name) {
        return TransactionManager.execute(entityManager -> {
            ProjectRepository projectRepository = new ProjectRepository(entityManager);
            Project project = projectRepository.findByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다. 이름: " + name));
            return projectMapper.toResponse(project);
        });
    }

    public List<ProjectResponse> getAllProjects() {
        return TransactionManager.execute(entityManager -> {
            ProjectRepository projectRepository = new ProjectRepository(entityManager);
            return projectMapper.toResponseList(projectRepository.findAll());
        });
    }

    public List<ProjectResponse> getProjectsByCreator(Long accountId) {
        return TransactionManager.execute(entityManager -> {
            ProjectRepository projectRepository = new ProjectRepository(entityManager);
            return projectMapper.toResponseList(projectRepository.findByCreatedByAccountId(accountId));
        });
    }

    public List<ProjectResponse> getProjectsByMember(Long accountId) {
        return TransactionManager.execute(entityManager -> {
            ProjectMemberRepository projectMemberRepository = new ProjectMemberRepository(entityManager);
            return projectMapper.toResponseList(projectMemberRepository.findProjectsByAccountId(accountId));
        });
    }

    public void deleteProject(Long projectId) {
        TransactionManager.executeVoid(entityManager -> {
            ProjectRepository projectRepository = new ProjectRepository(entityManager);
            Project project = getProjectOrThrow(projectRepository, projectId);

            boolean hasUnclosedIssue = project.getIssues().stream()
                    .anyMatch(issue -> issue.getStatus() != IssueStatus.CLOSED);

            if (hasUnclosedIssue) {
                throw new IllegalStateException("모든 이슈가 CLOSED 상태일 때만 프로젝트를 삭제할 수 있습니다.");
            }

            projectRepository.delete(project);
        });
    }

    private Project getProjectOrThrow(ProjectRepository projectRepository, Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다. ID: " + projectId));
    }

    private Account getAccountOrThrow(AccountRepository accountRepository, Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + accountId));
    }
}
