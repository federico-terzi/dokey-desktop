package app.editor.listcells;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import section.model.SystemCommands;
import utils.SystemItemManager;

import java.io.File;

public class SystemListCell extends ListCell<SystemCommands> {

    private GridPane grid = new GridPane();
    private ImageView image = new ImageView();
    private Label name = new Label();

    public SystemListCell() {
        configureGrid();
        addControlsToGrid();
    }

    private void configureGrid() {
        grid.setHgap(10);
        grid.setVgap(4);
        grid.setPadding(new Insets(0, 0, 0, 0));
    }
    private void addControlsToGrid() {
        grid.add(image, 0, 0, 1, 1);
        grid.add(name, 1, 0);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void addContent(SystemCommands systemCommand) {
        setText(null);
        File iconFile = SystemItemManager.getIconForType(systemCommand);
        if (iconFile != null) {
            Image appImage = new Image(iconFile.toURI().toString(), 32, 32, true, true);
            image.setImage(appImage);
        }

        name.setText(systemCommand.getTitle());
        setGraphic(grid);
    }

    @Override
    protected void updateItem(SystemCommands icon, boolean empty) {
        super.updateItem(icon, empty);
        if (empty) {
            clearContent();
        }else{
            addContent(icon);
        }
    }
}
