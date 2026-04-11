package com.example.its.shared.dto.issue;

import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatisticsResponse {

    private long totalIssueCount;
    private Map<IssueStatus, Long> statusCounts = new LinkedHashMap<>();
    private Map<Priority, Long> priorityCounts = new LinkedHashMap<>();

    public StatisticsResponse() {
    }

    public StatisticsResponse(long totalIssueCount, Map<IssueStatus, Long> statusCounts,
                              Map<Priority, Long> priorityCounts) {
        this.totalIssueCount = totalIssueCount;
        if (statusCounts != null) {
            this.statusCounts = new LinkedHashMap<>(statusCounts);
        }
        if (priorityCounts != null) {
            this.priorityCounts = new LinkedHashMap<>(priorityCounts);
        }
    }

    public long getTotalIssueCount() {
        return totalIssueCount;
    }

    public void setTotalIssueCount(long totalIssueCount) {
        this.totalIssueCount = totalIssueCount;
    }

    public Map<IssueStatus, Long> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<IssueStatus, Long> statusCounts) {
        this.statusCounts = statusCounts == null ? new LinkedHashMap<>() : new LinkedHashMap<>(statusCounts);
    }

    public Map<Priority, Long> getPriorityCounts() {
        return priorityCounts;
    }

    public void setPriorityCounts(Map<Priority, Long> priorityCounts) {
        this.priorityCounts = priorityCounts == null ? new LinkedHashMap<>() : new LinkedHashMap<>(priorityCounts);
    }
}
