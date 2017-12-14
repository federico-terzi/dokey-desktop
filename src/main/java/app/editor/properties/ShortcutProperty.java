package app.editor.properties;


import app.editor.controllers.ApplicationPropertyController;
import app.editor.controllers.ShortcutPropertyController;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import section.model.AppItem;
import section.model.Component;
import section.model.ShortcutItem;
import system.model.Application;
import system.model.ApplicationManager;
import system.sicons.ShortcutIcon;
import system.sicons.ShortcutIconManager;

import java.io.File;
import java.io.IOException;

public class ShortcutProperty extends Property {
    public ShortcutProperty(Component associatedComponent, ShortcutIconManager shortcutIconManager) {
        super(associatedComponent, null);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/properties/shortcut_property.fxml"));
        VBox root = null;
        try {
            root = fxmlLoader.load();
            root.setFillWidth(true);
            root.setMaxWidth(Double.MAX_VALUE);
            getChildren().add(root);

            ShortcutPropertyController controller = (ShortcutPropertyController) fxmlLoader.getController();

            ShortcutItem shortcutItem = (ShortcutItem) associatedComponent.getItem();
            controller.shortcutTitleLabel.setText(shortcutItem.getTitle());
            controller.shortcutTextField.setText(shortcutItem.getShortcut());

            ShortcutIcon shortcutIcon = shortcutIconManager.getIcon(shortcutItem.getIconID());

            if (shortcutIcon != null) {
                Image image = new Image(shortcutIcon.getFile().toURI().toString());
                controller.shortcutImageView.setImage(image);
            } else {
                controller.shortcutImageView.setManaged(false);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
