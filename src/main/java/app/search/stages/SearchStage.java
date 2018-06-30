package app.search.stages;

import app.search.controllers.SearchController;
import app.search.listcells.ResultListCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import system.ResourceUtils;
import system.search.SearchEngine;
import system.search.results.*;
import utils.ImageResolver;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SearchStage extends Stage {
    public static final int MAX_RESULTS = 6; // Maximum number of results
    public static final int MAX_RESULTS_FOR_AGENT = 3; // Maximum number of results for each agent

    private SearchController controller;
    private ResourceBundle resourceBundle;
    private SearchEngine searchEngine;

    private int screenCenterX;
    private int screenCenterY;

    /**
     * This map is used to store image caches
     */
    private ConcurrentHashMap<String, Image> imageCacheMap = new ConcurrentHashMap<>();

    // In this list are registered the priority of the results in the search bar
    // The first elements are displayed first
    private List<Class<? extends AbstractResult>> resultPriorityList = new ArrayList<>(20);

    // Similar to the resultPriorityList, here are registered only the types of filter
    // the user can select. For example, the terminal filter cannot be selected by the user.
    private List<Class<? extends AbstractResult>> userFilters = new ArrayList<>(10);

    // If this is set, only show the results of this category
    private Class<? extends AbstractResult> resultFilter = null;

    // The result list, when modified the listview updates
    private ObservableList<AbstractResult> observableResults = FXCollections.observableArrayList();

    // The current results for the given query
    private Map<Class<? extends AbstractResult>, List<? extends AbstractResult>> currentResults;

    public SearchStage(ResourceBundle resourceBundle, SearchEngine searchEngine) throws IOException {
        this.resourceBundle = resourceBundle;
        this.searchEngine = searchEngine;
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/search_dialog.fxml").toURI().toURL());
        fxmlLoader.setResources(resourceBundle);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(ResourceUtils.getResource("/css/search_dialog.css").toURI().toString());
        this.setTitle("Dokey Search");  // DON'T CHANGE, used on window in the focus mechanism.
        this.setScene(scene);
        this.setAlwaysOnTop(true);
        initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        this.getIcons().add(new Image(SearchStage.class.getResourceAsStream("/assets/icon.png")));

        controller = (SearchController) fxmlLoader.getController();
        controller.resultListView.setManaged(false);

        // Hide the filter box
        controller.filterBox.setManaged(true);
        controller.filterBox.setVisible(true);

        // Setup the list cells
        Image fallback = ImageResolver.getInstance().getImage(SearchStage.class.getResourceAsStream("/assets/photo.png"), 32);
        controller.resultListView.setCellFactory(new Callback<ListView<AbstractResult>, ListCell<AbstractResult>>() {
            @Override
            public ListCell<AbstractResult> call(ListView<AbstractResult> param) {
                return new ResultListCell(resourceBundle, fallback, imageCacheMap);
            }
        });

        // Setup the text field search callbacks
        controller.queryTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchEngine.requestQuery(newValue.trim(), (results) -> {
                currentResults = results;

                renderResults();
            });
        });
        // If someone deselects the textfield, re select it
        controller.queryTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == false) {
                Platform.runLater(() -> controller.queryTextField.requestFocus());
            }
        });

        // Setup keyboard events
        controller.queryTextField.setOnKeyPressed(event -> {
            int currentlySelected = controller.resultListView.getSelectionModel().getSelectedIndex();
            if (event.getCode() == KeyCode.DOWN) {  // Select next element in the list
                if (currentlySelected < (controller.resultListView.getItems().size() - 1)) {
                    controller.resultListView.getSelectionModel().select(currentlySelected + 1);
                }else{  // Select the first
                    if (controller.resultListView.getItems().size() > 0) {
                        controller.resultListView.getSelectionModel().select(0);
                    }
                }

                event.consume();
            } else if (event.getCode() == KeyCode.UP) {  // Select previous element in the list
                if (currentlySelected > 0) {
                    controller.resultListView.getSelectionModel().select(currentlySelected - 1);
                }else{  // Select the last one
                    if (controller.resultListView.getItems().size() > 0) {
                        controller.resultListView.getSelectionModel().select(controller.resultListView.getItems().size()-1);
                    }
                }

                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {  // Execute action and close the stage
                AbstractResult result = (AbstractResult) controller.resultListView.getSelectionModel().getSelectedItem();
                executeSearch(result);
            } else if (event.getCode() == KeyCode.ESCAPE) { // Close the search stage or remove filter
                if (resultFilter != null) {  // REMOVE FILTER
                    resultFilter = null;
                    renderResults();
                }else{  // CLOSE
                    close();
                }
            } else if (event.getCode() == KeyCode.TAB) {  // Filter results
                if ((controller.resultListView.getSelectionModel().getSelectedItem() == null) ||
                        ((AbstractResult) controller.resultListView.getSelectionModel().getSelectedItem())
                                .getClass().equals(resultFilter)) {
                    int index = 0;
                    if (resultFilter != null) {
                        int nextIndex = userFilters.indexOf(resultFilter) + 1;
                        if (nextIndex != -1) {
                            if (nextIndex >= userFilters.size()) {
                                index = 0;
                            }else{
                                index = nextIndex;
                            }
                        }
                    }
                    resultFilter = userFilters.get(index);
                    renderResults();
                }else{
                    if (controller.resultListView.getSelectionModel().getSelectedItem() != null) {
                        Class<? extends AbstractResult> selectedClass = (Class<? extends AbstractResult>) controller.resultListView.getSelectionModel().getSelectedItem().getClass();
                        // Make sure the selected item is filterable from the user
                        // for example, the terminal cannot be filtered
                        if (userFilters.contains(selectedClass)) {
                            resultFilter = selectedClass;
                            renderResults();
                        }
                    }
                }

                event.consume();
            }
        });
        // Detect if window lose focus
        focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == false)
                close();
        }));

        // Result list view click listener, execute the corresponding action
        controller.resultListView.setOnMouseClicked(event -> {
            AbstractResult result = (AbstractResult) controller.resultListView.getSelectionModel().getSelectedItem();
            executeSearch(result);
        });

        registerResultPriorityList();
        registerUserFilterList();

        controller.resultListView.setItems(observableResults);

        preInitialize();
    }

    /**
     * Actions that must be done before showing the bar to initialize it to its empty state.
     */
    public void preInitialize() {
        // Reset the result data structures
        currentResults = new HashMap<>();
        observableResults.clear();

        resultFilter = null;

        // Reset the query text field to an empty string
        controller.queryTextField.setText("");

        renderResults();
    }

    /**
     * Actions that must be done after showing the bar to initialize it.
     */
    public void postInitialize() {
        Platform.runLater(() -> positionBarOnScreen());
    }

    /**
     * Position the bar in the correct position of the screen
     */
    private void positionBarOnScreen() {
        // Calculate the correct coordinates for the center of the screen
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        setX((primScreenBounds.getWidth() - getWidth()) / 2);
        setY((primScreenBounds.getHeight() - getHeight()) / 4 * 1);
    }

    private void renderResults() {
        // Render filter label
        Platform.runLater(() -> {
            // Show the filter label
            if (resultFilter != null) {
                // Get the filter text
                try {
                    String labelID = (String) resultFilter.getDeclaredField("SEARCH_FILDER_RESOURCE_ID").get(null);
                    String filterText = resourceBundle.getString(labelID);
                    if (filterText != null) {
                        controller.filterLabel.setText(filterText);
                        controller.filterBox.setManaged(true);
                        controller.filterBox.setVisible(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{  // hide the filter label
                controller.filterBox.setManaged(false);
                controller.filterBox.setVisible(false);
            }
        });

        if (currentResults == null)
            return;

        ArrayList<AbstractResult> priorityResults = new ArrayList<>(20);

        // If the results has more than one category, limit the results for each one
        boolean limitResultsForCategory = currentResults.keySet().size() > 1;

        // If there is a result filter, only show those results
        if (resultFilter != null) {
            if (currentResults.containsKey(resultFilter)) {
                for (AbstractResult result : currentResults.get(resultFilter)) {
                    if (priorityResults.size() < MAX_RESULTS) {
                        priorityResults.add(result);
                    }
                }
            }
        }else{  // No filter, show all
            // Get the result for each result category
            for (Class<? extends AbstractResult> resultClass : resultPriorityList) {
                if (currentResults.containsKey(resultClass)) {
                    int currentResult = 0;

                    for (AbstractResult result : currentResults.get(resultClass)) {
                        if ((currentResult < MAX_RESULTS_FOR_AGENT || !limitResultsForCategory)
                                && priorityResults.size() < MAX_RESULTS) {
                            priorityResults.add(result);
                            currentResult++;
                        }else{
                            break;
                        }
                    }
                }
            }
        }

        // Update the list view
        Platform.runLater(() -> {
            observableResults.setAll(priorityResults);

            // Select the first item
            if (priorityResults.size() > 0) {
                controller.resultListView.getSelectionModel().select(0);
            }

            // Set the height based on the list view
            controller.resultListView.setPrefHeight(priorityResults.size() * ResultListCell.ROW_HEIGHT);

            // Show the list view and refresh stage size to fit all contents
            controller.resultListView.setManaged(true);
            sizeToScene();
        });
    }

    /**
     * Register the result type priority in the list
     */
    private void registerResultPriorityList() {
        resultPriorityList.add(QuickCommandResult.class);
        resultPriorityList.add(AddUrlToQuickCommandsResult.class);
        resultPriorityList.add(CalculatorResult.class);
        resultPriorityList.add(TerminalResult.class);
        resultPriorityList.add(DebugResult.class);
        resultPriorityList.add(ApplicationResult.class);
        resultPriorityList.add(ShortcutResult.class);
        resultPriorityList.add(BookmarkResult.class);
        resultPriorityList.add(GoogleSearchResult.class);
    }

    /**
     * Register the user filters.
     */
    private void registerUserFilterList() {
        userFilters.add(GoogleSearchResult.class);
        userFilters.add(BookmarkResult.class);
        userFilters.add(ShortcutResult.class);
        userFilters.add(ApplicationResult.class);
        userFilters.add(QuickCommandResult.class);
    }

    private void executeSearch(AbstractResult result) {
        if (result != null) {
            new Thread(() -> {
                result.executeAction();
            }).start();
            close();
        }
    }
}
