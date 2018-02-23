package system.section.importer;

import system.model.ApplicationManager;

/**
 * This class should give the ImportAgents access to resources like the ApplicationManager
 * and also set up some options.
 */
public abstract class Importer {
    private ApplicationManager appManager;

    // If this is true, the "compatibility mode" check will be set to true by default
    // This happens for example when a section with CMD shortcuts is given to a Windows host
    // the compatibility mode will replace all the CMD with CONTROL.
    private boolean shouldRequireCompatibilityMode = false;

    protected Importer(ApplicationManager appManager) {
        this.appManager = appManager;
    }

    public boolean isShouldRequireCompatibilityMode() {
        return shouldRequireCompatibilityMode;
    }

    public void setShouldRequireCompatibilityMode(boolean shouldRequireCompatibilityMode) {
        this.shouldRequireCompatibilityMode = shouldRequireCompatibilityMode;
    }

    public ApplicationManager getAppManager() {
        return appManager;
    }

    public void setAppManager(ApplicationManager appManager) {
        this.appManager = appManager;
    }
}
