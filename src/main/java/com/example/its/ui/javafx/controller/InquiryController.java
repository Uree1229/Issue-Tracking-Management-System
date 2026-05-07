package com.example.its.ui.javafx.controller;

import com.example.its.ui.javafx.model.InquiryQueryPayload;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class InquiryController {

    private MainLayoutController mainLayoutController;

    @FXML
    private TextField issueIdField;

    @FXML
    private TextField keywordField;

    @FXML
    private Label inquirySummaryLabel;

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void refreshData() {
        issueIdField.clear();
        keywordField.clear();
        inquirySummaryLabel.setText("Use an exact ID or keyword, then open a dedicated Inquiry Results window.");
    }

    @FXML
    private void handleInquire() {
        if (mainLayoutController == null) {
            return;
        }

        InquiryQueryPayload payload = new InquiryQueryPayload(parseIssueId(), trimmed(keywordField.getText()), false);
        mainLayoutController.showInquiryResultsWindow(payload);
    }

    @FXML
    private void showRecentIssues() {
        if (mainLayoutController == null) {
            return;
        }

        mainLayoutController.showInquiryResultsWindow(InquiryQueryPayload.recent());
    }

    private Long parseIssueId() {
        String value = trimmed(issueIdField.getText());
        if (value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String trimmed(String value) {
        return value == null ? "" : value.trim();
    }
}
