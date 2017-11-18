package app;

import app.UIControllers.InitializationController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sun.applet.Main;
import system.ApplicationManagerFactory;
import system.model.ApplicationManager;
import system.model.Window;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainApp extends Application {

    // Tray icon filename in the assets folder
    private static final String TRAY_ICON_FILENAME = "icon.png";

    // a timer allowing the tray icon to provide a periodic notification event.
    private Timer notificationTimer = new Timer();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Set the MODENA theme
        setUserAgentStylesheet(STYLESHEET_MODENA);

        // instructs the javafx system not to exit implicitly when the last application window is shut.
        Platform.setImplicitExit(false);

        // sets up the tray icon (using awt code run on the swing thread).
        javax.swing.SwingUtilities.invokeLater(this::addAppToTray);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/initialization.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 400, 275));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();

        InitializationController controller = (InitializationController) fxmlLoader.getController();

        controller.setAppNameLabel("yolo");

        ApplicationManager wm = ApplicationManagerFactory.getInstance();

        //Task for computing the Panels:
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                wm.loadApplications(new ApplicationManager.OnLoadApplicationsListener() {
                    @Override
                    public void onProgressUpdate(String applicationName, String iconPath, int current, int total) {
                        System.out.println("Loading: "+applicationName+" "+current+"/"+total);
                        double percentage = (current / (double) total);
                        File iconImage = new File(iconPath);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                controller.setAppNameLabel(applicationName);
                                controller.setAppProgressBar(percentage);
                                controller.setAppImageFile(iconImage);
                            }
                        });
                    }

                    @Override
                    public void onApplicationsLoaded() {
                        System.out.println("loaded!");
                    }
                });
                return null;
            }
        };

        new Thread(task).start();
    }

    /**
     * Sets up a system tray icon for the application.
     */
    private void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                Platform.exit();
            }

            File iconFile = new File(MainApp.class.getResource("/assets/"+TRAY_ICON_FILENAME).getFile());

            // set up a system tray icon.
            java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();

            java.awt.Image image = ImageIO.read(iconFile);
            java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image);
            trayIcon.setImageAutoSize(true);

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

            // create a timer which periodically displays a notification message.
            notificationTimer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            javax.swing.SwingUtilities.invokeLater(() ->
                                    trayIcon.displayMessage(
                                            "hello",
                                            "The time is now ",
                                            java.awt.TrayIcon.MessageType.INFO
                                    )
                            );
                        }
                    },
                    5_000,
                    60_000
            );

            // add the application tray icon to the system tray.
            tray.add(trayIcon);
        } catch (java.awt.AWTException | IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }
}
