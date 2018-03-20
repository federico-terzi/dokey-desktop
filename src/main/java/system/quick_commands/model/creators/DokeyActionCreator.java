package system.quick_commands.model.creators;

import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import system.quick_commands.model.DependencyResolver;
import system.quick_commands.model.actions.DokeyAction;
import system.quick_commands.model.actions.QuickAction;
import system.quick_commands.model.actions.WebLinkAction;

import java.net.URL;
import java.util.ResourceBundle;

public class DokeyActionCreator extends QuickActionCreator {
    private ChoiceBox<DokeyAction.DokeyActionType> choiceBox;

    public DokeyActionCreator(DependencyResolver resolver, ResourceBundle resourceBundle) {
        super(QuickAction.Type.DOKEY, resolver, resourceBundle);
    }

    @Override
    public void createActionBox(VBox box, OnActionModifiedListener listener) {
        choiceBox = new ChoiceBox<>();
        choiceBox.setConverter(new StringConverter<DokeyAction.DokeyActionType>() {
            @Override
            public String toString(DokeyAction.DokeyActionType object) {
                return object.getDescription(resourceBundle);
            }

            @Override
            public DokeyAction.DokeyActionType fromString(String string) {
                return null;
            }
        });
        choiceBox.setItems(FXCollections.observableArrayList(DokeyAction.DokeyActionType.values()));
        choiceBox.setMaxWidth(Double.MAX_VALUE);
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            DokeyAction action = new DokeyAction();
            action.setDokeyActionType(newValue);
            if (listener != null)
                listener.onActionModified(action);
        });
        choiceBox.getSelectionModel().select(0);

        box.getChildren().add(choiceBox);
    }

    @Override
    public void renderActionBox(QuickAction action) {
        if (action != null)
            choiceBox.getSelectionModel().select(((DokeyAction) action).getDokeyActionType());
    }

    @Override
    public String getDisplayText() {
        return resourceBundle.getString("dokey_control");
    }
}
