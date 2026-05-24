package com.example.its.ui.swing.model;

import com.example.its.shared.dto.issue.IssueSummaryResponse;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class IssueTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"ID", "제목", "상태", "우선순위", "Reporter", "Assignee", "등록일"};
    private final List<IssueSummaryResponse> rows = new ArrayList<>();

    @Override public int getRowCount()  { return rows.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }
    @Override public boolean isCellEditable(int row, int col) { return false; }

    @Override
    public Object getValueAt(int row, int col) {
        IssueSummaryResponse r = rows.get(row);
        return switch (col) {
            case 0 -> r.getIssueId();
            case 1 -> r.getTitle();
            case 2 -> r.getStatus() != null ? r.getStatus().name() : "";
            case 3 -> r.getPriority() != null ? r.getPriority().name() : "";
            case 4 -> r.getReporterName() != null ? r.getReporterName() : "";
            case 5 -> r.getAssigneeName() != null ? r.getAssigneeName() : "";
            case 6 -> r.getReportedAt() != null ? r.getReportedAt().toLocalDate().toString() : "";
            default -> "";
        };
    }

    public void setRows(List<IssueSummaryResponse> data) {
        rows.clear();
        rows.addAll(data);
        fireTableDataChanged();
    }

    public void clearRows() {
        rows.clear();
        fireTableDataChanged();
    }

    public Long getIssueIdAt(int row) {
        if (row < 0 || row >= rows.size()) return null;
        return rows.get(row).getIssueId();
    }
}
