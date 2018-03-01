package app.editor.model.item_actions;

import app.editor.components.ComponentGrid;
import javafx.stage.DirectoryChooser;
import section.model.Component;
import section.model.FolderItem;

import java.io.File;
import java.util.ResourceBundle;

public class FolderItemAction extends ItemAction {
    public FolderItemAction(ComponentGrid componentGrid, ResourceBundle resourceBundle) {
        super(2, resourceBundle.getString("folder"),
                resourceBundle.getString("add_folder"), "/assets/folder.png", componentGrid, resourceBundle);
    }

    @Override
    public void requestAddItem(int col, int row, OnActionCompletedListener listener) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(resourceBundle.getString("choose_the_folder"));
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

            if (listener != null) {
                listener.onActionCompleted(component);
            }
        }else{
            if (listener != null) {
                listener.onActionCanceled();
            }
        }
    }

    @Override
    public void requestEditItem(Component component, OnActionCompletedListener listener) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(resourceBundle.getString("choose_the_folder"));
        File selectedDirectory = chooser.showDialog(null);

        FolderItem item = (FolderItem) component.getItem();

        if (selectedDirectory != null) {
            // Create the component
            item.setPath(selectedDirectory.getAbsolutePath());
            item.setTitle(selectedDirectory.getName());

            // Notify the edit
            if (listener != null) {
                listener.onActionCompleted(component);
            }
        }else{
            if (listener != null) {
                listener.onActionCanceled();
            }
        }
    }
}
