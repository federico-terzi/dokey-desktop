package app.UIControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

public class InitializationController {
    @FXML private ProgressBar appProgressBar;

    @FXML private Label appNameLabel;

    public String getAppNameLabel() {
        return appNameLabel.textProperty().get();
    }

    public void setAppNameLabel(String text) {
        this.appNameLabel.textProperty().set(text);
    }

    public void setAppProgressBar(double progress) {
        appProgressBar.setProgress(progress);
    }
}
