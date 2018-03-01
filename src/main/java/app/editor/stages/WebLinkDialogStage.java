package app.editor.stages;

import app.editor.controllers.WebLinkDialogController;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
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
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import section.model.WebLinkItem;
import system.ResourceUtils;
import system.WebLinkResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class WebLinkDialogStage extends Stage {
    private WebLinkDialogController controller;
    private WebLinkResolver webLinkResolver;
    private OnWebLinkListener onWebLinkListener;

    private String imageUrl = null;

    private static int SEARCH_AFTER = 500; // milliseconds

    private PublishSubject<String> urlSubject = PublishSubject.create();

    private boolean isEdit = false;  // False when creating a new item, true when editing

    public WebLinkDialogStage(WebLinkResolver webLinkResolver, ResourceBundle resourceBundle,
                              OnWebLinkListener onWebLinkListener) throws IOException {
        this.webLinkResolver = webLinkResolver;
        this.onWebLinkListener = onWebLinkListener;

        // Load the layout
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/web_link_dialog.fxml").toURI().toURL());
        fxmlLoader.setResources(resourceBundle);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        this.setTitle(resourceBundle.getString("web_link"));
        this.setScene(scene);
        this.getIcons().add(new Image(WebLinkDialogStage.class.getResourceAsStream("/assets/icon.png")));
        scene.getStylesheets().add(ResourceUtils.getResource("/css/main.css").toURI().toString());
        setResizable(false);
        setAlwaysOnTop(true);

        controller = (WebLinkDialogController) fxmlLoader.getController();

        // Setup the image
        Image image = new Image(WebLinkDialogStage.class.getResourceAsStream("/assets/world.png"), 64, 64, true, true);
        controller.imageView.setImage(image);

        // Listener for the url field change
        controller.urlTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!isEdit) {
                    urlSubject.onNext(newValue);

                    isEdit = false;
                }
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
                // Make sure the link begins with a scheme
                String url = controller.urlTextField.getText();
                if (!url.startsWith("http")) {
                    url = "http://"+url;
                }

                onWebLinkListener.onWebLinkSelected(url,
                        controller.titleTextField.getText(),
                        imageUrl);
                close();
            }
        });

        // Refresh button
        controller.refreshBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                performSearch(controller.urlTextField.getText());
            }
        });

        // Focus the text field on startup
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.urlTextField.requestFocus();
            }
        });

        // Hide the progress bar
        controller.progressBar.setVisible(false);

        // Search subscription with debaunce
        urlSubject.debounce(500, TimeUnit.MILLISECONDS).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String url) {
                performSearch(url);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void performSearch(String urlQuery) {
        // Make sure the url is valid
        try {
            // Consider the case the url doesn't have the scheme ( HTTP )
            // a basic check is provided ( contains a dot )
            if (urlQuery.contains(".") && !urlQuery.startsWith("http")) {
                urlQuery = "http://"+urlQuery;
            }

            final String url = urlQuery;

            URL u = new URL(url);
            u.toURI();

            // Request the attributes
            getAttributes(url);
        } catch (Exception e) {}
    }

    private void getAttributes(String url) {
        // Run the progress bar
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controller.progressBar.setVisible(true);
                controller.progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            }
        });

        // Get the attributes and populate the fields
        Task getAttributesTask = new Task() {
            @Override
            protected Object call() throws Exception {
                // Get the attributes
                WebLinkResolver.Result res = webLinkResolver.getAttributes(url);

                // Update the fields
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        // Stop the progressbar
                        controller.progressBar.setProgress(0);
                        controller.progressBar.setVisible(false);

                        if (res != null) {  // If a result is available
                            // Populate the title
                            if (res.title != null) {
                                controller.titleTextField.setText(res.title);
                            }

                            // Populate the image
                            if (res.imageUrl != null) {
                                // Get the image from the cache
                                File imageFile = webLinkResolver.getImage(res.imageUrl);

                                if (imageFile != null) {
                                    // Setup the image
                                    try {
                                        FileInputStream fis = new FileInputStream(imageFile);
                                        Image image = new Image(fis, 64, 64, true, true);
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
                                Image image = new Image(WebLinkDialogStage.class.getResourceAsStream("/assets/world.png"), 64, 64, true, true);
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
        isEdit = true;

        // Set the fields
        controller.urlTextField.setText(item.getUrl());
        controller.titleTextField.setText(item.getTitle());

        // Set the image
        if (item.getIconID() != null) {
            imageUrl = item.getIconID();
            File imageFile = webLinkResolver.getImage(item.getIconID());
            if (imageFile != null) {
                try {
                    FileInputStream fis = new FileInputStream(imageFile);
                    Image image = new Image(fis, 48, 48, true, true);
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
            Image image = new Image(WebLinkDialogStage.class.getResourceAsStream("/assets/world.png"), 48, 48, true, true);
            controller.imageView.setImage(image);
        }
    }

    public interface OnWebLinkListener {
        void onWebLinkSelected(String url, String title, String imageUrl);
        void onCanceled();
    }
}
