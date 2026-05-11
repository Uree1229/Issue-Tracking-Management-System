package com.example.its.shared.dto.project;

import java.util.List;

public class ProjectCreateRequest {

    private String name;
    private String description;
    private Long createdByAccountId;
    private List<Long> memberAccountIds;

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

    public List<Long> getMemberAccountIds() {
        return memberAccountIds;
    }

    public void setMemberAccountIds(List<Long> memberAccountIds) {
        this.memberAccountIds = memberAccountIds;
    }
}
