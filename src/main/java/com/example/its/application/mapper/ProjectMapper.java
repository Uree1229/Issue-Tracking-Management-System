package com.example.its.application.mapper;

import com.example.its.persistence.entity.Project;
import com.example.its.shared.dto.project.ProjectResponse;
import com.example.its.shared.dto.tag.TagResponse;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectMapper {
    
    // Entity -> Response DTO 변환
    public ProjectResponse toResponse(Project project) {
        if (project == null) return null;

        // 1. 엔티티에 있는 태그들을 꺼내서 TagResponse 객체 리스트로 변환
        List<TagResponse> tagResponses = project.getTags().stream()
                .map(tag -> new TagResponse(
                        tag.getTagId(),
                        tag.getProject() != null ? tag.getProject().getProjectId() : null,
                        tag.getName(),
                        tag.getDescription()
                ))
                .collect(Collectors.toList());

        // 2. 변환된 태그 리스트를 ProjectResponse 생성자에 추가
        return new ProjectResponse(
                project.getProjectId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getCreatedBy() != null ? project.getCreatedBy().getAccountId() : null,
                project.getCreatedBy() != null ? project.getCreatedBy().getLoginId() : null,
                tagResponses // FE로 날아갈 태그 데이터
        );
    }

    // Entity 리스트 -> Response DTO 리스트 변환
    public List<ProjectResponse> toResponseList(List<Project> projects) {
        return projects.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}