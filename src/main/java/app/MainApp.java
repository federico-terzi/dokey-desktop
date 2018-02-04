package app;

import app.editor.stages.EditorStage;
import app.stages.SettingsStage;
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
import section.model.Section;
import system.*;
import system.adb.ADBManager;
import system.model.ApplicationManager;

import java.io.IOException;
import java.io.Serializable;
import java.util.Timer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp extends Application implements EngineWorker.OnDeviceConnectionListener, ADBManager.OnUSBDeviceConnectedListener, TrayIconManager.OnTrayActionListener {
    // a timer allowing the tray icon to provide a periodic notification event.
    private Timer notificationTimer = new Timer();

    private boolean isEditorOpen = false;
    private boolean isSettingsOpen = false;

    private TrayIconManager trayIconManager = new TrayIconManager();
    private ApplicationManager appManager;
    private ApplicationSwitchDaemon applicationSwitchDaemon;
    private ServerDiscoveryDaemon serverDiscoveryDaemon;
    private EngineServer engineServer;
    private ADBManager adbManager;

    private Stage primaryStage;
    private InitializationStage initializationStage;

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    public static void main(String[] args) {
        // Set the logging level
        Level level = Level.INFO;
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(level);
        LOG.setUseParentHandlers( false );
        LOG.setLevel(level);
        LOG.addHandler(consoleHandler);

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
        javax.swing.SwingUtilities.invokeLater(() -> trayIconManager.initialize(MainApp.this));

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
                    public void onPreloadUpdate(String applicationName, int current, int total) {
                        LOG.fine("Preload: " + applicationName + " " + current + "/" + total);

                        // Calculate the percentage
                        double percentage = (current / (double) total) * 0.5;

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                // Update the initialization stage
                                initializationStage.updateAppStatus(applicationName, percentage);
                            }
                        });
                    }

                    @Override
                    public void onProgressUpdate(String applicationName, String iconPath, int current, int total) {
                        LOG.fine("Loading: " + applicationName + " " + current + "/" + total);
                        // Calculate the percentage
                        double percentage = (current / (double) total) * 0.5 + 0.5;

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                // Update the initialization stage
                                initializationStage.updateAppStatus(applicationName, percentage);
                            }
                        });

                    }

                    @Override
                    public void onApplicationsLoaded() {
                        LOG.fine("loaded!");
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

        openEditor();
        //openSettings();
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

    private void openEditor() {
        if (isEditorOpen) {
            return;
        }

        try {
            isEditorOpen = true;
            EditorStage editorStage = new EditorStage(appManager, new EditorStage.OnEditorEventListener() {
                @Override
                public void onEditorClosed() {
                    isEditorOpen = false;
                }
            });
            editorStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEditorOpenRequest() {
        openEditor();
    }

    private void openSettings() {
        if (isSettingsOpen) {
            return;
        }

        try {
            isSettingsOpen = true;
            SettingsStage settingsStage = new SettingsStage(appManager, new SettingsStage.OnSettingsCloseListener() {
                @Override
                public void onSettingsClosed() {
                    isSettingsOpen = false;
                }
            });
            settingsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSettingsOpenRequest() {
        openSettings();
    }

    /**
     * Called when a device connects to the server.
     *
     * @param deviceID the string ID of the device
     * @param deviceName the name of the device
     */
    @Override
    public void onDeviceConnected(String deviceID, String deviceName) {
        LOG.info("Connected to: "+deviceID);

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
        LOG.info("Disconnected from: "+deviceID);

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
