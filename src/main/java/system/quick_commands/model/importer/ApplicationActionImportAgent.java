package system.quick_commands.model.importer;

import system.quick_commands.model.actions.ApplicationAction;
import system.quick_commands.model.actions.QuickAction;

/**
 * Used to import application actions.
 */
public class ApplicationActionImportAgent extends ImportAgent {
    protected ApplicationActionImportAgent(Importer importer) {
        super(importer);
    }

    @Override
    public boolean convertAction(QuickAction action) {
        ApplicationAction appAction = (ApplicationAction) action;

        // Get the new path for this system
        String newExecutablePath = importer.applicationPathResolver.searchApp(appAction.getExecutablePath());

        // If the application was found, set the executable path
        if (newExecutablePath != null) {
            appAction.setExecutablePath(newExecutablePath);
            return true;
        }

        return false;
    }
}
