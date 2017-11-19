package app;

import engine.EngineServer;
import engine.EngineWorker;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import system.ApplicationManagerFactory;
import system.ApplicationSwitchDaemon;
import system.model.ApplicationManager;

import java.io.File;
import java.io.IOException;
import java.util.Timer;

public class MainApp extends Application implements EngineWorker.OnDeviceConnectionListener {
    // a timer allowing the tray icon to provide a periodic notification event.
    private Timer notificationTimer = new Timer();

    private TrayIconManager trayIconManager = new TrayIconManager();
    private ApplicationManager appManager;
    private ApplicationSwitchDaemon applicationSwitchDaemon;

    private Stage primaryStage;
    private InitializationStage initializationStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;

        // Set the MODENA theme
        setUserAgentStylesheet(STYLESHEET_MODENA);

        // instructs the javafx system not to exit implicitly when the last application window is shut.
        Platform.setImplicitExit(false);

        // sets up the tray icon
        javax.swing.SwingUtilities.invokeLater(trayIconManager::initialize);

        // Initialize the application manager
        appManager = ApplicationManagerFactory.getInstance();

        // Initialize the application switch daemon
        applicationSwitchDaemon = new ApplicationSwitchDaemon(appManager);

        // load the applications
        loadApplications();
    }

    /**
     * Start the application loading process
     */
    private void loadApplications() throws IOException {
        // Create and show the initialization stage
        initializationStage = new InitializationStage();
        initializationStage.show();

        // Task for loading the applications
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                appManager.loadApplications(new ApplicationManager.OnLoadApplicationsListener() {
                    @Override
                    public void onProgressUpdate(String applicationName, String iconPath, int current, int total) {
                        System.out.println("Loading: " + applicationName + " " + current + "/" + total);
                        // Calculate the percentage
                        double percentage = (current / (double) total);
                        // Get the icon file
                        File iconImage = new File(iconPath);

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                // Update the initialization stage
                                initializationStage.updateAppStatus(applicationName, percentage, iconImage);
                            }
                        });
                    }

                    @Override
                    public void onApplicationsLoaded() {
                        System.out.println("loaded!");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                MainApp.this.onApplicationsLoaded();
                            }
                        });
                    }
                });
                return null;
            }
        };

        new Thread(task).start();
    }

    /**
     * Called when all applications are loaded.
     */
    private void onApplicationsLoaded() {
        // Hide the initialization
        if (initializationStage != null) {
            initializationStage.hide();
        }

        // Update the tray icon status
        trayIconManager.setTrayIconStatus("Starting service...");

        // Start the engine server
        startEngineServer();
    }

    /**
     * Start the engine server in another thread
     */
    private void startEngineServer() {
        // Start the application switch daemon
        applicationSwitchDaemon.start();

        EngineServer engineServer = new EngineServer(appManager, applicationSwitchDaemon);
        engineServer.setDeviceConnectionListener(this);
        engineServer.start();

        // Update the tray icon status
        trayIconManager.setTrayIconStatus("Not connected");
        trayIconManager.setLoading(false);
        trayIconManager.setTrayIcon(TrayIconManager.TRAY_ICON_FILENAME_READY);
    }

    /**
     * Called when a device connects to the server.
     *
     * @param deviceID the string ID of the device
     * @param deviceName the name of the device
     */
    @Override
    public void onDeviceConnected(String deviceID, String deviceName) {
        System.out.println("Connected to: "+deviceID);

        // Set the tray icon as running
        trayIconManager.setTrayIcon(TrayIconManager.TRAY_ICON_FILENAME_CONNECTED);
        trayIconManager.setTrayIconStatus("Connected");
    }

    /**
     * Called when a device disconnects from the server.
     *
     * @param deviceID the string ID of the device
     */
    @Override
    public void onDeviceDisconnected(String deviceID) {
        System.out.println("Disconnected from: "+deviceID);

        // Set the tray icon as ready
        trayIconManager.setTrayIcon(TrayIconManager.TRAY_ICON_FILENAME_READY);
        trayIconManager.setTrayIconStatus("Not connected");
    }

}
