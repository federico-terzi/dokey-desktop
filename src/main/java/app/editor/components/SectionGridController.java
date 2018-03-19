package app.editor.components;

import app.editor.listeners.OnSectionModifiedListener;
import app.editor.model.ScreenOrientation;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import section.model.Page;
import section.model.Section;
import system.WebLinkResolver;
import system.model.ApplicationManager;
import system.ShortcutIconManager;
import utils.ImageResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * This component will createView a Section into a Grid with button bar and animations.
 */
public class SectionGridController {
    public static final int PORTRAIT_HEIGHT = 450;
    public static final int PORTRAIT_WIDTH = 350;
    public static final int PORTRAIT_BOTTOM_BAR_HEIGHT = 100;
    public static final int LANDSCAPE_HEIGHT = 350;
    public static final int LANDSCAPE_WIDTH = 450;
    public static final int LANDSCAPE_BOTTOM_BAR_WIDTH = 100;
    private static final int BOTTOM_BAR_DEFAULT_COLS = 4;

    private static final double ENTER_SECTION_FADE_DURATION = 0.2;
    private static final double ROTATE_SECTION_DURATION = 0.2;
    private static final double BUTTON_BAR_DURATION = 0.2;

    private Section section;
    private VBox container;
    private ScreenOrientation screenOrientation;
    private ApplicationManager applicationManager;
    private ShortcutIconManager shortcutIconManager;
    private WebLinkResolver webLinkResolver;
    private OnSectionModifiedListener sectionModifiedListener;
    private ResourceBundle resourceBundle;
    private OnSectionGridEventListener sectionGridEventListener;

    private boolean isBottomBarVisible = false;

    // INTERNAL
    private Page activePage = null;
    private BottomBarGrid bottomBarGrid = null;
    private Node activePane;
    private Node previousPane;
    private Button showHideBottomBarButton = null;
    private boolean isFirstStart = true;

    public SectionGridController(Section section, VBox container, ScreenOrientation screenOrientation,
                                 ApplicationManager applicationManager, ShortcutIconManager shortcutIconManager,
                                 WebLinkResolver webLinkResolver, OnSectionModifiedListener onSectionModifiedListener,
                                 ResourceBundle resourceBundle, OnSectionGridEventListener sectionGridEventListener) {
        this.section = section;
        this.container = container;
        this.screenOrientation = screenOrientation;
        this.applicationManager = applicationManager;
        this.shortcutIconManager = shortcutIconManager;
        this.webLinkResolver = webLinkResolver;
        this.sectionGridEventListener = sectionGridEventListener;
        this.sectionModifiedListener = onSectionModifiedListener;
        this.resourceBundle = resourceBundle;

        // If there are bottom bar elements, show the bar
        if (section.getBottomBarItems().size() > 0) {
            isBottomBarVisible = true;
        }

        invalidate();
    }

    public interface OnSectionGridEventListener {
        void onRequestChangePageSize(Page page, Section section);

        void onRequestDeletePage(Page page, Section section);

        void onMovePageLeft(Page page, Section section);

        void onMovePageRight(Page page, Section section);

        void onRequestAddPage(Section section);
    }

    /**
     * Trigger a re-rendering of the grid
     */
    public void invalidate() {
        container.getChildren().clear();

        new Thread(() -> {
            Node view = createView();
            Platform.runLater(() -> render(view));
        }).start();
    }

    /**
     * Render the view and display it.
     */
    private void render(Node view) {
        container.getChildren().clear();
        container.getChildren().add(view);

        activePane = view;

        if (isFirstStart) {
            fadeIn();
            isFirstStart = false;
        }
    }

    /**
     * Create the view with all the components into the container.
     */
    private Node createView() {
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
            PageGrid pageGrid = new PageGrid(applicationManager, shortcutIconManager, webLinkResolver,
                    page, section, screenOrientation, resourceBundle);
            pageGrid.setSectionModifiedListener(sectionModifiedListener);
            pageGrid.setShortcutIconManager(shortcutIconManager);

            // Create the tab and add the page grid
            Tab tab = new Tab();
            Label tabTitle = new Label(page.getTitle());
            tab.setGraphic(tabTitle);
            tab.setContent(pageGrid);
            tabContent.put(tab, pageGrid);

            // Add the tab context menu
            final ContextMenu contextMenu = new ContextMenu();
            MenuItem changeSize = new MenuItem(resourceBundle.getString("change_grid_size"));
            changeSize.setStyle("-fx-text-fill: black;");
            changeSize.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (sectionGridEventListener != null) {
                        sectionGridEventListener.onRequestChangePageSize(page, section);
                    }
                }
            });
            MenuItem moveLeft = new MenuItem(resourceBundle.getString("move_left"));
            moveLeft.setStyle("-fx-text-fill: black;");
            moveLeft.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (sectionGridEventListener != null) {
                        sectionGridEventListener.onMovePageLeft(page, section);
                    }
                }
            });
            MenuItem moveRight = new MenuItem(resourceBundle.getString("move_right"));
            moveRight.setStyle("-fx-text-fill: black;");
            moveRight.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (sectionGridEventListener != null) {
                        sectionGridEventListener.onMovePageRight(page, section);
                    }
                }
            });
            MenuItem delete = new MenuItem(resourceBundle.getString("delete"));
            delete.setStyle("-fx-text-fill: black;");
            delete.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (sectionGridEventListener != null) {
                        sectionGridEventListener.onRequestDeletePage(page, section);
                    }
                }
            });
            contextMenu.getItems().addAll(changeSize, new SeparatorMenuItem(), moveLeft, moveRight,
                    new SeparatorMenuItem(), delete);
            tab.setContextMenu(contextMenu);

            // Add the tab
            tabPane.getTabs().add(tab);

            // Select active tab
            if (page.equals(activePage)) {
                tabPane.getSelectionModel().select(tab);
            }
        }

        // Add the bottom bar
        bottomBarGrid = new BottomBarGrid(applicationManager, shortcutIconManager, webLinkResolver,
                BOTTOM_BAR_DEFAULT_COLS, section, screenOrientation, resourceBundle);
        bottomBarGrid.setSectionModifiedListener(sectionModifiedListener);

        // This listener is used by the tab pane controller to select/add tabs
        TabPaneController.OnTabListener onTabListener = new TabPaneController.OnTabListener() {
            @Override
            public void onTabSelected(int index) {
                tabPane.getSelectionModel().select(index);
                activePage = section.getPages().get(index);  // Update the active page
            }

            @Override
            public void onAddTab() {
                if (sectionGridEventListener != null) {
                    sectionGridEventListener.onRequestAddPage(section);
                }
            }
        };

        Node currentPane;

        showHideBottomBarButton = new Button();  // Used to display/hide the bottombar
        showHideBottomBarButton.setOnAction(event -> setBottomBarVisible(!isBottomBarVisible));  // Toggle the bottombar visibility

        // Add the elements based on the orientation
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            // Page Grid
            VBox box = new VBox();
            box.setAlignment(Pos.CENTER);

            // Used to contain the components and avoid line bugs in transitions
            // by setting the background color to the same.
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
                bottomBarGrid.setManaged(false);
            }
            box.getChildren().add(bottomBarGrid);

            // Bottom bar open button
            showHideBottomBarButton.getStyleClass().add("expand-btn");
            showHideBottomBarButton.setMaxWidth(PORTRAIT_WIDTH);

            renderShowHideBtn();

            box.getChildren().add(showHideBottomBarButton);

            currentPane = box;
        } else {
            // Page grid
            HBox box = new HBox();
            box.setAlignment(Pos.BOTTOM_CENTER);
            box.getChildren().add(tabPane);

            // Used to contain the components and avoid line bugs in transitions
            // by setting the background color to the same.
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
                bottomBarGrid.setManaged(false);
            }
            box.getChildren().add(bottomBarGrid);

            // Bottom bar open button
            showHideBottomBarButton.getStyleClass().add("expand-btn");
            showHideBottomBarButton.setMaxHeight(LANDSCAPE_HEIGHT);

            renderShowHideBtn();

            box.getChildren().add(showHideBottomBarButton);

            currentPane = box;
        }

        // If the active page is contained in the current section, select the tab
        if (section.getPages().contains(activePage)) {
            int tabIndex = section.getPages().indexOf(activePage);
            tabPane.getSelectionModel().select(tabIndex);
        } else {  // Read the currently active page
            activePage = section.getPages().get(tabPane.getSelectionModel().getSelectedIndex());
        }

        return currentPane;
    }

    /**
     * Types of animation when loading a section
     */
    public enum SectionAnimationType {
        NONE,
        CROSSFADE,
        ROTATION,
        OPEN_BOTTOMBAR,
        CLOSE_BOTTOMBAR
    }

    private void renderShowHideBtn() {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            Image openBtnImage;
            if (!isBottomBarVisible) {
                openBtnImage = ImageResolver.getInstance().getImage(TabPaneController.class.getResourceAsStream("/assets/down_arrow.png"), 24);
            } else {
                openBtnImage = ImageResolver.getInstance().getImage(TabPaneController.class.getResourceAsStream("/assets/up_arrow.png"), 24);
            }
            ImageView imageView = new ImageView(openBtnImage);
            imageView.setFitWidth(24);
            imageView.setFitHeight(24);
            showHideBottomBarButton.setGraphic(imageView);
        } else {
            Image openBtnImage;
            if (!isBottomBarVisible) {
                openBtnImage = ImageResolver.getInstance().getImage(TabPaneController.class.getResourceAsStream("/assets/right_arrow.png"), 24);
            } else {
                openBtnImage = ImageResolver.getInstance().getImage(TabPaneController.class.getResourceAsStream("/assets/left_arrow.png"), 24);
            }
            ImageView imageView = new ImageView(openBtnImage);
            imageView.setFitWidth(24);
            imageView.setFitHeight(24);
            showHideBottomBarButton.setGraphic(imageView);
        }
    }

    private void showBottomBar() {
        bottomBarGrid.setManaged(true);

        TranslateTransition translateUp = new TranslateTransition(
                Duration.seconds(BUTTON_BAR_DURATION), activePane);
        translateUp.setInterpolator(Interpolator.EASE_OUT);
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            translateUp.setFromY(PORTRAIT_BOTTOM_BAR_HEIGHT / 2);
            translateUp.setToY(0);
        } else {
            translateUp.setFromX(LANDSCAPE_BOTTOM_BAR_WIDTH / 2);
            translateUp.setToX(0);
        }

        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(BUTTON_BAR_DURATION), bottomBarGrid);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            scaleTransition.setFromY(0);
            scaleTransition.setToY(1);
        } else {
            scaleTransition.setFromX(0);
            scaleTransition.setToX(1);
        }

        TranslateTransition translateDown = new TranslateTransition(
                Duration.seconds(BUTTON_BAR_DURATION), bottomBarGrid);
        translateDown.setInterpolator(Interpolator.EASE_OUT);
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            translateDown.setFromY(-PORTRAIT_BOTTOM_BAR_HEIGHT / 2);
            translateDown.setToY(0);
        } else {
            translateDown.setFromX(-LANDSCAPE_BOTTOM_BAR_WIDTH / 2);
            translateDown.setToX(0);
        }

        TranslateTransition buttonDown = new TranslateTransition(
                Duration.seconds(BUTTON_BAR_DURATION), showHideBottomBarButton);

        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            buttonDown.setFromY(-PORTRAIT_BOTTOM_BAR_HEIGHT);
            buttonDown.setToY(0);
        } else {
            buttonDown.setFromX(-PORTRAIT_BOTTOM_BAR_HEIGHT);
            buttonDown.setToX(0);
        }

        Transition transition = new ParallelTransition(translateUp, scaleTransition, translateDown, buttonDown);

        transition.setOnFinished(event -> {
            isBottomBarVisible = true;
            renderShowHideBtn();
        });

        transition.play();
    }

    private void hideBottomBar() {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(BUTTON_BAR_DURATION), bottomBarGrid);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            scaleTransition.setFromY(1);
            scaleTransition.setToY(0);
        } else {
            scaleTransition.setFromX(1);
            scaleTransition.setToX(0);
        }

        TranslateTransition translateDown = new TranslateTransition(
                Duration.seconds(BUTTON_BAR_DURATION), bottomBarGrid);
        translateDown.setInterpolator(Interpolator.EASE_OUT);
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            translateDown.setFromY(0);
            translateDown.setToY(-PORTRAIT_BOTTOM_BAR_HEIGHT / 2);
        } else {
            translateDown.setFromX(0);
            translateDown.setToX(-LANDSCAPE_BOTTOM_BAR_WIDTH / 2);
        }

        TranslateTransition buttonDown = new TranslateTransition(
                Duration.seconds(BUTTON_BAR_DURATION), showHideBottomBarButton);
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            buttonDown.setFromY(0);
            buttonDown.setToY(-PORTRAIT_BOTTOM_BAR_HEIGHT);
        } else {
            buttonDown.setFromX(0);
            buttonDown.setToX(-LANDSCAPE_BOTTOM_BAR_WIDTH);
        }

        TranslateTransition translateAll = new TranslateTransition(
                Duration.seconds(BUTTON_BAR_DURATION), activePane);
        translateAll.setInterpolator(Interpolator.EASE_OUT);

        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            translateAll.setFromY(0);
            translateAll.setToY(PORTRAIT_BOTTOM_BAR_HEIGHT / 2);
        } else {
            translateAll.setFromX(0);
            translateAll.setToX(LANDSCAPE_BOTTOM_BAR_WIDTH / 2);
        }

        Transition transition = new SequentialTransition(new ParallelTransition(scaleTransition, translateDown, buttonDown, translateAll));

        transition.setOnFinished(event -> {
            bottomBarGrid.setManaged(false);
            showHideBottomBarButton.setTranslateY(0);
            showHideBottomBarButton.setTranslateX(0);
            activePane.setTranslateY(0);
            activePane.setTranslateX(0);
            isBottomBarVisible = false;
            renderShowHideBtn();
        });

        transition.play();
    }

    public boolean isBottomBarVisible() {
        return isBottomBarVisible;
    }

    public void setBottomBarVisible(boolean bottomBarVisible) {
        isBottomBarVisible = bottomBarVisible;
        if (isBottomBarVisible) {
            showBottomBar();
        } else {
            hideBottomBar();
        }
    }

    private void rotate(int angle) {
        Node newView = createView();
        Node oldContent = activePane;

        oldContent.setCache(true);
        oldContent.setCacheHint(CacheHint.SPEED);

        RotateTransition rotate = new RotateTransition(
                Duration.seconds(ROTATE_SECTION_DURATION), oldContent);

        rotate.setToAngle(angle);
        rotate.setOnFinished(event -> {
            render(newView);
        });

        FadeTransition fadeOut = new FadeTransition(
                Duration.seconds(ENTER_SECTION_FADE_DURATION), oldContent);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0.5);

        FadeTransition fadeIn = new FadeTransition(
                Duration.seconds(ENTER_SECTION_FADE_DURATION), newView);
        fadeIn.setFromValue(0.5);
        fadeIn.setToValue(1);

        new SequentialTransition(new ParallelTransition(rotate, fadeOut), fadeIn).play();
    }

    private void fadeIn() {
        activePane.setCache(true);
        activePane.setCacheHint(CacheHint.SPEED);
        FadeTransition fadeIn = new FadeTransition(
                Duration.seconds(0.4), activePane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished((event) -> activePane.setCache(false));
        fadeIn.play();
    }

    public Section getSection() {
        return section;
    }

    public ScreenOrientation getScreenOrientation() {
        return screenOrientation;
    }

    public void setScreenOrientation(ScreenOrientation screenOrientation) {
        if (screenOrientation == ScreenOrientation.PORTRAIT && this.screenOrientation == ScreenOrientation.LANDSCAPE) {
            this.screenOrientation = screenOrientation;
            rotate(90);
        } else if (screenOrientation == ScreenOrientation.LANDSCAPE && this.screenOrientation == ScreenOrientation.PORTRAIT) {
            this.screenOrientation = screenOrientation;
            rotate(-90);
        }
        this.screenOrientation = screenOrientation;
    }

    public OnSectionModifiedListener getSectionModifiedListener() {
        return sectionModifiedListener;
    }

    public void setSectionModifiedListener(OnSectionModifiedListener sectionModifiedListener) {
        this.sectionModifiedListener = sectionModifiedListener;
    }

    public Page getActivePage() {
        return activePage;
    }

    public void setActivePage(Page activePage) {
        this.activePage = activePage;
    }

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
