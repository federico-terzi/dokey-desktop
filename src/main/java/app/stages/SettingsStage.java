package app.stages;

import app.UIControllers.SettingsController;
import app.editor.listcells.ApplicationListCell;
import app.editor.stages.AppSelectDialogStage;
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
import system.CacheManager;
import system.model.Application;
import system.model.ApplicationManager;
import utils.OSValidator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SettingsStage extends Stage {
    private ApplicationManager applicationManager;
    private OnSettingsCloseListener onSettingsCloseListener;

    private SettingsController controller;

    public SettingsStage(ApplicationManager applicationManager, OnSettingsCloseListener onSettingsCloseListener) throws IOException {
        this.applicationManager = applicationManager;
        this.onSettingsCloseListener = onSettingsCloseListener;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/settings_dialog.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(AppSelectDialogStage.class.getResource("/css/applistcell.css").toExternalForm());
        this.setTitle("Settings");
        this.setResizable(false);
        this.setScene(scene);
        this.getIcons().add(new Image(SettingsStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (SettingsController) fxmlLoader.getController();

        // Add application button
        controller.addApplicationBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select external application...");
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
                ApplicationListCell applicationListCell = new ApplicationListCell();
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

        // CLEAR CACHE
        controller.clearCacheBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                CacheManager cacheManager = CacheManager.getInstance();

                boolean result = cacheManager.clearCache();
                if (result) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Cache Deleted!");
                    alert.setHeaderText("Cache have been successfully deleted, now the app will close...");

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

        // Load the external applications
        loadExternalApplications();
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
