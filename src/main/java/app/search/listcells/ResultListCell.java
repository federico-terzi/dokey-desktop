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

import java.io.File;
import java.util.ResourceBundle;

public class ResultListCell extends ListCell<AbstractResult> {

    private GridPane grid = new GridPane();
    private ImageView image = new ImageView();
    private Label title = new Label();
    private Label description = new Label();

    private ResourceBundle resourceBundle;

    public ResultListCell(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

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

        // Request the image
        result.requestImage((resImage) -> {
            image.setImage(resImage);
        });

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
