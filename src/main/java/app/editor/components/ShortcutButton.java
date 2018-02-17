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
import section.model.Component;
import section.model.ShortcutItem;
import system.model.Application;
import system.sicons.ShortcutIcon;
import system.sicons.ShortcutIconManager;
import utils.OSValidator;

import java.io.File;
import java.io.IOException;

public class ShortcutButton extends ComponentButton {
    private ShortcutIconManager shortcutIconManager;
    private ShortcutItem item;

    public ShortcutButton(Component component, ShortcutIcon shortcutIcon, ShortcutIconManager shortcutIconManager) {
        super(component);
        this.shortcutIconManager = shortcutIconManager;

        item = (ShortcutItem) associatedComponent.getItem();

        // Set up the layout
        VBox vBox = new VBox();
        vBox.setFillWidth(true);
        vBox.setAlignment(Pos.CENTER);

        // If there is an image, set it.
        if (shortcutIcon != null) {
            if (shortcutIcon.getFile().isFile()) {
                Image image = new Image(shortcutIcon.getFile().toURI().toString(), 32, 32, true, true);
                ImageView imageView = new ImageView(image);
                vBox.getChildren().add(imageView);
            }
        }

        Label titleLabel = new Label(item.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px");
        Label shortcutLabel = new Label(getDisplayShortcut());
        shortcutLabel.setStyle("-fx-font-style: italic; -fx-font-size: 11px");
        shortcutLabel.setTextAlignment(TextAlignment.CENTER);
        vBox.getChildren().addAll(titleLabel, shortcutLabel);
        setGraphic(vBox);

        // Create the tooltip
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(item.getTitle() + "\n" + item.getShortcut());
        setTooltip(tooltip);
    }

    /**
     * @return the display version of the shortcut, correcting OS differences.
     */
    private String getDisplayShortcut() {
        String shortcut = item.getShortcut();

        if (OSValidator.isWindows()) {
            shortcut = shortcut.replace("META", "WIN");
        }else if (OSValidator.isMac()) {
            shortcut = shortcut.replace("META", "CMD");
        }

        return shortcut;
    }

    @Override
    public void showEditDialog() {
        try {
            ShortcutDialogStage stage = new ShortcutDialogStage(shortcutIconManager, new ShortcutDialogStage.OnShortcutListener() {
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
