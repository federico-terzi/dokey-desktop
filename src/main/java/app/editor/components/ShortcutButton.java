package app.editor.components;

import app.editor.stages.ShortcutDialogStage;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import section.model.Component;
import section.model.ShortcutItem;
import system.model.Application;
import system.sicons.ShortcutIcon;
import system.sicons.ShortcutIconManager;

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
                Image image = new Image(shortcutIcon.getFile().toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(32);
                imageView.setFitWidth(32);
                imageView.setSmooth(true);
                vBox.getChildren().add(imageView);
            }
        }

        Label titleLabel = new Label(item.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px");
        titleLabel.setWrapText(true);
        Label shortcutLabel = new Label(item.getShortcut());
        shortcutLabel.setStyle("-fx-font-style: italic; -fx-font-size: 11px");
        shortcutLabel.setTextAlignment(TextAlignment.CENTER);
        shortcutLabel.setWrapText(true);
        vBox.getChildren().addAll(titleLabel, shortcutLabel);
        setGraphic(vBox);
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
