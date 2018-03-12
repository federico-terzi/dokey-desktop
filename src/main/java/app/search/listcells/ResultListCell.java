package app.search.listcells;

import app.editor.listcells.SectionListCell;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import system.search.results.AbstractResult;
import system.search.results.GoogleSearchResult;

import java.io.File;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class ResultListCell extends ListCell<AbstractResult> {
    public static final int ROW_HEIGHT = 55;

    private HBox hBox = new HBox();
    private ImageView image = new ImageView();
    private Label title = new Label();
    private Label description = new Label();

    private ResourceBundle resourceBundle;
    private Image fallback;
    private ConcurrentHashMap<String, Image> imageCacheMap;

    public ResultListCell(ResourceBundle resourceBundle, Image fallback, ConcurrentHashMap<String, Image> imageCacheMap) {
        this.resourceBundle = resourceBundle;
        this.fallback = fallback;
        this.imageCacheMap = imageCacheMap;

        setPrefHeight(ROW_HEIGHT);

        configureGrid();
        addControlsToGrid();
    }

    private void configureGrid() {
        hBox.getStyleClass().add("dokey-search-result-box");
        title.getStyleClass().add("dokey-search-result-title");
        description.getStyleClass().add("dokey-search-result-description");
        image.getStyleClass().add("dokey-search-result-image");
    }
    private void addControlsToGrid() {
        hBox.getChildren().add(image);

        VBox vBox = new VBox();
        vBox.getChildren().add(title);
        vBox.getChildren().add(description);
        hBox.getChildren().add(vBox);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void addContent(AbstractResult result) {
        setText(null);

        title.setText(result.getTitle());
        description.setText(result.getDescription());

        // If the image is available in the cache, display it immediately
        // If not, show the fallback image and request it.
        String resultHash = result.getHash();
        if (resultHash != null && imageCacheMap.containsKey(resultHash)) {
           image.setImage(imageCacheMap.get(resultHash));
        }else{
            image.setImage(fallback);  // Image fallback

            // Request the image
            result.requestImage((resImage, hashID) -> {
                image.setImage(resImage);

                // Update the cache
                if (hashID != null) {
                    imageCacheMap.put(hashID, resImage);
                }
            });
        }

        // Set up selected result image behaviour
        if (result.isIcon()) {
            image.getStyleClass().add("dokey-search-result-icon");
        }else{
            image.getStyleClass().remove("dokey-search-result-icon");
        }

        setGraphic(hBox);
    }

    @Override
    protected void updateItem(AbstractResult application, boolean empty) {
        super.updateItem(application, empty);
        if (empty) {
            clearContent();
        }else{
            addContent(application);
        }
    }
}
