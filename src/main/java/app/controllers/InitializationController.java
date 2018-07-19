package app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class InitializationController {
    public Label statusLabel;
    public CheckBox startCheckbox;
    @FXML private ProgressBar appProgressBar;

    public void setAppProgressBar(double progress) {
        appProgressBar.setProgress(progress);
    }
}
