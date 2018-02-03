package app.editor.components;

import app.editor.stages.AppSelectDialogStage;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
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

        // Create the tooltip
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(application.getName());
        setTooltip(tooltip);

        // If there is an image, set it.
        if (application.getIconPath() != null) {
            File iconFile = new File(application.getIconPath());
            if (iconFile.isFile()) {
                Image image = new Image(iconFile.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(32);
                imageView.setFitWidth(32);
                imageView.setSmooth(true);
                imageView.setEffect(new DropShadow(5, 4, 4, Color.rgb(0,0,0, 0.3)));
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
