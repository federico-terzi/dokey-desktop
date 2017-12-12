package app.editor.stages;

import app.editor.listeners.OnComponentClickListener;
import app.editor.listeners.OnSectionModifiedListener;
import app.editor.comparators.SectionComparator;
import app.editor.components.BottomBarGrid;
import app.editor.components.EmptyButton;
import app.editor.components.PageGrid;
import app.editor.controllers.EditorController;
import app.editor.listcells.SectionListCell;
import app.editor.properties.Property;
import app.stages.AppListStage;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import section.model.Component;
import section.model.Page;
import section.model.Section;
import system.SectionManager;
import system.model.Application;
import system.model.ApplicationManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EditorStage extends Stage implements OnSectionModifiedListener {
    public static final int PAGE_HEIGHT = 400;
    public static final int CONTENT_WIDTH = 320;
    private static final int BOTTOM_BAR_DEFAULT_COLS = 4;
    private static final int BOTTOM_BAR_HEIGHT = 100;

    private EditorController controller;
    private ApplicationManager applicationManager;
    private SectionManager sectionManager;

    private List<Section> sections;
    private Section activeSection = null;

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
        // ADD SECTION BTN
        controller.getAddSectionBtn().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addSection();
            }
        });

        // SELECT SECTION FROM LIST VIEW
        controller.getSectionsListView().getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Section selectedSection = controller.getSectionsListView().getSelectionModel().getSelectedItem();
                if (selectedSection != null && selectedSection != activeSection) {
                    loadSection(selectedSection);
                }
            }
        });

        requestSectionList();
    }

    private void requestSectionList() {
        showStatus("Loading...");

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
        Collections.sort(sections, new SectionComparator(applicationManager));
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
        showStatus("Loading app...");

        // Clear the previous section
        controller.getContentBox().getChildren().clear();

        // Create the tabpane for the pages and set it up
        TabPane tabPane = new TabPane();
        tabPane.setMinWidth(CONTENT_WIDTH);
        tabPane.setPrefWidth(CONTENT_WIDTH);
        tabPane.setMaxWidth(CONTENT_WIDTH);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Add the pages
        for (Page page : section.getPages()) {
            PageGrid pageGrid = new PageGrid(applicationManager, page, section);
            pageGrid.setHeight(PAGE_HEIGHT);
            pageGrid.setSectionModifiedListener(this);
            pageGrid.setOnComponentClickListener(new OnComponentClickListener() {
                @Override
                public void onComponentClicked(Component component) {
                    requestProperty(component);
                }
            });

            Tab tab = new Tab();
            Label tabTitle = new Label(page.getTitle());
            tab.setGraphic(tabTitle);
            tab.setContent(pageGrid);

            // Add the tab context menu
            final ContextMenu contextMenu = new ContextMenu();
            MenuItem rename = new MenuItem("Rename...");
            rename.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    TextInputDialog dialog = new TextInputDialog(page.getTitle());
                    dialog.setTitle("Rename Page...");
                    dialog.setHeaderText("Rename the Page");
                    dialog.setContentText("Please enter the name:");

                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(s -> {
                        // Check that the string is valid
                        if (s.trim().isEmpty()) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Page name is not valid");
                            alert.setContentText("The name can't be empty!");
                            alert.show();
                            return;
                        }

                        page.setTitle(s);

                        // Save the section
                        sectionManager.saveSection(section);

                        // Reload the section
                        loadSection(section);
                    });
                }
            });
            MenuItem delete = new MenuItem("Delete");
            delete.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Delete Confirmation");
                    alert.setHeaderText("Do you really want to delete the page?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        section.getPages().remove(page);

                        // Save the section
                        sectionManager.saveSection(section);

                        // Reload the section
                        loadSection(section);
                    }
                }
            });
            contextMenu.getItems().addAll(rename, delete);
            tab.setContextMenu(contextMenu);

            // Handle the drag and drop focus switch
            tab.getGraphic().setOnDragEntered(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent event) {
                    tabPane.getSelectionModel().select(tab);
                }
            });
            tabPane.getTabs().add(tab);
        }
        // Add the "Add Page" tab
        Tab addTab = new Tab();
        Image image = new Image(EmptyButton.class.getResource("/assets/add.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(16);
        imageView.setFitWidth(16);
        imageView.setSmooth(true);
        addTab.setGraphic(imageView);
        tabPane.getTabs().add(addTab);
        // Add the "Add Page" event listener to create a new page
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if (newValue.equals(addTab)) {
                    // Create a new page
                    Page page = new Page();
                    page.setRowCount(SectionManager.DEFAULT_PAGE_ROWS);
                    page.setColCount(SectionManager.DEFAULT_PAGE_COLS);
                    page.setTitle("Page " + (section.getPages().size() + 1));

                    // Add the page
                    section.addPage(page);

                    // Save the section
                    sectionManager.saveSection(section);

                    // Reload the section
                    loadSection(section);
                }
            }
        });

        // Add the TabPane
        controller.getContentBox().getChildren().add(tabPane);


        // Add the bottom bar
        BottomBarGrid bottomBarGrid = new BottomBarGrid(applicationManager, section.getBottomBarItems(), BOTTOM_BAR_DEFAULT_COLS, section);
        bottomBarGrid.setWidth(CONTENT_WIDTH);
        bottomBarGrid.setHeight(BOTTOM_BAR_HEIGHT);
        bottomBarGrid.setSectionModifiedListener(this);
        controller.getContentBox().getChildren().add(bottomBarGrid);

        // Select the list view entry
        controller.getSectionsListView().getSelectionModel().select(section);

        activeSection = section;

        hideStatus();
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

    private void requestProperty(Component component) {
        Property property = Property.getPropertyForComponent(component);

        getController().getPropertiesContentPane().setCenter(property);
    }

    private void requestSectionForApplication(Application application) {
        // Create the section
        sectionManager.getShortcutSection(application.getExecutablePath());

        // Refresh the list
        requestSectionList();
    }

    public EditorController getController() {
        return controller;
    }

    public void hideStatus() {
        controller.getStatusLabel().setVisible(false);
        controller.getStatusProgressBar().setVisible(false);
    }

    public void showStatus(String status) {
        controller.getStatusLabel().setText(status);
        controller.getStatusLabel().setVisible(true);
        controller.getStatusProgressBar().setVisible(true);
    }

    @Override
    public void onSectionModified(Section section) {
        showStatus("Saving...");
        Task saveTask = new Task() {
            @Override
            protected Object call() throws Exception {
                sectionManager.saveSection(section);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        hideStatus();
                    }
                });
                return null;
            }
        };
        new Thread(saveTask).start();
    }
}
