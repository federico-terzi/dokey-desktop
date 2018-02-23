package system.section.importer;

import section.model.Item;

/**
 * This class analyzes the WebLinkItem to make sure it is valid.
 */
public class WebLinkImportAgent extends ImportAgent {
    protected WebLinkImportAgent(Importer importer) {
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
