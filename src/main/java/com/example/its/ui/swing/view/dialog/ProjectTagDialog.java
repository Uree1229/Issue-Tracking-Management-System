package com.example.its.ui.swing.view.dialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectTagDialog extends JDialog {

    private final JTextField tagNamesField = new JTextField(25);
    private final JButton applyButton  = new JButton("추가");
    private final JButton cancelButton = new JButton("취소");
    private final JLabel errorLabel    = new JLabel(" ");

    public ProjectTagDialog(JFrame parent) {
        super(parent, "프로젝트 태그 추가", true);
        setSize(420, 160);
        setLocationRelativeTo(parent);
        initComponents();
    }

    private void initComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("태그명 (쉼표로 구분)"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(tagNamesField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        errorLabel.setForeground(java.awt.Color.RED);
        form.add(errorLabel, gbc);

        add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(cancelButton);
        btnPanel.add(applyButton);
        add(btnPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> dispose());
    }

    public List<String> getTagNames() {
        return Arrays.stream(tagNamesField.getText().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    public void showError(String msg) { errorLabel.setText(msg); }

    public JButton getApplyButton()  { return applyButton; }
    public JButton getCancelButton() { return cancelButton; }
}
