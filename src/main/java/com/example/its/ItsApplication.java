package com.example.its;

import com.example.its.ui.javafx.support.UiDatabaseBootstrap;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class ItsApplication extends Application {

    private static final double MAIN_WIDTH = 1280;
    private static final double MAIN_HEIGHT = 780;
    private static final double LOGIN_WIDTH = 1160;
    private static final double LOGIN_HEIGHT = 760;

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        UiDatabaseBootstrap.ensureInitialized();
        primaryStage = stage;
        primaryStage.setTitle("Issue Tracking System");
        primaryStage.setMinWidth(1050);
        primaryStage.setMinHeight(700);
        showLoginView();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void showLoginView() {
        showView("/fxml/login-view.fxml", LOGIN_WIDTH, LOGIN_HEIGHT);
    }

    public static void showRegisterView() {
        showView("/fxml/register-view.fxml", LOGIN_WIDTH, LOGIN_HEIGHT);
    }

    public static void showMainView() {
        showView("/fxml/main-layout.fxml", MAIN_WIDTH, MAIN_HEIGHT);
    }

    private static void showView(String resourcePath, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource(resourcePath));
            Parent root = loader.load();

            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(ItsApplication.class.getResource("/styles/app.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load view: " + resourcePath, exception);
        }
    }
}
