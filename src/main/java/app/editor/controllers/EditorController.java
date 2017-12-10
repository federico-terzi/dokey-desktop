package app.editor.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
    private Button addSectionBtn;

    @FXML
    private Pane bottomSpacerPane;

    @FXML
    private VBox contentBox;

    @FXML
    void initialize() {
        HBox.setHgrow(bottomSpacerPane, Priority.ALWAYS);
    }

    public ListView<Section> getSectionsListView() {
        return sectionsListView;
    }

    public Button getAddSectionBtn() {
        return addSectionBtn;
    }

    public Pane getBottomSpacerPane() {
        return bottomSpacerPane;
    }

    public VBox getContentBox() {
        return contentBox;
    }

    public ProgressBar getStatusProgressBar() {
        return statusProgressBar;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }
}