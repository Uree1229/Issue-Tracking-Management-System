package com.example.its.shared.dto.project;

public class ProjectCreateRequest {

    private String name;
    private String description;
    private Long createdByAccountId;

    public ProjectCreateRequest() {
    }

    public ProjectCreateRequest(String name, String description, Long createdByAccountId) {
        this.name = name;
        this.description = description;
        this.createdByAccountId = createdByAccountId;
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

    public Long getCreatedByAccountId() {
        return createdByAccountId;
    }

    public void setCreatedByAccountId(Long createdByAccountId) {
        this.createdByAccountId = createdByAccountId;
    }
}
