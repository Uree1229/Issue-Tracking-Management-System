package com.example.its.shared.dto.project;

import java.util.ArrayList;
import java.util.List;

public class ProjectTagUpdateRequest {

    private Long projectId;
    private List<String> tagNamesToAdd = new ArrayList<>();
    private List<Long> tagIdsToRemove = new ArrayList<>();

    public ProjectTagUpdateRequest() {
    }

    // project에서 태그를 추가할땐 tagId가 없으므로 tagName으로 추가, 제거는 tagId로
    public ProjectTagUpdateRequest(Long projectId, List<String> tagNamesToAdd, List<Long> tagIdsToRemove) {
        this.projectId = projectId;
        if (tagNamesToAdd != null) {
            this.tagNamesToAdd = new ArrayList<>(tagNamesToAdd);
        }
        if (tagIdsToRemove != null) {
            this.tagIdsToRemove = new ArrayList<>(tagIdsToRemove);
        }
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<String> getTagNamesToAdd() {
        return tagNamesToAdd;
    }

    public void setTagNamesToAdd(List<String> tagNamesToAdd) {
        this.tagNamesToAdd = tagNamesToAdd == null ? new ArrayList<>() : new ArrayList<>(tagNamesToAdd);
    }

    public List<Long> getTagIdsToRemove() {
        return tagIdsToRemove;
    }

    public void setTagIdsToRemove(List<Long> tagIdsToRemove) {
        this.tagIdsToRemove = tagIdsToRemove == null ? new ArrayList<>() : new ArrayList<>(tagIdsToRemove);
    }
}
