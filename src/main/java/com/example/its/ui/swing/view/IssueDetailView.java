package com.example.its.ui.swing.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class IssueDetailView extends JPanel {

    // 이슈 기본 정보 (읽기 전용)
    private final JTextField titleField       = new JTextField();
    private final JLabel statusLabel          = new JLabel();
    private final JLabel priorityLabel        = new JLabel();
    private final JLabel reporterLabel        = new JLabel();
    private final JLabel assigneeLabel        = new JLabel();
    private final JLabel fixerLabel           = new JLabel();
    private final JLabel reportedAtLabel      = new JLabel();
    private final JLabel lastModifiedAtLabel  = new JLabel();
    private final JTextArea descriptionArea   = new JTextArea(5, 40);
    private final JLabel tagsLabel            = new JLabel();

    // 코멘트 영역
    private final JTextArea commentHistoryArea = new JTextArea(8, 40);
    private final JTextArea commentInputArea   = new JTextArea(3, 40);
    private final JButton addCommentButton     = new JButton("코멘트 추가");

    // 상태 변경 버튼 (항상 표시, 잘못된 전이는 BE에서 처리)
    private final JButton assignButton  = new JButton("Assignee 지정");
    private final JButton fixButton     = new JButton("Fixed 처리");
    private final JButton failButton    = new JButton("검증 실패");
    private final JButton resolveButton = new JButton("Resolved 처리");
    private final JButton reopenButton  = new JButton("Reopen");
    private final JButton closeButton   = new JButton("Close");

    // 추천 영역
    private final JLabel recommendationLabel = new JLabel(" ");

    private final JButton editTagsButton = new JButton("태그 편집");
    private final JButton backButton     = new JButton("← 이슈 목록");

    public IssueDetailView() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        initComponents();
    }

    private void initComponents() {
        JLabel title = new JLabel("이슈 상세", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(buildInfoPanel(), BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(buildCommentPanel(), BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("이슈 상세 정보"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        titleField.setEditable(false);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        addRow(panel, gbc, 0, "제목",       titleField);
        addRow(panel, gbc, 1, "상태",       statusLabel);
        addRow(panel, gbc, 2, "우선순위",    priorityLabel);
        addRow(panel, gbc, 3, "Reporter",   reporterLabel);
        addRow(panel, gbc, 4, "Assignee",   assigneeLabel);
        addRow(panel, gbc, 5, "Fixer",      fixerLabel);
        addRow(panel, gbc, 6, "등록일",      reportedAtLabel);
        addRow(panel, gbc, 7, "최종 수정일", lastModifiedAtLabel);
        addRow(panel, gbc, 8, "태그",        tagsLabel);

        gbc.gridx = 0; gbc.gridy = 9;
        panel.add(new JLabel("설명"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(new JScrollPane(descriptionArea), gbc);

        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        recommendationLabel.setBorder(BorderFactory.createTitledBorder("Assignee 추천"));
        panel.add(recommendationLabel, gbc);

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(comp, gbc);
    }

    private JPanel buildCommentPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("코멘트"));

        commentHistoryArea.setEditable(false);
        commentHistoryArea.setLineWrap(true);
        panel.add(new JScrollPane(commentHistoryArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(4, 4));
        commentInputArea.setLineWrap(true);
        inputPanel.add(new JScrollPane(commentInputArea), BorderLayout.CENTER);
        inputPanel.add(addCommentButton, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(backButton);
        panel.add(editTagsButton);
        panel.add(assignButton);
        panel.add(fixButton);
        panel.add(failButton);
        panel.add(resolveButton);
        panel.add(reopenButton);
        panel.add(closeButton);
        return panel;
    }

    public void setTitle(String title)               { titleField.setText(title); }
    public void setStatus(String status)             { statusLabel.setText(status); }
    public void setPriority(String priority)         { priorityLabel.setText(priority); }
    public void setReporter(String reporter)         { reporterLabel.setText(reporter); }
    public void setAssignee(String assignee)         { assigneeLabel.setText(assignee); }
    public void setFixer(String fixer)               { fixerLabel.setText(fixer); }
    public void setReportedAt(String date)           { reportedAtLabel.setText(date); }
    public void setLastModifiedAt(String date)       { lastModifiedAtLabel.setText(date); }
    public void setDescription(String desc)          { descriptionArea.setText(desc); }
    public void setTags(String tags)                 { tagsLabel.setText(tags); }
    public void setCommentHistory(String history)    { commentHistoryArea.setText(history); }
    public void setRecommendations(String text)      { recommendationLabel.setText(text); }

    public String getCommentInput() { return commentInputArea.getText().trim(); }
    public void clearCommentInput() { commentInputArea.setText(""); }

    public JButton getAssignButton()     { return assignButton; }
    public JButton getFixButton()        { return fixButton; }
    public JButton getFailButton()       { return failButton; }
    public JButton getResolveButton()    { return resolveButton; }
    public JButton getReopenButton()     { return reopenButton; }
    public JButton getCloseButton()      { return closeButton; }
    public JButton getAddCommentButton() { return addCommentButton; }
    public JButton getEditTagsButton()   { return editTagsButton; }
    public JButton getBackButton()       { return backButton; }
}
