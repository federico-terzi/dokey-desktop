package app.editor.components;

import app.editor.stages.ShortcutDialogStage;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import net.model.IconTheme;
import section.model.Component;
import section.model.ShortcutItem;
import system.model.Application;
import system.sicons.ShortcutIcon;
import system.sicons.ShortcutIconManager;
import utils.OSValidator;

import java.io.File;
import java.io.IOException;

public class ShortcutButton extends ComponentButton {
    private ShortcutItem item;

    public ShortcutButton(ComponentGrid componentGrid, Component component) {
        super(componentGrid, component);

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
                image = new Image(shortcutIcon.getFile().toURI().toString(), 36, 36, true, true);
            }
        }
        if (image == null) {   // No image found, default fallback
            image = new Image(ComponentButton.class.getResourceAsStream("/assets/image.png"), 36, 36, true, true);
        }

        // Create the icon box
        ImageView imageView = new ImageView(image);
        VBox box = new VBox();
        box.getStyleClass().add("shortcut-box");
        box.getChildren().addAll(imageView);
        setGraphic(box);
        setContentDisplay(ContentDisplay.TOP);

        // Create the tooltip
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(item.getTitle() + "\n" + item.getShortcut());
        setTooltip(tooltip);
    }

    @Override
    public void showEditDialog() {
        try {
            ShortcutDialogStage stage = new ShortcutDialogStage(componentGrid.getShortcutIconManager(), new ShortcutDialogStage.OnShortcutListener() {
                @Override
                public void onShortcutSelected(String shortcut, String name, ShortcutIcon icon) {
                    // Create the component
                    ShortcutItem item = new ShortcutItem();
                    item.setShortcut(shortcut);
                    item.setTitle(name);

                    // If an icon is specified, save the id
                    if (icon != null) {
                        item.setIconID(icon.getId());
                    }

                    associatedComponent.setItem(item);
                    if (getOnComponentActionListener() != null) {
                        getOnComponentActionListener().onComponentEdit();
                    }
                }

                @Override
                public void onCanceled() {

                }
            });
            stage.setShortcutItem(item);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
