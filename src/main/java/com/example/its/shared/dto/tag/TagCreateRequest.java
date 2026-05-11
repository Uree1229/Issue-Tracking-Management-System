package com.example.its.shared.dto.tag;

public class TagCreateRequest {

    private Long projectId;
    private String name;
    private String description;

    public TagCreateRequest() {
    }

    public TagCreateRequest(Long projectId, String name, String description) {
        this.projectId = projectId;
        this.name = name;
        this.description = description;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
