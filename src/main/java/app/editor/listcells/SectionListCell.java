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
import system.section.SectionInfoResolver;
import utils.ImageResolver;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.ResourceBundle;

public class SectionListCell extends ListCell<Section> {

    private ApplicationManager appManager;
    private SectionInfoResolver sectionInfoResolver;
    private ResourceBundle resourceBundle;
    private OnContextMenuListener onContextMenuListener;

    private GridPane grid = new GridPane();
    private ImageView image = new ImageView();
    private Label name = new Label();
    private Label path = new Label();

    public SectionListCell(ApplicationManager appManager, ResourceBundle resourceBundle, OnContextMenuListener onContextMenuListener) {
        this.appManager = appManager;
        this.resourceBundle = resourceBundle;
        this.onContextMenuListener = onContextMenuListener;
        sectionInfoResolver = new SectionInfoResolver(appManager, resourceBundle);

        configureGrid();
        addControlsToGrid();
    }

    private void configureGrid() {
        grid.setHgap(10);
        grid.setVgap(4);
        grid.setPadding(new Insets(0, 0, 0, 0));
        name.getStyleClass().add("sectionlistcell-name");
        path.getStyleClass().add("sectionlistcell-path");
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

    private void addContent(Section section) {
        setText(null);

        SectionInfoResolver.SectionInfo sectionInfo = sectionInfoResolver.getSectionInfo(section);

        if (sectionInfo != null) {
            image.setImage(sectionInfo.image);
            name.setText(sectionInfo.name);
            path.setText(sectionInfo.description);
        }

        if (section.getSectionType() == SectionType.SHORTCUTS) {
            image.getStyleClass().remove("style-icon");
        }else if (section.getSectionType() == SectionType.LAUNCHPAD || section.getSectionType() == SectionType.SYSTEM){
            image.getStyleClass().add("style-icon");
        }

        // Set up the context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem reloadItem = new MenuItem(resourceBundle.getString("reload"));
        reloadItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                onContextMenuListener.onReloadSection(section);
            }
        });
        Image reloadImage = ImageResolver.getInstance().getImage(SectionListCell.class.getResourceAsStream("/assets/refresh_black.png"), 16);
        ImageView reloadImageView = new ImageView(reloadImage);
        reloadImageView.setFitWidth(16);
        reloadImageView.setFitHeight(16);
        reloadImageView.setSmooth(true);
        reloadItem.setGraphic(reloadImageView);

        MenuItem exportItem = new MenuItem(resourceBundle.getString("export"));
        exportItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                onContextMenuListener.onExportSection(section);
            }
        });
        Image exportImage = ImageResolver.getInstance().getImage(SectionListCell.class.getResourceAsStream("/assets/export.png"), 16);
        ImageView exportImageView = new ImageView(exportImage);
        exportImageView.setFitWidth(16);
        exportImageView.setFitHeight(16);
        exportImageView.setSmooth(true);
        exportItem.setGraphic(exportImageView);

        MenuItem deleteItem = new MenuItem(resourceBundle.getString("delete"));
        deleteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(resourceBundle.getString("delete_confirmation"));
                alert.setHeaderText(resourceBundle.getString("delete_section_msg"));
                alert.setContentText(resourceBundle.getString("delete_section_msg2"));

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {  // OK DELETE
                    onContextMenuListener.onDeleteSection(section);
                }
            }
        });
        Image deleteImage = ImageResolver.getInstance().getImage(SectionListCell.class.getResourceAsStream("/assets/delete.png"), 16);
        ImageView deleteImageView = new ImageView(deleteImage);
        deleteImageView.setFitWidth(16);
        deleteImageView.setFitHeight(16);
        deleteImageView.setSmooth(true);
        deleteItem.setGraphic(deleteImageView);

        contextMenu.getItems().addAll(reloadItem, exportItem);

        if (section.getSectionType() == SectionType.SHORTCUTS) {  // Delete item is available only to shortcut sections
            contextMenu.getItems().addAll(new SeparatorMenuItem(), deleteItem);
        }

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
