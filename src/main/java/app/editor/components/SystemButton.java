package app.editor.components;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import section.model.Component;
import section.model.SystemItem;
import utils.SystemItemManager;

import java.io.File;
import java.util.ResourceBundle;

public class SystemButton extends ComponentButton {
    private SystemItem item;

    public SystemButton(ComponentGrid componentGrid, Component component, ResourceBundle resourceBundle) {
        super(componentGrid, component, resourceBundle);

        item = (SystemItem) associatedComponent.getItem();

        getStyleClass().add("system-button");

        // Set up the layout
        setText(item.getTitle());

        File iconFile = SystemItemManager.getIconForType(item.getCommandType());
        if (iconFile != null) {
            Image systemImage = new Image(iconFile.toURI().toString(), 48, 48, true, true);
            ImageView imageView = new ImageView(systemImage);
            setContentDisplay(ContentDisplay.TOP);
            setGraphic(imageView);
        }

        // Create the tooltip
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(getParentTooltip() + item.getTitle());
        setTooltip(tooltip);
    }
}
