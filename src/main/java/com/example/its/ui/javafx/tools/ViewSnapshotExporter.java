package com.example.its.ui.javafx.tools;

import com.example.its.ItsApplication;
import com.example.its.ui.javafx.controller.MainLayoutController;
import com.example.its.ui.javafx.model.AuthenticatedUser;
import com.example.its.ui.javafx.model.UiRole;
import com.example.its.ui.javafx.session.UserSession;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ViewSnapshotExporter extends Application {

    private static final double MAIN_WIDTH = 1280;
    private static final double MAIN_HEIGHT = 780;
    private static final double LOGIN_WIDTH = 1160;
    private static final double LOGIN_HEIGHT = 760;
    private static final Path OUTPUT_DIRECTORY = Path.of("build", "screenshots");

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        this.stage.setTitle("ITS Snapshot Exporter");
        this.stage.setX(30);
        this.stage.setY(30);

        try {
            Files.createDirectories(OUTPUT_DIRECTORY);
            renderLogin();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to initialize snapshot export.", exception);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void renderLogin() {
        UserSession.clear();
        LoadedView<Object> loadedView = loadView("/fxml/login-view.fxml");
        captureScene(loadedView.root(), LOGIN_WIDTH, LOGIN_HEIGHT, OUTPUT_DIRECTORY.resolve("01-login.png"), this::renderHome);
    }

    private void renderHome() {
        renderMainLayout("02-home.png", null, this::renderIssues);
    }

    private void renderIssues() {
        renderMainLayout("03-issues.png", controller -> controller.showIssues(null), this::renderSearch);
    }

    private void renderSearch() {
        renderMainLayout("04-search.png", controller -> controller.showSearch(null), this::renderInquiry);
    }

    private void renderInquiry() {
        renderMainLayout("05-inquiry.png", controller -> controller.showInquiry(101L), this::renderAdminUsers);
    }

    private void renderAdminUsers() {
        renderMainLayout("06-admin-users.png", controller -> controller.showAdmin(null), this::renderAdminProjects);
    }

    private void renderAdminProjects() {
        renderMainLayout("07-admin-projects.png", MainLayoutController::showAdminProjectTab, this::finish);
    }

    private void renderMainLayout(String fileName, Consumer<MainLayoutController> configuration, Runnable nextStep) {
        UserSession.setCurrentUser(new AuthenticatedUser(1L, "admin", "Administrator", UiRole.ADMIN));
        LoadedView<MainLayoutController> loadedView = loadView("/fxml/main-layout.fxml");

        if (configuration != null) {
            configuration.accept(loadedView.controller());
        }

        captureScene(
            loadedView.root(),
            MAIN_WIDTH,
            MAIN_HEIGHT,
            OUTPUT_DIRECTORY.resolve(fileName),
            nextStep
        );
    }

    private void finish() {
        stage.close();
        Platform.exit();
    }

    private void captureScene(Parent root, double width, double height, Path outputPath, Runnable nextStep) {
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(ItsApplication.class.getResource("/styles/app.css").toExternalForm());

        stage.setScene(scene);
        stage.show();

        Platform.runLater(() -> Platform.runLater(() -> {
            root.applyCss();
            root.layout();

            try {
                WritableImage image = scene.snapshot(null);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", outputPath.toFile());
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to write snapshot: " + outputPath, exception);
            }

            if (nextStep != null) {
                nextStep.run();
            }
        }));
    }

    private <T> LoadedView<T> loadView(String resourcePath) {
        try {
            FXMLLoader loader = new FXMLLoader(ItsApplication.class.getResource(resourcePath));
            Parent root = loader.load();
            return new LoadedView<>(root, loader.getController());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load view: " + resourcePath, exception);
        }
    }

    private record LoadedView<T>(Parent root, T controller) {
    }
}
