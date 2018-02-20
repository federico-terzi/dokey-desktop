package app.UIControllers;

import app.editor.listcells.ApplicationListCell;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import system.model.Application;

import java.net.URL;
import java.util.ResourceBundle;


public class SettingsController {
    public Button addApplicationBtn;
    public ListView externalAppListView;
    public Button clearCacheBtn;
    public CheckBox startupCheckbox;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    void initialize() {
    }
}