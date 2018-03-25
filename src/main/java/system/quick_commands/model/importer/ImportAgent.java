package system.quick_commands.model.importer;

import system.quick_commands.model.actions.QuickAction;

/**
 * This abstract class represents the entity responsible of converting quick actions.
 */
public abstract class ImportAgent {
    protected Importer importer;

    protected ImportAgent(Importer importer) {
        this.importer = importer;
    }

    /**
     * Adapt the given action to the current system.
     * @param action the action to convert.
     * @return true if succeeded, false otherwise.
     */
    public abstract boolean convertAction(QuickAction action);
}
