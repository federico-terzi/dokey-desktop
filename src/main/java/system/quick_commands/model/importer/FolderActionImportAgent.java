package system.quick_commands.model.importer;

import system.quick_commands.model.actions.FolderAction;
import system.quick_commands.model.actions.QuickAction;

import java.io.File;

/**
 * Used to import folder actions.
 */
public class FolderActionImportAgent extends ImportAgent {
    protected FolderActionImportAgent(Importer importer) {
        super(importer);
    }

    @Override
    public boolean convertAction(QuickAction action) {
        FolderAction folderAction = (FolderAction) action;

        // Check if the folder actually exists in the current PC
        File folder = new File(folderAction.getPath());
        return folder.isDirectory();
    }
}
