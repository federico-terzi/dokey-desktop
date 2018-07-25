package app.search.listcells;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import system.image.ImageResolver;
import system.search.results.AbstractResult;

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
    private ImageResolver imageResolver;

    public ResultListCell(ResourceBundle resourceBundle, Image fallback, ImageResolver imageResolver) {
        this.resourceBundle = resourceBundle;
        this.fallback = fallback;
        this.imageResolver = imageResolver;

        setPrefHeight(ROW_HEIGHT);

        configureGrid();
        addControlsToGrid();
    }

    private void configureGrid() {
        hBox.getStyleClass().add("dokey-search-result-box");
        title.getStyleClass().add("dokey-search-result-title");
        description.getStyleClass().add("dokey-search-result-description");
        image.getStyleClass().add("dokey-search-result-image");

        image.setFitWidth(32);
        image.setFitHeight(32);
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

        Image resolvedImage = imageResolver.resolveImage(result.getImageId(), 32);
        if (resolvedImage != null) {
            image.setImage(resolvedImage);
        }else{
            image.setImage(fallback);
        }

//        // Set up selected result image behaviour
//        image.getStyleClass().clear();
//        if (result.isIcon()) {
//            image.getStyleClass().add("dokey-search-result-icon");
//        }else{
//            image.getStyleClass().add("dokey-search-result-image");
//        }

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
