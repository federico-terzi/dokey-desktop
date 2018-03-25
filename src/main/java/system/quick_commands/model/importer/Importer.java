package system.quick_commands.model.importer;

import system.ApplicationPathResolver;

/**
 * This class should give the ImportAgents access to resources like the ApplicationManager.
 */
public abstract class Importer {
    protected ApplicationPathResolver applicationPathResolver;

    protected Importer(ApplicationPathResolver applicationPathResolver) {
        this.applicationPathResolver = applicationPathResolver;
    }

    public ApplicationPathResolver getApplicationPathResolver() {
        return applicationPathResolver;
    }

    public void setApplicationPathResolver(ApplicationPathResolver applicationPathResolver) {
        this.applicationPathResolver = applicationPathResolver;
    }
}
