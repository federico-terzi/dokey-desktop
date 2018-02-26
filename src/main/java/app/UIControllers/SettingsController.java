package app.UIControllers;

import app.editor.listcells.ApplicationListCell;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import system.model.Application;

import java.net.URL;
import java.util.ResourceBundle;


public class SettingsController {
    public Button addApplicationBtn;
    public ListView externalAppListView;
    public Button clearCacheBtn;
    public CheckBox startupCheckbox;
    public TextArea licensesTextArea;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    void initialize() {
    }
}