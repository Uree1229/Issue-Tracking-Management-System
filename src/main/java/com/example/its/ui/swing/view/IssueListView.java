package com.example.its.ui.swing.view;

import com.example.its.ui.swing.model.IssueTableModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class IssueListView extends JPanel {

    // 검색 필터
    private final JComboBox<String> statusFilter   = new JComboBox<>(
            new String[]{"전체", "NEW", "ASSIGNED", "FIXED", "RESOLVED", "CLOSED", "REOPENED"});
    private final JComboBox<String> priorityFilter = new JComboBox<>(
            new String[]{"전체", "BLOCKER", "CRITICAL", "MAJOR", "MINOR", "TRIVIAL"});
    private final JTextField reporterFilter  = new JTextField(10);
    private final JTextField assigneeFilter  = new JTextField(10);
    private final JTextField keywordFilter   = new JTextField(15);
    private final JButton searchButton       = new JButton("검색");
    private final JButton resetButton        = new JButton("초기화");

    // 이슈 목록 테이블
    private final IssueTableModel issueTableModel = new IssueTableModel();
    private final JTable table = new JTable(issueTableModel);

    // 하단 버튼
    private final JButton createButton     = new JButton("이슈 등록");
    private final JButton statisticsButton = new JButton("통계 보기");
    private final JButton backButton       = new JButton("← 프로젝트 목록");

    public IssueListView() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        initComponents();
    }

    private void initComponents() {
        JLabel title = new JLabel("이슈 목록", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(title, BorderLayout.NORTH);
        northPanel.add(buildFilterPanel(), BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(backButton);
        btnPanel.add(createButton);
        btnPanel.add(statisticsButton);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("검색 필터"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("상태"), gbc);
        gbc.gridx = 1; panel.add(statusFilter, gbc);

        gbc.gridx = 2; panel.add(new JLabel("우선순위"), gbc);
        gbc.gridx = 3; panel.add(priorityFilter, gbc);

        gbc.gridx = 4; panel.add(new JLabel("Reporter"), gbc);
        gbc.gridx = 5; panel.add(reporterFilter, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Assignee"), gbc);
        gbc.gridx = 1; panel.add(assigneeFilter, gbc);

        gbc.gridx = 2; panel.add(new JLabel("키워드"), gbc);
        gbc.gridx = 3; gbc.gridwidth = 2; panel.add(keywordFilter, gbc);

        gbc.gridx = 5; gbc.gridwidth = 1; panel.add(searchButton, gbc);
        gbc.gridx = 6; panel.add(resetButton, gbc);

        return panel;
    }

    public String getStatusFilter()   { return (String) statusFilter.getSelectedItem(); }
    public String getPriorityFilter() { return (String) priorityFilter.getSelectedItem(); }
    public String getReporterFilter() { return reporterFilter.getText().trim(); }
    public String getAssigneeFilter() { return assigneeFilter.getText().trim(); }
    public String getKeywordFilter()  { return keywordFilter.getText().trim(); }

    public void resetFilters() {
        statusFilter.setSelectedIndex(0);
        priorityFilter.setSelectedIndex(0);
        reporterFilter.setText("");
        assigneeFilter.setText("");
        keywordFilter.setText("");
    }

    public IssueTableModel getIssueTableModel() { return issueTableModel; }
    public JTable getTable() { return table; }
    public int getSelectedRow() { return table.getSelectedRow(); }

    public void clearSelection() { table.clearSelection(); }

    public JButton getSearchButton()     { return searchButton; }
    public JButton getResetButton()      { return resetButton; }
    public JButton getCreateButton()     { return createButton; }
    public JButton getStatisticsButton() { return statisticsButton; }
    public JButton getBackButton()       { return backButton; }

    public void setStatisticsButtonVisible(boolean visible) {
        statisticsButton.setVisible(visible);
    }

    public void setAssigneeFilterText(String text) {
        assigneeFilter.setText(text);
    }

    public void setAssigneeFilterEditable(boolean editable) {
        assigneeFilter.setEditable(editable);
    }
}
