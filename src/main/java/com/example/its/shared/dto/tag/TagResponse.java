package com.example.its.shared.dto.tag;

public class TagResponse {

    private Long tagId;
    private Long projectId;
    private String name;
    private String description;

    public TagResponse() {
    }

    public TagResponse(Long tagId, Long projectId, String name, String description) {
        this.tagId = tagId;
        this.projectId = projectId;
        this.name = name;
        this.description = description;
    }

    public Long getTagId() {
        return tagId;
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
}
