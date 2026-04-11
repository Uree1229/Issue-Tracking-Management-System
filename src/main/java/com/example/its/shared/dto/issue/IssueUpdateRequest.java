package com.example.its.shared.dto.issue;

import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;

import java.util.ArrayList;
import java.util.List;

public class IssueUpdateRequest {

    private String title;
    private String description;
    private Priority priority;
    private IssueStatus status;
    private Long assigneeAccountId;
    private Long fixerAccountId;
    private List<Long> tagIds = new ArrayList<>();

    public IssueUpdateRequest() {
    }

    public IssueUpdateRequest(String title, String description, Priority priority, IssueStatus status,
                              Long assigneeAccountId, Long fixerAccountId, List<Long> tagIds) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.assigneeAccountId = assigneeAccountId;
        this.fixerAccountId = fixerAccountId;
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

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public Long getAssigneeAccountId() {
        return assigneeAccountId;
    }

    public void setAssigneeAccountId(Long assigneeAccountId) {
        this.assigneeAccountId = assigneeAccountId;
    }

    public Long getFixerAccountId() {
        return fixerAccountId;
    }

    public void setFixerAccountId(Long fixerAccountId) {
        this.fixerAccountId = fixerAccountId;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds == null ? new ArrayList<>() : new ArrayList<>(tagIds);
    }
}
