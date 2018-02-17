package app.editor.listcells;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import system.model.Application;
import system.sicons.ShortcutIcon;

import java.io.File;

public class ShortcutIconListCell extends ListCell<ShortcutIcon> {

    private GridPane grid = new GridPane();
    private ImageView image = new ImageView();
    private Label name = new Label();
    private Label id = new Label();

    public ShortcutIconListCell() {
        configureGrid();
        addControlsToGrid();
    }

    private void configureGrid() {
        grid.setHgap(10);
        grid.setVgap(4);
        grid.setPadding(new Insets(0, 0, 0, 0));

        name.getStyleClass().add("shortcutlistcell-name");
        id.getStyleClass().add("shortcutlistcell-id");
    }
    private void addControlsToGrid() {
        grid.add(image, 0, 0, 1, 2);
        grid.add(name, 1, 0);
        grid.add(id, 1, 1);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void addContent(ShortcutIcon shortcutIcon) {
        setText(null);
        Image appImage = new Image(shortcutIcon.getFile().toURI().toString(), 32, 32, true, true);
        image.setImage(appImage);

        name.setText(shortcutIcon.getName());
        id.setText("ID: "+shortcutIcon.getId());
        setGraphic(grid);
    }

    @Override
    protected void updateItem(ShortcutIcon icon, boolean empty) {
        super.updateItem(icon, empty);
        if (empty) {
            clearContent();
        }else{
            addContent(icon);
        }
    }
}
