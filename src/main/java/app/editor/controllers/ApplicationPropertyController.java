package app.editor.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class ApplicationPropertyController {
    @FXML
    private Button changeAppBtn;

    @FXML
    private ImageView appIconImageView;

    @FXML
    private Label appTitleLabel;

    public Button getChangeAppBtn() {
        return changeAppBtn;
    }

    public ImageView getAppIconImageView() {
        return appIconImageView;
    }

    public Label getAppTitleLabel() {
        return appTitleLabel;
    }
}
