package app.editor.stages;

import app.editor.controllers.AppListController;
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
import system.model.ApplicationManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AppSelectDialogStage extends Stage {
    private AppListController controller;
    private ApplicationManager applicationManager;
    private OnApplicationListener listener;

    private String searchQuery = null;

    public AppSelectDialogStage(ApplicationManager applicationManager, OnApplicationListener listener) throws IOException {
        this.applicationManager = applicationManager;
        this.listener = listener;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/application_list.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 350, 550);
        scene.getStylesheets().add(AppSelectDialogStage.class.getResource("/css/applistcell.css").toExternalForm());
        this.setTitle("Applications");
        this.setScene(scene);
        this.getIcons().add(new Image(AppSelectDialogStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (AppListController) fxmlLoader.getController();

        populateAppListView();

        // Set the event listeners
        controller.getCancelBtn().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                listener.onCanceled();
                close();
            }
        });

        controller.getSelectBtn().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Application selectedApp = controller.getAppListView().getSelectionModel().getSelectedItem();

                if (selectedApp != null) {
                    listener.onApplicationSelected(selectedApp);
                    close();
                }
            }
        });

        controller.getSearchTextField().textProperty().addListener((observable, oldValue, newValue) -> {
            searchQuery = newValue;
            populateAppListView();
        });

        // Focus the text field
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.getSearchTextField().requestFocus();
            }
        });
    }

    private void populateAppListView() {
        List<Application> allApps = applicationManager.getApplicationList();

        // Filter the apps based on the query
        if (searchQuery != null && !searchQuery.isEmpty()) {
            allApps = allApps.stream().filter(application -> application.getName().toLowerCase().contains(searchQuery)).collect(Collectors.toList());
        }

        ObservableList<Application> apps = FXCollections.observableArrayList(allApps);

        Collections.sort(apps);
        controller.getAppListView().setItems(apps);
    }

    public AppListController getController() {
        return controller;
    }

    public interface OnApplicationListener {
        void onApplicationSelected(Application application);
        void onCanceled();
    }
}
