package com.example.its.ui.swing.view.dialog;

import com.example.its.shared.dto.tag.TagResponse;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TagManageDialog extends JDialog {

    private final List<TagResponse> currentTags = new ArrayList<>();
    private final List<String> toAdd = new ArrayList<>();
    private final List<Long> toRemove = new ArrayList<>();

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> tagList = new JList<>(listModel);
    private final JTextField addField = new JTextField(15);
    private final JButton addButton = new JButton("추가");
    private final JButton removeButton = new JButton("삭제");
    private final JButton doneButton = new JButton("완료");
    private final JLabel errorLabel = new JLabel(" ");

    public TagManageDialog(JFrame parent, List<TagResponse> initialTags) {
        super(parent, "프로젝트 태그 관리", true);
        setSize(400, 380);
        setLocationRelativeTo(parent);
        if (initialTags != null) currentTags.addAll(initialTags);
        initComponents();
        refreshList();
    }

    private void initComponents() {
        tagList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        center.add(new JScrollPane(tagList), BorderLayout.CENTER);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addPanel.add(new JLabel("새 태그명:"));
        addPanel.add(addField);
        addPanel.add(addButton);
        addPanel.add(removeButton);
        center.add(addPanel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        errorLabel.setForeground(Color.RED);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 6, 10));
        bottom.add(errorLabel, BorderLayout.WEST);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(doneButton);
        bottom.add(btnPanel, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        addButton.addActionListener(e -> handleAdd());
        removeButton.addActionListener(e -> handleRemove());
        doneButton.addActionListener(e -> dispose());
    }

    private void refreshList() {
        listModel.clear();
        currentTags.forEach(t -> listModel.addElement("[기존] " + t.getName()));
        toAdd.forEach(name -> listModel.addElement("[추가] " + name));
    }

    private void handleAdd() {
        String name = addField.getText().trim();
        if (name.isEmpty()) return;
        boolean duplicate = currentTags.stream().anyMatch(t -> t.getName().equals(name))
                || toAdd.contains(name);
        if (duplicate) { errorLabel.setText("이미 존재하는 태그명입니다."); return; }
        errorLabel.setText(" ");
        toAdd.add(name);
        addField.setText("");
        refreshList();
    }

    private void handleRemove() {
        int idx = tagList.getSelectedIndex();
        if (idx < 0) return;
        if (idx < currentTags.size()) {
            toRemove.add(currentTags.get(idx).getTagId());
            currentTags.remove(idx);
        } else {
            toAdd.remove(idx - currentTags.size());
        }
        refreshList();
    }

    public List<String> getTagNamesToAdd() { return toAdd; }
    public List<Long> getTagIdsToRemove()  { return toRemove; }
}
