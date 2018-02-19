package app.editor.components;

import app.editor.model.ScreenOrientation;
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
import net.model.IconTheme;
import section.model.*;
import system.model.Application;
import system.model.ApplicationManager;
import system.sicons.ShortcutIcon;
import system.sicons.ShortcutIconManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ComponentGrid extends GridPane {

    private ApplicationManager applicationManager;
    private ShortcutIconManager shortcutIconManager;
    protected ScreenOrientation screenOrientation;
    private OnComponentSelectedListener onComponentSelectedListener;

    protected Component[][] componentMatrix;

    // This map will hold the association between the item type and the corresponding button class
    protected Map<ItemType, Class<? extends ComponentButton>> itemTypeClassMap = new HashMap<>();

    public ComponentGrid(ApplicationManager applicationManager, ShortcutIconManager shortcutIconManager, Component[][] componentMatrix, ScreenOrientation screenOrientation) {
        super();
        this.applicationManager = applicationManager;
        this.shortcutIconManager = shortcutIconManager;
        this.componentMatrix = componentMatrix;
        this.screenOrientation = screenOrientation;

        setupItemTypeClassMap();

        render();

        setupConstraints();
    }

    /**
     * Populate the map that will hold the association between an item and the corresponding button.
     */
    private void setupItemTypeClassMap() {
        itemTypeClassMap.put(ItemType.APP, AppButton.class);
        itemTypeClassMap.put(ItemType.SHORTCUT, ShortcutButton.class);
        itemTypeClassMap.put(ItemType.FOLDER, FolderButton.class);
        itemTypeClassMap.put(ItemType.WEB_LINK, WebLinkButton.class);
        itemTypeClassMap.put(ItemType.SYSTEM, SystemButton.class);
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
                }else{
                    addEmptyButtonToGridPane(col, row);
                }
            }
        }
    }

    private void addEmptyButtonToGridPane(int col, int row) {
        EmptyButton emptyButton = new EmptyButton(this);
        addButtonToGridPane(col, row, emptyButton);
    }

    /**
     * Add a component to the GridPane.
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
        DragButton current = getButtonForComponent(component);

        // Set the context menu actions based on the type of button
        if (current instanceof ComponentButton) {
            ((ComponentButton) current).setOnComponentActionListener(new ComponentButton.OnComponentActionListener() {
                @Override
                public void onComponentEdit() {
                    render();
                    // Notify the listener
                    if (onComponentSelectedListener != null) {
                        onComponentSelectedListener.onEditComponentRequested(component);
                    }
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
            });
        }else if (current instanceof EmptyButton) {
            ((EmptyButton) current).setOnEmptyBtnActionListener(new EmptyButton.OnEmptyBtnActionListener() {
                @Override
                public void onAddApplication() {
                    requestApplicationSelect(col, row);
                }
                @Override
                public void onAddShortcut() {
                    requestShortcutSelect(col, row);
                }

                @Override
                public void onAddFolder() {
                    requestFolderSelect(col, row);
                }

                @Override
                public void onAddWebLink() {
                    requestWebLinkSelect(col, row);
                }

                @Override
                public void onAddSystem() {
                    requestSystemSelect(col, row);
                }
            });
        }

        // Set up the drag and drop
        current.setOnComponentDragListener(new DragButton.OnComponentDragListener() {
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
                    requestDeleteComponent(toBeDeleted.get());
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
                // TODO Selection

                //resetDragSelection();
                //activateDragSelection(col, row, component);
                return false;
            }
        });

        addButtonToGridPane(col, row, current);
    }

    private void addButtonToGridPane(int col, int row, DragButton button) {
        // Adjust the grid position based on the orientation
        int gridCol = getOrientedCol(col, row);
        int gridRow = getOrientedRow(col, row);

        // Add the component to the grid
        this.add(button, gridCol, gridRow, 1, 1);
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

    private void requestFolderSelect(int col, int row) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose the Folder");
        File selectedDirectory = chooser.showDialog(null);

        if (selectedDirectory != null) {
            // Create the component
            FolderItem item = new FolderItem();
            item.setPath(selectedDirectory.getAbsolutePath());
            item.setTitle(selectedDirectory.getName());

            Component component = new Component();
            component.setItem(item);
            component.setX(row);
            component.setY(col);

            componentMatrix[col][row] = component;

            // Notify the listener
            if (onComponentSelectedListener != null) {
                onComponentSelectedListener.onNewComponentRequested(component);
            }

            render();
        }
    }

    private void requestWebLinkSelect(int col, int row) {
        try {
            WebLinkDialogStage stage = new WebLinkDialogStage(new WebLinkDialogStage.OnWebLinkListener() {
                @Override
                public void onWebLinkSelected(String url, String title, String imageUrl) {
                    // Create the component
                    WebLinkItem item = new WebLinkItem();
                    item.setUrl(url);
                    item.setTitle(title);
                    item.setIconID(imageUrl);

                    Component component = new Component();
                    component.setItem(item);
                    component.setX(row);
                    component.setY(col);

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

    private void requestSystemSelect(int col, int row) {
        try {
            SystemDialogStage stage = new SystemDialogStage(new SystemDialogStage.OnSystemItemListener() {
                @Override
                public void onSystemItemSelected(SystemItem item) {
                    // Create the component
                    Component component = new Component();
                    component.setItem(item);
                    component.setX(row);
                    component.setY(col);

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


    private boolean requestOverrideComponentsDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(ShortcutDialogStage.class.getResourceAsStream("/assets/icon.png")));
        alert.setTitle("Overwrite Button(s)");
        alert.setHeaderText("Are you sure you want to overwrite these buttons?");
        alert.setContentText("If you proceed, a button will be deleted.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {  // Overwrite
            return true;
        } else {  // Don't overwite
            return false;
        }
    }

    public void setShortcutIconManager(ShortcutIconManager shortcutIconManager) {
        this.shortcutIconManager = shortcutIconManager;
    }

    public interface OnComponentSelectedListener {
        void onNewComponentRequested(Component component);

        void onDeleteComponentRequested(Component component);

        void onEditComponentRequested(Component component);
    }

    private void requestDeleteComponent(Component component) {
        // Delete the component from the matrix
        componentMatrix[component.getY()][component.getX()] = null;

        // Notify the listener
        if (onComponentSelectedListener != null) {
            onComponentSelectedListener.onDeleteComponentRequested(component);
        }
    }

    private DragButton getButtonForComponent(Component component) {
        if (component == null)
            return null;

        // Make sure the item type is valid
        if (itemTypeClassMap.containsKey(component.getItem().getItemType())) {
            try {
                // Generate a DragButton instance based on the type
                return itemTypeClassMap.get(component.getItem().getItemType())
                        .getConstructor(ComponentGrid.class).newInstance(this, component);
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

    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }

    public ShortcutIconManager getShortcutIconManager() {
        return shortcutIconManager;
    }
}
