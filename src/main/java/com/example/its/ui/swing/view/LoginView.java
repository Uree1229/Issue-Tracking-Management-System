package com.example.its.ui.swing.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginView extends JPanel {

    private final JTextField loginIdField   = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton loginButton       = new JButton("로그인");
    private final JLabel errorLabel         = new JLabel(" ");

    public LoginView() {
        setLayout(new GridBagLayout());
        initComponents();
    }

    private void initComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("ITS 로그인"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("아이디"), gbc);

        gbc.gridx = 1;
        form.add(loginIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("비밀번호"), gbc);

        gbc.gridx = 1;
        form.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        form.add(loginButton, gbc);

        gbc.gridy = 3;
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        form.add(errorLabel, gbc);

        add(form);
    }

    public String getLoginId() { return loginIdField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public JButton getLoginButton() { return loginButton; }
    public void showError(String message) { errorLabel.setText(message); }
    public void clearError() { errorLabel.setText(" "); }
    public void clearFields() { loginIdField.setText(""); passwordField.setText(""); }
}
