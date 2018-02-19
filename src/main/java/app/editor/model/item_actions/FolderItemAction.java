package app.editor.model.item_actions;

import app.editor.components.ComponentGrid;
import app.editor.stages.ShortcutDialogStage;
import javafx.stage.DirectoryChooser;
import section.model.Component;
import section.model.FolderItem;
import section.model.ShortcutItem;
import system.sicons.ShortcutIcon;

import java.io.File;
import java.io.IOException;

public class FolderItemAction extends ItemAction {
    public FolderItemAction(ComponentGrid componentGrid) {
        super(2,"Folder", "Add Folder", "/assets/folder.png", componentGrid);
    }

    @Override
    public void requestAddItem(int col, int row, OnActionCompletedListener listener) {
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

            if (listener != null) {
                listener.onActionCompleted(component);
            }
        }
    }

    @Override
    public void requestEditItem(Component component, OnActionCompletedListener listener) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose the Folder");
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
        }
    }
}
