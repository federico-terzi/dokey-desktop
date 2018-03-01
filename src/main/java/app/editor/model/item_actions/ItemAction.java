package app.editor.model.item_actions;

import app.editor.components.ComponentButton;
import app.editor.components.ComponentGrid;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import section.model.Component;

import java.util.ResourceBundle;

/**
 * This class represents an action associated with an Item.
 */
public abstract class ItemAction {
    private int contextMenuOrder;
    protected String title;
    protected String message;
    protected String iconPath;
    protected ComponentGrid componentGrid;
    protected ResourceBundle resourceBundle;

    public ItemAction(int contextMenuOrder, String title, String message, String iconPath, ComponentGrid componentGrid, ResourceBundle resourceBundle) {
        this.contextMenuOrder = contextMenuOrder;
        this.title = title;
        this.message = message;
        this.iconPath = iconPath;
        this.componentGrid = componentGrid;
        this.resourceBundle = resourceBundle;
    }

    /**
     * @return the MenuItem associated with this action.
     */
    public MenuItem getContextMenuItem() {
        MenuItem menuItem = new MenuItem(message);
        Image menuItemImage = new Image(ComponentButton.class.getResourceAsStream(iconPath), 32, 32, true, true);
        ImageView menuItemImageView = new ImageView(menuItemImage);
        menuItem.setGraphic(menuItemImageView);
        return menuItem;
    }

    public abstract void requestAddItem(int col, int row, OnActionCompletedListener listener);
    public abstract void requestEditItem(Component component, OnActionCompletedListener listener);

    public interface OnActionCompletedListener {
        void onActionCompleted(Component component);
        void onActionCanceled();
    }

    public int getContextMenuOrder() {
        return contextMenuOrder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
}
