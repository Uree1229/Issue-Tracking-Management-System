package com.example.its.shared.dto.project;

public class ProjectUpdateRequest {

    private String name;
    private String description;

    public ProjectUpdateRequest() {
    }

    public ProjectUpdateRequest(String name, String description) {
        this.name = name;
        this.description = description;
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
