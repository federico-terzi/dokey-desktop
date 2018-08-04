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

    // This map will hold the association between the executable filename ( for example idea.exe or Idea.app )
    // and the absolute path of the application. Used to convert the path of applications to local paths.
    private Map<String, String> filenamePathMap = new HashMap<>();

    // This set will contain the application executable paths
    private Set<String> applicationPathSet = new HashSet<>();

    private boolean isLoaded = false;

    public ApplicationPathResolver(ApplicationManager appManager) {
        this.appManager = appManager;
    }

    /**
     * Load the application data to search efficiently later.
     */
    public void load() {
        // Populate the filenamePathMap and the applicationPathSet
        for (Application application : appManager.getApplicationList()) {
            // Load the executable file to extract the name
            File executable = new File(application.getExecutablePath());
            filenamePathMap.put(executable.getName(), executable.getAbsolutePath());

            // Load the applicationPathSet
            applicationPathSet.add(executable.getAbsolutePath());
        }

        isLoaded = true;
    }

    /**
     * Searches in the PC to find the application.
     * NOTE: you must call load beforehand.
     * @param executablePath the requested application path.
     * @return the new executable path if found, null otherwise.
     */
    public String searchApp(String executablePath) {
        if (!isLoaded)
            throw new RuntimeException("You must call the load() function before searching for an app.");

        // Check if the application is already present
        if (applicationPathSet.contains(executablePath)) {  // The app is already present in the PC, great.
            return executablePath;
        }

        // Check if the application is present, but in another path
        File expected = new File(executablePath);
        String executableName = expected.getName();
        if (filenamePathMap.containsKey(executableName)) {  // App present, but in another path.
            // Return the new path
            return filenamePathMap.get(executableName);
        }

        // Not found
        return null;
    }
}
