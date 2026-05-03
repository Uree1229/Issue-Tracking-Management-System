package com.example.its.ui.javafx.support;

import javafx.scene.control.Alert;

public final class IntegrationPointHelper {

    private IntegrationPointHelper() {
    }

    public static void showPending(String title, String connectionHint) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Frontend Placeholder");
        alert.setHeaderText(title);
        alert.setContentText(connectionHint);
        alert.showAndWait();
    }
}
