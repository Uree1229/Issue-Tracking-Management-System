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

public class AccountManageView extends JPanel {

    private final String[] COLUMNS = {"ID", "로그인ID", "이름", "이메일", "역할", "활성화"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JButton createButton     = new JButton("계정 추가");
    private final JButton updateButton     = new JButton("수정");
    private final JButton deactivateButton = new JButton("비활성화");
    private final JButton backButton       = new JButton("← 프로젝트 목록");

    public AccountManageView() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        initComponents();
    }

    private void initComponents() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(backButton);
        btnPanel.add(createButton);
        btnPanel.add(updateButton);
        btnPanel.add(deactivateButton);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // TODO: AccountResponse 리스트로 교체
    public void setAccounts(Object[][] data) {
        tableModel.setRowCount(0);
        for (Object[] row : data) tableModel.addRow(row);
    }

    public int getSelectedRow() { return table.getSelectedRow(); }
    public Object getValueAt(int row, int col) { return tableModel.getValueAt(row, col); }

    public JButton getCreateButton()     { return createButton; }
    public JButton getUpdateButton()     { return updateButton; }
    public JButton getDeactivateButton() { return deactivateButton; }
    public JButton getBackButton()       { return backButton; }
}
