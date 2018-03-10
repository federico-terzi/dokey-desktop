package app.search.stages;

import app.controllers.InitializationController;
import app.search.controllers.SearchController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import system.ResourceUtils;

import java.io.IOException;
import java.util.ResourceBundle;

public class SearchStage extends Stage {
    private SearchController controller;

    public SearchStage(ResourceBundle resourceBundle) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/search_dialog.fxml").toURI().toURL());
        fxmlLoader.setResources(resourceBundle);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ResourceUtils.getResource("/css/search_dialog.css").toURI().toString());
        this.setTitle("Dokey Search");
        this.setScene(scene);
        initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        this.getIcons().add(new Image(SearchStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (SearchController) fxmlLoader.getController();
    }
}
