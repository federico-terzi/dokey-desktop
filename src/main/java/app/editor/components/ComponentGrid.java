package app.editor.components;

import app.editor.model.ScreenOrientation;
import app.editor.model.item_actions.*;
import app.editor.stages.ShortcutDialogStage;
import app.editor.stages.AppSelectDialogStage;
import app.editor.stages.SystemDialogStage;
import app.editor.stages.WebLinkDialogStage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import section.model.*;
import system.model.Application;
import system.model.ApplicationManager;
import system.sicons.ShortcutIcon;
import system.sicons.ShortcutIconManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ComponentGrid extends GridPane {

    private ApplicationManager applicationManager;
    private ShortcutIconManager shortcutIconManager;
    protected ScreenOrientation screenOrientation;
    private OnComponentSelectedListener onComponentSelectedListener;

    protected Component[][] componentMatrix;

    // This map will hold the association between the item type and the corresponding button class
    protected Map<ItemType, Class<? extends ComponentButton>> itemTypeClassMap = new HashMap<>();

    // This mapp will hold the association between the item type and the corresponding actions
    protected Map<ItemType, ItemAction> itemTypeActions = new HashMap<>();
    private List<ItemAction> orderedTypeActions;  // Generated automatically, do not touch

    public ComponentGrid(ApplicationManager applicationManager, ShortcutIconManager shortcutIconManager, Component[][] componentMatrix, ScreenOrientation screenOrientation) {
        super();
        this.applicationManager = applicationManager;
        this.shortcutIconManager = shortcutIconManager;
        this.componentMatrix = componentMatrix;
        this.screenOrientation = screenOrientation;

        setupItemTypeClasses();
        setupItemTypeActions();

        render();

        setupConstraints();
    }

    /**
     * Populate the map that will hold the association between an item and the corresponding button.
     */
    private void setupItemTypeClasses() {
        itemTypeClassMap.put(ItemType.APP, AppButton.class);
        itemTypeClassMap.put(ItemType.SHORTCUT, ShortcutButton.class);
        itemTypeClassMap.put(ItemType.FOLDER, FolderButton.class);
        itemTypeClassMap.put(ItemType.WEB_LINK, WebLinkButton.class);
        itemTypeClassMap.put(ItemType.SYSTEM, SystemButton.class);
    }


    /**
     * Populate the map that will hold the association between an item and the corresponding actions.
     */
    private void setupItemTypeActions() {
        itemTypeActions.put(ItemType.APP, new AppItemAction(this));
        itemTypeActions.put(ItemType.SHORTCUT, new ShortcutItemAction(this));
        itemTypeActions.put(ItemType.FOLDER, new FolderItemAction(this));
        itemTypeActions.put(ItemType.SYSTEM, new SystemItemAction(this));
        itemTypeActions.put(ItemType.WEB_LINK, new WebLinkItemAction(this));

        // Create the ordered list
        orderedTypeActions = new ArrayList<>(itemTypeActions.values());
        Collections.sort(orderedTypeActions, new Comparator<ItemAction>() {
            @Override
            public int compare(ItemAction o1, ItemAction o2) {
                return Integer.compare(o1.getContextMenuOrder(), o2.getContextMenuOrder());
            }
        });
    }

    /**
     * Setup the GridPane constraint to have equally large buttons
     */
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

    /**
     * @return the row count based on the current screen orientation.
     */
    protected int getOrientedRowCount() {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return componentMatrix[0].length;
        } else {
            return componentMatrix.length;
        }
    }

    /**
     * @return the col count based on the current screen orientation.
     */
    protected int getOrientedColCount() {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return componentMatrix.length;
        } else {
            return componentMatrix[0].length;
        }
    }

    /**
     * Given the col and row in the component matrix, return the actual col in the grid based on
     * the screen orientation.
     *
     * @param col the col index in the component matrix.
     * @param row the row index in the component matrix.
     * @return the actual col in the grid based on the screen orientation.
     */
    protected int getOrientedCol(int col, int row) {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return col;
        } else {
            return row;
        }
    }

    /**
     * Given the col and row in the component matrix, return the actual row in the grid based on
     * the screen orientation.
     *
     * @param col the col index in the component matrix.
     * @param row the row index in the component matrix.
     * @return the actual row in the grid based on the screen orientation.
     */
    protected int getOrientedRow(int col, int row) {
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            return row;
        } else {
            return componentMatrix.length - 1 - col;  // Number of columns - 1 - col
        }
    }

    /**
     * Render the componentMatrix into buttons in the GridPane
     */
    public void render() {
        // Delete all the previous nodes
        getChildren().clear();

        // Add all the components
        for (int col = 0; col < componentMatrix.length; col++) {
            for (int row = 0; row < componentMatrix[0].length; row++) {
                if (componentMatrix[col][row] != null) {
                    addComponentToGridPane(componentMatrix[col][row]);
                } else {
                    addEmptyButtonToGridPane(col, row);
                }
            }
        }
    }

    /**
     * Add an empty button in the given position.
     *
     * @param col the col index in the component matrix.
     * @param row the row index in the component matrix.
     */
    private void addEmptyButtonToGridPane(int col, int row) {
        EmptyButton emptyButton = new EmptyButton(this, orderedTypeActions, new EmptyButton.OnEmptyBtnActionListener() {
            @Override
            public void onActionSelected(ItemAction action) {
                // Request to add the item
                action.requestAddItem(col, row, new ItemAction.OnActionCompletedListener() {
                    @Override
                    public void onActionCompleted(Component component) {
                        // Add the item
                        componentMatrix[col][row] = component;

                        // Notify the listener
                        if (onComponentSelectedListener != null) {
                            onComponentSelectedListener.onNewComponentRequested(component);
                        }

                        render();
                    }
                });
            }
        });
        addButtonToGridPane(col, row, emptyButton);
    }

    /**
     * Add a component to the GridPane.
     *
     * @param component the component to add.
     * @return
     */
    public void addComponentToGridPane(Component component) {
        // Make sure the component is not null
        if (component == null)
            return;

        // Extract the coordinates from the component
        int col = component.getY();
        int row = component.getX();

        // Get the current button
        ComponentButton current = getButtonForComponent(component);

        // Make sure a button is available for the given component
        if (current == null)
            return;

        // Set the context menu actions
        current.setOnComponentActionListener(new ComponentButton.OnComponentActionListener() {
            @Override
            public void onComponentEdit() {
                ItemAction action = itemTypeActions.get(component.getItem().getItemType());

                // Make sure an action exists
                if (action == null)
                    return;

                // Request to add the item
                action.requestEditItem(component, new ItemAction.OnActionCompletedListener() {
                    @Override
                    public void onActionCompleted(Component component) {
                        // Add the item
                        componentMatrix[col][row] = component;

                        // Notify the listener
                        if (onComponentSelectedListener != null) {
                            onComponentSelectedListener.onEditComponentRequested(component);
                        }

                        render();
                    }
                });
            }

            @Override
            public void onComponentDelete() {
                deleteComponent(component, true);
                render();
            }

            // When the component is dropped away, request the
            // deletion from the grid
            @Override
            public void onComponentDroppedAway() {
                deleteComponent(component, true);
                render();
            }
        });

        addButtonToGridPane(col, row, current);
    }

    /**
     * Add the given button to the GridPane, correcting the coordinates based on the screen orientation.
     * and adding the drag and drop listener.
     *
     * @param col    the col index in the component matrix.
     * @param row    the row index in the component matrix.
     * @param button the button to add.
     */
    private void addButtonToGridPane(int col, int row, DragButton button) {
        // Set up the drag and drop
        button.setOnComponentDragListener(new DragButton.OnComponentDragListener() {
            @Override
            public boolean onComponentDropped(Component newComponent) {
                Optional<Component> toBeDeleted = Optional.empty();

                // The component already present in the newComponent requested position. If null, the position is empty.
                Component alreadyPresentComponent = componentMatrix[col][row];

                if (alreadyPresentComponent != null && !newComponent.getItem().equals(alreadyPresentComponent.getItem())) {
                    toBeDeleted = Optional.of(alreadyPresentComponent);
                }

                // If the component will overwrite a button, ask for confirmation
                if (toBeDeleted.isPresent()) {
                    if (!requestOverrideComponentsDialog()) {  // DONT OVERWRITE
                        return false;
                    }

                    // Delete the component
                    deleteComponent(toBeDeleted.get(), false);
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
                // Determine if the dropping can cause an overwrite.
                boolean isDangerous = true;
                if (button instanceof EmptyButton) {
                    isDangerous = false;
                }

                // Select the component
                button.setDragDestination(true, isDangerous);
                return false;
            }
        });

        // Adjust the grid position based on the orientation
        int gridCol = getOrientedCol(col, row);
        int gridRow = getOrientedRow(col, row);

        // Add the component to the grid
        this.add(button, gridCol, gridRow, 1, 1);
    }

    /**
     * Delete the given component from the componentMatrix, and send a notification to the
     * associated listener.
     *
     * @param component      the component to delete.
     * @param notifyListener if true, the listener will be notified of the deletion.
     */
    private void deleteComponent(Component component, boolean notifyListener) {
        // Delete the component from the matrix
        componentMatrix[component.getY()][component.getX()] = null;

        // Notify the listener
        if (onComponentSelectedListener != null && notifyListener) {
            onComponentSelectedListener.onDeleteComponentRequested(component);
        }
    }

    /**
     * Show a Dialog to ask for delete confirmation.
     *
     * @return true if accepted, false otherwise.
     */
    private boolean requestOverrideComponentsDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(ShortcutDialogStage.class.getResourceAsStream("/assets/icon.png")));
        alert.setTitle("Overwrite Button");
        alert.setHeaderText("Are you sure you want to overwrite this button?");
        alert.setContentText("If you proceed, a button will be deleted.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {  // Overwrite
            return true;
        } else {  // Don't overwite
            return false;
        }
    }

    /**
     * Get the Button associated with the given component.
     *
     * @param component the component.
     * @return the Button associated with the given component, null in case of errors.
     */
    private ComponentButton getButtonForComponent(Component component) {
        if (component == null)
            return null;

        // Make sure the item type is valid
        if (itemTypeClassMap.containsKey(component.getItem().getItemType())) {
            try {
                // Generate a DragButton instance based on the type
                return itemTypeClassMap.get(component.getItem().getItemType())
                        .getConstructor(ComponentGrid.class, Component.class).newInstance(this, component);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        // Invalid type or error.
        return null;
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

    public interface OnComponentSelectedListener {
        void onNewComponentRequested(Component component);

        void onDeleteComponentRequested(Component component);

        void onEditComponentRequested(Component component);
    }

    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }

    public ShortcutIconManager getShortcutIconManager() {
        return shortcutIconManager;
    }

    public void setShortcutIconManager(ShortcutIconManager shortcutIconManager) {
        this.shortcutIconManager = shortcutIconManager;
    }
}
