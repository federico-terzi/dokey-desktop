package system.section.importer;

import section.model.Item;

/**
 * This abstract class represents the entity responsible of analyzing
 * and converting an item.
 */
public abstract class ImportAgent {
    protected SectionImporter importer;

    protected ImportAgent(SectionImporter importer) {
        this.importer = importer;
    }

    /**
     * Analyze the given item to check if it is valid.
     * If not valid, it will set the "isValid" attribute of an item
     * to false.
     * @param item the item to analyze.
     * @return true if the item is valid, false otherwise.
     */
    public abstract boolean analyzeItem(Item item);
}
