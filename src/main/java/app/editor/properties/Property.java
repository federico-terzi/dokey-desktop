package app.editor.properties;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import section.model.AppItem;
import section.model.Component;
import section.model.ShortcutItem;
import system.model.ApplicationManager;
import system.sicons.ShortcutIconManager;

public class Property extends VBox {
    protected Component associatedComponent;
    protected ApplicationManager applicationManager;

    public Property(Component associatedComponent, ApplicationManager applicationManager) {
        this.associatedComponent = associatedComponent;
        this.applicationManager = applicationManager;
    }

    public static Property getPropertyForComponent(Component component, ApplicationManager applicationManager, ShortcutIconManager shortcutIconManager) {
        if (component.getItem() instanceof AppItem) {
            ApplicationProperty property = new ApplicationProperty(component, applicationManager);
            return property;
        }else if (component.getItem() instanceof ShortcutItem) {
            ShortcutProperty property = new ShortcutProperty(component, shortcutIconManager);
            return property;
        }

        return null;
    }
}
