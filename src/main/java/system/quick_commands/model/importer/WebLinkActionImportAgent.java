package system.quick_commands.model.importer;

import system.quick_commands.model.actions.QuickAction;

/**
 * Used to import web link actions.
 */
public class WebLinkActionImportAgent extends ImportAgent {
    protected WebLinkActionImportAgent(Importer importer) {
        super(importer);
    }

    @Override
    public boolean convertAction(QuickAction action) {
        return true;
    }
}
