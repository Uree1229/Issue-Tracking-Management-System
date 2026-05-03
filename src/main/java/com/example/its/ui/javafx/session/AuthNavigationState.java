package com.example.its.ui.javafx.session;

public final class AuthNavigationState {

    private static String pendingLoginId;
    private static String pendingMessage;

    private AuthNavigationState() {
    }

    public static void prepareLoginPrefill(String loginId, String message) {
        pendingLoginId = loginId;
        pendingMessage = message;
    }

    public static String consumePendingLoginId() {
        String value = pendingLoginId;
        pendingLoginId = null;
        return value;
    }

    public static String consumePendingMessage() {
        String value = pendingMessage;
        pendingMessage = null;
        return value;
    }
}
