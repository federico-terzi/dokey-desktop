package app.editor.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ShortcutDialogController {

    public Button iconBtn;
    @FXML
    private Button cancelBtn;

    @FXML
    private Button selectBtn;

    @FXML
    private TextField shortcutTextField;

    @FXML
    private Button clearShortcutBtn;

    @FXML
    private TextField nameTextField;

    public Button getCancelBtn() {
        return cancelBtn;
    }

    public TextField getShortcutTextField() {
        return shortcutTextField;
    }

    public Button getSelectBtn() {
        return selectBtn;
    }

    public Button getClearShortcutBtn() {
        return clearShortcutBtn;
    }

    public TextField getNameTextField() {
        return nameTextField;
    }
}
