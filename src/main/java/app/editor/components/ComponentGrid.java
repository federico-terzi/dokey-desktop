package app.editor.components;

import app.editor.listeners.OnComponentClickListener;
import app.editor.stages.ShortcutDialogStage;
import app.editor.stages.AppSelectDialogStage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import section.model.*;
import system.model.Application;
import system.model.ApplicationManager;

import java.io.IOException;
import java.util.Optional;

public class ComponentGrid extends GridPane{

    private ApplicationManager applicationManager;
    private int height;
    private int width;
    private OnComponentSelectedListener onComponentSelectedListener;
    private OnComponentClickListener onComponentClickListener;
    private SectionType sectionType = SectionType.LAUNCHPAD;

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
        // Remove the previous component
        removeComponentFromGrid(col, row, component);

        // Set up the button
        DragButton current = getButtonForComponent(component);
        current.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                onComponentClicked(col, row);
            }
        });
        // Set the context menu actions
        if (current instanceof ComponentButton) {
            ((ComponentButton) current).setOnComponentActionListener(new ComponentButton.OnComponentActionListener() {
                @Override
                public void onComponentEdit() {

                }

                @Override
                public void onComponentDelete() {
                    requestDeleteComponent(col, row);
                }

                // When the component is dropped away, request the
                // deletion from the grid
                @Override
                public void onComponentDroppedAway() {
                    requestDeleteComponent(col, row);
                }
            });
        }

        // Add the component to the matrix
        if (component != null) {
            componentMatrix[col][row] = component;
        }

        // Set up the drag and drop
        current.setOnComponentDragListener(new DragButton.OnComponentDragListener() {
            @Override
            public boolean onComponentDropped(Component component) {
                // If the button is not empty, ask for the deletion confirmation
                if (!(current instanceof EmptyButton)) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Overwrite Button");
                    alert.setHeaderText("Are you sure you want to overwrite this button?");
                    alert.setContentText("If you proceed, the button will be replaced.");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() != ButtonType.OK){
                        return false;
                    }else{  // OVERWRITE
                        // Delete the previous component
                        requestDeleteComponent(col, row);
                    }
                }

                // Change the component coordinates
                component.setX(row);
                component.setY(col);

                // Add the component in the new position
                addComponentToGrid(col, row, component);

                // Notify the listener
                if (onComponentSelectedListener != null) {
                    onComponentSelectedListener.onNewComponentRequested(component);
                }

                return true;
            }
        });

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

    private void removeComponentFromGrid(int col, int row, Component component) {
        // If the matrix contains a component, delete it.
        if (componentMatrix[col][row] != null) {
            // Remove the component from the grid
            Node node = getNodeFromGridPane(this, col, row);
            while (node != null) {
                this.getChildren().removeAll(node);
                node = getNodeFromGridPane(this, col, row);
            }
            // And from the matrix
            componentMatrix[col][row] = null;
        }
    }

    public void onComponentClicked(int col, int row) {
        Component component = componentMatrix[col][row];
        if (component == null) {  // Clicked on empty space
            if (sectionType == SectionType.LAUNCHPAD) {  // LAUNCHPAD SECTION
                requestApplicationSelect(col, row);
            }else if (sectionType == SectionType.SHORTCUTS) {  // LAUNCHPAD SHORTCUTS
                requestShortcutSelect(col, row);
            }

        }else{  // Clicked on active component
            if (onComponentClickListener != null) {
                onComponentClickListener.onComponentClicked(component);
            }
        }
    }

    private void requestApplicationSelect(int col, int row) {
        try {
            AppSelectDialogStage appSelectDialogStage = new AppSelectDialogStage(applicationManager, new AppSelectDialogStage.OnApplicationListener() {
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
            appSelectDialogStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestShortcutSelect(int col, int row) {
        try {
            ShortcutDialogStage stage = new ShortcutDialogStage(new ShortcutDialogStage.OnShortcutListener() {
                @Override
                public void onShortcutSelected(String shortcut, String name) {
                    // Create the component
                    ShortcutItem item = new ShortcutItem();
                    item.setShortcut(shortcut);
                    item.setTitle(name);
                    item.setIconID("copy");
                    Component component = new Component();
                    component.setItem(item);
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
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface OnComponentSelectedListener {
        void onNewComponentRequested(Component component);
        void onDeleteComponentRequested(Component component);
    }

    public void setOnComponentClickListener(OnComponentClickListener onComponentClickListener) {
        this.onComponentClickListener = onComponentClickListener;
    }

    public SectionType getSectionType() {
        return sectionType;
    }

    public void setSectionType(SectionType sectionType) {
        this.sectionType = sectionType;
    }



    private void requestDeleteComponent(int col, int row) {
        Component component = componentMatrix[col][row];

        // Remove the item from the grid and replace it with an empty one
        removeComponentFromGrid(col, row, component);
        addComponentToGrid(col, row, null);

        // Notify the listener
        if (onComponentSelectedListener != null) {
            onComponentSelectedListener.onDeleteComponentRequested(component);
        }
    }

    public DragButton getButtonForComponent(Component component) {
        if (component != null) {
            if (component.getItem() instanceof AppItem) {  // APP ITEM
                AppItem appItem = (AppItem) component.getItem();
                Application application = applicationManager.getApplication(appItem.getAppID());
                // Make sure the application exists
                if (application != null) {
                    AppButton appButton = new AppButton(component, application);
                    return appButton;
                }
            }else if (component.getItem() instanceof ShortcutItem) {  // SHORTCUT ITEM
                //ShortcutItem appItem = (ShortcutItem) component.getItem();
                return new ShortcutButton(component);
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
