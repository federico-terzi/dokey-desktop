package tests;

import system.applications.ApplicationManager;
import system.applications.MS.MSApplicationManager;
import system.applications.Window;
import system.bookmarks.ChromeBookmarkImportAgent;
import system.exceptions.UnsupportedOperatingSystemException;
import system.storage.StorageManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class WinTestMain {
    public static void main(String[] args) throws IOException {
        ApplicationManager applicationManager = new MSApplicationManager(StorageManager.getDefault(), null);
        applicationManager.loadApplications(new ApplicationManager.OnLoadApplicationsListener() {
            @Override
            public void onPreloadUpdate(String applicationName, int current, int total) {

            }

            @Override
            public void onProgressUpdate(String applicationName, String iconPath, int current, int total) {

            }

            @Override
            public void onApplicationsLoaded() {
                List<Window> windows = applicationManager.getWindowList();
                System.out.println(windows);
            }
        });

        System.exit(0);
    }
}
