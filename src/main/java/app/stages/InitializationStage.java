package app.stages;

import app.MainApp;
import app.UIControllers.InitializationController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import system.ResourceUtils;

import java.io.File;
import java.io.IOException;

public class InitializationStage extends Stage {
    private InitializationController controller;

    public InitializationStage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/initialization.fxml").toURI().toURL());
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(ResourceUtils.getResource("/css/initialization.css").toURI().toString());
        this.setTitle("Remote Key Initialization");
        this.setScene(scene);
        this.initStyle(StageStyle.UNDECORATED);
        this.getIcons().add(new Image(InitializationStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (InitializationController) fxmlLoader.getController();
    }

    public InitializationController getController() {
        return controller;
    }

    /**
     * Update the fields in the window
     * @param applicationName
     * @param percentage
     */
    public void updateAppStatus(String applicationName, double percentage) {
        controller.setAppNameLabel(applicationName);
        controller.setAppProgressBar(percentage);
    }
}
