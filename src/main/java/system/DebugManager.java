package system;

import system.model.ApplicationManager;
import system.storage.StorageManager;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to dispatch debug commands, useful to
 * debug the situation
 */
public class DebugManager {
    private ApplicationManager appManager;
    private StorageManager storageManager;

    // This map will hold all the possible commands
    private Map<String, OnCommandListener> commands = new HashMap<>();

    public DebugManager(ApplicationManager appManager, StorageManager storageManager) {
        this.appManager = appManager;
        this.storageManager = storageManager;

        // Register the commands
        commands.put("dir", () -> {  // OPEN DOKEY FOLDER
           appManager.open(storageManager.getStorageDir().getAbsolutePath());
        });
        commands.put("e", () -> {  // OPEN DOKEY EDITOR
            BroadcastManager.getInstance().sendBroadcast(BroadcastManager.OPEN_CONTROL_PANEL_REQUEST_EVENT, null);
        });
    }

    interface OnCommandListener {
        void invokeCommand();
    }

    /**
     * Invoke the passed command
     * @param command the command to dispatch
     */
    public void invokeCommand(String command) {
        if (commands.containsKey(command)) {
            commands.get(command).invokeCommand();
        }
    }
}
