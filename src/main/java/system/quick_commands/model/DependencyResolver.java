package system.quick_commands.model;

import system.WebLinkResolver;
import system.model.ApplicationManager;

/**
 * This interface is used to inject all the needed managers to the quick actions
 */
public interface DependencyResolver {
    ApplicationManager getApplicationManager();
    WebLinkResolver getWebLinkResolver();
}
