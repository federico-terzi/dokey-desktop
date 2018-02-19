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
        // TODO: with null application, throw exception

        // Set the button properties
        setText(application.getName());

        // Create the tooltip
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(application.getName());
        setTooltip(tooltip);

        // If there is an image, set it.
        if (application.getIconPath() != null) {
            File iconFile = new File(application.getIconPath());
            if (iconFile.isFile()) {
                Image image = new Image(iconFile.toURI().toString(), 48, 48, true, true);
                ImageView imageView = new ImageView(image);
                setGraphic(imageView);
                setContentDisplay(ContentDisplay.TOP);
            }
        }

    }

    @Override
    public void showEditDialog() {
        try {
            AppSelectDialogStage appSelectDialogStage = new AppSelectDialogStage(componentGrid.getApplicationManager(), new AppSelectDialogStage.OnApplicationListener() {
                @Override
                public void onApplicationSelected(Application application) {
                    // Create the component
                    AppItem appItem = new AppItem();
                    appItem.setAppID(application.getExecutablePath());
                    appItem.setTitle(application.getName());
                    appItem.setItemType(ItemType.APP);

                    associatedComponent.setItem(appItem);
                    if (getOnComponentActionListener() != null) {
                        getOnComponentActionListener().onComponentEdit();
                    }
                }

                @Override
                public void onCanceled() {

                }
            });
            appSelectDialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void getContextMenu(List<MenuItem> items) {
        super.getContextMenu(items);

        items.add(new SeparatorMenuItem());

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

        items.add(openShortcutPage);
    }
}
