package app.editor.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class ApplicationPropertyController {
    public TextField pathTextField;

    @FXML
    private ImageView appIconImageView;

    @FXML
    private Label appTitleLabel;

    public ImageView getAppIconImageView() {
        return appIconImageView;
    }

    public Label getAppTitleLabel() {
        return appTitleLabel;
    }
}
