package app;

import app.UIControllers.AppListController;
import app.UIControllers.InitializationController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;

public class AppListStage extends Stage {
    private AppListController controller;

    public AppListStage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/application_list.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 350, 550);
        this.setTitle("Applications");
        this.setScene(scene);
        this.getIcons().add(new Image(AppListStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (AppListController) fxmlLoader.getController();
    }

    public AppListController getController() {
        return controller;
    }
}
