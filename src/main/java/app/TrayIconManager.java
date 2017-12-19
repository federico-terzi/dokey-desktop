package app;

import app.editor.stages.EditorStage;
import javafx.application.Platform;
import system.model.ApplicationManager;
import utils.OSValidator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Used to manage the system tray icon
 */
public class TrayIconManager {
    // Tray icon filename IMAGE CODES
    public static final String TRAY_ICON_FILENAME_LOADING = "circle.png";
    public static final String TRAY_ICON_FILENAME_CONNECTED = "icon.png";
    public static final String TRAY_ICON_FILENAME_READY = "icon-gray.png";

    // Tray icon tooltip app name
    private static final String TRAY_ICON_APPNAME = "Remote Key";

    private static long ROTATION_INTERVAL = 500;  // Interval between rotations in milliseconds
    private static int ROTATION_DEGREE = 45;
    private int currentRotationAngle = 0;

    // Tray icon object
    private java.awt.TrayIcon trayIcon;

    // The timer used for the rotation animation
    private Timer rotationTimer = new Timer();

    private boolean isLoading = true;  // If true, set the tray icon as rotating

    private int trayIconWidth;  // Internal usage

    /**
     * Initialize the tray icon
     */
    public void initialize(OnTrayActionListener listener) {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                System.exit(0);
            }

            File iconFile = new File(TrayIconManager.class.getResource("/assets/" + TRAY_ICON_FILENAME_CONNECTED).getFile());

            // set up a system tray icon.
            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();

            // Set the tray image
            BufferedImage image = ImageIO.read(iconFile);
            trayIcon = new java.awt.TrayIcon(image);
            trayIconWidth = trayIcon.getSize().width;

            // Set the trayicon as loading
            setLoading(true);

            setTrayIconStatus("Initializing...");

            // if the user double-clicks on the tray icon, show the main app stage.
            trayIcon.addActionListener(event -> Platform.runLater(() -> System.out.println("double click!")));

            // if the user selects the default menu item (which includes the app name),
            // show the main app stage.
            java.awt.MenuItem openEditor = new java.awt.MenuItem("Open Editor");
            openEditor.addActionListener(event -> Platform.runLater(() -> {
                if (listener != null) {
                    listener.onEditorOpenRequest();
                }
            }));

            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openEditor.setFont(boldFont);

            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(event -> {
                //notificationTimer.cancel();
                tray.remove(trayIcon);
                System.exit(0);
            });

            // setup the popup menu for the application.
            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openEditor);
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

    public interface OnTrayActionListener {
        void onEditorOpenRequest();
    }

    /**
     * Set the status in the tray icon label
     *
     * @param status the status text
     */
    public void setTrayIconStatus(String status) {
        trayIcon.setToolTip(TRAY_ICON_APPNAME + "\n" + status);
    }

    /**
     * Set a new image in the tray icon.
     *
     * @param imageCode one of the IMAGE CODEs defined by this class.
     */
    public void setTrayIcon(String imageCode) {
        // Get the icon file
        File iconFile = getIconFile(imageCode);

        // Set the tray image and scale it with antialiasing
        BufferedImage image = null;
        try {
            image = ImageIO.read(iconFile);

            // If the current rotation angle is different from zero, rotate the image
            if (currentRotationAngle != 0) {
                image = rotate(image, currentRotationAngle);
            }
            trayIcon.setImage(image.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        if (loading) {
            rotationTimer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            javax.swing.SwingUtilities.invokeLater(() ->
                                    {
                                        if (isLoading) {
                                            currentRotationAngle += ROTATION_DEGREE;
                                            setTrayIcon(TRAY_ICON_FILENAME_LOADING);
                                        }
                                    }
                            );
                        }
                    },
                    0,
                    ROTATION_INTERVAL
            );
        } else {
            rotationTimer.cancel();
            currentRotationAngle = 0;

            // Change the icon to ready with a delay
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    javax.swing.SwingUtilities.invokeLater(() ->
                            {
                                setTrayIcon(TRAY_ICON_FILENAME_READY);
                            }
                    );
                }
            }).start();
        }
        isLoading = loading;
    }

    /**
     * Rotate the given image
     *
     * @param imgOld
     * @param deg
     * @return
     */
    public static BufferedImage rotate(BufferedImage imgOld, int deg) {                                                 //parameter same as method above

        BufferedImage imgNew = new BufferedImage(imgOld.getWidth(), imgOld.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);              //create new buffered image
        Graphics2D g = (Graphics2D) imgNew.getGraphics();                                                               //create new graphics
        g.rotate(Math.toRadians(deg), imgOld.getWidth() / 2, imgOld.getHeight() / 2);                                    //configure rotation
        g.drawImage(imgOld, 0, 0, null);                                                                                //draw rotated image
        return imgNew;                                                                                                  //return rotated image
    }

    /**
     * Get the correct icon file based on the operating system
     * @return the correct icon file
     */
    public static File getIconFile(String imageCode) {
        String osFolder = "";
        if (OSValidator.isWindows()) {
            osFolder = "win/";
        }else if (OSValidator.isMac()) {
            osFolder = "mac/";
        }
        return new File(TrayIconManager.class.getResource("/assets/" + osFolder + imageCode).getFile());
    }
}
