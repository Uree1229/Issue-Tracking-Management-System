package com.example.its.ui.swing.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class ProjectListView extends JPanel {

    private final String[] COLUMNS = {"ID", "프로젝트명", "설명", "생성일"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JButton createButton = new JButton("프로젝트 생성");
    private final JButton deleteButton = new JButton("프로젝트 삭제");
    private final JButton selectButton = new JButton("선택");
    private final JButton logoutButton = new JButton("로그아웃");

    public ProjectListView() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        initComponents();
    }

    private void initComponents() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // 관리자 전용 버튼 — 역할 확인 후 Controller에서 표시/숨김 처리
        btnPanel.add(createButton);
        btnPanel.add(deleteButton);
        btnPanel.add(selectButton);
        btnPanel.add(logoutButton);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // TODO: BE 연결 후 ProjectResponse 리스트로 교체
    public void setProjects(Object[][] data) {
        tableModel.setRowCount(0);
        for (Object[] row : data) tableModel.addRow(row);
    }

    public int getSelectedRow() { return table.getSelectedRow(); }
    public Object getValueAt(int row, int col) { return tableModel.getValueAt(row, col); }

    public JButton getCreateButton() { return createButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getSelectButton() { return selectButton; }
    public JButton getLogoutButton() { return logoutButton; }

    public void setAdminButtonsVisible(boolean visible) {
        createButton.setVisible(visible);
        deleteButton.setVisible(visible);
    }
}
