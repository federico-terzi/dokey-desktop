package app.editor.components;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import net.model.IconTheme;
import section.model.Component;
import section.model.ShortcutItem;
import system.ShortcutIcon;
import utils.ImageResolver;

import java.util.ResourceBundle;

public class ShortcutButton extends ComponentButton {
    private ShortcutItem item;

    public ShortcutButton(ComponentGrid componentGrid, Component component, ResourceBundle resourceBundle) {
        super(componentGrid, component, resourceBundle);

        // Add the style
        getStyleClass().add("shortcut-button");

        item = (ShortcutItem) component.getItem();

        // Get the icon from the shortcut icon manager
        ShortcutIcon shortcutIcon = null;
        if (item.getIconID() != null) {
            shortcutIcon = componentGrid.getShortcutIconManager().getIcon(item.getIconID(), IconTheme.DARK);
        }

        // Set up the button

        setText(item.getTitle());

        // If there is an image, set it.
        Image image = null;
        if (shortcutIcon != null) {
            if (shortcutIcon.getFile().isFile()) {
                image = ImageResolver.getInstance().getImage(shortcutIcon.getFile(), 36);
            }
        }
        if (image == null) {   // No image found, default fallback
            image = ImageResolver.getInstance().getImage(ComponentButton.class.getResourceAsStream("/assets/image.png"), 36);
        }

        // Create the icon box
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(36);
        imageView.setFitWidth(36);
        VBox box = new VBox();
        box.getStyleClass().add("shortcut-box");
        box.getChildren().addAll(imageView);
        setGraphic(box);
        setContentDisplay(ContentDisplay.TOP);

        // Create the tooltip
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(getParentTooltip() + item.getTitle() + "\n" + item.getShortcut());
        setTooltip(tooltip);
    }
}
