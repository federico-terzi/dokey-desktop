package functional_tests;

import system.bookmarks.ChromeBookmarkImportAgent;
import system.exceptions.UnsupportedOperatingSystemException;
import system.storage.StorageManager;

import java.io.IOException;

public class WinTestMain {
    public static void main(String[] args) throws IOException {
        try {
            ChromeBookmarkImportAgent chromeBookmarkImportAgent = new ChromeBookmarkImportAgent();
            chromeBookmarkImportAgent.importBookmarks();
        } catch (UnsupportedOperatingSystemException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}
