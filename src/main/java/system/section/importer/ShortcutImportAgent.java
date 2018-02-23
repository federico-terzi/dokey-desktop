package system.section.importer;

import section.model.AppItem;
import section.model.Item;
import section.model.ShortcutItem;
import system.model.Application;
import system.model.ApplicationManager;
import utils.OSValidator;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

        // If the OS is windows and the shortcut has "CMD" in it, set the compatibility mode.
        if (OSValidator.isWindows() && shortcutItem.getShortcut().contains("CMD")) {
            importer.setShouldRequireCompatibilityMode(true);
        }

        return true;
    }

    @Override
    public void convertItem(Item item) {
        // Cast the item
        ShortcutItem shortcutItem = (ShortcutItem) item;

        // If the compatibility mode is true and the OS is windows, convert all the CMD to CTRL
        if (OSValidator.isWindows() && shortcutItem.getShortcut().contains("CMD")) {
            String newShortcut = shortcutItem.getShortcut().replace("CMD", "CTRL");
            shortcutItem.setShortcut(newShortcut);
        }
    }
}
