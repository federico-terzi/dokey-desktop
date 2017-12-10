package app.editor.components;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import system.model.Application;

import java.io.File;

public class AppButton extends Button {
    private Application application;

    public AppButton(Application application) {
        super();
        this.application = application;

        // Set the button properties
        setText(application.getName());
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // If there is an image, set it.
        if (application.getIconPath() != null) {
            File iconFile = new File(application.getIconPath());
            if (iconFile.isFile()) {
                Image image = new Image(iconFile.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(32);
                imageView.setFitWidth(32);
                imageView.setSmooth(true);
                setGraphic(imageView);
                setContentDisplay(ContentDisplay.TOP);
            }
        }

    }
}
