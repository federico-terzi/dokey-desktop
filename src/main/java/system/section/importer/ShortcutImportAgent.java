package system.section.importer;

import section.model.Item;
import section.model.ShortcutItem;
import utils.OSValidator;

/**
 * This class analyzes the ShortcutItem to make sure it is valid.
 */
public class ShortcutImportAgent extends ImportAgent {
    protected ShortcutImportAgent(Importer importer) {
        super(importer);
    }

    @Override
    public boolean analyzeItem(Item item) {
        // Cast the item
        ShortcutItem shortcutItem = (ShortcutItem) item;

        // If a shortcut received in Mac contains the WIN key it is invalid and
        // cannot be converted.
        if (OSValidator.isMac() && shortcutItem.getShortcut().contains("WIN")) {
            return false;
        }

        return true;
    }

    @Override
    public void convertItem(Item item) {
        // Cast the item
        ShortcutItem shortcutItem = (ShortcutItem) item;

        // If the OS is windows, convert all the CMD to CTRL
        if (OSValidator.isWindows() && shortcutItem.getShortcut().contains("CMD")) {
            String newShortcut = shortcutItem.getShortcut().replace("CMD", "CTRL");
            shortcutItem.setShortcut(newShortcut);
        }else if (OSValidator.isMac() && shortcutItem.getShortcut().contains("CTRL")) { // If the OS is mac, convert all the CTRL to CMD
            String newShortcut = shortcutItem.getShortcut().replace("CTRL", "CMD");
            shortcutItem.setShortcut(newShortcut);
        }
    }
}
