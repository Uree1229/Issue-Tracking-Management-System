package com.example.its.ui.javafx.model;

public record InquiryQueryPayload(
    Long issueId,
    String keyword,
    boolean recentOnly
) {

    public static InquiryQueryPayload recent() {
        return new InquiryQueryPayload(null, "", true);
    }
}
