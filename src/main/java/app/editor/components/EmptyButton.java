package app.editor.components;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import system.model.Application;

import java.io.File;

public class EmptyButton extends DragButton {
    public EmptyButton() {
        super();

        // Set the button properties
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        getStyleClass().add("empty-btn");

        Image image = new Image(EmptyButton.class.getResourceAsStream("/assets/add.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(32);
        imageView.setFitWidth(32);
        imageView.setSmooth(true);
        setGraphic(imageView);
        setContentDisplay(ContentDisplay.TOP);
    }
}
