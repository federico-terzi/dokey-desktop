package system.section.importer;

import section.model.*;
import system.ApplicationPathResolver;
import system.model.ApplicationManager;
import system.section.SectionManager;
import utils.OSValidator;

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

    // This list will hold all the items
    private List<Item> items;

    // If this is set, when importing the section the related app id will be replaced with this one.
    private String overrideRelatedAppID = null;

    // This is set to false if the target app can't be found.
    private boolean hasTargetBeenFound = true;

    // If this is true, it means that the section has been created with another os.
    // Warn the user about possible problems.
    private boolean isCreatedWithAnotherOS = false;

    // PARAMETERS

    // If true, when importing the section all the invalid items will be deleted.
    private boolean deleteInvalidItems = true;

    // If true, when importing the section the items will be converted.
    private boolean compatibilityMode = false;

    private SectionManager sectionManager = new SectionManager();
    private HashMap<ItemType, ImportAgent> importAgents = new HashMap<>();

    public SectionImporter(File sectionFile, ApplicationManager appManager) {
        super(new ApplicationPathResolver(appManager));
        this.sectionFile = sectionFile;
    }

    /**
     * Analyze the given section file to find possible compatibility errors.
     * @throws SectionImportException if an error occurred while analyzing.
     */
    public void analyze() throws SectionImportException{
        // Load the sectionWrapper from file
        SectionWrapper sectionWrapper = sectionManager.importSectionFromFile(sectionFile);
        if (sectionWrapper == null) {
            throw new SectionImportException("Can't decode section from given file.");
        }

        // Get the section
        section = sectionWrapper.getSection();

        // Check if has been created with another os
        if (sectionWrapper.getOs() != null && !OSValidator.getOS().equals(sectionWrapper.getOs())) {
            isCreatedWithAnotherOS = true;
            shouldRequireCompatibilityMode = true;
        }

        // Reset the invalidItems list
        invalidItems = new ArrayList<>(100);

        // Initialize the items list
        items = new ArrayList<>(100);

        // Register the importAgents ( done here because initializing them can have an overhead,
        // and analyze() is usually run from another thread anyways ).
        registerImportAgents();

        // Load the application path resolver
        applicationPathResolver.load();

        // If the section is of type SHORTCUTS, the new path of the RelatedAppID must be found
        if (section.getSectionType() == SectionType.SHORTCUTS) {
            // Search for the new path
            String newRelatedAppID = applicationPathResolver.searchApp(section.getRelatedAppId());

            // If found, update the section related app id.
            if (newRelatedAppID != null) {
                section.setRelatedAppId(newRelatedAppID);
            }else{
                hasTargetBeenFound = false;
            }
        }

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
     * Converts all the items and saves the section to file
     */
    public void importSection() {
        // Convert all the items
        if (compatibilityMode) {
            for (Item item : items) {
                // Convert the item based on the type
                // Make sure the item type is valid
                if (importAgents.containsKey(item.getItemType())) {
                    importAgents.get(item.getItemType()).convertItem(item);
                }
            }
        }

        // Delete all the invalid items
        if (deleteInvalidItems) {
            for (Page page : section.getPages()) {
                List<Component> toBeDeleted = new ArrayList<>();
                for (Component component : page.getComponents()) {
                    if (invalidItems.contains(component.getItem())) {
                        toBeDeleted.add(component);
                    }
                }
                page.getComponents().removeAll(toBeDeleted);
            }
        }

        // Override the related app id if needed.
        if (overrideRelatedAppID != null) {
            section.setRelatedAppId(overrideRelatedAppID);
        }

        // Save the section
        sectionManager.saveSection(section);
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

    public String getOverrideRelatedAppID() {
        return overrideRelatedAppID;
    }

    public void setOverrideRelatedAppID(String overrideRelatedAppID) {
        this.overrideRelatedAppID = overrideRelatedAppID;
    }

    public boolean isDeleteInvalidItems() {
        return deleteInvalidItems;
    }

    public void setDeleteInvalidItems(boolean deleteInvalidItems) {
        this.deleteInvalidItems = deleteInvalidItems;
    }

    public boolean isCompatibilityMode() {
        return compatibilityMode;
    }

    public void setCompatibilityMode(boolean compatibilityMode) {
        this.compatibilityMode = compatibilityMode;
    }

    public boolean hasTargetBeenFound() {
        return hasTargetBeenFound;
    }

    public boolean isCreatedWithAnotherOS() {
        return isCreatedWithAnotherOS;
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
