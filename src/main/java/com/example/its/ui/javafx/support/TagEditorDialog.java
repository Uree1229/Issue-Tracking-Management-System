package com.example.its.ui.javafx.support;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class TagEditorDialog {

    public record Labels(
        String saveButtonText,
        String selectedTitle,
        String selectedHint,
        String availableLabel,
        String availablePrompt,
        String addExistingButtonText,
        String newTagLabel,
        String newTagPrompt,
        String addNewButtonText,
        String removeButtonText
    ) {
    }

    private TagEditorDialog() {
    }

    public static Optional<List<String>> show(
        String title,
        String header,
        Collection<String> initialTags,
        Collection<String> availableProjectTags
    ) {
        return show(title, header, initialTags, availableProjectTags, issueLabels());
    }

    public static Optional<List<String>> show(
        String title,
        String header,
        Collection<String> initialTags,
        Collection<String> availableProjectTags,
        Labels labels
    ) {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        ButtonType saveButtonType = new ButtonType(labels.saveButtonText(), ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.getDialogPane().getStyleClass().add("tag-editor-dialog");
        dialog.getDialogPane().setPrefWidth(520);
        if (TagEditorDialog.class.getResource("/styles/app.css") != null) {
        dialog.getDialogPane().getStylesheets().add(
                TagEditorDialog.class.getResource("/styles/app.css").toExternalForm()
            );
        }

        ObservableList<String> selectedTags = FXCollections.observableArrayList(normalize(initialTags));

        ListView<String> selectedTagListView = new ListView<>(selectedTags);
        selectedTagListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        selectedTagListView.setPrefHeight(170);
        selectedTagListView.setPlaceholder(new Label("No tags selected yet."));
        selectedTagListView.getStyleClass().add("tag-editor-list");

        ComboBox<String> existingTagCombo = new ComboBox<>();
        existingTagCombo.setPromptText(labels.availablePrompt());
        existingTagCombo.setMaxWidth(Double.MAX_VALUE);
        refreshAvailableOptions(existingTagCombo, availableProjectTags, selectedTags);

        TextField newTagField = new TextField();
        newTagField.setPromptText(labels.newTagPrompt());

        Button addExistingButton = new Button(labels.addExistingButtonText());
        addExistingButton.setOnAction(event -> {
            String selected = existingTagCombo.getValue();
            if (selected == null || selected.isBlank()) {
                return;
            }
            addTag(selectedTags, selected);
            refreshAvailableOptions(existingTagCombo, availableProjectTags, selectedTags);
        });

        Button addNewButton = new Button(labels.addNewButtonText());
        addNewButton.setOnAction(event -> {
            String entered = trim(newTagField.getText());
            if (entered.isBlank()) {
                return;
            }
            addTag(selectedTags, entered);
            newTagField.clear();
            refreshAvailableOptions(existingTagCombo, availableProjectTags, selectedTags);
        });

        Button removeSelectedButton = new Button(labels.removeButtonText());
        removeSelectedButton.setOnAction(event -> {
            List<String> selectedItems = new ArrayList<>(selectedTagListView.getSelectionModel().getSelectedItems());
            if (selectedItems.isEmpty()) {
                return;
            }
            selectedTags.removeIf(tag -> selectedItems.stream().anyMatch(selected -> selected.equalsIgnoreCase(tag)));
            refreshAvailableOptions(existingTagCombo, availableProjectTags, selectedTags);
        });

        addExistingButton.getStyleClass().add("ghost-button");
        addNewButton.getStyleClass().add("primary-button");
        removeSelectedButton.getStyleClass().add("ghost-button");

        Label selectedTagsTitle = new Label(labels.selectedTitle());
        selectedTagsTitle.getStyleClass().add("section-title");

        Label projectTagsLabel = new Label(labels.availableLabel());
        projectTagsLabel.getStyleClass().add("field-label");

        Label newTagLabel = new Label(labels.newTagLabel());
        newTagLabel.getStyleClass().add("field-label");

        HBox existingTagRow = new HBox(10, existingTagCombo, addExistingButton);
        existingTagRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(existingTagCombo, Priority.ALWAYS);

        HBox newTagRow = new HBox(10, newTagField, addNewButton);
        newTagRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(newTagField, Priority.ALWAYS);

        Label selectedTagsHint = new Label(labels.selectedHint());
        selectedTagsHint.getStyleClass().add("landing-copy");

        VBox selectedSection = new VBox(8,
            selectedTagsTitle,
            selectedTagsHint,
            selectedTagListView,
            removeSelectedButton
        );
        selectedSection.getStyleClass().add("tag-editor-card");

        VBox existingSection = new VBox(8,
            projectTagsLabel,
            existingTagRow
        );
        existingSection.getStyleClass().add("tag-editor-card");

        VBox newSection = new VBox(8,
            newTagLabel,
            newTagRow
        );
        newSection.getStyleClass().add("tag-editor-card");

        VBox content = new VBox(14,
            selectedSection,
            existingSection,
            newSection
        );
        content.setPadding(new Insets(10, 0, 0, 0));
        content.getStyleClass().add("tag-editor-dialog-content");

        dialog.getDialogPane().setContent(content);
        Button applyButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (applyButton != null) {
            applyButton.getStyleClass().add("primary-button");
        }
        if (cancelButton != null) {
            cancelButton.getStyleClass().add("ghost-button");
        }
        dialog.setResultConverter(buttonType -> buttonType == saveButtonType ? List.copyOf(selectedTags) : null);
        return dialog.showAndWait();
    }

    public static Labels issueLabels() {
        return new Labels(
            "Apply Tags",
            "Selected Tags",
            "Select the tags you want to use here. New tags automatically become available across this project.",
            "Available Tags",
            "Choose a tag to add",
            "Add Existing",
            "New Tag",
            "New tag name",
            "Add New",
            "Remove Selected"
        );
    }

    public static Labels projectLabels() {
        return new Labels(
            "Save Tags",
            "Available Tags",
            "Manage the tags that are available across this project. Tags used by issues cannot be deleted.",
            "Available Tags",
            "",
            "",
            "New Tag",
            "New tag name",
            "Add New",
            "Delete Selected"
        );
    }

    private static void addTag(ObservableList<String> selectedTags, String rawTag) {
        String tag = trim(rawTag);
        if (tag.isBlank()) {
            return;
        }
        boolean exists = selectedTags.stream().anyMatch(existing -> existing.equalsIgnoreCase(tag));
        if (exists) {
            return;
        }
        selectedTags.add(tag);
        selectedTags.sort(String.CASE_INSENSITIVE_ORDER);
    }

    private static List<String> normalize(Collection<String> tagNames) {
        Map<String, String> normalized = new LinkedHashMap<>();
        if (tagNames != null) {
            for (String tagName : tagNames) {
                String trimmed = trim(tagName);
                if (!trimmed.isBlank()) {
                    normalized.putIfAbsent(trimmed.toLowerCase(Locale.ROOT), trimmed);
                }
            }
        }
        List<String> values = new ArrayList<>(normalized.values());
        values.sort(String.CASE_INSENSITIVE_ORDER);
        return values;
    }

    private static void refreshAvailableOptions(
        ComboBox<String> comboBox,
        Collection<String> availableProjectTags,
        Collection<String> selectedTags
    ) {
        List<String> options = normalize(availableProjectTags).stream()
            .filter(tag -> selectedTags.stream().noneMatch(selected -> selected.equalsIgnoreCase(tag)))
            .toList();
        comboBox.setItems(FXCollections.observableArrayList(options));
        comboBox.setValue(options.isEmpty() ? null : options.get(0));
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
