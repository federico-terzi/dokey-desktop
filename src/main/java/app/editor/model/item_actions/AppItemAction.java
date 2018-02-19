package app.editor.model.item_actions;

import app.editor.components.ComponentGrid;
import app.editor.stages.AppSelectDialogStage;
import section.model.AppItem;
import section.model.Component;
import section.model.ItemType;
import system.model.Application;

import java.io.IOException;

public class AppItemAction extends ItemAction {
    public AppItemAction(ComponentGrid componentGrid) {
        super(0,"Application", "Add Application", "/assets/launcher.png", componentGrid);
    }

    @Override
    public void requestAddItem(int col, int row, OnActionCompletedListener listener) {
        try {
            AppSelectDialogStage appSelectDialogStage = new AppSelectDialogStage(componentGrid.getApplicationManager(),
                    new AppSelectDialogStage.OnApplicationListener() {
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

                    if (listener != null) {
                        listener.onActionCompleted(component);
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

    @Override
    public void requestEditItem(Component component, OnActionCompletedListener listener) {
        try {
            AppSelectDialogStage appSelectDialogStage = new AppSelectDialogStage(componentGrid.getApplicationManager(), new AppSelectDialogStage.OnApplicationListener() {
                @Override
                public void onApplicationSelected(Application application) {
                    // Create the component
                    AppItem appItem = new AppItem();
                    appItem.setAppID(application.getExecutablePath());
                    appItem.setTitle(application.getName());
                    appItem.setItemType(ItemType.APP);

                    component.setItem(appItem);

                    if (listener != null) {
                        listener.onActionCompleted(component);
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
}
