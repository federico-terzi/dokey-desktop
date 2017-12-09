package app.UIControllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TitledPane;
import section.model.Section;


public class EditorController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TitledPane propertiesPane;

    @FXML
    private ListView<Section> sectionsListView;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressBar statusProgressBar;

    @FXML
    void initialize() {

    }

    public ListView<Section> getSectionsListView() {
        return sectionsListView;
    }
}