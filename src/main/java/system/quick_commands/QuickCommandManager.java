package system.quick_commands;

import json.JSONException;
import json.JSONObject;
import json.JSONTokener;
import system.CacheManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to manage quick commands
 */
public class QuickCommandManager {
    public static final String QUICK_COMMANDS_DIR_NAME = "quickcommands";

    private List<QuickCommand> commands = new ArrayList<>(30);

    public QuickCommandManager() {

    }

    public void loadCommands() {
        commands = new ArrayList<>(30);

        // Get the quickcommands directory
        File userCommandsDir = CacheManager.getInstance().getQuickCommandsDir();

        // Go through all user section files
        for (File commandFile : userCommandsDir.listFiles()) {
            // Skip hidden files
            if (commandFile.isHidden())
                continue;

            // Get the section from the file
            QuickCommand currentCommand = getCommandFromFile(commandFile);
            if (currentCommand != null) {
                commands.add(currentCommand);
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
}
