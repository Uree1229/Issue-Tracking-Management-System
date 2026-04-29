package com.example.its.application.facade;

import com.example.its.application.mapper.ProjectMapper;
import com.example.its.application.service.ProjectService;
import com.example.its.persistence.entity.Project;
import com.example.its.shared.dto.project.ProjectCreateRequest;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.shared.dto.project.ProjectUpdateRequest;

import java.util.List;

public class ProjectFacade {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    public ProjectFacade(ProjectService projectService, ProjectMapper projectMapper) {
        this.projectService = projectService;
        this.projectMapper = projectMapper;
    }

    public ProjectResponse createProject(ProjectCreateRequest request) {
        Project project = projectService.createProject(request);
        return projectMapper.toResponse(project);
    }

    public ProjectResponse updateProject(Long id, ProjectUpdateRequest request) {
        Project project = projectService.updateProject(id, request);
        return projectMapper.toResponse(project);
    }

    public ProjectResponse getProject(Long id) {
        Project project = projectService.getProject(id);
        return projectMapper.toResponse(project);
    }

    public List<ProjectResponse> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return projectMapper.toResponseList(projects);
    }
}