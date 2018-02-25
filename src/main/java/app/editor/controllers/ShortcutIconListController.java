package app.editor.controllers;

import app.editor.listcells.ShortcutIconListCell;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import system.ShortcutIcon;

public class ShortcutIconListController {
    public TextField searchTextField;
    public Button cancelBtn;
    public Button selectBtn;
    public ListView shortcutListView;

    public void initialize() {
        shortcutListView.setCellFactory(new Callback<ListView<ShortcutIcon>, ListCell<ShortcutIcon>>() {
            @Override
            public ListCell<ShortcutIcon> call(ListView<ShortcutIcon> param) {
                return new ShortcutIconListCell();
            }
        });
    }
}
