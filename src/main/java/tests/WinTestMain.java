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
import system.storage.StorageManager;
import utils.OSValidator;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class WinTestMain {
    public static void main(String[] args) throws IOException {
//        ApplicationManager applicationManager = new MSApplicationManager(StorageManager.getDefault(), null);
//        applicationManager.loadApplications(new ApplicationManager.OnLoadApplicationsListener() {
//            @Override
//            public void onPreloadUpdate(String applicationName, int current, int total) {
//
//            }
//
//            @Override
//            public void onProgressUpdate(String applicationName, String iconPath, int current, int total) {
//
//            }
//
//            @Override
//            public void onApplicationsLoaded() {
//                List<Window> windows = applicationManager.getWindowList();
//                System.out.println(windows);
//            }
//        });
        // Load native libs directory
        File nativeLibsDirectory = ResourceUtils.getResource("/"+OSValidator.TAG+ "/nativelibs");
        if (nativeLibsDirectory != null) {
            System.setProperty("jna.library.path", nativeLibsDirectory.getAbsolutePath());
        }

        System.out.println(WinKeyboardLib.INSTANCE.forceDisableCapsLock());

        System.exit(0);
    }
}
