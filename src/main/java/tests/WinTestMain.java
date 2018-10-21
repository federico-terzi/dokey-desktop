package tests;

import com.sun.jna.Native;
import com.sun.jna.WString;
import system.ResourceUtils;
import system.applications.ApplicationManager;
import system.applications.MS.MSApplicationManager;
import system.applications.MS.ShellLinkResolver;
import system.applications.MS.WinExtractIconLib;
import system.applications.Window;
import system.bookmarks.ChromeBookmarkImportAgent;
import system.exceptions.UnsupportedOperatingSystemException;
import system.keyboard.bindings.WinKeyboardLib;
import system.startup.MSStartupManager;
import system.startup.StartupManager;
import system.storage.StorageManager;
import utils.OSValidator;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class WinTestMain {
    public static void main(String[] args) throws IOException {
        // Load native libs directory
        File nativeLibsDirectory = ResourceUtils.getResource("/"+OSValidator.TAG+ "/nativelibs");
        if (nativeLibsDirectory != null) {
            System.setProperty("jna.library.path", nativeLibsDirectory.getAbsolutePath());
        }

        ApplicationManager applicationManager = new MSApplicationManager(StorageManager.getDefault(), new MSStartupManager(StorageManager.getDefault()));
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
                for (Window win : windows) {
                    System.out.println(win);
                }
            }
        });
//
//        System.out.println(WinKeyboardLib.INSTANCE.forceDisableCapsLock());

        System.exit(0);
    }
}
