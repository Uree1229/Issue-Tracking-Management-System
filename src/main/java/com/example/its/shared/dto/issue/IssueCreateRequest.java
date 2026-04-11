package com.example.its.shared.dto.issue;

import com.example.its.persistence.entity.Priority;

import java.util.ArrayList;
import java.util.List;

public class IssueCreateRequest {

    private String title;
    private String description;
    private Priority priority;
    private Long reporterAccountId;
    private Long assigneeAccountId;
    private Long projectId;
    private List<Long> tagIds = new ArrayList<>();

    public IssueCreateRequest() {
    }

    public IssueCreateRequest(String title, String description, Priority priority, Long reporterAccountId,
                              Long assigneeAccountId, Long projectId, List<Long> tagIds) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.reporterAccountId = reporterAccountId;
        this.assigneeAccountId = assigneeAccountId;
        this.projectId = projectId;
        if (tagIds != null) {
            this.tagIds = new ArrayList<>(tagIds);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Long getReporterAccountId() {
        return reporterAccountId;
    }

    public void setReporterAccountId(Long reporterAccountId) {
        this.reporterAccountId = reporterAccountId;
    }

    public Long getAssigneeAccountId() {
        return assigneeAccountId;
    }

    public void setAssigneeAccountId(Long assigneeAccountId) {
        this.assigneeAccountId = assigneeAccountId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds == null ? new ArrayList<>() : new ArrayList<>(tagIds);
    }
}
