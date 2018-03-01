package app.editor.components;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import section.model.Component;
import section.model.FolderItem;

import java.util.ResourceBundle;

public class FolderButton extends ComponentButton {
    private FolderItem item;

    public FolderButton(ComponentGrid componentGrid, Component component, ResourceBundle resourceBundle) {
        super(componentGrid, component, resourceBundle);

        item = (FolderItem) associatedComponent.getItem();

        // Set up the layout
        setText(item.getTitle());

        getStyleClass().add("folder-button");

        Image folderImage = new Image(ComponentButton.class.getResourceAsStream("/assets/folder_white.png"), 48, 48, true, true);
        ImageView imageView = new ImageView(folderImage);
        setContentDisplay(ContentDisplay.TOP);
        setGraphic(imageView);

        // Create the tooltip
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(getParentTooltip() + item.getTitle()+"\n"+item.getPath());
        setTooltip(tooltip);
    }
}
