package com.example.its.application.service;

import com.example.its.application.mapper.ProjectMapper;
import com.example.its.persistence.entity.Account;
// import com.example.its.persistence.entity.Issue;
import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Project;
import com.example.its.persistence.repository.AccountRepository;
import com.example.its.persistence.repository.ProjectRepository;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.shared.dto.project.ProjectUpdateRequest;

import java.util.List;

// Fix: ProjectMapper를 주입받아 반환값을 DTO로 맵핑하도록 수정
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;
    private final ProjectMapper projectMapper;

    public ProjectService(ProjectRepository projectRepository, AccountRepository accountRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.accountRepository = accountRepository;
        this.projectMapper = projectMapper;
    }

    // 1. 프로젝트 Create
    public ProjectResponse createProject(ProjectCreateRequest request) {
        // 1. 프로젝트 이름 중복 검사
        if (projectRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 프로젝트 이름입니다: " + request.getName());
        }

        // 2. 생성자 존재 여부 확인
        Account creator = accountRepository.findById(request.getCreatedByAccountId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + request.getCreatedByAccountId()));
        
        // 3. 정적 팩토리 메서드를 사용하여 엔티티 생성
        // 04-29 회의내용에 따라 static factory method 사용하도록 수정
        Project project = Project.create(request.getName(), request.getDescription(), creator);
        return projectMapper.toResponse(projectRepository.save(project));
    }

    // 2. 프로젝트 정보 업데이트
    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request) {
        Project project = getProjectOrThrow(projectId);
        
        // 이름 변경 시 중복 검사
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
    }

    // 3. 프로젝트 Read(조회)
    public ProjectResponse getProject(Long projectId) {
        return projectMapper.toResponse(getProjectOrThrow(projectId));
    }

    public ProjectResponse getProjectByName(String name) {
        Project project = projectRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다. 이름: " + name));
        return projectMapper.toResponse(project);
    }

    public List<ProjectResponse> getAllProjects() {
        return projectMapper.toResponseList(projectRepository.findAll());
    }

    public List<ProjectResponse> getProjectsByCreator(Long accountId) {
        return projectMapper.toResponseList(projectRepository.findByCreatedByAccountId(accountId));
    }

    // 4. 프로젝트 Delete
    public void deleteProject(Long projectId) {
        Project project = getProjectOrThrow(projectId);
        
        // 정우님이 안내해주신 비즈니스 정책 반영하여 Fix
        // 하위 이슈가 존재하는데 그 중 하나라도 CLOSED가 아니면 삭제 불가
        boolean hasUnclosedIssue = project.getIssues().stream()
                .anyMatch(issue -> issue.getStatus() != IssueStatus.CLOSED);
                
        if (hasUnclosedIssue) {
            throw new IllegalStateException("모든 이슈가 'CLOSED' 상태일 때만 프로젝트를 삭제할 수 있습니다.");
        }
        
        projectRepository.delete(project);
    }

    // 내부 헬퍼 메서드
    private Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다. ID: " + projectId));
    }
}