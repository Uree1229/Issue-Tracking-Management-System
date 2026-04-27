package com.example.its.ui.swing.model;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * 이슈 목록 JTable용 모델.
 * TODO: BE 완성 후 Object[][]를 List<IssueSummaryResponse>로 교체
 */
public class IssueTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"ID", "제목", "상태", "우선순위", "Reporter", "Assignee", "등록일"};

    // [issueId, title, status, priority, reporterName, assigneeName, reportedAt]
    private final List<Object[]> rows = new ArrayList<>();

    @Override
    public int getRowCount() { return rows.size(); }

    @Override
    public int getColumnCount() { return COLUMNS.length; }

    @Override
    public String getColumnName(int col) { return COLUMNS[col]; }

    @Override
    public Object getValueAt(int row, int col) { return rows.get(row)[col]; }

    @Override
    public boolean isCellEditable(int row, int col) { return false; }

    public void setRows(List<Object[]> data) {
        rows.clear();
        rows.addAll(data);
        fireTableDataChanged();
    }

    public void clearRows() {
        rows.clear();
        fireTableDataChanged();
    }

    /** 선택된 행의 issueId(첫 번째 컬럼) 반환 */
    public Long getIssueIdAt(int row) {
        if (row < 0 || row >= rows.size()) return null;
        Object val = rows.get(row)[0];
        return val instanceof Long ? (Long) val : Long.valueOf(val.toString());
    }
}
