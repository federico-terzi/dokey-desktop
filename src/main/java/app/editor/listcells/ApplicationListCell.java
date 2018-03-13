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
import utils.ImageResolver;

import java.io.File;
import java.util.ResourceBundle;

public class ApplicationListCell extends ListCell<Application> {

    private GridPane grid = new GridPane();
    private ImageView image = new ImageView();
    private Label name = new Label();
    private Label path = new Label();

    private OnContextMenuListener onContextMenuListener;
    private ResourceBundle resourceBundle;

    public ApplicationListCell(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        configureGrid();
        addControlsToGrid();
    }

    private void configureGrid() {
        grid.setHgap(10);
        grid.setVgap(4);
        grid.setPadding(new Insets(0, 0, 0, 0));

        name.getStyleClass().add("applistcell-name");
        path.getStyleClass().add("applistcell-path");
        image.setFitWidth(32);
        image.setFitHeight(32);
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

        Image appImage = ImageResolver.getInstance().getImage(application.getIconFile(), 32);
        image.setImage(appImage);

        name.setText(application.getName());
        path.setText(application.getExecutablePath());
        setGraphic(grid);

        // Set up the context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem(resourceBundle.getString("delete"));
        deleteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (onContextMenuListener != null) {
                    onContextMenuListener.onDeleteApplication(application);
                }
            }
        });
        Image deleteImage = ImageResolver.getInstance().getImage(SectionListCell.class.getResourceAsStream("/assets/delete.png"), 16);
        ImageView deleteImageView = new ImageView(deleteImage);
        deleteImageView.setFitHeight(16);
        deleteImageView.setFitWidth(16);
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
