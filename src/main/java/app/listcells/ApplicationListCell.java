package app.listcells;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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

    public ApplicationListCell() {
        configureGrid();
        addControlsToGrid();
    }

    private void configureGrid() {
        grid.setHgap(10);
        grid.setVgap(4);
        grid.setPadding(new Insets(0, 0, 0, 0));

        image.setFitWidth(32);
        image.setFitHeight(32);
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
            Image appImage = new Image(new File(application.getIconPath()).toURI().toString());
            image.setImage(appImage);
        }

        name.setText(application.getName());
        path.setText(application.getExecutablePath());
        setGraphic(grid);
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
}
