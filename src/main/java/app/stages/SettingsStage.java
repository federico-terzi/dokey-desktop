package app.stages;

import app.controllers.SettingsController;
import app.editor.listcells.ApplicationListCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import system.StorageManager;
import system.ResourceUtils;
import system.SettingsManager;
import system.StartupManager;
import system.model.Application;
import system.model.ApplicationManager;
import utils.OSValidator;

import java.io.*;
import java.util.*;

public class SettingsStage extends Stage {
    private ApplicationManager applicationManager;
    private SettingsManager settingsManager;
    private OnSettingsCloseListener onSettingsCloseListener;

    private SettingsController controller;

    public SettingsStage(ApplicationManager applicationManager, ResourceBundle resourceBundle,
                         SettingsManager settingsManager, OnSettingsCloseListener onSettingsCloseListener) throws IOException {
        this.applicationManager = applicationManager;
        this.settingsManager = settingsManager;
        this.onSettingsCloseListener = onSettingsCloseListener;

        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/settings_dialog.fxml").toURI().toURL());
        fxmlLoader.setResources(resourceBundle);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ResourceUtils.getResource("/css/applistcell.css").toURI().toString());
        this.setTitle(resourceBundle.getString("settings"));
        this.setResizable(false);
        this.setScene(scene);
        this.getIcons().add(new Image(SettingsStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (SettingsController) fxmlLoader.getController();

        // Add application button
        controller.addApplicationBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(resourceBundle.getString("select_external_app"));
                if (OSValidator.isWindows()) {
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("Application EXE", "*.exe")
                    );
                }else if (OSValidator.isMac()) {
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("Application APP", "*.app")
                    );
                }else{
                    System.err.println("File chooser not configured for this system.");
                    return;
                }

                File appPath = fileChooser.showOpenDialog(SettingsStage.this);
                if (appPath != null) {
                    applicationManager.addExternalApplication(appPath.getAbsolutePath());
                    loadExternalApplications();
                }
            }
        });

        controller.externalAppListView.setCellFactory(new Callback<ListView<Application>, ListCell<Application>>() {
            @Override
            public ListCell<Application> call(ListView<Application> param) {
                ApplicationListCell applicationListCell = new ApplicationListCell(resourceBundle);
                applicationListCell.setOnContextMenuListener(new ApplicationListCell.OnContextMenuListener() {
                    @Override
                    public void onDeleteApplication(Application application) {
                        applicationManager.removeExternalApplication(application.getExecutablePath());
                        loadExternalApplications();
                    }
                });
                return applicationListCell;
            }
        });

        // AUTOMATIC STARTUP
        controller.startupCheckbox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Task startupTask = new Task() {
                    @Override
                    protected Object call() throws Exception {
                        StartupManager startupManager = StartupManager.getInstance();

                        boolean result;
                        if (startupManager.isAutomaticStartupEnabled()) {
                            result = startupManager.disableAutomaticStartup();
                        }else{
                            result = startupManager.enableAutomaticStartup();
                        }

                        // Update the checkbox
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                controller.startupCheckbox.setSelected(startupManager.isAutomaticStartupEnabled());
                            }
                        });

                        if (!result) {  // An error occurred
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle(resourceBundle.getString("error"));
                            alert.setHeaderText(resourceBundle.getString("cannot_start_automatically"));

                            Optional<ButtonType> alertResult = alert.showAndWait();
                        }

                        return null;
                    }
                };
                new Thread(startupTask).start();
            }
        });

        // DOKEY SEARCH
        controller.enableDokeySearchCheckbox.setSelected(settingsManager.isDokeySearchEnabled());
        controller.enableDokeySearchCheckbox.selectedProperty().addListener(((observable, oldValue, value) -> {
            settingsManager.setDokeySearchEnabled(value);
        }));

        // CLEAR CACHE
        controller.clearCacheBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                StorageManager storageManager = StorageManager.getInstance();

                boolean result = storageManager.clearCache();
                if (result) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(resourceBundle.getString("cache_deleted"));
                    alert.setHeaderText(resourceBundle.getString("cache_deleted_msg"));

                    Optional<ButtonType> alertResult = alert.showAndWait();
                    System.exit(0);
                }else{
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error deleting cache!");
                    alert.setHeaderText("Unfortunately, the cache couldn't be deleted!");

                    Optional<ButtonType> alertResult = alert.showAndWait();
                }
            }
        });

        setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (onSettingsCloseListener != null) {
                    onSettingsCloseListener.onSettingsClosed();
                }
            }
        });
        // View licenses btn
        controller.viewLicensesBtn.setOnAction(event -> {
            applicationManager.openWebLink("https://dokey.io/credits.html");
        });

        // Load the external applications
        loadExternalApplications();

        // Load start on startup status
        controller.startupCheckbox.setSelected(StartupManager.getInstance().isAutomaticStartupEnabled());
    }

    private void loadExternalApplications() {
        Task loadTask = new Task() {
            @Override
            protected Object call() throws Exception {
                List<String> externalApps = applicationManager.loadExternalAppPaths();
                List<Application> applications = new ArrayList<>();
                for (String appPath : externalApps) {
                    Application application = applicationManager.getApplication(appPath);
                    if (application != null) {
                        applications.add(application);
                    }
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        populateExternalAppListView(applications);
                    }
                });

                return null;
            }
        };

        new Thread(loadTask).start();
    }

    private void populateExternalAppListView(List<Application> apps) {
        ObservableList<Application> applications = FXCollections.observableArrayList(apps);

        Collections.sort(applications);
        controller.externalAppListView.setItems(applications);
    }

    public interface OnSettingsCloseListener {
        void onSettingsClosed();
    }
}
