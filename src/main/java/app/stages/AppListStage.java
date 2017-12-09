package app.stages;

import app.MainApp;
import app.UIControllers.AppListController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import system.model.Application;
import system.model.ApplicationManager;

import java.io.IOException;
import java.util.Collections;

public class AppListStage extends Stage {
    private AppListController controller;
    private ApplicationManager applicationManager;

    public AppListStage(ApplicationManager applicationManager) throws IOException {
        this.applicationManager = applicationManager;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/application_list.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 350, 550);
        scene.getStylesheets().add(AppListStage.class.getResource("/css/applistcell.css").toExternalForm());
        this.setTitle("Applications");
        this.setScene(scene);
        this.getIcons().add(new Image(AppListStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (AppListController) fxmlLoader.getController();

        populateAppListView();
    }

    private void populateAppListView() {
        ObservableList<Application> apps = FXCollections.observableArrayList(applicationManager.getApplicationList());
        Collections.sort(apps);
        controller.getAppListView().setItems(apps);
    }

    public AppListController getController() {
        return controller;
    }
}
