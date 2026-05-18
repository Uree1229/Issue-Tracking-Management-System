package com.example.its.shared.dto.issue;

public class DailyIssueStatisticsRequest {

    private Long projectId;
    private int days;

    public DailyIssueStatisticsRequest() {
    }

    public DailyIssueStatisticsRequest(Long projectId, int days) {
        this.projectId = projectId;
        this.days = days;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
