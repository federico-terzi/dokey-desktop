package app;

import app.editor.stages.EditorStage;
import app.editor.stages.ShortcutDialogStage;
import app.stages.InitializationStage;
import engine.EngineServer;
import engine.EngineWorker;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import net.discovery.ServerDiscoveryDaemon;
import net.model.DeviceInfo;
import net.model.ServerInfo;
import system.ApplicationManagerFactory;
import system.ApplicationSwitchDaemon;
import system.SectionManager;
import system.SystemInfoManager;
import system.adb.ADBManager;
import system.model.ApplicationManager;

import java.io.File;
import java.io.IOException;
import java.util.Timer;

public class MainApp extends Application implements EngineWorker.OnDeviceConnectionListener, ADBManager.OnUSBDeviceConnectedListener {
    // a timer allowing the tray icon to provide a periodic notification event.
    private Timer notificationTimer = new Timer();

    private TrayIconManager trayIconManager = new TrayIconManager();
    private ApplicationManager appManager;
    private ApplicationSwitchDaemon applicationSwitchDaemon;
    private ServerDiscoveryDaemon serverDiscoveryDaemon;
    private EngineServer engineServer;
    private ADBManager adbManager;

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

        // Initialize the discovery daemon
        ServerInfo serverInfo = SystemInfoManager.getServerInfo(EngineServer.SERVER_PORT);
        serverDiscoveryDaemon = new ServerDiscoveryDaemon(serverInfo);

        // Initialize the ADB manager
        adbManager = new ADBManager(this, serverInfo);

        // Initialize the sections
        SectionManager sectionManager = new SectionManager();
        sectionManager.getLaunchpadSection();

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

                        if (iconPath != null) {
                            // Get the icon file
                            File iconImage = new File(iconPath);

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    // Update the initialization stage
                                    initializationStage.updateAppStatus(applicationName, percentage, iconImage);
                                }
                            });
                        }else{
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    // Update the initialization stage
                                    initializationStage.updateAppStatus(applicationName, percentage, null);
                                }
                            });
                        }

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

        // Start the server discovery daemon
        serverDiscoveryDaemon.start();

        // Start the ADB daemon
        adbManager.startDaemon();

        engineServer = new EngineServer(appManager, applicationSwitchDaemon);
        engineServer.setDeviceConnectionListener(this);
        engineServer.start();

        // Update the tray icon status
        trayIconManager.setTrayIconStatus("Not connected");
        trayIconManager.setLoading(false);

        try {
            EditorStage editorStage = new EditorStage(appManager);
            editorStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop all the running services
     */
    private void stopAllServices() {
        applicationSwitchDaemon.setShouldStop(true);
        serverDiscoveryDaemon.stopDiscovery();
        engineServer.stopServer();
        adbManager.stopDaemon();
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


    /**
     * Called when a new USB device has been connected
     * @param deviceInfo
     */
    @Override
    public void onUSBDeviceConnected(DeviceInfo deviceInfo) {

    }

    /**
     * Called when a USB device has been disconnected.
     * @param deviceInfo
     */
    @Override
    public void onUSBDeviceDisconnected(DeviceInfo deviceInfo) {

    }
}
