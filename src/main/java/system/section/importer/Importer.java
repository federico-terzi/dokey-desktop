package system.section.importer;

import system.ApplicationPathResolver;

/**
 * This class should give the ImportAgents access to resources like the ApplicationManager
 * and also set up some options.
 */
public abstract class Importer {
    protected ApplicationPathResolver applicationPathResolver;

    // If this is true, the "compatibility mode" check will be set to true by default
    // This happens for example when a section with CMD shortcuts is given to a Windows host
    // the compatibility mode will replace all the CMD with CONTROL.
    protected boolean shouldRequireCompatibilityMode = false;

    protected Importer(ApplicationPathResolver applicationPathResolver) {
        this.applicationPathResolver = applicationPathResolver;
    }

    public boolean shouldRequireCompatibilityMode() {
        return shouldRequireCompatibilityMode;
    }

    public void setShouldRequireCompatibilityMode(boolean shouldRequireCompatibilityMode) {
        this.shouldRequireCompatibilityMode = shouldRequireCompatibilityMode;
    }

    public ApplicationPathResolver getApplicationPathResolver() {
        return applicationPathResolver;
    }

    public void setApplicationPathResolver(ApplicationPathResolver applicationPathResolver) {
        this.applicationPathResolver = applicationPathResolver;
    }
}
