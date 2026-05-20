package com.example.its.ui.swing.view;

import com.example.its.shared.dto.tag.TagResponse;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

public class IssueCreateView extends JPanel {

    private final JTextField titleField    = new JTextField(30);
    private final JTextArea descArea       = new JTextArea(6, 30);
    private final JComboBox<String> priorityBox = new JComboBox<>(
            new String[]{"BLOCKER", "CRITICAL", "MAJOR", "MINOR", "TRIVIAL"});

    private final JLabel reporterLabel     = new JLabel();
    private final JLabel reportedAtLabel   = new JLabel();

    // 태그: 기존(체크박스로 등록/해제) + 신규(입력 후 추가, 체크박스로 해제 가능)
    private final JPanel tagCheckPanel = new JPanel();
    private final List<JCheckBox> existingTagBoxes = new ArrayList<>();
    private final List<Long> existingTagIds = new ArrayList<>();
    private final List<JCheckBox> newTagBoxes = new ArrayList<>();
    private final List<String> newTagNames = new ArrayList<>();
    private final JTextField newTagField = new JTextField(12);
    private final JButton addNewTagButton = new JButton("신규 태그 추가");
    private final JLabel tagErrorLabel = new JLabel(" ");

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

        tagCheckPanel.setLayout(new BoxLayout(tagCheckPanel, BoxLayout.Y_AXIS));
        tagCheckPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        addNewTagButton.addActionListener(e -> handleAddNewTag());
        tagErrorLabel.setForeground(Color.RED);

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
        form.add(new JLabel("태그"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(buildTagPanel(), gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        form.add(new JLabel("Reporter (자동)"), gbc);
        gbc.gridx = 1;
        form.add(reporterLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        form.add(new JLabel("등록일 (자동)"), gbc);
        gbc.gridx = 1;
        form.add(reportedAtLabel, gbc);

        add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(cancelButton);
        btnPanel.add(submitButton);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel buildTagPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("태그 등록/해제"));

        JScrollPane scroll = new JScrollPane(tagCheckPanel);
        scroll.setPreferredSize(new Dimension(0, 120));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        addPanel.add(new JLabel("새 태그명:"));
        addPanel.add(newTagField);
        addPanel.add(addNewTagButton);
        addPanel.add(tagErrorLabel);
        panel.add(addPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void handleAddNewTag() {
        String name = newTagField.getText().trim();
        if (name.isEmpty()) return;

        boolean duplicateExisting = existingTagBoxes.stream()
                .anyMatch(cb -> cb.getText().equalsIgnoreCase(name));
        boolean duplicateNew = newTagNames.stream().anyMatch(n -> n.equalsIgnoreCase(name));
        if (duplicateExisting || duplicateNew) {
            tagErrorLabel.setText("이미 존재하는 태그명입니다.");
            return;
        }
        tagErrorLabel.setText(" ");

        JCheckBox cb = new JCheckBox("[신규] " + name, true);
        newTagBoxes.add(cb);
        newTagNames.add(name);
        tagCheckPanel.add(cb);
        tagCheckPanel.revalidate();
        tagCheckPanel.repaint();
        newTagField.setText("");
    }

    public String getTitle()       { return titleField.getText().trim(); }
    public String getDescription() { return descArea.getText().trim(); }
    public String getPriority()    { return (String) priorityBox.getSelectedItem(); }

    public void setReporter(String name)   { reporterLabel.setText(name); }
    public void setReportedAt(String date) { reportedAtLabel.setText(date); }

    public void setAvailableTags(List<TagResponse> tags) {
        existingTagBoxes.clear();
        existingTagIds.clear();
        newTagBoxes.clear();
        newTagNames.clear();
        tagCheckPanel.removeAll();

        if (tags != null) {
            for (TagResponse tag : tags) {
                JCheckBox cb = new JCheckBox(tag.getName(), false);
                existingTagBoxes.add(cb);
                existingTagIds.add(tag.getTagId());
                tagCheckPanel.add(cb);
            }
        }
        tagCheckPanel.revalidate();
        tagCheckPanel.repaint();
    }

    public List<Long> getSelectedExistingTagIds() {
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < existingTagBoxes.size(); i++) {
            if (existingTagBoxes.get(i).isSelected()) {
                result.add(existingTagIds.get(i));
            }
        }
        return result;
    }

    public List<String> getNewTagNamesToCreate() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < newTagBoxes.size(); i++) {
            if (newTagBoxes.get(i).isSelected()) {
                result.add(newTagNames.get(i));
            }
        }
        return result;
    }

    public void clearForm() {
        titleField.setText("");
        descArea.setText("");
        priorityBox.setSelectedItem("MAJOR");
        newTagField.setText("");
        tagErrorLabel.setText(" ");
        newTagBoxes.clear();
        newTagNames.clear();
        for (JCheckBox cb : existingTagBoxes) cb.setSelected(false);
        tagCheckPanel.removeAll();
        for (JCheckBox cb : existingTagBoxes) tagCheckPanel.add(cb);
        tagCheckPanel.revalidate();
        tagCheckPanel.repaint();
    }

    public JButton getSubmitButton() { return submitButton; }
    public JButton getCancelButton() { return cancelButton; }
}
