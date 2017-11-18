package app;

import javafx.application.Platform;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Used to manage the system tray icon
 */
public class TrayIconManager {
    // Tray icon filename in the assets folder
    private static final String TRAY_ICON_FILENAME = "icon.png";

    // Tray icon tooltip app name
    private static final String TRAY_ICON_APPNAME = "Remote Key";

    // Tray icon object
    private java.awt.TrayIcon trayIcon;

    /**
     * Initialize the tray icon
     */
    public void initialize() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                System.exit(0);
            }

            File iconFile = new File(TrayIconManager.class.getResource("/assets/"+TRAY_ICON_FILENAME).getFile());

            // set up a system tray icon.
            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();

            // Set the tray image and scale it with antialiasing
            BufferedImage image = ImageIO.read(iconFile);
            int trayIconWidth = new java.awt.TrayIcon(image).getSize().width;
            trayIcon = new java.awt.TrayIcon(image.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH));

            setTrayIconStatus("Initializing...");

            // if the user double-clicks on the tray icon, show the main app stage.
            trayIcon.addActionListener(event -> Platform.runLater( () -> System.out.println("double click!")));

            // if the user selects the default menu item (which includes the app name),
            // show the main app stage.
            java.awt.MenuItem openItem = new java.awt.MenuItem("hello, world");
            openItem.addActionListener(event -> Platform.runLater(() -> System.out.println("menu click!")));

            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openItem.setFont(boldFont);

            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(event -> {
                //notificationTimer.cancel();
                Platform.exit();
                tray.remove(trayIcon);
            });

            // setup the popup menu for the application.
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            // add the application tray icon to the system tray.
            tray.add(trayIcon);
        } catch (java.awt.AWTException | IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }

    /**
     * Set the status in the tray icon label
     * @param status the status text
     */
    public void setTrayIconStatus(String status) {
        trayIcon.setToolTip(TRAY_ICON_APPNAME +"\n" +status);
    }
}
