package com.example.its.ui.swing.view.dialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

public class AccountCreateDialog extends JDialog {

    private final JTextField loginIdField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JTextField nameField    = new JTextField(20);
    private final JTextField emailField   = new JTextField(20);
    private final JComboBox<String> roleBox = new JComboBox<>(
            new String[]{"PL", "DEV", "TESTER", "ADMIN"});

    private final JButton createButton = new JButton("생성");
    private final JButton cancelButton = new JButton("취소");
    private final JLabel errorLabel    = new JLabel(" ");

    public AccountCreateDialog(JFrame parent) {
        super(parent, "계정 추가", true);
        setSize(420, 320);
        setLocationRelativeTo(parent);
        initComponents();
    }

    private void initComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gbc, 0, "로그인ID *", loginIdField);
        addRow(form, gbc, 1, "비밀번호 *", passwordField);
        addRow(form, gbc, 2, "이름 *",    nameField);
        addRow(form, gbc, 3, "이메일 *",  emailField);
        addRow(form, gbc, 4, "역할 *",    roleBox);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        errorLabel.setForeground(java.awt.Color.RED);
        form.add(errorLabel, gbc);

        add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(cancelButton);
        btnPanel.add(createButton);
        add(btnPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> dispose());
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(comp, gbc);
    }

    public String getLoginId()  { return loginIdField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public String getName()     { return nameField.getText().trim(); }
    public String getEmail()    { return emailField.getText().trim(); }
    public String getRole()     { return (String) roleBox.getSelectedItem(); }

    public void showError(String msg) { errorLabel.setText(msg); }
    public void clearError()          { errorLabel.setText(" "); }

    public JButton getCreateButton() { return createButton; }
    public JButton getCancelButton() { return cancelButton; }
}
