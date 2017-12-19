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
import system.sicons.ShortcutIcon;
import system.sicons.ShortcutIconManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComponentGrid extends GridPane {

    private ApplicationManager applicationManager;
    private OnComponentSelectedListener onComponentSelectedListener;
    private OnComponentClickListener onComponentClickListener;
    private SectionType sectionType = SectionType.LAUNCHPAD;
    private ShortcutIconManager shortcutIconManager;

    private Component[][] componentMatrix;
    private boolean forceDiscardSpan = false;

    public ComponentGrid(ApplicationManager applicationManager, ShortcutIconManager shortcutIconManager, Component[][] componentMatrix) {
        super();
        this.applicationManager = applicationManager;
        this.shortcutIconManager = shortcutIconManager;
        this.componentMatrix = componentMatrix;

        render();

        setupConstraints();
    }

    private void setupConstraints() {
        for (int rowIndex = 0; rowIndex < componentMatrix[0].length; rowIndex++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS); // allow row to grow
            rc.setFillHeight(true); // ask nodes to fill height for row
            rc.setPercentHeight(100);
            // other settings as needed...
            getRowConstraints().add(rc);
        }
        for (int colIndex = 0; colIndex < componentMatrix.length; colIndex++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS); // allow column to grow
            cc.setFillWidth(true); // ask nodes to fill space for column
            cc.setPercentWidth(100);
            // other settings as needed...
            getColumnConstraints().add(cc);
        }
    }

    public void render() {
        // Delete all the previous nodes
        getChildren().clear();

        // Add all the components
        for (int col = 0; col < componentMatrix.length; col++) {
            for (int row = 0; row < componentMatrix[0].length; row++) {
                if (componentMatrix[col][row] != null) {
                    // if force discard span is specified, all the blocks will lose their span
                    if (forceDiscardSpan) {
                        componentMatrix[col][row].setYSpan(1);
                        componentMatrix[col][row].setXSpan(1);
                    }
                    addComponentToGrid(col, row, componentMatrix[col][row]);
                }else{
                    // Make sure in this position there isn't any spanned component
                    if (getComponentAtCords(col, row)==null) {
                        // Add the empty button
                        addComponentToGrid(col, row, null);
                    }
                }
            }
        }
    }

    private Component getComponentAtCords(int vCol, int vRow) {
        for (int col = 0; col < componentMatrix.length; col++) {
            for (int row = 0; row < componentMatrix[0].length; row++) {
                if (componentMatrix[col][row] != null &&
                        vCol >= componentMatrix[col][row].getY() &&
                        vCol < (componentMatrix[col][row].getY() + componentMatrix[col][row].getYSpan()) &&
                        vRow >= componentMatrix[col][row].getX() &&
                        vRow < (componentMatrix[col][row].getX() + componentMatrix[col][row].getXSpan())) {
                    return componentMatrix[col][row];
                }
            }
        }

        return null;
    }

    public boolean addComponentToGrid(int col, int row, Component component) {
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
                    render();
                }

                @Override
                public void onComponentDelete() {
                    requestDeleteComponent(component);
                    render();
                }

                // When the component is dropped away, request the
                // deletion from the grid
                @Override
                public void onComponentDroppedAway() {
                    requestDeleteComponent(component);
                    render();
                }

                @Override
                public void onComponentExpandRight() {
                    // Make sure the component can fit in the matrix
                    if ((component.getY()+component.getYSpan()+1)>componentMatrix.length) {
                        return;
                    }

                    List<Component> toBeDeleted = new ArrayList<>();

                    // Check if the expansion is valid
                    for (int jCol = col; jCol < (col + component.getYSpan()+1); jCol++) {
                        for (int jRow = row; jRow < (row + component.getXSpan()); jRow++) {
                            if (componentMatrix.length > jCol && componentMatrix[0].length > jRow) {
                                if (componentMatrix[jCol][jRow] != null && !component.equals(componentMatrix[jCol][jRow])) {
                                    toBeDeleted.add(componentMatrix[jCol][jRow]);
                                }
                            }
                        }
                    }

                    // If something has to be deleted, ask the user
                    if (toBeDeleted.size() > 0) {
                        if (!requestOverrideComponentsDialog(toBeDeleted.size())) {  // DONT OVERWRITE
                            return;
                        }
                    }

                    // Delete the components
                    for (Component delComponent : toBeDeleted) {
                        requestDeleteComponent(delComponent);
                    }

                    // Increase the size
                    component.setYSpan(component.getYSpan()+1);

                    // Notify the listener
                    if (onComponentSelectedListener != null) {
                        onComponentSelectedListener.onEditComponentRequested(component);
                    }

                    render();
                }

                @Override
                public void onComponentExpandBottom() {
                    // Make sure the component can fit in the matrix
                    if ((component.getX()+component.getXSpan()+1)>componentMatrix[0].length) {
                        return;
                    }

                    List<Component> toBeDeleted = new ArrayList<>();

                    // Check if the expansion is valid
                    for (int jCol = col; jCol < (col + component.getYSpan()); jCol++) {
                        for (int jRow = row; jRow < (row + component.getXSpan()+1); jRow++) {
                            if (componentMatrix.length > jCol && componentMatrix[0].length > jRow) {
                                if (componentMatrix[jCol][jRow] != null && !component.equals(componentMatrix[jCol][jRow])) {
                                    toBeDeleted.add(componentMatrix[jCol][jRow]);
                                }
                            }
                        }
                    }

                    // If something has to be deleted, ask the user
                    if (toBeDeleted.size() > 0) {
                        if (!requestOverrideComponentsDialog(toBeDeleted.size())) {  // DONT OVERWRITE
                            return;
                        }
                    }

                    // Delete the components
                    for (Component delComponent : toBeDeleted) {
                        requestDeleteComponent(delComponent);
                    }

                    // Increase the size
                    component.setXSpan(component.getXSpan()+1);

                    // Notify the listener
                    if (onComponentSelectedListener != null) {
                        onComponentSelectedListener.onEditComponentRequested(component);
                    }

                    render();
                }

                @Override
                public void onComponentShrinkLeft() {
                    // Make sure the component can fit in the matrix
                    if (component.getYSpan() <= 1) {
                        return;
                    }

                    // Decrease the size
                    component.setYSpan(component.getYSpan()-1);

                    // Notify the listener
                    if (onComponentSelectedListener != null) {
                        onComponentSelectedListener.onEditComponentRequested(component);
                    }

                    render();
                }

                @Override
                public void onComponentShrinkUp() {
                    // Make sure the component can fit in the matrix
                    if (component.getXSpan() <= 1) {
                        return;
                    }

                    // Decrease the size
                    component.setXSpan(component.getXSpan()-1);

                    // Notify the listener
                    if (onComponentSelectedListener != null) {
                        onComponentSelectedListener.onEditComponentRequested(component);
                    }

                    render();
                }
            });
        }

        // Set up the drag and drop
        current.setOnComponentDragListener(new DragButton.OnComponentDragListener() {
            @Override
            public boolean onComponentDropped(Component newComponent) {
                List<Component> toBeDeleted = new ArrayList<>();

                // Add the cells that the component touches if has multiple span
                for (int jCol = col; jCol < (col + newComponent.getYSpan()); jCol++) {
                    for (int jRow = row; jRow < (row + newComponent.getXSpan()); jRow++) {
                        if (componentMatrix.length > jCol && componentMatrix[0].length > jRow) {
                            if (componentMatrix[jCol][jRow] != null) {
                                if (componentMatrix[jCol][jRow] == null) {
                                    toBeDeleted.add(componentMatrix[jCol][jRow]);
                                }else{
                                    if (!newComponent.getItem().equals(componentMatrix[jCol][jRow].getItem())) {
                                        toBeDeleted.add(componentMatrix[jCol][jRow]);
                                    }
                                }
                            }
                        }
                    }
                }

                // If the component will overwrite some buttons, ask for confirmation
                if (toBeDeleted.size() > 0) {
                    if (!requestOverrideComponentsDialog(toBeDeleted.size())) {  // DONT OVERWRITE
                        return false;
                    }
                }

                // Delete all components
                for (Component delComponent : toBeDeleted) {
                    requestDeleteComponent(delComponent);
                }

                // Change the component coordinates
                newComponent.setX(row);
                newComponent.setY(col);

                componentMatrix[col][row] = newComponent;

                // Notify the listener
                if (onComponentSelectedListener != null) {
                    onComponentSelectedListener.onNewComponentRequested(newComponent);
                }

                render();

                return true;
            }
        });

        // Set up the span
        int colSpan = 1;
        int rowSpan = 1;
        if (component != null) {
            colSpan = component.getYSpan();
            rowSpan = component.getXSpan();
        }

        // Add the component to the grid
        this.add(current, col, row, colSpan, rowSpan);

        return true;
    }

    private void removeButtonFromGrid(int col, int row) {
        // Remove the component from the grid
        Node node = getNodeFromGridPane(this, col, row);
        while (node != null) {
            this.getChildren().removeAll(node);
            node = getNodeFromGridPane(this, col, row);
        }
    }

    public void onComponentClicked(int col, int row) {
        Component component = componentMatrix[col][row];
        if (component == null) {  // Clicked on empty space
            if (sectionType == SectionType.LAUNCHPAD) {  // LAUNCHPAD SECTION
                requestApplicationSelect(col, row);
            } else if (sectionType == SectionType.SHORTCUTS) {  // LAUNCHPAD SHORTCUTS
                requestShortcutSelect(col, row);
            }

        } else {  // Clicked on active component
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

                    componentMatrix[col][row] = component;

                    // Notify the listener
                    if (onComponentSelectedListener != null) {
                        onComponentSelectedListener.onNewComponentRequested(component);
                    }

                    render();
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
            ShortcutDialogStage stage = new ShortcutDialogStage(shortcutIconManager, new ShortcutDialogStage.OnShortcutListener() {
                @Override
                public void onShortcutSelected(String shortcut, String name, ShortcutIcon icon) {
                    // Create the component
                    ShortcutItem item = new ShortcutItem();
                    item.setShortcut(shortcut);
                    item.setTitle(name);

                    // If an icon is specified, save the id
                    if (icon != null) {
                        item.setIconID(icon.getId());
                    }

                    Component component = new Component();
                    component.setItem(item);
                    component.setX(row);
                    component.setY(col);
                    component.setXSpan(1);
                    component.setYSpan(1);

                    componentMatrix[col][row] = component;

                    // Notify the listener
                    if (onComponentSelectedListener != null) {
                        onComponentSelectedListener.onNewComponentRequested(component);
                    }

                    render();
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

    private boolean requestOverrideComponentsDialog(int number) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Overwrite Button(s)");
        alert.setHeaderText("Are you sure you want to overwrite these buttons?");
        alert.setContentText("If you proceed, " + number + " button(s) will be deleted.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {  // Overwrite
            return true;
        }else{  // Don't overwite
            return false;
        }
    }

    public ShortcutIconManager getShortcutIconManager() {
        return shortcutIconManager;
    }

    public void setShortcutIconManager(ShortcutIconManager shortcutIconManager) {
        this.shortcutIconManager = shortcutIconManager;
    }

    public void setForceDiscardSpan(boolean forceDiscardSpan) {
        this.forceDiscardSpan = forceDiscardSpan;
    }

    public interface OnComponentSelectedListener {
        void onNewComponentRequested(Component component);
        void onDeleteComponentRequested(Component component);
        void onEditComponentRequested(Component component);
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

    private void requestDeleteComponent(Component component) {
        // Delete the component from the matrix
        componentMatrix[component.getY()][component.getX()] = null;

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
                    AppButton appButton = new AppButton(component, application, applicationManager);
                    return appButton;
                }
            } else if (component.getItem() instanceof ShortcutItem) {  // SHORTCUT ITEM
                ShortcutItem appItem = (ShortcutItem) component.getItem();
                ShortcutIcon shortcutIcon = null;
                if (appItem.getIconID() != null) {
                    shortcutIcon = shortcutIconManager.getIcon(appItem.getIconID());
                }
                return new ShortcutButton(component, shortcutIcon, shortcutIconManager);
            }
        }

        // No component found, return the empty button
        return new EmptyButton();
    }

    public void setHeight(int height) {
        this.setPrefHeight(height);
        this.setMaxHeight(height);
        this.setMinHeight(height);
    }

    public void setWidth(int width) {
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
