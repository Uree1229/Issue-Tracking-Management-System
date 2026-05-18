package com.example.its.ui.javafx.controller;

import com.example.its.persistence.entity.IssueStatus;
import com.example.its.shared.dto.issue.IssueSearchCondition;
import com.example.its.shared.dto.issue.IssueSummaryResponse;
import com.example.its.shared.dto.issue.StatisticsResponse;
import com.example.its.ui.javafx.service.JavaFxBackendBridge;
import com.example.its.ui.javafx.session.UserSession;
import com.example.its.ui.javafx.support.UiAlertHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsController {
    private static final int DAY_WINDOW = 7;
    private static final int MONTH_WINDOW = 6;
    private static final String ACTIVE_TREND_BUTTON_STYLE = "analytics-toggle-button-active";

    private final Map<String, Long> dailyTrendData = new LinkedHashMap<>();
    private final Map<String, Long> monthlyTrendData = new LinkedHashMap<>();
    private boolean showingMonthlyTrend;

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
    private Button dailyTrendButton;

    @FXML
    private Button monthlyTrendButton;

    @FXML
    private BarChart<String, Number> workloadChart;

    @FXML
    private ListView<String> insightListView;

    @FXML
    private void initialize() {
        statusChart.setAnimated(false);
        trendChart.setAnimated(false);
        workloadChart.setAnimated(false);
        showingMonthlyTrend = false;
        updateTrendToggleButtons();
        refreshMetrics();
    }

    public void refreshMetrics() {
        Long projectId = UserSession.getCurrentProjectId();
        if (projectId == null) {
            showEmptyState("Create a project first to see analytics.");
            return;
        }

        try {
            StatisticsResponse statistics = backendBridge().getStatistics(projectId);
            dailyTrendData.clear();
            dailyTrendData.putAll(backendBridge().getDailyIssueStatistics(projectId, DAY_WINDOW));
            monthlyTrendData.clear();
            monthlyTrendData.putAll(backendBridge().getMonthlyIssueStatistics(projectId, MONTH_WINDOW));
            IssueSearchCondition condition = new IssueSearchCondition();
            condition.setProjectId(projectId);
            List<IssueSummaryResponse> issues = backendBridge().searchIssues(condition);

            long openCount = countStatuses(statistics, IssueStatus.NEW, IssueStatus.ASSIGNED, IssueStatus.REOPENED);
            long verificationCount = countStatuses(statistics, IssueStatus.FIXED, IssueStatus.RESOLVED);
            long closedCount = countStatuses(statistics, IssueStatus.CLOSED);

            totalIssuesValueLabel.setText(String.valueOf(statistics.getTotalIssueCount()));
            openIssuesValueLabel.setText(String.valueOf(openCount));
            verificationValueLabel.setText(String.valueOf(verificationCount));
            closedIssuesValueLabel.setText(String.valueOf(closedCount));

            populateStatusChart(statistics);
            populateCurrentTrendChart();
            populateWorkloadChart(issues);
            populateInsights(issues, openCount, verificationCount, closedCount);
        } catch (Exception exception) {
            showEmptyState("Analytics could not be loaded.");
            UiAlertHelper.showError("Analytics Failed", "Could not load project analytics.", exception);
        }
    }

    private void populateStatusChart(StatisticsResponse statistics) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Issues");

        for (IssueStatus status : IssueStatus.values()) {
            Number count = statistics.getStatusCounts().getOrDefault(status, 0L);
            series.getData().add(new XYChart.Data<>(status.name(), count));
        }

        statusChart.getData().setAll(series);
    }

    private void populateTrendChart(String seriesName, Map<String, Long> issueCounts) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        issueCounts.forEach((bucket, count) -> series.getData().add(new XYChart.Data<>(bucket, count)));
        trendChart.getData().setAll(series);
    }

    private void populateWorkloadChart(List<IssueSummaryResponse> issues) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Assigned Issues");

        Map<String, Long> workload = issues.stream()
            .filter(issue -> issue.getAssigneeName() != null && !issue.getAssigneeName().isBlank())
            .collect(Collectors.groupingBy(IssueSummaryResponse::getAssigneeName, Collectors.counting()));

        workload.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
            .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        workloadChart.getData().setAll(series);
        applyWorkloadBarWidthLimit();
    }

    private void populateInsights(List<IssueSummaryResponse> issues, long openCount, long verificationCount, long closedCount) {
        String topReporter = issues.stream()
            .filter(issue -> issue.getReporterName() != null && !issue.getReporterName().isBlank())
            .collect(Collectors.groupingBy(IssueSummaryResponse::getReporterName, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> entry.getKey() + " reported " + entry.getValue() + " issues")
            .orElse("No reporters yet");

        String busiestDeveloper = issues.stream()
            .filter(issue -> issue.getAssigneeName() != null && !issue.getAssigneeName().isBlank())
            .collect(Collectors.groupingBy(IssueSummaryResponse::getAssigneeName, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> entry.getKey() + " currently owns " + entry.getValue() + " issues")
            .orElse("No developer workload yet");

        String oldestOpenIssue = issues.stream()
            .filter(issue -> issue.getStatus() != IssueStatus.CLOSED)
            .filter(issue -> issue.getReportedAt() != null)
            .min(java.util.Comparator.comparing(IssueSummaryResponse::getReportedAt))
            .map(issue -> "#" + issue.getIssueId() + " has been open since " + issue.getReportedAt().toLocalDate())
            .orElse("No open issues");

        insightListView.setItems(FXCollections.observableArrayList(
            "Open issues waiting on triage or rework: " + openCount,
            "Verification queue (fixed or resolved): " + verificationCount,
            "Closed issues in the current project: " + closedCount,
            topReporter,
            busiestDeveloper,
            oldestOpenIssue
        ));
    }

    private long countStatuses(StatisticsResponse statistics, IssueStatus... statuses) {
        long total = 0L;
        for (IssueStatus status : statuses) {
            total += statistics.getStatusCounts().getOrDefault(status, 0L);
        }
        return total;
    }

    private void showEmptyState(String message) {
        totalIssuesValueLabel.setText("0");
        openIssuesValueLabel.setText("0");
        verificationValueLabel.setText("0");
        closedIssuesValueLabel.setText("0");
        statusChart.getData().clear();
        trendChart.getData().clear();
        workloadChart.getData().clear();
        insightListView.setItems(FXCollections.observableArrayList(message));
        dailyTrendData.clear();
        monthlyTrendData.clear();
    }

    private JavaFxBackendBridge backendBridge() {
        return JavaFxBackendBridge.getInstance();
    }

    private void applyWorkloadBarWidthLimit() {
        Platform.runLater(() -> Platform.runLater(() -> {
            double maxVisualWidth = 44;
            for (Node node : workloadChart.lookupAll(".chart-bar")) {
                if (node instanceof Region region) {
                    double currentWidth = region.getLayoutBounds().getWidth();
                    if (currentWidth > maxVisualWidth) {
                        double inset = (currentWidth - maxVisualWidth) / 2.0;
                        region.setStyle(String.format("-fx-background-insets: 0 %.1f 0 %.1f;", inset, inset));
                    } else {
                        region.setStyle("");
                    }
                }
            }
        }));
    }

    @FXML
    private void handleShowDailyTrend() {
        showingMonthlyTrend = false;
        updateTrendToggleButtons();
        populateCurrentTrendChart();
    }

    @FXML
    private void handleShowMonthlyTrend() {
        showingMonthlyTrend = true;
        updateTrendToggleButtons();
        populateCurrentTrendChart();
    }

    private void populateCurrentTrendChart() {
        if (showingMonthlyTrend) {
            populateTrendChart("Monthly Issues", monthlyTrendData);
            return;
        }
        populateTrendChart("Recent Daily Issues", dailyTrendData);
    }

    private void updateTrendToggleButtons() {
        dailyTrendButton.getStyleClass().remove(ACTIVE_TREND_BUTTON_STYLE);
        monthlyTrendButton.getStyleClass().remove(ACTIVE_TREND_BUTTON_STYLE);

        if (showingMonthlyTrend) {
            monthlyTrendButton.getStyleClass().add(ACTIVE_TREND_BUTTON_STYLE);
        } else {
            dailyTrendButton.getStyleClass().add(ACTIVE_TREND_BUTTON_STYLE);
        }
    }
}
