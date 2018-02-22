package app.editor.components;

import app.editor.stages.EditorStage;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Map;

public class TabPaneController {
    public static final double SLIDE_DURATION = 0.15;

    private TabPane tabPane;
    private Map<Tab, Node> tabContent;
    private Pane masterPane;
    private OnTabListener listener;

    public TabPaneController(TabPane tabPane, Map<Tab, Node> tabContent, Pane masterPane, OnTabListener listener) {
        super();
        this.tabPane = tabPane;
        this.tabContent = tabContent;
        this.masterPane = masterPane;
        this.listener = listener;

        masterPane.getStyleClass().add("tab-pane-controller");

        if (masterPane instanceof HBox) {
            ((HBox)masterPane).setAlignment(Pos.CENTER);
            masterPane.getStyleClass().add("portrait");
        }else{
            ((VBox)masterPane).setAlignment(Pos.CENTER);
            masterPane.getStyleClass().add("landscape");
        }

        render();
    }

    private void render() {
        masterPane.getChildren().clear();

        int index = 0;
        for (Tab tab : tabPane.getTabs()) {
            Button button = new Button();
            int finalIndex = index;
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (listener != null) {
                        listener.onTabSelected(finalIndex);
                        render();
                    }
                }
            });
            Image image = new Image(TabPaneController.class.getResourceAsStream("/assets/circle_full.png"), 16, 16, true, true);
            ImageView imageView = new ImageView(image);
            if (tab.isSelected()) {
                imageView.getStyleClass().add("dot-selected");
            }else{
                imageView.getStyleClass().remove("dot-selected");
            }
            button.setGraphic(imageView);
            button.setContextMenu(tab.getContextMenu());

            // Handle the drag and drop focus switch
            button.setOnDragEntered(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent event) {
                    tabPane.getSelectionModel().select(tab);
                    render();
                }
            });

            masterPane.getChildren().add(button);
            index++;
        }

        // Add button
        Button addBtn = new Button();
        addBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (listener != null) {
                    listener.onAddTab();
                    render();
                }
            }
        });
        Image image = new Image(TabPaneController.class.getResourceAsStream("/assets/add_white.png"), 16, 16, true, true);
        ImageView imageView = new ImageView(image);
        addBtn.setGraphic(imageView);
        masterPane.getChildren().add(addBtn);

        // Transition animation
        tabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldTab, newTab) -> {
                            int direction;

                            // Determine the slide direction
                            if (tabPane.getTabs().indexOf(newTab) > tabPane.getTabs().indexOf(oldTab)) {
                                direction = -1;
                            }else{
                                direction = 1;
                            }

                            oldTab.setContent(null);
                            Node oldContent = tabContent.get(oldTab);
                            Node newContent = tabContent.get(newTab);

                            newTab.setContent(oldContent);
                            TranslateTransition fadeOut = new TranslateTransition(
                                    Duration.seconds(SLIDE_DURATION), oldContent);
                            fadeOut.setByX(direction*SectionGridController.PORTRAIT_WIDTH);

                            TranslateTransition fadeIn = new TranslateTransition(
                                    Duration.seconds(SLIDE_DURATION), newContent);
                            fadeIn.setFromX(-direction*SectionGridController.PORTRAIT_WIDTH);
                            fadeIn.setToX(0);
                            fadeOut.setOnFinished(event -> {
                                newTab.setContent(newContent);
                            });

                            SequentialTransition crossFade = new SequentialTransition(
                                    fadeOut, fadeIn);
                            crossFade.play();
                        });
    }

    public interface OnTabListener {
        void onTabSelected(int index);
        void onAddTab();
    }
}
