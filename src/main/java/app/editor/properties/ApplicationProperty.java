package app.editor.properties;


import app.editor.controllers.ApplicationPropertyController;
import app.editor.controllers.EditorController;
import app.editor.stages.EditorStage;
import app.stages.AppListStage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import section.model.Component;

import java.awt.*;
import java.io.IOException;

public class ApplicationProperty extends Property{
    public ApplicationProperty(Component associatedComponent) {
        super(associatedComponent);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/properties/app_property.fxml"));
        VBox root = null;
        try {
            root = fxmlLoader.load();
            root.setFillWidth(true);
            root.setMaxWidth(Double.MAX_VALUE);
            getChildren().add(root);

            ApplicationPropertyController controller = (ApplicationPropertyController) fxmlLoader.getController();

            controller.getAppTitleLabel().setText(associatedComponent.getItem().getTitle());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
