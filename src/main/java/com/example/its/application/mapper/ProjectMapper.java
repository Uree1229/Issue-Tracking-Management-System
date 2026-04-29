package com.example.its.application.mapper;

import com.example.its.persistence.entity.Project;
import com.example.its.shared.dto.project.ProjectResponse;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectMapper {
    
    // Entity -> Response DTO 변환
    public ProjectResponse toResponse(Project project) {
        if (project == null) return null;
        
        // PM님이 DTO에 미리 만들어두신 from() 메서드를 그대로 활용합니다!
        return ProjectResponse.from(project);
    }

    // Entity 리스트 -> Response DTO 리스트 변환
    public List<ProjectResponse> toResponseList(List<Project> projects) {
        return projects.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}