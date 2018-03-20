package app.quickcommands.stages;

import app.quickcommands.controllers.CommandEditorController;
import app.utils.AbstractStage;
import app.utils.ConfirmationDialog;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import system.model.ApplicationManager;
import system.quick_commands.QuickCommand;
import system.quick_commands.QuickCommandManager;
import system.quick_commands.model.DependencyResolver;
import system.quick_commands.model.actions.QuickAction;
import system.quick_commands.model.creators.ApplicationActionCreator;
import system.quick_commands.model.creators.FolderActionCreator;
import system.quick_commands.model.creators.QuickActionCreator;
import system.quick_commands.model.creators.WebLinkActionCreator;

import java.io.IOException;
import java.util.*;

public class CommandEditorStage extends AbstractStage<CommandEditorController> {
    private QuickCommandManager quickCommandManager;
    private ResourceBundle resourceBundle;
    private ApplicationManager applicationManager;
    private DependencyResolver resolver;

    // The current command present in the bottom edit pane
    private QuickCommand currentCommand = new QuickCommand(true);

    // The type of action of the current command
    private QuickAction.Type currentActionType = null;

    private boolean isCurrentANewCommand = true;  // If true, the current command is a new one

    private Map<QuickAction.Type, QuickActionCreator> actionCreators = new HashMap<>();

    public CommandEditorStage(QuickCommandManager quickCommandManager, ResourceBundle resourceBundle,
                              ApplicationManager applicationManager, DependencyResolver resolver,
                              OnCommandEditorCloseListener onCommandEditorCloseListener) throws IOException {
        super(resourceBundle, "/layouts/command_editor.fxml", "/css/main.css", resourceBundle.getString("quick_commands"));

        this.quickCommandManager = quickCommandManager;
        this.resourceBundle = resourceBundle;
        this.applicationManager = applicationManager;
        this.resolver = resolver;

        resetFields();

        // Register all the action creators in the add button
        registerActionCreators();
        populateActionCreators();

        // Editor text field listener to avoid illegal characters in command name
        controller.commandTextField.textProperty().addListener(((observable, oldValue, command) -> {
            boolean valid = true;

            // Check if it contains a space
            if (command.contains(" "))
                valid = false;
            else if (!command.startsWith(":")) // Make sure the command starts with :
                valid = false;
            else if (command.length() > 1 && !StringUtils.isAlphanumeric(command.substring(1))) // Make sure the string is alpha numeric
                valid = false;

            // Make sure its valid
            if (valid) {
                currentCommand.setCommand(command);
            }

            renderFields();
        }));
        controller.nameTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
            currentCommand.setName(newValue);
            renderFields();
        }));

        // Item select listener
        controller.tableView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Copy the command
                QuickCommand commandCopy = QuickCommand.fromJson(((QuickCommand)newValue).json());
                loadQuickCommand((QuickCommand) commandCopy);
            }
        }));

        // Add the command
        controller.addCommandBtn.setOnAction(event -> {
            isCurrentANewCommand = true;
            resetFields();
            Platform.runLater(() -> controller.commandTextField.requestFocus());
        });
        // Remove the command
        controller.removeCommandBtn.setOnAction(event -> {
            ConfirmationDialog confirmationDialog = new ConfirmationDialog(resourceBundle.getString("delete_confirmation"),
                    resourceBundle.getString("items_delete_message"));

            confirmationDialog.success(() -> {
                ObservableList<QuickCommand> toBeRemoved = controller.tableView.getSelectionModel().getSelectedItems();

                for (QuickCommand command : toBeRemoved) {
                    quickCommandManager.deleteCommand(command);
                }

                resetFields();
                requestQuickCommandsList();
            });
        });

        // Clear action button
        controller.clearActionBtn.setOnAction(event -> {
            currentCommand.setAction(null);
            currentActionType = null;
            createActionBox();
            renderFields();
        });

        // Save button
        controller.saveBtn.setOnAction((event) -> saveCommand());

        // Close window listener
        setOnCloseRequest(event -> {
            if (onCommandEditorCloseListener != null) {
                onCommandEditorCloseListener.onClosed();
            }
        });

        // Configure table view and request command list
        configureTableView();
        requestQuickCommandsList();
    }

    /**
     * Configure the table view columns
     */
    private void configureTableView() {
        TableColumn commandCol = new TableColumn(resourceBundle.getString("command"));
        commandCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<QuickCommand, String>, ObservableValue>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures<QuickCommand, String> param) {
                return new SimpleStringProperty(param.getValue().getCommand());
            }
        });
        TableColumn nameCol = new TableColumn(resourceBundle.getString("name"));
        nameCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<QuickCommand, String>, ObservableValue>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures<QuickCommand, String> param) {
                return new SimpleStringProperty(param.getValue().getName());
            }
        });
        TableColumn actionCol = new TableColumn(resourceBundle.getString("action"));
        actionCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<QuickCommand, String>, ObservableValue>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures<QuickCommand, String> param) {
                return new SimpleStringProperty(param.getValue().getAction().getDisplayText(resolver, resourceBundle));
            }
        });
        controller.tableView.getSelectionModel().setSelectionMode(
                SelectionMode.MULTIPLE
        );

        controller.tableView.getColumns().addAll(commandCol, nameCol, actionCol);
    }

    /**
     * Request the quick commands list and populate the list view
     */
    private void requestQuickCommandsList() {
        new Thread(() -> {
            quickCommandManager.loadCommands();

            Platform.runLater(() -> {
                populateTableView(quickCommandManager.getCommands());
            });
        }).start();
    }

    /**
     * Populate the TableView with the quick commands
     */
    private void populateTableView(List<QuickCommand> commands) {
        ObservableList<QuickCommand> observableList = FXCollections.observableList(commands);
        controller.tableView.setItems(observableList);

        // Select the currentcommand in the list if present
        for (int i = 0; i<observableList.size(); i++) {
            if (observableList.get(i).getId().equals(currentCommand.getId())) {
                controller.tableView.getSelectionModel().select(i);
            }
        }
    }

    /**
     * Register all the action creators in the list
     */
    private void registerActionCreators() {
        QuickActionCreator appCreator = new ApplicationActionCreator(resolver, resourceBundle);
        actionCreators.put(appCreator.getActionType(), appCreator);

        QuickActionCreator urlCreator = new WebLinkActionCreator(resolver, resourceBundle);
        actionCreators.put(urlCreator.getActionType(), urlCreator);

        QuickActionCreator folderCreator = new FolderActionCreator(resolver, resourceBundle);
        actionCreators.put(folderCreator.getActionType(), folderCreator);
    }

    /**
     * Populate the add action button list with the action creators
     */
    private void populateActionCreators() {
        for (QuickActionCreator actionCreator : actionCreators.values()) {
            MenuItem menuItem = new MenuItem(actionCreator.getDisplayText());
            menuItem.setOnAction((event -> {
                currentCommand.setAction(null);  // Reset previous action
                currentActionType = actionCreator.getActionType();
                createActionBox();  // Inject the widgets
            }));
            controller.addActionBtn.getItems().add(menuItem);
        }
    }

    /**
     * Used to inject the widgets needed for the current action type
     */
    private void createActionBox() {
        controller.actionBox.getChildren().clear();  // Delete old content

        // If no action is specified, put a label
        if (currentActionType == null) {
            Label label = new Label(resourceBundle.getString("no_action_specified"));
            controller.actionBox.getChildren().add(label);
            return;
        }

        // Add the correct action box
        if (actionCreators.get(currentActionType) != null) {
            actionCreators.get(currentActionType).createActionBox(controller.actionBox, action -> {
                currentCommand.setAction(action);
                renderFields();
            });
        }
    }

    /**
     * Load the given quick command in the edit section.
     * @param command the command to visualize
     */
    private void loadQuickCommand(QuickCommand command) {
        currentCommand = command;
        isCurrentANewCommand = false;

        if (command.getAction() == null)
            currentActionType = null;
        else
            currentActionType = command.getAction().getType();

        createActionBox();

        renderFields();
    }

    /**
     * Render the fields to reflect the current command status
     */
    private void renderFields() {
        if (currentCommand == null)
            return;

        String commandText = ":";
        if (currentCommand.getCommand() != null)
            commandText = currentCommand.getCommand();

        controller.commandTextField.setText(commandText);
        controller.nameTextField.setText(currentCommand.getName());

        if (isCurrentANewCommand)
            controller.saveBtn.setText(resourceBundle.getString("add"));
        else
            controller.saveBtn.setText(resourceBundle.getString("save"));

        if (currentCommand.getAction() != null) {
            // Update action fields
            if (actionCreators.get(currentActionType) != null) {
                actionCreators.get(currentActionType).renderActionBox(currentCommand.getAction());
            }

            controller.addActionBtn.setManaged(false);
            controller.addActionBtn.setVisible(false);
            controller.clearActionBtn.setManaged(true);
            controller.clearActionBtn.setVisible(true);
        }else{
            controller.addActionBtn.setManaged(true);
            controller.addActionBtn.setVisible(true);
            controller.clearActionBtn.setManaged(false);
            controller.clearActionBtn.setVisible(false);
        }

        // Check if can be saved
        validateSave();
    }

    /**
     * Reset all the fields
     */
    private void resetFields() {
        currentCommand = new QuickCommand(true);
        currentActionType = null;
        createActionBox();
        renderFields();
    }


    /**
     * Save the current command
     */
    private void saveCommand() {
        // An action must be present to save the command
        if (currentCommand.getAction() == null)
            return;

        // If this is a new command, create it with a random ID.
        if (currentCommand == null)
            currentCommand = new QuickCommand(true);

        // Save it
        quickCommandManager.saveQuickCommand(currentCommand);

        // Reload the table view
        requestQuickCommandsList();
    }



    /**
     * Check if the current input is valid and enable the save button if so.
     */
    private void validateSave() {
        boolean valid = true;

        if (currentCommand.getAction() == null || currentCommand.getCommand() == null)
            valid = false;
        else if (currentCommand.getCommand().length() <= 1)
            valid = false;
        else if (quickCommandManager.getCommand(currentCommand.getCommand()) != null &&
                !quickCommandManager.getCommand(currentCommand.getCommand()).getId().equals(currentCommand.getId()))
            valid = false;

        controller.saveBtn.setDisable(!valid);
    }

    public interface OnCommandEditorCloseListener {
        void onClosed();
    }
}
