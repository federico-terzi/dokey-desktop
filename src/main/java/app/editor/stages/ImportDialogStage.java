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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import section.model.Item;
import section.model.SectionType;
import system.ResourceUtils;
import system.model.Application;
import system.model.ApplicationManager;
import system.section.SectionInfoResolver;
import system.section.importer.SectionImporter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class ImportDialogStage extends Stage {
    private final ImportDialogController controller;
    private File importedFile;
    private ApplicationManager applicationManager;

    private SectionImporter sectionImporter;
    private SectionInfoResolver sectionInfoResolver;

    private Application applicationTarget = null;  // If this is different than null, the section will be loaded for that app.

    public ImportDialogStage(File importedFile, ApplicationManager applicationManager) throws IOException {
        this.importedFile = importedFile;
        this.applicationManager = applicationManager;
        sectionInfoResolver = new SectionInfoResolver(applicationManager);

        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/import_dialog.fxml").toURI().toURL());
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        this.setTitle("Import Layout");
        this.setScene(scene);
        this.getIcons().add(new Image(ImportDialogStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (ImportDialogController) fxmlLoader.getController();

        // Set the event listeners
        controller.cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                close();
            }
        });

        controller.progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        // Create the section importer and start the analysis in another thread
        sectionImporter = new SectionImporter(importedFile, applicationManager);
        startAnalysis();
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
            if (sectionImporter.getOverrideRelatedAppID() == null && applicationTarget == null) {
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
        if (sectionInfo != null) {
            controller.targetImageView.setImage(sectionInfo.image);
            controller.sectionTitleLabel.setText(sectionInfo.name);
            controller.sectionDescriptionLabel.setText(sectionInfo.description);
        }

        // If the section has type SHORTCUTS enable the change target button
        // That is because a user cannot change a section target if is a launcher or system.
        if (sectionImporter.getSection().getSectionType() == SectionType.SHORTCUTS) {
            controller.changeTargetBtn.setDisable(false);
        }
    }
}
