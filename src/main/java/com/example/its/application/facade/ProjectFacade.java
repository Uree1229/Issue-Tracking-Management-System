package com.example.its.application.facade;

import com.example.its.application.service.ProjectService;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.shared.dto.project.ProjectUpdateRequest;

import java.util.List;

// Fix: Mapper를 없애버리고, Service가 준 DTO를 그대로 리턴하도록 수정
public class ProjectFacade {

    private final ProjectService projectService;

    public ProjectFacade(ProjectService projectService) {
        this.projectService = projectService;
    }

    public ProjectResponse createProject(ProjectCreateRequest request) {
        return projectService.createProject(request);
    }

    public ProjectResponse updateProject(Long id, ProjectUpdateRequest request) {
        return projectService.updateProject(id, request);
    }

    public ProjectResponse getProject(Long id) {
        return projectService.getProject(id);
    }

    public List<ProjectResponse> getAllProjects() {
        return projectService.getAllProjects();
    }
}