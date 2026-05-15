package com.example.its.ui.swing.view.dialog;

import com.example.its.shared.dto.tag.TagResponse;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TagEditDialog extends JDialog {

    private final List<TagResponse> projectTags;
    private final List<JCheckBox> checkBoxes = new ArrayList<>();
    private final Set<Long> originalIds;
    private boolean confirmed = false;

    private final JButton confirmButton = new JButton("확인");
    private final JButton cancelButton  = new JButton("취소");

    public TagEditDialog(JFrame parent, List<TagResponse> projectTags, Set<Long> currentTagIds) {
        super(parent, "이슈 태그 편집", true);
        setSize(320, 350);
        setLocationRelativeTo(parent);
        this.projectTags = projectTags != null ? projectTags : Collections.emptyList();
        this.originalIds = currentTagIds != null ? currentTagIds : Collections.emptySet();
        initComponents();
    }

    private void initComponents() {
        JPanel checkPanel = new JPanel();
        checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));
        checkPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        if (projectTags.isEmpty()) {
            checkPanel.add(new JLabel("이 프로젝트에 등록된 태그가 없습니다."));
        } else {
            for (TagResponse tag : projectTags) {
                JCheckBox cb = new JCheckBox(tag.getName(), originalIds.contains(tag.getTagId()));
                checkBoxes.add(cb);
                checkPanel.add(cb);
            }
        }

        add(new JScrollPane(checkPanel), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(cancelButton);
        btnPanel.add(confirmButton);
        add(btnPanel, BorderLayout.SOUTH);

        confirmButton.addActionListener(e -> { confirmed = true; dispose(); });
        cancelButton.addActionListener(e -> dispose());
    }

    public List<Long> getTagIdsToAdd() {
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < projectTags.size(); i++) {
            Long tagId = projectTags.get(i).getTagId();
            if (checkBoxes.get(i).isSelected() && !originalIds.contains(tagId)) result.add(tagId);
        }
        return result;
    }

    public List<Long> getTagIdsToRemove() {
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < projectTags.size(); i++) {
            Long tagId = projectTags.get(i).getTagId();
            if (!checkBoxes.get(i).isSelected() && originalIds.contains(tagId)) result.add(tagId);
        }
        return result;
    }

    public boolean isConfirmed() { return confirmed; }
}
