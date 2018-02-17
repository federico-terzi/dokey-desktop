package app.editor.listcells;

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
import system.model.Application;

import java.io.File;

public class ApplicationListCell extends ListCell<Application> {

    private GridPane grid = new GridPane();
    private ImageView image = new ImageView();
    private Label name = new Label();
    private Label path = new Label();

    private OnContextMenuListener onContextMenuListener;

    public ApplicationListCell() {
        configureGrid();
        addControlsToGrid();
    }

    private void configureGrid() {
        grid.setHgap(10);
        grid.setVgap(4);
        grid.setPadding(new Insets(0, 0, 0, 0));

        name.getStyleClass().add("applistcell-name");
        path.getStyleClass().add("applistcell-path");
    }
    private void addControlsToGrid() {
        grid.add(image, 0, 0, 1, 2);
        grid.add(name, 1, 0);
        grid.add(path, 1, 1);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void addContent(Application application) {
        setText(null);
        if (application.getIconPath() != null) {
            Image appImage = new Image(new File(application.getIconPath()).toURI().toString(), 32, 32, true, true);
            image.setImage(appImage);
        }

        name.setText(application.getName());
        path.setText(application.getExecutablePath());
        setGraphic(grid);

        // Set up the context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onContextMenuListener != null) {
                    onContextMenuListener.onDeleteApplication(application);
                }
            }
        });
        Image deleteImage = new Image(SectionListCell.class.getResourceAsStream("/assets/delete.png"), 16, 16, true, true);
        ImageView deleteImageView = new ImageView(deleteImage);
        deleteItem.setGraphic(deleteImageView);

        contextMenu.getItems().addAll(deleteItem);
        setContextMenu(contextMenu);
    }

    @Override
    protected void updateItem(Application application, boolean empty) {
        super.updateItem(application, empty);
        if (empty) {
            clearContent();
        }else{
            addContent(application);
        }
    }

    public void setOnContextMenuListener(OnContextMenuListener onContextMenuListener) {
        this.onContextMenuListener = onContextMenuListener;
    }

    public interface OnContextMenuListener {
        void onDeleteApplication(Application application);
    }
}
