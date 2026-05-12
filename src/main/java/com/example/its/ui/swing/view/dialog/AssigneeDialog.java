package com.example.its.ui.swing.view.dialog;

import com.example.its.shared.dto.issue.RecommendationResponse;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class AssigneeDialog extends JDialog {

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> devList = new JList<>(listModel);
    private final List<Long> accountIds = new ArrayList<>();

    private final JButton confirmButton = new JButton("Assign");
    private final JButton cancelButton  = new JButton("취소");

    public AssigneeDialog(JFrame parent) {
        super(parent, "Assignee 지정", true);
        setSize(400, 350);
        setLocationRelativeTo(parent);
        initComponents();
    }

    private void initComponents() {
        devList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        devList.setBorder(BorderFactory.createTitledBorder("개발자 목록 (추천 순)"));

        add(new JScrollPane(devList), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(cancelButton);
        btnPanel.add(confirmButton);
        add(btnPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> dispose());
    }

    public void setRecommendations(List<RecommendationResponse> recommendations) {
        listModel.clear();
        accountIds.clear();
        for (RecommendationResponse r : recommendations) {
            listModel.addElement(String.format("%s (%s) [%.1f점]", r.getName(), r.getLoginId(), r.getScore()));
            accountIds.add(r.getAccountId());
        }
    }

    public int getSelectedIndex() { return devList.getSelectedIndex(); }

    public Long getSelectedAccountId() {
        int idx = devList.getSelectedIndex();
        if (idx < 0 || idx >= accountIds.size()) return null;
        return accountIds.get(idx);
    }

    public JButton getConfirmButton() { return confirmButton; }
    public JButton getCancelButton()  { return cancelButton; }
}
