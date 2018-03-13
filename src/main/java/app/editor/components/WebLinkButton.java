package app.editor.components;

import javafx.application.Platform;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import section.model.Component;
import section.model.WebLinkItem;
import utils.ImageResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

public class WebLinkButton extends ComponentButton {
    private WebLinkItem item;

    public WebLinkButton(ComponentGrid componentGrid, Component component, ResourceBundle resourceBundle) {
        super(componentGrid, component, resourceBundle);

        item = (WebLinkItem) associatedComponent.getItem();

        // Set up the layout
        setText(item.getTitle());

        // Get the image if present in the cache
        if (item.getIconID() != null) {
            File imageFile = componentGrid.getWebLinkResolver().getImage(item.getIconID());
            if (imageFile != null) {
                loadWebImage(imageFile);
            }else{ // Image not available, request it
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        if (componentGrid.getWebLinkResolver().requestImage(item.getIconID())) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    File imageFile = componentGrid.getWebLinkResolver().getImage(item.getIconID());
                                    loadWebImage(imageFile);
                                }
                            });
                        }
                    }
                }).start();
            }
        }else{
            loadWebImage(null); // Default one
        }

        // Create the tooltip
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(getParentTooltip() + item.getTitle() + "\n" + item.getUrl());
        setTooltip(tooltip);
    }

    private void loadWebImage(File imageFile) {
        Image webImage = null;
        if (imageFile != null) {
            try {
                FileInputStream fis = new FileInputStream(imageFile);
                webImage = ImageResolver.getInstance().getImage(fis, 48);
                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{  // If image is not available, default fallback
            webImage = ImageResolver.getInstance().getImage(ComponentButton.class.getResourceAsStream("/assets/world.png"), 48);
        }

        // Set the image
        ImageView imageView = new ImageView(webImage);
        imageView.setFitHeight(48);
        imageView.setFitWidth(48);
        setContentDisplay(ContentDisplay.TOP);
        setGraphic(imageView);
    }
}
