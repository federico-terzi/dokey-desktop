package app.editor.stages;

import app.editor.listeners.OnComponentClickListener;
import app.editor.listeners.OnPropertyChangedListener;
import app.editor.listeners.OnSectionModifiedListener;
import app.editor.comparators.SectionComparator;
import app.editor.components.BottomBarGrid;
import app.editor.components.EmptyButton;
import app.editor.components.PageGrid;
import app.editor.controllers.EditorController;
import app.editor.listcells.SectionListCell;
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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import section.model.Component;
import section.model.Page;
import section.model.Section;
import system.SectionManager;
import system.model.Application;
import system.model.ApplicationManager;
import system.sicons.ShortcutIconManager;

import java.io.File;
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
    private ShortcutIconManager shortcutIconManager;
    private OnEditorCloseListener onEditorCloseListener;

    private List<Section> sections;
    private Section activeSection = null;

    public EditorStage(ApplicationManager applicationManager, OnEditorCloseListener onEditorCloseListener) throws IOException {
        this.applicationManager = applicationManager;
        this.onEditorCloseListener = onEditorCloseListener;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/section_editor.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(AppSelectDialogStage.class.getResource("/css/sectionlistcell.css").toExternalForm());
        scene.getStylesheets().add(AppSelectDialogStage.class.getResource("/css/editor.css").toExternalForm());
        this.setTitle("Editor");
        this.setScene(scene);
        this.getIcons().add(new Image(EditorStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (EditorController) fxmlLoader.getController();

        // Create the section manager
        sectionManager = new SectionManager();

        // Create the shortcut icon manager
        shortcutIconManager = new ShortcutIconManager();

        // Create the main menu
        Menu fileMenu = new Menu("File");
        MenuItem closeItem = new MenuItem("Close");
        closeItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                close();
            }
        });
        fileMenu.getItems().addAll(closeItem);
        controller.menuBar.getMenus().addAll(fileMenu);


        // Create the listview context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem reloadItem = new MenuItem("Reload");
        reloadItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                requestSectionList();
            }
        });
        MenuItem addApplicationItem = new MenuItem("Add Application...");
        addApplicationItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addSection();
            }
        });
        contextMenu.getItems().addAll(reloadItem, new SeparatorMenuItem(), addApplicationItem);
        controller.getSectionsListView().setContextMenu(contextMenu);

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

        setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (onEditorCloseListener != null) {
                    onEditorCloseListener.onEditorClosed();
                }
            }
        });

        requestSectionList();
    }

    public interface OnEditorCloseListener {
        void onEditorClosed();
    }

    private void requestSectionList() {
        showStatus("Loading...");

        // Reset the list view
        controller.getSectionsListView().getItems().clear();

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
                return new SectionListCell(applicationManager, new SectionListCell.OnContextMenuListener() {
                    @Override
                    public void onDeleteSection(Section section) {
                        sectionManager.deleteSection(section);
                        requestSectionList();
                    }

                    @Override
                    public void onReloadSection(Section section) {
                        requestSectionList();
                    }

                    @Override
                    public void onExportSection(Section section) {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Export Section...");
                        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Section layout JSON (*.json)", "*.json");
                        fileChooser.getExtensionFilters().add(extFilter);
                        File destFile = fileChooser.showSaveDialog(EditorStage.this);
                        if (destFile != null) {
                            boolean res = sectionManager.writeSectionToFile(section, destFile);
                            if (!res) {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error");
                                alert.setHeaderText("There was an error saving the file");
                                alert.show();
                            }
                        }
                    }
                });
            }
        });
        controller.getSectionsListView().setItems(sections);

        // If no active section is specified, load the first section
        if (activeSection == null) {
            loadSection(sections.get(0));
        }else{   // Load the active section
            // Calculate if the activeSection is present in the new list
            Optional<Section> newActiveSection =  sections.stream().filter(section -> section.getRelatedAppId() != null && section.getRelatedAppId().equals(activeSection.getRelatedAppId())).
                    findFirst();

            if (newActiveSection.isPresent()) {
                loadSection(activeSection);
                controller.getSectionsListView().getSelectionModel().select(newActiveSection.get());
            }else{
                loadSection(sections.get(0));  // Load the first
            }
        }

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
            PageGrid pageGrid = new PageGrid(applicationManager, shortcutIconManager, page, section);
            pageGrid.setHeight(PAGE_HEIGHT);
            pageGrid.setSectionModifiedListener(this);
            pageGrid.setSectionType(section.getSectionType());
            pageGrid.setShortcutIconManager(shortcutIconManager);
            pageGrid.setOnComponentClickListener(new OnComponentClickListener() {
                @Override
                public void onComponentClicked(Component component) {

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
        VBox imageVBox = new VBox();
        imageVBox.getChildren().add(imageView);
        addTab.setGraphic(imageVBox);
        tabPane.getTabs().add(addTab);
        // Add the "Add Page" event listener to create a new page
        imageVBox.setOnMouseClicked(event -> {
            addPageToSection(section);
        });
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if (newValue.equals(addTab)) {
                    addPageToSection(section);
                }
            }
        });

        // Add the TabPane
        controller.getContentBox().getChildren().add(tabPane);


        // Add the bottom bar
        BottomBarGrid bottomBarGrid = new BottomBarGrid(applicationManager, shortcutIconManager, section.getBottomBarItems(), BOTTOM_BAR_DEFAULT_COLS, section);
        bottomBarGrid.setWidth(CONTENT_WIDTH);
        bottomBarGrid.setHeight(BOTTOM_BAR_HEIGHT);
        bottomBarGrid.setSectionModifiedListener(this);
        bottomBarGrid.setSectionType(section.getSectionType());
        bottomBarGrid.setOnComponentClickListener(new OnComponentClickListener() {
            @Override
            public void onComponentClicked(Component component) {

            }
        });
        controller.getContentBox().getChildren().add(bottomBarGrid);

        // Select the list view entry
        controller.getSectionsListView().getSelectionModel().select(section);

        activeSection = section;

        hideStatus();
    }

    private void addPageToSection(Section section) {
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

    private void addSection() {
        try {
            AppSelectDialogStage appSelectDialogStage = new AppSelectDialogStage(applicationManager, new AppSelectDialogStage.OnApplicationListener() {
                @Override
                public void onApplicationSelected(Application application) {
                    requestSectionForApplication(application);
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
                // Update the last edit
                section.setLastEdit(System.currentTimeMillis());
                // Save the section
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
