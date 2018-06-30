package tests;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.W32APIOptions;
import javafx.scene.image.Image;
import system.MAC.MACUtils;
import system.MS.MSApplicationManager;
import system.ResourceUtils;
import system.bookmarks.BookmarkImportAgent;
import system.bookmarks.ChromeBookmarkImportAgent;
import system.exceptions.UnsupportedOperatingSystemException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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
