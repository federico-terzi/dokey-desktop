package app.editor.model.item_actions;

import app.editor.components.ComponentGrid;
import app.editor.stages.SystemDialogStage;
import app.editor.stages.WebLinkDialogStage;
import section.model.Component;
import section.model.SystemItem;
import section.model.WebLinkItem;

import java.io.IOException;
import java.util.ResourceBundle;

public class SystemItemAction extends ItemAction {
    public SystemItemAction(ComponentGrid componentGrid, ResourceBundle resourceBundle) {
        super(4,resourceBundle.getString("system"),
                resourceBundle.getString("add_system"),"/assets/shutdown.png" ,componentGrid, resourceBundle);
    }

    @Override
    public void requestAddItem(int col, int row, OnActionCompletedListener listener) {
        try {
            SystemDialogStage stage = new SystemDialogStage(resourceBundle, new SystemDialogStage.OnSystemItemListener() {
                @Override
                public void onSystemItemSelected(SystemItem item) {
                    // Create the component
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
            SystemDialogStage stage = new SystemDialogStage(resourceBundle, new SystemDialogStage.OnSystemItemListener() {
                @Override
                public void onSystemItemSelected(SystemItem item) {
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
            stage.setSystemItem((SystemItem) component.getItem());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
