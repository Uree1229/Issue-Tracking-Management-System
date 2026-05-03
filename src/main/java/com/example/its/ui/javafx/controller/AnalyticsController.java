package com.example.its.ui.javafx.controller;

import com.example.its.ui.javafx.model.IssueRowModel;
import com.example.its.ui.javafx.model.MockIssueDataProvider;
import com.example.its.ui.javafx.model.UiIssueStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsController {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MM/dd");

    @FXML
    private Label totalIssuesValueLabel;

    @FXML
    private Label openIssuesValueLabel;

    @FXML
    private Label verificationValueLabel;

    @FXML
    private Label closedIssuesValueLabel;

    @FXML
    private BarChart<String, Number> statusChart;

    @FXML
    private LineChart<String, Number> trendChart;

    @FXML
    private BarChart<String, Number> workloadChart;

    @FXML
    private ListView<String> insightListView;

    @FXML
    private void initialize() {
        statusChart.setAnimated(false);
        trendChart.setAnimated(false);
        workloadChart.setAnimated(false);
        refreshMetrics();
    }

    public void refreshMetrics() {
        ObservableList<IssueRowModel> issues = MockIssueDataProvider.createIssues();

        long openCount = issues.stream()
            .filter(issue -> issue.getStatus().isActiveWorkflowStatus())
            .count();
        long verificationCount = issues.stream()
            .filter(issue -> issue.getStatus().isVerificationStatus())
            .count();
        long closedCount = issues.stream()
            .filter(issue -> issue.getStatus() == UiIssueStatus.CLOSED)
            .count();

        totalIssuesValueLabel.setText(String.valueOf(issues.size()));
        openIssuesValueLabel.setText(String.valueOf(openCount));
        verificationValueLabel.setText(String.valueOf(verificationCount));
        closedIssuesValueLabel.setText(String.valueOf(closedCount));

        populateStatusChart(issues);
        populateTrendChart(issues);
        populateWorkloadChart(issues);
        populateInsights(issues, openCount, verificationCount, closedCount);
    }

    private void populateStatusChart(List<IssueRowModel> issues) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Issues");

        addStatusData(series, UiIssueStatus.NEW.displayName(), issues, UiIssueStatus.NEW);
        addStatusData(series, UiIssueStatus.ASSIGNED.displayName(), issues, UiIssueStatus.ASSIGNED);
        addStatusData(series, UiIssueStatus.FIXED.displayName(), issues, UiIssueStatus.FIXED);
        addStatusData(series, UiIssueStatus.RESOLVED.displayName(), issues, UiIssueStatus.RESOLVED);
        addStatusData(series, UiIssueStatus.CLOSED.displayName(), issues, UiIssueStatus.CLOSED);
        addStatusData(series, UiIssueStatus.REOPENED.displayName(), issues, UiIssueStatus.REOPENED);

        statusChart.getData().setAll(series);
    }

    private void populateTrendChart(List<IssueRowModel> issues) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Reported");

        LocalDate today = LocalDate.now();
        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        for (int index = 6; index >= 0; index--) {
            LocalDate day = today.minusDays(index);
            long count = issues.stream()
                .filter(issue -> issue.getReportedAt().toLocalDate().equals(day))
                .count();
            counts.put(day, count);
        }

        counts.forEach((day, count) -> series.getData().add(new XYChart.Data<>(day.format(DAY_FORMATTER), count)));
        trendChart.getData().setAll(series);
    }

    private void populateWorkloadChart(List<IssueRowModel> issues) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Assigned Issues");

        Map<String, Long> workload = issues.stream()
            .filter(issue -> !"Unassigned".equals(issue.getAssigneeDisplayName()))
            .collect(Collectors.groupingBy(IssueRowModel::getAssigneeDisplayName, Collectors.counting()));

        workload.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
            .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        workloadChart.getData().setAll(series);
    }

    private void populateInsights(List<IssueRowModel> issues, long openCount, long verificationCount, long closedCount) {
        String topReporter = issues.stream()
            .collect(Collectors.groupingBy(IssueRowModel::getReporterName, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> entry.getKey() + " reported " + entry.getValue() + " issues")
            .orElse("No reporters yet");

        String busiestDeveloper = issues.stream()
            .filter(issue -> !"Unassigned".equals(issue.getAssigneeDisplayName()))
            .collect(Collectors.groupingBy(IssueRowModel::getAssigneeDisplayName, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> entry.getKey() + " currently owns " + entry.getValue() + " issues")
            .orElse("No developer workload yet");

        String oldestOpenIssue = issues.stream()
            .filter(issue -> issue.getStatus() != UiIssueStatus.CLOSED)
            .min(Comparator.comparing(IssueRowModel::getReportedAt))
            .map(issue -> "#" + issue.getIssueId() + " has been open since " + issue.getReportedAt().toLocalDate())
            .orElse("No open issues");

        insightListView.setItems(FXCollections.observableArrayList(
            "Open issues waiting on triage or rework: " + openCount,
            "Verification queue (fixed or resolved): " + verificationCount,
            "Closed issues in the mock dataset: " + closedCount,
            topReporter,
            busiestDeveloper,
            oldestOpenIssue
        ));
    }

    private void addStatusData(XYChart.Series<String, Number> series, String label, List<IssueRowModel> issues, UiIssueStatus status) {
        long count = issues.stream()
            .filter(issue -> issue.getStatus() == status)
            .count();
        series.getData().add(new XYChart.Data<>(label, count));
    }
}
