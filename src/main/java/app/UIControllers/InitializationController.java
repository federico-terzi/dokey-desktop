package app.UIControllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

public class InitializationController {
    public Label statusLabel;
    public CheckBox startCheckbox;
    @FXML private ProgressBar appProgressBar;

    public void setAppProgressBar(double progress) {
        appProgressBar.setProgress(progress);
    }
}
