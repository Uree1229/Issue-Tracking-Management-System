package com.example.its.shared.dto.issue;

import java.util.ArrayList;
import java.util.List;

public class IssueTagUpdateRequest {

    private Long issueId;
    private List<Long> tagIdsToAdd = new ArrayList<>();
    private List<Long> tagIdsToRemove = new ArrayList<>();

    public IssueTagUpdateRequest() {
    }

    public IssueTagUpdateRequest(Long issueId, List<Long> tagIdsToAdd, List<Long> tagIdsToRemove) {
        this.issueId = issueId;
        if (tagIdsToAdd != null) {
            this.tagIdsToAdd = new ArrayList<>(tagIdsToAdd);
        }
        if (tagIdsToRemove != null) {
            this.tagIdsToRemove = new ArrayList<>(tagIdsToRemove);
        }
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public List<Long> getTagIdsToAdd() {
        return tagIdsToAdd;
    }

    public void setTagIdsToAdd(List<Long> tagIdsToAdd) {
        this.tagIdsToAdd = tagIdsToAdd == null ? new ArrayList<>() : new ArrayList<>(tagIdsToAdd);
    }

    public List<Long> getTagIdsToRemove() {
        return tagIdsToRemove;
    }

    public void setTagIdsToRemove(List<Long> tagIdsToRemove) {
        this.tagIdsToRemove = tagIdsToRemove == null ? new ArrayList<>() : new ArrayList<>(tagIdsToRemove);
    }
}
