package com.example.its.ui.swing.model;

import com.example.its.persistence.entity.IssueStatus;
import com.example.its.persistence.entity.Priority;
import com.example.its.shared.dto.issue.IssueSummaryResponse;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IssueTableModelTest {

    @Test
    void setRowsPopulatesColumnsInTheExpectedOrder() {
        // Given: a single issue with all summary fields populated
        IssueSummaryResponse issue = new IssueSummaryResponse(
                42L, "로그인 화면 깨짐", IssueStatus.ASSIGNED, Priority.MAJOR,
                7L, "tester1", 9L, "dev1", 100L,
                LocalDateTime.of(2026, 5, 1, 9, 30),
                LocalDateTime.of(2026, 5, 2, 10, 0)
        );
        IssueTableModel model = new IssueTableModel();

        // When: the row is pushed into the table model
        model.setRows(List.of(issue));

        // Then: each column should map to the expected field per the contract
        assertEquals(1, model.getRowCount());
        assertEquals(42L,            model.getValueAt(0, 0));
        assertEquals("로그인 화면 깨짐", model.getValueAt(0, 1));
        assertEquals("ASSIGNED",     model.getValueAt(0, 2));
        assertEquals("MAJOR",        model.getValueAt(0, 3));
        assertEquals("tester1",      model.getValueAt(0, 4));
        assertEquals("dev1",         model.getValueAt(0, 5));
        assertEquals("2026-05-01",   model.getValueAt(0, 6));
    }

    @Test
    void nullEnumsAndDatesAreRenderedAsEmptyStrings() {
        // Given: an issue with null status/priority/assignee/date (e.g., unassigned NEW issue mid-load)
        IssueSummaryResponse issue = new IssueSummaryResponse(
                1L, "신규 이슈", null, null,
                null, null, null, null, null,
                null, null
        );
        IssueTableModel model = new IssueTableModel();
        model.setRows(List.of(issue));

        // Then: nullables should fall back to "" so the UI never shows "null"
        assertEquals("", model.getValueAt(0, 2));
        assertEquals("", model.getValueAt(0, 3));
        assertEquals("", model.getValueAt(0, 4));
        assertEquals("", model.getValueAt(0, 5));
        assertEquals("", model.getValueAt(0, 6));
    }

    @Test
    void clearRowsResetsModelAndGetIssueIdAtHandlesOutOfRange() {
        // Given: a populated model
        IssueTableModel model = new IssueTableModel();
        model.setRows(List.of(
                new IssueSummaryResponse(10L, "a", IssueStatus.NEW, Priority.MAJOR,
                        null, null, null, null, null, null, null),
                new IssueSummaryResponse(11L, "b", IssueStatus.NEW, Priority.MAJOR,
                        null, null, null, null, null, null, null)
        ));

        // Then: getIssueIdAt should resolve in-range rows, return null for out-of-range
        assertEquals(10L, model.getIssueIdAt(0));
        assertEquals(11L, model.getIssueIdAt(1));
        assertNull(model.getIssueIdAt(-1));
        assertNull(model.getIssueIdAt(99));

        // When: rows are cleared
        model.clearRows();

        // Then: the model is empty and getIssueIdAt no longer resolves any row
        assertEquals(0, model.getRowCount());
        assertNull(model.getIssueIdAt(0));
    }

    @Test
    void emptyInitialStateExposesZeroRowsAndAllSevenColumns() {
        // Given: a fresh table model with no data
        IssueTableModel model = new IssueTableModel();
        model.setRows(Collections.emptyList());

        // Then: column count matches the spec'd UI header set ID/제목/상태/우선순위/Reporter/Assignee/등록일
        assertEquals(0, model.getRowCount());
        assertEquals(7, model.getColumnCount());
        assertEquals("ID",       model.getColumnName(0));
        assertEquals("제목",      model.getColumnName(1));
        assertEquals("상태",      model.getColumnName(2));
        assertEquals("우선순위",  model.getColumnName(3));
        assertEquals("Reporter", model.getColumnName(4));
        assertEquals("Assignee", model.getColumnName(5));
        assertEquals("등록일",    model.getColumnName(6));
    }
}
