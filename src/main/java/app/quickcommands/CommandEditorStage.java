package app.quickcommands;

import app.quickcommands.controllers.CommandEditorController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import system.ResourceUtils;
import system.model.ApplicationManager;
import system.quick_commands.QuickCommand;
import system.quick_commands.QuickCommandManager;
import system.quick_commands.model.DependencyResolver;
import system.quick_commands.model.actions.QuickAction;
import system.quick_commands.model.creators.ApplicationActionCreator;
import system.quick_commands.model.creators.QuickActionCreator;

import java.io.IOException;
import java.util.*;

public class CommandEditorStage extends Stage implements DependencyResolver {
    private QuickCommandManager quickCommandManager;
    private CommandEditorController controller;
    private ResourceBundle resourceBundle;
    private ApplicationManager applicationManager;

    // If null, the user is adding a new command. When different
    // from null, it means the user is modifying an existing one.
    private QuickCommand currentCommand = new QuickCommand(true);

    private Map<QuickAction.Type, QuickActionCreator> actionCreators = new HashMap<>();

    public CommandEditorStage(QuickCommandManager quickCommandManager, ResourceBundle resourceBundle,
                              ApplicationManager applicationManager,
                              OnCommandEditorCloseListener onCommandEditorCloseListener) throws IOException {
        this.quickCommandManager = quickCommandManager;
        this.resourceBundle = resourceBundle;
        this.applicationManager = applicationManager;

        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/command_editor.fxml").toURI().toURL());
        fxmlLoader.setResources(resourceBundle);
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        this.setTitle(resourceBundle.getString("quick_commands"));
        this.setScene(scene);
        this.getIcons().add(new Image(CommandEditorStage.class.getResourceAsStream("/assets/icon.png")));
        scene.getStylesheets().add(ResourceUtils.getResource("/css/main.css").toURI().toString());

        controller = (CommandEditorController) fxmlLoader.getController();

        renderFields();

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
                loadQuickCommand((QuickCommand) newValue);
            }
        }));

        // Add the command
        controller.addCommandBtn.setOnAction(event -> {
            resetFields();
        });

        // Clear action button
        controller.clearActionBtn.setOnAction(event -> {currentCommand.setAction(null); renderFields();});

        // Edit action button
        controller.editActionBtn.setOnAction(event -> {
            if (currentCommand.getAction() == null)
                return;

            // Get the associated action creator and trigger the edit dialog
            QuickActionCreator actionCreator = actionCreators.get(currentCommand.getAction().getType());
            if (actionCreator != null) {
                actionCreator.editAction(currentCommand.getAction(), onQuickActionListener);
            }
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
        TableColumn commandCol = new TableColumn("Command");
        commandCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<QuickCommand, String>, ObservableValue>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures<QuickCommand, String> param) {
                return new SimpleStringProperty(param.getValue().getCommand());
            }
        });
        TableColumn nameCol = new TableColumn("Name");
        nameCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<QuickCommand, String>, ObservableValue>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures<QuickCommand, String> param) {
                return new SimpleStringProperty(param.getValue().getName());
            }
        });
        TableColumn actionCol = new TableColumn("Action");
        actionCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<QuickCommand, String>, ObservableValue>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures<QuickCommand, String> param) {
                return new SimpleStringProperty(param.getValue().getAction().getDisplayText(CommandEditorStage.this, resourceBundle));
            }
        });

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
        QuickActionCreator appCreator = new ApplicationActionCreator(this, resourceBundle);
        actionCreators.put(appCreator.getActionType(), appCreator);
    }

    /**
     * Populate the add action button list with the action creators
     */
    private void populateActionCreators() {
        for (QuickActionCreator actionCreator : actionCreators.values()) {
            MenuItem menuItem = new MenuItem(actionCreator.getDisplayText());
            menuItem.setOnAction((event -> actionCreator.createAction(onQuickActionListener)));
            controller.addActionBtn.getItems().add(menuItem);
        }
    }

    /**
     * Used when adding or editing a quick action.
     */
    private QuickActionCreator.OnQuickActionListener onQuickActionListener = new QuickActionCreator.OnQuickActionListener() {
        @Override
        public void onQuickActionSelected(QuickAction action) {
            currentCommand.setAction(action);
            validateSave();
        }

        @Override
        public void onCanceled() {

        }
    };

    /**
     * Load the given quick command in the edit section.
     * @param command the command to visualize
     */
    private void loadQuickCommand(QuickCommand command) {
        currentCommand = command;
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

        if (currentCommand.getAction() != null) {
            controller.actionLabel.setText(currentCommand.getAction().getDisplayText(this, resourceBundle));

            controller.editActionBtn.setManaged(true);
            controller.editActionBtn.setVisible(true);
            controller.clearActionBtn.setManaged(true);
            controller.clearActionBtn.setVisible(true);
            controller.addActionBtn.setManaged(false);
            controller.addActionBtn.setVisible(false);
        }else{
            controller.actionLabel.setText("No action selected");  // TODO: i18n

            controller.editActionBtn.setManaged(false);
            controller.editActionBtn.setVisible(false);
            controller.clearActionBtn.setManaged(false);
            controller.clearActionBtn.setVisible(false);
            controller.addActionBtn.setManaged(true);
            controller.addActionBtn.setVisible(true);
        }

        // Check if can be saved
        validateSave();
    }

    /**
     * Reset all the fields
     */
    private void resetFields() {
        currentCommand = new QuickCommand(true);
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

        if (currentCommand.getAction() == null)
            valid = false;
        else if (currentCommand.getCommand().length() <= 1)
            valid = false;
        else if (quickCommandManager.getCommand(currentCommand.getCommand()) != null &&
                !quickCommandManager.getCommand(currentCommand.getCommand()).getId().equals(currentCommand.getId()))
            valid = false;

        controller.saveBtn.setDisable(!valid);
    }

    @Override
    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }

    public interface OnCommandEditorCloseListener {
        void onClosed();
    }
}
