package app.quickcommands;

import app.quickcommands.controllers.CommandEditorController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import section.model.Item;
import system.ResourceUtils;
import system.quick_commands.QuickCommand;
import system.quick_commands.QuickCommandManager;

import java.io.IOException;
import java.util.ResourceBundle;

public class CommandEditorStage extends Stage {
    private QuickCommandManager quickCommandManager;
    private CommandEditorController controller;
    private ResourceBundle resourceBundle;

    private QuickCommand currentCommand = null;  // If null, the user is adding a new command. When different
                                                 // from null, it means the user is modifying an existing one.
    private Item currentItem = null;  // Null until the user selects an action or is editing an existing command.

    public CommandEditorStage(QuickCommandManager quickCommandManager, ResourceBundle resourceBundle,
                              OnCommandEditorCloseListener onCommandEditorCloseListener) throws IOException {
        this.quickCommandManager = quickCommandManager;
        this.resourceBundle = resourceBundle;

        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/command_editor.fxml").toURI().toURL());
        fxmlLoader.setResources(resourceBundle);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        this.setTitle(resourceBundle.getString("quick_commands"));
        this.setScene(scene);
        this.getIcons().add(new Image(CommandEditorStage.class.getResourceAsStream("/assets/icon.png")));
        scene.getStylesheets().add(ResourceUtils.getResource("/css/main.css").toURI().toString());

        controller = (CommandEditorController) fxmlLoader.getController();

        // Disable save button initially
        controller.saveBtn.setDisable(true);

        // Editor text field listener to avoid illegal characters in command name
        controller.commandTextField.textProperty().addListener(((observable, oldValue, command) -> {
            boolean valid = true;

            // Check if it contains a space
            if (command.contains(" "))
                valid = false;

            // Make sure the command starts with :
            if (!command.startsWith(":"))
                valid = false;

            // Make sure the string is alpha numeric
            if (command.length() > 1 && !StringUtils.isAlphanumeric(command.substring(1)))
                valid = false;

            // If it is not valid, return to the previous value.
            if (!valid) {
                controller.commandTextField.setText(oldValue);
            }

            // Check if can be saved
            validateSave();
        }));
        controller.nameTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
            // Check if can be saved
            validateSave();
        }));

        controller.saveBtn.setOnAction((event) -> saveCommand());

        // Close window listener
        setOnCloseRequest(event -> {
            if (onCommandEditorCloseListener != null) {
                onCommandEditorCloseListener.onClosed();
            }
        });
    }

    /**
     * Save the current command
     */
    private void saveCommand() {
        // An item must be present to save the command
        if (currentItem == null)
            return;

        // If this is a new command, create it with a random ID.
        if (currentCommand == null)
            currentCommand = new QuickCommand(true);

        // Populate the fields
        currentCommand.setCommand(controller.commandTextField.getText());
        currentItem.setTitle(controller.nameTextField.getText());
        currentCommand.setAction(currentItem);

        // Save it
        quickCommandManager.saveQuickCommand(currentCommand);
    }

    /**
     * Check if the current input is valid and enable the save button if so.
     */
    private void validateSave() {
        boolean valid = true;

        if (currentItem == null)
            valid = false;

        controller.saveBtn.setDisable(!valid);
    }

    public interface OnCommandEditorCloseListener {
        void onClosed();
    }
}
