package com.example.its.application.service;

import com.example.its.application.mapper.ProjectMapper;
import com.example.its.persistence.entity.Account;
import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Project;
import com.example.its.persistence.repository.AccountRepository;
import com.example.its.persistence.repository.ProjectRepository;
import com.example.its.persistence.repository.TagRepository;
import com.example.its.persistence.transaction.TransactionManager;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.shared.dto.project.ProjectTagUpdateRequest;
import com.example.its.persistence.entity.Tag;


import java.util.List;

// Fix: ProjectMapper를 주입받아 반환값을 DTO로 맵핑하도록 수정
public class ProjectService {

    private final ProjectMapper projectMapper;

    public ProjectService(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    // 1. 프로젝트 Create
    public ProjectResponse createProject(ProjectCreateRequest request) {
        return TransactionManager.execute(entityManager -> {
            ProjectRepository projectRepository = new ProjectRepository(entityManager);
            AccountRepository accountRepository = new AccountRepository(entityManager);

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

            // 4. 생성 시 태그 추가 로직
            // 05-12 전달받은 내용에 따라 수정
            if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
                for (String tagName : request.getTagNames()) {
                    Tag tag = Tag.create(tagName, null, project);
                    project.addTag(tag);
                }
            }

            return projectMapper.toResponse(projectRepository.save(project));
        });
    }

    // 2. 프로젝트 태그 업데이트 (프로젝트 기본 정보 수정 불가)
    public ProjectResponse updateProjectTags(ProjectTagUpdateRequest request) {
        return TransactionManager.execute(entityManager -> {
            ProjectRepository projectRepository = new ProjectRepository(entityManager);
            TagRepository tagRepository = new TagRepository(entityManager); // 태그 조회를 위해 추가
            Project project = getProjectOrThrow(projectRepository, request.getProjectId());

            // 1) 태그 삭제 로직
            if (request.getTagIdsToRemove() != null) {
                for (Long tagId : request.getTagIdsToRemove()) {
                    Tag tag = tagRepository.findById(tagId)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그입니다. ID: " + tagId));

                    // 서비스 계층에서 사용 중인 태그 삭제 차단 정책 한번 더 검증
                    if (tagRepository.existsIssueUsingTag(tagId)) {
                        throw new IllegalStateException("이슈에서 사용 중인 태그는 삭제할 수 없습니다. 태그: " + tag.getName());
                    }

                    project.removeTag(tag);
                    tagRepository.delete(tag);
                }
            }

            // 2) 태그 추가 로직 (이름 수정 불가능하므로 단순 추가만 진행)
            if (request.getTagNamesToAdd() != null) {
                for (String tagName : request.getTagNamesToAdd()) {
                    // DB 제약조건(uq_tags_project_name) 위반 방지를 위해 중복 체크
                    if (!tagRepository.existsByProjectIdAndName(project.getProjectId(), tagName)) {
                        Tag newTag = Tag.create(tagName, null, project);
                        project.addTag(newTag);
                    }
                }
            }

            return projectMapper.toResponse(projectRepository.save(project));
        });
    }

    // 3. 프로젝트 Read(조회)
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

    // 4. 프로젝트 Delete
    public void deleteProject(Long projectId) {
        TransactionManager.executeVoid(entityManager -> {
            ProjectRepository projectRepository = new ProjectRepository(entityManager);
            Project project = getProjectOrThrow(projectRepository, projectId);

            // 정우님이 안내해주신 비즈니스 정책 반영하여 Fix
            // 하위 이슈가 존재하는데 그 중 하나라도 CLOSED가 아니면 삭제 불가
            boolean hasUnclosedIssue = project.getIssues().stream()
                    .anyMatch(issue -> issue.getStatus() != IssueStatus.CLOSED);

            if (hasUnclosedIssue) {
                throw new IllegalStateException("모든 이슈가 'CLOSED' 상태일 때만 프로젝트를 삭제할 수 있습니다.");
            }

            projectRepository.delete(project);
        });
    }

    // 내부 헬퍼 메서드
    private Project getProjectOrThrow(ProjectRepository projectRepository, Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다. ID: " + projectId));
    }
}
