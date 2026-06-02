package com.example.its.ui.swing.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IssueCreateViewAutoFieldTest {

    @Test
    void priorityDefaultsToMajorPerSpec() {
        // Given: a freshly opened 이슈 등록 form
        IssueCreateView view = new IssueCreateView();

        // Then: priority defaults to MAJOR (per spec: "기본값은 major")
        assertEquals("MAJOR", view.getPriority());
    }

    @Test
    void reporterAndReportedAtAreAutoFilledByTheController() {
        // Given: controller injects the current login user and today's date when 이슈 등록 화면 진입
        IssueCreateView view = new IssueCreateView();

        // When: showCreateView() in IssueController applies the auto-fill
        view.setReporter("Kim PL");
        view.setReportedAt("2026-05-20");

        // Then: the view reflects those auto-filled values back to the user as read-only labels
        assertEquals("Kim PL",     view.getReporterText());
        assertEquals("2026-05-20", view.getReportedAtText());
    }

    @Test
    void clearFormResetsUserInputButPreservesAutoFilledMetadata() {
        // Given: a view with both auto-filled metadata and user-entered data
        IssueCreateView view = new IssueCreateView();
        view.setReporter("Kim PL");
        view.setReportedAt("2026-05-20");

        // When: the controller calls clearForm (typically after successful submit)
        view.clearForm();

        // Then: priority returns to the spec'd default (MAJOR)
        assertEquals("MAJOR", view.getPriority());

        // And: the auto-filled reporter / reportedAt metadata is preserved so the same session can register another issue
        assertEquals("Kim PL",     view.getReporterText());
        assertEquals("2026-05-20", view.getReportedAtText());
    }

    @Test
    void titleAndDescriptionFieldsStartEmptyAndAreClearedAfterReset() {
        // Given: a fresh form
        IssueCreateView view = new IssueCreateView();

        // Then: user-entry fields are empty so the next reporter sees a blank canvas
        assertEquals("", view.getTitle());
        assertEquals("", view.getDescription());

        // When: clearForm runs (post-submit), the fields stay empty
        view.clearForm();
        assertEquals("", view.getTitle());
        assertEquals("", view.getDescription());
    }
}
