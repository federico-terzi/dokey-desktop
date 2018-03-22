package app;

import utils.OSValidator;

/**
 * Start the application as AWT as a workaround to hide the Dock icon from mac os
 */
public class MainLauncher {
    public static void main(String args[]) {
        // If mac, hide the dock icon
        if (OSValidator.isMac()) {
            System.setProperty("apple.awt.application.name", "Dokey");
            System.setProperty("apple.awt.UIElement", "true");
        }

        // Start the app as awt
        java.awt.Toolkit.getDefaultToolkit();

        // Launch the actual app
        MainApp.main(args);
    }
}
