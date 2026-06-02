package com.example.its.shared.dto.project;

import java.util.ArrayList;
import java.util.List;

public class ProjectCreateRequest {

    private String name;
    private String description;
    private Long createdByAccountId;
    private List<String> tagNames = new ArrayList<>();

    public ProjectCreateRequest() {
    }

    public ProjectCreateRequest(String name, String description, Long createdByAccountId) {
        this.name = name;
        this.description = description;
        this.createdByAccountId = createdByAccountId;
    }

    public ProjectCreateRequest(String name, String description, Long createdByAccountId, List<String> tagNames) {
        this(name, description, createdByAccountId);
        if (tagNames != null) {
            this.tagNames = new ArrayList<>(tagNames);
        }
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

    public List<String> getTagNames() {
        return tagNames;
    }

    public void setTagNames(List<String> tagNames) {
        this.tagNames = tagNames == null ? new ArrayList<>() : new ArrayList<>(tagNames);
    }
}
