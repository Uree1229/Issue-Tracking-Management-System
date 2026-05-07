package com.example.its.ui.javafx.support;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public final class UiAlertHelper {

    private UiAlertHelper() {
    }

    public static void showError(String title, String header, Throwable throwable) {
        showError(title, header, extractMessage(throwable, "An unexpected error occurred."));
    }

    public static void showError(String title, String header, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String title, String header, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static String extractMessage(Throwable throwable, String fallback) {
        Throwable cursor = throwable;
        while (cursor != null) {
            String message = cursor.getMessage();
            if (message != null && !message.isBlank()) {
                return message;
            }
            cursor = cursor.getCause();
        }
        return fallback;
    }

    public static String extractRootCauseMessage(Throwable throwable, String fallback) {
        Throwable cursor = throwable;
        String candidate = null;
        while (cursor != null) {
            String message = cursor.getMessage();
            if (message != null && !message.isBlank()) {
                candidate = message;
            }
            cursor = cursor.getCause();
        }
        return candidate == null ? fallback : candidate;
    }
}
