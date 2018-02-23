package system.section.importer;

import section.model.Item;

/**
 * This abstract class represents the entity responsible of analyzing
 * and converting an item.
 */
public abstract class ImportAgent {
    protected Importer importer;

    protected ImportAgent(Importer importer) {
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

    /**
     * Convert the given item based on the settings specified in the Importer.
     * @param item the item to convert.
     */
    public abstract void convertItem(Item item);
}
