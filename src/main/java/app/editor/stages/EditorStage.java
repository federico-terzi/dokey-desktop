package app.editor.stages;

import app.editor.components.ComponentGrid;
import app.editor.listeners.OnComponentClickListener;
import app.editor.listeners.OnSectionModifiedListener;
import app.editor.comparators.SectionComparator;
import app.editor.components.BottomBarGrid;
import app.editor.components.EmptyButton;
import app.editor.components.PageGrid;
import app.editor.controllers.EditorController;
import app.editor.listcells.SectionListCell;
import app.editor.model.ScreenOrientation;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Pair;
import section.model.Component;
import section.model.Page;
import section.model.Section;
import section.model.SectionType;
import system.BroadcastManager;
import system.ResourceUtils;
import system.SectionManager;
import system.model.Application;
import system.model.ApplicationManager;
import system.sicons.ShortcutIconManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EditorStage extends Stage implements OnSectionModifiedListener {
    public static final int PORTRAIT_HEIGHT = 400;
    public static final int PORTRAIT_WIDTH = 320;
    public static final int PORTRAIT_BOTTOM_BAR_HEIGHT = 100;
    public static final int LANDSCAPE_HEIGHT = 320;
    public static final int LANDSCAPE_WIDTH = 450;
    public static final int LANDSCAPE_BOTTOM_BAR_WIDTH = 100;
    public static final int BORDER_PADDING = 10;
    private static final int BOTTOM_BAR_DEFAULT_COLS = 4;


    private EditorController controller;
    private ApplicationManager applicationManager;
    private SectionManager sectionManager;
    private ShortcutIconManager shortcutIconManager;
    private OnEditorEventListener onEditorEventListener;

    private List<Section> sections;
    private String sectionQuery = null;

    private List<ComponentGrid> grids;

    private Section activeSection = null;
    private Page activePage = null;
    private ScreenOrientation screenOrientation = ScreenOrientation.PORTRAIT;

    public EditorStage(ApplicationManager applicationManager, OnEditorEventListener onEditorEventListener) throws IOException {
        this.applicationManager = applicationManager;
        this.onEditorEventListener = onEditorEventListener;

        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/section_editor.fxml").toURI().toURL());
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ResourceUtils.getResource("/css/sectionlistcell.css").toURI().toString());
        scene.getStylesheets().add(ResourceUtils.getResource("/css/editor.css").toURI().toString());
        this.setTitle("Dokey Editor");
        this.setScene(scene);
        this.getIcons().add(new Image(EditorStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (EditorController) fxmlLoader.getController();

        // Create the section manager
        sectionManager = new SectionManager();

        // Create the shortcut icon manager
        shortcutIconManager = new ShortcutIconManager();

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
                if (onEditorEventListener != null) {
                    onEditorEventListener.onEditorClosed();
                }
            }
        });

        // Toolbar buttons listeners
        controller.changeSizeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (activePage != null && activeSection != null) {
                    requestChangePageSize(activePage, activeSection);
                }
            }
        });
        controller.rotateViewBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Invert the screen orientation
                if (screenOrientation == ScreenOrientation.LANDSCAPE) {
                    screenOrientation = ScreenOrientation.PORTRAIT;
                }else{
                    screenOrientation = ScreenOrientation.LANDSCAPE;
                }

                // Make sure an active section exists
                if (activeSection != null) {
                    loadSection(activeSection);
                }
            }
        });
        controller.exportBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (activeSection != null) {
                    exportSection(activeSection);
                }
            }
        });
        controller.addApplicationBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addSection();
            }
        });
        controller.searchBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (controller.searchSectionTextField.isManaged()) {
                    sectionQuery = null;
                    controller.searchSectionTextField.setText(null);
                    populateSectionListView();
                    controller.searchSectionTextField.setManaged(false);
                }else{
                    controller.searchSectionTextField.setManaged(true);
                }
            }
        });

        // Listener for the search query
        controller.searchSectionTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            sectionQuery = newValue;
            populateSectionListView();
        });

        // Listener to reset all the drop selections
        controller.getContentBox().setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                grids.forEach(grid -> grid.resetDragSelection());
            }
        });


        requestSectionList();
    }

    public interface OnEditorEventListener {
        void onEditorClosed();
    }

    private void requestSectionList() {
        // Reset the list view
        controller.getSectionsListView().getItems().clear();

        Task sectionTask = new Task() {
            @Override
            protected Object call() throws Exception {
                // Get all the sections
                sections = sectionManager.getSections();

                // Filter out the sections without an associated application
                sections = sections.stream().filter(section -> {
                    return section.getSectionType() == SectionType.LAUNCHPAD ||
                            section.getSectionType() == SectionType.SYSTEM ||
                    applicationManager.getApplication(section.getRelatedAppId()) != null;
                }).collect(Collectors.toList());

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
        List<Section> input = this.sections;

        // If there is a search query, filter the results
        if (sectionQuery != null) {
            input = input.stream().filter(section -> {
                if (section.getSectionType()== SectionType.LAUNCHPAD) {
                    return true;
                }else if (section.getRelatedAppId() == null) {
                    return false;
                }

                Application app = applicationManager.getApplication(section.getRelatedAppId());
                if (app == null) {
                    return false;
                }
                return app.getName().toLowerCase().contains(sectionQuery.toLowerCase());
            }).collect(Collectors.toList());
        }

        ObservableList<Section> sections = FXCollections.observableArrayList(input);
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
                        exportSection(section);
                    }
                });
            }
        });
        controller.getSectionsListView().setItems(sections);

        // If no active section is specified, load the first section
        if (activeSection == null) {
            loadSection(sections.get(0));
        } else {   // Load the active section
            // Calculate if the activeSection is present in the new list
            Optional<Section> newActiveSection = sections.stream().filter(section -> section.getRelatedAppId() != null && section.getRelatedAppId().equals(activeSection.getRelatedAppId())).
                    findFirst();

            if (newActiveSection.isPresent()) {
                loadSection(activeSection);
                controller.getSectionsListView().getSelectionModel().select(newActiveSection.get());
            } else {
                loadSection(sections.get(0));  // Load the first
            }
        }

    }

    private void loadSection(Section section) {
        // Clear the previous grids
        grids = new ArrayList<>();

        // Clear the previous section
        controller.getContentBox().getChildren().clear();

        // Create the tabpane for the pages and set it up
        TabPane tabPane = new TabPane();
        tabPane.setMinWidth(getWidth(screenOrientation));
        tabPane.setPrefWidth(getWidth(screenOrientation));
        tabPane.setMaxWidth(getWidth(screenOrientation));
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Add the pages
        for (Page page : section.getPages()) {
            PageGrid pageGrid = new PageGrid(applicationManager, shortcutIconManager, page, section, screenOrientation);
            pageGrid.setSectionModifiedListener(this);
            pageGrid.setSectionType(section.getSectionType());
            pageGrid.setShortcutIconManager(shortcutIconManager);
            pageGrid.setOnComponentClickListener(new OnComponentClickListener() {
                @Override
                public void onComponentClicked(Component component) {

                }
            });
            grids.add(pageGrid);

            Tab tab = new Tab();
            Label tabTitle = new Label(page.getTitle());
            tab.setGraphic(tabTitle);
            tab.setContent(pageGrid);

            // Add the tab context menu
            final ContextMenu contextMenu = new ContextMenu();
            MenuItem rename = new MenuItem("Rename...");
            rename.setStyle("-fx-text-fill: black;");
            rename.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    TextInputDialog dialog = new TextInputDialog(page.getTitle());
                    Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(new Image(ShortcutDialogStage.class.getResourceAsStream("/assets/icon.png")));
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
                        onSectionModified(section);

                        // Reload the section
                        loadSection(section);
                    });
                }
            });
            MenuItem changeSize = new MenuItem("Change Grid Size...");
            changeSize.setStyle("-fx-text-fill: black;");
            changeSize.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    requestChangePageSize(page, section);
                }
            });
            MenuItem moveLeft = new MenuItem("Move Left");
            moveLeft.setStyle("-fx-text-fill: black;");
            moveLeft.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    int index = section.getPages().indexOf(page);
                    if (index > 0) {
                        Collections.swap(section.getPages(), index, index-1);

                        // Save the section
                        onSectionModified(section);

                        // Reload the section
                        loadSection(section);
                    }
                }
            });
            MenuItem moveRight = new MenuItem("Move Right");
            moveRight.setStyle("-fx-text-fill: black;");
            moveRight.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    int index = section.getPages().indexOf(page);
                    if (index >= 0 && index < (section.getPages().size()-1)) {
                        Collections.swap(section.getPages(), index, index+1);

                        // Save the section
                        onSectionModified(section);

                        // Reload the section
                        loadSection(section);
                    }
                }
            });
            MenuItem delete = new MenuItem("Delete");
            delete.setStyle("-fx-text-fill: black;");
            delete.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(new Image(ShortcutDialogStage.class.getResourceAsStream("/assets/icon.png")));
                    alert.setTitle("Delete Confirmation");
                    alert.setHeaderText("Do you really want to delete the page?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        section.getPages().remove(page);

                        // Save the section
                        onSectionModified(section);

                        // Reload the section
                        loadSection(section);
                    }
                }
            });
            contextMenu.getItems().addAll(rename, changeSize, new SeparatorMenuItem(), moveLeft, moveRight, new SeparatorMenuItem(), delete);
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
        Image image = new Image(EmptyButton.class.getResourceAsStream("/assets/add_white.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(16);
        imageView.setFitWidth(16);
        imageView.setSmooth(true);
        VBox imageVBox = new VBox();
        imageVBox.getChildren().add(imageView);
        addTab.setGraphic(imageVBox);
        addTab.setText("Add");
        tabPane.getTabs().add(addTab);
        // Add the "Add Page" event listener to create a new page
        imageVBox.setOnMouseClicked(event -> {
            addPageToSection(section);
        });
        // Change tab listener
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if (newValue.equals(addTab)) {
                    addPageToSection(section);
                }

                // Change the active page
                activePage = section.getPages().get(tabPane.getSelectionModel().getSelectedIndex());
            }
        });

        // Add the bottom bar
        BottomBarGrid bottomBarGrid = new BottomBarGrid(applicationManager, shortcutIconManager, BOTTOM_BAR_DEFAULT_COLS, section, screenOrientation);
        bottomBarGrid.setSectionModifiedListener(this);
        bottomBarGrid.setSectionType(section.getSectionType());
        bottomBarGrid.setOnComponentClickListener(new OnComponentClickListener() {
            @Override
            public void onComponentClicked(Component component) {

            }
        });
        grids.add(bottomBarGrid);


        // Add the elements based on the orientation
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            VBox box = new VBox();
            box.setAlignment(Pos.CENTER);
            // Add the elements
            box.getChildren().add(tabPane);

            Pane separator = new Pane();
            separator.setStyle("-fx-background-color: transparent; -fx-pref-height: 10px");
            box.getChildren().add(separator);

            box.getChildren().add(bottomBarGrid);
            box.getStyleClass().add("vertical-container");
            controller.getContentBox().getChildren().add(box);
        }else{
            HBox box = new HBox();
            box.setAlignment(Pos.BOTTOM_CENTER);
            // Add the elements
            box.getChildren().add(bottomBarGrid);

            Pane separator = new Pane();
            separator.setStyle("-fx-background-color: transparent; -fx-pref-width: 10px");
            box.getChildren().add(separator);

            box.getChildren().add(tabPane);
            controller.getContentBox().getChildren().add(box);
        }

        // Select the list view entry
        controller.getSectionsListView().getSelectionModel().select(section);

        activeSection = section;

        // If the active page is contained in the current section, select the tab
        if (section.getPages().contains(activePage)) {
            int tabIndex = section.getPages().indexOf(activePage);
            tabPane.getSelectionModel().select(tabIndex);
        } else {  // Read the currently active page
            activePage = section.getPages().get(tabPane.getSelectionModel().getSelectedIndex());
        }
    }

    private void requestChangePageSize(Page page, Section section) {
        // Ask for the new size based on the orientation
        int res[] = null;
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            res = showPageDialog(page.getRowCount(), page.getColCount());
        }else{
            res = showPageDialog(page.getColCount(), page.getRowCount());
        }

        // If the size is valid, change it
        if (res != null) {
            int cols, rows;
            // Modify the page based on the orientation
            if (screenOrientation == ScreenOrientation.PORTRAIT) {
                rows = res[0];
                cols = res[1];
            }else{
                rows = res[1];
                cols = res[0];
            }

            // Make sure that after resizing the page
            // no elements are outside the new size.
            List<Component> toBeDeleted = new ArrayList<>();
            for (Component component : page.getComponents()) {
                if (component.getX() >= rows ||
                        component.getY() >= cols) {
                    toBeDeleted.add(component);
                }
            }
            // If there are elements that will be deleted
            // ask for the confirmation
            if (toBeDeleted.size() > 0) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Confirmation");
                alert.setHeaderText("Some buttons will be deleted if you resize the page, are you sure?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() != ButtonType.OK) {
                    return;
                }

                // Delete the elements
                page.getComponents().removeAll(toBeDeleted);
            }

            page.setRowCount(rows);
            page.setColCount(cols);

            // Save the section
            onSectionModified(section);

            // Reload the section
            loadSection(section);
        }
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
        onSectionModified(section);

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

    private void exportSection(Section section) {
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

    private void requestSectionForApplication(Application application) {
        // Create the section
        sectionManager.getShortcutSection(application.getExecutablePath());

        // Refresh the list
        requestSectionList();
    }

    private int[] showPageDialog(int rows, int cols) {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(ShortcutDialogStage.class.getResourceAsStream("/assets/icon.png")));
        dialog.setTitle("Change Grid Size");
        dialog.setHeaderText("Specify the grid size below.\nThe rows and the columns must be a number greater than 0");

        // Set the button types.
        ButtonType changeSizeType = new ButtonType("Change Size", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeSizeType, ButtonType.CANCEL);

        // Create the rows and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Node changeSizeBtn = dialog.getDialogPane().lookupButton(changeSizeType);

        TextField rowField = new TextField();
        rowField.setText(String.valueOf(rows));
        TextField colField = new TextField();
        colField.setText(String.valueOf(cols));

        // Text validator
        ChangeListener<String> validator = ((observable, oldValue, newValue) ->
        {
            try {
                int row = Integer.parseInt(rowField.getText());
                int col = Integer.parseInt(colField.getText());
                if (row > 0 && col > 0) {
                    changeSizeBtn.setDisable(false);
                }else{
                    changeSizeBtn.setDisable(true);
                }
            } catch (NumberFormatException e) {
                changeSizeBtn.setDisable(true);
            }
        });
        rowField.textProperty().addListener(validator);
        colField.textProperty().addListener(validator);


        grid.add(new Label("Rows:"), 0, 0);
        grid.add(rowField, 1, 0);
        grid.add(new Label("Columns:"), 0, 1);
        grid.add(colField, 1, 1);


        dialog.getDialogPane().setContent(grid);

        // Request focus on the rows field by default.
        Platform.runLater(() -> rowField.requestFocus());

        // Convert the result to a rows-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeSizeType) {
                return new Pair<String, String>(rowField.getText(), colField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        if (result.isPresent()) {
            int out[] = new int[2];
            try {
                out[0] = Integer.parseInt(result.get().getKey());
                out[1] = Integer.parseInt(result.get().getValue());
                if (out[0] > 0 && out[1] > 0) {
                    return out;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Errore di input
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("The size must be a valid number!");
            alert.setContentText("The rows and the columns must be a number greater than 0.");
            alert.show();
        }
        return null;
    }

    public EditorController getController() {
        return controller;
    }

    @Override
    public void onSectionModified(Section section) {
        Task saveTask = new Task() {
            @Override
            protected Object call() throws Exception {
                // Save the section
                sectionManager.saveSection(section);

                // Notify the modified section
                String sectionJson = section.json().toString();
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.EDITOR_MODIFIED_SECTION_EVENT, sectionJson);

                return null;
            }
        };
        new Thread(saveTask).start();
    }

    public static int getWidth(ScreenOrientation screenOrientation) {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return PORTRAIT_WIDTH;
        }else{
            return LANDSCAPE_WIDTH;
        }
    }

    public static int getHeight(ScreenOrientation screenOrientation) {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return PORTRAIT_HEIGHT;
        }else{
            return LANDSCAPE_HEIGHT;
        }
    }
}
