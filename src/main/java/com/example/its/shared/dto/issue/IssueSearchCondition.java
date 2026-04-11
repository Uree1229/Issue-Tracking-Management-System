package com.example.its.shared.dto.issue;

import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;

public class IssueSearchCondition {

    private Long projectId;
    private IssueStatus status;
    private Priority priority;
    private Long reporterAccountId;
    private Long assigneeAccountId;
    private String keyword;

    public IssueSearchCondition() {
    }

    public IssueSearchCondition(Long projectId, IssueStatus status, Priority priority,
                                Long reporterAccountId, Long assigneeAccountId, String keyword) {
        this.projectId = projectId;
        this.status = status;
        this.priority = priority;
        this.reporterAccountId = reporterAccountId;
        this.assigneeAccountId = assigneeAccountId;
        this.keyword = keyword;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
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

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
