package app.editor.components;

import app.stages.AppListStage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import section.model.AppItem;
import section.model.Component;
import section.model.ItemType;
import system.model.Application;
import system.model.ApplicationManager;

import java.io.IOException;

public class ComponentGrid extends GridPane{

    private ApplicationManager applicationManager;
    private int height;
    private int width;
    private OnComponentSelectedListener onComponentSelectedListener;

    private Component[][] componentMatrix;

    public ComponentGrid(ApplicationManager applicationManager, Component[][] componentMatrix) {
        super();
        this.applicationManager = applicationManager;
        this.componentMatrix = componentMatrix;

        setupConstraints();

        render();
    }

    private void setupConstraints() {
        for (int rowIndex = 0; rowIndex < componentMatrix[0].length; rowIndex++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS) ; // allow row to grow
            rc.setFillHeight(true); // ask nodes to fill height for row
            rc.setPercentHeight(100);
            // other settings as needed...
            getRowConstraints().add(rc);
        }
        for (int colIndex = 0; colIndex < componentMatrix.length; colIndex++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS) ; // allow column to grow
            cc.setFillWidth(true); // ask nodes to fill space for column
            cc.setPercentWidth(100);
            // other settings as needed...
            getColumnConstraints().add(cc);
        }
    }

    public void render() {
        // Add all the components
        for (int col = 0; col < componentMatrix.length; col++) {
            for (int row = 0; row < componentMatrix[0].length; row++) {
                addComponentToGrid(col, row, componentMatrix[col][row]);
            }
        }
    }

    public void addComponentToGrid(int col, int row, Component component) {
        // If the matrix contains a component, delete it.
        if (componentMatrix[col][row] != null) {
            // Remove the component from the grid
            this.getChildren().removeAll(getNodeFromGridPane(this, col, row));
            // And from the matrix
            componentMatrix[col][row] = null;
        }

        // Set up the button
        Button current = getButtonForComponent(component);
        current.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                onComponentClicked(col, row);
            }
        });

        // Add the component to the matrix
        if (component != null) {
            componentMatrix[col][row] = component;
        }

        // Set up the span
        int colSpan = 1;
        int rowSpan = 1;
        if (componentMatrix[col][row] != null) {
            colSpan = componentMatrix[col][row].getYSpan();
            rowSpan = componentMatrix[col][row].getXSpan();
        }

        // Add the component to the grid
        this.add(current, col, row, colSpan, rowSpan);
    }

    public void onComponentClicked(int col, int row) {
        Component component = componentMatrix[col][row];
        if (component == null) {  // Clicked on empty space
            requestApplicationSelect(col, row);
        }
    }

    private void requestApplicationSelect(int col, int row) {
        try {
            AppListStage appListStage = new AppListStage(applicationManager, new AppListStage.OnApplicationListener() {
                @Override
                public void onApplicationSelected(Application application) {
                    // Create the component
                    AppItem appItem = new AppItem();
                    appItem.setAppID(application.getExecutablePath());
                    appItem.setTitle(application.getName());
                    appItem.setItemType(ItemType.APP);
                    Component component = new Component();
                    component.setItem(appItem);
                    component.setX(row);
                    component.setY(col);
                    component.setXSpan(1);
                    component.setYSpan(1);

                    // Add the item to the grid
                    addComponentToGrid(col, row, component);

                    // Notify the listener
                    if (onComponentSelectedListener != null) {
                        onComponentSelectedListener.onNewComponentRequested(component);
                    }
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

    public interface OnComponentSelectedListener {
        void onNewComponentRequested(Component component);
    }

    public Button getButtonForComponent(Component component) {
        if (component != null) {
            if (component.getItem() instanceof AppItem) {  // APP ITEM
                AppItem appItem = (AppItem) component.getItem();
                Application application = applicationManager.getApplication(appItem.getAppID());
                // Make sure the application exists
                if (application != null) {
                    AppButton appButton = new AppButton(application);
                    return appButton;
                }
            }
        }

        // No component found, return the empty button
        return new EmptyButton();
    }

    public void setHeight(int height) {
        this.height = height;
        this.setPrefHeight(height);
        this.setMaxHeight(height);
        this.setMinHeight(height);
    }

    public void setWidth(int width) {
        this.width = width;
        this.setPrefWidth(width);
        this.setMaxWidth(width);
        this.setMinWidth(width);
    }

    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    public OnComponentSelectedListener getOnComponentSelectedListener() {
        return onComponentSelectedListener;
    }

    public void setOnComponentSelectedListener(OnComponentSelectedListener onComponentSelectedListener) {
        this.onComponentSelectedListener = onComponentSelectedListener;
    }
}
