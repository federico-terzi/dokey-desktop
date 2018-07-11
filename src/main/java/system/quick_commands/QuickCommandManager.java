package system.quick_commands;

import json.JSONException;
import json.JSONObject;
import json.JSONTokener;
import system.StorageManager;
import system.quick_commands.model.actions.DokeyAction;
import system.quick_commands.model.actions.WebLinkAction;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class is used to manage quick commands
 */
public class QuickCommandManager {
    public static final String QUICK_COMMANDS_DIR_NAME = "quickcommands";

    private List<QuickCommand> commands = new ArrayList<>(30);
    private Map<String, QuickCommand> commandMap = new HashMap<>();

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();
    private ResourceBundle resourceBundle;

    public QuickCommandManager(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * Request to load all the quick commands, asynchronously
     */
    public void requestQuickCommands() {
        new Thread(() -> loadCommands()).start();
    }

    /**
     * Load all the quick commands, synchronously
     */
    public void loadCommands() {
        commands = new ArrayList<>(30);
        commandMap = new HashMap<>();

        // Get the quickcommands directory
        File userCommandsDir = StorageManager.getInstance().getCommandDir();

        // Go through all user command files
        for (File commandFile : userCommandsDir.listFiles()) {
            // Skip hidden files
            if (commandFile.isHidden())
                continue;

            // Get the command from the file
            QuickCommand currentCommand = getCommandFromFile(commandFile);
            if (currentCommand != null) {
                commands.add(currentCommand);
                commandMap.put(currentCommand.getCommand(), currentCommand);
            }
        }

        // Initialize default commands
        initializeDefaultCommands();
    }

    /**
     * Initialize default commands
     */
    private void initializeDefaultCommands() {
        boolean modified = false;

        if (!commandMap.containsKey(":editor")) {
            QuickCommand command = new QuickCommand(true);
            command.setName(resourceBundle.getString("open_editor"));
            DokeyAction action = new DokeyAction();
            action.setDokeyActionType(DokeyAction.DokeyActionType.OPEN_EDITOR);
            command.setAction(action);
            command.setCommand(":editor");
            saveQuickCommand(command);
            modified = true;
        }

        if (!commandMap.containsKey(":commands")) {
            QuickCommand command = new QuickCommand(true);
            command.setName(resourceBundle.getString("open_commands"));
            DokeyAction action = new DokeyAction();
            action.setDokeyActionType(DokeyAction.DokeyActionType.OPEN_COMMANDS);
            command.setAction(action);
            command.setCommand(":commands");
            saveQuickCommand(command);
            modified = true;
        }

        if (!commandMap.containsKey(":settings")) {
            QuickCommand command = new QuickCommand(true);
            command.setName(resourceBundle.getString("open_settings"));
            DokeyAction action = new DokeyAction();
            action.setDokeyActionType(DokeyAction.DokeyActionType.OPEN_SETTINGS);
            command.setAction(action);
            command.setCommand(":settings");
            saveQuickCommand(command);
            modified = true;
        }

        if (!commandMap.containsKey(":docs")) {
            QuickCommand command = new QuickCommand(true);
            command.setName(resourceBundle.getString("get_help"));
            WebLinkAction action = new WebLinkAction();
            action.setUrl("https://dokey.io/docs/");
            command.setAction(action);
            command.setCommand(":docs");
            saveQuickCommand(command);
            modified = true;
        }

        // If a command was added, reload all the commands
        if (modified) {
            loadCommands();
        }
    }

    /**
     * Read a quick command from the given file
     * @param commandFile the command File
     * @return the QuickCommand read from the file
     */
    public QuickCommand getCommandFromFile(File commandFile) {
        // Read the content
        try {
            FileInputStream fis = new FileInputStream(commandFile);
            JSONTokener tokener = new JSONTokener(fis);
            JSONObject jsonContent = new JSONObject(tokener);

            // Create the command by de-serialization
            QuickCommand command = QuickCommand.fromJson(jsonContent);

            fis.close();

            return command;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @return the List of quick commands
     */
    public List<QuickCommand> getCommands() {
        return commands;
    }

    /**
     * @return the List of quick commands that start with the given query
     */
    public List<QuickCommand> searchCommands(String query, int limit) {
        return commands.stream().filter((quickCommand -> quickCommand.getCommand().toLowerCase().startsWith(query)))
                .limit(limit).sorted(new Comparator<QuickCommand>() {
                    @Override
                    public int compare(QuickCommand o1, QuickCommand o2) {
                        return o1.getCommand().compareTo(o2.getCommand());
                    }
                }).collect(Collectors.toList());
    }

    /**
     * Get the command with the given command string.
     * Note: the command string must be in this format ":command",
     * starting with :
     * @param command the command to search
     * @return the corresponding QuickCommand if found, null otherwise.
     */
    public QuickCommand getCommand(String command) {
        return commandMap.get(command);
    }

    /**
     * Get the command with the given command id.
     * @param id the command id.
     * @return the corresponding QuickCommand if found, null otherwise.
     */
    public QuickCommand getCommandFromID(String id) {
        for (QuickCommand command : commands) {
            if (command.getId().equals(id)) {
                return command;
            }
        }
        return null;
    }

    /**
     * @param quickCommand
     * @return the File associated with the given command.
     */
    private File getCommandFile(QuickCommand quickCommand) {
        // Obtain the quick command file
        return new File(StorageManager.getInstance().getCommandDir(), quickCommand.getId()+".json");
    }

    /**
     * Save the given quick command to file
     * @param quickCommand the QuickCommand to save
     * @return true if succeeded, false otherwise.
     */
    public boolean saveQuickCommand(QuickCommand quickCommand) {
        File quickCommandFile = getCommandFile(quickCommand);

        // Save the quick command
        return writeCommandToFile(quickCommand, quickCommandFile);
    }

    /**
     * Write the given quick command to the given file
     *
     * @param quickCommand the QuickCommand to save.
     * @param dest    the destination File.
     * @return true if succeeded, false otherwise.
     */
    public synchronized boolean writeCommandToFile(QuickCommand quickCommand, File dest) {
        try {
            // Write the json quickCommand to the file
            FileOutputStream fos = new FileOutputStream(dest);
            PrintWriter pw = new PrintWriter(fos);
            JSONObject json = quickCommand.json();  // Get the quickCommand json
            pw.write(json.toString());
            pw.close();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Delete the given command from the filesystem
     * @param command
     * @return true if succeeded, false otherwise.
     */
    public synchronized boolean deleteCommand(QuickCommand command) {
        File commandFile = getCommandFile(command);
        return commandFile.delete();
    }
}
