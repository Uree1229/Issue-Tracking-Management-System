package com.example.its.ui.javafx.model;

public class AdminProjectRowModel {

    private final Long projectId;
    private final String name;
    private final String description;
    private final String version;
    private final boolean openForIssueEntry;
    private final String defaultTag;
    private final String defaultAssignee;

    public AdminProjectRowModel(
        Long projectId,
        String name,
        String description,
        String version,
        boolean openForIssueEntry,
        String defaultTag,
        String defaultAssignee
    ) {
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.version = version;
        this.openForIssueEntry = openForIssueEntry;
        this.defaultTag = defaultTag;
        this.defaultAssignee = defaultAssignee;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public boolean isOpenForIssueEntry() {
        return openForIssueEntry;
    }

    public String getDefaultTag() {
        return defaultTag;
    }

    public String getDefaultAssignee() {
        return defaultAssignee;
    }
}
