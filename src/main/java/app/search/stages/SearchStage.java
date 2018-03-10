package app.search.stages;

import app.search.controllers.SearchController;
import app.search.listcells.ResultListCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import system.ResourceUtils;
import system.search.SearchEngine;
import system.search.results.AbstractResult;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

public class SearchStage extends Stage {
    private SearchController controller;
    private SearchEngine searchEngine;

    public SearchStage(ResourceBundle resourceBundle, SearchEngine searchEngine) throws IOException {
        this.searchEngine = searchEngine;
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/search_dialog.fxml").toURI().toURL());
        fxmlLoader.setResources(resourceBundle);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ResourceUtils.getResource("/css/search_dialog.css").toURI().toString());
        this.setTitle("Dokey Search");
        this.setScene(scene);
        this.setAlwaysOnTop(true);
        initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        this.getIcons().add(new Image(SearchStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (SearchController) fxmlLoader.getController();
        controller.resultListView.setManaged(false);

        // TODO: esc key close event
        // TODO: stage loose focus close event

        // Setup the list cells
        controller.resultListView.setCellFactory(new Callback<ListView<AbstractResult>, ListCell<AbstractResult>>() {
            @Override
            public ListCell<AbstractResult> call(ListView<AbstractResult> param) {
                return new ResultListCell(resourceBundle);
            }
        });

        // Setup the text field search callbacks
        controller.queryTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchEngine.requestQuery(newValue, (results) -> {
                ObservableList<AbstractResult> observableResults = FXCollections.observableArrayList(results);

                // Update the list view
                Platform.runLater(() -> {
                    controller.resultListView.setItems(observableResults);

                    // Show the list view and refresh stage size to fit all contents
                    controller.resultListView.setManaged(true);
                    sizeToScene();
                });
            });
        });

        Platform.runLater(() -> sizeToScene());
        Platform.runLater(() -> controller.queryTextField.requestFocus());
    }
}
