package system.section.importer;

import section.model.AppItem;
import section.model.Item;
import system.ApplicationPathResolver;

/**
 * This class analyzes the AppItem to make sure it is valid.
 */
public class AppImportAgent extends ImportAgent {
    private ApplicationPathResolver applicationPathResolver;

    protected AppImportAgent(Importer importer) {
        super(importer);

        applicationPathResolver = importer.getApplicationPathResolver();
    }

    @Override
    public boolean analyzeItem(Item item) {
        // Cast the item
        AppItem appItem = (AppItem) item;

        // Search for the new executable path
        String newPath = applicationPathResolver.searchApp(appItem.getAppID());

        // If found, replace the old one.
        if (newPath != null) {
            // Replace the old path with the new one.
            appItem.setAppID(newPath);
            return true;
        }

        // Not found, mark it as invalid
        return false;
    }

    @Override
    public void convertItem(Item item) {
        // No actions needed
    }
}
