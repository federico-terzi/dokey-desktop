package app.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import system.ResourceUtils;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * This class can be used as the base building block for an app stage.
 * @param <T> the Controller of the layout.
 */
public class AbstractStage<T> extends Stage {
    protected T controller;
    protected ResourceBundle resourceBundle;

    protected AbstractStage(ResourceBundle resourceBundle, String layoutPath, String cssPath, String windowTitle) throws IOException{
        this.resourceBundle = resourceBundle;

        // Load the layout
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource(layoutPath).toURI().toURL());
        fxmlLoader.setResources(resourceBundle);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        this.setTitle(windowTitle);
        this.setScene(scene);
        this.getIcons().add(new Image(AbstractStage.class.getResourceAsStream("/assets/icon.png")));
        scene.getStylesheets().add(ResourceUtils.getResource(cssPath).toURI().toString());

        controller = (T) fxmlLoader.getController();
    }
}
