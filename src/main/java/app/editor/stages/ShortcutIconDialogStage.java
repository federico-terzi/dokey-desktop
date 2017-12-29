package app.editor.stages;

import app.editor.controllers.AppListController;
import app.editor.controllers.ShortcutIconListController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import system.model.Application;
import system.sicons.ShortcutIcon;
import system.sicons.ShortcutIconManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ShortcutIconDialogStage extends Stage {
    private ShortcutIconListController controller;
    private ShortcutIconManager shortcutIconManager;
    private OnIconSelectListener listener;

    private String searchQuery = null;

    public ShortcutIconDialogStage(ShortcutIconManager shortcutIconManager, OnIconSelectListener listener) throws IOException {
        this.listener = listener;
        this.shortcutIconManager = shortcutIconManager;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/shortcut_icon_list.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ShortcutIconDialogStage.class.getResource("/css/shortcutlistcell.css").toExternalForm());
        this.setTitle("Select the Icon");
        this.setScene(scene);
        this.getIcons().add(new Image(ShortcutIconDialogStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (ShortcutIconListController) fxmlLoader.getController();

        populateListView();

        // Set the event listeners
        controller.cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                listener.onCanceled();
                close();
            }
        });

        controller.selectBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ShortcutIcon icon = (ShortcutIcon) controller.shortcutListView.getSelectionModel().getSelectedItem();

                if (icon != null) {
                    listener.onIconSelected(icon);
                    close();
                }
            }
        });

        controller.searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchQuery = newValue;
            populateListView();
        });

        // Focus the text field
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.searchTextField.requestFocus();
            }
        });
    }

    private void populateListView() {
        List<ShortcutIcon> allIcons = shortcutIconManager.getIcons();

        // Filter the icons based on the query
        if (searchQuery != null && !searchQuery.isEmpty()) {
            allIcons = allIcons.stream().filter(icon -> icon.getName().toLowerCase().contains(searchQuery.toLowerCase())).collect(Collectors.toList());
        }

        ObservableList<ShortcutIcon> icons = FXCollections.observableArrayList(allIcons);

        Collections.sort(icons);
        controller.shortcutListView.setItems(icons);
    }

    public interface OnIconSelectListener {
        void onIconSelected(ShortcutIcon icon);
        void onCanceled();
    }
}
