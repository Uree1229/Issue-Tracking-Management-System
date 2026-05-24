package com.example.its.ui.swing.view.dialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class AccountUpdateDialog extends JDialog {

    private final JTextField nameField        = new JTextField(20);
    private final JTextField emailField       = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton saveButton   = new JButton("저장");
    private final JButton cancelButton = new JButton("취소");
    private final JLabel errorLabel    = new JLabel(" ");

    public AccountUpdateDialog(JFrame parent, String currentName, String currentEmail) {
        super(parent, "계정 수정", true);
        setSize(400, 260);
        setLocationRelativeTo(parent);
        nameField.setText(currentName);
        emailField.setText(currentEmail);
        initComponents();
    }

    private void initComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gbc, 0, "이름 *",       nameField);
        addRow(form, gbc, 1, "이메일 *",     emailField);
        addRow(form, gbc, 2, "새 비밀번호",  passwordField);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        errorLabel.setForeground(java.awt.Color.RED);
        form.add(errorLabel, gbc);

        add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(cancelButton);
        btnPanel.add(saveButton);
        add(btnPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> dispose());
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(comp, gbc);
    }

    public String getName()     { return nameField.getText().trim(); }
    public String getEmail()    { return emailField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public void showError(String msg) { errorLabel.setText(msg); }

    public JButton getSaveButton()   { return saveButton; }
    public JButton getCancelButton() { return cancelButton; }
}
