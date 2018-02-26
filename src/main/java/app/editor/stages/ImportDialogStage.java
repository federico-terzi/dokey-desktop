package app.editor.stages;

import app.editor.controllers.ImportDialogController;
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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import section.model.Item;
import section.model.Section;
import section.model.SectionType;
import system.ResourceUtils;
import system.model.Application;
import system.model.ApplicationManager;
import system.section.SectionInfoResolver;
import system.section.importer.SectionImporter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class ImportDialogStage extends Stage {
    private final ImportDialogController controller;
    private File importedFile;
    private OnImportEventListener listener;
    private ApplicationManager applicationManager;

    private SectionImporter sectionImporter;
    private SectionInfoResolver sectionInfoResolver;

    private Application applicationTarget = null;  // If this is different than null, the section will be loaded for that app.

    public ImportDialogStage(File importedFile, OnImportEventListener listener, ApplicationManager applicationManager) throws IOException {
        this.importedFile = importedFile;
        this.listener = listener;
        this.applicationManager = applicationManager;
        sectionInfoResolver = new SectionInfoResolver(applicationManager);

        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/import_dialog.fxml").toURI().toURL());
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        this.setTitle("Import Layout");
        this.setScene(scene);
        this.getIcons().add(new Image(ImportDialogStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (ImportDialogController) fxmlLoader.getController();

        // Create the section importer
        sectionImporter = new SectionImporter(importedFile, applicationManager);

        // Set the event listeners
        controller.cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                close();
            }
        });
        controller.importBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                finishImport();
            }
        });
        controller.changeTargetBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                changeTarget();
            }
        });
        controller.avoidInvalidItemsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
                sectionImporter.setDeleteInvalidItems(newValue)
        );
        controller.compatibilityModeCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
                sectionImporter.setCompatibilityMode(newValue)
        );

        controller.progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        // Start the analysis in another thread
        startAnalysis();
    }

    public interface OnImportEventListener {
        void onImportCompleted(Section section);
    }

    /**
     * Start the importing analysis in another thread.
     */
    private void startAnalysis() {
        Task analysisTask = new Task() {
            @Override
            protected Object call() throws Exception {
                sectionImporter.analyze();

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        renderInterface();

                        // If the section was created with another OS, warn the user about possible problems.
                        if (sectionImporter.isCreatedWithAnotherOS()) {
                            displayIncompatibleOSWarning();
                        }
                    }
                });
                return null;
            }
        };
        new Thread(analysisTask).start();
    }

    /**
     * Render the analysis result in the interface.
     */
    private void renderInterface() {
        // Hide the progress bar
        controller.progressBar.setVisible(false);
        controller.progressBar.setManaged(false);

        // Show the panels
        controller.targetPanel.setManaged(true);
        controller.optionsPanel.setManaged(true);

        // Render the target panel
        renderTargetPanel();

        // Show the invalid items tab if there are invalidItems
        if (sectionImporter.getInvalidItems().size() > 0) {
            controller.invalidItemsPanel.setManaged(true);

            // Populate the list view
            ObservableList<Item> invalidItems = FXCollections.observableArrayList(sectionImporter.getInvalidItems());
            controller.invalidItemsListView.setItems(invalidItems);
        }

        // Check compatibility mode checkbox if suggested by the importer
        if (sectionImporter.shouldRequireCompatibilityMode()) {
            controller.compatibilityModeCheckBox.setSelected(true);
        }

        validateImport();

        // Resize the scene to fit content
        this.sizeToScene();
    }

    /**
     * Check that the section can be imported.
     * For example, if has type SHORTCUTS, make sure that a target app is specified.
     */
    private void validateImport() {
        boolean canImport = true;

        if (sectionImporter.getSection().getSectionType() == SectionType.SHORTCUTS) {
            // If no application target is found, disable the import.
            if (!sectionImporter.hasTargetBeenFound() && applicationTarget == null) {
                canImport = false;
            }
        }

        controller.importBtn.setDisable(!canImport);
    }

    /**
     * Display the target info.
     */
    private void renderTargetPanel() {
        // Get the section info and render them
        SectionInfoResolver.SectionInfo sectionInfo = sectionInfoResolver.getSectionInfo(sectionImporter.getSection(), 64, applicationTarget);
        if (sectionInfo != null && sectionInfo.name != null) {
            controller.targetImageView.setImage(sectionInfo.image);
            controller.sectionTitleLabel.setText(sectionInfo.name);
            controller.sectionDescriptionLabel.setText(sectionInfo.description);
        }else{
            Image notFoundImage = new Image(SectionInfoResolver.class.getResourceAsStream("/assets/help.png"), 64, 64, true, true);
            controller.targetImageView.setImage(notFoundImage);
            controller.sectionTitleLabel.setText("Not Found");
            controller.sectionDescriptionLabel.setText("Can't find the target application, please select it manually.");
        }

        // If the section has type SHORTCUTS enable the change target button
        // That is because a user cannot change a section target if is a launcher or system.
        if (sectionImporter.getSection().getSectionType() == SectionType.SHORTCUTS) {
            controller.changeTargetBtn.setDisable(false);
        }
    }

    /**
     * Display a dialog to warn the user about possible problems in the importing
     * due to the section created with another OS.
     */
    private void displayIncompatibleOSWarning() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("OS Incompatibility Detected");
        alert.setHeaderText("This layout was created with another Operating System.");
        alert.setContentText("You can still import it with the Compatibility Mode, " +
                "but it may not work as expected.\nDo you want to proceed anyway?");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(ShortcutDialogStage.class.getResourceAsStream("/assets/icon.png")));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK) {  // Close the dialog
            close();
        }
    }

    /**
     * Prompt a dialog to select a new application target
     */
    private void changeTarget() {
        try {
            AppSelectDialogStage appSelectDialogStage = new AppSelectDialogStage(applicationManager,
                new AppSelectDialogStage.OnApplicationListener() {
                    @Override
                    public void onApplicationSelected(Application application) {
                        applicationTarget = application;
                        sectionImporter.setOverrideRelatedAppID(applicationTarget.getExecutablePath());

                        renderTargetPanel();
                        validateImport();
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

    /**
     * Save the imported file and notify the listener
     */
    private void finishImport() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sectionImporter.importSection();

                if (listener != null) {
                    listener.onImportCompleted(sectionImporter.getSection());
                }

                Platform.runLater(() -> close());
            }
        }).start();
    }
}
