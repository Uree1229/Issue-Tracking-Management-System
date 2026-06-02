package com.example.its.ui.javafx.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CommentItemModel {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String authorName;
    private final LocalDateTime createdAt;
    private final String content;

    public CommentItemModel(String authorName, LocalDateTime createdAt, String content) {
        this.authorName = authorName;
        this.createdAt = createdAt;
        this.content = content;
    }

    public String getAuthorName() {
        return authorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getContent() {
        return content;
    }

    public String toTimelineText() {
        return "[" + createdAt.format(DATE_TIME_FORMATTER) + "] " + authorName + ": " + content;
    }
}
