package app.stages;

import app.UIControllers.AppListController;
import app.UIControllers.EditorController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import section.model.Section;
import system.SectionManager;
import system.model.Application;
import system.model.ApplicationManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class EditorStage extends Stage {
    private EditorController controller;
    private ApplicationManager applicationManager;

    private SectionManager sectionManager;

    private List<Section> sections;

    public EditorStage(ApplicationManager applicationManager) throws IOException {
        this.applicationManager = applicationManager;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/section_editor.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        this.setTitle("Editor");
        this.setScene(scene);
        this.getIcons().add(new Image(EditorStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (EditorController) fxmlLoader.getController();

        // Create the section manager
        sectionManager = new SectionManager();

        requestSectionList();
    }

    private void requestSectionList() {
        Task sectionTask = new Task() {
            @Override
            protected Object call() throws Exception {
                // Get all the sections
                sections = sectionManager.getSections();

                // Populate the listview
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        populateSectionListView();
                    }
                });
                return null;
            }
        };

        new Thread(sectionTask).start();
    }

    private void populateSectionListView() {
        ObservableList<Section> sections = FXCollections.observableArrayList(this.sections);
        controller.getSectionsListView().setItems(sections);
    }

    public EditorController getController() {
        return controller;
    }
}
