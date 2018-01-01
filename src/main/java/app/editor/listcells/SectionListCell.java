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
            try {
                appImage = new Image(SectionListCell.class.getResource("/assets/apps.png").toURI().toString());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
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
        MenuItem exportItem = new MenuItem("Export...");
        exportItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                onContextMenuListener.onExportSection(section);
            }
        });
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
