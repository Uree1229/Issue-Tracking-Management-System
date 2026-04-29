package com.example.its.application.service;

import com.example.its.persistence.entity.Account;
import com.example.its.persistence.entity.Project;
import com.example.its.persistence.repository.AccountRepository;
import com.example.its.persistence.repository.ProjectRepository;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectUpdateRequest;

import java.util.List;

public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;

    public ProjectService(ProjectRepository projectRepository, AccountRepository accountRepository) {
        this.projectRepository = projectRepository;
        this.accountRepository = accountRepository;
    }

    // 1. 프로젝트 Create
    public Project createProject(ProjectCreateRequest request) {
        // 1. 프로젝트 이름 중복 검사
        if (projectRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 프로젝트 이름입니다: " + request.getName());
        }

        // 2. 생성자 존재 여부 확인
        Account creator = accountRepository.findById(request.getCreatedByAccountId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + request.getCreatedByAccountId()));

        // 3. 정적 팩토리 메서드를 사용하여 엔티티 생성
        // 04-29 회의내용에 따라 static factory method 사용하도록 수정
        Project project = Project.create(
            request.getName(), 
            request.getDescription(), 
            creator
        );

        return projectRepository.save(project);
    }

    // 2. 프로젝트 정보 업데이트
    public Project updateProject(Long projectId, ProjectUpdateRequest request) {
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

        return projectRepository.save(project);
    }

    // 3. 프로젝트 Read(조회)
    public Project getProject(Long projectId) {
        return getProjectOrThrow(projectId);
    }

    public Project getProjectByName(String name) {
        return projectRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다. 이름: " + name));
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getProjectsByCreator(Long accountId) {
        return projectRepository.findByCreatedByAccountId(accountId);
    }

    // 4. 프로젝트 Delete
    public void deleteProject(Long projectId) {
        Project project = getProjectOrThrow(projectId);
        
        // TODO: 연관된 비즈니스 정책이 있었는지 확인 필요
        // 만약 이슈가 존재하는 프로젝트 삭제불가 정책이 있었다면 주석 해제
        // if (!project.getIssues().isEmpty()) {
        //     throw new IllegalStateException("이슈가 존재하는 프로젝트는 삭제할 수 없습니다. 먼저 이슈를 정리해 주세요.");
        // }
        
        projectRepository.delete(project);
    }

    // 내부 헬퍼 메서드
    private Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다. ID: " + projectId));
    }
}