package app.editor.components;

import app.editor.stages.SystemDialogStage;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import section.model.Component;
import section.model.SystemItem;
import system.WebLinkResolver;
import utils.SystemItemManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SystemButton extends ComponentButton {
    private SystemItem item;

    public SystemButton(Component component) {
        super(component);

        item = (SystemItem) associatedComponent.getItem();

        // Set up the layout
        setText(item.getTitle());

        File iconFile = SystemItemManager.getIconForType(item.getCommandType());
        if (iconFile != null) {
            Image folderImage = new Image(iconFile.toURI().toString());
            ImageView imageView = new ImageView(folderImage);
            imageView.setSmooth(true);
            imageView.setFitHeight(32);
            imageView.setFitWidth(32);
            setContentDisplay(ContentDisplay.TOP);
            setGraphic(imageView);
        }

        // Create the tooltip
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(item.getTitle());
        setTooltip(tooltip);
    }

    @Override
    public void showEditDialog() {
        try {
            SystemDialogStage stage = new SystemDialogStage(new SystemDialogStage.OnSystemItemListener() {
                @Override
                public void onSystemItemSelected(SystemItem item) {
                    associatedComponent.setItem(item);
                    if (getOnComponentActionListener() != null) {
                        getOnComponentActionListener().onComponentEdit();
                    }
                }

                @Override
                public void onCanceled() {

                }
            });
            stage.setSystemItem(item);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
