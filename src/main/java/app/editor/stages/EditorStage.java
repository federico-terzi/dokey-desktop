package app.editor.stages;

import app.editor.components.BottomBarGrid;
import app.editor.components.PageGrid;
import app.editor.controllers.EditorController;
import app.editor.listcells.SectionListCell;
import app.stages.AppListStage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Callback;
import section.model.Section;
import system.SectionManager;
import system.model.Application;
import system.model.ApplicationManager;

import java.io.IOException;
import java.util.List;

public class EditorStage extends Stage {
    public static final int PAGE_HEIGHT = 400;
    public static final int PAGE_WIDTH = 320;
    private static final int BOTTOM_BAR_DEFAULT_COLS = 4;
    private static final int BOTTOM_BAR_WIDTH = PAGE_WIDTH;
    private static final int BOTTOM_BAR_HEIGHT  = 100;

    private EditorController controller;
    private ApplicationManager applicationManager;

    private SectionManager sectionManager;

    private List<Section> sections;

    public EditorStage(ApplicationManager applicationManager) throws IOException {
        this.applicationManager = applicationManager;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/section_editor.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(AppListStage.class.getResource("/css/sectionlistcell.css").toExternalForm());
        scene.getStylesheets().add(AppListStage.class.getResource("/css/editor.css").toExternalForm());
        this.setTitle("Editor");
        this.setScene(scene);
        this.getIcons().add(new Image(EditorStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (EditorController) fxmlLoader.getController();

        // Create the section manager
        sectionManager = new SectionManager();

        // Bind the action listeners
        controller.getAddSectionBtn().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addSection();
            }
        });

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
        controller.getSectionsListView().setCellFactory(new Callback<ListView<Section>, ListCell<Section>>() {
            @Override
            public ListCell<Section> call(ListView<Section> param) {
                return new SectionListCell(applicationManager);
            }
        });
        controller.getSectionsListView().setItems(sections);

        // Load the first section
        loadSection(sections.get(0));
    }

    private void loadSection(Section section) {
        // Add the pages        
        PageGrid pageGrid = new PageGrid(applicationManager, section.getPages().get(0));
        pageGrid.setHeight(PAGE_HEIGHT);
        pageGrid.setWidth(PAGE_WIDTH);
        
        controller.getContentBox().getChildren().add(pageGrid);
        
        // Add the bottom bar
        BottomBarGrid bottomBarGrid = new BottomBarGrid(applicationManager, section.getBottomBarItems(), BOTTOM_BAR_DEFAULT_COLS);
        bottomBarGrid.setWidth(BOTTOM_BAR_WIDTH);
        bottomBarGrid.setHeight(BOTTOM_BAR_HEIGHT);

        controller.getContentBox().getChildren().add(bottomBarGrid);
    }

    private void addSection() {
        try {
            AppListStage appListStage = new AppListStage(applicationManager, new AppListStage.OnApplicationListener() {
                @Override
                public void onApplicationSelected(Application application) {
                    requestSectionForApplication(application);
                }

                @Override
                public void onCanceled() {

                }
            });
            appListStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestSectionForApplication(Application application) {
        //TODO
    }

    public EditorController getController() {
        return controller;
    }
}
