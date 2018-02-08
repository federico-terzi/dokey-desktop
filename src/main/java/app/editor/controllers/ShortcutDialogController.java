package app.editor.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import net.model.KeyboardKeys;

public class ShortcutDialogController {

    public Button iconBtn;

    // Special key buttons
    public Button ctrlBtn;
    public Button altBtn;
    public Button metaBtn;
    public Button escBtn;
    public Button enterBtn;
    public Button delBtn;
    public Button shiftBtn;
    public Button tabBtn;

    public Button[] getSpecialBtns(){
        return new Button[]{ctrlBtn, altBtn, metaBtn, escBtn, enterBtn, delBtn, shiftBtn, tabBtn};
    }
    public KeyboardKeys[] specialKeys = new KeyboardKeys[]{KeyboardKeys.VK_CONTROL, KeyboardKeys.VK_ALT, KeyboardKeys.VK_META,
                                        KeyboardKeys.VK_ESCAPE, KeyboardKeys.VK_ENTER, KeyboardKeys.VK_DELETE, KeyboardKeys.VK_SHIFT,
                                        KeyboardKeys.VK_TAB};

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
