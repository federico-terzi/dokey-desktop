package app.editor.properties;


import app.editor.controllers.ApplicationPropertyController;
import app.editor.controllers.EditorController;
import app.editor.stages.EditorStage;
import app.stages.AppListStage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import section.model.AppItem;
import section.model.Component;
import system.model.Application;
import system.model.ApplicationManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ApplicationProperty extends Property{
    public ApplicationProperty(Component associatedComponent, ApplicationManager applicationManager) {
        super(associatedComponent, applicationManager);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/properties/app_property.fxml"));
        VBox root = null;
        try {
            root = fxmlLoader.load();
            root.setFillWidth(true);
            root.setMaxWidth(Double.MAX_VALUE);
            getChildren().add(root);

            ApplicationPropertyController controller = (ApplicationPropertyController) fxmlLoader.getController();

            controller.getAppTitleLabel().setText(associatedComponent.getItem().getTitle());
            Application application = applicationManager.getApplication(((AppItem) associatedComponent.getItem()).getAppID());
            // If there is an image, set it.
            if (application.getIconPath() != null) {
                File iconFile = new File(application.getIconPath());
                if (iconFile.isFile()) {
                    Image image = new Image(iconFile.toURI().toString());
                    controller.getAppIconImageView().setImage(image);
                }
            }
            controller.pathTextField.setText(application.getExecutablePath());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
