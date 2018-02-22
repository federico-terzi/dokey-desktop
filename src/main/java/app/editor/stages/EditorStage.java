package app.editor.stages;

import app.editor.animations.DividerTransition;
import app.editor.components.*;
import app.editor.listeners.OnSectionModifiedListener;
import app.editor.comparators.SectionComparator;
import app.editor.controllers.EditorController;
import app.editor.listcells.SectionListCell;
import app.editor.model.ScreenOrientation;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Pair;
import json.JSONObject;
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
import app.editor.components.SectionGridController.SectionAnimationType;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EditorStage extends Stage implements OnSectionModifiedListener {
    public static final double SECTION_LIST_VIEW_OPEN_POSITION = 0.3;

    // Limits in value
    private static final int MAX_ROWS = 6;
    private static final int MAX_COLS = 5;
    private static final int MAX_PAGES = 8;

    private EditorController controller;
    private ApplicationManager applicationManager;
    private SectionManager sectionManager;
    private ShortcutIconManager shortcutIconManager;
    private OnEditorEventListener onEditorEventListener;

    private List<Section> sections;
    private String sectionQuery = null;

    private Section activeSection = null;
    private ScreenOrientation screenOrientation = ScreenOrientation.PORTRAIT;
    private SectionGridController sectionGridController = null;

    private boolean areAppsShown = true;  // If true, the lateral list view is shown.

    // Used to debaunce modify network requests.
    private PublishSubject<Section> modifySectionPublisher = PublishSubject.create();

    public EditorStage(ApplicationManager applicationManager, OnEditorEventListener onEditorEventListener) throws IOException {
        this.applicationManager = applicationManager;
        this.onEditorEventListener = onEditorEventListener;

        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/section_editor.fxml").toURI().toURL());
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ResourceUtils.getResource("/css/sectionlistcell.css").toURI().toString());
        scene.getStylesheets().add(ResourceUtils.getResource("/css/main.css").toURI().toString());
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

        // Action listener for the close window event
        setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (onEditorEventListener != null) {
                    onEditorEventListener.onEditorClosed();

                    // Unregister broadcast listeners
                    BroadcastManager.getInstance().unregisterBroadcastListener(BroadcastManager.PHONE_MODIFIED_SECTION_EVENT, phoneSectionModifiedListener);
                    BroadcastManager.getInstance().unregisterBroadcastListener(BroadcastManager.OPEN_SHORTCUT_PAGE_FOR_APPLICATION_EVENT, openShortcutPageForAppListener);
                }
            }
        });

        // Toolbar buttons listeners
        controller.changeSizeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (activeSection != null && sectionGridController != null && sectionGridController.getActivePage() != null) {
                    requestChangePageSize(sectionGridController.getActivePage(), activeSection);
                    sectionGridController.invalidate();
                }
            }
        });
        controller.rotateViewBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Invert the screen orientation
                if (screenOrientation == ScreenOrientation.LANDSCAPE) {
                    screenOrientation = ScreenOrientation.PORTRAIT;
                } else {
                    screenOrientation = ScreenOrientation.LANDSCAPE;
                }

                // Make sure an active section exists
                if (sectionGridController != null) {
                    sectionGridController.setScreenOrientation(screenOrientation);
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
        controller.importBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

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
                } else {
                    controller.searchSectionTextField.setManaged(true);
                }
            }
        });
        controller.toggleAppsBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (areAppsShown) {
                    DividerTransition transition = new DividerTransition(controller.splitPane, 0);
                    transition.play();
                    areAppsShown = false;
                } else {
                    DividerTransition transition = new DividerTransition(controller.splitPane, SECTION_LIST_VIEW_OPEN_POSITION);
                    transition.play();
                    areAppsShown = true;
                }

                renderToggleAppsListView();
            }
        });

        // Keyboard events
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                System.out.println(event);
            }
        });

        // Listener for the search query
        controller.searchSectionTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            sectionQuery = newValue;
            populateSectionListView();
        });

        requestSectionList();

        renderToggleAppsListView();

        // Register broadcast listeners
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.PHONE_MODIFIED_SECTION_EVENT, phoneSectionModifiedListener);
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.OPEN_SHORTCUT_PAGE_FOR_APPLICATION_EVENT, openShortcutPageForAppListener);

        // Set up the Observer that will notify a section change to the device
        setupNotifySectionModifiedSubscription();
    }

    public interface OnEditorEventListener {
        void onEditorClosed();
    }

    /**
     * Request asynchronously the section list.
     *
     * @param targetSectionID The target SectionID that will be selected in the list
     */
    private void requestSectionList(String targetSectionID) {
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
                        // Fill the list view
                        populateSectionListView();

                        // Select the list view item if present
                        if (targetSectionID != null) {
                            // Select the correct entry in the list view
                            for (Section sec : controller.getSectionsListView().getItems()) {
                                if (sec.getStringID() != null && sec.getStringID().equals(targetSectionID)) {
                                    controller.getSectionsListView().getSelectionModel().select(sec);
                                }
                            }
                        }

                    }
                });
                return null;
            }
        };

        new Thread(sectionTask).start();
    }

    /**
     * Request asynchronously the section list.
     */
    private void requestSectionList() {
        requestSectionList(null);
    }

    /**
     * Fill the section list view.
     */
    private void populateSectionListView() {
        List<Section> input = this.sections;

        // If there is a search query, filter the results
        if (sectionQuery != null) {
            input = input.stream().filter(section -> {
                if (section.getSectionType() == SectionType.LAUNCHPAD) {
                    return true;
                } else if (section.getRelatedAppId() == null) {
                    return false;
                }

                Application app = applicationManager.getApplication(section.getRelatedAppId());
                if (app == null) {
                    return false;
                }
                return app.getName().toLowerCase().contains(sectionQuery.toLowerCase());
            }).collect(Collectors.toList());
        }

        // Create custom list cells
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

    /**
     * Handle the list view show/hide mechanism
     */
    private void renderToggleAppsListView() {
        Image image = null;
        String message = null;
        if (!areAppsShown) {
            image = new Image(EditorStage.class.getResourceAsStream("/assets/toolbar_icons/menu.png"), 20, 20, true, true);
            message = "Show Applications";
        } else {
            message = "Hide Applications";
            image = new Image(EditorStage.class.getResourceAsStream("/assets/toolbar_icons/back.png"), 20, 20, true, true);
        }
        ImageView buttonImageView = new ImageView(image);
        buttonImageView.setSmooth(true);
        Tooltip tooltip = new Tooltip(message);
        controller.toggleAppsBtn.setGraphic(buttonImageView);
        controller.toggleAppsBtn.setTooltip(tooltip);
    }

    /**
     * Display the given section in the editor.
     *  @param section       the Section to load.
     *
     */
    private void loadSection(Section section) {
        // Select the list view entry
        controller.getSectionsListView().getSelectionModel().select(section);
        activeSection = section;  // Update the active section

        sectionGridController = new SectionGridController(section, controller.getContentBox(),
                screenOrientation, applicationManager, shortcutIconManager, this,
                new SectionGridController.OnSectionGridEventListener() {
            @Override
            public void onRequestChangePageSize(Page page, Section section) {
                requestChangePageSize(page, section);
                sectionGridController.invalidate();
            }

            @Override
            public void onRequestDeletePage(Page page, Section section) {
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

                    // Re-render the grid
                    sectionGridController.invalidate();
                }
            }

            @Override
            public void onMovePageLeft(Page page, Section section) {
                int index = section.getPages().indexOf(page);
                if (index > 0) {
                    Collections.swap(section.getPages(), index, index - 1);

                    // Save the section
                    onSectionModified(section);

                    // Re-render the grid
                    sectionGridController.invalidate();
                }
            }

            @Override
            public void onMovePageRight(Page page, Section section) {
                int index = section.getPages().indexOf(page);
                if (index >= 0 && index < (section.getPages().size() - 1)) {
                    Collections.swap(section.getPages(), index, index + 1);

                    // Save the section
                    onSectionModified(section);

                    // Re-render the grid
                    sectionGridController.invalidate();
                }
            }

            @Override
            public void onRequestAddPage(Section section) {
                Page activePage = sectionGridController.getActivePage();
                addPageToSection(section);
                // Re-render the grid
                sectionGridController.setActivePage(activePage);
                sectionGridController.invalidate();
            }
        });
    }

    /**
     * Add an empty page in a section.
     *
     * @param section the section that will receive a new page.
     */
    private void addPageToSection(Section section) {
        // Make sure to not exceed the limit
        if (section.getPages().size() >= MAX_PAGES)
            return;

        // Create a new page
        Page page = new Page();
        page.setRowCount(SectionManager.DEFAULT_PAGE_ROWS);
        page.setColCount(SectionManager.DEFAULT_PAGE_COLS);
        page.setTitle("Page " + (section.getPages().size() + 1));

        // Add the page
        section.addPage(page);

        // Save the section
        onSectionModified(section);
    }

    /**
     * Create a section for the specified application, if not already present.
     *
     * @param executablePath the path of the application.
     */
    private void requestSectionForApplication(String executablePath) {
        // Create the section
        sectionManager.getShortcutSection(executablePath);

        // Refresh the list
        requestSectionList(executablePath);
    }

    /**
     * Add a dialog to select an application and create the corresponding section.
     */
    private void addSection() {
        try {
            AppSelectDialogStage appSelectDialogStage = new AppSelectDialogStage(applicationManager, new AppSelectDialogStage.OnApplicationListener() {
                @Override
                public void onApplicationSelected(Application application) {
                    requestSectionForApplication(application.getExecutablePath());
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
     * Display a file-chooser dialog and export the given section.
     *
     * @param section the Section to Export
     */
    private void exportSection(Section section) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Layout...");

        String filename = "";
        switch (section.getSectionType()) {
            case SHORTCUTS:
                // Get the application to extrapolate the name
                Application application = applicationManager.getApplication(section.getRelatedAppId());
                if (application != null) {
                    filename = application.getName().replaceAll("[^A-Za-z0-9]", ""); // Remove all non alphanumeric chars
                }
                break;
            case LAUNCHPAD:
                filename = "Launchpad";
                break;
            case SYSTEM:
                filename = "SystemCommands";
                break;
        }

        fileChooser.setInitialFileName(filename);
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

    /**
     * Request to change the side of a page.
     *
     * @param page    the page to resize.
     * @param section the section that contains the page.
     */
    private void requestChangePageSize(Page page, Section section) {
        // Ask for the new size based on the orientation
        int res[] = null;
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            res = showChangeGridSizeDialog(page.getRowCount(), page.getColCount());
        } else {
            res = showChangeGridSizeDialog(page.getColCount(), page.getRowCount());
        }

        // If the size is valid, change it
        if (res != null) {
            int cols, rows;
            // Modify the page based on the orientation
            if (screenOrientation == ScreenOrientation.PORTRAIT) {
                rows = res[0];
                cols = res[1];
            } else {
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
        }
    }

    /**
     * Show a dialog where the user can specify a new grid size.
     *
     * @param rows previous rows
     * @param cols previous cols
     * @return an int array containing the [ newRow, newCol ], or null if an error occurred.
     */
    private int[] showChangeGridSizeDialog(int rows, int cols) {
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
                if (row > 0 && col > 0 && row <= MAX_ROWS && col <= MAX_COLS) {
                    changeSizeBtn.setDisable(false);
                } else {
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

    // EVENTS

    /**
     * Called when the user modify a section in the desktop editor.
     *
     * @param section
     */
    @Override
    public void onSectionModified(Section section) {
        // Publish the section modified event
        modifySectionPublisher.onNext(section);
    }

    /**
     * Set up the Observer that will notify a section change to the device
     * with a Debaunce mechanism
     */
    private void setupNotifySectionModifiedSubscription() {
        modifySectionPublisher.debounce(200, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Section>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Section section) {
                        // Save the section
                        sectionManager.saveSection(section);

                        // Notify the modified section
                        String sectionJson = section.json().toString();
                        BroadcastManager.getInstance().sendBroadcast(BroadcastManager.EDITOR_MODIFIED_SECTION_EVENT, sectionJson);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * Called when the user modifies a section in the mobile app.
     */
    private BroadcastManager.BroadcastListener phoneSectionModifiedListener = new BroadcastManager.BroadcastListener() {
        @Override
        public void onBroadcastReceived(Serializable param) {
            Section section = Section.fromJson(new JSONObject((String) param));
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Page modifiedPage = null;

                    int index = 0;
                    // Determine which page has been modified to stay focused on that one
                    for (Section sec : controller.getSectionsListView().getItems()) {
                        if (sec.getStringID().equals(section.getStringID())) {
                            for (int i = 0; i < section.getPages().size(); i++) {
                                if (sec.getPages().size() > i && section.getPages().size() > i) {
                                    // Compare the pages based on the number of components
                                    if (sec.getPages().get(i).getComponents().size() != section.getPages().get(i).getComponents().size()) {
                                        modifiedPage = section.getPages().get(i);
                                        break;
                                    }
                                }
                            }

                            // Replace the in-memory section with the new one
                            controller.getSectionsListView().getItems().set(index, section);
                            break;
                        }

                        index++;
                    }

                    // Determine the animation type
                    SectionAnimationType animationType = SectionAnimationType.CROSSFADE;
                    if (activeSection != null && activeSection.getStringID().equals(section.getStringID())) {
                        animationType = SectionAnimationType.NONE;
                    }

                    loadSection(section);
                    if (sectionGridController != null) {
                        sectionGridController.setActivePage(modifiedPage);
                        sectionGridController.invalidate();
                    }

                    // Select the correct entry in the list view
                    for (Section sec : controller.getSectionsListView().getItems()) {
                        if (sec.getStringID().equals(section.getStringID())) {
                            controller.getSectionsListView().getSelectionModel().select(sec);
                            break;
                        }
                    }
                }
            });
        }
    };

    /**
     * Called when the user request to open the shortcut page for an app
     */
    private BroadcastManager.BroadcastListener openShortcutPageForAppListener = new BroadcastManager.BroadcastListener() {
        @Override
        public void onBroadcastReceived(Serializable param) {
            String executablePath = (String) param;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    requestSectionForApplication(executablePath);
                }
            });
        }
    };
}
