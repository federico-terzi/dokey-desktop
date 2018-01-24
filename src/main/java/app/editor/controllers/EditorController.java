package app.editor.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import section.model.Section;


public class EditorController {

    public Button changeSizeBtn;
    public Button rotateViewBtn;
    public TextField searchSectionTextField;
    public Button addApplicationBtn;
    public Button exportBtn;
    public Button searchBtn;
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<Section> sectionsListView;

    @FXML
    private VBox contentBox;

    @FXML
    void initialize() {
        // Initially hide the search bar
        searchSectionTextField.setManaged(false);
    }

    public ListView<Section> getSectionsListView() {
        return sectionsListView;
    }

    public VBox getContentBox() {
        return contentBox;
    }
}