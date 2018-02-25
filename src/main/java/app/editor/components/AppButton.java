package app.editor.components;

import app.editor.stages.AppSelectDialogStage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import section.model.AppItem;
import section.model.Component;
import section.model.ItemType;
import system.BroadcastManager;
import system.model.Application;
import system.model.ApplicationManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AppButton extends ComponentButton {
    private Application application;

    public AppButton(ComponentGrid componentGrid, Component component) {
        super(componentGrid, component);

        // Get the item
        AppItem item = (AppItem) component.getItem();

        // Get the application
        application = componentGrid.getApplicationManager().getApplication(item.getAppID());

        String title = null;
        Image image = null;

        if (application != null) {
            title = application.getName();
            if (application.getIconPath() != null) {
                File iconFile = new File(application.getIconPath());
                if (iconFile.isFile()) {
                    image = new Image(iconFile.toURI().toString(), 48, 48, true, true);
                }
            }
        }else{  // Fallback
            title = item.getTitle();
            image = new Image(ComponentButton.class.getResourceAsStream("/assets/image.png"), 48, 48, true, true);
        }

        // Set the button properties
        setText(title);

        // Create the tooltip
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(getParentTooltip() + title + "\n" + item.getAppID());
        setTooltip(tooltip);

        // Set up the image
        if (image != null) {
            ImageView imageView = new ImageView(image);
            setGraphic(imageView);
            setContentDisplay(ContentDisplay.TOP);
        }

    }

    @Override
    protected void getContextMenu(List<MenuItem> items) {
        super.getContextMenu(items);

        MenuItem openShortcutPage = new MenuItem("Open Shortcuts...");
        openShortcutPage.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.OPEN_SHORTCUT_PAGE_FOR_APPLICATION_EVENT, application.getExecutablePath());
            }
        });
        Image pageImage = new Image(ComponentButton.class.getResourceAsStream("/assets/keyboard.png"));
        ImageView pageImageView = new ImageView(pageImage);
        pageImageView.setFitWidth(16);
        pageImageView.setFitHeight(16);
        openShortcutPage.setGraphic(pageImageView);

        if (associatedComponent.getItem().isValid()) {  // Add this option only if the item is valid
            items.add(new SeparatorMenuItem());
            items.add(openShortcutPage);
        }
    }
}
