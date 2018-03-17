package system.quick_commands;

import json.JSONException;
import json.JSONObject;
import json.JSONTokener;
import system.CacheManager;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class is used to manage quick commands
 */
public class QuickCommandManager {
    public static final String QUICK_COMMANDS_DIR_NAME = "quickcommands";

    private List<QuickCommand> commands = new ArrayList<>(30);
    private Map<String, QuickCommand> commandMap = new HashMap<>();

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    public QuickCommandManager() {

    }

    /**
     * Request to load all the quick commands, asynchronously
     */
    public void requestQuickCommands() {
        new Thread(() -> {
            loadCommands();
        }).start();
    }

    /**
     * Load all the quick commands, synchronously
     */
    public void loadCommands() {
        commands = new ArrayList<>(30);
        commandMap = new HashMap<>();

        // Get the quickcommands directory
        File userCommandsDir = CacheManager.getInstance().getQuickCommandsDir();

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
     * Save the given quick command to file
     * @param quickCommand the QuickCommand to save
     * @return true if succeeded, false otherwise.
     */
    public boolean saveQuickCommand(QuickCommand quickCommand) {
        // Obtain the quick command file
        File quickCommandFile = new File(CacheManager.getInstance().getQuickCommandsDir(), quickCommand.getId()+".json");

        // Save the quick command
        return writeSectionToFile(quickCommand, quickCommandFile);
    }

    /**
     * Write the given quick command to the given file
     *
     * @param quickCommand the QuickCommand to save.
     * @param dest    the destination File.
     * @return true if succeeded, false otherwise.
     */
    public synchronized boolean writeSectionToFile(QuickCommand quickCommand, File dest) {
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
}
