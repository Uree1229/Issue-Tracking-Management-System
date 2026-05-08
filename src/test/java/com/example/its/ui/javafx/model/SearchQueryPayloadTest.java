package com.example.its.ui.javafx.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchQueryPayloadTest {

    @Test
    void keywordOnlyFactorySetsExpectedDefaults() {
        SearchQueryPayload payload = SearchQueryPayload.keywordOnly("login");

        assertNull(payload.issueId());
        assertEquals("login", payload.keyword());
        assertNull(payload.status());
        assertNull(payload.priority());
        assertNull(payload.reporterAccountId());
        assertNull(payload.assigneeAccountId());
        assertNull(payload.projectId());
        assertTrue(payload.includeDescription());
    }
}
