package com.example.its.ui.swing.view.dialog;

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

public class AssigneeDialog extends JDialog {

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> devList = new JList<>(listModel);

    private final JButton confirmButton = new JButton("Assign");
    private final JButton cancelButton  = new JButton("취소");

    private Long selectedAccountId = null;

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

    // TODO: AccountResponse 리스트로 교체, 추천 순위 함께 표시
    public void setDevelopers(String[] displayNames) {
        listModel.clear();
        for (String name : displayNames) listModel.addElement(name);
    }

    public int getSelectedIndex() { return devList.getSelectedIndex(); }

    public Long getSelectedAccountId() { return selectedAccountId; }
    public void setSelectedAccountId(Long id) { this.selectedAccountId = id; }

    public JButton getConfirmButton() { return confirmButton; }
    public JButton getCancelButton()  { return cancelButton; }
}
