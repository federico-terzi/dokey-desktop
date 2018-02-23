package system.section.importer;

import section.model.*;
import system.model.ApplicationManager;
import system.section.SectionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is used to import a section.
 */
public class SectionImporter extends Importer {
    private File sectionFile;

    private Section section = null;  // This will hold the loaded section

    // This list will hold all the items that are invalid in the system.
    // For example, an app that couldn't be found or a folder that doesn't exist.
    private List<Item> invalidItems;

    private SectionManager sectionManager = new SectionManager();
    private HashMap<ItemType, ImportAgent> importAgents = new HashMap<>();

    public SectionImporter(File sectionFile, ApplicationManager appManager) {
        super(appManager);
        this.sectionFile = sectionFile;
    }

    /**
     * Analyze the given section file to find possible compatibility errors.
     * @throws SectionImportException if an error occurred while analyzing.
     */
    public void analyze() throws SectionImportException{
        // Reset the invalidItems array
        invalidItems = new ArrayList<>(100);

        // Register the importAgents ( done here because initializing them can have an overhead,
        // and analyze() is usually run from another thread anyways ).
        registerImportAgents();

        // Load the section from file
        section = sectionManager.getSectionFromFile(sectionFile);
        if (section == null) {
            throw new SectionImportException("Can't decode section from given file.");
        }

        // This list will hold the items
        List<Item> items = new ArrayList<>(100);

        // Add all the items of the section to the list
        // Cycle through all pages
        for (Page page : section.getPages()) {
            // Cycle through all components
            for (Component component : page.getComponents()) {
                items.add(component.getItem());
            }
        }
        // Add also the bottom bar items
        items.addAll(section.getBottomBarItems());

        // Analyze them
        for (Item item : items) {
            // Analyze the item based on the type
            // Make sure the item type is valid
            if (importAgents.containsKey(item.getItemType())) {
                boolean result = importAgents.get(item.getItemType()).analyzeItem(item);
                if (!result) {  // Invalid item
                    item.setValid(false);
                    invalidItems.add(item);
                }else{
                    item.setValid(true);
                }
            }else{  // Item type not found, throw an exception
                throw new SectionImportException("The section contains an invalid item type.");
            }
        }
    }

    /**
     * Initialize and register the import agents.
     */
    private void registerImportAgents() {
        // Register the import agents
        importAgents.put(ItemType.APP, new AppImportAgent(this));
        importAgents.put(ItemType.SHORTCUT, new ShortcutImportAgent(this));
        importAgents.put(ItemType.FOLDER, new FolderImportAgent(this));
        importAgents.put(ItemType.WEB_LINK, new WebLinkImportAgent(this));
        importAgents.put(ItemType.SYSTEM, new SystemImportAgent(this));
    }

    public Section getSection() {
        return section;
    }

    public List<Item> getInvalidItems() {
        return invalidItems;
    }

    /**
     * If an exception occurs while importing the section, this error will be thrown.
     */
    public class SectionImportException extends Exception {
        public SectionImportException(String message) {
            super(message);
        }
    }
}
