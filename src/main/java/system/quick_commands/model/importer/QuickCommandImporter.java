package system.quick_commands.model.importer;

import json.JSONArray;
import json.JSONObject;
import json.JSONTokener;
import system.ApplicationPathResolver;
import system.model.ApplicationManager;
import system.quick_commands.QuickCommand;
import system.quick_commands.QuickCommandManager;
import system.quick_commands.model.actions.QuickAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to import quick commands.
 */
public class QuickCommandImporter extends Importer {
    private File commandFile;
    private QuickCommandManager quickCommandManager;

    private Map<QuickAction.Type, ImportAgent> importAgents = new HashMap<>();

    public QuickCommandImporter(File commandFile, QuickCommandManager quickCommandManager,
                                   ApplicationManager applicationManager) {
        super(new ApplicationPathResolver(applicationManager));
        this.commandFile = commandFile;
        this.quickCommandManager = quickCommandManager;

        registerImportAgents();
    }

    /**
     * Import the commands.
     * @return true if succeeded, false otherwise.
     */
    public boolean importCommands() {
        // Initialize the application path resolver
        applicationPathResolver.load();

        // Read the content of the file
        try {
            FileInputStream fis = new FileInputStream(commandFile);
            JSONTokener tokener = new JSONTokener(fis);
            JSONObject jsonContent = new JSONObject(tokener);
            JSONArray commandsArray = jsonContent.getJSONArray("commands");

            // Load the commands
            List<QuickCommand> commands = new ArrayList<>();
            commandsArray.forEach(json -> {
                QuickCommand command = QuickCommand.fromJson((JSONObject) json);
                commands.add(command);
            });

            // Analyze which commands can be imported and which not
            List<QuickCommand> toBeRemoved = new ArrayList<>();
            for (QuickCommand command : commands) {
                if (importAgents.containsKey(command.getAction().getType())) {
                    boolean result = importAgents.get(command.getAction().getType()).convertAction(command.getAction());
                    if (!result) {
                        toBeRemoved.add(command);
                        continue;
                    }

                    // If the command has the same id of an already existing command, mark is as removable
                    if (quickCommandManager.getCommandFromID(command.getId()) != null) {
                        toBeRemoved.add(command);
                        continue;
                    }

                    // If the command id is the same of an already existing command, change it by appending "Copy".
                    if (quickCommandManager.getCommand(command.getCommand()) != null) {
                        command.setCommand(command.getCommand()+"Copy");
                    }
                }
            }

            // Remove all commands
            commands.removeAll(toBeRemoved);

            // Add all the commands
            for (QuickCommand command : commands) {
                quickCommandManager.saveQuickCommand(command);
            }

            // Reload them
            quickCommandManager.loadCommands();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Initialize and register the import agents.
     */
    private void registerImportAgents() {
        importAgents.put(QuickAction.Type.WEB_LINK, new WebLinkActionImportAgent(this));
        importAgents.put(QuickAction.Type.DOKEY, new DokeyActionImportAgent(this));
        importAgents.put(QuickAction.Type.FOLDER, new FolderActionImportAgent(this));
        importAgents.put(QuickAction.Type.APP, new ApplicationActionImportAgent(this));
    }


}
