package com.example.its.ui.swing;

import com.example.its.ui.swing.view.MainFrame;

import javax.swing.SwingUtilities;

public class SwingApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.showLogin();
            frame.setVisible(true);
        });
    }
}
