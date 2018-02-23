package system.section.importer;

import section.model.FolderItem;
import section.model.Item;

import java.io.File;

/**
 * This class analyzes the SystemItem to make sure it is valid.
 */
public class SystemImportAgent extends ImportAgent {
    protected SystemImportAgent(Importer importer) {
        super(importer);
    }

    @Override
    public boolean analyzeItem(Item item) {
        // No actions needed
        return true;
    }

    @Override
    public void convertItem(Item item) {
        // No actions needed
    }
}
