package app.editor.model.item_actions;

import app.editor.components.ComponentGrid;
import app.editor.stages.ShortcutDialogStage;
import section.model.Component;
import section.model.ShortcutItem;
import system.ShortcutIcon;

import java.io.IOException;

public class ShortcutItemAction extends ItemAction {
    public ShortcutItemAction(ComponentGrid componentGrid) {
        super(1, "Shortcut", "Add Shortcut", "/assets/keyboard.png", componentGrid);
    }

    @Override
    public void requestAddItem(int col, int row, OnActionCompletedListener listener) {
        try {
            ShortcutDialogStage stage = new ShortcutDialogStage(componentGrid.getShortcutIconManager(), new ShortcutDialogStage.OnShortcutListener() {
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

                    if (listener != null) {
                        listener.onActionCompleted(component);
                    }
                }

                @Override
                public void onCanceled() {
                    if (listener != null) {
                        listener.onActionCanceled();
                    }
                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void requestEditItem(Component component, OnActionCompletedListener listener) {
        try {
            ShortcutDialogStage stage = new ShortcutDialogStage(componentGrid.getShortcutIconManager(), new ShortcutDialogStage.OnShortcutListener() {
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

                    component.setItem(item);

                    if (listener != null) {
                        listener.onActionCompleted(component);
                    }
                }

                @Override
                public void onCanceled() {
                    if (listener != null) {
                        listener.onActionCanceled();
                    }
                }
            });
            stage.setShortcutItem((ShortcutItem) component.getItem());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
