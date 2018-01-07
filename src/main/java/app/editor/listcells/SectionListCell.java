package app.editor.listcells;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import section.model.Section;
import section.model.SectionType;
import system.model.Application;
import system.model.ApplicationManager;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Optional;

public class SectionListCell extends ListCell<Section> {

    private ApplicationManager appManager;
    private OnContextMenuListener onContextMenuListener;

    private GridPane grid = new GridPane();
    private ImageView image = new ImageView();
    private Label name = new Label();
    private Label path = new Label();

    public SectionListCell(ApplicationManager appManager, OnContextMenuListener onContextMenuListener) {
        this.appManager = appManager;
        this.onContextMenuListener = onContextMenuListener;

        configureGrid();
        addControlsToGrid();
    }

    private void configureGrid() {
        grid.setHgap(10);
        grid.setVgap(4);
        grid.setPadding(new Insets(0, 0, 0, 0));

        image.setFitWidth(32);
        image.setFitHeight(32);
        image.setSmooth(true);
        name.getStyleClass().add("sectionlistcell-name");
        path.getStyleClass().add("sectionlistcell-path");
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

    private void addContent(Section section) {
        setText(null);

        if (section.getSectionType() == SectionType.SHORTCUTS) {
            Application application = appManager.getApplication(section.getRelatedAppId());

            if (application != null && application.getIconPath() != null) {
                Image appImage = new Image(new File(application.getIconPath()).toURI().toString());
                image.setImage(appImage);

                name.setText(application.getName());
                path.setText(application.getExecutablePath());
            }
        }else if (section.getSectionType() == SectionType.LAUNCHPAD){
            Image appImage = null;
            appImage = new Image(SectionListCell.class.getResourceAsStream("/assets/apps.png"));
            image.setImage(appImage);
            name.setText("Launchpad");
            path.setText("The main application launcher");
        }

        // Set up the context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem reloadItem = new MenuItem("Reload");
        reloadItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                onContextMenuListener.onReloadSection(section);
            }
        });
        Image reloadImage = new Image(SectionListCell.class.getResourceAsStream("/assets/refresh.png"));
        ImageView reloadImageView = new ImageView(reloadImage);
        reloadImageView.setFitWidth(16);
        reloadImageView.setFitHeight(16);
        reloadImageView.setSmooth(true);
        reloadItem.setGraphic(reloadImageView);

        MenuItem exportItem = new MenuItem("Export...");
        exportItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                onContextMenuListener.onExportSection(section);
            }
        });
        Image exportImage = new Image(SectionListCell.class.getResourceAsStream("/assets/export.png"));
        ImageView exportImageView = new ImageView(exportImage);
        exportImageView.setFitWidth(16);
        exportImageView.setFitHeight(16);
        exportImageView.setSmooth(true);
        exportItem.setGraphic(exportImageView);

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Confirmation");
                alert.setHeaderText("Are you sure you want to delete this section?");
                alert.setContentText("If you proceed, the section will be deleted.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {  // OK DELETE
                    onContextMenuListener.onDeleteSection(section);
                }
            }
        });
        Image deleteImage = new Image(SectionListCell.class.getResourceAsStream("/assets/delete.png"));
        ImageView deleteImageView = new ImageView(deleteImage);
        deleteImageView.setFitWidth(16);
        deleteImageView.setFitHeight(16);
        deleteImageView.setSmooth(true);
        deleteItem.setGraphic(deleteImageView);

        contextMenu.getItems().addAll(reloadItem, exportItem, new SeparatorMenuItem(), deleteItem);
        setContextMenu(contextMenu);

        setGraphic(grid);
    }

    @Override
    protected void updateItem(Section section, boolean empty) {
        super.updateItem(section, empty);
        if (empty) {
            clearContent();
        }else{
            addContent(section);
        }
    }

    public interface OnContextMenuListener {
        void onDeleteSection(Section section);
        void onReloadSection(Section section);
        void onExportSection(Section section);
    }
}
