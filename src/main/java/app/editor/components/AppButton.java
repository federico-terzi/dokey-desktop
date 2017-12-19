package app.editor.components;

import app.editor.stages.AppSelectDialogStage;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import section.model.AppItem;
import section.model.Component;
import section.model.ItemType;
import system.model.Application;
import system.model.ApplicationManager;

import java.io.File;
import java.io.IOException;

public class AppButton extends ComponentButton {
    private Application application;
    private ApplicationManager applicationManager;

    public AppButton(Component associatedComponent, Application application, ApplicationManager applicationManager) {
        super(associatedComponent);
        this.application = application;
        this.applicationManager = applicationManager;

        // Set the button properties
        setText(application.getName());

        // If there is an image, set it.
        if (application.getIconPath() != null) {
            File iconFile = new File(application.getIconPath());
            if (iconFile.isFile()) {
                Image image = new Image(iconFile.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(32);
                imageView.setFitWidth(32);
                imageView.setSmooth(true);
                setGraphic(imageView);
                setContentDisplay(ContentDisplay.TOP);
            }
        }

    }

    @Override
    public void showEditDialog() {
        try {
            AppSelectDialogStage appSelectDialogStage = new AppSelectDialogStage(applicationManager, new AppSelectDialogStage.OnApplicationListener() {
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
}
