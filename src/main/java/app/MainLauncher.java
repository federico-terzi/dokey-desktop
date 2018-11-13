package app;

import com.sun.jna.Native;
import system.ResourceUtils;
import utils.OSValidator;

import java.io.File;

/**
 * Start the application as AWT as a workaround to hide the Dock icon from mac os
 */
public class MainLauncher {
    public static void main(String args[]) {
        // If mac, hide the dock icon
        if (OSValidator.isMac()) {
            System.setProperty("apple.awt.application.name", "Dokey");
            System.setProperty("apple.awt.UIElement", "true");

            System.setProperty("jna.encoding", "UTF8");
        }

        // Load native libs directory
        File nativeLibsDirectory = ResourceUtils.getResource("/"+OSValidator.TAG+ "/nativelibs");
        if (nativeLibsDirectory != null) {
            System.setProperty("jna.library.path", nativeLibsDirectory.getAbsolutePath());
        }

        // Start the app as awt
        java.awt.Toolkit.getDefaultToolkit();

        // Launch the actual app
        MainApp.main(args);
    }
}
