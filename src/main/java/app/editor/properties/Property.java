package app.editor.properties;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import section.model.AppItem;
import section.model.Component;

public class Property extends HBox {
    protected Component associatedComponent;

    public Property(Component associatedComponent) {
        this.associatedComponent = associatedComponent;

        setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }

    public static Property getPropertyForComponent(Component component) {
        if (component.getItem() instanceof AppItem) {
            ApplicationProperty property = new ApplicationProperty(component);
            return property;
        }

        return null;
    }
}
