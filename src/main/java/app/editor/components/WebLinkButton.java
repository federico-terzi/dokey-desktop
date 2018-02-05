package app.editor.components;

import app.editor.stages.WebLinkDialogStage;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import section.model.Component;
import section.model.FolderItem;
import section.model.WebLinkItem;
import system.WebLinkResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class WebLinkButton extends ComponentButton {
    private WebLinkItem item;

    public WebLinkButton(Component component) {
        super(component);

        item = (WebLinkItem) associatedComponent.getItem();

        // Set up the layout
        setText(item.getTitle());

        // Get the image if present in the cache
        Image webImage = null;
        if (item.getIconID() != null) {
            File imageFile = WebLinkResolver.getImage(item.getIconID());
            if (imageFile != null) {
                try {
                    FileInputStream fis = new FileInputStream(imageFile);
                    webImage = new Image(fis);
                    fis.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (webImage == null) {  // If image is not available, default fallback
            webImage = new Image(ComponentButton.class.getResourceAsStream("/assets/world.png"));
        }

        // Set the image
        ImageView imageView = new ImageView(webImage);
        imageView.setSmooth(true);
        imageView.setFitHeight(32);
        imageView.setFitWidth(32);
        setContentDisplay(ContentDisplay.TOP);
        setGraphic(imageView);

        // Create the tooltip
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(item.getTitle() + "\n" + item.getUrl());
        setTooltip(tooltip);
    }

    @Override
    public void showEditDialog() {
        try {
            WebLinkDialogStage stage = new WebLinkDialogStage(new WebLinkDialogStage.OnWebLinkListener() {
                @Override
                public void onWebLinkSelected(String url, String title, String imageUrl) {
                    // Create the component
                    WebLinkItem item = new WebLinkItem();
                    item.setUrl(url);
                    item.setTitle(title);
                    item.setIconID(imageUrl);

                    associatedComponent.setItem(item);
                    if (getOnComponentActionListener() != null) {
                        getOnComponentActionListener().onComponentEdit();
                    }
                }

                @Override
                public void onCanceled() {

                }
            });
            stage.setWebLinkItem(item);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
