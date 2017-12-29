package app.editor.components;

import app.editor.listeners.OnComponentClickListener;
import app.editor.model.Direction;
import app.editor.model.ScreenOrientation;
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
    protected ScreenOrientation screenOrientation;

    private Component[][] componentMatrix;
    private Component[][] fillMatrix;
    private DragButton[][] buttonMatrix;
    private boolean forceDiscardSpan = false;

    public ComponentGrid(ApplicationManager applicationManager, ShortcutIconManager shortcutIconManager, Component[][] componentMatrix, ScreenOrientation screenOrientation) {
        super();
        this.applicationManager = applicationManager;
        this.shortcutIconManager = shortcutIconManager;
        this.componentMatrix = componentMatrix;
        this.screenOrientation = screenOrientation;

        render();

        setupConstraints();
    }

    protected int getOrientedRowCount() {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return componentMatrix[0].length;
        } else {
            return componentMatrix.length;
        }
    }

    protected int getOrientedColCount() {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return componentMatrix.length;
        } else {
            return componentMatrix[0].length;
        }
    }

    protected int getOrientedYSpan(Component component) {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return component.getYSpan();
        } else {
            return component.getXSpan();
        }
    }

    protected int getOrientedXSpan(Component component) {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return component.getXSpan();
        } else {
            return component.getYSpan();
        }
    }

    protected int getOrientedCol(int col, int row) {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return col;
        } else {
            return row;
        }
    }

    protected int getOrientedRow(int col, int row) {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return row;
        } else {
            return componentMatrix.length - 1 - col;  // Number of columns - 1 - col
        }
    }

    protected int getOrientedColSpanFactor() {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return 1;
        } else {
            return 1;
        }
    }

    protected int getOrientedRowSpanFactor() {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return 1;
        } else {
            return -1;
        }
    }

    protected int getOriginalCol(int col, int row) {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return col;
        } else {
            return componentMatrix.length - 1 - row;  // Columns - 1 - row
        }
    }

    protected int getOriginalRow(int col, int row) {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return row;
        } else {
            return col;
        }
    }

    private void setupConstraints() {
        for (int rowIndex = 0; rowIndex < getOrientedRowCount(); rowIndex++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS); // allow row to grow
            rc.setFillHeight(true); // ask nodes to fill height for row
            rc.setPercentHeight(100);
            // other settings as needed...
            getRowConstraints().add(rc);
        }
        for (int colIndex = 0; colIndex < getOrientedColCount(); colIndex++) {
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

        // Initialize the fill matrix
        fillMatrix = new Component[componentMatrix.length][componentMatrix[0].length];

        // Initialize the button matrix
        buttonMatrix = new DragButton[componentMatrix.length][componentMatrix[0].length];

        // Add all the components
        for (int col = 0; col < componentMatrix.length; col++) {
            for (int row = 0; row < componentMatrix[0].length; row++) {
                // These are the projected col and row based on the rotation
                int rotatedCol = getOrientedCol(col, row);
                int rotatedRow = getOrientedRow(col, row);
                int rotatedColSpanFactor = getOrientedColSpanFactor();
                int rotatedRowSpanFactor = getOrientedRowSpanFactor();

                if (componentMatrix[col][row] != null) {
                    // if force discard span is specified, all the blocks will lose their span
                    if (forceDiscardSpan) {
                        componentMatrix[col][row].setYSpan(1);
                        componentMatrix[col][row].setXSpan(1);
                    }

                    // Fill the span matrix
                    for (int jCol = col; jCol < (col + componentMatrix[col][row].getYSpan()); jCol++) {
                        for (int jRow = row; jRow < (row + componentMatrix[col][row].getXSpan()); jRow++) {
                            if (componentMatrix.length > jCol && componentMatrix[0].length > jRow) {
                                fillMatrix[jCol][jRow] = componentMatrix[col][row];
                            }
                        }
                    }

                    addComponentToGrid(rotatedCol, rotatedRow, rotatedColSpanFactor, rotatedRowSpanFactor, componentMatrix[col][row]);
                } else {
                    // Make sure in this position there isn't any spanned component
                    if (fillMatrix[col][row] == null) {
                        // Add the empty button
                        addComponentToGrid(rotatedCol, rotatedRow, rotatedColSpanFactor, rotatedRowSpanFactor, null);
                    }
                }
            }
        }
    }

    public boolean addComponentToGrid(int rotatedCol, int rotatedRow, int rotatedColSpanFactor, int rotatedRowSpanFactor, Component component) {
        // Get the original indexes in the matrix
        int col = getOriginalCol(rotatedCol, rotatedRow);
        int row = getOriginalRow(rotatedCol, rotatedRow);

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
                    resetDragSelection();
                    requestDeleteComponent(component);
                    render();
                }

                @Override
                public void onComponentExpandRight() {
                    switch (screenOrientation) {
                        case PORTRAIT:
                            resizeComponent(component, Direction.RIGHT);
                            break;
                        case LANDSCAPE:
                            resizeComponent(component, Direction.BOTTOM);
                            break;
                    }
                }

                @Override
                public void onComponentExpandBottom() {
                    switch (screenOrientation) {
                        case PORTRAIT:
                            resizeComponent(component, Direction.BOTTOM);
                            break;
                        case LANDSCAPE:
                            resizeComponent(component, Direction.LEFT);
                            break;
                    }
                }

                @Override
                public void onComponentShrinkLeft() {
                    switch (screenOrientation) {
                        case PORTRAIT:
                            resizeComponent(component, Direction.LEFT);
                            break;
                        case LANDSCAPE:
                            resizeComponent(component, Direction.TOP);
                            break;
                    }
                }

                @Override
                public void onComponentShrinkUp() {
                    switch (screenOrientation) {
                        case PORTRAIT:
                            resizeComponent(component, Direction.TOP);
                            break;
                        case LANDSCAPE:
                            resizeComponent(component, Direction.RIGHT);
                            break;
                    }
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
                            if (fillMatrix[jCol][jRow] != null && !newComponent.getItem().equals(fillMatrix[jCol][jRow].getItem())) {
                                toBeDeleted.add(fillMatrix[jCol][jRow]);
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

            @Override
            public boolean onComponentDropping(Component component) {
                resetDragSelection();
                activateDragSelection(col, row, component);
                return false;
            }
        });

        // Set up the span
        int colSpan = 1;
        int rowSpan = 1;
        if (component != null) {
            colSpan = getOrientedYSpan(component);
            rowSpan = getOrientedXSpan(component);
        }

        // Add the button to the matrix, also include the span
        for (int jCol = col; jCol < (col + colSpan); jCol++) {
            for (int jRow = row; jRow < (row + rowSpan); jRow++) {
                if (buttonMatrix.length > jCol && buttonMatrix[0].length > jRow) {
                    buttonMatrix[jCol][jRow] = current;
                }
            }
        }

        // Adjust the grid position based on the orientation
        int gridCol = rotatedCol;
        int gridRow = rotatedRow;
        if (rotatedColSpanFactor < 0) {
            gridCol -= (colSpan - 1);
            if (gridCol < 0) {
                colSpan = colSpan + gridCol;
                gridCol = 0;
            }
        }
        if (rotatedRowSpanFactor < 0) {
            gridRow -= (rowSpan - 1);
            if (gridRow < 0) {
                rowSpan = rowSpan + gridRow;
                gridRow = 0;
            }
        }

        // Add the component to the grid
        this.add(current, gridCol, gridRow, colSpan, rowSpan);

        return true;
    }

    private void moveComponent(Component component, Direction direction) {
        // Populate the final move based on the given direction
        int finalColMove = 0;
        int finalRowMove = 0;
        switch (direction) {
            case TOP:
                finalRowMove--;
                break;
            case LEFT:
                finalColMove--;
                break;
            case BOTTOM:
                finalRowMove++;
                break;
            case RIGHT:
                finalColMove++;
                break;
        }

        // Make sure the component stays in the grid
        if ((component.getX() + finalRowMove) < 0 || (component.getY() + finalColMove) < 0 ||
                (component.getX() + finalRowMove) >= componentMatrix[0].length ||
                (component.getY() + finalColMove) >= componentMatrix.length) {
            return;
        }

        int initialCol = component.getY();
        int initialRow = component.getX();
        int finalCol = component.getY() + finalColMove;
        int finalRow = component.getX() + finalRowMove;

        List<Component> toBeDeleted = new ArrayList<>();

        // Check if the expansion is valid
        for (int jCol = finalCol; jCol < (finalCol + component.getYSpan()); jCol++) {
            for (int jRow = finalRow; jRow < (finalRow + component.getXSpan()); jRow++) {
                if (componentMatrix.length > jCol && componentMatrix[0].length > jRow) {
                    if (fillMatrix[jCol][jRow] != null && !component.equals(fillMatrix[jCol][jRow])) {
                        toBeDeleted.add(fillMatrix[jCol][jRow]);
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

        // Delete the previous one
        componentMatrix[initialCol][initialRow] = null;

        // Change the position
        component.setX(finalRow);
        component.setY(finalCol);

        // Save the new one
        componentMatrix[finalCol][finalRow] = component;

        // Notify the listener
        if (onComponentSelectedListener != null) {
            onComponentSelectedListener.onEditComponentRequested(component);
        }

        render();
    }

    private void resizeComponent(Component component, Direction direction) {
        // Populate the final span based on the given direction
        int finalColSpan = 0;
        int finalRowSpan = 0;
        switch (direction) {
            case TOP:
                finalRowSpan--;
                break;
            case LEFT:
                finalColSpan--;
                break;
            case BOTTOM:
                finalRowSpan++;
                break;
            case RIGHT:
                finalColSpan++;
                break;
        }

        // To resize a component below the 1x1 size, the component must
        // be moved beforehand to accomodate the stretch
        if ((component.getXSpan() + finalRowSpan) < 1 || (component.getYSpan() + finalColSpan) < 1) {
            moveComponent(component, direction);
            // Reset the final span to a positive number
            finalColSpan = Math.abs(finalColSpan);
            finalRowSpan = Math.abs(finalRowSpan);
        }

        // Make sure the component doesn't exceed the matrix size
        if ((component.getX() + component.getXSpan() + finalRowSpan) > componentMatrix[0].length ||
                (component.getY() + component.getYSpan() + finalColSpan) > componentMatrix.length) {
            return;
        }

        int col = component.getY();
        int row = component.getX();

        List<Component> toBeDeleted = new ArrayList<>();

        // Check if the expansion is valid
        for (int jCol = col; jCol < (col + component.getYSpan() + finalColSpan); jCol++) {
            for (int jRow = row; jRow < (row + component.getXSpan() + finalRowSpan); jRow++) {
                if (componentMatrix.length > jCol && componentMatrix[0].length > jRow) {
                    if (fillMatrix[jCol][jRow] != null && !component.equals(fillMatrix[jCol][jRow])) {
                        toBeDeleted.add(fillMatrix[jCol][jRow]);
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
        component.setXSpan(component.getXSpan() + finalRowSpan);
        component.setYSpan(component.getYSpan() + finalColSpan);

        // Notify the listener
        if (onComponentSelectedListener != null) {
            onComponentSelectedListener.onEditComponentRequested(component);
        }

        render();
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
        } else {  // Don't overwite
            return false;
        }
    }

    private void activateDragSelection(int col, int row, Component component) {
        for (int jCol = col; jCol < (col + component.getYSpan()); jCol++) {
            for (int jRow = row; jRow < (row + component.getXSpan()); jRow++) {
                if (jCol < buttonMatrix.length && jRow < buttonMatrix[0].length &&
                        buttonMatrix[jCol][jRow] != null) {
                    boolean hasOverwriteDanger = !(buttonMatrix[jCol][jRow] instanceof EmptyButton) &&
                            !fillMatrix[jCol][jRow].equals(component);
                    buttonMatrix[jCol][jRow].setDragDestination(true, hasOverwriteDanger);
                }
            }
        }
    }

    public void resetDragSelection() {
        for (int jCol = 0; jCol < (buttonMatrix.length); jCol++) {
            for (int jRow = 0; jRow < (buttonMatrix[0].length); jRow++) {
                if (buttonMatrix[jCol][jRow] != null) {
                    buttonMatrix[jCol][jRow].setDragDestination(false, false);
                }
            }
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

    public OnComponentSelectedListener getOnComponentSelectedListener() {
        return onComponentSelectedListener;
    }

    public void setOnComponentSelectedListener(OnComponentSelectedListener onComponentSelectedListener) {
        this.onComponentSelectedListener = onComponentSelectedListener;
    }
}
