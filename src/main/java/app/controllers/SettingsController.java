package app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;


public class SettingsController {
    public Button addApplicationBtn;
    public ListView externalAppListView;
    public Button clearCacheBtn;
    public CheckBox startupCheckbox;
    public CheckBox enableDokeySearchCheckbox;
    public Button viewLicensesBtn;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    void initialize() {
    }
}