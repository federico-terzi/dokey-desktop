package app.editor.controllers;

import app.editor.listcells.SystemListCell;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import section.model.SystemCommands;
import section.model.SystemItem;

public class SystemDialogController {
    public CheckBox confirmCheckBox;
    public Button cancelBtn;
    public Button selectBtn;
    public ListView systemListView;

    public void initialize() {
        systemListView.setCellFactory(new Callback<ListView<SystemCommands>, ListCell<SystemCommands>>() {
            @Override
            public ListCell<SystemCommands> call(ListView<SystemCommands> param) {
                return new SystemListCell();
            }
        });
    }
}
