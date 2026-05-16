package com.example.its.ui.javafx.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchQueryPayloadTest {

    @Test
    void keywordOnlyFactorySetsExpectedDefaults() {
        // Given: a quick search request that starts from the home/search shortcut
        SearchQueryPayload payload = SearchQueryPayload.keywordOnly("login");

        // Then: only the keyword is filled and the rest of the filters stay empty
        assertNull(payload.issueId());
        assertEquals("login", payload.keyword());
        assertNull(payload.status());
        assertNull(payload.priority());
        assertNull(payload.reporterAccountId());
        assertNull(payload.assigneeAccountId());
        assertNull(payload.projectId());
        assertFalse(payload.activeOnly());
        assertTrue(payload.includeDescription());
    }

    @Test
    void constructorRetainsStructuredSearchCriteria() {
        // Given: a detailed search created from the advanced search form
        SearchQueryPayload payload = new SearchQueryPayload(
            15L,
            "assign",
            UiIssueStatus.ASSIGNED,
            UiPriority.MAJOR,
            3L,
            7L,
            100L,
            true,
            false
        );

        // Then: every structured filter should stay intact for backend handoff
        assertEquals(15L, payload.issueId());
        assertEquals("assign", payload.keyword());
        assertEquals(UiIssueStatus.ASSIGNED, payload.status());
        assertEquals(UiPriority.MAJOR, payload.priority());
        assertEquals(3L, payload.reporterAccountId());
        assertEquals(7L, payload.assigneeAccountId());
        assertEquals(100L, payload.projectId());
        assertTrue(payload.activeOnly());
        assertFalse(payload.includeDescription());
    }
}
