package app;

import app.UIControllers.InitializationController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;

public class InitializationStage extends Stage {
    private InitializationController controller;

    public InitializationStage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/initialization.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 400, 275);
        scene.getStylesheets().add(MainApp.class.getResource("/css/initialization.css").toExternalForm());
        this.setTitle("Hello World");
        this.setScene(scene);
        this.initStyle(StageStyle.UNDECORATED);

        controller = (InitializationController) fxmlLoader.getController();
    }

    public InitializationController getController() {
        return controller;
    }

    /**
     * Update the fields in the window
     * @param applicationName
     * @param percentage
     * @param iconFile
     */
    public void updateAppStatus(String applicationName, double percentage, File iconFile) {
        controller.setAppNameLabel(applicationName);
        controller.setAppProgressBar(percentage);
        controller.setAppImageFile(iconFile);
    }
}
