package com.example.its.shared.dto.issue;

public class MonthlyIssueStatisticsRequest {

    private Long projectId;
    private int months;

    public MonthlyIssueStatisticsRequest() {
    }

    public MonthlyIssueStatisticsRequest(Long projectId, int months) {
        this.projectId = projectId;
        this.months = months;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }
}
