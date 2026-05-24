package com.example.its.ui.swing.view.dialog;

import com.example.its.shared.dto.tag.TagResponse;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TagEditDialog extends JDialog {

    private final List<TagResponse> projectTags;
    private final List<JCheckBox> existingCheckBoxes = new ArrayList<>();
    private final List<JCheckBox> newCheckBoxes = new ArrayList<>();
    private final List<String> newTagNames = new ArrayList<>();
    private final Set<Long> originalIds;
    private boolean confirmed = false;

    private final JPanel checkPanel = new JPanel();
    private final JTextField newTagField = new JTextField(12);
    private final JButton addNewButton = new JButton("신규 태그 추가");
    private final JLabel errorLabel = new JLabel(" ");
    private final JButton confirmButton = new JButton("확인");
    private final JButton cancelButton  = new JButton("취소");

    public TagEditDialog(JFrame parent, List<TagResponse> projectTags, Set<Long> currentTagIds) {
        super(parent, "이슈 태그 편집", true);
        setSize(360, 420);
        setLocationRelativeTo(parent);
        this.projectTags = projectTags != null ? projectTags : Collections.emptyList();
        this.originalIds = currentTagIds != null ? currentTagIds : Collections.emptySet();
        initComponents();
    }

    private void initComponents() {
        checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));
        checkPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        if (projectTags.isEmpty()) {
            checkPanel.add(new JLabel("이 프로젝트에 등록된 태그가 없습니다."));
        } else {
            for (TagResponse tag : projectTags) {
                JCheckBox cb = new JCheckBox(tag.getName(), originalIds.contains(tag.getTagId()));
                existingCheckBoxes.add(cb);
                checkPanel.add(cb);
            }
        }

        add(new JScrollPane(checkPanel), BorderLayout.CENTER);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addPanel.setBorder(BorderFactory.createTitledBorder("신규 태그 등록"));
        addPanel.add(new JLabel("새 태그명:"));
        addPanel.add(newTagField);
        addPanel.add(addNewButton);

        JPanel south = new JPanel(new BorderLayout());
        errorLabel.setForeground(Color.RED);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        south.add(addPanel, BorderLayout.NORTH);
        south.add(errorLabel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(cancelButton);
        btnPanel.add(confirmButton);
        south.add(btnPanel, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);

        addNewButton.addActionListener(e -> handleAddNew());
        confirmButton.addActionListener(e -> { confirmed = true; dispose(); });
        cancelButton.addActionListener(e -> dispose());
    }

    private void handleAddNew() {
        String name = newTagField.getText().trim();
        if (name.isEmpty()) return;

        boolean duplicate = projectTags.stream().anyMatch(t -> t.getName().equalsIgnoreCase(name))
                || newTagNames.stream().anyMatch(n -> n.equalsIgnoreCase(name));
        if (duplicate) {
            errorLabel.setText("이미 존재하는 태그명입니다.");
            return;
        }
        errorLabel.setText(" ");

        JCheckBox cb = new JCheckBox("[신규] " + name, true);
        newCheckBoxes.add(cb);
        newTagNames.add(name);
        checkPanel.add(cb);
        checkPanel.revalidate();
        checkPanel.repaint();
        newTagField.setText("");
    }

    public List<Long> getTagIdsToAdd() {
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < projectTags.size(); i++) {
            Long tagId = projectTags.get(i).getTagId();
            if (existingCheckBoxes.get(i).isSelected() && !originalIds.contains(tagId)) result.add(tagId);
        }
        return result;
    }

    public List<Long> getTagIdsToRemove() {
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < projectTags.size(); i++) {
            Long tagId = projectTags.get(i).getTagId();
            if (!existingCheckBoxes.get(i).isSelected() && originalIds.contains(tagId)) result.add(tagId);
        }
        return result;
    }

    public List<String> getNewTagNamesToCreate() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < newTagNames.size(); i++) {
            if (newCheckBoxes.get(i).isSelected()) result.add(newTagNames.get(i));
        }
        return result;
    }

    public boolean isConfirmed() { return confirmed; }
}
