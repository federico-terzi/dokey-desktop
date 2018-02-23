package system.section.importer;

import section.model.Item;
import section.model.FolderItem;
import utils.OSValidator;

import java.io.File;

/**
 * This class analyzes the FolderItem to make sure it is valid.
 */
public class FolderImportAgent extends ImportAgent {
    protected FolderImportAgent(Importer importer) {
        super(importer);
    }

    @Override
    public boolean analyzeItem(Item item) {
        // Cast the item
        FolderItem folderItem = (FolderItem) item;

        // Check if the folder actually exists in the current PC
        File folder = new File(folderItem.getPath());
        return folder.isDirectory();
    }

    @Override
    public void convertItem(Item item) {
        // No actions needed
    }
}
