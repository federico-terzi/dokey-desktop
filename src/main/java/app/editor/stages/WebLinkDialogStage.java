package app.editor.stages;

import app.editor.controllers.WebLinkDialogController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import section.model.WebLinkItem;
import system.ResourceUtils;
import system.WebLinkResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.FutureTask;

public class WebLinkDialogStage extends Stage {
    private WebLinkDialogController controller;
    private OnWebLinkListener onWebLinkListener;

    private String imageUrl = null;

    private static int SEARCH_AFTER = 500; // milliseconds
    private Thread searchThread = null;

    public WebLinkDialogStage(OnWebLinkListener onWebLinkListener) throws IOException {
        this.onWebLinkListener = onWebLinkListener;

        // Load the layout
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/web_link_dialog.fxml").toURI().toURL());
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        this.setTitle("Web Link");
        this.setScene(scene);
        this.getIcons().add(new Image(WebLinkDialogStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (WebLinkDialogController) fxmlLoader.getController();

        // Setup the image
        Image image = new Image(WebLinkDialogStage.class.getResourceAsStream("/assets/world.png"));
        controller.imageView.setImage(image);

        // Listener for the url field change
        controller.urlTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // Make sure the url is valid
                try {
                    URL u = new URL(newValue);
                    u.toURI();

                    // Stop the previous thread if running
                    // NOTE: it acts like a debouncing mechanism.
                    if (searchThread != null) {
                        searchThread.stop();
                    }

                    // Start a new thread to check the attributes
                    searchThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // Request the attributes
                            getAttributes(newValue);
                        }
                    });
                    searchThread.start();

                } catch (Exception e) {}
            }
        });

        // Cancel button
        controller.cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                onWebLinkListener.onCanceled();
                close();
            }
        });

        // Select button
        controller.selectBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                onWebLinkListener.onWebLinkSelected(controller.urlTextField.getText(),
                        controller.titleTextField.getText(),
                        imageUrl);
                close();
            }
        });

        // Focus the text field on startup
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.urlTextField.requestFocus();
            }
        });
    }

    private void getAttributes(String url) {
        // Sleep for a bit. NOTE: part of the debouncing mechanism.
        try {
            Thread.sleep(SEARCH_AFTER);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Run the progress bar
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.progressBar.setManaged(true);
                controller.progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            }
        });

        // Get the attributes and populate the fields
        Task getAttributesTask = new Task() {
            @Override
            protected Object call() throws Exception {
                // Get the attributes
                WebLinkResolver.Result res = WebLinkResolver.getAttributes(url);

                // Update the fields
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        // Stop the progressbar
                        controller.progressBar.setProgress(0);
                        controller.progressBar.setManaged(false);

                        if (res != null) {  // If a result is available
                            // Populate the title
                            if (res.title != null) {
                                controller.titleTextField.setText(res.title);
                            }

                            // Populate the image
                            if (res.imageUrl != null) {
                                // Get the image from the cache
                                File imageFile = WebLinkResolver.getImage(res.imageUrl);

                                if (imageFile != null) {
                                    // Setup the image
                                    try {
                                        FileInputStream fis = new FileInputStream(imageFile);
                                        Image image = new Image(fis);
                                        controller.imageView.setImage(image);
                                        fis.close();
                                        imageUrl = res.imageUrl;
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }else{ // Image not available, default fallback
                                Image image = new Image(WebLinkDialogStage.class.getResourceAsStream("/assets/world.png"));
                                controller.imageView.setImage(image);
                                imageUrl = null;
                            }
                        }
                    }
                });

                return null;
            }
        };

        new Thread(getAttributesTask).start();
    }

    /**
     * Set the web link item, used when modifying a link.
     * @param item
     */
    public void setWebLinkItem(WebLinkItem item) {
        // Set the fields
        controller.urlTextField.setText(item.getUrl());
        controller.titleTextField.setText(item.getTitle());

        // Set the image
        if (item.getIconID() != null) {
            imageUrl = item.getIconID();
            File imageFile = WebLinkResolver.getImage(item.getIconID());
            if (imageFile != null) {
                try {
                    FileInputStream fis = new FileInputStream(imageFile);
                    Image image = new Image(fis);
                    controller.imageView.setImage(image);
                    fis.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            // Default image fallback
            Image image = new Image(WebLinkDialogStage.class.getResourceAsStream("/assets/world.png"));
            controller.imageView.setImage(image);
        }
    }

    public interface OnWebLinkListener {
        void onWebLinkSelected(String url, String title, String imageUrl);
        void onCanceled();
    }
}
