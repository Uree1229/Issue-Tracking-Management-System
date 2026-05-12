package com.example.its.application.facade;

import com.example.its.application.service.ProjectService;
import com.example.its.application.mapper.ProjectMapper;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.shared.dto.project.ProjectTagUpdateRequest;

import java.util.List;

public class ProjectFacade {

    private final ProjectService projectService;

    // Fix: 파라미터 없는 기본 생성자로 변경 (5/4 피드백 반영)
    public ProjectFacade() {
        ProjectMapper projectMapper = new ProjectMapper();
        this.projectService = new ProjectService(projectMapper);
    }

    public ProjectResponse createProject(ProjectCreateRequest request) {
        return projectService.createProject(request);
    }

    public ProjectResponse updateProjectTags(ProjectTagUpdateRequest request) {
        return projectService.updateProjectTags(request);
    }

    public ProjectResponse getProject(Long id) {
        return projectService.getProject(id);
    }

    public List<ProjectResponse> getAllProjects() {
        return projectService.getAllProjects();
    }
}
