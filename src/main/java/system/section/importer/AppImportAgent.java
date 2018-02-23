package system.section.importer;

import section.model.AppItem;
import section.model.Item;
import system.model.Application;
import system.model.ApplicationManager;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class analyzes the AppItem to make sure it is valid.
 */
public class AppImportAgent extends ImportAgent {
    private ApplicationManager appManager;

    // This map will hold the association between the executable filename ( for example idea.exe or Idea.app )
    // and the absolute path of the application. Used to convert the path of applications to local paths.
    private Map<String, String> filenamePathMap = new HashMap<>();

    // This set will contain the application executable paths
    private Set<String> applicationPathSet = new HashSet<>();

    protected AppImportAgent(SectionImporter importer) {
        super(importer);

        appManager = importer.getAppManager();

        // Populate the filenamePathMap and the applicationPathSet
        for (Application application : appManager.getApplicationList()) {
            // Load the executable file to extract the name
            File executable = new File(application.getExecutablePath());
            filenamePathMap.put(executable.getName(), executable.getAbsolutePath());

            // Load the applicationPathSet
            applicationPathSet.add(executable.getAbsolutePath());
        }
    }

    @Override
    public boolean analyzeItem(Item item) {
        // Cast the item
        AppItem appItem = (AppItem) item;

        // Check if the application is already present
        if (applicationPathSet.contains(appItem.getAppID())) {  // The app is already present in the PC, great.
            return true;
        }

        // Check if the application is present, but in another path
        File expected = new File(appItem.getAppID());
        String executableName = expected.getName();
        if (filenamePathMap.containsKey(executableName)) {  // App present, but in another path.
            // Replace the old path with the new one.
            appItem.setAppID(filenamePathMap.get(executableName));
            return true;
        }

        // Not found, mark it as invalid
        return false;
    }
}
