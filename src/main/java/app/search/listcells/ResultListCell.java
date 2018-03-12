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
import system.search.results.AbstractResult;
import system.search.results.GoogleSearchResult;

import java.io.File;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class ResultListCell extends ListCell<AbstractResult> {
    public static final int ROW_HEIGHT = 48;

    private GridPane grid = new GridPane();
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
        grid.setHgap(10);
        grid.setVgap(4);
        grid.setPadding(new Insets(0, 0, 0, 0));
    }
    private void addControlsToGrid() {
        grid.add(image, 0, 0, 1, 2);
        grid.add(title, 1, 0);
        grid.add(description, 1, 1);
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

        setGraphic(grid);
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
