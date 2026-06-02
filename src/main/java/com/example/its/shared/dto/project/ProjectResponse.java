package com.example.its.shared.dto.project;

// Feat: 태그 관련 DTO 스펙 추가
import com.example.its.shared.dto.tag.TagResponse;
import java.util.List;
import java.time.LocalDateTime;

public class ProjectResponse {

    private Long projectId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private Long createdByAccountId;
    private String createdByLoginId;
    private List<TagResponse> tags;
    
    public ProjectResponse() {
    }

    public ProjectResponse(Long projectId, String name, String description, LocalDateTime createdAt,
                           Long createdByAccountId, String createdByLoginId, List<TagResponse> tags) {
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.createdByAccountId = createdByAccountId;
        this.createdByLoginId = createdByLoginId;
        this.tags = tags;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getCreatedByAccountId() {
        return createdByAccountId;
    }

    public String getCreatedByLoginId() {
        return createdByLoginId;
    }

    public List<TagResponse> getTags() { 
        return tags; 
    }
}
