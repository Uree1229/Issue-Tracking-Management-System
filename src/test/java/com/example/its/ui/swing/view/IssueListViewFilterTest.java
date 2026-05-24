package com.example.its.ui.swing.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IssueListViewFilterTest {

    @Test
    void freshFilterPanelDefaultsToAllAndEmptyTextFields() {
        // Given: a freshly constructed issue list view
        IssueListView view = new IssueListView();

        // Then: combo filters default to "전체" so the initial query matches all issues
        assertEquals("전체", view.getStatusFilter());
        assertEquals("전체", view.getPriorityFilter());

        // And: text filters start empty so they don't unintentionally narrow the result
        assertEquals("", view.getReporterFilter());
        assertEquals("", view.getAssigneeFilter());
        assertEquals("", view.getKeywordFilter());
    }

    @Test
    void resetFiltersRestoresAllFieldsToTheirInitialState() {
        // Given: a view whose filters have been edited by the user
        IssueListView view = new IssueListView();
        view.setAssigneeFilterText("dev1");

        // When: the user clicks 초기화 (which invokes resetFilters)
        view.resetFilters();

        // Then: all fields go back to the cleared initial state
        assertEquals("전체", view.getStatusFilter());
        assertEquals("전체", view.getPriorityFilter());
        assertEquals("", view.getReporterFilter());
        assertEquals("", view.getAssigneeFilter());
        assertEquals("", view.getKeywordFilter());
    }

    @Test
    void textFiltersStripSurroundingWhitespaceWhenRead() {
        // Given: a view where the user typed an assignee with surrounding spaces
        IssueListView view = new IssueListView();
        view.setAssigneeFilterText("   dev1   ");

        // Then: the getter trims to avoid sending whitespace to the backend search
        assertEquals("dev1", view.getAssigneeFilter());
    }

    @Test
    void statisticsButtonIsHiddenUntilExplicitlyMadeVisibleForPL() {
        // Given: a fresh list view (default: PL-only statistics button is visible by Swing default)
        IssueListView view = new IssueListView();

        // When: a non-PL user enters → controller hides the button
        view.setStatisticsButtonVisible(false);
        assertTrue(!view.getStatisticsButton().isVisible());

        // When: a PL user enters → controller shows the button
        view.setStatisticsButtonVisible(true);
        assertTrue(view.getStatisticsButton().isVisible());
    }
}
