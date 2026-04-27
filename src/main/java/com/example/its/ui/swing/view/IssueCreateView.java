package com.example.its.ui.swing.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class IssueCreateView extends JPanel {

    private final JTextField titleField    = new JTextField(30);
    private final JTextArea descArea       = new JTextArea(6, 30);
    private final JComboBox<String> priorityBox = new JComboBox<>(
            new String[]{"BLOCKER", "CRITICAL", "MAJOR", "MINOR", "TRIVIAL"});

    // 자동 채움 (읽기 전용 표시용)
    private final JLabel reporterLabel     = new JLabel();
    private final JLabel reportedAtLabel   = new JLabel();

    private final JButton submitButton     = new JButton("등록");
    private final JButton cancelButton     = new JButton("취소");

    public IssueCreateView() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        initComponents();
    }

    private void initComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("이슈 등록"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        priorityBox.setSelectedItem("MAJOR");

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("제목 *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("설명 *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(new JScrollPane(descArea), gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(new JLabel("우선순위"), gbc);
        gbc.gridx = 1;
        form.add(priorityBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("Reporter (자동)"), gbc);
        gbc.gridx = 1;
        form.add(reporterLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        form.add(new JLabel("등록일 (자동)"), gbc);
        gbc.gridx = 1;
        form.add(reportedAtLabel, gbc);

        add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(cancelButton);
        btnPanel.add(submitButton);
        add(btnPanel, BorderLayout.SOUTH);
    }

    public String getTitle()       { return titleField.getText().trim(); }
    public String getDescription() { return descArea.getText().trim(); }
    public String getPriority()    { return (String) priorityBox.getSelectedItem(); }

    public void setReporter(String name)   { reporterLabel.setText(name); }
    public void setReportedAt(String date) { reportedAtLabel.setText(date); }

    public void clearForm() {
        titleField.setText("");
        descArea.setText("");
        priorityBox.setSelectedItem("MAJOR");
    }

    public JButton getSubmitButton() { return submitButton; }
    public JButton getCancelButton() { return cancelButton; }
}
