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
import javafx.animation.*;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EditorStage extends Stage implements OnSectionModifiedListener {
    public static final int PORTRAIT_HEIGHT = 450;
    public static final int PORTRAIT_WIDTH = 350;
    public static final int PORTRAIT_BOTTOM_BAR_HEIGHT = 100;
    public static final int LANDSCAPE_HEIGHT = 350;
    public static final int LANDSCAPE_WIDTH = 450;
    public static final int LANDSCAPE_BOTTOM_BAR_WIDTH = 100;
    private static final int BOTTOM_BAR_DEFAULT_COLS = 4;
    public static final double SECTION_LIST_VIEW_OPEN_POSITION = 0.3;
    private static final double ENTER_SECTION_FADE_DURATION = 0.2;
    private static final double ROTATE_SECTION_DURATION = 0.2;
    private static final double BUTTON_BAR_DURATION = 0.1;

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
    private Page activePage = null;
    private ScreenOrientation screenOrientation = ScreenOrientation.PORTRAIT;
    private Node activePane = null;
    private Node activeBottomBar = null;
    private Node activeOpenBtn = null;
    private boolean isBottomBarVisible = false;

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
                    loadSection(selectedSection, SectionAnimationType.CROSSFADE);
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
                } else {
                    screenOrientation = ScreenOrientation.LANDSCAPE;
                }

                // Make sure an active section exists
                if (activeSection != null) {
                    loadSection(activeSection, SectionAnimationType.ROTATION);
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
            loadSection(sections.get(0), SectionAnimationType.NONE);
        } else {   // Load the active section
            // Calculate if the activeSection is present in the new list
            Optional<Section> newActiveSection = sections.stream().filter(section -> section.getRelatedAppId() != null && section.getRelatedAppId().equals(activeSection.getRelatedAppId())).
                    findFirst();

            if (newActiveSection.isPresent()) {
                loadSection(activeSection, SectionAnimationType.CROSSFADE);
                controller.getSectionsListView().getSelectionModel().select(newActiveSection.get());
            } else {
                loadSection(sections.get(0), SectionAnimationType.NONE);  // Load the first
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
     *
     * @param section       the Section to load.
     * @param animationType the Animation the section will display.
     */
    private void loadSection(Section section, SectionAnimationType animationType) {
        // Select the list view entry
        controller.getSectionsListView().getSelectionModel().select(section);
        activeSection = section;  // Update the active section

        // Create the tabpane for the pages and set it up
        TabPane tabPane = new TabPane();
        tabPane.setMinWidth(getWidth(screenOrientation));
        tabPane.setPrefWidth(getWidth(screenOrientation));
        tabPane.setMaxWidth(getWidth(screenOrientation));

        // This map will hold the tabPane contents for each tab.
        // used for the slide animation when changing tab
        Map<Tab, Node> tabContent = new HashMap<>();

        // Add the pages
        for (Page page : section.getPages()) {
            // Create the page grid
            PageGrid pageGrid = new PageGrid(applicationManager, shortcutIconManager, page, section, screenOrientation);
            pageGrid.setSectionModifiedListener(this);
            pageGrid.setShortcutIconManager(shortcutIconManager);

            // Create the tab and add the page grid
            Tab tab = new Tab();
            Label tabTitle = new Label(page.getTitle());
            tab.setGraphic(tabTitle);
            tab.setContent(pageGrid);
            tabContent.put(tab, pageGrid);

            // Add the tab context menu
            final ContextMenu contextMenu = new ContextMenu();
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
                        Collections.swap(section.getPages(), index, index - 1);

                        // Save the section
                        onSectionModified(section);

                        // Reload the section
                        loadSection(section, SectionAnimationType.NONE);
                    }
                }
            });
            MenuItem moveRight = new MenuItem("Move Right");
            moveRight.setStyle("-fx-text-fill: black;");
            moveRight.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    int index = section.getPages().indexOf(page);
                    if (index >= 0 && index < (section.getPages().size() - 1)) {
                        Collections.swap(section.getPages(), index, index + 1);

                        // Save the section
                        onSectionModified(section);

                        // Reload the section
                        loadSection(section, SectionAnimationType.NONE);
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
                        loadSection(section, SectionAnimationType.NONE);
                    }
                }
            });
            contextMenu.getItems().addAll(changeSize, new SeparatorMenuItem(), moveLeft, moveRight, new SeparatorMenuItem(), delete);
            tab.setContextMenu(contextMenu);

            // Add the tab
            tabPane.getTabs().add(tab);

            // Select active tab
            if (page.equals(activePage)) {
                tabPane.getSelectionModel().select(tab);
            }
        }

        // Add the bottom bar
        BottomBarGrid bottomBarGrid = new BottomBarGrid(applicationManager, shortcutIconManager, BOTTOM_BAR_DEFAULT_COLS, section, screenOrientation);
        bottomBarGrid.setSectionModifiedListener(this);

        // This listener is used by the tab pane controller to select/add tabs
        TabPaneController.OnTabListener onTabListener = new TabPaneController.OnTabListener() {
            @Override
            public void onTabSelected(int index) {
                tabPane.getSelectionModel().select(index);
            }

            @Override
            public void onAddTab() {
                activePage = section.getPages().get(tabPane.getSelectionModel().getSelectedIndex());
                addPageToSection(section);
            }
        };

        // This listener is used to control the bottom bar opening mechanism
        EventHandler openBtnClickEvent = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                isBottomBarVisible = !isBottomBarVisible;
                if (isBottomBarVisible) {
                    loadSection(section, SectionAnimationType.OPEN_BOTTOMBAR);
                }else{
                    loadSection(section, SectionAnimationType.CLOSE_BOTTOMBAR);
                }
            }
        };

        Node currentPane;  // Used in the animation
        Button openBtn = new Button();  // Used to display the bottombar
        openBtn.setOnAction(openBtnClickEvent);

        // Add the elements based on the orientation
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            // Page Grid
            VBox box = new VBox();
            box.setAlignment(Pos.CENTER);

            VBox contentBox = new VBox();
            contentBox.setAlignment(Pos.CENTER);
            contentBox.setMaxWidth(PORTRAIT_WIDTH);
            contentBox.getStyleClass().add("section-box");
            box.getChildren().add(contentBox);

            // Add the elements
            contentBox.getChildren().add(tabPane);

            // Tab Pane Controller
            HBox container = new HBox();
            TabPaneController tabPaneDotController = new TabPaneController(tabPane, tabContent, container, onTabListener);
            contentBox.getChildren().add(container);
            container.setMaxWidth(PORTRAIT_WIDTH);

            // Bottom bar
            if (!isBottomBarVisible) {
                bottomBarGrid.setScaleY(0);
            }else{
                box.getChildren().add(bottomBarGrid);
            }

            // Bottom bar open button
            openBtn.getStyleClass().add("expand-btn");
            openBtn.setMaxWidth(PORTRAIT_WIDTH);
            Image openBtnImage;
            if (!isBottomBarVisible) {
                openBtnImage = new Image(TabPaneController.class.getResourceAsStream("/assets/down_arrow.png"), 24, 24, true, true);
            }else{
                openBtnImage = new Image(TabPaneController.class.getResourceAsStream("/assets/up_arrow.png"), 24, 24, true, true);
            }
            ImageView imageView = new ImageView(openBtnImage);
            openBtn.setGraphic(imageView);
            box.getChildren().add(openBtn);

            currentPane = box;
        } else {
            // Page grid
            HBox box = new HBox();
            box.setAlignment(Pos.BOTTOM_CENTER);
            box.getChildren().add(tabPane);

            HBox contentBox = new HBox();
            contentBox.setAlignment(Pos.CENTER);
            contentBox.setMaxHeight(LANDSCAPE_HEIGHT);
            contentBox.getStyleClass().add("section-box");
            box.getChildren().add(contentBox);

            // Tab pane controller
            VBox container = new VBox();
            TabPaneController tabPaneDotController = new TabPaneController(tabPane, tabContent, container, onTabListener);
            contentBox.getChildren().add(container);
            container.setMaxHeight(LANDSCAPE_HEIGHT);

            // Bottom bar
            if (!isBottomBarVisible) {
                bottomBarGrid.setScaleX(0);
            }else{
                box.getChildren().add(bottomBarGrid);
            }

            // Bottom bar open button
            openBtn.getStyleClass().add("expand-btn");
            openBtn.setMaxHeight(LANDSCAPE_HEIGHT);
            Image openBtnImage;
            if (!isBottomBarVisible) {
                openBtnImage = new Image(TabPaneController.class.getResourceAsStream("/assets/right_arrow.png"), 24, 24, true, true);
            }else{
                openBtnImage = new Image(TabPaneController.class.getResourceAsStream("/assets/left_arrow.png"), 24, 24, true, true);
            }
            ImageView imageView = new ImageView(openBtnImage);
            openBtn.setGraphic(imageView);
            box.getChildren().add(openBtn);

            currentPane = box;
        }

        // If the active page is contained in the current section, select the tab
        if (section.getPages().contains(activePage)) {
            int tabIndex = section.getPages().indexOf(activePage);
            tabPane.getSelectionModel().select(tabIndex);
        } else {  // Read the currently active page
            activePage = section.getPages().get(tabPane.getSelectionModel().getSelectedIndex());
        }

        // Animation
        if (activePane != null && animationType != SectionAnimationType.NONE) {
            Node oldContent = activePane;
            Node newContent = currentPane;

            // When the transition is completed, load the new pane in the editor
            EventHandler<ActionEvent> onTransitionCompleted = event -> {
                // Clear the previous section
                controller.getContentBox().getChildren().clear();
                controller.getContentBox().getChildren().add(activePane);
            };

            Transition crossFade = null;
            if (animationType == SectionAnimationType.CROSSFADE) {
                FadeTransition fadeOut = new FadeTransition(
                        Duration.seconds(ENTER_SECTION_FADE_DURATION), oldContent);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);

                fadeOut.setOnFinished(onTransitionCompleted);

                FadeTransition fadeIn = new FadeTransition(
                        Duration.seconds(ENTER_SECTION_FADE_DURATION), newContent);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                crossFade = new SequentialTransition(
                        fadeOut, fadeIn);
            } else if (animationType == SectionAnimationType.ROTATION) {
                RotateTransition rotate = new RotateTransition(
                        Duration.seconds(ROTATE_SECTION_DURATION), oldContent);

                int angle;
                if (screenOrientation == ScreenOrientation.PORTRAIT) {
                    angle = 90;
                } else {
                    angle = -90;
                }
                rotate.setToAngle(angle);
                rotate.setOnFinished(onTransitionCompleted);

                FadeTransition fadeOut = new FadeTransition(
                        Duration.seconds(ENTER_SECTION_FADE_DURATION), oldContent);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0.5);

                FadeTransition fadeIn = new FadeTransition(
                        Duration.seconds(ENTER_SECTION_FADE_DURATION), newContent);
                fadeIn.setFromValue(0.5);
                fadeIn.setToValue(1);

                crossFade = new SequentialTransition(
                        new ParallelTransition(rotate, fadeOut), fadeIn);
            }else if (animationType == SectionAnimationType.OPEN_BOTTOMBAR) {
                controller.getContentBox().getChildren().clear();
                controller.getContentBox().getChildren().add(currentPane);

                TranslateTransition translateUp = new TranslateTransition(
                        Duration.seconds(BUTTON_BAR_DURATION), newContent);
                translateUp.setInterpolator(Interpolator.EASE_OUT);
                if (screenOrientation == ScreenOrientation.PORTRAIT) {
                    translateUp.setFromY(PORTRAIT_BOTTOM_BAR_HEIGHT/2);
                    translateUp.setToY(0);
                }else{
                    translateUp.setFromX(LANDSCAPE_BOTTOM_BAR_WIDTH/2);
                    translateUp.setToX(0);
                }

                ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(BUTTON_BAR_DURATION), bottomBarGrid);
                scaleTransition.setInterpolator(Interpolator.EASE_OUT);
                if (screenOrientation == ScreenOrientation.PORTRAIT) {
                    scaleTransition.setFromY(0);
                    scaleTransition.setToY(1);
                }else{
                    scaleTransition.setFromX(0);
                    scaleTransition.setToX(1);
                }

                TranslateTransition translateDown = new TranslateTransition(
                        Duration.seconds(BUTTON_BAR_DURATION), bottomBarGrid);
                translateDown.setInterpolator(Interpolator.EASE_OUT);
                if (screenOrientation == ScreenOrientation.PORTRAIT) {
                    translateDown.setFromY(-PORTRAIT_BOTTOM_BAR_HEIGHT/2);
                    translateDown.setToY(0);
                }else{
                    translateDown.setFromX(-LANDSCAPE_BOTTOM_BAR_WIDTH/2);
                    translateDown.setToX(0);
                }

                TranslateTransition buttonDown = new TranslateTransition(
                        Duration.seconds(BUTTON_BAR_DURATION), openBtn);

                if (screenOrientation == ScreenOrientation.PORTRAIT) {
                    buttonDown.setFromY(-PORTRAIT_BOTTOM_BAR_HEIGHT);
                    buttonDown.setToY(0);
                }else{
                    buttonDown.setFromX(-PORTRAIT_BOTTOM_BAR_HEIGHT);
                    buttonDown.setToX(0);
                }

                crossFade = new SequentialTransition(
                        new ParallelTransition(translateUp, scaleTransition, translateDown, buttonDown));
            }else if (animationType == SectionAnimationType.CLOSE_BOTTOMBAR && activeBottomBar != null && activeOpenBtn != null) {
                    ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(BUTTON_BAR_DURATION), activeBottomBar);
                    scaleTransition.setInterpolator(Interpolator.EASE_OUT);

                    if (screenOrientation == ScreenOrientation.PORTRAIT) {
                        scaleTransition.setFromY(1);
                        scaleTransition.setToY(0);
                    }else {
                        scaleTransition.setFromX(1);
                        scaleTransition.setToX(0);
                    }

                    TranslateTransition translateDown = new TranslateTransition(
                            Duration.seconds(BUTTON_BAR_DURATION), activeBottomBar);
                    translateDown.setInterpolator(Interpolator.EASE_OUT);
                    if (screenOrientation == ScreenOrientation.PORTRAIT) {
                        translateDown.setFromY(0);
                        translateDown.setToY(-PORTRAIT_BOTTOM_BAR_HEIGHT/2);
                    }else{
                        translateDown.setFromX(0);
                        translateDown.setToX(-LANDSCAPE_BOTTOM_BAR_WIDTH/2);
                    }

                    TranslateTransition buttonDown = new TranslateTransition(
                            Duration.seconds(BUTTON_BAR_DURATION), activeOpenBtn);
                    if (screenOrientation == ScreenOrientation.PORTRAIT) {
                        buttonDown.setFromY(0);
                        buttonDown.setToY(-PORTRAIT_BOTTOM_BAR_HEIGHT/2);
                    }else{
                        buttonDown.setFromX(0);
                        buttonDown.setToX(-LANDSCAPE_BOTTOM_BAR_WIDTH/2);
                    }

                    buttonDown.setOnFinished(onTransitionCompleted);

                    TranslateTransition translateUp = new TranslateTransition(
                            Duration.seconds(BUTTON_BAR_DURATION), newContent);
                    translateUp.setInterpolator(Interpolator.EASE_OUT);

                    if (screenOrientation == ScreenOrientation.PORTRAIT) {
                        translateUp.setFromY(-PORTRAIT_BOTTOM_BAR_HEIGHT/2);
                        translateUp.setToY(0);
                    }else {
                        translateUp.setFromX(-LANDSCAPE_BOTTOM_BAR_WIDTH / 2);
                        translateUp.setToX(0);
                    }

                    crossFade = new SequentialTransition(
                            new ParallelTransition(scaleTransition, translateDown, buttonDown), translateUp);
            }
            if (crossFade != null) {
                crossFade.play();
            }
        } else {
            // Clear the previous section and add the new one
            controller.getContentBox().getChildren().clear();
            controller.getContentBox().getChildren().add(currentPane);
        }

        // Update the active pane
        activePane = currentPane;
        activeBottomBar = bottomBarGrid;
        activeOpenBtn = openBtn;
    }

    /**
     * Types of animation when loading a section
     */
    enum SectionAnimationType {
        NONE,
        CROSSFADE,
        ROTATION,
        OPEN_BOTTOMBAR,
        CLOSE_BOTTOMBAR
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

        // Reload the section
        loadSection(section, SectionAnimationType.NONE);
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

            // Reload the section
            loadSection(section, SectionAnimationType.CROSSFADE);
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

                    int index = 0;
                    // Determine which page has been modified to stay focused on that one
                    for (Section sec : controller.getSectionsListView().getItems()) {
                        if (sec.getStringID().equals(section.getStringID())) {
                            for (int i = 0; i < section.getPages().size(); i++) {
                                if (sec.getPages().size() > i && section.getPages().size() > i) {
                                    // Compare the pages based on the number of components
                                    if (sec.getPages().get(i).getComponents().size() != section.getPages().get(i).getComponents().size()) {
                                        activePage = section.getPages().get(i);
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

                    loadSection(section, animationType);

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

    public static int getWidth(ScreenOrientation screenOrientation) {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return PORTRAIT_WIDTH;
        } else {
            return LANDSCAPE_WIDTH;
        }
    }

    public static int getHeight(ScreenOrientation screenOrientation) {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return PORTRAIT_HEIGHT;
        } else {
            return LANDSCAPE_HEIGHT;
        }
    }
}
