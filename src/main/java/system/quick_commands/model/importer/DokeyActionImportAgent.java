package system.quick_commands.model.importer;

import system.quick_commands.model.actions.ApplicationAction;
import system.quick_commands.model.actions.QuickAction;

/**
 * Used to import dokey actions.
 */
public class DokeyActionImportAgent extends ImportAgent {
    protected DokeyActionImportAgent(Importer importer) {
        super(importer);
    }

    @Override
    public boolean convertAction(QuickAction action) {
        return true;
    }
}
