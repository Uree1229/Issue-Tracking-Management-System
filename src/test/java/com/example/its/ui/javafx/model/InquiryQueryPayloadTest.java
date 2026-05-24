package com.example.its.ui.javafx.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InquiryQueryPayloadTest {

    @Test
    void recentFactoryBuildsRecentOnlyPayload() {
        // Given: the user opens the recent-issues inquiry flow
        InquiryQueryPayload payload = InquiryQueryPayload.recent();

        // Then: the payload should request recent issues without an explicit id
        assertNull(payload.issueId());
        assertEquals("", payload.keyword());
        assertTrue(payload.recentOnly());
    }
}

