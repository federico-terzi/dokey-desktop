package system;

import system.applications.Application;
import system.applications.ApplicationManager;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to determine the position of an imported app into the local filesystem.
 * It searches through the local applications to find if the requested app is available in the PC.
 */
public class ApplicationPathResolver {
    private ApplicationManager appManager;

    // This map will hold the association between the application global id( for example idea.exe or Idea.app )
    // and the local id of the application. Used to convert the path of applications to local paths.
    private Map<String, String> globalIdLocalIdMap = new HashMap<>();

    // This set will contain the application ids
    private Set<String> applicationIdSet = new HashSet<>();

    private boolean isLoaded = false;

    public ApplicationPathResolver(ApplicationManager appManager) {
        this.appManager = appManager;
    }

    /**
     * Load the application data to search efficiently later.
     */
    public void load() {
        // Reset the maps
        globalIdLocalIdMap = new HashMap<>();
        applicationIdSet = new HashSet<>();

        // Populate the globalIdLocalIdMap and the applicationIdSet
        for (Application application : appManager.getApplicationList()) {
            globalIdLocalIdMap.put(application.getGlobalId(), application.getId());

            // Load the applicationIdSet
            applicationIdSet.add(application.getId());
        }

        isLoaded = true;
    }

    /**
     * Searches in the PC to find the application.
     * NOTE: you must call load beforehand.
     * @param applicationId the requested application id.
     * @return the application id if found, null otherwise.
     */
    public String searchApp(String applicationId) {
        if (!isLoaded)
            throw new RuntimeException("You must call the load() function before searching for an app.");

        // Check if the application is already present
        if (applicationIdSet.contains(applicationId)) {  // The app is already present in the PC, great.
            return applicationId;
        }

        // TODO: fix with new application id model
        /*
        // Check if the application is present, but in another path
        File expected = new File(applicationId);
        String executableName = expected.getName();
        if (globalIdLocalIdMap.containsKey(executableName)) {  // App present, but in another path.
            // Return the new path
            return globalIdLocalIdMap.get(executableName);
        }
        */

        // Not found
        return null;
    }
}
