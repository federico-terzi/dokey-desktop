package app.stages;

import app.MainApp;
import app.UIControllers.InitializationController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import system.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

public class InitializationStage extends Stage {
    private InitializationController controller;

    public InitializationStage(ResourceBundle resourceBundle) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/initialization.fxml").toURI().toURL());
        fxmlLoader.setResources(resourceBundle);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ResourceUtils.getResource("/css/initialization.css").toURI().toString());
        this.setTitle(resourceBundle.getString("dokey_init"));
        this.setScene(scene);
        initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
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
        controller.setAppProgressBar(percentage);
    }

    public boolean isStartupBoxChecked() {
        return controller.startCheckbox.isSelected();
    }
}
