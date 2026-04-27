package com.example.its.ui.swing.view.dialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
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

public class ProjectCreateDialog extends JDialog {

    private final JTextField nameField  = new JTextField(25);
    private final JTextArea descArea    = new JTextArea(4, 25);
    private final JButton createButton  = new JButton("생성");
    private final JButton cancelButton  = new JButton("취소");
    private final JLabel errorLabel     = new JLabel(" ");

    public ProjectCreateDialog(JFrame parent) {
        super(parent, "프로젝트 생성", true);
        setSize(400, 280);
        setLocationRelativeTo(parent);
        initComponents();
    }

    private void initComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("프로젝트명 *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("설명"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(new JScrollPane(descArea), gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        errorLabel.setForeground(java.awt.Color.RED);
        form.add(errorLabel, gbc);

        add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(cancelButton);
        btnPanel.add(createButton);
        add(btnPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> dispose());
    }

    public String getProjectName()    { return nameField.getText().trim(); }
    public String getDescription()    { return descArea.getText().trim(); }
    public void showError(String msg) { errorLabel.setText(msg); }
    public void clearError()          { errorLabel.setText(" "); }

    public JButton getCreateButton() { return createButton; }
    public JButton getCancelButton() { return cancelButton; }
}
