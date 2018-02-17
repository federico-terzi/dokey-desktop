package app.editor.components;

import app.editor.stages.WebLinkDialogStage;
import javafx.application.Platform;
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
        if (item.getIconID() != null) {
            File imageFile = WebLinkResolver.getImage(item.getIconID());
            if (imageFile != null) {
                loadWebImage(imageFile);
            }else{ // Image not available, request it
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        if (WebLinkResolver.requestImage(item.getIconID())) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    File imageFile = WebLinkResolver.getImage(item.getIconID());
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
        tooltip.setText(item.getTitle() + "\n" + item.getUrl());
        setTooltip(tooltip);
    }

    private void loadWebImage(File imageFile) {
        Image webImage = null;
        if (imageFile != null) {
            try {
                FileInputStream fis = new FileInputStream(imageFile);
                webImage = new Image(fis, 32, 32, true, true);
                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{  // If image is not available, default fallback
            webImage = new Image(ComponentButton.class.getResourceAsStream("/assets/world.png"), 32, 32, true, true);
        }

        // Set the image
        ImageView imageView = new ImageView(webImage);
        setContentDisplay(ContentDisplay.TOP);
        setGraphic(imageView);
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
